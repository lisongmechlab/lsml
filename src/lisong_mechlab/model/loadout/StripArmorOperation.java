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

import lisong_mechlab.model.chassi.ArmorSide;
import lisong_mechlab.model.loadout.part.LoadoutPart;
import lisong_mechlab.model.loadout.part.SetArmorOperation;
import lisong_mechlab.util.MessageXBar;

/**
 * This operation removes all armor from a {@link Loadout}.
 * 
 * @author Li Song
 */
public class StripArmorOperation extends LoadoutOperation{
   public StripArmorOperation(Loadout aLoadout, MessageXBar anXBar){
      super(aLoadout, anXBar, "strip armor");
      for(LoadoutPart loadoutPart : loadout.getPartLoadOuts()){
         if( loadoutPart.getInternalPart().getType().isTwoSided() ){
            addOp(new SetArmorOperation(xBar, loadoutPart, ArmorSide.FRONT, 0, true));
            addOp(new SetArmorOperation(xBar, loadoutPart, ArmorSide.BACK, 0, true));
         }
         else{
            addOp(new SetArmorOperation(xBar, loadoutPart, ArmorSide.ONLY, 0, true));
         }
      }
   }
}
