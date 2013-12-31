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
package lisong_mechlab.model.chassi;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertSame;

import org.junit.Test;

public class InternalPartTest{

   Chassi chassi = ChassiDB.lookup("TDR-5S");

   @Test
   public void testGetType() throws Exception{
      for(Part part : Part.values()){
         InternalPart cut = chassi.getInternalPart(part);

         assertSame(part, cut.getType());
      }
   }

   @Test
   public void testGetArmorMax() throws Exception{
      for(Part part : Part.values()){
         InternalPart cut = chassi.getInternalPart(part);

         if( part == Part.Head ){
            assertEquals(18, cut.getArmorMax());
         }
         else{
            assertEquals((int)cut.getHitpoints() * 2, cut.getArmorMax());
         }
      }
   }

   @Test
   public void testGetNumCriticalslots() throws Exception{
      for(Part part : Part.values()){
         InternalPart cut = chassi.getInternalPart(part);

         if( part == Part.Head || part == Part.RightLeg || part == Part.LeftLeg ){
            assertEquals(6, cut.getNumCriticalslots());
         }
         else{
            assertEquals(12, cut.getNumCriticalslots());
         }
      }
   }

   @Test
   public void testGetNumHardpoints() throws Exception{
      throw new RuntimeException("not yet implemented");
   }

   @Test
   public void testGetInternalItems() throws Exception{
      for(Part part : Part.values()){
         InternalPart cut = chassi.getInternalPart(part);

         switch( part ){
            case Head:
               assertEquals(3, cut.getInternalItems().size());
               break;
            case RightLeg:
               assertEquals(4, cut.getInternalItems().size());
               break;
            case RightArm:
               assertEquals(4, cut.getInternalItems().size());
               break;
            case LeftArm:
               assertEquals(4, cut.getInternalItems().size());
               break;
            case LeftLeg:
               assertEquals(4, cut.getInternalItems().size());
               break;
            case CenterTorso:
               assertEquals(1, cut.getInternalItems().size());
               break;
            default:
               break;
         }
      }
   }

   @Test
   public void testGetHitpoints() throws Exception{
      for(Part part : Part.values()){
         InternalPart cut = chassi.getInternalPart(part);

         switch( part ){
            case RightArm:
               assertEquals(20, cut.getHitpoints(), 0.0);
               break;
            case RightTorso:
               assertEquals(30, cut.getHitpoints(), 0.0);
               break;
            case RightLeg:
               assertEquals(30, cut.getHitpoints(), 0.0);
               break;
            case Head:
               assertEquals(15, cut.getHitpoints(), 0.0);
               break;
            case CenterTorso:
               assertEquals(42, cut.getHitpoints(), 0.0);
               break;
            case LeftTorso:
               assertEquals(30, cut.getHitpoints(), 0.0);
               break;
            case LeftLeg:
               assertEquals(30, cut.getHitpoints(), 0.0);
               break;
            case LeftArm:
               assertEquals(20, cut.getHitpoints(), 0.0);
               break;
            default:
               break;
         }
      }
   }
}
