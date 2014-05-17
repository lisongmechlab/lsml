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
import lisong_mechlab.model.item.JumpJet;
import lisong_mechlab.model.loadout.LoadoutStandard;
import lisong_mechlab.mwo_data.HardpointsXml;
import lisong_mechlab.mwo_data.Localization;
import lisong_mechlab.mwo_data.MechDefinition;
import lisong_mechlab.mwo_data.helpers.ItemStatsMech;
import lisong_mechlab.mwo_data.helpers.MdfComponent;
import lisong_mechlab.mwo_data.helpers.MdfMech;

import com.thoughtworks.xstream.annotations.XStreamAsAttribute;

/**
 * This class represents a bare inner sphere 'mech chassis.
 * <p>
 * The class is immutable as the chassis are fixed. To configure a inner sphere 'mech use {@link LoadoutStandard}.
 * 
 * @author Li Song
 */
public class ChassisStandard extends ChassisBase{
   @XStreamAsAttribute
   private final int                 engineMin;
   @XStreamAsAttribute
   private final int                 engineMax;
   @XStreamAsAttribute
   private final int                 maxJumpJets;
   private final InternalComponent[] components;

   public ChassisStandard(ItemStatsMech aStatsMech, MechDefinition aMdf, HardpointsXml aHardpoints, int aBaseVariant, String aSeries, List<Internal> aInternalsList){
      // @formatter:off
      super(aStatsMech.id, aStatsMech.name, aSeries, 
            Localization.key2string("@" + aStatsMech.name), 
            Localization.key2string("@" + aStatsMech.name + "_short"),
            aMdf.Mech.MaxTons,
            ChassisVariant.fromString(aMdf.Mech.VariantType),
            aBaseVariant,
            new BaseMovementProfile(aMdf.MovementTuningConfiguration),
            false);
      // @formatter:on
      MdfMech mdfMech = aMdf.Mech;
      engineMin = mdfMech.MinEngineRating;
      engineMax = mdfMech.MaxEngineRating;

      maxJumpJets = aMdf.Mech.MaxJumpJets;
      components = new InternalComponent[Location.values().length];
      for(MdfComponent component : aMdf.ComponentList){
         if( Location.isRear(component.Name) ){
            continue;
         }
         final Location part = Location.fromMwoName(component.Name);
         components[part.ordinal()] = new InternalComponent(component, part, aHardpoints, getMwoName(), aInternalsList);
      }
   }

   public int getEngineMax(){
      return engineMax;
   }

   public int getEngineMin(){
      return engineMin;
   }

   /**
    * @return The maximal, total amount of armor the chassis can support.
    */
   @Override
   public int getArmorMax(){
      int ans = 0;
      for(InternalComponent internalPart : components){
         ans += internalPart.getArmorMax();
      }
      return ans;
   }
   
   /**
    * @param aHardpointType
    *           The type of hard points to count.
    * @return The number of hard points of the given type.
    */
   public int getHardpointsCount(HardPointType aHardpointType){
      int sum = 0;
      for(InternalComponent part : components){
         sum += part.getHardPointCount(aHardpointType);
      }
      return sum;
   }

   /**
    * @param aLocation
    *           The location of the internal component we're interested in.
    * @return The internal component in the given location.
    */
   public InternalComponent getComponent(Location aLocation){
      return components[aLocation.ordinal()];
   }

   /**
    * @return A {@link Collection} of all the internal components.
    */
   public Collection<InternalComponent> getComponents(){
      return Collections.unmodifiableList(Arrays.asList(components));
   }

   /**
    * @return The maximal number of jump jets the chassis can support.
    */
   public int getJumpJetsMax(){
      return maxJumpJets;
   }

   @Override
   public boolean isAllowed(Item aItem){
      if( aItem instanceof Engine ){
         Engine engine = (Engine)aItem;
         return engine.getRating() >= getEngineMin() && engine.getRating() <= getEngineMax();
      }
      else if( aItem instanceof JumpJet ){
         JumpJet jj = (JumpJet)aItem;
         return getJumpJetsMax() > 0 && jj.getMinTons() <= getMassMax() && getMassMax() < jj.getMaxTons();
      }
      for(InternalComponent part : components){
         if( part.isAllowed(aItem) )
            return true;
      }
      return super.isAllowed(aItem);
   }
}
