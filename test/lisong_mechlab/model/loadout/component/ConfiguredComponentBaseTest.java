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
package lisong_mechlab.model.loadout.component;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;

import java.util.ArrayList;
import java.util.List;

import lisong_mechlab.model.chassi.ArmorSide;
import lisong_mechlab.model.chassi.ComponentBase;
import lisong_mechlab.model.chassi.HardPointType;
import lisong_mechlab.model.chassi.Location;
import lisong_mechlab.model.item.Engine;
import lisong_mechlab.model.item.HeatSink;
import lisong_mechlab.model.item.Item;
import lisong_mechlab.model.item.ItemDB;
import lisong_mechlab.util.ArrayUtils;

import org.junit.Test;
import org.mockito.Mockito;

/**
 * Test suite for {@link ConfiguredComponentBase}.
 * 
 * @author Li Song
 */
public abstract class ConfiguredComponentBaseTest{
   protected int           slots              = 12;
   protected Location      location           = Location.LeftArm;
   protected ComponentBase internal           = null;
   protected boolean       autoArmor          = false;
   protected int           internalFixedSlots = 0;
   protected List<Item>    internalFixedItems = new ArrayList<>();
   protected int           maxArmor           = 32;

   protected abstract ConfiguredComponentBase makeDefaultCUT();

   /**
    * Simple items without any requirements are equippable.
    */
   @Test
   public final void testCanAddItem_Simple(){
      Item item = Mockito.mock(Item.class);
      Mockito.when(item.getHardpointType()).thenReturn(HardPointType.NONE);
      Mockito.when(item.getNumCriticalSlots()).thenReturn(1);

      assertTrue(makeDefaultCUT().canAddItem(item));
   }

   /**
    * No item is equippable if the internal component can't support it.
    */
   @Test
   public final void testCanAddItem_NoInternalSupport(){
      Item item = Mockito.mock(Item.class);
      Mockito.when(item.getHardpointType()).thenReturn(HardPointType.NONE);
      Mockito.when(item.getNumCriticalSlots()).thenReturn(1);

      Mockito.when(internal.isAllowed(item)).thenReturn(false);
      assertFalse(makeDefaultCUT().canAddItem(item));
   }

   /**
    * Item's are not equippable if there is no space for them
    */
   @Test
   public final void testCanAddItem_NoSpace(){
      // Fixed items setup
      internalFixedSlots = 2;
      Item fixed1 = Mockito.mock(Item.class);
      Mockito.when(fixed1.getNumCriticalSlots()).thenReturn(internalFixedSlots);
      internalFixedItems.add(fixed1);

      // Setup existing items in the component
      Item item1 = Mockito.mock(Item.class);
      Mockito.when(item1.getHardpointType()).thenReturn(HardPointType.NONE);
      Mockito.when(item1.getNumCriticalSlots()).thenReturn(slots / 4);

      int freeSlots = 2;
      Item item2 = Mockito.mock(Item.class);
      Mockito.when(item2.getHardpointType()).thenReturn(HardPointType.NONE);
      Mockito.when(item2.getNumCriticalSlots()).thenReturn(slots - slots / 4 - freeSlots - internalFixedSlots);

      ConfiguredComponentBase cut = makeDefaultCUT();
      cut.addItem(item1);
      cut.addItem(item2);

      // Item to add
      Item item = Mockito.mock(Item.class);
      Mockito.when(item.getHardpointType()).thenReturn(HardPointType.NONE);

      // Test tight fit.
      Mockito.when(item.getNumCriticalSlots()).thenReturn(freeSlots);
      assertTrue(cut.canAddItem(item));

      // Test too big
      Mockito.when(item.getNumCriticalSlots()).thenReturn(freeSlots + 1);
      assertFalse(cut.canAddItem(item));
   }

   /**
    * We do not allow two C.A.S.E. in the same component as that is just bonkers.
    */
   @Test
   public final void testCanAddItem_TwoCASE(){
      ConfiguredComponentBase cut = makeDefaultCUT();
      cut.addItem(ItemDB.CASE);
      assertFalse(cut.canAddItem(ItemDB.CASE));
   }

   /**
    * Having C.A.S.E. does not prohibit other items.
    */
   @Test
   public final void testCanAddItem_OneCASE(){
      ConfiguredComponentBase cut = makeDefaultCUT();
      cut.addItem(ItemDB.CASE);

      Item item = Mockito.mock(Item.class);
      Mockito.when(item.getHardpointType()).thenReturn(HardPointType.NONE);
      Mockito.when(item.getNumCriticalSlots()).thenReturn(1);

      assertTrue(cut.canAddItem(item));
   }

   /**
    * C.A.S.E. is allowed (provided internal component allows it).
    */
   @Test
   public final void testCanAddItem_CASEAllowed(){
      assertTrue(makeDefaultCUT().canAddItem(ItemDB.CASE));
   }

   @Test
   public final void testAddRemoveCanRemoveItem() throws Exception{
      ConfiguredComponentBase cut = makeDefaultCUT();
      assertFalse(cut.canRemoveItem(ItemDB.CASE));
      cut.addItem(ItemDB.CASE);
      assertTrue(cut.canRemoveItem(ItemDB.CASE));
      cut.removeItem(ItemDB.CASE);
      assertFalse(cut.canRemoveItem(ItemDB.CASE));
   }

   @Test
   public final void testAllowAutomaticArmor() throws Exception{
      assertEquals(autoArmor, makeDefaultCUT().allowAutomaticArmor());
      autoArmor = !autoArmor;
      assertEquals(autoArmor, makeDefaultCUT().allowAutomaticArmor());
   }

   @Test(expected = IllegalArgumentException.class)
   public final void testSetGetArmor_WrongSide() throws Exception{
      location = Location.LeftArm;
      makeDefaultCUT().setArmor(ArmorSide.FRONT, maxArmor / 2, !autoArmor);
   }

   @Test(expected = IllegalArgumentException.class)
   public final void testSetGetArmor_WrongSide2() throws Exception{
      location = Location.CenterTorso;
      makeDefaultCUT().setArmor(ArmorSide.ONLY, maxArmor / 2, !autoArmor);
   }

   @Test
   public final void testSetGetArmor_SingleSided() throws Exception{
      location = Location.LeftArm;

      ConfiguredComponentBase cut = makeDefaultCUT();
      assertEquals(0, cut.getArmor(ArmorSide.ONLY));
      cut.setArmor(ArmorSide.ONLY, maxArmor / 2, !autoArmor);

      assertEquals(!autoArmor, cut.allowAutomaticArmor());
      assertEquals(maxArmor / 2, cut.getArmor(ArmorSide.ONLY));
   }

   @Test
   public final void testSetGetArmor_DoubleSided() throws Exception{
      location = Location.CenterTorso;

      maxArmor = 2 * 2 * 10;
      ConfiguredComponentBase cut = makeDefaultCUT();
      assertEquals(0, cut.getArmor(ArmorSide.FRONT));
      assertEquals(0, cut.getArmor(ArmorSide.BACK));
      cut.setArmor(ArmorSide.FRONT, maxArmor / 2, !autoArmor);
      cut.setArmor(ArmorSide.BACK, maxArmor / 4, !autoArmor);

      assertEquals(!autoArmor, cut.allowAutomaticArmor());
      assertEquals(maxArmor / 2, cut.getArmor(ArmorSide.FRONT));
      assertEquals(maxArmor / 4, cut.getArmor(ArmorSide.BACK));
   }

   @Test(expected = IllegalArgumentException.class)
   public final void testGetArmorMax_WrongSide() throws Exception{
      location = Location.CenterTorso;
      maxArmor = 2 * 2 * 10;
      makeDefaultCUT().getArmorMax(ArmorSide.ONLY);
   }

   @Test(expected = IllegalArgumentException.class)
   public final void testGetArmorMax_WrongSide2() throws Exception{
      location = Location.LeftArm;
      maxArmor = 2 * 2 * 10;
      makeDefaultCUT().getArmorMax(ArmorSide.FRONT);
   }

   @Test
   public final void testGetArmorMax_SingleSided() throws Exception{
      location = Location.LeftArm;
      ConfiguredComponentBase cut = makeDefaultCUT();
      cut.setArmor(ArmorSide.ONLY, maxArmor / 2, autoArmor);
      assertEquals(maxArmor, cut.getArmorMax(ArmorSide.ONLY));
   }

   @Test
   public final void testGetArmorMax_DoubleSided() throws Exception{
      location = Location.CenterTorso;
      maxArmor = 2 * 2 * 2 * 2 * 2 * 2;
      ConfiguredComponentBase cut = makeDefaultCUT();
      cut.setArmor(ArmorSide.FRONT, maxArmor / 8, autoArmor);
      cut.setArmor(ArmorSide.BACK, maxArmor / 4, autoArmor);

      assertEquals(maxArmor - maxArmor / 4, cut.getArmorMax(ArmorSide.FRONT));
      assertEquals(maxArmor - maxArmor / 8, cut.getArmorMax(ArmorSide.BACK));
   }

   @Test
   public final void testGetArmorTotal_SingleSided() throws Exception{
      location = Location.LeftArm;
      ConfiguredComponentBase cut = makeDefaultCUT();
      cut.setArmor(ArmorSide.ONLY, maxArmor / 2, autoArmor);
      assertEquals(maxArmor / 2, cut.getArmorTotal());
   }

   @Test
   public final void testGetArmorTotal_DoubleSided() throws Exception{
      location = Location.CenterTorso;
      maxArmor = 4 * 10;
      ConfiguredComponentBase cut = makeDefaultCUT();
      cut.setArmor(ArmorSide.FRONT, maxArmor / 4, autoArmor);
      cut.setArmor(ArmorSide.BACK, 2 * maxArmor / 4, autoArmor);
      assertEquals(maxArmor * 3 / 4, cut.getArmorTotal());
   }

   @Test
   public final void testGetEngineHeatsinks() throws Exception{
      HeatSink fixed1 = Mockito.mock(HeatSink.class);
      internalFixedItems.add(fixed1);
      
      Engine fixed2 = Mockito.mock(Engine.class);
      Mockito.when(fixed2.getNumHeatsinkSlots()).thenReturn(3);
      internalFixedItems.add(fixed2);
      
      HeatSink item1 = Mockito.mock(HeatSink.class);
      Item item2 = Mockito.mock(Item.class);
      
      ConfiguredComponentBase cut = makeDefaultCUT();
      cut.addItem(item1);
      cut.addItem(item2);
      
      assertEquals(2, cut.getEngineHeatsinks());
   }
   
   @Test
   public final void testGetEngineHeatsinks_overflow() throws Exception{
      HeatSink fixed1 = Mockito.mock(HeatSink.class);
      internalFixedItems.add(fixed1);
      
      Engine fixed2 = Mockito.mock(Engine.class);
      Mockito.when(fixed2.getNumHeatsinkSlots()).thenReturn(2);
      internalFixedItems.add(fixed2);
      
      HeatSink item1 = Mockito.mock(HeatSink.class);
      Item item2 = Mockito.mock(Item.class);
      
      ConfiguredComponentBase cut = makeDefaultCUT();
      cut.addItem(item1);
      cut.addItem(item1);
      cut.addItem(item1);
      cut.addItem(item2);
      
      assertEquals(2, cut.getEngineHeatsinks());
   }

   @Test
   public final void testGetEngineHeatsinksMax_fixedEngine() throws Exception{
      Engine fixed1 = Mockito.mock(Engine.class);
      Mockito.when(fixed1.getNumHeatsinkSlots()).thenReturn(3);
      internalFixedItems.add(fixed1);
      
      Item fixed2 = Mockito.mock(Item.class);
      internalFixedItems.add(fixed2);

      assertEquals(3, makeDefaultCUT().getEngineHeatsinksMax());
   }

   @Test
   public final void testGetEngineHeatsinksMax_userEngine() throws Exception{     
      Item item1 = Mockito.mock(Item.class);
      Engine item2 = Mockito.mock(Engine.class);
      Mockito.when(item2.getNumHeatsinkSlots()).thenReturn(2);
      
      ConfiguredComponentBase cut = makeDefaultCUT();
      cut.addItem(item1);
      cut.addItem(item2);

      assertEquals(2, cut.getEngineHeatsinksMax());
   }   
   
   @Test
   public final void testGetInternalComponent() throws Exception{
      assertSame(internal, makeDefaultCUT().getInternalComponent());
   }

   @Test
   public final void testGetItemMass() throws Exception{
      Item fixed1 = Mockito.mock(Item.class);
      Mockito.when(fixed1.getMass()).thenReturn(2.0);
      internalFixedItems.add(fixed1);
      
      Item fixed2 = Mockito.mock(Item.class);
      Mockito.when(fixed2.getMass()).thenReturn(3.0);
      internalFixedItems.add(fixed2);
      
      Item item1 = Mockito.mock(Item.class);
      Mockito.when(item1.getMass()).thenReturn(5.0);
      Item item2 = Mockito.mock(Item.class);
      Mockito.when(item2.getMass()).thenReturn(7.0);
      
      ConfiguredComponentBase cut = makeDefaultCUT();
      cut.addItem(item1);
      cut.addItem(item2);
      
      assertEquals(17.0, cut.getItemMass(), 0.0);
   }

   @Test
   public final void testGetItemsEquipped() throws Exception{
      Item fixed1 = Mockito.mock(Item.class);
      Mockito.when(fixed1.getMass()).thenReturn(2.0);
      internalFixedItems.add(fixed1);
      
      Item fixed2 = Mockito.mock(Item.class);
      Mockito.when(fixed2.getMass()).thenReturn(3.0);
      internalFixedItems.add(fixed2);
      
      Item item1 = Mockito.mock(Item.class);
      Mockito.when(item1.getMass()).thenReturn(5.0);
      Item item2 = Mockito.mock(Item.class);
      Mockito.when(item2.getMass()).thenReturn(7.0);
      
      ConfiguredComponentBase cut = makeDefaultCUT();
      cut.addItem(item1);
      cut.addItem(item2);
      
      List<Item> ans = new ArrayList<>();
      ans.add(item1);
      ans.add(item2);
      
      assertTrue(ArrayUtils.equalsUnordered(ans, cut.getItemsEquipped()));
   }

   @Test
   public final void testGetItemsFixed() throws Exception{
      Item fixed1 = Mockito.mock(Item.class);
      Mockito.when(fixed1.getMass()).thenReturn(2.0);
      internalFixedItems.add(fixed1);
      
      Item fixed2 = Mockito.mock(Item.class);
      Mockito.when(fixed2.getMass()).thenReturn(3.0);
      internalFixedItems.add(fixed2);
      
      Item item1 = Mockito.mock(Item.class);
      Mockito.when(item1.getMass()).thenReturn(5.0);
      Item item2 = Mockito.mock(Item.class);
      Mockito.when(item2.getMass()).thenReturn(7.0);
      
      ConfiguredComponentBase cut = makeDefaultCUT();
      cut.addItem(item1);
      cut.addItem(item2);
      
      List<Item> ans = new ArrayList<>();
      ans.add(fixed1);
      ans.add(fixed2);
      
      assertTrue(ArrayUtils.equalsUnordered(ans, new ArrayList<>(cut.getItemsFixed())));
   }

   @Test
   public final void testGetItemsOfHardpointType() throws Exception{
      Item fixed1 = Mockito.mock(Item.class);
      Mockito.when(fixed1.getHardpointType()).thenReturn(HardPointType.ENERGY);
      internalFixedItems.add(fixed1);
      
      Item fixed2 = Mockito.mock(Item.class);
      Mockito.when(fixed2.getHardpointType()).thenReturn(HardPointType.BALLISTIC);
      internalFixedItems.add(fixed2);
      
      Item item1 = Mockito.mock(Item.class);
      Mockito.when(item1.getHardpointType()).thenReturn(HardPointType.ENERGY);
      Item item2 = Mockito.mock(Item.class);
      Mockito.when(item2.getHardpointType()).thenReturn(HardPointType.ENERGY);
      
      ConfiguredComponentBase cut = makeDefaultCUT();
      cut.addItem(item1);
      cut.addItem(item2);
      
      assertEquals(3, cut.getItemsOfHardpointType(HardPointType.ENERGY));
   }

   @Test
   public final void testGetSlotsFreeUsed() throws Exception{
      slots = 20;
      internalFixedSlots = 5;
      
      Item fixed1 = Mockito.mock(Item.class);
      Mockito.when(fixed1.getNumCriticalSlots()).thenReturn(2);
      internalFixedItems.add(fixed1);
      
      Item fixed2 = Mockito.mock(Item.class);
      Mockito.when(fixed2.getNumCriticalSlots()).thenReturn(3);
      internalFixedItems.add(fixed2);
      
      Item item1 = Mockito.mock(Item.class);
      Mockito.when(item1.getNumCriticalSlots()).thenReturn(5);
      Item item2 = Mockito.mock(Item.class);
      Mockito.when(item2.getNumCriticalSlots()).thenReturn(7);
      
      ConfiguredComponentBase cut = makeDefaultCUT();
      cut.addItem(item1);
      cut.addItem(item2);
      
      assertEquals(17, cut.getSlotsUsed());
      assertEquals(slots-17, cut.getSlotsFree());
   }
   
   @Test
   public void testGetSlotsFreeUsed_EngineHS(){
      Engine engine = Mockito.mock(Engine.class);
      Mockito.when(engine.getNumCriticalSlots()).thenReturn(7);
      Mockito.when(engine.getNumHeatsinkSlots()).thenReturn(2);

      HeatSink heatSink = Mockito.mock(HeatSink.class);
      Mockito.when(heatSink.getNumCriticalSlots()).thenReturn(3);

      ConfiguredComponentBase cut = makeDefaultCUT();
      cut.addItem(engine);
      cut.addItem(heatSink);
      cut.addItem(heatSink);
      cut.addItem(heatSink);
      
      assertEquals(10, cut.getSlotsUsed());
      assertEquals(slots - 10, cut.getSlotsFree());
   }
}
