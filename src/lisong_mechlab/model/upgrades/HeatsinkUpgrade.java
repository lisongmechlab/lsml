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

import lisong_mechlab.model.item.HeatSink;
import lisong_mechlab.model.item.ItemDB;
import lisong_mechlab.model.mwo_parsing.helpers.ItemStatsUpgradeType;

public class HeatsinkUpgrade extends Upgrade{
   private final HeatSink heatSinkType;

   public HeatsinkUpgrade(ItemStatsUpgradeType aUpgradeType){
      super(aUpgradeType);
      heatSinkType = (HeatSink)ItemDB.lookup(aUpgradeType.UpgradeTypeStats.associatedItem);
   }

   /**
    * @return The type of {@link HeatSink}s associated with this upgrade.
    */
   public HeatSink getHeatSinkType(){
      return heatSinkType;
   }

   /**
    * @return <code>true</code> if this heat sink is a double type.
    */
   public boolean isDouble(){
      return heatSinkType.getNumCriticalSlots(null) > 1;
   }

}
