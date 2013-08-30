package lisong_mechlab.model.item;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

public class WeaponTest{

   @Test
   public void testIsEquippableOn() throws Exception{
      throw new RuntimeException("not yet implemented");
   }

   /**
    * Make sure {@link Weapon#getDamagePerVolley()} returns the volley damage and not missile damage.
    * 
    * @throws Exception
    */
   @Test
   public void testGetDamagePerVolley_lrm20() throws Exception{
      MissileWeapon lrm20 = (MissileWeapon)ItemDB.lookup("LRM 20");
      assertTrue(lrm20.getDamagePerVolley() > 10);
   }

   /**
    * Make sure {@link Weapon#getDamagePerVolley()} returns the volley damage and not projectile damage.
    * 
    * @throws Exception
    */
   @Test
   public void testGetDamagePerVolley_lb10x() throws Exception{
      BallisticWeapon lb10xac = (BallisticWeapon)ItemDB.lookup("LB 10-X AC");
      assertTrue(lb10xac.getDamagePerVolley() > 5);
   }

   @Test
   public void testGetDamagePerVolley_gauss() throws Exception{
      BallisticWeapon gauss = (BallisticWeapon)ItemDB.lookup("GAUSS RIFLE");
      assertTrue(gauss.getDamagePerVolley() > 10);
   }

   @Test
   public void testGetHeat_gauss(){
      BallisticWeapon gauss = (BallisticWeapon)ItemDB.lookup("GAUSS RIFLE");
      assertEquals(1.0, gauss.getHeat(), 0.0);
   }

   @Test
   public void testGetSecondsPerShot_mg() throws Exception{
      BallisticWeapon mg = (BallisticWeapon)ItemDB.lookup("MACHINE GUN");
      assertTrue(mg.getSecondsPerShot() > 0.05);
   }

   @Test
   public void testGetSecondsPerShot_gauss() throws Exception{
      BallisticWeapon mg = (BallisticWeapon)ItemDB.lookup("GAUSS RIFLE");
      assertTrue(mg.getSecondsPerShot() > 3);
   }

   @Test
   public void testGetRangeMin_ppc() throws Exception{
      EnergyWeapon ppc = (EnergyWeapon)ItemDB.lookup("PPC");
      assertEquals(90.0, ppc.getRangeMin(), 0.0);
   }

   @Test
   public void testGetRangeMax_ppc() throws Exception{
      EnergyWeapon ppc = (EnergyWeapon)ItemDB.lookup("PPC");
      assertEquals(1080.0, ppc.getRangeMax(), 0.0);
   }

   @Test
   public void testGetRangeLong_ppc() throws Exception{
      EnergyWeapon ppc = (EnergyWeapon)ItemDB.lookup("PPC");
      assertEquals(540.0, ppc.getRangeLong(), 0.0);
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
      assertEquals(gauss.getDamagePerVolley() / gauss.getHeat(), gauss.getStat("d/h"), 0.0);
   }

}
