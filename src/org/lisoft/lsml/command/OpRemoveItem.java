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
import org.lisoft.lsml.model.loadout.LoadoutBase;
import org.lisoft.lsml.model.loadout.component.ConfiguredComponentBase;
import org.lisoft.lsml.util.OperationStack.Operation;
import org.lisoft.lsml.util.message.MessageDelivery;

/**
 * This {@link Operation} removes an {@link Item} from a {@link ConfiguredComponentBase}.
 * 
 * @author Li Song
 */
public class OpRemoveItem extends OpItemBase {
    /**
     * Creates a new operation.
     * 
     * @param aMessageDelivery
     *            The {@link MessageDelivery} to send messages on when items are removed.
     * @param aLoadout
     *            The {@link LoadoutBase} to remove the item from.
     * @param aComponent
     *            The {@link ConfiguredComponentBase} to remove from.
     * @param aItem
     *            The {@link Item} to remove.
     */
    public OpRemoveItem(MessageDelivery aMessageDelivery, LoadoutBase<?> aLoadout, ConfiguredComponentBase aComponent,
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
        if (!(obj instanceof OpRemoveItem))
            return false;
        OpRemoveItem other = (OpRemoveItem) obj;
        return item == other.item && super.equals(other);
    }

    @Override
    public String describe() {
        return "remove " + item.getName() + " from " + component.getInternalComponent().getLocation();
    }

    @Override
    public void undo() {
        addItem(item);
    }

    @Override
    public void apply() {
        if (!component.canRemoveItem(item))
            throw new IllegalArgumentException("Can not remove item: " + item + " from " + component);
        removeItem(item);
    }
}
