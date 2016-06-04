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

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.lisoft.lsml.messages.MessageXBar;
import org.lisoft.lsml.model.garage.GaragePath;
import org.lisoft.lsml.model.loadout.Loadout;
import org.lisoft.lsml.util.CommandStack;
import org.lisoft.lsml.view_fx.util.FxControlUtils;
import org.lisoft.lsml.view_fx.util.GarageDirectoryDragUtils;

import javafx.scene.control.ContextMenu;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.scene.control.MenuItem;
import javafx.scene.control.TreeItem;
import javafx.scene.control.TreeView;
import javafx.scene.input.Dragboard;
import javafx.scene.input.TransferMode;

/**
 * @author Emily Björk
 */
public class LoadoutPillCell extends ListCell<Loadout> {

    private final LoadoutPillSmall pillSmall;
    private final LoadoutPill pill;
    private final TreeView<GaragePath<Loadout>> treeView;
    private final ListView<Loadout> listView;

    public LoadoutPillCell(MessageXBar aXBar, CommandStack aStack, TreeView<GaragePath<Loadout>> aTreeView,
            ListView<Loadout> aListView) {
        pill = new LoadoutPill(aStack, aXBar);
        pillSmall = new LoadoutPillSmall(aStack, aXBar);
        treeView = aTreeView;
        listView = aListView;

        final ContextMenu cm = new ContextMenu();
        final MenuItem delete = new MenuItem("Delete...");
        delete.setOnAction(aEvent -> {
            deleteMe(aXBar, aStack);
        });
        cm.getItems().add(delete);
        setContextMenu(cm);

        setOnMouseClicked(aEvent -> {
            if (FxControlUtils.isDoubleClick(aEvent)) {
                final Loadout loadout = getItem();
                if (null != loadout) {
                    LiSongMechLab.openLoadout(aXBar, loadout);
                }
                aEvent.consume();
            }
        });

        setOnDragDetected(aEvent -> {
            getSafeItem().ifPresent(aLoadout -> {
                final Dragboard dragboard = startDragAndDrop(TransferMode.COPY_OR_MOVE);

                getParentDir().ifPresent(aDir -> {
                    final List<String> paths = new ArrayList<>();
                    for (final Loadout selected : listView.getSelectionModel().getSelectedItems()) {
                        final StringBuilder sb = new StringBuilder();
                        new GaragePath<>(aDir, selected).toPath(sb);
                        paths.add(sb.toString());
                    }
                    GarageDirectoryDragUtils.doDrag(dragboard, paths);
                });
            });
            aEvent.consume();
        });
    }

    public Optional<Loadout> getSafeItem() {
        return Optional.ofNullable(getItem());
    }

    @Override
    protected void updateItem(Loadout aItem, boolean aEmpty) {
        super.updateItem(aItem, aEmpty);
        if (aItem != null && !aEmpty) {
            setText(null);
            final Optional<GaragePath<Loadout>> dir = getParentDir();
            dir.ifPresent(aDir -> {
                final boolean small = ApplicationModel.model.settings.getBoolean(Settings.UI_USE_SMALL_MECH_LIST)
                        .getValue();
                if (small) {
                    pillSmall.setLoadout(aItem, aDir.getTopDirectory());
                    setGraphic(pillSmall);
                }
                else {
                    pill.setLoadout(aItem, aDir.getTopDirectory());
                    setGraphic(pill);
                }
            });
        }
        else {
            setText(null);
            setGraphic(null);
        }
    }

    private void deleteMe(MessageXBar aXBar, CommandStack aStack) {
        getParentDir().ifPresent(aParentPath -> {
            getSafeItem().ifPresent(aLoadout -> {
                final GaragePath<Loadout> path = new GaragePath<>(aParentPath, aLoadout);
                GlobalGarage.remove(path, this, aStack, aXBar);
            });
        });
    }

    private Optional<GaragePath<Loadout>> getParentDir() {
        final TreeItem<GaragePath<Loadout>> item = treeView.getSelectionModel().getSelectedItem();
        if (null == item) {
            return Optional.empty();
        }
        return Optional.ofNullable(item.getValue());
    }
}
