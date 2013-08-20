package lisong_mechlab.model.item;

import static org.junit.Assert.*;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import lisong_mechlab.model.MessageXBar;
import lisong_mechlab.model.chassi.ChassiDB;
import lisong_mechlab.model.chassi.HardpointType;
import lisong_mechlab.model.loadout.Loadout;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

public class ItemTests{
   @Mock
   MessageXBar xBar;
   
   @Before
   public void setup(){
       MockitoAnnotations.initMocks(this);
   }
   
   @Test
   public void testEngines() throws Exception{
      Loadout hm = new Loadout(ChassiDB.lookup("Heavy Metal"), xBar);
      
      Engine std175 = (Engine)ItemDB.lookup("STD ENGINE 175");
      Engine std180 = (Engine)ItemDB.lookup("STD ENGINE 180");
      Engine xl330 = (Engine)ItemDB.lookup("XL ENGINE 330");
      Engine xl335 = (Engine)ItemDB.lookup("XL ENGINE 335");
      
      assertEquals(6, std175.getNumCriticalSlots());
      assertEquals(6, std180.getNumCriticalSlots());
      //assertEquals(12, xl330.getNumCriticalSlots());
      //assertEquals(12, xl335.getNumCriticalSlots());
      
      
      assertEquals(9.0, std175.getMass(), 0.0);
      assertEquals(9.0, std180.getMass(), 0.0);
      assertEquals(19.5, xl330.getMass(), 0.0);
      assertEquals(20.0, xl335.getMass(), 0.0);
      
      // Heavy Metal can equip 180-330 engines
      assertFalse(std175.isEquippableOn(hm)); 
      assertTrue(std180.isEquippableOn(hm));
      assertTrue(xl330.isEquippableOn(hm));
      assertFalse(xl335.isEquippableOn(hm));
   }

   /**
    * 
    */
   @Test
   public void testAMS(){
      AmmoWeapon ams = (AmmoWeapon)ItemDB.lookup("ANTI-MISSILE SYSTEM");
      assertEquals(1, ams.getNumCriticalSlots());
      assertEquals(0.5, ams.getMass(), 0.0);
      assertEquals(HardpointType.AMS, ams.getHardpointType());
   }

   /**
    * ECM/BAP/CC/CASE etc should exist and only be equippable on the correct mechs
    * @throws Exception 
    */
   @Test
   public void testModules() throws Exception{

      Item ECM = ItemDB.lookup("GUARDIAN ECM");
      Item CC = ItemDB.lookup("COMMAND CONSOLE");
      Item BAP = ItemDB.lookup("BEAGLE ACTIVE PROBE");
      Item Case = ItemDB.lookup("C.A.S.E.");
      
      Item JJC3 = ItemDB.lookup("Jump Jets - Class III");
      Item JJC4 = ItemDB.lookup("Jump Jets - Class IV");
      Item JJC5 = ItemDB.lookup("Jump Jets - Class V");

      assertEquals(2, ECM.getNumCriticalSlots());
      assertEquals(1, CC.getNumCriticalSlots());
      assertEquals(2, BAP.getNumCriticalSlots());
      assertEquals(1, Case.getNumCriticalSlots());
      assertEquals(1, JJC3.getNumCriticalSlots());
      assertEquals(1, JJC4.getNumCriticalSlots());
      assertEquals(1, JJC5.getNumCriticalSlots());

      assertEquals(1.5, ECM.getMass(), 0.0);
      assertEquals(3, CC.getMass(), 0.0);
      assertEquals(1.5, BAP.getMass(), 0.0);
      assertEquals(0.5, Case.getMass(), 0.0);
      assertEquals(1, JJC3.getMass(), 0.0);
      assertEquals(0.5, JJC4.getMass(), 0.0);
      assertEquals(0.5, JJC5.getMass(), 0.0);
      
      assertTrue(ECM.isEquippableOn(new Loadout("AS7-D-DC", xBar)));
      assertFalse(ECM.isEquippableOn(new Loadout("JR7-K", xBar)));
      
      assertTrue(JJC5.isEquippableOn(new Loadout("JR7-F", xBar)));
      assertFalse(JJC5.isEquippableOn(new Loadout("RVN-3L", xBar)));
      assertFalse(JJC5.isEquippableOn(new Loadout("TBT-5J", xBar)));
      assertFalse(JJC5.isEquippableOn(new Loadout("CTF-3D", xBar)));
      
      assertFalse(JJC4.isEquippableOn(new Loadout("JR7-F", xBar)));
      assertFalse(JJC4.isEquippableOn(new Loadout("hbk-4j", xBar)));
      assertTrue(JJC4.isEquippableOn(new Loadout("TBT-5J", xBar)));
      assertFalse(JJC4.isEquippableOn(new Loadout("CTF-3D", xBar)));
      
      assertFalse(JJC3.isEquippableOn(new Loadout("JR7-F", xBar)));
      assertFalse(JJC3.isEquippableOn(new Loadout("ILYA MUROMETS", xBar)));
      assertFalse(JJC3.isEquippableOn(new Loadout("TBT-5J", xBar)));
      assertTrue(JJC3.isEquippableOn(new Loadout("CTF-3D", xBar)));
   }

   /**
    * No item must exist twice in the database!
    */
   @Test
   public void testNoDoubles(){
      Collection<Item> items = ItemDB.lookup(Item.class);

      List<Item> found = new ArrayList<Item>();
      for(Item item : items){
         if( !found.contains(item) )
            found.add(item);
         else
            fail();
      }
   }

   /**
    * All weapons with ammo must have a valid ammo item and all ammo items are valid
    */
   @Test
   public void testWeaponsHaveAmmo(){
      Collection<AmmoWeapon> items = ItemDB.lookup(AmmoWeapon.class);

      for(AmmoWeapon item : items){
         Ammunition ammunition = item.getAmmoType();
         assertNotNull(ammunition);

         assertEquals(1.0, ammunition.getMass(), 0.0); // All ammo weigh 1 ton!
         assertNotNull(ammunition.getName()); // All ammo must have a name!

         assertEquals(1, ammunition.getNumCriticalSlots());
         assertTrue(ammunition.getShotsPerTon() > 0);

         // The name of the ammo must be traceable to the weapon
         String weaponPart = item.getName();
         if( weaponPart.indexOf(" ") != -1 ){
            weaponPart = weaponPart.substring(0, weaponPart.indexOf(" "));
         }

         if( item.getName().equals("ANTI-MISSILE SYSTEM") ){
            assertTrue(ammunition.getName().equals("AMS AMMO"));
         }
         else{
            assertTrue(ammunition.getName().contains(weaponPart)); // The ammo name must contain part of the weapon
                                                                   // name!
         }
      }
   }

   /**
    * There must be heat sinks in the item database
    */
   @Test
   public void testHeatsinks(){
      Collection<HeatSink> heatsinks = ItemDB.lookup(HeatSink.class);

      // Should contain double and standard
      assertEquals(2, heatsinks.size());

      // All parameters should be positive
      HeatSink sgl = null;
      HeatSink dbl = null;
      for(HeatSink heatSink : heatsinks){
         assertTrue(heatSink.getDissapation() > 0);
         assertTrue(heatSink.getCapacity() > 0);

         // Determine which is double/single
         if( null == sgl ){
            sgl = heatSink;
         }
         else{
            if( heatSink.getDissapation() > sgl.getDissapation() ){
               dbl = heatSink;
            }
            else{
               dbl = sgl;
               sgl = heatSink;
            }
         }
      }

      // Double should have higher values than single
      assertTrue(dbl.getDissapation() > sgl.getDissapation());
      assertTrue(dbl.getCapacity() > sgl.getCapacity());

      assertEquals(3, dbl.getNumCriticalSlots());
      assertEquals(1, sgl.getNumCriticalSlots());
   }

   /**
    * Test missile weapons and ammo for ARTEMIS functionality.
    */
   @Test
   public void testArtemisWeapons(){

      Collection<MissileWeapon> weapons = ItemDB.lookup(MissileWeapon.class);
      for(MissileWeapon weapon : weapons){
         if( weapon.getName().toLowerCase().contains("streak") || weapon.getName().toLowerCase().contains("narc") ){
            // ARTEMIS does not affect SSRM
            for(boolean artemis : new boolean[] {true, false}){
               assertEquals(weapon.getMass(), weapon.getMass(artemis), 0.0);
               assertEquals(weapon.getName(), weapon.getName(artemis));
               assertEquals(weapon.getNumCriticalSlots(), weapon.getNumCriticalSlots(artemis));
               Ammunition ammunition = weapon.getAmmoType();
               assertEquals(ammunition.getName(), ammunition.getName(artemis));
            }
         }
         else{
            // Same number of slots if ARTEMIS is disabled.
            assertEquals(weapon.getNumCriticalSlots(), weapon.getNumCriticalSlots(false));
            
            // One more slot if ARTEMIS is enabled.
            assertEquals(weapon.getNumCriticalSlots() + 1, weapon.getNumCriticalSlots(true));

            // Same mass if ARTEMIS is disabled.
            assertEquals(weapon.getMass(), weapon.getMass(false), 0.0);

            // One ton more mass if ARTEMIS is enabled.
            assertEquals(weapon.getMass() + 1.0, weapon.getMass(true), 0.0);

            // Weapon name must always contains he original name regardless of ARTEMIS status
            assertEquals(weapon.getName(), weapon.getName(false));
            assertTrue(weapon.getName(true).contains(weapon.getName())); 
            
            // Name contains ARTEMIS if ARTEMIS is enabled
            assertFalse(weapon.getName(false).toLowerCase().contains("artemis"));
            assertTrue(weapon.getName(true).toLowerCase().contains("artemis"));
            
            // Ammo name changes to reflect ARTEMIS status
            Ammunition ammunition = weapon.getAmmoType();
            assertEquals(ammunition.getName(), ammunition.getName(false));
            assertEquals(ammunition.getName() + " + ARTEMIS", ammunition.getName(true));
         }
      }

   }

   /**
    * Weapons that can not have ARTEMIS shouldn't have their ammo name affected by artemis status.
    */
   @Test
   public void testNonArtemisWeaponAmmo(){
      Collection<AmmoWeapon> weapons = ItemDB.lookup(AmmoWeapon.class);

      for(AmmoWeapon weapon : weapons){
         if( weapon.getName().toLowerCase().contains("lrm") )
            continue;
         if( weapon.getName().toLowerCase().contains("srm") && !weapon.getName().toLowerCase().contains("streak") )
            continue;

         for(boolean artemis : new boolean[] {true, false}){
            Ammunition ammunition = weapon.getAmmoType();
            assertEquals(ammunition.getName(), ammunition.getName(artemis));
         }
      }
   }
}
