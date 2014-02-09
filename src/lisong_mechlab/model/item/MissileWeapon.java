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
import lisong_mechlab.model.upgrades.GuidanceUpgrade;
import lisong_mechlab.model.upgrades.UpgradeDB;
import lisong_mechlab.model.upgrades.Upgrades;

public class MissileWeapon extends AmmoWeapon{
   protected final double flightSpeed;
   protected final int    requiredGuidancetype;

   public MissileWeapon(ItemStatsWeapon aStatsWeapon){
      super(aStatsWeapon, HardpointType.MISSILE, getAmmoType(aStatsWeapon));
      flightSpeed = aStatsWeapon.WeaponStats.speed;

      if( null != aStatsWeapon.Artemis )
         requiredGuidancetype = aStatsWeapon.Artemis.RestrictedTo;
      else
         requiredGuidancetype = -1;
   }

   static private Ammunition getAmmoType(ItemStatsWeapon aStatsWeapon){
      Ammunition regularAmmo = (Ammunition)ItemDB.lookup(aStatsWeapon.WeaponStats.ammoType);
      if( aStatsWeapon.WeaponStats.artemisAmmoType == null )
         return regularAmmo;

      if( aStatsWeapon.Artemis == null )
         return regularAmmo;

      if( aStatsWeapon.Artemis.RestrictedTo == 3051 ) // No artemis
         return regularAmmo;
      return (Ammunition)ItemDB.lookup(aStatsWeapon.WeaponStats.artemisAmmoType);
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
         return aLoadout.getUpgrades().getGuidance().getMwoId() == requiredGuidancetype;
      }
      return super.isEquippableOn(aLoadout);
   }

   @Override
   public int getNumCriticalSlots(Upgrades aUpgrades){
      if( isArtemisCapable() ){
         return super.getNumCriticalSlots(aUpgrades) + ((GuidanceUpgrade)UpgradeDB.lookup(requiredGuidancetype)).getSlots();
      }
      return super.getNumCriticalSlots(aUpgrades);
   }

   @Override
   public double getMass(Upgrades aUpgrades){
      if( isArtemisCapable() ){
         return super.getNumCriticalSlots(aUpgrades) + ((GuidanceUpgrade)UpgradeDB.lookup(requiredGuidancetype)).getTons();
      }
      return super.getMass(aUpgrades);
   }

   public boolean isArtemisCapable(){
      return requiredGuidancetype != -1;
   }

   public final static Comparator<Item> DEFAULT_ORDERING = DEFAULT_WEAPON_ORDERING;
}
