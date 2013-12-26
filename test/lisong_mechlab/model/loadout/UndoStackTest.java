/*
 * @formatter:off
 * Li Song Mechlab - A 'mech building tool for PGI's MechWarrior: Online.
 * Copyright (C) 2013  Emily Björk
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
package lisong_mechlab.model.loadout;

import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;
import lisong_mechlab.model.loadout.MechGarage.GarageUndoAction;
import lisong_mechlab.model.loadout.MechGarage.Message.Type;
import lisong_mechlab.util.MessageXBar;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.runners.MockitoJUnitRunner;

/**
 * A test suite for {@link UndoStack}
 * 
 * @author Emily Björk
 */
@RunWith(MockitoJUnitRunner.class)
public class UndoStackTest{

   @Mock
   MessageXBar xBar;
   UndoStack   cut;

   @Before
   public void setup(){
      cut = new UndoStack(xBar, 256);

      Mockito.verify(xBar).attach(cut);
   }

   @Test
   public final void testMaxDepth(){
      // Setup
      cut = new UndoStack(xBar, 2);
      UndoAction a0 = Mockito.mock(UndoAction.class);
      UndoAction a1 = Mockito.mock(UndoAction.class);
      UndoAction a2 = Mockito.mock(UndoAction.class);

      // Execute
      cut.pushAction(a0);
      cut.pushAction(a1);
      cut.pushAction(a2);

      // Verify
      assertSame(a2, cut.latestGlobal());
      cut.undoAction(a2);
      assertSame(a1, cut.latestGlobal());
      cut.undoAction(a1);
      assertNull(cut.latestGlobal());
   }

   /**
    * {@link UndoStack#pushAction(UndoAction)} shall push an action onto the top of the stack.
    */
   @Test
   public final void testPushAction(){
      // Setup
      UndoAction a0 = Mockito.mock(UndoAction.class);
      UndoAction a1 = Mockito.mock(UndoAction.class);

      // Execute
      cut.pushAction(a0);
      cut.pushAction(a1);

      // Verify
      assertSame(a1, cut.latestGlobal());
      cut.undoAction(a1);
      assertSame(a0, cut.latestGlobal());
   }

   /**
    * {@link UndoStack#latestLoadout(Loadout)} shall only return the latest {@link UndoAction} for which
    * {@link UndoAction#affects(Loadout)} returns true for the argument.
    */
   @Test
   public final void testLatestLoadout(){
      // Setup
      UndoAction a0 = Mockito.mock(UndoAction.class);
      Loadout l0 = Mockito.mock(Loadout.class);
      Mockito.when(a0.affects(l0)).thenReturn(true);
      UndoAction a1 = Mockito.mock(UndoAction.class);
      UndoAction a2 = Mockito.mock(UndoAction.class);
      Loadout l2 = Mockito.mock(Loadout.class);
      Mockito.when(a2.affects(l2)).thenReturn(true);
      UndoAction a21 = Mockito.mock(UndoAction.class);
      Mockito.when(a21.affects(l2)).thenReturn(true);
      cut.pushAction(a0);
      cut.pushAction(a1);
      cut.pushAction(a2);
      cut.pushAction(a21);

      // Execute & Verify
      assertTrue(cut.latestLoadout(l0).affects(l0));
      assertTrue(cut.latestLoadout(l2).affects(l2));
      assertSame(a0, cut.latestLoadout(l0));
      assertSame(a21, cut.latestLoadout(l2));
   }

   /**
    * {@link UndoStack#latestLoadout(Loadout)} shall return null if no action matching the argument was found.
    */
   @Test
   public final void testLatestLoadout_notFound(){
      // Setup
      UndoAction a0 = Mockito.mock(UndoAction.class);
      Loadout l0 = Mockito.mock(Loadout.class);
      Mockito.when(a0.affects(l0)).thenReturn(true);
      UndoAction a1 = Mockito.mock(UndoAction.class);
      UndoAction a2 = Mockito.mock(UndoAction.class);
      Loadout l2 = Mockito.mock(Loadout.class);
      UndoAction a21 = Mockito.mock(UndoAction.class);
      cut.pushAction(a0);
      cut.pushAction(a1);
      cut.pushAction(a2);
      cut.pushAction(a21);

      // Execute & Verify
      assertNull(cut.latestLoadout(l2));
   }

   /**
    * {@link UndoStack#latestGlobal()} shall return the latest {@link UndoAction} regardless of what kind of action it
    * is.
    */
   @Test
   public final void testLatestGlobal(){
      // Setup
      UndoAction a0 = Mockito.mock(UndoAction.class);
      Loadout l0 = Mockito.mock(Loadout.class);
      Mockito.when(a0.affects(l0)).thenReturn(true);
      UndoAction a1 = Mockito.mock(UndoAction.class);
      UndoAction a2 = Mockito.mock(UndoAction.class);
      Loadout l2 = Mockito.mock(Loadout.class);
      Mockito.when(a2.affects(l2)).thenReturn(true);
      UndoAction a21 = Mockito.mock(UndoAction.class);
      Mockito.when(a21.affects(l2)).thenReturn(true);
      cut.pushAction(a0);
      cut.pushAction(a1);
      cut.pushAction(a2);
      cut.pushAction(a21);

      // Execute & Verify
      assertSame(a21, cut.latestGlobal());
   }

   /**
    * {@link UndoStack#latestGlobal()} shall return null if there are no actions to undo.
    */
   @Test
   public final void testLatestGlobal_notFound(){
      // Execute & Verify
      assertNull(cut.latestGlobal());
   }

   /**
    * {@link UndoStack#latestGarage()} Shall return the latest {@link UndoAction} which affected the current
    * {@link MechGarage}.
    */
   @Test
   public final void testLatestGarage(){
      // Setup
      GarageUndoAction garageUndoAction0 = Mockito.mock(GarageUndoAction.class);
      GarageUndoAction garageUndoAction1 = Mockito.mock(GarageUndoAction.class);
      UndoAction a0 = Mockito.mock(UndoAction.class);
      Loadout l0 = Mockito.mock(Loadout.class);
      Mockito.when(a0.affects(l0)).thenReturn(true);
      UndoAction a1 = Mockito.mock(UndoAction.class);
      UndoAction a2 = Mockito.mock(UndoAction.class);
      Loadout l2 = Mockito.mock(Loadout.class);
      Mockito.when(a2.affects(l2)).thenReturn(true);
      UndoAction a21 = Mockito.mock(UndoAction.class);
      Mockito.when(a21.affects(l2)).thenReturn(true);
      cut.pushAction(a0);
      cut.pushAction(garageUndoAction0);
      cut.pushAction(a1);
      cut.pushAction(garageUndoAction1);
      cut.pushAction(a2);
      cut.pushAction(a21);

      // Execute & Verify
      assertSame(garageUndoAction1, cut.latestGarage());
   }

   /**
    * {@link UndoStack#latestGarage()} Shall return the null if no garage actions were found.
    */
   @Test
   public final void testLatestGarage_notFound(){
      // Setup
      UndoAction a0 = Mockito.mock(UndoAction.class);
      UndoAction a1 = Mockito.mock(UndoAction.class);
      UndoAction a2 = Mockito.mock(UndoAction.class);
      cut.pushAction(a0);
      cut.pushAction(a1);
      cut.pushAction(a2);

      // Execute & Verify
      assertNull(cut.latestGarage());
   }

   /**
    * The undo stack shall reset when a new garage is loaded.
    */
   @Test
   public final void testReceive_NewGarage(){
      // Setup
      UndoAction a0 = Mockito.mock(UndoAction.class);
      cut.pushAction(a0);

      // Execute
      cut.receive(new MechGarage.Message(Type.NewGarage, null));

      // Verify
      assertNull(cut.latestGlobal());
   }

   /**
    * The undo stack shall not reset when other messages than new garage is received.
    */
   @Test
   public final void testReceive_others(){
      // Setup
      UndoAction a0 = Mockito.mock(UndoAction.class);
      cut.pushAction(a0);

      // Execute
      Loadout.Message loadoutMessage = Mockito.mock(Loadout.Message.class);
      cut.receive(loadoutMessage);

      // Verify
      assertSame(a0, cut.latestGlobal());
   }

   /**
    * The undo stack shall not reset when other messages than new garage is received.
    */
   @Test
   public final void testReceive_otherGarage(){
      // Setup
      UndoAction a0 = Mockito.mock(UndoAction.class);
      cut.pushAction(a0);

      // Execute
      cut.receive(new MechGarage.Message(Type.LoadoutAdded, null));

      // Verify
      assertSame(a0, cut.latestGlobal());
   }

   /**
    * {@link UndoStack#undoAction(UndoAction)} shall not do anything when called with an action that is not in the
    * stack.
    */
   @Test
   public final void testUndoAction_invalid(){
      // Setup
      UndoAction action = Mockito.mock(UndoAction.class);

      // Execute
      cut.undoAction(action);

      // Verify
      Mockito.verifyZeroInteractions(action);
   }

   /**
    * {@link UndoStack#undoAction(UndoAction)} shall undo the action when called with an action that is in the stack.
    * And remove it from the stack so subsequent calls to undoAction will not do anything.
    */
   @Test
   public final void testUndoAction_valid(){
      // Setup
      UndoAction action = Mockito.mock(UndoAction.class);
      cut.pushAction(action);

      // Execute
      cut.undoAction(action);
      cut.undoAction(action);

      // Verify
      Mockito.verify(action, Mockito.times(1)).undo();
   }

   /**
    * {@link UndoStack#clearLoadout(Loadout)} shall remove all actions from the stack that affects the given loadout.
    */
   @Test
   public final void testClearLoadout(){
      // Setup
      GarageUndoAction garageUndoAction0 = Mockito.mock(GarageUndoAction.class);
      UndoAction a0 = Mockito.mock(UndoAction.class);
      Loadout l0 = Mockito.mock(Loadout.class);
      Mockito.when(a0.affects(l0)).thenReturn(true);
      UndoAction a1 = Mockito.mock(UndoAction.class);
      Loadout l1 = Mockito.mock(Loadout.class);
      Mockito.when(a1.affects(l1)).thenReturn(true);
      cut.pushAction(a0);
      cut.pushAction(garageUndoAction0);
      cut.pushAction(a1);

      // Execute
      cut.clearLoadout(l1);

      // Verify
      assertSame(garageUndoAction0, cut.latestGarage()); // unaffected
      assertSame(a0, cut.latestLoadout(l0)); // unaffected
      assertNull(cut.latestLoadout(l1)); // removed
   }
}
