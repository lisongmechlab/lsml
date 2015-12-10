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

import org.lisoft.lsml.messages.ItemMessage;
import org.lisoft.lsml.messages.ItemMessage.Type;
import org.lisoft.lsml.messages.MessageDelivery;
import org.lisoft.lsml.model.chassi.Location;
import org.lisoft.lsml.model.item.Engine;
import org.lisoft.lsml.model.item.EngineType;
import org.lisoft.lsml.model.item.Internal;
import org.lisoft.lsml.model.item.Item;
import org.lisoft.lsml.model.loadout.LoadoutBase;
import org.lisoft.lsml.model.loadout.LoadoutStandard;
import org.lisoft.lsml.model.loadout.component.ConfiguredComponentBase;
import org.lisoft.lsml.util.CommandStack.Command;

/**
 * A helper class for implementing {@link Command}s that affect items on a {@link ConfiguredComponentBase}.
 * 
 * @author Emily Björk
 */
public abstract class CmdItemBase extends Command {
    protected final MessageDelivery         messageDelivery;
    protected final ConfiguredComponentBase component;
    protected final LoadoutBase<?>          loadout;
    protected final Item                    item;

    /**
     * Creates a new {@link CmdItemBase}. The deriving classes shall throw if the the operation with the given item
     * would violate the {@link LoadoutStandard} or {@link ConfiguredComponentBase} invariant.
     * 
     * @param aMessageDelivery
     *            The {@link MessageDelivery} to send messages to when changes occur.
     * @param aLoadout
     *            The {@link LoadoutBase} to operate on.
     * @param aComponent
     *            The {@link ConfiguredComponentBase} that this operation will affect.
     * @param aItem
     *            The {@link Item} to add or remove.
     */
    protected CmdItemBase(MessageDelivery aMessageDelivery, LoadoutBase<?> aLoadout, ConfiguredComponentBase aComponent,
            Item aItem) {
        loadout = aLoadout;
        component = aComponent;
        messageDelivery = aMessageDelivery;
        item = aItem;
    }

    protected void add(ConfiguredComponentBase aComponent, Item aItem) {
        int index = aComponent.addItem(aItem);
        post(aComponent, Type.Added, aItem, index);
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((component == null) ? 0 : component.hashCode());
        result = prime * result + ((item == null) ? 0 : item.hashCode());
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (!(obj instanceof CmdItemBase))
            return false;
        CmdItemBase other = (CmdItemBase) obj;
        if (component != other.component)
            return false;
        if (item != other.item)
            return false;
        return true;
    }

    protected void post(ConfiguredComponentBase aComponent, Type aType, Item aItem, int aIndex) {
        if (messageDelivery != null) {
            messageDelivery.post(new ItemMessage(aComponent, aType, aItem, aIndex));
        }
    }

    protected void remove(ConfiguredComponentBase aComponent, Item aItem) {
        int index = aComponent.removeItem(aItem);
        post(aComponent, Type.Removed, aItem, index);
    }

    protected void addXLSides(Engine engine) {
        if (engine.getType() == EngineType.XL) {
            ConfiguredComponentBase lt = loadout.getComponent(Location.LeftTorso);
            ConfiguredComponentBase rt = loadout.getComponent(Location.RightTorso);

            Internal xlSide = engine.getSide();
            add(lt, xlSide);
            add(rt, xlSide);
        }
    }

    protected void removeXLSides(Engine engine) {
        if (engine.getType() == EngineType.XL) {
            ConfiguredComponentBase lt = loadout.getComponent(Location.LeftTorso);
            ConfiguredComponentBase rt = loadout.getComponent(Location.RightTorso);

            Internal xlSide = engine.getSide();
            remove(lt, xlSide);
            remove(rt, xlSide);
        }
    }
}
