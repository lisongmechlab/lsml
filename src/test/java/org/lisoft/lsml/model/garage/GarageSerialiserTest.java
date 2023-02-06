/*
 * Li Song Mechlab - A 'mech building tool for PGI's MechWarrior: Online.
 * Copyright (C) 2013-2023  Li Song
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
package org.lisoft.lsml.model.garage;

import static org.junit.Assert.*;
import static org.mockito.Mockito.mock;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;
import org.junit.Test;
import org.lisoft.lsml.application.ErrorReporter;
import org.lisoft.lsml.model.loadout.*;
import org.lisoft.lsml.mwo_data.ChassisDB;
import org.lisoft.lsml.mwo_data.Faction;
import org.lisoft.lsml.mwo_data.ItemDB;
import org.lisoft.lsml.mwo_data.mechs.Chassis;
import org.lisoft.lsml.mwo_data.mechs.ChassisClass;
import org.lisoft.lsml.mwo_data.mechs.Location;

public class GarageSerialiserTest {
  private final LoadoutBuilder builder = new LoadoutBuilder();
  private final ErrorReporter erc = mock(ErrorReporter.class);
  private final LoadoutFactory loadoutFactory = new DefaultLoadoutFactory();
  private final GarageSerializer cut = new GarageSerializer(erc, loadoutFactory, builder);

  // TODO: Test the error reporting.
  // TODO: Use Dagger for injecting the things

  /** Issue #337. Actuator state is not saved properly. */
  @Test
  public void testActuatorStateSaved() throws IOException {
    final LoadoutOmniMech loadout =
        (LoadoutOmniMech) loadoutFactory.produceEmpty(ChassisDB.lookup("WHK-B"));

    loadout.getComponent(Location.RightArm).setToggleState(ItemDB.LAA, false);

    final Garage garage = new Garage();
    garage.getLoadoutRoot().getValues().add(loadout);

    final Garage loadedGarage = saveAndLoad(garage);

    final LoadoutOmniMech loadedLoadout =
        (LoadoutOmniMech) loadedGarage.getLoadoutRoot().getValues().get(0);

    assertFalse(loadedLoadout.getComponent(Location.RightArm).getToggleState(ItemDB.LAA));
  }

  /** Issue #772 - Loading mechs with messed up toggle state shouldn't cause a crash. */
  @Test
  public void testRecoverBadTogglables() throws Exception {
    try (InputStream is =
        ClassLoader.getSystemClassLoader().getResourceAsStream("issue772.lsxml")) {
      Garage garage = cut.load(is);
      // No exceptions are thrown
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
        } catch (final Throwable e) {
          // Ignore loadouts for which stock cannot be loaded due to errors in the data
          // files.
        }
      }
    }

    final Garage loaded = saveAndLoad(garage);
    assertEquals(garage, loaded);
  }

  @Test
  public void testSaveLoadDropShips() throws IOException {
    final Garage garage = new Garage();
    garage.getDropShipRoot().getValues().add(new DropShip(Faction.CLAN));

    final Garage loaded = saveAndLoad(garage);
    assertEquals(garage, loaded);
  }

  /** Even if DHS are serialised before the Engine, they should be added as engine heat sinks. */
  @Test
  public void testUnMarshalDhsBeforeEngine() {
    final String xml =
        "<?xml version=\"1.0\" ?><garage><mechs><loadout name=\"AS7-BH\" chassi=\"AS7-BH\"><upgrades version=\"2\"><armor>2810</armor><structure>3100</structure><guidance>3051</guidance><heatsinks>3002</heatsinks></upgrades><efficiencies><speedTweak>false</speedTweak><coolRun>false</coolRun><heatContainment>false</heatContainment><anchorTurn>false</anchorTurn><doubleBasics>false</doubleBasics><fastfire>false</fastfire></efficiencies><component part=\"Head\" armor=\"0\" /><component part=\"LeftArm\" armor=\"0\" /><component part=\"LeftLeg\" armor=\"0\" /><component part=\"LeftTorso\" armor=\"0/0\" /><component part=\"CenterTorso\" armor=\"0/0\"><item>3001</item><item>3001</item><item>3001</item><item>3001</item><item>3001</item><item>3001</item><item>3278</item></component><component part=\"RightTorso\" armor=\"0/0\" /><component part=\"RightLeg\" armor=\"0\" /><component part=\"RightArm\" armor=\"0\" /></loadout></mechs></garage>";

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

  private Garage saveAndLoad(Garage aGarage) throws IOException {
    final ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
    try (final GZIPOutputStream zipStream = new GZIPOutputStream(outputStream)) {
      cut.save(zipStream, aGarage);
    }

    try (final ByteArrayInputStream inputStream =
            new ByteArrayInputStream(outputStream.toByteArray());
        final GZIPInputStream zipStream = new GZIPInputStream(inputStream)) {
      return cut.load(zipStream);
    }
  }
}
