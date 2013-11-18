/*
 * @formatter:off
 * Li Song Mech Lab - A 'mech building tool for PGI's MechWarrior: Online.
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
package lisong_mechlab.view;

import javax.swing.JOptionPane;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.SwingUtilities;

import lisong_mechlab.model.loadout.Loadout;
import lisong_mechlab.util.DecodingException;
import lisong_mechlab.util.MessageXBar;

/**
 * This class shows the 'mech lab panel.
 * 
 * @author Emily Björk
 */
public class MechLabPane extends JSplitPane{
   private static final long    serialVersionUID = 1079910953509846928L;
   private final LoadoutDesktop desktop;
   private final EquipmentPane  equipmentPane;
   private final JScrollPane    jScrollPane;
   private final MessageXBar    xBar;

   MechLabPane(MessageXBar aXBar){
      super(JSplitPane.HORIZONTAL_SPLIT, true);
      xBar = aXBar;
      desktop = new LoadoutDesktop(xBar);
      equipmentPane = new EquipmentPane(desktop, xBar);
      jScrollPane = new JScrollPane(equipmentPane);

      setLeftComponent(jScrollPane);
      setRightComponent(desktop);

      setDividerLocation(180);
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
