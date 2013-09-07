package lisong_mechlab.view.action;

import java.awt.event.ActionEvent;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.KeyStroke;

import lisong_mechlab.view.ChassiListView;

public class OpenMechSelectorAction extends AbstractAction{
   private static final long serialVersionUID = 2954190742894237229L;

   public OpenMechSelectorAction(String aTitle, KeyStroke key){
      super(aTitle);
      putValue(Action.ACCELERATOR_KEY, key);
   }
   
   @Override
   public void actionPerformed(ActionEvent aArg0){
      new ChassiListView();
   }
}
