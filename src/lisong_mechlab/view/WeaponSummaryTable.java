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

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.swing.JTable;
import javax.swing.SwingConstants;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.DefaultTableCellRenderer;

import lisong_mechlab.model.item.AmmoWeapon;
import lisong_mechlab.model.item.Ammunition;
import lisong_mechlab.model.item.Item;
import lisong_mechlab.model.item.ItemDB;
import lisong_mechlab.model.item.Weapon;
import lisong_mechlab.model.loadout.Efficiencies;
import lisong_mechlab.model.loadout.Loadout;
import lisong_mechlab.model.loadout.LoadoutPart;
import lisong_mechlab.model.loadout.LoadoutPart.Message.Type;
import lisong_mechlab.model.loadout.Upgrades;
import lisong_mechlab.util.MessageXBar;
import lisong_mechlab.util.MessageXBar.Message;
import lisong_mechlab.util.MessageXBar.Reader;

/**
 * This class displays a summary of weapons and ammo for a loadout in a JTable.
 * 
 * @author Emily Björk
 */
public class WeaponSummaryTable extends JTable implements Reader{
   private static final long   serialVersionUID = 868861599143353045L;
   private final Loadout       loadout;
   private final DecimalFormat decimalFormat    = new DecimalFormat("####");

   private static class WeaponModel extends AbstractTableModel{
      private static final long serialVersionUID = 1257566726770316140L;

      class Entry{
         private final Ammunition   ammoType;
         private int                ammoTons;
         private final List<Weapon> weapons = new ArrayList<Weapon>();

         Entry(Loadout aLoadout, Item anItem){
            if( anItem instanceof Ammunition ){
               ammoType = (Ammunition)anItem;
               ammoTons = 1;
            }
            else if( anItem instanceof Weapon ){
               weapons.add((Weapon)anItem);
               if( anItem instanceof AmmoWeapon ){
                  ammoType = ((AmmoWeapon)anItem).getAmmoType(aLoadout.getUpgrades());
               }
               else
                  ammoType = null;
               ammoTons = 0;
            }
            else
               throw new IllegalArgumentException("Item must be ammuniton or weapon!");
         }

         boolean consume(Loadout aLoadout, Item anItem){
            if( ammoType != null && ammoType == anItem ){
               ammoTons++;
               return true;
            }
            else if( anItem instanceof AmmoWeapon ){
               AmmoWeapon ammoWeapon = (AmmoWeapon)anItem;
               if( ammoType != null && ammoType == ammoWeapon.getAmmoType(aLoadout.getUpgrades()) ){
                  weapons.add(ammoWeapon);
                  return true;
               }
            }
            else if( anItem instanceof Weapon ){
               Weapon weapon = (Weapon)anItem;
               if( weapons.contains(weapon) ){
                  weapons.add(weapon);
                  return true;
               }
            }
            return false;
         }

         Ammunition getAmmoType(){
            return ammoType;
         }

         List<Weapon> getWeapons(){
            return weapons;
         }

         double getNumShots(){
            if( ammoType != null )
               return ammoType.getShotsPerTon() * ammoTons;
            return Double.POSITIVE_INFINITY;
         }
      }

      List<Entry> entries = new ArrayList<>();

      public void update(Loadout aLoadout){
         entries.clear();
         for(Item item : aLoadout.getAllItems()){
            boolean found = false;
            for(Entry entry : entries){
               if( entry.consume(aLoadout, item) ){
                  found = true;
                  break;
               }
            }
            if( !found && (item instanceof Ammunition || item instanceof Weapon) ){
               entries.add(new Entry(aLoadout, item));
            }
         }
         fireTableDataChanged();
      }

      @Override
      public int getColumnCount(){
         return 1;
      }

      @Override
      public int getRowCount(){
         return entries.size();
      }

      @Override
      public Object getValueAt(int aRowIndex, int aColumnIndex){
         return entries.get(aRowIndex);
      }
   }

   private class TotalDamageColumn extends AttributeTableColumn{
      private static final long serialVersionUID = -1036416917042517947L;

      public TotalDamageColumn(){
         super("T.Dmg", 0, "The total damage potential for the ammo equipped, assuming all shots hit at full damage.");
      }

      @Override
      public String valueOf(Object aSourceRowObject){
         WeaponModel.Entry entry = (WeaponModel.Entry)aSourceRowObject;
         if( entry.getWeapons().isEmpty() || entry.getWeapons().get(0) == ItemDB.AMS )
            return decimalFormat.format(0);

         double shots = entry.getNumShots();
         Weapon protoWeapon = entry.getWeapons().get(0);
         if( protoWeapon.getDamagePerShot() == 0.0 )
            return decimalFormat.format(0);
         return decimalFormat.format(shots * protoWeapon.getDamagePerShot() / protoWeapon.getAmmoPerPerShot());
      }
   }

   private class CombatSecondsColumn extends AttributeTableColumn{
      private static final long serialVersionUID = -1036416917042517947L;

      public CombatSecondsColumn(){
         super("Seconds", 0,
               "<html>The amount of time to use all ammo given a constant maximum fire rate.<br>I.e. how long you can use it in sustained combat.</html>");
      }

      @Override
      public String valueOf(Object aSourceRowObject){
         WeaponModel.Entry entry = (WeaponModel.Entry)aSourceRowObject;
         if( entry.getWeapons().isEmpty() )
            return decimalFormat.format(0);

         double shots = entry.getNumShots();
         double shotsPerSecond = 0;
         for(Weapon weapon : entry.getWeapons()){
            if( weapon instanceof AmmoWeapon ){
               AmmoWeapon ammoWeapon = (AmmoWeapon)weapon;
               shotsPerSecond += ammoWeapon.getAmmoPerPerShot() / ammoWeapon.getSecondsPerShot(loadout.getEfficiencies());
            }
         }
         return decimalFormat.format(shots / shotsPerSecond);
      }
   }

   private class VolleyAmountColumn extends AttributeTableColumn{
      private static final long serialVersionUID = -1036416917042517947L;

      public VolleyAmountColumn(){
         super("Volleys", 0, "The number of times a weapon group can be fired.");
      }

      @Override
      public String valueOf(Object aSourceRowObject){
         WeaponModel.Entry entry = (WeaponModel.Entry)aSourceRowObject;
         if( entry.getWeapons().isEmpty() )
            return decimalFormat.format(0);

         double shots = entry.getNumShots();
         double shotsPerVolley = 0;
         for(Weapon weapon : entry.getWeapons()){
            if( weapon instanceof AmmoWeapon )
               shotsPerVolley += ((AmmoWeapon)weapon).getAmmoPerPerShot();
         }
         return decimalFormat.format(shots / shotsPerVolley);
      }
   }

   private class AmmoAmountColumn extends AttributeTableColumn{
      private static final long serialVersionUID = -1036416917042517947L;

      public AmmoAmountColumn(){
         super("Ammo", 0, "The amount of ammo equipped.");
      }

      @Override
      public String valueOf(Object aSourceRowObject){
         WeaponModel.Entry entry = (WeaponModel.Entry)aSourceRowObject;
         return decimalFormat.format(entry.getNumShots());
      }
   }

   private class WeaponColumn extends AttributeTableColumn{
      private static final long serialVersionUID = -1036416917042517947L;

      public WeaponColumn(){
         super("Weapon", 0, "The weapon equipped or the ammo if only ammo is equipped.");
      }

      @Override
      public String valueOf(Object aSourceRowObject){
         WeaponModel.Entry entry = (WeaponModel.Entry)aSourceRowObject;
         // Weapon that doesn't use ammo
         if( entry.getAmmoType() == null ){
            // Ammo type will only be null if the entry was constructed with a Weapon that is not
            // AmmoWeapon, thus the weapons list will contain at least one entry. This is safe.
            String weaponName = entry.getWeapons().get(0).getShortName(loadout.getUpgrades());
            if( entry.getWeapons().size() > 1 ){
               return entry.getWeapons().size() + " x " + weaponName;
            }
            return weaponName;
         }
         // Ammo without matching weapon
         if( entry.getWeapons().isEmpty() ){
            return entry.getAmmoType().getShortName(loadout.getUpgrades());
         }
         // 1 >= AmmoWeapon with 0 or more tons of ammo.
         Weapon protoWeapon = entry.getWeapons().get(0);
         String weaponName = protoWeapon.getShortName(loadout.getUpgrades());
         if( protoWeapon.getName().toLowerCase().contains("srm") || protoWeapon.getName().toLowerCase().contains("lrm") ){
            Pattern pattern = Pattern.compile("(\\D+)(\\d+)");
            String prefix = null;
            int size = 0;
            for(Weapon weapon : entry.getWeapons()){
               Matcher matcher = pattern.matcher(weapon.getName());
               if( !matcher.matches() ){
                  throw new RuntimeException("Pattern didn't match! [" + weapon.getName() + "]");
               }
               if( prefix == null ){
                  prefix = matcher.group(1);
               }
               else{
                  if( !prefix.equals(matcher.group(1)) ){
                     throw new RuntimeException("Prefix missmatch! Expected [" + prefix + "] was [" + matcher.group(1) + "]");
                  }
               }
               size += Integer.parseInt(matcher.group(2));
            }
            return prefix + size;
         }
         if( entry.getWeapons().size() > 1 ){
            return entry.getWeapons().size() + " x " + weaponName;
         }
         return weaponName;
      }
   }

   public WeaponSummaryTable(Loadout aLoadout, MessageXBar aXBar){
      super(new WeaponModel());
      loadout = aLoadout;
      decimalFormat.setGroupingUsed(true);
      decimalFormat.setGroupingSize(3);
      ((WeaponModel)getModel()).update(loadout);
      aXBar.attach(this);
      setFillsViewportHeight(true);
      ((DefaultTableCellRenderer)getTableHeader().getDefaultRenderer()).setHorizontalAlignment(SwingConstants.CENTER);

      removeColumn(getColumnModel().getColumn(0));
      addColumn(new WeaponColumn());
      addColumn(new AmmoAmountColumn());
      addColumn(new VolleyAmountColumn());
      addColumn(new CombatSecondsColumn());
      addColumn(new TotalDamageColumn());
      getColumnModel().getColumn(0).setMinWidth(80);
      getColumnModel().getColumn(1).setMinWidth(30);
   }

   @Override
   public void receive(Message aMsg){
      if( aMsg.isForMe(loadout) ){
         if( aMsg instanceof LoadoutPart.Message ){
            LoadoutPart.Message message = (LoadoutPart.Message)aMsg;
            if( message.type == Type.ItemAdded || message.type == Type.ItemRemoved || message.type == Type.ItemsChanged ){
               ((WeaponModel)getModel()).update(loadout);
            }
         }
         else if( (aMsg instanceof Upgrades.Message) || (aMsg instanceof Efficiencies.Message) ){
            ((WeaponModel)getModel()).update(loadout);
         }
      }
   }
}
