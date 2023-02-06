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
package org.lisoft.lsml.mwo_data;

import java.util.*;
import org.lisoft.lsml.mwo_data.equipment.NoSuchItemException;
import org.lisoft.lsml.mwo_data.mechs.*;
import org.lisoft.lsml.mwo_data.mechs.StockLoadout.StockComponent;
import org.lisoft.lsml.view_fx.LiSongMechLab;

/**
 * This class acts as a database for all {@link OmniPod}s.
 *
 * @author Li Song
 */
public class OmniPodDB {
  private static final Map<String, OmniPod> chassisLocation2stock;
  private static final Map<Integer, OmniPod> id2pod;
  private static final Map<String, List<OmniPod>> series2pod;

  /*
   * A decision has been made to rely on static initializers for *DB classes. The motivation is that
   * all items are immutable, and this is the only way that allows providing global item constants
   * such as ItemDB.AMS.
   */
  static {
    final Database database = LiSongMechLab.getDatabase();

    series2pod = new HashMap<>();
    id2pod = new HashMap<>();
    for (final OmniPod omniPod : database.getOmniPods()) {
      final String series = omniPod.getChassisSeries();
      series2pod.computeIfAbsent(canonize(series), k -> new ArrayList<>()).add(omniPod);
      id2pod.put(omniPod.getId(), omniPod);
    }

    chassisLocation2stock = new HashMap<>();
    for (final StockLoadout stock : StockLoadoutDB.all()) {
      for (final StockComponent comp : stock.getComponents()) {
        comp.getOmniPod()
            .ifPresent(
                pod -> {
                  try {
                    String key = chassiLocationOf(stock.getChassis(), comp.getLocation());
                    chassisLocation2stock.put(key, id2pod.get(pod));
                  } catch (final NoSuchItemException e) {
                    throw new RuntimeException(e);
                  }
                });
      }
    }
  }

  /**
   * @return A {@link Collection} of all {@link OmniPod}s.
   */
  public static Collection<OmniPod> all() {
    return id2pod.values();
  }

  /**
   * @param aChassisSeries A chassis series to get all compatible pods for.
   * @param aLocation A location on the chassis to get all compatible pods for.
   * @return A {@link Collection} of {@link OmniPod}s that are compatible with the given chassis and
   *     {@link Location}.
   */
  public static List<OmniPod> lookup(ChassisOmniMech aChassisSeries, Location aLocation) {
    return lookup(aChassisSeries.getSeriesName(), aLocation);
  }

  /**
   * @param aId The id of the pod to look up.
   * @return An {@link OmniPod} with the correct ID.
   * @throws NoSuchItemException if no omnipod could be found with the given ID.
   */
  public static OmniPod lookup(int aId) throws NoSuchItemException {
    final OmniPod omnipod = id2pod.get(aId);
    if (omnipod == null) {
      throw new NoSuchItemException("No omnipod with ID: " + aId);
    }
    return omnipod;
  }

  /**
   * @param aSeries A chassis series to get all compatible pods for.
   * @param aLocation A location on the chassis to get all compatible pods for.
   * @return A {@link Collection} of {@link OmniPod}s that are compatible with the given chassis and
   *     {@link Location}.
   */
  public static List<OmniPod> lookup(String aSeries, Location aLocation) {
    final List<OmniPod> ans = new ArrayList<>();
    final String canonized = canonize(aSeries);
    for (final OmniPod omniPod : series2pod.get(canonized)) {
      if (omniPod.getLocation() == aLocation) {
        ans.add(omniPod);
      }
    }
    return ans;
  }

  public static Optional<OmniPod> lookupStock(ChassisOmniMech aChassis, Location aLocation) {
    return Optional.ofNullable(chassisLocation2stock.get(chassiLocationOf(aChassis, aLocation)));
  }

  private static String canonize(String aKey) {
    return aKey.toUpperCase();
  }

  private static String chassiLocationOf(Chassis aChassis, Location aLocation) {
    return aChassis.getKey() + aLocation.shortName();
  }
}
