package lisong_mechlab.view.help;

import java.awt.Desktop;
import java.net.URL;

import javax.swing.JOptionPane;

import lisong_mechlab.view.ProgramInit;

public class OnlineHelp{
   public static void openHelp(String aHelpTag){
      try{
         Desktop.getDesktop().browse((new URL("https://github.com/EmilyBjoerk/lsml/wiki/" + aHelpTag).toURI()));
      }
      catch( Exception e ){
         JOptionPane.showMessageDialog(ProgramInit.lsml(), "Couldn't open help file!\n" + e);
      }
   }
}
