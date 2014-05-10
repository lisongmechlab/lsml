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
package lisong_mechlab.model.upgrades;

import lisong_mechlab.model.chassi.Chassis;
import lisong_mechlab.mwo_data.helpers.ItemStatsUpgradeType;

/**
 * Represents an upgrade to a 'mechs internal structure.
 * 
 * @author Li Song
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
   public double getStructureMass(Chassis aChassis){
      double ans = aChassis.getMassMax() * internalStructurePct;

      return Math.round(10 * ans / 5) * 0.5;
   }
}
