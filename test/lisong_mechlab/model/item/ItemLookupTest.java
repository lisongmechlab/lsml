package lisong_mechlab.model.item;

import static org.junit.Assert.*;

import java.util.Collection;

import lisong_mechlab.model.loadout.Upgrades;

import org.junit.Test;
import static org.mockito.Mockito.*;

public class ItemLookupTest{

   /**
    * The {@link ItemDB} should not contain internals!
    */
   @Test
   public void testLookupInternals(){
      assertEquals(0, ItemDB.lookup(Internal.class).size());
   }

   @Test
   public void testLookupClass(){
      Collection<EnergyWeapon> eweaps = ItemDB.lookup(EnergyWeapon.class);

      Collection<Item> items = ItemDB.lookup(Item.class); // Should be all items

      assertTrue(items.containsAll(eweaps));

      for(Item item : items){
         if( item instanceof EnergyWeapon ){
            assertTrue(eweaps.contains(item));
         }
      }
   }

   /**
    * We have to be able to find items by MWO ID/MWO Key and Name.
    */
   @Test
   public void testLookup(){
      // Setup
      String name = "STD ENGINE 105";
      Item expected = ItemDB.lookup(name);

      // Lookup by name
      assertNotNull(ItemDB.lookup(name));

      // Lookup by MWO ID
      assertSame(expected, ItemDB.lookup(3219));

      // Lookup by MWO name key
      assertSame(expected, ItemDB.lookup("Engine_Std_105"));

      // Lookup by MWO name key (ducked up case)
      assertSame(expected, ItemDB.lookup("EnGine_stD_105"));
   }

   @Test
   public void testArtemisLookup(){
      MissileWeapon missileWeapon = (MissileWeapon)ItemDB.lookup("LRM 20");
      Upgrades upgrades = mock(Upgrades.class);
      when(upgrades.hasArtemis()).thenReturn(true);

      assertSame(missileWeapon, ItemDB.lookup(missileWeapon.getName(upgrades)));
   }

   @Test(expected = IllegalArgumentException.class)
   public void testLookupFailNonExistentId(){
      ItemDB.lookup(98751823);
   }

   @Test(expected = IllegalArgumentException.class)
   public void testLookupFailNegativeId(){
      ItemDB.lookup(-1);
   }

   @Test(expected = IllegalArgumentException.class)
   public void testLookupFailBadName(){
      ItemDB.lookup("HumbungaDingDong!");
   }
}
