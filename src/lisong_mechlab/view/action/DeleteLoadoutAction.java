package lisong_mechlab.view.action;

import java.awt.event.ActionEvent;

import javax.swing.AbstractAction;
import javax.swing.JOptionPane;

import lisong_mechlab.model.loadout.Loadout;
import lisong_mechlab.model.loadout.MechGarage;

public class DeleteLoadoutAction extends AbstractAction{
   private static final long serialVersionUID = -4813215864397617783L;
   private final Loadout     loadout;
   private final MechGarage  garage;

   public DeleteLoadoutAction(MechGarage aGarage, Loadout aLoadout){
      super("Delete loadout");
      loadout = aLoadout;
      garage = aGarage;
   }

   @Override
   public void actionPerformed(ActionEvent aE){
      if( garage.getMechs().contains(loadout) ){
         int result = JOptionPane.showConfirmDialog(null, "Are you certain you want to delete this loadout?", "Confirm operation",
                                                    JOptionPane.YES_NO_OPTION);
         if( JOptionPane.YES_OPTION == result ){
            try{
               garage.remove(loadout);
            }
            catch( RuntimeException e ){
               JOptionPane.showMessageDialog(null,
                                             "An error occured!\n"
                                                   + "Please report an issue at https://github.com/lisongmechlab/lsml/issues and copy paste the following this message:\n"
                                                   + e.getMessage() + "\nStack trace:\n" + e.getStackTrace());
            }
         }
      }
   }
}
