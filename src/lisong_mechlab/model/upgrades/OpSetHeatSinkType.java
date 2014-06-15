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
 * This {@link Operation} can alter the heat sink upgrade status of a {@link LoadoutStandard}.
 * 
 * @author Emily Björk
 */
public class OpSetHeatSinkType extends CompositeOperation{
   private final HeatSinkUpgrade oldValue;
   private final HeatSinkUpgrade newValue;
   private final UpgradesMutable upgrades;
   private final LoadoutStandard loadout;
   private final MessageXBar     xBar;

   /**
    * Creates a {@link OpSetHeatSinkType} that only affects a stand-alone {@link UpgradesMutable} object This is useful
    * only for altering {@link UpgradesMutable} objects which are not attached to a {@link LoadoutStandard} in any way.
    * 
    * @param aUpgrades
    *           The {@link UpgradesMutable} object to alter with this {@link Operation}.
    * @param aHeatsinkUpgrade
    *           The new heat sink type.
    */
   public OpSetHeatSinkType(UpgradesMutable aUpgrades, HeatSinkUpgrade aHeatsinkUpgrade){
      super(aHeatsinkUpgrade.getName());
      upgrades = aUpgrades;
      loadout = null;
      oldValue = upgrades.getHeatSink();
      newValue = aHeatsinkUpgrade;
      xBar = null;
   }

   /**
    * Creates a new {@link OpSetHeatSinkType} that will change the heat sink type of a {@link LoadoutStandard}.
    * 
    * @param aXBar
    *           A {@link MessageXBar} to signal changes in DHS status on.
    * @param aLoadout
    *           The {@link LoadoutStandard} to alter.
    * @param aHeatsinkUpgrade
    *           The new heat sink type.
    */
   public OpSetHeatSinkType(MessageXBar aXBar, LoadoutStandard aLoadout, HeatSinkUpgrade aHeatsinkUpgrade){
      super(aHeatsinkUpgrade.getName());
      upgrades = aLoadout.getUpgrades();
      loadout = aLoadout;
      oldValue = upgrades.getHeatSink();
      newValue = aHeatsinkUpgrade;
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

   protected void set(HeatSinkUpgrade aValue){
      if( aValue != upgrades.getHeatSink() ){
         upgrades.setHeatSink(aValue);

         if( xBar != null )
            xBar.post(new Message(ChangeMsg.HEATSINKS, upgrades));
      }
   }

   @Override
   public void buildOperation(){
      if( oldValue != newValue ){
         HeatSink oldHsType = oldValue.getHeatSinkType();
         HeatSink newHsType = newValue.getHeatSinkType();

         int globallyRemoved = 0;
         int globalEngineHs = 0;

         for(ConfiguredComponentBase component : loadout.getComponents()){
            int locallyRemoved = 0;
            for(Item item : component.getItemsEquipped()){
               if( item instanceof HeatSink ){
                  addOp(new OpRemoveItem(xBar, loadout, component, item));
                  globallyRemoved++;
                  locallyRemoved++;
               }
            }
            // Note: This will not be correct for omnimechs, but you can't change heat sink upgrades on them anyways.
            globalEngineHs += Math.min(locallyRemoved, component.getEngineHeatsinksMax());
         }

         int globalSlotsFree = (globallyRemoved - globalEngineHs) * oldHsType.getNumCriticalSlots() + loadout.getNumCriticalSlotsFree();
         int globalHsLag = 0;

         for(ConfiguredComponentBase component : loadout.getComponents()){
            int hsRemoved = 0;
            for(Item item : component.getItemsEquipped()){ // Don't remove fixed HS, not that we could on an omnimech
                                                           // anyways.
               if( item instanceof HeatSink ){
                  hsRemoved++;
               }
            }

            int hsInEngine = Math.min(hsRemoved, component.getEngineHeatsinksMax());
            int slotsFreed = (hsRemoved - hsInEngine) * oldHsType.getNumCriticalSlots();
            int slotsFree = Math.min(slotsFreed + component.getSlotsFree(), globalSlotsFree);
            int hsToAdd = Math.min(hsRemoved + globalHsLag, hsInEngine + slotsFree / newHsType.getNumCriticalSlots());

            globalSlotsFree -= newHsType.getNumCriticalSlots() * (hsToAdd - component.getEngineHeatsinksMax());
            if( hsToAdd < hsRemoved ){
               globalHsLag += hsRemoved - hsToAdd;
            }

            while( hsToAdd > 0 ){
               hsToAdd--;
               addOp(new OpAddItem(xBar, loadout, component, newHsType));
            }
         }
      }
   }
}
