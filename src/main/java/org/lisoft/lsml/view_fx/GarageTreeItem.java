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

import java.util.Iterator;

import org.lisoft.lsml.messages.GarageMessage;
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

    public GarageTreeItem(MessageReception aXBar, GaragePath<T> aPath, boolean aShowValues) {
        super(aPath, aPath.isLeaf() ? StyleManager.makeMechIcon() : StyleManager.makeDirectoryIcon());
        xBar = aXBar;
        xBar.attach(this);
        showValues = aShowValues;

        final GaragePath<T> path = getValue();
        if (!path.isLeaf()) {
            final GarageDirectory<T> topDirectory = path.getTopDirectory();
            for (final GarageDirectory<T> child : topDirectory.getDirectories()) {
                getChildren().add(new GarageTreeItem<>(xBar, new GaragePath<>(path, child), showValues));
            }

            if (showValues) {
                for (final T value : topDirectory.getValues()) {
                    getChildren().add(new GarageTreeItem<>(xBar, new GaragePath<>(path, value), showValues));
                }
            }
        }
    }

    @Override
    public void receive(Message aMsg) {
        if (aMsg instanceof GarageMessage) {
            if (!affectsMe((GarageMessage<?>) aMsg)) {
                return;
            }

            // The message affects us. I.e. either the parent or affected directory is the same object as the top
            // level directory of this path. This means that the message is actually of type GarageMessage<T> and can
            // be safely cast.
            @SuppressWarnings("unchecked")
            final GarageMessage<T> msg = (GarageMessage<T>) aMsg;

            Platform.runLater(() -> {
                switch (msg.type) {
                    case ADDED:
                        final GaragePath<T> path = getValue();
                        msg.directory.ifPresent(aDirectory -> {
                            final GarageDirectory<T> directory = aDirectory;
                            getChildren()
                                    .add(new GarageTreeItem<>(xBar, new GaragePath<>(path, directory), showValues));
                        });

                        msg.value.ifPresent(aValue -> {
                            final T value = aValue;
                            getChildren().add(new GarageTreeItem<>(xBar, new GaragePath<>(path, value), showValues));

                        });

                        break;
                    case REMOVED:
                        for (final Iterator<TreeItem<GaragePath<T>>> iterator = getChildren().iterator(); iterator
                                .hasNext();) {
                            final GaragePath<T> child = iterator.next().getValue();
                            if (child.isLeaf() && msg.value.isPresent()) {
                                if (child.getValue().get() == msg.value.get()) {
                                    iterator.remove();
                                    break;
                                }
                            }
                            else if (!child.isLeaf() && msg.directory.isPresent()) {
                                if (child.getTopDirectory() == msg.directory.get()) {
                                    iterator.remove();
                                    break;
                                }
                            }
                        }
                        break;
                    case RENAMED:
                        break;
                    default:
                        // No-Op
                }
            });
        }
    }

    private boolean affectsMe(GarageMessage<?> aMsg) {
        final GaragePath<T> path = getValue();
        if (aMsg.parentDir == null) {
            // The message affects the root directory (rename) or affects a object not in the garage tree.

            if (aMsg.directory.isPresent()) {
                // The message affects the root
                return path.isRoot() && path.getTopDirectory() == aMsg.directory.get(); // We are the affected root

            }
            // Message affects something not in the garage tree.
            return false;

        }
        if (aMsg.directory.isPresent()) {
            // The message affects a directory
            return path.getTopDirectory() == aMsg.parentDir; // We are the parent of the affected directory, it
                                                             // affects us.
        }
        else if (aMsg.value.isPresent()) {
            // The message affects a object in a directory
            if (!showValues) {
                return false; // We only contain directories
            }
            return path.getTopDirectory() == aMsg.parentDir; // We are the parent of the affected object, it affects
                                                             // us.
        }
        else {
            // Didn't affect anything...
            return false;
        }
    }

}
