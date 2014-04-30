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
public class InternalPart{
   @XStreamAsAttribute
   private final Part            type;
   @XStreamAsAttribute
   private final int             criticalslots;
   @XStreamAsAttribute
   private final int             maxarmor;
   @XStreamAsAttribute
   private final double          hitpoints;

   @XStreamAsAttribute
   // TODO: Make this computed
   private final int             internalSlots;

   private final List<Item>      internals  = new ArrayList<Item>();
   private final List<HardPoint> hardpoints = new ArrayList<>();

   /**
    * Constructs a new {@link InternalPart} from MWO data files that are parsed.
    * 
    * @param aComponent
    *           The component as parsed from the MWO .mdf for the chassis.
    * @param aPart
    *           The {@link Part} (head,leg etc) this {@link InternalPart} is for.
    * @param aHardpoints
    *           The hard points as parsed from the MWO .xml for hard points for the chassis.
    * @param aChassi
    *           The chassis that this internal part will be a part of.
    */
   public InternalPart(MdfComponent aComponent, Part aPart, HardpointsXml aHardpoints, Chassis aChassi){
      criticalslots = aComponent.Slots;
      type = aPart;
      hitpoints = aComponent.HP;
      maxarmor = (type == Part.Head) ? 18 : (int)(hitpoints * 2);

      if( null != aComponent.internals ){
         int internalsSize = 0;
         for(MdfInternal internal : aComponent.internals){
            Internal i = new Internal(internal);
            internals.add(i);
            internalsSize += i.getNumCriticalSlots(null);
         }
         internalSlots = internalsSize;
      }
      else{
         internalSlots = 0;
      }

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
                     hardpoints.add(HardPointCache.getHardpoint(hardpoint.ID, aChassi.getMwoName(), aPart));
                  }
                  else{
                     hardpoints.add(new HardPoint(HardPointType.MISSILE, tube, hasBayDoors));
                  }
               }
            }
            else{
               for(int i = 0; i < aHardpoints.slotsForId(hardpoint.ID); ++i)
                  hardpoints.add(new HardPoint(hardpointType));
            }
         }

         // For any mech with more than 2 missile hard points in CT, any launcher beyond the largest one can only
         // have 5 tubes (anything else is impossible to fit)
         if( type == Part.CenterTorso && getNumHardpoints(HardPointType.MISSILE) > 1 ){
            int maxTubes = 0;
            for(HardPoint hardpoint : hardpoints){
               maxTubes = Math.max(hardpoint.getNumMissileTubes(), maxTubes);
            }

            boolean maxAdded = false;
            for(int i = 0; i < hardpoints.size(); ++i){
               if( hardpoints.get(i).getType() != HardPointType.MISSILE )
                  continue;
               int tubes = hardpoints.get(i).getNumMissileTubes();
               if( (tubes < maxTubes && tubes > 5) || (tubes == maxTubes && maxAdded == true && tubes > 5) ){
                  hardpoints.set(i, new HardPoint(HardPointType.MISSILE, 5, hardpoints.get(i).hasBayDoor()));
               }
               if( tubes == maxTubes )
                  maxAdded = true;
            }
         }
      }

      // Stupid PGI making hacks to put ECM on a hard point... now I have to change my code...
      if( aComponent.CanEquipECM == 1 )
         hardpoints.add(new HardPoint(HardPointType.ECM));
   }

   @Override
   public String toString(){
      return getType().toString();
   }

   @Override
   public int hashCode(){
      final int prime = 31;
      int result = 1;
      result = prime * result + criticalslots;
      result = prime * result + hardpoints.hashCode();
      long temp;
      temp = Double.doubleToLongBits(hitpoints);
      result = prime * result + (int)(temp ^ (temp >>> 32));
      result = prime * result + internals.hashCode();
      result = prime * result + maxarmor;
      result = prime * result + type.hashCode();
      return result;
   }

   @Override
   public boolean equals(Object obj){
      if( this == obj )
         return true;
      if( !(obj instanceof InternalPart) )
         return false;
      InternalPart other = (InternalPart)obj;

      //@formatter:off
      return criticalslots == other.criticalslots && 
             type == other.type && 
             maxarmor == other.maxarmor && 
             hitpoints == other.hitpoints && 
             ArrayUtils.equalsUnordered(internals, other.internals) && 
             ArrayUtils.equalsUnordered(hardpoints, other.hardpoints);
      //@formatter:on
   }

   public Part getType(){
      return type;
   }

   public int getArmorMax(){
      return maxarmor;
   }

   public int getNumCriticalslots(){
      return criticalslots;
   }

   public int getNumHardpoints(HardPointType aHardpointType){
      int ans = 0;
      for(HardPoint it : hardpoints){
         if( it.getType() == aHardpointType ){
            ans++;
         }
      }
      return ans;
   }

   public Collection<Item> getInternalItems(){
      return Collections.unmodifiableList(internals);
   }

   public double getHitpoints(){
      return hitpoints;
   }

   public Collection<HardPoint> getHardpoints(){
      return Collections.unmodifiableList(hardpoints);
   }

   /**
    * Checks if a specific item is allowed on this component checking only local, static constraints. This method is
    * only useful if {@link Chassis#isAllowed(Item)} returns true.
    * 
    * @param aItem
    *           The {@link Item} to check.
    * @return <code>true</code> if the given {@link Item} is allowed on this {@link InternalPart}.
    */
   public boolean isAllowed(Item aItem){
      if( aItem instanceof Internal ){
         return false; // Can't add internals!
      }
      else if( aItem instanceof Engine ){
         return getType() == Part.CenterTorso;
      }
      else if( aItem instanceof JumpJet ){
         switch( type ){
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
         return (type == Part.LeftTorso || type == Part.RightTorso);
      }
      else if( aItem.getHardpointType() != HardPointType.NONE && getNumHardpoints(aItem.getHardpointType()) <= 0 ){
         return false;
      }
      return aItem.getNumCriticalSlots(null) <= getNumCriticalslots() - internalSlots;
   }

   /**
    * @return <code>true</code> if this component has missile bay doors.
    */
   public boolean hasMissileBayDoors(){
      for(HardPoint hardPoint : hardpoints){
         if( hardPoint.hasBayDoor() ){
            return true;
         }
      }
      return false;
   }
}
