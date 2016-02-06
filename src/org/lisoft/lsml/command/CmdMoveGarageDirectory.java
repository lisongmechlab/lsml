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
import org.lisoft.lsml.model.garage.GarageDirectory;

/**
 * This class will move a directory from one directory to another one.
 * 
 * @author Emily Björk
 * @param <T>
 *            The type of garage directory to move.
 */
public class CmdMoveGarageDirectory<T> extends MessageCommand {
    private final GarageDirectory<T> dst;
    private final GarageDirectory<T> dir;
    private final GarageDirectory<T> parent;

    /**
     * Creates a new garage move directory command.
     * 
     * @param aDelivery
     *            Where to post messages that affect the garage.
     * @param aDestination
     *            Where to move the directory.
     * @param aDirectory
     *            The directory to move.
     * @param aDirParent
     *            The parent containing the directory to move.
     */
    public CmdMoveGarageDirectory(MessageDelivery aDelivery, GarageDirectory<T> aDestination,
            GarageDirectory<T> aDirectory, GarageDirectory<T> aDirParent) {
        super(aDelivery);
        dst = aDestination;
        dir = aDirectory;
        parent = aDirParent;
    }

    @Override
    public String describe() {
        return "move garage folder";
    }

    @Override
    protected void apply() throws Exception {
        parent.getDirectories().remove(dir);
        dst.getDirectories().add(dir);
        post(new GarageMessage(GarageMessageType.MOVED));
    }

    @Override
    protected void undo() {
        dst.getDirectories().remove(dir);
        parent.getDirectories().add(dir);
        post(new GarageMessage(GarageMessageType.MOVED));
    }
}
