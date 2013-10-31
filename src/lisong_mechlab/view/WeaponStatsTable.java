/*
 * @formatter:off
 * Li Song Mech Lab - A 'mech building tool for PGI's MechWarrior: Online.
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
import java.awt.Font;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import javax.swing.JLabel;
import javax.swing.JTable;
import javax.swing.table.AbstractTableModel;

import lisong_mechlab.model.chassi.HardpointType;
import lisong_mechlab.model.item.ItemDB;
import lisong_mechlab.model.item.Weapon;
import lisong_mechlab.view.render.StyleManager;

/**
 * This class is a JTable which shows statistics for weapons.
 * 
 * @author Emily Björk
 */
public class WeaponStatsTable extends JTable{
   private static final long serialVersionUID = -5418864783866344614L;

   private static class Model extends AbstractTableModel{
      private static final long  serialVersionUID = 2420773283903826604L;
      private final List<Weapon> weapons          = new ArrayList<>();

      public Model(HardpointType aHardpointType){
         List<Weapon> allweapons = ItemDB.lookup(Weapon.class);

         for(Weapon weapon : allweapons){
            if( weapon.getHardpointType() == aHardpointType ){
               weapons.add(weapon);
            }
         }
      }

      @Override
      public int getColumnCount(){
         return 1;
      }

      @Override
      public int getRowCount(){
         return weapons.size();
      }

      @Override
      public Object getValueAt(int aRowIndex, int aColumnIndex){
         return weapons.get(aRowIndex);
      }

      public List<Weapon> getAllValues(){
         return weapons;
      }
   }

   private static class NameColumn extends AttributeTableColumn{
      private static final long serialVersionUID = -8721941613338621968L;

      public NameColumn(){
         super("Weapon", 0);
      }

      @Override
      public String valueOf(Object aSourceRowObject){
         return ((Weapon)aSourceRowObject).getName();
      }
   }

   private class StatColumn extends AttributeTableColumn{
      private static final long   serialVersionUID = -8721941613338621968L;
      private final String        statString;
      private final DecimalFormat df               = new DecimalFormat("###.#");
      private final boolean       lowestBest;

      public StatColumn(String aHeader, String aStatString, boolean isLowestBest){
         super(aHeader, 0);
         statString = aStatString;
         lowestBest = isLowestBest;
      }

      @Override
      public String valueOf(Object aSourceRowObject){
         return df.format(((Weapon)aSourceRowObject).getStat(statString, null));
      }

      @Override
      public void styleLabel(JLabel aJLabel, Object aSourceRowObject, boolean isSelected, boolean hasFocus){
         List<Weapon> weapons = ((Model)getModel()).getAllValues();
         List<Double> stats = new ArrayList<Double>();

         for(Weapon weapon : weapons){
            double stat = weapon.getStat(statString, null);
            if( !stats.contains(stat) ){
               stats.add(stat);
            }
         }
         Collections.sort(stats);

         final int position;
         int i = stats.indexOf(((Weapon)aSourceRowObject).getStat(statString, null));
         if( lowestBest ){
            position = i;
         }
         else{
            position = stats.size() - i - 1;
         }

         double a = 0.5;
         double b = 0.3;
         double c = 0.1;
         Color orig = StyleManager.getBgColorFor(HardpointType.MISSILE);
         Color best = new Color((int)(orig.getRed() * a + (1 - a) * 255), (int)(orig.getGreen() * a + (1 - a) * 255),
                                (int)(orig.getBlue() * a + (1 - a) * 255));
         Color secondBest = new Color((int)(orig.getRed() * b + (1 - b) * 255), (int)(orig.getGreen() * b + (1 - b) * 255),
                                      (int)(orig.getBlue() * b + (1 - b) * 255));
         Color thirdBest = new Color((int)(orig.getRed() * c + (1 - c) * 255), (int)(orig.getGreen() * c + (1 - c) * 255),
                                     (int)(orig.getBlue() * c + (1 - c) * 255));

         aJLabel.setOpaque(true);
         if( position == 0 ){
            aJLabel.setBackground(best);
            aJLabel.setFont(aJLabel.getFont().deriveFont(Font.BOLD));
         }
         else if( position == 1 ){
            aJLabel.setBackground(secondBest);
            aJLabel.setFont(aJLabel.getFont().deriveFont(Font.PLAIN));
         }
         else if( position == 2 ){
            aJLabel.setBackground(thirdBest);
            aJLabel.setFont(aJLabel.getFont().deriveFont(Font.PLAIN));
         }
         else{
            aJLabel.setBackground(Color.WHITE);
            aJLabel.setFont(aJLabel.getFont().deriveFont(Font.PLAIN));
         }
      }
   }

   public WeaponStatsTable(HardpointType aHardpointType){
      super(new Model(aHardpointType));

      removeColumn(getColumnModel().getColumn(0)); // Remove auto-generated column
      addColumn(new NameColumn());
      addColumn(new StatColumn("Tons", "t", true));
      addColumn(new StatColumn("Slots", "c", true));
      addColumn(new StatColumn("Alpha Strike", "d", false));
      addColumn(new StatColumn("Cycle time", "s", true));
      addColumn(new StatColumn("Heat", "h", true));
      addColumn(new StatColumn("DPS", "d/s", false));
      addColumn(new StatColumn("DPH", "d/h", false));
      addColumn(new StatColumn("DPT", "d/t", false));
      addColumn(new StatColumn("HPS", "h/s", true));
   }
}
