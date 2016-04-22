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
import org.lisoft.lsml.model.garage.GarageDirectory;
import org.lisoft.lsml.model.garage.GarageException;

/**
 * This operation renames a loadout.
 *
 * @author Li Song
 * @param <T>
 *            The type of the object to rename.
 */
public class CmdRename<T extends NamedObject> extends MessageCommand {
    private final T object;
    private final String newName;
    private String oldName;
    private final GarageDirectory<T> parentDir;

    /**
     * @param aNamedObject
     *            The {@link NamedObject} to rename.
     * @param aMessageDelivery
     *            A {@link MessageDelivery} to announce the change on.
     * @param aName
     *            The new name of the loadout.
     * @param aParentDir
     *            The directory that contains this loadout or empty.
     */
    public CmdRename(T aNamedObject, MessageDelivery aMessageDelivery, String aName, GarageDirectory<T> aParentDir) {
        super(aMessageDelivery);
        object = aNamedObject;
        newName = aName;
        parentDir = aParentDir;
    }

    @Override
    public void apply() throws GarageException {
        if (parentDir != null) {
            for (final T sibling : parentDir.getValues()) {
                if (sibling.getName().equalsIgnoreCase(newName)) {
                    throw new GarageException("A value with that name already exists!");
                }
            }
        }

        oldName = object.getName();
        if (oldName == newName) {
            return;
        }
        object.setName(newName);
        post(new GarageMessage<>(GarageMessageType.RENAMED, parentDir, object));
    }

    @Override
    public String describe() {
        return "rename loadout";
    }

    @Override
    public void undo() {
        if (oldName == object.getName()) {
            return;
        }
        object.setName(oldName);
        post(new GarageMessage<>(GarageMessageType.RENAMED, parentDir, object));
    }
}
