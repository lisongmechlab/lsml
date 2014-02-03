package lisong_mechlab.model.loadout.part;

import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.util.ArrayList;

import lisong_mechlab.model.chassi.InternalPart;
import lisong_mechlab.model.chassi.Part;
import lisong_mechlab.model.item.Item;
import lisong_mechlab.model.item.ItemDB;
import lisong_mechlab.model.loadout.Loadout;
import lisong_mechlab.model.upgrades.Upgrades;
import lisong_mechlab.util.MessageXBar;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.runners.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class RemoveItemOperationTest{
   @Mock
   private LoadoutPart  loadoutPart;
   @Mock
   private Loadout      loadout;
   @Mock
   private Upgrades     upgrades;
   @Mock
   private MessageXBar  xBar;
   @Mock
   private InternalPart internalPart;

   @Before
   public void setup(){
      Mockito.when(loadout.getUpgrades()).thenReturn(upgrades);
      Mockito.when(loadoutPart.getLoadout()).thenReturn(loadout);
      Mockito.when(loadoutPart.getInternalPart()).thenReturn(internalPart);
      Mockito.when(internalPart.getType()).thenReturn(Part.CenterTorso);
   }

   @Test
   public void testDescription(){
      Item item = ItemDB.ECM;

      RemoveItemOperation cut = new RemoveItemOperation(xBar, loadoutPart, item);

      assertTrue(cut.describe().contains("remove"));
      assertTrue(cut.describe().contains("from"));
      assertTrue(cut.describe().contains(loadoutPart.getInternalPart().getType().toString()));
      assertTrue(cut.describe().contains(item.getName()));
   }

   @Test
   public void testDescription_artemis(){
      Item item = ItemDB.lookup("LRM 20");
      Mockito.when(upgrades.hasArtemis()).thenReturn(true);

      RemoveItemOperation cut = new RemoveItemOperation(xBar, loadoutPart, item);

      assertTrue(cut.describe().contains("remove"));
      assertTrue(cut.describe().contains("from"));
      assertTrue(cut.describe().contains(loadoutPart.getInternalPart().getType().toString()));
      assertTrue(cut.describe().contains(item.getName(upgrades)));
   }

   /**
    * If an item can't be removed, an exception shall be thrown when the operation is applied.
    */
   @Test(expected = IllegalArgumentException.class)
   public void testCantAddItem(){
      RemoveItemOperation cut = null;
      try{
         Item item = ItemDB.lookup("LRM 20");
         Mockito.when(loadoutPart.getItems()).thenReturn(new ArrayList<Item>());
         cut = new RemoveItemOperation(xBar, loadoutPart, item);
      }
      catch( Throwable t ){
         fail("Setup failed");
         return;
      }

      cut.apply();
   }
}
