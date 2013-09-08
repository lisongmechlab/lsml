package lisong_mechlab.model.loadout.metrics;

import lisong_mechlab.model.item.Item;
import lisong_mechlab.model.item.ItemDB;
import lisong_mechlab.model.item.Weapon;
import lisong_mechlab.model.loadout.Loadout;

/**
 * This {@link Metric} calculates the maximal DPS a {@link Loadout} can output.
 * 
 * @author Emily Björk
 */
public class MaxDPS implements Metric{
   private final Loadout loadout;

   public MaxDPS(Loadout aLoadout){
      loadout = aLoadout;
   }

   @Override
   public double calculate(){
      double ans = 0;
      for(Item item : loadout.getAllItems()){
         if( item instanceof Weapon && item != ItemDB.AMS ){
            ans += ((Weapon)item).getStat("d/s");
         }
      }
      return ans;
   }
}
