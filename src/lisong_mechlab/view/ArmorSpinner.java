package lisong_mechlab.view;

import javax.swing.AbstractSpinnerModel;
import javax.swing.SwingUtilities;

import lisong_mechlab.model.MessageXBar;
import lisong_mechlab.model.MessageXBar.Message;
import lisong_mechlab.model.chassi.ArmorSide;
import lisong_mechlab.model.loadout.LoadoutPart;

public class ArmorSpinner extends AbstractSpinnerModel implements MessageXBar.Reader{
   private static final long serialVersionUID = 2130487332299251881L;
   private final LoadoutPart part;
   private final ArmorSide side;

   public ArmorSpinner(LoadoutPart aPart, ArmorSide anArmorSide, MessageXBar anXBar){
      part = aPart;
      side = anArmorSide;
      anXBar.attach(this);
   }
   
   @Override
   public Object getNextValue(){
      if( part.getArmor(side) < part.getArmorMax(side) ){
         return Integer.valueOf(part.getArmor(side) + 1);
      }
      return Integer.valueOf(part.getArmor(side) + 1);
   }

   @Override
   public Object getPreviousValue(){
      if( part.getArmor(side) < 1 )
         return null;
      return Integer.valueOf(part.getArmor(side) - 1);
   }

   @Override
   public Object getValue(){
      return Integer.valueOf(part.getArmor(side));
   }

   @Override
   public void setValue(Object arg0){
      part.setArmor(side, ((Integer)arg0).intValue());
      fireStateChanged();
   }

   @Override
   public void receive(Message aMsg){
      // TODO Be more selective!
      SwingUtilities.invokeLater(new Runnable(){
         @Override
         public void run(){
            fireStateChanged();            
         }
      });      
   }

}
