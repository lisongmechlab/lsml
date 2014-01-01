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
package lisong_mechlab.model.item;

import lisong_mechlab.model.chassi.HardpointType;
import lisong_mechlab.model.loadout.Upgrades;
import lisong_mechlab.model.mwo_parsing.helpers.ItemStatsWeapon;

public class AmmoWeapon extends Weapon{
   private final Ammunition ammoType;

   public AmmoWeapon(ItemStatsWeapon aStatsWeapon, HardpointType aHardpointType){
      super(aStatsWeapon, aHardpointType);
      ammoType = (Ammunition)ItemDB.lookup(aStatsWeapon.WeaponStats.ammoType); // MWO Name
   }

   public Ammunition getAmmoType(Upgrades aUpgrades){
      if( aUpgrades == null )
         return ammoType;
      return ammoType;
   }

   @Override
   public String getShortName(Upgrades anUpgrades){
      String name = getName(anUpgrades);
      name = name.replace("ANTI-MISSILE SYSTEM", "AMS");
      return name;
   }
}
