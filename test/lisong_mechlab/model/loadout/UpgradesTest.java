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
package lisong_mechlab.model.loadout;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.reset;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyZeroInteractions;

import java.util.ArrayList;
import java.util.List;

import lisong_mechlab.model.chassi.Part;
import lisong_mechlab.model.item.Item;
import lisong_mechlab.model.item.ItemDB;
import lisong_mechlab.model.loadout.Upgrades.Message.ChangeMsg;
import lisong_mechlab.model.loadout.part.LoadoutPart;
import lisong_mechlab.model.loadout.part.LoadoutPart.Message.Type;
import lisong_mechlab.util.MessageXBar;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;

public class UpgradesTest{
   @Mock
   MessageXBar xbar;
   Upgrades    cut;

   @Before
   public void setup(){
      MockitoAnnotations.initMocks(this);
      cut = new Upgrades(xbar);
   }

   @Test
   public void testInitialState(){
      assertFalse(cut.hasArtemis());
      assertFalse(cut.hasDoubleHeatSinks());
      assertFalse(cut.hasEndoSteel());
      assertFalse(cut.hasFerroFibrous());
   }

   //@formatter:off
   /**
    * When the Artemis status for the loadout changes, the ammo which is affected should be replaced with the correct
    * type.
    *//*
   @Test
   public void testReceive_artemisEnabled(){
      Mockito.when(mlc.upgrades.hasArtemis()).thenReturn(false);

      LoadoutPart cut = makeCUT(100, Part.LeftTorso, 12);
      cut.addItem("SRM AMMO", false);
      cut.addItem("LRM AMMO", false);
      cut.addItem("STREAK SRM AMMO", false);
      cut.addItem("NARC AMMO", false);
      Mockito.verify(xBar, Mockito.times(4)).post(new LoadoutPart.Message(cut, Type.ItemAdded));

      Mockito.when(mlc.upgrades.hasArtemis()).thenReturn(true);
      cut.receive(new Upgrades.Message(ChangeMsg.GUIDANCE, mlc.upgrades));
      Mockito.verify(xBar).post(new LoadoutPart.Message(cut, Type.ItemsChanged));

      List<Item> items = new ArrayList<>(cut.getItems());
      assertTrue(items.remove(ItemDB.lookup("SRM AMMO + ARTEMIS IV")));
      assertTrue(items.remove(ItemDB.lookup("LRM AMMO + ARTEMIS IV")));
      assertTrue(items.remove(ItemDB.lookup("STREAK SRM AMMO")));
      assertTrue(items.remove(ItemDB.lookup("NARC AMMO")));
      assertTrue(items.isEmpty());
   }
    
    * When the Artemis status for the loadout changes, the ammo which is affected should be replaced with the correct
    * type.
   @Test
   public void testReceive_artemisNoChange(){
      Mockito.when(mlc.upgrades.hasArtemis()).thenReturn(true);

      LoadoutPart cut = makeCUT(100, Part.LeftTorso, 12);
      cut.addItem("SRM AMMO + ARTEMIS IV", false);
      cut.addItem("LRM AMMO + ARTEMIS IV", false);
      cut.addItem("STREAK SRM AMMO", false);
      cut.addItem("NARC AMMO", false);
      Mockito.verify(xBar, Mockito.times(4)).post(new LoadoutPart.Message(cut, Type.ItemAdded));

      Mockito.when(mlc.upgrades.hasArtemis()).thenReturn(true);
      cut.receive(new Upgrades.Message(ChangeMsg.GUIDANCE, mlc.upgrades));

      List<Item> items = new ArrayList<>(cut.getItems());
      assertTrue(items.remove(ItemDB.lookup("SRM AMMO + ARTEMIS IV")));
      assertTrue(items.remove(ItemDB.lookup("LRM AMMO + ARTEMIS IV")));
      assertTrue(items.remove(ItemDB.lookup("STREAK SRM AMMO")));
      assertTrue(items.remove(ItemDB.lookup("NARC AMMO")));
      assertTrue(items.isEmpty());
   }
   */
   //@formatter:on

   @Test
   public void testDHS_disable_disabled(){
      // Execute
      cut.setDoubleHeatSinks(false);

      // Verify
      verifyZeroInteractions(xbar);
      assertFalse(cut.hasDoubleHeatSinks());
   }

   @Test
   public void testArtemis_disable_disabled(){
      // Execute
      cut.setArtemis(false);

      // Verify
      verifyZeroInteractions(xbar);
      assertFalse(cut.hasArtemis());
   }

   @Test
   public void testFF_disable_disabled(){
      // Execute
      cut.setFerroFibrous(false);

      // Verify
      verifyZeroInteractions(xbar);
      assertFalse(cut.hasFerroFibrous());
   }

   @Test
   public void testES_disable_disabled(){
      // Execute
      cut.setEndoSteel(false);

      // Verify
      verifyZeroInteractions(xbar);
      assertFalse(cut.hasEndoSteel());
   }

   // --------------
   @Test
   public void testDHS_enable_disabled(){
      // Execute
      cut.setDoubleHeatSinks(true);

      // Verify
      verify(xbar, times(1)).post(new Upgrades.Message(ChangeMsg.HEATSINKS, cut));
      assertTrue(cut.hasDoubleHeatSinks());
   }

   @Test
   public void testArtemis_enable_disabled(){
      // Execute
      cut.setArtemis(true);

      // Verify
      verify(xbar, times(1)).post(new Upgrades.Message(ChangeMsg.GUIDANCE, cut));
      assertTrue(cut.hasArtemis());
   }

   @Test
   public void testFF_enable_disabled(){
      // Execute
      cut.setFerroFibrous(true);

      // Verify
      verify(xbar, times(1)).post(new Upgrades.Message(ChangeMsg.ARMOR, cut));
      assertTrue(cut.hasFerroFibrous());
   }

   @Test
   public void testES_enable_disabled(){
      // Execute
      cut.setEndoSteel(true);

      // Verify
      verify(xbar, times(1)).post(new Upgrades.Message(ChangeMsg.STRUCTURE, cut));
      assertTrue(cut.hasEndoSteel());
   }

   // --------------
   @Test
   public void testDHS_enable_enabled(){
      // Setup
      cut.setDoubleHeatSinks(true);
      reset(xbar);

      // Execute
      cut.setDoubleHeatSinks(true);

      // Verify
      verifyZeroInteractions(xbar);
      assertTrue(cut.hasDoubleHeatSinks());
   }

   @Test
   public void testArtemis_enable_enabled(){
      // Setup
      cut.setArtemis(true);
      reset(xbar);

      // Execute
      cut.setArtemis(true);

      // Verify
      verifyZeroInteractions(xbar);
      assertTrue(cut.hasArtemis());
   }

   @Test
   public void testFF_enable_enabled(){
      // Setup
      cut.setFerroFibrous(true);
      reset(xbar);

      // Execute
      cut.setFerroFibrous(true);

      // Verify
      verifyZeroInteractions(xbar);
      assertTrue(cut.hasFerroFibrous());
   }

   @Test
   public void testES_enable_enabled(){
      // Setup
      cut.setEndoSteel(true);
      reset(xbar);

      // Execute
      cut.setEndoSteel(true);

      // Verify
      verifyZeroInteractions(xbar);
      assertTrue(cut.hasEndoSteel());
   }
}
