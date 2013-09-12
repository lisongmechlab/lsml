package lisong_mechlab.view.action;

import java.awt.event.ActionEvent;

import javax.swing.AbstractAction;
import javax.swing.JOptionPane;

import lisong_mechlab.model.loadout.Loadout;
import lisong_mechlab.model.loadout.export.Base64Import;

/**
 * This action opens up a share frame where the user can copy the link to the build.
 * 
 * @author Emily Björk
 */
public class ShareLoadoutAction extends AbstractAction{
   private static final long serialVersionUID = 6535485629587481198L;
   private final Loadout     loadout;

   public ShareLoadoutAction(Loadout aLoadout){
      super("Share!");
      loadout = aLoadout;
   }

   @Override
   public void actionPerformed(ActionEvent aArg0){
      JOptionPane.showMessageDialog(null, Base64Import.encode(loadout));
   }

}
