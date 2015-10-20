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
package org.lisoft.lsml.model.loadout;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;

import java.util.ArrayList;
import java.util.List;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.lisoft.lsml.messages.MessageXBar;
import org.lisoft.lsml.model.chassi.ArmorSide;
import org.lisoft.lsml.model.chassi.ChassisBase;
import org.lisoft.lsml.model.chassi.ChassisClass;
import org.lisoft.lsml.model.chassi.ChassisStandard;
import org.lisoft.lsml.model.chassi.Location;
import org.lisoft.lsml.model.datacache.ChassisDB;
import org.lisoft.lsml.model.datacache.ItemDB;
import org.lisoft.lsml.model.datacache.UpgradeDB;
import org.lisoft.lsml.model.item.Item;
import org.lisoft.lsml.model.loadout.component.ConfiguredComponentBase;
import org.lisoft.lsml.model.modifiers.Efficiencies;
import org.lisoft.lsml.util.CommandStack;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import com.thoughtworks.xstream.XStream;

import junitparams.JUnitParamsRunner;

/**
 * Tests for verifying save-load behaviour of Loadouts.
 * 
 * @author Emily Björk
 */
@RunWith(JUnitParamsRunner.class)
public class LoadoutSerializationTest {
    @Mock
    MessageXBar    xBar;
    @Mock
    CommandStack undoStack;

    @Before
    public void setup() {
        MockitoAnnotations.initMocks(this);
    }

    @SuppressWarnings("unused")
    private Object[] allChassis() {
        List<ChassisBase> chassis = new ArrayList<>(ChassisDB.lookup(ChassisClass.LIGHT));
        chassis.addAll(ChassisDB.lookup(ChassisClass.MEDIUM));
        chassis.addAll(ChassisDB.lookup(ChassisClass.HEAVY));
        chassis.addAll(ChassisDB.lookup(ChassisClass.ASSAULT));
        return chassis.toArray();
    }

    /**
     * We can make empty loadouts.
     */
    @Test
    public void testEmptyLoadout() {
        ChassisBase chassis = ChassisDB.lookup("CPLT-K2");
        LoadoutBase<?> cut = DefaultLoadoutFactory.instance.produceEmpty(chassis);

        assertEquals(0, cut.getArmor());
        assertSame(chassis, cut.getChassis());
        assertNull(cut.getEngine());
        assertEquals(0, cut.getHeatsinksCount());
        assertEquals(chassis.getNameShort(), cut.getName());
        assertEquals(21, cut.getNumCriticalSlotsUsed()); // 21 for empty K2
        assertEquals(57, cut.getNumCriticalSlotsFree()); // 57 for empty K2
        assertEquals(8, cut.getComponents().size());

        Efficiencies efficiencies = cut.getEfficiencies();
        assertFalse(efficiencies.hasCoolRun());
        assertFalse(efficiencies.hasDoubleBasics());
        assertFalse(efficiencies.hasHeatContainment());
        assertFalse(efficiencies.hasSpeedTweak());

        assertEquals(UpgradeDB.STANDARD_GUIDANCE, cut.getUpgrades().getGuidance());
        assertEquals(UpgradeDB.STANDARD_STRUCTURE, cut.getUpgrades().getStructure());
        assertEquals(UpgradeDB.STANDARD_ARMOR, cut.getUpgrades().getArmor());
        assertEquals(UpgradeDB.STANDARD_HEATSINKS, cut.getUpgrades().getHeatSink());
    }

    /**
     * We can load stock loadouts. (AS7-D)
     * 
     * @throws Exception
     */
    @Test
    public void testStockLoadoutAS7D() throws Exception {
        ChassisStandard chassi = (ChassisStandard) ChassisDB.lookup("AS7-D");
        LoadoutBase<?> cut = DefaultLoadoutFactory.instance.produceStock(chassi);

        assertEquals(608, cut.getArmor());
        assertSame(chassi, cut.getChassis());
        assertSame(ItemDB.lookup("STD ENGINE 300"), cut.getEngine());
        assertEquals(20, cut.getHeatsinksCount());
        assertEquals(100.0, cut.getMass(), 0.0);
        assertEquals(chassi.getNameShort(), cut.getName());
        assertEquals(5 * 12 + 3 * 6 - 13, cut.getNumCriticalSlotsUsed()); // 21 for empty K2
        assertEquals(13, cut.getNumCriticalSlotsFree()); // 13 for stock AS7-D
        assertEquals(8, cut.getComponents().size());

        assertEquals(UpgradeDB.STANDARD_GUIDANCE, cut.getUpgrades().getGuidance());
        assertEquals(UpgradeDB.STANDARD_STRUCTURE, cut.getUpgrades().getStructure());
        assertEquals(UpgradeDB.STANDARD_ARMOR, cut.getUpgrades().getArmor());
        assertEquals(UpgradeDB.STANDARD_HEATSINKS, cut.getUpgrades().getHeatSink());

        // Right leg:
        {
            ConfiguredComponentBase part = cut.getComponent(Location.RightLeg);
            List<Item> items = new ArrayList<Item>(part.getItemsEquipped());

            assertTrue(items.remove(ItemDB.SHS));
            assertTrue(items.remove(ItemDB.SHS));
            assertEquals(82, part.getArmor(ArmorSide.ONLY));
            assertTrue(items.isEmpty());

            assertEquals(4, part.getItemsFixed().size());
        }

        // Left leg:
        {
            ConfiguredComponentBase part = cut.getComponent(Location.LeftLeg);
            List<Item> items = new ArrayList<Item>(part.getItemsEquipped());

            assertTrue(items.remove(ItemDB.SHS));
            assertTrue(items.remove(ItemDB.SHS));
            assertEquals(82, part.getArmor(ArmorSide.ONLY));
            assertTrue(items.isEmpty());

            assertEquals(4, part.getItemsFixed().size());
        }
        // Right arm:
        {
            ConfiguredComponentBase part = cut.getComponent(Location.RightArm);
            List<Item> items = new ArrayList<Item>(part.getItemsEquipped());

            assertTrue(items.remove(ItemDB.SHS));
            assertTrue(items.remove(ItemDB.lookup("MEDIUM LASER")));
            assertEquals(68, part.getArmor(ArmorSide.ONLY));
            assertTrue(items.isEmpty());

            assertEquals(4, part.getItemsFixed().size());
        }

        // Left arm:
        {
            ConfiguredComponentBase part = cut.getComponent(Location.LeftArm);
            List<Item> items = new ArrayList<Item>(part.getItemsEquipped());

            assertTrue(items.remove(ItemDB.SHS));
            assertTrue(items.remove(ItemDB.lookup("MEDIUM LASER")));
            assertEquals(68, part.getArmor(ArmorSide.ONLY));
            assertTrue(items.isEmpty());

            assertEquals(4, part.getItemsFixed().size());
        }

        // Right torso:
        {
            ConfiguredComponentBase part = cut.getComponent(Location.RightTorso);
            List<Item> items = new ArrayList<Item>(part.getItemsEquipped());

            assertTrue(items.remove(ItemDB.lookup("AC/20")));
            assertTrue(items.remove(ItemDB.lookup("AC/20 AMMO")));
            assertTrue(items.remove(ItemDB.lookup("AC/20 AMMO")));
            assertEquals(64, part.getArmor(ArmorSide.FRONT));
            assertEquals(20, part.getArmor(ArmorSide.BACK));
            assertTrue(items.isEmpty());

            assertEquals(0, part.getItemsFixed().size());
        }

        // Left torso:
        {
            ConfiguredComponentBase part = cut.getComponent(Location.LeftTorso);
            List<Item> items = new ArrayList<Item>(part.getItemsEquipped());

            assertTrue(items.remove(ItemDB.lookup("LRM 20")));
            assertTrue(items.remove(ItemDB.lookup("SRM6")));
            assertTrue(items.remove(ItemDB.lookup("LRM AMMO")));
            assertTrue(items.remove(ItemDB.lookup("LRM AMMO")));
            assertTrue(items.remove(ItemDB.lookup("SRM AMMO")));
            assertTrue(items.remove(ItemDB.SHS));
            assertEquals(64, part.getArmor(ArmorSide.FRONT));
            assertEquals(20, part.getArmor(ArmorSide.BACK));
            assertTrue(items.isEmpty());

            assertEquals(0, part.getItemsFixed().size());
        }

        // Center torso:
        {
            ConfiguredComponentBase part = cut.getComponent(Location.CenterTorso);
            List<Item> items = new ArrayList<Item>(part.getItemsEquipped());

            assertTrue(items.remove(ItemDB.lookup("STD ENGINE 300")));
            assertTrue(items.remove(ItemDB.lookup("MEDIUM LASER")));
            assertTrue(items.remove(ItemDB.lookup("MEDIUM LASER")));
            assertTrue(items.remove(ItemDB.SHS));
            assertTrue(items.remove(ItemDB.SHS));
            assertEquals(94, part.getArmor(ArmorSide.FRONT));
            assertEquals(28, part.getArmor(ArmorSide.BACK));

            assertEquals(2, part.getEngineHeatsinks());
            assertTrue(items.isEmpty());

            assertEquals(1, part.getItemsFixed().size());
        }

        // Head:
        {
            ConfiguredComponentBase part = cut.getComponent(Location.Head);
            List<Item> items = new ArrayList<Item>(part.getItemsEquipped());

            assertTrue(items.remove(ItemDB.SHS));
            assertEquals(18, part.getArmor(ArmorSide.ONLY));
            assertTrue(items.isEmpty());

            assertEquals(3, part.getItemsFixed().size());
        }

        Efficiencies efficiencies = cut.getEfficiencies();
        assertFalse(efficiencies.hasCoolRun());
        assertFalse(efficiencies.hasDoubleBasics());
        assertFalse(efficiencies.hasHeatContainment());
        assertFalse(efficiencies.hasSpeedTweak());
    }

    /**
     * We can load stock loadouts. (SDR-5V)
     * 
     * @throws Exception
     */
    @Test
    public void testStockLoadoutSDR5V() throws Exception {
        ChassisStandard chassis = (ChassisStandard) ChassisDB.lookup("SDR-5V");
        LoadoutBase<?> cut = DefaultLoadoutFactory.instance.produceStock(chassis);

        assertEquals(112, cut.getArmor());
        assertSame(chassis, cut.getChassis());
        assertSame(ItemDB.lookup("STD ENGINE 240"), cut.getEngine());
        assertEquals(10, cut.getHeatsinksCount());
        assertEquals(30.0, cut.getMass(), 0.0);
        assertEquals(chassis.getNameShort(), cut.getName());
        assertEquals(5 * 12 + 3 * 6 - 36, cut.getNumCriticalSlotsUsed());
        assertEquals(36, cut.getNumCriticalSlotsFree());
        assertEquals(8, cut.getComponents().size());

        assertEquals(UpgradeDB.STANDARD_GUIDANCE, cut.getUpgrades().getGuidance());
        assertEquals(UpgradeDB.STANDARD_STRUCTURE, cut.getUpgrades().getStructure());
        assertEquals(UpgradeDB.STANDARD_ARMOR, cut.getUpgrades().getArmor());
        assertEquals(UpgradeDB.STANDARD_HEATSINKS, cut.getUpgrades().getHeatSink());

        // Right leg:
        {
            ConfiguredComponentBase part = cut.getComponent(Location.RightLeg);
            List<Item> items = new ArrayList<Item>(part.getItemsEquipped());

            assertEquals(12, part.getArmor(ArmorSide.ONLY));
            assertTrue(items.isEmpty());

            assertEquals(4, part.getItemsFixed().size());
        }

        // Left leg:
        {
            ConfiguredComponentBase part = cut.getComponent(Location.LeftLeg);
            List<Item> items = new ArrayList<Item>(part.getItemsEquipped());

            assertEquals(12, part.getArmor(ArmorSide.ONLY));
            assertTrue(items.isEmpty());

            assertEquals(4, part.getItemsFixed().size());
        }

        // Right arm:
        {
            ConfiguredComponentBase part = cut.getComponent(Location.RightArm);
            List<Item> items = new ArrayList<Item>(part.getItemsEquipped());

            assertEquals(10, part.getArmor(ArmorSide.ONLY));
            assertTrue(items.isEmpty());

            assertEquals(4, part.getItemsFixed().size());
        }

        // Left arm:
        {
            ConfiguredComponentBase part = cut.getComponent(Location.LeftArm);
            List<Item> items = new ArrayList<Item>(part.getItemsEquipped());

            assertEquals(10, part.getArmor(ArmorSide.ONLY));
            assertTrue(items.isEmpty());

            assertEquals(4, part.getItemsFixed().size());
        }

        // Right torso:
        {
            ConfiguredComponentBase part = cut.getComponent(Location.RightTorso);
            List<Item> items = new ArrayList<Item>(part.getItemsEquipped());

            assertTrue(items.remove(ItemDB.lookup("JUMP JETS - CLASS V")));
            assertTrue(items.remove(ItemDB.lookup("JUMP JETS - CLASS V")));
            assertTrue(items.remove(ItemDB.lookup("JUMP JETS - CLASS V")));
            assertTrue(items.remove(ItemDB.lookup("JUMP JETS - CLASS V")));
            assertEquals(12, part.getArmor(ArmorSide.FRONT));
            assertEquals(4, part.getArmor(ArmorSide.BACK));
            assertTrue(items.isEmpty());

            assertEquals(0, part.getItemsFixed().size());
        }

        // Left torso:
        {
            ConfiguredComponentBase part = cut.getComponent(Location.LeftTorso);
            List<Item> items = new ArrayList<Item>(part.getItemsEquipped());

            assertTrue(items.remove(ItemDB.lookup("JUMP JETS - CLASS V")));
            assertTrue(items.remove(ItemDB.lookup("JUMP JETS - CLASS V")));
            assertTrue(items.remove(ItemDB.lookup("JUMP JETS - CLASS V")));
            assertTrue(items.remove(ItemDB.lookup("JUMP JETS - CLASS V")));
            assertEquals(12, part.getArmor(ArmorSide.FRONT));
            assertEquals(4, part.getArmor(ArmorSide.BACK));
            assertTrue(items.isEmpty());

            assertEquals(0, part.getItemsFixed().size());
        }

        // Center torso:
        {
            ConfiguredComponentBase part = cut.getComponent(Location.CenterTorso);
            List<Item> items = new ArrayList<Item>(part.getItemsEquipped());

            assertTrue(items.remove(ItemDB.lookup("STD ENGINE 240")));
            assertTrue(items.remove(ItemDB.lookup("MEDIUM LASER")));
            assertTrue(items.remove(ItemDB.lookup("MEDIUM LASER")));
            assertEquals(16, part.getArmor(ArmorSide.FRONT));
            assertEquals(8, part.getArmor(ArmorSide.BACK));

            assertEquals(0, part.getEngineHeatsinks());
            assertTrue(items.isEmpty());

            assertEquals(1, part.getItemsFixed().size());
        }

        // Head:
        {
            ConfiguredComponentBase part = cut.getComponent(Location.Head);
            List<Item> items = new ArrayList<Item>(part.getItemsEquipped());

            assertTrue(items.remove(ItemDB.SHS));
            assertEquals(12, part.getArmor(ArmorSide.ONLY));
            assertTrue(items.isEmpty());

            assertEquals(3, part.getItemsFixed().size());
        }

        Efficiencies efficiencies = cut.getEfficiencies();
        assertFalse(efficiencies.hasCoolRun());
        assertFalse(efficiencies.hasDoubleBasics());
        assertFalse(efficiencies.hasHeatContainment());
        assertFalse(efficiencies.hasSpeedTweak());
    }

    @Test
    public void testGetJumpJetCount_noJJCapability() throws Exception {
        LoadoutBase<?> cut = DefaultLoadoutFactory.instance.produceStock(ChassisDB.lookup("HBK-4J"));
        assertEquals(0, cut.getJumpJetCount());
    }

    // TODO: Move these to statistics test!
    /*
     * @Test public void testHeatSHS_SmallEngine(){ Loadout cut = new Loadout(ChassiDB.lookup("ATLAS", "as7-k"));
     * cut.getInternalPartLoadout(InternalPartType.CenterTorso).addItem(ItemDB.lookup("STD ENGINE 210"));
     * cut.getInternalPartLoadout(InternalPartType.CenterTorso).addItem(ItemDB.SHS); assertEquals(9,
     * cut.getHeatsinksCount()); assertEquals(9.0, cut.getHeatCapacity(), 0.0); assertEquals(0.9,
     * cut.getHeatDissapation(), 0.0); cut.getEfficiencies().setCoolRun(true); assertEquals(9, cut.getHeatsinksCount());
     * assertEquals(9.0, cut.getHeatCapacity(), 0.0); assertEquals(0.9 * 1.075, cut.getHeatDissapation(), 0.0);
     * cut.getEfficiencies().setHeatContainment(true); cut.getEfficiencies().setCoolRun(false); assertEquals(9,
     * cut.getHeatsinksCount()); assertEquals(9.0 * 1.075, cut.getHeatCapacity(), 0.0); assertEquals(0.9,
     * cut.getHeatDissapation(), 0.0); }
     * 
     * @Test public void testHeatDHS_SmallEngine(){ Loadout cut = new Loadout(ChassiDB.lookup("ATLAS", "as7-k"));
     * cut.getUpgrades().setDoubleHeatSinks(true);
     * cut.getInternalPartLoadout(InternalPartType.CenterTorso).addItem(ItemDB.lookup("STD ENGINE 210")); // 8 internal
     * cut.getInternalPartLoadout(InternalPartType.LeftArm).addItem(ItemDB.DHS); assertEquals(9,
     * cut.getHeatsinksCount()); assertEquals(8 * 2 + 1.4 * 1, cut.getHeatCapacity(), 0.0); assertEquals((8 * 2 + 1.4 *
     * 1) / 10.0, cut.getHeatDissapation(), 0.0); cut.getEfficiencies().setCoolRun(true); assertEquals(9,
     * cut.getHeatsinksCount()); assertEquals((8 * 2 + 1.4 * 1), cut.getHeatCapacity(), 0.0); assertEquals((8 * 2 + 1.4
     * * 1) / 10.0 * 1.075, cut.getHeatDissapation(), 0.0); cut.getEfficiencies().setHeatContainment(true);
     * cut.getEfficiencies().setCoolRun(false); assertEquals(9, cut.getHeatsinksCount()); assertEquals((8 * 2 + 1.4 * 1)
     * * 1.075, cut.getHeatCapacity(), 0.0); assertEquals((8 * 2 + 1.4 * 1) / 10.0, cut.getHeatDissapation(), 0.0); }
     * 
     * @Test public void testHeatSHS_BigEngine(){ Loadout cut = new Loadout(ChassiDB.lookup("ATLAS", "as7-k"));
     * cut.getInternalPartLoadout(InternalPartType.CenterTorso).addItem(ItemDB.lookup("STD ENGINE 355"));
     * cut.getInternalPartLoadout(InternalPartType.CenterTorso).addItem(ItemDB.SHS);
     * cut.getInternalPartLoadout(InternalPartType.CenterTorso).addItem(ItemDB.SHS);
     * cut.getInternalPartLoadout(InternalPartType.CenterTorso).addItem(ItemDB.SHS);
     * cut.getInternalPartLoadout(InternalPartType.CenterTorso).addItem(ItemDB.SHS);
     * cut.getInternalPartLoadout(InternalPartType.CenterTorso).addItem(ItemDB.SHS);
     * cut.getInternalPartLoadout(InternalPartType.CenterTorso).addItem(ItemDB.SHS); assertEquals(16,
     * cut.getHeatsinksCount()); assertEquals(16.0, cut.getHeatCapacity(), 0.0); assertEquals(1.6,
     * cut.getHeatDissapation(), 0.0); cut.getEfficiencies().setCoolRun(true); assertEquals(16,
     * cut.getHeatsinksCount()); assertEquals(16.0, cut.getHeatCapacity(), 0.0); assertEquals(1.6 * 1.075,
     * cut.getHeatDissapation(), 0.0); cut.getEfficiencies().setHeatContainment(true);
     * cut.getEfficiencies().setCoolRun(false); assertEquals(16, cut.getHeatsinksCount()); assertEquals(16.0 * 1.075,
     * cut.getHeatCapacity(), 0.0); assertEquals(1.6, cut.getHeatDissapation(), 0.0); }
     * 
     * @Test public void testHeatDHS_BigEngine(){ Loadout cut = new Loadout(ChassiDB.lookup("ATLAS", "as7-k"));
     * cut.getUpgrades().setDoubleHeatSinks(true);
     * cut.getInternalPartLoadout(InternalPartType.CenterTorso).addItem(ItemDB.lookup("STD ENGINE 355"));
     * cut.getInternalPartLoadout(InternalPartType.CenterTorso).addItem(ItemDB.DHS);
     * cut.getInternalPartLoadout(InternalPartType.CenterTorso).addItem(ItemDB.DHS);
     * cut.getInternalPartLoadout(InternalPartType.CenterTorso).addItem(ItemDB.DHS);
     * cut.getInternalPartLoadout(InternalPartType.CenterTorso).addItem(ItemDB.DHS);
     * cut.getInternalPartLoadout(InternalPartType.LeftTorso).addItem(ItemDB.DHS);
     * cut.getInternalPartLoadout(InternalPartType.LeftTorso).addItem(ItemDB.DHS); assertEquals(16,
     * cut.getHeatsinksCount()); assertEquals((2 * 10 + 1.4 * 6), cut.getHeatCapacity(), 0.0); assertEquals((2 * 10 +
     * 1.4 * 6) / 10.0, cut.getHeatDissapation(), 0.0); cut.getEfficiencies().setCoolRun(true); assertEquals(16,
     * cut.getHeatsinksCount()); assertEquals((2 * 10 + 1.4 * 6), cut.getHeatCapacity(), 0.0); assertEquals((2 * 10 +
     * 1.4 * 6) / 10.0 * 1.075, cut.getHeatDissapation(), 0.0); cut.getEfficiencies().setHeatContainment(true);
     * cut.getEfficiencies().setCoolRun(false); assertEquals(16, cut.getHeatsinksCount()); assertEquals((2 * 10 + 1.4 *
     * 6) * 1.075, cut.getHeatCapacity(), 0.0); assertEquals((2 * 10 + 1.4 * 6) / 10.0, cut.getHeatDissapation(), 0.0);
     * }
     */

    /**
     * Even if DHS are serialized before the Engine, they should be added as engine heat sinks.
     */
    @Test
    public void testUnMarshalDhsBeforeEngine() {
        String xml = "<?xml version=\"1.0\" ?><loadout name=\"AS7-BH\" chassi=\"AS7-BH\"><upgrades version=\"2\"><armor>2810</armor><structure>3100</structure><guidance>3051</guidance><heatsinks>3002</heatsinks></upgrades><efficiencies><speedTweak>false</speedTweak><coolRun>false</coolRun><heatContainment>false</heatContainment><anchorTurn>false</anchorTurn><doubleBasics>false</doubleBasics><fastfire>false</fastfire></efficiencies><component part=\"Head\" armor=\"0\" /><component part=\"LeftArm\" armor=\"0\" /><component part=\"LeftLeg\" armor=\"0\" /><component part=\"LeftTorso\" armor=\"0/0\" /><component part=\"CenterTorso\" armor=\"0/0\"><item>3001</item><item>3001</item><item>3001</item><item>3001</item><item>3001</item><item>3001</item><item>3278</item></component><component part=\"RightTorso\" armor=\"0/0\" /><component part=\"RightLeg\" armor=\"0\" /><component part=\"RightArm\" armor=\"0\" /></loadout>";

        XStream stream = LoadoutBase.loadoutXstream();
        LoadoutStandard loadout = (LoadoutStandard) stream.fromXML(xml);

        assertEquals(6, loadout.getComponent(Location.CenterTorso).getEngineHeatsinks());
    }
}
