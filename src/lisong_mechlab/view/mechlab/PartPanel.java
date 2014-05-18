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

import java.awt.Color;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.Collection;
import java.util.Vector;

import javax.swing.AbstractAction;
import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JFormattedTextField;
import javax.swing.JLabel;
import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JSpinner;
import javax.swing.JTextField;
import javax.swing.SwingUtilities;
import javax.swing.text.DefaultFormatter;

import lisong_mechlab.model.DynamicSlotDistributor;
import lisong_mechlab.model.chassi.ArmorSide;
import lisong_mechlab.model.chassi.HardPointType;
import lisong_mechlab.model.chassi.InternalComponent;
import lisong_mechlab.model.chassi.OmniPod;
import lisong_mechlab.model.chassi.OmniPodDB;
import lisong_mechlab.model.loadout.LoadoutBase;
import lisong_mechlab.model.loadout.LoadoutOmniMech;
import lisong_mechlab.model.loadout.component.ConfiguredComponent;
import lisong_mechlab.model.loadout.component.ConfiguredComponent.Message.Type;
import lisong_mechlab.model.loadout.component.OpSetArmor;
import lisong_mechlab.util.MessageXBar;
import lisong_mechlab.util.MessageXBar.Message;
import lisong_mechlab.util.OperationStack;
import lisong_mechlab.view.ProgramInit;
import lisong_mechlab.view.render.ItemRenderer;
import lisong_mechlab.view.render.StyleManager;

public class PartPanel extends JPanel implements MessageXBar.Reader{
   class ArmorPopupAdapter extends MouseAdapter{
      private final MessageXBar    xBar;
      private final OperationStack stack;

      public ArmorPopupAdapter(OperationStack aStack, MessageXBar aXBar){
         stack = aStack;
         xBar = aXBar;
      }

      @Override
      public void mousePressed(MouseEvent e){
         if( e.isPopupTrigger() )
            doPop(e);
      }

      @Override
      public void mouseReleased(MouseEvent e){
         if( e.isPopupTrigger() )
            doPop(e);
      }

      private void doPop(MouseEvent e){
         JPopupMenu menu = new JPopupMenu("Armor Options");
         menu.add(new JMenuItem(new AbstractAction("Allow automatic adjustment"){
            private static final long serialVersionUID = 7539044187157207692L;

            @Override
            public void actionPerformed(ActionEvent aE){
               if( loadoutPart.getInternalComponent().getLocation().isTwoSided() ){
                  stack.pushAndApply(new OpSetArmor(xBar, loadout, loadoutPart, ArmorSide.FRONT, loadoutPart.getArmor(ArmorSide.FRONT), false));
               }
               else{
                  stack.pushAndApply(new OpSetArmor(xBar, loadout, loadoutPart, ArmorSide.ONLY, loadoutPart.getArmorTotal(), false));
               }
               xBar.post(new ConfiguredComponent.Message(loadoutPart, Type.ArmorDistributionUpdateRequest));
            }
         }));
         menu.show(e.getComponent(), e.getX(), e.getY());
      }
   }

   private static final int          ARMOR_LABEL_WIDTH   = 30;
   private static final int          ARMOR_SPINNER_WIDTH = 20;

   private static final long         serialVersionUID    = -4399442572295284661L;

   private final JLabel              frontArmorLabel;
   private final JLabel              backArmorLabel;
   private final JLabel              armorLabel;

   private final LoadoutBase<?, ?>   loadout;
   private final ConfiguredComponent loadoutPart;

   private final boolean             canHaveHardpoints;
   private final ArmorPopupAdapter   armorPopupAdapter;
   private JSpinner                  frontSpinner;
   private JSpinner                  backSpinner;
   private JSpinner                  spinner;

   private final JComboBox<OmniPod>  omnipodSelection;

   PartPanel(LoadoutBase<?, ?> aLoadout, ConfiguredComponent aLoadoutPart, MessageXBar anXBar, boolean aCanHaveHardpoints,
             DynamicSlotDistributor aSlotDistributor, JCheckBox aSymmetric, OperationStack aStack){
      setLayout(new BoxLayout(this, BoxLayout.PAGE_AXIS));
      anXBar.attach(this);
      loadout = aLoadout;
      loadoutPart = aLoadoutPart;
      canHaveHardpoints = aCanHaveHardpoints;
      armorPopupAdapter = new ArmorPopupAdapter(aStack, anXBar);

      if( aLoadoutPart.getInternalComponent().getLocation().isTwoSided() ){
         frontArmorLabel = new JLabel();
         backArmorLabel = new JLabel();
         armorLabel = null;
      }
      else{
         frontArmorLabel = null;
         backArmorLabel = null;
         armorLabel = new JLabel();
      }

      setLayout(new BoxLayout(this, BoxLayout.PAGE_AXIS));
      if( !ProgramInit.lsml().preferences.uiPreferences.getCompactMode() ){
         InternalComponent internalPart = aLoadoutPart.getInternalComponent();
         setBorder(BorderFactory.createCompoundBorder(BorderFactory.createTitledBorder(internalPart.getLocation().longName() + " ("
                                                                                       + (int)internalPart.getHitPoints() + " hp)"),
                                                      BorderFactory.createEmptyBorder(0, 2, 2, 4)));
      }

      if( LoadoutOmniMech.class.isAssignableFrom(aLoadout.getClass()) ){
         LoadoutOmniMech omniMech = (LoadoutOmniMech)aLoadout;
         // Omnimech
         Collection<OmniPod> compatiblePods = OmniPodDB.lookup(omniMech.getChassis(), aLoadoutPart.getInternalComponent().getLocation());
         omnipodSelection = new JComboBox<>(new Vector<>(compatiblePods));
         Dimension max = omnipodSelection.getMaximumSize();
         max.height = ItemRenderer.getItemHeight();
         omnipodSelection.setMaximumSize(max);
         add(omnipodSelection);
      }
      else{
         omnipodSelection = null;
      }

      add(makeArmorPanel(anXBar, aSymmetric, aStack));

      if( canHaveHardpoints )
         add(makeHardpointsPanel());

      // Critical slots
      PartList list = new PartList(aStack, aLoadout, aLoadoutPart, anXBar, aSlotDistributor);
      list.setFixedCellHeight(ItemRenderer.getItemHeight());
      list.setFixedCellWidth(ItemRenderer.getItemWidth());

      setAlignmentX(LEFT_ALIGNMENT);
      add(list);

      updateArmorPanel();
   }

   private JPanel makeHardpointsPanel(){
      JPanel panel = new JPanel();
      BoxLayout layoutManager = new BoxLayout(panel, BoxLayout.LINE_AXIS);
      panel.setLayout(layoutManager);
      // /panel.setBackground(Color.PINK.darker());
      panel.add(Box.createVerticalStrut(ItemRenderer.getItemHeight() + ItemRenderer.getItemHeight() / 2));

      for(HardPointType hp : HardPointType.values()){
         JLabel label = new JLabel();
         StyleManager.styleHardpointLabel(label, loadoutPart.getInternalComponent(), hp);
         panel.add(label);
      }

      panel.add(Box.createHorizontalGlue());
      updateArmorPanel();
      return panel;
   }

   private JPanel makeArmorPanel(MessageXBar anXBar, JCheckBox aSymmetric, OperationStack aStack){
      JPanel panel = new JPanel();
      Dimension labelDimension = new Dimension(ARMOR_LABEL_WIDTH, ItemRenderer.getItemHeight());
      Dimension spinnerDimension = new Dimension(ARMOR_SPINNER_WIDTH, 0);

      if( loadoutPart.getInternalComponent().getLocation().isTwoSided() ){
         frontArmorLabel.setPreferredSize(labelDimension);
         backArmorLabel.setPreferredSize(labelDimension);

         frontSpinner = new JSpinner(new ArmorSpinner(loadout, loadoutPart, ArmorSide.FRONT, anXBar, aSymmetric, aStack));
         frontSpinner.setMaximumSize(labelDimension);
         frontSpinner.getEditor().setPreferredSize(spinnerDimension);
         JFormattedTextField field = (JFormattedTextField)frontSpinner.getEditor().getComponent(0);
         ((DefaultFormatter)field.getFormatter()).setCommitsOnValidEdit(true);

         backSpinner = new JSpinner(new ArmorSpinner(loadout, loadoutPart, ArmorSide.BACK, anXBar, aSymmetric, aStack));
         backSpinner.setMaximumSize(labelDimension);
         backSpinner.getEditor().setPreferredSize(spinnerDimension);

         JPanel frontPanel = new JPanel();
         frontPanel.setLayout(new BoxLayout(frontPanel, BoxLayout.LINE_AXIS));
         if( ProgramInit.lsml().preferences.uiPreferences.getCompactMode() ){
            frontPanel.add(new JLabel("F:"));
         }
         else{
            frontPanel.add(new JLabel("Front:"));
         }
         frontPanel.add(Box.createHorizontalGlue());
         frontPanel.add(frontSpinner);
         frontPanel.add(frontArmorLabel);

         JPanel backPanel = new JPanel();
         backPanel.setLayout(new BoxLayout(backPanel, BoxLayout.LINE_AXIS));
         if( ProgramInit.lsml().preferences.uiPreferences.getCompactMode() ){
            backPanel.add(new JLabel("B:"));
         }
         else{
            backPanel.add(new JLabel("Back:"));
         }
         backPanel.add(Box.createHorizontalGlue());
         backPanel.add(backSpinner);
         backPanel.add(backArmorLabel);

         panel.setLayout(new BoxLayout(panel, BoxLayout.PAGE_AXIS));
         panel.add(frontPanel);
         panel.add(backPanel);
      }
      else{
         armorLabel.setPreferredSize(labelDimension);

         spinner = new JSpinner(new ArmorSpinner(loadout, loadoutPart, ArmorSide.ONLY, anXBar, aSymmetric, aStack));
         spinner.setMaximumSize(labelDimension);
         spinner.getEditor().setPreferredSize(spinnerDimension);
         JFormattedTextField field = (JFormattedTextField)spinner.getEditor().getComponent(0);
         ((DefaultFormatter)field.getFormatter()).setCommitsOnValidEdit(true);

         if( !ProgramInit.lsml().preferences.uiPreferences.getCompactMode() ){
            panel.add(new JLabel("Armor:"));
         }
         panel.add(Box.createHorizontalGlue());
         panel.add(spinner);
         panel.setLayout(new BoxLayout(panel, BoxLayout.LINE_AXIS));
         panel.add(armorLabel);
      }

      panel.addMouseListener(armorPopupAdapter);

      return panel;
   }

   void updateArmorPanel(){
      if( armorLabel != null ){
         armorLabel.setText(" /" + Integer.valueOf(loadoutPart.getInternalComponent().getArmorMax()));
         JTextField tf = ((JSpinner.DefaultEditor)spinner.getEditor()).getTextField();

         if( loadoutPart.allowAutomaticArmor() ){
            armorLabel.setForeground(Color.GRAY);
            tf.setForeground(Color.GRAY);
         }
         else{
            armorLabel.setForeground(Color.BLACK);
            tf.setForeground(Color.BLACK);
         }
      }
      if( backArmorLabel != null && frontArmorLabel != null ){
         frontArmorLabel.setText(" /" + Integer.valueOf(loadoutPart.getArmorMax(ArmorSide.FRONT)));
         backArmorLabel.setText(" /" + Integer.valueOf(loadoutPart.getArmorMax(ArmorSide.BACK)));
         JTextField tff = ((JSpinner.DefaultEditor)frontSpinner.getEditor()).getTextField();
         JTextField tfb = ((JSpinner.DefaultEditor)backSpinner.getEditor()).getTextField();

         if( loadoutPart.allowAutomaticArmor() ){
            frontArmorLabel.setForeground(Color.GRAY);
            backArmorLabel.setForeground(Color.GRAY);
            tff.setForeground(Color.GRAY);
            tfb.setForeground(Color.GRAY);
         }
         else{
            frontArmorLabel.setForeground(Color.BLACK);
            backArmorLabel.setForeground(Color.BLACK);
            tff.setForeground(Color.BLACK);
            tfb.setForeground(Color.BLACK);
         }
      }
   }

   @Override
   public void receive(Message aMsg){
      if( aMsg.isForMe(loadout) && aMsg instanceof ConfiguredComponent.Message ){
         ConfiguredComponent.Message msg = (ConfiguredComponent.Message)aMsg;
         if( msg.type == Type.ArmorChanged ){
            SwingUtilities.invokeLater(new Runnable(){
               @Override
               public void run(){
                  updateArmorPanel();
               }
            });
         }
      }
   }
}
