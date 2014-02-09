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
package lisong_mechlab.model.loadout.part;

import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;
import lisong_mechlab.model.chassi.InternalPart;
import lisong_mechlab.model.chassi.Part;
import lisong_mechlab.model.item.Item;
import lisong_mechlab.model.item.ItemDB;
import lisong_mechlab.model.loadout.Loadout;
import lisong_mechlab.model.upgrades.UpgradeDB;
import lisong_mechlab.model.upgrades.Upgrades;
import lisong_mechlab.util.MessageXBar;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.runners.MockitoJUnitRunner;

/**
 * Test suite for {@link AddItemOperation}.
 * 
 * @author Emily Björk
 */
@RunWith(MockitoJUnitRunner.class)
public class AddItemOperationTest{
   @Mock
   private LoadoutPart  loadoutPart;
   @Mock
   private Loadout      loadout;
   @Mock
   private Upgrades     upgrades;
   @Mock
   private MessageXBar  xBar;
   @Mock
   private InternalPart internalPart;

   @Before
   public void setup(){
      Mockito.when(loadout.getUpgrades()).thenReturn(upgrades);
      Mockito.when(loadoutPart.getLoadout()).thenReturn(loadout);
      Mockito.when(loadoutPart.getInternalPart()).thenReturn(internalPart);
      Mockito.when(internalPart.getType()).thenReturn(Part.CenterTorso);
   }

   @Test
   public void testDescription(){
      Item item = ItemDB.ECM;

      AddItemOperation cut = new AddItemOperation(xBar, loadoutPart, item);

      assertTrue(cut.describe().contains("add"));
      assertTrue(cut.describe().contains("to"));
      assertTrue(cut.describe().contains(loadoutPart.getInternalPart().getType().toString()));
      assertTrue(cut.describe().contains(item.getName()));
   }

   @Test
   public void testDescription_artemis(){
      Item item = ItemDB.lookup("LRM 20");
      Mockito.when(upgrades.getGuidance()).thenReturn(UpgradeDB.ARTEMIS_IV);

      AddItemOperation cut = new AddItemOperation(xBar, loadoutPart, item);

      assertTrue(cut.describe().contains("add"));
      assertTrue(cut.describe().contains("to"));
      assertTrue(cut.describe().contains(loadoutPart.getInternalPart().getType().toString()));
      assertTrue(cut.describe().contains(item.getName(upgrades)));
   }

   /**
    * If an item can't be added, an exception shall be thrown when the operation is applied.
    */
   @Test(expected = IllegalArgumentException.class)
   public void testCantAddItem(){
      AddItemOperation cut = null;
      try{
         Item item = ItemDB.lookup("LRM 20");
         Mockito.when(loadoutPart.canAddItem(item)).thenReturn(false);
         cut = new AddItemOperation(xBar, loadoutPart, item);
      }
      catch( Throwable t ){
         fail("Setup failed");
         return;
      }

      cut.apply();
   }
}
