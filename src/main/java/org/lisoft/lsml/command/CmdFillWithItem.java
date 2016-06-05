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

import org.lisoft.lsml.messages.MessageDelivery;
import org.lisoft.lsml.model.datacache.ItemDB;
import org.lisoft.lsml.model.item.Ammunition;
import org.lisoft.lsml.model.item.Item;
import org.lisoft.lsml.model.loadout.EquipException;
import org.lisoft.lsml.model.loadout.Loadout;
import org.lisoft.lsml.util.CommandStack.CompositeCommand;

/**
 * This command will fill the given {@link Loadout} with as much {@link Ammunition} as possible of the given type.
 *
 * @author Li Song
 */
public class CmdFillWithItem extends CompositeCommand {

    private final Loadout loadout;
    private final Item item;
    private final Item half;

    /**
     * Creates a new command.
     *
     * @param aDelivery
     *            The {@link MessageDelivery} to send messages this command generates on.
     * @param aLoadout
     *            The {@link Loadout} to fill.
     * @param aItem
     *            The {@link Ammunition} to fill with.
     */
    public CmdFillWithItem(MessageDelivery aDelivery, Loadout aLoadout, Item aItem) {
        super("fill with ammo", aDelivery);
        loadout = aLoadout;
        item = aItem;
        if (item instanceof Ammunition) {
            half = ItemDB.lookupHalfAmmo((Ammunition) item);
        }
        else {
            half = null;
        }
    }

    @Override
    protected void buildCommand() throws EquipException {
        final int maxByTonnage = (int) (loadout.getFreeMass() / item.getMass());
        final int maxBySlots = loadout.getFreeSlots() / item.getSlots();
        int toAdd = Math.min(maxByTonnage, maxBySlots);

        while (toAdd-- > 0) {
            addOp(new CmdAutoAddItem(loadout, messageBuffer, item, true));
        }

        if (half != null) {
            addOp(new CmdAutoAddItem(loadout, messageBuffer, half, true));
        }
    }
}
