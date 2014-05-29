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
package lisong_mechlab.model.chassi;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import lisong_mechlab.model.item.Engine;
import lisong_mechlab.model.item.Internal;
import lisong_mechlab.model.item.Item;
import lisong_mechlab.model.item.ItemDB;
import lisong_mechlab.util.ArrayUtils;

import com.thoughtworks.xstream.annotations.XStreamAsAttribute;

/**
 * This is a base class for all mech components.
 * 
 * @author Li Song
 */
public class ComponentBase{
   @XStreamAsAttribute
   private final int        criticalslots;
   @XStreamAsAttribute
   private final double     hitpoints;
   @XStreamAsAttribute
   private final Location   location;
   @XStreamAsAttribute
   private final int        maxarmor;
   private final List<Item> fixedItems;

   /**
    * Creates a new {@link ComponentBase}.
    * 
    * @param aCriticalSlots
    *           The number of critical slots in the component.
    * @param aHitPoints
    *           The number of internal hit points on the component (determines armor too).
    * @param aLocation
    *           The location of the component.
    * @param aFixedItems
    *           An array of fixed {@link Item}s for this component.
    */
   public ComponentBase(int aCriticalSlots, double aHitPoints, Location aLocation, Item[] aFixedItems){
      criticalslots = aCriticalSlots;
      hitpoints = aHitPoints;
      location = aLocation;
      maxarmor = calculateMaxArmor(aLocation, aHitPoints);
      fixedItems = Collections.unmodifiableList(Arrays.asList(aFixedItems));
   }

   @Override
   public int hashCode(){
      final int prime = 31;
      int result = 1;
      result = prime * result + criticalslots;
      long temp;
      temp = Double.doubleToLongBits(hitpoints);
      result = prime * result + (int)(temp ^ (temp >>> 32));
      result = prime * result + ((location == null) ? 0 : location.hashCode());
      return result;
   }

   @Override
   public boolean equals(Object obj){
      if( this == obj )
         return true;
      if( obj == null )
         return false;
      if( !(obj instanceof ComponentBase) )
         return false;
      ComponentBase other = (ComponentBase)obj;
      if( criticalslots != other.criticalslots )
         return false;
      if( hitpoints != other.hitpoints )
         return false;
      if( location != other.location )
         return false;
      else if( !ArrayUtils.equalsUnordered(fixedItems, other.fixedItems) )
         return false;
      return true;
   }

   /**
    * @return An unmodifiable collection of all {@link Item}s this {@link ComponentOmniMech} has.
    */
   public Collection<Item> getFixedItems(){
      return fixedItems;
   }

   /**
    * @return The number of slots that are occupied by fixed items in this component.
    */
   public int getFixedItemSlots(){
      int ans = 0;
      for(Item item : getFixedItems()){
         ans += item.getNumCriticalSlots();
      }
      return ans;
   }

   /**
    * @return The total number of critical slots in this location.
    */
   public int getSlots(){
      return criticalslots;
   }

   /**
    * @return The {@link Location} this component is mounted at.
    */
   public Location getLocation(){
      return location;
   }

   /**
    * @return The amount of structure hit points on this component.
    */
   public double getHitPoints(){
      return hitpoints;
   }

   /**
    * @return The maximum amount of armor on this component.
    */
   public int getArmorMax(){
      return maxarmor;
   }

   /**
    * Checks if a specific item is allowed on this component checking only local, static constraints. This method is
    * only useful if {@link ChassisBase#isAllowed(Item)} returns true.
    * 
    * @param aItem
    *           The {@link Item} to check.
    * @return <code>true</code> if the given {@link Item} is allowed on this {@link ComponentStandard}.
    */
   public boolean isAllowed(Item aItem){
      if( aItem instanceof Internal ){
         return false; // Can't add internals!
      }
      else if( aItem instanceof Engine ){
         return getLocation() == Location.CenterTorso;
      }
      else if( aItem == ItemDB.CASE ){
         return (getLocation() == Location.LeftTorso || getLocation() == Location.RightTorso);
      }
      return true;
   }

   private static int calculateMaxArmor(Location aLocation, double aHP){
      return (aLocation == Location.Head) ? 18 : (int)(aHP * 2);
   }
}
