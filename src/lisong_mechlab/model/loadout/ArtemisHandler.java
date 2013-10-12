package lisong_mechlab.model.loadout;

import lisong_mechlab.model.chassi.Chassi;
import lisong_mechlab.model.item.Item;
import lisong_mechlab.model.item.MissileWeapon;

public class ArtemisHandler{

   private Loadout loadout;
   private Chassi  chassi;
  // private int     additionalCritSlots;
 //  private int     additionalMass;

   public ArtemisHandler(Loadout loadoutUnderTest){
      loadout = loadoutUnderTest;
      chassi = loadout.getChassi();
   }

   public void checkLoadoutStillValid(){
      if( !loadout.getUpgrades().hasArtemis() ){
         for(LoadoutPart part : loadout.getPartLoadOuts()){
            if( !checkPartCanHoldArtemis(part) ){
               throw new IllegalArgumentException("Not enough free crit slots!");
            }
         }
      }

   }

   private boolean checkPartCanHoldArtemis(LoadoutPart aPart){
      int additionalCritSlots = 0;
      for(Item item : aPart.getItems()){
         if( (item instanceof MissileWeapon) && !item.getName().toLowerCase().contains("streak") && !item.getName().toLowerCase().contains("narc") ){
            additionalCritSlots++;
         }

      }
      if( ((aPart.getNumCriticalSlotsFree()) < additionalCritSlots) && additionalCritSlots != 0 ){

         return false;
      }
      return true;

   }

   public void checkArtemisAdditionLegal() throws IllegalArgumentException{
      int additionalCritSlots = 0;
      int additionalMass = 0;
      for(Item item : loadout.getAllItems()){
         if( (item instanceof MissileWeapon) && !item.getName().toLowerCase().contains("streak") && !item.getName().toLowerCase().contains("narc") ){
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
   
   public int getAdditionalMass() {
      int additionalMass = 0;
      for(Item item : loadout.getAllItems()){
         if( (item instanceof MissileWeapon) && !item.getName().toLowerCase().contains("streak") && !item.getName().toLowerCase().contains("narc") ){
            additionalMass++;
         }

      }
      return additionalMass;
   }
   
   public int getAdditionalSlots() {
      int additionalCritSlots = 0;
      for(Item item : loadout.getAllItems()){
         if( (item instanceof MissileWeapon) && !item.getName().toLowerCase().contains("streak") && !item.getName().toLowerCase().contains("narc") ){
            additionalCritSlots++;
         }

      }
      return additionalCritSlots;
   }

}
