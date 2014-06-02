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
package lisong_mechlab.model.metrics;

import lisong_mechlab.model.chassi.ChassisOmniMech;
import lisong_mechlab.model.chassi.ChassisStandard;
import lisong_mechlab.model.item.Engine;
import lisong_mechlab.model.item.ItemDB;
import lisong_mechlab.model.upgrades.Upgrades;

/**
 * This class calculates how many tons of payload a mech chassis can carry given a specific engine rating/type,
 * upgrades, and with max/no armor.
 * 
 * @author Emily Björk
 */
public class PayloadStatistics{

   private boolean  xlEngine;
   private boolean  maxArmor;
   private Upgrades upgrades;

   public PayloadStatistics(boolean aUseXlEngine, boolean aUseMaxArmor, Upgrades anUpgrades){
      xlEngine = aUseXlEngine;
      maxArmor = aUseMaxArmor;
      upgrades = anUpgrades;
   }

   public void changeUseXLEngine(boolean aUseXlEngine){
      xlEngine = aUseXlEngine;
   }

   public void changeUseMaxArmor(boolean aUseMaxArmor){
      maxArmor = aUseMaxArmor;
   }

   public void changeUpgrades(Upgrades anUpgrades){
      upgrades = anUpgrades;
   }

   /**
    * Calculates the payload tonnage for the given {@link ChassisStandard}, with the given engine rating under the
    * upgrades that have been given to the constructor. Will consider the status of the
    * {@link #changeUpgrades(Upgrades)}, {@link #changeUseMaxArmor(boolean)} and {@link #changeUseXLEngine(boolean)}.
    * <p>
    * If the engine is smaller than 250, then additional heat sinks required to operate the mech will be subtracted from
    * the payload.
    * 
    * @param aChassis
    *           The {@link ChassisStandard} to calculate for.
    * @param aEngineRating
    *           The engine rating to use.
    * @return The payload tonnage.
    */
   public double calculate(ChassisStandard aChassis, int aEngineRating){
      double internalMass = upgrades.getStructure().getStructureMass(aChassis);
      double maxPayload = aChassis.getMassMax() - internalMass;

      Engine engine = (Engine)ItemDB.lookup((xlEngine ? "XL" : "STD") + " ENGINE " + aEngineRating);
      maxPayload -= engine.getMass();
      maxPayload -= 10 - engine.getNumInternalHeatsinks();

      if( maxArmor ){
         maxPayload -= upgrades.getArmor().getArmorMass(aChassis.getArmorMax());
      }

      return maxPayload;
   }

   public double calculate(ChassisOmniMech aChassis){
      double ans = aChassis.getMassMax() - aChassis.getMassStripped() - (10 - aChassis.getFixedHeatSinks());
      if(maxArmor){
         ans -= aChassis.getArmorType().getArmorMass(aChassis.getArmorMax());
      }
      return ans;
   }
}
