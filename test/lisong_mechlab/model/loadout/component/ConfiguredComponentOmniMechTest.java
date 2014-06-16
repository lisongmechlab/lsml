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
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;

import java.util.ArrayList;
import java.util.List;

import lisong_mechlab.model.chassi.ComponentOmniMech;
import lisong_mechlab.model.chassi.HardPoint;
import lisong_mechlab.model.chassi.HardPointType;
import lisong_mechlab.model.chassi.OmniPod;
import lisong_mechlab.model.item.Internal;
import lisong_mechlab.model.item.Item;
import lisong_mechlab.model.item.ItemDB;
import lisong_mechlab.util.ArrayUtils;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Matchers;
import org.mockito.Mockito;

/**
 * Test suite for {@link ConfiguredComponentOmniMech}.
 * 
 * @author Emily Björk
 */
public class ConfiguredComponentOmniMechTest extends ConfiguredComponentBaseTest{

   protected ComponentOmniMech omniInternal;
   protected OmniPod           omniPod;
   protected boolean           missileBayDoors;
   protected List<HardPoint>   hardPoints = new ArrayList<>();

   @Before
   public void setup(){
      omniInternal = Mockito.mock(ComponentOmniMech.class);
      omniPod = Mockito.mock(OmniPod.class);
      internal = omniInternal;
      Mockito.when(internal.isAllowed(Matchers.any(Item.class))).thenReturn(true);
   }

   @Override
   protected ConfiguredComponentOmniMech makeDefaultCUT(){
      Mockito.when(internal.getLocation()).thenReturn(location);
      Mockito.when(internal.getSlots()).thenReturn(slots);
      Mockito.when(internal.getFixedItemSlots()).thenReturn(internalFixedSlots);
      Mockito.when(internal.getFixedItems()).thenReturn(internalFixedItems);
      Mockito.when(internal.getArmorMax()).thenReturn(maxArmor);
      if( null != omniPod ){
         Mockito.when(omniPod.hasMissileBayDoors()).thenReturn(missileBayDoors);
         Mockito.when(omniPod.getHardPoints()).thenReturn(hardPoints);
      }
      return new ConfiguredComponentOmniMech(omniInternal, autoArmor, omniPod);
   }

   @Test(expected = NullPointerException.class)
   public final void testCtor_NullOmniPod(){
      omniPod = null;
      makeDefaultCUT();
   }

   @Test
   public final void testCopyCtor(){
      ConfiguredComponentOmniMech cut = makeDefaultCUT();
      assertEquals(cut, new ConfiguredComponentOmniMech(cut));
   }

   @Test
   public void testIsAllowed_DynamicSlots(){
      Mockito.when(omniInternal.getDynamicArmorSlots()).thenReturn(2);
      Mockito.when(omniInternal.getDynamicStructureSlots()).thenReturn(3);

      Item internalItem = Mockito.mock(Internal.class);
      Mockito.when(internalItem.getNumCriticalSlots()).thenReturn(5);
      internalFixedItems.add(internalItem);
      internalFixedSlots = 5;

      int size = 2;
      slots = 2 + 3 + internalFixedSlots + size;

      Item item = Mockito.mock(Item.class);
      Mockito.when(item.getHardpointType()).thenReturn(HardPointType.NONE);

      Mockito.when(item.getNumCriticalSlots()).thenReturn(size);
      assertTrue(makeDefaultCUT().canAddItem(item));

      Mockito.when(item.getNumCriticalSlots()).thenReturn(size + 1);
      assertFalse(makeDefaultCUT().canAddItem(item));
   }

   @Test
   public void testIsAllowed_NoHardpoint(){
      Item item = Mockito.mock(Item.class);
      Mockito.when(item.getNumCriticalSlots()).thenReturn(1);
      Mockito.when(item.getHardpointType()).thenReturn(HardPointType.ENERGY);

      assertFalse(makeDefaultCUT().canAddItem(item));
   }

   @Test
   public void testIsAllowed_HasHardpoint(){
      Item item = Mockito.mock(Item.class);
      Mockito.when(item.getNumCriticalSlots()).thenReturn(1);
      Mockito.when(item.getHardpointType()).thenReturn(HardPointType.ENERGY);

      Mockito.when(omniPod.getHardPointCount(HardPointType.ENERGY)).thenReturn(1);
      hardPoints.add(new HardPoint(HardPointType.ENERGY));

      assertTrue(makeDefaultCUT().canAddItem(item));
   }

   @Test
   public void testIsAllowed_AllHardpointsTaken(){
      Item item = Mockito.mock(Item.class);
      Mockito.when(item.getNumCriticalSlots()).thenReturn(1);
      Mockito.when(item.getHardpointType()).thenReturn(HardPointType.ENERGY);

      Mockito.when(omniPod.getHardPointCount(HardPointType.ENERGY)).thenReturn(1);
      hardPoints.add(new HardPoint(HardPointType.ENERGY));
      ConfiguredComponentOmniMech cut = makeDefaultCUT();
      cut.addItem(item);

      assertFalse(cut.canAddItem(item));
   }

   @Test
   public final void testGetHardPointCount() throws Exception{
      Mockito.when(omniPod.getHardPointCount(HardPointType.MISSILE)).thenReturn(7);
      assertEquals(7, makeDefaultCUT().getHardPointCount(HardPointType.MISSILE));
   }

   @Test
   public final void testGetHardPoints() throws Exception{
      hardPoints.add(new HardPoint(HardPointType.ENERGY));
      hardPoints.add(new HardPoint(HardPointType.BALLISTIC));

      assertTrue(ArrayUtils.equalsUnordered(hardPoints, new ArrayList<>(makeDefaultCUT().getHardPoints())));
   }

   /**
    * When a large bore weapon such as any AC, Gauss or PPC is equipped, the LLA/HA should be removed.
    */
   @Test
   public final void testGetItemsFixed_LargeBoreEquipped(){
      Item LAA = ItemDB.LAA;
      Item HA = ItemDB.HA;
      Item UAA = ItemDB.UAA;
      internalFixedItems.clear();
      internalFixedItems.add(UAA);

      List<Item> omniPodFixed = new ArrayList<>();
      Mockito.when(omniPod.getFixedItems()).thenReturn(omniPodFixed);
      omniPodFixed.add(LAA);
      omniPodFixed.add(HA);

      Item largeBoreGun = Mockito.mock(Item.class);
      Mockito.when(omniInternal.shouldRemoveArmActuators(largeBoreGun)).thenReturn(true);

      ConfiguredComponentOmniMech cut = makeDefaultCUT();
      cut.addItem(largeBoreGun);

      List<Item> ans = cut.getItemsFixed();
      assertEquals(1, ans.size());
      assertSame(UAA, ans.remove(0));
   }

   /**
    * When a large bore weapon such as any AC, Gauss or PPC is fixed on the chassis, the LLA/HA should be removed.
    */
   @Test
   public final void testGetItemsFixed_LargeBoreFixed(){
      Item LAA = ItemDB.LAA;
      Item HA = ItemDB.HA;
      Item UAA = ItemDB.UAA;
      internalFixedItems.clear();
      internalFixedItems.add(UAA);
      
      List<Item> omniPodFixed = new ArrayList<>();
      Mockito.when(omniPod.getFixedItems()).thenReturn(omniPodFixed);
      omniPodFixed.add(LAA);
      omniPodFixed.add(HA);

      Item largeBoreGun = Mockito.mock(Item.class);
      Mockito.when(omniInternal.shouldRemoveArmActuators(largeBoreGun)).thenReturn(true);

      internalFixedItems.add(largeBoreGun);

      List<Item> ans = makeDefaultCUT().getItemsFixed();
      assertEquals(2, ans.size());
      assertTrue(ans.remove(UAA));
      assertTrue(ans.remove(largeBoreGun));
   }
   
   /**
    * When a large bore weapon such as any AC, Gauss or PPC is fixed on the chassis, the LLA/HA should be removed.
    */
   @Test
   public final void testGetItemsFixed_NoLargeBore(){
      Item LAA = ItemDB.LAA;
      Item HA = ItemDB.HA;
      Item UAA = ItemDB.UAA;
      internalFixedItems.clear();
      internalFixedItems.add(UAA);
      
      List<Item> omniPodFixed = new ArrayList<>();
      Mockito.when(omniPod.getFixedItems()).thenReturn(omniPodFixed);
      omniPodFixed.add(LAA);
      omniPodFixed.add(HA);

      Item smallBoreGun = Mockito.mock(Item.class);
      Mockito.when(omniInternal.shouldRemoveArmActuators(smallBoreGun)).thenReturn(false);

      internalFixedItems.add(smallBoreGun);

      List<Item> ans = makeDefaultCUT().getItemsFixed();
      assertEquals(4, ans.size());
      assertTrue(ans.remove(UAA));
      assertTrue(ans.remove(HA));
      assertTrue(ans.remove(LAA));
      assertTrue(ans.remove(smallBoreGun));
   }

   @Test
   public final void testHasMissileBayDoors() throws Exception{
      assertEquals(missileBayDoors, makeDefaultCUT().hasMissileBayDoors());
      missileBayDoors = !missileBayDoors;
      assertEquals(missileBayDoors, makeDefaultCUT().hasMissileBayDoors());
   }

   @Test
   public final void testGetOmniPod() throws Exception{
      assertSame(omniPod, makeDefaultCUT().getOmniPod());
   }

   @Test
   public final void testGetSlotsUsedFree_DynamicSlots(){
      Mockito.when(omniInternal.getDynamicArmorSlots()).thenReturn(2);
      Mockito.when(omniInternal.getDynamicStructureSlots()).thenReturn(3);

      assertEquals(5, makeDefaultCUT().getSlotsUsed());
      assertEquals(slots - 5, makeDefaultCUT().getSlotsFree());
   }

   @Test(expected = NullPointerException.class)
   public final void testSetOmniPod_Null() throws Exception{
      makeDefaultCUT().setOmniPod(null);
   }

   @Test
   public final void testSetGetOmniPod() throws Exception{
      ConfiguredComponentOmniMech cut = makeDefaultCUT();
      OmniPod omniPod2 = Mockito.mock(OmniPod.class);

      cut.setOmniPod(omniPod2);
      assertSame(omniPod2, cut.getOmniPod());
   }
}
