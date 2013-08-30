package lisong_mechlab.model.loadout.metrics;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

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
   private MaxSustainedDPS maxSustainedDPS;

   @Test
   public void testCalculate() throws Exception{
      fail("NYI");
   }

   @Test
   public void testGetDamageDistribution() throws Exception{
   // Setup
      List<Item> items = new ArrayList<>();
      when(loadout.getAllItems()).thenReturn(items);
      items.add(ItemDB.lookup("MACHINE GUN"));
      items.add(ItemDB.lookup("MACHINE GUN"));
      items.add(ItemDB.lookup("GAUSS RIFLE"));
      items.add(ItemDB.lookup("ANTI-MISSILE SYSTEM"));
      items.add(ItemDB.lookup("STREAK SRM 2"));
      items.add(ItemDB.lookup("LRM 20"));
      items.add(ItemDB.lookup("ER PPC"));
      items.add(ItemDB.lookup("ER PPC"));
      
      when(heatDissipation.calculate()).thenReturn(1.0);
      
      // Execute & Verify Range = 0
      Map<Weapon, Double> range0 = maxSustainedDPS.getDamageDistribution(0);
      assertEquals(2.0, range0.remove(ItemDB.lookup("MACHINE GUN")), 0.0); // Two of them!
      assertEquals(1.0, range0.remove(ItemDB.lookup("GAUSS RIFLE")), 0.0);
      assertFalse(range0.containsKey(ItemDB.lookup("ANTI-MISSILE SYSTEM")));
      assertTrue(range0.remove(ItemDB.lookup("STREAK SRM 2")) > 0.0);
      assertEquals(0.0, range0.remove(ItemDB.lookup("LRM 20")), 0.0);
      
      // Execute & Verify Range = 750
      Map<Weapon, Double> range750 = maxSustainedDPS.getDamageDistribution(750);
      assertEquals(0.0, range750.remove(ItemDB.lookup("MACHINE GUN")), 0.0); // Two of them!
      assertEquals(0.931818, range750.remove(ItemDB.lookup("GAUSS RIFLE")), 0.00001);
      assertFalse(range750.containsKey(ItemDB.lookup("ANTI-MISSILE SYSTEM")));
      assertEquals(0.0, range750.remove(ItemDB.lookup("STREAK SRM 2")), 0.0);
      assertTrue(range750.remove(ItemDB.lookup("LRM 20")) > 0.0);
   }

}
