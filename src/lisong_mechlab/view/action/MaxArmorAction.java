package lisong_mechlab.view.action;

import java.awt.event.ActionEvent;

import javax.swing.AbstractAction;
import javax.swing.JOptionPane;

import lisong_mechlab.model.loadout.Loadout;
import lisong_mechlab.view.LoadoutFrame;

/**
 * This action sets the armor to max on the given {@link Loadout}.
 * 
 * @author Li Song
 */
public class MaxArmorAction extends AbstractAction{
   private static final long  serialVersionUID = -5939335331941199195L;
   private final Loadout      loadout;
   private final double       ratio;
   private final LoadoutFrame loadoutFrame;

   /**
    * Creates a new {@link MaxArmorAction}. If <code>aRatio</code> is less than or equal to 0, the user is prompted for
    * a ratio.
    * 
    * @param aTitle
    *           The title to give to the action.
    * @param aLoadout
    *           The {@link Loadout} to set armor for.
    * @param aRatio
    *           The ratio between back and front as: <code>front/back</code>
    */
   public MaxArmorAction(String aTitle, LoadoutFrame aLoadout, double aRatio){
      super(aTitle);
      loadoutFrame = aLoadout;
      loadout = aLoadout.getLoadout();
      ratio = aRatio;
   }

   @Override
   public void actionPerformed(ActionEvent aArg0){
      try{
         if( ratio > 0 ){
            loadout.setMaxArmor(ratio);
         }
         else{
            String input = (String)JOptionPane.showInputDialog(loadoutFrame,
                                                               "Please enter the ratio between front and back armor as front:back.Example 3:1",
                                                               "Maximizing armor...", JOptionPane.INFORMATION_MESSAGE, null, null, "3:1");
            String[] s = input.split(":");
            if( s.length == 2 ){
               double front, back;
               try{
                  front = Double.parseDouble(s[0]);
                  back = Double.parseDouble(s[1]);
               }
               catch( Exception e ){
                  JOptionPane.showMessageDialog(loadoutFrame, "Error parsing ratio! Loadout was not changed!");
                  return;
               }
               loadout.setMaxArmor(front / back);
            }
            else
               JOptionPane.showMessageDialog(loadoutFrame, "Error parsing ratio! Loadout was not changed!");
         }
      }
      catch( IllegalArgumentException e ){
         JOptionPane.showMessageDialog(loadoutFrame, "Unable to set max armor! Error: " + e.getMessage());
      }
      catch( Throwable e ){
         JOptionPane.showMessageDialog(loadoutFrame, "Unexpected exception! Error: " + e);
      }
   }
}
