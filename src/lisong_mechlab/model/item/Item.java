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
package lisong_mechlab.model.item;

import lisong_mechlab.model.chassi.HardpointType;
import lisong_mechlab.model.loadout.Loadout;
import lisong_mechlab.model.loadout.Upgrades;
import lisong_mechlab.model.mwo_parsing.Localization;
import lisong_mechlab.model.mwo_parsing.helpers.ItemStats;

public class Item implements Comparable<Item>{
   private final String        locName;
   private final String        locDesc;
   private final String        mwoName;
   private final int           mwoIdx;

   private final int           slots;
   private final double        tons;
   private final HardpointType hardpointType;
   private final int           health;

   public Item(ItemStats anItemStats, HardpointType aHardpointType, int aNumSlots, double aNumTons, int aHealth){
      locName = Localization.key2string(anItemStats.Loc.nameTag);
      locDesc = Localization.key2string(anItemStats.Loc.descTag);
      mwoName = anItemStats.name;
      mwoIdx = Integer.parseInt(anItemStats.id);
      health = aHealth;

      slots = aNumSlots;
      tons = aNumTons;
      hardpointType = aHardpointType;
   }

   public Item(String aNameTag, String aDesc, int aSlots, int aHealth){
      locName = Localization.key2string(aNameTag);
      locDesc = Localization.key2string(aDesc);
      mwoName = "";
      mwoIdx = -1;
      health = aHealth;

      slots = aSlots;
      tons = 0;
      hardpointType = HardpointType.NONE;
   }

   public String getKey(){
      return mwoName;
   }

   @Override
   public String toString(){
      return getName();
   }

   public String getName(){
      return locName;
   }

   public int getNumCriticalSlots(Upgrades aUpgrades){
      if( aUpgrades == null )
         return slots;
      return slots;
   }

   public HardpointType getHardpointType(){
      return hardpointType;
   }

   public double getMass(Upgrades aUpgrades){
      if( aUpgrades == null )
         return tons;
      return tons;
   }

   public int getMwoIdx(){
      return mwoIdx;
   }

   public String getShortName(Upgrades aUpgrades){
      return getName(aUpgrades);
   }

   public String getDescription(){
      return locDesc;
   }

   /**
    * Determines if the given {@link Loadout} is able to equip the given item. Will consider the chassi and upgrades
    * only.
    * 
    * @param aLoadout
    * @return True if the {@link Loadout} is able to carry the weapon with current upgrades.
    */
   public boolean isEquippableOn(Loadout aLoadout){
      if( aLoadout == null )
         return true;
      return true;
   }

   /**
    * Defines the default sorting order of arbitrary items.
    * <p>
    * The sorting order is as follows:
    * <ol>
    * <li>Energy weapons</li>
    * <li>Ballistic weapons + ammo</li>
    * <li>Missile weapons + ammo</li>
    * <li>AMS + ammo</li>
    * <li>ECM</li>
    * <li>Other items except engines</li>
    * <li>Engines</li>
    * </ol>.
    */
   @Override
   public int compareTo(Item rhs){
      // Engines last
      if( this instanceof Engine && !(rhs instanceof Engine) )
         return 1;
      else if( !(this instanceof Engine) && rhs instanceof Engine )
         return -1;

      // Count ammunition types together with their parent weapon type.
      HardpointType lhsHp = this instanceof Ammunition ? ((Ammunition)this).getWeaponHardpointType() : this.getHardpointType();
      HardpointType rhsHp = rhs instanceof Ammunition ? ((Ammunition)rhs).getWeaponHardpointType() : rhs.getHardpointType();

      // Sort by hard point type (order they appear in the enumeration declaration)
      // This gives the main order of items as given in the java doc.
      int hp = lhsHp.compareTo(rhsHp);

      // Resolve ties
      if( hp == 0 ){

         // Ammunition after weapons in same hard point.
         if( this instanceof Ammunition && !(rhs instanceof Ammunition) )
            return 1;
         else if( !(this instanceof Ammunition) && rhs instanceof Ammunition )
            return -1;

         // Let weapon groups sort internally
         if( this instanceof EnergyWeapon && rhs instanceof EnergyWeapon ){
            return EnergyWeapon.DEFAULT_ORDERING.compare((EnergyWeapon)this, (EnergyWeapon)rhs);
         }
         else if( lhsHp == HardpointType.BALLISTIC ){
            return BallisticWeapon.DEFAULT_ORDERING.compare(this, rhs);
         }
         else if( lhsHp == HardpointType.MISSILE ){
            return MissileWeapon.DEFAULT_ORDERING.compare(this, rhs);
         }

         // Sort by class name, this groups single/double heat sinks together
         int classCompare = this.getClass().getName().compareTo(rhs.getClass().getName());

         // Resolve ties
         if( classCompare == 0 ){
            // Last resort: Lexicographical ordering
            return toString().compareTo(rhs.toString());
         }
         return classCompare;
      }
      return hp;
   }

   public String getName(Upgrades aUpgrades){
      if( aUpgrades == null )
         return getName();
      return getName();
   }

   public int getHealth(){
      return health;
   }
}
