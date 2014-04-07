/*
 * @formatter:off
 * Li Song Mechlab - A 'mech building tool for PGI's MechWarrior: Online.
 * Copyright (C) 2013  Emily Björk
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */  
//@formatter:on
package lisong_mechlab.view.mechlab;

import java.awt.Toolkit;

import javax.swing.JCheckBox;
import javax.swing.SpinnerNumberModel;
import javax.swing.SwingUtilities;

import lisong_mechlab.model.chassi.ArmorSide;
import lisong_mechlab.model.chassi.Part;
import lisong_mechlab.model.loadout.part.LoadoutPart;
import lisong_mechlab.model.loadout.part.LoadoutPart.Message.Type;
import lisong_mechlab.model.loadout.part.SetArmorOperation;
import lisong_mechlab.util.MessageXBar;
import lisong_mechlab.util.MessageXBar.Message;
import lisong_mechlab.util.OperationStack;
import lisong_mechlab.util.OperationStack.Operation;

public class ArmorSpinner extends SpinnerNumberModel implements MessageXBar.Reader{
   private static final long    serialVersionUID = 2130487332299251881L;
   private final LoadoutPart    part;
   private final ArmorSide      side;
   private final JCheckBox      symmetric;
   private final OperationStack opStack;
   private final MessageXBar    xBar;

   public ArmorSpinner(LoadoutPart aPart, ArmorSide anArmorSide, MessageXBar anXBar, JCheckBox aSymmetric, OperationStack anOperationStack){
      part = aPart;
      side = anArmorSide;
      symmetric = aSymmetric;
      xBar = anXBar;
      xBar.attach(this);
      opStack = anOperationStack;
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
      if( getValue().equals(arg0) )
         return;

      try{
         int armor = ((Integer)arg0).intValue();
         opStack.pushAndApply(new SetArmorOperation(xBar, part, side, armor, true));

         Part otherSide = part.getInternalPart().getType().oppositeSide();
         if( symmetric.isSelected() && otherSide != null ){
            Operation op2 = new SetArmorOperation(xBar, part.getLoadout().getPart(otherSide), side, armor, true);
            opStack.pushAndApply(op2);
         }
         fireStateChanged();
      }
      catch( IllegalArgumentException exception ){
         // TODO: Handle failed case better!
         Toolkit.getDefaultToolkit().beep();
      }
   }

   @Override
   public void receive(Message aMsg){
      if( aMsg.isForMe(part.getLoadout()) && aMsg instanceof LoadoutPart.Message ){
         LoadoutPart.Message message = (LoadoutPart.Message)aMsg;
         if( message.part != part )
            return;
         if( message.type == Type.ArmorChanged ){
            SwingUtilities.invokeLater(new Runnable(){
               @Override
               public void run(){
                  fireStateChanged();
               }
            });
         }
      }
   }

}
