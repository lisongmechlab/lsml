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
package org.lisoft.lsml.model.loadout;

import static org.junit.Assert.*;

import java.util.ArrayList;
import java.util.List;
import org.junit.Test;
import org.lisoft.lsml.model.ChassisDB;
import org.lisoft.lsml.model.ConsumableDB;
import org.lisoft.lsml.model.ItemDB;
import org.lisoft.lsml.model.OmniPodDB;
import org.lisoft.lsml.model.UpgradeDB;
import org.lisoft.lsml.util.TestHelpers;
import org.lisoft.mwo_data.mechs.Chassis;
import org.lisoft.mwo_data.mechs.Location;
import org.lisoft.mwo_data.mechs.OmniPod;
import org.lisoft.mwo_data.modifiers.PilotSkills;

/**
 * Test the default factory for creating loadouts.
 *
 * @author Li Song
 */
public class DefaultLoadoutFactoryTest {

  final DefaultLoadoutFactory cut = new DefaultLoadoutFactory();

  @Test
  public void testProduceClone_Actuators() {
    final LoadoutOmniMech loadout =
        (LoadoutOmniMech) cut.produceEmpty(ChassisDB.lookup("SCR-PRIME"));
    loadout.getComponent(Location.LeftArm).setToggleState(ItemDB.HA, false);
    loadout.getComponent(Location.LeftArm).setToggleState(ItemDB.LAA, false);
    loadout.getComponent(Location.RightArm).setToggleState(ItemDB.LAA, true);
    loadout.getComponent(Location.RightArm).setToggleState(ItemDB.HA, true);

    final Loadout clone = cut.produceClone(loadout);

    assertEquals(loadout, clone);
  }

  @Test
  public void testProduceClone_ItemsAndArmour() throws Exception {
    final String as7ddc_stock = "AA0000N1|jb|h^|h^|h^p01|@O|@O|X?q01|[O|\\O|h^|Z?|7@r41|h^|Y?s41|h^|Y?tB1|h^|h^uB1|h^|h^vB0wL0D0D0";
    final Loadout loadout = TestHelpers.parse(as7ddc_stock);
    assertTrue(loadout.getMass() > 95); // Verify that a stock build was loaded

    final Loadout clone = cut.produceClone(loadout);

    assertEquals(loadout, clone);
  }

  @Test
  public void testProduceClone_Modules() throws Exception {
    final LoadoutStandard loadout =
        (LoadoutStandard) cut.produceEmpty(ChassisDB.lookup("AS7-D-DC"));
    loadout.addModule(ConsumableDB.lookup("COOL SHOT"));
    loadout.addModule(ConsumableDB.lookup("ADVANCED UAV"));

    final Loadout clone = cut.produceClone(loadout);

    assertEquals(loadout, clone);
  }

  @Test
  public void testProduceClone_Name() {
    final LoadoutStandard loadout =
        (LoadoutStandard) cut.produceEmpty(ChassisDB.lookup("AS7-D-DC"));
    loadout.setName("NewName");

    final Loadout clone = cut.produceClone(loadout);

    assertEquals(loadout, clone);
  }

  @Test
  public void testProduceClone_NotSame() throws Exception {
    final String as7ddc_stock = "AA0000N1|jb|h^|h^|h^p01|@O|@O|X?q01|[O|\\O|h^|Z?|7@r41|h^|Y?s41|h^|Y?tB1|h^|h^uB1|h^|h^vB0wL0D0D0";
    final Loadout loadout = TestHelpers.parse(as7ddc_stock);
    assertTrue(loadout.getMass() > 95); // Verify that a stock build was loaded

    final Loadout clone = cut.produceClone(loadout);

    assertNotSame(loadout, clone);
  }

  @Test
  public void testProduceClone_OmniPods() {
    final LoadoutOmniMech loadout =
        (LoadoutOmniMech) cut.produceEmpty(ChassisDB.lookup("TBR-PRIME"));

    int podId = 0;
    for (final Location loc : Location.values()) {
      if (loc == Location.CenterTorso) {
        continue;
      }

      final List<OmniPod> possiblePods =
          new ArrayList<>(OmniPodDB.lookup(loadout.getChassis(), loc));
      final OmniPod newPod = possiblePods.get(podId % possiblePods.size());
      podId++;
      loadout.getComponent(loc).changeOmniPod(newPod);
    }

    final Loadout clone = cut.produceClone(loadout);

    assertEquals(loadout, clone);
  }

  @Test
  public void testProduceClone_Upgrades() {
    final LoadoutStandard loadout =
        (LoadoutStandard) cut.produceEmpty(ChassisDB.lookup("AS7-D-DC"));
    loadout.getUpgrades().setHeatSink(UpgradeDB.IS_DHS);
    loadout.getUpgrades().setArmour(UpgradeDB.IS_FF_ARMOUR);
    loadout.getUpgrades().setStructure(UpgradeDB.IS_ES_STRUCTURE);
    loadout.getUpgrades().setGuidance(UpgradeDB.ARTEMIS_IV);

    final Loadout clone = cut.produceClone(loadout);

    assertEquals(loadout, clone);
  }

  @Test
  public void testProduceEmpty() {
    final Chassis chassis = ChassisDB.lookup("CPLT-K2");
    final Loadout loadout = cut.produceEmpty(ChassisDB.lookup("CPLT-K2"));

    assertEquals(0, loadout.getArmour());
    assertSame(chassis, loadout.getChassis());
    assertNull(loadout.getEngine());
    assertEquals(0, loadout.getTotalHeatSinksCount());
    assertEquals(chassis.getShortName(), loadout.getName());
    assertEquals(21, loadout.getSlotsUsed()); // 21 for empty K2
    assertEquals(57, loadout.getFreeSlots()); // 57 for empty K2
    assertEquals(8, loadout.getComponents().size());

    final PilotSkills efficiencies = loadout.getEfficiencies();
    assertTrue(efficiencies.getModifiers().isEmpty());

    final WeaponGroups groups = loadout.getWeaponGroups();
    for (int i = 0; i < WeaponGroups.MAX_GROUPS; ++i) {
      assertTrue(groups.getWeapons(i, loadout).isEmpty());
      // assertSame(FiringMode.Optimal, groups.getFiringMode(i));
    }

    assertEquals(UpgradeDB.STD_GUIDANCE, loadout.getUpgrades().getGuidance());
    assertEquals(UpgradeDB.IS_STD_STRUCTURE, loadout.getUpgrades().getStructure());
    assertEquals(UpgradeDB.IS_STD_ARMOUR, loadout.getUpgrades().getArmour());
    assertEquals(UpgradeDB.IS_SHS, loadout.getUpgrades().getHeatSink());
  }

  /**
   * Must be able to load stock builds that have actuator states set.
   */
  @Test
  public void testProduceStock_Bug433() throws Exception {
    final LoadoutOmniMech loadout =
        (LoadoutOmniMech) cut.produceStock(ChassisDB.lookup("SCR-PRIME(S)"));
    assertFalse(loadout.getComponent(Location.LeftArm).getToggleState(ItemDB.LAA));
  }
}
