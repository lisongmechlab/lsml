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

import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import java.util.ArrayList;
import java.util.List;

import lisong_mechlab.model.chassi.ChassiDB;
import lisong_mechlab.model.chassi.Part;
import lisong_mechlab.model.item.Item;
import lisong_mechlab.model.item.ItemDB;
import lisong_mechlab.model.loadout.part.AddItemOperation;
import lisong_mechlab.model.loadout.part.LoadoutPart;
import lisong_mechlab.model.loadout.part.LoadoutPart.Message.Type;
import lisong_mechlab.model.upgrades.SetHeatSinkTypeOperation;
import lisong_mechlab.model.upgrades.UpgradeDB;
import lisong_mechlab.util.MessageXBar;
import lisong_mechlab.util.OperationStack;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

/**
 * Test suite for {@link AutoAddItemOperation}.
 * 
 * @author Emily Björk
 */
@RunWith(MockitoJUnitRunner.class)
public class AutoAddItemOperationTest{
   @Mock
   private MessageXBar    xBar;

   private OperationStack stack = new OperationStack(0);

   /**
    * {@link AutoAddItemOperation} shall throw an {@link IllegalArgumentException} if the item cannot be auto added on any permutation of the loadout.
    */
   @Test(expected=IllegalArgumentException.class)
   public void testMoveItem_NotPossible(){
      // Setup
      Loadout loadout = new Loadout(ChassiDB.lookup("AS7-D-DC"), xBar);
      stack.pushAndApply(new SetHeatSinkTypeOperation(xBar, loadout, UpgradeDB.DOUBLE_HEATSINKS));

      // 2 slots in either leg
      // 2 slots left in CT
      stack.pushAndApply(new AddItemOperation(xBar, loadout.getPart(Part.CenterTorso), ItemDB.lookup("XL ENGINE 200")));
      
      // 2 slots left on right arm, cannot contain DHS
      stack.pushAndApply(new AddItemOperation(xBar, loadout.getPart(Part.RightArm), ItemDB.DHS));
      stack.pushAndApply(new AddItemOperation(xBar, loadout.getPart(Part.RightArm), ItemDB.DHS));
      
      // 2 slots left on left arm, cannot contain DHS
      stack.pushAndApply(new AddItemOperation(xBar, loadout.getPart(Part.LeftArm), ItemDB.DHS));
      stack.pushAndApply(new AddItemOperation(xBar, loadout.getPart(Part.LeftArm), ItemDB.DHS));

      // 6 slots left in right torso (3 taken by engine)
      stack.pushAndApply(new AddItemOperation(xBar, loadout.getPart(Part.RightTorso), ItemDB.DHS));
      
      // 0 slots left in left torso (3 taken by engine)
      stack.pushAndApply(new AddItemOperation(xBar, loadout.getPart(Part.LeftTorso), ItemDB.DHS));
      stack.pushAndApply(new AddItemOperation(xBar, loadout.getPart(Part.LeftTorso), ItemDB.DHS));
      stack.pushAndApply(new AddItemOperation(xBar, loadout.getPart(Part.LeftTorso), ItemDB.DHS));
      
      Item gaussRifle = ItemDB.lookup("GAUSS RIFLE");
      
      // Execute
      stack.pushAndApply(new AutoAddItemOperation(loadout, xBar, gaussRifle));
   }
   
   /**
    * {@link AutoAddItemOperation} shall try to move items in order to make room for the added item if there is no room
    * in any component with a hard point but there are items that could be moved to make room.
    */
   @Test
   public void testMoveItem(){
      // Setup
      Loadout loadout = new Loadout(ChassiDB.lookup("AS7-D-DC"), xBar);
      stack.pushAndApply(new SetHeatSinkTypeOperation(xBar, loadout, UpgradeDB.DOUBLE_HEATSINKS));
      stack.pushAndApply(new AddItemOperation(xBar, loadout.getPart(Part.RightTorso), ItemDB.DHS));
      stack.pushAndApply(new AddItemOperation(xBar, loadout.getPart(Part.RightTorso), ItemDB.DHS));
      Item gaussRifle = ItemDB.lookup("GAUSS RIFLE");
      
      // Execute
      stack.pushAndApply(new AutoAddItemOperation(loadout, xBar, gaussRifle));
      
      // Verify
      List<Item> allItems = new ArrayList<>(loadout.getAllItems());
      assertTrue(allItems.remove(ItemDB.DHS));
      assertTrue(allItems.remove(ItemDB.DHS));
      assertTrue(allItems.remove(gaussRifle));
      
      // Must be minimal change to allow the item in.
      loadout.getPart(Part.RightTorso).getItems().contains(ItemDB.DHS);
      loadout.getPart(Part.RightTorso).getItems().contains(gaussRifle);
   }

   /**
    * {@link AutoAddItemOperation} shall add an item to the first applicable slot in this loadout. Order the items are
    * added is: RA, RT, RL, HD, CT, LT, LL, LA
    */
   @Test
   public void testAddItem(){
      Loadout loadout = new Loadout(ChassiDB.lookup("AS7-D-DC"), xBar);
      stack.pushAndApply(new SetHeatSinkTypeOperation(xBar, loadout, UpgradeDB.DOUBLE_HEATSINKS));

      Item mlas = ItemDB.lookup("MEDIUM LASER");
      Item ac20 = ItemDB.lookup("AC/20");
      Item lrm5 = ItemDB.lookup("LRM 5");
      Item lrm15 = ItemDB.lookup("LRM 15");
      Item std250 = ItemDB.lookup("STD ENGINE 250");

      stack.pushAndApply(new AutoAddItemOperation(loadout, xBar, mlas));
      assertTrue(loadout.getPart(Part.RightArm).getItems().contains(mlas));

      stack.pushAndApply(new AutoAddItemOperation(loadout, xBar, mlas));
      assertTrue(loadout.getPart(Part.LeftArm).getItems().contains(mlas));

      stack.pushAndApply(new AutoAddItemOperation(loadout, xBar, ac20));
      assertTrue(loadout.getPart(Part.RightTorso).getItems().contains(ac20));

      stack.pushAndApply(new AutoAddItemOperation(loadout, xBar, lrm5));
      assertTrue(loadout.getPart(Part.LeftTorso).getItems().contains(lrm5));

      stack.pushAndApply(new AutoAddItemOperation(loadout, xBar, lrm15));
      assertTrue(loadout.getPart(Part.LeftTorso).getItems().contains(lrm15));

      stack.pushAndApply(new AutoAddItemOperation(loadout, xBar, std250));
      assertTrue(loadout.getPart(Part.CenterTorso).getItems().contains(std250));

      // Fill right arm
      stack.pushAndApply(new AutoAddItemOperation(loadout, xBar, ItemDB.DHS));
      stack.pushAndApply(new AutoAddItemOperation(loadout, xBar, ItemDB.DHS));
      assertTrue(loadout.getPart(Part.RightArm).getItems().contains(ItemDB.DHS));
      verify(xBar, times(1 + 2)).post(new LoadoutPart.Message(loadout.getPart(Part.RightArm), Type.ItemAdded));

      // Skips RA, RT, RL, HD, CT (too few slots) and places the item in LT
      stack.pushAndApply(new AutoAddItemOperation(loadout, xBar, ItemDB.DHS));
      assertTrue(loadout.getPart(Part.LeftTorso).getItems().contains(ItemDB.DHS));

      // Skips RA (too few slots) and places the item in RT
      stack.pushAndApply(new AutoAddItemOperation(loadout, xBar, ItemDB.BAP));
      assertTrue(loadout.getPart(Part.RightTorso).getItems().contains(ItemDB.BAP));
   }

   /**
    * {@link AutoAddItemOperation}shall prioritize engine slots for heat sinks
    */
   @Test
   public void testAddItem_engineHS(){
      Loadout loadout = new Loadout(ChassiDB.lookup("AS7-D-DC"), xBar);

      Item std300 = ItemDB.lookup("STD ENGINE 300");
      stack.pushAndApply(new AutoAddItemOperation(loadout, xBar, std300));
      assertTrue(loadout.getPart(Part.CenterTorso).getItems().contains(ItemDB.lookup("STD ENGINE 300")));

      stack.pushAndApply(new AutoAddItemOperation(loadout, xBar, ItemDB.SHS)); // Engine HS slot 1
      stack.pushAndApply(new AutoAddItemOperation(loadout, xBar, ItemDB.SHS)); // Engine HS slot 2
      stack.pushAndApply(new AutoAddItemOperation(loadout, xBar, ItemDB.SHS)); // Right arm

      verify(xBar, times(1 + 2)).post(new LoadoutPart.Message(loadout.getPart(Part.CenterTorso), Type.ItemAdded));
      assertTrue(loadout.getPart(Part.CenterTorso).getItems().contains(ItemDB.SHS)); // 1 remaining
      assertTrue(loadout.getPart(Part.RightArm).getItems().contains(ItemDB.SHS));
   }
}
