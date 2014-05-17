/*
 * @formatter:off
 * Li Song Mechlab - A 'mech building tool for PGI's MechWarrior: Online.
 * Copyright (C) 2013  Li Song
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
package lisong_mechlab.model.chassi;

import lisong_mechlab.model.item.Engine;
import lisong_mechlab.model.upgrades.ArmorUpgrade;
import lisong_mechlab.model.upgrades.HeatSinkUpgrade;
import lisong_mechlab.model.upgrades.StructureUpgrade;

/**
 * This class models an omnimech chassis, i.e. the basic attributes associated with the chassis and the center omnipod.
 * 
 * @author Li Song
 */
public class ChassisOmniMech extends ChassisBase{

   private final Engine           engine;
   private final StructureUpgrade structureType;
   private final ArmorUpgrade     armorType;
   private final HeatSinkUpgrade  heatSinkType;
   private final OmniPod          centerTorsoOmniPod;

   private transient int          maxArmor = -1;

   /**
    * @param aMwoID
    * @param aMwoName
    * @param aSeries
    * @param aName
    * @param aShortName
    * @param aMaxTons
    * @param aVariant
    * @param aBaseVariant
    * @param aMovementProfile
    * @param aIsClan
    * @param aEngine
    * @param aStructureType
    * @param aArmorType
    * @param aHeatSinkType
    * @param aCenterTorso
    */
   public ChassisOmniMech(int aMwoID, String aMwoName, String aSeries, String aName, String aShortName, int aMaxTons, ChassisVariant aVariant,
                          int aBaseVariant, MovementProfile aMovementProfile, boolean aIsClan, Engine aEngine, StructureUpgrade aStructureType,
                          ArmorUpgrade aArmorType, HeatSinkUpgrade aHeatSinkType, OmniPod aCenterTorso){
      super(aMwoID, aMwoName, aSeries, aName, aShortName, aMaxTons, aVariant, aBaseVariant, aMovementProfile, aIsClan);
      engine = aEngine;
      structureType = aStructureType;
      armorType = aArmorType;
      heatSinkType = aHeatSinkType;
      centerTorsoOmniPod = aCenterTorso;
   }

   @Override
   public int getArmorMax(){
      if( maxArmor < 0 ){
         maxArmor = calculateMaxArmor();
      }
      return maxArmor;
   }

   /**
    * @return The maximal amount of armor this chassis can support.
    */
   private int calculateMaxArmor(){
      // Assuming that all omnipods for a location can carry the same amount of armor.
      int ans = 0;
      for(Location location : Location.values()){
         OmniPod omniPod = OmniPodDB.lookupOriginal(this, location);
         ans += omniPod.getArmorMax();
      }
      return ans;
   }

   /**
    * @return The engine that is fixed to this omnimech chassis.
    */
   public Engine getEngine(){
      return engine;
   }
}
