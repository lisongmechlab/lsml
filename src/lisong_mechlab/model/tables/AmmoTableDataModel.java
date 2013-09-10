package lisong_mechlab.model.tables;

import java.util.ArrayList;
import java.util.TreeMap;

import javax.swing.SwingUtilities;
import javax.swing.event.TableModelEvent;
import javax.swing.table.AbstractTableModel;

import lisong_mechlab.model.MessageXBar;
import lisong_mechlab.model.MessageXBar.Message;
import lisong_mechlab.model.item.AmmoWeapon;
import lisong_mechlab.model.item.Ammunition;
import lisong_mechlab.model.item.Weapon;
import lisong_mechlab.model.loadout.Loadout;
import lisong_mechlab.model.loadout.LoadoutPart;
import lisong_mechlab.model.loadout.metrics.TotalAmmoSupply;
import lisong_mechlab.model.loadout.metrics.TotalWeapons;

public class AmmoTableDataModel extends AbstractTableModel implements MessageXBar.Reader{
   private static final long            serialVersionUID = -2671906919112648859L;
   protected String                     weaponNames;
   private Loadout                      aLoadout;
   private TotalAmmoSupply              totalAmmoSupply;
   private TotalWeapons                 totalWeapons;
   private TreeMap<String, Weapon>      weaponColumn;
   private TreeMap<String, String>      ammoTypeColumn;
   private TreeMap<String, Double>      ammoQuantityColumn;
   private TreeMap<String, Integer>     volleyAmountColumn;
   private TreeMap<String, Double>      numberVolleyColumn;
   private TreeMap<String, Double>      combatColumn;
   private TreeMap<Ammunition, Integer> ammoEquipped;
   private TreeMap<Weapon, Integer>     weaponsEquipped;
   private String[]                     columnNames      = {"Weapon", "Ammo Type", "Ammo Quantity", "Volley Amount", "Number of Volleys",
         "Combat Seconds"                                };
   public AmmoTableDataModel(Loadout aloadout, MessageXBar aXBar){
      this.aLoadout = aloadout;
      totalAmmoSupply = new TotalAmmoSupply(aLoadout);
      totalAmmoSupply.calculate();
      totalWeapons = new TotalWeapons(aloadout);
      totalWeapons.calculate();
      initialiseLists();
      initialiseMaps();
      fillInAllColumns();
      aXBar.attach(this);

   }

   private void fillInAllColumns(){
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
         if( weapon instanceof AmmoWeapon ){
            ammoTypeColumn.put(weapon.getName(), ((AmmoWeapon)weapon).getAmmoType(aLoadout.getUpgrades()).getName());
         }
         else{
            ammoTypeColumn.put(weapon.getName(), "Energy");
         }
      }

   }

   public void fillInAmmoQuantity(){
      for(String weaponName : weaponColumn.keySet()){
         if( weaponColumn.get(weaponName) instanceof AmmoWeapon ){
            Ammunition ammoTypeTemp = ((AmmoWeapon)weaponColumn.get(weaponName)).getAmmoType(aLoadout.getUpgrades());
            if( ammoEquipped.keySet().contains(ammoTypeTemp) ){
               ammoQuantityColumn.put(weaponName, (double)ammoTypeTemp.getShotsPerTon() * ammoEquipped.get(ammoTypeTemp));
            }
            else{
               ammoQuantityColumn.put(weaponName, (double)0);
            }
         }
         else{
            ammoQuantityColumn.put(weaponName, Double.POSITIVE_INFINITY);
         }

      }
      ArrayList<Ammunition> tempListOfAmmo = new ArrayList<Ammunition>();
      for(Ammunition ammo : ammoEquipped.keySet()){
         tempListOfAmmo.add(ammo);
      }
      for(Weapon weapon : weaponsEquipped.keySet()){
         if( weapon instanceof AmmoWeapon )
            tempListOfAmmo.remove(((AmmoWeapon)weapon).getAmmoType(aLoadout.getUpgrades()));
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

               volleyAmountColumn.put(weaponName, weaponColumn.get(weaponName).getAmmoPerPerShot() * weaponsEquipped.get(weapon));

            }

         }

      }
      for(String weaponName : weaponColumn.keySet()){
         if( weaponColumn.get(weaponName) == null ){
            volleyAmountColumn.put(weaponName, 0);
         }
      }
   }

   public void fillInNumberVolleys(){
      for(String weaponName : weaponColumn.keySet()){
         if( weaponColumn.get(weaponName) != null ){
            numberVolleyColumn.put(weaponName, (ammoQuantityColumn.get(weaponName) / volleyAmountColumn.get(weaponName)));
         }
         else{
            numberVolleyColumn.put(weaponName, (double)0);
         }
      }
   }

   public void fillInCombatSeconds(){
      for(String weaponName : weaponColumn.keySet()){
         if( weaponColumn.get(weaponName) != null ){
            combatColumn.put(weaponName, (numberVolleyColumn.get(weaponName) * weaponColumn.get(weaponName).getSecondsPerShot()));

         }
         else{
            combatColumn.put(weaponName, (double)0);
         }
      }
   }

   @Override
   public Class<?> getColumnClass(int aColumnIndex){
      if( aColumnIndex == 0 ){
         return Integer.class;
      }
      if( aColumnIndex == 1 ){
         return String.class;
      }
      if( aColumnIndex == 2 ){
         return Double.class;
      }
      if( aColumnIndex == 3 ){
         return Integer.class;
      }
      if( aColumnIndex == 4 ){
         return Double.class;
      }
      if( aColumnIndex == 5 ){
         return Double.class;
      }
      return String.class;
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
      if( weaponColumn.size() >= ammoTypeColumn.size() ){
         return weaponColumn.size();
      }
      return ammoTypeColumn.size();

   }

   @Override
   public Object getValueAt(int aRowIndex, int aColumnIndex){

      if( aColumnIndex == 0 ){
         String[] weaponArray = new String[weaponColumn.size()];
         weaponArray = weaponColumn.keySet().toArray(weaponArray);
         return weaponArray[aRowIndex];

      }
      if( aColumnIndex == 1 ){
         String[] ammoTypeArray = new String[ammoTypeColumn.size()];
         ammoTypeArray = ammoTypeColumn.values().toArray(ammoTypeArray);
         return ammoTypeArray[aRowIndex];
      }
      if( aColumnIndex == 2 ){
         Double[] ammoQuantityArray = new Double[ammoQuantityColumn.size()];
         ammoQuantityArray = ammoQuantityColumn.values().toArray(ammoQuantityArray);
         return ammoQuantityArray[aRowIndex];
      }
      if( aColumnIndex == 3 ){
         Integer[] volleyAmountArray = new Integer[volleyAmountColumn.size()];
         volleyAmountArray = volleyAmountColumn.values().toArray(volleyAmountArray);
         return volleyAmountArray[aRowIndex];
      }
      if( aColumnIndex == 4 ){
         Double[] numberVolleyArray = new Double[numberVolleyColumn.size()];
         numberVolleyArray = numberVolleyColumn.values().toArray(numberVolleyArray);
         return numberVolleyArray[aRowIndex];
      }
      if( aColumnIndex == 5 ){
         Double[] combatArray = new Double[combatColumn.size()];
         combatArray = combatColumn.values().toArray(combatArray);
         return combatArray[aRowIndex];
      }
      return "false";
   }

   @Override
   public void receive(Message aMsg){
      if( aMsg instanceof LoadoutPart.Message )
         SwingUtilities.invokeLater(new Runnable(){

            @Override
            public void run(){
               initialiseLists();
               initialiseMaps();
               fillInAllColumns();
               fireTableDataChanged();

            }
         });

   }
}
