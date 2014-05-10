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
package lisong_mechlab.model.loadout.component;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import lisong_mechlab.model.chassi.ArmorSide;
import lisong_mechlab.model.chassi.ChassisDB;
import lisong_mechlab.model.chassi.Location;
import lisong_mechlab.model.loadout.Loadout;
import lisong_mechlab.model.loadout.component.ConfiguredComponent.Message.Type;
import lisong_mechlab.util.MessageXBar;
import lisong_mechlab.util.OperationStack;
import lisong_mechlab.util.OperationStack.Operation;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.runners.MockitoJUnitRunner;

/**
 * Test suite for {@link OpSetArmorSymmetric}.
 * 
 * @author Emily Björk
 */
@RunWith(MockitoJUnitRunner.class)
public class SymmetricArmorOperationTest{
   @Mock
   MessageXBar    xBar;

   OperationStack stack = new OperationStack(2);

   /**
    * Two operations can coalescele if they refer to the same (equality is not enough) component or the opposing
    * component, same side and have the same manual status.
    */
   @Test
   public void testCanCoalescele(){
      Loadout loadout = new Loadout(ChassisDB.lookup("AS7-D-DC"), null);
      ConfiguredComponent left = loadout.getPart(Location.LeftTorso);
      ConfiguredComponent right = loadout.getPart(Location.RightTorso);
      ConfiguredComponent arm = loadout.getPart(Location.LeftArm);
      int amount = 40;

      OpSetArmorSymmetric cut1 = new OpSetArmorSymmetric(xBar, left, ArmorSide.BACK, amount, true);
      OpSetArmorSymmetric cut2 = new OpSetArmorSymmetric(xBar, left, ArmorSide.BACK, amount, false);
      OpSetArmorSymmetric cut3 = new OpSetArmorSymmetric(xBar, left, ArmorSide.BACK, amount - 1, true);
      OpSetArmorSymmetric cut4 = new OpSetArmorSymmetric(xBar, left, ArmorSide.FRONT, amount, true);
      OpSetArmorSymmetric cut5 = new OpSetArmorSymmetric(xBar, right, ArmorSide.BACK, amount, true);
      OpSetArmorSymmetric cut6 = new OpSetArmorSymmetric(xBar, arm, ArmorSide.BACK, amount, true);
      Operation operation = Mockito.mock(Operation.class);

      assertFalse(cut1.canCoalescele(operation)); // Wrong class
      assertFalse(cut1.canCoalescele(null)); // Null
      assertFalse(cut1.canCoalescele(cut1)); // Can't coalescele with self.
      assertFalse(cut1.canCoalescele(cut2)); // manual-ness
      assertTrue(cut1.canCoalescele(cut3)); // armor amount
      assertFalse(cut1.canCoalescele(cut4)); // Side of part
      assertTrue(cut1.canCoalescele(cut5)); // opposite part
      assertFalse(cut1.canCoalescele(cut6)); // Other part
   }

   @Test
   public void testApply(){
      Loadout loadout = new Loadout(ChassisDB.lookup("AS7-D-DC"), null);
      ConfiguredComponent left = loadout.getPart(Location.LeftTorso);
      ConfiguredComponent right = loadout.getPart(Location.RightTorso);
      ArmorSide side = ArmorSide.BACK;
      int amount = 40;
      boolean manual = true;
      OpSetArmorSymmetric cut = new OpSetArmorSymmetric(xBar, left, side, amount, manual);
      Mockito.reset(xBar);

      stack.pushAndApply(cut);

      assertFalse(left.allowAutomaticArmor());
      assertFalse(right.allowAutomaticArmor());
      assertEquals(amount, left.getArmor(side));
      assertEquals(amount, right.getArmor(side));
      Mockito.verify(xBar).post(new ConfiguredComponent.Message(left, Type.ArmorChanged));
      Mockito.verify(xBar).post(new ConfiguredComponent.Message(right, Type.ArmorChanged));
   }

   @Test
   public void testApply_OnlyOneSideChanges(){
      for(Location setSide : new Location[] {Location.LeftTorso, Location.RightTorso}){
         Loadout loadout = new Loadout(ChassisDB.lookup("AS7-D-DC"), null);
         ConfiguredComponent left = loadout.getPart(Location.LeftTorso);
         ConfiguredComponent right = loadout.getPart(Location.RightTorso);
         ArmorSide side = ArmorSide.BACK;
         int amount = 40;
         boolean manual = true;
         stack.pushAndApply(new OpSetArmor(null, loadout.getPart(setSide), side, amount, false));
         OpSetArmorSymmetric cut = new OpSetArmorSymmetric(xBar, left, side, amount, manual);
         Mockito.reset(xBar);

         stack.pushAndApply(cut);

         assertFalse(left.allowAutomaticArmor());
         assertFalse(right.allowAutomaticArmor());
         assertEquals(amount, left.getArmor(side));
         assertEquals(amount, right.getArmor(side));
         Mockito.verify(xBar).post(new ConfiguredComponent.Message(left, Type.ArmorChanged));
         Mockito.verify(xBar).post(new ConfiguredComponent.Message(right, Type.ArmorChanged));
      }
   }

   @Test(expected = IllegalArgumentException.class)
   public void testApply_NotSymmetric(){
      Loadout loadout = new Loadout(ChassisDB.lookup("AS7-D-DC"), null);
      ConfiguredComponent left = loadout.getPart(Location.Head);
      ArmorSide side = ArmorSide.BACK;
      int amount = 40;
      boolean manual = true;
      OpSetArmorSymmetric cut = new OpSetArmorSymmetric(xBar, left, side, amount, manual);

      stack.pushAndApply(cut);
   }
}
