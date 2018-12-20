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

import org.lisoft.lsml.messages.*;
import org.lisoft.lsml.model.database.ChassisDB;
import org.lisoft.lsml.model.garage.*;
import org.lisoft.lsml.model.loadout.*;
import org.lisoft.lsml.model.search.SearchIndex;
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
    /**
     * A empty loadout for all chassis in the database.
     */
    private final static Set<Loadout> ALL_EMPTY;

    private final SearchIndex searchIndex = new SearchIndex();

    static {
        // TODO: Figure out a way to avoid calling loadoutFactory() here
        final LoadoutFactory factory = LiSongMechLab.getFXApplication().loadoutFactory();
        final Settings settings = LiSongMechLab.getFXApplication().settings();

        ALL_EMPTY = Collections.unmodifiableSet(ChassisDB.lookupAll().stream().map(c -> {
            final Loadout l = factory.produceDefault(c, settings);
            l.setName("[New] " + l.getName());
            return l;
        }).collect(Collectors.toSet()));
    }

    @FXML
    private TableView<Loadout> results;

    private final MessageXBar xBar;
    private final SimpleStringProperty filterString = new SimpleStringProperty();

    @Inject
    public SearchResultsPaneController(@Named("global") MessageXBar aXBar, GlobalGarage aGlobalGarage,
            LoadoutFactory aLoadoutFactory) {
        xBar = aXBar;
        xBar.attach(this);

        final ObservableList<Loadout> resultList = FXCollections.observableArrayList();
        results.setItems(resultList);
        rebuildIndex(aGlobalGarage);
        results.setRowFactory(tv -> {
            final TableRow<Loadout> row = new TableRow<>();
            row.setOnMouseClicked(event -> {
                if (FxControlUtils.isDoubleClick(event) && !row.isEmpty()) {
                    Loadout loadout = row.getItem();
                    if (null != loadout) {
                        // Only clone the proto loadouts so they don't get
                        // modified.
                        if (ALL_EMPTY.contains(loadout)) {
                            loadout = aLoadoutFactory.produceClone(loadout);
                        }
                        xBar.post(new ApplicationMessage(loadout, ApplicationMessage.Type.OPEN_LOADOUT, root));
                    }
                }
            });
            return row;
        });
        FxTableUtils.setupChassisTable(results);

        filterString.addListener((aObs, aOld, aNew) -> resultList.setAll(searchIndex.query(aNew)));
    }

    private void rebuildIndex(GlobalGarage aGlobalGarage) {
        for (final Loadout document : getAllDocuments(aGlobalGarage.getGarage())) {
            searchIndex.merge(document);
        }
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

    /**
     * @return A {@link StringProperty} that holds the current search string.
     */
    public StringProperty searchStringProperty() {
        return filterString;
    }

    private Collection<Loadout> getAllDocuments(Garage aGarage) {
        final List<Loadout> data = new ArrayList<>(ALL_EMPTY);

        // Do a DFS to visit all garage trees.
        final Stack<GarageDirectory<Loadout>> fringe = new Stack<>();
        fringe.push(aGarage.getLoadoutRoot());
        while (!fringe.empty()) {
            final GarageDirectory<Loadout> current = fringe.pop();
            for (final GarageDirectory<Loadout> child : current.getDirectories()) {
                fringe.push(child);
            }
            data.addAll(current.getValues());
        }
        return data;
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

        }
    }
}
