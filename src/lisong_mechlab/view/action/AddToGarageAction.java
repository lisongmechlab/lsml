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
import javax.swing.JOptionPane;
import javax.swing.KeyStroke;

import lisong_mechlab.model.loadout.Loadout;
import lisong_mechlab.view.ProgramInit;

/**
 * This action will add the given loadout to the garage.
 * 
 * @author Emily Björk
 */
public class AddToGarageAction extends AbstractAction{
   private static final long   serialVersionUID = -1720149730950545006L;
   private static final String SHORTCUT_STROKE  = "control S";
   private final Loadout       loadout;

   public AddToGarageAction(Loadout aLoadout){
      super("Add to garage");
      loadout = aLoadout;
      setEnabled(!ProgramInit.lsml().getGarage().getMechs().contains(aLoadout));
      putValue(Action.ACCELERATOR_KEY, KeyStroke.getKeyStroke(SHORTCUT_STROKE));
   }

   @Override
   public void actionPerformed(ActionEvent aArg0){
      try{
         ProgramInit.lsml().getGarage().add(loadout, true);
         setEnabled(false);
      }
      catch( IllegalArgumentException e ){
         JOptionPane.showMessageDialog(ProgramInit.lsml(), "Couldn't add to garage! Error: " + e.getMessage());
      }
   }
}
