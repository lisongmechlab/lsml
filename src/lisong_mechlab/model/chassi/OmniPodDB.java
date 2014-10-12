/*
 * @formatter:off
 * Li Song Mechlab - A 'mech building tool for PGI's MechWarrior: Online.
 * Copyright (C) 2013  Emily Björk
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
package lisong_mechlab.model.chassi;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import lisong_mechlab.model.DataCache;

/**
 * This class acts as a database for all {@link OmniPod}s.
 * 
 * @author Emily Björk
 */
public class OmniPodDB {
    private static final Map<String, List<OmniPod>> series2pod;
    private static final Map<Integer, OmniPod>      id2pod;

    /**
     * @param aChassis
     *            The chassis to get the pods for.
     * @return A {@List} of the {@link OmniPod}s that are "original" to the given chassis.
     */
    public static List<OmniPod> lookupOriginal(ChassisOmniMech aChassis) {
        List<OmniPod> ans = new ArrayList<>();
        for (Location location : Location.values()) {
            ans.add(OmniPodDB.lookupOriginal(aChassis, location));
        }
        return ans;
    }

    /**
     * @param aChassis
     *            The chassis to get the pod for.
     * @param aLocation
     *            The location to get the pod for.
     * @return The {@link OmniPod}s that is "original" to the given chassis and {@link Location}.
     */
    public static OmniPod lookupOriginal(ChassisOmniMech aChassis, Location aLocation) {
        for (OmniPod omniPod : lookup(aChassis.getSeriesName(), aLocation)) {
            if (omniPod.isOriginalForChassis(aChassis)) {
                return omniPod;
            }
        }
        throw new IllegalArgumentException("There exists no original omnipod for " + aChassis + " at " + aLocation);
    }

    /**
     * @param aSeries
     *            A chassis series to get all compatible pods for.
     * @param aLocation
     *            A location on the chassis to get all compatible pods for.
     * @return A {@link Collection} of {@link OmniPod}s that are compatible with the given chassis and {@link Location}.
     */
    public static Collection<OmniPod> lookup(String aSeries, Location aLocation) {
        List<OmniPod> ans = new ArrayList<>();
        for (OmniPod omniPod : series2pod.get(canonize(aSeries))) {
            if (omniPod.getLocation() == aLocation)
                ans.add(omniPod);
        }
        return ans;
    }

    /**
     * @param aChassisSeries
     *            A chassis series to get all compatible pods for.
     * @param aLocation
     *            A location on the chassis to get all compatible pods for.
     * @return A {@link Collection} of {@link OmniPod}s that are compatible with the given chassis and {@link Location}.
     */
    public static Collection<OmniPod> lookup(ChassisOmniMech aChassisSeries, Location aLocation) {
        return lookup(aChassisSeries.getSeriesName(), aLocation);
    }

    private static String canonize(String aKey) {
        return aKey.toUpperCase();
    }

    /**
     * A decision has been made to rely on static initializers for *DB classes. The motivation is that all items are
     * immutable, and this is the only way that allows providing global item constants such as ItemDB.AMS.
     */
    static {
        DataCache dataCache;
        try {
            dataCache = DataCache.getInstance();
        }
        catch (IOException e) {
            throw new RuntimeException(e); // Promote to unchecked. This is a critical failure.
        }

        series2pod = new HashMap<String, List<OmniPod>>();
        id2pod = new TreeMap<>();

        for (OmniPod omniPod : dataCache.getOmniPods()) {

            String series = omniPod.getChassisSeries();

            List<OmniPod> list = series2pod.get(canonize(series));
            if (list == null) {
                list = new ArrayList<>();
                series2pod.put(canonize(series), list);
            }
            list.add(omniPod);

            id2pod.put(omniPod.getMwoId(), omniPod);
        }
    }

    /**
     * @param aId
     *            The id of the pod to look up.
     * @return An {@link OmniPod} with the correct ID.
     */
    public static OmniPod lookup(int aId) {
        OmniPod omnipod = id2pod.get(aId);
        if (omnipod == null)
            throw new IllegalArgumentException("No omnipod with ID: " + aId);
        return omnipod;
    }

    /**
     * @return A {@link Collection} of all {@link OmniPod}s.
     */
    public static Collection<OmniPod> all() {
        return id2pod.values();
    }
}
