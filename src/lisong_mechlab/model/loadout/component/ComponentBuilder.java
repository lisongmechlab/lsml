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
package lisong_mechlab.model.loadout.component;

import lisong_mechlab.model.chassi.InternalComponent;
import lisong_mechlab.model.chassi.OmniPod;

/**
 * This factory object can construct configured components.
 * 
 * @author Emily Björk
 */
public class ComponentBuilder{
   public interface Factory<T extends ConfiguredComponent, U extends InternalComponent> {
      T create(U aInternalComponent, boolean aAutoArmor);
      T clone(T aConfiguredComponent);
      T[] createArray(int aSize);
   }

   private static class InnerSphereFactory implements Factory<ConfiguredComponent, InternalComponent>{
      @Override
      public ConfiguredComponent create(InternalComponent aInternalComponent, boolean aAutoArmor){
         return new ConfiguredComponent(aInternalComponent, aAutoArmor);
      }

      @Override
      public ConfiguredComponent[] createArray(int aSize){
         return new ConfiguredComponent[aSize];
      }

      @Override
      public ConfiguredComponent clone(ConfiguredComponent aConfiguredComponent){
         return new ConfiguredComponent(aConfiguredComponent);
      }
   }

   private static class ClanFactory implements Factory<ConfiguredOmniPod, OmniPod>{
      @Override
      public ConfiguredOmniPod create(OmniPod aInternalComponent, boolean aAutoArmor){
         return null; // FIXME TODO
      }
      @Override
      public ConfiguredOmniPod[] createArray(int aSize){
         return new ConfiguredOmniPod[aSize];
      }

      @Override
      public ConfiguredOmniPod clone(ConfiguredOmniPod aConfiguredComponent){
         // TODO Auto-generated method stub
         return null;
      }
   }

   private static Factory<ConfiguredComponent, InternalComponent> is   = new InnerSphereFactory();
   private static Factory<ConfiguredOmniPod, OmniPod>             omni = new ClanFactory();

   static public Factory<ConfiguredComponent, InternalComponent> getISComponentFactory(){
      return is;
   }

   static public Factory<ConfiguredOmniPod, OmniPod> getOmniPodFactory(){
      return omni;
   }
}
