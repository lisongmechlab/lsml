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

import org.lisoft.lsml.messages.GarageMessage;
import org.lisoft.lsml.messages.Message;
import org.lisoft.lsml.messages.MessageReceiver;
import org.lisoft.lsml.messages.MessageReception;
import org.lisoft.lsml.model.garage.GarageDirectory;
import org.lisoft.lsml.model.garage.GaragePath;
import org.lisoft.lsml.view_fx.style.StyleManager;

import javafx.application.Platform;
import javafx.scene.control.TreeItem;

/**
 * @author Li Song
 * @param <T>
 *            The value to show in the garage tree.
 */
public class GarageTreeItem<T> extends TreeItem<GaragePath<T>> implements MessageReceiver {
    private MessageReception xBar;
    private final boolean showValues;

    public GarageTreeItem(MessageReception aXBar, GaragePath<T> aPath, boolean aShowValues) {
        super(aPath, aPath.isLeaf() ? StyleManager.makeMechIcon() : StyleManager.makeDirectoryIcon());
        xBar = aXBar;
        xBar.attach(this);
        showValues = aShowValues;
        update();
    }

    private void update() {
        // FIXME: This naive approach will cause the tree to do unnecessary updates and collapse
        // upon modification. The garage message needs additional data to do this correctly. Postponing
        // those changes until after first alpha.
        getChildren().clear();

        GaragePath<T> path = getValue();
        if (!path.isLeaf()) {
            GarageDirectory<T> topDirectory = path.getTopDirectory();
            for (GarageDirectory<T> child : topDirectory.getDirectories()) {
                getChildren().add(new GarageTreeItem<>(xBar, new GaragePath<>(path, child), showValues));
            }

            if (showValues) {
                for (T value : topDirectory.getValues()) {
                    getChildren().add(new GarageTreeItem<>(xBar, new GaragePath<>(path, value), showValues));
                }
            }
        }
    }

    @Override
    public void receive(Message aMsg) {
        if (aMsg instanceof GarageMessage) {
            Platform.runLater(() -> {
                update();
            });
        }
    }
}
