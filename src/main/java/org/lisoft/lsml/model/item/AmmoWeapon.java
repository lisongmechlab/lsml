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

import org.lisoft.lsml.math.probability.GaussianDistribution;
import org.lisoft.lsml.model.chassi.HardPointType;
import org.lisoft.lsml.model.modifiers.Attribute;
import org.lisoft.lsml.model.modifiers.Modifier;

import com.thoughtworks.xstream.annotations.XStreamAsAttribute;

/**
 * Base class for weapons that consume ammunition.
 *
 * @author Li Song
 *
 */
public class AmmoWeapon extends Weapon {
    @XStreamAsAttribute
    private final String ammoTypeId;
    @XStreamAsAttribute
    private final Attribute spread;

    /**
     * This field will be set through reflection in a post-processing pass.
     */
    @XStreamAsAttribute
    private final Ammunition ammoType = null;

    /**
     * This field will be set through reflection in a post-processing pass.
     */
    @XStreamAsAttribute
    private final Ammunition ammoHalfType = null;

    public AmmoWeapon(
            // Item Arguments
            String aName, String aDesc, String aMwoName, int aMwoId, int aSlots, double aTons,
            HardPointType aHardPointType, double aHP, Faction aFaction,
            // HeatSource Arguments
            Attribute aHeat,
            // Weapon Arguments
            Attribute aCoolDown, Attribute aRangeZero, Attribute aRangeMin, Attribute aRangeLong, Attribute aRangeMax,
            double aFallOffExponent, int aRoundsPerShot, double aDamagePerProjectile, int aProjectilesPerRound,
            Attribute aProjectileSpeed, int aGhostHeatGroupId, double aGhostHeatMultiplier, int aGhostHeatMaxFreeAlpha,
            double aVolleyDelay, double aImpulse,
            // AmmoWeapon Arguments
            String aAmmoType, Attribute aSpread) {
        super(aName, aDesc, aMwoName, aMwoId, aSlots, aTons, aHardPointType, aHP, aFaction, aHeat, aCoolDown,
                aRangeZero, aRangeMin, aRangeLong, aRangeMax, aFallOffExponent, aRoundsPerShot, aDamagePerProjectile,
                aProjectilesPerRound, aProjectileSpeed, aGhostHeatGroupId, aGhostHeatMultiplier, aGhostHeatMaxFreeAlpha,
                aVolleyDelay, aImpulse);
        ammoTypeId = aAmmoType;
        spread = aSpread;
    }

    public Ammunition getAmmoHalfType() {
        return ammoHalfType;
    }

    /**
     * @return The {@link String} name of the ammo type required for this weapon.
     */
    public String getAmmoKey() {
        return ammoTypeId;
    }

    public Ammunition getAmmoType() {
        return ammoType;
    }

    @Override
    public double getRangeEffectiveness(double aRange, Collection<Modifier> aModifiers) {
        double spreadFactor = 1.0;
        if (hasSpread()) {
            // Assumption:
            // The 'spread' value is the standard deviation of a zero-mean gaussian distribution of angles.
            final GaussianDistribution gaussianDistribution = new GaussianDistribution();

            final double targetRadius = 6; // [m]
            final double maxAngle = Math.atan2(targetRadius, aRange) * 180 / Math.PI; // [deg]

            // X ~= N(0, spread)
            // P_hit = P(-maxAngle <= X; X <= +maxAngle)
            // Xn = (X - 0) / spread ~ N(0,1)
            // P_hit = cdf(maxAngle / spread) - cdf(-maxAngle / spread) = 2*cdf(maxAngle / spread) - 1.0;
            spreadFactor = 2 * gaussianDistribution.cdf(maxAngle / getSpread(aModifiers)) - 1;
        }
        return spreadFactor * super.getRangeEffectiveness(aRange, aModifiers);
    }

    /**
     * @param aModifiers
     *            {@link Modifier}s that can affect the spread value.
     * @return The spread value for the weapon.
     */
    public double getSpread(Collection<Modifier> aModifiers) {
        return spread.value(aModifiers);
    }

    /**
     * @return <code>true</code> if the weapon has builtin ammo.
     */
    public boolean hasBuiltInAmmo() {
        return null == ammoTypeId;
    }

    @Override
    public boolean hasSpread() {
        return spread.value(null) > 0;
    }

    public boolean isCompatibleAmmo(Ammunition aAmmunition) {
        return ammoTypeId.equals(aAmmunition.getAmmoType());
    }
}
