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
package org.lisoft.lsml.model.item;

import java.util.Collection;

import org.lisoft.lsml.model.chassi.HardPointType;
import org.lisoft.lsml.model.modifiers.Attribute;
import org.lisoft.lsml.model.modifiers.Modifier;

/**
 * An immutable class that represents an energy weapon.
 * 
 * @author Emily Björk
 */
public class EnergyWeapon extends Weapon {
    protected final Attribute burnTime;

    public EnergyWeapon(// Item Arguments
            String aName, String aDesc, String aMwoName, int aMwoId, int aSlots, double aTons, int aHP,
            Faction aFaction,
            // HeatSource Arguments
            Attribute aHeat,
            // Weapon Arguments
            Attribute aCooldown, Attribute aRangeZero, Attribute aRangeMin, Attribute aRangeLong, Attribute aRangeMax,
            double aFallOffExponent, int aRoundsPerShot, double aDamagePerProjectile, int aProjectilesPerRound,
            double aProjectileSpeed, int aGhostHeatGroupId, double aGhostHeatMultiplier, int aGhostHeatMaxFreeAlpha,
            double aVolleyDelay, double aImpulse,
            // EnergyWeaponm Arguments
            Attribute aBurnTime) {
        super(// Item Arguments
                aName, aDesc, aMwoName, aMwoId, aSlots, aTons, HardPointType.ENERGY, aHP, aFaction,
                // HeatSource Arguments
                aHeat,
                // Weapon Arguments
                aCooldown, aRangeZero, aRangeMin, aRangeLong, aRangeMax, aFallOffExponent, aRoundsPerShot,
                aDamagePerProjectile, aProjectilesPerRound, aProjectileSpeed, aGhostHeatGroupId, aGhostHeatMultiplier,
                aGhostHeatMaxFreeAlpha, aVolleyDelay, aImpulse);
        burnTime = aBurnTime;
    }

    @Override
    public double getSecondsPerShot(Collection<Modifier> aModifiers) {
        if (burnTime.value(null) == Double.POSITIVE_INFINITY) {
            return getCoolDown(aModifiers);
        }
        return getCoolDown(aModifiers) + getDuration(aModifiers);
    }

    @Override
    public String getShortName() {
        String name = getName();
        name = name.replace("LASER", "LAS");
        name = name.replace("LARGE ", "L");
        name = name.replace("LRG ", "L");
        name = name.replace("SML ", "S");
        name = name.replace("SMALL ", "S");
        name = name.replace("MED ", "M");
        name = name.replace("MEDIUM ", "M");
        name = name.replace("PULSE ", "P");
        return name;
    }

    public double getDuration(Collection<Modifier> aModifiers) {
        return burnTime.value(aModifiers);
    }
}
