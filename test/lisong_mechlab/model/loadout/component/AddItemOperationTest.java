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

import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;
import lisong_mechlab.model.NotificationMessage;
import lisong_mechlab.model.NotificationMessage.Severity;
import lisong_mechlab.model.chassi.ChassisDB;
import lisong_mechlab.model.chassi.ChassisStandard;
import lisong_mechlab.model.chassi.ComponentStandard;
import lisong_mechlab.model.chassi.Location;
import lisong_mechlab.model.item.Item;
import lisong_mechlab.model.item.ItemDB;
import lisong_mechlab.model.loadout.LoadoutStandard;
import lisong_mechlab.model.upgrades.UpgradeDB;
import lisong_mechlab.model.upgrades.Upgrades;
import lisong_mechlab.util.MessageXBar;
import lisong_mechlab.util.OperationStack;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.runners.MockitoJUnitRunner;

/**
 * Test suite for {@link OpAddItem}.
 * 
 * @author Li Song
 */
@RunWith(MockitoJUnitRunner.class)
public class AddItemOperationTest{
   @Mock
   private ConfiguredComponentBase loadoutPart;
   @Mock
   private LoadoutStandard             loadout;
   @Mock
   private Upgrades            upgrades;
   @Mock
   private MessageXBar         xBar;
   @Mock
   private ComponentStandard   internalPart;

   @Before
   public void setup(){
      Mockito.when(loadout.getUpgrades()).thenReturn(upgrades);
      Mockito.when(loadoutPart.getInternalComponent()).thenReturn(internalPart);
      Mockito.when(internalPart.getLocation()).thenReturn(Location.CenterTorso);
   }

   @Test
   public void testDescription(){
      Item item = ItemDB.ECM;

      OpAddItem cut = new OpAddItem(xBar, loadout, loadoutPart, item);

      assertTrue(cut.describe().contains("add"));
      assertTrue(cut.describe().contains("to"));
      assertTrue(cut.describe().contains(loadoutPart.getInternalComponent().getLocation().toString()));
      assertTrue(cut.describe().contains(item.getName()));
   }

   @Test
   public void testDescription_artemis(){
      Item item = ItemDB.lookup("LRM 20");
      Mockito.when(upgrades.getGuidance()).thenReturn(UpgradeDB.ARTEMIS_IV);

      OpAddItem cut = new OpAddItem(xBar, loadout, loadoutPart, item);

      assertTrue(cut.describe().contains("add"));
      assertTrue(cut.describe().contains("to"));
      assertTrue(cut.describe().contains(loadoutPart.getInternalComponent().getLocation().toString()));
      assertTrue(cut.describe().contains(item.getName()));
   }

   /**
    * If an item can't be added, an exception shall be thrown when the operation is applied.
    */
   @Test(expected = IllegalArgumentException.class)
   public void testCantAddItem(){
      OpAddItem cut = null;
      try{
         Item item = ItemDB.lookup("LRM 20");
         Mockito.when(loadout.canEquip(item)).thenReturn(true);
         Mockito.when(loadoutPart.canAddItem(item)).thenReturn(false);
         cut = new OpAddItem(xBar, loadout, loadoutPart, item);
      }
      catch( Throwable t ){
         fail("Setup failed");
         return;
      }

      cut.apply();
   }

   /**
    * C.A.S.E. together with an XL engine should generate a warning notice
    */
   @Test
   public void testAddItem_XLCaseLeft(){
      LoadoutStandard testLoadout = new LoadoutStandard((ChassisStandard)ChassisDB.lookup("AS7-D-DC"), null);
      OperationStack stack = new OperationStack(0);
      stack.pushAndApply(new OpAddItem(null, testLoadout, testLoadout.getComponent(Location.CenterTorso), ItemDB.lookup("XL ENGINE 300")));

      OpAddItem cut = new OpAddItem(xBar, testLoadout, testLoadout.getComponent(Location.LeftTorso), ItemDB.CASE);
      cut.apply();

      Mockito.verify(xBar).post(new NotificationMessage(Severity.WARNING, testLoadout, "C.A.S.E. together with XL engine has no effect."));
   }

   /**
    * C.A.S.E. together with an XL engine should generate a warning notice
    */
   @Test
   public void testAddItem_XLCaseRight(){
      LoadoutStandard testLoadout = new LoadoutStandard((ChassisStandard)ChassisDB.lookup("AS7-D-DC"), null);
      OperationStack stack = new OperationStack(0);
      stack.pushAndApply(new OpAddItem(null, testLoadout, testLoadout.getComponent(Location.CenterTorso), ItemDB.lookup("XL ENGINE 300")));

      OpAddItem cut = new OpAddItem(xBar, testLoadout, testLoadout.getComponent(Location.RightTorso), ItemDB.CASE);
      cut.apply();

      Mockito.verify(xBar).post(new NotificationMessage(Severity.WARNING, testLoadout, "C.A.S.E. together with XL engine has no effect."));
   }
}
