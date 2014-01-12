package lisong_mechlab.model.loadout.metrics;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.List;

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
public class GhostHeatTest{
   @Mock
   private Loadout   loadout;
   @InjectMocks
   private GhostHeat cut;

   @Test
   public void testCalculate_2ppc() throws Exception{
      // Example from: http://mwomercs.com/forums/topic/127904-heat-scale-the-maths/
      List<Item> weapons = new ArrayList<>();
      weapons.add(ItemDB.lookup("PPC"));
      weapons.add(ItemDB.lookup("PPC"));
      when(loadout.getAllItems()).thenReturn(weapons);

      double result = cut.calculate();
      assertEquals(0.0, result, 0.0);
   }

   @Test
   public void testCalculate_3ppc() throws Exception{
      // Example from: http://mwomercs.com/forums/topic/127904-heat-scale-the-maths/
      List<Item> weapons = new ArrayList<>();
      weapons.add(ItemDB.lookup("PPC"));
      weapons.add(ItemDB.lookup("PPC"));
      weapons.add(ItemDB.lookup("PPC"));
      when(loadout.getAllItems()).thenReturn(weapons);

      double result = cut.calculate();
      assertEquals(12.60, result, 0.0); // Base heat was bumped from 8 to 10, adjusting result
   }

   @Test
   public void testCalculate_4ppc() throws Exception{
      // Example from: http://mwomercs.com/forums/topic/127904-heat-scale-the-maths/
      List<Item> weapons = new ArrayList<>();
      weapons.add(ItemDB.lookup("PPC"));
      weapons.add(ItemDB.lookup("PPC"));
      weapons.add(ItemDB.lookup("PPC"));
      weapons.add(ItemDB.lookup("PPC"));
      when(loadout.getAllItems()).thenReturn(weapons);

      double result = cut.calculate();
      assertEquals(33.60, result, 0.0); // Base heat was bumped from 8 to 10, adjusting result
   }

   @Test
   public void testCalculate_linkedMixedGroup() throws Exception{
      // Example from: http://mwomercs.com/forums/topic/127904-heat-scale-the-maths/
      List<Item> weapons = new ArrayList<>();
      weapons.add(ItemDB.lookup("ER LARGE LASER"));
      weapons.add(ItemDB.lookup("LARGE LASER"));
      weapons.add(ItemDB.lookup("LRG PULSE LASER"));
      weapons.add(ItemDB.lookup("LARGE LASER"));
      when(loadout.getAllItems()).thenReturn(weapons);

      Weapon lpl = (Weapon)ItemDB.lookup("LRG PULSE LASER");

      double result = cut.calculate();
      assertEquals(lpl.getGhostHeatMultiplier() * lpl.getHeat() * (0.18 + 0.30), result, 0.0001);
   }

   @Test
   public void testCalculate_unlinkedMixedGroup() throws Exception{
      // Example from: http://mwomercs.com/forums/topic/127904-heat-scale-the-maths/
      List<Item> weapons = new ArrayList<>();
      weapons.add(ItemDB.lookup("AC/20"));
      weapons.add(ItemDB.lookup("MEDIUM LASER"));
      weapons.add(ItemDB.lookup("AC/20"));
      weapons.add(ItemDB.lookup("MEDIUM LASER"));
      weapons.add(ItemDB.lookup("MEDIUM LASER"));
      weapons.add(ItemDB.lookup("MEDIUM LASER"));
      weapons.add(ItemDB.lookup("MEDIUM LASER"));
      weapons.add(ItemDB.lookup("MEDIUM LASER"));
      weapons.add(ItemDB.lookup("MEDIUM LASER"));
      weapons.add(ItemDB.lookup("MEDIUM LASER"));
      weapons.add(ItemDB.lookup("MEDIUM LASER"));
      when(loadout.getAllItems()).thenReturn(weapons);

      Weapon ac20 = (Weapon)ItemDB.lookup("AC/20");
      Weapon mlas = (Weapon)ItemDB.lookup("MEDIUM LASER");

      double result = cut.calculate();
      double ac20penalty = ac20.getGhostHeatMultiplier() * ac20.getHeat() * 0.08;
      double mlaspenalty = mlas.getGhostHeatMultiplier() * mlas.getHeat() * (0.80 + 1.10 + 1.50);
      assertEquals(ac20penalty + mlaspenalty, result, 0.0001);
   }

   @Test
   public void testCalculate_unpenalizedWeapons() throws Exception{
      // Example from: http://mwomercs.com/forums/topic/127904-heat-scale-the-maths/
      List<Item> weapons = new ArrayList<>();
      weapons.add(ItemDB.lookup("SMALL LASER"));
      weapons.add(ItemDB.lookup("SML PULSE LASER"));
      weapons.add(ItemDB.lookup("AC/5"));
      weapons.add(ItemDB.lookup("LRM5"));
      weapons.add(ItemDB.lookup("FLAMER"));
      weapons.add(ItemDB.ECM); // Non weapon items should be handled without error.
      when(loadout.getAllItems()).thenReturn(weapons);

      double result = cut.calculate();
      assertEquals(0.0, result, 0.0);
   }
}
