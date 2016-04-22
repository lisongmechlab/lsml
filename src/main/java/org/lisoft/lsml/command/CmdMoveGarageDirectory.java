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
 * This class will move a directory from one directory to another one.
 *
 * @author Li Song
 * @param <T>
 *            The type of garage directory to move.
 */
public class CmdMoveGarageDirectory<T extends NamedObject> extends CompositeCommand {
    private final GarageDirectory<T> dstParent;
    private final GarageDirectory<T> dir;
    private final GarageDirectory<T> srcParent;

    /**
     * Creates a new garage move directory command.
     *
     * @param aDelivery
     *            Where to post messages that affect the garage.
     * @param aDstParent
     *            Where to move the directory.
     * @param aDirectory
     *            The directory to move.
     * @param aSrcParent
     *            The parent containing the directory to move.
     */
    public CmdMoveGarageDirectory(MessageDelivery aDelivery, GarageDirectory<T> aDstParent,
            GarageDirectory<T> aDirectory, GarageDirectory<T> aSrcParent) {
        super("move garage folder", aDelivery);
        dstParent = aDstParent;
        dir = aDirectory;
        srcParent = aSrcParent;
    }

    @Override
    protected void buildCommand() throws EquipException {
        addOp(new CmdRemoveGarageDirectory<>(messageBuffer, dir, srcParent));
        addOp(new CmdAddGarageDirectory<>(messageBuffer, dir, dstParent));
    }
}
