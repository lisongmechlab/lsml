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
package lisong_mechlab.model.chassi;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;
import lisong_mechlab.model.item.BallisticWeapon;
import lisong_mechlab.model.item.EnergyWeapon;
import lisong_mechlab.model.item.Item;
import lisong_mechlab.model.item.ItemDB;

import org.junit.Test;
import org.mockito.Mockito;

/**
 * Test suite for {@link ComponentOmniMech}.
 * 
 * @author Li Song
 */
public class ComponentOmniMechTest extends ComponentBaseTest{

   private OmniPod omniPod;
   private int     dynamicArmorSlots;
   private int     dynamicStructureSlots;

   @Override
   protected ComponentOmniMech makeDefaultCUT(){
      return new ComponentOmniMech(location, criticalSlots, hp, fixedItems, omniPod, dynamicStructureSlots, dynamicArmorSlots);
   }

   @Test
   public final void testHasFixedOmniPod() throws Exception{
      omniPod = null;
      assertFalse(makeDefaultCUT().hasFixedOmniPod());

      omniPod = Mockito.mock(OmniPod.class);
      assertTrue(makeDefaultCUT().hasFixedOmniPod());
   }

   @Test
   public final void testGetFixedOmniPod() throws Exception{
      omniPod = null;
      assertNull(null, makeDefaultCUT().getFixedOmniPod());

      omniPod = Mockito.mock(OmniPod.class);
      assertSame(omniPod, makeDefaultCUT().getFixedOmniPod());
   }

   @Test
   public final void testGetDynamicArmorSlots() throws Exception{
      dynamicArmorSlots = 3;
      assertEquals(dynamicArmorSlots, makeDefaultCUT().getDynamicArmorSlots());
   }

   @Test
   public final void testGetDynamicStructureSlots() throws Exception{
      dynamicStructureSlots = 3;
      assertEquals(dynamicStructureSlots, makeDefaultCUT().getDynamicStructureSlots());
   }

   /**
    * An item can't be too big considering the fixed dynamic slots and items.
    */
   @Test
   public final void testIsAllowed_fixedItemsAndSlots(){
      criticalSlots = 12;
      dynamicArmorSlots = 2;
      dynamicStructureSlots = 1;
      final int fixedSlots = 3;
      final int freeSlots = criticalSlots - dynamicArmorSlots - dynamicStructureSlots - fixedSlots;

      Item fixed = Mockito.mock(Item.class);
      Mockito.when(fixed.getNumCriticalSlots()).thenReturn(fixedSlots);

      fixedItems.clear();
      fixedItems.add(fixed);

      Item item = Mockito.mock(Item.class);
      Mockito.when(item.getNumCriticalSlots()).thenReturn(freeSlots);
      Mockito.when(item.getName()).thenReturn("mock item");

      assertTrue(makeDefaultCUT().isAllowed(item));

      Mockito.when(item.getNumCriticalSlots()).thenReturn(freeSlots + 1);
      assertFalse(makeDefaultCUT().isAllowed(item));
   }

   /**
    * When any large bore weapon, such as any AC, PPC or Gauss rifle is equipped, any Lower Arm Actuator (LLA) and/or
    * Hand Actuator (HA) is removed. This of course affects equipability of these weapons.
    */
   @Test
   public final void testIsAllowed_LargeBoreWeapon(){
      criticalSlots = 12;
      Item LAA = ItemDB.LAA;
      Item HA = ItemDB.HA;
      Item UAA = ItemDB.UAA;
      fixedItems.clear();
      fixedItems.add(UAA);
      fixedItems.add(LAA);
      fixedItems.add(HA);
      int freeSlots = criticalSlots - UAA.getNumCriticalSlots();
      int internalSlots = LAA.getNumCriticalSlots() + HA.getNumCriticalSlots();

      BallisticWeapon weapon = Mockito.mock(BallisticWeapon.class);
      Mockito.when(weapon.getNumCriticalSlots()).thenReturn(freeSlots - internalSlots);
      Mockito.when(weapon.getName()).thenReturn("mock item");

      // Pre check that the weapon is allowed in normal situations.
      assertTrue(makeDefaultCUT().isAllowed(weapon));

      // Now it's too big to fit.
      Mockito.when(weapon.getNumCriticalSlots()).thenReturn(freeSlots);
      assertFalse(makeDefaultCUT().isAllowed(weapon));

      // Check the real situation here
      Mockito.when(weapon.getName()).thenReturn("CLAN PPC");
      assertTrue(makeDefaultCUT().isAllowed(weapon));

      Mockito.when(weapon.getName()).thenReturn("AC/15");
      assertTrue(makeDefaultCUT().isAllowed(weapon));

      Mockito.when(weapon.getName()).thenReturn("LB 10-X AC");
      assertTrue(makeDefaultCUT().isAllowed(weapon));

      Mockito.when(weapon.getName()).thenReturn("CLAN LIGHT GAUSS RIFLE");
      assertTrue(makeDefaultCUT().isAllowed(weapon));

      Mockito.when(weapon.getName()).thenReturn("MACHINE GUN");
      assertFalse(makeDefaultCUT().isAllowed(weapon));
   }

   @Test
   public final void testShouldRemoveArmActuators(){
      BallisticWeapon ballistic = Mockito.mock(BallisticWeapon.class);
      Mockito.when(ballistic.getHardpointType()).thenReturn(HardPointType.BALLISTIC);
      EnergyWeapon energy = Mockito.mock(EnergyWeapon.class);
      Mockito.when(energy.getHardpointType()).thenReturn(HardPointType.ENERGY);

      // Check the real situation here
      Mockito.when(energy.getName()).thenReturn("CLAN PPC");
      makeDefaultCUT();
      assertTrue(ComponentOmniMech.shouldRemoveArmActuators(energy));

      Mockito.when(energy.getName()).thenReturn("LARGE LASER");
      makeDefaultCUT();
      assertFalse(ComponentOmniMech.shouldRemoveArmActuators(energy));

      Mockito.when(ballistic.getName()).thenReturn("AC/15");
      makeDefaultCUT();
      assertTrue(ComponentOmniMech.shouldRemoveArmActuators(ballistic));

      Mockito.when(ballistic.getName()).thenReturn("LB 10-X AC");
      makeDefaultCUT();
      assertTrue(ComponentOmniMech.shouldRemoveArmActuators(ballistic));

      Mockito.when(ballistic.getName()).thenReturn("CLAN LIGHT GAUSS RIFLE");
      makeDefaultCUT();
      assertTrue(ComponentOmniMech.shouldRemoveArmActuators(ballistic));

      Mockito.when(ballistic.getName()).thenReturn("C-LB5-X AC");
      makeDefaultCUT();
      assertTrue(ComponentOmniMech.shouldRemoveArmActuators(ballistic));
      
      Mockito.when(ballistic.getName()).thenReturn("MACHINE GUN");
      makeDefaultCUT();
      assertFalse(ComponentOmniMech.shouldRemoveArmActuators(ballistic));
      
   }
}
