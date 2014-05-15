package lisong_mechlab.model.metrics;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import lisong_mechlab.model.item.EnergyWeapon;
import lisong_mechlab.model.item.Engine;
import lisong_mechlab.model.item.Item;
import lisong_mechlab.model.item.ItemDB;
import lisong_mechlab.model.item.Weapon;
import lisong_mechlab.model.loadout.Loadout;
import lisong_mechlab.model.loadout.component.ConfiguredComponent;
import lisong_mechlab.util.MessageXBar;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.runners.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class HeatOverTimeTest{
   @Mock
   MessageXBar xBar;

   @Test
   public void testCalculate_TAG(){
      EnergyWeapon erllas = (EnergyWeapon)ItemDB.lookup("TAG");
      List<Item> items = new ArrayList<>();
      items.add(erllas);

      Loadout loadout = Mockito.mock(Loadout.class);
      Mockito.when(loadout.getAllItems()).thenReturn(items);

      HeatOverTime cut = new HeatOverTime(loadout, xBar);

      assertEquals(0, cut.calculate(0), 0.0);
      assertEquals(0, cut.calculate(100), 0.0);
   }

   @Test
   public void testCalculate_ERLLAS(){
      EnergyWeapon erllas = (EnergyWeapon)ItemDB.lookup("ER LARGE LASER");
      List<Item> items = new ArrayList<>();
      items.add(erllas);

      Loadout loadout = Mockito.mock(Loadout.class);
      Mockito.when(loadout.getAllItems()).thenReturn(items);

      HeatOverTime cut = new HeatOverTime(loadout, xBar);

      assertEquals(0, cut.calculate(0), 0.0);
      assertEquals(erllas.getHeat() / 2, cut.calculate(erllas.getDuration() / 2), 0.0);

      assertEquals(erllas.getHeat() * 10.5, cut.calculate(erllas.getSecondsPerShot(null) * 10 + erllas.getDuration() / 2), 0.0);
   }

   @Test
   public void testCalculate_ERPPC(){
      EnergyWeapon erppc = (EnergyWeapon)ItemDB.lookup("ER PPC");
      List<Item> items = new ArrayList<>();
      items.add(erppc);

      Loadout loadout = Mockito.mock(Loadout.class);
      Mockito.when(loadout.getAllItems()).thenReturn(items);

      HeatOverTime cut = new HeatOverTime(loadout, xBar);

      assertEquals(erppc.getHeat(), cut.calculate(0), 0.0);
      assertEquals(erppc.getHeat(), cut.calculate(0 + Math.ulp(1)), 0.0);

      assertEquals(erppc.getHeat() * 5, cut.calculate(erppc.getSecondsPerShot(null) * 5 - Math.ulp(1)), 0.0);
   }

   @Test
   public void testCalculate_AC20(){
      Weapon ac20 = (Weapon)ItemDB.lookup("AC/20");
      List<Item> items = new ArrayList<>();
      items.add(ac20);

      Loadout loadout = Mockito.mock(Loadout.class);
      Mockito.when(loadout.getAllItems()).thenReturn(items);

      HeatOverTime cut = new HeatOverTime(loadout, xBar);

      assertEquals(ac20.getHeat(), cut.calculate(0), 0.0);
      assertEquals(ac20.getHeat(), cut.calculate(0 + Math.ulp(1)), 0.0);

      assertEquals(ac20.getHeat() * 5, cut.calculate(ac20.getSecondsPerShot(null) * 5 - Math.ulp(1)), 0.0);
   }

   @Test
   public void testCalculate_Engine(){
      Engine engine = (Engine)ItemDB.lookup("STD ENGINE 200");
      List<Item> items = new ArrayList<>();
      items.add(engine);

      Loadout loadout = Mockito.mock(Loadout.class);
      Mockito.when(loadout.getAllItems()).thenReturn(items);

      HeatOverTime cut = new HeatOverTime(loadout, xBar);

      assertEquals(0, cut.calculate(0), 0.0);
      assertEquals(2, cut.calculate(10), 0.0);
      assertEquals(2.02, cut.calculate(10.1), 0.0);
   }

   @Test
   public void testCalculate_MultiItem(){
      Engine engine = (Engine)ItemDB.lookup("STD ENGINE 200");
      EnergyWeapon erllas = (EnergyWeapon)ItemDB.lookup("ER LARGE LASER");
      Weapon ac20 = (Weapon)ItemDB.lookup("AC/20");
      List<Item> items = new ArrayList<>();
      items.add(engine);
      items.add(erllas);
      items.add(ac20);

      Loadout loadout = Mockito.mock(Loadout.class);
      Mockito.when(loadout.getAllItems()).thenReturn(items);

      HeatOverTime cut = new HeatOverTime(loadout, xBar);

      assertEquals(0.2 * 20 + ac20.getHeat() * 5 + erllas.getHeat() * 5, cut.calculate(20 - Math.ulp(20)), Math.ulp(80));
   }

   @Test
   public void testUpdate(){
      Engine engine = (Engine)ItemDB.lookup("STD ENGINE 200");
      EnergyWeapon erllas = (EnergyWeapon)ItemDB.lookup("ER LARGE LASER");
      Weapon ac20 = (Weapon)ItemDB.lookup("AC/20");
      List<Item> items = new ArrayList<>();
      items.add(engine);
      items.add(erllas);
      items.add(ac20);

      Loadout loadout = Mockito.mock(Loadout.class);
      Mockito.when(loadout.getAllItems()).thenReturn(items);

      HeatOverTime cut = new HeatOverTime(loadout, xBar);
      Mockito.verify(xBar).attach(cut);

      double old = cut.calculate(20);
      items.remove(ac20);
      Collection<ConfiguredComponent> partLoadouts = Mockito.mock(Collection.class);
      Mockito.when(partLoadouts.contains(null)).thenReturn(true);
      Mockito.when(loadout.getComponents()).thenReturn(partLoadouts);
      cut.receive(new ConfiguredComponent.Message(null, ConfiguredComponent.Message.Type.ItemAdded));

      assertTrue(old != cut.calculate(20));
   }
}
