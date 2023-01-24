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
package org.lisoft.lsml.messages;

import org.junit.Test;

import java.lang.ref.WeakReference;

import static org.mockito.Mockito.*;

/**
 * A test suite for {@link MessageXBar}.
 *
 * @author Li Song
 */
@SuppressWarnings("unchecked")
public class MessageXBarTest {
    MessageXBar cut = new MessageXBar();

    @Test
    public void testDetach() {
        // Setup
        final MessageReceiver reader0 = mock(MessageReceiver.class);
        final MessageReceiver reader1 = mock(MessageReceiver.class);
        final Message msg = mock(Message.class);

        // Execute
        cut.attach(new WeakReference<>(reader0));
        cut.attach(new WeakReference<>(reader1));
        cut.detach(reader0);
        cut.post(msg);

        // Verify
        verify(reader0, never()).receive(msg);
        verify(reader1).receive(msg);
    }

    @Test
    public void testPostMessage() {
        // Setup
        final MessageReceiver reader0 = mock(MessageReceiver.class);
        final MessageReceiver reader1 = mock(MessageReceiver.class);
        final Message msg = mock(Message.class);

        // Execute
        cut.attach(reader0);
        cut.attach(new WeakReference<>(reader1));
        cut.post(msg);

        // Verify
        verify(reader0).receive(msg);
        verify(reader1).receive(msg);
    }

    @Test
    public void testWeakReference() {
        final MessageReceiver reader0 = mock(MessageReceiver.class);
        final WeakReference<MessageReceiver> ref = new WeakReference<>(reader0);
        final Message msg0 = mock(Message.class);
        final Message msg1 = mock(Message.class);
        final Message msg2 = mock(Message.class);

        cut.attach(ref);

        cut.post(msg0);
        verify(reader0).receive(msg0);

        ref.clear();
        cut.post(msg1);
        verify(reader0, never()).receive(msg1);
    }
}
