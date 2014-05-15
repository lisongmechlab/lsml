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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.util.List;

import lisong_mechlab.model.upgrades.Upgrades;

import org.junit.Test;

public class MissileWeaponTest{

   private final List<MissileWeapon> allMissileWeapons = ItemDB.lookup(MissileWeapon.class);

   /**
    * The number of critical slots is affected by Artemis.
    */
   @Test
   public void testGetNumCriticalSlots(){
      MissileWeapon srm6 = (MissileWeapon)ItemDB.lookup("SRM 6");
      MissileWeapon srm6artemis = (MissileWeapon)ItemDB.lookup("SRM 6 + ARTEMIS");

      assertEquals(srm6artemis.getNumCriticalSlots(), srm6.getNumCriticalSlots() + 1);
   }

   /**
    * The mass is affected by Artemis.
    */
   @Test
   public void testGetMass(){
      MissileWeapon srm6 = (MissileWeapon)ItemDB.lookup("SRM 6");
      MissileWeapon srm6artemis = (MissileWeapon)ItemDB.lookup("SRM 6 + ARTEMIS");

      assertEquals(srm6.getMass() + 1.0, srm6artemis.getMass(), 0.0);
   }

   /**
    * {@link MissileWeapon#getAmmoType(Upgrades)} shall return Artemis ammo type for weapons that are Artemis capable.
    */
   @Test
   public void testGetAmmoType(){
      for(MissileWeapon weapon : allMissileWeapons){
         if( weapon.getName().contains("ARTEMIS") )
            assertTrue(weapon.getAmmoType(null).getName().contains("ARTEMIS"));
         else
            assertFalse(weapon.getAmmoType(null).getName().contains("ARTEMIS"));
      }
   }

   /**
    * All missiles have an instant fall off on the near range.
    */
   @Test
   public void testGetRangeZero(){
      for(MissileWeapon weapon : allMissileWeapons){
         assertTrue(weapon.getRangeMin() - weapon.getRangeZero() < 0.0001);
      }
   }

   /**
    * All missiles have an instant fall off on the max range
    */
   @Test
   public void testGetRangeMax(){
      for(MissileWeapon weapon : allMissileWeapons){
         assertTrue(weapon.getRangeMax() - weapon.getRangeLong() < 0.0001);
      }
   }

   /**
    * Only SRMs and LRMs are Artemis capable
    */
   @Test
   public void testIsArtemisCapable(){
      for(MissileWeapon weapon : allMissileWeapons){
         if( weapon.getName().contains("STREAK") || weapon.getName().contains("NARC") ){
            assertFalse(weapon.isArtemisCapable());
         }
         else{
            assertTrue(weapon.isArtemisCapable());
            assertTrue(weapon.isArtemisCapable());
         }
      }
   }

   @Test
   public void testNotArtemisMissiles(){
      MissileWeapon lrm = (MissileWeapon)ItemDB.lookup("LRM 20");
      assertFalse(lrm.getName().toLowerCase().contains("artemis"));
   }

   /**
    * Make sure {@link Weapon#getDamagePerShot()} returns the volley damage and not missile damage.
    * 
    * @throws Exception
    */
   @Test
   public void testGetDamagePerShot_lrm20() throws Exception{
      MissileWeapon lrm20 = (MissileWeapon)ItemDB.lookup("LRM 20");
      assertTrue(lrm20.getDamagePerShot() > 10);
   }

   @Test
   public void testGetShotsPerVolley_lrm10() throws Exception{
      Weapon lb10xac = (Weapon)ItemDB.lookup("LRM 10");
      assertEquals(10, lb10xac.getAmmoPerPerShot());
   }

   @Test
   public void testGetRangeEffectivity_lrm20() throws Exception{
      MissileWeapon srm6 = (MissileWeapon)ItemDB.lookup("LRM 20");
      assertEquals(0.0, srm6.getRangeEffectivity(0), 0.0);
      assertEquals(0.0, srm6.getRangeEffectivity(srm6.getRangeMin() - Math.ulp(srm6.getRangeLong()) * Weapon.RANGE_ULP_FUZZ), 0.0);
      assertEquals(1.0, srm6.getRangeEffectivity(srm6.getRangeMin()), 0.0);
      assertEquals(1.0, srm6.getRangeEffectivity(srm6.getRangeLong()), 0.0);
      assertEquals(0.0, srm6.getRangeEffectivity(srm6.getRangeLong() + Math.ulp(srm6.getRangeLong()) * Weapon.RANGE_ULP_FUZZ), 0.0);
      assertEquals(0.0, srm6.getRangeEffectivity(srm6.getRangeMax()), 0.0);
   }

   @Test
   public void testGetRangeEffectivity_srm6() throws Exception{
      MissileWeapon srm6 = (MissileWeapon)ItemDB.lookup("SRM 6");
      assertEquals(1.0, srm6.getRangeEffectivity(0), 0.0);
      assertEquals(1.0, srm6.getRangeEffectivity(srm6.getRangeLong()), 0.0);
      assertEquals(0.0, srm6.getRangeEffectivity(srm6.getRangeLong() + Math.ulp(srm6.getRangeLong()) * Weapon.RANGE_ULP_FUZZ), 0.0);
      assertEquals(0.0, srm6.getRangeEffectivity(srm6.getRangeMax()), 0.0);
   }

}
