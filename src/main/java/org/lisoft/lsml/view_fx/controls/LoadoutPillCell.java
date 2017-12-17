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
package org.lisoft.lsml.view_fx.controls;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import javax.inject.Named;

import org.lisoft.lsml.messages.ApplicationMessage;
import org.lisoft.lsml.messages.MessageXBar;
import org.lisoft.lsml.model.garage.GaragePath;
import org.lisoft.lsml.model.loadout.Loadout;
import org.lisoft.lsml.model.loadout.LoadoutFactory;
import org.lisoft.lsml.util.CommandStack;
import org.lisoft.lsml.view_fx.GlobalGarage;
import org.lisoft.lsml.view_fx.Settings;
import org.lisoft.lsml.view_fx.controllers.mainwindow.LoadoutPillController;
import org.lisoft.lsml.view_fx.controllers.mainwindow.LoadoutPillSmallController;
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

    private final LoadoutPillSmallController pillSmall;
    private final LoadoutPillController pill;
    private final TreeView<GaragePath<Loadout>> treeView;
    private final ListView<Loadout> listView;
    private final Settings settings;

    public LoadoutPillCell(Settings aSettings, @Named("global") MessageXBar aXBar, CommandStack aStack,
            TreeView<GaragePath<Loadout>> aTreeView, ListView<Loadout> aListView, LoadoutFactory aLoadoutFactory) {
        pill = new LoadoutPillController(aStack, aXBar, aLoadoutFactory);
        pillSmall = new LoadoutPillSmallController(aStack, aXBar, aLoadoutFactory);
        treeView = aTreeView;
        listView = aListView;
        settings = aSettings;

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
                    aXBar.post(new ApplicationMessage(loadout, ApplicationMessage.Type.OPEN_LOADOUT, this));
                }
                aEvent.consume();
            }
        });

        setOnDragDetected(aEvent -> {
            getSafeItem().ifPresent(aLoadout -> {
                final Dragboard dragboard = startDragAndDrop(TransferMode.COPY_OR_MOVE);

                getParentPath().ifPresent(aParentPath -> {
                    final List<String> paths = new ArrayList<>();
                    for (final Loadout selected : listView.getSelectionModel().getSelectedItems()) {
                        final StringBuilder sb = new StringBuilder();
                        new GaragePath<>(aParentPath, selected).toPath(sb);
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
            final Optional<GaragePath<Loadout>> itemPath = getItemPath();
            itemPath.ifPresent(aPath -> {
                final boolean small = settings.getBoolean(Settings.UI_USE_SMALL_MECH_LIST).getValue();
                if (small) {
                    pillSmall.setLoadout(aItem, aPath);
                    setGraphic(pillSmall.getView());
                }
                else {
                    pill.setLoadout(aItem, aPath);
                    setGraphic(pill.getView());
                }
            });
        }
        else {
            setText(null);
            setGraphic(null);
        }
    }

    private void deleteMe(MessageXBar aXBar, CommandStack aStack) {
        getItemPath().ifPresent(aItemPath -> {
            GlobalGarage.remove(aItemPath, this, aStack, aXBar);
        });
    }

    private Optional<GaragePath<Loadout>> getItemPath() {
        final TreeItem<GaragePath<Loadout>> parentDir = treeView.getSelectionModel().getSelectedItem();
        final Loadout loadout = getItem();
        if (null == parentDir || null == loadout) {
            return Optional.empty();
        }
        return Optional.ofNullable(new GaragePath<>(parentDir.getValue(), loadout));
    }

    private Optional<GaragePath<Loadout>> getParentPath() {
        final TreeItem<GaragePath<Loadout>> parentDir = treeView.getSelectionModel().getSelectedItem();
        return Optional.ofNullable(parentDir.getValue());
    }
}
