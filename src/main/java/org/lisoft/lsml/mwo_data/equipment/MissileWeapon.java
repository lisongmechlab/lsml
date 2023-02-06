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

import org.lisoft.lsml.mwo_data.*;
import org.lisoft.lsml.mwo_data.mechs.HardPointType;
import org.lisoft.lsml.mwo_data.mechs.Upgrades;
import org.lisoft.lsml.mwo_data.modifiers.Attribute;

public class MissileWeapon extends AmmoWeapon {
  private final int baseItemId;
  /**
   * This variable is set through reflection in a post-processing pass when parsing the game data
   * due to data order dependencies. It will still be null for Artemis types.
   */
  private final GuidanceUpgrade requiredGuidance = null;

  private final int requiredGuidanceID;

  public MissileWeapon(
      // Item Arguments
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
      Attribute aCooldown,
      WeaponRangeProfile aRangeProfile,
      int aRoundsPerShot,
      int aVolleySize,
      double aDamagePerProjectile,
      int aProjectilesPerRound,
      Attribute aProjectileSpeed,
      int aGhostHeatGroupId,
      double aGhostHeatMultiplier,
      Attribute aGhostHeatMaxFreeAlpha,
      double aVolleyDelay,
      double aImpulse,
      // AmmoWeapon Arguments
      String aAmmoType,
      boolean aOneShot,
      int aAmmoPerShot,
      // MissileWeapon Arguments
      int aRequiredGuidanceId,
      int aBaseItemId) {
    super( // Item Arguments
        aName,
        aDesc,
        aMwoName,
        aMwoId,
        aSlots,
        aTons,
        HardPointType.MISSILE,
        aHP,
        aFaction,
        // HeatSource Arguments
        aHeat,
        // Weapon Arguments
        aCooldown,
        aRangeProfile,
        aRoundsPerShot,
        aVolleySize,
        aDamagePerProjectile,
        aProjectilesPerRound,
        aProjectileSpeed,
        aGhostHeatGroupId,
        aGhostHeatMultiplier,
        aGhostHeatMaxFreeAlpha,
        aVolleyDelay,
        aImpulse,
        // AmmoWeapon Arguments
        aAmmoType,
        aOneShot,
        aAmmoPerShot);
    requiredGuidanceID = aRequiredGuidanceId;
    baseItemId = aBaseItemId;
  }

  public int getBaseVariant() {
    return baseItemId;
  }

  @Override
  public double getMass() {
    if (isArtemisCapable()) {
      return super.getMass() + requiredGuidance.getTons();
    }
    return super.getMass();
  }

  /**
   * @return If this weapon requires a specific upgrade, this will return that upgrade, otherwise
   *     returns <code>null</code>.
   */
  public GuidanceUpgrade getRequiredUpgrade() {
    //noinspection ConstantConditions -- set by reflection, not always null
    return requiredGuidance;
  }

  /**
   * @return If this weapon requires a specific upgrade, this will return that upgrade, otherwise
   *     returns <= 0.
   */
  public int getRequiredUpgradeID() {
    return requiredGuidanceID;
  }

  @Override
  public int getSlots() {
    if (isArtemisCapable()) {
      return super.getSlots() + requiredGuidance.getSlots();
    }
    return super.getSlots();
  }

  @Override
  public boolean hasSpread() {
    return super.hasSpread() && getAliases().contains("srm");
  }

  public boolean isArtemisCapable() {
    //noinspection ConstantConditions -- set by reflection, not always null
    return requiredGuidance != null;
  }

  @Override
  public boolean isCompatible(Upgrades aUpgrades) {
    if (isArtemisCapable()) {
      return aUpgrades.getGuidance() == requiredGuidance;
    }
    return super.isCompatible(aUpgrades);
  }
}
