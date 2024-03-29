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
package org.lisoft.lsml.model.loadout;

import com.thoughtworks.xstream.annotations.XStreamAlias;
import java.util.Collection;
import org.lisoft.mwo_data.equipment.Engine;
import org.lisoft.mwo_data.equipment.Item;
import org.lisoft.mwo_data.mechs.ChassisStandard;
import org.lisoft.mwo_data.mechs.Location;
import org.lisoft.mwo_data.mechs.UpgradesMutable;
import org.lisoft.mwo_data.modifiers.Modifier;

/**
 * This class represents the complete state of a Mech's configuration.
 *
 * @author Li Song
 */
@XStreamAlias("loadout")
public class LoadoutStandard extends Loadout {
  private final UpgradesMutable upgrades;

  /**
   * Will create a new, empty load out based on the given chassis.
   *
   * @param aComponents The components of this loadout.
   * @param aChassis The chassis to base the load out on.
   * @param aUpgradesMutable The {@link UpgradesMutable} that will be used for this chassis.
   * @param aWeaponGroups A {@link WeaponGroups} object to model the weapon groups with.
   */
  LoadoutStandard(
      ConfiguredComponentStandard[] aComponents,
      ChassisStandard aChassis,
      UpgradesMutable aUpgradesMutable,
      WeaponGroups aWeaponGroups) {
    super(aComponents, aChassis, aWeaponGroups);

    upgrades = aUpgradesMutable;
  }

  @Override
  public boolean equals(Object obj) {
    if (this == obj) {
      return true;
    }
    if (!(obj instanceof final LoadoutStandard other)) {
      return false;
    }
    if (!super.equals(obj)) {
      return false;
    }
    return upgrades.equals(other.upgrades);
  }

  @Override
  public ChassisStandard getChassis() {
    return (ChassisStandard) super.getChassis();
  }

  @Override
  public ConfiguredComponentStandard getComponent(Location aLocation) {
    return (ConfiguredComponentStandard) super.getComponent(aLocation);
  }

  /**
   * @return The {@link Engine} equipped on this loadout, or <code>null</code> if no engine is
   *     equipped.
   */
  @Override
  public Engine getEngine() {
    // The engine is not among the fixed items for a standard loadout.
    for (final Item item : getComponent(Location.CenterTorso).getItemsEquipped()) {
      if (item instanceof Engine) {
        return (Engine) item;
      }
    }
    return null;
  }

  @Override
  public int getJumpJetsMax() {
    return getChassis().getJumpJetsMax();
  }

  @Override
  public Collection<Modifier> getQuirks() {
    return getChassis().getQuirks();
  }

  @Override
  public int getSlotsUsed() {
    int ans =
        getUpgrades().getStructure().getDynamicSlots()
            + getUpgrades().getArmour().getDynamicSlots();
    for (final ConfiguredComponent component : getComponents()) {
      ans += component.getSlotsUsed();
    }
    return ans;
  }

  @Override
  public UpgradesMutable getUpgrades() {
    return upgrades;
  }

  @Override
  public int hashCode() {
    final int prime = 31;
    int result = super.hashCode();
    result = prime * result + upgrades.hashCode();
    return result;
  }
}
