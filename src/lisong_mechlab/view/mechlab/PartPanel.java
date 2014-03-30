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
package lisong_mechlab.view.mechlab;

import java.awt.BorderLayout;
import java.awt.Dimension;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JCheckBox;
import javax.swing.JFormattedTextField;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSpinner;
import javax.swing.SwingUtilities;
import javax.swing.text.DefaultFormatter;

import lisong_mechlab.model.DynamicSlotDistributor;
import lisong_mechlab.model.chassi.ArmorSide;
import lisong_mechlab.model.chassi.HardPointType;
import lisong_mechlab.model.loadout.part.LoadoutPart;
import lisong_mechlab.util.MessageXBar;
import lisong_mechlab.util.MessageXBar.Message;
import lisong_mechlab.util.OperationStack;
import lisong_mechlab.view.render.ItemRenderer;
import lisong_mechlab.view.render.StyleManager;

public class PartPanel extends JPanel implements MessageXBar.Reader{
   private static final int  ARMOR_LABEL_WIDTH   = 30;
   private static final int  ARMOR_SPINNER_WIDTH = 20;

   private static final long serialVersionUID    = -4399442572295284661L;

   private JLabel            frontArmorLabel;
   private JLabel            backArmorLabel;

   private final LoadoutPart loadoutPart;

   private boolean           canHaveHardpoints;

   PartPanel(LoadoutPart aLoadoutPart, MessageXBar anXBar, boolean aCanHaveHardpoints, DynamicSlotDistributor aSlotDistributor, JCheckBox aSymmetric,
             OperationStack aStack){
      super(new BorderLayout());
      anXBar.attach(this);
      loadoutPart = aLoadoutPart;
      canHaveHardpoints = aCanHaveHardpoints;

      setLayout(new BoxLayout(this, BoxLayout.PAGE_AXIS));
      setBorder(BorderFactory.createCompoundBorder(BorderFactory.createTitledBorder(aLoadoutPart.getInternalPart().getType().longName()),
                                                   BorderFactory.createEmptyBorder(0, 2, 2, 4)));
      add(makeArmorPanel(anXBar, aSymmetric, aStack));

      if( canHaveHardpoints )
         add(makeHardpointsPanel());

      // Critical slots
      PartList list = new PartList(aStack, aLoadoutPart, anXBar, aSlotDistributor);
      list.setFixedCellHeight(ItemRenderer.ITEM_BASE_HEIGHT);
      list.setFixedCellWidth(ItemRenderer.ITEM_BASE_WIDTH);

      setAlignmentX(LEFT_ALIGNMENT);
      add(list);
      add(Box.createRigidArea(new Dimension(0, 1)));
   }

   private JPanel makeHardpointsPanel(){
      JPanel panel = new JPanel();
      BoxLayout layoutManager = new BoxLayout(panel, BoxLayout.LINE_AXIS);
      panel.setLayout(layoutManager);
      // /panel.setBackground(Color.PINK.darker());
      panel.add(Box.createVerticalStrut(ItemRenderer.ITEM_BASE_HEIGHT + ItemRenderer.ITEM_BASE_HEIGHT / 2));

      for(HardPointType hp : HardPointType.values()){
         JLabel label = new JLabel();
         StyleManager.styleHardpointLabel(label, loadoutPart.getInternalPart(), hp);
         panel.add(label);
      }

      panel.add(Box.createHorizontalGlue());
      return panel;
   }

   private JPanel makeArmorPanel(MessageXBar anXBar, JCheckBox aSymmetric, OperationStack aStack){
      JPanel panel = new JPanel();
      Dimension labelDimension = new Dimension(ARMOR_LABEL_WIDTH, ItemRenderer.ITEM_BASE_HEIGHT);
      Dimension spinnerDimension = new Dimension(ARMOR_SPINNER_WIDTH, 0);

      if( loadoutPart.getInternalPart().getType().isTwoSided() ){

         frontArmorLabel = new JLabel(" / " + Integer.valueOf(loadoutPart.getArmorMax(ArmorSide.FRONT)));
         frontArmorLabel.setPreferredSize(labelDimension);
         backArmorLabel = new JLabel(" / " + Integer.valueOf(loadoutPart.getArmorMax(ArmorSide.BACK)));
         backArmorLabel.setPreferredSize(labelDimension);

         JSpinner frontSpinner = new JSpinner(new ArmorSpinner(loadoutPart, ArmorSide.FRONT, anXBar, aSymmetric, aStack));
         frontSpinner.setMaximumSize(labelDimension);
         frontSpinner.getEditor().setPreferredSize(spinnerDimension);
         JFormattedTextField field = (JFormattedTextField)frontSpinner.getEditor().getComponent(0);
         ((DefaultFormatter)field.getFormatter()).setCommitsOnValidEdit(true);

         JSpinner backSpinner = new JSpinner(new ArmorSpinner(loadoutPart, ArmorSide.BACK, anXBar, aSymmetric, aStack));
         backSpinner.setMaximumSize(labelDimension);
         backSpinner.getEditor().setPreferredSize(spinnerDimension);

         JPanel frontPanel = new JPanel();
         frontPanel.setLayout(new BoxLayout(frontPanel, BoxLayout.LINE_AXIS));
         frontPanel.add(new JLabel("Front:"));
         frontPanel.add(Box.createHorizontalGlue());
         frontPanel.add(frontSpinner);
         frontPanel.add(frontArmorLabel);

         JPanel backPanel = new JPanel();
         backPanel.setLayout(new BoxLayout(backPanel, BoxLayout.LINE_AXIS));
         backPanel.add(new JLabel("Back:"));
         backPanel.add(Box.createHorizontalGlue());
         backPanel.add(backSpinner);
         backPanel.add(backArmorLabel);

         panel.setLayout(new BoxLayout(panel, BoxLayout.PAGE_AXIS));
         panel.add(frontPanel);
         panel.add(backPanel);
      }
      else{
         JLabel armorLabel = new JLabel(" / " + Integer.valueOf(loadoutPart.getInternalPart().getArmorMax()));
         armorLabel.setPreferredSize(labelDimension);

         JSpinner spinner = new JSpinner(new ArmorSpinner(loadoutPart, ArmorSide.ONLY, anXBar, aSymmetric, aStack));
         spinner.setMaximumSize(labelDimension);
         spinner.getEditor().setPreferredSize(spinnerDimension);
         JFormattedTextField field = (JFormattedTextField)spinner.getEditor().getComponent(0);
         ((DefaultFormatter)field.getFormatter()).setCommitsOnValidEdit(true);

         panel.add(new JLabel("Armor:"));
         panel.add(Box.createHorizontalGlue());
         panel.add(spinner);
         panel.setLayout(new BoxLayout(panel, BoxLayout.LINE_AXIS));
         panel.add(armorLabel);
      }
      return panel;
   }

   @Override
   public void receive(Message aMsg){
      SwingUtilities.invokeLater(new Runnable(){
         @Override
         public void run(){
            if( backArmorLabel != null && frontArmorLabel != null ){
               frontArmorLabel.setText(" / " + Integer.valueOf(loadoutPart.getArmorMax(ArmorSide.FRONT)));
               backArmorLabel.setText(" / " + Integer.valueOf(loadoutPart.getArmorMax(ArmorSide.BACK)));
            }
         }
      });
   }
}
