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
import java.util.Arrays;
import java.util.Optional;
import org.lisoft.lsml.model.loadout.Loadout;
import org.lisoft.mwo_data.Faction;
import org.lisoft.mwo_data.mechs.Location;

/**
 * Represents an upgrade to a Mech's armour.
 *
 * @author Li Song
 */
public class ArmourUpgrade extends Upgrade {
  @XStreamAsAttribute private final double armourPerTon;
  @XStreamAsAttribute private final Internal fixedSlotItem;
  @XStreamAsAttribute private final int[] fixedSlotsForComponent;
  @XStreamAsAttribute private final int slots;

  public ArmourUpgrade(
      String aUiName,
      String aUiDesc,
      String aMwoName,
      int aMwoId,
      Faction aFaction,
      int aExtraSlots,
      double aArmourPerTon,
      int[] aFixedSlotsForComponent,
      Internal aFixedSlotItem) {
    super(aUiName, aUiDesc, aMwoName, aMwoId, aFaction);
    slots = aExtraSlots;
    armourPerTon = aArmourPerTon;
    fixedSlotsForComponent =
        aFixedSlotsForComponent == null
            ? null
            : Arrays.copyOf(aFixedSlotsForComponent, aFixedSlotsForComponent.length);
    fixedSlotItem = aFixedSlotItem;
  }

  /**
   * Calculates the mass of the given amount of armour points.
   *
   * @param aArmour The amount of armour.
   * @return The mass of the given armour amount.
   */
  public double getArmourMass(int aArmour) {
    return aArmour / armourPerTon;
  }

  /**
   * @return The number of points of armour per ton from this armour type.
   */
  public double getArmourPerTon() {
    return armourPerTon;
  }

  /**
   * @return The number of dynamic armour slots required by this upgrade.
   */
  public int getDynamicSlots() {
    return slots;
  }

  /**
   * @return An optional {@link Item} that is used for the fixed slots.
   */
  public Optional<Internal> getFixedSlotItem() {
    return Optional.ofNullable(fixedSlotItem);
  }

  /**
   * Gets the number of fixed slots on the given location.
   *
   * @param aLocation The location to query for.
   * @return A number of slots that are fixed in that location.
   */
  public int getFixedSlotsFor(Location aLocation) {
    if (null != fixedSlotsForComponent) {
      return fixedSlotsForComponent[aLocation.ordinal()];
    }
    return 0;
  }

  /**
   * @return The number of extra slots required by this upgrade.
   */
  public int getTotalSlots() {
    return getTotalSlots(null);
  }

  @Override
  public int getTotalSlots(Loadout aLoadout) {
    int ans = slots;
    if (null != fixedSlotsForComponent) {
      for (final int s : fixedSlotsForComponent) {
        ans += s;
      }
    }
    return ans;
  }

  @Override
  public double getTotalTons(Loadout aLoadout) {
    final int armour = aLoadout.getArmour();
    final Faction faction = aLoadout.getChassis().getFaction();
    return getArmourMass(armour) - UpgradeDB.getDefaultArmour(faction).getArmourMass(armour);
  }

  @Override
  public UpgradeType getType() {
    return UpgradeType.ARMOUR;
  }
}
