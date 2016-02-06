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

import java.util.Objects;

import org.lisoft.lsml.messages.GarageMessage;
import org.lisoft.lsml.messages.GarageMessageType;
import org.lisoft.lsml.messages.MessageDelivery;
import org.lisoft.lsml.model.garage.DropShip;
import org.lisoft.lsml.model.garage.GarageDirectory;
import org.lisoft.lsml.model.garage.GarageException;
import org.lisoft.lsml.model.loadout.Loadout;
import org.lisoft.lsml.model.loadout.LoadoutStandard;

/**
 * This operation adds a new {@link LoadoutStandard} to a {@link GarageDirectory}.
 * 
 * @author Li Song
 * @param <T>
 *            The type to add to the garage. Must be {@link Loadout} or {@link DropShip}.
 */
public class CmdAddToGarage<T> extends MessageCommand {
    private final GarageDirectory<T> garageDirectory;
    private final T                  value;

    public CmdAddToGarage(MessageDelivery aDelivery, GarageDirectory<T> aGarageDirectory, T aValue) {
        super(aDelivery);
        garageDirectory = Objects.requireNonNull(aGarageDirectory);
        value = Objects.requireNonNull(aValue);
    }

    @Override
    public String describe() {
        return "add " + value.toString() + " to garage";
    }

    @Override
    protected void apply() throws GarageException {
        if (garageDirectory.getValues().contains(value)) {
            throw new GarageException("The loadout \"" + value.toString() + "\" already exists!");
        }
        garageDirectory.getValues().add(value);
        post(new GarageMessage(GarageMessageType.ADDED, garageDirectory, value));
    }

    @Override
    protected void undo() {
        garageDirectory.getValues().remove(value);
        post(new GarageMessage(GarageMessageType.REMOVED, garageDirectory, value));
    }
}
