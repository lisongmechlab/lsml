/*
 * @formatter:off
 * Li Song Mech Lab - A 'mech building tool for PGI's MechWarrior: Online.
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
package lisong_mechlab.view;

import java.awt.Component;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.SwingUtilities;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.TableCellRenderer;

import lisong_mechlab.model.chassi.Chassi;
import lisong_mechlab.model.chassi.ChassiClass;
import lisong_mechlab.model.chassi.ChassiDB;
import lisong_mechlab.model.chassi.HardpointType;
import lisong_mechlab.model.loadout.Loadout;
import lisong_mechlab.model.loadout.metrics.TopSpeed;
import lisong_mechlab.view.render.StyleManager;

/**
 * Displays all available {@link Chassi} in a pane.
 * 
 * @author Li Song
 */
public class ChassiListView extends JScrollPane{
   static public class ChassiTableModel extends AbstractTableModel{
      private static final long          serialVersionUID = -2726840937519789976L;

      private final List<Chassi>         lights           = new ArrayList<>();
      private final List<Chassi>         mediums          = new ArrayList<>();
      private final List<Chassi>         heavies          = new ArrayList<>();
      private final List<Chassi>         assaults         = new ArrayList<>();

      private final List<TableColumn<?>> columns          = new ArrayList<>();

      static class NameColumn extends TableColumn<Chassi>{
         public NameColumn(){
            super("Chassi", Chassi.class);
         }

         @Override
         public Chassi value(Chassi aChassi){
            return aChassi;
         }
      }

      static class TonsColumn extends TableColumn<String>{
         public TonsColumn(){
            super("Tons", String.class);
         }

         @Override
         public String value(Chassi aChassi){
            return "" + aChassi.getMassMax();
         }
      }

      static class SpeedColumn extends TableColumn<String>{
         DecimalFormat df = new DecimalFormat("###.#");

         public SpeedColumn(){
            super("Max Speed", String.class);
         }

         @Override
         public String value(Chassi aChassi){
            final double maxSpeed = TopSpeed.calculate(aChassi.getEngineMax(), aChassi, 1.0);
            final double maxSpeedTweak = TopSpeed.calculate(aChassi.getEngineMax(), aChassi, 1.1);
            return df.format(maxSpeed) + " kph (" + df.format(maxSpeedTweak) + " kph)";
         }
      }

      static class WeaponsColumn extends TableColumn<Chassi>{
         JPanel panel     = new JPanel();
         JLabel energy    = new JLabel();
         JLabel ballistic = new JLabel();
         JLabel missile   = new JLabel();

         public WeaponsColumn(){
            super("Hardpoints", Chassi.class);
            StyleManager.styleThinItem(energy, HardpointType.ENERGY);
            StyleManager.styleThinItem(ballistic, HardpointType.BALLISTIC);
            StyleManager.styleThinItem(missile, HardpointType.MISSILE);
            panel.add(energy);
            panel.add(ballistic);
            panel.add(missile);
         }

         @Override
         public Chassi value(Chassi aChassi){
            return aChassi;
         }

         @Override
         public TableCellRenderer getRenderer(){
            return new TableCellRenderer(){
               @Override
               public Component getTableCellRendererComponent(JTable aTable, Object aValue, boolean aIsSelected, boolean aHasFocus, int aRow,
                                                              int aColumn){
                  Chassi chassi = (Chassi)aValue;
                  int e = chassi.getHardpointsCount(HardpointType.ENERGY);
                  int b = chassi.getHardpointsCount(HardpointType.BALLISTIC);
                  int m = chassi.getHardpointsCount(HardpointType.MISSILE);
                  panel.removeAll();

                  if( e > 0 ){
                     energy.setText(e + " E");
                     panel.add(energy);
                  }
                  if( b > 0 ){
                     ballistic.setText(b + " B");
                     panel.add(ballistic);
                  }
                  if( m > 0 ){
                     missile.setText(m + " M");
                     panel.add(missile);
                  }
                  return panel;
               }
            };
         }
      }

      public ChassiTableModel(){
         columns.add(new NameColumn());
         columns.add(new SpeedColumn());
         columns.add(new TonsColumn());
         columns.add(new WeaponsColumn());

         Comparator<Chassi> cmp = new Comparator<Chassi>(){
            @Override
            public int compare(Chassi aArg0, Chassi aArg1){
               if( aArg0.getMassMax() == aArg1.getMassMax() )
                  return aArg0.getMwoName().compareTo(aArg1.getMwoName());
               return Integer.compare(aArg0.getMassMax(), aArg1.getMassMax());
            }
         };

         lights.addAll(ChassiDB.lookup(ChassiClass.LIGHT));
         Collections.sort(lights, cmp);

         mediums.addAll(ChassiDB.lookup(ChassiClass.MEDIUM));
         Collections.sort(mediums, cmp);

         heavies.addAll(ChassiDB.lookup(ChassiClass.HEAVY));
         Collections.sort(heavies, cmp);

         assaults.addAll(ChassiDB.lookup(ChassiClass.ASSAULT));
         Collections.sort(assaults, cmp);
      }

      @Override
      public Class<?> getColumnClass(int columnIndex){
         return columns.get(columnIndex).getClass();
      }

      @Override
      public String getColumnName(int col){
         return columns.get(col).header();
      }

      @Override
      public int getColumnCount(){
         return columns.size();
      }

      @Override
      public int getRowCount(){
         return lights.size() + mediums.size() + heavies.size() + assaults.size();
      }

      @Override
      public Object getValueAt(int row, int col){
         if( row < lights.size() )
            return columns.get(col).value(lights.get(row));

         row -= lights.size();
         if( row >= 0 && row < mediums.size() )
            return columns.get(col).value(mediums.get(row));

         row -= mediums.size();
         if( row >= 0 && row < heavies.size() )
            return columns.get(col).value(heavies.get(row));

         row -= heavies.size();
         if( row >= 0 && row < assaults.size() )
            return columns.get(col).value(assaults.get(row));

         return "";
      }
   }

   private static final long serialVersionUID = -4134588793726908789L;

   public ChassiListView(){
      super();

      final JTable table = new JTable(new ChassiTableModel());
      table.setDefaultRenderer(ChassiTableModel.WeaponsColumn.class, new ChassiTableModel.WeaponsColumn().getRenderer());
      table.setRowHeight(30);
      table.addMouseListener(new MouseAdapter(){
         @Override
         public void mouseClicked(MouseEvent e){
            if( SwingUtilities.isLeftMouseButton(e) && e.getClickCount() >= 2 ){
               final JTable target = (JTable)e.getSource();
               final int row = target.getSelectedRow();
               final int column = target.getSelectedColumn();
               final Object cell = target.getValueAt(row, column);
               if( cell instanceof Chassi ){
                  Chassi chassi = (Chassi)cell;
                  ProgramInit.lsml().mechLabPane.openLoadout(new Loadout(chassi, ProgramInit.lsml().xBar));
               }
            }
         }
      });

      setViewportView(table);
   }

}
