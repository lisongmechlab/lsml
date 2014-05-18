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

import lisong_mechlab.model.chassi.ChassisBase;
import lisong_mechlab.model.chassi.ChassisOmniMech;
import lisong_mechlab.model.chassi.ChassisStandard;
import lisong_mechlab.model.chassi.InternalComponent;
import lisong_mechlab.model.chassi.Location;
import lisong_mechlab.model.chassi.OmniPod;
import lisong_mechlab.model.chassi.OmniPodDB;
import lisong_mechlab.model.item.EngineType;
import lisong_mechlab.model.loadout.LoadoutBase;

/**
 * This factory object can construct configured components.
 * 
 * @author Li Song
 */
public class ComponentBuilder{
   public interface Factory<T extends ConfiguredComponent, U extends InternalComponent> {
      T[] cloneComponents(LoadoutBase<T, U> aLoadout);

      T[] defaultComponents(ChassisBase aChassis);
   }

   private static class StandardFactory implements Factory<ConfiguredComponent, InternalComponent>{
      @Override
      public ConfiguredComponent[] cloneComponents(LoadoutBase<ConfiguredComponent, InternalComponent> aLoadout){
         ConfiguredComponent[] ans = new ConfiguredComponent[Location.values().length];
         for(ConfiguredComponent component : aLoadout.getComponents()){
            ans[component.getInternalComponent().getLocation().ordinal()] = new ConfiguredComponent(component);
         }
         return ans;
      }

      @Override
      public ConfiguredComponent[] defaultComponents(ChassisBase aChassis){
         ChassisStandard chassis = (ChassisStandard)aChassis;
         ConfiguredComponent[] ans = new ConfiguredComponent[Location.values().length];
         for(InternalComponent component : chassis.getComponents()){
            ans[component.getLocation().ordinal()] = new ConfiguredComponent(component, true);
         }
         return ans;
      }
   }

   private static class OmniMechFactory implements Factory<ConfiguredOmniPod, OmniPod>{
      @Override
      public ConfiguredOmniPod[] cloneComponents(LoadoutBase<ConfiguredOmniPod, OmniPod> aLoadout){
         ConfiguredOmniPod[] ans = new ConfiguredOmniPod[Location.values().length];
         for(Location location : Location.values()){
            ans[location.ordinal()] = new ConfiguredOmniPod(aLoadout.getComponent(location));
         }
         return ans;
      }

      @Override
      public ConfiguredOmniPod[] defaultComponents(ChassisBase aChassis){
         ChassisOmniMech omniMech = (ChassisOmniMech)aChassis;
         ConfiguredOmniPod[] ans = new ConfiguredOmniPod[Location.values().length];
         for(Location location : Location.values()){
            ans[location.ordinal()] = new ConfiguredOmniPod(OmniPodDB.lookupOriginal(omniMech, location));
            if( location == Location.CenterTorso ){
               ans[location.ordinal()].addItem(omniMech.getEngine());
            }
            else if(location == Location.LeftTorso || location == Location.RightTorso && omniMech.getEngine().getType() == EngineType.XL){
               ans[location.ordinal()].addItem(ConfiguredOmniPod.ENGINE_INTERNAL_CLAN);
            }
         }
         return ans;
      }
   }

   private static Factory<ConfiguredComponent, InternalComponent> is   = new StandardFactory();
   private static Factory<ConfiguredOmniPod, OmniPod>             omni = new OmniMechFactory();

   static public Factory<ConfiguredComponent, InternalComponent> getISComponentFactory(){
      return is;
   }

   static public Factory<ConfiguredOmniPod, OmniPod> getOmniPodFactory(){
      return omni;
   }
}
