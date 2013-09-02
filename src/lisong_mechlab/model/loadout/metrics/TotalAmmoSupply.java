package lisong_mechlab.model.loadout.metrics;

import java.util.TreeMap;

import lisong_mechlab.model.item.AmmoWeapon;
import lisong_mechlab.model.item.Ammunition;
import lisong_mechlab.model.item.Item;
import lisong_mechlab.model.loadout.Loadout;

public class TotalAmmoSupply extends AmmoMetric{

   private final Loadout          loadout;
   private TreeMap<Ammunition, Integer> ammoValues;

   public TotalAmmoSupply(Loadout aLoadout){
      this.loadout = aLoadout;

      ammoValues = new TreeMap<>();
   }

   @Override
   public TreeMap<Ammunition, Integer> calculate(){
      ammoValues.clear();
      for(Item item : loadout.getAllItems()){
         if( item instanceof Ammunition ){
            if( ammoValues.containsKey(item) ){

               int ammoBuffer = ammoValues.get(item);
               ammoBuffer = ammoBuffer + ((Ammunition)item).getShotsPerTon();
               ammoValues.put((Ammunition)item, ammoBuffer);
               ammoBuffer = 0;

            }
            else{
               ammoValues.put((Ammunition)item, ((Ammunition)item).getShotsPerTon());
            }
         }

      }

      for(Item item : loadout.getAllItems()){
         if( item instanceof AmmoWeapon ){
            if( !ammoValues.containsKey(((AmmoWeapon)item).getAmmoType()) ){
               ammoValues.put((Ammunition)item, 0);
            }
         }

      }
      return ammoValues;
   }

   public TreeMap<String, Integer> getShotsPerVolleyForEach(){
      TreeMap<String, Integer> volleyValues = new TreeMap<>();
      for(Item item : loadout.getAllItems()){
         if( item instanceof AmmoWeapon ){
            if( ammoValues.containsKey(((AmmoWeapon)item).getAmmoType()) ){
               if( volleyValues.containsKey(((AmmoWeapon)item).getAmmoType().getName()) ){
                  int tempVolleyAmount = volleyValues.get(((AmmoWeapon)item).getAmmoType().getName())
                                         + ((AmmoWeapon)item).getNumberOfShotsPerVolley();
                  volleyValues.put(((AmmoWeapon)item).getAmmoType().getName(), tempVolleyAmount);
               }
               else{
                  volleyValues.put(((AmmoWeapon)item).getAmmoType().getName(), ((AmmoWeapon)item).getNumberOfShotsPerVolley());
               }

            }
            else{
               volleyValues.put(item.getName(), 0);
            }
         }
         else if( item instanceof Ammunition ){
            if( !volleyValues.containsKey(item.getName()) ){
               volleyValues.put(item.getName(), 0);
            }
         }
      }
      return volleyValues;
   }



}
