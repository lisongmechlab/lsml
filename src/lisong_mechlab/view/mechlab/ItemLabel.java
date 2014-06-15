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

import java.awt.Component;
import java.awt.GradientPaint;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.GridLayout;
import java.awt.RenderingHints;
import java.awt.event.ActionEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.text.DecimalFormat;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import javax.swing.AbstractAction;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JProgressBar;
import javax.swing.SwingUtilities;
import javax.swing.SwingWorker;
import javax.swing.TransferHandler;
import javax.swing.WindowConstants;

import lisong_mechlab.model.item.AmmoWeapon;
import lisong_mechlab.model.item.Ammunition;
import lisong_mechlab.model.item.Engine;
import lisong_mechlab.model.item.Item;
import lisong_mechlab.model.loadout.LoadoutBase;
import lisong_mechlab.model.loadout.OpAutoAddItem;
import lisong_mechlab.model.metrics.TopSpeed;
import lisong_mechlab.util.MessageXBar;
import lisong_mechlab.view.ItemTransferHandler;
import lisong_mechlab.view.ProgramInit;
import lisong_mechlab.view.mechlab.equipment.EquipmentPanel;
import lisong_mechlab.view.render.StyleManager;

/**
 * This class implements a JLabel to render an item that can be dragged onto a loadout.
 * 
 * @author Emily Björk
 */
public class ItemLabel extends JLabel{
   private static final long serialVersionUID = 1237952620487557121L;
   private final Item        item;

   private static class ProgressDialog extends JDialog{
      private static final long serialVersionUID = -6084430266229568009L;
      SwingWorker<Void, Void>   task;

      public ProgressDialog(){
         super(ProgramInit.lsml(), "SmartPlace in progress...", ModalityType.APPLICATION_MODAL);
         setLocationRelativeTo(ProgramInit.lsml());

         JPanel panel = new JPanel(new GridLayout(3, 1, 10, 10));
         panel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
         JProgressBar progressBar = new JProgressBar();
         progressBar.setIndeterminate(true);
         panel.add(new JLabel("That's a tricky proposition cap'n but I'll see what I can do..."));
         panel.add(progressBar);
         panel.add(new JButton(new AbstractAction("Abort"){
            private static final long serialVersionUID = 2384981612883023314L;

            @Override
            public void actionPerformed(ActionEvent aE){
               if( task != null )
                  task.cancel(true);
            }
         }));
         setContentPane(panel);
         setDefaultCloseOperation(WindowConstants.HIDE_ON_CLOSE);
         pack();
      }

      void setTask(SwingWorker<Void, Void> aTask){
         task = aTask;
      }
   }

   private static class AutoPlaceTask extends SwingWorker<Void, Void>{
      private OpAutoAddItem operation;
      private JDialog       dialog;
      private LoadoutFrame  loadoutFrame;
      private MessageXBar   xBar;
      private Item          itemToPlace;

      public AutoPlaceTask(JDialog aDialog, LoadoutFrame aLoadoutFrame, MessageXBar anXBar, Item aItem){
         dialog = aDialog;
         loadoutFrame = aLoadoutFrame;
         xBar = anXBar;
         itemToPlace = aItem;
      }

      @Override
      public Void doInBackground(){
         try{
            operation = new OpAutoAddItem(loadoutFrame.getLoadout(), xBar, itemToPlace);
         }
         catch( Throwable e ){ // Yeah anything thrown is a failure.
            operation = null;
         }
         return null;
      }

      @Override
      public void done(){
         // In EDT
         if( !isCancelled() ){
            if( operation == null ){
               JOptionPane.showMessageDialog(dialog, "No can do cap'n!", "Not possible", JOptionPane.OK_OPTION);
            }
            else{
               loadoutFrame.getOpStack().pushAndApply(operation);
            }
         }
         dialog.dispose();
      }
   }

   public ItemLabel(Item anItem, final EquipmentPanel aEquipmentPanel, final ItemInfoPanel aInfoPanel, final MessageXBar anXBar){
      item = anItem;

      StyleManager.styleItem(this, item);
      setToolTipText("<html>" + item.getName() + "<p>" + item.getDescription() + "</html>");

      setTransferHandler(new ItemTransferHandler());
      addMouseListener(new MouseAdapter(){
         @Override
         public void mousePressed(MouseEvent anEvent){
            final LoadoutFrame frame = ProgramInit.lsml().mechLabPane.getActiveLoadoutFrame();
            final LoadoutBase<?> loadout = aEquipmentPanel.getCurrentLoadout();

            Component component = anEvent.getComponent();
            if( component instanceof ItemLabel ){
               if( null != loadout ){
                  aInfoPanel.showItem(item, loadout.getUpgrades(), loadout.getEfficiencies(), loadout.getWeaponModifiers());
               }
               else{
                  aInfoPanel.showItem(item, null, null, null);
               }
            }

            ItemLabel button = (ItemLabel)anEvent.getSource();
            ItemTransferHandler handle = (ItemTransferHandler)button.getTransferHandler();
            handle.exportAsDrag(button, anEvent, TransferHandler.COPY);

            if( SwingUtilities.isLeftMouseButton(anEvent) && anEvent.getClickCount() >= 2 ){
               if( null != loadout ){
                  if( smartPlace ){
                     if( !ProgramInit.lsml().preferences.uiPreferences.getUseSmartPlace() ){
                        Object[] choices = {"Use SmartPlace", "Disable SmartPlace"};
                        Object defaultChoice = choices[0];
                        int choice = JOptionPane.showOptionDialog(ProgramInit.lsml(),
                                                                  "SmartPlace can re-arrange items on your loadout to make the item you're trying to equip fit.\n"
                                                                        + "No items will be removed, only moved.\n"
                                                                        + "It is not guaranteed that there exists an arrangement of items that allows the item to be added\n"
                                                                        + "in which case SmartPlace will try all possible combinations which might take time.\n"
                                                                        + "If smart place is taking too long you can safely abort it without changes to your loadout.\n\n"
                                                                        + "You can see if SmartPlace will be used on the item in the equipment pane if it is semi grayed out.\n",
                                                                  "Enable SmartPlace?", JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE,
                                                                  null, choices, defaultChoice);
                        if( choice == 0 ){
                           ProgramInit.lsml().preferences.uiPreferences.setUseSmartPlace(true);
                        }
                        else{
                           return;
                        }
                     }

                     final ProgressDialog dialog = new ProgressDialog();
                     final AutoPlaceTask task = new AutoPlaceTask(dialog, frame, anXBar, item);
                     task.execute();
                     dialog.setTask(task);
                     dialog.addWindowListener(new WindowAdapter(){
                        @Override
                        public void windowClosed(WindowEvent e){
                           task.cancel(true);
                        }
                     });

                     try{
                        task.get(500, TimeUnit.MILLISECONDS);
                     }
                     catch( InterruptedException e ){
                        return; // Cancelled
                     }
                     catch( ExecutionException e ){
                        throw new RuntimeException(e); // Corblimey
                     }
                     catch( TimeoutException e ){
                        dialog.setVisible(true); // Show progress meter if it's taking time and resume EDT
                     }
                  }
                  else if( loadout.canEquip(item) ){
                     frame.getOpStack().pushAndApply(new OpAutoAddItem(loadout, anXBar, item));
                  }
               }
            }
         }
      });

      updateVisibility(null);
   }

   private void updateText(LoadoutBase<?> aLoadout){
      StringBuilder builder = new StringBuilder();
      builder.append("<html>");
      builder.append(item.getShortName());
      builder.append("<br/><span style=\"font-size:x-small;\">");
      builder.append("Tons: ").append(item.getMass()).append("<br/>Slots: ").append(item.getNumCriticalSlots());

      if( item instanceof Engine && aLoadout != null ){
         Engine engine = (Engine)item;
         double speed = TopSpeed.calculate(engine.getRating(), aLoadout.getMovementProfile(), aLoadout.getChassis().getMassMax(),
                                           aLoadout.getEfficiencies().getSpeedModifier());
         DecimalFormat decimalFormat = new DecimalFormat("###");
         builder.append("<br/>" + decimalFormat.format(speed) + "kph");
      }
      builder.append("</span></html>");

      setText(builder.toString());
   }

   public Item getItem(){
      return item;
   }

   private boolean smartPlace = false;

   @Override
   protected void paintComponent(Graphics grphcs){
      if( !isOpaque() || !smartPlace ){
         super.paintComponent(grphcs);
         return;
      }

      Graphics2D g2d = (Graphics2D)grphcs;
      g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
      int offset = 60;
      GradientPaint gp = new GradientPaint(offset, 0, getBackground(), offset + 1, 1, StyleManager.getBgColorInvalid());
      g2d.setPaint(gp);
      g2d.fillRect(0, 0, getWidth(), getHeight());
      setOpaque(false);
      super.paintComponent(grphcs);
      setOpaque(true);
   }

   public void updateVisibility(LoadoutBase<?> aLoadout){
      boolean prevSmartPlace = smartPlace;
      smartPlace = false;
      if( aLoadout != null ){
         updateText(aLoadout);
         if( !aLoadout.getChassis().isAllowed(item) || !item.isCompatible(aLoadout.getUpgrades()) ){
            setVisible(false);
         }
         else{
            if( !aLoadout.canEquip(item) ){
               if( !aLoadout.getCandidateLocationsForItem(item).isEmpty() ){
                  StyleManager.styleItem(this, item);
                  smartPlace = true;
               }
               else{
                  StyleManager.colourInvalid(this);
               }
            }
            else{
               StyleManager.styleItem(this, item);
            }

            if( item instanceof Ammunition ){
               Ammunition ammunition = (Ammunition)item;
               if( aLoadout.getHardpointsCount(ammunition.getWeaponHardpointType()) < 1 ){
                  setVisible(false);
               }
               else{
                  boolean isUsable = false;
                  for(Item it : aLoadout.getAllItems()){
                     if( it instanceof AmmoWeapon ){
                        if( ((AmmoWeapon)it).getAmmoType(aLoadout.getUpgrades()) == ammunition ){
                           isUsable = true;
                           break;
                        }
                     }
                  }
                  setVisible(isUsable);
               }
            }
            else
               setVisible(true);
         }
      }
      else{
         updateText(null);
         StyleManager.styleItem(this, item);
         setVisible(true);
      }

      if( prevSmartPlace != smartPlace )
         repaint();
   }
}
