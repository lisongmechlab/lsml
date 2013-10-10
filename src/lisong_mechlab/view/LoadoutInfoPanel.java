package lisong_mechlab.view;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.text.DecimalFormat;

import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.GroupLayout;
import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JProgressBar;
import javax.swing.JTable;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;
import javax.swing.border.CompoundBorder;
import javax.swing.border.EmptyBorder;
import javax.swing.border.TitledBorder;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.JTableHeader;
import javax.swing.table.TableCellRenderer;

import lisong_mechlab.model.loadout.ArtemisHandler;
import lisong_mechlab.model.loadout.Loadout;
import lisong_mechlab.model.loadout.metrics.AlphaStrike;
import lisong_mechlab.model.loadout.metrics.CoolingRatio;
import lisong_mechlab.model.loadout.metrics.HeatCapacity;
import lisong_mechlab.model.loadout.metrics.HeatDissipation;
import lisong_mechlab.model.loadout.metrics.HeatGeneration;
import lisong_mechlab.model.loadout.metrics.JumpDistance;
import lisong_mechlab.model.loadout.metrics.MaxDPS;
import lisong_mechlab.model.loadout.metrics.MaxSustainedDPS;
import lisong_mechlab.model.loadout.metrics.TimeToOverHeat;
import lisong_mechlab.model.loadout.metrics.TopSpeed;
import lisong_mechlab.model.loadout.metrics.TotalAmmoSupply;
import lisong_mechlab.model.tables.AmmoTableDataModel;
import lisong_mechlab.util.MessageXBar;
import lisong_mechlab.util.MessageXBar.Message;
import javax.swing.GroupLayout.Alignment;
import javax.swing.LayoutStyle.ComponentPlacement;

public class LoadoutInfoPanel extends JPanel implements ItemListener, MessageXBar.Reader{
   private static final long        serialVersionUID = 4720126200474042446L;
   private final Loadout            loadout;
   private final JProgressBar       massBar;
   private final JLabel             massValue        = new JLabel("xxx");
   private final JProgressBar       armorBar;
   private final JLabel             armorValue       = new JLabel("xxx");
   private final JProgressBar       critslotsBar     = new JProgressBar(0, 5 * 12 + 3 * 6);
   private final JLabel             critslotsValue   = new JLabel("xxx");
   private final JCheckBox          ferroFibros      = new JCheckBox("Ferro-Fibrous");
   private final JCheckBox          endoSteel        = new JCheckBox("Endo-Steel");
   private final JCheckBox          artemis          = new JCheckBox("Artemis IV");

   private final JLabel             heatsinks        = new JLabel("xxx");
   private final JLabel             effectiveHS      = new JLabel("xxx");
   private final JLabel             timeToOverheat   = new JLabel("xxx");
   private final JLabel             coolingRatio     = new JLabel("xxx");
   private final JCheckBox          doubleHeatSinks  = new JCheckBox("Double Heatsinks");
   private final JCheckBox          coolRun          = new JCheckBox("Cool Run");
   private final JCheckBox          heatContainment  = new JCheckBox("Heat Containment");
   private final JCheckBox          doubleBasics     = new JCheckBox("Double Basics");

   private final JLabel             alphaStrike      = new JLabel("xxx");
   private final JLabel             dpsMax           = new JLabel("xxx");
   private final JLabel             dpsSustained     = new JLabel("xxx");
   private final JTable             totalAmmoSupply;

   private final JLabel             jumpJets         = new JLabel("xxx");
   private final JLabel             topSpeed         = new JLabel("xxx");
   private final JCheckBox          speedTweak       = new JCheckBox("Speed Tweak");

   // Metrics
   private final TopSpeed           metricTopSpeed;
   private final JumpDistance       metricJumpDistance;
   private final HeatGeneration     metricHeatGeneration;
   private final HeatDissipation    metricHeatDissipation;
   private final HeatCapacity       metricHeatCapacity;
   private final CoolingRatio       metricCoolingRatio;
   private final TimeToOverHeat     metricTimeToOverHeat;
   private final AlphaStrike        metricAlphaStrike;
   private final MaxDPS             metricMaxDPS;
   private final MaxSustainedDPS    metricSustainedDps;
   private final AmmoTableDataModel anAmmoTableDataModel;
   private transient Boolean        inhibitChanges   = false;
   private final Box horizontalBox_1 = Box.createHorizontalBox();
   private JLabel FFSpaceRequired;
   private final JLabel label = new JLabel("- Space Change:");
   private final JLabel ESSpaceRequired = new JLabel("0");
   private final JLabel label_1 = new JLabel("- Space Change:");
   private final JLabel ASpaceRequired = new JLabel("0.0");
   private final JLabel lblNewLabel = new JLabel("  - Slots Change:");
   private final JLabel FFSlotsChange = new JLabel("0");
   private final JLabel label_2 = new JLabel("  - Slots Change:");
   private final JLabel label_3 = new JLabel("14");
   private final JLabel label_4 = new JLabel("  - Slots Change:");
   private final JLabel ASlotsChange = new JLabel("14");

   public LoadoutInfoPanel(Loadout aConfiguration, MessageXBar anXBar){
      loadout = aConfiguration;

      metricTopSpeed = new TopSpeed(loadout);
      metricJumpDistance = new JumpDistance(loadout);
      metricHeatGeneration = new HeatGeneration(loadout);
      metricHeatDissipation = new HeatDissipation(loadout);
      metricHeatCapacity = new HeatCapacity(loadout);
      metricCoolingRatio = new CoolingRatio(metricHeatDissipation, metricHeatGeneration);
      metricTimeToOverHeat = new TimeToOverHeat(metricHeatCapacity, metricHeatDissipation, metricHeatGeneration);
      metricAlphaStrike = new AlphaStrike(loadout);
      metricMaxDPS = new MaxDPS(loadout);
      metricSustainedDps = new MaxSustainedDPS(loadout, metricHeatDissipation);
      new TotalAmmoSupply(loadout);

      anAmmoTableDataModel = new AmmoTableDataModel(loadout, anXBar);

      setLayout(new BoxLayout(this, BoxLayout.PAGE_AXIS));
      anXBar.attach(this);

      // General
      // ----------------------------------------------------------------------
      {
         JPanel general = new JPanel();
         general.setBorder(new CompoundBorder(new TitledBorder(null, "General"), new EmptyBorder(5, 5, 5, 5)));
         add(general);

         JLabel critslotsTxt = new JLabel("Critical Slots:");

         JLabel massTxt = new JLabel("Tonnage:");
         massBar = new JProgressBar(0, loadout.getChassi().getMassMax());

         JLabel armorTxt = new JLabel("Armor:");
         armorBar = new JProgressBar(0, loadout.getChassi().getArmorMax());

         ferroFibros.addItemListener(this);

         Box upgradesBox = Box.createHorizontalBox();
         upgradesBox.add(ferroFibros);
         
         Box horizontalBox = Box.createHorizontalBox();
         
         Box horizontalBox_2 = Box.createHorizontalBox();

         GroupLayout gl_general = new GroupLayout(general);
         gl_general.setHorizontalGroup(
            gl_general.createParallelGroup(Alignment.LEADING)
               .addGroup(gl_general.createSequentialGroup()
                  .addGroup(gl_general.createParallelGroup(Alignment.LEADING)
                     .addComponent(massTxt)
                     .addComponent(armorTxt)
                     .addComponent(critslotsTxt))
                  .addGroup(gl_general.createParallelGroup(Alignment.LEADING)
                     .addComponent(massBar, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
                     .addComponent(armorBar, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
                     .addComponent(critslotsBar, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE))
                  .addGroup(gl_general.createParallelGroup(Alignment.LEADING)
                     .addComponent(massValue)
                     .addComponent(armorValue)
                     .addComponent(critslotsValue))
                  .addContainerGap(56, Short.MAX_VALUE))
               .addGroup(gl_general.createSequentialGroup()
                  .addComponent(upgradesBox, GroupLayout.DEFAULT_SIZE, 308, Short.MAX_VALUE)
                  .addContainerGap())
               .addGroup(Alignment.TRAILING, gl_general.createSequentialGroup()
                  .addContainerGap()
                  .addGroup(gl_general.createParallelGroup(Alignment.TRAILING)
                     .addComponent(horizontalBox_2, Alignment.LEADING, GroupLayout.DEFAULT_SIZE, 308, Short.MAX_VALUE)
                     .addComponent(horizontalBox, Alignment.LEADING, GroupLayout.DEFAULT_SIZE, 308, Short.MAX_VALUE))
                  .addContainerGap())
         );
         gl_general.setVerticalGroup(
            gl_general.createParallelGroup(Alignment.LEADING)
               .addGroup(gl_general.createSequentialGroup()
                  .addGroup(gl_general.createParallelGroup(Alignment.LEADING)
                     .addComponent(massTxt)
                     .addComponent(massBar, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
                     .addComponent(massValue))
                  .addGroup(gl_general.createParallelGroup(Alignment.LEADING)
                     .addComponent(armorTxt)
                     .addComponent(armorBar, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
                     .addComponent(armorValue))
                  .addGroup(gl_general.createParallelGroup(Alignment.LEADING)
                     .addComponent(critslotsTxt)
                     .addComponent(critslotsBar, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
                     .addComponent(critslotsValue))
                  .addPreferredGap(ComponentPlacement.RELATED)
                  .addComponent(upgradesBox, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
                  .addPreferredGap(ComponentPlacement.RELATED)
                  .addComponent(horizontalBox, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
                  .addPreferredGap(ComponentPlacement.RELATED)
                  .addComponent(horizontalBox_2, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
                  .addContainerGap(GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
         );
         
         JLabel lblspaceRequired = new JLabel("- Space Change:");
         upgradesBox.add(lblspaceRequired);
         
         FFSpaceRequired = new JLabel("0");
         upgradesBox.add(FFSpaceRequired);
         
         upgradesBox.add(lblNewLabel);
         
         upgradesBox.add(FFSlotsChange);
         horizontalBox_2.add(artemis);
         
         horizontalBox_2.add(label_1);
         
         horizontalBox_2.add(ASpaceRequired);
         
         horizontalBox_2.add(label_4);
         
         horizontalBox_2.add(ASlotsChange);
         artemis.addItemListener(this);
         horizontalBox.add(endoSteel);
         
         horizontalBox.add(label);
         
         horizontalBox.add(ESSpaceRequired);
         
         horizontalBox.add(label_2);
         
         horizontalBox.add(label_3);
         endoSteel.addItemListener(this);
         gl_general.setAutoCreateContainerGaps(true);
         gl_general.setAutoCreateGaps(true);
         // @formatter:on

         general.setLayout(gl_general);
      }

      // Mobility
      // ----------------------------------------------------------------------
      {
         JPanel mobility = new JPanel();
         mobility.setBorder(new CompoundBorder(new TitledBorder(null, "Mobility"), new EmptyBorder(5, 5, 5, 5)));
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
         heat.setBorder(new CompoundBorder(new TitledBorder(null, "Heat"), new EmptyBorder(5, 5, 5, 5)));
         heat.setLayout(new BoxLayout(heat, BoxLayout.PAGE_AXIS));
         heat.add(Box.createHorizontalGlue());
         add(heat);

         heatsinks.setAlignmentX(Component.CENTER_ALIGNMENT);
         heat.add(heatsinks);

         effectiveHS.setAlignmentX(Component.CENTER_ALIGNMENT);
         heat.add(effectiveHS);

         timeToOverheat.setAlignmentX(Component.CENTER_ALIGNMENT);
         heat.add(timeToOverheat);

         coolingRatio.setAlignmentX(Component.CENTER_ALIGNMENT);
         heat.add(coolingRatio);

         Box horizontalBox = Box.createHorizontalBox();
         horizontalBox.setAlignmentY(Component.CENTER_ALIGNMENT);
         horizontalBox.add(doubleHeatSinks);
         horizontalBox.add(coolRun);
         heat.add(horizontalBox);
         
         heat.add(horizontalBox_1);
         horizontalBox_1.add(heatContainment);
         horizontalBox_1.add(doubleBasics);
         doubleBasics.addItemListener(this);
         heatContainment.addItemListener(this);
         doubleHeatSinks.addItemListener(this);
         coolRun.addItemListener(this);

      }

      // Offense
      // ----------------------------------------------------------------------
      {
         JPanel offence = new JPanel();
         offence.setBorder(new CompoundBorder(new TitledBorder(null, "Offense"), new EmptyBorder(5, 5, 5, 5)));
         offence.setLayout(new BoxLayout(offence, BoxLayout.PAGE_AXIS));
         offence.add(Box.createHorizontalGlue());
         add(offence);

         alphaStrike.setAlignmentX(Component.CENTER_ALIGNMENT);
         offence.add(alphaStrike);

         dpsMax.setAlignmentX(Component.CENTER_ALIGNMENT);
         offence.add(dpsMax);

         dpsSustained.setAlignmentX(Component.CENTER_ALIGNMENT);
         offence.add(dpsSustained);
      }
      // Ammo
      {

         JPanel ammo = new JPanel();
         totalAmmoSupply = new JTable(anAmmoTableDataModel);

         totalAmmoSupply.setModel(anAmmoTableDataModel);
         JTableHeader header = totalAmmoSupply.getTableHeader();
         header.setDefaultRenderer(new HeaderRenderer(totalAmmoSupply));
         // totalAmmoSupply.updateUI();

         ammo.setLayout(new BorderLayout()); // unless already there
         ammo.add(totalAmmoSupply, BorderLayout.CENTER);
         ammo.add(totalAmmoSupply.getTableHeader(), BorderLayout.NORTH);
         ammo.setBorder(new CompoundBorder(new TitledBorder(null, "Weapons"), new EmptyBorder(5, 5, 5, 5)));
         add(ammo);

      }

      add(Box.createVerticalGlue());

      updateDisplay();
   }

   // TODO sets formatting correctly but throws exception on system exit need to
   // FIXME: Move this somewhere else.
   private static class HeaderRenderer implements TableCellRenderer{

      DefaultTableCellRenderer renderer;

      public HeaderRenderer(JTable table){
         if( table.getTableHeader().getDefaultRenderer() instanceof DefaultTableCellRenderer ){
            renderer = (DefaultTableCellRenderer)table.getTableHeader().getDefaultRenderer();
            renderer.setHorizontalAlignment(SwingConstants.CENTER);
         }

      }

      @Override
      public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int col){
         return renderer.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, col);
      }
   }

   public void updateDisplay(){
      SwingUtilities.invokeLater(new Runnable(){

         @Override
         public void run(){
            synchronized( inhibitChanges ){

               inhibitChanges = true;

               // General
               // ----------------------------------------------------------------------
               final DecimalFormat df = new DecimalFormat("#.##");
               df.setMinimumFractionDigits(2);
               
               final DecimalFormat dfshort = new DecimalFormat("#");

               double mass = loadout.getMass();
               massBar.setValue((int)Math.ceil(mass));
               massValue.setText(df.format(mass) + " (" + df.format(loadout.getChassi().getMassMax() - mass) + " free)");

               armorBar.setValue(loadout.getArmor());
               armorValue.setText(loadout.getArmor() + " / " + loadout.getChassi().getArmorMax());

               critslotsBar.setValue(loadout.getNumCriticalSlotsUsed());
               critslotsValue.setText(loadout.getNumCriticalSlotsFree() + " free");

               artemis.setSelected(loadout.getUpgrades().hasArtemis());
               endoSteel.setSelected(loadout.getUpgrades().hasEndoSteel());
               ferroFibros.setSelected(loadout.getUpgrades().hasFerroFibrous());
               int standardArmorSpace = loadout.getArmor()/32;
               double FFArmorSpace = (loadout.getArmor()/32) /1.12;
               FFSpaceRequired.setText("" + (df.format(standardArmorSpace - FFArmorSpace)));
               FFSlotsChange.setText("14");
               ESSpaceRequired.setText("" + loadout.getChassi().getMassMax() * 0.05);
               
               ASpaceRequired.setText("" + new ArtemisHandler(loadout).getAdditionalMass());
               ASlotsChange.setText("" + new ArtemisHandler(loadout).getAdditionalSlots());

               // Mobility
               // ----------------------------------------------------------------------
               topSpeed.setText("Top speed: " + df.format(metricTopSpeed.calculate()) + " km/h");
               jumpJets.setText("Jump Jets: " + loadout.getJumpJetCount() + "/" + loadout.getChassi().getMaxJumpJets() + " ("
                                + df.format(metricJumpDistance.calculate()) + " m)");
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

               heatsinks.setText("Heatsinks: " + loadout.getHeatsinksCount());
               effectiveHS.setText("Heat capacity: " + df.format(metricHeatCapacity.calculate()));
               timeToOverheat.setText("Seconds to Overheat: " + df.format(metricTimeToOverHeat.calculate()));
               coolingRatio.setText("Cooling efficiency: " + dfshort.format(metricCoolingRatio.calculate()*100.0)+"%");

               // Offense
               // ----------------------------------------------------------------------
               alphaStrike.setText("Alpha strike: " + df.format(metricAlphaStrike.calculate()));
               dpsMax.setText("Max DPS: " + df.format(metricMaxDPS.calculate()));
               dpsSustained.setText("Max Sustained DPS: " + df.format(metricSustainedDps.calculate()));

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
            ArtemisHandler artemisChecker = new ArtemisHandler(loadout);
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
         else{
            throw new RuntimeException("Unknown source control!");
         }
      }
      catch( IllegalArgumentException e ){
         JOptionPane.showMessageDialog(this, e.getMessage());
      }
      catch( RuntimeException e ){
         JOptionPane.showMessageDialog(this, "Error while changing upgrades or efficiency!: " + e.getMessage());
      }
   }

   @Override
   public void receive(Message aMsg){
      if( aMsg.isForMe(loadout) ){
         updateDisplay();
      }
   }
}
