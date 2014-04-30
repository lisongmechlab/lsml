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
package lisong_mechlab.model.upgrades;

import lisong_mechlab.model.item.Ammunition;
import lisong_mechlab.model.item.Item;
import lisong_mechlab.model.item.MissileWeapon;
import lisong_mechlab.model.loadout.Loadout;
import lisong_mechlab.model.loadout.part.AddItemOperation;
import lisong_mechlab.model.loadout.part.LoadoutPart;
import lisong_mechlab.model.loadout.part.RemoveItemOperation;
import lisong_mechlab.model.upgrades.Upgrades.Message;
import lisong_mechlab.model.upgrades.Upgrades.Message.ChangeMsg;
import lisong_mechlab.util.MessageXBar;
import lisong_mechlab.util.OperationStack.Operation;

/**
 * This {@link Operation} changes the guidance status of a {@link Loadout}.
 * 
 * @author Li Song
 */
public class SetGuidanceTypeOperation extends UpgradeOperation{
   private final GuidanceUpgrade oldValue;
   private final GuidanceUpgrade newValue;
   private boolean operationReady = false;

   /**
    * Creates a {@link SetGuidanceTypeOperation} that only affects a stand-alone {@link Upgrades} object This is useful only
    * for altering {@link Upgrades} objects which are not attached to a {@link Loadout} in any way.
    * 
    * @param anUpgrades
    *           The {@link Upgrades} object to alter with this {@link Operation}.
    * @param aGuidanceUpgrade
    *           The new upgrade to use.
    */
   public SetGuidanceTypeOperation(Upgrades anUpgrades, GuidanceUpgrade aGuidanceUpgrade){
      super(anUpgrades, aGuidanceUpgrade.getName());
      oldValue = upgrades.getGuidance();
      newValue = aGuidanceUpgrade;
   }

   /**
    * Creates a new {@link SetGuidanceTypeOperation} that will change the guidance upgrade of a {@link Loadout}.
    * 
    * @param anXBar
    *           A {@link MessageXBar} to signal changes in guidance status on.
    * @param aLoadout
    *           The {@link Loadout} to alter.
    * @param aGuidanceUpgrade
    *           The new upgrade to use.
    */
   public SetGuidanceTypeOperation(MessageXBar anXBar, Loadout aLoadout, GuidanceUpgrade aGuidanceUpgrade){
      super(anXBar, aLoadout, aGuidanceUpgrade.getName());
      oldValue = upgrades.getGuidance();
      newValue = aGuidanceUpgrade;
   }

   @Override
   protected void apply(){
      prepareOperation();
      set(newValue);
      super.apply();
   }

   @Override
   protected void undo(){
      super.undo();
      set(oldValue);
   }

   protected void set(GuidanceUpgrade aValue){
      if( aValue != upgrades.getGuidance() ){
         upgrades.setGuidance(aValue);
         if( xBar != null )
            xBar.post(new Message(ChangeMsg.GUIDANCE, upgrades));
      }
   }
   
   private void prepareOperation(){
      if( operationReady )
         return;
      operationReady = true;
      
      if( loadout != null ){
         if( newValue.getExtraSlots(loadout) > loadout.getNumCriticalSlotsFree() )
            throw new IllegalArgumentException("Too few critical slots available in loadout!");

         for(LoadoutPart part : loadout.getPartLoadOuts()){
            if( newValue.getExtraSlots(part) > part.getNumCriticalSlotsFree() )
               throw new IllegalArgumentException("Too few critical slots available in " + part.getInternalPart().getType() + "!");
         }

         if( newValue.getExtraTons(loadout) > loadout.getFreeMass() ){
            throw new IllegalArgumentException("Too heavy to add artmemis!");
         }

         for(LoadoutPart part : loadout.getPartLoadOuts()){
            for(Item item : part.getItems()){
               if( item instanceof MissileWeapon ){
                  MissileWeapon oldWeapon = (MissileWeapon)item;
                  MissileWeapon newWeapon = newValue.upgrade(oldWeapon);
                  if( oldWeapon != newWeapon ){
                     addOp(new RemoveItemOperation(xBar, part, oldWeapon));
                     addOp(new AddItemOperation(xBar, part, newWeapon));
                  }
               }
               else if( item instanceof Ammunition ){
                  Ammunition oldAmmo = (Ammunition)item;
                  Ammunition newAmmo = newValue.upgrade(oldAmmo);
                  if( oldAmmo != newAmmo ){
                     addOp(new RemoveItemOperation(xBar, part, oldAmmo));
                     addOp(new AddItemOperation(xBar, part, newAmmo));
                  }
               }
            }
         }
      }
   }
}
