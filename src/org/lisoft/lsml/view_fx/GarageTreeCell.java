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

import org.lisoft.lsml.model.garage.GarageDirectory;

import javafx.scene.control.TreeCell;
import javafx.scene.control.TreeItem;

/**
 * @author Emily
 *
 */
public class GarageTreeCell<T> extends TreeCell<GarageDirectory<T>> {

    @Override
    protected void updateItem(GarageDirectory<T> aItem, boolean aEmpty) {
        super.updateItem(aItem, aEmpty);

        TreeItem<GarageDirectory<T>> i = getTreeItem();

        if (null != aItem) {
            setText(aItem.getName());
        }
        else {
            setText(null);
        }
    }
}
