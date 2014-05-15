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
import lisong_mechlab.model.loadout.component.ConfiguredComponent;
import lisong_mechlab.model.loadout.component.OpSetArmor;
import lisong_mechlab.util.MessageXBar;

/**
 * This operation sets the maximum amount of armor possible on a mech with a given ratio between front and back.
 * 
 * @author Li Song
 */
public class OpSetMaxArmor extends OpLoadoutBase{
   public OpSetMaxArmor(Loadout aLoadout, MessageXBar anXBar, double aRatio, boolean aManualSet){
      super(aLoadout, anXBar, "set max armor");
      for(ConfiguredComponent component : loadout.getComponents()){
         final int max = component.getInternalComponent().getArmorMax();
         if( component.getInternalComponent().getLocation().isTwoSided() ){
            // 1) front + back = max
            // 2) front / back = ratio
            // front = back * ratio
            // front = max - back
            // = > back * ratio = max - back
            int back = (int)(max / (aRatio + 1));
            int front = max - back;

            addOp(new OpSetArmor(xBar, aLoadout, component, ArmorSide.BACK, 0, aManualSet));
            addOp(new OpSetArmor(xBar, aLoadout, component, ArmorSide.FRONT, front, aManualSet));
            addOp(new OpSetArmor(xBar, aLoadout, component, ArmorSide.BACK, back, aManualSet));
         }
         else{
            addOp(new OpSetArmor(xBar, aLoadout, component, ArmorSide.ONLY, max, aManualSet));
         }
      }
   }
}
