package lisong_mechlab.model.loadout.part;

import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.util.ArrayList;

import lisong_mechlab.model.chassi.InternalComponent;
import lisong_mechlab.model.chassi.Location;
import lisong_mechlab.model.item.Internal;
import lisong_mechlab.model.item.Item;
import lisong_mechlab.model.item.ItemDB;
import lisong_mechlab.model.loadout.Loadout;
import lisong_mechlab.model.upgrades.UpgradeDB;
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
   private ConfiguredComponent  loadoutPart;
   @Mock
   private Loadout      loadout;
   @Mock
   private Upgrades     upgrades;
   @Mock
   private MessageXBar  xBar;
   @Mock
   private InternalComponent internalPart;

   @Before
   public void setup(){
      Mockito.when(loadout.getUpgrades()).thenReturn(upgrades);
      Mockito.when(loadoutPart.getLoadout()).thenReturn(loadout);
      Mockito.when(loadoutPart.getInternalPart()).thenReturn(internalPart);
      Mockito.when(internalPart.getLocation()).thenReturn(Location.CenterTorso);
   }

   @Test
   public void testDescription(){
      Item item = ItemDB.ECM;

      OpRemoveItem cut = new OpRemoveItem(xBar, loadoutPart, item);

      assertTrue(cut.describe().contains("remove"));
      assertTrue(cut.describe().contains("from"));
      assertTrue(cut.describe().contains(loadoutPart.getInternalPart().getLocation().toString()));
      assertTrue(cut.describe().contains(item.getName()));
   }

   @Test
   public void testDescription_artemis(){
      Item item = ItemDB.lookup("LRM 20");
      Mockito.when(upgrades.getGuidance()).thenReturn(UpgradeDB.ARTEMIS_IV);

      OpRemoveItem cut = new OpRemoveItem(xBar, loadoutPart, item);

      assertTrue(cut.describe().contains("remove"));
      assertTrue(cut.describe().contains("from"));
      assertTrue(cut.describe().contains(loadoutPart.getInternalPart().getLocation().toString()));
      assertTrue(cut.describe().contains(item.getName(upgrades)));
   }

   /**
    * If an item can't be removed, an exception shall be thrown when the operation is applied.
    */
   @Test(expected = IllegalArgumentException.class)
   public void testCantRemoveItem(){
      OpRemoveItem cut = null;
      try{
         Item item = ItemDB.lookup("LRM 20");
         Mockito.when(loadoutPart.getItems()).thenReturn(new ArrayList<Item>());
         cut = new OpRemoveItem(xBar, loadoutPart, item);
      }
      catch( Throwable t ){
         fail("Setup failed");
         return;
      }

      cut.apply();
   }

   /**
    * Internal items can't be removed. Shall throw directly on creation.
    */
   @Test(expected = IllegalArgumentException.class)
   public void testCantRemoveInternal(){
      Internal item = Mockito.mock(Internal.class);
      new OpRemoveItem(xBar, loadoutPart, item);
   }
}
