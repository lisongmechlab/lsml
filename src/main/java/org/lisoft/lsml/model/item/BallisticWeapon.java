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
package org.lisoft.lsml.model.item;

import java.util.Collection;

import org.lisoft.lsml.model.chassi.HardPointType;
import org.lisoft.lsml.model.modifiers.*;

import com.thoughtworks.xstream.annotations.XStreamAsAttribute;

/**
 * An immutable class that represents a ballistic weapon.
 *
 * @author Li Song
 */
public class BallisticWeapon extends AmmoWeapon {
    @XStreamAsAttribute
    protected final Attribute jammingChance;
    @XStreamAsAttribute
    protected final int shotsduringcooldown;
    @XStreamAsAttribute
    protected final Attribute jammingTime;
    @XStreamAsAttribute
    protected final double chargeTime;

    @XStreamAsAttribute
    protected final double rampUpTime;
    @XStreamAsAttribute
    protected final double rampDownTime;
    @XStreamAsAttribute
    protected final double jamRampUpTime;
    @XStreamAsAttribute
    protected final Attribute jamRampDownTime;
    @XStreamAsAttribute
    protected final double rampDownDelay;

    public BallisticWeapon(
            // Item Arguments
            String aName, String aDesc, String aMwoName, int aMwoId, int aSlots, double aTons, double aHP,
            Faction aFaction,
            // HeatSource Arguments
            Attribute aHeat,
            // Weapon Arguments
            Attribute aCooldown, WeaponRangeProfile aRangeProfile, int aRoundsPerShot, double aDamagePerProjectile,
            int aProjectilesPerRound, Attribute aProjectileSpeed, int aGhostHeatGroupId, double aGhostHeatMultiplier,
            Attribute aGhostHeatMaxFreeAlpha, double aVolleyDelay, double aImpulse,
            // AmmoWeapon Arguments
            String aAmmoType,
            // Ballistic Arguments
            Attribute aJammingChance, Attribute aJammingTime, int aShotsDuringCooldown, double aChargeTime,
            double aRampUpTime, double aRampDownTime, double aRampDownDelay, double aJamRampUpTime,
            Attribute aJamRampDownTime) {
        super(// Item Arguments
                aName, aDesc, aMwoName, aMwoId, aSlots, aTons, HardPointType.BALLISTIC, aHP, aFaction,
                // HeatSource Arguments
                aHeat,
                // Weapon Arguments
                aCooldown, aRangeProfile, aRoundsPerShot, aDamagePerProjectile, aProjectilesPerRound, aProjectileSpeed,
                aGhostHeatGroupId, aGhostHeatMultiplier, aGhostHeatMaxFreeAlpha, aVolleyDelay, aImpulse,
                // AmmoWeapon Arguments
                aAmmoType);
        jammingChance = aJammingChance;
        jammingTime = aJammingTime;
        shotsduringcooldown = aShotsDuringCooldown;
        chargeTime = aChargeTime;

        rampUpTime = aRampUpTime;
        rampDownTime = aRampDownTime;
        rampDownDelay = aRampDownDelay;
        jamRampUpTime = aJamRampUpTime;
        jamRampDownTime = aJamRampDownTime;
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

    public double getChargeTime() {
        return chargeTime;
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
        return super.getSecondsPerShot(aModifiers) + chargeTime;
    }

    @Override
    public double getSecondsPerShot(Collection<Modifier> aModifiers) {
        if (canDoubleFire()) {
            final double cd = getRawSecondsPerShot(aModifiers);
            final double jamP = getJamProbability(aModifiers);
            final double jamT = getJamTime(aModifiers);

            if (rampUpTime != 0.0) {
                // RAC
                double expectedShotsBeforeJam = 0.0;
                double p_k = jamP;
                final int infinity = 1000;
                for (int k = 0; k < infinity; ++k) {
                    expectedShotsBeforeJam += k * p_k;
                    p_k *= (1 - jamP);
                }
                final double expectedTimeBeforeJam = expectedShotsBeforeJam * cd;

                final double period = jamRampUpTime + expectedTimeBeforeJam
                        + Math.max(rampDownDelay + getJamRampDownTime(aModifiers), jamT);
                final double shots = (jamRampUpTime - rampUpTime + expectedTimeBeforeJam) / cd;
                return period / shots;
            }
            // UAC
            return (jamT * jamP + cd) / ((1 - jamP) * (1 + shotsduringcooldown) + jamP);
        }
        return getRawSecondsPerShot(aModifiers);
    }

    public double getShotsDuringCooldown() {
        return shotsduringcooldown;
    }

    public double getJamRampDownTime(Collection<Modifier> aModifiers) {
        return jamRampDownTime.value(aModifiers);
    }

}
