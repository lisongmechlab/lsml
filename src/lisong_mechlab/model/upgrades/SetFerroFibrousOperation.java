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
package lisong_mechlab.model.upgrades;

import lisong_mechlab.model.loadout.Loadout;
import lisong_mechlab.model.upgrades.Upgrades.Message;
import lisong_mechlab.model.upgrades.Upgrades.Message.ChangeMsg;
import lisong_mechlab.util.MessageXBar;
import lisong_mechlab.util.OperationStack.Operation;

/**
 * This {@link Upgrades} can change the armor type of a {@link Loadout}.
 * 
 * @author Emily
 */
public class SetFerroFibrousOperation extends UpgradeOperation{
   final boolean oldValue;
   final boolean newValue;

   /**
    * Creates a {@link SetFerroFibrousOperation} that only affects a stand-alone {@link Upgrades} object This is useful
    * only for altering {@link Upgrades} objects which are not attached to a {@link Loadout} in any way.
    * 
    * @param anUpgrades
    *           The {@link Upgrades} object to alter with this {@link Operation}.
    * @param aFerroFibrous
    *           The new armor type when this upgrades has been applied.
    */
   public SetFerroFibrousOperation(Upgrades anUpgrades, boolean aFerroFibrous){
      super(anUpgrades, aFerroFibrous ? "enable Ferro Fibrous" : "disable Ferro Fibrous");
      oldValue = upgrades.hasFerroFibrous();
      newValue = aFerroFibrous;
   }

   /**
    * Creates a new {@link SetEndoSteelOperation} that will change the internal structure of a {@link Loadout}.
    * 
    * @param anXBar
    *           A {@link MessageXBar} to signal changes in internal structure on.
    * @param aLoadout
    *           The {@link Loadout} to alter.
    * @param aFerroFibrous
    *           The new armor type this upgrades is applied.
    */
   public SetFerroFibrousOperation(MessageXBar anXBar, Loadout aLoadout, boolean aFerroFibrous){
      super(anXBar, aLoadout, aFerroFibrous ? "enable Ferro Fibrous" : "disable Ferro Fibrous");
      oldValue = upgrades.hasFerroFibrous();
      newValue = aFerroFibrous;
   }

   @Override
   protected void apply(){
      set(newValue);
   }

   @Override
   protected void undo(){
      set(oldValue);
   }

   protected void set(boolean aValue){
      if( aValue != upgrades.hasFerroFibrous() ){
         boolean old = upgrades.hasFerroFibrous();
         upgrades.setFerro(aValue);

         try{
            verifyLoadoutInvariant();
         }
         catch( Exception e ){
            upgrades.setFerro(old);
            throw new IllegalArgumentException("Couldn't change armour type: ", e);
         }

         if( xBar != null )
            xBar.post(new Message(ChangeMsg.ARMOR, upgrades));
      }
   }
}
