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

import java.util.Comparator;

import lisong_mechlab.model.chassi.HardpointType;
import lisong_mechlab.model.loadout.Loadout;
import lisong_mechlab.model.mwo_parsing.helpers.ItemStatsWeapon;
import lisong_mechlab.model.upgrades.Upgrades;

public class MissileWeapon extends AmmoWeapon{
   // private static final String ARTEMIS = " + ARTEMIS";
   protected final double     flightSpeed;
   protected final Ammunition artemisAmmo;

   public MissileWeapon(ItemStatsWeapon aStatsWeapon){
      super(aStatsWeapon, HardpointType.MISSILE);
      flightSpeed = aStatsWeapon.WeaponStats.speed;
      artemisAmmo = aStatsWeapon.WeaponStats.artemisAmmoType == null ? getAmmoType(null)
                                                                    : (Ammunition)ItemDB.lookup(aStatsWeapon.WeaponStats.artemisAmmoType);
   }

   @Override
   public double getRangeZero(){
      return super.getRangeMin() - Math.ulp(super.getRangeMin()) * RANGE_ULP_FUZZ;
   }

   @Override
   public double getRangeMax(){
      // Missile fall off is a bit different from other weapons because long = max.
      // Emulate a steep fall off by nudging max ever so slightly
      return super.getRangeMax() + Math.ulp(super.getRangeMax()) * RANGE_ULP_FUZZ;
   }

   @Override
   public boolean isEquippableOn(Loadout aLoadout){
      if( isArtemisCapable() ){
         if( !getName().contains("ARTEMIS") && aLoadout.getUpgrades().hasArtemis() )
            return false;
         else if( getName().contains("ARTEMIS") && !aLoadout.getUpgrades().hasArtemis() )
            return false;
      }

      return super.isEquippableOn(aLoadout);
   }

   @Override
   public int getNumCriticalSlots(Upgrades aUpgrades){
      // TODO: Ugly fix
      if( getName().contains("ARTEMIS") || (aUpgrades != null && aUpgrades.hasArtemis() && isArtemisCapable()) )
         return super.getNumCriticalSlots(aUpgrades) + 1;
      return super.getNumCriticalSlots(aUpgrades);
   }

   @Override
   public double getMass(Upgrades aUpgrades){
      // TODO: Ugly fix
      if( getName().contains("ARTEMIS") || (aUpgrades != null && aUpgrades.hasArtemis() && isArtemisCapable()) )
         return super.getMass(aUpgrades) + 1.0;
      return super.getMass(aUpgrades);
   }

   @Override
   public Ammunition getAmmoType(Upgrades aUpgrades){
      if( aUpgrades != null && aUpgrades.hasArtemis() && isArtemisCapable() )
         return artemisAmmo;
      return super.getAmmoType(aUpgrades);
   }

   public boolean isArtemisCapable(){
      return (getName().contains("LRM") || getName().contains("SRM") && !getName().contains("STREAK"));
   }

   public final static Comparator<Item> DEFAULT_ORDERING = DEFAULT_WEAPON_ORDERING;
}
