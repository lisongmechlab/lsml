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

import java.io.File;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import lisong_mechlab.converter.GameDataFile;
import lisong_mechlab.model.loadout.Loadout;
import lisong_mechlab.model.mwo_parsing.HardpointsXml;
import lisong_mechlab.model.mwo_parsing.Localization;
import lisong_mechlab.model.mwo_parsing.MechDefinition;
import lisong_mechlab.model.mwo_parsing.helpers.ItemStatsMech;
import lisong_mechlab.model.mwo_parsing.helpers.MdfComponent;
import lisong_mechlab.model.mwo_parsing.helpers.MdfMech;

/**
 * This class represents a bare mech chassi. The class is immutable as the chassii are fixed. To configure a 'mech use
 * {@link Loadout}.
 * 
 * @author Emily Björk
 */
public class Chassi{
   private final ChassiClass             chassiclass;
   private final String                  name;
   private final String                  shortName;
   private final String                  mwoName;
   private final int                     maxTons;
   private final Map<Part, InternalPart> parts;
   private final int                     maxJumpJets;
   private final int                     engineMin;
   private final int                     engineMax;
   private final double                  engineFactor;
   private final int                     mwoId;

   public Chassi(ItemStatsMech aStatsMech, GameDataFile aGameData){
      MechDefinition mdf = null;
      HardpointsXml hardpoints = null;
      MdfMech mdfMech = null;
      try{
         String mdfFile = aStatsMech.mdf.replace('\\', '/');
         mdf = MechDefinition.fromXml(aGameData.openGameFile(new File(GameDataFile.MDF_ROOT, mdfFile)));
         hardpoints = HardpointsXml.fromXml(aGameData.openGameFile(new File("Game", mdf.HardpointPath)));
         mdfMech = mdf.Mech;
      }
      catch( Exception e ){
         throw new RuntimeException("Unable to load chassi configuration!", e);
      }

      name = Localization.key2string(aStatsMech.Loc.nameTag);
      shortName = Localization.key2string(aStatsMech.Loc.shortNameTag);
      mwoName = aStatsMech.name;
      mwoId = aStatsMech.id;
      engineMin = mdfMech.MinEngineRating;
      engineMax = mdfMech.MaxEngineRating;
      maxJumpJets = mdfMech.MaxJumpJets;
      maxTons = mdfMech.MaxTons;
      engineFactor = mdf.MovementTuningConfiguration.MaxMovementSpeed;
      chassiclass = ChassiClass.fromMaxTons(maxTons);

      Map<Part, InternalPart> tempParts = new HashMap<Part, InternalPart>();
      for(MdfComponent component : mdf.ComponentList){
         if( Part.isRear(component.Name) ){
            continue;
         }
         final Part part = Part.fromMwoName(component.Name);
         tempParts.put(part, new InternalPart(component, part, hardpoints, this));
      }
      parts = Collections.unmodifiableMap(tempParts);
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
      if( !(obj instanceof Chassi) )
         return false;
      return (mwoId == ((Chassi)obj).mwoId);
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

   public boolean isEcmCapable(){
      return getHardpointsCount(HardpointType.ECM) > 0;
   }

   public int getHardpointsCount(HardpointType aHardpointType){
      int sum = 0;
      for(InternalPart part : parts.values()){
         sum += part.getNumHardpoints(aHardpointType);
      }
      return sum;
   }

   public int getMwoId(){
      return mwoId;
   }
}
