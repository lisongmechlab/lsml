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
package lisong_mechlab.model.upgrades;

import lisong_mechlab.model.chassi.HardPointType;
import lisong_mechlab.model.item.Ammunition;
import lisong_mechlab.model.item.Item;
import lisong_mechlab.model.item.ItemDB;
import lisong_mechlab.model.item.MissileWeapon;
import lisong_mechlab.model.loadout.LoadoutBase;
import lisong_mechlab.model.loadout.LoadoutStandard;
import lisong_mechlab.model.loadout.component.ConfiguredComponentBase;
import lisong_mechlab.mwo_data.helpers.ItemStatsUpgradeType;

import com.thoughtworks.xstream.annotations.XStreamAlias;
import com.thoughtworks.xstream.annotations.XStreamAsAttribute;

/**
 * This class models a guidance upgrade.
 * 
 * @author Li Song
 */
@XStreamAlias("GuidanceUpgrade")
public class GuidanceUpgrade extends Upgrade{
   @XStreamAsAttribute
   final private int    slots;
   @XStreamAsAttribute
   final private double tons;

   public GuidanceUpgrade(ItemStatsUpgradeType aUpgradeType){
      super(aUpgradeType);
      slots = aUpgradeType.ArtemisTypeStats.extraSlots;
      tons = aUpgradeType.ArtemisTypeStats.extraTons;
   }

   public int getSlots(){
      return slots;
   }

   public double getTons(){
      return tons;
   }

   /**
    * Calculates how many extra slots are needed in total for the given upgrade.
    * 
    * @param aLoadout
    *           The loadout to calculate for.
    * @return A number of slots needed.
    */
   public int getExtraSlots(LoadoutBase<?> aLoadout){
      int ans = 0;
      for(ConfiguredComponentBase part : aLoadout.getComponents()){
         ans += getExtraSlots(part);
      }
      return ans;
   }

   /**
    * Calculates how many extra slots are needed for the given {@link ConfiguredComponentBase} for the given upgrade.
    * 
    * @param aLoadoutPart
    *           The {@link ConfiguredComponentBase} to calculate for.
    * @return A number of slots needed.
    */
   public int getExtraSlots(ConfiguredComponentBase aLoadoutPart){
      int ans = 0;
      for(Item item : aLoadoutPart.getItemsFixed()){
         if( item instanceof MissileWeapon ){
            MissileWeapon weapon = (MissileWeapon)item;
            if( weapon.isArtemisCapable() ){
               ans += slots;
            }
         }
      }
      for(Item item : aLoadoutPart.getItemsEquipped()){
         if( item instanceof MissileWeapon ){
            MissileWeapon weapon = (MissileWeapon)item;
            if( weapon.isArtemisCapable() ){
               ans += slots;
            }
         }
      }
      return ans;
   }

   /**
    * Calculates how many extra tons are needed in total for the given upgrade.
    * 
    * @param aLoadout
    *           The {@link LoadoutStandard} to calculate for.
    * @return A number of tons needed.
    */
   public double getExtraTons(LoadoutBase<?> aLoadout){
      double ans = 0;
      for(ConfiguredComponentBase part : aLoadout.getComponents()){
         ans += getExtraTons(part);
      }
      return ans;
   }

   /**
    * Calculates how many extra tons are needed for the given {@link ConfiguredComponentBase} for the given upgrade.
    * 
    * @param aLoadoutPart
    *           The {@link ConfiguredComponentBase} to calculate for.
    * @return A number of tons needed.
    */
   public double getExtraTons(ConfiguredComponentBase aLoadoutPart){
      double ans = 0;
      for(Item item : aLoadoutPart.getItemsEquipped()){
         if( item instanceof MissileWeapon ){
            MissileWeapon weapon = (MissileWeapon)item;
            if( weapon.isArtemisCapable() ){
               ans += tons;
            }
         }
      }
      for(Item item : aLoadoutPart.getItemsFixed()){
         if( item instanceof MissileWeapon ){
            MissileWeapon weapon = (MissileWeapon)item;
            if( weapon.isArtemisCapable() ){
               ans += tons;
            }
         }
      }
      return ans;
   }

   /**
    * Upgrades a {@link MissileWeapon} to match this guidance type.
    * 
    * @param aOldWeapon
    *           The {@link MissileWeapon} to upgrade.
    * @return A {@link MissileWeapon} which is an appropriate variant for this guidance type.
    */
   public MissileWeapon upgrade(MissileWeapon aOldWeapon){
      MissileWeapon baseVariant = aOldWeapon.getBaseVariant();
      if( null == baseVariant )
         return aOldWeapon;

      for(MissileWeapon weapon : ItemDB.lookup(MissileWeapon.class)){
         if( weapon.getBaseVariant() == baseVariant && weapon.getRequiredUpgrade() == this ){
            return weapon;
         }
      }
      throw new RuntimeException("Unable to find upgraded version of: " + baseVariant);
   }

   /**
    * Upgrades a {@link Ammunition} to match this guidance type.
    * 
    * @param aOldAmmo
    *           The {@link Ammunition} to upgrade.
    * @return An {@link Ammunition} object of the appropriate type for this guidance.
    */
   public Ammunition upgrade(Ammunition aOldAmmo){
      if( aOldAmmo.getWeaponHardpointType() != HardPointType.MISSILE ){
         return aOldAmmo;
      }

      for(MissileWeapon weapon : ItemDB.lookup(MissileWeapon.class)){
         if( weapon.getAmmoType(null) == aOldAmmo ){
            return upgrade(weapon).getAmmoType(null);
         }
      }

      throw new RuntimeException("Unable to find upgraded version of: " + aOldAmmo);
   }
}
