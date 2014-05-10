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

import lisong_mechlab.model.chassi.Location;
import lisong_mechlab.model.item.Item;
import lisong_mechlab.model.item.ItemDB;
import lisong_mechlab.model.loadout.Loadout;
import lisong_mechlab.model.loadout.component.ConfiguredComponent.Message;
import lisong_mechlab.model.loadout.component.ConfiguredComponent.Message.Type;
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
 * Test suite for the abstract {@link OpItemBase} class.
 * 
 * @author Emily Björk
 */
@RunWith(MockitoJUnitRunner.class)
public class ItemOperationTest{

   class CutClass extends OpItemBase{
      /**
       * @param anXBar
       * @param aLoadoutPart
       */
      public CutClass(MessageXBar anXBar, ConfiguredComponent aLoadoutPart){
         super(anXBar, aLoadoutPart);
      }

      // @formatter:off
      @Override public String describe(){return null;}
      @Override protected void apply(){/* NO-OP */}
      @Override protected void undo(){/* NO-OP */}
      // @formatter:on     
   }

   @Mock
   private ConfiguredComponent loadoutPart;
   @Mock
   private MessageXBar         xBar;

   private CutClass            cut;

   @Before
   public void setup(){
      cut = new CutClass(xBar, loadoutPart);
   }

   /**
    * removeItem() shall remove the item without qeustinos.
    */
   @Test
   public final void testRemoveItem(){
      Item ecm = ItemDB.ECM;

      cut.removeItem(ecm);

      Mockito.verify(loadoutPart).removeItem(ecm);
      Mockito.verify(xBar).post(new Message(loadoutPart, Type.ItemRemoved));
   }

   /**
    * addItem() shall add an item without performing any checks.
    */
   @Test
   public final void testAddItem(){
      Item ecm = ItemDB.ECM;
      Loadout loadout = Mockito.mock(Loadout.class);
      Mockito.when(loadoutPart.getLoadout()).thenReturn(loadout);

      cut.addItem(ecm);

      Mockito.verify(loadoutPart).addItem(ecm);
      Mockito.verify(xBar).post(new Message(loadoutPart, Type.ItemAdded));
   }

   /**
    * Adding a standard engine shall not cause a stir.
    */
   @Test
   public final void testAddItem_StdEngine(){
      Item item = ItemDB.lookup("STD ENGINE 300");
      Loadout loadout = Mockito.mock(Loadout.class);
      Mockito.when(loadoutPart.getLoadout()).thenReturn(loadout);

      cut.addItem(item);

      Mockito.verify(loadoutPart).addItem(item);
      Mockito.verify(xBar).post(new Message(loadoutPart, Type.ItemAdded));
   }

   /**
    * Removing an XL engine shall also remove ENGINE_INTERNAL from side torsos
    */
   @Test
   public final void testRemoveItem_XLEngine(){
      Item item = ItemDB.lookup("XL ENGINE 300");
      Loadout loadout = Mockito.mock(Loadout.class);
      ConfiguredComponent lt = Mockito.mock(ConfiguredComponent.class);
      ConfiguredComponent rt = Mockito.mock(ConfiguredComponent.class);
      Mockito.when(loadout.getPart(Location.LeftTorso)).thenReturn(lt);
      Mockito.when(loadout.getPart(Location.RightTorso)).thenReturn(rt);
      Mockito.when(loadoutPart.getLoadout()).thenReturn(loadout);

      cut.removeItem(item);

      Mockito.verify(loadoutPart).removeItem(item);
      Mockito.verify(lt).removeItem(ConfiguredComponent.ENGINE_INTERNAL);
      Mockito.verify(rt).removeItem(ConfiguredComponent.ENGINE_INTERNAL);
      Mockito.verify(xBar).post(new Message(loadoutPart, Type.ItemRemoved));
      Mockito.verify(xBar).post(new Message(lt, Type.ItemRemoved));
      Mockito.verify(xBar).post(new Message(rt, Type.ItemRemoved));
   }

   /**
    * Removing a standard engine shall also remove engine heat sinks (SHS).
    */
   @Test
   public final void testRemoveItem_StdEngine_DHS(){
      int numEngineHs = 2;
      Item item = ItemDB.lookup("STD ENGINE 300");
      Upgrades upgrades = Mockito.mock(Upgrades.class);
      Loadout loadout = Mockito.mock(Loadout.class);
      Mockito.when(upgrades.getHeatSink()).thenReturn(UpgradeDB.DOUBLE_HEATSINKS);
      Mockito.when(loadout.getUpgrades()).thenReturn(upgrades);
      Mockito.when(loadoutPart.getLoadout()).thenReturn(loadout);
      Mockito.when(loadoutPart.getNumEngineHeatsinks()).thenReturn(numEngineHs);

      cut.removeItem(item);

      Mockito.verify(loadoutPart).removeItem(item);
      Mockito.verify(loadoutPart, Mockito.times(numEngineHs)).removeItem(ItemDB.DHS);
      Mockito.verify(xBar).post(new Message(loadoutPart, Type.ItemRemoved));
   }

   /**
    * Removing a standard engine shall also remove engine heat sinks (DHS).
    */
   @Test
   public final void testRemoveItem_StdEngine_SHS(){
      int numEngineHs = 2;
      Item item = ItemDB.lookup("STD ENGINE 300");
      Upgrades upgrades = Mockito.mock(Upgrades.class);
      Loadout loadout = Mockito.mock(Loadout.class);
      Mockito.when(upgrades.getHeatSink()).thenReturn(UpgradeDB.STANDARD_HEATSINKS);
      Mockito.when(loadout.getUpgrades()).thenReturn(upgrades);
      Mockito.when(loadoutPart.getLoadout()).thenReturn(loadout);
      Mockito.when(loadoutPart.getNumEngineHeatsinks()).thenReturn(numEngineHs);

      cut.removeItem(item);

      Mockito.verify(loadoutPart).removeItem(item);
      Mockito.verify(loadoutPart, Mockito.times(numEngineHs)).removeItem(ItemDB.SHS);
      Mockito.verify(xBar).post(new Message(loadoutPart, Type.ItemRemoved));
   }

   /**
    * Adding an XL engine shall add LoadoutPart.ENGINE_INTERNAL to both side torsii.
    */
   @Test
   public final void testAddItem_XLEngine(){
      Loadout loadout = Mockito.mock(Loadout.class);
      ConfiguredComponent lt = Mockito.mock(ConfiguredComponent.class);
      ConfiguredComponent rt = Mockito.mock(ConfiguredComponent.class);
      Mockito.when(loadout.getPart(Location.LeftTorso)).thenReturn(lt);
      Mockito.when(loadout.getPart(Location.RightTorso)).thenReturn(rt);
      Mockito.when(loadoutPart.getLoadout()).thenReturn(loadout);
      Item item = ItemDB.lookup("XL ENGINE 300");

      cut.addItem(item);

      Mockito.verify(loadoutPart).addItem(item);
      Mockito.verify(lt).addItem(ConfiguredComponent.ENGINE_INTERNAL);
      Mockito.verify(rt).addItem(ConfiguredComponent.ENGINE_INTERNAL);
      Mockito.verify(xBar).post(new Message(loadoutPart, Type.ItemAdded));
      Mockito.verify(xBar).post(new Message(lt, Type.ItemAdded));
      Mockito.verify(xBar).post(new Message(rt, Type.ItemAdded));
   }

   /**
    * addItem() has a special case where it shall re-add engine heat sinks that were removed in a previous call to
    * removeItem(). This is to facilitate undo behavior on engines to include heat sinks (SHS)
    */
   @Test
   public final void testAddItem_addEngineAfterRemoveSHS(){
      final int numEngineHs = 2;
      Loadout loadout = Mockito.mock(Loadout.class);
      Upgrades upgrades = Mockito.mock(Upgrades.class);
      Mockito.when(upgrades.getHeatSink()).thenReturn(UpgradeDB.DOUBLE_HEATSINKS);
      Mockito.when(loadout.getUpgrades()).thenReturn(upgrades);
      Mockito.when(loadoutPart.getLoadout()).thenReturn(loadout);
      Mockito.when(loadoutPart.getNumEngineHeatsinks()).thenReturn(numEngineHs);
      Item item = ItemDB.lookup("STD ENGINE 300");

      cut.removeItem(item);
      cut.addItem(item);

      Mockito.verify(loadoutPart, Mockito.times(numEngineHs)).addItem(ItemDB.DHS);
   }

   /**
    * addItem() has a special case where it shall re-add engine heat sinks that were removed in a previous call to
    * removeItem(). This is to facilitate undo behavior on engines to include heat sinks (DHS)
    */
   @Test
   public final void testAddItem_addEngineAfterRemoveDHS(){
      final int numEngineHs = 2;
      Loadout loadout = Mockito.mock(Loadout.class);
      Upgrades upgrades = Mockito.mock(Upgrades.class);
      Mockito.when(upgrades.getHeatSink()).thenReturn(UpgradeDB.DOUBLE_HEATSINKS);
      Mockito.when(loadout.getUpgrades()).thenReturn(upgrades);
      Mockito.when(loadoutPart.getLoadout()).thenReturn(loadout);
      Mockito.when(loadoutPart.getNumEngineHeatsinks()).thenReturn(numEngineHs);
      Item item = ItemDB.lookup("STD ENGINE 300");

      cut.removeItem(item);
      cut.addItem(item);

      Mockito.verify(loadoutPart, Mockito.times(numEngineHs)).addItem(ItemDB.DHS);
   }

}
