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

import static org.junit.Assert.assertSame;

import org.junit.Test;

public class ModuleCathegoryTest {
    private static final String NON_EXISTENT = "Foobar";

    @Test
    public void testFromMwo() {
        assertSame(ModuleCathegory.UNKOWN, ModuleCathegory.fromMwo(null));
        assertSame(ModuleCathegory.SUPPORT, ModuleCathegory.fromMwo("ePTModule_Support"));
        assertSame(ModuleCathegory.VISION, ModuleCathegory.fromMwo("ePTModule_Vision"));
        assertSame(ModuleCathegory.SENSOR, ModuleCathegory.fromMwo("ePTModule_Sensor"));
        assertSame(ModuleCathegory.TARGETING, ModuleCathegory.fromMwo("ePTModule_Target"));
        assertSame(ModuleCathegory.CONSUMABLE, ModuleCathegory.fromMwo("ePTModule_Consumable"));
        assertSame(ModuleCathegory.WEAPON_MODULE, ModuleCathegory.fromMwo("ePTModule_WeaponMod"));
        assertSame(ModuleCathegory.WEAPON_RANGE, ModuleCathegory.fromMwo("ePTModule_Range"));
        assertSame(ModuleCathegory.MISCELLANEOUS, ModuleCathegory.fromMwo("ePTModule_Misc"));
        assertSame(ModuleCathegory.WEAPON_COOLDOWN, ModuleCathegory.fromMwo("ePTModule_Cooldown"));
    }

    @Test(expected = IllegalArgumentException.class)
    public void testFromMwoRubbish() {
        assertSame(ModuleCathegory.UNKOWN, ModuleCathegory.fromMwo(NON_EXISTENT));
    }
}
