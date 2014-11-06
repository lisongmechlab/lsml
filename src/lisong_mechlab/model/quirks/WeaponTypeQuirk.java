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

import lisong_mechlab.model.chassi.HardPointType;
import lisong_mechlab.model.item.Weapon;
import lisong_mechlab.model.item.WeaponModifier;
import lisong_mechlab.model.pilot.PilotSkillTree;

/**
 * A base class for quirks that affects all weapons of a specific type.
 * 
 * @author Li Song
 *
 */
public abstract class WeaponTypeQuirk extends Quirk implements WeaponModifier {
    private final HardPointType type;

    /**
     * Creates a new quirk
     * 
     * @param aName The human readable name of the quirk.
     * @param aValue The value of the quirk.
     * 
     * @param aType
     *            The {@link HardPointType} that this quirk affects.
     */
    public WeaponTypeQuirk(String aName, double aValue, HardPointType aType) {
        super(aName, aValue);
        type = aType;
    }

    @Override
    public boolean affectsWeapon(Weapon aWeapon) {
        return aWeapon.getHardpointType() == type;
    }

    @Override
    public double extraMaxRange(Weapon aWeapon, double aRange, PilotSkillTree aPilotSkillTree) {
        return 0;
    }

    @Override
    public double extraLongRange(Weapon aWeapon, double aRange, PilotSkillTree aPilotSkillTree) {
        return 0;
    }

    @Override
    public double extraWeaponHeat(Weapon aWeapon, double aHeat, PilotSkillTree aPilotSkillTree) {
        return 0;
    }

    @Override
    public double extraCooldown(Weapon aWeapon, double aCooldown, PilotSkillTree aPilotSkillTree) {
        return 0;
    }

    @Override
    public double extraDuration(Weapon aWeapon, double aDuration, PilotSkillTree aPilotSkillTree) {
        return 0;
    }
    
    @Override
    protected void writeValue(StringBuilder aSB) {
        if (value > 0) {
            aSB.append("+");
        }
        aSB.append(FORMAT.format(value * 100)).append("%");
    }
}
