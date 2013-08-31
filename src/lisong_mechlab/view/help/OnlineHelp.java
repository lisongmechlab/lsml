package lisong_mechlab.view.help;

import java.awt.Desktop;
import java.net.URL;

import javax.swing.JOptionPane;

public class OnlineHelp{
   public static void openHelp(String aHelpTag){
      try{
         Desktop.getDesktop().browse((new URL("https://github.com/EmilyBjoerk/lsml/wiki/" + aHelpTag).toURI()));
      }
      catch( Exception e ){
         JOptionPane.showMessageDialog(null, "Couldn't open help file!\n" + e);
      }
   }
}
