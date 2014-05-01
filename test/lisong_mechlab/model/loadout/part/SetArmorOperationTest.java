package lisong_mechlab.model.loadout.part;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.util.ArrayList;
import java.util.List;

import lisong_mechlab.model.chassi.ArmorSide;
import lisong_mechlab.model.chassi.InternalPart;
import lisong_mechlab.model.chassi.Part;
import lisong_mechlab.model.loadout.Loadout;
import lisong_mechlab.model.loadout.part.LoadoutPart.Message.Type;
import lisong_mechlab.model.upgrades.UpgradeDB;
import lisong_mechlab.model.upgrades.Upgrades;
import lisong_mechlab.util.MessageXBar;
import lisong_mechlab.util.OperationStack.Operation;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InOrder;
import org.mockito.Matchers;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.runners.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class SetArmorOperationTest{
   private static final int TEST_MAX_ARMOR = 30;
   private ArmorSide        armorSide      = ArmorSide.ONLY;
   @Mock
   private Loadout          loadout;
   @Mock
   private Upgrades         upgrades;
   @Mock
   private LoadoutPart      loadoutPart;
   @Mock
   private MessageXBar      xBar;
   @Mock
   private InternalPart     internalPart;

   @Before
   public void setup(){
      Mockito.when(loadout.getUpgrades()).thenReturn(upgrades);
      Mockito.when(loadoutPart.getLoadout()).thenReturn(loadout);
      Mockito.when(loadoutPart.getInternalPart()).thenReturn(internalPart);
      Mockito.when(loadoutPart.getArmor(armorSide)).thenReturn(TEST_MAX_ARMOR);
      Mockito.when(internalPart.getType()).thenReturn(Part.CenterTorso);
      Mockito.when(internalPart.getArmorMax()).thenReturn(TEST_MAX_ARMOR);
   }

   /**
    * The description shall contain the words "armor" and "change".
    * 
    * @throws Exception
    */
   @Test
   public final void testDescribe() throws Exception{
      int armor = 13;
      SetArmorOperation cut = new SetArmorOperation(xBar, loadoutPart, armorSide, armor, true);

      assertTrue(cut.describe().contains("armor"));
      assertTrue(cut.describe().contains("change"));
   }

   /**
    * Any attempt to create an {@link SetArmorOperation} with negative armor shall throw an
    * {@link IllegalArgumentException} on creation.
    * 
    * @throws Exception
    */
   @Test(expected = IllegalArgumentException.class)
   public final void testCtorNegativeArmor() throws Exception{
      new SetArmorOperation(xBar, loadoutPart, armorSide, -1, true);
   }

   /**
    * Any attempt to create a {@link SetArmorOperation} with more armor than the internal component can hold (100%
    * failure regardless of current armor value) shall throw an {@link IllegalArgumentException} on construction.
    * 
    * @throws Exception
    */
   @Test(expected = IllegalArgumentException.class)
   public final void testCtorTooMuchArmor() throws Exception{
      new SetArmorOperation(xBar, loadoutPart, armorSide, TEST_MAX_ARMOR + 1, true);
   }

   /**
    * Two set armor operations can coalescele if they refer to the same (equality is not enough) component, same side
    * and have the same manual status.
    * 
    * @throws Exception
    */
   @Test
   public final void testCanCoalescele() throws Exception{
      int armor = 20;
      LoadoutPart part1 = Mockito.mock(LoadoutPart.class);
      LoadoutPart part2 = Mockito.mock(LoadoutPart.class);

      // Part 1 & 2 are identical but not the same.
      Mockito.when(part1.getLoadout()).thenReturn(loadout);
      Mockito.when(part1.getInternalPart()).thenReturn(internalPart);
      Mockito.when(part1.getArmor(ArmorSide.BACK)).thenReturn(armor);
      Mockito.when(part1.getArmor(ArmorSide.FRONT)).thenReturn(armor);
      Mockito.when(part2.getLoadout()).thenReturn(loadout);
      Mockito.when(part2.getInternalPart()).thenReturn(internalPart);
      Mockito.when(part2.getArmor(ArmorSide.BACK)).thenReturn(armor);
      Mockito.when(part2.getArmor(ArmorSide.FRONT)).thenReturn(armor);
      Mockito.when(internalPart.getType()).thenReturn(Part.CenterTorso);
      Mockito.when(internalPart.getArmorMax()).thenReturn(TEST_MAX_ARMOR);

      SetArmorOperation cut1 = new SetArmorOperation(xBar, part1, ArmorSide.FRONT, armor, true);
      SetArmorOperation cut2 = new SetArmorOperation(xBar, part1, ArmorSide.FRONT, armor, false);
      SetArmorOperation cut3 = new SetArmorOperation(xBar, part1, ArmorSide.BACK, armor, true);
      SetArmorOperation cut4 = new SetArmorOperation(xBar, part2, ArmorSide.FRONT, armor, true);
      SetArmorOperation cut5 = new SetArmorOperation(xBar, part1, ArmorSide.FRONT, armor - 1, true);
      Operation operation = Mockito.mock(Operation.class);

      assertFalse(cut1.canCoalescele(operation));
      assertFalse(cut1.canCoalescele(null));
      assertFalse(cut1.canCoalescele(cut1)); // Can't coalescele with self.
      assertFalse(cut1.canCoalescele(cut2));
      assertFalse(cut1.canCoalescele(cut3));
      assertFalse(cut1.canCoalescele(cut4));
      assertTrue(cut1.canCoalescele(cut5));
   }

   /**
    * Attempting to add armor that would cause the mass limit on the loadout to be exceeded shall result in an
    * {@link IllegalArgumentException} when the operation is applied.
    * <p>
    * It must not be thrown on creation as there may be a composite operation that will be executed before this one that
    * reduces the armor so that this operation will succeed.
    */
   @Test(expected = IllegalArgumentException.class)
   public final void testApply_TooHeavy(){
      // Setup
      final double freeTons = 0.1;
      final int oldArmor = 20;
      final int newArmor = oldArmor + (int)(freeTons * 32) + 1;

      
      List<LoadoutPart> parts = new ArrayList<>();
      Mockito.when(upgrades.getArmor()).thenReturn(UpgradeDB.STANDARD_ARMOR);
      Mockito.when(loadout.getFreeMass()).thenReturn(freeTons);
      Mockito.when(loadout.getPartLoadOuts()).thenReturn(parts);
      Mockito.when(loadoutPart.getArmorMax(armorSide)).thenReturn(TEST_MAX_ARMOR);
      Mockito.when(loadoutPart.getArmor(armorSide)).thenReturn(oldArmor);
      SetArmorOperation cut = null;
      try{
         cut = new SetArmorOperation(xBar, loadoutPart, armorSide, newArmor, true);
      }
      catch( Throwable t ){
         fail("Setup threw!");
         return;
      }

      // Execute
      cut.apply();

      // Verify (automatic)
   }

   /**
    * A symmetric operation shall apply results to both sides.
    */
   @Test
   public final void testApply_Symm(){
      // Setup
      final int oldArmor = 20;
      final int newArmor = oldArmor;

      Mockito.when(loadout.getFreeMass()).thenReturn(100.0);
      Mockito.when(loadoutPart.getArmorMax(armorSide)).thenReturn(TEST_MAX_ARMOR);
      Mockito.when(loadoutPart.getArmor(armorSide)).thenReturn(oldArmor);
      SetArmorOperation cut = new SetArmorOperation(xBar, loadoutPart, armorSide, newArmor, true);

      // Execute
      cut.apply();

      Mockito.verifyZeroInteractions(xBar);
      Mockito.verify(loadoutPart, Mockito.never()).setArmor(Matchers.any(ArmorSide.class), Matchers.anyInt(), Matchers.anyBoolean());
   }

   /**
    * An armor operation that would result in no change shall not execute.
    */
   @Test
   public final void testApply_NoChange(){
      // Setup
      final int oldArmor = 20;
      final int newArmor = oldArmor;

      Mockito.when(loadout.getFreeMass()).thenReturn(100.0);
      Mockito.when(loadoutPart.getArmorMax(armorSide)).thenReturn(TEST_MAX_ARMOR);
      Mockito.when(loadoutPart.getArmor(armorSide)).thenReturn(oldArmor);
      SetArmorOperation cut = new SetArmorOperation(xBar, loadoutPart, armorSide, newArmor, true);

      // Execute
      cut.apply();

      Mockito.verifyZeroInteractions(xBar);
      Mockito.verify(loadoutPart, Mockito.never()).setArmor(Matchers.any(ArmorSide.class), Matchers.anyInt(), Matchers.anyBoolean());
   }

   /**
    * Setting the armor up-to and including the maximum free tonnage shall succeed. Shall take ferro fibrous into
    * account too.
    */
   @Test
   public final void testApply_NotTooHeavy_ferro(){
      // Setup
      final double freeTons = 0.1;
      final int oldArmor = 20;
      final int newArmor = oldArmor + (int)(freeTons * 32 * 1.12);

      Mockito.when(upgrades.getArmor()).thenReturn(UpgradeDB.FERRO_FIBROUS_ARMOR);
      Mockito.when(loadout.getFreeMass()).thenReturn(freeTons);
      Mockito.when(loadoutPart.getArmorMax(armorSide)).thenReturn(TEST_MAX_ARMOR);
      Mockito.when(loadoutPart.getArmor(armorSide)).thenReturn(oldArmor);
      SetArmorOperation cut = null;
      try{
         cut = new SetArmorOperation(xBar, loadoutPart, armorSide, newArmor, true);
      }
      catch( Throwable t ){
         fail("Setup threw!");
         return;
      }

      // Execute
      cut.apply();

      // Verify
      Mockito.verify(loadoutPart).setArmor(armorSide, newArmor, false);
      Mockito.verify(xBar).post(new LoadoutPart.Message(loadoutPart, Type.ArmorChanged));
   }

   /**
    * Attempting to set armor that is more than the side can support (but less than free tonnage) shall fail with an
    * {@link IllegalArgumentException}.
    */
   @Test(expected = IllegalArgumentException.class)
   public final void testApply_TooMuchArmorForSide(){
      // Setup
      final int newArmor = 20;

      Mockito.when(loadout.getFreeMass()).thenReturn(100.0);
      Mockito.when(loadoutPart.getArmorMax(armorSide)).thenReturn(newArmor - 1);
      SetArmorOperation cut = null;
      try{
         cut = new SetArmorOperation(xBar, loadoutPart, armorSide, newArmor, true);
      }
      catch( Throwable t ){
         fail("Setup threw!");
         return;
      }

      // Execute
      cut.apply();
   }

   /**
    * Apply shall successfully change the armor value if called with an armor amount less than the current amount and
    * the 'mech is over-tonnage.
    * 
    * @throws Exception
    */
   @Test
   public void testApply_ReduceWhenOverTonnage() throws Exception{

      // Setup
      final double freeTons = -0.1; // Over-tonned
      final int oldArmor = 20;
      final int newArmor = 1;

      Mockito.when(upgrades.getArmor()).thenReturn(UpgradeDB.STANDARD_ARMOR);
      Mockito.when(loadout.getFreeMass()).thenReturn(freeTons);
      Mockito.when(loadoutPart.getArmorMax(armorSide)).thenReturn(TEST_MAX_ARMOR);
      Mockito.when(loadoutPart.getArmor(armorSide)).thenReturn(oldArmor);
      SetArmorOperation cut = null;
      try{
         cut = new SetArmorOperation(xBar, loadoutPart, armorSide, newArmor, true);
      }
      catch( Throwable t ){
         fail("Setup threw!");
         return;
      }

      // Execute
      cut.apply();

      // Verify
      Mockito.verify(loadoutPart).setArmor(armorSide, newArmor, false);
      Mockito.verify(xBar).post(new LoadoutPart.Message(loadoutPart, Type.ArmorChanged));
   }

   /**
    * Undoing an operation where the new value is the old value shall not do anything.
    */
   @Test
   public final void testUndo_NoChange(){
      // Setup
      int newArmor = 20;
      int oldArmor = 20;

      Mockito.when(loadout.getFreeMass()).thenReturn(100.0);
      Mockito.when(loadoutPart.getArmorMax(armorSide)).thenReturn(TEST_MAX_ARMOR);
      Mockito.when(loadoutPart.getArmor(armorSide)).thenReturn(oldArmor);

      SetArmorOperation cut = new SetArmorOperation(xBar, loadoutPart, armorSide, newArmor, true);

      // Execute
      cut.apply();
      cut.undo();

      Mockito.verifyZeroInteractions(xBar);
      Mockito.verify(loadoutPart, Mockito.never()).setArmor(Matchers.any(ArmorSide.class), Matchers.anyInt(), Matchers.anyBoolean());

   }

   /**
    * Undoing when apply has not been called shall throw an instance of {@link RuntimeException}.
    */
   @Test(expected = RuntimeException.class)
   public final void testUndo_WithoutApply(){
      // Setup
      SetArmorOperation cut = null;
      try{
         int newArmor = 20;
         int oldArmor = 21;

         Mockito.when(loadout.getFreeMass()).thenReturn(100.0);
         Mockito.when(loadoutPart.getArmorMax(armorSide)).thenReturn(TEST_MAX_ARMOR);
         Mockito.when(loadoutPart.getArmor(armorSide)).thenReturn(oldArmor);

         cut = new SetArmorOperation(xBar, loadoutPart, armorSide, newArmor, true);

      }
      catch( Throwable t ){
         fail("Setup threw!");
         return;
      }
      // Execute
      cut.undo();
   }

   /**
    * Undoing twice to only one apply shall throw an instance of {@link RuntimeException}.
    */
   @Test(expected = RuntimeException.class)
   public final void testUndo_DoubleUndoAfterApply(){
      // Setup
      SetArmorOperation cut = null;
      try{
         int newArmor = 20;
         int oldArmor = 21;

         Mockito.when(loadout.getFreeMass()).thenReturn(100.0);
         Mockito.when(loadoutPart.getArmorMax(armorSide)).thenReturn(TEST_MAX_ARMOR);
         Mockito.when(loadoutPart.getArmor(armorSide)).thenReturn(oldArmor);

         cut = new SetArmorOperation(xBar, loadoutPart, armorSide, newArmor, true);

      }
      catch( Throwable t ){
         fail("Setup threw!");
         return;
      }

      // Execute
      cut.apply();
      cut.undo();
      cut.undo();
   }

   /**
    * Undoing the operation shall set the armor that was at the time of the operation was applied.
    */
   @Test
   public final void testUndo(){
      // Setup
      int newArmor = 20;
      int oldArmor = 25;

      Mockito.when(upgrades.getArmor()).thenReturn(UpgradeDB.STANDARD_ARMOR);
      Mockito.when(loadout.getFreeMass()).thenReturn(100.0);
      Mockito.when(loadoutPart.getArmorMax(armorSide)).thenReturn(TEST_MAX_ARMOR);
      Mockito.when(loadoutPart.getArmor(armorSide)).thenReturn(0);
      Mockito.when(loadoutPart.allowAutomaticArmor()).thenReturn(true);

      SetArmorOperation cut = new SetArmorOperation(xBar, loadoutPart, armorSide, newArmor, true);

      Mockito.when(loadoutPart.getArmor(armorSide)).thenReturn(oldArmor);

      // Execute
      cut.apply();
      cut.undo();

      InOrder inOrder = Mockito.inOrder(xBar, loadoutPart);
      inOrder.verify(loadoutPart).setArmor(armorSide, newArmor, false);
      inOrder.verify(xBar).post(new LoadoutPart.Message(loadoutPart, Type.ArmorChanged));
      inOrder.verify(loadoutPart).setArmor(armorSide, oldArmor, true);
      inOrder.verify(xBar).post(new LoadoutPart.Message(loadoutPart, Type.ArmorChanged));
   }
}
