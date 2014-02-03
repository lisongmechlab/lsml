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

import lisong_mechlab.model.item.HeatSink;
import lisong_mechlab.model.item.Item;
import lisong_mechlab.model.item.ItemDB;
import lisong_mechlab.model.loadout.Loadout;
import lisong_mechlab.model.loadout.OperationStack.Operation;
import lisong_mechlab.model.loadout.part.AddItemOperation;
import lisong_mechlab.model.loadout.part.LoadoutPart;
import lisong_mechlab.model.loadout.part.RemoveItemOperation;
import lisong_mechlab.model.upgrades.Upgrades.Message;
import lisong_mechlab.model.upgrades.Upgrades.Message.ChangeMsg;
import lisong_mechlab.util.MessageXBar;

/**
 * This {@link Operation} can alter the heat sink upgrade status of a {@link Loadout}.
 * 
 * @author Emily Björk
 */
public class SetDHSOperation extends UpgradeOperation{
   final boolean oldValue;
   final boolean newValue;

   /**
    * Creates a {@link SetDHSOperation} that only affects a stand-alone {@link Upgrades} object This is useful only for
    * altering {@link Upgrades} objects which are not attached to a {@link Loadout} in any way.
    * 
    * @param anUpgrades
    *           The {@link Upgrades} object to alter with this {@link Operation}.
    * @param aDHS
    *           The new status of DHS when this upgrades has been applied.
    */
   public SetDHSOperation(Upgrades anUpgrades, boolean aDHS){
      super(anUpgrades, aDHS ? "enable DHS" : "disable DHS");
      oldValue = upgrades.hasDoubleHeatSinks();
      newValue = aDHS;
   }

   /**
    * Creates a new {@link SetDHSOperation} that will change the DHS status of a {@link Loadout}.
    * 
    * @param anXBar
    *           A {@link MessageXBar} to signal changes in DHS status on.
    * @param aLoadout
    *           The {@link Loadout} to alter.
    * @param aDHS
    *           The new DHS status after this upgrades is applied.
    */
   public SetDHSOperation(MessageXBar anXBar, Loadout aLoadout, boolean aDHS){
      super(anXBar, aLoadout, aDHS ? "enable DHS" : "disable DHS");
      oldValue = upgrades.hasDoubleHeatSinks();
      newValue = aDHS;

      if( oldValue != newValue ){
         for(LoadoutPart loadoutPart : loadout.getPartLoadOuts()){
            int hsRemoved = 0;
            for(Item item : loadoutPart.getItems()){
               if( item instanceof HeatSink ){
                  addOp(new RemoveItemOperation(xBar, loadoutPart, item));
                  hsRemoved++;
               }
            }

            HeatSink oldHsType = oldValue ? ItemDB.DHS : ItemDB.SHS;
            HeatSink newHsType = newValue ? ItemDB.DHS : ItemDB.SHS;
            int slotsFree = oldHsType.getNumCriticalSlots(upgrades) * hsRemoved + loadoutPart.getNumCriticalSlotsFree();
            int hsToAdd = Math.min(hsRemoved, slotsFree / newHsType.getNumCriticalSlots(upgrades));
            while( hsToAdd > 0 ){
               hsToAdd--;
               addOp(new AddItemOperation(xBar, loadoutPart, newHsType));
            }
         }
      }
   }

   @Override
   protected void apply(){
      set(newValue);
      super.apply();
   }

   @Override
   protected void undo(){
      super.undo();
      set(oldValue);
   }

   protected void set(boolean aValue){
      if( aValue != upgrades.hasDoubleHeatSinks() ){
         boolean old = upgrades.hasDoubleHeatSinks();
         upgrades.setDHS(aValue);

         try{
            verifyLoadoutInvariant();
         }
         catch( Exception e ){
            upgrades.setDHS(old);
            throw new IllegalArgumentException("Couldn't change heat sinks: ", e);
         }

         if( xBar != null )
            xBar.post(new Message(ChangeMsg.HEATSINKS, upgrades));
      }
   }
}
