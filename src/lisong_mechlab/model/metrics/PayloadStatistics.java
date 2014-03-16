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

import lisong_mechlab.model.chassi.Chassis;
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
    * @param aChassi
    * @param anEngineRating
    * @return the calculated value
    */
   public double calculate(Chassis aChassi, int anEngineRating){
      double internalMass = upgrades.getStructure().getStructureMass(aChassi);
      double maxPayload = aChassi.getMassMax() - internalMass;

      Engine engine = (Engine)ItemDB.lookup((xlEngine ? "XL" : "STD") + " ENGINE " + anEngineRating);
      maxPayload -= engine.getMass(null);
      maxPayload -= 10 - engine.getNumInternalHeatsinks();

      if( maxArmor ){
         maxPayload -= upgrades.getArmor().getArmorMass(aChassi.getArmorMax());
      }

      return maxPayload;
   }
}
