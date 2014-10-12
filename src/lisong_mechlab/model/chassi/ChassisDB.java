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
 * This class implements a database with all the chassis in the game.
 * 
 * @author Emily Björk
 */
public class ChassisDB {
    static private final Map<String, ChassisBase>        name2chassis;
    static private final Map<String, List<ChassisBase>>  series2chassis;
    static private final Map<Integer, ChassisBase>       id2chassis;
    static private final Map<Integer, List<ChassisBase>> chassis2variant;

    /**
     * Looks up a chassis by a name such as "AS7-D-DC" or "DAISHI PRIME"
     * 
     * @param aChassisName
     *            The name to use as lookup key.
     * @return The chassis that matches the lookup string.
     */
    public static ChassisBase lookup(String aChassisName) {
        String keyShortName = canonize(aChassisName);
        if (!name2chassis.containsKey(keyShortName)) {
            throw new IllegalArgumentException("No chassi variation named: " + aChassisName + " !");
        }
        return name2chassis.get(keyShortName);
    }

    /**
     * @param aChassiId
     *            The ID of the chassis to look for.
     * @return A {@link ChassisBase} matching the argument.
     */
    public static ChassisBase lookup(int aChassiId) {
        return id2chassis.get(aChassiId);
    }

    /**
     * @param aChassis
     *            A {@link ChassisStandard} to get variations for.
     * @return A {@link List} of all variants of this chassis (normal, champion, phoenix etc)
     */
    public static Collection<? extends ChassisBase> lookupVariations(ChassisBase aChassis) {
        return chassis2variant.get(aChassis.getMwoId());
    }

    /**
     * Looks up all chassis of the given chassis class.
     * 
     * @param aChassiClass
     * @return An {@link List} of all {@link ChassisStandard} with the given {@link ChassisClass}.
     */
    public static Collection<? extends ChassisBase> lookup(ChassisClass aChassiClass) {
        List<ChassisBase> chassii = new ArrayList<>(4 * 4);
        for (ChassisBase chassis : name2chassis.values()) {
            if (chassis.getChassiClass() == aChassiClass && !chassii.contains(chassis)) {
                chassii.add(chassis);
            }
        }
        return chassii;
    }

    /**
     * Looks up all chassis that are part of a series. For example all Cataphracts.
     * 
     * @param aSeries
     *            The name of the series to find.
     * @return A {@link List} of all chassis that are part of that series.
     */
    public static Collection<? extends ChassisBase> lookupSeries(String aSeries) {
        String keyShortName = canonize(aSeries);
        if (!series2chassis.containsKey(keyShortName)) {
            throw new IllegalArgumentException("No chassi variation by that name!");
        }
        return series2chassis.get(keyShortName);
    }

    private static void addToVariationDb(int aBaseID, ChassisBase aChassis) {
        int baseId = aBaseID;
        if (baseId < 0) {
            baseId = aChassis.getMwoId();
        }

        List<ChassisBase> list = chassis2variant.get(baseId);
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

        name2chassis = new HashMap<>();
        series2chassis = new HashMap<>();
        id2chassis = new TreeMap<>();
        chassis2variant = new HashMap<>();

        for (ChassisBase chassis : dataCache.getChassis()) {
            final String model = canonize(chassis.getName());
            final String modelShort = canonize(chassis.getNameShort());

            addToVariationDb(chassis.getBaseVariantId(), chassis);
            name2chassis.put(modelShort, chassis);
            name2chassis.put(model, chassis);
            id2chassis.put(chassis.getMwoId(), chassis);

            if (!series2chassis.containsKey(chassis.getSeriesName())) {
                List<ChassisBase> chassilist = new ArrayList<>();
                series2chassis.put(chassis.getSeriesName(), chassilist);
            }
            series2chassis.get(chassis.getSeriesName()).add(chassis);
        }
    }
}
