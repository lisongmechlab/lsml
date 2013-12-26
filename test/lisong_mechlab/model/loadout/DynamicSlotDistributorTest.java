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
package lisong_mechlab.model.loadout;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.when;

import java.util.Arrays;
import java.util.List;

import lisong_mechlab.model.helpers.MockLoadoutContainer;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.runners.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class DynamicSlotDistributorTest{
   MockLoadoutContainer   mlc = new MockLoadoutContainer();
   List<LoadoutPart>      priorityOrder;
   DynamicSlotDistributor cut;

   @Before
   public void setup(){
      cut = new DynamicSlotDistributor(mlc.loadout);

      // Priority order: RA, RT, RL, HD, CT, LT, LL, LA
      priorityOrder = Arrays.asList(mlc.ra, mlc.rt, mlc.rl, mlc.hd, mlc.ct, mlc.lt, mlc.ll, mlc.la);
   }

   @Test
   public void testGetDynamicStructureSlotsForComponent_NoUpgrades(){
      when(mlc.upgrades.hasEndoSteel()).thenReturn(false);
      when(mlc.upgrades.hasFerroFibrous()).thenReturn(false);

      when(mlc.ra.getNumCriticalSlotsFree()).thenReturn(12);
      when(mlc.rt.getNumCriticalSlotsFree()).thenReturn(12);
      when(mlc.rl.getNumCriticalSlotsFree()).thenReturn(12);
      when(mlc.hd.getNumCriticalSlotsFree()).thenReturn(12);
      when(mlc.ct.getNumCriticalSlotsFree()).thenReturn(12);
      when(mlc.ll.getNumCriticalSlotsFree()).thenReturn(12);
      when(mlc.lt.getNumCriticalSlotsFree()).thenReturn(12);
      when(mlc.la.getNumCriticalSlotsFree()).thenReturn(12);

      assertEquals(0, cut.getDynamicStructureSlots(mlc.ra));
      assertEquals(0, cut.getDynamicStructureSlots(mlc.rt));
      assertEquals(0, cut.getDynamicStructureSlots(mlc.rl));
      assertEquals(0, cut.getDynamicStructureSlots(mlc.hd));
      assertEquals(0, cut.getDynamicStructureSlots(mlc.ct));
      assertEquals(0, cut.getDynamicStructureSlots(mlc.lt));
      assertEquals(0, cut.getDynamicStructureSlots(mlc.ll));
      assertEquals(0, cut.getDynamicStructureSlots(mlc.la));

      assertEquals(0, cut.getDynamicArmorSlots(mlc.ra));
      assertEquals(0, cut.getDynamicArmorSlots(mlc.rt));
      assertEquals(0, cut.getDynamicArmorSlots(mlc.rl));
      assertEquals(0, cut.getDynamicArmorSlots(mlc.hd));
      assertEquals(0, cut.getDynamicArmorSlots(mlc.ct));
      assertEquals(0, cut.getDynamicArmorSlots(mlc.lt));
      assertEquals(0, cut.getDynamicArmorSlots(mlc.ll));
      assertEquals(0, cut.getDynamicArmorSlots(mlc.la));
   }

   /**
    * Calculates the cumulative number of slots that are free up until the argument according to the priority order of
    * components.
    * 
    * @param aPart
    * @return
    */
   private int cumSlotsFree(LoadoutPart aPart){
      int i = priorityOrder.indexOf(aPart);
      int sum = 0;
      while( i > 0 ){
         i--;
         sum += priorityOrder.get(i).getNumCriticalSlotsFree();
      }
      return sum;
   }

   /**
    * Calculates the number of cumulative slots that are occupied by dynamic slots given the maximum number of dynamic
    * slots that can be distributed.
    * 
    * @param aPart
    * @param slotsTotal
    * @return
    */
   private int slotsOccupied(LoadoutPart aPart, int slotsTotal){
      return Math.min(aPart.getNumCriticalSlotsFree(), Math.max(0, slotsTotal - cumSlotsFree(aPart)));
   }

   private String expectedStructure(int slotsTotal){
      StringBuilder sb = new StringBuilder();
      sb.append("{");
      for(LoadoutPart part : priorityOrder){
         sb.append(part).append(" = ");
         sb.append(slotsOccupied(part, slotsTotal));
         sb.append(", ");
      }
      sb.append("}");
      return sb.toString();
   }

   @Test
   public void testGetDynamicStructureSlotsForComponent_Priority(){
      when(mlc.upgrades.hasEndoSteel()).thenReturn(true);
      when(mlc.upgrades.hasFerroFibrous()).thenReturn(false);

      when(mlc.ra.getNumCriticalSlotsFree()).thenReturn(12);
      when(mlc.rt.getNumCriticalSlotsFree()).thenReturn(12);
      when(mlc.rl.getNumCriticalSlotsFree()).thenReturn(12);
      when(mlc.hd.getNumCriticalSlotsFree()).thenReturn(12);
      when(mlc.ct.getNumCriticalSlotsFree()).thenReturn(12);
      when(mlc.ll.getNumCriticalSlotsFree()).thenReturn(12);
      when(mlc.lt.getNumCriticalSlotsFree()).thenReturn(12);
      when(mlc.la.getNumCriticalSlotsFree()).thenReturn(12);

      for(LoadoutPart part : priorityOrder){
         assertEquals(expectedStructure(14), slotsOccupied(part, 14), cut.getDynamicStructureSlots(part));
      }

      when(mlc.ra.getNumCriticalSlotsFree()).thenReturn(1);
      for(LoadoutPart part : priorityOrder){
         assertEquals(expectedStructure(14), slotsOccupied(part, 14), cut.getDynamicStructureSlots(part));
      }

      when(mlc.rt.getNumCriticalSlotsFree()).thenReturn(1);
      for(LoadoutPart part : priorityOrder){
         assertEquals(expectedStructure(14), slotsOccupied(part, 14), cut.getDynamicStructureSlots(part));
      }

      when(mlc.rl.getNumCriticalSlotsFree()).thenReturn(2);
      for(LoadoutPart part : priorityOrder){
         assertEquals(expectedStructure(14), slotsOccupied(part, 14), cut.getDynamicStructureSlots(part));
      }

      when(mlc.hd.getNumCriticalSlotsFree()).thenReturn(1);
      for(LoadoutPart part : priorityOrder){
         assertEquals(expectedStructure(14), slotsOccupied(part, 14), cut.getDynamicStructureSlots(part));
      }

      when(mlc.ct.getNumCriticalSlotsFree()).thenReturn(3);
      for(LoadoutPart part : priorityOrder){
         assertEquals(expectedStructure(14), slotsOccupied(part, 14), cut.getDynamicStructureSlots(part));
      }

      when(mlc.lt.getNumCriticalSlotsFree()).thenReturn(1);
      for(LoadoutPart part : priorityOrder){
         assertEquals(expectedStructure(14), slotsOccupied(part, 14), cut.getDynamicStructureSlots(part));
      }

      when(mlc.ll.getNumCriticalSlotsFree()).thenReturn(1);
      for(LoadoutPart part : priorityOrder){
         assertEquals(expectedStructure(14), slotsOccupied(part, 14), cut.getDynamicStructureSlots(part));
      }

      when(mlc.la.getNumCriticalSlotsFree()).thenReturn(1);
      for(LoadoutPart part : priorityOrder){
         assertEquals(expectedStructure(14), slotsOccupied(part, 14), cut.getDynamicStructureSlots(part));
      }
      // Slot overflow, fail graciously, no exceptions thrown
   }

   @Test
   public void testGetDynamicArmorSlotsForComponent_Priority(){
      when(mlc.upgrades.hasEndoSteel()).thenReturn(false);
      when(mlc.upgrades.hasFerroFibrous()).thenReturn(true);

      when(mlc.ra.getNumCriticalSlotsFree()).thenReturn(12);
      when(mlc.rt.getNumCriticalSlotsFree()).thenReturn(12);
      when(mlc.rl.getNumCriticalSlotsFree()).thenReturn(12);
      when(mlc.hd.getNumCriticalSlotsFree()).thenReturn(12);
      when(mlc.ct.getNumCriticalSlotsFree()).thenReturn(12);
      when(mlc.ll.getNumCriticalSlotsFree()).thenReturn(12);
      when(mlc.lt.getNumCriticalSlotsFree()).thenReturn(12);
      when(mlc.la.getNumCriticalSlotsFree()).thenReturn(12);

      for(LoadoutPart part : priorityOrder){
         assertEquals(expectedStructure(14), slotsOccupied(part, 14), cut.getDynamicArmorSlots(part));
      }

      when(mlc.ra.getNumCriticalSlotsFree()).thenReturn(1);
      for(LoadoutPart part : priorityOrder){
         assertEquals(expectedStructure(14), slotsOccupied(part, 14), cut.getDynamicArmorSlots(part));
      }

      when(mlc.rt.getNumCriticalSlotsFree()).thenReturn(1);
      for(LoadoutPart part : priorityOrder){
         assertEquals(expectedStructure(14), slotsOccupied(part, 14), cut.getDynamicArmorSlots(part));
      }

      when(mlc.rl.getNumCriticalSlotsFree()).thenReturn(2);
      for(LoadoutPart part : priorityOrder){
         assertEquals(expectedStructure(14), slotsOccupied(part, 14), cut.getDynamicArmorSlots(part));
      }

      when(mlc.hd.getNumCriticalSlotsFree()).thenReturn(1);
      for(LoadoutPart part : priorityOrder){
         assertEquals(expectedStructure(14), slotsOccupied(part, 14), cut.getDynamicArmorSlots(part));
      }

      when(mlc.ct.getNumCriticalSlotsFree()).thenReturn(3);
      for(LoadoutPart part : priorityOrder){
         assertEquals(expectedStructure(14), slotsOccupied(part, 14), cut.getDynamicArmorSlots(part));
      }

      when(mlc.lt.getNumCriticalSlotsFree()).thenReturn(1);
      for(LoadoutPart part : priorityOrder){
         assertEquals(expectedStructure(14), slotsOccupied(part, 14), cut.getDynamicArmorSlots(part));
      }

      when(mlc.ll.getNumCriticalSlotsFree()).thenReturn(1);
      for(LoadoutPart part : priorityOrder){
         assertEquals(expectedStructure(14), slotsOccupied(part, 14), cut.getDynamicArmorSlots(part));
      }

      when(mlc.la.getNumCriticalSlotsFree()).thenReturn(1);
      for(LoadoutPart part : priorityOrder){
         assertEquals(expectedStructure(14), slotsOccupied(part, 14), cut.getDynamicArmorSlots(part));
      }
      // Slot overflow, fail graciously, no exceptions thrown
   }

   /**
    * Dynamic armor slots are distributed before dynamic structure (arbitrary design decision).
    */
   @Test
   public void testMixedArmorStructurePriority(){
      when(mlc.upgrades.hasEndoSteel()).thenReturn(true);
      when(mlc.upgrades.hasFerroFibrous()).thenReturn(true);

      when(mlc.ra.getNumCriticalSlotsFree()).thenReturn(4); // 4 armor
      when(mlc.rt.getNumCriticalSlotsFree()).thenReturn(4);
      when(mlc.rl.getNumCriticalSlotsFree()).thenReturn(4);
      when(mlc.hd.getNumCriticalSlotsFree()).thenReturn(4); // 2 armor 2 structure
      when(mlc.ct.getNumCriticalSlotsFree()).thenReturn(2);
      when(mlc.lt.getNumCriticalSlotsFree()).thenReturn(4);
      when(mlc.ll.getNumCriticalSlotsFree()).thenReturn(7); // 6 structure
      when(mlc.la.getNumCriticalSlotsFree()).thenReturn(1); // 0 structure

      assertEquals(0, cut.getDynamicStructureSlots(mlc.ra));
      assertEquals(4, cut.getDynamicArmorSlots(mlc.ra));

      assertEquals(0, cut.getDynamicStructureSlots(mlc.rt));
      assertEquals(4, cut.getDynamicArmorSlots(mlc.rt));

      assertEquals(0, cut.getDynamicStructureSlots(mlc.rl));
      assertEquals(4, cut.getDynamicArmorSlots(mlc.rl));

      assertEquals(2, cut.getDynamicStructureSlots(mlc.hd));
      assertEquals(2, cut.getDynamicArmorSlots(mlc.hd));

      assertEquals(2, cut.getDynamicStructureSlots(mlc.ct));
      assertEquals(0, cut.getDynamicArmorSlots(mlc.ct));

      assertEquals(4, cut.getDynamicStructureSlots(mlc.lt));
      assertEquals(0, cut.getDynamicArmorSlots(mlc.lt));

      assertEquals(6, cut.getDynamicStructureSlots(mlc.ll));
      assertEquals(0, cut.getDynamicArmorSlots(mlc.ll));

      assertEquals(0, cut.getDynamicStructureSlots(mlc.la));
      assertEquals(0, cut.getDynamicArmorSlots(mlc.la));
   }
}
