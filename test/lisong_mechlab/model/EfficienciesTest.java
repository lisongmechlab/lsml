/*
 * @formatter:off
 * Li Song Mechlab - A 'mech building tool for PGI's MechWarrior: Online.
 * Copyright (C) 2013  Li Song
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
package lisong_mechlab.model;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.reset;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyZeroInteractions;
import lisong_mechlab.model.Efficiencies.Message.Type;
import lisong_mechlab.util.MessageXBar;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class EfficienciesTest{
   @Mock
   private MessageXBar  xBar;
   private Efficiencies cut;

   @Before
   public void setup(){
      cut = new Efficiencies();
   }

   @Test
   public void testSetHasSpeedTweak() throws Exception{
      // Default false
      assertEquals(false, cut.hasSpeedTweak());
      verifyZeroInteractions(xBar);

      // We want messages too!
      for(boolean b : new boolean[] {true, false}){
         cut.setSpeedTweak(b, xBar);
         assertEquals(b, cut.hasSpeedTweak());
         verify(xBar).post(new Efficiencies.Message(cut, Type.Changed));
         reset(xBar);
      }

      // No messages if there was no change.
      for(boolean b : new boolean[] {true, false}){
         cut.setSpeedTweak(b, xBar);
         reset(xBar);
         cut.setSpeedTweak(b, xBar);
         verifyZeroInteractions(xBar);
      }
   }

   @Test
   public void testGetSpeedModifier() throws Exception{
      assertEquals(1.0, cut.getSpeedModifier(), 0.0);

      // These don't affect heat capacity
      cut.setHeatContainment(true, xBar);
      cut.setCoolRun(true, xBar);
      cut.setDoubleBasics(true, xBar);
      assertEquals(1.0, cut.getSpeedModifier(), 0.0);

      // These do
      cut.setSpeedTweak(true, xBar);
      assertEquals(1.1, cut.getSpeedModifier(), 0.0);
      cut.setDoubleBasics(false, xBar);
      assertEquals(1.1, cut.getSpeedModifier(), 0.0);
   }

   @Test
   public void testSetHasCoolRun() throws Exception{
      // Default false
      assertEquals(false, cut.hasCoolRun());
      verifyZeroInteractions(xBar);

      // We want messages too!
      for(boolean b : new boolean[] {true, false}){
         cut.setCoolRun(b, xBar);
         assertEquals(b, cut.hasCoolRun());
         verify(xBar).post(new Efficiencies.Message(cut, Type.Changed));
         reset(xBar);
      }

      // No messages if there was no change.
      for(boolean b : new boolean[] {true, false}){
         cut.setCoolRun(b, xBar);
         reset(xBar);
         cut.setCoolRun(b, xBar);
         verifyZeroInteractions(xBar);
      }
   }

   @Test
   public void testGetHeatDissipationModifier() throws Exception{
      assertEquals(1.0, cut.getHeatDissipationModifier(), 0.0);

      // These don't affect heat capacity
      cut.setHeatContainment(true, xBar);
      cut.setSpeedTweak(true, xBar);
      cut.setDoubleBasics(true, xBar); // Only if we have heat containment
      assertEquals(1.0, cut.getHeatDissipationModifier(), 0.0);

      cut.setHeatContainment(false, xBar);
      cut.setSpeedTweak(false, xBar);
      cut.setDoubleBasics(false, xBar);

      // These do
      cut.setCoolRun(true, xBar);
      assertEquals(1.075, cut.getHeatDissipationModifier(), 0.0);
      cut.setDoubleBasics(true, xBar);
      assertEquals(1.15, cut.getHeatDissipationModifier(), 0.0);
   }

   @Test
   public void testSetHasHeatContainment() throws Exception{
      // Default false
      assertEquals(false, cut.hasHeatContainment());
      verifyZeroInteractions(xBar);

      // We want messages too!
      for(boolean b : new boolean[] {true, false}){
         cut.setHeatContainment(b, xBar);
         assertEquals(b, cut.hasHeatContainment());
         verify(xBar).post(new Efficiencies.Message(cut, Type.Changed));
         reset(xBar);
      }

      // No messages if there was no change.
      for(boolean b : new boolean[] {true, false}){
         cut.setHeatContainment(b, xBar);
         reset(xBar);
         cut.setHeatContainment(b, xBar);
         verifyZeroInteractions(xBar);
      }
   }

   @Test
   public void testGetHeatCapacityModifier() throws Exception{
      assertEquals(1.0, cut.getHeatCapacityModifier(), 0.0);

      // These don't affect heat capacity
      cut.setCoolRun(true, xBar);
      cut.setSpeedTweak(true, xBar);
      cut.setDoubleBasics(true, xBar); // Only if we have heat containment
      assertEquals(1.0, cut.getHeatCapacityModifier(), 0.0);

      cut.setCoolRun(false, xBar);
      cut.setSpeedTweak(false, xBar);
      cut.setDoubleBasics(false, xBar);

      // These do
      cut.setHeatContainment(true, xBar);
      assertEquals(1.1, cut.getHeatCapacityModifier(), 0.0);
      cut.setDoubleBasics(true, xBar);
      assertEquals(1.2, cut.getHeatCapacityModifier(), 0.0);
   }

   @Test
   public void testSetHasDoubleBasics() throws Exception{
      // Default false
      assertEquals(false, cut.hasDoubleBasics());
      verifyZeroInteractions(xBar);

      // We want messages too!
      for(boolean b : new boolean[] {true, false}){
         cut.setDoubleBasics(b, xBar);
         assertEquals(b, cut.hasDoubleBasics());
         verify(xBar).post(new Efficiencies.Message(cut, Type.Changed));
         reset(xBar);
      }

      // No messages if there was no change.
      for(boolean b : new boolean[] {true, false}){
         cut.setDoubleBasics(b, xBar);
         reset(xBar);
         cut.setDoubleBasics(b, xBar);
         verifyZeroInteractions(xBar);
      }
   }

   @Test
   public void testSetHasFastFire() throws Exception{
      // Default false
      assertEquals(false, cut.hasFastFire());
      verifyZeroInteractions(xBar);

      // We want messages too!
      for(boolean b : new boolean[] {true, false}){
         cut.setFastFire(b, xBar);
         assertEquals(b, cut.hasFastFire());
         verify(xBar).post(new Efficiencies.Message(cut, Type.Changed));
         reset(xBar);
      }

      // No messages if there was no change.
      for(boolean b : new boolean[] {true, false}){
         cut.setFastFire(b, xBar);
         reset(xBar);
         cut.setFastFire(b, xBar);
         verifyZeroInteractions(xBar);
      }
   }

   @Test
   public void testGetWeaponCycletimeModifier() throws Exception{
      assertEquals(1.0, cut.getWeaponCycleTimeModifier(), 0.0);

      cut.setFastFire(true, xBar);

      assertEquals(0.95, cut.getWeaponCycleTimeModifier(), 0.0);

      // Double basics doesn't affect cycle time
      cut.setDoubleBasics(true, xBar);
      assertEquals(0.95, cut.getWeaponCycleTimeModifier(), 0.0);
   }

}
