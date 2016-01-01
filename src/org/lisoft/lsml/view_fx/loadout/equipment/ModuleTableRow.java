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

import org.lisoft.lsml.command.CmdAddModule;
import org.lisoft.lsml.messages.MessageDelivery;
import org.lisoft.lsml.model.item.Equipment;
import org.lisoft.lsml.model.item.PilotModule;
import org.lisoft.lsml.model.loadout.EquipResult;
import org.lisoft.lsml.model.loadout.LoadoutBase;
import org.lisoft.lsml.util.CommandStack;
import org.lisoft.lsml.view_fx.LiSongMechLab;
import org.lisoft.lsml.view_fx.style.StyleManager;

import javafx.scene.control.TreeTableRow;
import javafx.scene.input.Dragboard;
import javafx.scene.input.MouseButton;
import javafx.scene.input.TransferMode;

/**
 * Fixes styles for equipment rendering in the loadout window.
 * 
 * @author Emily Björk
 *
 */
public class ModuleTableRow extends TreeTableRow<Object> {
    private final LoadoutBase<?> loadout;

    public ModuleTableRow(LoadoutBase<?> aLoadout, CommandStack aCommandStack, MessageDelivery aMessageDelivery) {
        loadout = aLoadout;
        setOnDragDetected(aEvent -> {
            PilotModule item = getValueAsItem();
            if (null == item)
                return;
            Dragboard db = startDragAndDrop(TransferMode.COPY);
            LiSongMechLab.addEquipmentDrag(db, item);
            aEvent.consume();
        });

        setOnMouseClicked(aEvent -> {
            if (MouseButton.PRIMARY == aEvent.getButton() && 2 == aEvent.getClickCount()) {
                PilotModule item = getValueAsItem();
                if (null == item)
                    return;
                LiSongMechLab.safeCommand(this, aCommandStack, new CmdAddModule(aMessageDelivery, loadout, item));
            }
            aEvent.consume();
        });
    }

    private PilotModule getValueAsItem() {
        Object object = getItem();
        if (!(object instanceof PilotModule))
            return null;
        return (PilotModule) object;
    }

    @Override
    protected void updateItem(Object aItem, boolean aEmpty) {
        super.updateItem(aItem, aEmpty);

        final EquipmentCategory category;
        if (aItem instanceof EquipmentCategory) {
            category = (EquipmentCategory) aItem;
            StyleManager.changeStyle(this, category);
            pseudoClassStateChanged(StyleManager.CSS_PC_UNEQUIPPABLE, false);
        }
        else if (aItem instanceof PilotModule) {
            PilotModule pilotModule = (PilotModule) aItem;

            boolean equippable = loadout.canAddModule(pilotModule) == EquipResult.SUCCESS;
            pseudoClassStateChanged(StyleManager.CSS_PC_UNEQUIPPABLE, !equippable);
            category = EquipmentCategory.classify(((Equipment) aItem));

            StyleManager.changeListStyle(this, category);
        }
        else {
            category = null;
            StyleManager.changeListStyle(this, category);
            pseudoClassStateChanged(StyleManager.CSS_PC_UNEQUIPPABLE, false);
        }
    }
}
