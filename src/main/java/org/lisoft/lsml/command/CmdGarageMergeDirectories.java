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
import org.lisoft.lsml.model.garage.GaragePath;
import org.lisoft.lsml.model.loadout.EquipException;
import org.lisoft.lsml.util.CommandStack.Command;
import org.lisoft.lsml.util.CommandStack.CompositeCommand;

/**
 * This {@link Command} will take the contents from the source {@link GarageDirectory} and merge them into the
 * destination such that all folders and values from the source are added to the destination unless they already exist.
 * <p>
 * Folders are compared as case insensitive and values are compared by <code>equals(Object)</code>.
 *
 * @param <T> The type of the values in the garage directories to merge.
 * @author Li Song
 */
public class CmdGarageMergeDirectories<T extends NamedObject> extends CompositeCommand {
    private final GaragePath<T> dst;
    private final GaragePath<T> src;

    public CmdGarageMergeDirectories(String aDescription, MessageDelivery aMessageTarget, GaragePath<T> aDstPath,
                                     GaragePath<T> aSrcPath) {
        super(aDescription, aMessageTarget);
        dst = aDstPath;
        src = aSrcPath;
    }

    @Override
    protected void buildCommand() throws EquipException {
        merge(dst, src);
    }

    void merge(GaragePath<T> aDst, GaragePath<T> aSrc) {
        for (final T value : aSrc.getTopDirectory().getValues()) {
            if (!aDst.getTopDirectory().getValues().contains(value)) {
                addOp(new CmdGarageAdd<>(messageBuffer, aDst, value));
            }
        }

        for (final GarageDirectory<T> srcChild : aSrc.getTopDirectory().getDirectories()) {
            boolean found = false;
            for (final GarageDirectory<T> dstChild : aDst.getTopDirectory().getDirectories()) {
                if (dstChild.getName().equals(srcChild.getName())) {
                    merge(new GaragePath<>(dst, dstChild), new GaragePath<>(src, srcChild));
                    found = true;
                    break;
                }
            }
            if (!found) {
                final GarageDirectory<T> dstChild = new GarageDirectory<>(srcChild.getName());
                addOp(new CmdGarageAddDirectory<>(messageBuffer, aDst, dstChild));
                merge(new GaragePath<>(dst, dstChild), new GaragePath<>(src, srcChild));
            }
        }
    }
}
