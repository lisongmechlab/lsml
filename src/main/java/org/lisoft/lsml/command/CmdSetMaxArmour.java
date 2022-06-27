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

/**
 * This operation sets the maximum amount of armour possible on a mech with a given ratio between front and back.
 *
 * @author Li Song
 */
public class CmdSetMaxArmour extends CmdLoadoutBase {
    private final boolean manualSet;
    private final double ratio;

    public CmdSetMaxArmour(Loadout aLoadout, MessageDelivery aMessageDelivery, double aRatio, boolean aManualSet) {
        super(aLoadout, aMessageDelivery, "set max armour");
        manualSet = aManualSet;
        ratio = aRatio;
    }

    @Override
    public void buildCommand() {
        for (ConfiguredComponent component : loadout.getComponents()) {
            final int max = component.getInternalComponent().getArmourMax();
            if (component.getInternalComponent().getLocation().isTwoSided()) {
                // 1) front + back = max
                // 2) front / back = ratio
                // front = back * ratio
                // front = max - back
                // = > back * ratio = max - back
                int back = (int) (max / (ratio + 1));
                int front = max - back;

                addOp(new CmdSetArmour(messageBuffer, loadout, component, ArmourSide.BACK, 0, manualSet));
                addOp(new CmdSetArmour(messageBuffer, loadout, component, ArmourSide.FRONT, front, manualSet));
                addOp(new CmdSetArmour(messageBuffer, loadout, component, ArmourSide.BACK, back, manualSet));
            } else {
                addOp(new CmdSetArmour(messageBuffer, loadout, component, ArmourSide.ONLY, max, manualSet));
            }
        }
    }
}
