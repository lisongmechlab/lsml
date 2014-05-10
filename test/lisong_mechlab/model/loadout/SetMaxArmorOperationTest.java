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
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.atLeast;
import static org.mockito.Mockito.reset;
import static org.mockito.Mockito.verify;
import lisong_mechlab.model.chassi.ArmorSide;
import lisong_mechlab.model.chassi.ChassisDB;
import lisong_mechlab.model.chassi.InternalComponent;
import lisong_mechlab.model.loadout.component.ConfiguredComponent;
import lisong_mechlab.util.MessageXBar;
import lisong_mechlab.util.OperationStack;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

/**
 * Test suite for {@link OpSetMaxArmor}.
 * 
 * @author Emily Björk
 */
@RunWith(MockitoJUnitRunner.class)
public class SetMaxArmorOperationTest{

   @Mock
   private MessageXBar          xBar;

   private final OperationStack stack = new OperationStack(0);

   @Test
   public void testApply(){
      // Setup
      Loadout cut = new Loadout(ChassisDB.lookup("AS7-D-DC"), xBar);
      final double front_back_ratio = 3.0 / 2.0;
      final int tolerance = 1;

      // Execute
      stack.pushAndApply(new OpSetMaxArmor(cut, xBar, front_back_ratio, true));

      // Verify
      // All parts have max armor
      for(InternalComponent part : cut.getChassi().getInternalParts()){
         assertEquals(part.getArmorMax(), cut.getPart(part.getLocation()).getArmorTotal());

         // Double sided parts have a ratio of 3 : 2 armor between front and back.
         if( part.getLocation().isTwoSided() ){
            int front = cut.getPart(part.getLocation()).getArmor(ArmorSide.FRONT);
            int back = cut.getPart(part.getLocation()).getArmor(ArmorSide.BACK);

            double lb = (double)(front - tolerance) / (back + tolerance);
            double ub = (double)(front + tolerance) / (back - tolerance);

            assertTrue(lb < front_back_ratio);
            assertTrue(ub > front_back_ratio);

            verify(xBar, atLeast(2)).post(new ConfiguredComponent.Message(cut.getPart(part.getLocation()),
                                                                          ConfiguredComponent.Message.Type.ArmorChanged));
         }
         else
            verify(xBar).post(new ConfiguredComponent.Message(cut.getPart(part.getLocation()), ConfiguredComponent.Message.Type.ArmorChanged));
      }
   }

   @Test
   public void testApply_alreadyMaxArmor(){
      // Setup
      Loadout cut = new Loadout(ChassisDB.lookup("AS7-D-DC"), xBar);
      final double front_back_ratio = 3.0 / 2.0;
      final int tolerance = 1;
      stack.pushAndApply(new OpSetMaxArmor(cut, xBar, 1.0, true));
      reset(xBar);

      // Execute
      stack.pushAndApply(new OpSetMaxArmor(cut, xBar, front_back_ratio, true));

      // Verify
      // All parts have max armor
      for(InternalComponent part : cut.getChassi().getInternalParts()){
         assertEquals(part.getArmorMax(), cut.getPart(part.getLocation()).getArmorTotal());

         // Double sided parts have a ratio of 3 : 2 armor between front and back.
         if( part.getLocation().isTwoSided() ){
            int front = cut.getPart(part.getLocation()).getArmor(ArmorSide.FRONT);
            int back = cut.getPart(part.getLocation()).getArmor(ArmorSide.BACK);

            double lb = (double)(front - tolerance) / (back + tolerance);
            double ub = (double)(front + tolerance) / (back - tolerance);

            assertTrue(lb < front_back_ratio);
            assertTrue(ub > front_back_ratio);
         }
      }
   }
}
