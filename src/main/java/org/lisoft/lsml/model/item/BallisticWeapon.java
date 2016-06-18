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

import com.thoughtworks.xstream.annotations.XStreamAsAttribute;

/**
 * An immutable class that represents a ballistic weapon.
 *
 * @author Emily Björk
 */
public class BallisticWeapon extends AmmoWeapon {
    @XStreamAsAttribute
    protected final Attribute jammingChance;
    @XStreamAsAttribute
    protected final int shotsduringcooldown;
    @XStreamAsAttribute
    protected final Attribute jammingTime;

    public BallisticWeapon(
            // Item Arguments
            String aName, String aDesc, String aMwoName, int aMwoId, int aSlots, double aTons, int aHP,
            Faction aFaction,
            // HeatSource Arguments
            Attribute aHeat,
            // Weapon Arguments
            Attribute aCooldown, Attribute aRangeZero, Attribute aRangeMin, Attribute aRangeLong, Attribute aRangeMax,
            double aFallOffExponent, int aRoundsPerShot, double aDamagePerProjectile, int aProjectilesPerRound,
            Attribute aProjectileSpeed, int aGhostHeatGroupId, double aGhostHeatMultiplier, int aGhostHeatMaxFreeAlpha,
            double aVolleyDelay, double aImpulse,
            // AmmoWeapon Arguments
            String aAmmoType, Attribute aSpread, Attribute aJammingChance, Attribute aJammingTime,
            int aShotsDuringCooldown) {
        super(// Item Arguments
                aName, aDesc, aMwoName, aMwoId, aSlots, aTons, HardPointType.BALLISTIC, aHP, aFaction,
                // HeatSource Arguments
                aHeat,
                // Weapon Arguments
                aCooldown, aRangeZero, aRangeMin, aRangeLong, aRangeMax, aFallOffExponent, aRoundsPerShot,
                aDamagePerProjectile, aProjectilesPerRound, aProjectileSpeed, aGhostHeatGroupId, aGhostHeatMultiplier,
                aGhostHeatMaxFreeAlpha, aVolleyDelay, aImpulse,
                // AmmoWeapon Arguments
                aAmmoType, aSpread);
        jammingChance = aJammingChance;
        jammingTime = aJammingTime;
        shotsduringcooldown = aShotsDuringCooldown;
    }

    public boolean canDoubleFire() {
        return jammingChance.value(null) > 0.0;
    }

    public double getJamProbability(Collection<Modifier> aModifiers) {
        return jammingChance.value(aModifiers);
    }

    public double getJamTime(Collection<Modifier> aModifiers) {
        return jammingTime.value(aModifiers);
    }

    /**
     * The unmodified rate of fire for the weapon. Mainly useful for ultra-ac type weapons where
     * {@link #getSecondsPerShot(Collection)} returns the statistical value.
     *
     * @param aModifiers
     *            The modifiers to apply from quirks etc.
     * @return The rate of fire [seconds/round]
     */
    public double getRawSecondsPerShot(Collection<Modifier> aModifiers) {
        if (getMwoId() == 1021 || getMwoId() == 1208) { // IS/Clan Gauss rifle
            // TODO: Fix this when they add the charge time to the itemstats.xml
            return super.getSecondsPerShot(aModifiers) + 0.75;
        }
        return super.getSecondsPerShot(aModifiers);
    }

    @Override
    public double getSecondsPerShot(Collection<Modifier> aModifiers) {
        if (canDoubleFire()) {
            final double cd = getRawSecondsPerShot(aModifiers);
            final double jamP = getJamProbability(aModifiers);
            final double jamT = getJamTime(aModifiers);
            return (jamT * jamP + cd) / ((1 - jamP) * (1 + shotsduringcooldown) + jamP);
        }
        return getRawSecondsPerShot(aModifiers);
    }

    @Override
    public String getShortName() {
        String name = getName();
        name = name.replace("ULTRA ", "U");
        name = name.replace("MACHINE GUN", "MG");
        return name;
    }

    public double getShotsDuringCooldown() {
        return shotsduringcooldown;
    }

}
