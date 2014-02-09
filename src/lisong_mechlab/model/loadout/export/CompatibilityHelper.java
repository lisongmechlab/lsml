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
package lisong_mechlab.model.loadout.export;

import java.util.List;

import lisong_mechlab.model.chassi.HardpointType;
import lisong_mechlab.model.item.Ammunition;
import lisong_mechlab.model.item.Item;
import lisong_mechlab.model.item.ItemDB;
import lisong_mechlab.model.item.MissileWeapon;
import lisong_mechlab.model.loadout.Upgrades;

/**
 * This class helps dealing with compatibility issues that arise along the way.
 * 
 * @author Li Song
 */
public class CompatibilityHelper{

   private final static List<MissileWeapon> missileWeapons = ItemDB.lookup(MissileWeapon.class);

   /**
    * February 4th patch introduced new weapon IDs for artemis enabled missile launchers. This function canonizes old
    * missile launchers to the new types if applicable.
    * 
    * @param anItem
    * @param aHasArtemis
    * @return
    */
   public static Item fixArtemis(final Item anItem, boolean aHasArtemis){
      Upgrades withArtemis = new Upgrades(null);
      withArtemis.setArtemis(true);
      Upgrades withoutArtemis = new Upgrades(null);
      withoutArtemis.setArtemis(false);

      Item ans = anItem;
      if( aHasArtemis ){
         if( anItem instanceof MissileWeapon ){
            MissileWeapon weapon = (MissileWeapon)anItem;
            if( weapon.isArtemisCapable() && !weapon.getName(null).contains("ARTEMIS") ){
               ans = ItemDB.lookup(weapon.getName() + " + ARTEMIS");
            }
         }
         else if( anItem instanceof Ammunition ){
            Ammunition ammunition = (Ammunition)anItem;
            if( ammunition.getWeaponHardpointType() == HardpointType.MISSILE ){
               for(MissileWeapon weapon : missileWeapons){
                  if( weapon.getAmmoType(withoutArtemis) == ammunition ){
                     ans = weapon.getAmmoType(withArtemis);
                     break;
                  }
               }
            }
         }
      }
      else{ // No Artemis
         if( anItem instanceof MissileWeapon ){
            MissileWeapon weapon = (MissileWeapon)anItem;
            if( weapon.isArtemisCapable() && weapon.getName(null).contains("ARTEMIS") ){
               ans = ItemDB.lookup(weapon.getName().substring(0, weapon.getName().indexOf(" + ARTEMIS")));
            }
         }
         else if( anItem instanceof Ammunition ){
            Ammunition ammunition = (Ammunition)anItem;
            if( ammunition.getWeaponHardpointType() == HardpointType.MISSILE ){
               for(MissileWeapon weapon : missileWeapons){
                  if( weapon.getAmmoType(withArtemis) == ammunition ){
                     ans = weapon.getAmmoType(withoutArtemis);
                     break;
                  }
               }
            }
         }
      }
      return ans;
   }
}
