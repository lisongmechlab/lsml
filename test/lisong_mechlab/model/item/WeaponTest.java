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
   public void testGetShotsPerVolley_lrm10() throws Exception{
      Weapon lb10xac = (Weapon)ItemDB.lookup("LRM 10");
      assertEquals(10, lb10xac.getAmmoPerPerShot());
   }

   @Test
   public void testGetDamagePerShot_gauss() throws Exception{
      Weapon gauss = (Weapon)ItemDB.lookup("GAUSS RIFLE");
      assertTrue(gauss.getDamagePerShot() > 10);
   }

   @Test
   public void testGetHeat_gauss(){
      Weapon gauss = (Weapon)ItemDB.lookup("GAUSS RIFLE");
      assertEquals(1.0, gauss.getHeat(), 0.0);
   }

   @Test
   public void testGetSecondsPerShot_mg() throws Exception{
      Weapon mg = (Weapon)ItemDB.lookup("MACHINE GUN");
      assertTrue(mg.getSecondsPerShot() > 0.05);
   }

   @Test
   public void testGetSecondsPerShot_gauss() throws Exception{
      Weapon mg = (Weapon)ItemDB.lookup("GAUSS RIFLE");
      assertTrue(mg.getSecondsPerShot() > 3);
   }

   @Test
   public void testGetRangeMin_ppc() throws Exception{
      Weapon ppc = (Weapon)ItemDB.lookup("PPC");
      assertEquals(90.0, ppc.getRangeMin(), 0.0);
   }

   @Test
   public void testGetRangeMax_ppc() throws Exception{
      Weapon ppc = (Weapon)ItemDB.lookup("PPC");
      assertEquals(1080.0, ppc.getRangeMax(), 0.0);
   }

   @Test
   public void testGetRangeLong_ppc() throws Exception{
      Weapon ppc = (Weapon)ItemDB.lookup("PPC");
      assertEquals(540.0, ppc.getRangeLong(), 0.0);
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

   @Test
   public void testGetRangeEffectivity_mg() throws Exception{
      BallisticWeapon mg = (BallisticWeapon)ItemDB.lookup("MACHINE GUN");
      assertEquals(1.0, mg.getRangeEffectivity(0), 0.0);
      assertEquals(1.0, mg.getRangeEffectivity(mg.getRangeLong()), 0.0);
      assertEquals(0.5, mg.getRangeEffectivity((mg.getRangeLong() + mg.getRangeMax()) / 2), 0.0);
      assertEquals(0.0, mg.getRangeEffectivity(mg.getRangeMax()), 0.0);
   }

   @Test
   public void testGetRangeEffectivity_gaussrifle() throws Exception{
      BallisticWeapon gauss = (BallisticWeapon)ItemDB.lookup("GAUSS RIFLE");
      assertEquals(1.0, gauss.getRangeEffectivity(0), 0.0);
      assertEquals(1.0, gauss.getRangeEffectivity(gauss.getRangeLong()), 0.0);
      assertEquals(0.5, gauss.getRangeEffectivity((gauss.getRangeLong() + gauss.getRangeMax()) / 2), 0.0);
      assertEquals(0.0, gauss.getRangeEffectivity(gauss.getRangeMax()), 0.0);

      assertTrue(gauss.getRangeEffectivity(750) < 0.95);
      assertTrue(gauss.getRangeEffectivity(750) > 0.8);
   }

   @Test
   public void testGetStat_gauss() throws Exception{
      BallisticWeapon gauss = (BallisticWeapon)ItemDB.lookup("GAUSS RIFLE");
      assertEquals(gauss.getDamagePerShot() / gauss.getHeat(), gauss.getStat("d/h"), 0.0);
   }

}
