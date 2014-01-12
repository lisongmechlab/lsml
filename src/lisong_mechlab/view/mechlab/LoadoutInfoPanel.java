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
import java.awt.Component;
import java.awt.ComponentOrientation;
import java.awt.Dimension;
import java.awt.GridLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;

import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.GroupLayout;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JProgressBar;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;
import javax.swing.border.Border;
import javax.swing.border.CompoundBorder;
import javax.swing.border.EmptyBorder;
import javax.swing.border.TitledBorder;

import lisong_mechlab.model.environment.Environment;
import lisong_mechlab.model.loadout.ArtemisHandler;
import lisong_mechlab.model.loadout.Loadout;
import lisong_mechlab.model.loadout.LoadoutPart;
import lisong_mechlab.model.loadout.metrics.AlphaStrike;
import lisong_mechlab.model.loadout.metrics.CoolingRatio;
import lisong_mechlab.model.loadout.metrics.GhostHeat;
import lisong_mechlab.model.loadout.metrics.HeatCapacity;
import lisong_mechlab.model.loadout.metrics.HeatDissipation;
import lisong_mechlab.model.loadout.metrics.HeatGeneration;
import lisong_mechlab.model.loadout.metrics.JumpDistance;
import lisong_mechlab.model.loadout.metrics.MaxDPS;
import lisong_mechlab.model.loadout.metrics.MaxSustainedDPS;
import lisong_mechlab.model.loadout.metrics.TimeToOverHeat;
import lisong_mechlab.model.loadout.metrics.TopSpeed;
import lisong_mechlab.util.MessageXBar;
import lisong_mechlab.util.MessageXBar.Message;
import lisong_mechlab.view.ProgramInit;
import lisong_mechlab.view.WeaponSummaryTable;
import lisong_mechlab.view.render.ProgressBarRenderer;

public class LoadoutInfoPanel extends JPanel implements ItemListener, MessageXBar.Reader{
   private static final long            serialVersionUID = 4720126200474042446L;
   private final Loadout                loadout;

   // General pane
   private final JProgressBar           massBar;
   private final JLabel                 massValue        = new JLabel("xxx");
   private final JProgressBar           armorBar;
   private final JLabel                 armorValue       = new JLabel("xxx");
   private final JProgressBar           critslotsBar     = new JProgressBar(0, 5 * 12 + 3 * 6);
   private final JLabel                 critslotsValue   = new JLabel("xxx");
   private final JCheckBox              ferroFibros      = new JCheckBox("Ferro-Fibrous");
   private final JCheckBox              endoSteel        = new JCheckBox("Endo-Steel");
   private final JCheckBox              artemis          = new JCheckBox("Artemis IV");

   // Movement pane
   private final JLabel                 topSpeed         = new JLabel("xxx");
   private final JCheckBox              speedTweak       = new JCheckBox("Speed Tweak");
   private final JLabel                 jumpJets         = new JLabel("xxx");

   // Heat pane
   private final JLabel                 heatsinks        = new JLabel("xxx");
   private final JLabel                 effectiveHS      = new JLabel("xxx");
   private final JLabel                 timeToOverheat   = new JLabel("xxx");
   private final JLabel                 coolingRatio     = new JLabel("xxx");
   private final JCheckBox              doubleHeatSinks  = new JCheckBox("Double Heatsinks");
   private final JCheckBox              coolRun          = new JCheckBox("Cool Run");
   private final JCheckBox              heatContainment  = new JCheckBox("Heat Containment");
   private final JCheckBox              doubleBasics     = new JCheckBox("Double Basics");
   private final JComboBox<Environment> environemnts;

   // Offense pane
   private final JComboBox<String>      range;
   private final JLabel                 alphaStrike      = new JLabel("xxx");
   private final JLabel                 dpsMax           = new JLabel("xxx");
   private final JLabel                 dpsSustained     = new JLabel("xxx");
   private final JCheckBox              fastFire         = new JCheckBox("Fast Fire");
   private final JLabel                 ghostHeat        = new JLabel("xxx");
   private final JTable                 weaponTable;

   // Metrics
   private final TopSpeed               metricTopSpeed;
   private final JumpDistance           metricJumpDistance;
   private final HeatGeneration         metricHeatGeneration;
   private final HeatDissipation        metricHeatDissipation;
   private final HeatCapacity           metricHeatCapacity;
   private final CoolingRatio           metricCoolingRatio;
   private final TimeToOverHeat         metricTimeToOverHeat;
   private final AlphaStrike            metricAlphaStrike;
   private final GhostHeat              metricGhostHeat;

   private final MaxDPS                 metricMaxDPS;
   private final MaxSustainedDPS        metricSustainedDps;
   private transient Boolean            inhibitChanges   = false;
   private final ArtemisHandler         artemisChecker;

   public LoadoutInfoPanel(Loadout aConfiguration, MessageXBar anXBar){
      loadout = aConfiguration;

      metricGhostHeat = new GhostHeat(loadout);
      metricTopSpeed = new TopSpeed(loadout);
      metricJumpDistance = new JumpDistance(loadout);
      metricHeatGeneration = new HeatGeneration(loadout);
      metricHeatDissipation = new HeatDissipation(loadout, null);
      metricHeatCapacity = new HeatCapacity(loadout);
      metricCoolingRatio = new CoolingRatio(metricHeatDissipation, metricHeatGeneration);
      metricTimeToOverHeat = new TimeToOverHeat(metricHeatCapacity, metricHeatDissipation, metricHeatGeneration);
      metricAlphaStrike = new AlphaStrike(loadout);
      metricMaxDPS = new MaxDPS(loadout);
      metricSustainedDps = new MaxSustainedDPS(loadout, metricHeatDissipation);

      artemisChecker = new ArtemisHandler(loadout);

      setLayout(new BoxLayout(this, BoxLayout.PAGE_AXIS));
      anXBar.attach(this);

      Border innerBorder = new EmptyBorder(0, 4, 4, 4);
      // General
      // ----------------------------------------------------------------------
      {
         JPanel general = new JPanel();
         general.setBorder(new CompoundBorder(new TitledBorder(null, "General"), innerBorder));
         add(general);

         JLabel critslotsTxt = new JLabel("Slots:");
         critslotsBar.setUI(new ProgressBarRenderer());
         critslotsTxt.setAlignmentY(CENTER_ALIGNMENT);

         JLabel massTxt = new JLabel("Tons:");
         massBar = new JProgressBar(0, loadout.getChassi().getMassMax());
         massBar.setUI(new ProgressBarRenderer());

         JLabel armorTxt = new JLabel("Armor:");
         armorBar = new JProgressBar(0, loadout.getChassi().getArmorMax());
         armorBar.setUI(new ProgressBarRenderer());

         Insets upgradeInsets = new Insets(0, 0, 0, 0);
         ferroFibros.addItemListener(this);
         ferroFibros.setVerticalTextPosition(SwingConstants.TOP);
         ferroFibros.setMargin(upgradeInsets);
         ferroFibros.setMultiClickThreshhold(100);
         endoSteel.addItemListener(this);
         endoSteel.setVerticalTextPosition(SwingConstants.TOP);
         endoSteel.setMargin(upgradeInsets);
         artemis.addItemListener(this);
         artemis.setVerticalTextPosition(SwingConstants.TOP);
         artemis.setMargin(upgradeInsets);

         Box upgradesBox = Box.createHorizontalBox();
         upgradesBox.add(ferroFibros);
         upgradesBox.add(endoSteel);
         upgradesBox.add(artemis);

         GroupLayout gl_general = new GroupLayout(general);
         gl_general.setAutoCreateGaps(true);

         // @formatter:off
         gl_general.setHorizontalGroup(
            gl_general.createParallelGroup().addGroup(
               gl_general.createSequentialGroup().addGroup(
                  gl_general.createParallelGroup().addComponent(massTxt).addComponent(armorTxt).addComponent(critslotsTxt)
               ).addGroup(
                  gl_general.createParallelGroup().addComponent(massBar).addComponent(armorBar).addComponent(critslotsBar)
               ).addGroup(
                  gl_general.createParallelGroup(GroupLayout.Alignment.TRAILING).addComponent(massValue).addComponent(armorValue).addComponent(critslotsValue)
               )
            ).addComponent(upgradesBox)
         );
      
         gl_general.setVerticalGroup(
            gl_general.createSequentialGroup().addGroup(
               gl_general.createParallelGroup(GroupLayout.Alignment.CENTER).addComponent(massTxt).addComponent(massBar).addComponent(massValue)
            ).addGroup(
               gl_general.createParallelGroup(GroupLayout.Alignment.CENTER).addComponent(armorTxt).addComponent(armorBar).addComponent(armorValue)
            ).addGroup(
               gl_general.createParallelGroup(GroupLayout.Alignment.CENTER).addComponent(critslotsTxt).addComponent(critslotsBar).addComponent(critslotsValue)
            ).addComponent(upgradesBox)
         );
         // @formatter:on

         general.setLayout(gl_general);
      }

      // Mobility
      // ----------------------------------------------------------------------
      {
         JPanel mobility = new JPanel();
         mobility.setBorder(new TitledBorder(null, "Mobility"));
         mobility.setLayout(new BoxLayout(mobility, BoxLayout.PAGE_AXIS));
         mobility.add(Box.createHorizontalGlue());
         add(mobility);

         jumpJets.setAlignmentX(Component.CENTER_ALIGNMENT);
         mobility.add(jumpJets);

         topSpeed.setAlignmentX(Component.CENTER_ALIGNMENT);
         mobility.add(topSpeed);

         speedTweak.setAlignmentX(Component.CENTER_ALIGNMENT);
         mobility.add(speedTweak);
         speedTweak.addItemListener(this);
      }

      // Heat
      // ----------------------------------------------------------------------
      {
         JPanel heat = new JPanel();
         heat.setBorder(new CompoundBorder(new TitledBorder(null, "Heat"), innerBorder));
         heat.setLayout(new BoxLayout(heat, BoxLayout.PAGE_AXIS));
         heat.add(Box.createHorizontalGlue());
         add(heat);

         List<Environment> evs = new ArrayList<>(ProgramInit.ENVIRONMENT_DB.lookupAll());
         evs.add(0, new Environment("neutral", 0.0));
         environemnts = new JComboBox<>(evs.toArray(new Environment[evs.size()]));
         environemnts.addActionListener(new ActionListener(){
            @Override
            public void actionPerformed(ActionEvent aArg0){
               Environment environment = (Environment)environemnts.getSelectedItem();
               metricHeatDissipation.changeEnvironment(environment);
               updateDisplay();
            }
         });
         environemnts.setSelectedIndex(0);
         heat.add(environemnts);

         heatsinks.setAlignmentX(Component.CENTER_ALIGNMENT);
         heat.add(heatsinks);

         effectiveHS.setAlignmentX(Component.CENTER_ALIGNMENT);
         heat.add(effectiveHS);

         timeToOverheat.setAlignmentX(Component.CENTER_ALIGNMENT);
         timeToOverheat.setToolTipText("The number of seconds your mech can go \"All guns a'blazing\" before it overheats, assuming no ghost heat.");
         heat.add(timeToOverheat);

         ghostHeat.setAlignmentX(Component.CENTER_ALIGNMENT);
         ghostHeat.setToolTipText("The amount of extra heat incurred during an alpha strike.");
         heat.add(ghostHeat);

         coolingRatio.setAlignmentX(Component.CENTER_ALIGNMENT);
         heat.add(coolingRatio);

         GridLayout gridLayout = new GridLayout(2, 2);
         JPanel upgrades = new JPanel(gridLayout);

         upgrades.setAlignmentY(Component.CENTER_ALIGNMENT);
         coolRun.setComponentOrientation(ComponentOrientation.RIGHT_TO_LEFT);
         upgrades.add(coolRun);
         upgrades.add(heatContainment);

         doubleHeatSinks.setComponentOrientation(ComponentOrientation.RIGHT_TO_LEFT);
         upgrades.add(doubleHeatSinks);
         upgrades.add(doubleBasics);
         heat.add(upgrades);

         doubleBasics.addItemListener(this);
         heatContainment.addItemListener(this);
         doubleHeatSinks.addItemListener(this);
         coolRun.addItemListener(this);
      }

      // Offense
      // ----------------------------------------------------------------------
      {
         JPanel offence = new JPanel();
         offence.setBorder(new CompoundBorder(new TitledBorder(null, "Offense"), innerBorder));
         offence.setLayout(new BoxLayout(offence, BoxLayout.PAGE_AXIS));
         offence.add(Box.createHorizontalGlue());
         offence.add(Box.createVerticalGlue());
         add(offence);

         JPanel panel = new JPanel();
         panel.add(new JLabel("Range:"));
         panel.setToolTipText("Select the range of engagement that alpha strike, max and sustained DPS will be calculated for. Set this to \"opt\" or \"optimal\" to automatically select your optimal ranges.");
         
         String ranges[] = new String[]{"Optimal", "90", "180", "270", "300", "450","675", "720",  "810", "900", "1080", "1350", "1620", "1980", "2160"}; 
         range = new JComboBox<String>(ranges);
         range.setEditable(true);
         range.setToolTipText(panel.getToolTipText());
         range.addActionListener(new ActionListener(){
            @Override
            public void actionPerformed(ActionEvent aArg0){
               String value = (String)range.getSelectedItem();
               final int r;
               if(value.toLowerCase().contains("opt")){
                  r = -1;
               }else{
                  try{
                     r = Integer.parseInt(value);
                  }catch(NumberFormatException e){
                     JOptionPane.showMessageDialog(LoadoutInfoPanel.this, "Please enter an integer range or \"optimal\" or \"opt\" to select the optimal range automatically.");
                     range.setSelectedIndex(0);
                     return;
                  }
               }
               metricAlphaStrike.changeRange(r);
               metricMaxDPS.changeRange(r);
               metricSustainedDps.changeRange(r);
               updateDisplay();
            }
         });
         panel.add(range);
         panel.add(fastFire);
         fastFire.addItemListener(this);
         fastFire.setAlignmentX(Component.CENTER_ALIGNMENT);
         offence.add(panel);

         alphaStrike.setAlignmentX(Component.CENTER_ALIGNMENT);
         offence.add(alphaStrike);

         dpsMax.setAlignmentX(Component.CENTER_ALIGNMENT);
         offence.add(dpsMax);

         dpsSustained.setAlignmentX(Component.CENTER_ALIGNMENT);
         offence.add(dpsSustained);

         offence.add(Box.createVerticalStrut(5));

         weaponTable = new WeaponSummaryTable(loadout, anXBar);

         JScrollPane weapons = new JScrollPane(weaponTable);
         weapons.setPreferredSize(new Dimension(260, 100));
         offence.add(weapons);
      }

      updateDisplay();
   }

   public void updateDisplay(){
      SwingUtilities.invokeLater(new Runnable(){
         @Override
         public void run(){
            synchronized( inhibitChanges ){

               inhibitChanges = true;

               // General
               // ----------------------------------------------------------------------
               final DecimalFormat df2 = new DecimalFormat("###.##");
               df2.setMinimumFractionDigits(2);

               final DecimalFormat df1 = new DecimalFormat("###.#");
               df1.setMinimumFractionDigits(1);

               final DecimalFormat df0 = new DecimalFormat("###");

               double mass = loadout.getMass();
               massBar.setValue((int)Math.ceil(mass));
               massValue.setText(df2.format(loadout.getChassi().getMassMax() - mass) + " free");
               massBar.setString(df1.format(mass) + " / " + df0.format(loadout.getChassi().getMassMax()));

               armorBar.setValue(loadout.getArmor());
               armorBar.setString(loadout.getArmor() + " / " + loadout.getChassi().getArmorMax());
               armorValue.setText((loadout.getChassi().getArmorMax() - loadout.getArmor()) + " free");

               critslotsBar.setValue(loadout.getNumCriticalSlotsUsed());
               critslotsBar.setString(loadout.getNumCriticalSlotsUsed() + " / " + (12 * 5 + 3 * 6));
               critslotsValue.setText(loadout.getNumCriticalSlotsFree() + " free");

               artemis.setSelected(loadout.getUpgrades().hasArtemis());
               endoSteel.setSelected(loadout.getUpgrades().hasEndoSteel());
               ferroFibros.setSelected(loadout.getUpgrades().hasFerroFibrous());

               {
                  final String esSavedMass = df2.format(loadout.getChassi().getMassMax() * 0.05);
                  if( loadout.getUpgrades().hasEndoSteel() ){
                     endoSteel.setText("<html>Endo-Steel<br>(<span style=\"color: green;\">-" + esSavedMass + "t</span>, "
                                       + "<span style=\"color: red;\">+14s</span>)" + "</html>");
                  }
                  else{
                     endoSteel.setText("<html>Endo-Steel<br>(<span style=\"color: gray;\">-" + esSavedMass + "t</span>, "
                                       + "<span style=\"color: gray;\">+14s</span>)" + "</html>");
                  }
               }

               {
                  final double armorMass = loadout.getArmor() / LoadoutPart.ARMOR_PER_TON;
                  final String ffSavedMass = df2.format(armorMass - armorMass / 1.12);
                  if( loadout.getUpgrades().hasFerroFibrous() ){
                     ferroFibros.setText("<html>Ferro-Fibrous<br>(<span style=\"color: green;\">-" + ffSavedMass + "t</span>, "
                                         + "<span style=\"color: red;\">+14s</span>)" + "</html>");
                  }
                  else{
                     ferroFibros.setText("<html>Ferro-Fibrous<br>(<span style=\"color: gray;\">-" + ffSavedMass + "t</span>, "
                                         + "<span style=\"color: gray;\">+14s</span>)" + "</html>");
                  }
               }

               {
                  final String artemisMass = df0.format(artemisChecker.getAdditionalMass());
                  final int artemisSlots = artemisChecker.getAdditionalSlots();
                  if( loadout.getUpgrades().hasArtemis() ){
                     artemis.setText("<html>Artemis IV<br>(<span style=\"color: red;\">+" + artemisMass + "t</span>, "
                                     + "<span style=\"color: red;\">+" + artemisSlots + "s</span>)" + "</html>");
                  }
                  else{
                     artemis.setText("<html>Artemis IV<br>(<span style=\"color: gray;\">+" + artemisMass + "t</span>, "
                                     + "<span style=\"color: gray;\">+" + artemisSlots + "s</span>)" + "</html>");
                  }
               }

               // Mobility
               // ----------------------------------------------------------------------
               topSpeed.setText("Top speed: " + df2.format(metricTopSpeed.calculate()) + " km/h");
               jumpJets.setText("Jump Jets: " + loadout.getJumpJetCount() + "/" + loadout.getChassi().getMaxJumpJets() + " ("
                                + df2.format(metricJumpDistance.calculate()) + " m)");
               speedTweak.setSelected(loadout.getEfficiencies().hasSpeedTweak());

               // Heat
               // ----------------------------------------------------------------------
               doubleHeatSinks.setSelected(loadout.getUpgrades().hasDoubleHeatSinks());
               coolRun.setSelected(loadout.getEfficiencies().hasCoolRun());
               heatContainment.setSelected(loadout.getEfficiencies().hasHeatContainment());
               if( !coolRun.isSelected() || !heatContainment.isSelected() ){
                  doubleBasics.setSelected(false);
                  doubleBasics.setEnabled(false);
               }
               else{
                  doubleBasics.setEnabled(true);
                  doubleBasics.setSelected(loadout.getEfficiencies().hasDoubleBasics());
               }

               if( loadout.getHeatsinksCount() < 10 ){
                  heatsinks.setForeground(Color.RED);
               }
               else{
                  heatsinks.setForeground(effectiveHS.getForeground());
               }
               heatsinks.setText("Heatsinks: " + loadout.getHeatsinksCount());
               effectiveHS.setText("Heat capacity: " + df2.format(metricHeatCapacity.calculate()));
               double ghostHeatPenalty = metricGhostHeat.calculate();
               if( ghostHeatPenalty > 0 )
                  ghostHeat.setForeground(Color.RED);
               else
                  ghostHeat.setForeground(alphaStrike.getForeground());
               ghostHeat.setText("Ghost heat: " + df2.format(ghostHeatPenalty));
               timeToOverheat.setText("Seconds to Overheat: " + df2.format(metricTimeToOverHeat.calculate()));
               coolingRatio.setText("Cooling efficiency: " + df0.format(metricCoolingRatio.calculate() * 100.0) + "%");

               // Offense
               // ----------------------------------------------------------------------
               fastFire.setSelected(loadout.getEfficiencies().hasFastFire());
               alphaStrike.setText("Alpha strike: " + df2.format(metricAlphaStrike.calculate()) + " @ " + df0.format(metricAlphaStrike.getRange())
                                   + "m");
               dpsMax.setText("Max DPS: " + df2.format(metricMaxDPS.calculate()) + " @ " + df0.format(metricMaxDPS.getRange()) + "m");
               dpsSustained.setText("Max Sustained DPS: " + df2.format(metricSustainedDps.calculate()) + " @ "
                                    + df0.format(metricSustainedDps.getRange()) + "m");

               inhibitChanges = false;
            }
         }
      });

   }

   @Override
   public void itemStateChanged(ItemEvent anEvent){
      synchronized( inhibitChanges ){
         if( inhibitChanges )
            return;
      }

      JCheckBox source = (JCheckBox)anEvent.getSource();

      try{
         if( source == artemis ){
            try{
               artemisChecker.checkLoadoutStillValid();
               artemisChecker.checkArtemisAdditionLegal();
               loadout.getUpgrades().setArtemis(anEvent.getStateChange() == ItemEvent.SELECTED);
            }
            catch( IllegalArgumentException e ){
               throw e;
            }

            updateDisplay();

         }
         else if( source == endoSteel ){
            loadout.getUpgrades().setEndoSteel(anEvent.getStateChange() == ItemEvent.SELECTED);
         }
         else if( source == ferroFibros ){
            loadout.getUpgrades().setFerroFibrous(anEvent.getStateChange() == ItemEvent.SELECTED);
         }
         else if( source == speedTweak ){
            loadout.getEfficiencies().setSpeedTweak(anEvent.getStateChange() == ItemEvent.SELECTED);
         }
         else if( source == doubleHeatSinks ){
            loadout.getUpgrades().setDoubleHeatSinks(anEvent.getStateChange() == ItemEvent.SELECTED);
         }
         else if( source == coolRun ){
            loadout.getEfficiencies().setCoolRun(anEvent.getStateChange() == ItemEvent.SELECTED);
         }
         else if( source == heatContainment ){
            loadout.getEfficiencies().setHeatContainment(anEvent.getStateChange() == ItemEvent.SELECTED);
         }
         else if( source == doubleBasics ){
            loadout.getEfficiencies().setDoubleBasics(anEvent.getStateChange() == ItemEvent.SELECTED);
         }
         else if( source == fastFire ){
            loadout.getEfficiencies().setFastFire(anEvent.getStateChange() == ItemEvent.SELECTED);
         }
         else{
            throw new RuntimeException("Unknown source control!");
         }
      }
      catch( IllegalArgumentException e ){
         JOptionPane.showMessageDialog(ProgramInit.lsml(), e.getMessage());
      }
      catch( RuntimeException e ){
         JOptionPane.showMessageDialog(ProgramInit.lsml(), "Error while changing upgrades or efficiency!: " + e.getMessage());
      }
   }

   @Override
   public void receive(Message aMsg){
      if( aMsg.isForMe(loadout) ){
         updateDisplay();
      }
   }

   public MaxSustainedDPS getMaxSustainedDPSMetric(){
      return metricSustainedDps;
   }
}
