package lisong_mechlab.view.action;

import java.awt.event.ActionEvent;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.KeyStroke;

import lisong_mechlab.view.help.OnlineHelp;

public class OpenHelp extends AbstractAction{
   private static final long serialVersionUID = 1675788460737342705L;
   private final String      helpTag;

   public OpenHelp(String aTitle, String aHelpTag, KeyStroke key){
      super(aTitle);
      helpTag = aHelpTag;
      putValue(Action.ACCELERATOR_KEY, key);
   }

   @Override
   public void actionPerformed(ActionEvent aArg0){
      OnlineHelp.openHelp(helpTag);
   }
}
