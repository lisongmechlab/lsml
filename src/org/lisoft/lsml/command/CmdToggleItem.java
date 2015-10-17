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
package org.lisoft.lsml.command;

import org.lisoft.lsml.messages.ComponentMessage;
import org.lisoft.lsml.messages.ComponentMessage.Type;
import org.lisoft.lsml.messages.MessageDelivery;
import org.lisoft.lsml.model.item.Item;
import org.lisoft.lsml.model.item.ItemDB;
import org.lisoft.lsml.model.loadout.EquipResult;
import org.lisoft.lsml.model.loadout.EquipResult.EquipResultType;
import org.lisoft.lsml.model.loadout.LoadoutBase;
import org.lisoft.lsml.model.loadout.component.ConfiguredComponentOmniMech;
import org.lisoft.lsml.util.CommandStack.Command;

/**
 * This operation toggles the state of toggleable items, for now HA/LAA.
 * 
 * @author Emily Björk
 */
public class CmdToggleItem extends Command {
    private final Item                        item;
    private final MessageDelivery             messageDelivery;
    private final LoadoutBase<?>              loadout;
    private final ConfiguredComponentOmniMech component;
    private final boolean                     newState;
    private boolean                           oldState;
    private boolean                           oldHAState;

    public CmdToggleItem(MessageDelivery aMessageDelivery, LoadoutBase<?> aLoadout,
            ConfiguredComponentOmniMech aComponent, Item aItem, boolean aNewState) {
        if (aItem != ItemDB.HA && aItem != ItemDB.LAA) {
            throw new IllegalArgumentException("Can't toggle anything but HA/LAA");
        }
        messageDelivery = aMessageDelivery;
        item = aItem;
        loadout = aLoadout;
        component = aComponent;
        newState = aNewState;
    }

    @Override
    public String describe() {
        return "toggle " + item;
    }

    @Override
    protected void apply() throws EquipResult {
        oldState = component.getToggleState(item);
        oldHAState = component.getToggleState(ItemDB.HA);

        if (newState == oldState)
            return;

        if (newState == true) {
            if (loadout.getNumCriticalSlotsFree() < 1) {
                throw EquipResult.make(EquipResultType.NotEnoughSlots);
            }
            EquipResult e = component.canToggleOn(item);
            if (e != EquipResult.SUCCESS) {
                throw e;
            }
        }

        component.setToggleState(item, newState);

        if (item == ItemDB.LAA && newState == false && component.getToggleState(ItemDB.HA)) {
            component.setToggleState(ItemDB.HA, false);
        }

        if (messageDelivery != null) {
            messageDelivery.post(new ComponentMessage(component, Type.ItemsChanged));
        }
    }

    @Override
    protected void undo() {
        if (newState == oldState)
            return;
        component.setToggleState(item, oldState);
        if (item == ItemDB.LAA && oldHAState == true && EquipResult.SUCCESS == component.canToggleOn(ItemDB.HA)) {
            component.setToggleState(ItemDB.HA, true);
        }
        if (messageDelivery != null) {
            messageDelivery.post(new ComponentMessage(component, Type.ItemsChanged));
        }
    }
}
