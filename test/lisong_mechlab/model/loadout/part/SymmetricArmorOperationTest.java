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
package lisong_mechlab.model.loadout.part;

import static org.junit.Assert.*;
import lisong_mechlab.model.chassi.ArmorSide;
import lisong_mechlab.model.chassi.ChassiDB;
import lisong_mechlab.model.chassi.Part;
import lisong_mechlab.model.loadout.Loadout;
import lisong_mechlab.model.loadout.part.LoadoutPart.Message.Type;
import lisong_mechlab.util.MessageXBar;
import lisong_mechlab.util.OperationStack;
import lisong_mechlab.util.OperationStack.Operation;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.runners.MockitoJUnitRunner;

/**
 * Test suite for {@link SymmetricArmorOperation}.
 * 
 * @author Li Song
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
      Loadout loadout = new Loadout(ChassiDB.lookup("AS7-D-DC"), null);
      LoadoutPart left = loadout.getPart(Part.LeftTorso);
      LoadoutPart right = loadout.getPart(Part.RightTorso);
      LoadoutPart arm = loadout.getPart(Part.LeftArm);
      int amount = 40;

      SymmetricArmorOperation cut1 = new SymmetricArmorOperation(xBar, left, ArmorSide.BACK, amount, true);
      SymmetricArmorOperation cut2 = new SymmetricArmorOperation(xBar, left, ArmorSide.BACK, amount, false);
      SymmetricArmorOperation cut3 = new SymmetricArmorOperation(xBar, left, ArmorSide.BACK, amount - 1, true);
      SymmetricArmorOperation cut4 = new SymmetricArmorOperation(xBar, left, ArmorSide.FRONT, amount, true);
      SymmetricArmorOperation cut5 = new SymmetricArmorOperation(xBar, right, ArmorSide.BACK, amount, true);
      SymmetricArmorOperation cut6 = new SymmetricArmorOperation(xBar, arm, ArmorSide.BACK, amount, true);
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
      Loadout loadout = new Loadout(ChassiDB.lookup("AS7-D-DC"), null);
      LoadoutPart left = loadout.getPart(Part.LeftTorso);
      LoadoutPart right = loadout.getPart(Part.RightTorso);
      ArmorSide side = ArmorSide.BACK;
      int amount = 40;
      boolean manual = true;
      SymmetricArmorOperation cut = new SymmetricArmorOperation(xBar, left, side, amount, manual);
      Mockito.reset(xBar);

      stack.pushAndApply(cut);

      assertFalse(left.allowAutomaticArmor());
      assertFalse(right.allowAutomaticArmor());
      assertEquals(amount, left.getArmor(side));
      assertEquals(amount, right.getArmor(side));
      Mockito.verify(xBar).post(new LoadoutPart.Message(left, Type.ArmorChanged));
      Mockito.verify(xBar).post(new LoadoutPart.Message(right, Type.ArmorChanged));
   }

   @Test
   public void testApply_OnlyOneSideChanges(){
      for(Part setSide : new Part[] {Part.LeftTorso, Part.RightTorso}){
         Loadout loadout = new Loadout(ChassiDB.lookup("AS7-D-DC"), null);
         LoadoutPart left = loadout.getPart(Part.LeftTorso);
         LoadoutPart right = loadout.getPart(Part.RightTorso);
         ArmorSide side = ArmorSide.BACK;
         int amount = 40;
         boolean manual = true;
         stack.pushAndApply(new SetArmorOperation(null, loadout.getPart(setSide), side, amount, false));
         SymmetricArmorOperation cut = new SymmetricArmorOperation(xBar, left, side, amount, manual);
         Mockito.reset(xBar);

         stack.pushAndApply(cut);

         assertFalse(left.allowAutomaticArmor());
         assertFalse(right.allowAutomaticArmor());
         assertEquals(amount, left.getArmor(side));
         assertEquals(amount, right.getArmor(side));
         Mockito.verify(xBar).post(new LoadoutPart.Message(left, Type.ArmorChanged));
         Mockito.verify(xBar).post(new LoadoutPart.Message(right, Type.ArmorChanged));
      }
   }

   @Test(expected = IllegalArgumentException.class)
   public void testApply_NotSymmetric(){
      Loadout loadout = new Loadout(ChassiDB.lookup("AS7-D-DC"), null);
      LoadoutPart left = loadout.getPart(Part.Head);
      ArmorSide side = ArmorSide.BACK;
      int amount = 40;
      boolean manual = true;
      SymmetricArmorOperation cut = new SymmetricArmorOperation(xBar, left, side, amount, manual);

      stack.pushAndApply(cut);
   }
}
