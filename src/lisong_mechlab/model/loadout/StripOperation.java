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
import lisong_mechlab.model.upgrades.SetArtemisOperation;
import lisong_mechlab.model.upgrades.SetDHSOperation;
import lisong_mechlab.model.upgrades.SetEndoSteelOperation;
import lisong_mechlab.model.upgrades.SetFerroFibrousOperation;
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
      addOp(new SetEndoSteelOperation(xBar, loadout, false));
      addOp(new SetArtemisOperation(xBar, loadout, false));
      addOp(new SetFerroFibrousOperation(xBar, loadout, false));
      addOp(new SetDHSOperation(xBar, loadout, false));
   }
}
