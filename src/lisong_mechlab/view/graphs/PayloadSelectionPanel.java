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
package lisong_mechlab.view.graphs;

import java.awt.BorderLayout;
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
import javax.swing.JScrollPane;
import javax.swing.ListSelectionModel;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

import lisong_mechlab.model.chassi.ChassisBase;
import lisong_mechlab.model.chassi.ChassisClass;
import lisong_mechlab.model.chassi.ChassisDB;
import lisong_mechlab.model.chassi.ChassisOmniMech;
import lisong_mechlab.model.chassi.ChassisStandard;
import lisong_mechlab.model.metrics.PayloadStatistics;
import lisong_mechlab.model.upgrades.OpSetArmorType;
import lisong_mechlab.model.upgrades.OpSetStructureType;
import lisong_mechlab.model.upgrades.UpgradeDB;
import lisong_mechlab.model.upgrades.UpgradesMutable;
import lisong_mechlab.util.OperationStack;
import lisong_mechlab.view.graphs.PayloadGraphPanel.Entry;

/**
 * Draws the panel where one can select chassis by payload tonnage.
 * 
 * @author Li Song
 */
public class PayloadSelectionPanel extends JPanel {
	private static class PayloadSettingsPanel extends JPanel {
		private static final long serialVersionUID = 4965116372512246203L;
		private final JRadioButton xlEngine = new JRadioButton("XL engine", false);
		private final JRadioButton stdEngine = new JRadioButton("STD engine", true);
		private final ButtonGroup engineGroup = new ButtonGroup();
		private final JRadioButton noArmor = new JRadioButton("0% armor", false);
		private final JRadioButton maxArmor = new JRadioButton("100% armor", true);
		private final ButtonGroup armorGroup = new ButtonGroup();
		private final JCheckBox endoSteel = new JCheckBox("Endo-Steel");
		private final JCheckBox ferroFibrous = new JCheckBox("Ferro-Fibrous");
		private final JCheckBox speedTweak = new JCheckBox("Speed Tweak");
		private final JList<PayloadGraphPanel.Entry> graphEntries;

		public PayloadSettingsPanel(Collection<Entry> aChassis) {
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
			JScrollPane scrollPane = new JScrollPane(graphEntries);
			entriesPanel.add(scrollPane);
			add(entriesPanel);

			graphEntries.setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
			graphEntries.addSelectionInterval(0, graphEntries.getModel().getSize() - 1);
		}

		void setupListeners(final PayloadStatistics aPayloadStatistics, final PayloadGraphPanel aGraphPanel,
				final UpgradesMutable aUpgrades) {
			final OperationStack stack = new OperationStack(0);

			graphEntries.getSelectionModel().addListSelectionListener(new ListSelectionListener() {
				@Override
				public void valueChanged(ListSelectionEvent aArg0) {
					aGraphPanel.selectChassis(graphEntries.getSelectedValuesList());
					aGraphPanel.updateGraph();
				}
			});

			xlEngine.addActionListener(new ActionListener() {
				@Override
				public void actionPerformed(ActionEvent aArg0) {
					aPayloadStatistics.changeUseXLEngine(xlEngine.isSelected());
					aGraphPanel.updateGraph();
				}
			});
			stdEngine.addActionListener(new ActionListener() {
				@Override
				public void actionPerformed(ActionEvent aE) {
					aPayloadStatistics.changeUseXLEngine(!stdEngine.isSelected());
					aGraphPanel.updateGraph();
				}
			});
			noArmor.addActionListener(new ActionListener() {
				@Override
				public void actionPerformed(ActionEvent aE) {
					aPayloadStatistics.changeUseMaxArmor(!noArmor.isSelected());
					aGraphPanel.updateGraph();
				}
			});
			maxArmor.addActionListener(new ActionListener() {
				@Override
				public void actionPerformed(ActionEvent aE) {
					aPayloadStatistics.changeUseMaxArmor(maxArmor.isSelected());
					aGraphPanel.updateGraph();
				}
			});
			endoSteel.addActionListener(new ActionListener() {
				@Override
				public void actionPerformed(ActionEvent aE) {
					stack.pushAndApply(new OpSetStructureType(aUpgrades,
							endoSteel.isSelected() ? UpgradeDB.ENDO_STEEL_STRUCTURE : UpgradeDB.STANDARD_STRUCTURE));
					aGraphPanel.updateGraph();
				}
			});
			ferroFibrous.addActionListener(new ActionListener() {
				@Override
				public void actionPerformed(ActionEvent aE) {
					stack.pushAndApply(new OpSetArmorType(aUpgrades,
							ferroFibrous.isSelected() ? UpgradeDB.FERRO_FIBROUS_ARMOR : UpgradeDB.STANDARD_ARMOR));
					aGraphPanel.updateGraph();
				}
			});

		}
	}

	private static final long serialVersionUID = 1L;

	private final UpgradesMutable upgrades;
	private final PayloadGraphPanel graphPanel;
	private final PayloadStatistics payloadStatistics;
	private Collection<PayloadGraphPanel.Entry> chassis;

	public PayloadSelectionPanel() {
		setLayout(new BorderLayout());

		chassis = calculateUniqueSpeedChassis();
		PayloadSettingsPanel settingsPanel = new PayloadSettingsPanel(chassis);

		upgrades = new UpgradesMutable(UpgradeDB.FERRO_FIBROUS_ARMOR, UpgradeDB.ENDO_STEEL_STRUCTURE,
				UpgradeDB.ARTEMIS_IV, UpgradeDB.DOUBLE_HEATSINKS);
		payloadStatistics = new PayloadStatistics(false, true, upgrades);
		graphPanel = new PayloadGraphPanel(payloadStatistics, settingsPanel.speedTweak);
		graphPanel.selectChassis(chassis);
		graphPanel.updateGraph();

		settingsPanel.setupListeners(payloadStatistics, graphPanel, upgrades);

		add(settingsPanel, BorderLayout.WEST);
		add(graphPanel, BorderLayout.CENTER);
	}

	private Collection<PayloadGraphPanel.Entry> calculateUniqueSpeedChassis() {
		Collection<Collection<ChassisBase>> temp = new ArrayList<>();

		List<ChassisBase> all = new ArrayList<>(ChassisDB.lookup(ChassisClass.LIGHT));
		all.addAll(ChassisDB.lookup(ChassisClass.MEDIUM));
		all.addAll(ChassisDB.lookup(ChassisClass.HEAVY));
		all.addAll(ChassisDB.lookup(ChassisClass.ASSAULT));

		Collections.sort(all, new Comparator<ChassisBase>() {
			@Override
			public int compare(ChassisBase aO1, ChassisBase aO2) {
				return Integer.compare(aO1.getMassMax(), aO2.getMassMax());
			}
		});

		for (ChassisBase currentChassis : all) {
			if (currentChassis.getVariantType().isVariation())
				continue;
			boolean skip = false;
			for (Collection<ChassisBase> chassiGroup : temp) {
				ChassisBase aChassi = chassiGroup.iterator().next();
				if (currentChassis instanceof ChassisStandard && aChassi instanceof ChassisStandard) {
					ChassisStandard currentStdChassis = (ChassisStandard) currentChassis;
					ChassisStandard stdChassis = (ChassisStandard) aChassi;
					if (stdChassis.getMassMax() == currentStdChassis.getMassMax()
							&& stdChassis.getEngineMin() == currentStdChassis.getEngineMin()
							&& stdChassis.getEngineMax() == currentStdChassis.getEngineMax()
							&& stdChassis.getMovementProfileBase().getMaxMovementSpeed() == currentStdChassis
									.getMovementProfileBase().getMaxMovementSpeed()
							&& stdChassis.isSameSeries(currentStdChassis)) {
						chassiGroup.add(currentStdChassis);
						skip = true;
						break;
					}
				} else if (currentChassis instanceof ChassisOmniMech && aChassi instanceof ChassisOmniMech) {
					ChassisOmniMech currentOmniChassis = (ChassisOmniMech) currentChassis;
					ChassisOmniMech omniChassis = (ChassisOmniMech) aChassi;
					if (omniChassis.getMassMax() == currentOmniChassis.getMassMax()
							&& omniChassis.getFixedEngine() == currentOmniChassis.getFixedEngine()
							&& omniChassis.getMovementProfileBase().getMaxMovementSpeed() == currentOmniChassis
									.getMovementProfileBase().getMaxMovementSpeed()
							&& omniChassis.isSameSeries(currentOmniChassis)) {
						chassiGroup.add(currentOmniChassis);
						skip = true;
						break;
					}
				}
			}
			if (!skip) {
				temp.add(new ArrayList<ChassisBase>(Arrays.asList(currentChassis)));
			}
		}

		Collection<PayloadGraphPanel.Entry> ans = new ArrayList<>();
		for (Collection<ChassisBase> chassiGroup : temp) {
			ans.add(new PayloadGraphPanel.Entry(chassiGroup));
		}

		return ans;
	}
}
