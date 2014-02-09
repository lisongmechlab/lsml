/*
 * @formatter:off
 * Li Song Mechlab - A 'mech building tool for PGI's MechWarrior: Online.
 * Copyright (C) 2013  Li Song
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */  
//@formatter:on
package lisong_mechlab.model.garage;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;
import static org.mockito.Mockito.reset;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyZeroInteractions;

import java.io.File;
import java.io.IOException;

import lisong_mechlab.model.garage.MechGarage.Message;
import lisong_mechlab.model.garage.MechGarage.Message.Type;
import lisong_mechlab.model.loadout.Loadout;
import lisong_mechlab.util.MessageXBar;
import lisong_mechlab.util.OperationStack;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

public class MechGarageTest{

   File           testFile = null;

   @Mock
   MessageXBar    xBar;

   @Mock
   OperationStack undoStack;

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
      // Execute
      MechGarage cut = new MechGarage(xBar);

      // Verify
      verify(xBar).post(new Message(MechGarage.Message.Type.NewGarage, cut));
      verifyZeroInteractions(undoStack);

      assertTrue(cut.getMechs().isEmpty());
      assertNull(cut.getFile());
   }

   /**
    * Loading an empty garage shall produce an empty garage with the correct file path set.
    * 
    * @throws IOException
    */
   @Test
   public void testOpen() throws IOException{
      // Setup
      MechGarage savedGarage = new MechGarage(xBar);
      savedGarage.saveas(testFile);
      reset(xBar);

      // Execute
      MechGarage c = MechGarage.open(testFile, xBar);

      // Verify
      verify(xBar).post(new Message(MechGarage.Message.Type.NewGarage, c));
      verifyZeroInteractions(undoStack);

      assertTrue(c.getMechs().isEmpty());
      assertSame(testFile, c.getFile());
   }

   /**
    * Saving a mech garage that has not been loaded or saveas-ed before shall throw an error as it has no associated
    * file name.
    */
   @Test
   public void testSaveWithoutName(){
      // Setup
      MechGarage cut = new MechGarage(xBar);
      reset(xBar);

      // Execute
      try{
         cut.save();
         fail();
      }

      // Verify
      catch( IOException exception ){/* Expected exception */}
      verifyZeroInteractions(xBar);
      verifyZeroInteractions(undoStack);
   }

   /**
    * Attempting to use {@link MechGarage#saveas(File)} on a file that already exist shall throw without editing the
    * file.
    * 
    * @throws IOException
    *            Shouldn't be thrown.
    */
   @Test
   public void testSaveOverwrite() throws IOException{
      // Setup
      MechGarage cut = new MechGarage(xBar);
      cut.saveas(testFile);
      testFile.setLastModified(0);
      reset(xBar);

      // Execute
      try{
         cut.saveas(testFile); // File already exists
         fail(); // Must throw!
      }

      // Verify
      catch( IOException e ){
         assertEquals(0, testFile.lastModified()); // Must not have been modified
      }
      verifyZeroInteractions(xBar);
      verifyZeroInteractions(undoStack);
   }

   /**
    * {@link MechGarage#saveas(File)} shall produce a file that can be subsequently
    * {@link MechGarage#open(File, MessageXBar)}ed to restore the contents of the garage before the call to
    * {@link MechGarage#saveas(File)}
    * 
    * @throws Exception
    *            Shouldn't be thrown.
    */
   @Test
   public void testSaveAsOpen() throws Exception{
      // Setup
      Loadout lo1 = new Loadout("as7-d-dc", xBar);
      Loadout lo2 = new Loadout("as7-k", xBar);
      MechGarage cut = new MechGarage(xBar);
      cut.add(lo1);
      cut.add(lo2);
      reset(xBar);
      reset(undoStack);

      // Execute
      cut.saveas(testFile);
      MechGarage loadedGarage = MechGarage.open(testFile, xBar);

      // Verify
      verify(xBar).post(new MechGarage.Message(Type.Saved, cut));
      verify(xBar).post(new MechGarage.Message(Type.NewGarage, loadedGarage));
      verifyZeroInteractions(undoStack);
      assertEquals(2, loadedGarage.getMechs().size());
      assertEquals(lo1, loadedGarage.getMechs().get(0));
      assertEquals(lo2, loadedGarage.getMechs().get(1));
   }

   /**
    * {@link MechGarage#save()} shall overwrite previously saved garage.
    * 
    * @throws Exception
    *            Shouldn't be thrown.
    */
   @Test
   public void testSave() throws Exception{
      // Setup
      Loadout lo1 = new Loadout("as7-d-dc", xBar);
      Loadout lo2 = new Loadout("as7-k", xBar);
      MechGarage cut = new MechGarage(xBar);
      cut.add(lo1);
      cut.saveas(testFile); // Create garage with one mech and save it.
      cut = MechGarage.open(testFile, xBar);
      cut.add(lo2); // Add a mech and use the save() function. The same file should have been overwritten.
      reset(xBar);
      reset(undoStack);

      // Execute
      cut.save();

      // Open the garage to verify.
      verify(xBar).post(new MechGarage.Message(Type.Saved, cut));
      verifyZeroInteractions(undoStack);

      cut = MechGarage.open(testFile, xBar);
      assertEquals(2, cut.getMechs().size());
      assertEquals(lo1, cut.getMechs().get(0));
      assertEquals(lo2, cut.getMechs().get(1));
   }

   /**
    * add(Loadout, boolean) shall add a loadout to the garage that can subsequently be removed with remove(Loadout,
    * boolean).
    * 
    * @throws Exception
    *            Shouldn't be thrown.
    */
   @Test
   public void testAddRemoveLoadout() throws Exception{
      // Setup
      Loadout loadout = new Loadout("as7-d-dc", xBar);
      MechGarage cut = new MechGarage(xBar);
      reset(undoStack);

      // Execute
      cut.add(loadout);

      // Verify
      assertEquals(1, cut.getMechs().size());
      assertSame(loadout, cut.getMechs().get(0));
      verify(xBar).post(new Message(MechGarage.Message.Type.LoadoutAdded, cut, loadout));
      verifyZeroInteractions(undoStack);

      // Execute
      cut.remove(loadout);

      // Verify
      assertTrue(cut.getMechs().isEmpty());
      verify(xBar).post(new Message(MechGarage.Message.Type.LoadoutRemoved, cut, loadout));
      verifyZeroInteractions(undoStack);
   }

   /**
    * Adding the same {@link Loadout} twice is an error and shall throw an {@link IllegalArgumentException}.
    * 
    * @throws Exception
    *            Shouldn't be thrown.
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
         reset(undoStack);
         cut.add(loadout);
         fail("Expected exception!");
      }
      catch( IllegalArgumentException e ){
         // Success!
         verifyZeroInteractions(xBar);
         verifyZeroInteractions(undoStack);
      }
      catch( Exception e ){
         fail("Wrong exception!");
      }
   }

   /**
    * Removing an nonexistent loadout is a no-op.
    * 
    * @throws Exception
    *            Shouldn't be thrown.
    */
   @Test
   public void testRemoveLoadoutNonexistent() throws Exception{
      // Setup
      Loadout loadout = new Loadout("as7-d-dc", xBar);
      MechGarage cut = new MechGarage(xBar);
      reset(xBar);
      reset(undoStack);
      cut.remove(loadout);

      verifyZeroInteractions(xBar);
      verifyZeroInteractions(undoStack);
   }

}
