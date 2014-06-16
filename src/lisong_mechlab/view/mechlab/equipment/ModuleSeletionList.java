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
package lisong_mechlab.view.mechlab.equipment;

import javax.swing.DefaultListModel;
import javax.swing.JList;
import javax.swing.JPanel;

import lisong_mechlab.model.item.PilotModule;
import lisong_mechlab.model.item.PilotModuleDB;

/**
 * This {@link JPanel} shows all the available pilot modules on the equipment panel.
 * 
 * @author Emily Björk
 */
public class ModuleSeletionList extends JList<PilotModule>{
   private static final long serialVersionUID = -5162141596342256532L;

   public ModuleSeletionList(){
      DefaultListModel<PilotModule> model = new DefaultListModel<>();
      for(PilotModule pilotModule : PilotModuleDB.lookup(PilotModule.class)){
         model.addElement(pilotModule);   
      }
      
      setModel(model);
            
      setTransferHandler(new ModuleTransferHandler());
      setDragEnabled(true);
   }

}
