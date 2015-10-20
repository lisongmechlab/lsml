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
package org.lisoft.lsml.view.mechlab.loadoutframe;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.ComponentOrientation;
import java.awt.Dimension;
import java.awt.GridLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.math.RoundingMode;
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
import javax.swing.JToggleButton;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;

import org.lisoft.lsml.command.CmdSetArmorType;
import org.lisoft.lsml.command.CmdSetGuidanceType;
import org.lisoft.lsml.command.CmdSetHeatSinkType;
import org.lisoft.lsml.command.CmdSetStructureType;
import org.lisoft.lsml.messages.LoadoutMessage;
import org.lisoft.lsml.messages.Message;
import org.lisoft.lsml.messages.MessageReceiver;
import org.lisoft.lsml.messages.MessageXBar;
import org.lisoft.lsml.messages.UpgradesMessage.ChangeMsg;
import org.lisoft.lsml.model.chassi.ChassisBase;
import org.lisoft.lsml.model.environment.Environment;
import org.lisoft.lsml.model.environment.EnvironmentDB;
import org.lisoft.lsml.model.item.Faction;
import org.lisoft.lsml.model.loadout.LoadoutBase;
import org.lisoft.lsml.model.loadout.LoadoutMetrics;
import org.lisoft.lsml.model.loadout.LoadoutStandard;
import org.lisoft.lsml.model.metrics.TopSpeed;
import org.lisoft.lsml.model.metrics.TorsoTwistYawSpeed;
import org.lisoft.lsml.model.metrics.TurningSpeed;
import org.lisoft.lsml.model.upgrades.ArmorUpgrade;
import org.lisoft.lsml.model.upgrades.GuidanceUpgrade;
import org.lisoft.lsml.model.upgrades.HeatSinkUpgrade;
import org.lisoft.lsml.model.upgrades.StructureUpgrade;
import org.lisoft.lsml.model.upgrades.UpgradeDB;
import org.lisoft.lsml.util.CommandStack;
import org.lisoft.lsml.view.MetricDisplay;
import org.lisoft.lsml.view.WeaponSummaryTable;
import org.lisoft.lsml.view.models.UpgradeModel;
import org.lisoft.lsml.view.render.ProgressBarRenderer;
import org.lisoft.lsml.view.render.StyleManager;

public class LoadoutInfoPanel extends JPanel implements MessageReceiver {
    public static final String SUST_DPS_TEXT            = "Sust. DPS: %.1f @ %.0f m";
    public static final String SUST_DPS_TOOLTIP         = "The DPS you can sustain over a prolonged encounter with your current heat dissipation under assumption that the player will not trigger ghost heat. For Ultra-AC type weapons, this caculates the statistically expected DPS. Takes environmental heat and spread of LB-X type weapons into account.";
    public static final String MAX_DPS_TEXT             = "DPS: %.1f @ %.0f m";
    public static final String MAX_DPS_TOOLTIP          = "The maximum damage you can deal per second at the displayed range. For Ultra-AC type weapons, this caculates the statistically expected DPS. Also takes spread of LB-X and similar weapons into account.";
    public static final String BURST_DAMAGE_TEXT        = "Burst  %.1f s: %.1f @ %.0f m";
    public static final String BURST_DAMAGE_TOOLTIP     = "The amount of damage you can deal in a limited time window, under the assumption that heat can be ignored (see time to overheat above). For double fire weapons (U-AC) includes the statistically expected damage. For LB-X type weapons it includes the spread of the weapon.";
    public static final String ALPHA_HEAT_TOOLTIP       = "The amount of heat done by an alpha strike, not including ghost heat.";
    public static final String ALPHA_HEAT_TEXT          = "Alpha heat: %.1f";
    public static final String ALPHA_DAMAGE_TEXT        = "Alpha: %.1f @ %.0f m";
    public static final String ALPHA_DAMAGE_TOOLTIP     = "The maximum damage you can deal at the displayed range in one volley.";
    public static final String GHOST_HEAT_TEXT          = "Ghost heat: %.1f";
    public static final String GHOST_HEAT_TOOLTIP       = "The amount of extra heat you receive on an alpha strike due to the ghost heat mechanic.";
    public static final String TIME_TO_OVERHEAT_TEXT    = "Seconds to overheat: %.1f";
    public static final String TIME_TO_OVERHEAT_TOOLTIP = "The amount of seconds you can go \"All guns a'blazing\" before overheating, assuming no ghost heat.";
    public static final String COOLING_RATIO_TEXT       = "Cooling ratio: %.0f %%";
    public static final String COOLING_RATIO_TOOLTIP    = "How much of your maximal heat generation that can be dissipated. A value of 100% means that you will never overheat.";
    public static final String TIME_TO_COOL_TEXT        = "Time to cool: %.1f";
    public static final String TIME_TO_COOL_TOOLTIP     = "The time the loadout needs to cool from overheat to 0, while moving at full speed.";

    private static final long serialVersionUID = 4720126200474042446L;

    public final static DecimalFormat df2_floor = new DecimalFormat("###.##");
    public final static DecimalFormat df2       = new DecimalFormat("###.##");
    public final static DecimalFormat df1_floor = new DecimalFormat("###.#");
    public final static DecimalFormat df1       = new DecimalFormat("###.#");
    public final static DecimalFormat df0       = new DecimalFormat("###");

    static {
        df2_floor.setMinimumFractionDigits(2);
        df2_floor.setRoundingMode(RoundingMode.FLOOR);
        df2.setMinimumFractionDigits(2);
        df1_floor.setMinimumFractionDigits(1);
        df1_floor.setRoundingMode(RoundingMode.FLOOR);
        df1.setMinimumFractionDigits(1);
    }

    private final LoadoutBase<?> loadout;

    // General pane
    private final JProgressBar massBar;
    private final JLabel       massValue      = new JLabel("xxx");
    private final JProgressBar armorBar;
    private final JLabel       armorValue     = new JLabel("xxx");
    private final JProgressBar critslotsBar   = new JProgressBar(0, 5 * 12 + 3 * 6);
    private final JLabel       critslotsValue = new JLabel("xxx");
    private final JCheckBox    ferroFibros    = new JCheckBox();
    private final JCheckBox    endoSteel      = new JCheckBox();
    private final JCheckBox    artemis        = new JCheckBox();

    // Movement pane
    private final MetricDisplay topSpeed;
    private final MetricDisplay turnSpeed;
    private final MetricDisplay twistSpeed;
    private final JCheckBox     speedTweak = new JCheckBox("Speed Tweak");
    private final JCheckBox     anchorTurn = new JCheckBox("Anchor Turn");
    private final MetricDisplay jumpJets;

    // Heat pane
    private final JLabel                 heatsinks       = new JLabel("xxx");
    private final MetricDisplay          effectiveHS;
    private final MetricDisplay          timeToOverheat;
    private final MetricDisplay          coolingRatio;
    private final MetricDisplay          timeToCool;
    private final JCheckBox              doubleHeatSinks = new JCheckBox("Double Heatsinks");
    private final JCheckBox              coolRun         = new JCheckBox("Cool Run");
    private final JCheckBox              heatContainment = new JCheckBox("Heat Containment");
    private final JCheckBox              doubleBasics    = new JCheckBox("Double Basics");
    private final JComboBox<Environment> environemnts;

    // Offense pane
    private final JComboBox<String> range;
    private final MetricDisplay     alphaStrike;
    private final MetricDisplay     dpsMax;
    private final MetricDisplay     dpsSustained;
    private final MetricDisplay     burstDamage;
    private final JCheckBox         fastFire = new JCheckBox("F. Fire");
    private final MetricDisplay     ghostHeat;
    private final JTable            weaponTable;

    private final CommandStack          cmdStack;
    private final transient MessageXBar xBar;

    // Constants for loadout
    final ArmorUpgrade     armorFF;
    final ArmorUpgrade     armorStandard;
    final StructureUpgrade structureEs;
    final StructureUpgrade structureStandards;
    final HeatSinkUpgrade  heatSinkStandard;
    final HeatSinkUpgrade  heatSinkDouble;

    public LoadoutInfoPanel(LoadoutBase<?> aLoadout, final LoadoutMetrics aMetrics, CommandStack aCommandStack,
            MessageXBar aXBar) {
        loadout = aLoadout;
        cmdStack = aCommandStack;

        // Constants
        boolean isClan = aLoadout.getChassis().getFaction() == Faction.Clan;
        armorFF = isClan ? UpgradeDB.CLAN_FERRO_FIBROUS_ARMOR : UpgradeDB.FERRO_FIBROUS_ARMOR;
        armorStandard = isClan ? UpgradeDB.CLAN_STANDARD_ARMOR : UpgradeDB.STANDARD_ARMOR;
        structureEs = isClan ? UpgradeDB.CLAN_ENDO_STEEL_STRUCTURE : UpgradeDB.ENDO_STEEL_STRUCTURE;
        structureStandards = isClan ? UpgradeDB.CLAN_STANDARD_STRUCTURE : UpgradeDB.STANDARD_STRUCTURE;
        heatSinkStandard = isClan ? UpgradeDB.CLAN_STANDARD_HEATSINKS : UpgradeDB.STANDARD_HEATSINKS;
        heatSinkDouble = isClan ? UpgradeDB.CLAN_DOUBLE_HEATSINKS : UpgradeDB.DOUBLE_HEATSINKS;

        setLayout(new BoxLayout(this, BoxLayout.PAGE_AXIS));
        xBar = aXBar;
        xBar.attach(this);

        setupUpgrades();
        setupEfficiencies();

        // General
        // ----------------------------------------------------------------------
        {
            JPanel general = new JPanel();
            general.setBorder(StyleManager.sectionBorder("General"));
            add(general);

            JLabel critslotsTxt = new JLabel("Slots:");
            critslotsBar.setUI(new ProgressBarRenderer());
            critslotsTxt.setAlignmentY(CENTER_ALIGNMENT);

            JLabel massTxt = new JLabel("Tons:");
            massBar = new JProgressBar(0, loadout.getChassis().getMassMax());
            massBar.setUI(new ProgressBarRenderer());

            JLabel armorTxt = new JLabel("Armor:");
            armorBar = new JProgressBar(0, loadout.getChassis().getArmorMax());
            armorBar.setUI(new ProgressBarRenderer());

            // One property change listener is enough, if one gets it all get it.
            critslotsTxt.addPropertyChangeListener("font", new PropertyChangeListener() {
                @Override
                public void propertyChange(PropertyChangeEvent aArg0) {
                    SwingUtilities.invokeLater(new Runnable() {
                        @Override
                        public void run() {
                            critslotsBar.setUI(new ProgressBarRenderer());
                            massBar.setUI(new ProgressBarRenderer());
                            armorBar.setUI(new ProgressBarRenderer());
                        }
                    });
                }
            });

            Box upgradesBox = Box.createHorizontalBox();
            upgradesBox.add(ferroFibros);
            upgradesBox.add(endoSteel);
            upgradesBox.add(artemis);

            GroupLayout gl_general = new GroupLayout(general);
            gl_general.setAutoCreateGaps(true);

            // @formatter:off
            gl_general.setHorizontalGroup(gl_general.createParallelGroup()
                    .addGroup(gl_general.createSequentialGroup()
                            .addGroup(gl_general.createParallelGroup().addComponent(massTxt).addComponent(armorTxt)
                                    .addComponent(critslotsTxt))
                    .addGroup(gl_general.createParallelGroup().addComponent(massBar).addComponent(armorBar)
                            .addComponent(critslotsBar))
                    .addGroup(gl_general.createParallelGroup(GroupLayout.Alignment.TRAILING).addComponent(massValue)
                            .addComponent(armorValue).addComponent(critslotsValue)))
                    .addComponent(upgradesBox));

            gl_general.setVerticalGroup(gl_general.createSequentialGroup()
                    .addGroup(gl_general.createParallelGroup(GroupLayout.Alignment.CENTER).addComponent(massTxt)
                            .addComponent(massBar).addComponent(massValue))
                    .addGroup(gl_general.createParallelGroup(GroupLayout.Alignment.CENTER).addComponent(armorTxt)
                            .addComponent(armorBar).addComponent(armorValue))
                    .addGroup(gl_general.createParallelGroup(GroupLayout.Alignment.CENTER).addComponent(critslotsTxt)
                            .addComponent(critslotsBar).addComponent(critslotsValue))
                    .addComponent(upgradesBox));
            // @formatter:on

            general.setLayout(gl_general);
        }

        add(new ArmorDistributionPanel(loadout, cmdStack, aXBar));

        // Mobility
        // ----------------------------------------------------------------------
        {
            JPanel mobility = new JPanel();
            mobility.setBorder(StyleManager.sectionBorder("Mobility"));
            mobility.setLayout(new BoxLayout(mobility, BoxLayout.PAGE_AXIS));
            mobility.add(Box.createHorizontalGlue());
            add(mobility);

            {
                jumpJets = new MetricDisplay(aMetrics.jumpDistance, "Jump Jets: %d/%d (%.1f m)",
                        "The maximal height your mech will reach at the apex of its jump.", aXBar, loadout);
                jumpJets.setAlignmentX(Component.CENTER_ALIGNMENT);
                mobility.add(jumpJets);

                topSpeed = new MetricDisplay(new TopSpeed(loadout), "Top Speed: %.1f km/h",
                        "The maximum speed the mech can move at.", aXBar, loadout);
                topSpeed.setAlignmentX(Component.CENTER_ALIGNMENT);
                mobility.add(topSpeed);

                JPanel panel = new JPanel(new BorderLayout());
                panel.add(topSpeed, BorderLayout.WEST);
                panel.add(jumpJets, BorderLayout.EAST);
                mobility.add(panel);
            }

            {
                turnSpeed = new MetricDisplay(new TurningSpeed(loadout), "Turn Speed: %.1f °/s",
                        "The rate at which your mech can turn its legs.", aXBar, loadout);
                turnSpeed.setAlignmentX(CENTER_ALIGNMENT);
                mobility.add(turnSpeed);

                twistSpeed = new MetricDisplay(new TorsoTwistYawSpeed(loadout), "Twist Speed: %.1f °/s",
                        "The rate at which your mech can turn its tors in relation to the legs.", aXBar, loadout);
                twistSpeed.setAlignmentX(CENTER_ALIGNMENT);
                mobility.add(twistSpeed);

                JPanel panel = new JPanel(new BorderLayout());
                panel.add(turnSpeed, BorderLayout.WEST);
                panel.add(twistSpeed, BorderLayout.EAST);
                mobility.add(panel);
            }

            JPanel eff = new JPanel(new GridLayout(1, 2));
            eff.setAlignmentY(Component.CENTER_ALIGNMENT);

            speedTweak.setComponentOrientation(ComponentOrientation.RIGHT_TO_LEFT);

            eff.add(speedTweak);
            eff.add(anchorTurn);
            mobility.add(eff);
        }

        // Heat
        // ----------------------------------------------------------------------
        {
            JPanel heat = new JPanel();
            heat.setBorder(StyleManager.sectionBorder("Heat"));
            heat.setLayout(new BoxLayout(heat, BoxLayout.PAGE_AXIS));
            heat.add(Box.createHorizontalGlue());
            add(heat);

            JPanel envPanel = new JPanel();
            List<Environment> evs = new ArrayList<>(EnvironmentDB.lookupAll());
            evs.add(0, new Environment("neutral", 0.0));
            environemnts = new JComboBox<>(evs.toArray(new Environment[evs.size()]));
            environemnts.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent aArg0) {
                    Environment environment = (Environment) environemnts.getSelectedItem();
                    aMetrics.heatDissipation.changeEnvironment(environment);
                    xBar.post(new LoadoutMessage(loadout, LoadoutMessage.Type.UPDATE));
                }
            });

            envPanel.add(new JLabel("Environment:"));
            envPanel.add(environemnts);
            heat.add(envPanel);

            {
                effectiveHS = new MetricDisplay(aMetrics.heatCapacity, "Heat capacity: %.1f",
                        "The amount of heat your mech can hold without overheating.", aXBar, loadout);

                JPanel panel = new JPanel(new BorderLayout());
                panel.add(heatsinks, BorderLayout.WEST);
                panel.add(effectiveHS, BorderLayout.EAST);
                heat.add(panel);
            }

            {
                coolingRatio = new MetricDisplay(aMetrics.coolingRatio, COOLING_RATIO_TEXT, COOLING_RATIO_TOOLTIP,
                        aXBar, loadout, true);

                timeToOverheat = new MetricDisplay(aMetrics.alphaTimeToOverHeat, TIME_TO_OVERHEAT_TEXT,
                        TIME_TO_OVERHEAT_TOOLTIP, aXBar, loadout);

                JPanel panel = new JPanel(new BorderLayout());
                panel.add(coolingRatio, BorderLayout.WEST);
                panel.add(timeToOverheat, BorderLayout.EAST);
                heat.add(panel);
            }

            {

                timeToCool = new MetricDisplay(aMetrics.timeToCool, TIME_TO_COOL_TEXT, TIME_TO_COOL_TOOLTIP, aXBar,
                        loadout);

                ghostHeat = new MetricDisplay(aMetrics.ghostHeat, GHOST_HEAT_TEXT, GHOST_HEAT_TOOLTIP, aXBar, loadout) {
                    private static final long serialVersionUID = 1L;

                    @Override
                    protected void updateText() {
                        if (metric.calculate() > 0)
                            setForeground(Color.RED);
                        else
                            setForeground(effectiveHS.getForeground());
                        super.updateText();
                    }
                };

                JPanel panel = new JPanel(new BorderLayout());
                panel.add(timeToCool, BorderLayout.WEST);
                panel.add(ghostHeat, BorderLayout.EAST);
                heat.add(panel);
            }

            JPanel upgrades = new JPanel(new GridLayout(2, 2));
            upgrades.setAlignmentY(Component.CENTER_ALIGNMENT);
            coolRun.setComponentOrientation(ComponentOrientation.RIGHT_TO_LEFT);
            upgrades.add(coolRun);
            upgrades.add(heatContainment);

            doubleHeatSinks.setComponentOrientation(ComponentOrientation.RIGHT_TO_LEFT);
            upgrades.add(doubleHeatSinks);
            upgrades.add(doubleBasics);
            heat.add(upgrades);
        }

        // Offense
        // ----------------------------------------------------------------------
        {
            JPanel offence = new JPanel(new BorderLayout());
            offence.setBorder(StyleManager.sectionBorder("Offense"));

            JPanel offenceTop = new JPanel();
            offenceTop.setLayout(new BoxLayout(offenceTop, BoxLayout.PAGE_AXIS));

            {
                JPanel panel = new JPanel();
                panel.add(new JLabel("Range:"));
                panel.setToolTipText(
                        "Select the range of engagement that alpha strike, max and sustained DPS will be calculated for. Set this to \"opt\" or \"optimal\" to automatically select your optimal ranges.");

                String ranges[] = new String[] { "Optimal", "90", "180", "270", "300", "450", "675", "720", "810",
                        "900", "1080", "1350", "1620", "1980", "2160" };
                range = new JComboBox<String>(ranges);
                range.setEditable(true);
                range.setToolTipText(panel.getToolTipText());
                Dimension rp = range.getPreferredSize();
                rp.width = range.getFontMetrics(range.getFont()).stringWidth("Optimal") + 30;
                range.setPreferredSize(rp);
                range.addActionListener(new ActionListener() {
                    @Override
                    public void actionPerformed(ActionEvent aArg0) {
                        String value = (String) range.getSelectedItem();
                        final int r;
                        if (value.toLowerCase().contains("opt")) {
                            r = -1;
                        }
                        else {
                            try {
                                r = Integer.parseInt(value);
                            }
                            catch (NumberFormatException e) {
                                JOptionPane.showMessageDialog(LoadoutInfoPanel.this,
                                        "Please enter an integer range or \"optimal\" or \"opt\" to select the optimal range automatically.");
                                range.setSelectedIndex(0);
                                return;
                            }
                        }
                        aMetrics.changeRange(r);
                        SwingUtilities.invokeLater(new Runnable() {
                            @Override
                            public void run() {
                                xBar.post(new LoadoutMessage(loadout, LoadoutMessage.Type.UPDATE));
                            }
                        });
                    }
                });
                panel.add(range);
                {
                    JPanel pane = new JPanel();
                    pane.add(new JLabel("Time:"));
                    pane.setToolTipText(
                            "The length of the engagement you're designing for. Will affect the \"Burst\" value.");

                    Double times[] = new Double[] { 5.0, 10.0, 15.0, 20.0, 30.0, 45.0, 60.0 };
                    final JComboBox<Double> timeOfEngagement = new JComboBox<Double>(times);
                    timeOfEngagement.setEditable(true);
                    timeOfEngagement.setToolTipText(pane.getToolTipText());
                    Dimension tp = timeOfEngagement.getPreferredSize();
                    tp.width = timeOfEngagement.getFontMetrics(timeOfEngagement.getFont()).stringWidth("999") + 30;
                    timeOfEngagement.setPreferredSize(tp);
                    timeOfEngagement.addActionListener(new ActionListener() {
                        @Override
                        public void actionPerformed(ActionEvent aArg0) {
                            double time = (Double) timeOfEngagement.getSelectedItem();
                            aMetrics.changeTime(time);
                            SwingUtilities.invokeLater(new Runnable() {
                                @Override
                                public void run() {
                                    xBar.post(new LoadoutMessage(loadout, LoadoutMessage.Type.UPDATE));
                                }
                            });
                        }
                    });
                    aMetrics.changeTime(5.0);
                    pane.add(timeOfEngagement);
                    panel.add(pane);
                }

                panel.add(fastFire);
                fastFire.setToolTipText("The fast fire talent. Reduces weapon cooldown by 5%.");
                fastFire.setAlignmentX(Component.CENTER_ALIGNMENT);
                offenceTop.add(panel);
            }

            {
                alphaStrike = new MetricDisplay(aMetrics.alphaStrike, ALPHA_DAMAGE_TEXT, ALPHA_DAMAGE_TOOLTIP, aXBar,
                        loadout);
                alphaStrike.setAlignmentX(Component.CENTER_ALIGNMENT);

                burstDamage = new MetricDisplay(aMetrics.burstDamageOverTime, BURST_DAMAGE_TEXT, BURST_DAMAGE_TOOLTIP,
                        aXBar, loadout);
                burstDamage.setAlignmentX(Component.CENTER_ALIGNMENT);

                JPanel panel = new JPanel(new BorderLayout());
                panel.add(alphaStrike, BorderLayout.WEST);
                panel.add(burstDamage, BorderLayout.EAST);
                offenceTop.add(panel);
            }

            {
                dpsMax = new MetricDisplay(aMetrics.maxDPS, MAX_DPS_TEXT, MAX_DPS_TOOLTIP, aXBar, loadout);
                dpsMax.setAlignmentX(Component.CENTER_ALIGNMENT);

                dpsSustained = new MetricDisplay(aMetrics.maxSustainedDPS, SUST_DPS_TEXT, SUST_DPS_TOOLTIP, aXBar,
                        loadout);
                dpsSustained.setAlignmentX(Component.CENTER_ALIGNMENT);

                JPanel panel = new JPanel(new BorderLayout());
                panel.add(dpsMax, BorderLayout.WEST);
                panel.add(dpsSustained, BorderLayout.EAST);
                offenceTop.add(panel);
            }

            offenceTop.add(Box.createVerticalStrut(5));

            weaponTable = new WeaponSummaryTable(loadout, aXBar);
            JScrollPane weapons = new JScrollPane(weaponTable);
            weapons.setPreferredSize(new Dimension(260, 150));

            offence.add(offenceTop, BorderLayout.NORTH);
            offence.add(weapons, BorderLayout.CENTER);
            add(offence);
        }

        environemnts.setSelectedIndex(0);
        updateDisplay();
    }

    public void updateDisplay() {

        // General
        // ----------------------------------------------------------------------
        double mass = loadout.getMass();
        massBar.setValue((int) Math.ceil(mass));
        massValue.setText(df2_floor.format(loadout.getChassis().getMassMax() - mass) + " free");
        massBar.setString(df1_floor.format(mass) + " / " + df0.format(loadout.getChassis().getMassMax()));

        armorBar.setValue(loadout.getArmor());
        armorBar.setString(loadout.getArmor() + " / " + loadout.getChassis().getArmorMax());
        armorValue.setText((loadout.getChassis().getArmorMax() - loadout.getArmor()) + " free");

        critslotsBar.setValue(loadout.getNumCriticalSlotsUsed());
        critslotsBar
                .setString(loadout.getNumCriticalSlotsUsed() + " / " + loadout.getChassis().getCriticalSlotsTotal());
        critslotsValue.setText(loadout.getNumCriticalSlotsFree() + " free");

        updateUpgrades();

        if (loadout.getHeatsinksCount() < 10) {
            heatsinks.setForeground(Color.RED);
        }
        else {
            heatsinks.setForeground(effectiveHS.getForeground());
        }
        heatsinks.setText("Heatsinks: " + loadout.getHeatsinksCount());
    }

    private void updateUpgrades() {
        {
            final ChassisBase chassis = loadout.getChassis();
            final String esSavedMass = df2
                    .format(structureStandards.getStructureMass(chassis) - structureEs.getStructureMass(chassis));
            final String esSlots = Integer.toString(structureEs.getExtraSlots());
            if ((loadout.getUpgrades().getStructure() == structureEs)) {
                endoSteel.setText("<html>Endo-Steel<br>(<span style=\"color: green;\">-" + esSavedMass + "t</span>, "
                        + "<span style=\"color: red;\">+" + esSlots + "s</span>)" + "</html>");
            }
            else {
                endoSteel.setText("<html>Endo-Steel<br>(<span style=\"color: gray;\">-" + esSavedMass + "t</span>, "
                        + "<span style=\"color: gray;\">+" + esSlots + "s</span>)" + "</html>");
            }
        }
        {

            final int armor = loadout.getArmor();
            final String ffSavedMass = df2.format(armorStandard.getArmorMass(armor) - armorFF.getArmorMass(armor));
            final String ffSlots = Integer.toString(armorFF.getExtraSlots());
            if (loadout.getUpgrades().getArmor() == armorFF) {
                ferroFibros.setText("<html>Ferro-Fibrous<br>(<span style=\"color: green;\">-" + ffSavedMass
                        + "t</span>, " + "<span style=\"color: red;\">+" + ffSlots + "s</span>)" + "</html>");
            }
            else {
                ferroFibros.setText("<html>Ferro-Fibrous<br>(<span style=\"color: gray;\">-" + ffSavedMass
                        + "t</span>, " + "<span style=\"color: gray;\">+" + ffSlots + "s</span>)" + "</html>");
            }
        }

        {
            final String artemisMass = df0.format(UpgradeDB.ARTEMIS_IV.getExtraTons(loadout));
            final int artemisSlots = UpgradeDB.ARTEMIS_IV.getExtraSlots(loadout);
            if (loadout.getUpgrades().getGuidance() == UpgradeDB.ARTEMIS_IV) {
                artemis.setText("<html>Artemis IV<br>(<span style=\"color: red;\">+" + artemisMass + "t</span>, "
                        + "<span style=\"color: red;\">+" + artemisSlots + "s</span>)" + "</html>");
            }
            else {
                artemis.setText("<html>Artemis IV<br>(<span style=\"color: gray;\">+" + artemisMass + "t</span>, "
                        + "<span style=\"color: gray;\">+" + artemisSlots + "s</span>)" + "</html>");
            }
        }
    }

    private void setupUpgrades() {
        Insets upgradeInsets = new Insets(0, 0, 0, 0);

        endoSteel.setVerticalTextPosition(SwingConstants.TOP);
        endoSteel.setMargin(upgradeInsets);
        endoSteel.setMultiClickThreshhold(100);
        endoSteel.setModel(new UpgradeModel(xBar, loadout, ChangeMsg.STRUCTURE) {
            @Override
            public boolean isSelected() {
                return loadout.getUpgrades().getStructure() == structureEs;
            }

            @Override
            public boolean isEnabled() {
                return loadout instanceof LoadoutStandard;
            }

            @Override
            public void changeValue(boolean aEnabled) throws Exception {
                StructureUpgrade structure = aEnabled ? structureEs : structureStandards;
                cmdStack.pushAndApply(new CmdSetStructureType(xBar, (LoadoutStandard) loadout, structure));
            }
        });

        ferroFibros.setVerticalTextPosition(SwingConstants.TOP);
        ferroFibros.setMargin(upgradeInsets);
        ferroFibros.setMultiClickThreshhold(100);
        ferroFibros.setModel(new UpgradeModel(xBar, loadout, ChangeMsg.ARMOR) {
            @Override
            public boolean isSelected() {
                return loadout.getUpgrades().getArmor() == armorFF;
            }

            @Override
            public boolean isEnabled() {
                return loadout instanceof LoadoutStandard;
            }

            @Override
            public void changeValue(boolean aEnabled) throws Exception {
                ArmorUpgrade armor = aEnabled ? armorFF : armorStandard;
                cmdStack.pushAndApply(new CmdSetArmorType(xBar, (LoadoutStandard) loadout, armor));
            }
        });

        doubleHeatSinks.setModel(new UpgradeModel(xBar, loadout, ChangeMsg.HEATSINKS) {
            @Override
            public boolean isSelected() {
                return loadout.getUpgrades().getHeatSink() == heatSinkDouble;
            }

            @Override
            public boolean isEnabled() {
                return loadout instanceof LoadoutStandard;
            }

            @Override
            public void changeValue(boolean aEnabled) throws Exception {
                HeatSinkUpgrade heatSinks = aEnabled ? heatSinkDouble : heatSinkStandard;
                cmdStack.pushAndApply(new CmdSetHeatSinkType(xBar, (LoadoutStandard) loadout, heatSinks));
            }
        });

        artemis.setVerticalTextPosition(SwingConstants.TOP);
        artemis.setMargin(upgradeInsets);
        artemis.setMultiClickThreshhold(100);
        artemis.setModel(new UpgradeModel(xBar, loadout, ChangeMsg.GUIDANCE) {
            @Override
            public boolean isSelected() {
                return loadout.getUpgrades().getGuidance() == UpgradeDB.ARTEMIS_IV;
            }

            @Override
            public void changeValue(boolean aEnabled) throws Exception {
                GuidanceUpgrade guidance = aEnabled ? UpgradeDB.ARTEMIS_IV : UpgradeDB.STANDARD_GUIDANCE;
                cmdStack.pushAndApply(new CmdSetGuidanceType(xBar, loadout, guidance));
            }
        });

        updateUpgrades();
    }

    private void setupEfficiencies() {
        speedTweak.setModel(new JToggleButton.ToggleButtonModel() {
            @Override
            public void setSelected(boolean aB) {
                loadout.getEfficiencies().setSpeedTweak(aB, xBar);
            }

            @Override
            public boolean isSelected() {
                return loadout.getEfficiencies().hasSpeedTweak();
            }
        });

        anchorTurn.setModel(new JToggleButton.ToggleButtonModel() {
            @Override
            public void setSelected(boolean aB) {
                loadout.getEfficiencies().setAnchorTurn(aB, xBar);
            }

            @Override
            public boolean isSelected() {
                return loadout.getEfficiencies().hasAnchorTurn();
            }
        });

        coolRun.setModel(new JToggleButton.ToggleButtonModel() {
            @Override
            public void setSelected(boolean aB) {
                loadout.getEfficiencies().setCoolRun(aB, xBar);
            }

            @Override
            public boolean isSelected() {
                return loadout.getEfficiencies().hasCoolRun();
            }
        });

        heatContainment.setModel(new JToggleButton.ToggleButtonModel() {
            @Override
            public void setSelected(boolean aB) {
                loadout.getEfficiencies().setHeatContainment(aB, xBar);
            }

            @Override
            public boolean isSelected() {
                return loadout.getEfficiencies().hasHeatContainment();
            }
        });

        doubleBasics.setModel(new JToggleButton.ToggleButtonModel() {
            @Override
            public void setSelected(boolean aB) {
                loadout.getEfficiencies().setDoubleBasics(aB, xBar);
            }

            @Override
            public boolean isSelected() {
                return loadout.getEfficiencies().hasDoubleBasics();
            }
        });

        fastFire.setModel(new JToggleButton.ToggleButtonModel() {
            @Override
            public void setSelected(boolean aB) {
                loadout.getEfficiencies().setFastFire(aB, xBar);
            }

            @Override
            public boolean isSelected() {
                return loadout.getEfficiencies().hasFastFire();
            }
        });
    }

    @Override
    public void receive(Message aMsg) {
        if (aMsg.isForMe(loadout)) {
            updateDisplay();
        }
    }
}
