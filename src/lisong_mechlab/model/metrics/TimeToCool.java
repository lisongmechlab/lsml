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
package lisong_mechlab.model.metrics;

import lisong_mechlab.model.item.Engine;

/**
 * This class calculates how long it will take for the mech to cool down from max heat to zero. Under the assumption
 * that the mech is moving at full speed.
 * 
 * @author Li Song
 */
public class TimeToCool implements Metric {

	private final HeatCapacity capacity;
	private final HeatDissipation dissipation;

	/**
	 * @param aHeatCapacity
	 * @param aHeatDissipation
	 */
	public TimeToCool(HeatCapacity aHeatCapacity, HeatDissipation aHeatDissipation) {
		capacity = aHeatCapacity;
		dissipation = aHeatDissipation;
	}

	/**
	 * @see lisong_mechlab.model.metrics.Metric#calculate()
	 */
	@Override
	public double calculate() {
		return capacity.calculate() / (dissipation.calculate() - Engine.ENGINE_HEAT_FULL_THROTTLE);
	}

}
