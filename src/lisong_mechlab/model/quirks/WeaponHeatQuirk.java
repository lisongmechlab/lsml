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

import lisong_mechlab.model.item.Weapon;
import lisong_mechlab.model.pilot.PilotSkillTree;
import lisong_mechlab.model.quirks.Quirks.QuirkBenefit;

/**
 * A quirk that affects the heat generation of a specific weapon.
 * 
 * @author Li Song
 *
 */
public class WeaponHeatQuirk extends WeaponQuirk {
    public WeaponHeatQuirk(String aName, double aValue, Weapon aAffectedWeapon) {
        super(aName, aValue, aAffectedWeapon);
    }

    @Override
    public QuirkBenefit isPositiveGood() {
        return QuirkBenefit.NEGATIVE_GOOD;
    }

    @Override
    public double extraWeaponHeat(Weapon aWeapon, double aHeat, PilotSkillTree aPilotSkillTree) {
        return aHeat*value;
    }
}
