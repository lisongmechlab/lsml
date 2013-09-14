package lisong_mechlab.model.loadout;

import lisong_mechlab.model.chassi.Chassi;
import lisong_mechlab.model.item.Ammunition;
import lisong_mechlab.model.item.Item;
import lisong_mechlab.model.item.MissileWeapon;

public class ArtemisHandler{

   private Loadout loadout;
   private Chassi  chassi;
   private int     additionalCritSlots;
   private int     additionalMass;

   public ArtemisHandler(Loadout loadoutUnderTest){
      loadout = loadoutUnderTest;
      chassi = loadout.getChassi();
      additionalCritSlots = 0;
      additionalMass = 0;
   }

   public void checkLoadoutStillValid(){
      if( !loadout.getUpgrades().hasArtemis() ){
         for(LoadoutPart part : loadout.getPartLoadOuts()){
            additionalCritSlots = 0;
            additionalMass = 0;
            if( !checkPartCanHoldArtemis(part) ){
               throw new IllegalArgumentException("Not enough free crit slots!");
            }
         }
      }

   }

   private boolean checkPartCanHoldArtemis(LoadoutPart aPart){

      for(Item item : aPart.getItems()){
         if( (item instanceof MissileWeapon) ){
            additionalCritSlots++;
            additionalMass++;
         }

      }
      if( ((aPart.getNumCriticalSlotsFree()) < additionalCritSlots) && additionalCritSlots != 0 ){

         return false;
      }
      return true;

   }

   public void checkArtemisAdditionLegal() throws IllegalArgumentException{
      additionalCritSlots = 0;
      additionalMass = 0;
      for(Item item : loadout.getAllItems()){
         if( (item instanceof MissileWeapon) ){
            additionalCritSlots++;
            additionalMass++;
         }

      }
      if( !loadout.getUpgrades().hasArtemis() ){
         if( (loadout.getMass() + additionalMass) > chassi.getMassMax() ){
            throw new IllegalArgumentException("Not enough free mass!");
         }
         if( (loadout.getNumCriticalSlotsFree() - additionalCritSlots) < 0 ){
            throw new IllegalArgumentException("Not enough free crit slots!");
         }
      }

   }

}
