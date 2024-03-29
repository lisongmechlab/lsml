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
package org.lisoft.mwo_data.equipment;

import com.thoughtworks.xstream.annotations.XStreamAsAttribute;
import java.util.Collection;
import org.lisoft.lsml.model.ItemDB;
import org.lisoft.mwo_data.Faction;
import org.lisoft.mwo_data.mechs.HardPointType;
import org.lisoft.mwo_data.modifiers.Attribute;
import org.lisoft.mwo_data.modifiers.Modifier;

/**
 * Base class for weapons that consume ammunition.
 *
 * @author Li Song
 */
public class AmmoWeapon extends Weapon {
  /** This field will be set through reflection in a post-processing pass. */
  @XStreamAsAttribute private final Ammunition ammoHalfType = null;

  @XStreamAsAttribute private final int ammoPerShot;
  /** This field will be set through reflection in a post-processing pass. */
  @XStreamAsAttribute private final Ammunition ammoType = null;

  @XStreamAsAttribute private final String ammoTypeId;
  @XStreamAsAttribute private final boolean oneShot;
  @XStreamAsAttribute private final double volleyDelay;
  @XStreamAsAttribute private final int volleySize;

  public AmmoWeapon(
      // Item Arguments
      String aName,
      String aDesc,
      String aMwoName,
      int aMwoId,
      int aSlots,
      double aTons,
      HardPointType aHardPointType,
      double aHP,
      Faction aFaction,
      // HeatSource Arguments
      Attribute aHeat,
      // Weapon Arguments
      Attribute aCoolDown,
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
      int aAmmoPerShot) {
    super(
        aName,
        aDesc,
        aMwoName,
        aMwoId,
        aSlots,
        aTons,
        aHardPointType,
        aHP,
        aFaction,
        aHeat,
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
    ammoTypeId = aAmmoType;
    volleySize = aVolleySize;
    volleyDelay = aVolleyDelay;
    ammoPerShot = aAmmoPerShot;
    oneShot = aOneShot;
  }

  public Ammunition getAmmoHalfType() {
    //noinspection ConstantConditions -- set by reflection, not always null
    return ammoHalfType;
  }

  /**
   * Obtain the MWO identifier of the ammo that goes with this weapon so that it can be looked up in
   * the {@link ItemDB}.
   *
   * @return The {@link String} name of the ammo type required for this weapon.
   */
  public String getAmmoId() {
    return ammoTypeId;
  }

  public int getAmmoPerShot() {
    return ammoPerShot;
  }

  public Ammunition getAmmoType() {
    //noinspection ConstantConditions -- set by reflection, not always null
    return ammoType;
  }

  public int getBuiltInRounds() {
    return hasBuiltInAmmo() ? getRoundsPerShot() : 0;
  }

  @Override
  public double getCoolDown(Collection<Modifier> aModifiers) {
    return isOneShot() ? Double.POSITIVE_INFINITY : super.getCoolDown(aModifiers);
  }

  public double getFiringDelay() {
    double numVolleys = Math.ceil((double) getRoundsPerShot() / (double) volleySize);
    return (numVolleys - 1) * volleyDelay;
  }

  @Override
  public double getRawFiringPeriod(Collection<Modifier> aModifiers) {
    double firingDelay = getFiringDelay();
    double coolDown = getCoolDown(aModifiers);
    return isOneShot() ? Double.POSITIVE_INFINITY : firingDelay + coolDown;
  }

  /**
   * One shot weapons typically have built in ammunition.
   *
   * @return <code>true</code> if the weapon has builtin ammo.
   * @see #isOneShot()
   */
  public boolean hasBuiltInAmmo() {
    return null == ammoTypeId;
  }

  public boolean isCompatibleAmmo(Ammunition aAmmunition) {
    if (hasBuiltInAmmo()) {
      return false;
    }
    return ammoTypeId.equals(aAmmunition.getAmmoId());
  }

  public boolean isOneShot() {
    return oneShot;
  }
}
