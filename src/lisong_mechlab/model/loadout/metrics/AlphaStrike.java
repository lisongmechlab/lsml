package lisong_mechlab.model.loadout.metrics;

import lisong_mechlab.model.item.Item;
import lisong_mechlab.model.item.Weapon;
import lisong_mechlab.model.loadout.Loadout;

public class AlphaStrike extends Metric{
   private final Loadout loadout;

   public AlphaStrike(Loadout aLoadout){
      loadout = aLoadout;
   }

   @Override
   public double calculate(){
      double ans = 0;
      for(Item item : loadout.getAllItems()){
         if( item instanceof Weapon ){
            ans += ((Weapon)item).getDamagePerVolley();
         }
      }
      return ans;
   }
}
