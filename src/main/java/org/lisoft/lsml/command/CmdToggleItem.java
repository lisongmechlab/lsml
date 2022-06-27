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
package org.lisoft.lsml.command;

import org.lisoft.lsml.messages.ItemMessage;
import org.lisoft.lsml.messages.ItemMessage.Type;
import org.lisoft.lsml.messages.MessageDelivery;
import org.lisoft.lsml.model.database.ItemDB;
import org.lisoft.lsml.model.item.Item;
import org.lisoft.lsml.model.loadout.ConfiguredComponentOmniMech;
import org.lisoft.lsml.model.loadout.EquipException;
import org.lisoft.lsml.model.loadout.EquipResult;
import org.lisoft.lsml.model.loadout.EquipResult.EquipResultType;
import org.lisoft.lsml.model.loadout.Loadout;
import org.lisoft.lsml.util.CommandStack.Command;

/**
 * This operation toggles the state of toggleable items, for now HA/LAA.
 *
 * @author Li Song
 */
public class CmdToggleItem implements Command {
    private final ConfiguredComponentOmniMech component;
    private final Item item;
    private final Loadout loadout;
    private final MessageDelivery messageDelivery;
    private final boolean newState;
    private boolean oldHAState;
    private boolean oldState;

    public CmdToggleItem(MessageDelivery aMessageDelivery, Loadout aLoadout, ConfiguredComponentOmniMech aComponent,
                         Item aItem, boolean aNewState) {
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
    public void apply() throws EquipException {
        oldState = component.getToggleState(item);
        oldHAState = component.getToggleState(ItemDB.HA);

        if (newState == oldState) {
            return;
        }

        if (newState == true) {
            if (item == ItemDB.HA && false == component.getToggleState(ItemDB.LAA)) {
                EquipException.checkAndThrow(
                        EquipResult.make(component.getInternalComponent().getLocation(), EquipResultType.LaaBeforeHa));
            }

            if (loadout.getFreeSlots() < 1) {
                EquipException.checkAndThrow(EquipResult.make(EquipResultType.NotEnoughSlots));
            }
            final EquipResult e = component.canToggleOn(item);
            EquipException.checkAndThrow(e);
        }

        if (item == ItemDB.LAA && newState == false && component.getToggleState(ItemDB.HA)) {
            component.setToggleState(ItemDB.HA, false);
            post(Type.Removed, ItemDB.HA);
        }

        component.setToggleState(item, newState);
        post(newState ? Type.Added : Type.Removed, item);
    }

    @Override
    public String describe() {
        return "toggle " + item.getName();
    }

    @Override
    public void undo() {
        if (newState == oldState) {
            return;
        }
        if (oldHAState) {
            component.setToggleState(ItemDB.HA, true);
            post(Type.Added, ItemDB.HA);
        }
        component.setToggleState(item, oldState);
        post(oldState ? Type.Added : Type.Removed, item);
    }

    private void post(Type aType, Item aItem) {
        if (messageDelivery != null) {
            messageDelivery.post(new ItemMessage(component, aType, aItem, -1));
        }
    }
}
