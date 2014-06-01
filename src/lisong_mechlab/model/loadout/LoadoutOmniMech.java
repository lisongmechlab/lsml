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
package lisong_mechlab.model.loadout;

import lisong_mechlab.model.chassi.ChassisOmniMech;
import lisong_mechlab.model.chassi.MovementProfile;
import lisong_mechlab.model.chassi.MovementProfileSum;
import lisong_mechlab.model.chassi.OmniPod;
import lisong_mechlab.model.item.Engine;
import lisong_mechlab.model.item.Item;
import lisong_mechlab.model.loadout.component.ComponentBuilder;
import lisong_mechlab.model.loadout.component.ComponentBuilder.Factory;
import lisong_mechlab.model.loadout.component.ConfiguredComponentOmniMech;
import lisong_mechlab.model.upgrades.UpgradeDB;
import lisong_mechlab.model.upgrades.Upgrades;
import lisong_mechlab.util.MessageXBar;
import lisong_mechlab.util.OperationStack.Operation;

/**
 * This class represents a configured loadout for an omnimech.
 * 
 * @author Emily Björk
 */
public class LoadoutOmniMech extends LoadoutBase<ConfiguredComponentOmniMech>{
   transient private final MovementProfileSum movementProfile;
   transient private final Upgrades           upgrades;

   /**
    * @param aFactory
    * @param aChassis
    * @param aXBar
    */
   public LoadoutOmniMech(Factory<ConfiguredComponentOmniMech> aFactory, ChassisOmniMech aChassis, MessageXBar aXBar){
      super(aFactory, aChassis, aXBar);
      movementProfile = new MovementProfileSum(aChassis.getMovementProfileBase());
      upgrades = new Upgrades(aChassis.getArmorType(), aChassis.getStructureType(), UpgradeDB.STANDARD_GUIDANCE, aChassis.getHeatSinkType());
      for(ConfiguredComponentOmniMech component : getComponents()){
         movementProfile.addMovementProfile(component.getOmniPod().getQuirks());
      }
   }

   /**
    * @param aOmniPodFactory
    * @param aLoadoutOmniMech
    */
   public LoadoutOmniMech(Factory<ConfiguredComponentOmniMech> aOmniPodFactory, LoadoutOmniMech aLoadoutOmniMech){
      super(aOmniPodFactory, aLoadoutOmniMech);
      movementProfile = new MovementProfileSum(getChassis().getMovementProfileBase());
      for(ConfiguredComponentOmniMech component : getComponents()){
         movementProfile.addMovementProfile(component.getOmniPod().getQuirks());
      }
      upgrades = new Upgrades(aLoadoutOmniMech.getUpgrades());
   }

   /**
    * This setter method is only intended to be used from package local {@link Operation}s. It's a raw, unchecked
    * accessor.
    * 
    * @param aOmniPod
    *           The omnipod to set, it's put in it's dedicated slot.
    */
   void setOmniPod(OmniPod aOmniPod){
      ConfiguredComponentOmniMech component = getComponent(aOmniPod.getLocation());
      movementProfile.removeMovementProfile(component.getOmniPod().getQuirks());
      movementProfile.addMovementProfile(aOmniPod.getQuirks());
      component.setOmniPod(aOmniPod);
   }

   @Override
   public MovementProfile getMovementProfile(){
      return movementProfile;
   }

   @Override
   public int getJumpJetsMax(){
      int ans = 0;
      for(ConfiguredComponentOmniMech component : getComponents()){
         ans += component.getOmniPod().getJumpJetsMax();
      }
      return ans;
   }

   @Override
   public ChassisOmniMech getChassis(){
      return (ChassisOmniMech)super.getChassis();
   }

   @Override
   protected boolean canEquipGlobal(Item aItem){
      if( aItem instanceof Engine )
         return false;
      return super.canEquipGlobal(aItem);
   }

   @Override
   public LoadoutOmniMech clone(MessageXBar aXBar){
      return new LoadoutOmniMech(ComponentBuilder.getOmniPodFactory(), this);
   }

   @Override
   public Engine getEngine(){
      return getChassis().getEngine();
   }

   /**
    * @return The number of globally used critical slots.
    */
   @Override
   public int getNumCriticalSlotsUsed(){
      int ans = 0;
      for(ConfiguredComponentOmniMech component : getComponents()){
         ans += component.getSlotsUsed();
      }
      return ans;
   }

   @Override
   public Upgrades getUpgrades(){
      return upgrades;
   }
}
