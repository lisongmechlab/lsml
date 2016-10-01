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
import java.util.Collections;

import org.lisoft.lsml.model.chassi.HardPointType;
import org.lisoft.lsml.model.datacache.ItemDB;
import org.lisoft.lsml.model.modifiers.Attribute;
import org.lisoft.lsml.model.modifiers.Modifier;
import org.lisoft.lsml.model.modifiers.ModifierDescription;

import com.thoughtworks.xstream.annotations.XStreamAsAttribute;

public class Weapon extends HeatSource {
    static final int RANGE_ULP_FUZZ = 5;

    private final Attribute coolDown;

    private final Attribute rangeZero;
    private final Attribute rangeMin;
    private final Attribute rangeLong;
    private final Attribute rangeMax;
    @XStreamAsAttribute
    private final double fallOffExponent;
    /** How many rounds of ammo per shot of the weapon. */
    @XStreamAsAttribute
    private final int roundsPerShot;
    /** How much damage one projectile does. */
    @XStreamAsAttribute
    private final double damagePerProjectile;
    /** How many projectile per one round of ammo. */
    @XStreamAsAttribute
    private final int projectilesPerRound;
    @XStreamAsAttribute
    private final Attribute projectileSpeed;

    @XStreamAsAttribute
    private final int ghostHeatGroupId;

    @XStreamAsAttribute
    private final double ghostHeatMultiplier;

    @XStreamAsAttribute
    private final int ghostHeatFreeAlpha;

    @XStreamAsAttribute
    private final double volleyDelay;

    @XStreamAsAttribute
    private final double impulse;

    public Weapon(
            // Item Arguments
            String aName, String aDesc, String aMwoName, int aMwoId, int aSlots, double aTons,
            HardPointType aHardPointType, int aHP, Faction aFaction,
            // HeatSource Arguments
            Attribute aHeat,
            // Weapon Arguments
            Attribute aCoolDown, Attribute aRangeZero, Attribute aRangeMin, Attribute aRangeLong, Attribute aRangeMax,
            double aFallOffExponent, int aRoundsPerShot, double aDamagePerProjectile, int aProjectilesPerRound,
            Attribute aProjectileSpeed, int aGhostHeatGroupId, double aGhostHeatMultiplier, int aGhostHeatMaxFreeAlpha,
            double aVolleyDelay, double aImpulse) {
        super(aName, aDesc, aMwoName, aMwoId, aSlots, aTons, aHardPointType, aHP, aFaction, null, null, aHeat);
        coolDown = aCoolDown;
        rangeZero = aRangeZero;
        rangeMin = aRangeMin;
        rangeLong = aRangeLong;
        rangeMax = aRangeMax;
        fallOffExponent = aFallOffExponent;
        roundsPerShot = aRoundsPerShot;
        damagePerProjectile = aDamagePerProjectile;
        projectilesPerRound = aProjectilesPerRound;
        projectileSpeed = aProjectileSpeed;
        ghostHeatGroupId = aGhostHeatGroupId;
        ghostHeatMultiplier = aGhostHeatMultiplier;
        ghostHeatFreeAlpha = aGhostHeatMaxFreeAlpha;
        volleyDelay = aVolleyDelay;
        impulse = aImpulse;

        if (roundsPerShot < 1) {
            throw new IllegalArgumentException("All weapons must have Rounds per shot > 0");
        }
    }

    /**
     * @return A {@link Collection} of aliases for the weapon.
     */
    public Collection<String> getAliases() {
        // All attributes have the same aliases, just pick one
        return Collections.unmodifiableCollection(coolDown.getSelectors());
    }

    public int getAmmoPerPerShot() {
        return roundsPerShot;
    }

    /**
     * @return The number of projectiles fired when the player presses "fire".
     */
    public int getProjectilesPerShot(){
        return projectilesPerRound*roundsPerShot;
    }

    public double getCoolDown(Collection<Modifier> aModifiers) {
        return coolDown.value(aModifiers);
    }

    public double getDamagePerShot() {
        return damagePerProjectile * projectilesPerRound * roundsPerShot;
    }

    /**
     * 0 = un-grouped 1 = PPC, ER PPC 2 = LRM20/15/10 3 = LL, ER LL, LPL 4 = SRM6 SRM4
     *
     * @return The ID of the group this weapon belongs to.
     */
    public int getGhostHeatGroup() {
        return ghostHeatGroupId;
    }

    public int getGhostHeatMaxFreeAlpha() {
        return ghostHeatFreeAlpha;
    }

    public double getGhostHeatMultiplier() {
        return ghostHeatMultiplier;
    }

    public double getImpulse() {
        return impulse;
    }

    public double getProjectileSpeed(Collection<Modifier> aModifiers) {
        return projectileSpeed.value(aModifiers);
    }

    public double getRangeEffectiveness(double range, Collection<Modifier> aModifiers) {
        // Assume linear fall off
        if (range < getRangeZero(aModifiers)) {
            return 0;
        }
        else if (range < getRangeMin(aModifiers)) {
            return Math.pow((range - getRangeZero(aModifiers)) / (getRangeMin(aModifiers) - getRangeZero(aModifiers)),
                    fallOffExponent);
        }
        else if (range <= getRangeLong(aModifiers)) {
            return 1.0;
        }
        else if (range < getRangeMax(aModifiers)) {
            // Presumably long range fall off can also be exponential, we'll wait until there is evidence of the fact.
            return 1.0 - (range - getRangeLong(aModifiers)) / (getRangeMax(aModifiers) - getRangeLong(aModifiers));
        }
        else {
            return 0;
        }
    }

    public double getRangeLong(Collection<Modifier> aModifiers) {
        return rangeLong.value(aModifiers);
    }

    public double getRangeMax(Collection<Modifier> aModifiers) {
        if (rangeMax.value(null) == rangeLong.value(null)) {
            return Math.nextUp(rangeMax.value(aModifiers));
        }
        return rangeMax.value(aModifiers);
    }

    public double getRangeMin(Collection<Modifier> aModifiers) {
        return rangeMin.value(aModifiers);
    }

    public double getRangeZero(Collection<Modifier> aModifiers) {
        if (rangeZero.value(null) == rangeMin.value(null)) {
            return Math.nextAfter(rangeZero.value(aModifiers), Double.NEGATIVE_INFINITY);
        }
        return rangeZero.value(aModifiers);
    }

    public double getSecondsPerShot(Collection<Modifier> aModifiers) {
        return getCoolDown(aModifiers) + volleyDelay * (roundsPerShot - 1);
    }

    /**
     * Calculates an arbitrary statistic for the weapon based on the string. The string format is (regexp):
     * "[dsthc]+(/[dsthc]+)?" where d=damage, s=seconds, t=tons, h=heat, c=critical slots. For example "d/hhs" is damage
     * per heat^2 second.
     *
     * @param aWeaponStat
     *            A string specifying the statistic to be calculated. Must match the regexp pattern
     *            "[dsthc]+(/[dsthc]+)?".
     * @param aModifiers
     *            A list of {@link Modifier}s to take into account.
     * @return The calculated statistic.
     */
    public double getStat(String aWeaponStat, Collection<Modifier> aModifiers) {
        double nominator = 1;
        int index = 0;
        while (index < aWeaponStat.length() && aWeaponStat.charAt(index) != '/') {
            switch (aWeaponStat.charAt(index)) {
                case 'd':
                    nominator *= getDamagePerShot();
                    break;
                case 's':
                    nominator *= getSecondsPerShot(aModifiers);
                    break;
                case 't':
                    nominator *= getMass();
                    break;
                case 'h':
                    nominator *= getHeat(aModifiers);
                    break;
                case 'c':
                    nominator *= getSlots();
                    break;
                default:
                    throw new IllegalArgumentException("Unknown identifier: " + aWeaponStat.charAt(index));
            }
            index++;
        }

        index++; // Skip past the '/' if we encountered it, otherwise we'll be at the end of the string anyway.
        double denominator = 1;
        while (index < aWeaponStat.length()) {
            switch (aWeaponStat.charAt(index)) {
                case 'd':
                    denominator *= getDamagePerShot();
                    break;
                case 's':
                    denominator *= getSecondsPerShot(aModifiers);
                    break;
                case 't':
                    denominator *= getMass();
                    break;
                case 'h':
                    denominator *= getHeat(aModifiers);
                    break;
                case 'c':
                    denominator *= getSlots();
                    break;
                default:
                    throw new IllegalArgumentException("Unknown identifier: " + aWeaponStat.charAt(index));
            }
            index++;
        }
        if (nominator == 0.0 && denominator == 0.0) {
            // We take the Brahmaguptan interpretation of 0/0 to be 0 (year 628).
            return 0;
        }
        return nominator / denominator;
    }

    /**
     * @return <code>true</code> if this weapon has a non-linear fall off.
     */
    public boolean hasNonLinearFalloff() {
        return 1.0 != fallOffExponent;
    }

    public boolean hasSpread() {
        return false;
    }

    /**
     * @return <code>true</code> if the Lower Arm Actuator (LAA) and/or Hand Actuator (HA) should be removed if this
     *         weapon is equipped.
     */
    public boolean isLargeBore() {
        return getAliases().contains(ModifierDescription.SPEC_WEAPON_LARGE_BORE);
    }

    public boolean isOffensive() {
        return this != ItemDB.AMS && this != ItemDB.C_AMS;
    }
}
