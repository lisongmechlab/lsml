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
package lisong_mechlab.model.chassi;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import lisong_mechlab.model.DataCache;
import lisong_mechlab.model.item.Engine;
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

/**
 * This class is a data structure representing an arbitrary internal part of the 'mech's structure.
 * <p>
 * It is implemented as immutable.
 * 
 * @author Emily Björk
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

   /**
    * Constructs a new {@link ComponentStandard} from MWO data files that are parsed.
    * 
    * @param aComponent
    *           The component as parsed from the MWO .mdf for the chassis.
    * @param aLocation
    *           The {@link Location} (head,leg etc) this {@link ComponentStandard} is for.
    * @param aHardpoints
    *           The hard points as parsed from the MWO .xml for hard points for the chassis.
    * @param aChassiMwoName
    *           The MWO name of the chassis that this internal part will be a part of (used for hard point lookup).
    * @param aDataCache
    *           The {@link DataCache} that is currently being parsed.
    */
   public ComponentStandard(MdfComponent aComponent, Location aLocation, HardpointsXml aHardpoints, String aChassiMwoName, DataCache aDataCache){
      this(aLocation, aComponent.Slots, aComponent.HP, parseInternals(aComponent, aDataCache), parseHardPoints(aLocation, aComponent, aHardpoints,
                                                                                                               aChassiMwoName));
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
      else if( aItem instanceof JumpJet ){
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
      else if( aItem instanceof Engine ){
         return getLocation() == Location.CenterTorso;
      }
      else if( aItem == ItemDB.CASE ){
         return (getLocation() == Location.LeftTorso || getLocation() == Location.RightTorso);
      }
      return aItem.getNumCriticalSlots() <= getSlots() - getFixedItemSlots();
   }

   private static List<Item> parseInternals(MdfComponent aComponent, DataCache aDataCache){
      List<Item> ans = new ArrayList<>();
      if( null != aComponent.internals ){
         for(MdfInternal internal : aComponent.internals){
            boolean found = false;
            for(Item item : aDataCache.getItems()){
               if( item.getMwoId() == internal.ItemID ){
                  ans.add(item);
                  found = true;
                  break;
               }
            }
            if( !found ){
               throw new IllegalArgumentException("Internal not found in data cache!");
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
