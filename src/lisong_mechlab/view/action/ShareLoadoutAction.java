package lisong_mechlab.view.action;

import java.awt.HeadlessException;
import java.awt.event.ActionEvent;

import javax.swing.AbstractAction;
import javax.swing.JOptionPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;

import lisong_mechlab.model.loadout.Loadout;
import lisong_mechlab.util.EncodingException;
import lisong_mechlab.view.ProgramInit;

/**
 * This action opens up a share frame where the user can copy the link to the build.
 * 
 * @author Li Song
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
      try{
         String text = ProgramInit.lsml().loadoutCoder.encode(loadout);
         JTextArea textArea = new JTextArea(text);
         textArea.setColumns(50);
         textArea.setLineWrap(true);
         textArea.setWrapStyleWord(true);
         textArea.setSize(textArea.getPreferredSize().width, 1);
         JOptionPane.showMessageDialog(null, textArea, "Link to share this loadout!", JOptionPane.PLAIN_MESSAGE);
      }
      catch( HeadlessException e ){
         // TODO Auto-generated catch block
         e.printStackTrace();
      }
      catch( EncodingException e ){
         JOptionPane.showMessageDialog(null, "Unable to encode loadout!" + e);
      }
   }

}
