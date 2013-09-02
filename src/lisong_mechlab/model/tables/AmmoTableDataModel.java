package lisong_mechlab.model.tables;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map.Entry;
import java.util.Set;
import java.util.TreeMap;

import javax.swing.event.TableModelEvent;
import javax.swing.event.TableModelListener;
import javax.swing.table.TableModel;

import lisong_mechlab.model.MessageXBar;
import lisong_mechlab.model.item.Ammunition;
import lisong_mechlab.model.item.Item;
import lisong_mechlab.model.loadout.Loadout;
import lisong_mechlab.model.loadout.LoadoutPart;
import lisong_mechlab.model.loadout.metrics.TotalAmmoSupply;

public class AmmoTableDataModel implements TableModel, MessageXBar.Reader{

   
   private final List<TableModelListener>                     listeners = new ArrayList<TableModelListener>();
   
   protected String weaponNames;
   private Loadout aLoadout;
   private TotalAmmoSupply totalAmmoSupply;
   private Object[][] data = {{"srm"}, {"345"}};//Exception occurs if there arn't some values.
   private String[] columnNames = {"Weapon" , "Ammo", "Volley Amount" , "Number of Volleys"};
   private MessageXBar aXBar;
   
   public AmmoTableDataModel(Loadout aloadout, MessageXBar aXBar){
      this.aLoadout = aloadout;
      totalAmmoSupply = new TotalAmmoSupply(aLoadout);
      totalAmmoSupply.calculate();
      this.aXBar = aXBar;
      aXBar.attach(this);

   }
   
   public void fillInData(){
      data = new Object[totalAmmoSupply.calculate().size()][4];
      Set<Entry<Ammunition, Integer>> entrySet = totalAmmoSupply.calculate().entrySet();
      Iterator<Entry<Ammunition, Integer>> entryIterator = entrySet.iterator();
      int entryCounter = 0;
      while( entryIterator.hasNext()){
         Entry<Ammunition, Integer> entryTemp = entryIterator.next();
            data[entryCounter][0] = entryTemp.getKey().getName();
          data[entryCounter][1] = entryTemp.getValue();
          entryCounter++;
      }
      fillInVolleyData();
      fillInTotalNumberOfVolleys();
   }
   
   public void fillInVolleyData(){
      
      TreeMap<String, Integer> volleyMap = (TreeMap<String, Integer>)totalAmmoSupply.getShotsPerVolleyForEach();
      Integer[] volleyArray = volleyMap.values().toArray(new Integer[volleyMap.values().size()]);
      for(int i = 0; i < data.length; i++){
         data[i][2] = volleyArray[i];
      }
      
   }
   
   public void fillInTotalNumberOfVolleys(){
      for(int i = 0; i < data.length; i++){
         int allAmmo =  (int)data[i][1];
         int volleyValue = (int)data[i][2];
         if(volleyValue != 0){
                     data[i][3] = allAmmo/volleyValue;
         }
         else{
            data[i][3] = 0;
         }
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
      if(aColumnIndex == 0){
         return String.class;
      }
      else
         return Integer.class;
   }

   @Override
   public int getColumnCount(){
      return columnNames.length;
   }

   @Override
   public String getColumnName(int aColumnIndex){
      return columnNames[aColumnIndex];
   }
   
  
   public void tableChanged(TableModelEvent e){
      totalAmmoSupply = new TotalAmmoSupply(aLoadout);
      totalAmmoSupply.calculate();
      fillInData();
      
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

   @Override
   public void receive(MessageXBar.Message aMsg){
      if(this.aXBar != null){
           if(aMsg instanceof LoadoutPart.Message){
         totalAmmoSupply = new TotalAmmoSupply(aLoadout);
         totalAmmoSupply.calculate();
//         if(!(totalAmmoSupply.calculate().size() == 0)){
            fillInData();
//         }
            tableChanged(new TableModelEvent(this));
         
      }
      }
    
      
   }

}
