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

import lisong_mechlab.model.item.Item;

import com.thoughtworks.xstream.annotations.XStreamAsAttribute;

/**
 * This class serves as a generic base for all chassis types (IS/Clan)
 * 
 * @author Emily Björk
 */
public abstract class ChassisBase{
   @XStreamAsAttribute
   private final int             baseVariant;
   @XStreamAsAttribute
   private final ChassisClass    chassiclass;
   @XStreamAsAttribute
   private final int             maxTons;
   private final MovementProfile movementProfile;
   @XStreamAsAttribute
   private final int             mwoId;
   @XStreamAsAttribute
   private final String          mwoName;
   @XStreamAsAttribute
   private final String          name;
   @XStreamAsAttribute
   private final String          series;
   @XStreamAsAttribute
   private final String          shortName;
   @XStreamAsAttribute
   private final ChassisVariant  variant;
   @XStreamAsAttribute
   private final boolean         clan;

   public ChassisBase(int aMwoID, String aMwoName, String aSeries, String aName, String aShortName, int aMaxTons, ChassisVariant aVariant,
                      int aBaseVariant, MovementProfile aMovementProfile, boolean aIsClan){
      mwoId = aMwoID;
      mwoName = aMwoName;
      series = aSeries;
      name = aName;
      shortName = aShortName;
      maxTons = aMaxTons;
      chassiclass = ChassisClass.fromMaxTons(maxTons);
      variant = aVariant;
      baseVariant = aBaseVariant;
      movementProfile = aMovementProfile;
      clan = aIsClan;
   }

   @Override
   public boolean equals(Object obj){
      if( !this.getClass().isAssignableFrom(obj.getClass()) )
         return false;
      return (mwoId == ((ChassisBase)obj).mwoId);
   }

   /**
    * @return The maximal, total amount of armor the chassis can support.
    */
   public abstract int getArmorMax();

   /**
    * @return The ID of the base variant of this chassis, or <code>-1</code> if this is not a derived chassis type.
    */
   public int getBaseVariantId(){
      return baseVariant;
   }

   /**
    * @return The weight class of the chassis.
    */
   public ChassisClass getChassiClass(){
      return chassiclass;
   }

   /**
    * @return The total number of critical slots on this chassis.
    */
   public int getCriticalSlotsTotal(){
      return 12 * 5 + 6 * 3;
   }

   /**
    * @return The maximal tonnage the chassis can support.
    */
   public int getMassMax(){
      return maxTons;
   }

   /**
    * @return The base {@link MovementProfile} for this chassis.
    */
   public MovementProfile getMovementProfile(){
      return movementProfile;
   }

   /**
    * @return The MWO internal ID of the chassis.
    */
   public int getMwoId(){
      return mwoId;
   }

   /**
    * @return The MWO internal name of the chassis.
    */
   public String getMwoName(){
      return mwoName;
   }

   /**
    * @return The full, long name of the chassis.
    */
   public String getName(){
      return name;
   }

   /**
    * @return The short, abbreviated name of the chassis.
    */
   public String getNameShort(){
      return shortName;
   }

   /**
    * @return The name of the series this {@link ChassisStandard} belongs to, e.g. "CATAPHRACT", "ATLAS" etc.
    */
   public String getSeriesName(){
      return series;
   }

   /**
    * @return The chassis variant of this mech.
    */
   public ChassisVariant getVariantType(){
      return variant;
   }

   @Override
   public int hashCode(){
      return mwoId;
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
      if( aItem.isClan() != clan ){
         return false;
      }
      return false;
   }

   /**
    * @param aChassis
    *           The {@link ChassisBase} to compare to.
    * @return <code>true</code> if this and that chassis are of the same series (i.e. both are Hunchbacks etc).
    */
   public boolean isSameSeries(ChassisBase aChassis){
      return series.equals(aChassis.series);
   }

   /**
    * @return <code>true</code> if this chassis is a clan chassis.
    */
   public boolean isClan(){
      return clan;
   }

   @Override
   public String toString(){
      return getNameShort();
   }
}
