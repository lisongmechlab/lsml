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

import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyVetoException;
import java.beans.VetoableChangeListener;

import javax.swing.Action;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JInternalFrame;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JSplitPane;
import javax.swing.KeyStroke;

import lisong_mechlab.model.chassi.Part;
import lisong_mechlab.model.loadout.DynamicSlotDistributor;
import lisong_mechlab.model.loadout.Loadout;
import lisong_mechlab.model.loadout.MechGarage;
import lisong_mechlab.model.loadout.UndoStack;
import lisong_mechlab.util.MessageXBar;
import lisong_mechlab.util.MessageXBar.Message;
import lisong_mechlab.util.SwingHelpers;
import lisong_mechlab.view.ProgramInit;
import lisong_mechlab.view.action.AddToGarageAction;
import lisong_mechlab.view.action.CloneLoadoutAction;
import lisong_mechlab.view.action.DeleteLoadoutAction;
import lisong_mechlab.view.action.MaxArmorAction;
import lisong_mechlab.view.action.RenameLoadoutAction;
import lisong_mechlab.view.action.ShareLoadoutAction;
import lisong_mechlab.view.action.UndoLoadoutAction;
import lisong_mechlab.view.graphs.DamageGraph;

public class LoadoutFrame extends JInternalFrame implements MessageXBar.Reader{
   private static final String    CMD_UNDO_LOADOUT   = "undo loadout";
   private static final String    CMD_RENAME_LOADOUT = "rename loadout";
   private static final String    CMD_SAVE_TO_GARAGE = "add to garage";
   private static final long      serialVersionUID   = -9181002222136052106L;
   private static int             openFrameCount     = 0;
   private static final int       xOffset            = 30, yOffset = 30;
   private final Loadout          loadout;
   private final MessageXBar      xbar;
   private final UndoStack        undoStack;
   private final Action           actionUndoLoadout;
   private final Action           actionRename;
   private final Action           actionAddToGarage;
   private final LoadoutInfoPanel infoPanel;

   public LoadoutFrame(Loadout aLoadout, MessageXBar anXBar, UndoStack anUndoStack){
      super(aLoadout.toString(), true, // resizable
            true, // closable
            false, // maximizable
            true);// iconifiable

      undoStack = anUndoStack;
      xbar = anXBar;
      xbar.attach(this);
      loadout = aLoadout;

      // Actions
      actionUndoLoadout = new UndoLoadoutAction(xbar, loadout);
      actionRename = new RenameLoadoutAction(this);
      actionAddToGarage = new AddToGarageAction(loadout);

      JMenuBar menuBar = new JMenuBar();
      menuBar.add(createMenuLoadout());
      menuBar.add(createMenuArmor());
      menuBar.add(createMenuGraphs());
      menuBar.add(createMenuShare());
      setJMenuBar(menuBar);

      // Set the window's location.
      setLocation(xOffset * openFrameCount, yOffset * openFrameCount);
      openFrameCount++;

      infoPanel = new LoadoutInfoPanel(aLoadout, anXBar);
      JSplitPane sp = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, false, createMechView(aLoadout, anXBar), infoPanel);

      sp.setDividerLocation(-1);
      sp.setDividerSize(0);

      setFrameIcon(null);
      setContentPane(sp);

      pack();
      setVisible(true);

      addVetoableChangeListener(new VetoableChangeListener(){
         @Override
         public void vetoableChange(PropertyChangeEvent aE) throws PropertyVetoException{
            if( aE.getPropertyName().equals("closed") && aE.getNewValue().equals(true) ){
               if( !isSaved() ){
                  int ans = JOptionPane.showConfirmDialog(ProgramInit.lsml(), "Would you like to save " + loadout.getName() + " to your garage?",
                                                          "Save to garage?", JOptionPane.YES_NO_CANCEL_OPTION);
                  if( ans == JOptionPane.YES_OPTION ){
                     ProgramInit.lsml().getGarage().add(loadout, true);
                  }
                  else if( ans == JOptionPane.NO_OPTION ){
                     // Discard loadout
                  }
                  else{
                     throw new PropertyVetoException("Save canceled!", aE);
                  }
               }
               // Being closed, clear undo stack of references to this loadout.
               undoStack.clearLoadout(loadout);
            }
         }
      });
      setupKeybindings();
   }

   private void setupKeybindings(){
      SwingHelpers.bindAction(this, CMD_UNDO_LOADOUT, actionUndoLoadout);
      SwingHelpers.bindAction(this, CMD_RENAME_LOADOUT, actionRename);
      SwingHelpers.bindAction(this, CMD_SAVE_TO_GARAGE, actionAddToGarage);
   }

   public boolean isSaved(){
      return ProgramInit.lsml().getGarage().getMechs().contains(loadout);
   }

   public Loadout getLoadout(){
      return loadout;
   }

   private JPanel createMechView(Loadout aConfiguration, MessageXBar anXBar){
      final JPanel panel = new JPanel();
      panel.setLayout(new BoxLayout(panel, BoxLayout.LINE_AXIS));

      Dimension padding = new Dimension(5, 0);

      panel.add(Box.createRigidArea(padding));

      DynamicSlotDistributor slotDistributor = new DynamicSlotDistributor(loadout);

      // Right Arm
      {
         final JPanel subPanel = new JPanel();
         subPanel.setLayout(new BoxLayout(subPanel, BoxLayout.PAGE_AXIS));
         subPanel.add(Box.createVerticalStrut(50));
         subPanel.add(new PartPanel(aConfiguration.getPart(Part.RightArm), anXBar, true, slotDistributor));
         subPanel.add(Box.createVerticalGlue());
         panel.add(subPanel);
      }

      panel.add(Box.createRigidArea(padding));

      // Right Torso + Leg
      {
         final JPanel subPanel = new JPanel();
         subPanel.setLayout(new BoxLayout(subPanel, BoxLayout.PAGE_AXIS));
         subPanel.add(new PartPanel(aConfiguration.getPart(Part.RightTorso), anXBar, true, slotDistributor));
         subPanel.add(new PartPanel(aConfiguration.getPart(Part.RightLeg), anXBar, false, slotDistributor));
         subPanel.add(Box.createVerticalGlue());
         panel.add(subPanel);
      }

      panel.add(Box.createRigidArea(padding));

      // Center Torso + Head
      {
         final JPanel subPanel = new JPanel();
         subPanel.setLayout(new BoxLayout(subPanel, BoxLayout.PAGE_AXIS));
         subPanel.add(new PartPanel(aConfiguration.getPart(Part.Head), anXBar, true, slotDistributor));
         subPanel.add(new PartPanel(aConfiguration.getPart(Part.CenterTorso), anXBar, true, slotDistributor));
         subPanel.add(Box.createVerticalGlue());
         panel.add(subPanel);
      }

      panel.add(Box.createRigidArea(padding));

      // Left Torso + Leg
      {
         final JPanel subPanel = new JPanel();
         subPanel.setLayout(new BoxLayout(subPanel, BoxLayout.PAGE_AXIS));
         subPanel.add(new PartPanel(aConfiguration.getPart(Part.LeftTorso), anXBar, true, slotDistributor));
         subPanel.add(new PartPanel(aConfiguration.getPart(Part.LeftLeg), anXBar, false, slotDistributor));
         subPanel.add(Box.createVerticalGlue());
         panel.add(subPanel);
      }

      panel.add(Box.createRigidArea(padding));

      // Left Arm
      {
         final JPanel subPanel = new JPanel();
         subPanel.setLayout(new BoxLayout(subPanel, BoxLayout.PAGE_AXIS));
         subPanel.add(Box.createVerticalStrut(50));
         subPanel.add(new PartPanel(aConfiguration.getPart(Part.LeftArm), anXBar, true, slotDistributor));
         subPanel.add(Box.createVerticalGlue());
         panel.add(subPanel);
      }
      // panel.setVisible(true);
      // panel.validate();
      // setMinimumSize(panel.getSize());
      // setMaximumSize(getMinimumSize());
      // setPreferredSize(getMinimumSize());
      return panel;
   }

   private JMenuItem createMenuItem(String text, ActionListener anActionListener){
      JMenuItem item = new JMenuItem(text);
      item.addActionListener(anActionListener);
      return item;
   }

   private JMenu createMenuShare(){
      JMenu menu = new JMenu("Share!");
      menu.add(new JMenuItem(new ShareLoadoutAction(this)));
      return menu;
   }

   private JMenu createMenuLoadout(){
      JMenu menu = new JMenu("Loadout");
      menu.add(new JMenuItem(actionAddToGarage));
      menu.add(new JMenuItem(actionUndoLoadout));
      menu.add(new JMenuItem(actionRename));
      menu.add(new JMenuItem(new DeleteLoadoutAction(ProgramInit.lsml().getGarage(), this, KeyStroke.getKeyStroke("D"))));

      menu.add(createMenuItem("Load stock", new ActionListener(){
         @Override
         public void actionPerformed(ActionEvent aArg0){
            try{
               loadout.loadStock();
            }
            catch( Exception e ){
               JOptionPane.showMessageDialog(ProgramInit.lsml(), "Couldn't load stock loadout! Error: " + e.getMessage());
            }
         }
      }));

      menu.add(createMenuItem("Strip mech", new ActionListener(){
         @Override
         public void actionPerformed(ActionEvent aArg0){
            loadout.strip();
         }
      }));

      menu.add(new JMenuItem(new CloneLoadoutAction("Clone", loadout, KeyStroke.getKeyStroke("C"))));
      return menu;
   }

   private JMenu createMenuArmor(){
      JMenu menu = new JMenu("Armor");

      menu.add(createMenuItem("Strip Armor", new ActionListener(){
         @Override
         public void actionPerformed(ActionEvent aArg0){
            loadout.stripArmor();
         }
      }));

      {
         JMenu subMenu = new JMenu("Max Armor");
         menu.add(subMenu);
         subMenu.add(new JMenuItem(new MaxArmorAction("3:1", this, 3)));
         subMenu.add(new JMenuItem(new MaxArmorAction("5:1", this, 5)));
         subMenu.add(new JMenuItem(new MaxArmorAction("10:1", this, 10)));
         subMenu.add(new JMenuItem(new MaxArmorAction("Custom...", this, -1)));
      }
      return menu;
   }

   private JMenu createMenuGraphs(){
      JMenu menu = new JMenu("Graphs");

      menu.add(createMenuItem("Damage", new ActionListener(){
         @Override
         public void actionPerformed(ActionEvent aArg0){
            new DamageGraph(loadout, xbar, infoPanel.getMaxSustainedDPSMetric());
         }
      }));
      return menu;
   }

   @Override
   public void receive(Message aMsg){
      if( !aMsg.isForMe(loadout) )
         return;

      if( aMsg instanceof MechGarage.Message ){
         MechGarage.Message msg = (MechGarage.Message)aMsg;
         if( msg.type == MechGarage.Message.Type.LoadoutRemoved ){
            dispose(); // Closes frame
         }
      }
      else if( aMsg instanceof Loadout.Message ){
         Loadout.Message msg = (Loadout.Message)aMsg;
         if( msg.type == Loadout.Message.Type.RENAME ){
            setTitle(loadout.toString());
         }
      }
   }
}
