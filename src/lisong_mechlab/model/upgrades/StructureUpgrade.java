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
package lisong_mechlab.model.upgrades;

import lisong_mechlab.model.chassi.Chassi;
import lisong_mechlab.model.mwo_parsing.helpers.ItemStatsUpgradeType;

/**
 * Represents an upgrade to a 'mechs internal structure.
 * 
 * @author Emily Björk
 */
public class StructureUpgrade extends Upgrade{
   private final double internalStructurePct;
   private final int    extraSlots;

   public StructureUpgrade(ItemStatsUpgradeType aUpgradeType){
      super(aUpgradeType);

      internalStructurePct = aUpgradeType.UpgradeTypeStats.pointMultiplier;
      extraSlots = aUpgradeType.UpgradeTypeStats.slots;
   }

   /**
    * @return The number of extra slots that this upgrade requires to be applied.
    */
   public int getExtraSlots(){
      return extraSlots;
   }

   /**
    * Calculates the mass of the internal structure of a mech of the given chassis.
    * 
    * @param aChassis
    *           The chassis to calculate the internal structure mass for.
    * @return The mass of the internal structure.
    */
   public double getStructureMass(Chassi aChassis){
      return (aChassis.getMassMax() + 9) / 10 * 10 * internalStructurePct;
//      ans *= 0.5;
//      ans += (chassi.getMassMax() % 10) * 0.05; // TODO: Replace with proper upgrade handling
   }
}
