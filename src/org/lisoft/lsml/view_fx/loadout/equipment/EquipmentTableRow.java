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

import org.lisoft.lsml.command.CmdAutoAddItem;
import org.lisoft.lsml.command.CmdRemoveMatching;
import org.lisoft.lsml.messages.MessageDelivery;
import org.lisoft.lsml.model.item.Item;
import org.lisoft.lsml.model.loadout.EquipResult;
import org.lisoft.lsml.model.loadout.LoadoutBase;
import org.lisoft.lsml.util.CommandStack;
import org.lisoft.lsml.view_fx.LiSongMechLab;
import org.lisoft.lsml.view_fx.UIPreferences;
import org.lisoft.lsml.view_fx.style.StyleManager;

import javafx.scene.control.CheckMenuItem;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.MenuItem;
import javafx.scene.control.TreeTableRow;
import javafx.scene.input.Dragboard;
import javafx.scene.input.MouseButton;
import javafx.scene.input.TransferMode;

/**
 * Fixes styles for equipment rendering in the loadout window.
 * 
 * @author Li Song
 *
 */
public class EquipmentTableRow extends TreeTableRow<Object> {
    private final LoadoutBase<?> loadout;
    private final MenuItem       autoEquip;

    public EquipmentTableRow(LoadoutBase<?> aLoadout, CommandStack aStack, MessageDelivery aMessageDelivery) {
        loadout = aLoadout;
        setOnDragDetected(aEvent -> {
            Item item = getValueAsItem();
            if (null == item)
                return;
            Dragboard db = startDragAndDrop(TransferMode.COPY);
            LiSongMechLab.addEquipmentDrag(db, item);
            aEvent.consume();
        });

        setOnMouseClicked(aEvent -> {
            int clicks = aEvent.getClickCount();
            System.out.println(clicks);
            if (MouseButton.PRIMARY == aEvent.getButton() && 2 <= clicks && clicks % 2 == 0) {
                Item item = getValueAsItem();
                if (null == item)
                    return;

                LiSongMechLab.safeCommand(this, aStack, new CmdAutoAddItem(loadout, aMessageDelivery, item));
            }
            aEvent.consume();
        });

        autoEquip = new MenuItem("Auto equip");
        autoEquip.setOnAction(e -> {
            final Item item = getValueAsItem();
            if (null != item) {
                LiSongMechLab.safeCommand(this, aStack, new CmdAutoAddItem(loadout, aMessageDelivery, item));
            }
        });

        MenuItem removeAll = new MenuItem("Remove all");
        removeAll.setOnAction(e -> {
            final Item item = getValueAsItem();
            if (null != item) {
                LiSongMechLab.safeCommand(this, aStack, new CmdRemoveMatching("remove all " + item.getName(),
                        aMessageDelivery, loadout, i -> i == item));
            }
        });

        CheckMenuItem showModifier = new CheckMenuItem("Tool tips with quirks");
        showModifier.selectedProperty().bindBidirectional(UIPreferences.toolTipShowModifiedValuesProperty());

        setContextMenu(new ContextMenu(autoEquip, removeAll, showModifier));

    }

    private Item getValueAsItem() {
        Object object = getItem();
        if (!(object instanceof Item))
            return null;
        return (Item) object;
    }

    @Override
    protected void updateItem(Object aItem, boolean aEmpty) {
        super.updateItem(aItem, aEmpty);
        if (aItem instanceof Item) {
            Item treeItem = (Item) aItem;

            StyleManager.changeListStyle(this, EquipmentCategory.classify(treeItem));

            if (EquipResult.SUCCESS == loadout.canEquipDirectly(treeItem)) {
                // Directly equippable
                pseudoClassStateChanged(StyleManager.CSS_PC_UNEQUIPPABLE, false);
                pseudoClassStateChanged(StyleManager.CSS_PC_SMARTPLACEABLE, false);
                autoEquip.setDisable(false);
            }
            else if (!loadout.getCandidateLocationsForItem(treeItem).isEmpty()) {
                // Might be smart placeable
                pseudoClassStateChanged(StyleManager.CSS_PC_UNEQUIPPABLE, false);
                pseudoClassStateChanged(StyleManager.CSS_PC_SMARTPLACEABLE, true);
                autoEquip.setDisable(false);
            }
            else {
                pseudoClassStateChanged(StyleManager.CSS_PC_UNEQUIPPABLE, true);
                pseudoClassStateChanged(StyleManager.CSS_PC_SMARTPLACEABLE, false);
                autoEquip.setDisable(true);
            }
        }
        else {
            final EquipmentCategory category;
            if (aItem instanceof EquipmentCategory) {
                category = (EquipmentCategory) aItem;
            }
            else {
                category = null;
            }
            StyleManager.changeStyle(this, category);
            pseudoClassStateChanged(StyleManager.CSS_PC_UNEQUIPPABLE, false);
            pseudoClassStateChanged(StyleManager.CSS_PC_SMARTPLACEABLE, false);
        }
    }
}
