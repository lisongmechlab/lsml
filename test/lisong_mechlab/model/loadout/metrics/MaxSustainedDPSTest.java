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

import lisong_mechlab.model.item.BallisticWeapon;
import lisong_mechlab.model.item.EnergyWeapon;
import lisong_mechlab.model.item.Item;
import lisong_mechlab.model.item.ItemDB;
import lisong_mechlab.model.item.Weapon;
import lisong_mechlab.model.loadout.Loadout;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

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
      assertEquals(0.931818, range750.remove(ItemDB.lookup("GAUSS RIFLE")), 0.00001);
      assertEquals(0.0, range750.remove(ItemDB.lookup("STREAK SRM 2")), 0.0);
      assertTrue(range750.remove(ItemDB.lookup("LRM 20")) > 0.0);
      assertFalse(range750.containsKey(ItemDB.AMS));
   }

   /**
    * AMS shall not be added to DPS
    */
   @Test
   public void testCalculate_ams(){
      // Setup
      List<Item> items = new ArrayList<>();
      BallisticWeapon gauss = (BallisticWeapon)ItemDB.lookup("GAUSS RIFLE");
      items.add(gauss);
      items.add(ItemDB.AMS);

      when(loadout.getAllItems()).thenReturn(items);
      when(heatDissipation.calculate()).thenReturn(1.0);

      double result = cut.calculate();

      assertEquals(gauss.getStat("d/s", null), result, 0.0);
   }

   /**
    * PPC shall have an instant fall off (patch 2013-09-03)
    */
   @Test
   public void testGetWeaponRatios_ppc(){
      // Setup
      List<Item> items = new ArrayList<>();
      EnergyWeapon ppc = (EnergyWeapon)ItemDB.lookup("PPC");
      items.add(ppc);

      when(loadout.getAllItems()).thenReturn(items);
      when(heatDissipation.calculate()).thenReturn(10.0);

      Map<Weapon, Double> result_0 = cut.getWeaponRatios(90.0 - 0.001);
      Map<Weapon, Double> result_1 = cut.getWeaponRatios(90.0 + 0.001);

      assertTrue(result_0.containsKey(ppc));
      assertEquals(0.0, result_0.get(ppc).doubleValue(), 0.0);

      assertTrue(result_1.containsKey(ppc));
      assertEquals(1.0, result_1.get(ppc).doubleValue(), 0.0);
   }
   
   
   /**Tests that getWeaponRatios correctly handles the machine guns zero heat production.
    * 
    */
   @Test
   public void testGetWeaponRatios_machineGun(){
      // Setup
      List<Item> items = new ArrayList<>();
      BallisticWeapon mg = (BallisticWeapon)ItemDB.lookup("MACHINE GUN");
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
      BallisticWeapon gauss = (BallisticWeapon)ItemDB.lookup("GAUSS RIFLE");
      EnergyWeapon erppc = (EnergyWeapon)ItemDB.lookup("ER PPC");
      EnergyWeapon llas = (EnergyWeapon)ItemDB.lookup("LARGE LASER");

      final long seed = 1;
      Random rng = new Random(seed);
      items.add(gauss);
      items.add(erppc);
      items.add(erppc);
      items.add(llas);
      Collections.shuffle(items, rng); // "Deterministically random" shuffle

      // There is enough heat to dissipate the GAUSS, LLaser and 1.5 ER PPCs
      double heat = gauss.getStat("h/s", null) + erppc.getStat("h/s", null) * 1.5 + llas.getStat("h/s", null);

      when(loadout.getAllItems()).thenReturn(items);
      when(heatDissipation.calculate()).thenReturn(heat);

      // Execute
      double result = cut.calculate();

      // Verify
      double expected = gauss.getStat("d/s", null) + erppc.getStat("d/s", null) * 1.5 + llas.getStat("d/s", null);
      assertEquals(expected, result, 0.0);
   }
}
