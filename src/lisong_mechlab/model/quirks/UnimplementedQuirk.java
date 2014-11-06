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

import lisong_mechlab.model.quirks.Quirks.QuirkBenefit;

/**
 * This quirk is used for quirk types that are not yet implemented or that have no effect on the results of LSML.
 * 
 * @author Li Song
 */
public class UnimplementedQuirk extends Quirk {
    private final QuirkBenefit positiveGood;
    
    public UnimplementedQuirk(String aName, double aValue, QuirkBenefit aPositiveGood) {
        super(aName, aValue);
        positiveGood = aPositiveGood;
    }

    @Override
    public QuirkBenefit isPositiveGood() {
        return positiveGood;
    }
}
