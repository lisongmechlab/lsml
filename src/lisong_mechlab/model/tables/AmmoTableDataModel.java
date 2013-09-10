package lisong_mechlab.model.tables;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Map.Entry;
import java.util.Set;
import java.util.TreeMap;

import javax.swing.event.TableModelEvent;
import javax.swing.event.TableModelListener;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.TableCellRenderer;
import javax.swing.table.TableColumn;
import javax.swing.table.TableModel;

import org.jfree.ui.tabbedui.AbstractTabbedUI;

import lisong_mechlab.model.MessageXBar;
import lisong_mechlab.model.item.AmmoWeapon;
import lisong_mechlab.model.item.Ammunition;
import lisong_mechlab.model.item.Item;
import lisong_mechlab.model.item.Weapon;
import lisong_mechlab.model.loadout.Loadout;
import lisong_mechlab.model.loadout.LoadoutPart;
import lisong_mechlab.model.loadout.metrics.TotalAmmoSupply;
import lisong_mechlab.model.loadout.metrics.TotalWeapons;

public class AmmoTableDataModel extends AbstractTableModel{

   
   
   protected String weaponNames;
   private Loadout aLoadout;
   private TotalAmmoSupply totalAmmoSupply;
   private TotalWeapons totalWeapons;
   private TreeMap<String, Weapon> weaponColumn;
   private TreeMap<String, String> ammoTypeColumn;
   private TreeMap<String, Double> ammoQuantityColumn;
   private TreeMap<String, Integer> volleyAmountColumn;
   private TreeMap<String, Double> numberVolleyColumn;
   private TreeMap<String, Double> combatColumn;
   private TreeMap<Ammunition, Integer> ammoEquipped;
   private TreeMap<Weapon, Integer> weaponsEquipped;
   private String[] columnNames = {"Weapon" , "Ammo Type",  "Ammo Quantity", "Volley Amount" , "Number of Volleys", "Combat Seconds"};
   
   public AmmoTableDataModel(Loadout aloadout, MessageXBar aXBar){
      this.aLoadout = aloadout;
      totalAmmoSupply = new TotalAmmoSupply(aLoadout);
      totalAmmoSupply.calculate();
      totalWeapons = new TotalWeapons(aloadout);
      totalWeapons.calculate();
      initialiseLists();
      initialiseMaps();
      fillInData();
      fillInAmmoQuantity();
      fillInVolleyAmount();
      fillInNumberVolleys();
      fillInCombatSeconds();
      

   }
   
   public void initialiseLists(){
      weaponColumn = new TreeMap<>();
      ammoQuantityColumn = new TreeMap<>();
      ammoTypeColumn = new TreeMap<>();
      volleyAmountColumn = new TreeMap<>();
      numberVolleyColumn = new TreeMap<>();
      combatColumn = new TreeMap<>();
      
      
   }
   
   public void initialiseMaps(){
      ammoEquipped = totalAmmoSupply.calculate();
      weaponsEquipped = totalWeapons.calculate();
   }
   
   public void fillInData(){
      for(Weapon weapon : weaponsEquipped.keySet()){
         weaponColumn.put(weapon.getName(), weapon);
         if(weapon instanceof AmmoWeapon){
            ammoTypeColumn.put(weapon.getName(), ((AmmoWeapon)weapon).getAmmoType().getName());
         }
         else{
            ammoTypeColumn.put(weapon.getName(), "Energy");
         }
      }
      
   }
   
   public void fillInAmmoQuantity(){
      for(String weaponName : weaponColumn.keySet()){
         if(weaponColumn.get(weaponName) instanceof AmmoWeapon){
            Ammunition ammoTypeTemp = ((AmmoWeapon)weaponColumn.get(weaponName)).getAmmoType();
            if(ammoEquipped.keySet().contains(ammoTypeTemp)){
               ammoQuantityColumn.put(weaponName, (double)ammoTypeTemp.getShotsPerTon()* ammoEquipped.get(ammoTypeTemp));
            }
            else{
               ammoQuantityColumn.put(weaponName, (double)0);
            }
         }
         else{
            ammoQuantityColumn.put(weaponName, Double.POSITIVE_INFINITY);
         }
         
         
      }
      ArrayList<Ammunition> tempListOfAmmo   = new ArrayList<Ammunition>(); 
      for(Ammunition ammo :ammoEquipped.keySet()){
         tempListOfAmmo.add(ammo);
      }
      for(Weapon weapon : weaponsEquipped.keySet()){
         if(weapon instanceof AmmoWeapon)
         tempListOfAmmo.remove(((AmmoWeapon)weapon).getAmmoType());
      }
      for(Ammunition ammo : tempListOfAmmo){
         ammoTypeColumn.put(ammo.getName() + " Only", ammo.getName());
         ammoQuantityColumn.put(ammo.getName() + " Only", (double)ammoEquipped.get(ammo) * ammo.getShotsPerTon());
         weaponColumn.put(ammo.getName() + " Only", null);
      }
      
     
   }
   
   public void fillInVolleyAmount(){
      for(Weapon weapon : weaponsEquipped.keySet()){
         for(String weaponName : weaponColumn.keySet()){
            if( weapon.getName() == weaponName ){
              
                  volleyAmountColumn.put(weaponName, weaponColumn.get(weaponName).getNumberOfShotsPerVolley() * weaponsEquipped.get(weapon));
               
               
            }

         }

      }
      for(String weaponName : weaponColumn.keySet()){
         if(weaponColumn.get(weaponName) == null){
            volleyAmountColumn.put(weaponName, 0);
         }
      }
   }
   
   public void fillInNumberVolleys(){
      for(String weaponName : weaponColumn.keySet()){
         if(weaponColumn.get(weaponName) != null){
            numberVolleyColumn.put(weaponName, (ammoQuantityColumn.get(weaponName)/volleyAmountColumn.get(weaponName)));
         }
         else{
            numberVolleyColumn.put(weaponName, (double)0);
         }
      }
   }
   
   public void fillInCombatSeconds(){
      for(String weaponName : weaponColumn.keySet()){
         if(weaponColumn.get(weaponName) != null){
               combatColumn.put(weaponName, (numberVolleyColumn.get(weaponName) * weaponColumn.get(weaponName).getSecondsPerShot()));

         }
         else {
            combatColumn.put(weaponName, (double)0);
         }
      }
   }
   
   
   
   
   
   
  
   @Override
   public Class<?> getColumnClass(int aColumnIndex){
      if(aColumnIndex == 0){
         return Integer.class;
      }
      if(aColumnIndex == 1){
         return String.class;
      }
      if(aColumnIndex == 2){
         return Double.class;
      }
      if(aColumnIndex == 3){
         return Integer.class;
      }
      if(aColumnIndex == 4){
         return Double.class;
      }
      if(aColumnIndex == 5){
         return Double.class;
      }
      else return String.class;
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
      if(weaponColumn.size() >= ammoTypeColumn.size()){
         return weaponColumn.size();
      }
      else return ammoTypeColumn.size();
      
   }

   @Override
   public Object getValueAt(int aRowIndex, int aColumnIndex){
      
      if(aColumnIndex == 0){
         String[] weaponArray= new String[weaponColumn.size()];
         weaponArray= (String[])weaponColumn.keySet().toArray(weaponArray);
         return weaponArray[aRowIndex];
         
      }
      if(aColumnIndex == 1){
         String[] ammoTypeArray = new String[ammoTypeColumn.size()];
         ammoTypeArray= (String[])ammoTypeColumn.values().toArray(ammoTypeArray);
         return ammoTypeArray[aRowIndex];
      }
      if(aColumnIndex == 2){
         Double[] ammoQuantityArray = new Double[ammoQuantityColumn.size()];
         ammoQuantityArray= (Double[])ammoQuantityColumn.values().toArray(ammoQuantityArray);
         return ammoQuantityArray[aRowIndex];
      }
      if(aColumnIndex == 3){
         Integer[] volleyAmountArray = new Integer[volleyAmountColumn.size()];
         volleyAmountArray= (Integer[])volleyAmountColumn.values().toArray(volleyAmountArray);
         return volleyAmountArray[aRowIndex];
      }
      if(aColumnIndex == 4){
         Double[] numberVolleyArray = new Double[numberVolleyColumn.size()];
         numberVolleyArray= (Double[])numberVolleyColumn.values().toArray(numberVolleyArray);
         return numberVolleyArray[aRowIndex];
      }
      if(aColumnIndex == 5){
         Double[] combatArray = new Double[combatColumn.size()];
         combatArray= (Double[])combatColumn.values().toArray(combatArray);
         return combatArray[aRowIndex];
      }
      else return "false";
   }
  

  

   

}
