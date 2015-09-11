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
package lisong_mechlab.model.item;

import java.util.Collection;
import java.util.Comparator;

import lisong_mechlab.model.chassi.HardPointType;
import lisong_mechlab.model.modifiers.Attribute;
import lisong_mechlab.model.modifiers.Modifier;
import lisong_mechlab.util.GaussianDistribution;

import com.thoughtworks.xstream.annotations.XStreamAsAttribute;

/**
 * An immutable class that represents a ballistic weapon.
 * 
 * @author Li Song
 */
public class BallisticWeapon extends AmmoWeapon {
    @XStreamAsAttribute
    protected final double    spread;
    @XStreamAsAttribute
    protected final Attribute jammingChance;
    @XStreamAsAttribute
    protected final int       shotsduringcooldown;
    @XStreamAsAttribute
    protected final Attribute jammingTime;

    public BallisticWeapon(String aName, String aDesc, String aMwoName, int aMwoId, int aSlots, double aTons, int aHP,
            Faction aFaction, Attribute aHeat, Attribute aCooldown, Attribute aRangeZero, Attribute aRangeMin,
            Attribute aRangeLong, Attribute aRangeMax, double aFallOffExponent, int aRoundsPerShot,
            double aDamagePerProjectile, int aProjectilesPerRound, double aProjectileSpeed, int aGhostHeatGroupId,
            double aGhostHeatMultiplier, int aGhostHeatMaxFreeAlpha, String aAmmoType, double aSpread,
            Attribute aJammingChance, Attribute aJammingTime, int aShotsDuringCooldown, double aVolleyDelay) {
        super(aName, aDesc, aMwoName, aMwoId, aSlots, aTons, HardPointType.BALLISTIC, aHP, aFaction, aHeat, aCooldown,
                aRangeZero, aRangeMin, aRangeLong, aRangeMax, aFallOffExponent, aRoundsPerShot, aDamagePerProjectile,
                aProjectilesPerRound, aProjectileSpeed, aGhostHeatGroupId, aGhostHeatMultiplier,
                aGhostHeatMaxFreeAlpha, aAmmoType, aVolleyDelay);
        spread = aSpread;
        jammingChance = aJammingChance;
        jammingTime = aJammingTime;
        shotsduringcooldown = aShotsDuringCooldown;
    }

    @Override
    public String getShortName() {
        String name = getName();
        name = name.replace("ULTRA ", "U");
        name = name.replace("MACHINE GUN", "MG");
        return name;
    }

    @Override
    public boolean hasSpread() {
        return spread > 0;
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

    public double getShotsDuringCooldown() {
        return shotsduringcooldown;
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
            return super.getSecondsPerShot(aModifiers) + 0.75; // TODO: Fix this when they add the charge time to the
            // itemstats.xml
        }
        return super.getSecondsPerShot(aModifiers);
    }

    @Override
    public double getRangeEffectivity(double range, Collection<Modifier> aPilotModules) {
        double spreadFactor = 1.0;
        if (hasSpread()) {
            // Assumption:
            // The 'spread' value is the standard deviation of a zero-mean gaussian distribution of angles.
            GaussianDistribution gaussianDistribution = new GaussianDistribution();

            final double targetRadius = 6; // [m]
            double maxAngle = Math.atan2(targetRadius, range) * 180 / Math.PI; // [deg]

            // X ~= N(0, spread)
            // P_hit = P(-maxAngle <= X; X <= +maxangle)
            // Xn = (X - 0) / spread ~ N(0,1)
            // P_hit = cdf(maxangle / spread) - cdf(-maxangle / spread) = 2*cdf(maxangle / spread) - 1.0;
            double P_hit = 2 * gaussianDistribution.cdf(maxAngle / spread) - 1;
            spreadFactor = P_hit;
        }
        return spreadFactor * super.getRangeEffectivity(range, aPilotModules);
    }

    public final static Comparator<Item> DEFAULT_ORDERING = DEFAULT_WEAPON_ORDERING;
}
