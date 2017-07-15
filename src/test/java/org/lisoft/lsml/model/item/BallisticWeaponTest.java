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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.util.Arrays;
import java.util.List;

import org.junit.Test;
import org.lisoft.lsml.model.database.ItemDB;
import org.lisoft.lsml.model.modifiers.Modifier;
import org.lisoft.lsml.model.modifiers.ModifierDescription;
import org.lisoft.lsml.model.modifiers.ModifierType;
import org.lisoft.lsml.model.modifiers.Operation;

/**
 * Test suite for ballistic weapons.
 *
 * @author Li Song
 */
public class BallisticWeaponTest {

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
