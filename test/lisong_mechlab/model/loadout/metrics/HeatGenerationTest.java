package lisong_mechlab.model.loadout.metrics;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.List;

import lisong_mechlab.model.helpers.MockLoadoutContainer;
import lisong_mechlab.model.item.Engine;
import lisong_mechlab.model.item.Item;
import lisong_mechlab.model.item.ItemDB;
import lisong_mechlab.model.item.Weapon;

import org.junit.Test;

/**
 * Test suite for {@link HeatGeneration}.
 * 
 * @author Emily Björk
 */
public class HeatGenerationTest{
   private final MockLoadoutContainer mlc = new MockLoadoutContainer();
   private final HeatGeneration       cut = new HeatGeneration(mlc.loadout);

   /**
    * Heat generation shall include heat per second from all weapons as well as the base heat from the engine. But no
    * heat from the jump jets.
    */
   @Test
   public void testCalculate(){
      List<Item> items = new ArrayList<>();
      Weapon ppc = (Weapon)ItemDB.lookup("PPC");
      Weapon ll = (Weapon)ItemDB.lookup("LARGE LASER");
      Weapon lrm20 = (Weapon)ItemDB.lookup("LRM 20");
      Weapon lb10x = (Weapon)ItemDB.lookup("LB 10-X AC");
      Engine engine = (Engine)ItemDB.lookup("STD ENGINE 300");
      Item jj = ItemDB.lookup("JUMP JETS - CLASS V");
      items.add(ItemDB.BAP); // Shall not barf on non-weapons
      items.add(ppc);
      items.add(ll);
      items.add(lrm20);
      items.add(lb10x);
      items.add(engine);
      items.add(jj);
      when(mlc.loadout.getAllItems()).thenReturn(items);

      final double expected = ppc.getStat("h/s") + ll.getStat("h/s") + lrm20.getStat("h/s") + lb10x.getStat("h/s") + engine.getHeat();
      assertEquals(expected, cut.calculate(), 0.0);
   }

}
