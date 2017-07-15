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
import org.lisoft.lsml.model.item.Ammunition;
import org.lisoft.lsml.model.item.Item;
import org.lisoft.lsml.model.loadout.EquipException;
import org.lisoft.lsml.model.loadout.Loadout;
import org.lisoft.lsml.model.loadout.LoadoutFactory;
import org.lisoft.lsml.util.CommandStack.CompositeCommand;

/**
 * This command will fill the given {@link Loadout} with as many of a given {@link Item} as possible.
 *
 * @author Li Song
 */
public class CmdFillWithItem extends CompositeCommand {

    private final Loadout loadout;
    private final Item item;
    private final Item half;
    private final LoadoutFactory loadoutFactory;

    /**
     * Creates a new command to fill with ammunition. Will also add half-tonners if provided.
     *
     * @param aDelivery
     *            The {@link MessageDelivery} to send messages this command generates on.
     * @param aLoadout
     *            The {@link Loadout} to fill.
     * @param aAmmoType
     *            The {@link Ammunition} to fill with.
     * @param aAmmoHalfType
     *            The {@link Ammunition} to fill with half tons with.
     * @param aLoadoutFactory
     *            A {@link LoadoutFactory} used to construct copies in the search process to fill the {@link Loadout}.
     */
    public CmdFillWithItem(MessageDelivery aDelivery, Loadout aLoadout, Ammunition aAmmoType, Ammunition aAmmoHalfType,
            LoadoutFactory aLoadoutFactory) {
        super("fill with ammo", aDelivery);
        loadout = aLoadout;
        item = aAmmoType;
        half = aAmmoHalfType;
        loadoutFactory = aLoadoutFactory;
    }

    /**
     * Creates a new command.
     *
     * @param aDelivery
     *            The {@link MessageDelivery} to send messages this command generates on.
     * @param aLoadout
     *            The {@link Loadout} to fill.
     * @param aItem
     *            The {@link Item} to fill with.
     * @param aLoadoutFactory
     *            A {@link LoadoutFactory} used to construct copies in the search process to fill the {@link Loadout}.
     */
    public CmdFillWithItem(MessageDelivery aDelivery, Loadout aLoadout, Item aItem, LoadoutFactory aLoadoutFactory) {
        super("fill with ammo", aDelivery);
        loadout = aLoadout;
        item = aItem;
        half = null;
        loadoutFactory = aLoadoutFactory;
    }

    @Override
    protected void buildCommand() throws EquipException {
        final int maxByTonnage = (int) (loadout.getFreeMass() / item.getMass());
        final int maxBySlots = loadout.getFreeSlots() / item.getSlots();
        int toAdd = Math.min(maxByTonnage, maxBySlots);

        while (toAdd-- > 0) {
            addOp(new CmdAutoAddItem(loadout, messageBuffer, item, true, loadoutFactory));
        }

        if (half != null) {
            addOp(new CmdAutoAddItem(loadout, messageBuffer, half, true, loadoutFactory));
        }
    }
}
