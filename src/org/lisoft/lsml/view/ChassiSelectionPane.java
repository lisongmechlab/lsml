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
package org.lisoft.lsml.view;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.event.ActionEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import javax.swing.AbstractAction;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.SwingUtilities;
import javax.swing.table.TableCellRenderer;
import javax.swing.table.TableColumn;

import org.lisoft.lsml.messages.Message;
import org.lisoft.lsml.messages.MessageReceiver;
import org.lisoft.lsml.messages.MessageXBar;
import org.lisoft.lsml.model.chassi.ChassisBase;
import org.lisoft.lsml.model.chassi.ChassisClass;
import org.lisoft.lsml.model.chassi.ChassisOmniMech;
import org.lisoft.lsml.model.chassi.ChassisStandard;
import org.lisoft.lsml.model.chassi.HardPointType;
import org.lisoft.lsml.model.chassi.Location;
import org.lisoft.lsml.model.chassi.MovementProfile;
import org.lisoft.lsml.model.chassi.OmniPod;
import org.lisoft.lsml.model.chassi.OmniPodDB;
import org.lisoft.lsml.model.item.Faction;
import org.lisoft.lsml.model.item.ItemDB;
import org.lisoft.lsml.model.item.ModuleSlot;
import org.lisoft.lsml.model.loadout.DefaultLoadoutFactory;
import org.lisoft.lsml.model.loadout.LoadoutBase;
import org.lisoft.lsml.model.metrics.TopSpeed;
import org.lisoft.lsml.model.modifiers.Efficiencies;
import org.lisoft.lsml.model.modifiers.Modifier;
import org.lisoft.lsml.view.models.ChassiTableModel;
import org.lisoft.lsml.view.preferences.Preferences;
import org.lisoft.lsml.view.preferences.UiPreferences;
import org.lisoft.lsml.view.render.ScrollablePanel;
import org.lisoft.lsml.view.render.StyleManager;

/**
 * Displays all available {@link ChassisStandard} in a pane.
 * 
 * @author Li Song
 */
public class ChassiSelectionPane extends JPanel implements MessageReceiver {
    static class NameColumn extends AttributeTableColumn {
        public NameColumn() {
            super("Chassi", 0);
        }

        @Override
        public String valueOf(Object aSourceRowObject) {
            return ((ChassisBase) aSourceRowObject).getName();
        }
    }

    static class TonsColumn extends AttributeTableColumn {
        public TonsColumn() {
            super("Tons", 0);
        }

        @Override
        public String valueOf(Object aSourceRowObject) {
            return Integer.toString(((ChassisBase) aSourceRowObject).getMassMax());
        }
    }

    static class JumpJetsColumn extends TableColumn {
        private final JPanel panel = new JPanel();
        private final JLabel text  = new JLabel();

        public JumpJetsColumn() {
            super(0);
            setHeaderValue("Jump Jets");
            StyleManager.styleThinItem(text, ItemDB.lookup("JUMP JETS - CLASS V"));
        }

        @Override
        public TableCellRenderer getCellRenderer() {
            return new TableCellRenderer() {
                @Override
                public Component getTableCellRendererComponent(JTable aTable, Object aValue, boolean aIsSelected,
                        boolean aHasFocus, int aRow, int aColumn) {
                    ChassisBase chassis = (ChassisBase) aValue;
                    panel.removeAll();

                    final int jjsa;
                    if (chassis instanceof ChassisStandard) {
                        jjsa = ((ChassisStandard) chassis).getJumpJetsMax();
                    }
                    else {
                        ChassisOmniMech omniMech = (ChassisOmniMech) chassis;
                        int jjs = omniMech.getFixedJumpJets();
                        for (OmniPod omniPod : OmniPodDB.lookupOriginal(omniMech)) {
                            jjs += omniPod.getJumpJetsMax();
                        }
                        jjsa = jjs;
                    }

                    if (jjsa > 0) {
                        text.setText(jjsa + " JJ");
                        panel.add(text);
                    }
                    return panel;
                }
            };
        }
    }

    static class PilotModulesColumn extends TableColumn {
        private final JPanel     panel = new JPanel();
        private final JLabel     text  = new JLabel();
        private final ModuleSlot slot;

        public PilotModulesColumn(ModuleSlot aSlot) {
            super(0);
            slot = aSlot;
            panel.add(text);
            setHeaderValue("Modules (" + slot + ")");
        }

        @Override
        public TableCellRenderer getCellRenderer() {
            return new TableCellRenderer() {
                @Override
                public Component getTableCellRendererComponent(JTable aTable, Object aValue, boolean aIsSelected,
                        boolean aHasFocus, int aRow, int aColumn) {
                    ChassisBase chassis = ((ChassisBase) aValue);
                    final int modules;
                    switch (slot) {
                        case CONSUMABLE:
                            modules = chassis.getConsumableModulesMax();
                            break;
                        case MECH:
                            modules = chassis.getMechModulesMax();
                            break;
                        case WEAPON:
                            modules = chassis.getWeaponModulesMax();
                            break;
                        case HYBRID:
                        default:
                            throw new RuntimeException("Bad module type!");
                    }
                    text.setText(Integer.toString(modules));
                    return panel;
                }
            };
        }
    }

    static class SpeedColumn extends AttributeTableColumn {
        DecimalFormat df = new DecimalFormat("###.#");

        public SpeedColumn() {
            super("Max Speed", 0);
        }

        @Override
        public String valueOf(Object aSourceRowObject) {
            List<Modifier> modifiers = new ArrayList<>();
            final int rating;
            if (aSourceRowObject instanceof ChassisStandard) {
                ChassisStandard chassis = (ChassisStandard) aSourceRowObject;
                modifiers.addAll(chassis.getQuirks());
                rating = chassis.getEngineMax();
            }
            else if (aSourceRowObject instanceof ChassisOmniMech) {
                ChassisOmniMech chassis = (ChassisOmniMech) aSourceRowObject;
                modifiers.addAll(chassis.getStockModifiers());
                rating = chassis.getFixedEngine().getRating();
            }
            else {
                throw new IllegalArgumentException("Unknown chassis type!");
            }

            ChassisBase chassis = (ChassisBase) aSourceRowObject;
            MovementProfile mp = chassis.getMovementProfileBase();

            final double maxSpeed = TopSpeed.calculate(rating, mp, chassis.getMassMax(), modifiers);
            modifiers.add(Efficiencies.SPEED_TWEAK);
            final double maxSpeedTweak = TopSpeed.calculate(rating, mp, chassis.getMassMax(), modifiers);
            return df.format(maxSpeed) + " kph (" + df.format(maxSpeedTweak) + " kph)";
        }
    }

    static class PartColumn extends TableColumn {
        private final JPanel   panel     = new JPanel();
        private final JLabel   energy    = new JLabel();
        private final JLabel   ballistic = new JLabel();
        private final JLabel   missile   = new JLabel();
        private final JLabel   ams       = new JLabel();
        private final JLabel   ecm       = new JLabel();
        private final Location part;

        public PartColumn(Location aPart) {
            super(0);
            setHeaderValue(aPart.longName());
            StyleManager.styleThinItem(energy, HardPointType.ENERGY);
            StyleManager.styleThinItem(ballistic, HardPointType.BALLISTIC);
            StyleManager.styleThinItem(missile, HardPointType.MISSILE);
            StyleManager.styleThinItem(ams, HardPointType.AMS);
            StyleManager.styleThinItem(ecm, HardPointType.ECM);

            panel.add(energy);
            panel.add(ballistic);
            panel.add(missile);
            panel.add(ams);
            panel.add(ecm);
            part = aPart;
        }

        @Override
        public TableCellRenderer getCellRenderer() {
            return new TableCellRenderer() {
                @Override
                public Component getTableCellRendererComponent(JTable aTable, Object aValue, boolean aIsSelected,
                        boolean aHasFocus, int aRow, int aColumn) {
                    ChassisBase chassis = (ChassisBase) aValue;
                    LoadoutBase<?> stock;
                    try {
                        stock = DefaultLoadoutFactory.instance.produceStock(chassis);
                    }
                    catch (Exception e) {
                        Thread.getDefaultUncaughtExceptionHandler().uncaughtException(Thread.currentThread(), e);
                        return panel;
                    }
                    StyleManager.styleHardpointLabel(energy, stock.getComponent(part), HardPointType.ENERGY);
                    StyleManager.styleHardpointLabel(ballistic, stock.getComponent(part), HardPointType.BALLISTIC);
                    StyleManager.styleHardpointLabel(missile, stock.getComponent(part), HardPointType.MISSILE);
                    StyleManager.styleHardpointLabel(ams, stock.getComponent(part), HardPointType.AMS);
                    StyleManager.styleHardpointLabel(ecm, stock.getComponent(part), HardPointType.ECM);
                    return panel;
                }
            };
        }
    }

    private final List<JTable> tables = new ArrayList<>();
    private final JCheckBox    hideSpecials;
    private final Preferences  preferences;

    public ChassiSelectionPane(final Preferences aPreferences, MessageXBar aXBar) {
        super(new BorderLayout());
        aXBar.attach(this);

        preferences = aPreferences;
        {
            hideSpecials = new JCheckBox("Hide mech variations", preferences.uiPreferences.getHideSpecialMechs());
            hideSpecials.setToolTipText(
                    "<html>Will hide mech variations (champion, founders, phoenix, sarah, etc) from chassis lists.<br/>"
                            + "Stock loadouts are still available on the \"Load stock\" menu action on relevant loadouts</html>");
            hideSpecials.addActionListener(new AbstractAction() {
                private static final long serialVersionUID = -8136020916897237506L;

                @Override
                public void actionPerformed(ActionEvent aArg0) {
                    aPreferences.uiPreferences.setHideSpecialMechs(hideSpecials.isSelected());
                }
            });
            add(hideSpecials, BorderLayout.NORTH);
        }

        JPanel tablesPanel = new ScrollablePanel();
        tablesPanel.setLayout(new BoxLayout(tablesPanel, BoxLayout.PAGE_AXIS));

        for (Faction faction : new Faction[] { Faction.InnerSphere, Faction.Clan }) {
            for (ChassisClass chassisClass : ChassisClass.values()) {
                if (ChassisClass.COLOSSAL == chassisClass)
                    continue;

                JTable table = new JTable(
                        new ChassiTableModel(faction, chassisClass, aPreferences.uiPreferences.getHideSpecialMechs()));
                table.setRowHeight(30);
                table.addMouseListener(new MouseAdapter() {

                    @Override
                    public void mouseClicked(MouseEvent e) {
                        if (SwingUtilities.isLeftMouseButton(e) && e.getClickCount() >= 2) {
                            final JTable target = (JTable) e.getSource();
                            final int row = target.getSelectedRow();
                            final int column = target.getSelectedColumn();
                            final Object cell = target.getValueAt(row, column);
                            if (cell instanceof ChassisBase) {
                                LoadoutBase<?> loadout = DefaultLoadoutFactory.instance
                                        .produceEmpty((ChassisBase) cell);
                                ProgramInit.lsml().tabbedPane.setSelectedComponent(ProgramInit.lsml().mechLabPane);
                                ProgramInit.lsml().mechLabPane.openLoadout(loadout, false);
                            }
                        }
                    }
                });

                table.removeColumn(table.getColumnModel().getColumn(0)); // Remove auto-generated column
                table.addColumn(new NameColumn());
                table.addColumn(new SpeedColumn());
                table.addColumn(new TonsColumn());
                table.addColumn(new PilotModulesColumn(ModuleSlot.CONSUMABLE));
                table.addColumn(new PilotModulesColumn(ModuleSlot.MECH));
                table.addColumn(new PilotModulesColumn(ModuleSlot.WEAPON));
                for (

                Location part : Arrays.asList(Location.RightArm, Location.RightTorso, Location.CenterTorso,
                        Location.LeftTorso, Location.LeftArm, Location.Head)) {
                    table.addColumn(new PartColumn(part));
                }
                table.addColumn(new JumpJetsColumn());
                tables.add(table);

                JPanel tp = new JPanel(new BorderLayout());
                tp.add(table.getTableHeader(), BorderLayout.NORTH);
                tp.add(table, BorderLayout.CENTER);

                tablesPanel.add(new CollapsiblePanel(faction.toString() + " - " + chassisClass.toString(), tp, true));
            }
        }

        tablesPanel.add(Box.createVerticalGlue());

        JScrollPane js = new JScrollPane(tablesPanel);
        js.getVerticalScrollBar().setUnitIncrement(32);

        add(js, BorderLayout.CENTER);

        add(Box.createVerticalGlue(), BorderLayout.SOUTH);
    }

    @Override
    public void receive(Message aMsg) {
        if (aMsg instanceof UiPreferences.PreferencesMessage) {
            UiPreferences.PreferencesMessage msg = (UiPreferences.PreferencesMessage) aMsg;
            if (msg.attribute == UiPreferences.UI_HIDE_SPECIAL_MECHS) {
                hideSpecials.setSelected(preferences.uiPreferences.getHideSpecialMechs());

                for (JTable table : tables) {
                    ((ChassiTableModel) table.getModel()).recreate(hideSpecials.isSelected());
                }
            }
        }
    }

}
