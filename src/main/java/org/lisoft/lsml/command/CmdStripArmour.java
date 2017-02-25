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
import org.lisoft.lsml.model.chassi.ArmourSide;
import org.lisoft.lsml.model.loadout.ConfiguredComponent;
import org.lisoft.lsml.model.loadout.Loadout;
import org.lisoft.lsml.model.loadout.LoadoutStandard;
import org.lisoft.lsml.util.CommandStack.CompositeCommand;

/**
 * This operation removes all armour from a {@link LoadoutStandard}.
 *
 * @author Li Song
 */
public class CmdStripArmour extends CompositeCommand {
    protected final Loadout loadout;

    public CmdStripArmour(Loadout aLoadout, MessageDelivery aMessageDelivery) {
        super("strip armour", aMessageDelivery);
        loadout = aLoadout;
    }

    @Override
    public void buildCommand() {
        for (final ConfiguredComponent component : loadout.getComponents()) {
            for (final ArmourSide side : ArmourSide.allSides(component.getInternalComponent())) {
                addOp(new CmdSetArmour(messageBuffer, loadout, component, side, 0, true));
            }
        }
    }
}
