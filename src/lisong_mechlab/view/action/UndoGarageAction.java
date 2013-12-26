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
package lisong_mechlab.view.action;

import java.awt.event.ActionEvent;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.KeyStroke;
import javax.swing.SwingUtilities;

import lisong_mechlab.model.loadout.MechGarage;
import lisong_mechlab.util.MessageXBar;
import lisong_mechlab.util.MessageXBar.Message;
import lisong_mechlab.util.MessageXBar.Reader;
import lisong_mechlab.view.ProgramInit;

/**
 * This action will undo a change to the garage.
 * 
 * @author Emily Björk
 */
public class UndoGarageAction extends AbstractAction implements Reader{
   private static final long   serialVersionUID = 665074705972425989L;
   private static final String SHORTCUT_STROKE  = "control G";

   public UndoGarageAction(MessageXBar anXBar){
      putValue(Action.ACCELERATOR_KEY, KeyStroke.getKeyStroke(SHORTCUT_STROKE));
      anXBar.attach(this);
      setEnabled(false); // Initially
   }

   @Override
   public Object getValue(String key){
      if( key == Action.NAME ){
         if( isEnabled() ){
            return ProgramInit.lsml().undoStack.latestGarage().describe();
         }
         return "Undo Garage";
      }
      return super.getValue(key);
   }

   @Override
   public void actionPerformed(ActionEvent aArg0){
      ProgramInit.lsml().undoStack.undoAction(ProgramInit.lsml().undoStack.latestGarage());
   }

   @Override
   public void receive(final Message aMsg){
      SwingUtilities.invokeLater(new Runnable(){

         @Override
         public void run(){
            if( aMsg instanceof MechGarage.Message ){
               if( ProgramInit.lsml() == null || ProgramInit.lsml().undoStack == null )
                  setEnabled(false);
               else
                  setEnabled(null != ProgramInit.lsml().undoStack.latestGarage());
               firePropertyChange(NAME, "", getValue(NAME));
            }
         }
      });

   }

}
