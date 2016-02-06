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
package org.lisoft.lsml.view_fx;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.lisoft.lsml.messages.MessageXBar;
import org.lisoft.lsml.model.garage.Garage;
import org.lisoft.lsml.model.garage.GarageDirectory;
import org.lisoft.lsml.model.loadout.Loadout;
import org.lisoft.lsml.view_fx.util.FxmlHelpers;
import org.lisoft.lsml.view_fx.util.LoadoutDragHelper;
import org.lisoft.lsml.view_fx.util.LoadoutDragHelper.LoadoutDragData;

import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.scene.control.TreeItem;
import javafx.scene.control.TreeView;
import javafx.scene.input.Dragboard;
import javafx.scene.input.MouseButton;
import javafx.scene.input.TransferMode;

/**
 * @author Li Song
 */
public class LoadoutPillCell extends ListCell<Loadout> {

    private final LoadoutPill                        pill;
    private final TreeView<GarageDirectory<Loadout>> treeView;
    private final ListView<Loadout>                  listView;

    public LoadoutPillCell(Garage aGarage, MessageXBar aXBar, TreeView<GarageDirectory<Loadout>> aTreeView,
            ListView<Loadout> aListView) {
        pill = new LoadoutPill();
        treeView = aTreeView;
        listView = aListView;

        setOnMouseClicked(aEvent -> {
            if (aEvent.getButton() == MouseButton.PRIMARY && aEvent.getClickCount() >= 2) {
                LiSongMechLab.openLoadout(aXBar, getItem(), aGarage);
            }
        });

        setOnDragDetected(aEvent -> {
            getSafeItem().ifPresent(aLoadout -> {
                Dragboard dragboard = startDragAndDrop(TransferMode.COPY_OR_MOVE);
                TreeItem<GarageDirectory<Loadout>> treeItem = treeView.getSelectionModel().getSelectedItem();
                if (null != treeItem) {
                    List<String> path = FxmlHelpers.getTreePath(treeItem);
                    List<Loadout> selected = new ArrayList<>(listView.getSelectionModel().getSelectedItems());

                    LoadoutDragHelper.doDrag(dragboard, new LoadoutDragData<Loadout>(selected, path, Loadout.class));
                }
            });
            aEvent.consume();
        });
    }

    @Override
    protected void updateItem(Loadout aItem, boolean aEmpty) {
        super.updateItem(aItem, aEmpty);
        if (aItem != null && !aEmpty) {
            setText(null);
            pill.setLoadout(aItem);
            setGraphic(pill);
        }
        else {
            setText(null);
            setGraphic(null);
        }
    }

    public Optional<Loadout> getSafeItem() {
        return Optional.ofNullable(getItem());
    }
}
