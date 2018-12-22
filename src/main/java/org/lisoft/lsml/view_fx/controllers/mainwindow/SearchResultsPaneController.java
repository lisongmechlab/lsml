/*
 * @formatter:off
 * Li Song Mechlab - A 'mech building tool for PGI's MechWarrior: Online.
 * Copyright (C) 2013  Li Song
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
//@formatter:on
package org.lisoft.lsml.view_fx.controllers.mainwindow;

import java.util.*;
import java.util.stream.Collectors;

import javax.inject.*;

import org.lisoft.lsml.command.CmdGarageRename;
import org.lisoft.lsml.messages.*;
import org.lisoft.lsml.model.database.ChassisDB;
import org.lisoft.lsml.model.garage.GarageDirectory;
import org.lisoft.lsml.model.loadout.*;
import org.lisoft.lsml.model.search.SearchIndex;
import org.lisoft.lsml.util.CommandStack;
import org.lisoft.lsml.view_fx.*;
import org.lisoft.lsml.view_fx.controllers.AbstractFXController;
import org.lisoft.lsml.view_fx.util.*;

import javafx.beans.property.*;
import javafx.collections.*;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.input.KeyEvent;

/**
 * This pane shows the search results based on a filter text.
 *
 * @author Li Song
 */
public class SearchResultsPaneController extends AbstractFXController implements MessageReceiver {
    private final Set<Loadout> allEmptyLoadouts;
    private final SimpleStringProperty filterString = new SimpleStringProperty();
    private final GlobalGarage globalGarage;
    private final CommandStack globalStack;
    private final LoadoutFactory loadoutFactory;
    private final ObservableList<Loadout> resultList;

    @FXML
    private TableView<Loadout> results;

    private final SearchIndex searchIndex = new SearchIndex();
    private final MessageXBar xBar;

    @Inject
    public SearchResultsPaneController(@Named("global") MessageXBar aXBar, CommandStack aStack,
            GlobalGarage aGlobalGarage, LoadoutFactory aLoadoutFactory, Settings aSettings) {
        xBar = aXBar;
        xBar.attach(this);
        globalStack = aStack;
        loadoutFactory = aLoadoutFactory;
        globalGarage = aGlobalGarage;

        allEmptyLoadouts = Collections.unmodifiableSet(ChassisDB.lookupAll().stream().map(c -> {
            final Loadout l = loadoutFactory.produceDefault(c, aSettings);
            l.setName("[New] " + l.getName());
            return l;
        }).collect(Collectors.toSet()));

        buildIndex();
        FxTableUtils.setupChassisTable(results);

        resultList = FXCollections.observableArrayList();
        results.setItems(resultList);
        results.setRowFactory(tv -> makeSearchResultRow());
        filterString.addListener((aObs, aOld, aNew) -> refreshQuery(aNew));
    }

    @FXML
    public void closeWindow() {
        xBar.post(new ApplicationMessage(ApplicationMessage.Type.CLOSE_OVERLAY, root));
    }

    /**
     * This is necessary to allow ESC to close the overlay if one of the search results has focus.
     *
     * @param aEvent
     *            The event that triggered this call.
     */
    @FXML
    public void keyRelease(KeyEvent aEvent) {
        FxControlUtils.escapeWindow(aEvent, root, () -> closeWindow());
    }

    @Override
    public void receive(Message aMsg) {
        if (aMsg instanceof GarageMessage) {
            final GarageMessage<?> garageMessage = (GarageMessage<?>) aMsg;
            if (garageMessage.type == GarageMessageType.ADDED) {
                garageMessage.path.getValue().ifPresent(document -> {
                    if (document instanceof Loadout) {
                        searchIndex.merge((Loadout) document);
                    }
                });
            }
            else if (garageMessage.type == GarageMessageType.REMOVED) {
                garageMessage.path.getValue().ifPresent(document -> {
                    if (document instanceof Loadout) {
                        searchIndex.unmerge((Loadout) document);
                    }
                });
            }
            else {
                garageMessage.path.getValue().ifPresent(document -> {
                    if (document instanceof Loadout) {
                        searchIndex.update();
                    }
                });
            }
            if (!filterString.isEmpty().get()) {
                refreshQuery(filterString.get());
            }
        }
    }

    /**
     * @return A {@link StringProperty} that holds the current search string.
     */
    public StringProperty searchStringProperty() {
        return filterString;
    }

    private TableRow<Loadout> makeSearchResultRow() {
        final TableRow<Loadout> row = new TableRow<>();
        row.setOnMouseClicked(event -> {
            event.consume();
            if (row.isEmpty() && null != row.getItem()) {
                return;
            }
            Loadout loadout = row.getItem();

            if (FxControlUtils.isDoubleClick(event)) {
                if (allEmptyLoadouts.contains(loadout)) {
                    // Don't modify proto layouts
                    loadout = loadoutFactory.produceClone(loadout);
                }
                xBar.post(new ApplicationMessage(loadout, ApplicationMessage.Type.OPEN_LOADOUT, root));
            }
        });
        row.setContextMenu(showSearchResultContextMenu(row));
        return row;
    }

    private void buildIndex() {
        allEmptyLoadouts.forEach(l -> searchIndex.merge(l));

        final Stack<GarageDirectory<Loadout>> fringe = new Stack<>();
        fringe.push(globalGarage.getGarage().getLoadoutRoot());
        while (!fringe.empty()) {
            final GarageDirectory<Loadout> current = fringe.pop();
            for (final GarageDirectory<Loadout> child : current.getDirectories()) {
                fringe.push(child);
            }
            current.getValues().forEach(l -> searchIndex.merge(l));
        }
    }

    private boolean refreshQuery(String aNew) {
        return resultList.setAll(searchIndex.query(aNew));
    }

    private ContextMenu showSearchResultContextMenu(TableRow<Loadout> aRow) {
        final MenuItem rename = new MenuItem("Rename...");
        final MenuItem delete = new MenuItem("Delete...");
        final ContextMenu cm = new ContextMenu(rename, delete);
        cm.setOnShowing(e -> {
            if (aRow.isEmpty() && null != aRow.getItem()) {
                return;
            }
            final Loadout loadout = aRow.getItem();
            final boolean isProtoLayout = allEmptyLoadouts.contains(loadout);
            rename.setDisable(isProtoLayout);
            delete.setDisable(isProtoLayout);
            delete.setOnAction(event -> globalGarage.getGarage().getLoadoutRoot().find(loadout)
                    .ifPresent(p -> GlobalGarage.remove(p, root, globalStack, xBar)));
            rename.setOnAction(event -> {
                final TextInputDialog dialog = new TextInputDialog(loadout.getName());
                dialog.setTitle("Rename Loadout...");
                dialog.setHeaderText("Renaming loadout: " + loadout.getName());

                dialog.showAndWait().ifPresent(
                        result -> globalGarage.getGarage().getLoadoutRoot().find(loadout).ifPresent(p -> LiSongMechLab
                                .safeCommand(root, globalStack, new CmdGarageRename<>(xBar, p, result), xBar)));
            });
        });
        return cm;
    }
}
