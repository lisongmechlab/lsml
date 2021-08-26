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
import org.lisoft.lsml.model.modifiers.Attribute;
import org.lisoft.lsml.model.modifiers.Modifier;
import org.lisoft.lsml.model.modifiers.ModifierDescription;
import org.lisoft.lsml.util.Pair;

import com.thoughtworks.xstream.annotations.XStreamAsAttribute;

/**
 * Models the basic properties of all weapons.
 *
 * Note: We use the term "shot" with a very specific meaning: a shot is one weapon release event.
 * For most weapons this is one trigger pull. For MGs, flamers and RAC style weapons it's one projectile.
 */
public class Weapon extends HeatSource {
    static final int RANGE_ULP_FUZZ = 5;

    private final Attribute coolDown;

    private final WeaponRangeProfile rangeProfile;

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
    private final Attribute ghostHeatFreeAlpha;

    @XStreamAsAttribute
    private final double volleyDelay;

    @XStreamAsAttribute
    private final double impulse;

    public Weapon(
            // Item Arguments
            String aName, String aDesc, String aMwoName, int aMwoId, int aSlots, double aTons,
            HardPointType aHardPointType, double aHP, Faction aFaction,
            // HeatSource Arguments
            Attribute aHeat,
            // Weapon Arguments
            Attribute aCoolDown, WeaponRangeProfile aRangeProfile, int aRoundsPerShot, double aDamagePerProjectile,
            int aProjectilesPerRound, Attribute aProjectileSpeed, int aGhostHeatGroupId, double aGhostHeatMultiplier,
            Attribute aGhostHeatMaxFreeAlpha, double aVolleyDelay, double aImpulse) {
        super(aName, aDesc, aMwoName, aMwoId, aSlots, aTons, aHardPointType, aHP, aFaction, null, null, aHeat);
        coolDown = aCoolDown;
        rangeProfile = aRangeProfile;
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
     * Gets the modified cooldown value as show in-game. For single shot weapons, this can be positive infinity. For
     * repeated fire weapons (RAC, MG, Flamer, etc) this is the time between projectiles.
     *
     * @param aModifiers that could affect the value
     * @return the actual cooldown value
     */
    public double getCoolDown(Collection<Modifier> aModifiers) {
        return coolDown.value(aModifiers);
    }

    public double getDamagePerProjectile() {
        return damagePerProjectile;
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

    public int getGhostHeatMaxFreeAlpha(Collection<Modifier> aModifiers) {
        return (int) Math.round(ghostHeatFreeAlpha.value(aModifiers));
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

    /**
     * @return The number of projectiles fired when the player presses "fire".
     */
    public int getProjectilesPerShot() {
        return projectilesPerRound * roundsPerShot;
    }

    public double getRangeEffectiveness(double aRange, Collection<Modifier> aModifiers) {
        return rangeProfile.rangeEffectiveness(aRange, aModifiers);
    }

    public double getRangeMax(Collection<Modifier> aModifiers) {
        return rangeProfile.getMaxRange(aModifiers);
    }

    public Pair<Double, Double> getRangeOptimal(Collection<Modifier> aModifiers) {
        return rangeProfile.getOptimalRange(aModifiers);
    }

    public WeaponRangeProfile getRangeProfile() {
        return rangeProfile;
    }

    /**
     * The statistically expected time between shots accounting for weapon jams and double fire etc.
     *
     * @return The firing period [seconds]
     */
    public double getExpectedFiringPeriod(Collection<Modifier> aModifiers) {
        return getRawFiringPeriod(aModifiers) ;
    }

    /**
     * The unmodified time between shots. C.f. {@link #getExpectedFiringPeriod(Collection)}.
     *
     * Note that this is different from cooldown which is the time the weapon is unavailable between uses, this is
     * the time between activations of the weapon. In particular this includes the time that it takes to charge
     * a gauss rifle, the burn time of lasers, the volley delay from LRMs etc that is not included in cooldown.
     *
     * @return The firing period [seconds]
     */
    public double getRawFiringPeriod(Collection<Modifier> aModifiers) {
        return getCoolDown(aModifiers) + volleyDelay * (roundsPerShot - 1);
    }

    /**
     * Gets a specified stat value by character identifier. Useful for constructing composite stats from a string.
     *
     * Format: d=damage, s=seconds, t=tons, h=heat, c=critical slots, r=raw cooldown
     *
     * @param aStat the stat to get.
     * @param aModifiers The modifiers to apply from quirks etc.
     * @return the value of the specified stat
     */
    public double getStat(char aStat, Collection<Modifier> aModifiers){
        switch (aStat) {
            case 'd': return getDamagePerShot();
            case 's': return getExpectedFiringPeriod(aModifiers);
            case 'r': return getRawFiringPeriod(aModifiers);
            case 't': return getMass();
            case 'h': return getHeat(aModifiers);
            case 'c': return getSlots();
            default: throw new IllegalArgumentException("Unknown identifier: " + aStat);
        }
    }

    /**
     * Calculates an arbitrary statistic for the weapon based on a string, the format is "[dsthcr]+(/[dsthcr]+)?".
     * For example "d/hhs" is damage per heat^2 second, see {@link #getStat(char, Collection)} for format details.
     *
     * @param aWeaponStat
     *            A string specifying the statistic to be calculated. Must match the regexp pattern
     *            "[dsthcr]+(/[dsthcr]+)?".
     * @param aModifiers
     *            A list of {@link Modifier}s to take into account.
     * @return The calculated statistic.
     */
    public double getStat(String aWeaponStat, Collection<Modifier> aModifiers) {
        double nominator = 1.0;
        int index = 0;
        while (index < aWeaponStat.length() && aWeaponStat.charAt(index) != '/') {
            nominator *= getStat(aWeaponStat.charAt(index++), aModifiers);
        }
        index++; // Skip past the '/' if we encountered it, otherwise we'll be at the end of the string anyway.
        double denominator = 1.0;
        while (index < aWeaponStat.length()) {
            denominator *= getStat(aWeaponStat.charAt(index++), aModifiers);
        }
        if (nominator == 0.0 && denominator == 0.0) {
            // We take the Brahmaguptan interpretation of 0/0 to be 0 (year 628).
            return 0;
        }
        return nominator / denominator;
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
        return getHardpointType() != HardPointType.AMS;
    }
}
