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
import lisong_mechlab.model.loadout.part.LoadoutPart.Message;
import lisong_mechlab.model.loadout.part.LoadoutPart.Message.Type;
import lisong_mechlab.util.MessageXBar;
import lisong_mechlab.util.OperationStack.Operation;

/**
 * This {@link Operation} will change the armor of a {@link LoadoutPart}.
 * 
 * @author Emily Björk
 */
public class SetArmorOperation extends Operation{
   private final ArmorSide   side;
   private final int         amount;
   private int               oldAmount = -1;
   private final MessageXBar xBar;
   private final LoadoutPart loadoutPart;

   /**
    * Sets the armor for a given side of the component. Throws if the operation will fail.
    * 
    * @param anXBar
    *           The {@link MessageXBar} to announce changes to.
    * @param aLoadoutPart
    *           The {@link LoadoutPart} to change.
    * @param anArmorSide
    *           The side to set the armor for.
    * @param anArmorAmount
    *           The amount to set the armor to.
    * @throws IllegalArgumentException
    *            Thrown if the component can't take any more armor or if the loadout doesn't have enough free tonnage to
    *            support the armor.
    */
   public SetArmorOperation(MessageXBar anXBar, LoadoutPart aLoadoutPart, ArmorSide anArmorSide, int anArmorAmount){
      xBar = anXBar;
      loadoutPart = aLoadoutPart;
      side = anArmorSide;
      amount = anArmorAmount;

      if( amount < 0 )
         throw new IllegalArgumentException("Armor must be positive!");

      if( amount > loadoutPart.getInternalPart().getArmorMax() )
         throw new IllegalArgumentException("Armor must be less than components max armor!");
   }

   @Override
   public String describe(){
      return "change armor";
   }

   @Override
   protected void apply(){
      oldAmount = loadoutPart.getArmor(side);
      if( amount != oldAmount ){

         if( amount > loadoutPart.getArmorMax(side) )
            throw new IllegalArgumentException("Exceeded max armor! Max allowed: " + loadoutPart.getArmorMax(side) + " Was: " + amount);

         int armorDiff = amount - oldAmount;

         // TODO: Replace with armor handling later
         double armorTons = armorDiff / (LoadoutPart.ARMOR_PER_TON * (loadoutPart.getLoadout().getUpgrades().hasFerroFibrous() ? 1.12 : 1));
         if( armorTons > loadoutPart.getLoadout().getFreeMass() ){
            throw new IllegalArgumentException("Not enough tonnage to add more armor!");
         }
         loadoutPart.setArmor(side, amount);
         xBar.post(new Message(loadoutPart, Type.ArmorChanged));
      }
   }

   @Override
   protected void undo(){
      if( oldAmount < 0 ){
         throw new RuntimeException("Apply was not called before undo!");
      }

      if( amount != oldAmount ){
         loadoutPart.setArmor(side, oldAmount);
         xBar.post(new Message(loadoutPart, Type.ArmorChanged));
      }
      oldAmount = -1;
   }
}
