/*
 * @formatter:off
 * Li Song Mechlab - A 'mech building tool for PGI's MechWarrior: Online.
 * Copyright (C) 2013  Li Song
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
package org.lisoft.lsml.model.datacache;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.TreeMap;

import org.lisoft.lsml.application.LiSongMechlabApplication;
import org.lisoft.lsml.model.chassi.Chassis;
import org.lisoft.lsml.model.chassi.ChassisOmniMech;
import org.lisoft.lsml.model.chassi.Location;
import org.lisoft.lsml.model.chassi.OmniPod;
import org.lisoft.lsml.model.loadout.StockLoadout;
import org.lisoft.lsml.model.loadout.StockLoadout.StockComponent;

/**
 * This class acts as a database for all {@link OmniPod}s.
 *
 * @author Li Song
 */
public class OmniPodDB {
    private static final Map<String, List<OmniPod>> series2pod;
    private static final Map<Integer, OmniPod> id2pod;

    private static final Map<String, OmniPod> chassiLocation2stock;

    /**
     * A decision has been made to rely on static initializers for *DB classes. The motivation is that all items are
     * immutable, and this is the only way that allows providing global item constants such as ItemDB.AMS.
     */
    static {
        final DataCache dataCache = LiSongMechlabApplication.getDataCache()
                .orElseThrow(() -> new RuntimeException("Cannot run without datacache"));

        series2pod = new HashMap<>();
        id2pod = new TreeMap<>();

        for (final OmniPod omniPod : dataCache.getOmniPods()) {
            final String series = omniPod.getChassisSeries();

            List<OmniPod> list = series2pod.get(canonize(series));
            if (list == null) {
                list = new ArrayList<>();
                series2pod.put(canonize(series), list);
            }
            list.add(omniPod);

            id2pod.put(omniPod.getMwoId(), omniPod);
        }

        chassiLocation2stock = new HashMap<>();
        for (final StockLoadout stock : StockLoadoutDB.all()) {
            for (final StockComponent comp : stock.getComponents()) {
                comp.getOmniPod().ifPresent(pod -> {
                    final String key = chassiLocationOf(stock.getChassis(), comp.getLocation());
                    chassiLocation2stock.put(key, id2pod.get(pod));
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
     * @param aChassisSeries
     *            A chassis series to get all compatible pods for.
     * @param aLocation
     *            A location on the chassis to get all compatible pods for.
     * @return A {@link Collection} of {@link OmniPod}s that are compatible with the given chassis and {@link Location}.
     */
    public static List<OmniPod> lookup(ChassisOmniMech aChassisSeries, Location aLocation) {
        return lookup(aChassisSeries.getSeriesName(), aLocation);
    }

    /**
     * @param aId
     *            The id of the pod to look up.
     * @return An {@link OmniPod} with the correct ID.
     */
    public static OmniPod lookup(int aId) {
        final OmniPod omnipod = id2pod.get(aId);
        if (omnipod == null) {
            throw new IllegalArgumentException("No omnipod with ID: " + aId);
        }
        return omnipod;
    }

    /**
     * @param aSeries
     *            A chassis series to get all compatible pods for.
     * @param aLocation
     *            A location on the chassis to get all compatible pods for.
     * @return A {@link Collection} of {@link OmniPod}s that are compatible with the given chassis and {@link Location}.
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
        return Optional.ofNullable(chassiLocation2stock.get(chassiLocationOf(aChassis, aLocation)));
    }

    private static String canonize(String aKey) {
        return aKey.toUpperCase();
    }

    private final static String chassiLocationOf(Chassis aChassis, Location aLocation) {
        return aChassis.getKey() + aLocation.shortName();
    }
}
