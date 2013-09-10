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

import lisong_mechlab.model.MessageXBar;
import lisong_mechlab.model.MessageXBar.Message;
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

public class LoadoutInfoPanel extends JPanel implements ItemListener, MessageXBar.Reader{
   private static final long        serialVersionUID = 4720126200474042446L;
   private final Loadout            loadout;
   private final MessageXBar        xBar;

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
   private final TotalAmmoSupply    metricTotalAmmoSupply;
   private final AmmoTableDataModel anAmmoTableDataModel;
   private transient Boolean        inhibitChanges   = false;

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
      metricTotalAmmoSupply = new TotalAmmoSupply(loadout);

      anAmmoTableDataModel = new AmmoTableDataModel(loadout, anXBar);

      setLayout(new BoxLayout(this, BoxLayout.PAGE_AXIS));
      this.xBar = anXBar;

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
         endoSteel.addItemListener(this);
         artemis.addItemListener(this);

         Box upgradesBox = Box.createHorizontalBox();
         upgradesBox.add(Box.createHorizontalGlue());
         upgradesBox.add(ferroFibros);
         upgradesBox.add(endoSteel);
         upgradesBox.add(artemis);
         upgradesBox.add(Box.createHorizontalGlue());

         GroupLayout gl_general = new GroupLayout(general);
         gl_general.setAutoCreateContainerGaps(true);
         gl_general.setAutoCreateGaps(true);

         // @formatter:off
         gl_general.setHorizontalGroup(
            gl_general.createParallelGroup().addGroup(
               gl_general.createSequentialGroup().addGroup(
                  gl_general.createParallelGroup().addComponent(massTxt).addComponent(armorTxt).addComponent(critslotsTxt)
               ).addGroup(
                  gl_general.createParallelGroup().addComponent(massBar).addComponent(armorBar).addComponent(critslotsBar)
               ).addGroup(
                  gl_general.createParallelGroup().addComponent(massValue).addComponent(armorValue).addComponent(critslotsValue)
               )
            ).addComponent(upgradesBox)
         );
      
         gl_general.setVerticalGroup(
            gl_general.createSequentialGroup().addGroup(
               gl_general.createParallelGroup().addComponent(massTxt).addComponent(massBar).addComponent(massValue)
            ).addGroup(
               gl_general.createParallelGroup().addComponent(armorTxt).addComponent(armorBar).addComponent(armorValue)
            ).addGroup(
               gl_general.createParallelGroup().addComponent(critslotsTxt).addComponent(critslotsBar).addComponent(critslotsValue)
            ).addComponent(upgradesBox)
         );
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
         horizontalBox.add(heatContainment);
         horizontalBox.add(doubleBasics);
         heat.add(horizontalBox);
         doubleHeatSinks.addItemListener(this);
         coolRun.addItemListener(this);
         heatContainment.addItemListener(this);
         doubleBasics.addItemListener(this);

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
         JTableHeader header =  totalAmmoSupply.getTableHeader();
         header.setDefaultRenderer(new HeaderRenderer(totalAmmoSupply));
//          totalAmmoSupply.updateUI();
       
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
private static class HeaderRenderer implements TableCellRenderer {

   DefaultTableCellRenderer renderer;

   public HeaderRenderer(JTable table) {
      if(table.getTableHeader().getDefaultRenderer() instanceof DefaultTableCellRenderer){
         renderer = (DefaultTableCellRenderer)
               table.getTableHeader().getDefaultRenderer();
           renderer.setHorizontalAlignment(JLabel.CENTER);
      }
      
   }

   @Override
   public Component getTableCellRendererComponent(
       JTable table, Object value, boolean isSelected,
       boolean hasFocus, int row, int col) {
       return renderer.getTableCellRendererComponent(
           table, value, isSelected, hasFocus, row, col);
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
               coolingRatio.setText("Cooling efficiency: " + df.format(metricCoolingRatio.calculate()));

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
            loadout.getUpgrades().setArtemis(anEvent.getStateChange() == ItemEvent.SELECTED);
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
            throw new RuntimeException("Unkown source control!");
         }
      }
      catch( RuntimeException e ){
         JOptionPane.showMessageDialog(this, "Error while changing upgrades or efficiency!: " + e.getStackTrace());
      }
   }

   @Override
   public void receive(Message aMsg){
      updateDisplay(); // TODO be a bit more selective when to update
   }
}
