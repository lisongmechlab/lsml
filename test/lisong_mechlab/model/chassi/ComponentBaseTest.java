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

import static org.junit.Assert.*;

import java.util.ArrayList;
import java.util.List;

import lisong_mechlab.model.item.Item;

import org.junit.Test;
import org.mockito.Mockito;

/**
 * Test suite for {@link ComponentBase}.
 * 
 * @author Li Song
 */
public abstract class ComponentBaseTest{

   protected int criticalSlots = 5;
   protected double hp = 15;
   protected Location location = Location.Head;
   protected List<Item> fixedItems = new ArrayList<>();
   
   protected abstract ComponentBase makeDefaultCUT();
   
   @Test
   public void testGetFixedItems_NotNull(){
      assertNotNull(makeDefaultCUT().getFixedItems());
   }
   
   @Test
   public void testGetFixedItems(){
      assertEquals(fixedItems, makeDefaultCUT().getFixedItems());
   }
   
   @Test(expected=UnsupportedOperationException.class)
   public void testGetFixedItems_Immutable(){
      makeDefaultCUT().getFixedItems().add(null);
   }
   
   /**
    * The string representation of the component shall contain the name of the location.
    */
   @Test
   public void testToString(){
      assertTrue(makeDefaultCUT().toString().contains(location.toString()));
   }
   
   @Test
   public void testGetFixedItemSlots(){
      Item item1 = Mockito.mock(Item.class);
      Item item2 = Mockito.mock(Item.class);
      
      Mockito.when(item1.getNumCriticalSlots()).thenReturn(3);
      Mockito.when(item2.getNumCriticalSlots()).thenReturn(4);
      
      fixedItems.clear();
      fixedItems.add(item1);
      fixedItems.add(item2);
      
      assertEquals(7, makeDefaultCUT().getFixedItemSlots());
   }
   
   @Test
   public void testGetSlots(){
      assertEquals(criticalSlots, makeDefaultCUT().getSlots());
   }
   
   @Test
   public void testGetLocation(){
      assertEquals(location, makeDefaultCUT().getLocation());
   }
   
   @Test
   public void testGetHitPoints(){
      assertEquals(hp, makeDefaultCUT().getHitPoints(), 0.0);
   }
   
   @Test
   public void testGetArmorMax_Head(){
      hp = 20;
      location = Location.Head;
      assertEquals(18, makeDefaultCUT().getArmorMax());
   }
   
   @Test
   public void testGetArmorMax_Other(){
      hp = 20;
      location = Location.CenterTorso;
      assertEquals(40, makeDefaultCUT().getArmorMax());
   }
}
