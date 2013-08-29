package lisong_mechlab.view;

import java.beans.PropertyVetoException;
import java.util.ArrayList;
import java.util.List;

import javax.swing.JDesktopPane;
import javax.swing.event.InternalFrameEvent;
import javax.swing.event.InternalFrameListener;

import lisong_mechlab.model.MessageXBar;
import lisong_mechlab.model.loadout.Loadout;

public class LoadoutDesktop extends JDesktopPane implements InternalFrameListener{
   private static final long           serialVersionUID = -3967290040803547940L;
   private List<InternalFrameListener> listeners        = new ArrayList<InternalFrameListener>();

   public LoadoutDesktop(){
      setTransferHandler(new ItemTransferHandler());
   }
   
   public void openLoadout(Loadout aLoadout, MessageXBar anXBar){
      LoadoutFrame frame = new LoadoutFrame(aLoadout, anXBar);
      frame.addInternalFrameListener(this);
      add(frame);

      try{
         frame.setVisible(true);
         frame.setSelected(true);
         frame.setFocusable(true);
      }
      catch( PropertyVetoException e ){
      }
   }

   public void addInternalFrameListener(InternalFrameListener aListener){
      listeners.add(aListener);
   }

   @Override
   public void internalFrameActivated(InternalFrameEvent aE){
      for(InternalFrameListener frameListener : listeners){
         frameListener.internalFrameActivated(aE);
      }
   }

   @Override
   public void internalFrameClosed(InternalFrameEvent aE){
      for(InternalFrameListener frameListener : listeners){
         frameListener.internalFrameClosed(aE);
      }
   }

   @Override
   public void internalFrameClosing(InternalFrameEvent aE){
      LoadoutFrame loadoutFrame = (LoadoutFrame)aE.getInternalFrame();

      if( !loadoutFrame.isSaved() ){
         // TODO: Ask to save
      }

      for(InternalFrameListener frameListener : listeners){
         frameListener.internalFrameClosing(aE);
      }
   }

   @Override
   public void internalFrameDeactivated(InternalFrameEvent aE){
      for(InternalFrameListener frameListener : listeners){
         frameListener.internalFrameDeactivated(aE);
      }
   }

   @Override
   public void internalFrameDeiconified(InternalFrameEvent aE){
      for(InternalFrameListener frameListener : listeners){
         frameListener.internalFrameDeiconified(aE);
      }
   }

   @Override
   public void internalFrameIconified(InternalFrameEvent aE){
      for(InternalFrameListener frameListener : listeners){
         frameListener.internalFrameIconified(aE);
      }
   }

   @Override
   public void internalFrameOpened(InternalFrameEvent aE){
      for(InternalFrameListener frameListener : listeners){
         frameListener.internalFrameOpened(aE);
      }
   }
}
