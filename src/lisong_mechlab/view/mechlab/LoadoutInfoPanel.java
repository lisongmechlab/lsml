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

import java.awt.BorderLayout;
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
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.math.RoundingMode;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;

import javax.swing.AbstractAction;
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

import lisong_mechlab.model.environment.Environment;
import lisong_mechlab.model.environment.EnvironmentDB;
import lisong_mechlab.model.item.Faction;
import lisong_mechlab.model.loadout.LoadoutBase;
import lisong_mechlab.model.loadout.LoadoutMessage;
import lisong_mechlab.model.loadout.LoadoutOmniMech;
import lisong_mechlab.model.loadout.LoadoutStandard;
import lisong_mechlab.model.metrics.AlphaStrike;
import lisong_mechlab.model.metrics.AlphaTimeToOverHeat;
import lisong_mechlab.model.metrics.BurstDamageOverTime;
import lisong_mechlab.model.metrics.CoolingRatio;
import lisong_mechlab.model.metrics.GhostHeat;
import lisong_mechlab.model.metrics.HeatCapacity;
import lisong_mechlab.model.metrics.HeatDissipation;
import lisong_mechlab.model.metrics.HeatGeneration;
import lisong_mechlab.model.metrics.HeatOverTime;
import lisong_mechlab.model.metrics.JumpDistance;
import lisong_mechlab.model.metrics.MaxDPS;
import lisong_mechlab.model.metrics.MaxSustainedDPS;
import lisong_mechlab.model.metrics.RangeMetric;
import lisong_mechlab.model.metrics.RangeTimeMetric;
import lisong_mechlab.model.metrics.TimeToCool;
import lisong_mechlab.model.metrics.TopSpeed;
import lisong_mechlab.model.metrics.TurningSpeed;
import lisong_mechlab.model.metrics.TwistSpeed;
import lisong_mechlab.model.upgrades.ArmorUpgrade;
import lisong_mechlab.model.upgrades.HeatSinkUpgrade;
import lisong_mechlab.model.upgrades.OpSetArmorType;
import lisong_mechlab.model.upgrades.OpSetGuidanceType;
import lisong_mechlab.model.upgrades.OpSetHeatSinkType;
import lisong_mechlab.model.upgrades.OpSetStructureType;
import lisong_mechlab.model.upgrades.StructureUpgrade;
import lisong_mechlab.model.upgrades.UpgradeDB;
import lisong_mechlab.util.OperationStack;
import lisong_mechlab.util.message.Message;
import lisong_mechlab.util.message.MessageXBar;
import lisong_mechlab.view.MetricDisplay;
import lisong_mechlab.view.ProgramInit;
import lisong_mechlab.view.WeaponSummaryTable;
import lisong_mechlab.view.render.ProgressBarRenderer;
import lisong_mechlab.view.render.StyleManager;

public class LoadoutInfoPanel extends JPanel implements ItemListener, Message.Recipient {
	private static final long				serialVersionUID	= 4720126200474042446L;

	private final static DecimalFormat		df2_floor			= new DecimalFormat("###.##");
	private final static DecimalFormat		df2					= new DecimalFormat("###.##");
	private final static DecimalFormat		df1_floor			= new DecimalFormat("###.#");
	private final static DecimalFormat		df1					= new DecimalFormat("###.#");
	private final static DecimalFormat		df0					= new DecimalFormat("###");

	static {
		df2_floor.setMinimumFractionDigits(2);
		df2_floor.setRoundingMode(RoundingMode.FLOOR);
		df2.setMinimumFractionDigits(2);
		df1_floor.setMinimumFractionDigits(1);
		df1_floor.setRoundingMode(RoundingMode.FLOOR);
		df1.setMinimumFractionDigits(1);
	}

	private final LoadoutBase<?>			loadout;

	// General pane
	private final JProgressBar				massBar;
	private final JLabel					massValue			= new JLabel("xxx");
	private final JProgressBar				armorBar;
	private final JLabel					armorValue			= new JLabel("xxx");
	private final JProgressBar				critslotsBar		= new JProgressBar(0, 5 * 12 + 3 * 6);
	private final JLabel					critslotsValue		= new JLabel("xxx");
	private final JCheckBox					ferroFibros			= new JCheckBox();
	private final JCheckBox					endoSteel			= new JCheckBox();
	private final JCheckBox					artemis				= new JCheckBox();

	// Movement pane
	private final MetricDisplay				topSpeed;
	private final MetricDisplay				turnSpeed;
	private final MetricDisplay				twistSpeed;
	private final JCheckBox					speedTweak			= new JCheckBox("Speed Tweak");
	private final JCheckBox					anchorTurn			= new JCheckBox("Anchor Turn");
	private final JLabel					jumpJets			= new JLabel("xxx");

	// Heat pane
	private final JLabel					heatsinks			= new JLabel("xxx");
	private final MetricDisplay				effectiveHS;
	private final MetricDisplay				timeToOverheat;
	private final MetricDisplay				coolingRatio;
	private final MetricDisplay				timeToCool;
	private final JCheckBox					doubleHeatSinks		= new JCheckBox("Double Heatsinks");
	private final JCheckBox					coolRun				= new JCheckBox("Cool Run");
	private final JCheckBox					heatContainment		= new JCheckBox("Heat Containment");
	private final JCheckBox					doubleBasics		= new JCheckBox("Double Basics");
	private final JComboBox<Environment>	environemnts;

	// Offense pane
	private final JComboBox<String>			range;
	private final MetricDisplay				alphaStrike;
	private final MetricDisplay				dpsMax;
	private final MetricDisplay				dpsSustained;
	private final MetricDisplay				burstDamage;
	private final JCheckBox					fastFire			= new JCheckBox("F. Fire");
	private final MetricDisplay				ghostHeat;
	private final JTable					weaponTable;

	private final JumpDistance				metricJumpDistance;
	private transient Boolean				inhibitChanges		= false;
	private final MaxSustainedDPS			metricSustainedDps;
	private final OperationStack			opStack;
	private final transient MessageXBar		xBar;

	public LoadoutInfoPanel(LoadoutFrame aLoadoutFrame, MessageXBar anXBar) {
		loadout = aLoadoutFrame.getLoadout();
		opStack = aLoadoutFrame.getOpStack();

		metricJumpDistance = new JumpDistance(loadout);

		setLayout(new BoxLayout(this, BoxLayout.PAGE_AXIS));
		xBar = anXBar;
		xBar.attach(this);

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

			setupUpgrades();

			Box upgradesBox = Box.createHorizontalBox();
			upgradesBox.add(ferroFibros);
			upgradesBox.add(endoSteel);
			upgradesBox.add(artemis);

			GroupLayout gl_general = new GroupLayout(general);
			gl_general.setAutoCreateGaps(true);

			// @formatter:off
			gl_general.setHorizontalGroup(gl_general
					.createParallelGroup()
					.addGroup(
							gl_general
									.createSequentialGroup()
									.addGroup(
											gl_general.createParallelGroup().addComponent(massTxt)
													.addComponent(armorTxt).addComponent(critslotsTxt))
									.addGroup(
											gl_general.createParallelGroup().addComponent(massBar)
													.addComponent(armorBar).addComponent(critslotsBar))
									.addGroup(
											gl_general.createParallelGroup(GroupLayout.Alignment.TRAILING)
													.addComponent(massValue).addComponent(armorValue)
													.addComponent(critslotsValue))).addComponent(upgradesBox));

			gl_general
					.setVerticalGroup(gl_general
							.createSequentialGroup()
							.addGroup(
									gl_general.createParallelGroup(GroupLayout.Alignment.CENTER).addComponent(massTxt)
											.addComponent(massBar).addComponent(massValue))
							.addGroup(
									gl_general.createParallelGroup(GroupLayout.Alignment.CENTER).addComponent(armorTxt)
											.addComponent(armorBar).addComponent(armorValue))
							.addGroup(
									gl_general.createParallelGroup(GroupLayout.Alignment.CENTER)
											.addComponent(critslotsTxt).addComponent(critslotsBar)
											.addComponent(critslotsValue)).addComponent(upgradesBox));
			// @formatter:on

			general.setLayout(gl_general);
		}

		add(new ArmorDistributionPanel(loadout, opStack, anXBar));

		// Mobility
		// ----------------------------------------------------------------------
		{
			JPanel mobility = new JPanel();
			mobility.setBorder(StyleManager.sectionBorder("Mobility"));
			mobility.setLayout(new BoxLayout(mobility, BoxLayout.PAGE_AXIS));
			mobility.add(Box.createHorizontalGlue());
			add(mobility);

			{
				jumpJets.setAlignmentX(Component.CENTER_ALIGNMENT);
				mobility.add(jumpJets);

				topSpeed = new MetricDisplay(new TopSpeed(loadout), "Top Speed: %.1f km/h",
						"The maximum speed the mech can move at.", anXBar, loadout);
				topSpeed.setAlignmentX(Component.CENTER_ALIGNMENT);
				mobility.add(topSpeed);

				JPanel panel = new JPanel(new BorderLayout());
				panel.add(topSpeed, BorderLayout.WEST);
				panel.add(jumpJets, BorderLayout.EAST);
				mobility.add(panel);
			}

			{
				turnSpeed = new MetricDisplay(new TurningSpeed(loadout), "Turn Speed: %.1f °/s",
						"The rate at which your mech can turn its legs.", anXBar, loadout);
				turnSpeed.setAlignmentX(CENTER_ALIGNMENT);
				mobility.add(turnSpeed);

				twistSpeed = new MetricDisplay(new TwistSpeed(loadout), "Twist Speed: %.1f °/s",
						"The rate at which your mech can turn its tors in relation to the legs.", anXBar, loadout);
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
			speedTweak.addItemListener(this);
			anchorTurn.addItemListener(this);
		}

		final HeatCapacity heatCapacity = new HeatCapacity(loadout);
		final HeatDissipation heatDissipation = new HeatDissipation(loadout, null);
		final HeatGeneration heatGeneration = new HeatGeneration(loadout);

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
					heatDissipation.changeEnvironment(environment);
					xBar.post(new LoadoutMessage(loadout, LoadoutMessage.Type.UPDATE));
				}
			});
			environemnts.setSelectedIndex(0);

			envPanel.add(new JLabel("Environment:"));
			envPanel.add(environemnts);
			heat.add(envPanel);

			{
				effectiveHS = new MetricDisplay(heatCapacity, "Heat capacity: %.1f",
						"The amount of heat your mech can hold without overheating.", anXBar, loadout);

				JPanel panel = new JPanel(new BorderLayout());
				panel.add(heatsinks, BorderLayout.WEST);
				panel.add(effectiveHS, BorderLayout.EAST);
				heat.add(panel);
			}

			{
				coolingRatio = new MetricDisplay(new CoolingRatio(heatDissipation, heatGeneration),
						"Cooling ratio: %.0f %%", "How much of your maximal heat generation that can be dissipated. "
								+ "A value of 100% means that you will never overheat.", anXBar, loadout, true);

				HeatOverTime heatOverTime = new HeatOverTime(loadout, xBar);
				timeToOverheat = new MetricDisplay(
						new AlphaTimeToOverHeat(heatCapacity, heatOverTime, heatDissipation),
						"Seconds to Overheat: %.1f",
						"The amount of seconds you can go \"All guns a'blazing\" before overheating, assuming no ghost heat.",
						anXBar, loadout);

				JPanel panel = new JPanel(new BorderLayout());
				panel.add(coolingRatio, BorderLayout.WEST);
				panel.add(timeToOverheat, BorderLayout.EAST);
				heat.add(panel);
			}

			{

				timeToCool = new MetricDisplay(new TimeToCool(heatCapacity, heatDissipation), "Time to cool: %.1f",
						"The time the loadout needs to cool from overheat to 0, while moving at full speed.", anXBar,
						loadout);

				ghostHeat = new MetricDisplay(new GhostHeat(loadout), "Ghost heat: %.1f",
						"The amount of extra heat you receive on an alpha strike due to the ghost heat mechanic.",
						anXBar, loadout) {
					private static final long	serialVersionUID	= 1L;

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

			doubleBasics.addItemListener(this);
			heatContainment.addItemListener(this);
			coolRun.addItemListener(this);
		}

		// Offense
		// ----------------------------------------------------------------------
		{
			JPanel offence = new JPanel(new BorderLayout());
			offence.setBorder(StyleManager.sectionBorder("Offense"));

			JPanel offenceTop = new JPanel();
			offenceTop.setLayout(new BoxLayout(offenceTop, BoxLayout.PAGE_AXIS));

			final RangeTimeMetric metricBurstDamage = new BurstDamageOverTime(loadout, anXBar);
			final RangeMetric metricAlphaStrike = new AlphaStrike(loadout);
			final RangeMetric metricMaxDPS = new MaxDPS(loadout);
			metricSustainedDps = new MaxSustainedDPS(loadout, heatDissipation);

			{
				JPanel panel = new JPanel();
				panel.add(new JLabel("Range:"));
				panel.setToolTipText("Select the range of engagement that alpha strike, max and sustained DPS will be calculated for. Set this to \"opt\" or \"optimal\" to automatically select your optimal ranges.");

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
						} else {
							try {
								r = Integer.parseInt(value);
							} catch (NumberFormatException e) {
								JOptionPane
										.showMessageDialog(LoadoutInfoPanel.this,
												"Please enter an integer range or \"optimal\" or \"opt\" to select the optimal range automatically.");
								range.setSelectedIndex(0);
								return;
							}
						}
						metricAlphaStrike.changeRange(r);
						metricMaxDPS.changeRange(r);
						metricSustainedDps.changeRange(r);
						metricBurstDamage.changeRange(r);
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
					pane.setToolTipText("The length of the engagement you're designing for. Will affect the \"Burst\" value.");

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
							metricBurstDamage.changeTime(time);
							SwingUtilities.invokeLater(new Runnable() {
								@Override
								public void run() {
									xBar.post(new LoadoutMessage(loadout, LoadoutMessage.Type.UPDATE));
								}
							});
						}
					});
					metricBurstDamage.changeTime(5.0);
					pane.add(timeOfEngagement);
					panel.add(pane);
				}

				panel.add(fastFire);
				fastFire.addItemListener(this);
				fastFire.setToolTipText("The fast fire talent. Reduces weapon cooldown by 5%.");
				fastFire.setAlignmentX(Component.CENTER_ALIGNMENT);
				offenceTop.add(panel);
			}

			{
				alphaStrike = new MetricDisplay(metricAlphaStrike, "Alpha: %.1f @ %.0f m",
						"The maximum damage you can deal at the displayed range in one volley.", anXBar, loadout);
				alphaStrike.setAlignmentX(Component.CENTER_ALIGNMENT);

				burstDamage = new MetricDisplay(
						metricBurstDamage,
						"Burst  %.1f s: %.1f @ %.0f m",
						"The maximum damage you can deal under the given time frame at the displayed range."
								+ "This is taken under the assumption that you do not overheat, check time to overheat above.",
						anXBar, loadout);
				burstDamage.setAlignmentX(Component.CENTER_ALIGNMENT);

				JPanel panel = new JPanel(new BorderLayout());
				panel.add(alphaStrike, BorderLayout.WEST);
				panel.add(burstDamage, BorderLayout.EAST);
				offenceTop.add(panel);
			}

			{
				dpsMax = new MetricDisplay(metricMaxDPS, "DPS: %.1f @ %.0f m",
						"The maximum damage you can deal per second at the displayed range.", anXBar, loadout);
				dpsMax.setAlignmentX(Component.CENTER_ALIGNMENT);

				dpsSustained = new MetricDisplay(metricSustainedDps, "Sust. DPS: %.1f @ %.0f m",
						"The maximum damage you can deal per second at the displayed range, over a long period"
								+ " of time. This depends on your heat dissipation and weapon heat generation.",
						anXBar, loadout);
				dpsSustained.setAlignmentX(Component.CENTER_ALIGNMENT);

				JPanel panel = new JPanel(new BorderLayout());
				panel.add(dpsMax, BorderLayout.WEST);
				panel.add(dpsSustained, BorderLayout.EAST);
				offenceTop.add(panel);
			}

			offenceTop.add(Box.createVerticalStrut(5));

			weaponTable = new WeaponSummaryTable(loadout, anXBar);
			JScrollPane weapons = new JScrollPane(weaponTable);
			weapons.setPreferredSize(new Dimension(260, 150));

			offence.add(offenceTop, BorderLayout.NORTH);
			offence.add(weapons, BorderLayout.CENTER);
			add(offence);
		}
		updateDisplay();
	}

	public void updateDisplay() {
		SwingUtilities.invokeLater(new Runnable() {
			@Override
			public void run() {
				synchronized (inhibitChanges) {

					inhibitChanges = true;

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
					critslotsBar.setString(loadout.getNumCriticalSlotsUsed() + " / "
							+ loadout.getChassis().getCriticalSlotsTotal());
					critslotsValue.setText(loadout.getNumCriticalSlotsFree() + " free");

					updateUpgrades();

					// Mobility
					// ----------------------------------------------------------------------
					jumpJets.setText("Jump Jets: " + loadout.getJumpJetCount() + "/" + loadout.getJumpJetsMax() + " ("
							+ df2.format(metricJumpDistance.calculate()) + " m)");
					speedTweak.setSelected(loadout.getEfficiencies().hasSpeedTweak());
					anchorTurn.setSelected(loadout.getEfficiencies().hasAnchorTurn());

					// Heat
					// ----------------------------------------------------------------------
					coolRun.setSelected(loadout.getEfficiencies().hasCoolRun());
					heatContainment.setSelected(loadout.getEfficiencies().hasHeatContainment());
					if (!coolRun.isSelected() || !heatContainment.isSelected()) {
						doubleBasics.setSelected(false);
						doubleBasics.setEnabled(false);
					} else {
						doubleBasics.setEnabled(true);
						doubleBasics.setSelected(loadout.getEfficiencies().hasDoubleBasics());
					}

					if (loadout.getHeatsinksCount() < 10) {
						heatsinks.setForeground(Color.RED);
					} else {
						heatsinks.setForeground(effectiveHS.getForeground());
					}
					heatsinks.setText("Heatsinks: " + loadout.getHeatsinksCount());

					// Offense
					// ----------------------------------------------------------------------
					fastFire.setSelected(loadout.getEfficiencies().hasFastFire());

					inhibitChanges = false;
				}
			}
		});
	}

	private void updateUpgrades() {
		artemis.setSelected(loadout.getUpgrades().getGuidance() != UpgradeDB.STANDARD_GUIDANCE);
		endoSteel.setSelected(loadout.getUpgrades().getStructure() != UpgradeDB.STANDARD_STRUCTURE);
		ferroFibros.setSelected(loadout.getUpgrades().getArmor() != UpgradeDB.STANDARD_ARMOR);
		doubleHeatSinks.setSelected(loadout.getUpgrades().getHeatSink() != UpgradeDB.STANDARD_HEATSINKS);

		boolean isClan = loadout.getChassis().getFaction() == Faction.Clan;

		StructureUpgrade es = isClan ? UpgradeDB.CLAN_ENDO_STEEL_STRUCTURE : UpgradeDB.ENDO_STEEL_STRUCTURE;
		ArmorUpgrade ff = isClan ? UpgradeDB.CLAN_FERRO_FIBROUS_ARMOR : UpgradeDB.FERRO_FIBROUS_ARMOR;

		{
			final String esSavedMass = df2.format(UpgradeDB.STANDARD_STRUCTURE.getStructureMass(loadout.getChassis())
					- es.getStructureMass(loadout.getChassis()));
			final String esSlots = Integer.toString(es.getExtraSlots());
			if ((loadout.getUpgrades().getStructure() == es)) {
				endoSteel.setText("<html>Endo-Steel<br>(<span style=\"color: green;\">-" + esSavedMass + "t</span>, "
						+ "<span style=\"color: red;\">+" + esSlots + "s</span>)" + "</html>");
			} else {
				endoSteel.setText("<html>Endo-Steel<br>(<span style=\"color: gray;\">-" + esSavedMass + "t</span>, "
						+ "<span style=\"color: gray;\">+" + esSlots + "s</span>)" + "</html>");
			}
		}

		{
			final String ffSavedMass = df2.format(UpgradeDB.STANDARD_ARMOR.getArmorMass(loadout.getArmor())
					- ff.getArmorMass(loadout.getArmor()));
			final String ffSlots = Integer.toString(ff.getExtraSlots());
			if (loadout.getUpgrades().getArmor() == ff) {
				ferroFibros.setText("<html>Ferro-Fibrous<br>(<span style=\"color: green;\">-" + ffSavedMass
						+ "t</span>, " + "<span style=\"color: red;\">+" + ffSlots + "s</span>)" + "</html>");
			} else {
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
			} else {
				artemis.setText("<html>Artemis IV<br>(<span style=\"color: gray;\">+" + artemisMass + "t</span>, "
						+ "<span style=\"color: gray;\">+" + artemisSlots + "s</span>)" + "</html>");
			}
		}
	}

	private void setupUpgrades() {
		Insets upgradeInsets = new Insets(0, 0, 0, 0);
		ferroFibros.setVerticalTextPosition(SwingConstants.TOP);
		ferroFibros.setMargin(upgradeInsets);
		ferroFibros.setMultiClickThreshhold(100);
		endoSteel.setVerticalTextPosition(SwingConstants.TOP);
		endoSteel.setMargin(upgradeInsets);
		endoSteel.setMultiClickThreshhold(100);
		artemis.setVerticalTextPosition(SwingConstants.TOP);
		artemis.setMargin(upgradeInsets);
		artemis.setMultiClickThreshhold(100);

		if (loadout instanceof LoadoutOmniMech) {
			ferroFibros.setEnabled(false);
			endoSteel.setEnabled(false);
			doubleHeatSinks.setEnabled(false);
		} else if (loadout instanceof LoadoutStandard) {
			final LoadoutStandard loadoutStandard = (LoadoutStandard) loadout;
			endoSteel.setAction(new AbstractAction() {
				private static final long	serialVersionUID	= 1L;

				@Override
				public void actionPerformed(ActionEvent aE) {
					final StructureUpgrade structure;
					if (loadout.getChassis().getFaction() == Faction.Clan) {
						structure = endoSteel.isSelected() ? UpgradeDB.CLAN_ENDO_STEEL_STRUCTURE
								: UpgradeDB.CLAN_STANDARD_STRUCTURE;
					} else {
						structure = endoSteel.isSelected() ? UpgradeDB.ENDO_STEEL_STRUCTURE
								: UpgradeDB.STANDARD_STRUCTURE;
					}
					try {
						opStack.pushAndApply(new OpSetStructureType(xBar, loadoutStandard, structure));
					} catch (IllegalArgumentException e) {
						endoSteel.setSelected(!endoSteel.isSelected());
						JOptionPane.showMessageDialog(ProgramInit.lsml(), e.getMessage());
					}
				}
			});

			ferroFibros.setAction(new AbstractAction() {
				private static final long	serialVersionUID	= 1L;

				@Override
				public void actionPerformed(ActionEvent aE) {
					final ArmorUpgrade armor;
					if (loadout.getChassis().getFaction() == Faction.Clan) {
						armor = ferroFibros.isSelected() ? UpgradeDB.CLAN_FERRO_FIBROUS_ARMOR
								: UpgradeDB.CLAN_STANDARD_ARMOR;
					} else {
						armor = ferroFibros.isSelected() ? UpgradeDB.FERRO_FIBROUS_ARMOR : UpgradeDB.STANDARD_ARMOR;
					}
					try {
						opStack.pushAndApply(new OpSetArmorType(xBar, loadoutStandard, armor));
					} catch (IllegalArgumentException e) {
						ferroFibros.setSelected(!ferroFibros.isSelected());
						JOptionPane.showMessageDialog(ProgramInit.lsml(), e.getMessage());
					}
				}
			});

			doubleHeatSinks.setAction(new AbstractAction("Double Heat Sinks") {
				private static final long	serialVersionUID	= 1L;

				@Override
				public void actionPerformed(ActionEvent aE) {
					final HeatSinkUpgrade heatSink;
					if (loadout.getChassis().getFaction() == Faction.Clan) {
						heatSink = doubleHeatSinks.isSelected() ? UpgradeDB.CLAN_DOUBLE_HEATSINKS
								: UpgradeDB.CLAN_STANDARD_HEATSINKS;
					} else {
						heatSink = doubleHeatSinks.isSelected() ? UpgradeDB.DOUBLE_HEATSINKS
								: UpgradeDB.STANDARD_HEATSINKS;
					}
					try {
						opStack.pushAndApply(new OpSetHeatSinkType(xBar, loadoutStandard, heatSink));
					} catch (IllegalArgumentException e) {
						doubleHeatSinks.setSelected(!doubleHeatSinks.isSelected());
						JOptionPane.showMessageDialog(ProgramInit.lsml(), e.getMessage());
					}
				}
			});
		}

		artemis.setAction(new AbstractAction() {
			private static final long	serialVersionUID	= 1L;

			@Override
			public void actionPerformed(ActionEvent aE) {
				try {
					opStack.pushAndApply(new OpSetGuidanceType(xBar, loadout,
							artemis.isSelected() ? UpgradeDB.ARTEMIS_IV : UpgradeDB.STANDARD_GUIDANCE));
				} catch (IllegalArgumentException e) {
					artemis.setSelected(false); // Disabling can never fail
					JOptionPane.showMessageDialog(ProgramInit.lsml(), e.getMessage());
				}
			}
		});

		updateUpgrades();
	}

	@Override
	public void itemStateChanged(ItemEvent anEvent) {
		synchronized (inhibitChanges) {
			if (inhibitChanges)
				return;
		}

		JCheckBox source = (JCheckBox) anEvent.getSource();

		try {
			if (source == speedTweak) {
				loadout.getEfficiencies().setSpeedTweak(anEvent.getStateChange() == ItemEvent.SELECTED, xBar);
			} else if (source == anchorTurn) {
				loadout.getEfficiencies().setAnchorTurn(anEvent.getStateChange() == ItemEvent.SELECTED, xBar);
			} else if (source == coolRun) {
				loadout.getEfficiencies().setCoolRun(anEvent.getStateChange() == ItemEvent.SELECTED, xBar);
			} else if (source == heatContainment) {
				loadout.getEfficiencies().setHeatContainment(anEvent.getStateChange() == ItemEvent.SELECTED, xBar);
			} else if (source == doubleBasics) {
				loadout.getEfficiencies().setDoubleBasics(anEvent.getStateChange() == ItemEvent.SELECTED, xBar);
			} else if (source == fastFire) {
				loadout.getEfficiencies().setFastFire(anEvent.getStateChange() == ItemEvent.SELECTED, xBar);
			} else {
				throw new RuntimeException("Unknown source control!");
			}
		} catch (IllegalArgumentException e) {
			JOptionPane.showMessageDialog(ProgramInit.lsml(), e.getMessage());
		} catch (RuntimeException e) {
			JOptionPane.showMessageDialog(ProgramInit.lsml(),
					"Error while changing upgrades or efficiency!: " + e.getMessage());
		}
	}

	@Override
	public void receive(Message aMsg) {
		if (aMsg.isForMe(loadout)) {
			updateDisplay();
		}
	}

	public MaxSustainedDPS getMaxSustainedDPSMetric() {
		return metricSustainedDps;
	}
}
