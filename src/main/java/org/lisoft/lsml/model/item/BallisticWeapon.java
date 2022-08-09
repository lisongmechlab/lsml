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

import com.thoughtworks.xstream.annotations.XStreamAsAttribute;
import org.lisoft.lsml.model.chassi.HardPointType;
import org.lisoft.lsml.model.metrics.helpers.IntegratedImpulseTrain;
import org.lisoft.lsml.model.metrics.helpers.IntegratedSignal;
import org.lisoft.lsml.model.modifiers.Attribute;
import org.lisoft.lsml.model.modifiers.Modifier;

import java.util.Collection;
import java.util.PriorityQueue;

/**
 * An immutable class that represents a ballistic weapon.
 *
 * @author Li Song
 */
public class BallisticWeapon extends AmmoWeapon {
    @XStreamAsAttribute
    protected final double chargeTime;
    @XStreamAsAttribute
    protected final Attribute jamRampDownTime;
    @XStreamAsAttribute
    protected final double jamRampUpTime;
    @XStreamAsAttribute
    protected final Attribute jammingChance;
    @XStreamAsAttribute
    protected final Attribute jammingTime;
    @XStreamAsAttribute
    protected final double rampDownDelay;
    @XStreamAsAttribute
    protected final double rampDownTime;
    @XStreamAsAttribute
    protected final double rampUpTime;
    @XStreamAsAttribute
    protected final int shotsDuringCooldown;

    public BallisticWeapon(
        // Item Arguments
        String aName, String aDesc, String aMwoName, int aMwoId, int aSlots, double aTons, double aHP, Faction aFaction,
        // HeatSource Arguments
        Attribute aHeat,
        // Weapon Arguments
        Attribute aCooldown, WeaponRangeProfile aRangeProfile, int aRoundsPerShot, double aDamagePerProjectile,
        int aProjectilesPerRound, Attribute aProjectileSpeed, int aGhostHeatGroupId, double aGhostHeatMultiplier,
        Attribute aGhostHeatMaxFreeAlpha, double aVolleyDelay, double aImpulse,
        // AmmoWeapon Arguments
        String aAmmoType, boolean aOneShot, int aAmmoPerShot,
        // Ballistic Arguments
        Attribute aJammingChance, Attribute aJammingTime, int aShotsDuringCooldown, double aChargeTime,
        double aRampUpTime, double aRampDownTime, double aRampDownDelay, double aJamRampUpTime,
        Attribute aJamRampDownTime) {
        super(// Item Arguments
              aName, aDesc, aMwoName, aMwoId, aSlots, aTons, HardPointType.BALLISTIC, aHP, aFaction,
              // HeatSource Arguments
              aHeat,
              // Weapon Arguments
              aCooldown, aRangeProfile, aRoundsPerShot, 1, aDamagePerProjectile, aProjectilesPerRound, aProjectileSpeed,
              aGhostHeatGroupId, aGhostHeatMultiplier, aGhostHeatMaxFreeAlpha, aVolleyDelay, aImpulse,
              // AmmoWeapon Arguments
              aAmmoType, aOneShot, aAmmoPerShot);
        jammingChance = aJammingChance;
        jammingTime = aJammingTime;
        shotsDuringCooldown = aShotsDuringCooldown;
        chargeTime = aChargeTime;

        rampUpTime = aRampUpTime;
        rampDownTime = aRampDownTime;
        rampDownDelay = aRampDownDelay;
        jamRampUpTime = aJamRampUpTime;
        jamRampDownTime = aJamRampDownTime;
    }

    public boolean canJam() {
        return jammingChance.value(null) > 0.0;
    }

    public double getChargeTime() {
        return chargeTime;
    }

    @Override
    public double getExpectedFiringPeriod(Collection<Modifier> aModifiers) {
        if (canJam()) {
            final double cd = getRawFiringPeriod(aModifiers);
            final double jamP = getJamProbability(aModifiers);
            final double jamT = getJamTime(aModifiers);

            if (rampUpTime != 0.0) {
                // RAC

                // p_k must have form: "jamP*(1-jamP)^k for k=0...+inf" to terminate each success string with a failure.
                // Failure to do so will skew the probability mass.
                double p_k = jamP;

                PriorityQueue<Double> values = new PriorityQueue<>();
                final int infinity = 1000;
                for (int k = 0; k < infinity; ++k) {
                    values.add(k * p_k);
                    p_k *= (1.0 - jamP);
                }
                while (values.size() > 1) {
                    values.add(values.remove() + values.remove());
                }
                final double expectedShotsBeforeJam = values.remove();
                final double expectedTimeBeforeJam = expectedShotsBeforeJam * cd;

                final double period = jamRampUpTime + expectedTimeBeforeJam +
                                      Math.max(rampDownDelay + getJamRampDownTime(aModifiers), jamT);
                final double shots = (jamRampUpTime - rampUpTime + expectedTimeBeforeJam) / cd;
                return period / shots;
            }
            // UAC
            return (jamT * jamP + cd) / ((1 - jamP) * (1 + shotsDuringCooldown) + jamP);
        }
        return getRawFiringPeriod(aModifiers);
    }

    @Override
    public IntegratedSignal getExpectedHeatSignal(Collection<Modifier> aModifiers) {
        final double expectedFiringPeriod = getExpectedFiringPeriod(aModifiers);
        final double heatGenerated = getHeat(aModifiers);
        return new IntegratedImpulseTrain(expectedFiringPeriod, heatGenerated);
    }

    public double getJamProbability(Collection<Modifier> aModifiers) {
        return jammingChance.value(aModifiers);
    }

    public double getJamRampDownTime(Collection<Modifier> aModifiers) {
        return jamRampDownTime.value(aModifiers);
    }

    /**
     * The amount of time after a starting firing (or clearing a jam) that the gun will not jam.
     *
     * @param aModifiers that can affect the value.
     * @return a duration in seconds.
     */
    public double getJamRampUpTime(Collection<Modifier> aModifiers) {
        return jamRampUpTime;
    }

    public double getJamTime(Collection<Modifier> aModifiers) {
        return jammingTime.value(aModifiers);
    }

    /**
     * The amount of time after pulling the trigger that the gun starts shooting.
     *
     * @param aModifiers that can affect the value.
     * @return a duration in seconds.
     */
    public double getRampUpTime(Collection<Modifier> aModifiers) {
        return rampUpTime;
    }

    /**
     * The unmodified rate of fire for the weapon. Mainly useful for ultra-ac type weapons where
     * {@link #getExpectedFiringPeriod(Collection)} returns the statistical value.
     *
     * @param aModifiers The modifiers to apply from quirks etc.
     * @return The rate of fire [seconds/round]
     */
    @Override
    public double getRawFiringPeriod(Collection<Modifier> aModifiers) {
        return super.getRawFiringPeriod(aModifiers) + chargeTime;
    }

    public int getShotsDuringCooldown() {
        return shotsDuringCooldown;
    }

}
