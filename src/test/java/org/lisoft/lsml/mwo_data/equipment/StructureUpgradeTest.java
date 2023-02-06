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
package org.lisoft.lsml.mwo_data.equipment;

import static org.junit.Assert.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import org.junit.Test;
import org.lisoft.lsml.mwo_data.mechs.ChassisStandard;

/**
 * Test suite for {@link StructureUpgrade}
 *
 * @author Li Song
 */
public class StructureUpgradeTest {
  /** Test properties of endo-steel structure */
  @Test
  public void testEndoSteelStructure() throws Exception {
    final int es_id = 3101;
    final StructureUpgrade cut = (StructureUpgrade) UpgradeDB.lookup(es_id);

    final ChassisStandard chassi = mock(ChassisStandard.class);
    final int chassiMass = 35;
    when(chassi.getMassMax()).thenReturn(chassiMass);

    assertNotNull(cut);
    assertEquals(es_id, cut.getId());
    assertTrue(cut.getName().contains("ENDO-STEEL"));
    assertFalse(cut.getDescription().equals(""));
    assertEquals(14, cut.getExtraSlots());
    assertEquals(2.0, cut.getStructureMass(chassi), 0.0);
  }

  /**
   * Test the rounding of endo-steel structure (all tonnage amounts are rounded up to the closest
   * half ton)
   */
  @Test
  public void testEndoSteelStructure_rounding() throws Exception {
    final int es_id = 3101;
    final StructureUpgrade cut = (StructureUpgrade) UpgradeDB.lookup(es_id);

    final ChassisStandard chassi = mock(ChassisStandard.class);
    final int chassiMass = 35;
    when(chassi.getMassMax()).thenReturn(chassiMass);
    assertEquals(2.0, cut.getStructureMass(chassi), 0.0);
  }

  /** Test properties of standard structure */
  @Test
  public void testStandardStructure() throws Exception {
    final int ss_id = 3100;
    final StructureUpgrade cut = (StructureUpgrade) UpgradeDB.lookup(ss_id);

    final ChassisStandard chassi = mock(ChassisStandard.class);
    final int chassiMass = 35;
    when(chassi.getMassMax()).thenReturn(chassiMass);

    assertNotNull(cut);
    assertEquals(ss_id, cut.getId());
    assertTrue(cut.getName().contains("STANDARD"));
    assertFalse(cut.getDescription().equals(""));
    assertEquals(0, cut.getExtraSlots());
    assertEquals(chassiMass * 0.1, cut.getStructureMass(chassi), 0.0);
  }
}
