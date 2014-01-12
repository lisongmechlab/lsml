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

import lisong_mechlab.model.item.Internal;
import lisong_mechlab.model.item.Item;
import lisong_mechlab.model.mwo_parsing.HardpointsXml;
import lisong_mechlab.model.mwo_parsing.helpers.MdfComponent;
import lisong_mechlab.model.mwo_parsing.helpers.MdfInternal;
import lisong_mechlab.util.ArrayUtils;

/**
 * This class is a data structure representing an arbitrary internal part of the 'mech's structure.
 * <p>
 * It is implemented as immutable.
 * 
 * @author Emily Björk
 */
public class InternalPart{
   private final int             criticalslots;
   private final Part            type;
   private final int             maxarmor;
   private final double          hitpoints;
   private final List<Item>      internals  = new ArrayList<Item>();
   private final List<Hardpoint> hardpoints = new ArrayList<>();

   /**
    * Constructs a new {@link InternalPart} from MWO datafiles that are parsed.
    * 
    * @param aComponent
    *           The component as parsed from the MWO .mdf for the chassis.
    * @param aPart
    *           The {@link Part} (head,leg etc) this {@link InternalPart} is for.
    * @param aHardpoints
    *           The hardpoints as parsed from the MWO .xml for hard points for the chassis.
    * @param aChassi
    *           The chassi that this internal part will be a part of.
    */
   public InternalPart(MdfComponent aComponent, Part aPart, HardpointsXml aHardpoints, Chassi aChassi){
      criticalslots = aComponent.Slots;
      type = aPart;
      hitpoints = aComponent.HP;
      maxarmor = (type == Part.Head) ? 18 : (int)(hitpoints * 2);

      if( null != aComponent.internals ){
         for(MdfInternal internal : aComponent.internals){
            internals.add(new Internal(internal));
         }
      }

      if( null != aComponent.hardpoints ){
         for(MdfComponent.Hardpoint hardpoint : aComponent.hardpoints){
            final HardpointType hardpointType = HardpointType.fromMwoType(hardpoint.Type);

            if( hardpointType == HardpointType.MISSILE ){
               List<Integer> tubes = aHardpoints.tubesForId(hardpoint.ID);
               for(Integer tube : tubes){
                  // FIXME: Hardcoded case for hbk-4j which has 2 LRM10s as an LRM20 but the data files are missleading
                  if( aChassi.getNameShort().equals("HBK-4J") && aPart == Part.RightTorso ){
                     tube = 10;
                  }
                  if( tube < 1 ){
                     hardpoints.add(HardpointCache.getHardpoint(hardpoint.ID, aChassi.getMwoName(), aPart));
                  }
                  else{
                     hardpoints.add(new Hardpoint(HardpointType.MISSILE, tube));
                  }
               }
            }
            else{
               for(int i = 0; i < aHardpoints.slotsForId(hardpoint.ID); ++i)
                  hardpoints.add(new Hardpoint(hardpointType));
            }
         }

         // For any mech with more than 2 missile hardpoints in CT, any launcher beyond the largest one can only
         // have 5 tubes (anything else is impossible to fit)
         if( type == Part.CenterTorso && getNumHardpoints(HardpointType.MISSILE) > 1 ){
            int maxTubes = 0;
            for(Hardpoint hardpoint : hardpoints){
               maxTubes = Math.max(hardpoint.getNumMissileTubes(), maxTubes);
            }

            boolean maxAdded = false;
            for(int i = 0; i < hardpoints.size(); ++i){
               if( hardpoints.get(i).getType() != HardpointType.MISSILE )
                  continue;
               int tubes = hardpoints.get(i).getNumMissileTubes();
               if( (tubes < maxTubes && tubes > 5) || (tubes == maxTubes && maxAdded == true && tubes > 5) ){
                  hardpoints.set(i, new Hardpoint(HardpointType.MISSILE, 5));
               }
               if( tubes == maxTubes )
                  maxAdded = true;
            }
         }
      }

      // Stupid PGI making hacks to put ECM on a hardpoint... now I have to change my code...
      if( aComponent.CanEquipECM == 1 )
         hardpoints.add(new Hardpoint(HardpointType.ECM));
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

   public int getNumHardpoints(HardpointType aHardpointType){
      int ans = 0;
      for(Hardpoint it : hardpoints){
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

   public Collection<Hardpoint> getHardpoints(){
      return Collections.unmodifiableList(hardpoints);
   }
}
