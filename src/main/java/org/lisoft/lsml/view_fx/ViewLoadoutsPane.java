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

import org.lisoft.lsml.messages.GarageMessage;
import org.lisoft.lsml.messages.Message;
import org.lisoft.lsml.messages.MessageReceiver;
import org.lisoft.lsml.model.garage.GaragePath;
import org.lisoft.lsml.model.loadout.Loadout;
import org.lisoft.lsml.view_fx.util.FxControlUtils;

import javafx.application.Platform;
import javafx.beans.property.Property;
import javafx.fxml.FXML;
import javafx.scene.control.ListView;
import javafx.scene.control.SelectionMode;
import javafx.scene.control.TreeItem;
import javafx.scene.control.TreeView;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.BorderPane;

/**
 * This container will show the {@link Loadout}s stored in the currently open garage.
 *
 * @author Emily Björk
 */
public class ViewLoadoutsPane extends BorderPane implements MessageReceiver {
    private final ApplicationModel model;
    @FXML
    private ListView<Loadout> loadout_pills;
    @FXML
    private TreeView<GaragePath<Loadout>> loadout_tree;

    /**
     * Creates a new {@link ViewLoadoutsPane} that will show the garage contents.
     *
     * @param aModel
     *            The {@link ApplicationModel} that contains the application state.
     */
    public ViewLoadoutsPane(ApplicationModel aModel) {
        FxControlUtils.loadFxmlControl(this);
        model = aModel;
        model.xBar.attach(this);
        refresh();

        final Property<String> garageFile = model.settings.getString(Settings.CORE_GARAGE_FILE);
        garageFile.addListener((aObs, aOld, aNew) -> {
            Platform.runLater(() -> {
                refresh();
            });
        });
    }

    @FXML
    public void addGarageFolder() {
        final TreeItem<GaragePath<Loadout>> selectedItem = loadout_tree.getSelectionModel().getSelectedItem();
        if (null == selectedItem) {
            final GaragePath<Loadout> root = loadout_tree.getRoot().getValue();
            GlobalGarage.addFolder(root, this, model.cmdStack, model.xBar);
        }
        else {
            GaragePath<Loadout> item = selectedItem.getValue();
            if (item.isLeaf()) {
                item = item.getParent();
            }

            GlobalGarage.addFolder(item, this, model.cmdStack, model.xBar);
        }
    }

    @FXML
    public void garageTreeKeyRelease(KeyEvent aEvent) {
        if (aEvent.getCode() == KeyCode.DELETE) {
            removeSelectedGarageFolder();
            aEvent.consume();
        }
    }

    @FXML
    public void loadoutPillKeyRelease(KeyEvent aEvent) {
        if (aEvent.getCode() == KeyCode.DELETE) {
            deleteSelectedLoadout();
            aEvent.consume();
        }
    }

    @Override
    public void receive(Message aMsg) {
        if (aMsg instanceof GarageMessage) {
            final GarageMessage<?> msg = (GarageMessage<?>) aMsg;

            final TreeItem<GaragePath<Loadout>> selectedItem = loadout_tree.getSelectionModel().getSelectedItem();
            if (null != selectedItem) {
                msg.value.ifPresent(aValue -> {
                    if (aValue instanceof Loadout) {
                        updateAllLoadoutPills(selectedItem.getValue());
                    }
                });
            }
        }
    }

    public void refresh() {
        FxControlUtils.setupGarageTree(loadout_tree, model.globalGarage.getGarage().getLoadoutRoot(), model.xBar,
                model.cmdStack, false);
        loadout_tree.getSelectionModel().selectedItemProperty().addListener((aObservable, aOld, aNew) -> {
            updateAllLoadoutPills(aNew.getValue());
        });
        loadout_pills.setCellFactory(aView -> new LoadoutPillCell(model.xBar, model.cmdStack, loadout_tree, aView));
        loadout_pills.getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE);
    }

    @FXML
    public void removeSelectedGarageFolder() {
        final TreeItem<GaragePath<Loadout>> selectedItem = loadout_tree.getSelectionModel().getSelectedItem();
        if (null == selectedItem) {
            return;
        }

        final GaragePath<Loadout> item = selectedItem.getValue();
        if (null == item) {
            return;
        }

        if (item.isLeaf()) {
            return;
        }

        GlobalGarage.remove(item, this, model.cmdStack, model.xBar);
    }

    /**
     * Deletes the currently selected loadout, if there is one. No-op otherwise.
     */
    private void deleteSelectedLoadout() {
        final TreeItem<GaragePath<Loadout>> parent = loadout_tree.getSelectionModel().getSelectedItem();
        final Loadout loadout = loadout_pills.getSelectionModel().getSelectedItem();
        if (parent != null && parent.getValue() != null && loadout != null) {
            final GaragePath<Loadout> parentPath = parent.getValue();
            final GaragePath<Loadout> path = new GaragePath<>(parentPath, loadout);
            GlobalGarage.remove(path, this, model.cmdStack, model.xBar);
        }
    }

    private void updateAllLoadoutPills(GaragePath<Loadout> aNew) {
        if (null != aNew) {
            loadout_pills.getItems().setAll(aNew.getTopDirectory().getValues());
        }
        else {
            loadout_pills.getItems().clear();
        }
    }
}
