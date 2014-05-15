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
package lisong_mechlab.model.upgrades;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import lisong_mechlab.model.chassi.ChassisIS;

import org.junit.Test;

/**
 * Test suite for {@link StructureUpgrade}
 * 
 * @author Emily Björk
 */
public class StructureUpgradeTest{
   /**
    * Test properties of standard structure
    */
   @Test
   public void testStandardStructure(){
      final int ss_id = 3100;
      StructureUpgrade cut = (StructureUpgrade)UpgradeDB.lookup(ss_id);

      ChassisIS chassi = mock(ChassisIS.class);
      final int chassiMass = 35;
      when(chassi.getMassMax()).thenReturn(chassiMass);

      assertNotNull(cut);
      assertEquals(ss_id, cut.getMwoId());
      assertEquals("STANDARD STRUCTURE", cut.getName());
      assertFalse(cut.getDescription().equals(""));
      assertEquals(0, cut.getExtraSlots());
      assertEquals(chassiMass * 0.1, cut.getStructureMass(chassi), 0.0);
   }

   /**
    * Test properties of endo-steel structure
    */
   @Test
   public void testEndoSteelStructure(){
      final int es_id = 3101;
      StructureUpgrade cut = (StructureUpgrade)UpgradeDB.lookup(es_id);

      ChassisIS chassi = mock(ChassisIS.class);
      final int chassiMass = 35;
      when(chassi.getMassMax()).thenReturn(chassiMass);

      assertNotNull(cut);
      assertEquals(es_id, cut.getMwoId());
      assertEquals("ENDO-STEEL STRUCTURE", cut.getName());
      assertFalse(cut.getDescription().equals(""));
      assertEquals(14, cut.getExtraSlots());
      assertEquals(2.0, cut.getStructureMass(chassi), 0.0);
   }

   /**
    * Test the rounding of endo-steel structure (all tonnage amounts are rounded up to the closest half ton)
    */
   @Test
   public void testEndoSteelStructure_rounding(){
      final int es_id = 3101;
      StructureUpgrade cut = (StructureUpgrade)UpgradeDB.lookup(es_id);

      ChassisIS chassi = mock(ChassisIS.class);
      final int chassiMass = 35;
      when(chassi.getMassMax()).thenReturn(chassiMass);
      assertEquals(2.0, cut.getStructureMass(chassi), 0.0);
   }
}
