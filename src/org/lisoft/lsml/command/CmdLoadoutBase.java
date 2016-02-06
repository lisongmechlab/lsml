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

import org.lisoft.lsml.messages.MessageDelivery;
import org.lisoft.lsml.model.loadout.Loadout;
import org.lisoft.lsml.model.loadout.LoadoutStandard;
import org.lisoft.lsml.util.CommandStack.CompositeCommand;

/**
 * Base class for operations operating on a {@link LoadoutStandard}.
 * 
 * @author Emily Björk
 */
public abstract class CmdLoadoutBase extends CompositeCommand {
    protected final Loadout loadout;

    /**
     * @param aLoadout
     *            The {@link LoadoutStandard} to operate on.
     * @param aMessageDelivery
     *            The {@link MessageDelivery} to announce changes on the loadout to.
     * @param aDescription
     *            A human readable description of the operation.
     */
    public CmdLoadoutBase(Loadout aLoadout, MessageDelivery aMessageDelivery, String aDescription) {
        super(aDescription, aMessageDelivery);
        loadout = aLoadout;
    }
}
