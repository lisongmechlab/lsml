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
import org.lisoft.lsml.model.garage.GarageException;

/**
 * This class removes a directory under the given garage directory. All children are also removed.
 * 
 * @author Emily Björk
 * @param <T>
 *            The value type of the {@link GarageDirectory}.
 */
public class CmdRemoveGarageDirectory<T> extends MessageCommand {
    private final GarageDirectory<T> dir;
    private final GarageDirectory<T> parent;

    public CmdRemoveGarageDirectory(MessageDelivery aDelivery, GarageDirectory<T> aDir, GarageDirectory<T> aParent) {
        super(aDelivery);
        dir = aDir;
        parent = aParent;
    }

    @Override
    public String describe() {
        return "remove folder";
    }

    @Override
    protected void apply() throws Exception {
        if (!parent.getDirectories().contains(dir)) {
            throw new GarageException("Not a child of parent!");
        }
        parent.getDirectories().remove(dir);
        post(new GarageMessage(GarageMessageType.REMOVED));
    }

    @Override
    protected void undo() {

        parent.getDirectories().add(dir);
        post(new GarageMessage(GarageMessageType.ADDED));
    }

}
