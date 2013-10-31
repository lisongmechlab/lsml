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

package lisong_mechlab.model.tables;

import java.util.ArrayList;
import java.util.TreeMap;

import javax.swing.SwingUtilities;
import javax.swing.table.AbstractTableModel;

import lisong_mechlab.model.item.AmmoWeapon;
import lisong_mechlab.model.item.Ammunition;
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

   private MissileEntry                 lrmEntry;
   private MissileEntry                 streakEntry;


   private String[]                     columnNames      = {"Weapon", "Ammo", "Volleys", "Cmbt", "T.Dmg"};
   private TreeMap<String, Double>      damageMap;

   private MissileEntry srmEntry;

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
      damageMap = new TreeMap<>();
      lrmEntry = null;
      srmEntry = null;
      streakEntry = null;
      
   }

   public void initialiseMaps(){
      ammoEquipped = totalAmmoSupply.calculate();
      weaponsEquipped = totalWeapons.calculate();
   }

   public void fillInData(){
      for(Weapon weapon : weaponsEquipped.keySet()){

         if( !(weapon instanceof MissileWeapon )){
            weaponColumn.put(weapon.getName() + " x " + weaponsEquipped.get(weapon), weapon);
         }

      }
      fillInMissileData();

   }

   private void fillInMissileData(){
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
      if(((AmmoWeapon)weapon).getAmmoType(aLoadout.getUpgrades()).getName().contains("LRM")){
         if(lrmEntry == null){
         lrmEntry = new MissileEntry(((AmmoWeapon)weapon).getAmmoType(aLoadout.getUpgrades()));
         lrmEntry.addAnotherWeapon(weapon);
      }
      else{
         lrmEntry.addAnotherWeapon(weapon);
      }
      }
      
   }

   private void initialiseSrmFields(Weapon weapon){
      if( ((AmmoWeapon)weapon).getAmmoType(aLoadout.getUpgrades()).getName().contains("SRM AMMO") && !((AmmoWeapon)weapon).getAmmoType(aLoadout.getUpgrades()).getName().contains("STREAK") ){
         if(srmEntry == null){
            srmEntry = new MissileEntry(((AmmoWeapon)weapon).getAmmoType(aLoadout.getUpgrades()));
            srmEntry.addAnotherWeapon(weapon);
         }
         else{
            srmEntry.addAnotherWeapon(weapon);
         }
      }
   }
   
   private void initialiseStreakFields(Weapon weapon){
      if( ((AmmoWeapon)weapon).getAmmoType(aLoadout.getUpgrades()).getName().contains("STREAK") ){
         if(streakEntry == null){
            streakEntry = new MissileEntry(((AmmoWeapon)weapon).getAmmoType(aLoadout.getUpgrades()));
            streakEntry.addAnotherWeapon(weapon);
         }
         else{
            streakEntry.addAnotherWeapon(weapon);
         }
      }
   }
   
   private void fillInStreakTableValues(){
      if( streakEntry != null ){
         weaponColumn.put("STREAK SRM " + streakEntry.getVolleyTotal(), null);
         volleyAmountColumn.put("STREAK SRM " + streakEntry.getVolleyTotal(), streakEntry.getVolleyTotal());
      }
   }

   private void fillInLrmTableValues(){
      if( lrmEntry != null){
         
         weaponColumn.put("LRM " + lrmEntry.getVolleyTotal(), null);
         volleyAmountColumn.put("LRM " + lrmEntry.getVolleyTotal(), lrmEntry.getVolleyTotal());
      }
   }

   private void fillInSrmTableValues(){
      if( srmEntry != null ){
         weaponColumn.put("SRM " + srmEntry.getVolleyTotal(), null);
         volleyAmountColumn.put("SRM " + srmEntry.getVolleyTotal(), srmEntry.getVolleyTotal());

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
               ammoQuantityColumn.put(weaponName, 0.0);
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
      ArrayList<Ammunition> tempListOfAmmo = new ArrayList<>();
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
         if( ammoEquipped.keySet().contains(streakEntry.getAmmoType()) ){
            ammoQuantityColumn.put(weaponName, (double)streakEntry.getAmmoType().getShotsPerTon() * ammoEquipped.get(streakEntry.getAmmoType()));
         }
         else{
            ammoQuantityColumn.put(weaponName, 0.0);
         }
      }
   }
   
   private void fillInSrmAmmoQuanitity(String weaponName){
      if( weaponName.contains("SRM") && !weaponName.contains("STREAK") ){
         if( ammoEquipped.keySet().contains(srmEntry.getAmmoType()) ){
            ammoQuantityColumn.put(weaponName, (double)srmEntry.getAmmoType().getShotsPerTon() * ammoEquipped.get(srmEntry.getAmmoType()));
         }
         else{
            ammoQuantityColumn.put(weaponName, 0.0);
         }
      }
   }

   private void fillInLrmAmmoQuantity(String weaponName){
      if( weaponName.contains("LRM") ){
         if( ammoEquipped.keySet().contains(lrmEntry.getAmmoType()) ){
            ammoQuantityColumn.put(weaponName, (double)lrmEntry.getAmmoType().getShotsPerTon() * ammoEquipped.get(lrmEntry.getAmmoType()));
         }
         else{
            ammoQuantityColumn.put(weaponName, 0.0);
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
               numberVolleyColumn.put(weaponName, 0.0);
            }
            else
               numberVolleyColumn.put(weaponName, (ammoQuantityColumn.get(weaponName) / volleyAmountColumn.get(weaponName)));
         }
         else{
            numberVolleyColumn.put(weaponName, 0.0);
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
      if( weaponName.contains("SRM") && !weaponName.contains("STREAK") && srmEntry != null){
         combatColumn.put(weaponName, (numberVolleyColumn.get(weaponName) * srmEntry.getCooldownAverage()));
      }
      else if(weaponName.contains("STREAK") && streakEntry != null){
         combatColumn.put(weaponName, (numberVolleyColumn.get(weaponName) * streakEntry.getCooldownAverage()));
      }
      else if( weaponName.contains("LRM") && lrmEntry != null ){
         combatColumn.put(weaponName, (numberVolleyColumn.get(weaponName) * lrmEntry.getCooldownAverage()));
      }
      else
         combatColumn.put(weaponName, 0.0);
   }
   

   private void fillInDamageList(){
      for(String weaponName : weaponColumn.keySet()){
         if( (weaponColumn.get(weaponName) != null) && !(weaponName.contains("SRM")) && !(weaponName.contains("LRM")) ){
         double totalDamage = weaponColumn.get(weaponName).getDamagePerShot() * ammoQuantityColumn.get(weaponName);
         damageMap.put(weaponName, totalDamage);
         
         }else if( weaponName.contains("SRM") && !weaponName.contains("STREAK") && srmEntry != null ){
            damageMap.put(weaponName, ammoQuantityColumn.get(weaponName)  * srmEntry.getDamageAverage());
         }
         else if( weaponName.contains("STREAK") && streakEntry != null ){
            damageMap.put(weaponName, (ammoQuantityColumn.get(weaponName)  * streakEntry.getDamageAverage()));
         }
         else if( weaponName.contains("LRM") && lrmEntry != null){
            damageMap.put(weaponName, (ammoQuantityColumn.get(weaponName)  * lrmEntry.getDamageAverage()));
         }
         else
            damageMap.put(weaponName, 0.0);
      }
      
      
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
            return 0.0;
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
            return 0.0;
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
