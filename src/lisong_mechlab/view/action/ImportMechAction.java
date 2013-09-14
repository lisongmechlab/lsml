package lisong_mechlab.view.action;

import java.awt.event.ActionEvent;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.JOptionPane;
import javax.swing.KeyStroke;

import lisong_mechlab.view.ProgramInit;

public class ImportMechAction extends AbstractAction{
   private static final long serialVersionUID = -9019953619423428349L;

   public ImportMechAction(String aTitle, KeyStroke key){
      super(aTitle);
      putValue(Action.ACCELERATOR_KEY, key);
   }
   @Override
   public void actionPerformed(ActionEvent aArg0){
      String input = JOptionPane.showInputDialog(null, "Paste the lsml:// link:", "Import mech...", JOptionPane.PLAIN_MESSAGE);
      ProgramInit.lsml().desktop.openLoadout(input);
   }

}
