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

import static org.junit.Assert.*;

import java.util.ArrayList;
import java.util.List;

import lisong_mechlab.model.chassi.ComponentOmniMech;
import lisong_mechlab.model.chassi.HardPoint;
import lisong_mechlab.model.chassi.HardPointType;
import lisong_mechlab.model.chassi.OmniPod;
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
 * @author Li Song
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
      Item LAA = ItemDB.lookup("@mdf_LAA");
      Item HA = ItemDB.lookup("@mdf_HA");
      Item UAA = ItemDB.lookup("@mdf_UAA");
      internalFixedItems.clear();
      internalFixedItems.add(UAA);
      internalFixedItems.add(LAA);
      internalFixedItems.add(HA);
      
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
      Item LAA = ItemDB.lookup("@mdf_LAA");
      Item HA = ItemDB.lookup("@mdf_HA");
      Item UAA = ItemDB.lookup("@mdf_UAA");
      internalFixedItems.clear();
      internalFixedItems.add(UAA);
      internalFixedItems.add(LAA);
      internalFixedItems.add(HA);
      
      Item largeBoreGun = Mockito.mock(Item.class);
      Mockito.when(omniInternal.shouldRemoveArmActuators(largeBoreGun)).thenReturn(true);
      
      internalFixedItems.add(largeBoreGun);
      
      List<Item> ans = makeDefaultCUT().getItemsFixed();
      assertEquals(2, ans.size());
      assertTrue(ans.remove(UAA));
      assertTrue(ans.remove(largeBoreGun));
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
