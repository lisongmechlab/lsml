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
   private final double[]     cooldownModifier;
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
    * @param aFaction
    *           The required faction for this module.
    * @param aCathegory
    *           The {@link ModuleCathegory} for this {@link Module}.
    * @param aModuleSlot
    *           The {@link ModuleSlot} of this {@link Module}.
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
    * @param aCooldownModifier
    *           An array of <code>aMaxRank</code> length with the amounts to add or subtract from the cooldown attribute
    *           of the weapon.
    */
   public WeaponModule(String aMwoName, int aMwoIdx, String aName, String aDescription, Faction aFaction, ModuleCathegory aCathegory,
                       ModuleSlot aModuleSlot, List<Weapon> aAffectedWeapon, int aMaxRank, double aLongRangeModifier[], double aMaxRangeModifier[],
                       double aHeatModifier[], double aCooldownModifier[]){
      super(aMwoName, aMwoIdx, aName, aDescription, aFaction, aCathegory, aModuleSlot);
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

      if( aCooldownModifier.length != maxRank ){
         throw new IllegalArgumentException("Length of aCooldownModifier must match aMaxRank!");
      }

      longRangeModifier = aLongRangeModifier;
      maxRangeModifier = aMaxRangeModifier;
      heatModifier = aHeatModifier;
      cooldownModifier = aCooldownModifier;
   }

   public int getMaxRank(){
      return maxRank;
   }

   @Override
   public boolean affectsWeapon(Weapon aWeapon){
      if( aWeapon instanceof MissileWeapon ){
         MissileWeapon baseWeapon = ((MissileWeapon)aWeapon).getBaseVariant();
         if( null != baseWeapon )
            return affectedWeapon.contains(baseWeapon);
      }
      return affectedWeapon.contains(aWeapon);
   }

   @Override
   public double extraMaxRange(Weapon aWeapon, double aRange, PilotSkillTree aPilotSkillTree){
      return maxRangeModifier[maxRank - 1];
   }

   @Override
   public double extraLongRange(Weapon aWeapon, double aRange, PilotSkillTree aPilotSkillTree){
      return longRangeModifier[maxRank - 1];
   }

   @Override
   public double extraWeaponHeat(Weapon aWeapon, double aHeat, PilotSkillTree aPilotSkillTree){
      return heatModifier[maxRank - 1];
   }

   @Override
   public double extraCooldown(Weapon aWeapon, double aCooldown, PilotSkillTree aPilotSkillTree){
      return cooldownModifier[maxRank - 1];
   }
}
