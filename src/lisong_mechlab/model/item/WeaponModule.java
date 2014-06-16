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
package lisong_mechlab.model.item;

import java.util.List;

import lisong_mechlab.model.pilot.PilotSkillTree;

/**
 * A {@link PilotModule} that alters weapon attributes.
 * 
 * @author Emily Björk
 */
public class WeaponModule extends PilotModule implements WeaponModifier{
   private final List<Weapon> affectedWeapon;
   private final double[]     longRangeModifier;
   private final double[]     maxRangeModifier;
   private final double[]     heatModifier;
   private final int          maxRank;

   /**
    * @param aMwoName
    *           The name of the module in the MWO data files.
    * @param aMwoIdx
    *           The ID of the module in the MWO data files.
    * @param aName
    *           The human readable name of the module.
    * @param aDescription
    *           The human readable description of the module.
    * @param aAffectedWeapon
    *           The weapon that this module affects.
    * @param aMaxRank
    *           The highest rank this module has.
    * @param aLongRangeModifier
    *           An array of <code>aMaxRank</code> length with the amounts to add or subtract from the long range of the
    *           weapon.
    * @param aMaxRangeModifier
    *           An array of <code>aMaxRank</code> length with the amounts to add or subtract from the maximum range of
    *           the weapon.
    * @param aHeatModifier
    *           An array of <code>aMaxRank</code> length with the amounts to add or subtract from the heat attribute of
    *           the weapon.
    */
   public WeaponModule(String aMwoName, int aMwoIdx, String aName, String aDescription, List<Weapon> aAffectedWeapon, int aMaxRank,
                       double aLongRangeModifier[], double aMaxRangeModifier[], double aHeatModifier[]){
      super(aMwoName, aMwoIdx, aName, aDescription);
      maxRank = aMaxRank;
      affectedWeapon = aAffectedWeapon;

      if( aLongRangeModifier.length != maxRank ){
         throw new IllegalArgumentException("Length of aLongRangeModifier must match aMaxRank!");
      }

      if( aMaxRangeModifier.length != maxRank ){
         throw new IllegalArgumentException("Length of aMaxRangeModifier must match aMaxRank!");
      }

      if( aHeatModifier.length != maxRank ){
         throw new IllegalArgumentException("Length of aHeatModifier must match aMaxRank!");
      }

      longRangeModifier = aLongRangeModifier;
      maxRangeModifier = aMaxRangeModifier;
      heatModifier = aHeatModifier;
   }

   public int getMaxRank(){
      return maxRank;
   }

   @Override
   public boolean affectsWeapon(Weapon aWeapon){
      return affectedWeapon.contains(aWeapon);
   }

   @Override
   public double extraMaxRange(Weapon aWeapon, double aRange, PilotSkillTree aPilotSkillTree){
      return aRange + maxRangeModifier[maxRank - 1];
   }

   @Override
   public double extraLongRange(Weapon aWeapon, double aRange, PilotSkillTree aPilotSkillTree){
      return aRange + longRangeModifier[maxRank - 1];
   }

   @Override
   public double extraHeat(Weapon aWeapon, double aHeat, PilotSkillTree aPilotSkillTree){
      return aHeat + heatModifier[maxRank - 1]; // TODO: Use pilot skill tree
   }

   @Override
   public double extraCooldown(Weapon aWeapon, double aCooldown, PilotSkillTree aPilotSkillTree){
      return 0; // No pilot module modifies cooldown yet.
   }
}
