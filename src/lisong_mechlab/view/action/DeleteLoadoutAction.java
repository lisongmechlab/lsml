package lisong_mechlab.view.action;

import java.awt.event.ActionEvent;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.JOptionPane;
import javax.swing.KeyStroke;

import lisong_mechlab.model.loadout.Loadout;
import lisong_mechlab.model.loadout.MechGarage;

public class DeleteLoadoutAction extends AbstractAction{
   private static final long serialVersionUID = -4813215864397617783L;
   private final Loadout     loadout;
   private final MechGarage  garage;

   public DeleteLoadoutAction(MechGarage aGarage, Loadout aLoadout, KeyStroke key){
      super("Delete loadout");
      loadout = aLoadout;
      garage = aGarage;
      putValue(Action.ACCELERATOR_KEY, key);
   }

   @Override
   public void actionPerformed(ActionEvent aE){
      if( garage.getMechs().contains(loadout) ){
         int result = JOptionPane.showConfirmDialog(null, "Are you certain you want to delete the loadout: " + loadout.getName() + "?",
                                                    "Confirm operation", JOptionPane.YES_NO_OPTION);
         if( JOptionPane.YES_OPTION == result ){
            try{
               garage.remove(loadout);
            }
            catch( RuntimeException e ){
               JOptionPane.showMessageDialog(null,
                                             "An error occured!\n"
                                                   + "Please report an issue at https://github.com/EmilyBjoerk/lsml/issues and copy paste the following this message:\n"
                                                   + e.getMessage() + "\nStack trace:\n" + e.getStackTrace());
            }
         }
      }
   }
}
