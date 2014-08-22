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
package lisong_mechlab.model.upgrades;

import static org.junit.Assert.assertNotEquals;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import lisong_mechlab.model.item.HeatSink;
import lisong_mechlab.model.item.Item;
import lisong_mechlab.model.item.ItemDB;
import lisong_mechlab.model.loadout.LoadoutStandard;
import lisong_mechlab.model.loadout.component.ConfiguredComponentStandard;
import lisong_mechlab.model.loadout.export.Base64LoadoutCoder;
import lisong_mechlab.util.DecodingException;

import org.junit.Test;
import org.mockito.Mockito;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;

/**
 * Test suite for {@link HeatSinkUpgrade}
 * 
 * @author Li Song
 */
public class OpSetHeatSinkTypeTest{
   private int                               maxEquippableNewType;
   private int                               equippedHs;
   private int                               engineHsSlots;
   private int                               maxGloballyEquippableNewType;
   private final HeatSink                    shs        = Mockito.mock(HeatSink.class);
   private final HeatSinkUpgrade             shsUpgrade = Mockito.mock(HeatSinkUpgrade.class);
   private final HeatSink                    dhs        = Mockito.mock(HeatSink.class);
   private final HeatSinkUpgrade             dhsUpgrade = Mockito.mock(HeatSinkUpgrade.class);
   private final UpgradesMutable             upgrades   = Mockito.mock(UpgradesMutable.class);
   private final List<Item>                  items      = new ArrayList<>();
   private final ConfiguredComponentStandard component  = Mockito.mock(ConfiguredComponentStandard.class);
   private final LoadoutStandard             loadout    = Mockito.mock(LoadoutStandard.class);

   private HeatSink                          newType;
   private HeatSink                          oldType;

   private void makeDefaultCut(){
      Mockito.when(shs.getNumCriticalSlots()).thenReturn(1);
      Mockito.when(shsUpgrade.getHeatSinkType()).thenReturn(shs);
      Mockito.when(dhs.getNumCriticalSlots()).thenReturn(2);
      Mockito.when(dhsUpgrade.getHeatSinkType()).thenReturn(dhs);
      Mockito.when(upgrades.getHeatSink()).thenReturn(shsUpgrade);
      
      Mockito.when(component.getItemsEquipped()).thenReturn(items);
      Mockito.when(component.getEngineHeatsinksMax()).thenReturn(engineHsSlots);
      Mockito.doAnswer(new Answer<Void>(){
         @Override
         public Void answer(InvocationOnMock aInvocation) throws Throwable{
            if( equippedHs >= maxEquippableNewType )
               throw new IllegalArgumentException("Can't Add!");
            equippedHs++;
            return null;
         }
      }).when(component).addItem(newType);
      Mockito.when(component.canAddItem(newType)).then(new Answer<Boolean>(){
         @Override
         public Boolean answer(InvocationOnMock aInvocation) throws Throwable{
            return equippedHs < maxEquippableNewType;
         }
      });
      Mockito.doAnswer(new Answer<Void>(){
         @Override
         public Void answer(InvocationOnMock aInvocation) throws Throwable{
            if( equippedHs <= 0 )
               throw new IllegalArgumentException("Can't remove!");
            equippedHs--;
            return null;
         }
      }).when(component).removeItem(oldType);
      Mockito.when(component.canRemoveItem(oldType)).then(new Answer<Boolean>(){
         @Override
         public Boolean answer(InvocationOnMock aInvocation) throws Throwable{
            return equippedHs > 0;
         }
      });

      Mockito.when(loadout.getName()).thenReturn("Mock Loadout");
      Mockito.when(loadout.getUpgrades()).thenReturn(upgrades);
      Mockito.when(loadout.getComponents()).thenReturn(Arrays.asList(component));
      Mockito.when(loadout.canEquip(newType)).then(new Answer<Boolean>(){
         @Override
         public Boolean answer(InvocationOnMock aInvocation) throws Throwable{
            return equippedHs < maxGloballyEquippableNewType;
         }
      });
   }

   @Test
   public void testIssue288() throws DecodingException{
      String lsml = "lsml://rRoAkUBDDVASZBRDDVAGvqmbPkyZMmTJkxmZiZMmTJkyZMJkxgjXEyZMVZOTTAI=";
      Base64LoadoutCoder coder = new Base64LoadoutCoder();
      LoadoutStandard loaded = (LoadoutStandard)coder.parse(lsml);

      OpSetHeatSinkType cut = new OpSetHeatSinkType(null, loaded, UpgradeDB.DOUBLE_HEATSINKS);
      cut.apply();

      for(Item item : loaded.getAllItems()){
         assertNotEquals(item, ItemDB.SHS);
      }
   }
   
   @Test
   public void testIssue288_test2() throws DecodingException{
      String lsml = "lsml://rQAAFwAAAAAAAAAAAAAAQapmxMmTJkwmTJkwFvpkyZMAmTJh";
      Base64LoadoutCoder coder = new Base64LoadoutCoder();
      LoadoutStandard loaded = (LoadoutStandard)coder.parse(lsml);

      OpSetHeatSinkType cut = new OpSetHeatSinkType(null, loaded, UpgradeDB.DOUBLE_HEATSINKS);
      cut.apply();

      for(Item item : loaded.getAllItems()){
         assertNotEquals(item, ItemDB.SHS);
      }
   }

   @Test
   public void testDHSBug1() throws DecodingException{
      String lsml = "lsml://rQAAawgMBA4ODAQMBA4IQapmzq6gTJgt1+H0kJkx1dSMFA==";
      Base64LoadoutCoder coder = new Base64LoadoutCoder();
      LoadoutStandard loaded = (LoadoutStandard)coder.parse(lsml);

      OpSetHeatSinkType cut = new OpSetHeatSinkType(null, loaded, UpgradeDB.DOUBLE_HEATSINKS);
      cut.apply();

      for(Item item : loaded.getAllItems()){
         assertNotEquals(item, ItemDB.SHS);
      }
   }

   @Test
   public void testSwapSHS4DHS_GlobalLimit(){
      // Setup
      items.add(shs);
      items.add(shs);
      items.add(shs);
      items.add(shs);
      items.add(shs); // 5 outside.

      newType = dhs;
      oldType = shs;

      engineHsSlots = 0;
      equippedHs = items.size();
      maxEquippableNewType = 5;
      maxGloballyEquippableNewType = 2;

      makeDefaultCut();
      
      Mockito.when(component.getSlotsFree()).thenReturn(10);

      // Execute
      OpSetHeatSinkType cut = new OpSetHeatSinkType(null, loadout, dhsUpgrade);
      cut.apply();

      // Verify
      Mockito.verify(component, Mockito.times(items.size())).removeItem(shs);
      Mockito.verify(component, Mockito.times(maxGloballyEquippableNewType)).addItem(dhs);
   }

   @Test
   public void testSwapSHS4DHS_InEngine(){
      // Setup
      items.add(shs);
      items.add(shs);
      items.add(shs);
      items.add(shs);
      items.add(shs); // 3 in engine, 2 outside.

      newType = dhs;
      oldType = shs;

      engineHsSlots = 3;
      equippedHs = items.size();
      maxEquippableNewType = 4;
      maxGloballyEquippableNewType = 10;

      makeDefaultCut();
      
      Mockito.when(component.getSlotsFree()).thenReturn(0);

      // Execute
      OpSetHeatSinkType cut = new OpSetHeatSinkType(null, loadout, dhsUpgrade);
      cut.apply();

      // Verify
      Mockito.verify(component, Mockito.times(items.size())).removeItem(shs);
      Mockito.verify(component, Mockito.times(maxEquippableNewType)).addItem(dhs);
   }

   @Test
   public void testSwapSHS4DHS_NotInEngine(){
      // Setup
      items.add(shs);
      items.add(shs);
      items.add(shs);
      items.add(shs);
      items.add(shs); // 3 in engine, 2 outside.

      newType = dhs;
      oldType = shs;

      engineHsSlots = 0;
      equippedHs = items.size();
      maxEquippableNewType = 2;
      maxGloballyEquippableNewType = 10;

      makeDefaultCut();
      
      Mockito.when(component.getSlotsFree()).thenReturn(0);

      // Execute
      OpSetHeatSinkType cut = new OpSetHeatSinkType(null, loadout, dhsUpgrade);
      cut.apply();

      // Verify
      Mockito.verify(component, Mockito.times(items.size())).removeItem(shs);
      Mockito.verify(component, Mockito.times(maxEquippableNewType)).addItem(dhs);
   }
   
   @Test
   public void testSwapSHS4DHS_NothingRemoved(){
      // Setup
      newType = dhs;
      oldType = shs;

      engineHsSlots = 4;
      equippedHs = items.size();
      maxEquippableNewType = 4;
      maxGloballyEquippableNewType = 10;

      makeDefaultCut();
      
      Mockito.when(component.getSlotsFree()).thenReturn(8);

      // Execute
      OpSetHeatSinkType cut = new OpSetHeatSinkType(null, loadout, dhsUpgrade);
      cut.apply();

      // Verify
      Mockito.verify(component, Mockito.times(items.size())).removeItem(shs);
      Mockito.verify(component, Mockito.times(0)).addItem(dhs);
   }
}
