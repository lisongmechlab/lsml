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

import org.lisoft.lsml.messages.GarageMessage;
import org.lisoft.lsml.messages.GarageMessageType;
import org.lisoft.lsml.messages.MessageDelivery;
import org.lisoft.lsml.model.NamedObject;
import org.lisoft.lsml.model.garage.GarageDirectory;
import org.lisoft.lsml.model.garage.GarageException;
import org.lisoft.lsml.model.garage.GaragePath;

/**
 * This operation adds a new {@link NamedObject} to a {@link GarageDirectory}.
 *
 * @author Emily Björk
 * @param <T>
 *            The type of the object to add.
 */
public class CmdGarageAdd<T extends NamedObject> extends MessageCommand {
    private final GaragePath<T> dstPath;
    private final T value;

    /**
     * Creates a new operation to add the given value under the destination path. The destination path must refer to a
     * directory. Otherwise {@link #apply()} will throw.
     *
     * @param aDelivery
     *            A {@link MessageDelivery} to send messages on.
     * @param aPath
     *            The destination path to add the new value under.
     * @param aValue
     *            The new value to add.
     */
    public CmdGarageAdd(MessageDelivery aDelivery, GaragePath<T> aPath, T aValue) {
        super(aDelivery);
        dstPath = aPath;
        value = aValue;
    }

    @Override
    public void apply() throws GarageException {
        if (dstPath.isLeaf()) {
            throw new GarageException("Destination is not a directory!");
        }

        final GarageDirectory<T> garageDirectory = dstPath.getTopDirectory();
        if (!GaragePath.isNameAvailalble(dstPath, value.getName())) {
            throw new GarageException("A entry with the name \"" + value.toString() + "\" already exists!");
        }
        garageDirectory.getValues().add(value);
        post(new GarageMessage<>(GarageMessageType.ADDED, new GaragePath<>(dstPath, value)));
    }

    @Override
    public String describe() {
        return "add " + value.toString() + " to garage";
    }

    @Override
    public void undo() {
        dstPath.getTopDirectory().getValues().remove(value);
        post(new GarageMessage<>(GarageMessageType.REMOVED, new GaragePath<>(dstPath, value)));
    }
}
