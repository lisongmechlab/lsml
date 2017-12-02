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

import org.lisoft.lsml.model.NoSuchItemException;
import org.lisoft.lsml.model.database.ItemDB;
import org.lisoft.lsml.model.modifiers.Modifier;
import org.lisoft.lsml.util.Pair;

/**
 * This {@link Comparator} is used to sort items in various ways.
 *
 * @author Li Song
 *
 */
public class ItemComparator implements Comparator<Item>, Serializable {
    /**
     * Applies the same sorting as {@link ItemComparator} but on {@link String} arguments instead.
     *
     * @author Li Song
     */
    public static class ByString implements Comparator<String>, Serializable {
        private static final long serialVersionUID = 4397318588150862878L;
        private final ItemComparator ic;

        public ByString(boolean aPgiMode) {
            ic = new ItemComparator(aPgiMode);
        }

        @Override
        public int compare(String aO1, String aO2) {
            try {
                return ic.compare(ItemDB.lookup(aO1), ItemDB.lookup(aO2));
            }
            catch (final NoSuchItemException e) {
                return aO1.compareTo(aO2);
            }
        }
    }

    private static final long serialVersionUID = 6037307095837548227L;

    private static final int CLASS_SCORE = 100000000; // 100E6
    private static final int FACTION_SCORE = 1; // 100E3

    private static final Map<Item, Pair<Integer, Integer>> ITEM_PRIORITY;

    private static final int RANK_ENERGY = 1 * CLASS_SCORE;
    private static final int RANK_BALLISTIC = 2 * CLASS_SCORE;
    private static final int RANK_MISSILE = 3 * CLASS_SCORE;
    private static final int RANK_AMMOWEAPON = 4 * CLASS_SCORE;
    private static final int RANK_WEAPON = 5 * CLASS_SCORE;
    private static final int RANK_ECM = 6 * CLASS_SCORE;
    private static final int RANK_TCOMP = 7 * CLASS_SCORE;
    private static final int RANK_HEAT_SINK = 8 * CLASS_SCORE;
    private static final int RANK_JUMP_JET = 9 * CLASS_SCORE;
    private static final int RANK_MASC = 10 * CLASS_SCORE;
    private static final int RANK_MISC = 11 * CLASS_SCORE;
    private static final int RANK_ENGINE = 12 * CLASS_SCORE;

    static {
        ITEM_PRIORITY = new HashMap<>();

        for (final Item item : ItemDB.lookup(Item.class)) {
            if (item instanceof Ammunition) {
                continue; // Ammo added together with the weapons later on
            }

            else if (item instanceof BallisticWeapon) {
                final BallisticWeapon weapon = (BallisticWeapon) item;
                final int rank = rankBallistic(weapon);
                ITEM_PRIORITY.put(weapon, new Pair<>(rank, rank));
                if (!weapon.hasBuiltInAmmo()) {
                    ITEM_PRIORITY.put(weapon.getAmmoType(), new Pair<>(rank + 1, rank + 1));
                    ITEM_PRIORITY.put(weapon.getAmmoHalfType(), new Pair<>(rank + 2, rank + 2));
                }
            }
            else if (item instanceof EnergyWeapon) {
                final EnergyWeapon weapon = (EnergyWeapon) item;
                final int rank = rankEnergy(weapon);
                ITEM_PRIORITY.put(weapon, new Pair<>(rank, rank));
            }
            else if (item instanceof MissileWeapon) {
                final MissileWeapon weapon = (MissileWeapon) item;
                final int rank = rankMissile(weapon);
                ITEM_PRIORITY.put(weapon, new Pair<>(rank, rank));
                if (!weapon.hasBuiltInAmmo() && (weapon.getAmmoPerPerShot() == 5 || weapon.getAmmoPerPerShot() == 2
                        || weapon.getAmmoPerPerShot() == 1)) {
                    ITEM_PRIORITY.put(weapon.getAmmoType(), new Pair<>(rank + 1, rank + 1));
                    ITEM_PRIORITY.put(weapon.getAmmoHalfType(), new Pair<>(rank + 2, rank + 2));
                }
            }
            else if (item instanceof AmmoWeapon) {
                final AmmoWeapon weapon = (AmmoWeapon) item;
                final int rank = RANK_AMMOWEAPON + factionScore(weapon) + (weapon.getName().contains("LAS") ? 0 : 10);
                ITEM_PRIORITY.put(weapon, new Pair<>(rank, rank));
                if (!weapon.hasBuiltInAmmo()) {
                    ITEM_PRIORITY.put(weapon.getAmmoType(), new Pair<>(rank + 1, rank + 1));
                    ITEM_PRIORITY.put(weapon.getAmmoHalfType(), new Pair<>(rank + 2, rank + 2));
                }
            }
            else if (item instanceof Weapon) {
                final Weapon weapon = (Weapon) item;
                final int rank = RANK_WEAPON;
                ITEM_PRIORITY.put(weapon, new Pair<>(rank, rank));
            }
            else if (item instanceof Engine) {
                final Engine engine = (Engine) item;
                ITEM_PRIORITY.put(item, new Pair<>(rankEngine(engine, false), rankEngine(engine, true)));
            }
            else if (item instanceof JumpJet) {
                final JumpJet jj = (JumpJet) item;
                final int rank = (int) (RANK_JUMP_JET + 10 * jj.getMinTons());
                ITEM_PRIORITY.put(item, new Pair<>(rank, rank));
            }
            else if (item instanceof MASC) {
                final MASC masc = (MASC) item;
                final int rank = RANK_MASC + 10 * masc.getMinTons();
                ITEM_PRIORITY.put(item, new Pair<>(rank, rank));
            }
            else if (item instanceof ECM) {
                final ECM ecm = (ECM) item;
                final int rank = RANK_ECM + 10 * ecm.getId() % CLASS_SCORE;
                ITEM_PRIORITY.put(item, new Pair<>(rank, rank));
            }
            else if (item instanceof TargetingComputer) {
                final TargetingComputer tc = (TargetingComputer) item;
                final int rank = RANK_TCOMP + (int) (100 * tc.getMass());
                ITEM_PRIORITY.put(item, new Pair<>(rank, rank));
            }
            else if (item instanceof HeatSink) {
                final HeatSink hs = (HeatSink) item;
                final int rank = RANK_HEAT_SINK + 10 * hs.getSlots();
                ITEM_PRIORITY.put(item, new Pair<>(rank, rank));
            }
            else {
                final int rank = RANK_MISC;
                ITEM_PRIORITY.put(item, new Pair<>(rank, rank));
            }
        }
    }

    public static Comparator<Weapon> byRange(Collection<Modifier> aModifiers) {
        return (aO1, aO2) -> Double.compare(aO2.getRangeMax(aModifiers), aO1.getRangeMax(aModifiers));
    }

    /**
     * Calculates an integer score of the damage in the range [1,1000]
     *
     * @param aItem
     *            The {@link Weapon} to calculate the damage score for.
     * @return The damage score as an <code>int</code>.
     */
    private static int damageScore(Weapon aItem) {
        // Assume damage can be in range [0.05, 50]
        // Scale it up by factor 20 to distinguish small numbers.
        int score = (int) (aItem.getDamagePerShot() * 20.0);

        // Score is now in range: [1,1000]. Make large damage come before small by negating the range.
        score = 1001 - score;
        return score; // Result is in range [1, 1000]
    }

    private static int factionScore(Item aItem) {
        return aItem.getFaction() == Faction.CLAN ? 0 : FACTION_SCORE;
    }

    private static int rankBallistic(BallisticWeapon aItem) {
        final int factionBase = factionScore(aItem);
        final int damageBase = damageScore(aItem);
        int score = RANK_BALLISTIC + factionBase;

        if (aItem.getName().contains("GAUSS")) {
            score += 1 * CLASS_SCORE / 10;
        }
        else if (aItem.getName().contains("AC/")) {
            if (aItem.getName().matches("(C-)?(ULTRA |U-).*")) {
                score += 3 * CLASS_SCORE / 10 + damageBase;
            }
            else {
                score += 2 * CLASS_SCORE / 10 + damageBase;
            }
        }
        else if (aItem.getName().contains("-X AC")) {
            score += 4 * CLASS_SCORE / 10 + damageBase;
        }
        else if (aItem.getName().contains("MACHINE GUN")) {
            score += 5 * CLASS_SCORE / 10;
        }
        if (score >= RANK_BALLISTIC + CLASS_SCORE) {
            throw new RuntimeException("Ballistic weapon sorting rank overflow");
        }
        return score;
    }

    private static int rankEnergy(EnergyWeapon aItem) {
        final int factionScore = factionScore(aItem);
        final int scorePPC = aItem.getName().contains("PPC") ? 1 * CLASS_SCORE / 10 : 0;
        final int scoreLaser = aItem.getName().contains("LASER") ? 2 * CLASS_SCORE / 10 : 0;
        final int scoreFlamer = aItem.getName().contains("FLAMER") ? 3 * CLASS_SCORE / 10 : 0;
        final int scoreTag = aItem.getName().contains("TAG") ? 4 * CLASS_SCORE / 10 : 0;

        final int scorePulse = aItem.getName().contains("PULSE") ? -2 * CLASS_SCORE / 100 : 0;
        final int scoreER = aItem.getName().contains("ER ") ? -1 * CLASS_SCORE / 100 : 0;
        final int scoreHeavy = aItem.getName().contains("HEAVY ") ? 3 * CLASS_SCORE / 100 : 0;
        final int scoreLight = aItem.getName().contains("LIGHT ") ? 4 * CLASS_SCORE / 100 : 0;

        final int score = RANK_ENERGY + scorePPC + scoreLaser + scoreFlamer + scoreTag + scorePulse + scoreER
                + scoreHeavy + scoreLight + damageScore(aItem) + factionScore;

        if (score >= RANK_ENERGY + CLASS_SCORE) {
            throw new RuntimeException("Energy weapon sorting rank overflow");
        }
        return score;
    }

    private static int rankEngine(Engine aItem, boolean aPgiMode) {
        // Yes, change the order of the arguments so we get clan first.
        final int factionScore = factionScore(aItem);
        final int xlScore = aItem.getType() == EngineType.STD ? 5 : 0;
        final int ratingScore = 10 * (aPgiMode ? aItem.getRating() : 1000 - aItem.getRating());
        final int score = RANK_ENGINE + ratingScore + xlScore + factionScore;
        if (score >= RANK_ENGINE + CLASS_SCORE) {
            throw new RuntimeException("Engine sorting rank overflow");
        }
        return score;
    }

    private static int rankMissile(MissileWeapon aItem) {
        final int scoreLRM = aItem.getName().contains("LRM ") ? 1 * CLASS_SCORE / 10 : 0;
        final int scoreMRM = aItem.getName().contains("MRM ") ? 2 * CLASS_SCORE / 10 : 0;
        final int scoreSRM = aItem.getName().contains("SRM ") ? 3 * CLASS_SCORE / 10 : 0;
        final int scoreRocket = aItem.getName().contains("ROCKET ") ? 4 * CLASS_SCORE / 10 : 0;
        final int scoreATM = aItem.getName().contains("ATM ") ? 5 * CLASS_SCORE / 10 : 0;
        final int scoreNARC = aItem.getName().contains("NARC") ? 6 * CLASS_SCORE / 10 : 0;

        final int scoreStreak = aItem.getName().matches("(C-)?S(TREAK |-).*") ? 3 * CLASS_SCORE / 10 : 0;

        final int scoreArtemis = aItem.getName().contains("ARTEM") ? 1 : 0;

        final int score = RANK_MISSILE + scoreLRM + scoreMRM + scoreSRM + scoreRocket + scoreATM + scoreStreak
                + scoreNARC + scoreArtemis + (50 - aItem.getAmmoPerPerShot()) * 1000 + factionScore(aItem) * 10;

        if (score >= RANK_MISSILE + CLASS_SCORE) {
            throw new RuntimeException("Missile weapon sorting rank overflow");
        }
        return score;
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
        if (null == aLhs) {
            return aRhs == null ? 0 : 1;
        }
        if (null == aRhs) {
            return -1;
        }

        final Pair<Integer, Integer> left = ITEM_PRIORITY.get(aLhs);
        final Pair<Integer, Integer> right = ITEM_PRIORITY.get(aRhs);

        if (null == left) {
            return null == right ? aLhs.getName().compareTo(aRhs.getName()) : 1;
        }
        if (null == right) {
            return -1;
        }

        if (pgiMode) {
            return Integer.compare(left.second, right.second);
        }
        return Integer.compare(left.first, right.first);
    }
}
