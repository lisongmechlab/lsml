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
package org.lisoft.lsml.view_fx.style;

import org.lisoft.lsml.model.item.Item;
import org.lisoft.lsml.model.item.Weapon;
import org.lisoft.lsml.model.loadout.LoadoutBase;

import javafx.collections.ObservableList;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.control.Tooltip;
import javafx.scene.layout.VBox;

/**
 * This class can build tool tips for items accounting for loadout quirks.
 * 
 * @author Emily Björk
 */
public class ItemToolTipFormatter {

    public Tooltip format(Item aItem, LoadoutBase<?> aLoadoutBase) {
        if (aItem instanceof Weapon) {
            Weapon weapon = (Weapon) aItem;

            // TODO: Make cute icons!

            VBox box = new VBox();
            ObservableList<Node> children = box.getChildren();
            children.add(new Label(aItem.getDescription()));
            children.add(new Label("DPS: " + weapon.getStat("d/s", aLoadoutBase.getModifiers())));

            Tooltip tooltip = new Tooltip();
            tooltip.setGraphic(box);
            return tooltip;
        }
        return new Tooltip(aItem.getDescription());
    }
}
