package lisong_mechlab.view.action;

import java.awt.event.ActionEvent;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.JOptionPane;
import javax.swing.KeyStroke;

import lisong_mechlab.model.loadout.Loadout;
import lisong_mechlab.view.LoadoutFrame;
import lisong_mechlab.view.ProgramInit;

public class RenameLoadoutAction extends AbstractAction{
   private static final long  serialVersionUID = -673375419929455179L;
   private final LoadoutFrame loadoutFrame;
   private final Loadout      loadout;

   public RenameLoadoutAction(Loadout aLoadout, KeyStroke key){
      super("Rename loadout...");
      loadout = aLoadout;
      loadoutFrame = null;
      putValue(Action.ACCELERATOR_KEY, key);
   }

   public RenameLoadoutAction(LoadoutFrame aLoadoutFrame, KeyStroke key){
      super("Rename loadout...");
      loadout = aLoadoutFrame.getLoadout();
      loadoutFrame = aLoadoutFrame;
      putValue(Action.ACCELERATOR_KEY, key);
   }

   @Override
   public void actionPerformed(ActionEvent aE){
      String name = JOptionPane.showInputDialog(ProgramInit.lsml(), "Give a name", loadout.getName());
      if( name == null || name.isEmpty() ){
         JOptionPane.showMessageDialog(ProgramInit.lsml(), "No name given!");
         return;
      }
      loadout.rename(name);
   }
}
