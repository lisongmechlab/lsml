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
import lisong_mechlab.model.item.Item;

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

}
