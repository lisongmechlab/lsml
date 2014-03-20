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

import lisong_mechlab.model.loadout.part.LoadoutPart;
import lisong_mechlab.model.loadout.part.StripPartOperation;
import lisong_mechlab.model.upgrades.SetArmorTypeOperation;
import lisong_mechlab.model.upgrades.SetStructureTypeOperation;
import lisong_mechlab.model.upgrades.SetGuidanceOperation;
import lisong_mechlab.model.upgrades.SetHeatSinkTypeOperation;
import lisong_mechlab.model.upgrades.UpgradeDB;
import lisong_mechlab.util.MessageXBar;

/**
 * This operation removes all armor, upgrades and items from a {@link Loadout}.
 * 
 * @author Li Song
 */
public class StripOperation extends LoadoutOperation{
   public StripOperation(Loadout aLoadout, MessageXBar anXBar){
      super(aLoadout, anXBar, "strip mech");
      
      for(LoadoutPart loadoutPart : loadout.getPartLoadOuts()){
         addOp(new StripPartOperation(xBar, loadoutPart));
      }
      addOp(new SetStructureTypeOperation(xBar, loadout, UpgradeDB.STANDARD_STRUCTURE));
      addOp(new SetGuidanceOperation(xBar, loadout, UpgradeDB.STANDARD_GUIDANCE));
      addOp(new SetArmorTypeOperation(xBar, loadout, UpgradeDB.STANDARD_ARMOR));
      addOp(new SetHeatSinkTypeOperation(xBar, loadout, UpgradeDB.STANDARD_HEATSINKS));
   }
}
