/*
 * @formatter:off
 * Li Song Mechlab - A 'mech building tool for PGI's MechWarrior: Online.
 * Copyright (C) 2013  Li Song
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
import java.util.Collection;

import javax.swing.AbstractAction;
import javax.swing.JList;
import javax.swing.JOptionPane;

import lisong_mechlab.model.chassi.ChassiDB;
import lisong_mechlab.model.chassi.Chassis;
import lisong_mechlab.model.loadout.LoadStockOperation;
import lisong_mechlab.model.loadout.Loadout;
import lisong_mechlab.util.MessageXBar;
import lisong_mechlab.util.OperationStack;
import lisong_mechlab.view.ProgramInit;

/**
 * This action loads a stock loadout. If the chassis has multiple variations the user is prompted for which stock
 * loadout to load.
 * 
 * @author Li Song
 */
public class LoadStockAction extends AbstractAction{
   private static final long    serialVersionUID = 4350731510583942480L;
   private final Loadout        loadout;
   private final OperationStack stack;
   private final MessageXBar    xBar;
   private final Component      component;

   /**
    * Creates a new {@link LoadStockAction}.
    * 
    * @param aLoadout
    *           The {@link Loadout} to load stock for.
    * @param aStack
    *           The {@link OperationStack} stack that shall be used for undo information.
    * @param aXBar
    *           The {@link MessageXBar} that shall be used for signaling changes to the {@link Loadout}.
    * @param aComponent
    *           The {@link Component} on which any dialogs will be centered.
    */
   public LoadStockAction(Loadout aLoadout, OperationStack aStack, MessageXBar aXBar, Component aComponent){
      super(getActionName(aLoadout.getChassi()));
      loadout = aLoadout;
      stack = aStack;
      xBar = aXBar;
      component = aComponent;
   }

   @Override
   public void actionPerformed(ActionEvent aArg0){
      final Collection<Chassis> variations = ChassiDB.lookupVariations(loadout.getChassi());

      try{
         if( variations.size() == 1 ){
            stack.pushAndApply(new LoadStockOperation(loadout.getChassi(), loadout, xBar));
         }
         else{
            JList<Chassis> list = new JList<>(variations.toArray(new Chassis[variations.size()]));
            JOptionPane.showConfirmDialog(component, list, "Which stock loadout?", JOptionPane.OK_CANCEL_OPTION);
            if( list.getSelectedValue() != null ){
               stack.pushAndApply(new LoadStockOperation(list.getSelectedValue(), loadout, xBar));
            }
         }
      }
      catch( Exception e ){
         JOptionPane.showMessageDialog(ProgramInit.lsml(), "Couldn't load stock loadout! Error: " + e.getMessage());
      }
   }

   private static String getActionName(Chassis aChassis){
      if( ChassiDB.lookupVariations(aChassis).size() > 1 ){
         return "Load stock...";
      }
      return "Load stock";
   }
}
