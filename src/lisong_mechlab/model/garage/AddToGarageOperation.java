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
package lisong_mechlab.model.garage;

import lisong_mechlab.model.loadout.Loadout;
import lisong_mechlab.model.loadout.OperationStack.Operation;

/**
 * This operation adds a new {@link Loadout} to a {@link MechGarage}.
 * 
 * @author Emily Björk
 */
public class AddToGarageOperation extends Operation{
   private final MechGarage garage;
   private final Loadout loadout;
   
   public AddToGarageOperation(MechGarage aGarage, Loadout aLoadout){
      garage = aGarage;
      loadout = aLoadout;
   }

   @Override
   public String describe(){
      return "add mech to garage";
   }

   @Override
   protected void apply(){
      if( garage.getMechs().contains(loadout) ){
         throw new IllegalArgumentException("The loadout \"" + loadout.getName() + "\" is already saved to the garage!");
      }
      garage.add(loadout);
   }

   @Override
   protected void undo(){
      if( !garage.getMechs().contains(loadout) ){
         throw new IllegalArgumentException("The loadout \"" + loadout.getName() + "\" is not in the garage!");
      }
      garage.remove(loadout);
   }
}
