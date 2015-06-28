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
import java.util.Collections;
import java.util.Comparator;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import lisong_mechlab.model.chassi.HardPointType;
import lisong_mechlab.model.modifiers.Attribute;
import lisong_mechlab.model.modifiers.Modifier;
import lisong_mechlab.model.modifiers.ModifiersDB;

import com.thoughtworks.xstream.annotations.XStreamAsAttribute;

public class Weapon extends HeatSource {
    public static final int RANGE_ULP_FUZZ = 5;

    private final Attribute cooldown;
    private final Attribute rangeZero;
    private final Attribute rangeMin;
    private final Attribute rangeLong;
    private final Attribute rangeMax;
    @XStreamAsAttribute
    private final double    fallOffExponent;

    /** How many rounds of ammo per shot of the weapon. */
    @XStreamAsAttribute
    private final int       roundsPerShot;
    /** How much damage one projectile does. */
    @XStreamAsAttribute
    private final double    damagePerProjectile;
    /** How many projectile per one round of ammo. */
    @XStreamAsAttribute
    private final int       projectilesPerRound;
    @XStreamAsAttribute
    private final double    projectileSpeed;
    @XStreamAsAttribute
    private final int       ghostHeatGroupId;
    @XStreamAsAttribute
    private final double    ghostHeatMultiplier;
    @XStreamAsAttribute
    private final int       ghostHeatFreeAlpha;
    @XStreamAsAttribute
    protected final double  volleyDelay;

    public Weapon(String aName, String aDesc, String aMwoName, int aMwoId, int aSlots, double aTons,
            HardPointType aHardPointType, int aHP, Faction aFaction, Attribute aHeat, Attribute aCooldown,
            Attribute aRangeZero, Attribute aRangeMin, Attribute aRangeLong, Attribute aRangeMax,
            double aFallOffExponent, int aRoundsPerShot, double aDamagePerProjectile, int aProjectilesPerRound,
            double aProjectileSpeed, int aGhostHeatGroupId, double aGhostHeatMultiplier, int aGhostHeatMaxFreeAlpha,
            double aVolleyDelay) {
        super(aName, aDesc, aMwoName, aMwoId, aSlots, aTons, aHardPointType, aHP, aFaction, null, null, aHeat);
        cooldown = aCooldown;
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
    }

    public boolean isOffensive() {
        return this != ItemDB.AMS && this != ItemDB.C_AMS;
    }

    /**
     * 0 = ungrouped 1 = PPC, ER PPC 2 = LRM20/15/10 3 = LL, ER LL, LPL 4 = SRM6 SRM4
     * 
     * @return The ID of the group this weapon belongs to.
     */
    public int getGhostHeatGroup() {
        return ghostHeatGroupId;
    }

    public double getGhostHeatMultiplier() {
        return ghostHeatMultiplier;
    }

    public int getGhostHeatMaxFreeAlpha() {
        return ghostHeatFreeAlpha;
    }

    public double getDamagePerShot() {
        return damagePerProjectile * projectilesPerRound * roundsPerShot;
    }

    public int getAmmoPerPerShot() {
        return roundsPerShot;
    }

    public double getSecondsPerShot(Collection<Modifier> aModifiers) {
        return getCoolDown(aModifiers) + volleyDelay*roundsPerShot;
    }

    public double getCoolDown(Collection<Modifier> aModifiers) {
        return cooldown.value(aModifiers);
    }

    public double getProjectileSpeed() {
        return projectileSpeed;
    }

    public double getRangeZero(Collection<Modifier> aModifiers) {
        if (rangeZero.value(null) == rangeMin.value(null))
            return Math.nextAfter(rangeZero.value(aModifiers), Double.NEGATIVE_INFINITY);
        return rangeZero.value(aModifiers);
    }

    public double getRangeMin(Collection<Modifier> aModifiers) {
        return rangeMin.value(aModifiers);
    }

    public double getRangeMax(Collection<Modifier> aModifiers) {
        if (rangeMax.value(null) == rangeLong.value(null))
            return Math.nextUp(rangeMax.value(aModifiers));
        return rangeMax.value(aModifiers);
    }

    public double getRangeLong(Collection<Modifier> aModifiers) {
        return rangeLong.value(aModifiers);
    }

    public double getRangeEffectivity(double range, Collection<Modifier> aModifiers) {
        // Assume linear fall off
        if (range < getRangeZero(aModifiers))
            return 0;
        else if (range < getRangeMin(aModifiers))
            return Math.pow((range - getRangeZero(aModifiers)) / (getRangeMin(aModifiers) - getRangeZero(aModifiers)),
                    fallOffExponent);
        else if (range <= getRangeLong(aModifiers))
            return 1.0;
        else if (range < getRangeMax(aModifiers)) {
            // Presumably long range fall off can also be exponential, we'll wait until there is evidence of the fact.
            return 1.0 - (range - getRangeLong(aModifiers)) / (getRangeMax(aModifiers) - getRangeLong(aModifiers));
        }
        else
            return 0;
    }

    /**
     * Calculates an arbitrary statistic for the weapon based on the string. The string format is (regexp):
     * "[dsthc]+(/[dsthc]+)?" where d=damage, s=seconds, t=tons, h=heat, c=criticalslots. For example "d/hhs" is damage
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
                    nominator *= getNumCriticalSlots();
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
                    denominator *= getNumCriticalSlots();
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

    public boolean hasSpread() {
        return false;
    }

    public final static Comparator<Item> DEFAULT_WEAPON_ORDERING;
    static {
        DEFAULT_WEAPON_ORDERING = new Comparator<Item>() {
            private final Pattern p = Pattern.compile("(\\D*)(\\d*)?.*");

            @Override
            public int compare(Item aLhs, Item aRhs) {
                Matcher mLhs = p.matcher(aLhs.getName());
                Matcher mRhs = p.matcher(aRhs.getName());

                if (!mLhs.matches())
                    throw new RuntimeException("LHS didn't match pattern! [" + aLhs.getName() + "]");

                if (!mRhs.matches())
                    throw new RuntimeException("RHS didn't match pattern! [" + aRhs.getName() + "]");

                if (mLhs.group(1).equals(mRhs.group(1))) {
                    // Same prefix
                    String lhsSuffix = mLhs.group(2);
                    String rhsSuffix = mRhs.group(2);
                    if (lhsSuffix != null && lhsSuffix.length() > 0 && rhsSuffix != null && rhsSuffix.length() > 0)
                        return -Integer.compare(Integer.parseInt(lhsSuffix), Integer.parseInt(rhsSuffix));
                }
                return mLhs.group(1).compareTo(mRhs.group(1));
            }
        };
    }

    /**
     * @return <code>true</code> if this weapon has a non-linear fall off.
     */
    public boolean hasNonLinearFalloff() {
        return 1.0 != fallOffExponent;
    }

    /**
     * @return <code>true</code> if the Lower Arm Actuator (LAA) and/or Hand Actuator (HA) should be removed if this
     *         weapon is equipped.
     */
    public boolean isLargeBore() {
        return getAliases().contains(ModifiersDB.SEL_WEAPON_LARGE_BORE);
    }

    /**
     * @return A {@link Collection} of aliases for the weapon.
     */
    public Collection<String> getAliases() {
        // All attributes have the same aliases, just pick one
        return Collections.unmodifiableCollection(cooldown.getSelectors());
    }
}
