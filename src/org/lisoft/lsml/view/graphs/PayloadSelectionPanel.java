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
package org.lisoft.lsml.view.graphs;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Vector;

import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.ButtonGroup;
import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JScrollPane;
import javax.swing.ListCellRenderer;
import javax.swing.ListSelectionModel;
import javax.swing.UIManager;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

import org.lisoft.lsml.model.chassi.ChassisBase;
import org.lisoft.lsml.model.chassi.ChassisClass;
import org.lisoft.lsml.model.chassi.ChassisOmniMech;
import org.lisoft.lsml.model.chassi.ChassisStandard;
import org.lisoft.lsml.model.datacache.ChassisDB;
import org.lisoft.lsml.model.datacache.UpgradeDB;
import org.lisoft.lsml.model.metrics.PayloadStatistics;
import org.lisoft.lsml.model.upgrades.UpgradesMutable;

/**
 * Draws the panel where one can select chassis by payload tonnage.
 * 
 * @author Emily Björk
 */
public class PayloadSelectionPanel extends JPanel {
    private static class PayloadSettingsPanel extends JPanel {
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
        private final JList<Collection<ChassisBase>> graphEntries;

        public PayloadSettingsPanel(Collection<Collection<ChassisBase>> aChassis) {
            setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
            graphEntries = new JList<>(new Vector<>(aChassis));
            engineGroup.add(stdEngine);
            engineGroup.add(xlEngine);
            armorGroup.add(noArmor);
            armorGroup.add(maxArmor);

            graphEntries.setCellRenderer(new ListCellRenderer<Collection<ChassisBase>>() {
                @Override
                public Component getListCellRendererComponent(JList<? extends Collection<ChassisBase>> aList,
                        Collection<ChassisBase> aValue, int aIndex, boolean aIsSelected, boolean aCellHasFocus) {

                    List<ChassisBase> uniqueSeries = new ArrayList<>();
                    for (ChassisBase chassis : aValue) {
                        boolean shouldAdd = true;
                        for (ChassisBase uniqueChassis : uniqueSeries) {
                            if (chassis.isSameSeries(uniqueChassis)) {
                                shouldAdd = false;
                                break;
                            }
                        }
                        if (shouldAdd) {
                            uniqueSeries.add(chassis);
                        }
                    }

                    StringBuilder sb = new StringBuilder();
                    boolean first = true;
                    for (ChassisBase chassis : uniqueSeries) {
                        if (!first)
                            sb.append(", ");
                        first = false;
                        sb.append(chassis.getSeriesName());
                    }

                    JLabel label = new JLabel(sb.toString());
                    if (aIsSelected) {
                        label.setForeground(UIManager.getColor("Tree.selectionForeground"));
                        label.setBackground(UIManager.getColor("Tree.selectionBackground"));
                        label.setOpaque(true);
                    }
                    return label;
                }
            });

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
                    aUpgrades.setStructure(
                            endoSteel.isSelected() ? UpgradeDB.ENDO_STEEL_STRUCTURE : UpgradeDB.STANDARD_STRUCTURE);
                    aGraphPanel.updateGraph();
                }
            });
            ferroFibrous.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent aE) {
                    aUpgrades.setArmor(
                            ferroFibrous.isSelected() ? UpgradeDB.FERRO_FIBROUS_ARMOR : UpgradeDB.STANDARD_ARMOR);
                    aGraphPanel.updateGraph();
                }
            });

        }
    }

    private static final long                   serialVersionUID = 1L;

    private final UpgradesMutable               upgrades;
    private final PayloadGraphPanel             graphPanel;
    private final PayloadStatistics             payloadStatistics;
    private Collection<Collection<ChassisBase>> chassisGroups;

    public PayloadSelectionPanel() {
        setLayout(new BorderLayout());

        chassisGroups = calculateUniqueSpeedChassis();
        PayloadSettingsPanel settingsPanel = new PayloadSettingsPanel(chassisGroups);

        boolean xlEngine = false;
        boolean maxArmor = true;

        upgrades = new UpgradesMutable(UpgradeDB.FERRO_FIBROUS_ARMOR, UpgradeDB.ENDO_STEEL_STRUCTURE,
                UpgradeDB.ARTEMIS_IV, UpgradeDB.DOUBLE_HEATSINKS);
        payloadStatistics = new PayloadStatistics(xlEngine, maxArmor, upgrades);
        graphPanel = new PayloadGraphPanel(payloadStatistics, settingsPanel.speedTweak);
        graphPanel.selectChassis(chassisGroups);
        graphPanel.updateGraph();

        settingsPanel.endoSteel.setSelected(upgrades.getStructure() != UpgradeDB.STANDARD_STRUCTURE);
        settingsPanel.ferroFibrous.setSelected(upgrades.getArmor() != UpgradeDB.STANDARD_ARMOR);
        if (maxArmor)
            settingsPanel.maxArmor.setSelected(true);
        else
            settingsPanel.noArmor.setSelected(true);

        if (xlEngine)
            settingsPanel.xlEngine.setSelected(true);
        else
            settingsPanel.stdEngine.setSelected(true);

        settingsPanel.setupListeners(payloadStatistics, graphPanel, upgrades);

        add(settingsPanel, BorderLayout.WEST);
        add(graphPanel, BorderLayout.CENTER);
    }

    /**
     * Groups all chassis available by their speed ranges and std/omni mech.
     * 
     * @return
     */
    private Collection<Collection<ChassisBase>> calculateUniqueSpeedChassis() {
        Collection<Collection<ChassisBase>> collectionOfGroup = new ArrayList<>();

        List<ChassisBase> all = new ArrayList<>();
        for (ChassisClass chassisClass : ChassisClass.values()) {
            all.addAll(ChassisDB.lookup(chassisClass));
        }

        Collections.sort(all, new Comparator<ChassisBase>() {
            @Override
            public int compare(ChassisBase aO1, ChassisBase aO2) {
                return Integer.compare(aO1.getMassMax(), aO2.getMassMax());
            }
        });

        for (ChassisBase chassis : all) {
            if (chassis.getVariantType().isVariation())
                continue;

            boolean skip = false;
            for (Collection<ChassisBase> chassiGroup : collectionOfGroup) {
                ChassisBase representant = chassiGroup.iterator().next();
                if (chassis instanceof ChassisStandard && representant instanceof ChassisStandard) {
                    ChassisStandard ch_rep = (ChassisStandard) chassis;
                    ChassisStandard ch_std = (ChassisStandard) representant;
                    if (ch_std.getMassMax() == ch_rep.getMassMax() && ch_std.getMovementProfileBase()
                            .getMaxMovementSpeed(null) == ch_rep.getMovementProfileBase().getMaxMovementSpeed(null)) {
                        chassiGroup.add(ch_rep);
                        skip = true;
                        break;
                    }
                }
                else if (chassis instanceof ChassisOmniMech && representant instanceof ChassisOmniMech) {
                    ChassisOmniMech ch_omn = (ChassisOmniMech) chassis;
                    ChassisOmniMech ch_rep = (ChassisOmniMech) representant;
                    if (ch_rep.getMassMax() == ch_omn.getMassMax() && ch_rep.getFixedEngine() == ch_omn.getFixedEngine()
                            && ch_rep.getMovementProfileBase().getMaxMovementSpeed(null) == ch_omn
                                    .getMovementProfileBase().getMaxMovementSpeed(null)) {
                        chassiGroup.add(ch_omn);
                        skip = true;
                        break;
                    }
                }
            }
            if (!skip) {
                collectionOfGroup.add(new ArrayList<ChassisBase>(Arrays.asList(chassis)));
            }
        }
        return collectionOfGroup;
    }
}
