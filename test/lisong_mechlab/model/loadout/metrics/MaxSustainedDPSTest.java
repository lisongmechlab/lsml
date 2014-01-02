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
package lisong_mechlab.model.loadout.metrics;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Random;

import lisong_mechlab.model.item.Item;
import lisong_mechlab.model.item.ItemDB;
import lisong_mechlab.model.item.Weapon;
import lisong_mechlab.model.loadout.Loadout;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

/**
 * Test suite for {@link MaxSustainedDPS} {@link Metric}.
 * 
 * @author Emily Björk
 */
@RunWith(MockitoJUnitRunner.class)
public class MaxSustainedDPSTest{
   @Mock
   private HeatDissipation heatDissipation;

   @Mock
   private Loadout         loadout;
   @InjectMocks
   private MaxSustainedDPS cut;

   @Test
   public void testGetWeaponRatios() throws Exception{
      // Setup
      List<Item> items = new ArrayList<>();
      when(loadout.getAllItems()).thenReturn(items);
      items.add(ItemDB.lookup("MACHINE GUN"));
      items.add(ItemDB.lookup("MACHINE GUN"));
      items.add(ItemDB.lookup("GAUSS RIFLE"));
      items.add(ItemDB.AMS);
      items.add(ItemDB.lookup("STD ENGINE 245"));
      items.add(ItemDB.DHS);
      items.add(ItemDB.lookup("STREAK SRM 2"));
      items.add(ItemDB.lookup("LRM 20"));
      items.add(ItemDB.lookup("ER PPC"));
      items.add(ItemDB.lookup("ER PPC"));

      when(heatDissipation.calculate()).thenReturn(1.0);

      // Execute & Verify Range = 0
      Map<Weapon, Double> range0 = cut.getWeaponRatios(0);
      assertEquals(2.0, range0.remove(ItemDB.lookup("MACHINE GUN")), 0.0); // Two of them!
      assertEquals(1.0, range0.remove(ItemDB.lookup("GAUSS RIFLE")), 0.0);
      assertTrue(range0.remove(ItemDB.lookup("STREAK SRM 2")) > 0.0);
      assertEquals(0.0, range0.remove(ItemDB.lookup("LRM 20")), 0.0);
      assertFalse(range0.containsKey(ItemDB.AMS));

      // Execute & Verify Range = 750
      Map<Weapon, Double> range750 = cut.getWeaponRatios(750);
      assertEquals(0.0, range750.remove(ItemDB.lookup("MACHINE GUN")), 0.0); // Two of them!
      assertEquals(1.0, range750.remove(ItemDB.lookup("GAUSS RIFLE")), 0.00001);
      assertEquals(0.0, range750.remove(ItemDB.lookup("STREAK SRM 2")), 0.0);
      assertTrue(range750.remove(ItemDB.lookup("LRM 20")) > 0.0);
      assertFalse(range750.containsKey(ItemDB.AMS));
   }

   /**
    * PPC shall have an instant fall off (patch 2013-09-03)
    */
   @Test
   public void testGetWeaponRatios_ppc(){
      // Setup
      List<Item> items = new ArrayList<>();
      Weapon ppc = (Weapon)ItemDB.lookup("PPC");
      items.add(ppc);

      when(loadout.getAllItems()).thenReturn(items);
      when(heatDissipation.calculate()).thenReturn(10.0);
      
      assertEquals(ppc.getStat("d/s", null, null), cut.calculate(90.0 + 0.001), 0.0);
      assertEquals(0.0, cut.calculate(90.0 - 0.001), 0.0);
   }

   /**
    * Tests that getWeaponRatios correctly handles the machine guns zero heat production.
    */
   @Test
   public void testGetWeaponRatios_machineGun(){
      // Setup
      List<Item> items = new ArrayList<>();
      Weapon mg = (Weapon)ItemDB.lookup("MACHINE GUN");
      items.add(mg);

      when(loadout.getAllItems()).thenReturn(items);
      when(heatDissipation.calculate()).thenReturn(0.0);

      Map<Weapon, Double> result_0 = cut.getWeaponRatios(-1);

      assertTrue(result_0.containsKey(mg));
      assertEquals(0.0, result_0.get(mg).doubleValue(), 0.0);
   }

   /**
    * Damage shall be correctly calculated, taking high DpH weapons into account first regardless of order they are
    * found.
    */
   @Test
   public void testCalculate(){
      // Setup
      List<Item> items = new ArrayList<>();
      Weapon gauss = (Weapon)ItemDB.lookup("GAUSS RIFLE");
      Weapon erppc = (Weapon)ItemDB.lookup("ER PPC");
      Weapon llas = (Weapon)ItemDB.lookup("LARGE LASER");

      final long seed = 1;
      Random rng = new Random(seed);
      items.add(gauss);
      items.add(erppc);
      items.add(erppc);
      items.add(llas);
      Collections.shuffle(items, rng); // "Deterministically random" shuffle

      // There is enough heat to dissipate the GAUSS, LLaser and 1.5 ER PPCs
      double heat = gauss.getStat("h/s", null, null) + erppc.getStat("h/s", null, null) * 1.5 + llas.getStat("h/s", null, null);

      when(loadout.getAllItems()).thenReturn(items);
      when(heatDissipation.calculate()).thenReturn(heat);

      // Execute
      double result = cut.calculate(300.0); // 300.0 is inside LLAS optimal

      // Verify
      double expected = gauss.getStat("d/s", null, null) + erppc.getStat("d/s", null, null) * 1.5 + llas.getStat("d/s", null, null);
      assertEquals(expected, result, 0.0);
   }

   /**
    * AMS shall not be added to DPS
    */
   @Test
   public void testCalculate_ams(){
      // Setup
      List<Item> items = new ArrayList<>();
      Weapon gauss = (Weapon)ItemDB.lookup("GAUSS RIFLE");
      items.add(gauss);
      items.add(ItemDB.AMS);

      when(loadout.getAllItems()).thenReturn(items);
      when(heatDissipation.calculate()).thenReturn(1.0);

      double result = cut.calculate(0.0);

      assertEquals(gauss.getStat("d/s", null, null), result, 0.0);
   }
}
