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

import lisong_mechlab.model.item.HeatSink;
import lisong_mechlab.model.item.ItemDB;
import lisong_mechlab.mwo_data.helpers.ItemStatsUpgradeType;

public class HeatSinkUpgrade extends Upgrade{
   private HeatSink heatSinkType;

   public HeatSinkUpgrade(ItemStatsUpgradeType aUpgradeType){
      super(aUpgradeType);
   }

   /**
    * @return The type of {@link HeatSink}s associated with this upgrade.
    */
   public HeatSink getHeatSinkType(){
      if( heatSinkType == null )
         heatSinkType = (HeatSink)ItemDB.lookup(getAssociateItemId());
      return heatSinkType;
   }

   /**
    * @return <code>true</code> if this heat sink is a double type.
    */
   public boolean isDouble(){
      return getHeatSinkType().getNumCriticalSlots(null) > 1;
   }

}
