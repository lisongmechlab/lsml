package lisong_mechlab.view.action;

import java.awt.event.ActionEvent;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.KeyStroke;

import lisong_mechlab.model.loadout.Loadout;
import lisong_mechlab.util.EncodingException;
import lisong_mechlab.view.ProgramInit;

/**
 * Clones an existing loadout under a new name.
 * 
 * @author Li Song
 */
public class CloneLoadoutAction extends AbstractAction{
   private static final long serialVersionUID = 2146995440483341395L;
   private final Loadout loadout;
   
   public CloneLoadoutAction(String aTitle, Loadout aLoadout, KeyStroke aKeyStroke){
      super(aTitle);
      loadout = aLoadout;
      putValue(Action.ACCELERATOR_KEY, aKeyStroke);
   }

   @Override
   public void actionPerformed(ActionEvent aArg0){
      try{
         // TODO: Create a proper clone function, encoding to a string and recoding is silly
         ProgramInit.lsml().desktop.openLoadout(ProgramInit.lsml().loadoutCoder.encode(loadout));
      }
      catch( EncodingException e ){
         throw new RuntimeException(e);
      }
   }
}
