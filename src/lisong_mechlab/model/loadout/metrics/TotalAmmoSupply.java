package lisong_mechlab.model.loadout.metrics;

import java.util.Map.Entry;
import java.util.TreeMap;


import lisong_mechlab.model.item.Ammunition;
import lisong_mechlab.model.item.Item;
import lisong_mechlab.model.loadout.Loadout;

public class TotalAmmoSupply extends AmmoMetric{
   
   private final Loadout loadout;
   private TreeMap<String, Integer> ammoValues;

   public TotalAmmoSupply(Loadout aLoadout){
      this.loadout = aLoadout;
      ammoValues = new TreeMap<>();
   }
   
   
   @Override
   public TreeMap<String, Integer> calculate(){
      ammoValues.clear();
      for(Item item : loadout.getAllItems()){
         if( item instanceof Ammunition ){
            if( ammoValues.containsKey(item.getName()) ){

               int ammoBuffer = ammoValues.get(item.getName());
               ammoBuffer = ammoBuffer + ((Ammunition)item).getShotsPerTon();
               ammoValues.put(item.getName(), ammoBuffer);
               ammoBuffer = 0;

            }
            else{
               ammoValues.put(item.getName(), ((Ammunition)item).getShotsPerTon());
            }
         }

      }
      return ammoValues;
   }
   
   public String generateString(){
      String concatenatedAmmoValues = "";
      if(ammoValues.isEmpty()){
         return "";
      }
      for(Entry<String, Integer> entry : ammoValues.entrySet()){
         concatenatedAmmoValues = concatenatedAmmoValues + entry.getKey() + "-  " + entry.getValue() + ",  ";
      }
      return concatenatedAmmoValues;
   }
}
