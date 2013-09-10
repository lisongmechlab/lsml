package lisong_mechlab.model.loadout.metrics;

import java.util.TreeMap;

import lisong_mechlab.model.item.Item;
import lisong_mechlab.model.item.Weapon;
import lisong_mechlab.model.loadout.Loadout;

public class TotalWeapons extends TableMetric{

   
   private final Loadout          loadout;
   private TreeMap<Weapon, Integer> weaponValues;

   public TotalWeapons(Loadout aLoadout){
      this.loadout = aLoadout;

      weaponValues = new TreeMap<>();
   }
   @Override
   public TreeMap<Weapon, Integer> calculate(){
      weaponValues.clear();
      for(Item item : loadout.getAllItems()){
         if( item instanceof Weapon ){
            if(weaponValues.containsKey(item)){
               int tempValue = weaponValues.get(item);
               tempValue++;
               weaponValues.put((Weapon) item, tempValue);
            }
            else{
                         weaponValues.put((Weapon) item, 1);  
            }

         }
         
      }
      return weaponValues;
   }

}
