package lisong_mechlab.model.loadout.metrics;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

import java.util.TreeMap;

import lisong_mechlab.model.MessageXBar;
import lisong_mechlab.model.chassi.ChassiDB;
import lisong_mechlab.model.item.Ammunition;
import lisong_mechlab.model.item.Item;
import lisong_mechlab.model.loadout.Loadout;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.runners.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class TotalAmmoSupplyTest{

   @Spy
   MessageXBar             xBar;

   @Mock
   private Loadout         loadout;
   @InjectMocks
   private TotalAmmoSupply totalAmmoSupply;

   @Test
   public void testGenerate(){
      // Setup
      Loadout cut = new Loadout(ChassiDB.lookup("COM-2D"), xBar);
      try{
         cut.loadStock();
      }
      catch( Exception e ){
         fail("Unexpected exception when loading stock!");
         e.printStackTrace();
      }
      totalAmmoSupply = new TotalAmmoSupply(cut);
      // Verify
      Item testItem = null;
      for(Item item : cut.getAllItems()){
         if( item instanceof Ammunition ){
            testItem = item;
         }

      }
      TreeMap<Item, Integer> ammoValuesTest = totalAmmoSupply.calculate();
      Integer actual = ammoValuesTest.get(testItem);
      assertEquals(200, actual.intValue());

   }

   @Test
   public void testGetVolleyNumber(){
      // Setup
      Loadout cut = new Loadout(ChassiDB.lookup("COM-2D"), xBar);
      try{
         cut.loadStock();
      }
      catch( Exception e ){
         fail("Unexpected exception when loading stock!");
         e.printStackTrace();
      }
      totalAmmoSupply = new TotalAmmoSupply(cut);
      // Verify
      totalAmmoSupply.calculate();
      TreeMap<String, Integer> volleyValuesTest = totalAmmoSupply.getShotsPerVolleyForEach();
      Integer actual = volleyValuesTest.get("SRM AMMO");
      assertEquals(10, actual.intValue());
   }

}
