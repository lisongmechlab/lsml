package lisong_mechlab.model.loadout;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import lisong_mechlab.model.MessageXBar;
import lisong_mechlab.model.chassi.ArmorSide;
import lisong_mechlab.model.chassi.HardpointType;
import lisong_mechlab.model.chassi.InternalPart;
import lisong_mechlab.model.chassi.Part;
import lisong_mechlab.model.helpers.MockLoadoutContainer;
import lisong_mechlab.model.item.Internal;
import lisong_mechlab.model.item.Item;
import lisong_mechlab.model.item.ItemDB;
import lisong_mechlab.model.item.MissileWeapon;
import lisong_mechlab.model.loadout.LoadoutPart.Message.Type;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

public class LoadoutPartTest{
   @Mock
   MessageXBar          xBar;
   MockLoadoutContainer mlc = new MockLoadoutContainer();

   @Mock
   InternalPart         part;

   @Before
   public void setup(){
      MockitoAnnotations.initMocks(this);

      when(mlc.loadout.getNumCriticalSlotsFree()).thenReturn(20);
      when(mlc.chassi.getMassMax()).thenReturn(100);
   }

   @After
   public void tearDown(){
      // We do not allow spurious messages on the crossbar!
      verifyNoMoreInteractions(xBar);
   }

   private LoadoutPart makeCUT(int max_armor, Part type, int numCritSlots){
      List<Item> internals = new ArrayList<>();
      return makeCUT(internals, max_armor, type, numCritSlots);
   }

   /**
    * Creates a {@link LoadoutPart} and verifies the initial state.
    * 
    * @param internals
    * @param max_armor
    * @param type
    * @param numCritSlots
    * @return
    */
   private LoadoutPart makeCUT(List<Item> internals, int max_armor, Part type, int numCritSlots){
      when(part.getNumCriticalslots()).thenReturn(numCritSlots);
      when(part.getArmorMax()).thenReturn(max_armor);
      when(part.getInternalItems()).thenReturn(internals);
      when(part.getType()).thenReturn(type);

      int usedCrits = 0;
      for(Item i : internals){
         usedCrits += i.getNumCriticalSlots();
      }

      // Execute
      LoadoutPart cut = new LoadoutPart(mlc.loadout, part, xBar);
      verify(xBar).attach(cut);

      // Verify default state
      assertSame(part, cut.getInternalPart());
      assertEquals(numCritSlots - usedCrits, cut.getNumCriticalSlotsFree());
      assertEquals(usedCrits, cut.getNumCriticalSlotsUsed());
      if( type.isTwoSided() ){
         assertEquals(0, cut.getArmor(ArmorSide.FRONT));
         assertEquals(0, cut.getArmor(ArmorSide.BACK));
         assertEquals(max_armor, cut.getArmorMax(ArmorSide.FRONT));
         assertEquals(max_armor, cut.getArmorMax(ArmorSide.BACK));
      }
      else{
         assertEquals(0, cut.getArmor(ArmorSide.ONLY));
         assertEquals(max_armor, cut.getArmorMax(ArmorSide.ONLY));
      }
      assertEquals(0, cut.getArmorTotal());
      assertEquals(internals, cut.getItems());
      assertEquals(0.0, cut.getItemMass(), 0.0);
      assertEquals(0, cut.getNumEngineHeatsinksMax());

      return cut;
   }

   /**
    * Tests construction of a loadout part for CT. (Double sided armor and some internals)
    */
   @Test
   public void testLoadoutPart_CT(){
      // Setup
      Internal gyro = mock(Internal.class);
      when(gyro.getNumCriticalSlots()).thenReturn(4);
      when(gyro.getMass()).thenReturn(0.0);

      makeCUT(Arrays.asList((Item)gyro), 31, Part.CenterTorso, 12);
   }

   /**
    * Tests construction of a loadout part for CT. (Double sided armor and some internals)
    */
   @Test
   public void testLoadoutPart_LL(){
      // Setup
      Internal hip = mock(Internal.class);
      Internal ula = mock(Internal.class);
      Internal lla = mock(Internal.class);
      Internal fa = mock(Internal.class);
      when(hip.getNumCriticalSlots()).thenReturn(1);
      when(hip.getMass()).thenReturn(0.0);
      when(ula.getNumCriticalSlots()).thenReturn(1);
      when(ula.getMass()).thenReturn(0.0);
      when(lla.getNumCriticalSlots()).thenReturn(1);
      when(lla.getMass()).thenReturn(0.0);
      when(fa.getNumCriticalSlots()).thenReturn(1);
      when(fa.getMass()).thenReturn(0.0);
      List<Item> internals = Arrays.asList((Item)hip, (Item)ula, (Item)lla, (Item)fa);

      makeCUT(internals, 31, Part.LeftLeg, 12);
   }

   @Test
   public void testGetNumCriticalSlotsUsed() throws Exception{
      // Setup
      MissileWeapon srm = (MissileWeapon)ItemDB.lookup("STREAK SRM 2");
      MissileWeapon lrm_artemis = (MissileWeapon)ItemDB.lookup("LRM5");

      when(mlc.upgrades.hasArtemis()).thenReturn(true);
      when(part.getNumHardpoints(HardpointType.MISSILE)).thenReturn(2);

      LoadoutPart cut = makeCUT(0, Part.LeftTorso, 12);
      cut.addItem(lrm_artemis);
      cut.addItem(srm);
      verify(xBar, times(2)).post(new LoadoutPart.Message(cut, Type.ItemAdded));

      // Execute & verify
      assertEquals(3, cut.getNumCriticalSlotsUsed());
   }

   @Test(expected = IllegalArgumentException.class)
   public void testAddItem_fail_nomessage() throws Exception{
      LoadoutPart cut = makeCUT(0, Part.LeftTorso, 12);

      cut.addItem("AC/20");
   }

   @Test
   public void testAddItem_success() throws Exception{
      LoadoutPart cut = makeCUT(0, Part.LeftTorso, 12);

      cut.addItem("AC/20 AMMO");

      verify(xBar).post(new LoadoutPart.Message(cut, Type.ItemAdded));
      assertTrue(cut.getItems().contains(ItemDB.lookup("AC/20 AMMO")));
   }

   @Test
   public void testAddItem_jumpJetsBadPart() throws Exception{
      for(Part part : new Part[] {Part.LeftArm, Part.RightArm, Part.Head}){
         LoadoutPart cut = makeCUT(0, part, 12);
         when(mlc.loadout.getJumpJetCount()).thenReturn(0);
         when(mlc.chassi.getMaxJumpJets()).thenReturn(5);
         try{
            cut.addItem("JUMP JETS - CLASS V");
            fail("Expected exception!");
         }
         catch( Exception e ){
            // success
         }
      }
   }

   /**
    * Engine heat sinks will behave like regular items added to the component with the exception that their slots will
    * not count towards the critical slots used by the component.
    */
   @Test
   public void testAddItem_EngineHeatsink() throws Exception{
      Item[] items = new Item[] {ItemDB.SHS, ItemDB.DHS};
      for(Item i : items){
         Internal gyro = mock(Internal.class);
         when(gyro.getNumCriticalSlots()).thenReturn(4);
         when(gyro.getMass()).thenReturn(0.0);
         LoadoutPart cut = makeCUT(Arrays.asList((Item)gyro), 0, Part.CenterTorso, 12);
         when(mlc.upgrades.hasDoubleHeatSinks()).thenReturn(i == ItemDB.DHS);
         when(mlc.chassi.getEngineMax()).thenReturn(400);
         when(mlc.chassi.getEngineMin()).thenReturn(100);
         cut.addItem("STD ENGINE 400");

         // Execute
         cut.addItem(i);

         // Verify
         assertEquals(1, cut.getNumEngineHeatsinks());
         assertTrue(cut.getItems().contains(i));
         assertEquals(10, cut.getNumCriticalSlotsUsed());

         // Execute
         cut.addItem(i);
         cut.addItem(i);
         cut.addItem(i);
         cut.addItem(i);
         cut.addItem(i);

         // Verify
         assertEquals(6, cut.getNumEngineHeatsinks());
         assertEquals(10, cut.getNumCriticalSlotsUsed());

         if( i == ItemDB.DHS ){
            // Execute
            try{
               cut.addItem(i);
               fail();
            }
            catch( Exception e ){
               // Success
            }

            // Verify (internal slots all occupied)
            assertEquals(6, cut.getNumEngineHeatsinks());
            assertEquals(10, cut.getNumCriticalSlotsUsed());

            verify(xBar, times(7)).post(new LoadoutPart.Message(cut, Type.ItemAdded));
         }
         else{
            // Execute
            cut.addItem(i);

            // Verify (internal slots all occupied)
            assertEquals(6, cut.getNumEngineHeatsinks());
            assertEquals(11, cut.getNumCriticalSlotsUsed());

            verify(xBar, times(8)).post(new LoadoutPart.Message(cut, Type.ItemAdded));
         }
      }
   }

   @Test
   public void testAddItem_CASE_side_torso() throws Exception{
      for(Part part : new Part[] {Part.LeftTorso, Part.RightTorso}){
         LoadoutPart cut = makeCUT(0, part, 12);

         cut.addItem("C.A.S.E.");

         verify(xBar).post(new LoadoutPart.Message(cut, Type.ItemAdded));
         assertTrue(cut.getItems().contains(ItemDB.lookup("C.A.S.E.")));
      }
   }

   @Test
   public void testAddItem_CASE_invalid() throws Exception{
      for(Part part : new Part[] {Part.LeftArm, Part.LeftLeg, Part.CenterTorso, Part.Head, Part.RightArm, Part.RightLeg}){
         LoadoutPart cut = makeCUT(0, part, 12);

         try{
            cut.addItem("C.A.S.E.");
            fail(); // No hardpoints
         }
         catch( Exception e ){
            // Success
         }
         assertFalse(cut.getItems().contains(ItemDB.lookup("C.A.S.E.")));
      }
   }

   /**
    * {@link LoadoutPart#canAddItem(Item)} shall return false if the {@link Loadout} doesn't have enough free slots.
    * 
    * @throws Exception
    */
   @Test
   public void testCanAddItem_TooFewSlots() throws Exception{
      LoadoutPart cut = makeCUT(0, Part.LeftTorso, 12);
      when(mlc.loadout.getNumCriticalSlotsFree()).thenReturn(ItemDB.BAP.getNumCriticalSlots() - 1);

      assertFalse(cut.canAddItem(ItemDB.BAP));
   }

   @Test
   public void testRemoveItem_success() throws Exception{
      LoadoutPart cut = makeCUT(0, Part.LeftTorso, 12);
      Item item = ItemDB.lookup("AC/20 AMMO");

      cut.addItem(item);
      cut.removeItem(item);

      verify(xBar).post(new LoadoutPart.Message(cut, Type.ItemAdded));
      verify(xBar).post(new LoadoutPart.Message(cut, Type.ItemRemoved));
   }

   @Test
   public void testRemoveItem_nosuchitem() throws Exception{
      LoadoutPart cut = makeCUT(0, Part.LeftTorso, 12);
      Item item = ItemDB.lookup("AC/20 AMMO");

      cut.removeItem(item);
   }

   @Test
   public void testSetArmor() throws Exception{
      LoadoutPart cut = makeCUT(0, Part.LeftTorso, 12);
      when(part.getArmorMax()).thenReturn(20);

      cut.setArmor(ArmorSide.FRONT, 10);

      assertEquals(10, cut.getArmor(ArmorSide.FRONT));
      assertEquals(0, cut.getArmor(ArmorSide.BACK));
      verify(xBar).post(new LoadoutPart.Message(cut, Type.ArmorChanged));
   }

   /**
    * {@link LoadoutPart#setArmor(ArmorSide, int)} shall throw an {@link IllegalArgumentException} if the armor amount
    * is above the max for the component.
    * 
    * @throws Exception
    */
   @Test(expected = IllegalArgumentException.class)
   public void testSetArmor_tooMuch() throws Exception{
      int max = 10;
      LoadoutPart cut = makeCUT(max, Part.LeftTorso, 12);

      cut.setArmor(ArmorSide.FRONT, max + 1);
   }

   /**
    * {@link LoadoutPart#setArmor(ArmorSide, int)} shall throw an {@link IllegalArgumentException} if there is not
    * enough free tons to add that amount of armor.
    * 
    * @throws Exception
    */
   @Test(expected = IllegalArgumentException.class)
   public void testSetArmor_notEnoughTons() throws Exception{
      int maxArmor = 20;
      LoadoutPart cut = makeCUT(maxArmor + 1, Part.LeftTorso, 12);
      when(mlc.loadout.getFreeMass()).thenReturn(-1.0);

      cut.setArmor(ArmorSide.FRONT, maxArmor + 1);
   }

   /**
    * Setting a armor value shall not throw regardless of previous value if new value is valid.
    */
   @Test
   public void testSetArmor_alreadyMax() throws Exception{
      int maxArmor = 20;
      LoadoutPart cut = makeCUT(maxArmor, Part.LeftTorso, 12);

      cut.setArmor(ArmorSide.FRONT, 5);
      cut.setArmor(ArmorSide.BACK, 10);
      cut.setArmor(ArmorSide.FRONT, 10);

      assertEquals(10, cut.getArmor(ArmorSide.FRONT));
      assertEquals(10, cut.getArmor(ArmorSide.BACK));
      verify(xBar, times(3)).post(new LoadoutPart.Message(cut, Type.ArmorChanged));
   }

}
