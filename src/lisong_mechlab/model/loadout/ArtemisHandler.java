/*
 * @formatter:off
 * Li Song Mechlab - A 'mech building tool for PGI's MechWarrior: Online.
 * Copyright (C) 2013  Emily Björk
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */  
//@formatter:on
package lisong_mechlab.model.loadout;

import lisong_mechlab.model.chassi.Chassi;
import lisong_mechlab.model.item.Item;
import lisong_mechlab.model.item.MissileWeapon;

public class ArtemisHandler{

   private Loadout loadout;
   private Chassi  chassi;

   // private int additionalCritSlots;
   // private int additionalMass;

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

   public int getAdditionalMass(){
      int additionalMass = 0;
      for(Item item : loadout.getAllItems()){
         if( (item instanceof MissileWeapon) && !item.getName().toLowerCase().contains("streak") && !item.getName().toLowerCase().contains("narc") ){
            additionalMass++;
         }

      }
      return additionalMass;
   }

   public int getAdditionalSlots(){
      int additionalCritSlots = 0;
      for(Item item : loadout.getAllItems()){
         if( (item instanceof MissileWeapon) && !item.getName().toLowerCase().contains("streak") && !item.getName().toLowerCase().contains("narc") ){
            additionalCritSlots++;
         }

      }
      return additionalCritSlots;
   }

}
