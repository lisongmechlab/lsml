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
package lisong_mechlab.model.loadout;

import lisong_mechlab.model.loadout.component.ConfiguredComponent;
import lisong_mechlab.model.loadout.component.OpStripComponent;
import lisong_mechlab.model.upgrades.OpSetArmorType;
import lisong_mechlab.model.upgrades.OpSetGuidanceType;
import lisong_mechlab.model.upgrades.OpSetHeatSinkType;
import lisong_mechlab.model.upgrades.OpSetStructureType;
import lisong_mechlab.model.upgrades.UpgradeDB;
import lisong_mechlab.util.MessageXBar;

/**
 * This operation removes all armor, upgrades and items from a {@link LoadoutStandard}.
 * 
 * @author Li Song
 */
public class OpStripLoadout extends OpLoadoutBase{
   public OpStripLoadout(LoadoutBase<?, ?> aLoadout, MessageXBar aXBar){
      super(aLoadout, aXBar, "strip mech");

      for(ConfiguredComponent component : loadout.getComponents()){
         addOp(new OpStripComponent(xBar, loadout, component));
      }

      if( aLoadout instanceof LoadoutStandard ){
         LoadoutStandard loadoutStandard = (LoadoutStandard)aLoadout;
         addOp(new OpSetStructureType(xBar, loadoutStandard, UpgradeDB.STANDARD_STRUCTURE));
         addOp(new OpSetGuidanceType(xBar, loadoutStandard, UpgradeDB.STANDARD_GUIDANCE));
         addOp(new OpSetArmorType(xBar, loadoutStandard, UpgradeDB.STANDARD_ARMOR));
         addOp(new OpSetHeatSinkType(xBar, loadoutStandard, UpgradeDB.STANDARD_HEATSINKS));
      }
   }
}
