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
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;

import org.junit.Test;
import org.lisoft.lsml.application.ErrorReporter;
import org.lisoft.lsml.model.chassi.Chassis;
import org.lisoft.lsml.model.chassi.ChassisClass;
import org.lisoft.lsml.model.chassi.Location;
import org.lisoft.lsml.model.database.ChassisDB;
import org.lisoft.lsml.model.database.ItemDB;
import org.lisoft.lsml.model.item.Faction;
import org.lisoft.lsml.model.loadout.DefaultLoadoutFactory;
import org.lisoft.lsml.model.loadout.Loadout;
import org.lisoft.lsml.model.loadout.LoadoutBuilder;
import org.lisoft.lsml.model.loadout.LoadoutFactory;
import org.lisoft.lsml.model.loadout.LoadoutOmniMech;
import org.lisoft.lsml.model.loadout.LoadoutStandard;

@SuppressWarnings("javadoc")
public class GarageSerialiserTest {
    private final ErrorReporter erc = mock(ErrorReporter.class);
    private final LoadoutFactory loadoutFactory = new DefaultLoadoutFactory();
    private final LoadoutBuilder builder = new LoadoutBuilder();
    private final GarageSerialiser cut = new GarageSerialiser(erc, loadoutFactory, builder);

    // TODO: Test the error reporting.
    // TODO: Use Dagger for injecting the things

    /**
     * Issue #337. Actuator state is not saved properly.
     */
    @Test
    public void testActuatorStateSaved() {
        final LoadoutOmniMech loadout = (LoadoutOmniMech) loadoutFactory.produceEmpty(ChassisDB.lookup("WHK-B"));

        loadout.getComponent(Location.RightArm).setToggleState(ItemDB.LAA, false);

        final Garage garage = new Garage();
        garage.getLoadoutRoot().getValues().add(loadout);

        final ByteArrayOutputStream baos = new ByteArrayOutputStream();
        cut.save(baos, garage);

        final ByteArrayInputStream bais = new ByteArrayInputStream(baos.toByteArray());
        final Garage loadedGarage = cut.load(bais);
        final LoadoutOmniMech loadedLoadout = (LoadoutOmniMech) loadedGarage.getLoadoutRoot().getValues().get(0);

        assertFalse(loadedLoadout.getComponent(Location.RightArm).getToggleState(ItemDB.LAA));
    }

    /**
     * Make sure that we can load a garage from LSML 1.5.0.
     */
    @Test
    public void testLoad_LSML150() throws Exception {
        try (InputStream is = ClassLoader.getSystemClassLoader().getResourceAsStream("garage_150.xml");
                BufferedInputStream bis = new BufferedInputStream(is);) {

            final Garage garage = cut.load(bis);
            int totalLoadouts = 0;

            for (final GarageDirectory<Loadout> dir : garage.getLoadoutRoot().getDirectories()) {
                if (dir.getName().equals(ChassisClass.LIGHT.getUiName())
                        || dir.getName().equals(ChassisClass.MEDIUM.getUiName())
                        || dir.getName().equals(ChassisClass.HEAVY.getUiName())
                        || dir.getName().equals(ChassisClass.ASSAULT.getUiName())) {
                    final int numLoadouts = dir.getValues().size();
                    totalLoadouts += numLoadouts;
                    final int minLoadouts = 5;
                    assertTrue("Expected at least " + minLoadouts + " loadouts, only got: " + numLoadouts,
                            numLoadouts > minLoadouts);
                    for (final Loadout loadout : dir.getValues()) {
                        final Chassis chassis = loadout.getChassis();
                        final Loadout stock = loadoutFactory.produceStock(chassis);
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
     * Check that we can load a garage from LSML 1.7.2 with all stock loadouts and IS + CLAN drop ships.
     */
    @Test
    public void testLoad_LSML172() throws Exception {
        try (InputStream is = ClassLoader.getSystemClassLoader().getResourceAsStream("garage_172.xml");
                BufferedInputStream bis = new BufferedInputStream(is);) {

            final Garage garage = cut.load(bis);

            assertEquals(4, garage.getLoadoutRoot().getDirectories().size());
            assertEquals(2, garage.getDropShipRoot().getDirectories().size());

            for (final GarageDirectory<Loadout> dir : garage.getLoadoutRoot().getDirectories()) {
                if (dir.getName().equals(ChassisClass.LIGHT.getUiName())
                        || dir.getName().equals(ChassisClass.MEDIUM.getUiName())
                        || dir.getName().equals(ChassisClass.HEAVY.getUiName())
                        || dir.getName().equals(ChassisClass.ASSAULT.getUiName())) {
                    assertTrue(dir.getValues().size() > 60);
                    for (final Loadout loadout : dir.getValues()) {
                        final Chassis chassis = loadout.getChassis();
                        final Loadout stock = loadoutFactory.produceStock(chassis);
                        assertEquals(stock, loadout);
                    }
                }
                else {
                    fail("Unexpected directory name: " + dir);
                }
            }

            for (final GarageDirectory<DropShip> dir : garage.getDropShipRoot().getDirectories()) {
                if (dir.getName().equals(Faction.CLAN.getUiName())
                        || dir.getName().equals(Faction.INNERSPHERE.getUiName())) {
                    assertEquals(1, dir.getValues().size());
                    for (int i = 0; i < DropShip.MECHS_IN_DROPSHIP; ++i) {
                        final Loadout loadout = dir.getValues().get(0).getMech(i);
                        final Chassis chassis = loadout.getChassis();
                        final Loadout stock = loadoutFactory.produceStock(chassis);
                        assertEquals(stock, loadout);
                    }
                }
            }

        }
    }

    @Test
    public void testSaveLoadDropShips() throws IOException {
        final Garage garage = new Garage();
        garage.getDropShipRoot().getValues().add(new DropShip(Faction.CLAN));

        final ByteArrayOutputStream baos = new ByteArrayOutputStream(512 * 1024);
        try (final GZIPOutputStream zipOS = new GZIPOutputStream(baos);) {
            cut.save(zipOS, garage);
        }

        try (final ByteArrayInputStream bais = new ByteArrayInputStream(baos.toByteArray());
                final GZIPInputStream zipIS = new GZIPInputStream(bais);) {
            final Garage loaded = cut.load(zipIS);
            assertEquals(garage, loaded);
        }
    }

    @Test
    public void testSaveLoadAllStock() throws Exception {
        final Garage garage = new Garage();
        for (final ChassisClass chassisClass : ChassisClass.values()) {
            if (chassisClass == ChassisClass.COLOSSAL) {
                continue;
            }
            final GarageDirectory<Loadout> directory = new GarageDirectory<>(chassisClass.getUiName());
            garage.getLoadoutRoot().getDirectories().add(directory);
            for (final Chassis chassis : ChassisDB.lookup(chassisClass)) {
                try {
                    directory.getValues().add(loadoutFactory.produceStock(chassis));
                }
                catch (final Throwable e) {
                    // Ignore loadouts for which stock cannot be loaded due to errors in the data
                    // files.
                }
            }
        }
        final ByteArrayOutputStream baos = new ByteArrayOutputStream(512 * 1024);
        try (final GZIPOutputStream zipOS = new GZIPOutputStream(baos);) {
            cut.save(zipOS, garage);
        }

        try (final ByteArrayInputStream bais = new ByteArrayInputStream(baos.toByteArray());
                final GZIPInputStream zipIS = new GZIPInputStream(bais);) {
            final Garage loaded = cut.load(zipIS);
            assertEquals(garage, loaded);
        }
    }

    /**
     * Even if DHS are serialised before the Engine, they should be added as engine heat sinks.
     */
    @Test
    public void testUnMarshalDhsBeforeEngine() {
        final String xml = "<?xml version=\"1.0\" ?><garage><mechs><loadout name=\"AS7-BH\" chassi=\"AS7-BH\"><upgrades version=\"2\"><armor>2810</armor><structure>3100</structure><guidance>3051</guidance><heatsinks>3002</heatsinks></upgrades><efficiencies><speedTweak>false</speedTweak><coolRun>false</coolRun><heatContainment>false</heatContainment><anchorTurn>false</anchorTurn><doubleBasics>false</doubleBasics><fastfire>false</fastfire></efficiencies><component part=\"Head\" armor=\"0\" /><component part=\"LeftArm\" armor=\"0\" /><component part=\"LeftLeg\" armor=\"0\" /><component part=\"LeftTorso\" armor=\"0/0\" /><component part=\"CenterTorso\" armor=\"0/0\"><item>3001</item><item>3001</item><item>3001</item><item>3001</item><item>3001</item><item>3001</item><item>3278</item></component><component part=\"RightTorso\" armor=\"0/0\" /><component part=\"RightLeg\" armor=\"0\" /><component part=\"RightArm\" armor=\"0\" /></loadout></mechs></garage>";

        final Garage garage = cut.load(new ByteArrayInputStream(xml.getBytes(StandardCharsets.UTF_8)));
        boolean found = false;
        for (final GarageDirectory<Loadout> dir : garage.getLoadoutRoot().getDirectories()) {
            if (dir.getName() == ChassisClass.ASSAULT.getUiName()) {
                final LoadoutStandard loadout = (LoadoutStandard) dir.getValues().get(0);
                assertEquals(6, loadout.getComponent(Location.CenterTorso).getEngineHeatSinks());
                found = true;
            }
        }
        assertTrue(found);
    }
}
