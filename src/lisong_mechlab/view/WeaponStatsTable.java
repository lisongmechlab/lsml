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
package lisong_mechlab.view;

import java.awt.Color;
import java.awt.Component;
import java.awt.event.MouseEvent;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import javax.swing.JTable;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.JTableHeader;
import javax.swing.table.TableColumnModel;

import lisong_mechlab.model.chassi.HardPointType;
import lisong_mechlab.model.item.ItemDB;
import lisong_mechlab.model.item.Weapon;

/**
 * This class is a JTable which shows statistics for weapons.
 * 
 * @author Emily Björk
 */
public class WeaponStatsTable extends JTable {
    private static final long serialVersionUID = -5418864783866344614L;

    private static class Model extends AbstractTableModel {
        private static final long  serialVersionUID = 2420773283903826604L;
        private final List<Weapon> weapons          = new ArrayList<>();
        private final List<Column> columns          = new ArrayList<>();

        abstract class Column {
            private final String header;
            private final String tooltip;

            public Column(String aHeader, String aTooltip) {
                header = aHeader;
                tooltip = aTooltip;
            }

            public abstract Object valueAt(int aRow);

            public abstract Class<?> getValueClass();

            public String getHeader() {
                return header;
            }

            public String getTooltip() {
                return tooltip;
            }
        }

        class StatColumn extends Column {
            private final String stat;

            public StatColumn(String aHeader, String aTooltip, String aStat) {
                super(aHeader, aTooltip);
                stat = aStat;
            }

            @Override
            public Object valueAt(int aRow) {
                return weapons.get(aRow).getStat(stat, null);
            }

            @Override
            public Class<?> getValueClass() {
                return Double.class;
            }
        }

        public Model(HardPointType aHardpointType) {
            List<Weapon> allweapons = ItemDB.lookup(Weapon.class);

            for (Weapon weapon : allweapons) {
                if (weapon.getHardpointType() == aHardpointType) {
                    weapons.add(weapon);
                }
            }
            Collections.sort(weapons);

            columns.add(new Column("Name", "The name of the weapon system.") {
                @Override
                public Object valueAt(int aRow) {
                    return weapons.get(aRow).getName();
                }

                @Override
                public Class<?> getValueClass() {
                    return String.class;
                }
            });

            columns.add(new StatColumn("Tons", "The mass of the weapon system.", "t"));
            columns.add(new StatColumn("Slots", "The number of critical slots required for the weapon system.", "c"));
            columns.add(new StatColumn("Alpha Strike", "The amount of damage one volley of the weapon will do.", "d"));
            columns.add(new Column("Optimal Range", "The maximal distance the weapon will do full damage at.") {
                @Override
                public Object valueAt(int aRow) {
                    return weapons.get(aRow).getRangeLong(null);
                }

                @Override
                public Class<?> getValueClass() {
                    return Double.class;
                }
            });
            columns.add(new Column("Max Range", "The distance at which the weapons damage becomes zero.") {
                @Override
                public Object valueAt(int aRow) {
                    return weapons.get(aRow).getRangeMax(null);
                }

                @Override
                public Class<?> getValueClass() {
                    return Double.class;
                }
            });
            columns.add(new Column("Projectile Speed", "The speed that the weapon's projectiles travel with.") {
                @Override
                public Object valueAt(int aRow) {
                    double speed = weapons.get(aRow).getProjectileSpeed();
                    if (speed == 0)
                        return Double.POSITIVE_INFINITY;
                    return speed;
                }

                @Override
                public Class<?> getValueClass() {
                    return Double.class;
                }
            });
            columns.add(new StatColumn("Cycle time", "The amount of time needed between consecutive shots.", "s"));
            columns.add(new StatColumn("Heat", "The amount of heat generated by one firing of the weapon.", "h"));
            columns.add(new StatColumn("DPS", "Damage Per Second (DPS).", "d/s"));
            columns.add(new StatColumn("DPH",
                    "Damage Per Heat (DPH). Equivalent to DPS / HPS, i.e. dps per heat generation.", "d/h"));
            columns.add(new StatColumn("DPT", "Damage Per Ton (DPT).", "d/t"));
            columns.add(new StatColumn("DPST", "Damage Per Second and Ton (DPST).", "d/st"));
            columns.add(new StatColumn("HPS", "Heat Per Second (HPS).", "h/s"));
        }

        @Override
        public int getColumnCount() {
            return columns.size();
        }

        @Override
        public String getColumnName(int aColumn) {
            return columns.get(aColumn).getHeader();
        }

        @Override
        public Class<?> getColumnClass(int aColumn) {
            return columns.get(aColumn).getValueClass();
        }

        @Override
        public int getRowCount() {
            return weapons.size();
        }

        @Override
        public Object getValueAt(int aRowIndex, int aColumnIndex) {
            return columns.get(aColumnIndex).valueAt(aRowIndex);
        }

        public String getColumnTooltip(int aColumn) {
            return columns.get(aColumn).getTooltip();
        }
    }

    class ZebraRenderer extends DefaultTableCellRenderer {
        private static final long serialVersionUID = 291815378407610527L;
        private final Color       fg               = new Color(0xf4f6f9);
        private final Color       fg_darker        = new Color(0xe3ebf4);

        @Override
        public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected,
                boolean hasFocus, int row, int column) {
            Component c = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
            if (row % 2 == 0) {
                if (!isSelected) {
                    c.setBackground(fg);
                }
                else {
                    c.setBackground(fg.darker());
                }
            }
            else {
                if (!isSelected) {
                    c.setBackground(fg_darker);
                }
                else {
                    c.setBackground(fg_darker.darker());
                }
            }
            c.setForeground(Color.BLACK);
            return c;
        }
    }

    class DecimalFormatRenderer extends ZebraRenderer {
        private static final long  serialVersionUID = 291815378407610527L;
        private final NumberFormat format;

        public DecimalFormatRenderer(NumberFormat aNumberFormat) {
            format = aNumberFormat;
        }

        @Override
        public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected,
                boolean hasFocus, int row, int column) {
            return super.getTableCellRendererComponent(table, format.format(value), isSelected, hasFocus, row, column);
        }
    }

    public WeaponStatsTable(HardPointType aHardpointType) {
        super(new Model(aHardpointType));
        setAutoCreateRowSorter(true);

        TableColumnModel tcm = getColumnModel();
        NumberFormat format = NumberFormat.getInstance();
        format.setMaximumFractionDigits(3);

        tcm.getColumn(0).setCellRenderer(new ZebraRenderer());
        DecimalFormatRenderer dfr = new DecimalFormatRenderer(format);
        for (int i = 1; i < tcm.getColumnCount(); ++i) {
            tcm.getColumn(i).setCellRenderer(dfr);
        }
    }

    @Override
    protected JTableHeader createDefaultTableHeader() {
        return new JTableHeader(columnModel) {
            private static final long serialVersionUID = 8692956739102109610L;

            @Override
            public String getToolTipText(MouseEvent e) {
                java.awt.Point p = e.getPoint();
                return ((Model) getModel()).getColumnTooltip(convertColumnIndexToModel(columnModel
                        .getColumnIndexAtX(p.x)));
            }
        };
    }
}
