package lisong_mechlab.model.loadout;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import lisong_mechlab.model.MessageXBar;
import lisong_mechlab.model.chassi.ArmorSide;
import lisong_mechlab.model.chassi.Chassi;
import lisong_mechlab.model.chassi.HardpointType;
import lisong_mechlab.model.chassi.InternalPart;
import lisong_mechlab.model.chassi.Part;
import lisong_mechlab.model.item.Internal;
import lisong_mechlab.model.item.Item;
import lisong_mechlab.model.item.ItemDB;
import lisong_mechlab.model.item.MissileWeapon;
import lisong_mechlab.model.loadout.LoadoutPart.Message.Type;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

public class LoadoutPartTest{
   @Mock
   MessageXBar  xBar;
   @Mock
   Loadout      loadout;
   @Mock
   InternalPart part;

   @Before
   public void setup(){
      MockitoAnnotations.initMocks(this);
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
      LoadoutPart cut = new LoadoutPart(loadout, part, xBar);

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

      Upgrades upgrades = mock(Upgrades.class);
      when(upgrades.hasArtemis()).thenReturn(true);
      when(loadout.getUpgrades()).thenReturn(upgrades);

      Chassi chassi = mock(Chassi.class);
      when(chassi.getMassMax()).thenReturn(100);
      when(loadout.getChassi()).thenReturn(chassi);

      when(part.getNumHardpoints(HardpointType.MISSILE)).thenReturn(2);

      LoadoutPart cut = makeCUT(0, Part.LeftTorso, 12);
      cut.addItem(lrm_artemis);
      cut.addItem(srm);

      // Execute & verify
      assertEquals(3, cut.getNumCriticalSlotsUsed());
   }

   @Test
   public void testGetNumItemsOfHardpointType() throws Exception{
      throw new RuntimeException("not yet implemented");
   }

   @Test
   public void testGetItems() throws Exception{
      throw new RuntimeException("not yet implemented");
   }

   @Test
   public void testAddItemString() throws Exception{
      throw new RuntimeException("not yet implemented");
   }

   @Test
   public void testAddItem_fail_nomessage() throws Exception{
      LoadoutPart cut = makeCUT(0, Part.LeftTorso, 12);
      Chassi chassi = mock(Chassi.class);
      when(loadout.getChassi()).thenReturn(chassi);
      when(chassi.getMassMax()).thenReturn(100);
      reset(xBar);

      try{
         cut.addItem("AC/20");
         fail(); // No hardpoints
      }
      catch( Exception e ){
         // Success
      }

      verifyZeroInteractions(xBar); // No message sent
   }

   @Test
   public void testAddItem_success() throws Exception{
      LoadoutPart cut = makeCUT(0, Part.LeftTorso, 12);
      Chassi chassi = mock(Chassi.class);
      when(loadout.getChassi()).thenReturn(chassi);
      when(chassi.getMassMax()).thenReturn(100);
      reset(xBar);

      cut.addItem("AC/20 AMMO");

      verify(xBar).post(new LoadoutPart.Message(cut, Type.ItemAdded));
      assertTrue(cut.getItems().contains(ItemDB.lookup("AC/20 AMMO")));
   }

   /**
    * Engine heat sinks will behave like regular items added to the component with the exception that their slots will
    * not count towards the critical slots used by the component.
    */
   @Test
   public void testAddItem_EngineHeatsink() throws Exception{
      Item[] items = new Item[] {ItemDB.lookup("STD HEAT SINK"), ItemDB.lookup("DOUBLE HEAT SINK")};
      for(Item i : items){
         Internal gyro = mock(Internal.class);
         when(gyro.getNumCriticalSlots()).thenReturn(4);
         when(gyro.getMass()).thenReturn(0.0);
         LoadoutPart cut = makeCUT(Arrays.asList((Item)gyro), 0, Part.CenterTorso, 12);
         Chassi chassi = mock(Chassi.class);
         Upgrades upgrades = mock(Upgrades.class);
         when(loadout.getChassi()).thenReturn(chassi);
         when(loadout.getUpgrades()).thenReturn(upgrades);
         when(upgrades.hasDoubleHeatSinks()).thenReturn(i == ItemDB.lookup("DOUBLE HEAT SINK"));
         when(chassi.getEngineMax()).thenReturn(400);
         when(chassi.getEngineMin()).thenReturn(100);
         when(chassi.getMassMax()).thenReturn(100);
         cut.addItem("STD ENGINE 400");
         reset(xBar);

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

         if( i == ItemDB.lookup("DOUBLE HEAT SINK")){
            // Execute
            try{
               cut.addItem(i);
               fail();
            }catch(Exception e){
               // Success
            }
            
            // Verify (internal slots all occupied)
            assertEquals(6, cut.getNumEngineHeatsinks());
            assertEquals(10, cut.getNumCriticalSlotsUsed());

            verify(xBar, times(6)).post(new LoadoutPart.Message(cut, Type.ItemAdded));
         }
         else{
            // Execute
            cut.addItem(i);
            
            // Verify (internal slots all occupied)
            assertEquals(6, cut.getNumEngineHeatsinks());
            assertEquals(11, cut.getNumCriticalSlotsUsed());

            verify(xBar, times(7)).post(new LoadoutPart.Message(cut, Type.ItemAdded));            
         }
      }
   }

   @Test
   public void testAddItemItem() throws Exception{
      throw new RuntimeException("not yet implemented");
   }

   @Test
   public void testCanAddItem_CASE_side_torso() throws Exception{
      for(Part part : new Part[] {Part.LeftTorso, Part.RightTorso}){
         LoadoutPart cut = makeCUT(0, part, 12);
         Chassi chassi = mock(Chassi.class);
         when(loadout.getChassi()).thenReturn(chassi);
         when(chassi.getMassMax()).thenReturn(100);
         reset(xBar);

         cut.addItem("C.A.S.E.");

         verify(xBar).post(new LoadoutPart.Message(cut, Type.ItemAdded));
         assertTrue(cut.getItems().contains(ItemDB.lookup("C.A.S.E.")));
      }
   }

   @Test
   public void testCanAddItem_CASE_invalid() throws Exception{
      for(Part part : new Part[] {Part.LeftArm, Part.LeftLeg, Part.CenterTorso, Part.Head, Part.RightArm, Part.RightLeg}){
         LoadoutPart cut = makeCUT(0, part, 12);
         Chassi chassi = mock(Chassi.class);
         when(loadout.getChassi()).thenReturn(chassi);
         when(chassi.getMassMax()).thenReturn(100);
         reset(xBar);

         try{
            cut.addItem("C.A.S.E.");
            fail(); // No hardpoints
         }
         catch( Exception e ){
            // Success
         }

         verifyZeroInteractions(xBar);
         assertFalse(cut.getItems().contains(ItemDB.lookup("C.A.S.E.")));
      }
   }

   @Test
   public void testCanAddItem() throws Exception{
      throw new RuntimeException("not yet implemented");
   }

   @Test
   public void testRemoveItem_success() throws Exception{
      LoadoutPart cut = makeCUT(0, Part.LeftTorso, 12);
      Chassi chassi = mock(Chassi.class);
      when(loadout.getChassi()).thenReturn(chassi);
      when(chassi.getMassMax()).thenReturn(100);
      Item item = ItemDB.lookup("AC/20 AMMO");
      cut.addItem(item);
      reset(xBar);

      cut.removeItem(item);

      verify(xBar).post(new LoadoutPart.Message(cut, Type.ItemRemoved));
   }

   @Test
   public void testRemoveItem_nosuchitem() throws Exception{
      LoadoutPart cut = makeCUT(0, Part.LeftTorso, 12);
      Item item = ItemDB.lookup("AC/20 AMMO");
      reset(xBar);

      cut.removeItem(item);

      verifyZeroInteractions(xBar);
   }

   @Test
   public void testRemoveAllItems() throws Exception{
      throw new RuntimeException("not yet implemented");
   }

   @Test
   public void testSetArmor() throws Exception{
      LoadoutPart cut = makeCUT(0, Part.LeftTorso, 12);
      when(part.getArmorMax()).thenReturn(20);
      reset(xBar);

      cut.setArmor(ArmorSide.FRONT, 10);

      assertEquals(10, cut.getArmor(ArmorSide.FRONT));
      assertEquals(0, cut.getArmor(ArmorSide.BACK));
      verify(xBar).post(new LoadoutPart.Message(cut, Type.ArmorChanged));
   }

   /**
    * Setting a armor value shall not throw regardless of previous value is new value is valid.
    */
   @Test
   public void testSetArmor_alreadyMax() throws Exception{
      LoadoutPart cut = makeCUT(0, Part.LeftTorso, 12);
      when(part.getArmorMax()).thenReturn(20);
      cut.setArmor(ArmorSide.FRONT, 5);
      cut.setArmor(ArmorSide.BACK, 10);
      reset(xBar);

      cut.setArmor(ArmorSide.FRONT, 10);

      assertEquals(10, cut.getArmor(ArmorSide.FRONT));
      assertEquals(10, cut.getArmor(ArmorSide.BACK));
      verify(xBar).post(new LoadoutPart.Message(cut, Type.ArmorChanged));
   }
   
   @Test
   public void testGetArmorTotal() throws Exception{
      throw new RuntimeException("not yet implemented");
   }

   @Test
   public void testGetArmor() throws Exception{
      throw new RuntimeException("not yet implemented");
   }

   @Test
   public void testGetArmorMax() throws Exception{
      throw new RuntimeException("not yet implemented");
   }

   @Test
   public void testGetItemMass() throws Exception{
      throw new RuntimeException("not yet implemented");
   }

   @Test
   public void testGetNumEngineHeatsinksMax() throws Exception{
      throw new RuntimeException("not yet implemented");
   }

}
