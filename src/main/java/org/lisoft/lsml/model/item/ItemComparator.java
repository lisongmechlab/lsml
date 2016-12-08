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

import java.io.Serializable;
import java.util.Collection;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.lisoft.lsml.model.modifiers.Modifier;

/**
 * This {@link Comparator} is used to sort items in various ways.
 *
 * @author Li Song
 *
 */
public class ItemComparator implements Comparator<Item>, Serializable {
    enum LaserSize {
        LARGE, MEDIUM, SMALL;

        public static LaserSize identify(String aSize) {
            if (aSize.equals("LARGE") || aSize.equals("LRG")) {
                return LARGE;
            }
            else if (aSize.equals("MEDIUM") || aSize.equals("MED")) {
                return MEDIUM;
            }
            else if (aSize.equals("SMALL") || aSize.equals("SML")) {
                return SMALL;
            }
            else {
                throw new IllegalArgumentException("Unknown laser size!");
            }
        }
    }

    enum WeaponType {
        ER_PPC, PPC, LPLAS, MPLAS, SPLAS, ERLLAS, ERMLAS, ERSLAS, LLAS, MLAS, SLAS, FLAMER, TAG, // Energy
        GAUSS, AC20, AC10, AC5, AC2, UAC20, UAC10, UAC5, UAC2, LBX20, LBX10, LBX5, LBX2, MACHINEGUN, // Ballistic
        LRM20, LRM15, LRM10, LRM5, LRM_AMMO, SRM6, SRM4, SRM2, SRM_AMMO, SSRM6, SSRM4, SSRM2, SSRM_AMMO, NARC, // Missile
        AMS, UNKNOWN; // Misc

        private final static Pattern ENERGY_PATTERN = Pattern
                .compile("(?:C-)?\\s*(ER )?\\s*(LARGE|LRG|MEDIUM|MED|SMALL|SML)?\\s*(PULSE)?\\s*(LASER|PPC).*");

        private final static Pattern BALLISTIC_PATTERN = Pattern
                .compile("(?:C-)?(U-|ULTRA )?(MACHINE|GAUSS|AC|LB)\\D*(\\d+)?.*");
        private final static Pattern MISSILE_PATTERN = Pattern
                .compile("(?:C-)?(LRM|SRM|STREAK SRM|NARC) ?(AMMO)?\\D*(\\d+)?.*");

        public static WeaponType identify(String aWeapon) {
            final Matcher energy = ENERGY_PATTERN.matcher(aWeapon);
            if (energy.matches()) {
                // Group 1: ER
                // Group 2: Laser Size
                // Group 3: pulse
                // Group 4: PPC/Laser
                final boolean isER = "ER ".equals(energy.group(1));
                final boolean isPPC = "PPC".equals(energy.group(4));
                final boolean isPulse = "PULSE".equals(energy.group(3));

                if (isPPC) {
                    return isER ? ER_PPC : PPC;
                }
                // Must be laser as the regex matched
                switch (LaserSize.identify(energy.group(2))) {
                    case LARGE:
                        return isPulse ? LPLAS : isER ? ERLLAS : LLAS;
                    case MEDIUM:
                        return isPulse ? MPLAS : isER ? ERMLAS : MLAS;
                    case SMALL:
                        return isPulse ? SPLAS : isER ? ERSLAS : SLAS;
                    default:
                        throw new RuntimeException("Missing case in switch!");
                }
            }
            else if (aWeapon.contains(TAG.toString())) {
                return TAG;
            }
            else if (aWeapon.contains(FLAMER.toString())) {
                return FLAMER;
            }

            final Matcher ballistic = BALLISTIC_PATTERN.matcher(aWeapon);
            if (ballistic.matches()) {
                // Group 1: ULTRA
                // Group 2: MACHINE/GAUSS/AC/LB
                // Group 3: size
                if ("GAUSS".equals(ballistic.group(2))) {
                    return GAUSS;
                }
                if ("MACHINE".equals(ballistic.group(2))) {
                    return MACHINEGUN;
                }

                final boolean isULTRA = "ULTRA ".equals(ballistic.group(1)) || "U-".equals(ballistic.group(1));
                final boolean isLB = "LB".equals(ballistic.group(2));
                switch (ballistic.group(3)) {
                    case "2":
                        return isLB ? LBX2 : isULTRA ? UAC2 : AC2;
                    case "5":
                        return isLB ? LBX5 : isULTRA ? UAC5 : AC5;
                    case "10":
                        return isLB ? LBX10 : isULTRA ? UAC10 : AC10;
                    case "20":
                        return isLB ? LBX20 : isULTRA ? UAC20 : AC20;
                    default:
                        throw new RuntimeException("Missing case in switch!");
                }
            }

            final Matcher missile = MISSILE_PATTERN.matcher(aWeapon);
            if (missile.matches()) {
                // Group 1: LRM/SRM/STREAK SRM/NARC
                // Group 2: AMMO
                // Group 3: size
                final boolean isAmmo = "AMMO".equals(missile.group(2));
                final String size = missile.group(3);
                switch (missile.group(1)) {
                    case "NARC":
                        return WeaponType.NARC;
                    case "LRM":
                        return isAmmo ? LRM_AMMO
                                : "20".equals(size) ? LRM20
                                        : "15".equals(size) ? LRM15 : "10".equals(size) ? LRM10 : LRM5;
                    case "SRM":
                        return isAmmo ? SRM_AMMO : "6".equals(size) ? SRM6 : "4".equals(size) ? SRM4 : SRM2;
                    case "STREAK SRM":
                        return isAmmo ? SSRM_AMMO : "6".equals(size) ? SSRM6 : "4".equals(size) ? SSRM4 : SSRM2;
                    default:
                        throw new RuntimeException("Missing case in switch: " + missile.group(2));
                }
            }

            if (aWeapon.contains(AMS.toString())) {
                return AMS;
            }
            return UNKNOWN;

        }
    }

    private static final long serialVersionUID = 6037307095837548227L;
    private static final int WEAPON_PRIORITY = 10;
    private static final Map<Class<?>, Integer> CLASS_PRIORITY;

    public final static Comparator<String> WEAPONS_NATURAL_STRING;
    public final static Comparator<Item> WEAPONS_NATURAL;
    public final static Comparator<Item> NATURAL_PGI = new ItemComparator(true);
    public final static Comparator<Item> NATURAL_LSML = new ItemComparator(false);

    static {
        CLASS_PRIORITY = new HashMap<>();

        CLASS_PRIORITY.put(EnergyWeapon.class, WEAPON_PRIORITY);
        CLASS_PRIORITY.put(BallisticWeapon.class, WEAPON_PRIORITY);
        CLASS_PRIORITY.put(MissileWeapon.class, WEAPON_PRIORITY);
        CLASS_PRIORITY.put(AmmoWeapon.class, WEAPON_PRIORITY);
        CLASS_PRIORITY.put(Ammunition.class, WEAPON_PRIORITY);
        CLASS_PRIORITY.put(Weapon.class, WEAPON_PRIORITY);

        CLASS_PRIORITY.put(ECM.class, 20);
        CLASS_PRIORITY.put(TargetingComputer.class, 21);
        CLASS_PRIORITY.put(Item.class, 22);

        CLASS_PRIORITY.put(HeatSink.class, 30);
        CLASS_PRIORITY.put(Module.class, 31);
        CLASS_PRIORITY.put(JumpJet.class, 32);
        CLASS_PRIORITY.put(MASC.class, 33);

        CLASS_PRIORITY.put(Engine.class, 100);
        CLASS_PRIORITY.put(HeatSource.class, 200);
        CLASS_PRIORITY.put(Internal.class, 300);

        WEAPONS_NATURAL_STRING = (aLhs, aRhs) -> compareWeaponsByString(aLhs, aRhs);
        WEAPONS_NATURAL = (aLhs, aRhs) -> WEAPONS_NATURAL_STRING.compare(aLhs.getName(), aRhs.getName());
    }

    public static Comparator<Weapon> byRange(Collection<Modifier> aModifiers) {
        return (aO1, aO2) -> {
            final int comp = Double.compare(aO2.getRangeMax(aModifiers), aO1.getRangeMax(aModifiers));
            if (comp == 0) {
                // PGI mode is irrelevant when sorting by range.
                return NATURAL_LSML.compare(aO1, aO2);
            }
            return comp;
        };
    }

    private static int compareWeaponsByString(String aLhs, String aRhs) {
        final WeaponType rhsType = WeaponType.identify(aRhs);
        final WeaponType lhsType = WeaponType.identify(aLhs);
        final int cmp = lhsType.compareTo(rhsType);
        if (0 != cmp) {
            return cmp;
        }

        if (rhsType != WeaponType.UNKNOWN) { // cmp == 0 -> lhs != UNKNOWN too
            final int factionCmp = Boolean.compare(aRhs.startsWith("C-"), aLhs.startsWith("C-"));
            if (0 != factionCmp) {
                return factionCmp;
            }

            final int ammoCmp = Boolean.compare(aLhs.contains("AMMO"), aRhs.contains("AMMO"));
            if (0 != ammoCmp) {
                return ammoCmp;
            }

            final int ammoHalfCmp = Boolean.compare(aLhs.contains("(1/2)"), aRhs.contains("(1/2)"));
            if (0 != ammoHalfCmp) {
                return ammoHalfCmp;
            }
        }

        return aLhs.compareTo(aRhs); // Fall back on lexicographical ordering for unknowns
    }

    private final boolean pgiMode;

    /**
     * Creates a new comparator.
     *
     * @param aPgiMode
     *            <code>true</code> if the comparator should match PGI's sort order.
     */
    public ItemComparator(boolean aPgiMode) {
        pgiMode = aPgiMode;
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

        if (aLhs instanceof Engine) {
            return compareEngines((Engine) aLhs, (Engine) aRhs);
        }

        if (aLhs instanceof JumpJet) {
            return compareJumpJets((JumpJet) aLhs, (JumpJet) aRhs);
        }

        if (WEAPON_PRIORITY == obtainPriority(aLhs)) {
            return WEAPONS_NATURAL.compare(aLhs, aRhs);
        }
        return aLhs.getShortName().compareTo(aRhs.getShortName());
    }

    private int compareByClass(Item lhs, Item rhs) {
        return Integer.compare(obtainPriority(lhs), obtainPriority(rhs));
    }

    private int compareEngines(Engine aLhs, Engine aRhs) {
        final int ratingCmp = Integer.compare(aRhs.getRating(), aLhs.getRating());
        if (0 != ratingCmp) {
            if (pgiMode) {
                return -ratingCmp;
            }
            return ratingCmp;
        }

        final int typeCmp = aLhs.getType().compareTo(aRhs.getType());
        if (0 != typeCmp) {
            return typeCmp;
        }
        return aRhs.getFaction().compareTo(aLhs.getFaction());
    }

    private int compareJumpJets(JumpJet aLhs, JumpJet aRhs) {
        return Double.compare(aLhs.getMinTons(), aRhs.getMinTons());
    }

    private int obtainPriority(Item aLhs) {
        Class<?> clazz = aLhs.getClass();
        while (null != clazz) {
            final Integer priority = CLASS_PRIORITY.get(clazz);
            if (null != priority) {
                return priority;
            }
            clazz = clazz.getSuperclass();
        }
        return -1;
    }
}
