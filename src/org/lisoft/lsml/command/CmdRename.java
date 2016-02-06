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

import org.lisoft.lsml.messages.LoadoutMessage;
import org.lisoft.lsml.messages.LoadoutMessage.Type;
import org.lisoft.lsml.messages.MessageDelivery;
import org.lisoft.lsml.model.loadout.Loadout;
import org.lisoft.lsml.model.loadout.LoadoutStandard;
import org.lisoft.lsml.util.CommandStack.Command;

/**
 * This operation renames a loadout.
 * 
 * @author Emily Björk
 */
public class CmdRename extends Command {
    private final MessageDelivery messageDelivery;
    private final Loadout         loadout;
    private final String          newName;
    private String                oldName;

    /**
     * @param aLoadout
     *            The {@link LoadoutStandard} to rename.
     * @param aMessageDelivery
     *            A {@link MessageDelivery} to announce the change on.
     * @param aName
     *            The new name of the loadout.
     */
    public CmdRename(Loadout aLoadout, MessageDelivery aMessageDelivery, String aName) {
        loadout = aLoadout;
        messageDelivery = aMessageDelivery;
        newName = aName;
    }

    @Override
    public void undo() {
        if (oldName == loadout.getName())
            return;
        loadout.rename(oldName);
        messageDelivery.post(new LoadoutMessage(loadout, Type.RENAME));
    }

    @Override
    public void apply() {
        oldName = loadout.getName();
        if (oldName == newName)
            return;
        loadout.rename(newName);
        if (messageDelivery != null)
            messageDelivery.post(new LoadoutMessage(loadout, Type.RENAME));
    }

    @Override
    public String describe() {
        return "rename loadout";
    }
}
