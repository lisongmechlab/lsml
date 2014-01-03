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

import java.awt.Component;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Arrays;
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
import javax.swing.table.TableColumn;

import lisong_mechlab.model.chassi.Chassi;
import lisong_mechlab.model.chassi.ChassiClass;
import lisong_mechlab.model.chassi.ChassiDB;
import lisong_mechlab.model.chassi.HardpointType;
import lisong_mechlab.model.chassi.Part;
import lisong_mechlab.model.item.ItemDB;
import lisong_mechlab.model.loadout.Loadout;
import lisong_mechlab.model.loadout.metrics.TopSpeed;
import lisong_mechlab.view.mechlab.PartPanel;
import lisong_mechlab.view.render.StyleManager;

/**
 * Displays all available {@link Chassi} in a pane.
 * 
 * @author Emily Björk
 */
public class ChassiSelectionPane extends JScrollPane{
   static public class ChassiTableModel extends AbstractTableModel{
      private static final long  serialVersionUID = -2726840937519789976L;
      private final List<Chassi> lights           = new ArrayList<>();
      private final List<Chassi> mediums          = new ArrayList<>();
      private final List<Chassi> heavies          = new ArrayList<>();
      private final List<Chassi> assaults         = new ArrayList<>();

      public ChassiTableModel(){
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
      public int getColumnCount(){
         return 1;
      }

      @Override
      public int getRowCount(){
         return lights.size() + mediums.size() + heavies.size() + assaults.size();
      }

      @Override
      public Object getValueAt(int row, int col){
         if( row < lights.size() )
            return lights.get(row);

         row -= lights.size();
         if( row >= 0 && row < mediums.size() )
            return mediums.get(row);

         row -= mediums.size();
         if( row >= 0 && row < heavies.size() )
            return heavies.get(row);

         row -= heavies.size();
         if( row >= 0 && row < assaults.size() )
            return assaults.get(row);

         return "";
      }
   }

   static class NameColumn extends AttributeTableColumn{
      private static final long serialVersionUID = -816217603635882304L;

      public NameColumn(){
         super("Chassi", 0);
      }

      @Override
      public String valueOf(Object aSourceRowObject){
         return ((Chassi)aSourceRowObject).getName();
      }
   }

   static class TonsColumn extends AttributeTableColumn{
      private static final long serialVersionUID = -3845466109033447928L;

      public TonsColumn(){
         super("Tons", 0);
      }

      @Override
      public String valueOf(Object aSourceRowObject){
         return Integer.toString(((Chassi)aSourceRowObject).getMassMax());
      }
   }

   static class JumpJetsColumn extends TableColumn{
      private static final long serialVersionUID = -3845466109033447928L;
      private final JPanel      panel            = new JPanel();
      private final JLabel      jjs              = new JLabel();

      public JumpJetsColumn(){
         super(0);
         setHeaderValue("Jump Jets");
         StyleManager.styleThinItem(jjs, ItemDB.lookup("JUMP JETS - CLASS V"));
      }
      
      @Override
      public TableCellRenderer getCellRenderer(){
         return new TableCellRenderer(){
            @Override
            public Component getTableCellRendererComponent(JTable aTable, Object aValue, boolean aIsSelected, boolean aHasFocus, int aRow, int aColumn){
               Chassi chassi = (Chassi)aValue;
               panel.removeAll();
               
               int jjsa = chassi.getMaxJumpJets();

               if( jjsa > 0 ){
                  jjs.setText(jjsa + " JJ");
                  panel.add(jjs);
               }
               return panel;
            }
         };
      }
   }

   static class SpeedColumn extends AttributeTableColumn{
      private static final long serialVersionUID = -1453377097733119292L;
      DecimalFormat             df               = new DecimalFormat("###.#");

      public SpeedColumn(){
         super("Max Speed", 0);
      }

      @Override
      public String valueOf(Object aSourceRowObject){
         Chassi chassi = (Chassi)aSourceRowObject;
         final double maxSpeed = TopSpeed.calculate(chassi.getEngineMax(), chassi, 1.0);
         final double maxSpeedTweak = TopSpeed.calculate(chassi.getEngineMax(), chassi, 1.1);
         return df.format(maxSpeed) + " kph (" + df.format(maxSpeedTweak) + " kph)";
      }
   }

   static class PartColumn extends TableColumn{
      private static final long serialVersionUID = -6290392366218233232L;
      private final JPanel      panel            = new JPanel();
      private final JLabel      energy           = new JLabel();
      private final JLabel      ballistic        = new JLabel();
      private final JLabel      missile          = new JLabel();
      private final JLabel      ams              = new JLabel();
      private final JLabel      ecm              = new JLabel();
      private final Part        part;

      public PartColumn(Part aPart){
         super(0);
         setHeaderValue(aPart.longName());
         StyleManager.styleThinItem(energy, HardpointType.ENERGY);
         StyleManager.styleThinItem(ballistic, HardpointType.BALLISTIC);
         StyleManager.styleThinItem(missile, HardpointType.MISSILE);
         StyleManager.styleThinItem(ams, HardpointType.AMS);
         StyleManager.styleThinItem(ecm, HardpointType.ECM);
         part = aPart;
      }

      @Override
      public TableCellRenderer getCellRenderer(){
         return new TableCellRenderer(){
            @Override
            public Component getTableCellRendererComponent(JTable aTable, Object aValue, boolean aIsSelected, boolean aHasFocus, int aRow, int aColumn){
               Chassi chassi = (Chassi)aValue;
               int e = chassi.getInternalPart(part).getNumHardpoints(HardpointType.ENERGY);
               int b = chassi.getInternalPart(part).getNumHardpoints(HardpointType.BALLISTIC);
               int m = chassi.getInternalPart(part).getNumHardpoints(HardpointType.MISSILE);
               int a = chassi.getInternalPart(part).getNumHardpoints(HardpointType.AMS);
               int c = chassi.getInternalPart(part).getNumHardpoints(HardpointType.ECM);
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
                  missile.setText(PartPanel.formatMissileHardpointText(chassi.getInternalPart(part)));
                  panel.add(missile);
               }
               if( a > 0 ){
                  ams.setText("AMS");
                  panel.add(ams);
               }
               if( c > 0 ){
                  ecm.setText("ECM");
                  panel.add(ecm);
               }
               return panel;
            }
         };
      }
   }

   private static final long serialVersionUID = -4134588793726908789L;

   public ChassiSelectionPane(){
      final JTable table = new JTable(new ChassiTableModel());
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
                  ProgramInit.lsml().tabbedPane.setSelectedComponent(ProgramInit.lsml().mechLabPane);
                  ProgramInit.lsml().mechLabPane.openLoadout(new Loadout(chassi, ProgramInit.lsml().xBar, ProgramInit.lsml().undoStack));
               }
            }
         }
      });

      table.removeColumn(table.getColumnModel().getColumn(0)); // Remove auto-generated column
      table.addColumn(new NameColumn());
      table.addColumn(new SpeedColumn());
      table.addColumn(new TonsColumn());
      for(Part part : Arrays.asList(Part.RightArm, Part.RightTorso, Part.CenterTorso, Part.LeftTorso, Part.LeftArm, Part.Head)){
         table.addColumn(new PartColumn(part));
      }
      table.addColumn(new JumpJetsColumn());

      setViewportView(table);
   }

}
