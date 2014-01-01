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

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.lang.ref.WeakReference;

import lisong_mechlab.util.MessageXBar.Message;
import lisong_mechlab.util.MessageXBar.Reader;

import org.junit.Test;

/**
 * A test suite for {@link MessageXBar}.
 * 
 * @author Emily Björk
 */
public class MessageXBarTest{
   MessageXBar cut = new MessageXBar();

   @Test
   public void testPostMessage(){
      // Setup
      Reader reader0 = mock(Reader.class);
      Reader reader1 = mock(Reader.class);
      Message msg = mock(Message.class);

      // Execute
      cut.attach(reader0);
      cut.attach(new WeakReference<Reader>(reader1));
      cut.post(msg);

      // Verify
      verify(reader0).receive(msg);
      verify(reader1).receive(msg);
   }

   @Test
   public void testDetach(){
      // Setup
      Reader reader0 = mock(Reader.class);
      Reader reader1 = mock(Reader.class);
      Message msg = mock(Message.class);

      // Execute
      cut.attach(new WeakReference<Reader>(reader0));
      cut.attach(new WeakReference<Reader>(reader1));
      cut.detach(reader0);
      cut.post(msg);

      // Verify
      verify(reader0, never()).receive(msg);
      verify(reader1).receive(msg);
   }

   @Test
   public void testWeakReference(){
      WeakReference<Reader> ref = mock(WeakReference.class);// new WeakReference<MessageXBar.Reader>(reader0);
      Reader reader0 = mock(Reader.class);
      Message msg0 = mock(Message.class);
      Message msg1 = mock(Message.class);
      Message msg2 = mock(Message.class);

      when(ref.get()).thenReturn(reader0, (Reader)null);

      // Execute
      cut.attach(ref);
      ref.clear();

      cut.post(msg0); // Stub will return the reader, it receives the message
      verify(reader0).receive(msg0);

      cut.post(msg1); // Stub will return null, the reader must not receive the message nor be queried again
      verify(reader0, never()).receive(msg1);

      cut.post(msg2);
      verify(reader0, never()).receive(msg2);

      // Verify
      verify(ref, times(2)).get();
   }
}
