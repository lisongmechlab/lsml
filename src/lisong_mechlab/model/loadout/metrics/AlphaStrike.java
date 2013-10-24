package lisong_mechlab.model.loadout.metrics;

import lisong_mechlab.model.item.Item;
import lisong_mechlab.model.item.ItemDB;
import lisong_mechlab.model.item.Weapon;
import lisong_mechlab.model.loadout.Loadout;

/**
 * This metric calculates the alpha strike for a given {@link Loadout}.
 * 
 * @author Li Song
 */
public class AlphaStrike implements Metric{
   private final Loadout loadout;

   public AlphaStrike(final Loadout aLoadout){
      loadout = aLoadout;
   }

   @Override
   public double calculate(){
      double ans = 0;
      for(Item item : loadout.getAllItems()){
         if( item instanceof Weapon && item != ItemDB.AMS){
            ans += ((Weapon)item).getDamagePerShot();
         }
      }
      return ans;
   }
}
