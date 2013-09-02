package lisong_mechlab.model.loadout.metrics;

import static org.junit.Assert.*;

import java.util.TreeMap;

import lisong_mechlab.model.MessageXBar;
import lisong_mechlab.model.chassi.ChassiDB;
import lisong_mechlab.model.item.Item;
import lisong_mechlab.model.loadout.Loadout;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.Spy;
import org.mockito.runners.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class TotalAmmoSupplyTest{

   @Spy
   MessageXBar             xBar;

   @Mock
   private Loadout         loadout;
   @InjectMocks
   private TotalAmmoSupply          totalAmmoSupply;

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
      //Verify
     TreeMap<Item, Integer> ammoValuesTest =  totalAmmoSupply.calculate();
     Integer actual = ammoValuesTest.get("SRM AMMO");
     assertEquals(200, actual.intValue());
      
   }
   

}
