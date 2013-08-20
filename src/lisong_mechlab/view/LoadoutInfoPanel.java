package lisong_mechlab.view;

import java.awt.Component;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.text.DecimalFormat;

import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.GroupLayout;
import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JProgressBar;
import javax.swing.SwingUtilities;
import javax.swing.border.CompoundBorder;
import javax.swing.border.EmptyBorder;
import javax.swing.border.TitledBorder;

import lisong_mechlab.model.MessageXBar;
import lisong_mechlab.model.MessageXBar.Message;
import lisong_mechlab.model.loadout.Loadout;
import lisong_mechlab.model.loadout.Statistics;

import com.jgoodies.forms.factories.DefaultComponentFactory;

public class LoadoutInfoPanel extends JPanel implements ItemListener, MessageXBar.Reader{
   private static final long  serialVersionUID = 4720126200474042446L;
   final private Loadout      configuration;

   final private JProgressBar massBar;
   final private JLabel       massValue        = new JLabel("xxx");
   final private JProgressBar armorBar;
   final private JLabel       armorValue       = new JLabel("xxx");
   final private JProgressBar critslotsBar     = new JProgressBar(0, 5 * 12 + 3 * 6);
   final private JLabel       critslotsValue   = new JLabel("xxx");
   final private JCheckBox    ferroFibros      = new JCheckBox("Ferro-Fibrous");
   final private JCheckBox    endoSteel        = new JCheckBox("Endo-Steel");
   final private JCheckBox    artemis          = new JCheckBox("Artemis IV");

   final private JLabel       heatsinks        = new JLabel("xxx");
   final private JLabel       effectiveHS      = new JLabel("xxx");
   final private JLabel       timeToOverheat   = new JLabel("xxx");
   final private JLabel       coolingRatio     = new JLabel("xxx");
   final private JCheckBox    doubleHeatSinks  = new JCheckBox("Double Heatsinks");
   final private JCheckBox    coolRun          = new JCheckBox("Cool Run");
   final private JCheckBox    heatContainment  = new JCheckBox("Heat Containment");
   final private JCheckBox    doubleBasics     = new JCheckBox("Double Basics");

   final private JLabel       alphaStrike      = new JLabel("xxx");
   final private JLabel       dpsMax           = new JLabel("xxx");
   final private JLabel       dpsSustained     = new JLabel("xxx");

   final private JLabel       jumpDistance     = new JLabel("xxx");
   final private JLabel       topSpeed         = new JLabel("xxx");
   final private JCheckBox    speedTweak       = new JCheckBox("Speed Tweak");

   final private JLabel       rating;

   final private Statistics   statistics;

   public LoadoutInfoPanel(Loadout aConfiguration, MessageXBar anXBar){
      configuration = aConfiguration;
      statistics = new Statistics(configuration);
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
         massBar = new JProgressBar(0, configuration.getChassi().getMassMax());

         JLabel armorTxt = new JLabel("Armor:");
         armorBar = new JProgressBar(0, configuration.getChassi().getArmorMax());

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

         jumpDistance.setAlignmentX(Component.CENTER_ALIGNMENT);
         mobility.add(jumpDistance);

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

      // Summary
      // ----------------------------------------------------------------------
      {
         JPanel summary = new JPanel();
         summary.setBorder(new CompoundBorder(new TitledBorder(null, "Summary"), new EmptyBorder(5, 5, 5, 5)));
         add(summary);

         rating = DefaultComponentFactory.getInstance().createLabel("Battle Rating: 5123");
         summary.add(rating);
      }

      add(Box.createVerticalGlue());

      updateDisplay();
   }

   public void updateDisplay(){
      SwingUtilities.invokeLater(new Runnable(){
         @Override
         public void run(){
            // General
            // ----------------------------------------------------------------------
            final DecimalFormat df = new DecimalFormat("#.##");
            df.setMinimumFractionDigits(2);

            double mass = configuration.getMass();
            massBar.setValue((int)Math.ceil(mass));
            massValue.setText(df.format(mass) + " (" + df.format(configuration.getChassi().getMassMax() - mass) + " free)");

            armorBar.setValue(configuration.getArmor());
            armorValue.setText(configuration.getArmor() + " / " + configuration.getChassi().getArmorMax());

            critslotsBar.setValue(configuration.getNumCriticalSlotsUsed());
            critslotsValue.setText(configuration.getNumCriticalSlotsFree() + " free");

            artemis.setSelected(configuration.getUpgrades().hasArtemis());
            endoSteel.setSelected(configuration.getUpgrades().hasEndoSteel());
            ferroFibros.setSelected(configuration.getUpgrades().hasFerroFibrous());

            // Mobility
            // ----------------------------------------------------------------------
            topSpeed.setText("Top speed: " + df.format(statistics.getTopSpeed()) + " km/h");
            jumpDistance.setText("Jump distance: " + df.format(statistics.getJumpDistance()) + " m");
            speedTweak.setSelected(configuration.getEfficiencies().hasSpeedTweak());

            // Heat
            // ----------------------------------------------------------------------
            doubleHeatSinks.setSelected(configuration.getUpgrades().hasDoubleHeatSinks());
            coolRun.setSelected(configuration.getEfficiencies().hasCoolRun());
            heatContainment.setSelected(configuration.getEfficiencies().hasHeatContainment());
            doubleBasics.setSelected(configuration.getEfficiencies().hasDoubleBasics());

            heatsinks.setText("Heatsinks: " + configuration.getHeatsinksCount());
            effectiveHS.setText("Heat capacity: " + df.format(statistics.getHeatCapacity()));
            timeToOverheat.setText("Seconds to Overheat: " + df.format(statistics.getTimeToOverHeat()));
            coolingRatio.setText("Cooling efficiency: " + df.format(statistics.getCoolingRatio()));

            // Offense
            // ----------------------------------------------------------------------

            // Summary
            // ----------------------------------------------------------------------
         }
      });

   }

   @Override
   public void itemStateChanged(ItemEvent anEvent){
      JCheckBox source = (JCheckBox)anEvent.getSource();

      if( source == artemis ){
         configuration.getUpgrades().setArtemis(anEvent.getStateChange() == ItemEvent.SELECTED);
      }
      else if( source == endoSteel ){
         configuration.getUpgrades().setEndoSteel(anEvent.getStateChange() == ItemEvent.SELECTED);
      }
      else if( source == ferroFibros ){
         configuration.getUpgrades().setFerroFibrous(anEvent.getStateChange() == ItemEvent.SELECTED);
      }
      else if( source == speedTweak ){
         configuration.getEfficiencies().setSpeedTweak(anEvent.getStateChange() == ItemEvent.SELECTED);
      }
      else if( source == doubleHeatSinks ){
         configuration.getUpgrades().setDoubleHeatSinks(anEvent.getStateChange() == ItemEvent.SELECTED);
      }
      else if( source == coolRun ){
         configuration.getEfficiencies().setCoolRun(anEvent.getStateChange() == ItemEvent.SELECTED);
      }
      else if( source == heatContainment ){
         configuration.getEfficiencies().setHeatContainment(anEvent.getStateChange() == ItemEvent.SELECTED);
      }
      else if( source == doubleBasics ){
         configuration.getEfficiencies().setDoubleBasics(anEvent.getStateChange() == ItemEvent.SELECTED);
      }
      else{
         throw new RuntimeException("Unkown source control!");
      }
   }

   @Override
   public void receive(Message aMsg){
      updateDisplay(); // TODO be a bit more selective when to update
   }
}
