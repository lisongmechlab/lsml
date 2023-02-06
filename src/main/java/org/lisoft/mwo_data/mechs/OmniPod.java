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
package org.lisoft.mwo_data.mechs;

import com.thoughtworks.xstream.annotations.XStreamAsAttribute;
import java.util.*;
import org.lisoft.mwo_data.Faction;
import org.lisoft.mwo_data.ItemDB;
import org.lisoft.mwo_data.equipment.Item;
import org.lisoft.mwo_data.equipment.MwoObject;
import org.lisoft.mwo_data.modifiers.Modifier;

/**
 * This class represents an omnipod of an OmniMech configuration.
 *
 * @author Li Song
 */
public class OmniPod extends MwoObject {
  private static final List<Integer> ALLOWED_TOGGLEABLE_IDS = List.of(ItemDB.HA_ID, ItemDB.LAA_ID);
  @XStreamAsAttribute private final String chassis;
  private final List<Item> fixedItems;
  private final List<HardPoint> hardPoints;
  @XStreamAsAttribute private final Location location;
  @XStreamAsAttribute private final int maxJumpJets;
  private final OmniPodSet omniPodSet;
  @XStreamAsAttribute private final Collection<Modifier> quirks;
  @XStreamAsAttribute private final String series;
  private final List<Item> toggleableItems;

  /**
   * Creates a new {@link OmniPod}.
   *
   * @param aMwoId The MWO ID of this {@link OmniPod}.
   * @param aLocation The {@link Location} that this omni pod can be mounted at.
   * @param aSeriesName The name of the series this {@link OmniPod} belongs to, for example "TIMBER
   *     WOLF".
   * @param aOriginalChassisID The MWO ID of the specific variant that this {@link OmniPod} is part
   *     of, for example "TIMBER WOLF PRIME".
   * @param aOmniPodSet The {@link OmniPodSet} that this omni pod belongs to.
   * @param aQuirks A {@link Collection} of {@link Modifier}s this {@link OmniPod} will bring to the
   *     loadout if equipped.
   * @param aHardPoints A {@link List} of {@link HardPoint}s for this {@link OmniPod}.
   * @param aFixedItems A {@link List} of fixed items on this {@link OmniPod}.
   * @param aToggleableItems A {@link List} of items in this {@link OmniPod} that may be toggled.
   * @param aMaxJumpJets The maximum number of jump jets this {@link OmniPod} can support.
   * @param aFaction The faction this omnipod is for.
   */
  public OmniPod(
      int aMwoId,
      Location aLocation,
      String aSeriesName,
      String aOriginalChassisID,
      OmniPodSet aOmniPodSet,
      Collection<Modifier> aQuirks,
      List<HardPoint> aHardPoints,
      List<Item> aFixedItems,
      List<Item> aToggleableItems,
      int aMaxJumpJets,
      Faction aFaction) {
    super(aSeriesName, "", "", aMwoId, aFaction);
    location = aLocation;
    series = aSeriesName.toUpperCase();
    chassis = aOriginalChassisID.toUpperCase();
    omniPodSet = aOmniPodSet;
    quirks = aQuirks;
    hardPoints = aHardPoints;
    maxJumpJets = aMaxJumpJets;
    fixedItems = aFixedItems;
    toggleableItems = aToggleableItems;

    for (Item item : toggleableItems) {
      if (!ALLOWED_TOGGLEABLE_IDS.contains(item.getId())) {
        String sb =
            "OmniPod ID: "
                + aMwoId
                + " for "
                + aOriginalChassisID.toUpperCase()
                + " - "
                + location.longName()
                + " has a nonsensical toggleable item: "
                + item.getName()
                + " stopping parsing to prevent potential data corruption.";
        throw new RuntimeException(sb);
      }
    }
  }

  /**
   * @return The name of the chassis that this {@link OmniPod} belongs to.
   */
  public String getChassisName() {
    return chassis;
  }

  /**
   * @return The chassis series this {@link OmniPod} is part of. For example "DIRE WOLF".
   */
  public String getChassisSeries() {
    return series;
  }

  /**
   * @return An unmodifiable {@link List} of {@link Item}s that are fixed on this {@link OmniPod}.
   *     Typically empty.
   */
  public List<Item> getFixedItems() {
    return Collections.unmodifiableList(fixedItems);
  }

  /**
   * @param aHardPointType The type of {@link HardPoint}s to count.
   * @return The number of {@link HardPoint}s of the given type.
   */
  public int getHardPointCount(HardPointType aHardPointType) {
    int ans = 0;
    for (final HardPoint it : hardPoints) {
      if (it.getType() == aHardPointType) {
        ans++;
      }
    }
    return ans;
  }

  /**
   * @return An unmodifiable collection of all {@link HardPoint}s this {@link OmniPod} has.
   */
  public Collection<HardPoint> getHardPoints() {
    return Collections.unmodifiableCollection(hardPoints);
  }

  /**
   * @return The maximum number of jump jets one can equip on this omnipod.
   */
  public int getJumpJetsMax() {
    return maxJumpJets;
  }

  /**
   * @return {@link Location} that this omnipod can be equipped on.
   */
  public Location getLocation() {
    return location;
  }

  /**
   * @return The {@link OmniPodSet} that this {@link OmniPod} belongs to.
   */
  public OmniPodSet getOmniPodSet() {
    return omniPodSet;
  }

  /**
   * @return The omnipod specific movement quirks.
   */
  public Collection<Modifier> getQuirks() {
    return quirks;
  }

  /**
   * @return An unmodifiable {@link List} of {@link Item}s that are toggleable on this {@link
   *     OmniPod}. Typically only LAA and HA.
   */
  public List<Item> getToggleableItems() {
    return Collections.unmodifiableList(toggleableItems);
  }

  /**
   * @return <code>true</code> if this {@link OmniPod} has missile bay doors.
   */
  public boolean hasMissileBayDoors() {
    for (final HardPoint hardPoint : hardPoints) {
      if (hardPoint.hasMissileBayDoor()) {
        return true;
      }
    }
    return false;
  }

  /**
   * @param aChassis The chassis to check for compatibility to.
   * @return <code>true</code> if the argument is a compatible chassis.
   */
  public boolean isCompatible(ChassisOmniMech aChassis) {
    return aChassis.getSeriesName().toUpperCase().equals(series);
  }

  @Override
  public String toString() {
    return getChassisName();
  }
}
