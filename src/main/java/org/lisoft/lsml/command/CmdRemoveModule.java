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
import org.lisoft.lsml.messages.MessageDelivery;
import org.lisoft.lsml.model.item.PilotModule;
import org.lisoft.lsml.model.loadout.Loadout;
import org.lisoft.lsml.util.CommandStack.Command;

/**
 * This {@link Command} removes a module from a loadout.
 * 
 * @author Emily Björk
 */
public class CmdRemoveModule extends Command {
    private final PilotModule module;
    private final Loadout loadout;
    private final transient MessageDelivery messageDelivery;

    /**
     * Creates a new {@link CmdRemoveModule}.
     * 
     * @param aMessageDelivery
     *            The {@link MessageDelivery} to signal changes to the loadout on.
     * @param aLoadout
     *            The {@link Loadout} to remove the module from.
     * @param aLookup
     *            The {@link PilotModule} to remove.
     */
    public CmdRemoveModule(MessageDelivery aMessageDelivery, Loadout aLoadout, PilotModule aLookup) {
        module = aLookup;
        loadout = aLoadout;
        messageDelivery = aMessageDelivery;
    }

    @Override
    public String describe() {
        return "remove " + module + " from " + loadout;
    }

    @Override
    protected void apply() {
        loadout.removeModule(module);
        post();
    }

    private void post() {
        if (messageDelivery != null) {
            messageDelivery.post(new LoadoutMessage(loadout, LoadoutMessage.Type.MODULES_CHANGED));
        }
    }

    @Override
    protected void undo() {
        loadout.addModule(module);
        post();
    }
}
