package lisong_mechlab.model.chassi;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.util.List;

import org.junit.Test;

public class ChassiDBTest{

   @Test(expected=IllegalArgumentException.class)
   public void testLookupFailed(){
      // Successful lookup is tested by ChassiTest.java
      ChassiDB.lookup("Nonexistent mech");
   }
   
   @Test
   public void testLookupByChassiSeries(){
      List<Chassi> cataphracts = ChassiDB.lookupSeries("CATAphract");
      List<Chassi> cataphracts1 = ChassiDB.lookupSeries("CTF");
      
      assertEquals(cataphracts, cataphracts1);
      
      assertTrue(cataphracts.remove(ChassiDB.lookup("ILYA MUROMETS")));
      assertTrue(cataphracts.remove(ChassiDB.lookup("CTF-1X")));
      assertTrue(cataphracts.remove(ChassiDB.lookup("CTF-2X")));
      assertTrue(cataphracts.remove(ChassiDB.lookup("CTF-3D")));
      assertTrue(cataphracts.remove(ChassiDB.lookup("CTF-4X")));
      assertTrue(cataphracts.isEmpty());
   }
   
   @Test(expected=IllegalArgumentException.class)
   public void testLookupByChassiSeriesFail(){
      ChassiDB.lookupSeries("No such series");
   }
   
   @Test
   public void testLookupByChassiClass(){
      List<Chassi> heavies = ChassiDB.lookup(ChassiClass.HEAVY);
      
      assertTrue(heavies.contains(ChassiDB.lookup("ILYA MUROMETS")));
      assertTrue(heavies.contains(ChassiDB.lookup("JM6-DD")));
      assertTrue(heavies.contains(ChassiDB.lookup("CPLT-C4")));
      assertTrue(heavies.contains(ChassiDB.lookup("FLAME")));
      
      for(Chassi chassi : heavies){
         assertEquals(ChassiClass.HEAVY, chassi.getChassiClass());
      }
   }

}
