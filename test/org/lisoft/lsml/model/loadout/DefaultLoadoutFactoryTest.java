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
package org.lisoft.lsml.model.loadout;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotSame;
import static org.junit.Assert.assertTrue;

import java.util.ArrayList;
import java.util.List;

import org.junit.Test;
import org.lisoft.lsml.model.chassi.ChassisDB;
import org.lisoft.lsml.model.chassi.Location;
import org.lisoft.lsml.model.chassi.OmniPod;
import org.lisoft.lsml.model.chassi.OmniPodDB;
import org.lisoft.lsml.model.item.ItemDB;
import org.lisoft.lsml.model.item.PilotModuleDB;
import org.lisoft.lsml.model.upgrades.UpgradeDB;

/**
 * Test the default factory for creating loadouts.
 * 
 * @author Li Song
 */
public class DefaultLoadoutFactoryTest {

    DefaultLoadoutFactory cut = new DefaultLoadoutFactory();

    @Test
    public void testProduceClone_NotSame() {
        LoadoutBase<?> loadout = cut.produceStock(ChassisDB.lookup("AS7-D-DC"));
        assertTrue(loadout.getMass() > 99.7); // Verify that a stock build was loaded

        LoadoutBase<?> clone = cut.produceClone(loadout);

        assertNotSame(loadout, clone);
    }

    @Test
    public void testProduceClone_ItemsAndArmor() {
        LoadoutBase<?> loadout = cut.produceStock(ChassisDB.lookup("AS7-D-DC"));
        assertTrue(loadout.getMass() > 99.7); // Verify that a stock build was loaded

        LoadoutBase<?> clone = cut.produceClone(loadout);

        assertEquals(loadout, clone);
    }

    @Test
    public void testProduceClone_OmniPods() {
        LoadoutOmniMech loadout = (LoadoutOmniMech) cut.produceEmpty(ChassisDB.lookup("TBR-PRIME"));

        int podId = 0;
        for (Location loc : Location.values()) {
            if (loc == Location.CenterTorso)
                continue;

            List<OmniPod> possiblePods = new ArrayList<>(OmniPodDB.lookup(loadout.getChassis(), loc));
            OmniPod newPod = possiblePods.get(podId % possiblePods.size());
            podId++;
            loadout.getComponent(loc).setOmniPod(newPod);
        }

        LoadoutBase<?> clone = cut.produceClone(loadout);

        assertEquals(loadout, clone);
    }

    @Test
    public void testProduceClone_Actuators() {
        LoadoutOmniMech loadout = (LoadoutOmniMech) cut.produceEmpty(ChassisDB.lookup("SCR-PRIME"));
        loadout.getComponent(Location.LeftArm).setToggleState(ItemDB.HA, false);
        loadout.getComponent(Location.LeftArm).setToggleState(ItemDB.LAA, false);
        loadout.getComponent(Location.RightArm).setToggleState(ItemDB.LAA, true);
        loadout.getComponent(Location.RightArm).setToggleState(ItemDB.HA, true);

        LoadoutBase<?> clone = cut.produceClone(loadout);

        assertEquals(loadout, clone);
    }

    @Test
    public void testProduceClone_Efficiencies() {
        LoadoutBase<?> loadout = cut.produceEmpty(ChassisDB.lookup("AS7-D-DC"));
        loadout.getEfficiencies().setAnchorTurn(true, null);
        loadout.getEfficiencies().setTwistSpeed(true, null);
        loadout.getEfficiencies().setTwistX(true, null);
        loadout.getEfficiencies().setArmReflex(true, null);
        loadout.getEfficiencies().setFastFire(true, null);
        loadout.getEfficiencies().setCoolRun(true, null);
        loadout.getEfficiencies().setHeatContainment(true, null);
        loadout.getEfficiencies().setDoubleBasics(true, null);

        LoadoutBase<?> clone = cut.produceClone(loadout);

        assertEquals(loadout, clone);
    }

    @Test
    public void testProduceClone_Upgrades() {
        LoadoutStandard loadout = (LoadoutStandard) cut.produceEmpty(ChassisDB.lookup("AS7-D-DC"));
        loadout.getUpgrades().setHeatSink(UpgradeDB.DOUBLE_HEATSINKS);
        loadout.getUpgrades().setArmor(UpgradeDB.FERRO_FIBROUS_ARMOR);
        loadout.getUpgrades().setStructure(UpgradeDB.ENDO_STEEL_STRUCTURE);
        loadout.getUpgrades().setGuidance(UpgradeDB.ARTEMIS_IV);

        LoadoutBase<?> clone = cut.produceClone(loadout);

        assertEquals(loadout, clone);
    }

    @Test
    public void testProduceClone_Name() {
        LoadoutStandard loadout = (LoadoutStandard) cut.produceEmpty(ChassisDB.lookup("AS7-D-DC"));
        loadout.rename("NewName");

        LoadoutBase<?> clone = cut.produceClone(loadout);

        assertEquals(loadout, clone);
    }

    @Test
    public void testProduceClone_Modules() {
        LoadoutStandard loadout = (LoadoutStandard) cut.produceEmpty(ChassisDB.lookup("AS7-D-DC"));
        loadout.addModule(PilotModuleDB.lookup("COOL SHOT 9 BY 9"));
        loadout.addModule(PilotModuleDB.lookup("ADVANCED UAV"));
        loadout.addModule(PilotModuleDB.lookup("SRM 6 COOLDOWN 5"));
        loadout.addModule(PilotModuleDB.lookup("MEDIUM LASER RANGE 5"));
        loadout.addModule(PilotModuleDB.lookup("HILL CLIMB"));

        LoadoutBase<?> clone = cut.produceClone(loadout);

        assertEquals(loadout, clone);
    }
}
