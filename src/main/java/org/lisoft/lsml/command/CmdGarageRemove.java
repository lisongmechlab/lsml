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
import org.lisoft.lsml.model.garage.GaragePath;

/**
 * Removes a {@link NamedObject} from a {@link GarageDirectory}.
 *
 * @param <T> The type of the value to remove.
 * @author Li Song
 */
public class CmdGarageRemove<T extends NamedObject> extends MessageCommand {
    private final GaragePath<T> path;

    public CmdGarageRemove(MessageDelivery aDelivery, GaragePath<T> aPath) {
        super(aDelivery);
        path = aPath;
    }

    @Override
    public void apply() throws GarageException {
        if (path.isLeaf()) {
            if (!path.getTopDirectory().getValues().remove(path.getValue().get())) {
                throw new GarageException("The object to be deleted: \"" + path + "\" doesn't exist!");
            }
        } else {
            final GaragePath<T> parent = path.getParent();
            if (null == parent) {
                throw new GarageException("Cannot remove the root!");
            }
            final GarageDirectory<T> parentDir = parent.getTopDirectory();

            if (!parentDir.getDirectories().remove(path.getTopDirectory())) {
                throw new GarageException("The directory to be deleted: \"" + path + "\" doesn't exist!");
            }
        }
        post(new GarageMessage<>(GarageMessageType.REMOVED, path));
    }

    @Override
    public String describe() {
        final StringBuilder sb = new StringBuilder();
        sb.append("remove ");
        path.toPath(sb);
        sb.append(" from garage");
        return sb.toString();
    }

    @Override
    public void undo() {
        if (path.isLeaf()) {
            path.getTopDirectory().getValues().add(path.getValue().get());
        } else {
            path.getParentDirectory().getDirectories().add(path.getTopDirectory());
        }
        post(new GarageMessage<>(GarageMessageType.ADDED, path));
    }
}
