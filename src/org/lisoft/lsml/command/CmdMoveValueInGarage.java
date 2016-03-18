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
import org.lisoft.lsml.model.NamedObject;
import org.lisoft.lsml.model.garage.GarageDirectory;
import org.lisoft.lsml.model.loadout.EquipException;
import org.lisoft.lsml.util.CommandStack.CompositeCommand;

/**
 * This command will move a value from one {@link GarageDirectory} to another.
 * 
 * @author Li Song
 * @param <T>
 *            The type of the value to move.
 */
public class CmdMoveValueInGarage<T extends NamedObject> extends CompositeCommand {
    private final GarageDirectory<T> src;
    private final GarageDirectory<T> dst;
    private final T                  value;

    public CmdMoveValueInGarage(MessageDelivery aMessageTarget, T aValue, GarageDirectory<T> aDestination,
            GarageDirectory<T> aSource) {
        super("move in garage", aMessageTarget);
        src = aSource;
        dst = aDestination;
        value = aValue;
    }

    @Override
    protected void buildCommand() throws EquipException {
        addOp(new CmdRemoveFromGarage(messageBuffer, src, value));
        addOp(new CmdAddToGarage<>(messageBuffer, dst, value));
    }
}
