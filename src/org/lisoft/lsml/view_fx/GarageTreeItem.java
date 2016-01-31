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
import org.lisoft.lsml.messages.MessageXBar;
import org.lisoft.lsml.model.garage.GarageDirectory;

import javafx.scene.Node;
import javafx.scene.control.TreeItem;
import javafx.scene.layout.Region;

/**
 * @author Li Song
 * @param <T>
 *            The value to show in the garage tree.
 */
public class GarageTreeItem<T> extends TreeItem<GarageDirectory<T>> implements MessageReceiver {
    private MessageXBar xBar;

    static Node makeGraphic() {
        Region r = new Region();
        r.getStyleClass().add("svg-folder");
        r.getStyleClass().add("icon");
        r.getStyleClass().add("icon-small");
        return r;
    }

    public GarageTreeItem(MessageXBar aXBar, GarageDirectory<T> aDir) {
        super(aDir, makeGraphic());
        xBar = aXBar;
        xBar.attach(this);

        update();
    }

    private void update() {
        getChildren().clear();
        for (GarageDirectory<T> child : getValue().getDirectories()) {
            getChildren().add(new GarageTreeItem<>(xBar, child));
        }
    }

    @Override
    public void receive(Message aMsg) {
        if (aMsg instanceof GarageMessage) {
            GarageMessage garageMessage = (GarageMessage) aMsg;
            if (garageMessage.garageDir == getValue()) {
                update();
            }
        }
    }
}
