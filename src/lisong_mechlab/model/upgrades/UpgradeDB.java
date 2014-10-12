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
package lisong_mechlab.model.upgrades;

import java.io.IOException;
import java.util.Map;
import java.util.TreeMap;

import lisong_mechlab.model.DataCache;

/**
 * A database class that holds all the {@link Upgrade}s parsed from the game files.
 * 
 * @author Emily Björk
 */
public class UpgradeDB {
    public static final ArmorUpgrade           STANDARD_ARMOR;
    public static final StructureUpgrade       STANDARD_STRUCTURE;
    public static final GuidanceUpgrade        STANDARD_GUIDANCE;
    public static final HeatSinkUpgrade        STANDARD_HEATSINKS;
    public static final GuidanceUpgrade        ARTEMIS_IV;
    public static final HeatSinkUpgrade        DOUBLE_HEATSINKS;
    public static final ArmorUpgrade           FERRO_FIBROUS_ARMOR;
    public static final StructureUpgrade       ENDO_STEEL_STRUCTURE;
    public static final HeatSinkUpgrade        CLAN_DOUBLE_HEATSINKS;
    public static final ArmorUpgrade           CLAN_FERRO_FIBROUS_ARMOR;
    public static final StructureUpgrade       CLAN_ENDO_STEEL_STRUCTURE;
    public static final HeatSinkUpgrade        CLAN_STANDARD_HEATSINKS;
    public static final ArmorUpgrade           CLAN_STANDARD_ARMOR;
    public static final StructureUpgrade       CLAN_STANDARD_STRUCTURE;
    private static final Map<Integer, Upgrade> id2upgrade;

    /**
     * Looks up an {@link Upgrade} by its MW:O ID.
     * 
     * @param aMwoId
     *            The ID to look up.
     * @return The {@link Upgrade} for the sought for ID.
     * @throws IllegalArgumentException
     *             Thrown if the ID is not a valid upgrade ID.
     */
    public static Upgrade lookup(int aMwoId) throws IllegalArgumentException {
        Upgrade ans = id2upgrade.get(aMwoId);
        if (null == ans) {
            throw new IllegalArgumentException("The ID: " + aMwoId + " is not a valid MWO upgrade ID!");
        }
        return ans;
    }

    /**
     * A decision has been made to rely on static initializers for *DB classes. The motivation is that all items are
     * immutable, and this is the only way that allows providing global item constans such as ItemDB.AMS.
     */
    static {
        DataCache dataCache;
        try {
            dataCache = DataCache.getInstance();
        }
        catch (IOException e) {
            throw new RuntimeException(e); // Promote to unchecked. This is a critical failure.
        }

        id2upgrade = new TreeMap<Integer, Upgrade>();
        for (Upgrade upgrade : dataCache.getUpgrades()) {
            id2upgrade.put(upgrade.getMwoId(), upgrade);
        }

        STANDARD_ARMOR = (ArmorUpgrade) lookup(2810);
        FERRO_FIBROUS_ARMOR = (ArmorUpgrade) lookup(2811);
        CLAN_FERRO_FIBROUS_ARMOR = (ArmorUpgrade) lookup(2815);
        CLAN_STANDARD_ARMOR = (ArmorUpgrade) lookup(2816);

        STANDARD_STRUCTURE = (StructureUpgrade) lookup(3100);
        ENDO_STEEL_STRUCTURE = (StructureUpgrade) lookup(3101);
        CLAN_ENDO_STEEL_STRUCTURE = (StructureUpgrade) lookup(3102);
        CLAN_STANDARD_STRUCTURE = (StructureUpgrade) lookup(3103);

        STANDARD_HEATSINKS = (HeatSinkUpgrade) lookup(3003);
        DOUBLE_HEATSINKS = (HeatSinkUpgrade) lookup(3002);
        CLAN_DOUBLE_HEATSINKS = (HeatSinkUpgrade) lookup(3005);
        CLAN_STANDARD_HEATSINKS = (HeatSinkUpgrade) lookup(3006);

        STANDARD_GUIDANCE = (GuidanceUpgrade) lookup(3051);
        ARTEMIS_IV = (GuidanceUpgrade) lookup(3050);
    }
}
