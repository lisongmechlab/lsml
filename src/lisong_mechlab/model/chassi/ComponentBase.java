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

import java.util.Collections;
import java.util.List;

import lisong_mechlab.model.item.Engine;
import lisong_mechlab.model.item.HeatSink;
import lisong_mechlab.model.item.Item;
import lisong_mechlab.model.item.JumpJet;

import com.thoughtworks.xstream.annotations.XStreamAsAttribute;

/**
 * This is a base class for all mech components.
 * 
 * @author Li Song
 */
public abstract class ComponentBase{
   @XStreamAsAttribute
   private final int        slots;
   @XStreamAsAttribute
   private final double     hitpoints;
   @XStreamAsAttribute
   private final Location   location;
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
   public ComponentBase(int aCriticalSlots, double aHitPoints, Location aLocation, List<Item> aFixedItems){
      slots = aCriticalSlots;
      hitpoints = aHitPoints;
      location = aLocation;
      fixedItems = Collections.unmodifiableList(aFixedItems);
   }

   /**
    * @return An unmodifiable collection of all {@link Item}s this {@link ComponentOmniMech} has.
    */
   public List<Item> getFixedItems(){
      return fixedItems;
   }

   /**
    * @return The number of slots that are occupied by fixed items in this component.
    */
   public int getFixedItemSlots(){
      int ans = 0;
      int hs = 0;
      int hsSlots = 0;
      int hsSize = 0;
      for(Item item : getFixedItems()){
         ans += item.getNumCriticalSlots();
         if( item instanceof Engine ){
            Engine engine = (Engine)item;
            hsSlots = engine.getNumHeatsinkSlots();
         }
         else if( item instanceof HeatSink ){
            hs++;
            hsSize = item.getNumCriticalSlots();
         }
      }
      return ans - Math.min(hs, hsSlots) * hsSize;
   }

   /**
    * @return The total number of critical slots in this location.
    */
   public int getSlots(){
      return slots;
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
      return calculateMaxArmor(getLocation(), hitpoints);
   }

   @Override
   public String toString(){
      return getLocation().toString();
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
      return isAllowed(aItem, null);
   }
   
   /**
    * Checks if a specific item is allowed on this component checking only local, static constraints. This method is
    * only useful if {@link ChassisBase#isAllowed(Item)} returns true.
    * 
    * @param aItem
    *           The {@link Item} to check.
    * @param aEngine If not <code>null</code>, this engine is assumed to be equipped.
    * @return <code>true</code> if the given {@link Item} is allowed on this {@link ComponentStandard}.
    */
   public boolean isAllowed(Item aItem, @SuppressWarnings("unused") Engine aEngine){
      if( aItem instanceof JumpJet ){
         switch( getLocation() ){
            case RightTorso:
            case CenterTorso:
            case LeftTorso:
            case RightLeg:
            case LeftLeg:
               return true;
            default:
               return false;
         }
      }
      return true;
   }

   private static int calculateMaxArmor(Location aLocation, double aHP){
      return (aLocation == Location.Head) ? 18 : (int)(aHP * 2);
   }
}
