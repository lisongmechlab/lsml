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
package org.lisoft.lsml.command;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;

import org.junit.Test;
import org.lisoft.lsml.model.chassi.ArmourSide;
import org.lisoft.lsml.model.chassi.ChassisOmniMech;
import org.lisoft.lsml.model.chassi.ChassisStandard;
import org.lisoft.lsml.model.datacache.ChassisDB;
import org.lisoft.lsml.model.datacache.UpgradeDB;
import org.lisoft.lsml.model.loadout.DefaultLoadoutFactory;
import org.lisoft.lsml.model.loadout.LoadoutOmniMech;
import org.lisoft.lsml.model.loadout.LoadoutStandard;
import org.lisoft.lsml.model.loadout.component.ConfiguredComponent;
import org.lisoft.lsml.util.CommandStack;

public class CmdStripLoadoutTest {

    @Test
    public void testStripClanOmniMech() throws Exception {
        ChassisOmniMech chassis = (ChassisOmniMech) ChassisDB.lookup("TBR-PRIME");
        LoadoutOmniMech loadout = (LoadoutOmniMech) DefaultLoadoutFactory.instance.produceStock(chassis);
        LoadoutOmniMech original = (LoadoutOmniMech) DefaultLoadoutFactory.instance.produceClone(loadout);
        assertEquals(loadout, original);

        CmdStripLoadout cut = new CmdStripLoadout(null, loadout);

        CommandStack stack = new CommandStack(10);
        stack.pushAndApply(cut);

        assertSame(chassis.getFixedStructureType(), loadout.getUpgrades().getStructure());
        assertSame(chassis.getFixedArmourType(), loadout.getUpgrades().getArmour());
        assertSame(chassis.getFixedHeatSinkType(), loadout.getUpgrades().getHeatSink());
        assertSame(UpgradeDB.STD_GUIDANCE, loadout.getUpgrades().getGuidance());

        for (ConfiguredComponent component : loadout.getComponents()) {
            for (ArmourSide side : ArmourSide.allSides(component.getInternalComponent())) {
                assertEquals(0, component.getArmour(side));
            }
            assertTrue(component.getItemsEquipped().isEmpty());
        }
    }

    @Test
    public void testStripClanStandardMech() throws Exception {
        ChassisStandard chassis = (ChassisStandard) ChassisDB.lookup("JR7-IIC");
        LoadoutStandard loadout = (LoadoutStandard) DefaultLoadoutFactory.instance.produceStock(chassis);
        LoadoutStandard original = (LoadoutStandard) DefaultLoadoutFactory.instance.produceClone(loadout);
        assertEquals(loadout, original);

        CmdStripLoadout cut = new CmdStripLoadout(null, loadout);

        CommandStack stack = new CommandStack(10);
        stack.pushAndApply(cut);

        assertSame(UpgradeDB.CLAN_STD_STRUCTURE, loadout.getUpgrades().getStructure());
        assertSame(UpgradeDB.CLAN_STD_ARMOUR, loadout.getUpgrades().getArmour());
        assertSame(UpgradeDB.CLAN_SHS, loadout.getUpgrades().getHeatSink());
        assertSame(UpgradeDB.STD_GUIDANCE, loadout.getUpgrades().getGuidance());

        for (ConfiguredComponent component : loadout.getComponents()) {
            for (ArmourSide side : ArmourSide.allSides(component.getInternalComponent())) {
                assertEquals(0, component.getArmour(side));
            }
            assertTrue(component.getItemsEquipped().isEmpty());
        }
    }
}
