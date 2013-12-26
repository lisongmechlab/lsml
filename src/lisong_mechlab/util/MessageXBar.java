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

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.List;

import lisong_mechlab.model.loadout.Loadout;

/**
 * Implements a message passing framework for an UI where the components don't have to know about each other, only about
 * the crossbar.
 * 
 * @author Emily Björk
 */
public class MessageXBar{
   private transient final List<WeakReference<Reader>> readers = new ArrayList<WeakReference<MessageXBar.Reader>>();

   /**
    * Classes that need to be able to listen in on the {@link MessageXBar} should implement this interface.
    * 
    * @author Emily Björk
    */
   public interface Reader{
      void receive(Message aMsg);
   }

   /**
    * A base interface for all messages sent on the {@link MessageXBar}.
    * 
    * @author Emily Björk
    */
   public interface Message{
      public boolean isForMe(Loadout aLoadout);
   }

   /**
    * Sends a message to all listeners on the {@link MessageXBar}. Those listeners which have been disposed of since the
    * last call to {@link #post(Message)} will be automatically disposed of.
    * 
    * @param aMessage
    *           The message to send.
    */
   public void post(Message aMessage){
      List<WeakReference<Reader>> toBeRemoved = new ArrayList<WeakReference<Reader>>();
      for(WeakReference<Reader> ref : readers){
         Reader reader = ref.get();
         if( reader != null )
            reader.receive(aMessage);
         else
            toBeRemoved.add(ref);
      }
      readers.removeAll(toBeRemoved);
   }

   /**
    * Attaches a new {@link Reader} to this {@link MessageXBar}. The {@link Reader} is automatically converted to a weak
    * reference.
    * 
    * @see #attach(Reader)
    * @param aReader
    *           The {@link Reader} to add.
    */
   public void attach(Reader aReader){
      attach(new WeakReference<MessageXBar.Reader>(aReader));
   }

   /**
    * Attaches a new {@link Reader} to this {@link MessageXBar}. The {@link MessageXBar} only keeps weak references so
    * this won't prevent objects from being garbage collected.
    * 
    * @param aWeakReference
    *           The object that shall receive messages.
    */
   public void attach(WeakReference<Reader> aWeakReference){
      readers.add(aWeakReference);
   }

   /**
    * Detaches a {@link Reader} from the {@link MessageXBar}.
    * 
    * @param aReader
    *           The object that shall be removed messages.
    */
   public void detach(Reader aReader){
      List<WeakReference<Reader>> toBeRemoved = new ArrayList<WeakReference<Reader>>();
      for(WeakReference<Reader> ref : readers){
         if( ref.get() == aReader ){
            toBeRemoved.add(ref);
         }
      }
      readers.removeAll(toBeRemoved);
   }
}
