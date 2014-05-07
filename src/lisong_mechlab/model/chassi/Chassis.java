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

import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import lisong_mechlab.model.item.Engine;
import lisong_mechlab.model.item.Item;
import lisong_mechlab.model.item.JumpJet;
import lisong_mechlab.model.loadout.Loadout;
import lisong_mechlab.mwo_data.HardpointsXml;
import lisong_mechlab.mwo_data.Localization;
import lisong_mechlab.mwo_data.MechDefinition;
import lisong_mechlab.mwo_data.helpers.ItemStatsMech;
import lisong_mechlab.mwo_data.helpers.MdfComponent;
import lisong_mechlab.mwo_data.helpers.MdfMech;

import com.thoughtworks.xstream.annotations.XStreamAsAttribute;

/**
 * This class represents a bare mech chassis. The class is immutable as the chassis are fixed. To configure a 'mech use
 * {@link Loadout}.
 * 
 * @author Li Song
 */
public class Chassis{
   @XStreamAsAttribute
   private final ChassiClass             chassiclass;
   @XStreamAsAttribute
   private final String                  name;
   @XStreamAsAttribute
   private final String                  shortName;
   @XStreamAsAttribute
   private final String                  mwoName;
   @XStreamAsAttribute
   private final int                     maxTons;
   private final Map<Part, InternalPart> parts;
   @XStreamAsAttribute
   private final int                     maxJumpJets;
   @XStreamAsAttribute
   private final int                     engineMin;
   @XStreamAsAttribute
   private final int                     engineMax;
   @XStreamAsAttribute
   private final double                  engineFactor;
   @XStreamAsAttribute
   private final int                     mwoId;
   @XStreamAsAttribute
   private final double                  turnFactor;
   @XStreamAsAttribute
   private final double                  twistFactor;
   @XStreamAsAttribute
   private final ChassiVariant           variant;
   @XStreamAsAttribute
   private final int                     baseVariant;
   @XStreamAsAttribute
   private final String                  series;
   @XStreamAsAttribute
   private final String                  seriesShort;

   public Chassis(ItemStatsMech aStatsMech, MechDefinition aMdf, HardpointsXml aHardpoints, int aBaseVariant, String aSeries, String aSeriesShort){
      MdfMech mdfMech = aMdf.Mech;
      name = Localization.key2string("@" + aStatsMech.name);
      shortName = Localization.key2string("@" + aStatsMech.name + "_short");
      mwoName = aStatsMech.name;
      mwoId = aStatsMech.id;
      engineMin = mdfMech.MinEngineRating;
      engineMax = mdfMech.MaxEngineRating;
      maxJumpJets = mdfMech.MaxJumpJets;
      maxTons = mdfMech.MaxTons;
      engineFactor = aMdf.MovementTuningConfiguration.MaxMovementSpeed;
      chassiclass = ChassiClass.fromMaxTons(maxTons);
      turnFactor = aMdf.MovementTuningConfiguration.TorsoTurnSpeedPitch;
      twistFactor = aMdf.MovementTuningConfiguration.TorsoTurnSpeedYaw;
      variant = ChassiVariant.fromString(aMdf.Mech.VariantType);
      baseVariant = aBaseVariant;

      Map<Part, InternalPart> tempParts = new HashMap<Part, InternalPart>();
      for(MdfComponent component : aMdf.ComponentList){
         if( Part.isRear(component.Name) ){
            continue;
         }
         final Part part = Part.fromMwoName(component.Name);
         tempParts.put(part, new InternalPart(component, part, aHardpoints, this));
      }
      parts = Collections.unmodifiableMap(tempParts);

      series = aSeries;
      seriesShort = aSeriesShort;
   }

   /**
    * @return The name of the series this {@link Chassis} belongs to, e.g. "CATAPHRACT", "ATLAS" etc.
    */
   public String getSeriesName(){
      return series;
   }

   /**
    * @return The short name of the series this {@link Chassis} belongs to, e.g. "CTF", "AS7" etc.
    */
   public String getSeriesNameShort(){
      return seriesShort;
   }

   public double getSpeedFactor(){
      return engineFactor;
   }

   @Override
   public String toString(){
      return getNameShort();
   }

   @Override
   public int hashCode(){
      return mwoId;
   }

   @Override
   public boolean equals(Object obj){
      if( !(obj instanceof Chassis) )
         return false;
      return (mwoId == ((Chassis)obj).mwoId);
   }

   public int getEngineMax(){
      return engineMax;
   }

   public int getEngineMin(){
      return engineMin;
   }

   public String getName(){
      return name;
   }

   public String getNameShort(){
      return shortName;
   }

   public String getMwoName(){
      return mwoName;
   }

   public InternalPart getInternalPart(Part aPartType){
      return parts.get(aPartType);
   }

   public Collection<InternalPart> getInternalParts(){
      return parts.values();
   }

   public double getInternalMass(){
      return getMassMax() * 0.10;
   }

   /**
    * @return The ID of the base variant of this chassis, or <code>-1</code> if this is not a derived chassis type.
    */
   public int getBaseVariantId(){
      return baseVariant;
   }

   public int getMassMax(){
      return maxTons;
   }

   public int getArmorMax(){
      int ans = 0;
      for(InternalPart internalPart : parts.values()){
         ans += internalPart.getArmorMax();
      }
      return ans;
   }

   public ChassiClass getChassiClass(){
      return chassiclass;
   }

   public int getMaxJumpJets(){
      return maxJumpJets;
   }

   public int getHardpointsCount(HardPointType aHardpointType){
      int sum = 0;
      for(InternalPart part : parts.values()){
         sum += part.getNumHardpoints(aHardpointType);
      }
      return sum;
   }

   public int getMwoId(){
      return mwoId;
   }

   public boolean isSameSeries(Chassis aChassis){
      return shortName.split("-")[0].equals(aChassis.shortName.split("-")[0]);
   }

   public double getTurnFactor(){
      return 360.0 / 31.4; // Matching smurfy for now, this should be somewhere in the data files.
   }

   public double getTwistFactor(){
      return twistFactor;
   }

   /**
    * This method checks static, global constraints on an {@link Item}.
    * <p>
    * If this method returns <code>false</code> for an {@link Item}, that item will never be possible to equip on any
    * loadout based on this chassis.
    * 
    * @param aItem
    *           The {@link Item} to check for.
    * @return <code>true</code> if this chassis can equip the {@link Item}.
    */
   public boolean isAllowed(Item aItem){
      if( aItem instanceof JumpJet ){
         JumpJet jj = (JumpJet)aItem;
         return getMaxJumpJets() > 0 && jj.getMinTons() <= getMassMax() && getMassMax() < jj.getMaxTons();
      }
      else if( aItem instanceof Engine ){
         Engine engine = (Engine)aItem;
         return engine.getRating() >= getEngineMin() && engine.getRating() <= getEngineMax();
      }
      else{
         for(InternalPart part : parts.values()){
            if( part.isAllowed(aItem) )
               return true;
         }
      }
      return false;
   }

   /**
    * @return The chassis variant of this mech.
    */
   public ChassiVariant getVariantType(){
      return variant;
   }
}
