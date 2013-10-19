package lisong_mechlab.model.tables;

import java.util.ArrayList;
import java.util.TreeMap;

import javax.swing.SwingUtilities;
import javax.swing.table.AbstractTableModel;

import lisong_mechlab.model.item.AmmoWeapon;
import lisong_mechlab.model.item.Ammunition;
import lisong_mechlab.model.item.BallisticWeapon;
import lisong_mechlab.model.item.EnergyWeapon;
import lisong_mechlab.model.item.MissileWeapon;
import lisong_mechlab.model.item.Weapon;
import lisong_mechlab.model.loadout.Loadout;
import lisong_mechlab.model.loadout.LoadoutPart;
import lisong_mechlab.model.loadout.Upgrades;
import lisong_mechlab.model.loadout.metrics.TotalAmmoSupply;
import lisong_mechlab.model.loadout.metrics.TotalWeapons;
import lisong_mechlab.util.MessageXBar;
import lisong_mechlab.util.MessageXBar.Message;

public class AmmoTableDataModel extends AbstractTableModel implements MessageXBar.Reader{
   private static final long            serialVersionUID = -2671906919112648859L;
   protected String                     weaponNames;
   private Loadout                      aLoadout;
   private TotalAmmoSupply              totalAmmoSupply;
   private TotalWeapons                 totalWeapons;
   private TreeMap<String, Weapon>      weaponColumn;
   private TreeMap<String, Double>      ammoQuantityColumn;
   private TreeMap<String, Integer>     volleyAmountColumn;
   private TreeMap<String, Double>      numberVolleyColumn;
   private TreeMap<String, Double>      combatColumn;
   private TreeMap<Ammunition, Integer> ammoEquipped;
   private TreeMap<Weapon, Integer>     weaponsEquipped;
   private int                          srmVolleyTotal;
   private int                          lrmVolleyTotal;
   private Ammunition                   srmAmmoType;
   private Ammunition                   lrmAmmoType;
   private ArrayList<Double>            srmCooldownList;
   private ArrayList<Double>            lrmCooldownList;
   private ArrayList<Double>            srmDamageList;
   private ArrayList<Double>            lrmDamageList;
   private String[]                     columnNames      = {"Weapon", "Ammo", "Vlys", "Scs", "Dmg"};
   private TreeMap<String, Double>      damageMap;
   private int streakVolleyTotal;
   private Ammunition streakAmmoType;
   private ArrayList<Double> streakCooldownList;
   private ArrayList<Double> streakDamageList;

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
      fillInDamageList();
   }

   public void initialiseLists(){
      weaponColumn = new TreeMap<>();
      ammoQuantityColumn = new TreeMap<>();
      volleyAmountColumn = new TreeMap<>();
      numberVolleyColumn = new TreeMap<>();
      combatColumn = new TreeMap<>();
      srmCooldownList = new ArrayList<>();
      lrmCooldownList = new ArrayList<>();
      streakCooldownList = new ArrayList<>();
      srmDamageList = new ArrayList<>();
      lrmDamageList = new ArrayList<>();
      streakDamageList = new ArrayList<>();
      damageMap = new TreeMap<>();
      
   }

   public void initialiseMaps(){
      ammoEquipped = totalAmmoSupply.calculate();
      weaponsEquipped = totalWeapons.calculate();
   }

   public void fillInData(){
      for(Weapon weapon : weaponsEquipped.keySet()){

         if( weapon instanceof BallisticWeapon ){
            weaponColumn.put(weapon.getName() + " x " + weaponsEquipped.get(weapon), weapon);
         }
         if( weapon instanceof EnergyWeapon ){
            weaponColumn.put(weapon.getName() + " x " + weaponsEquipped.get(weapon), weapon);
         }

      }
      fillInMissileData();

   }

   private void fillInMissileData(){
      srmVolleyTotal = 0;
      lrmVolleyTotal = 0;
      streakVolleyTotal = 0;
      for(Weapon weapon : weaponsEquipped.keySet()){
         if( weapon instanceof MissileWeapon ){
            initialiseSrmFields(weapon);
            initialiseLrmFields(weapon);
            initialiseStreakFields(weapon);
         }

      }
      fillInSrmTableValues();
      fillInLrmTableValues();
      fillInStreakTableValues();

   }

   private void initialiseLrmFields(Weapon weapon){
      if( ((AmmoWeapon)weapon).getAmmoType(aLoadout.getUpgrades()).getName().contains("LRM AMMO") ){
         lrmVolleyTotal = weapon.getAmmoPerPerShot() * weaponsEquipped.get(weapon) + lrmVolleyTotal;
         lrmAmmoType = ((AmmoWeapon)weapon).getAmmoType(aLoadout.getUpgrades());
         lrmCooldownList.add(weapon.getSecondsPerShot());
         lrmDamageList.add(weapon.getDamagePerShot());
      }
   }

   private void initialiseSrmFields(Weapon weapon){
      if( ((AmmoWeapon)weapon).getAmmoType(aLoadout.getUpgrades()).getName().contains("SRM AMMO") && !((AmmoWeapon)weapon).getAmmoType(aLoadout.getUpgrades()).getName().contains("STREAK") ){
         srmVolleyTotal = weapon.getAmmoPerPerShot() * weaponsEquipped.get(weapon) + srmVolleyTotal;
         srmAmmoType = ((AmmoWeapon)weapon).getAmmoType(aLoadout.getUpgrades());
         srmCooldownList.add(weapon.getSecondsPerShot());
         srmDamageList.add(weapon.getDamagePerShot());
      }
   }
   
   private void initialiseStreakFields(Weapon weapon){
      if( ((AmmoWeapon)weapon).getAmmoType(aLoadout.getUpgrades()).getName().contains("STREAK") ){
         streakVolleyTotal = weapon.getAmmoPerPerShot() * weaponsEquipped.get(weapon) + streakVolleyTotal;
         streakAmmoType = ((AmmoWeapon)weapon).getAmmoType(aLoadout.getUpgrades());
         streakCooldownList.add(weapon.getSecondsPerShot());
         streakDamageList.add(weapon.getDamagePerShot());
      }
   }
   
   private void fillInStreakTableValues(){
      if( streakVolleyTotal != 0 ){
         int volleyValue = streakVolleyTotal;
         weaponColumn.put("STREAK SRM " + streakVolleyTotal, null);
         volleyAmountColumn.put("STREAK SRM " + streakVolleyTotal, volleyValue);
      }
   }

   private void fillInLrmTableValues(){
      if( lrmVolleyTotal != 0 ){
         int volleyValue = lrmVolleyTotal;
         weaponColumn.put("LRM " + lrmVolleyTotal, null);
         volleyAmountColumn.put("LRM " + lrmVolleyTotal, volleyValue);
      }
   }

   private void fillInSrmTableValues(){
      if( srmVolleyTotal != 0 ){
         weaponColumn.put("SRM " + srmVolleyTotal, null);
         int volleyValue = srmVolleyTotal;
         volleyAmountColumn.put("SRM " + srmVolleyTotal, volleyValue);

      }
   }

   public void fillInAmmoQuantity(){
      for(String weaponName : weaponColumn.keySet()){
         if( weaponColumn.get(weaponName) instanceof BallisticWeapon ){
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
         fillInSrmAmmoQuanitity(weaponName);
         fillInLrmAmmoQuantity(weaponName);
         fillInStreakAmmoQuanitity(weaponName);

      }
      fillInAmmoOnlyAmmoQuantity();

   }

   private void fillInAmmoOnlyAmmoQuantity(){
      ArrayList<Ammunition> tempListOfAmmo = new ArrayList<Ammunition>();
      for(Ammunition ammo : ammoEquipped.keySet()){
         tempListOfAmmo.add(ammo);
      }
      for(Weapon weapon : weaponsEquipped.keySet()){
         if( weapon instanceof AmmoWeapon )
            tempListOfAmmo.remove(((AmmoWeapon)weapon).getAmmoType(aLoadout.getUpgrades()));
      }
      for(Ammunition ammo : tempListOfAmmo){
         ammoQuantityColumn.put(ammo.getName() + " Only", (double)ammoEquipped.get(ammo) * ammo.getShotsPerTon());
         weaponColumn.put(ammo.getName() + " Only", null);
      }
   }

   private void fillInStreakAmmoQuanitity(String weaponName){
      if( weaponName.contains("STREAK") ){
         if( ammoEquipped.keySet().contains(streakAmmoType) ){
            ammoQuantityColumn.put(weaponName, (double)streakAmmoType.getShotsPerTon() * ammoEquipped.get(streakAmmoType));
         }
         else{
            ammoQuantityColumn.put(weaponName, (double)0);
         }
      }
   }
   
   private void fillInSrmAmmoQuanitity(String weaponName){
      if( weaponName.contains("SRM") && !weaponName.contains("STREAK") ){
         if( ammoEquipped.keySet().contains(srmAmmoType) ){
            ammoQuantityColumn.put(weaponName, (double)srmAmmoType.getShotsPerTon() * ammoEquipped.get(srmAmmoType));
         }
         else{
            ammoQuantityColumn.put(weaponName, (double)0);
         }
      }
   }

   private void fillInLrmAmmoQuantity(String weaponName){
      if( weaponName.contains("LRM") ){
         if( ammoEquipped.keySet().contains(lrmAmmoType) ){
            ammoQuantityColumn.put(weaponName, (double)lrmAmmoType.getShotsPerTon() * ammoEquipped.get(lrmAmmoType));
         }
         else{
            ammoQuantityColumn.put(weaponName, (double)0);
         }
      }
   }

   public void fillInVolleyAmount(){
      fillInEquippedWeaponVolleyAmounts();
      fillInAmmoOnlyVolleyAmounts();
   }

   private void fillInEquippedWeaponVolleyAmounts(){
      for(Weapon weapon : weaponsEquipped.keySet()){
         for(String weaponName : weaponColumn.keySet()){
            if( (weapon.getName() + " x " + weaponsEquipped.get(weapon)).equals(weaponName) ){
               if( weapon instanceof AmmoWeapon ){
                  volleyAmountColumn.put(weaponName, weaponColumn.get(weaponName).getAmmoPerPerShot() * weaponsEquipped.get(weapon));
               }
               else if( weapon instanceof EnergyWeapon ){
                  volleyAmountColumn.put(weaponName, weaponsEquipped.get(weapon));
               }

            }

         }

      }
   }

   private void fillInAmmoOnlyVolleyAmounts(){
      for(String weaponName : weaponColumn.keySet()){
         if( (weaponColumn.get(weaponName) == null) && !((weaponName.contains("SRM")) || (weaponName.contains("LRM "))) ){
            volleyAmountColumn.put(weaponName, 0);
         }
         else if( weaponName.contains("Only") ){
            volleyAmountColumn.put(weaponName, 0);
         }
      }
   }

   public void fillInNumberVolleys(){
      for(String weaponName : weaponColumn.keySet()){
         if( (weaponColumn.get(weaponName) != null) || (weaponName.contains("SRM"))  || (weaponName.contains("LRM")) ){
            if( weaponName.contains("Only") ){
               numberVolleyColumn.put(weaponName, (double)0);
            }
            else
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
            fillInMissileCombatSeconds(weaponName);
         }
      }
   }

   private void fillInMissileCombatSeconds(String weaponName){
      if( weaponName.contains("SRM") && !weaponName.contains("STREAK")){
         combatColumn.put(weaponName, (numberVolleyColumn.get(weaponName) * calculateSrmCooldownAverage()));
      }
      else if(weaponName.contains("STREAK")){
         combatColumn.put(weaponName, (numberVolleyColumn.get(weaponName) * calculateStreakCooldownAverage()));
      }
      else if( weaponName.contains("LRM") ){
         combatColumn.put(weaponName, (numberVolleyColumn.get(weaponName) * calculateLrmCooldownAverage()));
      }
      else
         combatColumn.put(weaponName, (double)0);
   }
   
   private Double calculateStreakCooldownAverage(){
      Double total = (double)0;
      if(streakCooldownList.isEmpty()) return (double)0;
      for(Double inter : streakCooldownList){
         total += inter;
      }
      return total / streakCooldownList.size();
   }

   private void fillInDamageList(){
      for(String weaponName : weaponColumn.keySet()){
         if( (weaponColumn.get(weaponName) != null) && !(weaponName.contains("SRM")) && !(weaponName.contains("LRM")) ){
         double totalDamage = weaponColumn.get(weaponName).getDamagePerShot() * ammoQuantityColumn.get(weaponName);
         damageMap.put(weaponName, totalDamage);
         
         }else if( weaponName.contains("SRM") && !weaponName.contains("STREAK") ){
            damageMap.put(weaponName, numberVolleyColumn.get(weaponName)  * calculateSrmDamageAverage());
         }
         else if( weaponName.contains("STREAK") ){
            damageMap.put(weaponName, (numberVolleyColumn.get(weaponName)  * calculateStreakDamageAverage()));
         }
         else if( weaponName.contains("LRM") ){
            damageMap.put(weaponName, (numberVolleyColumn.get(weaponName)  * calculateLrmDamageAverage()));
         }
         else
            damageMap.put(weaponName, (double)0);
      }
      
      
   }

   private Double calculateStreakDamageAverage(){
      Double total = (double)0;
      if(streakDamageList.isEmpty()) return (double)0;
      for(Double inter : streakDamageList){
         total += inter;
      }
      return total / streakDamageList.size();
   }

   private Double calculateSrmDamageAverage(){
      Double total = (double)0;
      if(srmDamageList.isEmpty()) return (double)0;
      for(Double inter : srmDamageList){
         total += inter;
      }
      return total / srmDamageList.size();
   }
   
   private Double calculateLrmDamageAverage(){
      Double total = (double)0;
      if(lrmDamageList.isEmpty()) return (double)0;
      for(Double inter : lrmDamageList){
         total += inter;
      }
      return total / lrmDamageList.size();
   }

   public double calculateSrmCooldownAverage(){
      Double total = (double)0;
      if(srmCooldownList.isEmpty()) return 0;
      for(Double inter : srmCooldownList){
         total = inter + total;
      }
      return total / srmCooldownList.size();
   }

   public double calculateLrmCooldownAverage(){
      Double total = (double)0;
      if(srmCooldownList.isEmpty()) return 0;
      for(Double inter : lrmCooldownList){
         total = inter + total;
      }
      return total / lrmCooldownList.size();
   }

   @Override
   public Class<?> getColumnClass(int aColumnIndex){
      if( aColumnIndex == 0 ){
         return String.class;
      }
      if( aColumnIndex == 1 ){
         return Double.class;
      }
      if( aColumnIndex == 2 ){
         return Double.class;
      }
      if( aColumnIndex == 3 ){
         return Double.class;
      }
      if( aColumnIndex == 4 ){
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

   public void tableChanged(){
      totalAmmoSupply = new TotalAmmoSupply(aLoadout);
      totalAmmoSupply.calculate();
      fillInData();
   }

   @Override
   public int getRowCount(){

      return weaponColumn.size();

   }

   @Override
   public Object getValueAt(int aRowIndex, int aColumnIndex){

      if( aColumnIndex == 0 ){
         String[] weaponArray = new String[weaponColumn.size()];
         weaponArray = weaponColumn.keySet().toArray(weaponArray);
         return weaponArray[aRowIndex];

      }
      if( aColumnIndex == 1 ){
         Double[] ammoQuantityArray = new Double[ammoQuantityColumn.size()];
         ammoQuantityArray = ammoQuantityColumn.values().toArray(ammoQuantityArray);
         return ammoQuantityArray[aRowIndex];
      }
      if( aColumnIndex == 2 ){
         Double[] numberVolleyArray = new Double[numberVolleyColumn.size()];
         numberVolleyArray = numberVolleyColumn.values().toArray(numberVolleyArray);
         if( !numberVolleyArray[aRowIndex].isInfinite() ){
            return Math.floor(numberVolleyArray[aRowIndex]);
         }
         return numberVolleyArray[aRowIndex];
      }
      if( aColumnIndex == 3 ){
         Double[] combatArray = new Double[combatColumn.size()];
         combatArray = combatColumn.values().toArray(combatArray);
         if( !combatArray[aRowIndex].isInfinite() ){
            return Math.floor(combatArray[aRowIndex]);
         }
         if(combatArray[aRowIndex].isNaN()){
            return (double)0;
         }
         return combatArray[aRowIndex];
      }
      if( aColumnIndex == 4 ){
         Double[] damageArray = new Double[damageMap.size()];
         damageArray = damageMap.values().toArray(damageArray);
         if( !damageArray[aRowIndex].isInfinite() ){
            return Math.floor(damageArray[aRowIndex]);
         }
         if(damageArray[aRowIndex].isNaN()){
            return (double)0;
         }
         return damageArray[aRowIndex];
      }
      return "false";
   }

   @Override
   public void receive(Message aMsg){
      if( !aMsg.isForMe(aLoadout) )
         return;

      if( (aMsg instanceof LoadoutPart.Message && ((LoadoutPart.Message)aMsg).type != LoadoutPart.Message.Type.ArmorChanged)
          || aMsg instanceof Upgrades.Message )
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
