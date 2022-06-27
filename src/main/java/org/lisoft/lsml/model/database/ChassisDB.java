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
package org.lisoft.lsml.model.database;

import org.lisoft.lsml.model.NoSuchItemException;
import org.lisoft.lsml.model.chassi.Chassis;
import org.lisoft.lsml.model.chassi.ChassisClass;
import org.lisoft.lsml.model.chassi.ChassisStandard;
import org.lisoft.lsml.view_fx.LiSongMechLab;

import java.util.*;

/**
 * This class implements a database with all the chassis in the game.
 *
 * @author Li Song
 */
public class ChassisDB {
    static private final Map<Integer, List<Chassis>> chassis2variant;
    static private final Map<Integer, Chassis> id2chassis;
    static private final Map<String, Chassis> name2chassis;
    static private final Map<String, List<Chassis>> series2chassis;

    /**
     * A decision has been made to rely on static initializers for *DB classes. The motivation is that all items are
     * immutable, and this is the only way that allows providing global item constants such as ItemDB.AMS.
     */
    static {
        final Database database = LiSongMechLab.getDatabase()
                                               .orElseThrow(() -> new RuntimeException("Cannot run without database"));
        name2chassis = new HashMap<>();
        series2chassis = new HashMap<>();
        id2chassis = new HashMap<>();
        chassis2variant = new HashMap<>();

        for (final Chassis chassis : database.getChassis()) {
            final String model = canonize(chassis.getName());
            final String modelShort = canonize(chassis.getShortName());

            addToVariationDb(chassis.getBaseVariantId(), chassis);
            name2chassis.put(modelShort, chassis);
            name2chassis.put(model, chassis);
            id2chassis.put(chassis.getId(), chassis);

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
     * @param aChassiClass The {@link ChassisClass} to look up.
     * @return An {@link List} of all {@link ChassisStandard} with the given {@link ChassisClass}.
     */
    public static Collection<Chassis> lookup(ChassisClass aChassiClass) {
        final List<Chassis> chassii = new ArrayList<>(4 * 4);
        for (final Chassis chassis : name2chassis.values()) {
            if (chassis.getChassisClass() == aChassiClass && !chassii.contains(chassis)) {
                chassii.add(chassis);
            }
        }
        return chassii;
    }

    /**
     * @param aChassiId The ID of the chassis to look for.
     * @return A {@link Chassis} matching the argument.
     * @throws NoSuchItemException If no chassis exists by that ID.
     */
    public static Chassis lookup(int aChassiId) throws NoSuchItemException {
        final Chassis c = id2chassis.get(aChassiId);
        if (null == c) {
            throw new NoSuchItemException("No chassis by ID: " + aChassiId);
        }
        return c;
    }

    /**
     * Looks up a chassis by a name such as "AS7-D-DC" or "DAISHI PRIME"
     *
     * @param aChassisName The name to use as lookup key.
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
     * @param aSeries The name of the series to find.
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
     * @param aChassis A {@link ChassisStandard} to get variations for.
     * @return A {@link List} of all variants of this chassis (normal, champion, phoenix etc)
     */
    public static Collection<Chassis> lookupVariations(Chassis aChassis) {
        return chassis2variant.get(aChassis.getId());
    }

    private static void addToVariationDb(int aBaseID, Chassis aChassis) {
        int baseId = aBaseID;
        if (baseId < 0) {
            baseId = aChassis.getId();
        }

        List<Chassis> list = chassis2variant.get(baseId);
        if (null == list) {
            list = new ArrayList<>();
            chassis2variant.put(baseId, list);
        }
        if (baseId != aChassis.getId()) {
            chassis2variant.put(aChassis.getId(), list);
        }

        list.add(aChassis);
    }

    static private String canonize(String aName) {
        return aName.toLowerCase().trim();
    }
}
