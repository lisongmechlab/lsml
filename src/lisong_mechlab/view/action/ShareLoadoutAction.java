package lisong_mechlab.view.action;

import java.awt.Component;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.HeadlessException;
import java.awt.event.ActionEvent;

import javax.swing.AbstractAction;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTextArea;

import lisong_mechlab.util.EncodingException;
import lisong_mechlab.view.LoadoutFrame;
import lisong_mechlab.view.ProgramInit;

/**
 * This action opens up a share frame where the user can copy the link to the build.
 * 
 * @author Emily Björk
 */
public class ShareLoadoutAction extends AbstractAction{
   private static final long  serialVersionUID = 6535485629587481198L;
   private final LoadoutFrame loadoutFrame;

   public ShareLoadoutAction(LoadoutFrame aLoadoutFrame){
      super("Share!");
      loadoutFrame = aLoadoutFrame;
   }

   @Override
   public void actionPerformed(ActionEvent aArg0){
      try{
         String trampolineLink = ProgramInit.lsml().loadoutCoder.encodeHttpTrampoline(loadoutFrame.getLoadout());
         String lsmlLink = ProgramInit.lsml().loadoutCoder.encodeLSML(loadoutFrame.getLoadout());
         JTextArea textArea = new JTextArea(lsmlLink);
         textArea.setAlignmentX(Component.LEFT_ALIGNMENT);
         textArea.setEditable(false);
         textArea.setColumns(80);
         textArea.setLineWrap(true);
         textArea.setWrapStyleWord(true);
         textArea.setSize(textArea.getPreferredSize());

         JTextArea textArea2 = new JTextArea(trampolineLink);
         textArea2.setAlignmentX(Component.LEFT_ALIGNMENT);
         textArea2.setEditable(false);
         textArea2.setColumns(80);
         textArea2.setLineWrap(true);
         textArea2.setWrapStyleWord(true);
         textArea2.setSize(textArea2.getPreferredSize());

         JLabel trampolineLabel = new JLabel("HTTP Trampoline link:");
         Font labelFont = trampolineLabel.getFont().deriveFont(Font.BOLD);
         trampolineLabel.setToolTipText("Use this for forums and programs that doesn't support LSML:// protocol links (most forums/software).");
         trampolineLabel.setFont(labelFont);
         
         JLabel lsmlLabel = new JLabel("LSML Link:");
         lsmlLabel.setToolTipText("Use this if it is supported by your forum/software, for example credditmwo.com.");
         lsmlLabel.setFont(labelFont);

         JPanel p = new JPanel();
         p.setLayout(new BoxLayout(p, BoxLayout.PAGE_AXIS));
         p.add(trampolineLabel);
         p.add(textArea2);
         p.add(Box.createRigidArea(new Dimension(0, 10)));
         p.add(lsmlLabel);
         p.add(textArea);

         JOptionPane.showMessageDialog(ProgramInit.lsml(), p, "Link to share this loadout!", JOptionPane.PLAIN_MESSAGE);
      }
      catch( HeadlessException e ){
         // TODO Auto-generated catch block
         e.printStackTrace();
      }
      catch( EncodingException e ){
         JOptionPane.showMessageDialog(ProgramInit.lsml(), "Unable to encode loadout!" + e);
      }
   }

}
