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

import org.lisoft.lsml.messages.GarageMessage;
import org.lisoft.lsml.messages.GarageMessageType;
import org.lisoft.lsml.messages.MessageDelivery;
import org.lisoft.lsml.model.NamedObject;
import org.lisoft.lsml.model.garage.GarageException;
import org.lisoft.lsml.model.garage.GaragePath;

/**
 * This operation renames a loadout.
 *
 * @author Li Song
 * @param <T>
 *            The type of the object to rename.
 */
public class CmdGarageRename<T extends NamedObject> extends MessageCommand {
    private final String newName;
    private String oldName;
    private final GaragePath<T> path;

    /**
     * @param aMessageDelivery
     *            A {@link MessageDelivery} to announce the change on.
     * @param aNewName
     *            The new name of the loadout.
     * @param aPath
     *            A path to the object to rename.
     */
    public CmdGarageRename(MessageDelivery aMessageDelivery, GaragePath<T> aPath, String aNewName) {
        super(aMessageDelivery);
        newName = aNewName;
        path = aPath;
    }

    @Override
    public void apply() throws GarageException {
        if (!path.isRoot()) {
            if (!GaragePath.isNameAvailalble(path.getParent(), newName)) {
                throw new GarageException("A value with the name \"" + newName.toString() + "\" already exists!");
            }
        }

        if (path.isLeaf()) {
            final T object = path.getValue().get();
            oldName = object.getName();
            object.setName(newName);
        }
        else {
            oldName = path.getTopDirectory().getName();
            path.getTopDirectory().setName(newName);
        }
        post(new GarageMessage<>(GarageMessageType.RENAMED, path));
    }

    @Override
    public String describe() {
        return "rename";
    }

    @Override
    public void undo() {
        if (path.isLeaf()) {
            final T object = path.getValue().get();
            object.setName(oldName);
        }
        else {
            path.getTopDirectory().setName(oldName);
        }
        post(new GarageMessage<>(GarageMessageType.RENAMED, path));
    }
}
