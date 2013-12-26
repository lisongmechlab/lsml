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

import lisong_mechlab.view.ProgramInit;

public class ImportMechAction extends AbstractAction{
   private static final long serialVersionUID = -9019953619423428349L;

   public ImportMechAction(String aTitle, KeyStroke key){
      super(aTitle);
      putValue(Action.ACCELERATOR_KEY, key);
   }

   @Override
   public void actionPerformed(ActionEvent aArg0){
      String input = JOptionPane.showInputDialog(ProgramInit.lsml(), "Paste the lsml:// link:", "Import mech...", JOptionPane.PLAIN_MESSAGE);
      if( null == input )
         return;
      ProgramInit.lsml().mechLabPane.openLoadout(input);
   }

}
