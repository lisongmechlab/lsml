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

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import lisong_mechlab.model.item.Engine;
import lisong_mechlab.model.item.Item;
import lisong_mechlab.model.item.ItemDB;

/**
 * This class is a data structure representing an arbitrary internal part of the 'mech's structure.
 * <p>
 * It is implemented as immutable.
 * 
 * @author Li Song
 */
public class ComponentStandard extends ComponentBase{
   private final List<HardPoint> hardPoints = new ArrayList<>();

   /**
    * Creates a new {@link ComponentStandard} with the given properties.
    * 
    * @param aSlots
    *           The total number of slots in this component.
    * @param aLocation
    *           The location that the component is mounted at.
    * @param aHP
    *           The hit points of the component.
    * @param aFixedItems
    *           An array of internal items and other items that are locked.
    * @param aHardPoints
    *           A {@link List} of {@link HardPoint}s for the component.
    */
   public ComponentStandard(Location aLocation, int aSlots, double aHP, List<Item> aFixedItems, List<HardPoint> aHardPoints){
      super(aSlots, aHP, aLocation, aFixedItems);
      hardPoints.addAll(aHardPoints);
   }

   public int getHardPointCount(HardPointType aHardpointType){
      int ans = 0;
      for(HardPoint it : hardPoints){
         if( it.getType() == aHardpointType ){
            ans++;
         }
      }
      return ans;
   }

   public Collection<HardPoint> getHardPoints(){
      return Collections.unmodifiableList(hardPoints);
   }

   /**
    * @return <code>true</code> if this component has missile bay doors.
    */
   public boolean hasMissileBayDoors(){
      for(HardPoint hardPoint : hardPoints){
         if( hardPoint.hasBayDoor() ){
            return true;
         }
      }
      return false;
   }

   @Override
   public boolean isAllowed(Item aItem){
      if( aItem.getHardpointType() != HardPointType.NONE && getHardPointCount(aItem.getHardpointType()) <= 0 ){
         return false;
      }
      else if( aItem instanceof Engine ){
         return getLocation() == Location.CenterTorso;
      }
      else if( aItem == ItemDB.CASE ){
         return (getLocation() == Location.LeftTorso || getLocation() == Location.RightTorso);
      }
      if( aItem.getNumCriticalSlots() > getSlots() - getFixedItemSlots() ){
         return false;
      }
      return super.isAllowed(aItem);
   }
}
