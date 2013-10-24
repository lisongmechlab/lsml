package lisong_mechlab.model.loadout.metrics;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import lisong_mechlab.model.helpers.MockLoadoutContainer;
import lisong_mechlab.model.item.Item;
import lisong_mechlab.model.item.ItemDB;
import lisong_mechlab.model.item.Weapon;
import static org.junit.Assert.*;
import static org.mockito.Mockito.*;
import org.junit.Test;

/**
 * Test suite for {@link AlphaStrike}.
 * 
 * @author Emily Björk
 */
public class AlphaStrikeTest{
   private final MockLoadoutContainer mlc = new MockLoadoutContainer();
   private final AlphaStrike          cut = new AlphaStrike(mlc.loadout);

   /**
    * Calculate shall sum up the per volley damage of all weapons.
    */
   @Test
   public void testCalculate(){
      List<Item> items = new ArrayList<>();
      Weapon ppc = (Weapon)ItemDB.lookup("PPC");
      Weapon ll = (Weapon)ItemDB.lookup("LARGE LASER");
      Weapon lrm20 = (Weapon)ItemDB.lookup("LRM 20");
      Weapon lb10x = (Weapon)ItemDB.lookup("LB 10-X AC");
      items.add(ItemDB.BAP); // Shall not barf on non-weapons
      items.add(ppc);
      items.add(ll);
      items.add(lrm20);
      items.add(lb10x);
      when(mlc.loadout.getAllItems()).thenReturn(items);

      double expected = ppc.getDamagePerShot() + ll.getDamagePerShot() + lrm20.getDamagePerShot() + lb10x.getDamagePerShot();
      assertEquals(expected, cut.calculate(), 0.0);
   }
   
   /**
    * AMS shall not be counted to the alpha strike.
    */
   @Test
   public void testCalculate_NoAMS(){
      List<Item> items = Arrays.asList((Item)ItemDB.AMS);
      when(mlc.loadout.getAllItems()).thenReturn(items);
      
      assertEquals(0, cut.calculate(), 0.0);
   }
}
