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
package lisong_mechlab.model.metrics;

import lisong_mechlab.model.chassi.HeatModifier;
import lisong_mechlab.model.loadout.LoadoutBase;
import lisong_mechlab.model.loadout.LoadoutStandard;

/**
 * This {@link Metric} calculates the total heat capacity of a {@link LoadoutStandard}.
 * 
 * @author Emily Björk
 */
public class HeatCapacity implements Metric {
	private final LoadoutBase<?> loadout;
	private static final double MECH_BASE_HEAT_CAPACITY = 30;

	public HeatCapacity(final LoadoutBase<?> aLoadout) {
		loadout = aLoadout;
	}

	@Override
	public double calculate() {
		double ans = 0;
		int enginehs = 0;
		if (loadout.getEngine() != null) {
			enginehs = loadout.getEngine().getNumInternalHeatsinks();
		}

		// Engine internal HS count as true doubles
		ans += enginehs * (loadout.getUpgrades().getHeatSink().isDouble() ? 2 : 1);
		ans += (loadout.getHeatsinksCount() - enginehs)
				* loadout.getUpgrades().getHeatSink().getHeatSinkType().getCapacity();
		ans = (MECH_BASE_HEAT_CAPACITY + ans) * loadout.getEfficiencies().getHeatCapacityModifier();

		double extra = 0;
		for (HeatModifier heatModifier : loadout.getHeatModifiers()) {
			extra += heatModifier.extraHeatCapacity(ans);
		}

		return ans + extra;
	}
}
