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
package lisong_mechlab.view;

import java.awt.event.MouseEvent;

import javax.swing.JTable;
import javax.swing.SwingConstants;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.JTableHeader;

import lisong_mechlab.model.loadout.Loadout;
import lisong_mechlab.model.tables.AmmoTableDataModel;
import lisong_mechlab.util.MessageXBar;

public class WeaponSummaryTable extends JTable{
   private static final long        serialVersionUID    = 868861599143353045L;
   private final AmmoTableDataModel ammoTableDataModel;
   private final String[]           weaponTableTooltips = {"The weapon equipped or the ammo if only ammo is equipped.",
         "The amount of ammo equipped.", "The number of times a weapon can be fired.",
         "<html>The amount of time to use all ammo given a constant maximum fire rate.<br>I.e. how long you can use it in sustained combat.</html>",
         "The total damage potential for the ammo equipped."};

   public WeaponSummaryTable(Loadout aLoadout, MessageXBar aXBar){
      ammoTableDataModel = new AmmoTableDataModel(aLoadout, aXBar);
      setFillsViewportHeight(true);
      setModel(ammoTableDataModel);
      ((DefaultTableCellRenderer)getTableHeader().getDefaultRenderer()).setHorizontalAlignment(SwingConstants.CENTER);
      getColumnModel().getColumn(0).setMinWidth(80);
      getColumnModel().getColumn(1).setMinWidth(30);
      getTableHeader();
   }

   @Override
   protected JTableHeader createDefaultTableHeader(){
      return new JTableHeader(columnModel){
         private static final long serialVersionUID = 1L;

         @Override
         public String getToolTipText(MouseEvent e){
            java.awt.Point p = e.getPoint();
            int index = columnModel.getColumnIndexAtX(p.x);
            int realIndex = columnModel.getColumn(index).getModelIndex();
            return weaponTableTooltips[realIndex];
         }
      };
   }
}
