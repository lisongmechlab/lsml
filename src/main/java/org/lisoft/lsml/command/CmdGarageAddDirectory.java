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
 * This class adds a new directory under the given garage directory.
 *
 * @author Emily Björk
 * @param <T>
 *            The value type of the {@link GarageDirectory}.
 */
public class CmdGarageAddDirectory<T extends NamedObject> extends MessageCommand {
    private final GarageDirectory<T> dir;
    private final GaragePath<T> dstPath;

    /**
     * Creates a new command to add the given directory under the directory denoted by the destination path. If the
     * destination path doesn't refer to a directory, then {@link #apply()} will throw.
     *
     * @param aDelivery
     *            A {@link MessageDelivery} to send messages on.
     * @param aDestPath
     *            The destination path to add the new directory under.
     * @param aNewDir
     *            The new directory to add.
     */
    public CmdGarageAddDirectory(MessageDelivery aDelivery, GaragePath<T> aDestPath, GarageDirectory<T> aNewDir) {
        super(aDelivery);
        dir = aNewDir;
        dstPath = aDestPath;
    }

    @Override
    public void apply() throws GarageException {
        if (dstPath.isLeaf()) {
            throw new GarageException("Destination is not a directory!");
        }

        final GarageDirectory<T> parent = dstPath.getTopDirectory();
        if (!GaragePath.isNameAvailalble(dstPath, dir.getName())) {
            throw new GarageException("A directory with the name \"" + dir.toString() + "\" already exists!");
        }
        parent.getDirectories().add(dir);
        post(new GarageMessage<>(GarageMessageType.ADDED, new GaragePath<>(dstPath, dir)));
    }

    @Override
    public String describe() {
        return "make directory " + dir.getName();
    }

    @Override
    public void undo() {
        dstPath.getTopDirectory().getDirectories().remove(dir);
        post(new GarageMessage<>(GarageMessageType.REMOVED, new GaragePath<>(dstPath, dir)));
    }
}
