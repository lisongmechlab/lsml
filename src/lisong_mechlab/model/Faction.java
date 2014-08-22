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
package lisong_mechlab.model;

/**
 * An enumeration of all the available factions.
 * 
 * @author Li Song
 */
public enum Faction{
   Any, InnerSphere, Clan;

   public boolean isCompatible(Faction aFaction){
      if( this == Any || aFaction == Any )
         return true;
      return this == aFaction;
   }

   /**
    * @param aFaction
    *           The value found in MWO data files.
    * @return The {@link Faction} matching the MWO string value.
    */
   public static Faction fromMwo(String aFaction){
      if( null == aFaction || "clan,innersphere".equals(aFaction.toLowerCase()) )
         return Any;
      return valueOf(aFaction);
   }
}
