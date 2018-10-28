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
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.util.Arrays;

import org.junit.Test;
import org.lisoft.lsml.model.chassi.HardPointType;
import org.lisoft.lsml.model.database.ItemDB;
import org.lisoft.lsml.model.modifiers.Modifier;
import org.lisoft.lsml.model.modifiers.ModifierDescription;
import org.lisoft.lsml.model.modifiers.ModifierType;
import org.lisoft.lsml.model.modifiers.Operation;

/**
 * Test suite for {@link AmmoWeapon}.
 *
 * @author Li Song
 */
public class AmmoWeaponTest {

    @Test
    public final void testIsCompatibleAmmo() throws Exception {
        final BallisticWeapon ac20 = (BallisticWeapon) ItemDB.lookup("AC/20");

        final Ammunition ac20ammo = (Ammunition) ItemDB.lookup("AC/20 AMMO");
        final Ammunition ac20ammoHalf = (Ammunition) ItemDB.lookup("AC/20 AMMO (1/2)");

        assertTrue(ac20.isCompatibleAmmo(ac20ammoHalf));
        assertTrue(ac20.isCompatibleAmmo(ac20ammo));
    }

    @Test
    public final void testIsCompatibleAmmoBuiltinAmmo() throws Exception {

        final AmmoWeapon builtInAmmo = new AmmoWeapon("", "", "", 0, 0, 0.0, HardPointType.ENERGY, 0, Faction.CLAN,
                null, null, null, 1, 1, 1, null, 0, 0.0, null, 0.0, 0.0, null);
        final Ammunition ac20ammo = new Ammunition("", "", "", 0, 0, 0.0, HardPointType.NONE, 0.0, Faction.CLAN, 10,
                "ammotype", 0.0);

        assertFalse(builtInAmmo.isCompatibleAmmo(ac20ammo));
    }

    @Test
    public final void testSpreadQuirks() throws Exception {
        final ModifierDescription quirkDescription = new ModifierDescription(null, "key", Operation.MUL,
                ModifierDescription.SEL_ALL_WEAPONS, ModifierDescription.SPEC_WEAPON_SPREAD,
                ModifierType.POSITIVE_GOOD);
        final Modifier modifier = new Modifier(quirkDescription, 1.0);

        final AmmoWeapon cut = (AmmoWeapon) ItemDB.lookup("SRM6");

        final double normal = cut.getRangeProfile().getSpread().value(null);
        final double quirked = cut.getRangeProfile().getSpread().value(Arrays.asList(modifier));

        assertEquals(normal * 2, quirked, 0.0);
    }
}
