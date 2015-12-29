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
import org.lisoft.lsml.model.chassi.ArmorSide;
import org.lisoft.lsml.model.loadout.LoadoutBase;
import org.lisoft.lsml.model.loadout.LoadoutStandard;
import org.lisoft.lsml.model.loadout.component.ConfiguredComponentBase;
import org.lisoft.lsml.util.CommandStack.CompositeCommand;

/**
 * This operation removes all armor from a {@link LoadoutStandard}.
 * 
 * @author Emily Björk
 */
public class CmdStripArmor extends CompositeCommand {
    protected final LoadoutBase<?> loadout;

    public CmdStripArmor(LoadoutBase<?> aLoadout, MessageDelivery aMessageDelivery) {
        super("strip armor", aMessageDelivery);
        loadout = aLoadout;
    }

    @Override
    public void buildCommand() {
        for (ConfiguredComponentBase component : loadout.getComponents()) {
            for (ArmorSide side : ArmorSide.allSides(component.getInternalComponent())) {
                addOp(new CmdSetArmor(messageBuffer, loadout, component, side, 0, true));
            }
        }
    }
}
