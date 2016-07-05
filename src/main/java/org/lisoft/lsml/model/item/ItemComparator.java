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

import java.io.Serializable;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.lisoft.lsml.model.chassi.HardPointType;

/**
 * This {@link Comparator} is used to sort items in various ways.
 *
 * @author Emily Björk
 *
 */
public class ItemComparator implements Comparator<Item>, Serializable {
    private static final long serialVersionUID = 6037307095837548227L;
    private static final int AMMO_WEAPON_PRIORITY = 10;
    private static final Map<Class<? extends Item>, Integer> CLASS_PRIORITY;
    private final static Pattern GENERIC_WEAPON_PATTERN = Pattern.compile("(\\D*)(\\d*)?.*");
    private final static Pattern ENERGY_PATTERN = Pattern
            .compile("(?:C-)?\\s*(ER)?\\s*(LARGE|LRG|MEDIUM|MED|SMALL|SML)?\\s*(PULSE)?\\s*(LASER|PPC).*");

    public final static Comparator<String> WEAPONS_NATURAL_STRING;
    public final static Comparator<Item> WEAPONS_NATURAL;
    public final static Comparator<Weapon> WEAPONS_BY_RANGE;
    public final static Comparator<Item> NATURAL = new ItemComparator();

    static {
        CLASS_PRIORITY = new HashMap<>();

        CLASS_PRIORITY.put(EnergyWeapon.class, AMMO_WEAPON_PRIORITY - 1);
        CLASS_PRIORITY.put(BallisticWeapon.class, AMMO_WEAPON_PRIORITY);
        CLASS_PRIORITY.put(MissileWeapon.class, AMMO_WEAPON_PRIORITY);
        CLASS_PRIORITY.put(AmmoWeapon.class, AMMO_WEAPON_PRIORITY);
        CLASS_PRIORITY.put(Ammunition.class, AMMO_WEAPON_PRIORITY);
        CLASS_PRIORITY.put(Weapon.class, AMMO_WEAPON_PRIORITY + 1);

        CLASS_PRIORITY.put(ECM.class, 20);
        CLASS_PRIORITY.put(TargetingComputer.class, 21);
        CLASS_PRIORITY.put(Item.class, 22);

        CLASS_PRIORITY.put(HeatSink.class, 30);
        CLASS_PRIORITY.put(JumpJet.class, 31);
        CLASS_PRIORITY.put(MASC.class, 32);

        CLASS_PRIORITY.put(Engine.class, 100);
        CLASS_PRIORITY.put(HeatSource.class, 200);
        CLASS_PRIORITY.put(Internal.class, 300);

        WEAPONS_NATURAL_STRING = (aLhs, aRhs) -> compareWeaponsByString(aLhs, aRhs);
        WEAPONS_NATURAL = (aLhs, aRhs) -> WEAPONS_NATURAL_STRING.compare(aLhs.getName(), aRhs.getName());

        WEAPONS_BY_RANGE = (aO1, aO2) -> {
            final int comp = Double.compare(aO2.getRangeMax(null), aO1.getRangeMax(null));
            if (comp == 0) {
                return NATURAL.compare(aO1, aO2);
            }
            return comp;
        };
    }

    private static int compareWeaponsByString(String aLhs, String aRhs) {
        Matcher mLhs = ENERGY_PATTERN.matcher(aLhs);
        Matcher mRhs = ENERGY_PATTERN.matcher(aRhs);
        if (mLhs.matches() && mRhs.matches()) {
            // Group PPCs and Lasers together
            final int ppcVsLaser = mRhs.group(4).compareTo(mLhs.group(4));
            if (ppcVsLaser == 0) {
                // Group pulses together.
                if (mLhs.group(3) != null && mRhs.group(3) == null) {
                    return -1;
                }
                else if (mLhs.group(3) == null && mRhs.group(3) != null) {
                    return 1;
                }

                // Group ER together
                if (mLhs.group(1) != null && mRhs.group(1) == null) {
                    return -1;
                }
                else if (mLhs.group(1) == null && mRhs.group(1) != null) {
                    return 1;
                }

                // Order by size
                if (mLhs.group(2) != null && mRhs.group(2) != null) {
                    return Integer.compare(laserSizeIndex(mRhs.group(2)), laserSizeIndex(mLhs.group(2)));
                }
            }
            return ppcVsLaser;
        }

        mLhs = GENERIC_WEAPON_PATTERN.matcher(aLhs);
        mRhs = GENERIC_WEAPON_PATTERN.matcher(aRhs);

        if (!mLhs.matches()) {
            throw new RuntimeException("LHS didn't match pattern! [" + aLhs + "]");
        }

        if (!mRhs.matches()) {
            throw new RuntimeException("RHS didn't match pattern! [" + aRhs + "]");
        }

        if (mLhs.group(1).equals(mRhs.group(1))) {
            // Same prefix
            final String lhsSuffix = mLhs.group(2);
            final String rhsSuffix = mRhs.group(2);
            if (lhsSuffix != null && lhsSuffix.length() > 0 && rhsSuffix != null && rhsSuffix.length() > 0) {
                return Integer.compare(Integer.parseInt(rhsSuffix), Integer.parseInt(lhsSuffix));
            }
        }
        return mLhs.group(1).compareTo(mRhs.group(1));
    }

    private static int laserSizeIndex(String aSize) {
        if (aSize.equals("LARGE") || aSize.equals("LRG")) {
            return 3;
        }
        else if (aSize.equals("MEDIUM") || aSize.equals("MED")) {
            return 2;
        }
        else if (aSize.equals("SMALL") || aSize.equals("SML")) {
            return 1;
        }
        else {
            throw new RuntimeException("Unknown laser size!");
        }
    }

    /**
     * Defines the default sorting order of arbitrary items.
     * <p>
     * The sorting order is as follows:
     * <ol>
     * <li>Energy weapons</li>
     * <li>Ballistic weapons + ammo</li>
     * <li>Missile weapons + ammo</li>
     * <li>AMS + ammo</li>
     * <li>ECM</li>
     * <li>Other items except engines</li>
     * <li>Engines</li>
     * </ol>
     * .
     */
    @Override
    public int compare(Item aLhs, Item aRhs) {
        final int classCompare = compareByClass(aLhs, aRhs);
        if (classCompare != 0) {
            return classCompare;
        }

        if (aLhs instanceof Engine && aRhs instanceof Engine) {
            return compareEngines((Engine) aLhs, (Engine) aRhs);
        }

        if (aLhs instanceof JumpJet && aRhs instanceof JumpJet) {
            return compareJumpJets((JumpJet) aLhs, (JumpJet) aRhs);
        }

        if (AMMO_WEAPON_PRIORITY == CLASS_PRIORITY.get(aLhs.getClass())) {
            return compareAmmoWeaponsOrAmmo(aLhs, aRhs);
        }

        if (aLhs instanceof Weapon && aRhs instanceof Weapon) {
            return WEAPONS_NATURAL.compare(aLhs, aRhs);
        }

        return aLhs.getShortName().compareTo(aRhs.getShortName());
    }

    private int compareAmmoWeaponsOrAmmo(Item aLhs, Item aRhs) {
        // Count ammunition types together with their parent weapon type.
        final HardPointType lhsHp = aLhs instanceof Ammunition ? ((Ammunition) aLhs).getWeaponHardpointType()
                : aLhs.getHardpointType();
        final HardPointType rhsHp = aRhs instanceof Ammunition ? ((Ammunition) aRhs).getWeaponHardpointType()
                : aRhs.getHardpointType();

        // Sort by hard point type (order they appear in the enumeration declaration)
        // This gives the main order of items as given in the java doc.
        final int hp = lhsHp.compareTo(rhsHp);

        // Resolve ties
        if (hp == 0) {
            // Ammunition after weapons in same hard point.
            if (aLhs instanceof Ammunition && !(aRhs instanceof Ammunition)) {
                return 1;
            }
            else if (!(aLhs instanceof Ammunition) && aRhs instanceof Ammunition) {
                return -1;
            }

            return WEAPONS_NATURAL.compare(aLhs, aRhs);
        }
        return hp;
    }

    private int compareByClass(Item lhs, Item rhs) {
        return Integer.compare(CLASS_PRIORITY.get(lhs.getClass()), CLASS_PRIORITY.get(rhs.getClass()));
    }

    private int compareEngines(Engine aLhs, Engine aRhs) {
        final int ratingCmp = Integer.compare(aLhs.getRating(), aRhs.getRating());
        if (ratingCmp == 0) {
            return aLhs.getType().compareTo(aRhs.getType());
        }
        return ratingCmp;
    }

    private int compareJumpJets(JumpJet aLhs, JumpJet aRhs) {
        return Double.compare(aLhs.getMinTons(), aRhs.getMinTons());
    }
}
