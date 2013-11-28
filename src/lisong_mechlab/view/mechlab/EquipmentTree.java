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
package lisong_mechlab.view.mechlab;

import java.awt.Component;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.text.DecimalFormat;

import javax.swing.JMenuItem;
import javax.swing.JPopupMenu;
import javax.swing.JTree;
import javax.swing.KeyStroke;
import javax.swing.SwingUtilities;
import javax.swing.ToolTipManager;
import javax.swing.event.InternalFrameEvent;
import javax.swing.event.InternalFrameListener;
import javax.swing.tree.DefaultTreeCellRenderer;
import javax.swing.tree.TreePath;

import lisong_mechlab.model.chassi.Chassi;
import lisong_mechlab.model.chassi.HardpointType;
import lisong_mechlab.model.item.HeatSource;
import lisong_mechlab.model.item.Item;
import lisong_mechlab.model.item.ItemDB;
import lisong_mechlab.model.item.Weapon;
import lisong_mechlab.model.loadout.Loadout;
import lisong_mechlab.model.loadout.Upgrades;
import lisong_mechlab.util.MessageXBar;
import lisong_mechlab.view.ItemTransferHandler;
import lisong_mechlab.view.ProgramInit;
import lisong_mechlab.view.action.CloneLoadoutAction;
import lisong_mechlab.view.action.DeleteLoadoutAction;
import lisong_mechlab.view.action.RenameLoadoutAction;
import lisong_mechlab.view.mechlab.equipment.EquipmentTreeModel;
import lisong_mechlab.view.render.StyleManager;

public class EquipmentTree extends JTree{
   private static final long serialVersionUID = -8856874024057864775L;
   EquipmentTreeModel        model            = null;
   private final MessageXBar xBar;
   private Loadout           loadout;

   private class Renderer extends DefaultTreeCellRenderer implements InternalFrameListener{
      private static final long serialVersionUID = 5198340883942696537L;

      @Override
      public Component getTreeCellRendererComponent(JTree aTree, Object aValue, boolean aSel, boolean anExpanded, boolean aLeaf, int aRow,
                                                    boolean aHasFocus){
         super.getTreeCellRendererComponent(aTree, aValue, aSel, anExpanded, aLeaf, aRow, aHasFocus);

         if( aValue instanceof String ){
            setOpaque(true);
            Item item = ItemDB.lookup((String)aValue);
            if( loadout != null ){
               if( !loadout.isEquippable(item) )
                  StyleManager.colourInvalid(this);
               else
                  StyleManager.colour(this, item);
            }
            else{
               StyleManager.colour(this, item);
            }
         }
         else
            setOpaque(false);

         return this;
      }

      @Override
      public void internalFrameActivated(InternalFrameEvent aArg0){
         LoadoutFrame frame = (LoadoutFrame)aArg0.getInternalFrame();
         loadout = frame.getLoadout();
      }

      @Override
      public void internalFrameDeactivated(InternalFrameEvent aE){
         loadout = null;
      }

      @Override
      public void internalFrameIconified(InternalFrameEvent aE){
         loadout = null;
      }

      @Override
      public void internalFrameDeiconified(InternalFrameEvent aArg0){
         LoadoutFrame frame = (LoadoutFrame)aArg0.getInternalFrame();
         loadout = frame.getLoadout();
      }

      @Override
      public void internalFrameClosing(InternalFrameEvent aE){
         // No-Op
      }

      @Override
      public void internalFrameClosed(InternalFrameEvent aArg0){
         // No-Op -- This may be received after the new frame is activated
         // And is the cause for issue #64
      }

      @Override
      public void internalFrameOpened(InternalFrameEvent aArg0){
         // No-Op
      }
   }

   public EquipmentTree(final LoadoutDesktop aLoadoutDesktop, MessageXBar crossBar){
      model = new EquipmentTreeModel(crossBar);
      xBar = crossBar;

      ToolTipManager.sharedInstance().registerComponent(this);
      Renderer renderer = new Renderer();
      setCellRenderer(renderer);
      setModel(model);
      setDragEnabled(true);
      setRootVisible(false);
      setShowsRootHandles(true);
      aLoadoutDesktop.addInternalFrameListener(model);
      aLoadoutDesktop.addInternalFrameListener(renderer);
      setTransferHandler(new ItemTransferHandler());

      addMouseListener(new MouseAdapter(){
         @Override
         public void mousePressed(MouseEvent e){
            if( SwingUtilities.isRightMouseButton(e) ){
               Object clicked = getClickedObject(e);
               if( clicked instanceof Loadout ){
                  EquipmentTree.this.setSelectionPath(getClosestPathForLocation(e.getX(), e.getY()));

                  Loadout clickedLoadout = (Loadout)clicked;
                  JPopupMenu menu = new JPopupMenu();
                  JMenuItem label = new JMenuItem(clickedLoadout.getName());
                  label.setEnabled(false);
                  menu.add(label);
                  menu.add(new JMenuItem(new RenameLoadoutAction(clickedLoadout, KeyStroke.getKeyStroke("R"))));
                  menu.add(new JMenuItem(new DeleteLoadoutAction(ProgramInit.lsml().getGarage(), clickedLoadout, KeyStroke.getKeyStroke("D"))));
                  menu.add(new JMenuItem(new CloneLoadoutAction("Clone", clickedLoadout, KeyStroke.getKeyStroke("C"))));
                  menu.show(EquipmentTree.this, e.getX(), e.getY());
               }
            }
            if( SwingUtilities.isLeftMouseButton(e) && e.getClickCount() >= 2 ){
               Object clicked = getClickedObject(e);
               int clicks = e.getClickCount() / 2;
               if( clicked instanceof Chassi ){
                  Chassi chassi = (Chassi)clicked;
                  Loadout clickedLoadout = new Loadout(chassi, xBar);
                  aLoadoutDesktop.openLoadout(clickedLoadout);
               }
               else if( clicked instanceof Loadout ){
                  aLoadoutDesktop.openLoadout((Loadout)clicked);
               }
               else if( clicked instanceof Item ){
                  if( loadout != null ){
                     while( clicks-- > 0 )
                        loadout.addItem((Item)clicked);
                  }
               }
               else if( clicked instanceof String ){
                  if( loadout != null ){
                     while( clicks-- > 0 )
                        loadout.addItem((String)clicked);
                  }
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
         if( leaf instanceof String ){
            sb.append("<html>");
            generateItemTooltip(sb, ItemDB.lookup((String)leaf));
            sb.append("</html>");
            return sb.toString();
         }
         else if( leaf instanceof Chassi ){
            Chassi chassi = (Chassi)leaf;
            sb.append("<html>");
            sb.append("Max Tons: ").append(chassi.getMassMax()).append(" Engine: ").append(chassi.getEngineMin()).append(" - ")
              .append(chassi.getEngineMax()).append("<br>");
            sb.append("Max Jump Jets: ").append(chassi.getMaxJumpJets()).append(" ECM: ").append(chassi.isEcmCapable() ? "Yes" : "No").append("<br>");
            sb.append("Ballistics: ").append(chassi.getHardpointsCount(HardpointType.BALLISTIC)).append(" Energy: ")
              .append(chassi.getHardpointsCount(HardpointType.ENERGY)).append(" Missile: ").append(chassi.getHardpointsCount(HardpointType.MISSILE))
              .append(" AMS: ").append(chassi.getHardpointsCount(HardpointType.AMS)).append("<br>");
            sb.append("</html>");
            return sb.toString();
         }
      }
      return null;
   }

   /**
    * Formats a tooltip for the current item into the given {@link StringBuilder}.
    * 
    * @param sb
    * @param item
    */
   private void generateItemTooltip(StringBuilder sb, Item item){
      Upgrades upgrades = (loadout != null) ? (loadout.getUpgrades()) : (null);
      DecimalFormat df = new DecimalFormat("#####.#");

      sb.append(item.getDescription()).append("<br>");
      sb.append("Slots: ").append(item.getNumCriticalSlots(upgrades)).append(" Tons: ").append(df.format(item.getMass(upgrades))).append("<br>");
      if( item instanceof HeatSource ){
         if( item instanceof Weapon ){
            Weapon weapon = (Weapon)item;
            sb.append("Damage: ").append(df.format(weapon.getDamagePerShot())).append(" Cooldown: ").append(df.format(weapon.getSecondsPerShot()))
              .append("<br>");
            sb.append("Optimal: ").append(df.format(weapon.getRangeMin())).append(" - ").append(df.format(weapon.getRangeLong())).append(" / ")
              .append(df.format(weapon.getRangeMax())).append("<br>");
            sb.append("DPS: ").append(df.format(weapon.getStat("d/s", upgrades))).append(" DPH: ").append(df.format(weapon.getStat("d/h", upgrades)))
              .append(" HPS: ").append(df.format(weapon.getStat("h/s", upgrades))).append("<br>");
         }
         sb.append("Heat: ").append(df.format(((HeatSource)item).getHeat())).append("<br>");
      }
   }

   public Loadout getCurrentLoadout(){
      return loadout;
   }
}
