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
package lisong_mechlab.model.loadout.component;

import lisong_mechlab.model.chassi.ArmorSide;
import lisong_mechlab.model.loadout.LoadoutBase;
import lisong_mechlab.model.loadout.component.ConfiguredComponentBase.Message;
import lisong_mechlab.model.loadout.component.ConfiguredComponentBase.Message.Type;
import lisong_mechlab.util.MessageXBar;
import lisong_mechlab.util.OperationStack.Operation;

/**
 * This {@link Operation} will change the armor of a {@link ConfiguredComponentBase}.
 * 
 * @author Li Song
 */
public class OpSetArmor extends Operation{
   private final ArmorSide           side;
   private final int                 amount;
   private int                       oldAmount = -1;
   private boolean                   oldManual;
   private final MessageXBar         xBar;
   private final LoadoutBase<?, ?>   loadout;
   private final ConfiguredComponentBase loadoutPart;
   private final boolean             manual;

   /**
    * Sets the armor for a given side of the component. Throws if the operation will fail.
    * 
    * @param anXBar
    *           The {@link MessageXBar} to announce changes to.
    * @param aLoadout
    *           The {@link LoadoutBase} to change.
    * @param aLoadoutPart
    *           The {@link ConfiguredComponentBase} to change.
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
   public OpSetArmor(MessageXBar anXBar, LoadoutBase<?, ?> aLoadout, ConfiguredComponentBase aLoadoutPart, ArmorSide anArmorSide, int anArmorAmount,
                     boolean aManualSet){
      xBar = anXBar;
      loadout = aLoadout;
      loadoutPart = aLoadoutPart;
      side = anArmorSide;
      amount = anArmorAmount;
      manual = aManualSet;

      if( amount < 0 )
         throw new IllegalArgumentException("Armor must be positive!");

      if( amount > loadoutPart.getInternalComponent().getArmorMax() )
         throw new IllegalArgumentException("Armor must be less than components max armor!");
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
      if( !(aOperation instanceof OpSetArmor) )
         return false;
      OpSetArmor that = (OpSetArmor)aOperation;
      if( that.manual != manual )
         return false;
      if( that.loadoutPart != loadoutPart )
         return false;
      if( that.side != side )
         return false;
      return true;
   }

   @Override
   public String describe(){
      return "change armor";
   }

   @Override
   protected void apply(){
      oldAmount = loadoutPart.getArmor(side);
      oldManual = !loadoutPart.allowAutomaticArmor();
      if( amount != oldAmount || oldManual != manual ){

         if( amount > loadoutPart.getArmorMax(side) )
            throw new IllegalArgumentException("Exceeded max armor! Max allowed: " + loadoutPart.getArmorMax(side) + " Was: " + amount);

         int armorDiff = amount - oldAmount;
         double armorTons = loadout.getUpgrades().getArmor().getArmorMass(armorDiff);
         if( armorTons > loadout.getFreeMass() ){
            // See if the armor can be freed from a combination of automatic components. They will be redistributed
            // afterwards. FIXME: Devise a proper solution, this is ugly.
            int freed = 0;
            if( manual == true && freed < armorDiff ){
               for(ConfiguredComponentBase otherPart : loadout.getComponents()){
                  if( loadoutPart != otherPart && otherPart.allowAutomaticArmor() ){
                     freed += otherPart.getArmorTotal();
                     if( otherPart.getInternalComponent().getLocation().isTwoSided() ){
                        otherPart.setArmor(ArmorSide.FRONT, 0, true);
                        otherPart.setArmor(ArmorSide.BACK, 0, true);
                     }
                     else{
                        otherPart.setArmor(ArmorSide.ONLY, 0, true);
                     }
                  }
               }
            }
            if( freed < armorDiff ){
               throw new IllegalArgumentException("Not enough tonnage to add more armor!");
            }
         }
         loadoutPart.setArmor(side, amount, !manual);
         if( xBar != null ){
            xBar.post(new Message(loadoutPart, Type.ArmorChanged, !manual));
         }
      }
   }

   @Override
   protected void undo(){
      if( oldAmount < 0 ){
         throw new RuntimeException("Apply was not called before undo!");
      }

      if( amount != oldAmount || oldManual != manual ){
         loadoutPart.setArmor(side, oldAmount, !oldManual);
         if( xBar != null ){
            xBar.post(new Message(loadoutPart, Type.ArmorChanged, !manual));
         }
      }
      oldAmount = -1;
   }
}
