package lisong_mechlab.view.action;

import java.awt.event.ActionEvent;

import javax.swing.AbstractAction;
import javax.swing.JOptionPane;

import lisong_mechlab.model.loadout.Loadout;

public class RenameLoadoutAction extends AbstractAction{
   private static final long serialVersionUID = -673375419929455179L;
   private final Loadout     loadout;

   public RenameLoadoutAction(Loadout aLoadout){
      super("Rename loadout...");
      loadout = aLoadout;
   }

   @Override
   public void actionPerformed(ActionEvent aE){
      String name = JOptionPane.showInputDialog("Give a name", loadout.getName());
      if( name.isEmpty() ){
         JOptionPane.showMessageDialog(null, "No name given!");
         return;
      }
      loadout.rename(name);
   }

}
