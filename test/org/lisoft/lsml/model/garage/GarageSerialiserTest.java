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
package org.lisoft.lsml.model.garage;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;
import static org.mockito.Mockito.mock;

import java.io.BufferedInputStream;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;

import org.junit.Test;
import org.lisoft.lsml.model.chassi.Chassis;
import org.lisoft.lsml.model.chassi.ChassisClass;
import org.lisoft.lsml.model.chassi.Location;
import org.lisoft.lsml.model.datacache.ChassisDB;
import org.lisoft.lsml.model.datacache.ItemDB;
import org.lisoft.lsml.model.item.Faction;
import org.lisoft.lsml.model.loadout.DefaultLoadoutFactory;
import org.lisoft.lsml.model.loadout.Loadout;
import org.lisoft.lsml.model.loadout.LoadoutBuilder.ErrorReportingCallback;
import org.lisoft.lsml.model.loadout.LoadoutOmniMech;
import org.lisoft.lsml.model.loadout.LoadoutStandard;

public class GarageSerialiserTest {
    private GarageSerialiser       cut = new GarageSerialiser();
    private ErrorReportingCallback erc = mock(ErrorReportingCallback.class);

    // TODO: Test the error reporting.

    @Test
    public void testSaveLoadAllStock() throws Exception {

        Garage garage = new Garage();
        for (ChassisClass chassisClass : ChassisClass.values()) {
            if (chassisClass == ChassisClass.COLOSSAL) {
                continue;
            }
            GarageDirectory<Loadout> directory = new GarageDirectory<>(chassisClass.getUiName());
            garage.getLoadoutRoot().getDirectories().add(directory);
            for (Chassis chassis : ChassisDB.lookup(chassisClass)) {
                directory.getValues().add(DefaultLoadoutFactory.instance.produceStock(chassis));
            }
        }

        ByteArrayOutputStream baos = new ByteArrayOutputStream(512 * 1024);
        cut.save(baos, garage, erc);

        ByteArrayInputStream bais = new ByteArrayInputStream(baos.toByteArray());
        Garage loaded = cut.load(bais, erc);

        assertEquals(garage, loaded);
    }

    /**
     * Check that we can load a garage from LSML 1.7.2 with all stock loadouts and IS + CLAN drop ships.
     * 
     * @throws Exception
     */
    @Test
    public void testLoad_LSML172() throws Exception {
        try (FileInputStream fis = new FileInputStream("resources/resources/garage_172.xml");
                BufferedInputStream bis = new BufferedInputStream(fis);) {

            Garage garage = cut.load(bis, erc);

            assertEquals(4, garage.getLoadoutRoot().getDirectories().size());
            assertEquals(2, garage.getDropShipRoot().getDirectories().size());

            for (GarageDirectory<Loadout> dir : garage.getLoadoutRoot().getDirectories()) {
                if (dir.getName().equals(ChassisClass.LIGHT.getUiName())
                        || dir.getName().equals(ChassisClass.MEDIUM.getUiName())
                        || dir.getName().equals(ChassisClass.HEAVY.getUiName())
                        || dir.getName().equals(ChassisClass.ASSAULT.getUiName())) {
                    assertTrue(dir.getValues().size() > 60);
                    for (Loadout loadout : dir.getValues()) {
                        Chassis chassis = loadout.getChassis();
                        Loadout stock = DefaultLoadoutFactory.instance.produceStock(chassis);
                        assertEquals(stock, loadout);
                    }
                }
                else {
                    fail("Unexpected directory name: " + dir);
                }
            }

            for (GarageDirectory<DropShip> dir : garage.getDropShipRoot().getDirectories()) {
                if (dir.getName().equals(Faction.CLAN.getUiName())
                        || dir.getName().equals(Faction.INNERSPHERE.getUiName())) {
                    assertEquals(1, dir.getValues().size());
                    for (int i = 0; i < DropShip.MECHS_IN_DROPSHIP; ++i) {
                        Loadout loadout = dir.getValues().get(0).getMech(i);
                        Chassis chassis = loadout.getChassis();
                        Loadout stock = DefaultLoadoutFactory.instance.produceStock(chassis);
                        assertEquals(stock, loadout);
                    }
                }
            }

        }
    }

    /**
     * Make sure that we can load a garage from LSML 1.5.0.
     * 
     * @throws Exception
     */
    @Test
    public void testLoad_LSML150() throws Exception {

        try (FileInputStream fis = new FileInputStream("resources/resources/garage_150.xml");
                BufferedInputStream bis = new BufferedInputStream(fis);) {

            Garage garage = cut.load(bis, erc);
            int totalLoadouts = 0;

            for (GarageDirectory<Loadout> dir : garage.getLoadoutRoot().getDirectories()) {
                if (dir.getName().equals(ChassisClass.LIGHT.getUiName())
                        || dir.getName().equals(ChassisClass.MEDIUM.getUiName())
                        || dir.getName().equals(ChassisClass.HEAVY.getUiName())
                        || dir.getName().equals(ChassisClass.ASSAULT.getUiName())) {
                    final int numLoadouts = dir.getValues().size();
                    totalLoadouts += numLoadouts;
                    final int minLoadouts = 5;
                    assertTrue("Expected at least " + minLoadouts + " loadouts, only got: " + numLoadouts,
                            numLoadouts > minLoadouts);
                    for (Loadout loadout : dir.getValues()) {
                        Chassis chassis = loadout.getChassis();
                        Loadout stock = DefaultLoadoutFactory.instance.produceStock(chassis);
                        assertEquals(stock, loadout);
                    }
                }
                else {
                    fail("Unexpected directory name: " + dir);
                }
            }

            assertEquals(64, totalLoadouts);
        }
    }

    /**
     * Issue #337. Actuator state is not saved properly.
     * 
     * @throws IOException
     */
    @Test
    public void testActuatorStateSaved() throws IOException {
        LoadoutOmniMech loadout = (LoadoutOmniMech) DefaultLoadoutFactory.instance
                .produceEmpty(ChassisDB.lookup("WHK-B"));

        loadout.getComponent(Location.RightArm).setToggleState(ItemDB.LAA, false);

        Garage garage = new Garage();
        garage.getLoadoutRoot().getValues().add(loadout);

        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        cut.save(baos, garage, erc);

        ByteArrayInputStream bais = new ByteArrayInputStream(baos.toByteArray());
        Garage loadedGarage = cut.load(bais, erc);
        LoadoutOmniMech loadedLoadout = (LoadoutOmniMech) loadedGarage.getLoadoutRoot().getValues().get(0);

        assertFalse(loadedLoadout.getComponent(Location.RightArm).getToggleState(ItemDB.LAA));
    }

    /**
     * Even if DHS are serialized before the Engine, they should be added as engine heat sinks.
     */
    @Test
    public void testUnMarshalDhsBeforeEngine() {
        String xml = "<?xml version=\"1.0\" ?><garage><mechs><loadout name=\"AS7-BH\" chassi=\"AS7-BH\"><upgrades version=\"2\"><armor>2810</armor><structure>3100</structure><guidance>3051</guidance><heatsinks>3002</heatsinks></upgrades><efficiencies><speedTweak>false</speedTweak><coolRun>false</coolRun><heatContainment>false</heatContainment><anchorTurn>false</anchorTurn><doubleBasics>false</doubleBasics><fastfire>false</fastfire></efficiencies><component part=\"Head\" armor=\"0\" /><component part=\"LeftArm\" armor=\"0\" /><component part=\"LeftLeg\" armor=\"0\" /><component part=\"LeftTorso\" armor=\"0/0\" /><component part=\"CenterTorso\" armor=\"0/0\"><item>3001</item><item>3001</item><item>3001</item><item>3001</item><item>3001</item><item>3001</item><item>3278</item></component><component part=\"RightTorso\" armor=\"0/0\" /><component part=\"RightLeg\" armor=\"0\" /><component part=\"RightArm\" armor=\"0\" /></loadout></mechs></garage>";

        Garage garage = cut.load(new ByteArrayInputStream(xml.getBytes(StandardCharsets.UTF_8)), erc);
        boolean found = false;
        for (GarageDirectory<Loadout> dir : garage.getLoadoutRoot().getDirectories()) {
            if (dir.getName() == ChassisClass.ASSAULT.getUiName()) {
                LoadoutStandard loadout = (LoadoutStandard) dir.getValues().get(0);
                assertEquals(6, loadout.getComponent(Location.CenterTorso).getEngineHeatSinks());
                found = true;
            }
        }
        assertTrue(found);
    }
}
