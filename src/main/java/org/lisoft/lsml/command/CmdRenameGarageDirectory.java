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

import java.util.Optional;

import org.lisoft.lsml.messages.GarageMessage;
import org.lisoft.lsml.messages.GarageMessageType;
import org.lisoft.lsml.messages.MessageDelivery;
import org.lisoft.lsml.model.garage.GarageDirectory;
import org.lisoft.lsml.model.garage.GarageException;

/**
 * This command will change the name of a {@link GarageDirectory}.
 * 
 * @author Emily Björk
 * @param <T>
 */
public class CmdRenameGarageDirectory<T> extends MessageCommand {

    private GarageDirectory<T> dir;
    private String name;
    private String oldName;
    private Optional<GarageDirectory<T>> parentDir;

    public CmdRenameGarageDirectory(MessageDelivery aDelivery, GarageDirectory<T> aDir, String aNewName,
            Optional<GarageDirectory<T>> aParent) {
        super(aDelivery);
        dir = aDir;
        name = aNewName;
        parentDir = aParent;
    }

    @Override
    public String describe() {
        return "rename folder";
    }

    @Override
    protected void apply() throws GarageException {
        if (parentDir.isPresent()) {
            for (GarageDirectory<T> sibling : parentDir.get().getDirectories()) {
                if (sibling.getName().toLowerCase().equals(name)) {
                    throw new GarageException("A directory with that name already exists!");
                }
            }
        }

        oldName = dir.getName();
        dir.setName(name);
        post(new GarageMessage(GarageMessageType.RENAMED));
    }

    @Override
    protected void undo() {
        dir.setName(oldName);
        post(new GarageMessage(GarageMessageType.RENAMED));
    }

}
