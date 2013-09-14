package lisong_mechlab.model.loadout.metrics;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

import java.util.TreeMap;

import lisong_mechlab.model.chassi.ChassiDB;
import lisong_mechlab.model.item.Item;
import lisong_mechlab.model.item.Weapon;
import lisong_mechlab.model.loadout.Loadout;
import lisong_mechlab.util.MessageXBar;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.runners.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class TotalWeaponsTest{

   @Spy
   MessageXBar          xBar;

   @Mock
   private Loadout      loadout;
   @InjectMocks
   private TotalWeapons totalWeapons;

   @Test
   public void testCalculate(){
      // Setup
      Loadout cut = new Loadout(ChassiDB.lookup("COM-2D"), xBar);
      try{
         cut.loadStock();
      }
      catch( Exception e ){
         fail("Unexpected exception when loading stock!");
         e.printStackTrace();
      }
      totalWeapons = new TotalWeapons(cut);
      // Verify
      Item testItem = null;
      for(Item item : cut.getAllItems()){
         if( item instanceof Weapon ){
            testItem = item;
         }

      }
      TreeMap<Weapon, Integer> ammoValuesTest = totalWeapons.calculate();
      Integer actual = ammoValuesTest.get(testItem);
      assertEquals(1, actual.intValue());

   }

}
