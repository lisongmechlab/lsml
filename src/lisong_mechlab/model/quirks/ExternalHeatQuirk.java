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
package lisong_mechlab.model.quirks;

import lisong_mechlab.model.chassi.HeatModifier;
import lisong_mechlab.model.quirks.Quirks.QuirkBenefit;

/**
 * This quirk affects the transfer of heat to and from the mech and the environment.
 * 
 * @author Li Song
 */
public class ExternalHeatQuirk extends Quirk implements HeatModifier {

    public ExternalHeatQuirk(String aName, double aValue) {
        super(aName, aValue);
    }

    @Override
    public double extraEnvironmentHeat(double aEnvironmentHeat) {
        return aEnvironmentHeat * value;
    }

    @Override
    public double extraHeatDissipation(double aHeat) {
        return 0;
    }

    @Override
    public double extraHeatGeneration(double aHeat) {
        return 0;
    }

    @Override
    public double extraHeatCapacity(double aHeat) {
        return 0;
    }

    @Override
    public QuirkBenefit isPositiveGood() {
        return QuirkBenefit.INDETERMINATE;
    }

    @Override
    protected void writeValue(StringBuilder aSB) {
        if (value > 0) {
            aSB.append("+");
        }
        aSB.append(FORMAT.format(value * 100)).append("%");
    }
}
