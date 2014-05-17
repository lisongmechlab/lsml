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

import lisong_mechlab.model.chassi.OmniPod;
import lisong_mechlab.model.loadout.LoadoutOmniMech;

/**
 * This class models a configured {@link OmniPod} on an {@link LoadoutOmniMech}.
 * 
 * @author Emily Björk
 */
public class ConfiguredOmniPod extends ConfiguredComponent{

   /**
    * @param aOmniPod
    *           The internal omnipod for this component.
    */
   public ConfiguredOmniPod(OmniPod aOmniPod){
      super(aOmniPod, true);
   }

   /**
    * @param aConfiguredOmnipod
    */
   public ConfiguredOmniPod(ConfiguredOmniPod aConfiguredOmnipod){
      super(aConfiguredOmnipod);
      // TODO Auto-generated constructor stub
   }

   @Override
   public OmniPod getInternalComponent(){
      return (OmniPod)super.getInternalComponent();
   }

}
