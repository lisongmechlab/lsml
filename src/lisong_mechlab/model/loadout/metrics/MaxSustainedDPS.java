package lisong_mechlab.model.loadout.metrics;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import lisong_mechlab.model.item.Item;
import lisong_mechlab.model.item.ItemDB;
import lisong_mechlab.model.item.Weapon;
import lisong_mechlab.model.loadout.Loadout;

public class MaxSustainedDPS extends Metric{
   private final Loadout         loadout;
   private final HeatDissipation dissipation;

   public MaxSustainedDPS(Loadout aLoadout, HeatDissipation aHeatDissipation){
      loadout = aLoadout;
      dissipation = aHeatDissipation;
   }

   @Override
   public double calculate(){
      double ans = 0.0;
      Map<Weapon, Double> dd = getDamageDistribution(-1);
      for(Map.Entry<Weapon, Double> entry : dd.entrySet()){
         ans += entry.getKey().getStat("d/s") * entry.getValue();
      }
      return ans;
   }

   /**
    * Calculates a distribution of damage over the weapons that achieves the maximal sustained damage throughput.
    * 
    * @return A {@link Map} with {@link Weapon} as key and a {@link Double} as value representing a % of how often the
    *         weapon is used.
    */
   public Map<Weapon, Double> getDamageDistribution(final double range){
      double heatleft = dissipation.calculate();
      List<Weapon> weapons = new ArrayList<>(15);
      for(Item item : loadout.getAllItems()){
         if( item instanceof Weapon && item != ItemDB.lookup("ANTI-MISSILE SYSTEM") ){
            weapons.add((Weapon)item);
         }
      }
      if( range >= 0 ){
         Collections.sort(weapons, new Comparator<Weapon>(){
            @Override
            public int compare(Weapon aO1, Weapon aO2){
               return Double.compare(aO2.getRangeEffectivity(range) * aO2.getStat("d/h"), aO1.getRangeEffectivity(range) * aO1.getStat("d/h"));
            }
         });
      }
      else{
         Collections.sort(weapons, new Comparator<Weapon>(){
            @Override
            public int compare(Weapon aO1, Weapon aO2){
               return Double.compare(aO2.getStat("d/h"), aO1.getStat("d/h"));
            }
         });
      }

      Map<Weapon, Double> ans = new HashMap<>();
      while( !weapons.isEmpty() ){
         Weapon weapon = weapons.remove(0);
         final double heat = weapon.getStat("h/s");
         final double ratio;
         final double rangefactor = (range >= 0) ? weapon.getRangeEffectivity(range) : 1.0;

         if( heat < heatleft ){
            ratio = rangefactor;
            heatleft -= heat;
         }
         else{
            ratio = heatleft / weapon.getStat("h/s") * rangefactor;
            heatleft = 0;
         }

         if( ans.containsKey(weapon) )
            ans.put(weapon, Double.valueOf(ans.get(weapon).doubleValue() + ratio));
         else
            ans.put(weapon, Double.valueOf(ratio));
      }
      return ans;
   }
}
