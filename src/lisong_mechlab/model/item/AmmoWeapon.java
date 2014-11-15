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
package lisong_mechlab.model.item;

import lisong_mechlab.model.chassi.HardPointType;
import lisong_mechlab.model.modifiers.Attribute;

import com.thoughtworks.xstream.annotations.XStreamAsAttribute;

/**
 * Base class for weapons that consume ammunition.
 * 
 * @author Emily Björk
 *
 */
public class AmmoWeapon extends Weapon {
    @XStreamAsAttribute
    private final String ammoTypeId;

    public AmmoWeapon(String aName, String aDesc, String aMwoName, int aMwoId, int aSlots, double aTons,
            HardPointType aHardPointType, int aHP, Faction aFaction, Attribute aHeat, Attribute aCooldown,
            Attribute aRangeZero, Attribute aRangeMin, Attribute aRangeLong, Attribute aRangeMax,
            double aFallOffExponent, int aRoundsPerShot, double aDamagePerProjectile, int aProjectilesPerRound,
            double aProjectileSpeed, int aGhostHeatGroupId, double aGhostHeatMultiplier, int aGhostHeatMaxFreeAlpha,
            String aAmmoType) {
        super(aName, aDesc, aMwoName, aMwoId, aSlots, aTons, aHardPointType, aHP, aFaction, aHeat, aCooldown,
                aRangeZero, aRangeMin, aRangeLong, aRangeMax, aFallOffExponent, aRoundsPerShot, aDamagePerProjectile,
                aProjectilesPerRound, aProjectileSpeed, aGhostHeatGroupId, aGhostHeatMultiplier, aGhostHeatMaxFreeAlpha);
        ammoTypeId = aAmmoType;
    }

    public boolean isCompatibleAmmo(Ammunition aAmmunition) {
        return ammoTypeId.equals(aAmmunition.getAmmoType());
    }

    @Override
    public String getShortName() {
        String name = getName();
        name = name.replace("ANTI-MISSILE SYSTEM", "AMS");
        return name;
    }

    /**
     * @return The {@link String} name of the ammo type required for this weapon.
     */
    public String getAmmoType() {
        return ammoTypeId;
    }
}
