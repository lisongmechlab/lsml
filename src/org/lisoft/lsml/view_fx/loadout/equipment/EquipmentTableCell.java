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
package org.lisoft.lsml.view_fx.loadout.equipment;

import org.lisoft.lsml.model.item.Item;
import org.lisoft.lsml.model.loadout.EquipResult;
import org.lisoft.lsml.model.loadout.LoadoutBase;
import org.lisoft.lsml.view_fx.style.StyleManager;

import javafx.scene.control.TreeTableCell;
import javafx.scene.layout.Region;

/**
 * This cell renders info about an {@link Item} in the equipment panel.
 * 
 * @author Li Song
 */
public class EquipmentTableCell extends TreeTableCell<Object, String> {
    private final LoadoutBase<?> loadout;
    private final boolean        showIcon;

    public EquipmentTableCell(LoadoutBase<?> aLoadout, boolean aShowIcon) {
        loadout = aLoadout;
        showIcon = aShowIcon;
    }

    @Override
    protected void updateItem(String aItem, boolean aEmpty) {
        super.updateItem(aItem, aEmpty);
        setText(aItem);

        Object treeItemObject = getTreeTableRow().getItem();
        if (treeItemObject instanceof Item) {
            Item treeItem = (Item) treeItemObject;
            //
            // StyleManager.changeStyle(this, EquipmentCategory.classify(treeItem));
            //
            if (EquipResult.SUCCESS == loadout.canEquipDirectly(treeItem)) {
                // Directly equippable
                pseudoClassStateChanged(StyleManager.CSS_PC_UNEQUIPPABLE, false);
                pseudoClassStateChanged(StyleManager.CSS_PC_SMARTPLACEABLE, false);
            }
            else if (!loadout.getCandidateLocationsForItem(treeItem).isEmpty()) {
                // Might be smart placeable
                pseudoClassStateChanged(StyleManager.CSS_PC_UNEQUIPPABLE, false);
                pseudoClassStateChanged(StyleManager.CSS_PC_SMARTPLACEABLE, true);
            }
            else {
                pseudoClassStateChanged(StyleManager.CSS_PC_UNEQUIPPABLE, true);
                pseudoClassStateChanged(StyleManager.CSS_PC_SMARTPLACEABLE, false);
            }

            if (showIcon) {
                Region r = new Region();
                StyleManager.changeIcon(r, treeItem);
                setGraphic(r);
            }
        }
        else {
            // final EquipmentCategory category;
            // if (treeItemObject instanceof EquipmentCategory) {
            // category = (EquipmentCategory) treeItemObject;
            // }
            // else {
            // category = null;
            // }
            // StyleManager.changeCategoryStyle(this, category);
            pseudoClassStateChanged(StyleManager.CSS_PC_UNEQUIPPABLE, false);
            pseudoClassStateChanged(StyleManager.CSS_PC_SMARTPLACEABLE, false);
            if (showIcon) {
                setGraphic(null);
            }
        }
    }
}
