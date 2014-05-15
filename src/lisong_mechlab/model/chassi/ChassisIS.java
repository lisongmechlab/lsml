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

import lisong_mechlab.model.item.Engine;
import lisong_mechlab.model.item.Item;
import lisong_mechlab.model.loadout.Loadout;
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
 * The class is immutable as the chassis are fixed. To configure a inner sphere 'mech use {@link Loadout}.
 * 
 * @author Li Song
 */
public class ChassisIS extends ChassisBase<InternalComponent>{
   @XStreamAsAttribute
   private final int engineMin;
   @XStreamAsAttribute
   private final int engineMax;

   private static InternalComponent[] makeParts(MechDefinition aMdf, HardpointsXml aHardpoints, String aChassisMwoName){
      InternalComponent[] ans = new InternalComponent[Location.values().length];
      for(MdfComponent component : aMdf.ComponentList){
         if( Location.isRear(component.Name) ){
            continue;
         }
         final Location part = Location.fromMwoName(component.Name);
         ans[part.ordinal()] = new InternalComponent(component, part, aHardpoints, aChassisMwoName);
      }
      return ans;
   }

   public ChassisIS(ItemStatsMech aStatsMech, MechDefinition aMdf, HardpointsXml aHardpoints, int aBaseVariant, String aSeries){
      // @formatter:off
      super(aStatsMech.id, aStatsMech.name, aSeries, 
            Localization.key2string("@" + aStatsMech.name), 
            Localization.key2string("@" + aStatsMech.name + "_short"),
            makeParts(aMdf, aHardpoints, aStatsMech.name),
            aMdf.Mech.MaxJumpJets,
            aMdf.Mech.MaxTons,
            ChassisVariant.fromString(aMdf.Mech.VariantType),
            aBaseVariant,
            new BaseMovementProfile(aMdf));
      // @formatter:on
      MdfMech mdfMech = aMdf.Mech;
      engineMin = mdfMech.MinEngineRating;
      engineMax = mdfMech.MaxEngineRating;
   }

   public int getEngineMax(){
      return engineMax;
   }

   public int getEngineMin(){
      return engineMin;
   }

   @Override
   public boolean isAllowed(Item aItem){
      if( aItem instanceof Engine ){
         Engine engine = (Engine)aItem;
         return engine.getRating() >= getEngineMin() && engine.getRating() <= getEngineMax();
      }
      return super.isAllowed(aItem);
   }
}
