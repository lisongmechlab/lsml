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
package org.lisoft.lsml.view_fx.loadout.equipment;

import java.util.Collection;

import org.lisoft.lsml.model.item.Item;
import org.lisoft.lsml.model.loadout.EquipResult;
import org.lisoft.lsml.model.loadout.Loadout;
import org.lisoft.lsml.model.modifiers.Modifier;
import org.lisoft.lsml.view_fx.UIPreferences;
import org.lisoft.lsml.view_fx.style.ItemToolTipFormatter;
import org.lisoft.lsml.view_fx.style.StyleManager;

import javafx.scene.control.TreeTableCell;
import javafx.scene.layout.Region;

/**
 * This cell renders info about an {@link Item} in the equipment panel.
 * 
 * @author Emily Björk
 */
public class EquipmentTableCell extends TreeTableCell<Object, String> {
    private final Loadout              loadout;
    private final boolean              showIcon;
    private final ItemToolTipFormatter toolTipFormatter;

    public EquipmentTableCell(Loadout aLoadout, boolean aShowIcon, ItemToolTipFormatter aToolTipFormatter) {
        loadout = aLoadout;
        showIcon = aShowIcon;
        toolTipFormatter = aToolTipFormatter;

        setOnMouseEntered(e -> {
            Item item = getRowItem();
            if (null != item) {
                final Collection<Modifier> modifiers;
                if (UIPreferences.getToolTipShowModifiedValues()) {
                    modifiers = loadout.getModifiers();
                }
                else {
                    modifiers = null;
                }
                setTooltip(toolTipFormatter.format(item, aLoadout, modifiers));
                getTooltip().setAutoHide(false);
                // FIXME: Set timeout to infinite once we're on JavaFX9, see:
                // https://bugs.openjdk.java.net/browse/JDK-8090477
            }
            else {
                setTooltip(null);
            }
        });

    }

    private Item getRowItem() {
        Object treeItemObject = getTreeTableRow().getItem();
        if (treeItemObject instanceof Item) {
            return (Item) treeItemObject;
        }
        return null;
    }

    @Override
    protected void updateItem(String aText, boolean aEmpty) {
        super.updateItem(aText, aEmpty);
        setText(aText);

        Item item = getRowItem();
        if (null != item) {
            if (EquipResult.SUCCESS == loadout.canEquipDirectly(item)) {
                // Directly equippable
                pseudoClassStateChanged(StyleManager.CSS_PC_UNEQUIPPABLE, false);
                pseudoClassStateChanged(StyleManager.CSS_PC_SMARTPLACEABLE, false);
            }
            else if (!loadout.getCandidateLocationsForItem(item).isEmpty()) {
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
                StyleManager.changeIcon(r, item);
                setGraphic(r);
            }
        }
        else {
            setContextMenu(null);
            pseudoClassStateChanged(StyleManager.CSS_PC_UNEQUIPPABLE, false);
            pseudoClassStateChanged(StyleManager.CSS_PC_SMARTPLACEABLE, false);
            if (showIcon) {
                setGraphic(null);
            }
        }
    }
}
