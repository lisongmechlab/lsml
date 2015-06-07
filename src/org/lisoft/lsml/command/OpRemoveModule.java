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

import org.lisoft.lsml.model.item.PilotModule;
import org.lisoft.lsml.model.loadout.LoadoutBase;
import org.lisoft.lsml.model.loadout.LoadoutMessage;
import org.lisoft.lsml.util.OperationStack.Operation;
import org.lisoft.lsml.util.message.MessageXBar;

/**
 * This {@link Operation} removes a module from a loadout.
 * 
 * @author Emily Björk
 */
public class OpRemoveModule extends Operation {
    private final PilotModule           module;
    private final LoadoutBase<?>        loadout;
    private final transient MessageXBar xBar;

    /**
     * Creates a new {@link OpRemoveModule}.
     * 
     * @param aXBar
     *            The {@link MessageXBar} to signal changes to the loadout on.
     * @param aLoadout
     *            The {@link LoadoutBase} to remove the module from.
     * @param aLookup
     *            The {@link PilotModule} to remove.
     */
    public OpRemoveModule(MessageXBar aXBar, LoadoutBase<?> aLoadout, PilotModule aLookup) {
        module = aLookup;
        loadout = aLoadout;
        xBar = aXBar;
    }

    @Override
    public String describe() {
        return "remove " + module + " from " + loadout;
    }

    @Override
    protected void apply() {
        loadout.removeModule(module);
        if (xBar != null) {
            xBar.post(new LoadoutMessage(loadout, LoadoutMessage.Type.MODULES_CHANGED));
        }
    }

    @Override
    protected void undo() {
        loadout.addModule(module);
        if (xBar != null) {
            xBar.post(new LoadoutMessage(loadout, LoadoutMessage.Type.MODULES_CHANGED));
        }
    }
}
