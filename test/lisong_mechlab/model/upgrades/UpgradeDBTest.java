package lisong_mechlab.model.upgrades;

import static org.junit.Assert.*;
import lisong_mechlab.model.upgrades.UpgradeDB;

import org.junit.BeforeClass;
import org.junit.Test;



public class UpgradeDBTest{
   
   @BeforeClass
   public static void staticSetup(){
      UpgradeDB.initialize();
   }
   
   @Test(expected=IllegalArgumentException.class)
   public void testLookup_BadId(){
      UpgradeDB.lookup(0);
   }
   
   @Test
   public void testLookup_alternativeIdLookup(){
      assertSame(UpgradeDB.lookup(2810), UpgradeDB.lookup(2800)); // Standard Armor
      assertSame(UpgradeDB.lookup(2811), UpgradeDB.lookup(2801)); // Ferro-Fibrous Armor
      

      assertSame(UpgradeDB.lookup(3003), UpgradeDB.lookup(3000)); // SHS
      assertSame(UpgradeDB.lookup(3002), UpgradeDB.lookup(3001)); // DHS
      
      assertSame(UpgradeDB.lookup(3050), UpgradeDB.lookup(9001)); // Artemis
   }
}
