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

import org.lisoft.lsml.messages.GarageMessage;
import org.lisoft.lsml.messages.GarageMessageType;
import org.lisoft.lsml.messages.Message;
import org.lisoft.lsml.messages.MessageReceiver;
import org.lisoft.lsml.messages.MessageReception;
import org.lisoft.lsml.model.NamedObject;
import org.lisoft.lsml.model.garage.GarageDirectory;
import org.lisoft.lsml.model.garage.GaragePath;
import org.lisoft.lsml.view_fx.style.StyleManager;

import javafx.application.Platform;
import javafx.scene.control.TreeItem;

/**
 * @author Emily Björk
 * @param <T>
 *            The value to show in the garage tree.
 */
public class GarageTreeItem<T extends NamedObject> extends TreeItem<GaragePath<T>> implements MessageReceiver {
    private final MessageReception xBar;
    private final boolean showValues;
    private final Class<T> clazz;

    public GarageTreeItem(MessageReception aXBar, GaragePath<T> aPath, boolean aShowValues, Class<T> aClazz) {
        super(aPath, aPath.isLeaf() ? StyleManager.makeMechIcon() : StyleManager.makeDirectoryIcon());
        xBar = aXBar;
        xBar.attach(this);
        showValues = aShowValues;
        clazz = aClazz;

        final GaragePath<T> path = getValue();
        if (!path.isLeaf()) {
            final GarageDirectory<T> topDirectory = path.getTopDirectory();
            for (final GarageDirectory<T> child : topDirectory.getDirectories()) {
                getChildren().add(new GarageTreeItem<>(xBar, new GaragePath<>(path, child), showValues, aClazz));
            }

            if (showValues) {
                for (final T value : topDirectory.getValues()) {
                    getChildren().add(new GarageTreeItem<>(xBar, new GaragePath<>(path, value), showValues, aClazz));
                }
            }
            sortChildren();
        }
    }

    @Override
    public void receive(Message aMsg) {
        if (aMsg instanceof GarageMessage) {
            final GarageMessage<?> msg = (GarageMessage<?>) aMsg;
            final GaragePath<?> msgPath = msg.path;
            final GaragePath<T> myPath = getValue();

            if (msg.type == GarageMessageType.ADDED) {
                // New object or directory
                if (!myPath.isLeaf() && msgPath.getParentDirectory() == myPath.getTopDirectory()) {
                    // Don't add leaves if we're not showing values.
                    if (msgPath.isLeaf() && !showValues) {
                        return;
                    }

                    Platform.runLater(() -> {
                        // Because the msgPath's parent directory refers by identity to us, this cast is safe.
                        @SuppressWarnings("unchecked")
                        final GaragePath<T> path = (GaragePath<T>) msgPath;

                        getChildren().add(new GarageTreeItem<>(xBar, path, showValues, clazz));
                        sortChildren();
                    });
                }
            }
            else {
                if (myPath.equals(msgPath)) {
                    switch (msg.type) {
                        case RENAMED:
                            valueChangedEvent();
                            sortChildren();
                            break;
                        case REMOVED:
                            final TreeItem<GaragePath<T>> parent = getParent();
                            if (parent != null) {
                                // We already got unlinked from parent...
                                parent.getChildren().remove(this);
                            }
                            break;
                        case ADDED: // Fall-through, not possible
                        default:
                            throw new RuntimeException("Unknown value in switch!");
                    }
                }
                else if (myPath.equals(msgPath.getParent()) && msg.type == GarageMessageType.RENAMED) {
                    sortChildren();
                }
            }
        }
    }

    private void sortChildren() {
        getChildren().sort((aLHS, aRHS) -> {
            final GaragePath<T> lhs = aLHS.getValue();
            final GaragePath<T> rhs = aRHS.getValue();

            if (lhs.isLeaf() && rhs.isLeaf()) {
                return lhs.getValue().get().getName().compareToIgnoreCase(rhs.getValue().get().getName());
            }
            if (lhs.isLeaf() != rhs.isLeaf()) {
                return Boolean.compare(lhs.isLeaf(), rhs.isLeaf());
            }
            final String lhsName = lhs.getTopDirectory().getName();
            final String rhsName = rhs.getTopDirectory().getName();
            return lhsName.compareToIgnoreCase(rhsName);
        });
    }

}
