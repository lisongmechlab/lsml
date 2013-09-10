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
import lisong_mechlab.model.loadout.Statistics;
import lisong_mechlab.model.loadout.metrics.AlphaStrike;
import lisong_mechlab.model.loadout.metrics.HeatDissipation;
import lisong_mechlab.model.loadout.metrics.MaxDPS;
import lisong_mechlab.model.loadout.metrics.MaxSustainedDPS;
import lisong_mechlab.model.loadout.metrics.TopSpeed;
import lisong_mechlab.model.loadout.metrics.TotalAmmoSupply;
import lisong_mechlab.model.tables.AmmoTableDataModel;

public class LoadoutInfoPanel extends JPanel implements ItemListener, MessageXBar.Reader{
   private static final long        serialVersionUID = 4720126200474042446L;
   final private Loadout            loadout;
   private MessageXBar              xBar;

   final private JProgressBar       massBar;
   final private JLabel             massValue        = new JLabel("xxx");
   final private JProgressBar       armorBar;
   final private JLabel             armorValue       = new JLabel("xxx");
   final private JProgressBar       critslotsBar     = new JProgressBar(0, 5 * 12 + 3 * 6);
   final private JLabel             critslotsValue   = new JLabel("xxx");
   final private JCheckBox          ferroFibros      = new JCheckBox("Ferro-Fibrous");
   final private JCheckBox          endoSteel        = new JCheckBox("Endo-Steel");
   final private JCheckBox          artemis          = new JCheckBox("Artemis IV");

   final private JLabel             heatsinks        = new JLabel("xxx");
   final private JLabel             effectiveHS      = new JLabel("xxx");
   final private JLabel             timeToOverheat   = new JLabel("xxx");
   final private JLabel             coolingRatio     = new JLabel("xxx");
   final private JCheckBox          doubleHeatSinks  = new JCheckBox("Double Heatsinks");
   final private JCheckBox          coolRun          = new JCheckBox("Cool Run");
   final private JCheckBox          heatContainment  = new JCheckBox("Heat Containment");
   final private JCheckBox          doubleBasics     = new JCheckBox("Double Basics");

   final private JLabel             alphaStrike      = new JLabel("xxx");
   final private JLabel             dpsMax           = new JLabel("xxx");
   final private JLabel             dpsSustained     = new JLabel("xxx");
   private JTable                   totalAmmoSupply  = new JTable();

   final private JLabel             jumpJets         = new JLabel("xxx");
   final private JLabel             topSpeed         = new JLabel("xxx");
   final private JCheckBox          speedTweak       = new JCheckBox("Speed Tweak");

   final private Statistics         statistics;
   final private HeatDissipation    metricHeatDissipation;
   final private AlphaStrike        metricAlphaStrike;
   final private MaxDPS             metricMaxDPS;
   final private MaxSustainedDPS    metricSustainedDps;
   final private TotalAmmoSupply    metricTotalAmmoSupply;
   final private AmmoTableDataModel anAmmoTableDataModel;
   final private TopSpeed     topSpeedMetric;
   transient private Boolean        inhibitChanges   = false;

   public LoadoutInfoPanel(Loadout aConfiguration, MessageXBar anXBar){
      loadout = aConfiguration;
      statistics = new Statistics(loadout);
      metricAlphaStrike = new AlphaStrike(loadout);
      metricMaxDPS = new MaxDPS(loadout);
      metricHeatDissipation = new HeatDissipation(loadout);
      metricSustainedDps = new MaxSustainedDPS(loadout, metricHeatDissipation);
      metricTotalAmmoSupply = new TotalAmmoSupply(loadout);
      anAmmoTableDataModel = new AmmoTableDataModel(loadout, anXBar);
      topSpeedMetric = new TopSpeed(loadout);
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
//       
         ammo.setLayout(new BorderLayout()); // unless already there
         ammo.add(totalAmmoSupply, BorderLayout.CENTER);
         ammo.add(totalAmmoSupply.getTableHeader(), BorderLayout.NORTH);
         ammo.setBorder(new CompoundBorder(new TitledBorder(null, "Weapons"), new EmptyBorder(5, 5, 5, 5)));
         add(ammo);

      }

      add(Box.createVerticalGlue());

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
               topSpeed.setText("Top speed: " + df.format(topSpeedMetric.calculate()) + " km/h");
               jumpJets.setText("Jump Jets: " + loadout.getJumpJetCount() + "/" + loadout.getChassi().getMaxJumpJets() + " ("
                                + df.format(statistics.getJumpDistance()) + " m)");
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
               effectiveHS.setText("Heat capacity: " + df.format(statistics.getHeatCapacity()));
               timeToOverheat.setText("Seconds to Overheat: " + df.format(statistics.getTimeToOverHeat()));
               coolingRatio.setText("Cooling efficiency: " + df.format(statistics.getCoolingRatio()));

               // Offense
               // ----------------------------------------------------------------------
               alphaStrike.setText("Alpha strike: " + df.format(metricAlphaStrike.calculate()));
               dpsMax.setText("Max DPS: " + df.format(metricMaxDPS.calculate()));
               dpsSustained.setText("Max Sustained DPS: " + df.format(metricSustainedDps.calculate()));

               metricTotalAmmoSupply.calculate();

               AmmoTableDataModel anAmmoTableDataModel1 = new AmmoTableDataModel(loadout, xBar);
               anAmmoTableDataModel1.fillInData();
               totalAmmoSupply.setModel(anAmmoTableDataModel1);
               
               
              JTableHeader header =  totalAmmoSupply.getTableHeader();
              header.setDefaultRenderer(new HeaderRenderer(totalAmmoSupply));
               totalAmmoSupply.updateUI();

               // Summary
               // ----------------------------------------------------------------------

               inhibitChanges = false;
            }
         }
      });

   }
   
//    TODO sets formatting correctly but throws exception on system exit need to 
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
         JOptionPane.showMessageDialog(this, e.getMessage());
      }
   }

   @Override
   public void receive(Message aMsg){
      updateDisplay(); // TODO be a bit more selective when to update
   }
}
