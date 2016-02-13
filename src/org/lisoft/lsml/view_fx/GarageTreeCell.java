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

import java.util.List;
import java.util.Optional;

import org.lisoft.lsml.command.CmdAddGarageDirectory;
import org.lisoft.lsml.command.CmdMoveGarageDirectory;
import org.lisoft.lsml.command.CmdMoveValueInGarage;
import org.lisoft.lsml.command.CmdRemoveGarageDirectory;
import org.lisoft.lsml.command.CmdRenameGarageDirectory;
import org.lisoft.lsml.messages.MessageDelivery;
import org.lisoft.lsml.model.garage.GarageDirectory;
import org.lisoft.lsml.model.loadout.EquipException;
import org.lisoft.lsml.util.CommandStack;
import org.lisoft.lsml.util.CommandStack.CompositeCommand;
import org.lisoft.lsml.view_fx.util.FxmlHelpers;
import org.lisoft.lsml.view_fx.util.GarageDirectoryDragHelper;
import org.lisoft.lsml.view_fx.util.LoadoutDragHelper;
import org.lisoft.lsml.view_fx.util.LoadoutDragHelper.LoadoutDragData;

import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.MenuItem;
import javafx.scene.control.TreeCell;
import javafx.scene.control.TreeItem;
import javafx.scene.control.TreeView;
import javafx.scene.control.cell.TextFieldTreeCell;
import javafx.scene.input.Dragboard;
import javafx.scene.input.TransferMode;
import javafx.util.StringConverter;

/**
 * This class implements the drag and drop functionality for {@link TreeCell} as a containing {@link GarageDirectory}.
 * 
 * @author Li Song
 * @param <T>
 */
public class GarageTreeCell<T> extends TextFieldTreeCell<GarageDirectory<T>> {
    private final TreeView<GarageDirectory<T>> treeView;
    private final CommandStack                 cmdStack;
    private final MessageDelivery              xBar;

    private class RenameConverter extends StringConverter<GarageDirectory<T>> {
        @Override
        public GarageDirectory<T> fromString(String aString) {
            GarageDirectory<T> value = getTreeItem().getValue();
            if (value != null) {
                LiSongMechLab.safeCommand(GarageTreeCell.this, cmdStack,
                        new CmdRenameGarageDirectory<>(xBar, value, aString));
            }
            return value;
        }

        @Override
        public String toString(GarageDirectory<T> aObject) {
            return aObject.getName();
        }
    }

    public GarageTreeCell(MessageDelivery aXBar, CommandStack aCmdStack, TreeView<GarageDirectory<T>> aTreeView,
            Class<? extends T> aClass) {
        treeView = aTreeView;
        cmdStack = aCmdStack;
        xBar = aXBar;

        setConverter(new RenameConverter());

        setOnDragDetected(aEvent -> {
            TreeItem<GarageDirectory<T>> item = getTreeItem();
            // Make sure not to drag the root node.
            if (item != null && item.getParent() != null) {
                List<String> path = FxmlHelpers.getTreePath(item);
                Dragboard dragboard = startDragAndDrop(TransferMode.COPY_OR_MOVE);
                GarageDirectoryDragHelper.doDrag(dragboard, path);
            }
            aEvent.consume();
        });

        setOnDragOver(aEvent -> {
            Dragboard db = aEvent.getDragboard();
            if (GarageDirectoryDragHelper.isDrag(db) || LoadoutDragHelper.isDrag(db, aClass)) {
                aEvent.acceptTransferModes(TransferMode.COPY_OR_MOVE);
            }
            aEvent.consume();
        });

        setOnDragDropped(aEvent -> {
            Dragboard db = aEvent.getDragboard();
            boolean success = false;
            {
                Optional<List<String>> data = GarageDirectoryDragHelper.unpackDrag(db);
                if (data.isPresent()) {
                    List<String> aPath = data.get();
                    Optional<TreeItem<GarageDirectory<T>>> sourceOptional = FxmlHelpers
                            .resolveTreePath(treeView.getRoot(), aPath);

                    if (sourceOptional.isPresent()) {
                        TreeItem<GarageDirectory<T>> source = sourceOptional.get();
                        TreeItem<GarageDirectory<T>> treeParent = source.getParent();
                        GarageDirectory<T> sourceDir = source.getValue();
                        GarageDirectory<T> parentDir = treeParent.getValue();

                        Optional<GarageDirectory<T>> destination = getSafeItem();
                        if (destination.isPresent() && destination.get() != sourceDir) {
                            success = LiSongMechLab.safeCommand(this, aCmdStack,
                                    new CmdMoveGarageDirectory<T>(aXBar, destination.get(), sourceDir, parentDir));
                        }
                    }
                }
            }
            {
                Optional<LoadoutDragData<T>> loadoutDragDataOptional = LoadoutDragHelper.unpackDrag(db, aClass);
                if (loadoutDragDataOptional.isPresent()) {
                    LoadoutDragData<T> loadoutDragData = loadoutDragDataOptional.get();
                    Optional<TreeItem<GarageDirectory<T>>> sourceOptional = FxmlHelpers
                            .resolveTreePath(treeView.getRoot(), loadoutDragData.sourcePath);

                    if (sourceOptional.isPresent()) {
                        TreeItem<GarageDirectory<T>> sourceTreeItem = sourceOptional.get();
                        GarageDirectory<T> sourceDir = sourceTreeItem.getValue();
                        Optional<GarageDirectory<T>> destination = getSafeItem();

                        if (destination.isPresent()) {
                            for (T value : loadoutDragData.loadouts) {

                                LiSongMechLab.safeCommand(this, cmdStack, new CompositeCommand("move in garage", xBar) {
                                    @Override
                                    protected void buildCommand() throws EquipException {
                                        addOp(new CmdMoveValueInGarage<T>(messageBuffer, value, destination.get(),
                                                sourceDir));
                                    }
                                });
                            }
                        }

                    }
                }
            }

            aEvent.setDropCompleted(success);
            aEvent.consume();
        });

        MenuItem addFolder = new MenuItem("New folder...");
        addFolder.setOnAction(aEvent -> {
            getSafeItem().ifPresent(aValue -> {
                LiSongMechLab.safeCommand(treeView, cmdStack,
                        new CmdAddGarageDirectory<>(xBar, new GarageDirectory<>("New Folder"), aValue));
            });
            aEvent.consume();
        });
        MenuItem removeFolder = new MenuItem("Remove");
        removeFolder.setOnAction(aEvent -> {
            getSafeItem().ifPresent(aValue -> {
                getSafeParentItem().ifPresent(aParentValue -> {
                    Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
                    alert.setContentText("Are you sure you want to delete the folder: " + aValue.getName());
                    alert.showAndWait().ifPresent(aButton -> {
                        if (aButton == ButtonType.OK) {
                            LiSongMechLab.safeCommand(treeView, cmdStack,
                                    new CmdRemoveGarageDirectory<>(xBar, aValue, aParentValue));
                        }
                    });
                });
            });
            aEvent.consume();
        });

        setContextMenu(new ContextMenu(addFolder, removeFolder));
    }

    @Override
    public void updateItem(GarageDirectory<T> aItem, boolean aEmpty) {
        super.updateItem(aItem, aEmpty);
        if (!aEmpty && aItem != null) {
            setText(aItem.getName());
            setGraphic(getTreeItem().getGraphic());
        }
        else {
            setText(null);
            setGraphic(null);
        }
    }

    public Optional<GarageDirectory<T>> getSafeItem() {
        return Optional.ofNullable(getItem());
    }

    public Optional<GarageDirectory<T>> getSafeParentItem() {
        TreeItem<GarageDirectory<T>> currentTreeItem = getTreeItem();
        if (currentTreeItem != null) {
            TreeItem<GarageDirectory<T>> currentParent = currentTreeItem.getParent();
            if (currentParent != null) {
                return Optional.ofNullable(currentParent.getValue());
            }
        }
        return Optional.empty();
    }
}
