package lisong_mechlab.view;

import java.awt.Color;
import java.beans.PropertyVetoException;
import java.util.ArrayList;
import java.util.List;

import javax.swing.BorderFactory;
import javax.swing.JDesktopPane;
import javax.swing.JInternalFrame;
import javax.swing.SwingUtilities;
import javax.swing.event.InternalFrameEvent;
import javax.swing.event.InternalFrameListener;

import lisong_mechlab.model.loadout.Loadout;
import lisong_mechlab.util.MessageXBar;

/**
 * This class is the {@link JDesktopPane} where all the {@link LoadoutFrame} are shown to the user. It provides a method
 * to be notified of the focus of the frames.
 * <p>
 * All methods must be called from the Swing EDT.
 * 
 * @author Emily Björk
 */
public class LoadoutDesktop extends JDesktopPane implements InternalFrameListener{
   private static final long                 serialVersionUID = -3967290040803547940L;
   private final List<InternalFrameListener> listeners        = new ArrayList<InternalFrameListener>();
   private final MessageXBar                 xBar;

   /**
    * Creates a new {@link LoadoutDesktop}.
    */
   public LoadoutDesktop(MessageXBar anXBar){
      assert (SwingUtilities.isEventDispatchThread());

      xBar = anXBar;
      setBorder(BorderFactory.createLoweredSoftBevelBorder());
      setBackground(Color.GRAY.brighter());
      setTransferHandler(new ItemTransferHandler());
   }

   /**
    * Will open the given {@link Loadout} into the desktop pane by creating a new {@link LoadoutFrame}.
    * 
    * @param aLoadout
    *           The {@link Loadout} to create the frame for.
    */
   public void openLoadout(Loadout aLoadout){
      assert (SwingUtilities.isEventDispatchThread());

      LoadoutFrame frame = new LoadoutFrame(aLoadout, xBar);
      frame.addInternalFrameListener(this); // The desktop acts as forwarder of frame events from the frames.
      add(frame);

      try{
         frame.setVisible(true);
         frame.setSelected(true);
         frame.setFocusable(true);
      }
      catch( PropertyVetoException e ){
         // No-Op
      }
   }

   /**
    * Closes all open {@link LoadoutFrame}s. Exceptions from the frames are swallowed.
    */
   public void closeAll(){
      assert (SwingUtilities.isEventDispatchThread());

      for(JInternalFrame frame : getAllFrames()){
         try{
            frame.setClosed(true);
         }
         catch( PropertyVetoException e ){
            // No-Op
         }
         frame.dispose();
      }
   }

   /**
    * Allows the given {@link InternalFrameListener} to receive {@link InternalFrameEvent}s from any subwindow of this
    * {@link LoadoutDesktop}.
    * 
    * @param aListener
    *           The listener to send messages to. A <code>null</code> argument will cause a {@link NullPointerException}
    *           .
    */
   public void addInternalFrameListener(InternalFrameListener aListener){
      assert (SwingUtilities.isEventDispatchThread());
      if( null == aListener )
         throw new NullPointerException("Received a null listener to addInternalFrameListener()!");
      listeners.add(aListener);
   }

   /**
    * Removes the given {@link InternalFrameListener} from this {@link LoadoutDesktop}. No further messages will be
    * sent. No exception is thrown on a <code>null</code> argument or if the argument is not a listener of this
    * {@link LoadoutDesktop}.
    * 
    * @param aListener
    *           The listener to remove.
    */
   public void removeInternalFrameListener(InternalFrameListener aListener){
      listeners.remove(aListener);
   }

   @Override
   public void internalFrameActivated(InternalFrameEvent aE){
      assert (SwingUtilities.isEventDispatchThread());
      for(InternalFrameListener frameListener : listeners){
         frameListener.internalFrameActivated(aE);
      }
   }

   @Override
   public void internalFrameClosed(InternalFrameEvent aE){
      assert (SwingUtilities.isEventDispatchThread());
      for(InternalFrameListener frameListener : listeners){
         frameListener.internalFrameClosed(aE);
      }
   }

   @Override
   public void internalFrameClosing(InternalFrameEvent aE){
      assert (SwingUtilities.isEventDispatchThread());
      for(InternalFrameListener frameListener : listeners){
         frameListener.internalFrameClosing(aE);
      }
   }

   @Override
   public void internalFrameDeactivated(InternalFrameEvent aE){
      assert (SwingUtilities.isEventDispatchThread());
      for(InternalFrameListener frameListener : listeners){
         frameListener.internalFrameDeactivated(aE);
      }
   }

   @Override
   public void internalFrameDeiconified(InternalFrameEvent aE){
      assert (SwingUtilities.isEventDispatchThread());
      for(InternalFrameListener frameListener : listeners){
         frameListener.internalFrameDeiconified(aE);
      }
   }

   @Override
   public void internalFrameIconified(InternalFrameEvent aE){
      assert (SwingUtilities.isEventDispatchThread());
      for(InternalFrameListener frameListener : listeners){
         frameListener.internalFrameIconified(aE);
      }
   }

   @Override
   public void internalFrameOpened(InternalFrameEvent aE){
      assert (SwingUtilities.isEventDispatchThread());
      for(InternalFrameListener frameListener : listeners){
         frameListener.internalFrameOpened(aE);
      }
   }
}
