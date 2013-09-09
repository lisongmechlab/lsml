package lisong_mechlab.model.loadout.metrics;

import java.util.TreeMap;

import lisong_mechlab.model.item.AmmoWeapon;
import lisong_mechlab.model.item.Ammunition;
import lisong_mechlab.model.item.Item;
import lisong_mechlab.model.item.Weapon;
import lisong_mechlab.model.loadout.Loadout;

public class TotalAmmoSupply extends TableMetric{

   private final Loadout          loadout;
   private TreeMap<Ammunition, Integer> ammoValues;

   public TotalAmmoSupply(Loadout aLoadout){
      this.loadout = aLoadout;

      ammoValues = new TreeMap<>();
   }

   @Override
   public TreeMap<Ammunition, Integer> calculate(){
      TreeMap<Ammunition, Integer> ammoMap = new TreeMap<>();
      for(Item item : loadout.getAllItems()){
         if( item instanceof Ammunition ){
            if(ammoMap.containsKey(item)){
               int tempValue = ammoMap.get(item);
               ammoMap.put((Ammunition)item, ++tempValue);
            }
            else{
               ammoMap.put((Ammunition)item, 1);
            }
            
         }
         
      }
      return ammoMap;
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

   public TreeMap<String, Double> getSecondsForEach(){
      TreeMap<String, Double> secondValues = new TreeMap<>();
      for(Item item : loadout.getAllItems()){
         if( item instanceof AmmoWeapon ){
            if( ammoValues.containsKey(((AmmoWeapon)item).getAmmoType()) ){
               if( secondValues.containsKey(((AmmoWeapon)item).getAmmoType().getName()) ){
                  double tempVolleyAmount = secondValues.get(((AmmoWeapon)item).getAmmoType().getName())
                                         + ((AmmoWeapon)item).getSecondsPerShot();
                  secondValues.put(((AmmoWeapon)item).getAmmoType().getName(), tempVolleyAmount);
               }
               else{
                  secondValues.put(((AmmoWeapon)item).getAmmoType().getName(), ((AmmoWeapon)item).getSecondsPerShot());
               }

            }
            else{
               secondValues.put(item.getName(), (double)0);
            }
         }
         else if( item instanceof Ammunition ){
            if( !secondValues.containsKey(item.getName()) ){
               secondValues.put(item.getName(), (double)0);
            }
         }
      }
      return secondValues;
   }

}
