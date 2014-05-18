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
import lisong_mechlab.model.chassi.Location;
import lisong_mechlab.model.chassi.MovementProfile;
import lisong_mechlab.model.chassi.MovementProfileSum;
import lisong_mechlab.model.chassi.OmniPod;
import lisong_mechlab.model.item.Engine;
import lisong_mechlab.model.item.Item;
import lisong_mechlab.model.loadout.component.ComponentBuilder;
import lisong_mechlab.model.loadout.component.ComponentBuilder.Factory;
import lisong_mechlab.model.loadout.component.ConfiguredOmniPod;
import lisong_mechlab.util.MessageXBar;
import lisong_mechlab.util.OperationStack.Operation;

/**
 * @author Emily Björk
 */
public class LoadoutOmniMech extends LoadoutBase<ConfiguredOmniPod, OmniPod>{
   transient private final MovementProfileSum movementProfile;

   /**
    * @param aFactory
    * @param aChassis
    * @param aXBar
    */
   public LoadoutOmniMech(Factory<ConfiguredOmniPod, OmniPod> aFactory, ChassisOmniMech aChassis, MessageXBar aXBar){
      super(aFactory, aChassis, aXBar);
      movementProfile = new MovementProfileSum(aChassis.getMovementProfile());     
   }

   /**
    * @param aOmniPodFactory
    * @param aLoadoutOmniMech
    */
   public LoadoutOmniMech(Factory<ConfiguredOmniPod, OmniPod> aOmniPodFactory, LoadoutOmniMech aLoadoutOmniMech){
      super(aOmniPodFactory, aLoadoutOmniMech);
      movementProfile = new MovementProfileSum(getChassis().getMovementProfile());
      for(ConfiguredOmniPod omniPod : getComponents()){
         movementProfile.addMovementProfile(omniPod.getInternalComponent().getQuirks());
      }
   }

   /**
    * This setter method is only intended to be used from package local {@link Operation}s. It's a raw, unchecked
    * accessor.
    * 
    * @param aOmniPod
    *           The omnipod to set, it's put in it's dedicated slot.
    */
   public void setOmniPod(OmniPod aOmniPod){
      movementProfile.removeMovementProfile(getComponent(aOmniPod.getLocation()).getInternalComponent().getQuirks());
      movementProfile.addMovementProfile(aOmniPod.getQuirks());
      setOmniPod(aOmniPod);
   }

   @Override
   public MovementProfile getMovementProfile(){
      return movementProfile;
   }

   @Override
   public int getJumpJetsMax(){
      int ans = 0;
      for(ConfiguredOmniPod omniPod : getComponents()){
         ans += omniPod.getInternalComponent().getJumpJetsMax();
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
}
