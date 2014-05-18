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
import lisong_mechlab.model.item.Internal;
import lisong_mechlab.model.item.Item;
import lisong_mechlab.model.item.ItemDB;
import lisong_mechlab.model.item.JumpJet;
import lisong_mechlab.mwo_data.HardPointCache;
import lisong_mechlab.mwo_data.HardpointsXml;
import lisong_mechlab.mwo_data.WeaponDoorSet;
import lisong_mechlab.mwo_data.WeaponDoorSet.WeaponDoor;
import lisong_mechlab.mwo_data.helpers.HardPointInfo;
import lisong_mechlab.mwo_data.helpers.MdfComponent;
import lisong_mechlab.mwo_data.helpers.MdfInternal;
import lisong_mechlab.util.ArrayUtils;

import com.thoughtworks.xstream.annotations.XStreamAsAttribute;

/**
 * This class is a data structure representing an arbitrary internal part of the 'mech's structure.
 * <p>
 * It is implemented as immutable.
 * 
 * @author Li Song
 */
public class InternalComponent{
   @XStreamAsAttribute
   private final int             criticalslots;
   private final List<HardPoint> hardPoints = new ArrayList<>();
   @XStreamAsAttribute
   private final double          hitpoints;

   protected final List<Item>    internals  = new ArrayList<Item>();
   @XStreamAsAttribute
   private final Location        location;
   @XStreamAsAttribute
   private final int             internalSlots;
   @XStreamAsAttribute
   private final int             maxarmor;

   /**
    * Creates a new {@link InternalComponent} with the given properties.
    * 
    * @param aSlots
    *           The total number of slots in this component.
    * @param aLocation
    *           The location that the component is mounted at.
    * @param aHP
    *           The hit points of the component.
    * @param aInternalItems
    *           A {@link List} of internal items and other items that are locked.
    * @param aHardPoints
    *           A {@link List} of {@link HardPoint}s for the component.
    */
   public InternalComponent(Location aLocation, int aSlots, double aHP, List<Item> aInternalItems, List<HardPoint> aHardPoints){
      criticalslots = aSlots;
      location = aLocation;
      hitpoints = aHP;
      internals.addAll(aInternalItems);
      hardPoints.addAll(aHardPoints);
      maxarmor = calculateMaxArmor(aLocation, aHP);
      internalSlots = calculateInternalSlots(internals);
   }

   /**
    * Constructs a new {@link InternalComponent} from MWO data files that are parsed.
    * 
    * @param aComponent
    *           The component as parsed from the MWO .mdf for the chassis.
    * @param aLocation
    *           The {@link Location} (head,leg etc) this {@link InternalComponent} is for.
    * @param aHardpoints
    *           The hard points as parsed from the MWO .xml for hard points for the chassis.
    * @param aChassiMwoName
    *           The MWO name of the chassis that this internal part will be a part of (used for hard point lookup).
    * @param aInternalsList
    *           A list to insert any internals created during the loading (used to extract internal actuators etc to the
    *           ItemDB to avoid data duplication).
    */
   public InternalComponent(MdfComponent aComponent, Location aLocation, HardpointsXml aHardpoints, String aChassiMwoName,
                            List<Internal> aInternalsList){
      this(aLocation, aComponent.Slots, aComponent.HP, parseInternals(aComponent, aInternalsList), parseHardPoints(aLocation, aComponent,
                                                                                                                   aHardpoints, aChassiMwoName));
   }

   @Override
   public boolean equals(Object obj){
      if( this == obj )
         return true;
      if( !(obj instanceof InternalComponent) )
         return false;
      InternalComponent other = (InternalComponent)obj;

      //@formatter:off
      return criticalslots == other.criticalslots && 
             location == other.location && 
             maxarmor == other.maxarmor && 
             hitpoints == other.hitpoints && 
             ArrayUtils.equalsUnordered(internals, other.internals) && 
             ArrayUtils.equalsUnordered(hardPoints, other.hardPoints);
      //@formatter:on
   }

   public int getArmorMax(){
      return maxarmor;
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

   public double getHitPoints(){
      return hitpoints;
   }

   public Collection<Item> getInternalItems(){
      return Collections.unmodifiableList(internals);
   }

   /**
    * @return The {@link Location} this component is mounted at.
    */
   public Location getLocation(){
      return location;
   }

   /**
    * @return The total number of critical slots in this location.
    */
   public int getSlots(){
      return criticalslots;
   }

   @Override
   public int hashCode(){
      final int prime = 31;
      int result = 1;
      result = prime * result + criticalslots;
      result = prime * result + hardPoints.hashCode();
      long temp;
      temp = Double.doubleToLongBits(hitpoints);
      result = prime * result + (int)(temp ^ (temp >>> 32));
      result = prime * result + internals.hashCode();
      result = prime * result + maxarmor;
      result = prime * result + location.hashCode();
      return result;
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

   /**
    * Checks if a specific item is allowed on this component checking only local, static constraints. This method is
    * only useful if {@link ChassisStandard#isAllowed(Item)} returns true.
    * 
    * @param aItem
    *           The {@link Item} to check.
    * @return <code>true</code> if the given {@link Item} is allowed on this {@link InternalComponent}.
    */
   public boolean isAllowed(Item aItem){
      if( aItem instanceof Internal ){
         return false; // Can't add internals!
      }
      else if( aItem instanceof Engine ){
         return getLocation() == Location.CenterTorso;
      }
      else if( aItem instanceof JumpJet ){
         switch( location ){
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
      else if( aItem == ItemDB.CASE ){
         return (location == Location.LeftTorso || location == Location.RightTorso);
      }
      else if( aItem.getHardpointType() != HardPointType.NONE && getHardPointCount(aItem.getHardpointType()) <= 0 ){
         return false;
      }
      return aItem.getNumCriticalSlots() <= getSlots() - internalSlots;
   }

   @Override
   public String toString(){
      return getLocation().toString();
   }

   private static int calculateMaxArmor(Location aLocation, double aHP){
      return (aLocation == Location.Head) ? 18 : (int)(aHP * 2);
   }

   private static int calculateInternalSlots(List<Item> aItems){
      int ans = 0;
      for(Item item : aItems){
         if( item instanceof Internal )
            ans += item.getNumCriticalSlots();
      }
      return ans;
   }

   private static List<Item> parseInternals(MdfComponent aComponent, List<Internal> aInternalsList){
      List<Item> ans = new ArrayList<>();
      if( null != aComponent.internals ){
         for(MdfInternal internal : aComponent.internals){
            boolean found = false;
            for(Internal i : aInternalsList){
               if( i.getKey().equals(internal.Name) ){
                  if( i.getNumCriticalSlots() != internal.Slots ){
                     throw new RuntimeException("Slots missmatch between internals.");
                  }
                  ans.add(i);
                  found = true;
                  break;
               }
            }
            if( !found ){
               Internal i = new Internal(internal);
               ans.add(i);
               aInternalsList.add(i);
            }
         }
      }
      return ans;
   }

   private static List<HardPoint> parseHardPoints(Location aLocation, MdfComponent aComponent, HardpointsXml aHardpoints, String aChassiMwoName){
      List<HardPoint> ans = new ArrayList<>();
      if( null != aComponent.hardpoints ){
         for(MdfComponent.Hardpoint hardpoint : aComponent.hardpoints){
            final HardPointType hardpointType = HardPointType.fromMwoType(hardpoint.Type);

            HardPointInfo hardPointInto = null;
            for(HardPointInfo hpi : aHardpoints.hardpoints){
               if( hpi.id == hardpoint.ID ){
                  hardPointInto = hpi;
               }
            }

            if( hardPointInto == null ){
               throw new NullPointerException("Found no matching hardpoint in the data files!");
            }

            boolean hasBayDoors = false;
            if( hardPointInto.NoWeaponAName != null && aHardpoints.weapondoors != null ){
               for(WeaponDoorSet doorSet : aHardpoints.weapondoors){
                  for(WeaponDoor weaponDoor : doorSet.weaponDoors){
                     if( hardPointInto.NoWeaponAName.equals(weaponDoor.AName) ){
                        hasBayDoors = true;
                     }
                  }
               }
            }

            if( hardpointType == HardPointType.MISSILE ){
               List<Integer> tubes = aHardpoints.tubesForId(hardpoint.ID);
               for(Integer tube : tubes){
                  if( tube < 1 ){
                     ans.add(HardPointCache.getHardpoint(hardpoint.ID, aChassiMwoName, aLocation));
                  }
                  else{
                     ans.add(new HardPoint(HardPointType.MISSILE, tube, hasBayDoors));
                  }
               }
            }
            else{
               for(int i = 0; i < aHardpoints.slotsForId(hardpoint.ID); ++i)
                  ans.add(new HardPoint(hardpointType));
            }
         }

         // For any mech with more than 2 missile hard points in CT, any launcher beyond the largest one can only
         // have 5 tubes (anything else is impossible to fit)
         if( aLocation == Location.CenterTorso ){
            int missileHps = 0;
            for(HardPoint hardPoint : ans){
               if( hardPoint.getType() == HardPointType.MISSILE )
                  missileHps++;
            }
            if( missileHps > 1 ){
               int maxTubes = 0;
               for(HardPoint hardpoint : ans){
                  maxTubes = Math.max(hardpoint.getNumMissileTubes(), maxTubes);
               }

               boolean maxAdded = false;
               for(int i = 0; i < ans.size(); ++i){
                  if( ans.get(i).getType() != HardPointType.MISSILE )
                     continue;
                  int tubes = ans.get(i).getNumMissileTubes();
                  if( (tubes < maxTubes && tubes > 5) || (tubes == maxTubes && maxAdded == true && tubes > 5) ){
                     ans.set(i, new HardPoint(HardPointType.MISSILE, 5, ans.get(i).hasBayDoor()));
                  }
                  if( tubes == maxTubes )
                     maxAdded = true;
               }
            }
         }
      }

      // Stupid PGI making hacks to put ECM on a hard point... now I have to change my code...
      if( aComponent.CanEquipECM == 1 )
         ans.add(new HardPoint(HardPointType.ECM));

      return ans;
   }
}
