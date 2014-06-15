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
package lisong_mechlab.view.mechlab.equipment;

import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

import javax.swing.JMenuItem;
import javax.swing.JPopupMenu;
import javax.swing.JTextField;
import javax.swing.JTree;
import javax.swing.KeyStroke;
import javax.swing.SwingUtilities;
import javax.swing.ToolTipManager;
import javax.swing.tree.TreePath;

import lisong_mechlab.model.chassi.ChassisBase;
import lisong_mechlab.model.chassi.ChassisOmniMech;
import lisong_mechlab.model.chassi.ChassisStandard;
import lisong_mechlab.model.chassi.HardPointType;
import lisong_mechlab.model.loadout.LoadoutBase;
import lisong_mechlab.model.loadout.LoadoutOmniMech;
import lisong_mechlab.model.loadout.LoadoutStandard;
import lisong_mechlab.model.loadout.component.ComponentBuilder;
import lisong_mechlab.util.MessageXBar;
import lisong_mechlab.view.ItemTransferHandler;
import lisong_mechlab.view.ProgramInit;
import lisong_mechlab.view.action.CloneLoadoutAction;
import lisong_mechlab.view.action.DeleteLoadoutAction;
import lisong_mechlab.view.action.RenameLoadoutAction;
import lisong_mechlab.view.mechlab.LoadoutDesktop;
import lisong_mechlab.view.preferences.Preferences;

public class GarageTree extends JTree{
   private static final long serialVersionUID = -8856874024057864775L;
   GarageTreeModel           model            = null;
   private final MessageXBar xBar;

   public GarageTree(final LoadoutDesktop aLoadoutDesktop, MessageXBar anXBar, JTextField aFilterBar, Preferences aPreferences){
      model = new GarageTreeModel(anXBar, aFilterBar, this, aPreferences);
      xBar = anXBar;

      ToolTipManager.sharedInstance().registerComponent(this);
      setModel(model);
      setDragEnabled(true);
      setRootVisible(false);
      setShowsRootHandles(true);
      aLoadoutDesktop.addInternalFrameListener(model);
      setTransferHandler(new ItemTransferHandler());

      for(int i = 0; i < model.getChildCount(model.getRoot()); ++i){
         TreePath path = new TreePath(new Object[] {model.getRoot(), model.getChild(model.getRoot(), i)});
         expandPath(path);
      }

      addMouseListener(new MouseAdapter(){
         @Override
         public void mousePressed(MouseEvent e){
            if( SwingUtilities.isRightMouseButton(e) ){
               Object clicked = getClickedObject(e);
               if( clicked instanceof LoadoutBase<?> ){
                  GarageTree.this.setSelectionPath(getClosestPathForLocation(e.getX(), e.getY()));

                  LoadoutBase<?> clickedLoadout = (LoadoutBase<?>)clicked;
                  JPopupMenu menu = new JPopupMenu();
                  JMenuItem label = new JMenuItem(clickedLoadout.getName());
                  label.setEnabled(false);
                  menu.add(label);
                  menu.add(new JMenuItem(new RenameLoadoutAction(clickedLoadout, xBar, null)));
                  menu.add(new JMenuItem(new DeleteLoadoutAction(xBar, ProgramInit.lsml().getGarage(), clickedLoadout)));
                  menu.add(new JMenuItem(new CloneLoadoutAction("Clone", clickedLoadout, KeyStroke.getKeyStroke("C"))));
                  menu.show(GarageTree.this, e.getX(), e.getY());
               }
            }
            if( SwingUtilities.isLeftMouseButton(e) && e.getClickCount() >= 2 ){
               Object clicked = getClickedObject(e);
               if( clicked instanceof ChassisBase ){
                  LoadoutBase<?> loadout = null;
                  if( clicked instanceof ChassisStandard ){
                     ChassisStandard chassi = (ChassisStandard)clicked;
                     loadout = new LoadoutStandard(chassi, xBar);

                  }
                  else if( clicked instanceof ChassisOmniMech ){
                     ChassisOmniMech chassi = (ChassisOmniMech)clicked;
                     loadout = new LoadoutOmniMech(ComponentBuilder.getOmniPodFactory(), chassi, xBar);
                  }
                  else{
                     throw new RuntimeException("Unknown chassis type!");
                  }
                  aLoadoutDesktop.openLoadout(loadout);
               }
               else if( clicked instanceof LoadoutBase<?> ){
                  aLoadoutDesktop.openLoadout((LoadoutBase<?>)clicked);
               }
            }
         }
      });
   }

   private Object getClickedObject(MouseEvent e){
      TreePath path = getPathForLocation(e.getX(), e.getY());
      if( path == null )
         return null;
      return path.getLastPathComponent();
   }

   @Override
   public String getToolTipText(MouseEvent event){
      TreePath mouseover = getPathForLocation(event.getX(), event.getY());
      if( mouseover != null ){
         StringBuilder sb = new StringBuilder(100);
         Object leaf = mouseover.getLastPathComponent();
         if( leaf instanceof ChassisStandard ){
            ChassisStandard chassi = (ChassisStandard)leaf;
            sb.append("<html>");
            sb.append("Max Tons: ").append(chassi.getMassMax()).append(" Engine: ").append(chassi.getEngineMin()).append(" - ")
              .append(chassi.getEngineMax()).append("<br>");
            sb.append("Max Jump Jets: ").append(chassi.getJumpJetsMax()).append(" ECM: ")
              .append(chassi.getHardPointsCount(HardPointType.ECM) > 0 ? "Yes" : "No").append("<br>");
            sb.append("Ballistics: ").append(chassi.getHardPointsCount(HardPointType.BALLISTIC)).append(" Energy: ")
              .append(chassi.getHardPointsCount(HardPointType.ENERGY)).append(" Missile: ").append(chassi.getHardPointsCount(HardPointType.MISSILE))
              .append(" AMS: ").append(chassi.getHardPointsCount(HardPointType.AMS)).append("<br>");
            sb.append("</html>");
            return sb.toString();
         }
      }
      return null;
   }
}
