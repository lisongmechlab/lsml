package lisong_mechlab.util;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.List;

/**
 * Implements a message passing framework for an UI where the components don't have to know about each other, only about
 * the crossbar.
 * 
 * @author Li Song
 */
public class MessageXBar{
   private final List<WeakReference<Reader>> readers = new ArrayList<WeakReference<MessageXBar.Reader>>();

   public interface Reader{
      void receive(Message aMsg);
   }

   public interface Message{
      /* Empty interface. */
   }

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

   public void attach(WeakReference<Reader> aWeakReference){
      readers.add(aWeakReference);
   }

   public void detach(Reader aReader){
      List<WeakReference<Reader>> toBeRemoved = new ArrayList<WeakReference<Reader>>();
      for(WeakReference<Reader> ref : readers){
         if( ref.get() == aReader ){
            toBeRemoved.add(ref);
         }
      }
      readers.removeAll(toBeRemoved);
   }

   public void attach(Reader aReader){
      attach(new WeakReference<MessageXBar.Reader>(aReader));
   }
}
