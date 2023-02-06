/*
 * Li Song Mechlab - A 'mech building tool for PGI's MechWarrior: Online.
 * Copyright (C) 2023  Li Song
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
package org.lisoft.lsml.model;

import static org.junit.Assert.*;

import java.util.Map;
import java.util.Optional;
import org.junit.Test;
import org.lisoft.mwo_data.mechs.ChassisOmniMech;
import org.lisoft.mwo_data.mechs.Location;
import org.lisoft.mwo_data.mechs.OmniPod;

/**
 * Test suite for the {@link OmniPodSelector} class.
 *
 * @author Li Song
 */
public class OmniPodSelectorTest {

  @Test
  public void testSelectPods() {
    final ChassisOmniMech hbr_a = (ChassisOmniMech) ChassisDB.lookup("HBR-A");
    final ChassisOmniMech hbr_b = (ChassisOmniMech) ChassisDB.lookup("HBR-B");
    final ChassisOmniMech hbr_prime = (ChassisOmniMech) ChassisDB.lookup("HBR-PRIME");

    final OmniPodSelector cut = new OmniPodSelector();
    final Optional<Map<Location, OmniPod>> ans = cut.selectPods(hbr_a, 7, 2, 0, 0, true);

    // Should only exist one solution:
    // RA-Prime, RT-B, HD-A, LT-Prime, LA-A
    assertTrue(ans.isPresent());
    final Map<Location, OmniPod> pods = ans.get();
    assertSame(
        OmniPodDB.lookupStock(hbr_prime, Location.RightArm).get(), pods.get(Location.RightArm));
    assertSame(
        OmniPodDB.lookupStock(hbr_b, Location.RightTorso).get(), pods.get(Location.RightTorso));
    assertSame(OmniPodDB.lookupStock(hbr_a, Location.Head).get(), pods.get(Location.Head));
    assertSame(
        OmniPodDB.lookupStock(hbr_prime, Location.LeftTorso).get(), pods.get(Location.LeftTorso));
    assertSame(OmniPodDB.lookupStock(hbr_a, Location.LeftArm).get(), pods.get(Location.LeftArm));
  }

  /** Test that hard points in the CT are counted towards the required values. */
  @Test
  public void testSelectPods_CTHardPointCounted() {
    final ChassisOmniMech ifr_a = (ChassisOmniMech) ChassisDB.lookup("IFR-A");
    final ChassisOmniMech ifr_d = (ChassisOmniMech) ChassisDB.lookup("IFR-D");
    final ChassisOmniMech ifr_prime = (ChassisOmniMech) ChassisDB.lookup("IFR-PRIME");

    final OmniPodSelector cut = new OmniPodSelector();
    final Optional<Map<Location, OmniPod>> ans = cut.selectPods(ifr_a, 5, 0, 0, 0, false);

    // Expected solution:
    // RA-D, LA-Prime
    assertTrue(ans.isPresent());
    final Map<Location, OmniPod> pods = ans.get();
    assertSame(OmniPodDB.lookupStock(ifr_d, Location.RightArm).get(), pods.get(Location.RightArm));
    assertSame(
        OmniPodDB.lookupStock(ifr_prime, Location.LeftArm).get(), pods.get(Location.LeftArm));
  }

  @Test
  public void testSelectPods_NoSolution() {
    final ChassisOmniMech adr_prime = (ChassisOmniMech) ChassisDB.lookup("ADR-PRIME");

    final OmniPodSelector cut = new OmniPodSelector();
    final Optional<Map<Location, OmniPod>> ans = cut.selectPods(adr_prime, 5, 0, 2, 0, false);

    assertFalse(ans.isPresent());
  }

  /**
   * Test that jump jets that are satisfied through the chassis (as opposed to the omni mech) are
   * accounted properly.
   */
  @Test
  public void testSelectPods_NonOmniJJ() {
    final ChassisOmniMech shc_a = (ChassisOmniMech) ChassisDB.lookup("SHC-A");
    final ChassisOmniMech shc_b = (ChassisOmniMech) ChassisDB.lookup("SHC-B");
    final ChassisOmniMech shc_p = (ChassisOmniMech) ChassisDB.lookup("SHC-P");
    final ChassisOmniMech shc_prime = (ChassisOmniMech) ChassisDB.lookup("SHC-PRIME");

    final OmniPodSelector cut = new OmniPodSelector();
    final Optional<Map<Location, OmniPod>> ans = cut.selectPods(shc_prime, 0, 2, 3, 6, true);

    // Expected solution:
    // RA-B, RT-A, LT-B, LA-P
    assertTrue(ans.isPresent());
    final Map<Location, OmniPod> pods = ans.get();
    assertSame(OmniPodDB.lookupStock(shc_b, Location.RightArm).get(), pods.get(Location.RightArm));
    assertSame(
        OmniPodDB.lookupStock(shc_a, Location.RightTorso).get(), pods.get(Location.RightTorso));
    assertSame(
        OmniPodDB.lookupStock(shc_b, Location.LeftTorso).get(), pods.get(Location.LeftTorso));
    assertSame(OmniPodDB.lookupStock(shc_p, Location.LeftArm).get(), pods.get(Location.LeftArm));
  }

  @Test
  public void testSelectPods_OmniJJ() {
    final ChassisOmniMech tbr_s = (ChassisOmniMech) ChassisDB.lookup("TBR-S");
    final ChassisOmniMech tbr_prime = (ChassisOmniMech) ChassisDB.lookup("TBR-PRIME");

    final OmniPodSelector cut = new OmniPodSelector();
    final Optional<Map<Location, OmniPod>> ans = cut.selectPods(tbr_prime, 0, 0, 0, 4, false);

    // Expected solution:
    // RT/LT-S
    assertTrue(ans.isPresent());
    final Map<Location, OmniPod> pods = ans.get();
    assertSame(
        OmniPodDB.lookupStock(tbr_s, Location.RightTorso).get(), pods.get(Location.RightTorso));
    assertSame(
        OmniPodDB.lookupStock(tbr_s, Location.LeftTorso).get(), pods.get(Location.LeftTorso));
  }
}
