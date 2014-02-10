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
package lisong_mechlab.util;

import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertSame;
import lisong_mechlab.util.OperationStack.Operation;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InOrder;
import org.mockito.Mockito;
import org.mockito.runners.MockitoJUnitRunner;

/**
 * A test suite for {@link OperationStack}
 * 
 * @author Emily Björk
 */
@RunWith(MockitoJUnitRunner.class)
public class OperationStackTest{
   OperationStack cut;

   @Before
   public void setup(){
      cut = new OperationStack(256);

   }

   /**
    * {@link OperationStack#pushAndApply(Operation)} shall push an {@link Operation} and call it's {@link Operation#apply()}
    * function.
    */
   @Test
   public final void testPushAndApply(){
      Operation op = Mockito.mock(Operation.class);

      cut.pushAndApply(op);

      Mockito.verify(op).apply();
   }

   /**
    * {@link OperationStack#nextUndo()} shall return the {@link Operation} that would be undone if
    * {@link OperationStack#undo()} was called now.
    */
   @Test
   public final void testNextUndo(){
      Operation op = Mockito.mock(Operation.class);
      cut.pushAndApply(op);

      assertSame(op, cut.nextUndo());
   }

   /**
    * {@link OperationStack#nextUndo()} shall return <code>null</code> if there is nothing to undo.
    */
   @Test
   public final void testNextUndo_empty(){
      assertNull(cut.nextUndo());

      Operation op = Mockito.mock(Operation.class);
      cut.pushAndApply(op);
      cut.undo();

      assertNull(cut.nextUndo());
   }

   /**
    * {@link OperationStack#nextRedo()} shall return the {@link Operation} that would be done if
    * {@link OperationStack#redo()} was called now.
    */
   @Test
   public final void testNextRedo(){
      Operation op = Mockito.mock(Operation.class);
      cut.pushAndApply(op);
      cut.undo();

      assertSame(op, cut.nextRedo());
   }

   /**
    * {@link OperationStack#nextRedo()} shall return null if there is nothing to redo.
    */
   @Test
   public final void testNextRedo_empty(){
      assertNull(cut.nextRedo());

      Operation op = Mockito.mock(Operation.class);
      cut.pushAndApply(op);
      cut.undo();
      cut.redo();

      assertNull(cut.nextRedo());
   }

   @Test
   public final void testUndo(){
      Operation op = Mockito.mock(Operation.class);
      InOrder inOrder = Mockito.inOrder(op);

      cut.pushAndApply(op);
      cut.undo();

      inOrder.verify(op).apply();
      inOrder.verify(op).undo();
   }

   @Test
   public final void testUndo_emptystack(){
      cut.undo(); // No-op
   }

   @Test
   public final void testRedo_emptystack(){
      cut.redo(); // No-op
   }

   @Test
   public final void testRedo(){
      Operation op = Mockito.mock(Operation.class);
      InOrder inOrder = Mockito.inOrder(op);

      cut.pushAndApply(op);
      cut.undo();
      cut.redo();

      inOrder.verify(op).apply();
      inOrder.verify(op).undo();
      inOrder.verify(op).apply();
   }

   @Test
   public final void testRedoAfterApplyAfterUndo(){
      Operation a0 = Mockito.mock(Operation.class);
      Operation a1 = Mockito.mock(Operation.class);
      Operation a2 = Mockito.mock(Operation.class);
      InOrder inOrder = Mockito.inOrder(a0, a1, a2);

      cut.pushAndApply(a0);
      cut.pushAndApply(a1);
      cut.undo();
      cut.pushAndApply(a2);
      cut.undo();
      cut.undo();
      cut.redo();

      inOrder.verify(a0).apply();
      inOrder.verify(a1).apply();
      inOrder.verify(a1).undo();
      inOrder.verify(a2).apply();
      inOrder.verify(a2).undo();
      inOrder.verify(a0).undo();

      inOrder.verify(a0).apply();
   }

   @Test
   public final void testMaxDepth(){
      // Setup
      cut = new OperationStack(2);
      Operation a0 = Mockito.mock(Operation.class);
      Operation a1 = Mockito.mock(Operation.class);
      Operation a2 = Mockito.mock(Operation.class);
      InOrder inOrder = Mockito.inOrder(a0, a1, a2);

      // Execute
      cut.pushAndApply(a0);
      cut.pushAndApply(a1);
      cut.pushAndApply(a2);
      cut.undo();
      cut.undo();
      cut.undo();

      // Verify
      inOrder.verify(a0).apply();
      inOrder.verify(a1).apply();
      inOrder.verify(a2).apply();
      inOrder.verify(a2).undo();
      inOrder.verify(a1).undo();
      Mockito.verifyNoMoreInteractions(a0); // Undo not called
   }

   @Test
   public final void testApplyAfterUndo(){
      // Setup
      Operation a0 = Mockito.mock(Operation.class);
      Operation a1 = Mockito.mock(Operation.class);
      Operation a2 = Mockito.mock(Operation.class);
      InOrder inOrder = Mockito.inOrder(a0, a1, a2);

      // Execute
      cut.pushAndApply(a0);
      cut.pushAndApply(a1);
      cut.undo();
      cut.pushAndApply(a2);
      cut.undo();
      cut.undo();

      // Verify
      inOrder.verify(a0).apply();
      inOrder.verify(a1).apply();
      inOrder.verify(a1).undo();
      inOrder.verify(a2).apply();
      inOrder.verify(a2).undo();
      inOrder.verify(a0).undo();
   }

   @Test
   public final void testApplyAfterUndoCapped(){
      // Setup
      cut = new OperationStack(2);
      Operation a0 = Mockito.mock(Operation.class);
      Operation a1 = Mockito.mock(Operation.class);
      Operation a2 = Mockito.mock(Operation.class);
      Operation a3 = Mockito.mock(Operation.class);
      Operation a4 = Mockito.mock(Operation.class);
      InOrder inOrder = Mockito.inOrder(a0, a1, a2, a3, a4);

      // Execute
      cut.pushAndApply(a0);
      cut.pushAndApply(a1);
      cut.pushAndApply(a2);
      cut.pushAndApply(a3);
      cut.undo();
      cut.pushAndApply(a4);
      cut.undo();
      cut.undo();
      cut.undo();
      cut.undo();

      // Verify
      inOrder.verify(a0).apply();
      inOrder.verify(a1).apply();
      inOrder.verify(a2).apply();
      inOrder.verify(a3).apply();
      inOrder.verify(a3).undo();
      inOrder.verify(a4).apply();
      inOrder.verify(a4).undo();
      inOrder.verify(a2).undo();

      Mockito.verifyNoMoreInteractions(a0); // Fell off the undo stack
      Mockito.verifyNoMoreInteractions(a1);
   }

   @Test
   public final void testApplyAfterUndoAll(){
      // Setup
      Operation a0 = Mockito.mock(Operation.class);
      Operation a1 = Mockito.mock(Operation.class);
      Operation a2 = Mockito.mock(Operation.class);
      InOrder inOrder = Mockito.inOrder(a0, a1, a2);

      // Execute
      cut.pushAndApply(a0);
      cut.pushAndApply(a1);
      cut.undo();
      cut.undo();
      cut.undo(); // One extra
      cut.pushAndApply(a2);

      // Verify
      inOrder.verify(a0).apply();
      inOrder.verify(a1).apply();
      inOrder.verify(a1).undo();
      inOrder.verify(a0).undo();
      inOrder.verify(a2).apply();
   }
}
