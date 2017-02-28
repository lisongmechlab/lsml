/*
 * @formatter:off
 * Li Song Mechlab - A 'mech building tool for PGI's MechWarrior: Online.
 * Copyright (C) 2013  Emily Björk
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
package org.lisoft.lsml.view_fx;

import java.util.Collections;
import java.util.Set;
import java.util.Stack;
import java.util.stream.Collectors;

import org.lisoft.lsml.model.datacache.ChassisDB;
import org.lisoft.lsml.model.garage.Garage;
import org.lisoft.lsml.model.garage.GarageDirectory;
import org.lisoft.lsml.model.loadout.DefaultLoadoutFactory;
import org.lisoft.lsml.model.loadout.Loadout;
import org.lisoft.lsml.model.loadout.LoadoutFactory;
import org.lisoft.lsml.view_fx.util.FxControlUtils;
import org.lisoft.lsml.view_fx.util.FxTableUtils;

import javafx.beans.value.ObservableStringValue;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.fxml.FXML;
import javafx.scene.control.TableRow;
import javafx.scene.control.TableView;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.BorderPane;

/**
 * This pane shows the search results based on a filter text.
 *
 * @author Emily Björk
 */
public class SearchResultsPane extends BorderPane {
    /**
     * A empty loadout for all chassis in the database.
     */
    private final static Set<Loadout> ALL_EMPTY = Collections.unmodifiableSet(ChassisDB.lookupAll().stream().map(c -> {
        final Loadout l = DefaultLoadoutFactory.instance.produceDefault(c, ApplicationModel.model.settings);
        l.setName("[New] " + l.getName());
        return l;
    }).collect(Collectors.toSet()));

    @FXML
    private TableView<Loadout> results;
    private final Runnable onClose;
    private final LoadoutFactory loadoutFactory = DefaultLoadoutFactory.instance;

    /**
     * Creates a new search result pane.
     *
     * @param aFilterString
     *            An {@link ObservableStringValue} of the filter string to use.
     * @param aGarage
     *            A {@link Garage} to get contents to filter from.
     * @param aOnClose
     *            A callback to call when this window is closed.
     */
    public SearchResultsPane(ObservableStringValue aFilterString, Garage aGarage, Runnable aOnClose) {
        FxControlUtils.loadFxmlControl(this);
        onClose = aOnClose;

        final ObservableList<Loadout> data = getAllResults(aGarage);

        final FilteredList<Loadout> filteredList = new FilteredList<>(data);
        results.setItems(filteredList);
        results.setRowFactory(tv -> {
            final TableRow<Loadout> row = new TableRow<>();
            row.setOnMouseClicked(event -> {
                if (FxControlUtils.isDoubleClick(event) && !row.isEmpty()) {
                    Loadout loadout = row.getItem();
                    if (null != loadout) {
                        // Only clone the proto loadouts so they don't get modified.
                        if (ALL_EMPTY.contains(loadout)) {
                            loadout = loadoutFactory.produceClone(loadout);
                        }
                        LiSongMechLab.openLoadout(ApplicationModel.model.xBar, loadout, getScene());
                    }
                }
            });
            return row;
        });
        FxTableUtils.setupChassisTable(results);

        aFilterString.addListener((aObs, aOld, aNew) -> {
            if (aNew != null && !aNew.isEmpty()) {
                filteredList.setPredicate(new SearchFilter(aNew));
            }
            else {
                filteredList.setPredicate(x -> false);
            }
        });
    }

    @FXML
    public void closeWindow() {
        onClose.run();
    }

    @FXML
    public void keyRelease(KeyEvent aEvent) {
        FxControlUtils.escapeWindow(aEvent, this, () -> closeWindow());
    }

    private ObservableList<Loadout> getAllResults(Garage aGarage) {
        final ObservableList<Loadout> data = FXCollections.observableArrayList(ALL_EMPTY);

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
}
