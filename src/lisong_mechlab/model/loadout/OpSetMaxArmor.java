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
package lisong_mechlab.model.loadout;

import lisong_mechlab.model.chassi.ArmorSide;
import lisong_mechlab.model.loadout.component.ConfiguredComponentBase;
import lisong_mechlab.model.loadout.component.OpSetArmor;
import lisong_mechlab.util.message.MessageDelivery;

/**
 * This operation sets the maximum amount of armor possible on a mech with a given ratio between front and back.
 * 
 * @author Li Song
 */
public class OpSetMaxArmor extends OpLoadoutBase {
	private final boolean	manualSet;
	private double			ratio;

	public OpSetMaxArmor(LoadoutBase<?> aLoadout, MessageDelivery aMessageDelivery, double aRatio, boolean aManualSet) {
		super(aLoadout, aMessageDelivery, "set max armor");
		manualSet = aManualSet;
		ratio = aRatio;
	}

	@Override
	public void buildOperation() {
		for (ConfiguredComponentBase component : loadout.getComponents()) {
			final int max = component.getInternalComponent().getArmorMax();
			if (component.getInternalComponent().getLocation().isTwoSided()) {
				// 1) front + back = max
				// 2) front / back = ratio
				// front = back * ratio
				// front = max - back
				// = > back * ratio = max - back
				int back = (int) (max / (ratio + 1));
				int front = max - back;

				addOp(new OpSetArmor(messageBuffer, loadout, component, ArmorSide.BACK, 0, manualSet));
				addOp(new OpSetArmor(messageBuffer, loadout, component, ArmorSide.FRONT, front, manualSet));
				addOp(new OpSetArmor(messageBuffer, loadout, component, ArmorSide.BACK, back, manualSet));
			} else {
				addOp(new OpSetArmor(messageBuffer, loadout, component, ArmorSide.ONLY, max, manualSet));
			}
		}
	}
}
