package lisong_mechlab.model.upgrade;

import static org.junit.Assert.*;
import lisong_mechlab.model.upgrade.ArmorUpgrade;
import lisong_mechlab.model.upgrade.UpgradeDB;

import org.junit.BeforeClass;
import org.junit.Test;

/**
 * Test suite for {@link ArmorUpgrade}.
 * 
 * @author Li Song
 */
public class ArmorUpgradeTest{
   @BeforeClass
   public static void staticSetup(){
      UpgradeDB.initialize();
   }

   /**
    * Test properties of standard armor
    */
   @Test
   public void testStandardArmor(){
      final int sa_id = 2810;
      ArmorUpgrade cut = (ArmorUpgrade)UpgradeDB.lookup(sa_id);

      assertNotNull(cut);
      assertEquals(sa_id, cut.getMwoId());
      assertEquals("STANDARD ARMOR", cut.getName());
      assertFalse(cut.getDescription().equals(""));
      assertEquals(0, cut.getExtraSlots());
      assertEquals(32.0, cut.getArmorPerTon(), 0.0);
   }

   /**
    * Test properties of Ferro-Fibrous armor
    */
   @Test
   public void testFerroFibrousArmor(){
      final int ff_id = 2811;
      ArmorUpgrade cut = (ArmorUpgrade)UpgradeDB.lookup(ff_id);

      assertNotNull(cut);
      assertEquals(ff_id, cut.getMwoId());
      assertEquals("FERRO FIBROUS ARMOR", cut.getName());
      assertFalse(cut.getDescription().equals(""));
      assertEquals(14, cut.getExtraSlots());
      assertEquals(35.84, cut.getArmorPerTon(), 0.0);
   }
}
