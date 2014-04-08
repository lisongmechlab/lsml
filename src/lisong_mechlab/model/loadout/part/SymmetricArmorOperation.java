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
package lisong_mechlab.model.loadout.part;

import lisong_mechlab.model.chassi.ArmorSide;
import lisong_mechlab.model.chassi.Part;
import lisong_mechlab.model.loadout.Loadout;
import lisong_mechlab.util.MessageXBar;
import lisong_mechlab.util.OperationStack.CompositeOperation;
import lisong_mechlab.util.OperationStack.Operation;

/**
 * This {@link Operation} sets armor symmetrically on both sides of a {@link Loadout}.
 * 
 * @author Emily Björk
 */
public class SymmetricArmorOperation extends CompositeOperation{
   private final LoadoutPart part;
   private final ArmorSide   side;
   private final boolean     manual;

   /**
    * Creates a new {@link SymmetricArmorOperation}.
    * 
    * @param anXBar
    *           The {@link MessageXBar} to announce changes to.
    * @param aLoadoutPart
    *           The primary side {@link LoadoutPart} to change (the opposite side will be changed automatically).
    * @param anArmorSide
    *           The side to set the armor for.
    * @param anArmorAmount
    *           The amount to set the armor to.
    * @param aManualSet
    *           True if this set operation is done manually. Will disable automatic armor assignments.
    * @throws IllegalArgumentException
    *            Thrown if the component can't take any more armor or if the loadout doesn't have enough free tonnage to
    *            support the armor.
    */
   public SymmetricArmorOperation(MessageXBar anXBar, LoadoutPart aLoadoutPart, ArmorSide anArmorSide, int anArmorAmount, boolean aManualSet){
      super("change armor");

      part = aLoadoutPart;
      side = anArmorSide;
      manual = aManualSet;

      Part otherSide = aLoadoutPart.getInternalPart().getType().oppositeSide();
      if( otherSide == null )
         throw new IllegalArgumentException("Symmetric armor operation is only usable with comoponents that have an opposing side.");

      addOp(new SetArmorOperation(anXBar, aLoadoutPart, anArmorSide, anArmorAmount, aManualSet));
      addOp(new SetArmorOperation(anXBar, aLoadoutPart.getLoadout().getPart(otherSide), anArmorSide, anArmorAmount, aManualSet));
   }

   /**
    * @see lisong_mechlab.util.OperationStack.Operation#canCoalescele(lisong_mechlab.util.OperationStack.Operation)
    */
   @Override
   public boolean canCoalescele(Operation aOperation){
      if( this == aOperation )
         return false;
      if( aOperation == null )
         return false;
      if( !(aOperation instanceof SymmetricArmorOperation) )
         return false;
      SymmetricArmorOperation that = (SymmetricArmorOperation)aOperation;
      if( that.manual != manual )
         return false;
      if( that.part != part  && that.part != part.getLoadout().getPart(part.getInternalPart().getType().oppositeSide()))
         return false;
      if( that.side != side )
         return false;
      return true;
   }

}
