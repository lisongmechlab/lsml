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
import org.lisoft.mwo_data.Faction;
import org.lisoft.mwo_data.ItemDB;
import org.lisoft.mwo_data.mechs.HardPointType;

/**
 * This class models a guidance upgrade.
 *
 * @author Li Song
 */
public class GuidanceUpgrade extends Upgrade {
  @XStreamAsAttribute private final int slots;
  @XStreamAsAttribute private final double spreadFactor;
  @XStreamAsAttribute private final double tons;

  public GuidanceUpgrade(
      String aUiName,
      String aUiDesc,
      String aMwoName,
      int aMwoId,
      Faction aFaction,
      int aSlots,
      double aTons,
      double aSpreadFactor) {
    super(aUiName, aUiDesc, aMwoName, aMwoId, aFaction);
    slots = aSlots;
    tons = aTons;
    spreadFactor = aSpreadFactor;
  }

  public int getSlots() {
    return slots;
  }

  /**
   * @return The spread factor for this guidance upgrade.
   */
  public double getSpreadFactor() {
    return spreadFactor;
  }

  public double getTons() {
    return tons;
  }

  @Override
  public UpgradeType getType() {
    return UpgradeType.ARTEMIS;
  }

  /**
   * Upgrades a {@link Ammunition} to match this guidance type.
   *
   * @param aOldAmmo The {@link Ammunition} to upgrade.
   * @return An {@link Ammunition} object of the appropriate type for this guidance.
   */
  public Ammunition upgrade(Ammunition aOldAmmo) {
    if (aOldAmmo.getWeaponHardPointType() != HardPointType.MISSILE) {
      return aOldAmmo;
    }

    for (final MissileWeapon weapon : ItemDB.lookup(MissileWeapon.class)) {
      if (weapon.isCompatibleAmmo(aOldAmmo)) {
        final MissileWeapon representative = upgrade(weapon);

        for (final Ammunition ammunition : ItemDB.lookup(Ammunition.class)) {
          if (representative.isCompatibleAmmo(ammunition)
              && ammunition.getMass() == aOldAmmo.getMass()) {
            return ammunition;
          }
        }
        break;
      }
    }

    throw new RuntimeException("Unable to find upgraded version of: " + aOldAmmo);
  }

  /**
   * Upgrades a {@link MissileWeapon} to match this guidance type.
   *
   * @param aOldWeapon The {@link MissileWeapon} to upgrade.
   * @return A {@link MissileWeapon} which is an appropriate variant for this guidance type.
   */
  public MissileWeapon upgrade(MissileWeapon aOldWeapon) {
    final int baseVariant = aOldWeapon.getBaseVariant();
    if (baseVariant <= 0) {
      return aOldWeapon;
    }

    for (final MissileWeapon weapon : ItemDB.lookup(MissileWeapon.class)) {
      if (weapon.getBaseVariant() == baseVariant && weapon.getRequiredUpgrade() == this) {
        return weapon;
      }
    }
    throw new RuntimeException("Unable to find upgraded version of: " + baseVariant);
  }
}
