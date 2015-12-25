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

import org.lisoft.lsml.messages.LoadoutMessage;
import org.lisoft.lsml.messages.MessageDelivery;
import org.lisoft.lsml.messages.MessageXBar;
import org.lisoft.lsml.model.item.PilotModule;
import org.lisoft.lsml.model.loadout.EquipResult;
import org.lisoft.lsml.model.loadout.LoadoutBase;
import org.lisoft.lsml.util.CommandStack.Command;

/**
 * This {@link Command} adds a module to a loadout.
 * 
 * @author Li Song
 */
public class CmdAddModule extends Command {
    private final PilotModule               module;
    private final LoadoutBase<?>            loadout;
    private final transient MessageDelivery messageDelivery;

    /**
     * Creates a new {@link CmdAddModule}.
     * 
     * @param aMessageDelivery
     *            The {@link MessageXBar} to signal changes to the loadout on.
     * @param aLoadout
     *            The {@link LoadoutBase} to add the module to.
     * @param aLookup
     *            The {@link PilotModule} to add.
     */
    public CmdAddModule(MessageDelivery aMessageDelivery, LoadoutBase<?> aLoadout, PilotModule aLookup) {
        module = aLookup;
        loadout = aLoadout;
        messageDelivery = aMessageDelivery;
    }

    @Override
    public String describe() {
        return "add " + module + " to " + loadout;
    }

    void post() {
        if (messageDelivery != null) {
            messageDelivery.post(new LoadoutMessage(loadout, LoadoutMessage.Type.MODULES_CHANGED));
        }
    }

    @Override
    protected void apply() throws EquipResult {
        EquipResult result = loadout.canAddModule(module);
        result.checkFailureAndThrow();
        loadout.addModule(module);

        post();
    }

    @Override
    protected void undo() {
        loadout.removeModule(module);
        post();
    }
}
