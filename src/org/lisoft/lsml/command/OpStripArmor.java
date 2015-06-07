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

import org.lisoft.lsml.model.chassi.ArmorSide;
import org.lisoft.lsml.model.loadout.LoadoutBase;
import org.lisoft.lsml.model.loadout.LoadoutStandard;
import org.lisoft.lsml.model.loadout.component.ConfiguredComponentBase;
import org.lisoft.lsml.util.OperationStack.CompositeOperation;
import org.lisoft.lsml.util.message.MessageDelivery;

/**
 * This operation removes all armor from a {@link LoadoutStandard}.
 * 
 * @author Li Song
 */
public class OpStripArmor extends CompositeOperation {
    protected final LoadoutBase<?> loadout;

    public OpStripArmor(LoadoutBase<?> aLoadout, MessageDelivery aMessageDelivery) {
        super("strip armor", aMessageDelivery);
        loadout = aLoadout;
    }

    @Override
    public void buildOperation() {
        for (ConfiguredComponentBase component : loadout.getComponents()) {
            if (component.getInternalComponent().getLocation().isTwoSided()) {
                addOp(new OpSetArmor(messageBuffer, loadout, component, ArmorSide.FRONT, 0, true));
                addOp(new OpSetArmor(messageBuffer, loadout, component, ArmorSide.BACK, 0, true));
            }
            else {
                addOp(new OpSetArmor(messageBuffer, loadout, component, ArmorSide.ONLY, 0, true));
            }
        }
    }
}
