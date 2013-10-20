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
   private final String[]           weaponTableTooltips = {"Weapon: The weapon equipped or the ammo if only ammo is equipped.",
         "Ammo: The amount of ammo equipped.", "Volleys: The number of times a weapon can be fired.",
         "Sec: The amount of time to use all ammo given a constant maximum fire rate.", "Dmg: The total damage potential for the ammo equipped."};

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
