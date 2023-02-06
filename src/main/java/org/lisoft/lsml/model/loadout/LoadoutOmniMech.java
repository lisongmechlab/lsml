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
import java.util.ArrayList;
import java.util.Collection;
import org.lisoft.lsml.mwo_data.equipment.Engine;
import org.lisoft.lsml.mwo_data.mechs.*;
import org.lisoft.lsml.mwo_data.modifiers.Modifier;
import org.lisoft.lsml.util.CommandStack.Command;

/**
 * This class represents a configured loadout for an omnimech.
 *
 * @author Li Song
 */
@XStreamAlias("loadout")
public class LoadoutOmniMech extends Loadout {
  private final transient Upgrades upgrades;

  /**
   * Creates a new, empty loadout.
   *
   * @param aComponents The components of this loadout.
   * @param aChassis The chassis to base this loadout on.
   * @param aUpgrades The upgrades to use.
   * @param aWeaponGroups The weapon groups object for this loadout.
   */
  public LoadoutOmniMech(
      ConfiguredComponentOmniMech[] aComponents,
      ChassisOmniMech aChassis,
      Upgrades aUpgrades,
      WeaponGroups aWeaponGroups) {
    super(aComponents, aChassis, aWeaponGroups);
    upgrades = aUpgrades;
  }

  @Override
  public boolean equals(Object obj) {
    if (this == obj) {
      return true;
    }
    if (!(obj instanceof LoadoutOmniMech)) {
      return false;
    }
    if (!super.equals(obj)) {
      return false;
    }
    final LoadoutOmniMech other = (LoadoutOmniMech) obj;
    return upgrades.equals(other.upgrades);
  }

  @Override
  public ChassisOmniMech getChassis() {
    return (ChassisOmniMech) super.getChassis();
  }

  @Override
  public ConfiguredComponentOmniMech getComponent(Location aLocation) {
    return (ConfiguredComponentOmniMech) super.getComponent(aLocation);
  }

  @Override
  public Engine getEngine() {
    return getChassis().getFixedEngine();
  }

  @Override
  public int getJumpJetsMax() {
    int ans = getChassis().getFixedJumpJets();
    for (final Location location : Location.values()) {
      ans += getComponent(location).getOmniPod().getJumpJetsMax();
    }
    return ans;
  }

  @Override
  public Collection<Modifier> getQuirks() {
    final Collection<Modifier> ans = new ArrayList<>();
    for (final Location location : Location.values()) {
      ans.addAll(getComponent(location).getOmniPod().getQuirks());
    }

    boolean hasSetBonus = true;
    final OmniPodSet omniPodSet = getComponent(Location.CenterTorso).getOmniPod().getOmniPodSet();
    for (final Location location : Location.values()) {
      if (getComponent(location).getOmniPod().getOmniPodSet() != omniPodSet) {
        hasSetBonus = false;
        break;
      }
    }
    if (hasSetBonus) {
      ans.addAll(omniPodSet.getModifiers());
    }
    return ans;
  }

  /**
   * @return The number of globally used critical slots.
   */
  @Override
  public int getSlotsUsed() {
    int ans = 0;
    for (final Location location : Location.values()) {
      ans += getComponent(location).getSlotsUsed();
    }
    return ans;
  }

  @Override
  public Upgrades getUpgrades() {
    return upgrades;
  }

  @Override
  public int hashCode() {
    final int prime = 31;
    int result = super.hashCode();
    result = prime * result + (upgrades == null ? 0 : upgrades.hashCode());
    return result;
  }

  /**
   * This setter method is only intended to be used from package local {@link Command}s. It's a raw,
   * unchecked accessor.
   *
   * @param aOmniPod The omnipod to set, it's put in it's dedicated slot.
   */
  public void setOmniPod(OmniPod aOmniPod) {
    final ConfiguredComponentOmniMech component = getComponent(aOmniPod.getLocation());
    component.changeOmniPod(aOmniPod);
  }
}
