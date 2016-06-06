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
package org.lisoft.lsml.model.datacache;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import org.lisoft.lsml.model.chassi.Chassis;
import org.lisoft.lsml.model.chassi.ChassisClass;
import org.lisoft.lsml.model.chassi.ChassisStandard;

/**
 * This class implements a database with all the chassis in the game.
 *
 * @author Emily Björk
 */
public class ChassisDB {
    static private final Map<String, Chassis> name2chassis;
    static private final Map<String, List<Chassis>> series2chassis;
    static private final Map<Integer, Chassis> id2chassis;
    static private final Map<Integer, List<Chassis>> chassis2variant;

    /**
     * A decision has been made to rely on static initializers for *DB classes. The motivation is that all items are
     * immutable, and this is the only way that allows providing global item constants such as ItemDB.AMS.
     */
    static {
        DataCache dataCache;
        try {
            dataCache = DataCache.getInstance();
        }
        catch (final IOException e) {
            throw new RuntimeException(e); // Promote to unchecked. This is a critical failure.
        }

        name2chassis = new HashMap<>();
        series2chassis = new HashMap<>();
        id2chassis = new TreeMap<>();
        chassis2variant = new HashMap<>();

        for (final Chassis chassis : dataCache.getChassis()) {
            final String model = canonize(chassis.getName());
            final String modelShort = canonize(chassis.getNameShort());

            addToVariationDb(chassis.getBaseVariantId(), chassis);
            name2chassis.put(modelShort, chassis);
            name2chassis.put(model, chassis);
            id2chassis.put(chassis.getMwoId(), chassis);

            if (!series2chassis.containsKey(chassis.getSeriesName())) {
                final List<Chassis> chassilist = new ArrayList<>();
                series2chassis.put(chassis.getSeriesName(), chassilist);
            }
            series2chassis.get(chassis.getSeriesName()).add(chassis);
        }
    }

    /**
     * Looks up all chassis of the given chassis class.
     *
     * @param aChassiClass
     * @return An {@link List} of all {@link ChassisStandard} with the given {@link ChassisClass}.
     */
    public static Collection<Chassis> lookup(ChassisClass aChassiClass) {
        final List<Chassis> chassii = new ArrayList<>(4 * 4);
        for (final Chassis chassis : name2chassis.values()) {
            if (chassis.getChassiClass() == aChassiClass && !chassii.contains(chassis)) {
                chassii.add(chassis);
            }
        }
        return chassii;
    }

    /**
     * @param aChassiId
     *            The ID of the chassis to look for.
     * @return A {@link Chassis} matching the argument.
     */
    public static Chassis lookup(int aChassiId) {
        return id2chassis.get(aChassiId);
    }

    /**
     * Looks up a chassis by a name such as "AS7-D-DC" or "DAISHI PRIME"
     *
     * @param aChassisName
     *            The name to use as lookup key.
     * @return The chassis that matches the lookup string.
     */
    public static Chassis lookup(String aChassisName) {
        final String keyShortName = canonize(aChassisName);
        if (!name2chassis.containsKey(keyShortName)) {
            throw new IllegalArgumentException("No chassi variation named: " + aChassisName + " !");
        }
        return name2chassis.get(keyShortName);
    }

    public static Collection<Chassis> lookupAll() {
        return id2chassis.values();
    }

    /**
     * Looks up all chassis that are part of a series. For example all Cataphracts.
     *
     * @param aSeries
     *            The name of the series to find.
     * @return A {@link List} of all chassis that are part of that series.
     */
    public static Collection<Chassis> lookupSeries(String aSeries) {
        final String keyShortName = canonize(aSeries);
        if (!series2chassis.containsKey(keyShortName)) {
            throw new IllegalArgumentException("No chassi variation by that name!");
        }
        return series2chassis.get(keyShortName);
    }

    /**
     * @param aChassis
     *            A {@link ChassisStandard} to get variations for.
     * @return A {@link List} of all variants of this chassis (normal, champion, phoenix etc)
     */
    public static Collection<Chassis> lookupVariations(Chassis aChassis) {
        return chassis2variant.get(aChassis.getMwoId());
    }

    private static void addToVariationDb(int aBaseID, Chassis aChassis) {
        int baseId = aBaseID;
        if (baseId < 0) {
            baseId = aChassis.getMwoId();
        }

        List<Chassis> list = chassis2variant.get(baseId);
        if (null == list) {
            list = new ArrayList<>();
            chassis2variant.put(baseId, list);
        }
        if (baseId != aChassis.getMwoId()) {
            chassis2variant.put(aChassis.getMwoId(), list);
        }

        list.add(aChassis);
    }

    static private String canonize(String aName) {
        return aName.toLowerCase().trim();
    }
}
