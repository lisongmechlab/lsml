package lisong_mechlab.model.metrics;

import static org.junit.Assert.*;

import java.util.ArrayList;
import java.util.List;

import lisong_mechlab.model.Efficiencies;
import lisong_mechlab.model.item.EnergyWeapon;
import lisong_mechlab.model.item.Item;
import lisong_mechlab.model.item.ItemDB;
import lisong_mechlab.model.item.Weapon;
import lisong_mechlab.model.loadout.Loadout;
import lisong_mechlab.model.loadout.part.LoadoutPart;
import lisong_mechlab.util.MessageXBar;

import org.junit.Test;
import org.mockito.Mockito;

public class BurstDamageOverTimeTest{

   @Test
   public void testBurstDamageOverTime(){
      // Setup
      Loadout aLoadout = Mockito.mock(Loadout.class);
      MessageXBar aXBar = Mockito.mock(MessageXBar.class);

      // Execute
      BurstDamageOverTime cut = new BurstDamageOverTime(aLoadout, aXBar);

      // Verify
      Mockito.verify(aXBar).attach(cut);
      Mockito.verifyNoMoreInteractions(aXBar);
   }

   /**
    * {@link BurstDamageOverTime#calculate(double, double)} shall calculate the result correctly taking range falloff
    * and weapon cool downs into account.
    * 
    * @throws Exception
    */
   @Test
   public final void testCalculate() throws Exception{
      // Setup
      Weapon ac20 = (Weapon)ItemDB.lookup("AC/20");
      EnergyWeapon erppc = (EnergyWeapon)ItemDB.lookup("ER PPC");
      EnergyWeapon erllas = (EnergyWeapon)ItemDB.lookup("ER LARGE LASER");
      List<Item> items = new ArrayList<>();
      items.add(ac20);
      items.add(erllas);
      items.add(erppc);
      items.add(LoadoutPart.ENGINE_INTERNAL); // Shouldn't barf on internals or other items
      items.add(ItemDB.BAP);
      Loadout aLoadout = Mockito.mock(Loadout.class);
      Efficiencies efficiencies = Mockito.mock(Efficiencies.class);
      Mockito.when(efficiencies.getWeaponCycleTimeModifier()).thenReturn(1.0);
      Mockito.when(aLoadout.getAllItems()).thenReturn(items);
      Mockito.when(aLoadout.getEfficiencies()).thenReturn(efficiencies);
      MessageXBar aXBar = Mockito.mock(MessageXBar.class);
      final double time = erllas.getSecondsPerShot(efficiencies) * 3 + erllas.getDuration() / 2; // 3.5 ER LLAS

      // Execute
      BurstDamageOverTime cut = new BurstDamageOverTime(aLoadout, aXBar);
      double burst = cut.calculate(500, time);

      // Verify
      double expected = erllas.getDamagePerShot() * 3.5;
      expected += ((int)(time / ac20.getSecondsPerShot(efficiencies) + 1)) * ac20.getDamagePerShot() * ac20.getRangeEffectivity(500);
      expected += ((int)(time / erppc.getSecondsPerShot(efficiencies) + 1)) * erppc.getDamagePerShot() * erppc.getRangeEffectivity(500);
      assertEquals(expected, burst, 0.0);
   }

   /**
    * {@link BurstDamageOverTime#calculate(double, double)} shall not barf on items that are not weapons.
    * 
    * @throws Exception
    */
   @Test
   public final void testCalculate_OtherItems() throws Exception{
      // Setup
      List<Item> items = new ArrayList<>();
      items.add(LoadoutPart.ENGINE_INTERNAL); // Shouldn't barf on internals or other items
      items.add(ItemDB.BAP);
      Loadout aLoadout = Mockito.mock(Loadout.class);
      Efficiencies efficiencies = Mockito.mock(Efficiencies.class);
      Mockito.when(efficiencies.getWeaponCycleTimeModifier()).thenReturn(1.0);
      Mockito.when(aLoadout.getAllItems()).thenReturn(items);
      Mockito.when(aLoadout.getEfficiencies()).thenReturn(efficiencies);
      MessageXBar aXBar = Mockito.mock(MessageXBar.class);

      // Execute
      BurstDamageOverTime cut = new BurstDamageOverTime(aLoadout, aXBar);
      double burst = cut.calculate(500, 500);

      // Verify
      assertEquals(0.0, burst, 0.0);
   }
   
   /**
    * {@link BurstDamageOverTime#calculate(double, double)} shall not include AMS!!
    * 
    * @throws Exception
    */
   @Test
   public final void testCalculate_NoAMS() throws Exception{
      // Setup
      List<Item> items = new ArrayList<>();
      items.add(ItemDB.AMS);
      Loadout aLoadout = Mockito.mock(Loadout.class);
      Efficiencies efficiencies = Mockito.mock(Efficiencies.class);
      Mockito.when(efficiencies.getWeaponCycleTimeModifier()).thenReturn(1.0);
      Mockito.when(aLoadout.getAllItems()).thenReturn(items);
      Mockito.when(aLoadout.getEfficiencies()).thenReturn(efficiencies);
      MessageXBar aXBar = Mockito.mock(MessageXBar.class);

      // Execute
      BurstDamageOverTime cut = new BurstDamageOverTime(aLoadout, aXBar);
      double burst = cut.calculate(0, 500);

      // Verify
      assertEquals(0.0, burst, 0.0);
   }

   /**
    * The implementation caches partial results. So even if we change parameters, the result shall be calculated for the
    * correct parameters.
    * 
    * @throws Exception
    */
   @Test
   public final void testCalculate_Cacheupdate() throws Exception{
      // Setup
      EnergyWeapon erllas = (EnergyWeapon)ItemDB.lookup("ER LARGE LASER");
      List<Item> items = new ArrayList<>();
      items.add(erllas);
      Loadout aLoadout = Mockito.mock(Loadout.class);
      Efficiencies efficiencies = Mockito.mock(Efficiencies.class);
      Mockito.when(efficiencies.getWeaponCycleTimeModifier()).thenReturn(1.0);
      Mockito.when(aLoadout.getAllItems()).thenReturn(items);
      Mockito.when(aLoadout.getEfficiencies()).thenReturn(efficiencies);
      MessageXBar aXBar = Mockito.mock(MessageXBar.class);

      // Execute
      BurstDamageOverTime cut = new BurstDamageOverTime(aLoadout, aXBar);
      cut.calculate(123, 321); // Dummy just make sure it's different from below

      double time = erllas.getSecondsPerShot(efficiencies) * 3 + erllas.getDuration() / 2; // 3.5 ER LLAS
      double burst = cut.calculate(500, time);

      // Verify
      double expected = erllas.getDamagePerShot() * 3.5;
      assertEquals(expected, burst, 0.0);
   }
}
