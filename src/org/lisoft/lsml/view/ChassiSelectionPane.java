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
package org.lisoft.lsml.view;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.event.ActionEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.text.DecimalFormat;
import java.util.ArrayList;
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
import javax.swing.table.TableColumnModel;

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
import org.lisoft.lsml.model.modifiers.ModifiersDB;
import org.lisoft.lsml.view.mechlab.FilteredHtmlQuirksRenderingStrategy;
import org.lisoft.lsml.view.mechlab.QuirksRenderingStrategy;
import org.lisoft.lsml.view.models.ChassiTableModel;
import org.lisoft.lsml.view.preferences.Preferences;
import org.lisoft.lsml.view.preferences.PreferencesMessage;
import org.lisoft.lsml.view.preferences.UiPreferences;
import org.lisoft.lsml.view.render.ScrollablePanel;
import org.lisoft.lsml.view.render.StyleManager;

/**
 * Displays all available {@link ChassisStandard} in a pane.
 * 
 * @author Emily Björk
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
        private final JPanel panel = new JPanel(new GridBagLayout());
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

    static class PilotModulesColumn extends AttributeTableColumn {
        private final ModuleSlot slot;

        public PilotModulesColumn(ModuleSlot aSlot) {
            super("Modules (" + aSlot + ")", 0);
            slot = aSlot;
        }

        @Override
        public String valueOf(Object aSourceRowObject) {
            ChassisBase chassis = ((ChassisBase) aSourceRowObject);
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
            return Integer.toString(modules);
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
        private final JPanel   panel     = new JPanel(new GridBagLayout());
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

            GridBagConstraints gbc = new GridBagConstraints();
            gbc.gridx = 0;
            panel.add(energy, gbc);
            gbc.gridx++;
            panel.add(ballistic, gbc);
            gbc.gridx++;
            panel.add(missile, gbc);
            gbc.gridx++;
            panel.add(ams, gbc);
            gbc.gridx++;
            panel.add(ecm, gbc);
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

    static class QuirksColumn extends TableColumn {
        private final static QuirksRenderingStrategy rs = new FilteredHtmlQuirksRenderingStrategy(
                ModifiersDB.getAllWeaponSelectors(), false);

        public QuirksColumn() {
            super(0);
            setHeaderValue("Weapon Quirks");
        }

        @Override
        public TableCellRenderer getCellRenderer() {
            return new TableCellRenderer() {
                @Override
                public Component getTableCellRendererComponent(JTable aTable, Object aValue, boolean aIsSelected,
                        boolean aHasFocus, int aRow, int aColumn) {
                    ChassisBase chassis = (ChassisBase) aValue;
                    LoadoutBase<?> loadout = DefaultLoadoutFactory.instance.produceEmpty(chassis);
                    return rs.render(loadout);
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

        boolean hideSpecialMechs = aPreferences.uiPreferences.getHideSpecialMechs();

        MouseAdapter tableMouseAdapter = new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (SwingUtilities.isLeftMouseButton(e) && e.getClickCount() >= 2) {
                    final JTable target = (JTable) e.getSource();
                    final int row = target.getSelectedRow();
                    final int column = target.getSelectedColumn();
                    final Object cell = target.getValueAt(row, column);
                    if (cell instanceof ChassisBase) {
                        LoadoutBase<?> loadout = DefaultLoadoutFactory.instance.produceEmpty((ChassisBase) cell);
                        ProgramInit.lsml().tabbedPane.setSelectedComponent(ProgramInit.lsml().mechLabPane);
                        ProgramInit.lsml().mechLabPane.openLoadout(loadout, false);
                    }
                }
            }
        };

        for (Faction faction : new Faction[] { Faction.InnerSphere, Faction.Clan }) {
            for (ChassisClass chassisClass : ChassisClass.values()) {
                if (ChassisClass.COLOSSAL == chassisClass)
                    continue;

                final JTable table = new JTable(new ChassiTableModel(faction, chassisClass, hideSpecialMechs));
                table.addMouseListener(tableMouseAdapter);

                table.removeColumn(table.getColumnModel().getColumn(0)); // Remove auto-generated column
                table.addColumn(new NameColumn());
                table.addColumn(new SpeedColumn());
                table.addColumn(new TonsColumn());
                table.addColumn(new PilotModulesColumn(ModuleSlot.CONSUMABLE));
                table.addColumn(new PilotModulesColumn(ModuleSlot.MECH));
                table.addColumn(new PilotModulesColumn(ModuleSlot.WEAPON));
                for (

                Location part : Location.right2Left()) {
                    if (part == Location.LeftLeg || part == Location.RightLeg)
                        continue;
                    table.addColumn(new PartColumn(part));
                }
                table.addColumn(new JumpJetsColumn());
                table.addColumn(new QuirksColumn());
                resizeColumnWidth(table);
                resizeRowHeight(table);
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
        if (aMsg instanceof PreferencesMessage) {
            PreferencesMessage msg = (PreferencesMessage) aMsg;
            if (msg.attribute == UiPreferences.UI_HIDE_SPECIAL_MECHS) {
                hideSpecials.setSelected(preferences.uiPreferences.getHideSpecialMechs());

                for (JTable table : tables) {
                    ((ChassiTableModel) table.getModel()).recreate(hideSpecials.isSelected());
                }
            }
        }
    }

    private void resizeColumnWidth(JTable table) {
        final TableColumnModel columnModel = table.getColumnModel();
        for (int column = 0; column < table.getColumnCount(); column++) {
            int width = 50; // Min width
            for (int row = 0; row < table.getRowCount(); row++) {
                TableCellRenderer renderer = table.getCellRenderer(row, column);
                Component comp = table.prepareRenderer(renderer, row, column);
                width = Math.max(comp.getPreferredSize().width + 1, width);
            }
            columnModel.getColumn(column).setPreferredWidth(width);
        }
    }

    private void resizeRowHeight(JTable table) {
        for (int row = 0; row < table.getRowCount(); row++) {
            int height = 30; // Min height
            for (int col = 0; col < table.getColumnCount(); col++) {
                TableCellRenderer renderer = table.getCellRenderer(row, col);
                Component comp = table.prepareRenderer(renderer, row, col);
                height = Math.max(comp.getPreferredSize().height + 1, height);
            }
            table.setRowHeight(row, height);
        }
    }

}
