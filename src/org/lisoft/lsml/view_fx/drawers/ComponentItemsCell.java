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
package org.lisoft.lsml.view_fx.drawers;

import org.lisoft.lsml.model.item.Item;
import org.lisoft.lsml.view_fx.loadout.component.ComponentItemsList;
import org.lisoft.lsml.view_fx.loadout.component.ItemView;
import org.lisoft.lsml.view_fx.style.StyleManager;

import javafx.geometry.Pos;

/**
 * This class is responsible for rendering items on the components.
 * 
 * @author Li Song
 */
public class ComponentItemsCell extends ItemView.Cell<Item> {
    public ComponentItemsCell(ItemView<Item> aItemView) {
        super(aItemView);
        setAlignment(Pos.TOP_LEFT);
        getStyleClass().add(StyleManager.CSS_CLASS_EQUIPPED);
    }

    @Override
    protected void updateItem(Item aItem, boolean aEmpty) {

        super.updateItem(aItem, aEmpty);
        if (null == aItem) {
            setText("EMPTY");
            setRowSpan(1);
            pseudoClassStateChanged(StyleManager.CSS_PC_FIXED, false);
        }
        else {
            setText(aItem.getShortName());
            setRowSpan(aItem.getNumCriticalSlots());

            ComponentItemsList list = (ComponentItemsList) getListView().getItems();
            boolean isFixed = list.isFixed(getIndex());
            pseudoClassStateChanged(StyleManager.CSS_PC_FIXED, isFixed);
        }
        StyleManager.changeItemStyle(this, aItem);
    }
}