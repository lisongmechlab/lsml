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
package lisong_mechlab.view.graphs;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.ButtonGroup;
import javax.swing.JCheckBox;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.ListSelectionModel;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

import lisong_mechlab.model.chassi.Chassi;
import lisong_mechlab.model.chassi.ChassiClass;
import lisong_mechlab.model.chassi.ChassiDB;
import lisong_mechlab.model.metrics.PayloadStatistics;
import lisong_mechlab.model.upgrades.SetEndoSteelOperation;
import lisong_mechlab.model.upgrades.SetFerroFibrousOperation;
import lisong_mechlab.model.upgrades.Upgrades;
import lisong_mechlab.util.OperationStack;
import lisong_mechlab.view.graphs.PayloadGraphPanel.Entry;

/**
 * Draws the panel where one can select chassis by payload tonnage.
 * 
 * @author Emily Björk
 */
public class PayloadSelectionPanel extends JPanel{
   private static class PayloadSettingsPanel extends JPanel{
      private static final long                    serialVersionUID = 4965116372512246203L;
      private final JRadioButton                   xlEngine         = new JRadioButton("XL engine", false);
      private final JRadioButton                   stdEngine        = new JRadioButton("STD engine", true);
      private final ButtonGroup                    engineGroup      = new ButtonGroup();
      private final JRadioButton                   noArmor          = new JRadioButton("0% armor", false);
      private final JRadioButton                   maxArmor         = new JRadioButton("100% armor", true);
      private final ButtonGroup                    armorGroup       = new ButtonGroup();
      private final JCheckBox                      endoSteel        = new JCheckBox("Endo-Steel");
      private final JCheckBox                      ferroFibrous     = new JCheckBox("Ferro-Fibrous");
      private final JCheckBox                      speedTweak       = new JCheckBox("Speed Tweak");
      private final JList<PayloadGraphPanel.Entry> graphEntries;

      public PayloadSettingsPanel(Collection<Entry> aChassis){
         setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
         graphEntries = new JList<>(aChassis.toArray(new PayloadGraphPanel.Entry[aChassis.size()]));
         engineGroup.add(stdEngine);
         engineGroup.add(xlEngine);
         armorGroup.add(noArmor);
         armorGroup.add(maxArmor);

         JPanel enginePanel = new JPanel();
         enginePanel.setLayout(new BoxLayout(enginePanel, BoxLayout.X_AXIS));
         enginePanel.add(stdEngine);
         enginePanel.add(xlEngine);
         add(enginePanel);

         JPanel armorPanel = new JPanel();
         armorPanel.setLayout(new BoxLayout(armorPanel, BoxLayout.X_AXIS));
         armorPanel.add(noArmor);
         armorPanel.add(maxArmor);
         add(armorPanel);

         add(speedTweak);
         add(endoSteel);
         add(ferroFibrous);

         JPanel entriesPanel = new JPanel();
         entriesPanel.setLayout(new BoxLayout(entriesPanel, BoxLayout.Y_AXIS));
         entriesPanel.setBorder(BorderFactory.createTitledBorder("Chassis to include"));
         entriesPanel.add(graphEntries);
         add(entriesPanel);

         graphEntries.setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
         graphEntries.addSelectionInterval(0, graphEntries.getModel().getSize() - 1);
      }

      void setupListeners(final PayloadStatistics aPayloadStatistics, final PayloadGraphPanel aGraphPanel, final Upgrades aUpgrades){
         final OperationStack stack = new OperationStack(0);

         graphEntries.getSelectionModel().addListSelectionListener(new ListSelectionListener(){
            @Override
            public void valueChanged(ListSelectionEvent aArg0){
               aGraphPanel.selectChassis(graphEntries.getSelectedValuesList());
               aGraphPanel.updateGraph();
            }
         });

         xlEngine.addActionListener(new ActionListener(){
            @Override
            public void actionPerformed(ActionEvent aArg0){
               aPayloadStatistics.changeUseXLEngine(xlEngine.isSelected());
               aGraphPanel.updateGraph();
            }
         });
         stdEngine.addActionListener(new ActionListener(){
            @Override
            public void actionPerformed(ActionEvent aE){
               aPayloadStatistics.changeUseXLEngine(!stdEngine.isSelected());
               aGraphPanel.updateGraph();
            }
         });
         noArmor.addActionListener(new ActionListener(){
            @Override
            public void actionPerformed(ActionEvent aE){
               aPayloadStatistics.changeUseMaxArmor(!noArmor.isSelected());
               aGraphPanel.updateGraph();
            }
         });
         maxArmor.addActionListener(new ActionListener(){
            @Override
            public void actionPerformed(ActionEvent aE){
               aPayloadStatistics.changeUseMaxArmor(maxArmor.isSelected());
               aGraphPanel.updateGraph();
            }
         });
         endoSteel.addActionListener(new ActionListener(){
            @Override
            public void actionPerformed(ActionEvent aE){
               stack.pushAndApply(new SetEndoSteelOperation(aUpgrades, endoSteel.isSelected()));
               aGraphPanel.updateGraph();
            }
         });
         ferroFibrous.addActionListener(new ActionListener(){
            @Override
            public void actionPerformed(ActionEvent aE){
               stack.pushAndApply(new SetFerroFibrousOperation(aUpgrades, ferroFibrous.isSelected()));
               aGraphPanel.updateGraph();
            }
         });

      }
   }

   private static final long                   serialVersionUID = 1L;

   private final Upgrades                      upgrades;
   private final PayloadGraphPanel             graphPanel;
   private final PayloadStatistics             payloadStatistics;
   private Collection<PayloadGraphPanel.Entry> chassis;

   public PayloadSelectionPanel(){
      setLayout(new BoxLayout(this, BoxLayout.X_AXIS));

      chassis = calculateUniqueSpeedChassis();
      PayloadSettingsPanel settingsPanel = new PayloadSettingsPanel(chassis);

      upgrades = new Upgrades();
      payloadStatistics = new PayloadStatistics(false, true, upgrades);
      graphPanel = new PayloadGraphPanel(payloadStatistics, settingsPanel.speedTweak);
      graphPanel.selectChassis(chassis);
      graphPanel.updateGraph();

      settingsPanel.setupListeners(payloadStatistics, graphPanel, upgrades);

      add(settingsPanel);
      add(graphPanel);
   }

   private Collection<PayloadGraphPanel.Entry> calculateUniqueSpeedChassis(){
      Collection<Collection<Chassi>> temp = new ArrayList<>();

      List<Chassi> all = ChassiDB.lookup(ChassiClass.LIGHT);
      all.addAll(ChassiDB.lookup(ChassiClass.MEDIUM));
      all.addAll(ChassiDB.lookup(ChassiClass.HEAVY));
      all.addAll(ChassiDB.lookup(ChassiClass.ASSAULT));

      Collections.sort(all, new Comparator<Chassi>(){
         @Override
         public int compare(Chassi aO1, Chassi aO2){
            return Integer.compare(aO1.getMassMax(), aO2.getMassMax());
         }
      });

      for(Chassi chassi : all){
         if( chassi.isSpecialVariant() )
            continue;
         boolean skip = false;
         for(Collection<Chassi> chassiGroup : temp){
            Chassi aChassi = chassiGroup.iterator().next();
            if( aChassi.getMassMax() == chassi.getMassMax() && aChassi.getEngineMin() == chassi.getEngineMin()
                && aChassi.getEngineMax() == chassi.getEngineMax() && aChassi.getSpeedFactor() == chassi.getSpeedFactor()
                && aChassi.isSameSeries(chassi) ){
               chassiGroup.add(chassi);
               skip = true;
               break;
            }
         }
         if( !skip ){
            temp.add(new ArrayList<Chassi>(Arrays.asList(chassi)));
         }
      }

      Collection<PayloadGraphPanel.Entry> ans = new ArrayList<>();
      for(Collection<Chassi> chassiGroup : temp){
         ans.add(new PayloadGraphPanel.Entry(chassiGroup));
      }

      return ans;
   }
}
