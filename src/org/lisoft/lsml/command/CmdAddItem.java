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

import org.lisoft.lsml.model.item.Item;
import org.lisoft.lsml.model.loadout.EquipResult;
import org.lisoft.lsml.model.loadout.LoadoutBase;
import org.lisoft.lsml.model.loadout.component.ConfiguredComponentBase;
import org.lisoft.lsml.util.CommandStack.Command;
import org.lisoft.lsml.util.message.MessageDelivery;

/**
 * This {@link Command} adds an {@link Item} to a {@link ConfiguredComponentBase}.
 * 
 * @author Li Song
 */
public class CmdAddItem extends CmdItemBase {
    /**
     * Creates a new operation.
     * 
     * @param aMessageDelivery
     *            The {@link MessageDelivery} to send messages on when items are added.
     * @param aLoadout
     *            The {@link LoadoutBase} to remove the item from.
     * @param aComponent
     *            The {@link ConfiguredComponentBase} to add to.
     * @param aItem
     *            The {@link Item} to add.
     */
    public CmdAddItem(MessageDelivery aMessageDelivery, LoadoutBase<?> aLoadout, ConfiguredComponentBase aComponent,
            Item aItem) {
        super(aMessageDelivery, aLoadout, aComponent, aItem);
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = super.hashCode();
        result = prime * result + ((item == null) ? 0 : item.hashCode());
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (!(obj instanceof CmdRemoveItem))
            return false;
        CmdAddItem other = (CmdAddItem) obj;
        return item == other.item && super.equals(other);
    }

    @Override
    public String describe() {
        return "add " + item.getName() + " to " + component.getInternalComponent().getLocation();
    }

    @Override
    public void undo() {
        removeItem(item);
    }

    @Override
    public void apply() throws EquipResult {
        EquipResult result = loadout.canEquip(item);
        result.checkFailureAndThrow();

        result = component.canEquip(item);
        result.checkFailureAndThrow();
        addItem(item);
    }
}
