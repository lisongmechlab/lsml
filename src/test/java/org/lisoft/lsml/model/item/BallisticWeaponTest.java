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

import static org.junit.Assert.*;

import java.util.*;

import org.junit.Test;
import org.lisoft.lsml.model.database.ItemDB;
import org.lisoft.lsml.model.modifiers.*;

/**
 * Test suite for ballistic weapons.
 *
 * @author Li Song
 */
public class BallisticWeaponTest {
    @Test
    public void testGaussChargeTime() throws Exception {
        final BallisticWeapon isGauss = (BallisticWeapon) ItemDB.lookup(1021);
        final BallisticWeapon clanGauss = (BallisticWeapon) ItemDB.lookup(1208);

        assertNotEquals(0.0, isGauss.getChargeTime());
        assertNotEquals(0.0, clanGauss.getChargeTime());
    }

    @Test
    public void testRAC() {
        final Attribute aCooldown = new Attribute(1.0 / 7.275, Collections.EMPTY_SET);
        final Attribute aJammingChance = new Attribute(0.037, Collections.EMPTY_SET);
        final Attribute aJammingTime = new Attribute(10.0, Collections.EMPTY_SET);
        final double aJamRampUpTime = 8.0;
        final Attribute aJamRampDownTime = new Attribute(10.0, Collections.EMPTY_SET);
        final double aRampUpTime = 0.75;
        final double aRampDownTime = 2.0;
        final double aRampDownDelay = 0.3;
        final double aChargeTime = 0.0;
        final BallisticWeapon cut = new BallisticWeapon("name", "desc", "mwoname", 0, 1, 1.0, 10.0, Faction.INNERSPHERE, // Item
                new Attribute(1, Collections.EMPTY_SET), // Heat
                aCooldown, null, 1, 1, 1, new Attribute(1, Collections.EMPTY_SET), 0, 0,
                new Attribute(1, Collections.EMPTY_SET), 0, 0, // Weapon
                "ammo", // Ammo
                aJammingChance, aJammingTime, 1, aChargeTime, aRampUpTime, aRampDownTime, aRampDownDelay,
                aJamRampUpTime, aJamRampDownTime);

        // Compute the infinite sum (and probability sum to verify algorithm) to high precision to test the accuracy of
        // the approximations done in the implementation.
        final double p_jam = aJammingChance.value(null);
        final PriorityQueue<Double> sumComponentsP = new PriorityQueue<>();
        final PriorityQueue<Double> sumComponents = new PriorityQueue<>();
        for (int k = 0;; ++k) {
            final double p = Math.pow(1 - p_jam, k) * p_jam;
            sumComponentsP.add(p);
            sumComponents.add(k * p);
            if (p < Math.ulp(0.000001)) {
                break;
            }
        }
        while (sumComponents.size() > 1) {
            // Sum smallest to smallest first to get best precision
            sumComponentsP.add(sumComponentsP.remove() + sumComponentsP.remove());
            sumComponents.add(sumComponents.remove() + sumComponents.remove());
        }
        final double p_sum = sumComponentsP.remove();
        final double expectedShots = sumComponents.remove();
        assertEquals(1.0, p_sum, 1E-15); // Verifies the above probability expression is correct

        // Compute expected result
        final double expectedTimeUntilJam = expectedShots * aCooldown.value(null);
        final double period = aJamRampUpTime + expectedTimeUntilJam
                + Math.max(aRampDownDelay + aJamRampDownTime.value(null), aJammingTime.value(null));
        final double shotingTime = (aJamRampUpTime - aRampUpTime) + expectedTimeUntilJam;
        final double expected = aCooldown.value(null) * period / shotingTime;
        assertTrue(expected > aCooldown.value(null));
        assertEquals(expected, cut.getSecondsPerShot(null), 0.0000001);
    }

    @Test
    public void testCUAC10() throws Exception {
        final BallisticWeapon cut = (BallisticWeapon) ItemDB.lookup(1206);

        assertTrue(cut.getName().contains("C-ULTRA AC/10"));
        assertEquals(3, cut.getAmmoPerPerShot());
        assertEquals(10.0, cut.getDamagePerShot(), 0.0001);

        final double expectedSecondsPerShot = cut.getCoolDown(null) + 0.11 * 2;

        assertEquals(expectedSecondsPerShot, cut.getRawSecondsPerShot(null), 0.0);
    }

    @Test
    public void testJammingChanceQuirk() throws Exception {
        final BallisticWeapon cut = (BallisticWeapon) ItemDB.lookup(1206);

        final ModifierDescription modifierDescription = new ModifierDescription("", "key", Operation.MUL,
                Arrays.asList("ultraautocannon"), "jamchance", ModifierType.NEGATIVE_GOOD);
        final Modifier modifier = new Modifier(modifierDescription, -0.3);
        final List<Modifier> modifiers = Arrays.asList(modifier);

        final double unmodified = cut.getJamProbability(null);
        final double unmodifiedDps = cut.getStat("d/s", null);

        final double modified = cut.getJamProbability(modifiers);
        final double modifiedDps = cut.getStat("d/s", modifiers);

        assertEquals(unmodified * 0.7, modified, 0.0);
        assertTrue(unmodifiedDps * 1.05 < modifiedDps);
    }

    @Test
    public void testJamRampDownQuirk() throws Exception {
        final BallisticWeapon cut = (BallisticWeapon) ItemDB.lookup("ROTARY AC/2");

        final ModifierDescription modifierDescription = new ModifierDescription("", "key", Operation.MUL,
                Arrays.asList("rotaryautocannon"), "jamrampdownduration", ModifierType.NEGATIVE_GOOD);

        final Modifier modifier = new Modifier(modifierDescription, -0.3);
        final List<Modifier> modifiers = Arrays.asList(modifier);

        final double unmodified = cut.getJamRampDownTime(null);
        final double unmodifiedDps = cut.getStat("d/s", null);

        final double modified = cut.getJamRampDownTime(modifiers);
        final double modifiedDps = cut.getStat("d/s", modifiers);

        assertEquals(unmodified * 0.7, modified, 0.0);
        assertTrue(unmodifiedDps * 1.01 < modifiedDps);
    }

    @Test
    public void testJammingTimeQuirk() throws Exception {
        final BallisticWeapon cut = (BallisticWeapon) ItemDB.lookup(1206);

        final ModifierDescription modifierDescription = new ModifierDescription("", "key", Operation.MUL,
                Arrays.asList("ultraautocannon"), "jamtime", ModifierType.NEGATIVE_GOOD);

        final Modifier modifier = new Modifier(modifierDescription, -0.3);
        final List<Modifier> modifiers = Arrays.asList(modifier);

        final double unmodified = cut.getJamTime(null);
        final double unmodifiedDps = cut.getStat("d/s", null);

        final double modified = cut.getJamTime(modifiers);
        final double modifiedDps = cut.getStat("d/s", modifiers);

        assertEquals(unmodified * 0.7, modified, 0.0);
        assertTrue(unmodifiedDps * 1.01 < modifiedDps);
    }

    @Test
    public void testLB10X() throws Exception {
        final BallisticWeapon cut = (BallisticWeapon) ItemDB.lookup("LB 10-X AC");

        assertTrue(cut.getName().contains("LB 10-X AC"));
        assertEquals(1, cut.getAmmoPerPerShot());
        assertTrue(cut.getDamagePerShot() > 5.0);

        final double expectedSecondsPerShot = cut.getCoolDown(null);

        assertEquals(expectedSecondsPerShot, cut.getRawSecondsPerShot(null), 0.0);
    }

    @Test
    public void testLB10X_Damage() throws Exception {
        final BallisticWeapon cut = (BallisticWeapon) ItemDB.lookup(1023);

        assertTrue(cut.getName().contains("LB 10-X AC"));
        assertEquals(1, cut.getAmmoPerPerShot());
        assertEquals(10.0, cut.getDamagePerShot(), 0.0);
    }
}
