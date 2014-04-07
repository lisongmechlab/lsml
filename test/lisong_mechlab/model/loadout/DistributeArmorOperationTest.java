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
import lisong_mechlab.model.chassi.ArmorSide;
import lisong_mechlab.model.chassi.ChassiDB;
import lisong_mechlab.model.chassi.Part;
import lisong_mechlab.model.item.ItemDB;
import lisong_mechlab.model.loadout.export.Base64LoadoutCoder;
import lisong_mechlab.model.loadout.part.AddItemOperation;
import lisong_mechlab.model.loadout.part.LoadoutPart;
import lisong_mechlab.model.loadout.part.SetArmorOperation;
import lisong_mechlab.util.DecodingException;
import lisong_mechlab.util.MessageXBar;
import lisong_mechlab.util.OperationStack;

import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

/**
 * Test suite for {@link DistributeArmorOperation}.
 * 
 * @author Emily Björk
 */
@RunWith(MockitoJUnitRunner.class)
public class DistributeArmorOperationTest{
   @Mock
   MessageXBar    xBar;

   OperationStack stack = new OperationStack(0);

   @BeforeClass
   static public void setup(){
      // Make sure everything is parsed so that we don't cause a timeout due to game-file parsing
      ItemDB.lookup("LRM 20");
      ChassiDB.lookup("AS7-D-DC");
   }

   /**
    * The operator shall succeed at placing the armor points somewhere on an empty loadout.
    */
   @Test(timeout = 6000)
   public void testArmorDistributor_Distribute(){
      // Setup
      Loadout loadout = new Loadout(ChassiDB.lookup("HGN-733C"), xBar); // 90 tons, 9 tons internals

      // Execute (10 tons of armor)
      DistributeArmorOperation cut = new DistributeArmorOperation(loadout, 32 * 10, 1.0, xBar);
      stack.pushAndApply(cut);

      // Verify
      assertEquals(9.0 + 10, loadout.getMass(), 0.0);
      assertEquals(320, loadout.getArmor());
   }

   /**
    * The operator shall not barf if there is not enough free tonnage to accommodate the request. It shall allocate as
    * much as possible to fill the loadout.
    */
   @Test(timeout = 6000)
   public void testArmorDistributor_NotEnoughTonnage(){
      // Setup
      Loadout loadout = new Loadout(ChassiDB.lookup("LCT-3M"), xBar);
      stack.pushAndApply(new AddItemOperation(xBar, loadout.getPart(Part.RightArm), ItemDB.lookup("ER PPC")));
      stack.pushAndApply(new AddItemOperation(xBar, loadout.getPart(Part.CenterTorso), ItemDB.lookup("STD ENGINE 190")));

      // Execute
      DistributeArmorOperation cut = new DistributeArmorOperation(loadout, 138, 1.0, xBar);
      stack.pushAndApply(cut);

      // Verify
      assertTrue(loadout.getFreeMass() < loadout.getUpgrades().getArmor().getArmorMass(1));
      assertTrue(32 < loadout.getArmor());
   }

   /**
    * The operator shall not barf if there is not enough free tonnage to accommodate the request. It shall allocate as
    * much as possible to fill the loadout.
    * 
    * @throws DecodingException
    */
   @Test(timeout = 6000)
   public void testArmorDistributor_NotEnoughTonnage2() throws DecodingException{
      // Setup
      Base64LoadoutCoder coder = new Base64LoadoutCoder(null);
      Loadout loadout = coder.parse("lsml://rRsAkAtICFASaw1ICFALuihsfxmYtWt+nq0w9U1oz8oflBb6erRaKQ==");
      for(LoadoutPart part : loadout.getPartLoadOuts()){
         if( part.getInternalPart().getType().isTwoSided() ){
            stack.pushAndApply(new SetArmorOperation(null, part, ArmorSide.FRONT, part.getArmor(ArmorSide.FRONT), false));
         }
         else{
            stack.pushAndApply(new SetArmorOperation(null, part, ArmorSide.ONLY, part.getArmorTotal(), false));
         }
      }

      // Execute
      DistributeArmorOperation cut = new DistributeArmorOperation(loadout, 500, 8.0, xBar);
      stack.pushAndApply(cut);

      // Verify
      assertTrue(loadout.getFreeMass() < loadout.getUpgrades().getArmor().getArmorMass(1));
   }

   /**
    * If the budget given is larger than can be assigned due to manually set parts, the operator shall assign max armor
    * to the remaining parts.
    */
   @Test(timeout = 6000)
   public void testArmorDistributor_RespectManual_TooBigBudget(){
      // Setup
      Loadout loadout = new Loadout(ChassiDB.lookup("HGN-733C"), xBar); // 90 tons, 9 tons internals
      stack.pushAndApply(new SetArmorOperation(xBar, loadout.getPart(Part.LeftLeg), ArmorSide.ONLY, 64, true));
      stack.pushAndApply(new SetArmorOperation(xBar, loadout.getPart(Part.RightLeg), ArmorSide.ONLY, 64, true));

      // Execute
      DistributeArmorOperation cut = new DistributeArmorOperation(loadout, 558, 1.0, xBar);
      stack.pushAndApply(cut);

      // Verify
      assertEquals(558 - 2 * (76 - 64), loadout.getArmor());
   }

   /**
    * The operator shall not barf if the manually set armors take up more than the available budget.
    */
   @Test(timeout = 6000)
   public void testArmorDistributor_RespectManual_NegativeBudget(){
      // Setup
      Loadout loadout = new Loadout(ChassiDB.lookup("HGN-733C"), xBar); // 90 tons, 9 tons internals
      stack.pushAndApply(new SetArmorOperation(xBar, loadout.getPart(Part.LeftLeg), ArmorSide.ONLY, 64, true));
      stack.pushAndApply(new SetArmorOperation(xBar, loadout.getPart(Part.RightLeg), ArmorSide.ONLY, 64, true));

      // Execute
      DistributeArmorOperation cut = new DistributeArmorOperation(loadout, 32, 1.0, xBar);
      stack.pushAndApply(cut);

      // Verify
      assertEquals(9.0 + 4.0, loadout.getMass(), 0.0);
      assertEquals(128, loadout.getArmor());
   }

   /**
    * The operator shall take already existing armor amounts into account when deciding on how much armor to distribute.
    */
   @Test(timeout = 6000)
   public void testArmorDistributor_RespectManual_CorrectTotal(){
      // Setup
      Loadout loadout = new Loadout(ChassiDB.lookup("HGN-733C"), xBar); // 90 tons, 9 tons internals
      stack.pushAndApply(new SetArmorOperation(xBar, loadout.getPart(Part.LeftLeg), ArmorSide.ONLY, 70, true));
      stack.pushAndApply(new SetArmorOperation(xBar, loadout.getPart(Part.RightLeg), ArmorSide.ONLY, 70, true));

      // Execute (15 tons of armor, would be 76 on each leg if they weren't manual)
      DistributeArmorOperation cut = new DistributeArmorOperation(loadout, 480, 1.0, xBar);
      stack.pushAndApply(cut);

      // Verify
      assertEquals(9.0 + 15, loadout.getMass(), 0.0);
      assertEquals(480, loadout.getArmor());

      assertEquals(70, loadout.getPart(Part.LeftLeg).getArmorTotal());
      assertEquals(70, loadout.getPart(Part.RightLeg).getArmorTotal());
   }

   /**
    * The operator shall not add armor to manually assigned locations.
    */
   @Test(timeout = 6000)
   public void testArmorDistributor_RespectManual_DoNotAdd(){
      // Setup
      Loadout loadout = new Loadout(ChassiDB.lookup("HGN-733C"), xBar); // 90 tons, 9 tons internals
      stack.pushAndApply(new SetArmorOperation(xBar, loadout.getPart(Part.LeftLeg), ArmorSide.ONLY, 70, true));
      stack.pushAndApply(new SetArmorOperation(xBar, loadout.getPart(Part.RightLeg), ArmorSide.ONLY, 70, true));

      // Execute (15 tons of armor, would be 76 on each leg if they weren't manual)
      DistributeArmorOperation cut = new DistributeArmorOperation(loadout, 480, 1.0, xBar);
      stack.pushAndApply(cut);

      // Verify
      assertEquals(9.0 + 15, loadout.getMass(), 0.0);
      assertEquals(480, loadout.getArmor());

      assertEquals(70, loadout.getPart(Part.LeftLeg).getArmorTotal());
      assertEquals(70, loadout.getPart(Part.RightLeg).getArmorTotal());
   }

   /**
    * The operator shall not remove armor from manually assigned locations.
    */
   @Test(timeout = 6000)
   public void testArmorDistributor_RespectManual_DoNotRemove(){
      // Setup
      Loadout loadout = new Loadout(ChassiDB.lookup("HGN-733C"), xBar); // 90 tons, 9 tons internals
      stack.pushAndApply(new SetArmorOperation(xBar, loadout.getPart(Part.LeftLeg), ArmorSide.ONLY, 70, true));
      stack.pushAndApply(new SetArmorOperation(xBar, loadout.getPart(Part.RightLeg), ArmorSide.ONLY, 70, true));

      // Execute (5 tons of armor, would be 47 on each leg if they weren't manual)
      DistributeArmorOperation cut = new DistributeArmorOperation(loadout, 160, 1.0, xBar);
      stack.pushAndApply(cut);

      // Verify
      assertEquals(9.0 + 5, loadout.getMass(), 0.0);
      assertEquals(160, loadout.getArmor());

      assertEquals(70, loadout.getPart(Part.LeftLeg).getArmorTotal());
      assertEquals(70, loadout.getPart(Part.RightLeg).getArmorTotal());
   }

   /**
    * The operator shall respect the front-back ratio.
    */
   @Test(timeout = 6000)
   public void testArmorDistributor_FrontBackRatio(){
      // Setup
      final double frontBackRatio = 5.0;
      Loadout loadout = new Loadout(ChassiDB.lookup("HGN-733C"), xBar); // 90 tons, 9 tons internals

      // Execute (10 tons of armor)
      DistributeArmorOperation cut = new DistributeArmorOperation(loadout, 32 * 10, frontBackRatio, xBar);
      stack.pushAndApply(cut);

      // Verify
      int front = loadout.getPart(Part.CenterTorso).getArmor(ArmorSide.FRONT);
      int back = loadout.getPart(Part.CenterTorso).getArmor(ArmorSide.BACK);

      int tolerance = 1;
      double lb = (double)(front - tolerance) / (back + tolerance);
      double ub = (double)(front + tolerance) / (back - tolerance);

      assertTrue(lb < frontBackRatio);
      assertTrue(ub > frontBackRatio);
   }

   /**
    * Values that are even half tons shall not be rounded down.
    */
   @Test(timeout = 6000)
   public void testArmorDistributor_EvenHalfNoRoundDown(){
      // Setup
      Loadout loadout = new Loadout(ChassiDB.lookup("HGN-733C"), xBar); // 90 tons, 9 tons internals

      // Execute (10.75 tons of armor)
      DistributeArmorOperation cut = new DistributeArmorOperation(loadout, 32 * 10 + 16, 1.0, xBar);
      stack.pushAndApply(cut);

      // Verify
      assertEquals(9.0 + 10.5, loadout.getMass(), 0.0);
      assertEquals(336, loadout.getArmor());
   }

   /**
    * Values that are not even half tons shall be rounded down.
    */
   @Test(timeout = 6000)
   public void testArmorDistributor_RoundDown(){
      // Setup
      Loadout loadout = new Loadout(ChassiDB.lookup("HGN-733C"), xBar); // 90 tons, 9 tons internals

      // Execute (10.75 tons of armor)
      DistributeArmorOperation cut = new DistributeArmorOperation(loadout, 32 * 10 + 16 + 8, 1.0, xBar);
      stack.pushAndApply(cut);

      // Verify
      double evenHalfTons = (int)(loadout.getMass() * 2.0) / 2.0;
      double diffTons = loadout.getMass() - evenHalfTons;
      assertTrue(Math.abs(diffTons) < loadout.getUpgrades().getArmor().getArmorMass(1));
   }

   /**
    * Old armor values on automatically managed parts should be cleared
    */
   @Test(timeout = 6000)
   public void testArmorDistributor_ClearOld(){
      // Setup
      Loadout loadout = new Loadout(ChassiDB.lookup("CPLT-A1"), xBar); // 65 tons, 6.5 tons internals
      stack.pushAndApply(new SetArmorOperation(xBar, loadout.getPart(Part.LeftArm), ArmorSide.ONLY, 2, false));
      stack.pushAndApply(new SetArmorOperation(xBar, loadout.getPart(Part.RightArm), ArmorSide.ONLY, 2, false));

      // Execute (6.5 tons of armor)
      DistributeArmorOperation cut = new DistributeArmorOperation(loadout, 208, 1.0, xBar);
      stack.pushAndApply(cut);

      // Verify
      assertEquals(6.5 + 6.5, loadout.getMass(), 0.0);
      assertEquals(208, loadout.getArmor());
   }

   /**
    * Shield arms should get lower priority.
    */
   @Test(timeout = 6000)
   public void testArmorDistributor_ShieldArm(){
      // Setup
      Loadout loadout = new Loadout(ChassiDB.lookup("BNC-3S"), xBar); // 95 tons, 9.5 tons internals
      stack.pushAndApply(new AddItemOperation(xBar, loadout.getPart(Part.LeftArm), ItemDB.lookup("PPC")));

      // Execute (10.0 tons of armor)
      DistributeArmorOperation cut = new DistributeArmorOperation(loadout, 32 * 10, 1.0, xBar);
      stack.pushAndApply(cut);

      // Verify
      LoadoutPart shieldArm = loadout.getPart(Part.RightArm);
      LoadoutPart weaponArm = loadout.getPart(Part.LeftArm);

      assertTrue(shieldArm.getArmorTotal() < weaponArm.getArmorTotal() / 2);
   }

   /**
    * The operation shall succeed even if there is already max armor on the mech. (More on front parts than rear)
    */
   @Test(timeout = 6000)
   public void testArmorDistributor_AlreadyMaxArmor_FrontRear(){
      // Setup
      Loadout loadout = new Loadout(ChassiDB.lookup("HGN-733C"), xBar); // 90 tons, 9 tons internals
      stack.pushAndApply(new SetMaxArmorOperation(loadout, xBar, 5.0, false));

      // Execute (10.0 tons of armor)
      DistributeArmorOperation cut = new DistributeArmorOperation(loadout, 32 * 10, 1.0, xBar);
      stack.pushAndApply(cut);

      // Verify
      assertEquals(9.0 + 10.0, loadout.getMass(), 0.0);
      assertEquals(320, loadout.getArmor());
   }

   /**
    * The operation shall succeed even if there is already max armor on the mech. (More on rear parts than front)
    */
   @Test(timeout = 6000)
   public void testArmorDistributor_AlreadyMaxArmor_RearFront(){
      // Setup
      Loadout loadout = new Loadout(ChassiDB.lookup("HGN-733C"), xBar); // 90 tons, 9 tons internals
      stack.pushAndApply(new SetMaxArmorOperation(loadout, xBar, 0.2, false));

      // Execute (10.0 tons of armor)
      DistributeArmorOperation cut = new DistributeArmorOperation(loadout, 32 * 10, 1.0, xBar);
      stack.pushAndApply(cut);

      // Verify
      assertEquals(9.0 + 10.0, loadout.getMass(), 0.0);
      assertEquals(320, loadout.getArmor());
   }
}
