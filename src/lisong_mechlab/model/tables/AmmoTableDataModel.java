package lisong_mechlab.model.tables;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map.Entry;
import java.util.Set;

import javax.swing.event.TableModelEvent;
import javax.swing.event.TableModelListener;
import javax.swing.table.TableModel;

import lisong_mechlab.model.loadout.Loadout;
import lisong_mechlab.model.loadout.metrics.TotalAmmoSupply;

public class AmmoTableDataModel implements TableModel{
   
   private final List<TableModelListener>                     listeners = new ArrayList<TableModelListener>();
   
   protected String weaponNames;
   private Loadout aLoadout;
   private TotalAmmoSupply totalAmmoSupply;
   private Object[][] data = {{"srm"}, {"345"}};
   private String[] columnNames = {"Weapon" , "Ammo"};
   
   public AmmoTableDataModel(Loadout aloadout){
      this.aLoadout = aloadout;
      totalAmmoSupply = new TotalAmmoSupply(aLoadout);
      totalAmmoSupply.calculate();
//      data = new Object[totalAmmoSupply.calculate().size()][2];
//      fillInData();
   }
   
   public void fillInData(){
      data = new Object[totalAmmoSupply.calculate().size()][2];
      Set<Entry<String, Integer>> entrySet = totalAmmoSupply.calculate().entrySet();
      Iterator<Entry<String, Integer>> entryIterator = entrySet.iterator();
      int entryCounter = 0;
      while( entryIterator.hasNext()){
         Entry<String, Integer> entryTemp = entryIterator.next();
          data[entryCounter][0] = entryTemp.getKey();
          data[entryCounter][1] = entryTemp.getValue();
          entryCounter++;
      }
   }

   @Override
   public void addTableModelListener(TableModelListener aL){
     listeners.add(aL);
      
   }

   public void notifyTreeChange(TableModelEvent e){
      for(TableModelListener listener : listeners){
         listener.tableChanged(e);
      }
   }
   @Override
   public Class<?> getColumnClass(int aColumnIndex){
      return getValueAt(0, aColumnIndex).getClass();
   }

   @Override
   public int getColumnCount(){
      return columnNames.length;
   }

   @Override
   public String getColumnName(int aColumnIndex){
      return columnNames[aColumnIndex];
   }

   @Override
   public int getRowCount(){
      return data.length;
   }

   @Override
   public Object getValueAt(int aRowIndex, int aColumnIndex){
      return data[aRowIndex][aColumnIndex];
   }

   @Override
   public boolean isCellEditable(int aRowIndex, int aColumnIndex){
      // TODO Auto-generated method stub
      return false;
   }

   @Override
   public void removeTableModelListener(TableModelListener aL){
      // TODO Auto-generated method stub
      
   }

   @Override
   public void setValueAt(Object aValue, int aRowIndex, int aColumnIndex){
      // TODO Auto-generated method stub
      
   }

}
