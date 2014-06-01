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

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.event.ActionEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;

import javax.swing.AbstractAction;
import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.SwingUtilities;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.TableCellRenderer;
import javax.swing.table.TableColumn;

import lisong_mechlab.model.Efficiencies;
import lisong_mechlab.model.chassi.ChassisBase;
import lisong_mechlab.model.chassi.ChassisClass;
import lisong_mechlab.model.chassi.ChassisDB;
import lisong_mechlab.model.chassi.ChassisOmniMech;
import lisong_mechlab.model.chassi.ChassisStandard;
import lisong_mechlab.model.chassi.HardPointType;
import lisong_mechlab.model.chassi.Location;
import lisong_mechlab.model.item.ItemDB;
import lisong_mechlab.model.loadout.LoadoutBase;
import lisong_mechlab.model.loadout.LoadoutOmniMech;
import lisong_mechlab.model.loadout.LoadoutStandard;
import lisong_mechlab.model.loadout.OpLoadStock;
import lisong_mechlab.model.loadout.component.ComponentBuilder;
import lisong_mechlab.model.metrics.TopSpeed;
import lisong_mechlab.util.MessageXBar;
import lisong_mechlab.util.OperationStack;
import lisong_mechlab.view.preferences.Preferences;
import lisong_mechlab.view.preferences.UiPreferences;
import lisong_mechlab.view.preferences.UiPreferences.Message;
import lisong_mechlab.view.render.StyleManager;

/**
 * Displays all available {@link ChassisStandard} in a pane.
 * 
 * @author Emily Björk
 */
public class ChassiSelectionPane extends JPanel implements MessageXBar.Reader{
   static public class ChassiTableModel extends AbstractTableModel{
      private static final long                 serialVersionUID = -2726840937519789976L;
      private final List<ChassisStandard>       lights           = new ArrayList<>();
      private final List<ChassisStandard>       mediums          = new ArrayList<>();
      private final List<ChassisStandard>       heavies          = new ArrayList<>();
      private final List<ChassisStandard>       assaults         = new ArrayList<>();
      private final Comparator<ChassisStandard> cmp              = new Comparator<ChassisStandard>(){
                                                                    @Override
                                                                    public int compare(ChassisStandard aArg0, ChassisStandard aArg1){
                                                                       if( aArg0.getMassMax() == aArg1.getMassMax() )
                                                                          return aArg0.getMwoName().compareTo(aArg1.getMwoName());
                                                                       return Integer.compare(aArg0.getMassMax(), aArg1.getMassMax());
                                                                    }
                                                                 };

      public ChassiTableModel(boolean aFilterSpecials){
         recreate(aFilterSpecials);
      }

      public void recreate(boolean aFilterSpecials){
         doit(lights, aFilterSpecials, ChassisClass.LIGHT);
         doit(mediums, aFilterSpecials, ChassisClass.MEDIUM);
         doit(heavies, aFilterSpecials, ChassisClass.HEAVY);
         doit(assaults, aFilterSpecials, ChassisClass.ASSAULT);
         fireTableDataChanged();
      }

      private void doit(List<ChassisStandard> aList, boolean aFilterSpecials, ChassisClass aChassiClass){
         Collection<? extends ChassisBase> all = ChassisDB.lookup(aChassiClass);

         aList.clear();
         for(ChassisBase base : all){
            if( base instanceof ChassisStandard )
               aList.add((ChassisStandard)base);
         }

         if( aFilterSpecials ){
            Iterator<ChassisStandard> it = aList.iterator();
            while( it.hasNext() ){
               ChassisStandard c = it.next();
               if( c.getVariantType().isVariation() ){
                  it.remove();
               }
            }
         }
         Collections.sort(aList, cmp);
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
         return ((ChassisStandard)aSourceRowObject).getName();
      }
   }

   static class TonsColumn extends AttributeTableColumn{
      private static final long serialVersionUID = -3845466109033447928L;

      public TonsColumn(){
         super("Tons", 0);
      }

      @Override
      public String valueOf(Object aSourceRowObject){
         return Integer.toString(((ChassisStandard)aSourceRowObject).getMassMax());
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
               ChassisStandard chassi = (ChassisStandard)aValue;
               panel.removeAll();

               int jjsa = chassi.getJumpJetsMax();

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
         if( aSourceRowObject instanceof ChassisStandard ){
            ChassisStandard chassis = (ChassisStandard)aSourceRowObject;

            Efficiencies efficiencies = new Efficiencies((MessageXBar)null);
            efficiencies.setSpeedTweak(false);

            final double maxSpeed = TopSpeed.calculate(chassis.getEngineMax(), chassis.getMovementProfileBase(), chassis.getMassMax(),
                                                       efficiencies.getSpeedModifier());

            efficiencies.setSpeedTweak(true);
            final double maxSpeedTweak = TopSpeed.calculate(chassis.getEngineMax(), chassis.getMovementProfileBase(), chassis.getMassMax(),
                                                            efficiencies.getSpeedModifier());
            return df.format(maxSpeed) + " kph (" + df.format(maxSpeedTweak) + " kph)";
         }
         else if( aSourceRowObject instanceof ChassisOmniMech ){
            ChassisOmniMech chassis = (ChassisOmniMech)aSourceRowObject;

            Efficiencies efficiencies = new Efficiencies((MessageXBar)null);
            efficiencies.setSpeedTweak(false);

            final double maxSpeed = TopSpeed.calculate(chassis.getEngine().getRating(), chassis.getMovementProfileStock(), chassis.getMassMax(),
                                                       efficiencies.getSpeedModifier());

            efficiencies.setSpeedTweak(true);
            final double maxSpeedTweak = TopSpeed.calculate(chassis.getEngine().getRating(), chassis.getMovementProfileStock(), chassis.getMassMax(),
                                                            efficiencies.getSpeedModifier());

            // TODO: Show min-max
            return df.format(maxSpeed) + " kph (" + df.format(maxSpeedTweak) + " kph)";
         }
         else{
            throw new IllegalArgumentException("Unknown chassis type!");
         }
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
      private final Location    part;

      public PartColumn(Location aPart){
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
      public TableCellRenderer getCellRenderer(){
         return new TableCellRenderer(){
            @Override
            public Component getTableCellRendererComponent(JTable aTable, Object aValue, boolean aIsSelected, boolean aHasFocus, int aRow, int aColumn){
               ChassisBase chassi = (ChassisBase)aValue;
               LoadoutBase<?> stock;
               if( aValue instanceof ChassisStandard ){
                  stock = new LoadoutStandard((ChassisStandard)chassi, null);
                  OperationStack stack = new OperationStack(0);
                  stack.pushAndApply(new OpLoadStock(chassi, stock, null));
               }
               else if( aValue instanceof ChassisOmniMech ){
                  stock = new LoadoutOmniMech(ComponentBuilder.getOmniPodFactory(), (ChassisOmniMech)chassi, null);
                  OperationStack stack = new OperationStack(0);
                  stack.pushAndApply(new OpLoadStock(chassi, stock, null));
               }
               else{
                  throw new IllegalArgumentException("Expected a chassis type as value!");
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

   private static final long serialVersionUID = -4134588793726908789L;
   private final JTable      table;
   private final JCheckBox   hideSpecials;
   private final Preferences preferences;

   public ChassiSelectionPane(final Preferences aPreferences, MessageXBar aXBar){
      super(new BorderLayout());
      aXBar.attach(this);

      preferences = aPreferences;
      {
         hideSpecials = new JCheckBox("Hide mech variations", preferences.uiPreferences.getHideSpecialMechs());
         hideSpecials.setToolTipText("<html>Will hide mech variations (champion, founders, phoenix, sarah, etc) from chassis lists.<br/>"
                                     + "Stock loadouts are still available on the \"Load stock\" menu action on relevant loadouts</html>");
         hideSpecials.addActionListener(new AbstractAction(){
            private static final long serialVersionUID = -8136020916897237506L;

            @Override
            public void actionPerformed(ActionEvent aArg0){
               aPreferences.uiPreferences.setHideSpecialMechs(hideSpecials.isSelected());
            }
         });
         add(hideSpecials, BorderLayout.NORTH);
      }
      {
         table = new JTable(new ChassiTableModel(aPreferences.uiPreferences.getHideSpecialMechs()));
         table.setRowHeight(30);
         table.addMouseListener(new MouseAdapter(){
            @Override
            public void mouseClicked(MouseEvent e){
               if( SwingUtilities.isLeftMouseButton(e) && e.getClickCount() >= 2 ){
                  final JTable target = (JTable)e.getSource();
                  final int row = target.getSelectedRow();
                  final int column = target.getSelectedColumn();
                  final Object cell = target.getValueAt(row, column);
                  if( cell instanceof ChassisStandard ){
                     ChassisStandard chassi = (ChassisStandard)cell;
                     ProgramInit.lsml().tabbedPane.setSelectedComponent(ProgramInit.lsml().mechLabPane);
                     ProgramInit.lsml().mechLabPane.openLoadout(new LoadoutStandard(chassi, ProgramInit.lsml().xBar));
                  }
               }
            }
         });

         table.removeColumn(table.getColumnModel().getColumn(0)); // Remove auto-generated column
         table.addColumn(new NameColumn());
         table.addColumn(new SpeedColumn());
         table.addColumn(new TonsColumn());
         for(Location part : Arrays.asList(Location.RightArm, Location.RightTorso, Location.CenterTorso, Location.LeftTorso, Location.LeftArm,
                                           Location.Head)){
            table.addColumn(new PartColumn(part));
         }
         table.addColumn(new JumpJetsColumn());
         add(new JScrollPane(table), BorderLayout.CENTER);
      }
   }

   @Override
   public void receive(MessageXBar.Message aMsg){
      if( aMsg instanceof UiPreferences.Message ){
         UiPreferences.Message msg = (Message)aMsg;
         if( msg.attribute == UiPreferences.UI_HIDE_SPECIAL_MECHS ){
            hideSpecials.setSelected(preferences.uiPreferences.getHideSpecialMechs());
            ((ChassiTableModel)table.getModel()).recreate(hideSpecials.isSelected());
         }
      }
   }

}
