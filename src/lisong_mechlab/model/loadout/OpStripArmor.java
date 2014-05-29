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
import lisong_mechlab.model.loadout.component.ConfiguredComponentBase;
import lisong_mechlab.model.loadout.component.OpSetArmor;
import lisong_mechlab.util.MessageXBar;

/**
 * This operation removes all armor from a {@link LoadoutStandard}.
 * 
 * @author Li Song
 */
public class OpStripArmor extends OpLoadoutBase{
   public OpStripArmor(LoadoutBase<?, ?> aLoadout, MessageXBar anXBar){
      super(aLoadout, anXBar, "strip armor");
      for(ConfiguredComponentBase component : loadout.getComponents()){
         if( component.getInternalComponent().getLocation().isTwoSided() ){
            addOp(new OpSetArmor(xBar, aLoadout, component, ArmorSide.FRONT, 0, true));
            addOp(new OpSetArmor(xBar, aLoadout, component, ArmorSide.BACK, 0, true));
         }
         else{
            addOp(new OpSetArmor(xBar, aLoadout, component, ArmorSide.ONLY, 0, true));
         }
      }
   }
}
