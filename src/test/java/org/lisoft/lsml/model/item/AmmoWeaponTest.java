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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.junit.Test;
import org.lisoft.lsml.math.probability.BinomialDistribution;
import org.lisoft.lsml.model.chassi.HardPointType;
import org.lisoft.lsml.model.database.ItemDB;
import org.lisoft.lsml.model.database.ModifiersDB;
import org.lisoft.lsml.model.modifiers.Attribute;
import org.lisoft.lsml.model.modifiers.Modifier;
import org.lisoft.lsml.model.modifiers.ModifierDescription;
import org.lisoft.lsml.model.modifiers.ModifierType;
import org.lisoft.lsml.model.modifiers.Operation;

/**
 * Test suite for {@link AmmoWeapon}.
 *
 * @author Emily Björk
 */
public class AmmoWeaponTest {
    private final List<String> selectors = new ArrayList<>(ModifiersDB.getAllWeaponSelectors());

    private final String name = "name";
    private final String desc = "desc";
    private final String mwoMame = "mwoName";
    private final int id = 3;
    private final int slots = 2;
    private final double tons = 3.1;
    private final int hp = 10;
    private final Attribute heat = new Attribute(2.0, selectors, ModifierDescription.SPEC_WEAPON_SPREAD);
    private final Attribute cd = new Attribute(3.0, selectors, ModifierDescription.SPEC_WEAPON_SPREAD);
    private Attribute rangeZero = new Attribute(10.0, selectors, ModifierDescription.SPEC_WEAPON_SPREAD);
    private Attribute rangeMin = new Attribute(20.0, selectors, ModifierDescription.SPEC_WEAPON_SPREAD);
    private Attribute rangeLong = new Attribute(30.0, selectors, ModifierDescription.SPEC_WEAPON_SPREAD);
    private Attribute rangeMax = new Attribute(40.0, selectors, ModifierDescription.SPEC_WEAPON_SPREAD);
    private final double fallOffExponent = 0.0;
    private int roundsPerShot = 1;
    private final double damagePerProjectile = 10;
    private int projectilesPerRound = 1;
    private Attribute projectileSpeed;
    private final int ghostHeatGroupId = 0;
    private final double ghostHeatMultiplier = 12;
    private final int ghostHeatFreeAlpha = 3;
    private final double volleyDelay = 0.0;
    private final double impulse = 0.0;
    private final String ammoType = "ammo";
    private Attribute spread = new Attribute(0.0, selectors, ModifierDescription.SPEC_WEAPON_SPREAD);
    private final HardPointType hardPoint = HardPointType.NONE;

    @Test
    public void testGetRangeEffectivenessSpreadLotsOfProjectiles() {
        // We don't want range affecting the result.
        rangeZero = new Attribute(0.0, selectors, ModifierDescription.SPEC_WEAPON_SPREAD);
        rangeMin = new Attribute(0.0, selectors, ModifierDescription.SPEC_WEAPON_SPREAD);
        rangeLong = new Attribute(1000.0, selectors, ModifierDescription.SPEC_WEAPON_SPREAD);
        rangeMax = new Attribute(1000.0, selectors, ModifierDescription.SPEC_WEAPON_SPREAD);

        // Compute the required range to have "angle" field of view on "radius" sized target.
        final double angleRad = Math.PI / 6.0; // 30 degrees
        final double radius = 6.0;
        final double range = radius / Math.tan(angleRad); // tan = y / x -> range = radius / tan

        // 1 STD dev = the angle of attack. Means each trial has expected 68.27% chance to hit the
        // target at the given range.
        spread = new Attribute(Math.toDegrees(angleRad), selectors, ModifierDescription.SPEC_WEAPON_SPREAD);
        final double P_hit = 0.6827;

        projectilesPerRound = 20;
        roundsPerShot = 1;
        final AmmoWeapon cut = new AmmoWeapon(name, desc, mwoMame, id, slots, tons, hardPoint, hp, Faction.CLAN, heat,
                cd, rangeZero, rangeMin, rangeLong, rangeMax, fallOffExponent, roundsPerShot, damagePerProjectile,
                projectilesPerRound, projectileSpeed, ghostHeatGroupId, ghostHeatMultiplier, ghostHeatFreeAlpha,
                volleyDelay, impulse, ammoType, spread);

        final BinomialDistribution bin = new BinomialDistribution(P_hit, projectilesPerRound);

        double expectedHits = 0;
        for (int i = 1; i <= projectilesPerRound; ++i) {
            expectedHits += i * bin.pdf(i);
        }

        final double ans = cut.getRangeEffectiveness(range, null);
        assertEquals(expectedHits / projectilesPerRound, ans, 0.0002);
    }

    @Test
    public void testGetRangeEffectivenessSpreadManyProjectiles() {
        // We don't want range affecting the result.
        rangeZero = new Attribute(0.0, selectors, ModifierDescription.SPEC_WEAPON_SPREAD);
        rangeMin = new Attribute(0.0, selectors, ModifierDescription.SPEC_WEAPON_SPREAD);
        rangeLong = new Attribute(1000.0, selectors, ModifierDescription.SPEC_WEAPON_SPREAD);
        rangeMax = new Attribute(1000.0, selectors, ModifierDescription.SPEC_WEAPON_SPREAD);

        // Compute the required range to have "angle" field of view on "radius" sized target.
        final double angleRad = Math.PI / 6.0; // 30 degrees
        final double radius = 6.0;
        final double range = radius / Math.tan(angleRad); // tan = y / x -> range = radius / tan

        // 1 STD dev = the angle of attack. Means each trial has expected 68.27% chance to hit the
        // target at the given range.
        spread = new Attribute(Math.toDegrees(angleRad), selectors, ModifierDescription.SPEC_WEAPON_SPREAD);
        // final double P_hit = 0.6827;

        projectilesPerRound = 6;
        roundsPerShot = 1;
        final AmmoWeapon cut = new AmmoWeapon(name, desc, mwoMame, id, slots, tons, hardPoint, hp, Faction.CLAN, heat,
                cd, rangeZero, rangeMin, rangeLong, rangeMax, fallOffExponent, roundsPerShot, damagePerProjectile,
                projectilesPerRound, projectileSpeed, ghostHeatGroupId, ghostHeatMultiplier, ghostHeatFreeAlpha,
                volleyDelay, impulse, ammoType, spread);

        // Each shot has 68.27% chance to land on target, we need to calculate the estimated number of
        // shots that will hit. Values taken from statistical tables for p = 0.70, introduces ~2% error
        // final double p0 = 0.0007;
        final double p1 = 0.0102;
        final double p2 = 0.0595;
        final double p3 = 0.1852;
        final double p4 = 0.3241;
        final double p5 = 0.3025;
        final double p6 = 0.1176;

        final double estHits = p1 * 1 + p2 * 2 + p3 * 3 + p4 * 4 + p5 * 5 + p6 * 6;

        final double ans = cut.getRangeEffectiveness(range, null);
        assertEquals(estHits / projectilesPerRound, ans, 0.02); // Accept 2% error as we introduced it above.
    }

    @Test
    public void testGetRangeEffectivenessSpreadOneProjectile() {
        // We don't want range affecting the result.
        rangeZero = new Attribute(0.0, selectors, ModifierDescription.SPEC_WEAPON_SPREAD);
        rangeMin = new Attribute(0.0, selectors, ModifierDescription.SPEC_WEAPON_SPREAD);
        rangeLong = new Attribute(1000.0, selectors, ModifierDescription.SPEC_WEAPON_SPREAD);
        rangeMax = new Attribute(1000.0, selectors, ModifierDescription.SPEC_WEAPON_SPREAD);

        // Compute the required range to have "angle" field of view on "radius" sized target.
        final double angleRad = Math.PI / 6.0; // 30 degrees
        final double radius = 6.0;
        final double range = radius / Math.tan(angleRad); // tan = y / x -> range = radius / tan

        // 1 STD dev = the angle of attack. Means each trial has expected 68.27% chance to hit the
        // target at the given range.
        spread = new Attribute(Math.toDegrees(angleRad), selectors, ModifierDescription.SPEC_WEAPON_SPREAD);
        final double P_hit = 0.6827;

        projectilesPerRound = 1;
        roundsPerShot = 1;
        final AmmoWeapon cut = new AmmoWeapon(name, desc, mwoMame, id, slots, tons, hardPoint, hp, Faction.CLAN, heat,
                cd, rangeZero, rangeMin, rangeLong, rangeMax, fallOffExponent, roundsPerShot, damagePerProjectile,
                projectilesPerRound, projectileSpeed, ghostHeatGroupId, ghostHeatMultiplier, ghostHeatFreeAlpha,
                volleyDelay, impulse, ammoType, spread);

        final double ans = cut.getRangeEffectiveness(range, null);
        assertEquals(P_hit, ans, 0.0001);
    }

    @Test
    public final void testIsCompatibleAmmo() throws Exception {
        final BallisticWeapon ac20 = (BallisticWeapon) ItemDB.lookup("AC/20");

        final Ammunition ac20ammo = (Ammunition) ItemDB.lookup("AC/20 AMMO");
        final Ammunition ac20ammoHalf = (Ammunition) ItemDB.lookup("AC/20 AMMO (1/2)");

        assertTrue(ac20.isCompatibleAmmo(ac20ammoHalf));
        assertTrue(ac20.isCompatibleAmmo(ac20ammo));
    }

    @Test
    public final void testSpreadQuirks() {
        final ModifierDescription quirkDescription = new ModifierDescription(null, null, Operation.MUL,
                ModifierDescription.SEL_ALL_WEAPONS, ModifierDescription.SPEC_WEAPON_SPREAD,
                ModifierType.POSITIVE_GOOD);
        final Modifier modifier = new Modifier(quirkDescription, 1.0);

        final AmmoWeapon cut = (AmmoWeapon) ItemDB.lookup("SRM6");

        final double normal = cut.getSpread(null);
        final double quirked = cut.getSpread(Arrays.asList(modifier));

        assertEquals(normal * 2, quirked, 0.0);
    }
}
