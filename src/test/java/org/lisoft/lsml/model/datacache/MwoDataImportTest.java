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
package org.lisoft.lsml.model.datacache;

import static org.junit.Assert.assertEquals;

import java.util.ArrayList;
import java.util.List;

import org.junit.Test;
import org.lisoft.lsml.model.chassi.ChassisStandard;
import org.lisoft.lsml.model.item.TargetingComputer;
import org.lisoft.lsml.model.item.Weapon;
import org.lisoft.lsml.model.item.WeaponModule;
import org.lisoft.lsml.model.modifiers.Modifier;
import org.lisoft.lsml.model.modifiers.ModifierDescription;
import org.lisoft.lsml.model.modifiers.ModifierDescription.ModifierType;

/**
 * This test suite tests that various parts of the data files are parsed in a correct manner. Mainly regression tests.
 *
 * @author Emily Björk
 */
public class MwoDataImportTest {

    @Test
    public void testBug478() {
        final TargetingComputer tc = (TargetingComputer) ItemDB.lookup("TARGETING COMP. MK VII");
        final Weapon ppc = (Weapon) ItemDB.lookup("C-ER PPC");

        final double raw = ppc.getProjectileSpeed(null);
        final double mod = ppc.getProjectileSpeed(tc.getModifiers());
        final double bonus = 0.35;
        final double expected = raw * (1 + bonus);
        assertEquals(expected, mod, 1E-15);
    }

    /**
     * The ROF quirk should apply correctly to machine guns.
     */
    @Test
    public void testBug566() {
        final ChassisStandard lct1v = (ChassisStandard) ChassisDB.lookup("LCT-1V");
        final Weapon mg = (Weapon) ItemDB.lookup("MACHINE GUN");

        final double cdRaw = mg.getCoolDown(null);
        final double cdModified = mg.getCoolDown(lct1v.getQuirks());

        // CD = 1/rof
        // CDquirk = 1 / (rof*(1+modifier)) = CD * 1/(1+modifier)
        // modifier = 20%

        assertEquals(cdRaw / 1.2, cdModified, 1E-15);
    }

    @Test
    public void testBug569() {
        final WeaponModule module = (WeaponModule) PilotModuleDB.lookup("CL. ER PPC COOLDOWN 5");
        final Weapon ppc = (Weapon) ItemDB.lookup("C-ER PPC");

        final List<Modifier> allModifiers = new ArrayList<>(module.getModifiers());
        assertEquals(1, allModifiers.size());
        assertEquals(ModifierType.NEGATIVE_GOOD, allModifiers.get(0).getDescription().getModifierType());

        final double raw = ppc.getCoolDown(null);
        final double mod = ppc.getCoolDown(module.getModifiers());
        final double bonus = 0.12;
        final double expected = raw * (1 - bonus);
        assertEquals(expected, mod, 1E-15);
    }

    /**
     * The cool down quirk needs to be changed to negative good.
     */
    @Test
    public void testCDNegativeGood() {
        final ChassisStandard lct1v = (ChassisStandard) ChassisDB.lookup("LCT-1V");
        final Weapon llas = (Weapon) ItemDB.lookup("LARGE LASER");

        int found = 0;
        for (final Modifier modifier : lct1v.getQuirks()) {
            final ModifierDescription description = modifier.getDescription();
            if (ModifierDescription.SPEC_WEAPON_COOL_DOWN.equals(description.getSpecifier())) {
                assertEquals(ModifierType.NEGATIVE_GOOD, description.getModifierType());
                found++;
            }
        }
        assertEquals(2, found);

        final double cdRaw = llas.getCoolDown(null);
        final double cdModified = llas.getCoolDown(lct1v.getQuirks());

        assertEquals(cdRaw * 0.5, cdModified, 0.0);
    }
}
