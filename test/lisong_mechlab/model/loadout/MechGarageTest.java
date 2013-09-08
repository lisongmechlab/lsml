package lisong_mechlab.model.loadout;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.io.File;
import java.io.IOException;

import lisong_mechlab.model.MessageXBar;
import lisong_mechlab.model.chassi.ChassiDB;
import lisong_mechlab.model.loadout.MechGarage.Message;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import static org.mockito.Mockito.*;

import org.mockito.MockitoAnnotations;

public class MechGarageTest{

   File        testFile = null;

   @Mock
   MessageXBar xBar;

   @Before
   public void setup(){
      MockitoAnnotations.initMocks(this);
      testFile = new File("test_mechgarage_" + Math.random() + ".xml");
   }

   @After
   public void teardown(){
      testFile.delete();
   }

   /**
    * Default constructing a mech garage gives an unnamed, empty garage.
    */
   @Test
   public void testMechGarage(){
      MechGarage cut = new MechGarage(xBar);
      assertTrue(cut.getMechs().isEmpty());
      assertNull(cut.getFile());

      verify(xBar).post(new Message(MechGarage.Message.Type.NewGarage, cut));
   }

   /**
    * Loading an empty garage shall produce an empty garage with the correct file path set.
    * 
    * @throws IOException
    */
   @Test
   public void testOpen() throws IOException{
      // Setup
      MessageXBar xBar2 = mock(MessageXBar.class);
      MechGarage cut = new MechGarage(xBar);
      cut.saveas(testFile);
      verify(xBar).post(new Message(MechGarage.Message.Type.Saved, cut));

      // Execute
      MechGarage c = MechGarage.open(testFile, xBar2);

      // Verify
      verify(xBar2).post(new Message(MechGarage.Message.Type.NewGarage, c));
      assertTrue(c.getMechs().isEmpty());
      assertSame(testFile, c.getFile());
   }

   /**
    * Saving a mech garage that has not been loaded or saveas-ed before shall throw an error as it has no associated
    * file name.
    * 
    * @throws IOException
    */
   @Test
   public void testSaveWithoutName() throws IOException{
      // Setup
      MechGarage cut = new MechGarage(xBar);

      verify(xBar).post(new Message(MechGarage.Message.Type.NewGarage, cut));
      verifyNoMoreInteractions(xBar); // We don't want any more messages on the xbar

      // Execute
      try{
         cut.save(); // Saving without a name shall throw
         fail();
      }
      catch( IOException exception ){
         // Success!
      }
   }

   /**
    * Attempting to use {@link MechGarage#saveas(File)} on a file that already exist shall throw without editing the
    * file.
    * 
    * @throws IOException
    */
   @Test
   public void testSaveOverwrite() throws IOException{
      // Setup
      MechGarage cut = new MechGarage(xBar);
      cut.saveas(testFile);
      testFile.setLastModified(0);

      // Execute
      try{
         cut.saveas(testFile); // File already exists
         fail(); // Must throw!
      }

      // Verify
      catch( IOException e ){
         assertEquals(0, testFile.lastModified()); // Must not have been modified
      }
      verify(xBar).post(new Message(MechGarage.Message.Type.Saved, cut));
   }

   @Test
   public void testSaveAs() throws Exception{
      Loadout lo1 = new Loadout("as7-d-dc", xBar);
      Loadout lo2 = new Loadout("as7-k", xBar);

      MechGarage cut0 = new MechGarage(xBar);
      cut0.add(lo1);
      cut0.add(lo2);

      cut0.saveas(testFile);

      MechGarage cut1 = MechGarage.open(testFile, xBar);

      assertEquals(2, cut1.getMechs().size());
      assertEquals(ChassiDB.lookup("as7-d-dc"), cut1.getMechs().get(0).getChassi());
      assertEquals(ChassiDB.lookup("as7-k"), cut1.getMechs().get(1).getChassi());
   }

   @Test
   public void testSave() throws Exception{
      Loadout lo1 = new Loadout("as7-d-dc", xBar);
      Loadout lo2 = new Loadout("as7-k", xBar);

      // Create garage with one mech and save it.
      MechGarage cut = new MechGarage(xBar);
      cut.add(lo1);
      cut.saveas(testFile);

      // Open garage, verify it is correct
      cut = MechGarage.open(testFile, xBar);
      assertEquals(1, cut.getMechs().size());
      assertEquals(ChassiDB.lookup("as7-d-dc"), cut.getMechs().get(0).getChassi());

      // Add a mech and use the save() function. The same file should have been overwritten.
      cut.add(lo2);
      cut.save();

      // Open the garage to verify.
      cut = MechGarage.open(testFile, xBar);
      assertEquals(2, cut.getMechs().size());
      assertEquals(ChassiDB.lookup("as7-d-dc"), cut.getMechs().get(0).getChassi());
      assertEquals(ChassiDB.lookup("as7-k"), cut.getMechs().get(1).getChassi());
   }

   @Test
   public void testAddRemoveLoadout() throws Exception{
      // Setup
      Loadout loadout = new Loadout("as7-d-dc", xBar);
      MechGarage cut = new MechGarage(xBar);

      // Execute
      cut.add(loadout);

      // Verify
      assertEquals(1, cut.getMechs().size());
      assertSame(loadout, cut.getMechs().get(0));
      verify(xBar).post(new Message(MechGarage.Message.Type.LoadoutAdded, cut, loadout));

      // Execute
      cut.remove(loadout);

      // Verify
      assertTrue(cut.getMechs().isEmpty());
      verify(xBar).post(new Message(MechGarage.Message.Type.LoadoutRemoved, cut, loadout));
   }

   /**
    * @throws Exception
    */
   @Test
   public void testAddLoadoutTwice() throws Exception{
      // Setup
      Loadout loadout = new Loadout("as7-d-dc", xBar);
      MechGarage cut = new MechGarage(xBar);

      // Execute
      cut.add(loadout);

      try{
         reset(xBar);
         cut.add(loadout);
         fail("Expected exception!");
      }
      catch( IllegalArgumentException e ){
         // Success!
         verifyZeroInteractions(xBar);
      }
      catch( Exception e ){
         fail("Wrong exception!");
      }
   }
}
