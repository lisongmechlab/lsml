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

import lisong_mechlab.model.chassi.Part;
import lisong_mechlab.model.item.HeatSink;
import lisong_mechlab.model.item.Item;
import lisong_mechlab.model.loadout.part.AddItemOperation;
import lisong_mechlab.model.loadout.part.LoadoutPart;
import lisong_mechlab.util.MessageXBar;

/**
 * This operation automatically places an item at a suitable location on the {@link Loadout}.
 * 
 * @author Li Song
 */
public class AutoAddItemOperation extends LoadoutOperation{
   public AutoAddItemOperation(Loadout aLoadout, MessageXBar anXBar, Item anItem){
      super(aLoadout, anXBar, "auto place item");
      LoadoutPart ct = loadout.getPart(Part.CenterTorso);
      if( anItem instanceof HeatSink && ct.getNumEngineHeatsinks() < ct.getNumEngineHeatsinksMax() && ct.canAddItem(anItem) ){
         addOp(new AddItemOperation(xBar, ct, anItem));
         return;
      }

      Part[] partOrder = new Part[] {Part.RightArm, Part.RightTorso, Part.RightLeg, Part.Head, Part.CenterTorso, Part.LeftTorso, Part.LeftLeg,
            Part.LeftArm};

      for(Part part : partOrder){
         LoadoutPart loadoutPart = loadout.getPart(part);
         if( loadoutPart.canAddItem(anItem) ){
            addOp(new AddItemOperation(xBar, loadoutPart, anItem));
            return;
         }
      }
   }
}
