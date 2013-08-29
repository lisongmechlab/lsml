package lisong_mechlab.model.loadout.metrics;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import lisong_mechlab.model.item.Item;
import lisong_mechlab.model.item.Weapon;
import lisong_mechlab.model.loadout.Loadout;

public class MaxSustainedDPS extends Metric{
   private final Loadout loadout;
   private final HeatDissipation dissipation;

   public MaxSustainedDPS(Loadout aLoadout, HeatDissipation aHeatDissipation){
      loadout = aLoadout;
      dissipation = aHeatDissipation;
   }

   @Override
   public double calculate(){
      double heatleft = dissipation.calculate();
      List<Weapon> weapons = new ArrayList<>(15);
            for(Item item : loadout.getAllItems()){
         if( item instanceof Weapon ){
            weapons.add((Weapon)item);
         }
      }
      Collections.sort(weapons, new Comparator<Weapon>(){
         @Override
         public int compare(Weapon aO1, Weapon aO2){
            return Double.compare(aO1.getStat("d/h"), aO1.getStat("d/h"));
         }});
      
      double ans = 0;
      while(heatleft>0 && !weapons.isEmpty()){
         Weapon weapon = weapons.remove(0);
         double heat = weapon.getStat("h/s");
         if(heat < heatleft){
            heatleft -= heat;
            ans += weapon.getStat("d/s");
         }
         else{
            ans += weapon.getStat("d/s") * heatleft / weapon.getStat("h/s");
         }
      }
      return ans;
   }
}
