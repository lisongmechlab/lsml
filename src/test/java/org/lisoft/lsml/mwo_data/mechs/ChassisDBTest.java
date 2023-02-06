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
package org.lisoft.lsml.mwo_data.mechs;

import static org.junit.Assert.*;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import junitparams.JUnitParamsRunner;
import junitparams.Parameters;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.lisoft.lsml.mwo_data.*;
import org.lisoft.lsml.mwo_data.equipment.Item;

/**
 * This is a test suite for {@link ChassisDB}. The test verifies that the data is loaded properly
 * from the data files.
 *
 * @author Li Song
 */
@RunWith(JUnitParamsRunner.class)
public class ChassisDBTest {

  @Test
  public void testCanLoadBasicMech() {
    final ChassisStandard cut = (ChassisStandard) ChassisDB.lookup("Ilya Muromets");

    assertEquals(140, cut.getEngineMin());
    assertEquals(340, cut.getEngineMax());

    assertEquals("ILYA MUROMETS", cut.getName());
    assertEquals("CTF-IM", cut.getShortName());
    assertEquals(cut.getShortName(), cut.toString());
    assertEquals("ctf-im", cut.getKey());

    assertEquals(70.0, cut.getMassMax(), 0.0);

    assertEquals(434, cut.getArmourMax());

    assertEquals(16.2, cut.getMovementProfileBase().getSpeedFactor(null), 0.0);

    assertSame(ChassisClass.HEAVY, cut.getChassisClass());
    assertEquals(0, cut.getJumpJetsMax());
    assertEquals(0, cut.getHardPointsCount(HardPointType.ECM));

    // Do a through test only on the Ilyas components
    {
      final ComponentStandard pt = cut.getComponent(Location.Head);

      assertEquals(18, pt.getArmourMax());
      assertEquals(15.0, pt.getHitPoints(null), 0.0);
      assertEquals(6, pt.getSlots());
      assertEquals(0, pt.getHardPointCount(HardPointType.ENERGY));
      assertEquals(0, pt.getHardPointCount(HardPointType.BALLISTIC));
      assertEquals(0, pt.getHardPointCount(HardPointType.AMS));
      assertEquals(0, pt.getHardPointCount(HardPointType.MISSILE));
      assertEquals(Location.Head, pt.getLocation());
      assertFalse(pt.getLocation().isTwoSided());
      assertEquals(pt.getLocation().toString(), pt.toString());

      assertEquals(3, pt.getFixedItems().size());
    }

    {
      final ComponentStandard pt = cut.getComponent(Location.RightArm);
      assertEquals(44, pt.getArmourMax());
      assertEquals(22.0, pt.getHitPoints(null), 0.0);
      assertEquals(12, pt.getSlots());
      assertEquals(1, pt.getHardPointCount(HardPointType.ENERGY));
      assertEquals(1, pt.getHardPointCount(HardPointType.BALLISTIC));
      assertEquals(0, pt.getHardPointCount(HardPointType.AMS));
      assertEquals(0, pt.getHardPointCount(HardPointType.MISSILE));
      assertEquals(Location.RightArm, pt.getLocation());
      assertFalse(pt.getLocation().isTwoSided());
      assertEquals(pt.getLocation().toString(), pt.toString());
      assertEquals(3, pt.getFixedItems().size());
    }

    {
      final ComponentStandard pt = cut.getComponent(Location.LeftArm);
      assertEquals(44, pt.getArmourMax());
      assertEquals(22.0, pt.getHitPoints(null), 0.0);
      assertEquals(12, pt.getSlots());
      assertEquals(0, pt.getHardPointCount(HardPointType.ENERGY));
      assertEquals(1, pt.getHardPointCount(HardPointType.BALLISTIC));
      assertEquals(0, pt.getHardPointCount(HardPointType.AMS));
      assertEquals(0, pt.getHardPointCount(HardPointType.MISSILE));
      assertEquals(Location.LeftArm, pt.getLocation());
      assertFalse(pt.getLocation().isTwoSided());
      assertEquals(pt.getLocation().toString(), pt.toString());
      assertEquals(4, pt.getFixedItems().size());
    }

    {
      final ComponentStandard pt = cut.getComponent(Location.RightTorso);
      assertEquals(60, pt.getArmourMax());
      assertEquals(30.0, pt.getHitPoints(null), 0.0);
      assertEquals(12, pt.getSlots());
      assertEquals(1, pt.getHardPointCount(HardPointType.ENERGY));
      assertEquals(1, pt.getHardPointCount(HardPointType.BALLISTIC));
      assertEquals(0, pt.getHardPointCount(HardPointType.AMS));
      assertEquals(0, pt.getHardPointCount(HardPointType.MISSILE));
      assertEquals(Location.RightTorso, pt.getLocation());
      assertTrue(pt.getLocation().isTwoSided());
      assertEquals(pt.getLocation().toString(), pt.toString());
      assertEquals(0, pt.getFixedItems().size());
    }

    {
      final ComponentStandard pt = cut.getComponent(Location.LeftTorso);
      assertEquals(60, pt.getArmourMax());
      assertEquals(30.0, pt.getHitPoints(null), 0.0);
      assertEquals(12, pt.getSlots());
      assertEquals(1, pt.getHardPointCount(HardPointType.ENERGY));
      assertEquals(0, pt.getHardPointCount(HardPointType.BALLISTIC));
      assertEquals(1, pt.getHardPointCount(HardPointType.AMS));
      assertEquals(0, pt.getHardPointCount(HardPointType.MISSILE));
      assertEquals(Location.LeftTorso, pt.getLocation());
      assertTrue(pt.getLocation().isTwoSided());
      assertEquals(pt.getLocation().toString(), pt.toString());
      assertEquals(0, pt.getFixedItems().size());
    }

    {
      final ComponentStandard pt = cut.getComponent(Location.CenterTorso);
      assertEquals(88, pt.getArmourMax());
      assertEquals(44.0, pt.getHitPoints(null), 0.0);
      assertEquals(12, pt.getSlots());
      assertEquals(0, pt.getHardPointCount(HardPointType.ENERGY));
      assertEquals(0, pt.getHardPointCount(HardPointType.BALLISTIC));
      assertEquals(0, pt.getHardPointCount(HardPointType.AMS));
      assertEquals(0, pt.getHardPointCount(HardPointType.MISSILE));
      assertEquals(Location.CenterTorso, pt.getLocation());
      assertTrue(pt.getLocation().isTwoSided());
      assertEquals(pt.getLocation().toString(), pt.toString());
      assertEquals(1, pt.getFixedItems().size());
    }

    {
      final ComponentStandard pt = cut.getComponent(Location.RightLeg);
      assertEquals(60, pt.getArmourMax());
      assertEquals(30.0, pt.getHitPoints(null), 0.0);
      assertEquals(6, pt.getSlots());
      assertEquals(0, pt.getHardPointCount(HardPointType.ENERGY));
      assertEquals(0, pt.getHardPointCount(HardPointType.BALLISTIC));
      assertEquals(0, pt.getHardPointCount(HardPointType.AMS));
      assertEquals(0, pt.getHardPointCount(HardPointType.MISSILE));
      assertEquals(Location.RightLeg, pt.getLocation());
      assertFalse(pt.getLocation().isTwoSided());
      assertEquals(pt.getLocation().toString(), pt.toString());
      assertEquals(4, pt.getFixedItems().size());
    }

    {
      final ComponentStandard pt = cut.getComponent(Location.LeftLeg);
      assertEquals(60, pt.getArmourMax());
      assertEquals(30.0, pt.getHitPoints(null), 0.0);
      assertEquals(6, pt.getSlots());
      assertEquals(0, pt.getHardPointCount(HardPointType.ENERGY));
      assertEquals(0, pt.getHardPointCount(HardPointType.BALLISTIC));
      assertEquals(0, pt.getHardPointCount(HardPointType.AMS));
      assertEquals(0, pt.getHardPointCount(HardPointType.MISSILE));
      assertEquals(Location.LeftLeg, pt.getLocation());
      assertFalse(pt.getLocation().isTwoSided());
      assertEquals(pt.getLocation().toString(), pt.toString());
      assertEquals(4, pt.getFixedItems().size());
    }
  }

  @Test
  public void testCanLoadBasicMech2() {
    final ChassisStandard cut = (ChassisStandard) ChassisDB.lookup("Jenner JR7-F");

    assertEquals(100, cut.getEngineMin()); // However no such engine exists :)
    assertEquals(300, cut.getEngineMax());

    assertEquals("JENNER JR7-F", cut.getName());
    assertEquals("JR7-F", cut.getShortName());
    assertEquals(cut.getShortName(), cut.toString());
    assertEquals("jr7-f", cut.getKey());

    assertEquals(35.0, cut.getMassMax(), 0.0);

    assertEquals(238, cut.getArmourMax());

    assertSame(ChassisClass.LIGHT, cut.getChassisClass());
    assertEquals(5, cut.getJumpJetsMax());
    assertEquals(0, cut.getHardPointsCount(HardPointType.ECM));

    assertEquals(3, cut.getComponent(Location.Head).getFixedItems().size());

    assertEquals(2, cut.getComponent(Location.RightArm).getFixedItems().size());
    assertEquals(2, cut.getComponent(Location.LeftArm).getFixedItems().size());

    assertEquals(4, cut.getComponent(Location.RightLeg).getFixedItems().size());
    assertEquals(4, cut.getComponent(Location.LeftLeg).getFixedItems().size());

    assertEquals(3, cut.getComponent(Location.RightArm).getHardPointCount(HardPointType.ENERGY));
    assertEquals(3, cut.getComponent(Location.LeftArm).getHardPointCount(HardPointType.ENERGY));
    assertEquals(1, cut.getComponent(Location.LeftTorso).getHardPointCount(HardPointType.AMS));
  }

  @Test
  public void testCanLoadECMInfo() {
    final ChassisStandard cut = (ChassisStandard) ChassisDB.lookup("AS7-D-DC");

    assertEquals(200, cut.getEngineMin());
    assertEquals(360, cut.getEngineMax());

    assertEquals("ATLAS AS7-D-DC", cut.getName());
    assertEquals("AS7-D-DC", cut.getShortName());
    assertEquals(cut.getShortName(), cut.toString());
    assertEquals("as7-d-dc", cut.getKey());

    assertEquals(100.0, cut.getMassMax(), 0.0);

    assertEquals(614, cut.getArmourMax());

    assertSame(ChassisClass.ASSAULT, cut.getChassisClass());
    assertEquals(0, cut.getJumpJetsMax());
    assertEquals(1, cut.getHardPointsCount(HardPointType.ECM));

    assertEquals(3, cut.getComponent(Location.Head).getFixedItems().size());

    assertEquals(4, cut.getComponent(Location.RightArm).getFixedItems().size());
    assertEquals(4, cut.getComponent(Location.LeftArm).getFixedItems().size());

    assertEquals(4, cut.getComponent(Location.RightLeg).getFixedItems().size());
    assertEquals(4, cut.getComponent(Location.LeftLeg).getFixedItems().size());

    assertEquals(1, cut.getComponent(Location.RightArm).getHardPointCount(HardPointType.ENERGY));
    assertEquals(1, cut.getComponent(Location.LeftArm).getHardPointCount(HardPointType.ENERGY));
    assertEquals(1, cut.getComponent(Location.LeftArm).getHardPointCount(HardPointType.AMS));
    assertEquals(3, cut.getComponent(Location.LeftTorso).getHardPointCount(HardPointType.MISSILE));
    assertEquals(
        2, cut.getComponent(Location.RightTorso).getHardPointCount(HardPointType.BALLISTIC));
  }

  @Test
  public void testCanLoadEngineInfo() throws Exception {
    final ChassisStandard cut = (ChassisStandard) ChassisDB.lookup("ILYA MUROMETS");

    final Item tooSmall = ItemDB.lookup("STD ENGINE 135");
    final Item tooLarge = ItemDB.lookup("STD ENGINE 345");
    final Item smallest = ItemDB.lookup("STD ENGINE 140");
    final Item largest = ItemDB.lookup("STD ENGINE 340");

    assertFalse(cut.isAllowed(tooSmall));
    assertTrue(cut.isAllowed(smallest));
    assertTrue(cut.isAllowed(largest));
    assertFalse(cut.isAllowed(tooLarge));
  }

  @Test
  public void testCanLoadHardpointInfo() throws Exception {
    final ChassisStandard cut = (ChassisStandard) ChassisDB.lookup("ILYA MUROMETS");

    final Item lrm20 = ItemDB.lookup("LRM 20");
    final Item ac20 = ItemDB.lookup("AC/20");

    assertFalse(cut.isAllowed(lrm20));
    assertTrue(cut.isAllowed(ac20));
  }

  @Test
  public void testCanLoadHardpointInfo2() {
    final ChassisStandard chassi = (ChassisStandard) ChassisDB.lookup("TDR-5S");
    final Collection<HardPoint> hardpoints =
        chassi.getComponent(Location.RightTorso).getHardPoints();
    assertEquals(3, hardpoints.size());

    final List<HardPoint> hps = new ArrayList<>(hardpoints);
    boolean foundAms = false;
    boolean foundLrm10 = false;
    boolean foundLrm20 = false;
    for (final HardPoint hardpoint : hps) {
      if (hardpoint.getType() == HardPointType.AMS) {
        if (foundAms) {
          fail("Two ams when only one expected!");
        }
        foundAms = true;
      } else if (hardpoint.getType() == HardPointType.MISSILE) {
        if (hardpoint.getNumMissileTubes() == 20) {
          if (foundLrm20) {
            fail("Expected only one 20-tuber!");
          }
          foundLrm20 = true;
        } else if (hardpoint.getNumMissileTubes() == 10) {
          if (foundLrm10) {
            fail("Expected only one 10-tuber!");
          }
          foundLrm10 = true;
        } else {
          fail("Unexpected tube count!");
        }
      } else {
        fail("Unexpected hardpoint!");
      }
    }

    assertTrue(foundAms);
    assertTrue(foundLrm10);
    assertTrue(foundLrm20);
  }

  @Test
  public void testCanLoadHardpointInfo3() {
    final ChassisStandard chassi = (ChassisStandard) ChassisDB.lookup("TDR-5S");
    assertEquals(
        3, chassi.getComponent(Location.LeftTorso).getHardPointCount(HardPointType.ENERGY));
    assertEquals(
        0, chassi.getComponent(Location.LeftTorso).getHardPointCount(HardPointType.BALLISTIC));

    assertEquals(1, chassi.getComponent(Location.RightTorso).getHardPointCount(HardPointType.AMS));
    assertEquals(
        2, chassi.getComponent(Location.RightTorso).getHardPointCount(HardPointType.MISSILE));
  }

  @Test
  public void testCanLoadJJInfo() throws Exception {
    final ChassisStandard jj55tons = (ChassisStandard) ChassisDB.lookup("WVR-6R");
    final ChassisStandard jj70tons = (ChassisStandard) ChassisDB.lookup("QKD-4G");
    final ChassisStandard nojj55tons = (ChassisStandard) ChassisDB.lookup("KTO-18");

    final Item classIV = ItemDB.lookup("JUMP JETS - CLASS IV");
    final Item classIII = ItemDB.lookup("JUMP JETS - CLASS III");

    assertFalse(nojj55tons.isAllowed(classIV));
    assertFalse(nojj55tons.isAllowed(classIII));
    assertTrue(jj55tons.isAllowed(classIV));
    assertFalse(jj55tons.isAllowed(classIII));
    assertFalse(jj70tons.isAllowed(classIV));
    assertTrue(jj70tons.isAllowed(classIII));
  }

  @Test
  public void testLookupByChassiClass() {
    final Collection<? extends Chassis> heavies = ChassisDB.lookup(ChassisClass.HEAVY);

    assertTrue(heavies.contains(ChassisDB.lookup("ILYA MUROMETS")));
    assertTrue(heavies.contains(ChassisDB.lookup("JM6-DD")));
    assertTrue(heavies.contains(ChassisDB.lookup("CPLT-C4")));
    assertTrue(heavies.contains(ChassisDB.lookup("FLAME")));
    assertTrue(heavies.contains(ChassisDB.lookup("PROTECTOR")));

    for (final Chassis chassi : heavies) {
      assertEquals(ChassisClass.HEAVY, chassi.getChassisClass());
    }
  }

  /** Test that the assault group contains the right mechs */
  @Test
  public void testLookupByChassiClass_Assault() {
    final Collection<? extends Chassis> heavies = ChassisDB.lookup(ChassisClass.ASSAULT);

    assertTrue(heavies.contains(ChassisDB.lookup("PRETTY BABY")));
    assertTrue(heavies.contains(ChassisDB.lookup("DRAGON SLAYER")));
    assertTrue(heavies.contains(ChassisDB.lookup("MISERY")));
    assertTrue(heavies.contains(ChassisDB.lookup("AS7-D-DC")));

    for (final Chassis chassi : heavies) {
      assertEquals(ChassisClass.ASSAULT, chassi.getChassisClass());
    }
  }

  @Test
  public void testLookupByChassiSeries() {
    final Collection<? extends Chassis> cataphracts = ChassisDB.lookupSeries("CATAphract");

    for (final Chassis c : cataphracts) {
      assertTrue(c.getKey().toLowerCase().contains("ctf-"));
    }

    assertTrue(cataphracts.remove(ChassisDB.lookup("ILYA MUROMETS")));
    assertTrue(cataphracts.remove(ChassisDB.lookup("CTF-1X")));
    assertTrue(cataphracts.remove(ChassisDB.lookup("CTF-2X")));
    assertTrue(cataphracts.remove(ChassisDB.lookup("CTF-3D")));
    assertTrue(cataphracts.remove(ChassisDB.lookup("CTF-3D(C)")));
    assertTrue(cataphracts.remove(ChassisDB.lookup("CTF-4X")));
    assertTrue(cataphracts.remove(ChassisDB.lookup("CTF-0XP")));
  }

  @Test(expected = IllegalArgumentException.class)
  public void testLookupByChassiSeriesFail() {
    ChassisDB.lookupSeries("No such series");
  }

  @Test(expected = IllegalArgumentException.class)
  public void testLookupFailed() {
    // Successful lookup is tested by ChassiTest.java
    ChassisDB.lookup("Nonexistent mech");
  }

  /**
   * {@link ChassisDB#lookupVariations(Chassis)} shall return a list of all chassis variations for
   * the given chassis (including the chassis given as argument).
   *
   * @param aLookup The chassis name to use as a lookup.
   * @param aExpected The expected chassis in addition to the lookup.
   */
  @Parameters({
    "SDR-5K, SDR-5K(C)",
    "SDR-5K(C), SDR-5K",
    "HBK-4P, HBK-4P(C)",
    "HBK-4P(C), HBK-4P",
    "CTF-3D, CTF-3D(C)",
    "CTF-3D(C), CTF-3D(C)",
    "TDR-5S(P), TDR-5S",
    "TDR-5S, TDR-5S(P)"
  })
  @Test
  public void testLookupVariations_LookupFromNormal(String aLookup, String aExpected) {
    final Collection<? extends Chassis> ans = ChassisDB.lookupVariations(ChassisDB.lookup(aLookup));
    assertTrue(ans.contains(ChassisDB.lookup(aLookup)));
    assertTrue(ans.contains(ChassisDB.lookup(aExpected)));
    assertTrue(ans.size() >= 2);
  }
}
