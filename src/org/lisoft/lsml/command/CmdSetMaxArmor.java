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
import org.lisoft.lsml.model.loadout.Loadout;
import org.lisoft.lsml.model.loadout.component.ConfiguredComponent;

/**
 * This operation sets the maximum amount of armor possible on a mech with a given ratio between front and back.
 * 
 * @author Emily Björk
 */
public class CmdSetMaxArmor extends CmdLoadoutBase {
    private final boolean manualSet;
    private final double  ratio;

    public CmdSetMaxArmor(Loadout aLoadout, MessageDelivery aMessageDelivery, double aRatio, boolean aManualSet) {
        super(aLoadout, aMessageDelivery, "set max armor");
        manualSet = aManualSet;
        ratio = aRatio;
    }

    @Override
    public void buildCommand() {
        for (ConfiguredComponent component : loadout.getComponents()) {
            final int max = component.getInternalComponent().getArmorMax();
            if (component.getInternalComponent().getLocation().isTwoSided()) {
                // 1) front + back = max
                // 2) front / back = ratio
                // front = back * ratio
                // front = max - back
                // = > back * ratio = max - back
                int back = (int) (max / (ratio + 1));
                int front = max - back;

                addOp(new CmdSetArmor(messageBuffer, loadout, component, ArmorSide.BACK, 0, manualSet));
                addOp(new CmdSetArmor(messageBuffer, loadout, component, ArmorSide.FRONT, front, manualSet));
                addOp(new CmdSetArmor(messageBuffer, loadout, component, ArmorSide.BACK, back, manualSet));
            }
            else {
                addOp(new CmdSetArmor(messageBuffer, loadout, component, ArmorSide.ONLY, max, manualSet));
            }
        }
    }
}
