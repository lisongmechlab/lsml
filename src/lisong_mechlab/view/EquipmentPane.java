package lisong_mechlab.view;

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

import lisong_mechlab.model.MessageXBar;
import lisong_mechlab.model.chassi.Chassi;
import lisong_mechlab.model.chassi.HardpointType;
import lisong_mechlab.model.item.HeatSource;
import lisong_mechlab.model.item.Item;
import lisong_mechlab.model.item.ItemDB;
import lisong_mechlab.model.item.Weapon;
import lisong_mechlab.model.loadout.Loadout;
import lisong_mechlab.view.action.DeleteLoadoutAction;
import lisong_mechlab.view.action.RenameLoadoutAction;
import lisong_mechlab.view.equipment.EquipmentTreeModel;

public class EquipmentPane extends JTree{
   private static final long serialVersionUID = -8856874024057864775L;
   EquipmentTreeModel        treeModel        = null;
   private final MessageXBar xBar;

   private static class Renderer extends DefaultTreeCellRenderer implements InternalFrameListener{
      private static final long serialVersionUID = 5198340883942696537L;
      private Loadout           loadout;

      @Override
      public Component getTreeCellRendererComponent(JTree tree, Object value, boolean sel, boolean expanded, boolean leaf, int row, boolean hasFocus){
         super.getTreeCellRendererComponent(tree, value, sel, expanded, leaf, row, hasFocus);

         if( value instanceof String ){
            setOpaque(true);
            Item item = ItemDB.lookup((String)value);
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

   public EquipmentPane(final LoadoutDesktop aLoadoutDesktop, final LSML aLsml, MessageXBar crossBar) throws Exception{
      treeModel = new EquipmentTreeModel(aLsml, crossBar);
      xBar = crossBar;

      ToolTipManager.sharedInstance().registerComponent(this);
      Renderer renderer = new Renderer();
      setCellRenderer(renderer);
      setModel(treeModel);
      setDragEnabled(true);
      setRootVisible(false);
      setShowsRootHandles(true);
      aLoadoutDesktop.addInternalFrameListener(treeModel);
      aLoadoutDesktop.addInternalFrameListener(renderer);
      setTransferHandler(new ItemTransferHandler());

      addMouseListener(new MouseAdapter(){
         @Override
         public void mousePressed(MouseEvent e){
            if( SwingUtilities.isRightMouseButton(e) ){
               Object clicked = getClickedObject(e);
               if( clicked instanceof Loadout ){
                  EquipmentPane.this.setSelectionPath(getClosestPathForLocation(e.getX(), e.getY()));

                  Loadout loadout = (Loadout)clicked;
                  JPopupMenu menu = new JPopupMenu();
                  JMenuItem label = new JMenuItem(loadout.getName());
                  label.setEnabled(false);
                  menu.add(label);
                  menu.add(new JMenuItem(new RenameLoadoutAction(loadout, KeyStroke.getKeyStroke("R"))));
                  menu.add(new JMenuItem(new DeleteLoadoutAction(LSML.getInstance().getGarage(), loadout, KeyStroke.getKeyStroke("D"))));
                  menu.show(EquipmentPane.this, e.getX(), e.getY());
               }
            }
            if( SwingUtilities.isLeftMouseButton(e) && e.getClickCount() == 2 ){
               Object clicked = getClickedObject(e);
               if( clicked instanceof Chassi ){
                  Chassi chassi = (Chassi)clicked;
                  Loadout loadout = new Loadout(chassi, xBar);
                  aLoadoutDesktop.openLoadout(loadout, xBar);
               }
               else if( clicked instanceof Loadout ){
                  aLoadoutDesktop.openLoadout((Loadout)clicked, xBar);
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
            Item item = ItemDB.lookup((String)leaf);
            DecimalFormat df = new DecimalFormat("#####.#");
            sb.append("<html>");
            sb.append(item.getDescription()).append("<br>");
            sb.append("Slots: ").append(item.getNumCriticalSlots()).append(" Tons: ").append(df.format(item.getMass())).append("<br>");
            if( item instanceof HeatSource ){
               if( item instanceof Weapon ){
                  Weapon weapon = (Weapon)item;
                  sb.append("Damage: ").append(df.format(weapon.getDamagePerVolley())).append(" Cooldown: ")
                    .append(df.format(weapon.getSecondsPerShot())).append("<br>");
                  sb.append("Optimal: ").append(df.format(weapon.getRangeMin())).append(" - ").append(df.format(weapon.getRangeLong())).append(" / ")
                    .append(df.format(weapon.getRangeMax())).append("<br>");
                  sb.append("DPS: ").append(df.format(weapon.getStat("d/s"))).append(" DPH: ").append(df.format(weapon.getStat("d/h")))
                    .append(" HPS: ").append(df.format(weapon.getStat("h/s"))).append("<br>");
               }
               sb.append("Heat: ").append(df.format(((HeatSource)item).getHeat())).append("<br>");
            }
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
}
