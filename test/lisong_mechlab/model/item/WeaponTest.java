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
package lisong_mechlab.model.item;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

public class WeaponTest{

   @Test
   public void testInequality(){
      MissileWeapon lrm10 = (MissileWeapon)ItemDB.lookup("LRM 10");
      MissileWeapon lrm15 = (MissileWeapon)ItemDB.lookup("LRM 15");
      assertFalse(lrm10.equals(lrm15));
   }

   @Test
   public void testGetDamagePerShot_lpl() throws Exception{
      Weapon weapon = (Weapon)ItemDB.lookup("LRG PULSE LASER");
      assertTrue(weapon.getDamagePerShot() > 8);
   }

   @Test
   public void testGetDamagePerShot_ml() throws Exception{
      Weapon weapon = (Weapon)ItemDB.lookup("MEDIUM LASER");
      assertTrue(weapon.getDamagePerShot() > 4);
   }

   /**
    * Make sure {@link Weapon#getDamagePerShot()} returns the volley damage and not projectile damage.
    * 
    * @throws Exception
    */
   @Test
   public void testGetDamagePerShot_lb10x() throws Exception{
      Weapon lb10xac = (Weapon)ItemDB.lookup("LB 10-X AC");
      assertTrue(lb10xac.getDamagePerShot() > 5);
   }

   @Test
   public void testGetShotsPerVolley_lb10x() throws Exception{
      Weapon lb10xac = (Weapon)ItemDB.lookup("LB 10-X AC");
      assertEquals(1, lb10xac.getAmmoPerPerShot());
   }

   @Test
   public void testGetDamagePerShot_gauss() throws Exception{
      Weapon gauss = (Weapon)ItemDB.lookup("GAUSS RIFLE");
      assertTrue(gauss.getDamagePerShot() > 10);
   }

   @Test
   public void testGetHeat_gauss(){
      Weapon gauss = (Weapon)ItemDB.lookup("GAUSS RIFLE");
      assertEquals(1.0, gauss.getHeat(null), 0.0);
   }

   @Test
   public void testGetHeat_chargeDps(){
      Weapon gauss = (Weapon)ItemDB.lookup("GAUSS RIFLE");
      assertEquals(gauss.getCoolDown(null, null) + 0.75, gauss.getSecondsPerShot(null, null), 0.0);
   }

   @Test
   public void testGetSecondsPerShot_mg() throws Exception{
      Weapon mg = (Weapon)ItemDB.lookup("MACHINE GUN");
      assertTrue(mg.getSecondsPerShot(null, null) > 0.05);
   }

   @Test
   public void testGetSecondsPerShot_gauss() throws Exception{
      Weapon mg = (Weapon)ItemDB.lookup("GAUSS RIFLE");
      assertTrue(mg.getSecondsPerShot(null, null) > 3);
   }

   @Test
   public void testGetRangeMin_ppc() throws Exception{
      Weapon ppc = (Weapon)ItemDB.lookup("PPC");
      assertEquals(90.0, ppc.getRangeMin(), 0.0);
   }

   @Test
   public void testGetRangeMax_ppc() throws Exception{
      Weapon ppc = (Weapon)ItemDB.lookup("PPC");
      assertEquals(1080.0, ppc.getRangeMax(null), 0.0);
   }

   @Test
   public void testGetRangeLong_ppc() throws Exception{
      Weapon ppc = (Weapon)ItemDB.lookup("PPC");
      assertEquals(540.0, ppc.getRangeLong(null), 0.0);
   }

   @Test
   public void testGetRangeEffectivity_mg() throws Exception{
      BallisticWeapon mg = (BallisticWeapon)ItemDB.lookup("MACHINE GUN");
      assertEquals(1.0, mg.getRangeEffectivity(0, null), 0.0);
      assertEquals(1.0, mg.getRangeEffectivity(mg.getRangeLong(null), null), 0.1); // High spread on MG
      assertTrue(0.5 >= mg.getRangeEffectivity((mg.getRangeLong(null) + mg.getRangeMax(null)) / 2, null)); // Spread + falloff
      assertEquals(0.0, mg.getRangeEffectivity(mg.getRangeMax(null), null), 0.0);
   }

   @Test
   public void testGetRangeEffectivity_gaussrifle() throws Exception{
      BallisticWeapon gauss = (BallisticWeapon)ItemDB.lookup("GAUSS RIFLE");
      assertEquals(1.0, gauss.getRangeEffectivity(0, null), 0.0);
      assertEquals(1.0, gauss.getRangeEffectivity(gauss.getRangeLong(null), null), 0.0);
      assertEquals(0.5, gauss.getRangeEffectivity((gauss.getRangeLong(null) + gauss.getRangeMax(null)) / 2, null), 0.0);
      assertEquals(0.0, gauss.getRangeEffectivity(gauss.getRangeMax(null), null), 0.0);

      assertTrue(gauss.getRangeEffectivity(750, null) < 0.95);
      assertTrue(gauss.getRangeEffectivity(750, null) > 0.8);
   }

   @Test
   public void testGetStat_gauss() throws Exception{
      BallisticWeapon gauss = (BallisticWeapon)ItemDB.lookup("GAUSS RIFLE");
      assertEquals(gauss.getDamagePerShot() / gauss.getHeat(null), gauss.getStat("d/h", null, null), 0.0);
   }

}
