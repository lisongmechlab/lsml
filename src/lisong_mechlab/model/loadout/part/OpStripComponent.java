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
package lisong_mechlab.model.loadout.part;

import lisong_mechlab.model.chassi.ArmorSide;
import lisong_mechlab.model.item.HeatSink;
import lisong_mechlab.model.item.Internal;
import lisong_mechlab.model.item.Item;
import lisong_mechlab.util.MessageXBar;
import lisong_mechlab.util.OperationStack.CompositeOperation;
import lisong_mechlab.util.OperationStack.Operation;

/**
 * This {@link Operation} will remove all items and armor on this component.
 * 
 * @author Li Song
 */
public class OpStripComponent extends CompositeOperation{
   /**
    * @param aLoadoutPart
    *           The {@link ConfiguredComponent} to strip.
    * @param anXBar
    *           Where to announce changes from this operation.
    */
   public OpStripComponent(MessageXBar anXBar, ConfiguredComponent aLoadoutPart){
      super("strip part");
      // Engine heat sinks are removed together with the engine.
      int hsSkipp = aLoadoutPart.getNumEngineHeatsinks();
      for(Item item : aLoadoutPart.getItems()){
         if( !(item instanceof Internal) ){
            if( item instanceof HeatSink ){
               if( hsSkipp > 0 ){
                  hsSkipp--;
                  continue;
               }
            }
            addOp(new OpRemoveItem(anXBar, aLoadoutPart, item));
         }
      }
      if( aLoadoutPart.getInternalPart().getLocation().isTwoSided() ){
         addOp(new OpSetArmor(anXBar, aLoadoutPart, ArmorSide.FRONT, 0, false));
         addOp(new OpSetArmor(anXBar, aLoadoutPart, ArmorSide.BACK, 0, false));
      }
      else{
         addOp(new OpSetArmor(anXBar, aLoadoutPart, ArmorSide.ONLY, 0, false));
      }
   }
}
