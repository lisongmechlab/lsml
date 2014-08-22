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
import lisong_mechlab.model.loadout.LoadoutBase;
import lisong_mechlab.model.loadout.LoadoutStandard;
import lisong_mechlab.model.loadout.component.ConfiguredComponentBase;
import lisong_mechlab.model.loadout.component.OpAddItem;
import lisong_mechlab.model.loadout.component.OpRemoveItem;
import lisong_mechlab.model.upgrades.Upgrades.Message;
import lisong_mechlab.model.upgrades.Upgrades.Message.ChangeMsg;
import lisong_mechlab.util.MessageXBar;
import lisong_mechlab.util.OperationStack.CompositeOperation;
import lisong_mechlab.util.OperationStack.Operation;

/**
 * This {@link Operation} changes the guidance status of a {@link LoadoutStandard}.
 * 
 * @author Li Song
 */
public class OpSetGuidanceType extends CompositeOperation{
   private final GuidanceUpgrade oldValue;
   private final GuidanceUpgrade newValue;
   private final Upgrades        upgrades;
   private final LoadoutBase<?>  loadout;
   private final MessageXBar     xBar;

   /**
    * Creates a {@link OpSetGuidanceType} that only affects a stand-alone {@link UpgradesMutable} object This is useful
    * only for altering {@link UpgradesMutable} objects which are not attached to a {@link LoadoutBase} in any way.
    * 
    * @param aUpgrades
    *           The {@link UpgradesMutable} object to alter with this {@link Operation}.
    * @param aGuidanceUpgrade
    *           The new upgrade to use.
    */
   public OpSetGuidanceType(Upgrades aUpgrades, GuidanceUpgrade aGuidanceUpgrade){
      super(aGuidanceUpgrade.getName());
      upgrades = aUpgrades;
      loadout = null;
      oldValue = upgrades.getGuidance();
      newValue = aGuidanceUpgrade;
      xBar = null;
   }

   /**
    * Creates a new {@link OpSetGuidanceType} that will change the guidance upgrade of a {@link LoadoutStandard}.
    * 
    * @param aXBar
    *           A {@link MessageXBar} to signal changes in guidance status on.
    * @param aLoadout
    *           The {@link LoadoutBase} to alter.
    * @param aGuidanceUpgrade
    *           The new upgrade to use.
    */
   public OpSetGuidanceType(MessageXBar aXBar, LoadoutBase<?> aLoadout, GuidanceUpgrade aGuidanceUpgrade){
      super(aGuidanceUpgrade.getName());
      upgrades = aLoadout.getUpgrades();
      loadout = aLoadout;
      oldValue = upgrades.getGuidance();
      newValue = aGuidanceUpgrade;
      xBar = aXBar;
   }

   @Override
   protected void apply(){
      set(newValue);
      super.apply();
   }

   @Override
   protected void undo(){
      set(oldValue);
      super.undo();
   }

   protected void set(GuidanceUpgrade aValue){
      if( aValue != upgrades.getGuidance() ){
         upgrades.setGuidance(aValue);
         if( xBar != null )
            xBar.post(new Message(ChangeMsg.GUIDANCE, upgrades));
      }
   }

   @Override
   public void buildOperation(){
      if( loadout != null ){
         if( newValue.getExtraSlots(loadout) > loadout.getNumCriticalSlotsFree() )
            throw new IllegalArgumentException("Too few critical slots available in loadout!");

         for(ConfiguredComponentBase part : loadout.getComponents()){
            if( newValue.getExtraSlots(part) > part.getSlotsFree() )
               throw new IllegalArgumentException("Too few critical slots available in " + part.getInternalComponent().getLocation() + "!");
         }

         if( newValue.getExtraTons(loadout) > loadout.getFreeMass() ){
            throw new IllegalArgumentException("Too heavy to add artmemis!");
         }

         for(ConfiguredComponentBase component : loadout.getComponents()){
            for(Item item : component.getItemsEquipped()){
               // FIXME: What about fixed missile launchers?
               if( item instanceof MissileWeapon ){
                  MissileWeapon oldWeapon = (MissileWeapon)item;
                  MissileWeapon newWeapon = newValue.upgrade(oldWeapon);
                  if( oldWeapon != newWeapon ){
                     addOp(new OpRemoveItem(xBar, loadout, component, oldWeapon));
                     addOp(new OpAddItem(xBar, loadout, component, newWeapon));
                  }
               }
               else if( item instanceof Ammunition ){
                  Ammunition oldAmmo = (Ammunition)item;
                  Ammunition newAmmo = newValue.upgrade(oldAmmo);
                  if( oldAmmo != newAmmo ){
                     addOp(new OpRemoveItem(xBar, loadout, component, oldAmmo));
                     addOp(new OpAddItem(xBar, loadout, component, newAmmo));
                  }
               }
            }
         }
      }
   }
}
