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
package lisong_mechlab.model.item;

import lisong_mechlab.model.chassi.HardPointType;
import lisong_mechlab.model.upgrades.Upgrades;
import lisong_mechlab.mwo_data.Localization;
import lisong_mechlab.mwo_data.helpers.ItemStats;

import com.thoughtworks.xstream.annotations.XStreamAsAttribute;

public class Item implements Comparable<Item>{
   private final String        locName;
   private final String        locDesc;
   @XStreamAsAttribute
   private final String        mwoName;
   @XStreamAsAttribute
   private final int           mwoIdx;

   @XStreamAsAttribute
   private final int           slots;
   @XStreamAsAttribute
   private final double        tons;
   @XStreamAsAttribute
   private final HardPointType hardpointType;
   @XStreamAsAttribute
   private final int           health;

   public Item(ItemStats anItemStats, HardPointType aHardpointType, int aNumSlots, double aNumTons, int aHealth){
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
      mwoName = aNameTag;
      mwoIdx = -1;
      health = aHealth;

      slots = aSlots;
      tons = 0;
      hardpointType = HardPointType.NONE;
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

   public HardPointType getHardpointType(){
      return hardpointType;
   }

   public double getMass(Upgrades aUpgrades){
      if( aUpgrades == null )
         return tons;
      return tons;
   }

   public int getMwoId(){
      return mwoIdx;
   }

   public String getShortName(Upgrades aUpgrades){
      return getName(aUpgrades);
   }

   public String getDescription(){
      return locDesc;
   }

   /**
    * This method checks if this {@link Item} can be equipped in combination with the given {@link Upgrades}.
    * 
    * @param aUpgrades
    *           The {@link Upgrades} to check against.
    * @return <code>true</code> if this {@link Item} is compatible with the given upgrades.
    */
   @SuppressWarnings("unused") // Interface
   public boolean isCompatible(Upgrades aUpgrades){
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
      HardPointType lhsHp = this instanceof Ammunition ? ((Ammunition)this).getWeaponHardpointType() : this.getHardpointType();
      HardPointType rhsHp = rhs instanceof Ammunition ? ((Ammunition)rhs).getWeaponHardpointType() : rhs.getHardpointType();

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
         else if( lhsHp == HardPointType.BALLISTIC ){
            return BallisticWeapon.DEFAULT_ORDERING.compare(this, rhs);
         }
         else if( lhsHp == HardPointType.MISSILE ){
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
