/*
 * Li Song Mechlab - A 'mech building tool for PGI's MechWarrior: Online.
 * Copyright (C) 2013-2023  Li Song
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
package org.lisoft.lsml.mwo_data.equipment;

import java.util.Collection;
import org.lisoft.lsml.mwo_data.Faction;
import org.lisoft.lsml.mwo_data.mechs.HardPointType;
import org.lisoft.lsml.mwo_data.modifiers.Attribute;
import org.lisoft.lsml.mwo_data.modifiers.Modifier;

/**
 * An immutable class that represents an energy weapon.
 *
 * @author Li Song
 */
public class EnergyWeapon extends Weapon {
  protected final Attribute burnTime;

  public EnergyWeapon( // Item Arguments
      String aName,
      String aDesc,
      String aMwoName,
      int aMwoId,
      int aSlots,
      double aTons,
      double aHP,
      Faction aFaction,
      // HeatSource Arguments
      Attribute aHeat,
      // Weapon Arguments
      Attribute aCoolDown,
      WeaponRangeProfile aRangeProfile,
      int aRoundsPerShot,
      double aDamagePerProjectile,
      int aProjectilesPerRound,
      Attribute aProjectileSpeed,
      int aGhostHeatGroupId,
      double aGhostHeatMultiplier,
      Attribute aGhostHeatMaxFreeAlpha,
      double aVolleyDelay,
      double aImpulse,
      // EnergyWeapon Arguments
      Attribute aBurnTime) {
    super( // Item Arguments
        aName,
        aDesc,
        aMwoName,
        aMwoId,
        aSlots,
        aTons,
        HardPointType.ENERGY,
        aHP,
        aFaction,
        // HeatSource Arguments
        aHeat,
        // Weapon Arguments
        aCoolDown,
        aRangeProfile,
        aRoundsPerShot,
        aDamagePerProjectile,
        aProjectilesPerRound,
        aProjectileSpeed,
        aGhostHeatGroupId,
        aGhostHeatMultiplier,
        aGhostHeatMaxFreeAlpha,
        aImpulse);
    burnTime = aBurnTime;
  }

  /**
   * Returns the burn duration of a laser beam. Will return 0 for energy weapons that do not have a
   * burn duration and {@link Double#POSITIVE_INFINITY} for weapons that are "streaming", i.e. that
   * are active for as long as the trigger is held down. Such as flamers and TAGs.
   *
   * @param aModifiers A collection of modifiers that might affect the duration value.
   * @return A real value representing the burn duration.
   */
  public double getDuration(Collection<Modifier> aModifiers) {
    return burnTime.value(aModifiers);
  }

  @Override
  public double getRawFiringPeriod(Collection<Modifier> aModifiers) {
    if (burnTime.value(null) == Double.POSITIVE_INFINITY) {
      return getCoolDown(aModifiers);
    }
    return getCoolDown(aModifiers) + getDuration(aModifiers);
  }
}
