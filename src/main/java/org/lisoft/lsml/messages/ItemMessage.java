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
package org.lisoft.lsml.messages;

import org.lisoft.lsml.model.item.Item;
import org.lisoft.lsml.model.loadout.ConfiguredComponent;
import org.lisoft.lsml.model.loadout.Loadout;

/**
 * This message is sent when an item is added to or removed from the loadout.
 * 
 * @author Li Song
 */
public class ItemMessage implements Message {
    public static enum Type {
        Added, Removed
    }

    public final ConfiguredComponent component;
    public final Type type;
    public final Item item;
    public final int relativeIndex;

    @Override
    public String toString() {
        return item.getName() + " " + type + " to " + component.getInternalComponent().getLocation() + " at "
                + relativeIndex;
    }

    /**
     * Creates a new {@link ItemMessage}.
     * 
     * @param aComponent
     *            The component the affect item is/was on.
     * @param aItem
     *            The affected item.
     * @param aRelativeIndex
     *            The index among the equipped items of the affected component of the new or old position for addition
     *            or removal respectively. For toggleable items, negative values are used.
     * @param aType
     *            The {@link Type} of the message.
     */
    public ItemMessage(ConfiguredComponent aComponent, Type aType, Item aItem, int aRelativeIndex) {
        component = aComponent;
        type = aType;
        item = aItem;
        relativeIndex = aRelativeIndex;
    }

    /*
     * (non-Javadoc)
     * 
     * @see java.lang.Object#hashCode()
     */
    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((component == null) ? 0 : component.hashCode());
        result = prime * result + ((item == null) ? 0 : item.hashCode());
        result = prime * result + relativeIndex;
        result = prime * result + ((type == null) ? 0 : type.hashCode());
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (!(obj instanceof ItemMessage))
            return false;
        ItemMessage other = (ItemMessage) obj;
        if (component != other.component)
            return false;
        if (item != other.item)
            return false;
        if (relativeIndex != other.relativeIndex)
            return false;
        if (type != other.type)
            return false;
        return true;
    }

    @Override
    public boolean isForMe(Loadout aLoadout) {
        return aLoadout.getComponent(component.getInternalComponent().getLocation()) == component;
    }

    @Override
    public boolean affectsHeatOrDamage() {
        // For now, the majority of items has some effect on heat or damage.
        // We accept the unnecessary updates for items that don't really affect the
        // heat or damage.
        return true;
    }

}
