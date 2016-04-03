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

import org.junit.Test;
import org.lisoft.lsml.model.datacache.ItemDB;
import org.lisoft.lsml.model.modifiers.Modifier;
import org.lisoft.lsml.model.modifiers.ModifierDescription;
import org.lisoft.lsml.model.modifiers.ModifierDescription.ModifierType;
import org.lisoft.lsml.model.modifiers.ModifierDescription.Operation;

/**
 * Test suite for {@link AmmoWeapon}.
 * 
 * @author Li Song
 */
public class AmmoWeaponTest {

    @Test
    public final void testIsCompatibleAmmo() throws Exception {
        BallisticWeapon ac20 = (BallisticWeapon) ItemDB.lookup("AC/20");

        Ammunition ac20ammo = (Ammunition) ItemDB.lookup("AC/20 AMMO");
        Ammunition ac20ammoHalf = (Ammunition) ItemDB.lookup("AC/20 AMMO (1/2)");

        assertTrue(ac20.isCompatibleAmmo(ac20ammoHalf));
        assertTrue(ac20.isCompatibleAmmo(ac20ammo));
    }

    @Test
    public final void testSpreadQuirks() {
        ModifierDescription quirkDescription = new ModifierDescription(null, null, Operation.MUL,
                ModifierDescription.SEL_ALL_WEAPONS, ModifierDescription.SPEC_WEAPON_SPREAD,
                ModifierType.POSITIVE_GOOD);
        Modifier modifier = new Modifier(quirkDescription, 1.0);

        AmmoWeapon cut = (AmmoWeapon) ItemDB.lookup("SRM6");

        double normal = cut.getSpread(null);
        double quirked = cut.getSpread(Arrays.asList(modifier));

        assertEquals(normal * 2, quirked, 0.0);
    }

}
