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
package org.lisoft.lsml.util;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.lisoft.lsml.util.CommandStack.Command;
import org.mockito.InOrder;
import org.mockito.Mockito;
import org.mockito.junit.MockitoJUnitRunner;

import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertSame;
import static org.mockito.ArgumentMatchers.any;

/**
 * A test suite for {@link CommandStack} TODO: Test coalesceling
 *
 * @author Li Song
 */
@SuppressWarnings("javadoc")
@RunWith(MockitoJUnitRunner.class)
public class CommandStackTest {
    CommandStack cut;

    @Before
    public void setup() {
        cut = new CommandStack(256);

    }

    @Test
    public final void testApplyAfterUndo() throws Exception {
        // Setup
        final Command a0 = Mockito.mock(Command.class);
        final Command a1 = Mockito.mock(Command.class);
        final Command a2 = Mockito.mock(Command.class);
        final InOrder inOrder = Mockito.inOrder(a0, a1, a2);

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
    public final void testApplyAfterUndoAll() throws Exception {
        // Setup
        final Command a0 = Mockito.mock(Command.class);
        final Command a1 = Mockito.mock(Command.class);
        final Command a2 = Mockito.mock(Command.class);
        final InOrder inOrder = Mockito.inOrder(a0, a1, a2);

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

    @Test
    public final void testApplyAfterUndoCapped() throws Exception {
        // Setup
        cut = new CommandStack(2);
        final Command a0 = Mockito.mock(Command.class);
        final Command a1 = Mockito.mock(Command.class);
        final Command a2 = Mockito.mock(Command.class);
        final Command a3 = Mockito.mock(Command.class);
        final Command a4 = Mockito.mock(Command.class);
        final InOrder inOrder = Mockito.inOrder(a0, a1, a2, a3, a4);

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

        Mockito.verify(a0, Mockito.atLeastOnce()).canCoalesce(any(Command.class));
        Mockito.verify(a1, Mockito.atLeastOnce()).canCoalesce(any(Command.class));
        Mockito.verifyNoMoreInteractions(a0); // Fell off the undo stack
        Mockito.verifyNoMoreInteractions(a1);
    }

    @Test
    public final void testMaxDepth() throws Exception {
        // Setup
        cut = new CommandStack(2);
        final Command a0 = Mockito.mock(Command.class);
        final Command a1 = Mockito.mock(Command.class);
        final Command a2 = Mockito.mock(Command.class);
        final InOrder inOrder = Mockito.inOrder(a0, a1, a2);

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
        Mockito.verify(a0, Mockito.atLeastOnce()).canCoalesce(any(Command.class));
        Mockito.verifyNoMoreInteractions(a0); // Undo not called
    }

    /**
     * {@link CommandStack#nextRedo()} shall return the {@link Command} that would be done if
     * {@link CommandStack#redo()} was called now.
     */
    @Test
    public final void testNextRedo() throws Exception {
        final Command op = Mockito.mock(Command.class);
        cut.pushAndApply(op);
        cut.undo();

        assertSame(op, cut.nextRedo());
    }

    /**
     * {@link CommandStack#nextRedo()} shall return null if there is nothing to redo.
     */
    @Test
    public final void testNextRedo_empty() throws Exception {
        assertNull(cut.nextRedo());

        final Command op = Mockito.mock(Command.class);
        cut.pushAndApply(op);
        cut.undo();
        cut.redo();

        assertNull(cut.nextRedo());
    }

    /**
     * {@link CommandStack#nextUndo()} shall return the {@link Command} that would be undone if
     * {@link CommandStack#undo()} was called now.
     */
    @Test
    public final void testNextUndo() throws Exception {
        final Command op = Mockito.mock(Command.class);
        cut.pushAndApply(op);

        assertSame(op, cut.nextUndo());
    }

    /**
     * {@link CommandStack#nextUndo()} shall return <code>null</code> if there is nothing to undo.
     */
    @Test
    public final void testNextUndo_empty() throws Exception {
        assertNull(cut.nextUndo());

        final Command op = Mockito.mock(Command.class);
        cut.pushAndApply(op);
        cut.undo();

        assertNull(cut.nextUndo());
    }

    /**
     * {@link CommandStack#pushAndApply(Command)} shall push an {@link Command} and call it's {@link Command#apply()}
     * function.
     */
    @Test
    public final void testPushAndApply() throws Exception {
        final Command op = Mockito.mock(Command.class);

        cut.pushAndApply(op);

        Mockito.verify(op).apply();
    }

    @Test
    public final void testRedo() throws Exception {
        final Command op = Mockito.mock(Command.class);
        final InOrder inOrder = Mockito.inOrder(op);

        cut.pushAndApply(op);
        cut.undo();
        cut.redo();

        inOrder.verify(op).apply();
        inOrder.verify(op).undo();
        inOrder.verify(op).apply();
    }

    @Test
    public final void testRedoAfterApplyAfterUndo() throws Exception {
        final Command a0 = Mockito.mock(Command.class);
        final Command a1 = Mockito.mock(Command.class);
        final Command a2 = Mockito.mock(Command.class);
        final InOrder inOrder = Mockito.inOrder(a0, a1, a2);

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
    public final void testRedo_emptystack() throws Exception {
        cut.redo(); // No-op
    }

    @Test
    public final void testUndo() throws Exception {
        final Command op = Mockito.mock(Command.class);
        final InOrder inOrder = Mockito.inOrder(op);

        cut.pushAndApply(op);
        cut.undo();

        inOrder.verify(op).apply();
        inOrder.verify(op).undo();
    }

    @Test
    public final void testUndo_emptystack() {
        cut.undo(); // No-op
    }
}
