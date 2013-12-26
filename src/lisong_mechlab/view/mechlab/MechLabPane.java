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

import javax.swing.JOptionPane;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTabbedPane;
import javax.swing.SwingUtilities;

import lisong_mechlab.model.loadout.Loadout;
import lisong_mechlab.model.loadout.UndoStack;
import lisong_mechlab.util.DecodingException;
import lisong_mechlab.util.MessageXBar;
import lisong_mechlab.view.ProgramInit;
import lisong_mechlab.view.mechlab.equipment.EquipmentPanel;
import lisong_mechlab.view.mechlab.equipment.GarageTree;

/**
 * This class shows the 'mech lab pane in the main tabbed pane.
 * 
 * @author Emily Björk
 */
public class MechLabPane extends JSplitPane{
   private static final long    serialVersionUID = 1079910953509846928L;
   private final LoadoutDesktop desktop;
   private final GarageTree     equipmentPane;
   private final JScrollPane    jScrollPane;
   private final MessageXBar    xBar;

   public MechLabPane(MessageXBar anXBar, UndoStack anUndoStack){
      super(JSplitPane.HORIZONTAL_SPLIT, true);
      xBar = anXBar;
      desktop = new LoadoutDesktop(xBar, anUndoStack);
      equipmentPane = new GarageTree(desktop, xBar, anUndoStack);
      EquipmentPanel panel = new EquipmentPanel(desktop, xBar);
      jScrollPane = new JScrollPane(equipmentPane);

      JTabbedPane tabbedPane = new JTabbedPane();
      tabbedPane.addTab("Equipment", panel);
      tabbedPane.addTab("Garage", jScrollPane);

      setLeftComponent(tabbedPane);

      // setLeftComponent(jScrollPane);
      setRightComponent(desktop);

      setDividerLocation(panel.getMinimumSize().width);
   }

   /**
    * Will open the given {@link Loadout} into the desktop pane by creating a new {@link LoadoutFrame}.
    * 
    * @param aLoadout
    *           The {@link Loadout} to create the frame for.
    */
   public void openLoadout(Loadout aLoadout){
      desktop.openLoadout(aLoadout);
   }

   /**
    * @return The currently selected loadout.
    */
   public Loadout getCurrentLoadout(){
      if( null != desktop.getSelectedFrame() )
         return ((LoadoutFrame)desktop.getSelectedFrame()).getLoadout();
      return null;
   }

   /**
    * Will open the given {@link Loadout} into the desktop pane by creating a new {@link LoadoutFrame}.
    * 
    * @param aLoadout
    *           The {@link Loadout} to create the frame for.
    */
   public void openLoadout(String aLSMLUrl){
      assert (SwingUtilities.isEventDispatchThread());
      try{
         openLoadout(ProgramInit.lsml().loadoutCoder.parse(aLSMLUrl));
      }
      catch( DecodingException e ){
         JOptionPane.showMessageDialog(ProgramInit.lsml(), "Unable to import loadout from \"" + aLSMLUrl + "\"! Error:" + e);
      }
   }

   /**
    * @return <code>true</code> if the pane was closed, <code>false</code> otherwise.
    */
   public boolean close(){
      return desktop.closeAll();
   }

}
