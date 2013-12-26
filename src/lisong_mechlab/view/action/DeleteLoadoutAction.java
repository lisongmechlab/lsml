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

import java.awt.Component;
import java.awt.event.ActionEvent;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.JOptionPane;
import javax.swing.KeyStroke;

import lisong_mechlab.model.loadout.Loadout;
import lisong_mechlab.model.loadout.MechGarage;
import lisong_mechlab.view.ProgramInit;
import lisong_mechlab.view.mechlab.LoadoutFrame;

public class DeleteLoadoutAction extends AbstractAction{
   private static final long  serialVersionUID = -4813215864397617783L;
   private final Loadout      loadout;
   private final MechGarage   garage;
   private final LoadoutFrame loadoutFrame;

   public DeleteLoadoutAction(MechGarage aGarage, LoadoutFrame aLoadoutFrame, KeyStroke key){
      super("Delete loadout");
      loadoutFrame = aLoadoutFrame;
      loadout = aLoadoutFrame.getLoadout();
      garage = aGarage;
      putValue(Action.ACCELERATOR_KEY, key);
   }

   public DeleteLoadoutAction(MechGarage aGarage, Loadout aLoadout, KeyStroke key){
      super("Delete loadout");
      loadoutFrame = null;
      loadout = aLoadout;
      garage = aGarage;
      putValue(Action.ACCELERATOR_KEY, key);
   }

   @Override
   public void actionPerformed(ActionEvent aE){
      if( garage.getMechs().contains(loadout) ){
         Component source = loadoutFrame == null ? ProgramInit.lsml() : loadoutFrame;

         int result = JOptionPane.showConfirmDialog(source, "Are you certain you want to delete the loadout: " + loadout.getName() + "?",
                                                    "Confirm operation", JOptionPane.YES_NO_OPTION);
         if( JOptionPane.YES_OPTION == result ){
            try{
               garage.remove(loadout, true);
            }
            catch( RuntimeException e ){
               JOptionPane.showMessageDialog(source,
                                             "An error occured!\n"
                                                   + "Please report an issue at https://github.com/EmilyBjoerk/lsml/issues and copy paste the following this message:\n"
                                                   + e.getMessage() + "\nStack trace:\n" + e.getStackTrace());
            }
         }
      }
   }
}
