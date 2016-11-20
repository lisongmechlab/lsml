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

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import org.lisoft.lsml.command.CmdGarageMultiMoveOperation;
import org.lisoft.lsml.command.CmdGarageRename;
import org.lisoft.lsml.messages.MessageDelivery;
import org.lisoft.lsml.model.NamedObject;
import org.lisoft.lsml.model.garage.GarageDirectory;
import org.lisoft.lsml.model.garage.GaragePath;
import org.lisoft.lsml.util.CommandStack;
import org.lisoft.lsml.view_fx.util.GarageDirectoryDragUtils;

import javafx.scene.control.ContextMenu;
import javafx.scene.control.MenuItem;
import javafx.scene.control.TreeCell;
import javafx.scene.control.TreeItem;
import javafx.scene.control.cell.TextFieldTreeCell;
import javafx.scene.input.DragEvent;
import javafx.scene.input.Dragboard;
import javafx.scene.input.TransferMode;
import javafx.util.StringConverter;

/**
 * This class implements the drag and drop functionality for {@link TreeCell} as a containing {@link GarageDirectory}.
 *
 * @author Emily Björk
 * @param <T>
 *            The value type of the garage that is displayed.
 */
public class GarageTreeCell<T extends NamedObject> extends TextFieldTreeCell<GaragePath<T>> {
    private class RenameConverter extends StringConverter<GaragePath<T>> {
        @Override
        public GaragePath<T> fromString(String aString) {
            LiSongMechLab.safeCommand(GarageTreeCell.this, cmdStack,
                    new CmdGarageRename<>(xBar, getTreeItem().getValue(), aString), xBar);
            return getTreeItem().getValue();
        }

        @Override
        public String toString(GaragePath<T> aObject) {
            return aObject.toString();
        }
    }

    private final CommandStack cmdStack;

    private final MessageDelivery xBar;

    private final MenuItem remove;

    public GarageTreeCell(MessageDelivery aXBar, CommandStack aStack) {
        cmdStack = aStack;
        xBar = aXBar;

        setConverter(new RenameConverter());

        setOnDragDetected(aEvent -> {
            final List<String> paths = new ArrayList<>();
            for (final TreeItem<GaragePath<T>> item : getTreeView().getSelectionModel().getSelectedItems()) {
                if (item != null && item.getParent() != null) {
                    final StringBuilder sb = new StringBuilder();
                    item.getValue().toPath(sb);
                    paths.add(sb.toString());
                }
            }
            if (!paths.isEmpty()) {
                final Dragboard dragboard = startDragAndDrop(TransferMode.COPY_OR_MOVE);
                GarageDirectoryDragUtils.doDrag(dragboard, paths);
                aEvent.consume();
            }
        });

        setOnDragOver(aEvent -> {
            final Dragboard db = aEvent.getDragboard();
            if (GarageDirectoryDragUtils.isDrag(db)) {
                aEvent.acceptTransferModes(TransferMode.COPY_OR_MOVE);
            }
            aEvent.consume();
        });

        setOnDragDropped(aEvent -> {
            aEvent.setDropCompleted(dropEvent(aEvent));
            aEvent.consume();
        });

        final MenuItem newFolder = new MenuItem("New folder...");
        newFolder.setOnAction(aEvent -> {
            GaragePath<T> path = getItem();
            if (path == null) {
                path = getTreeView().getRoot().getValue();
            }
            GlobalGarage.addFolder(path, GarageTreeCell.this, cmdStack, xBar);
            aEvent.consume();
        });

        remove = new MenuItem("Remove");
        remove.setOnAction(aEvent -> {
            final List<GaragePath<T>> paths = getTreeView().getSelectionModel().getSelectedItems().stream()
                    .map(treeItem -> treeItem.getValue()).collect(Collectors.toList());
            GlobalGarage.remove(paths, GarageTreeCell.this, cmdStack, aXBar);
            aEvent.consume();
        });

        setContextMenu(new ContextMenu(newFolder, remove));
    }

    public Optional<GaragePath<T>> getSafeItem() {
        return Optional.ofNullable(getItem());
    }

    @Override
    public void updateItem(GaragePath<T> aItem, boolean aEmpty) {
        super.updateItem(aItem, aEmpty);
        if (!aEmpty && aItem != null) {
            setText(aItem.toString());
            setGraphic(getTreeItem().getGraphic());

            remove.setDisable(aItem.getParent() == null);
        }
        else {
            setText(null);
            setGraphic(null);
        }
    }

    private boolean dropEvent(DragEvent aEvent) {
        final Dragboard db = aEvent.getDragboard();
        final Optional<List<String>> data = GarageDirectoryDragUtils.unpackDrag(db);
        if (data.isPresent()) {

            final GarageDirectory<T> root = getRootDir();
            final GaragePath<T> destDir = getTreeItem() == null ? getTreeView().getRoot().getValue()
                    : getTreeItem().getValue();

            final List<GaragePath<T>> paths = data.get().stream().map(path -> {
                try {
                    return GaragePath.fromPath(path, root);
                }
                catch (final IOException e) {
                    // Shouldn't really happen... idk what to do. Raise an error?
                    LiSongMechLab.showError(this, e);
                }
                return null;
            }).filter(path -> path != null).collect(Collectors.toList());

            LiSongMechLab.safeCommand(this, cmdStack, new CmdGarageMultiMoveOperation<>(xBar, destDir, paths), xBar);
            return true;
        }
        return false;
    }

    private GarageDirectory<T> getRootDir() {
        return getTreeView().getRoot().getValue().getTopDirectory();
    }
}
