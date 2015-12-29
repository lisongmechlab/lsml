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
import java.util.Map;
import java.util.TreeMap;

import org.lisoft.lsml.model.item.Faction;
import org.lisoft.lsml.model.upgrades.ArmorUpgrade;
import org.lisoft.lsml.model.upgrades.GuidanceUpgrade;
import org.lisoft.lsml.model.upgrades.HeatSinkUpgrade;
import org.lisoft.lsml.model.upgrades.StructureUpgrade;
import org.lisoft.lsml.model.upgrades.Upgrade;

/**
 * A database class that holds all the {@link Upgrade}s parsed from the game files.
 * 
 * @author Emily Björk
 */
public class UpgradeDB {
    public static final GuidanceUpgrade        ARTEMIS_IV;
    public static final HeatSinkUpgrade        CLAN_DHS;
    public static final StructureUpgrade       CLAN_ES_STRUCTURE;
    public static final ArmorUpgrade           CLAN_FF_ARMOR;
    public static final HeatSinkUpgrade        CLAN_SHS;
    public static final ArmorUpgrade           CLAN_STD_ARMOR;
    public static final StructureUpgrade       CLAN_STD_STRUCTURE;
    public static final HeatSinkUpgrade        IS_DHS;
    public static final StructureUpgrade       IS_ES_STRUCTURE;
    public static final ArmorUpgrade           IS_FF_ARMOR;
    public static final HeatSinkUpgrade        IS_SHS;
    public static final ArmorUpgrade           IS_STD_ARMOR;
    public static final StructureUpgrade       IS_STD_STRUCTURE;
    public static final GuidanceUpgrade        STD_GUIDANCE;
    private static final Map<Integer, Upgrade> id2upgrade;

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

        id2upgrade = new TreeMap<Integer, Upgrade>();
        for (Upgrade upgrade : dataCache.getUpgrades()) {
            id2upgrade.put(upgrade.getMwoId(), upgrade);
        }

        IS_STD_ARMOR = (ArmorUpgrade) lookup(2810);
        IS_FF_ARMOR = (ArmorUpgrade) lookup(2811);
        CLAN_FF_ARMOR = (ArmorUpgrade) lookup(2815);
        CLAN_STD_ARMOR = (ArmorUpgrade) lookup(2816);

        IS_STD_STRUCTURE = (StructureUpgrade) lookup(3100);
        IS_ES_STRUCTURE = (StructureUpgrade) lookup(3101);
        CLAN_ES_STRUCTURE = (StructureUpgrade) lookup(3102);
        CLAN_STD_STRUCTURE = (StructureUpgrade) lookup(3103);

        IS_SHS = (HeatSinkUpgrade) lookup(3003);
        IS_DHS = (HeatSinkUpgrade) lookup(3002);
        CLAN_DHS = (HeatSinkUpgrade) lookup(3005);
        CLAN_SHS = (HeatSinkUpgrade) lookup(3006);

        STD_GUIDANCE = (GuidanceUpgrade) lookup(3051);
        ARTEMIS_IV = (GuidanceUpgrade) lookup(3050);
    }

    public static ArmorUpgrade getArmor(Faction aFaction, boolean aUpgraded) {
        if (Faction.CLAN == aFaction) {
            return aUpgraded ? CLAN_FF_ARMOR : CLAN_STD_ARMOR;
        }
        return aUpgraded ? IS_FF_ARMOR : IS_STD_ARMOR;
    }

    public static HeatSinkUpgrade getHeatSinks(Faction aFaction, boolean aUpgraded) {
        if (Faction.CLAN == aFaction) {
            return aUpgraded ? CLAN_DHS : CLAN_SHS;
        }
        return aUpgraded ? IS_DHS : IS_SHS;
    }

    public static StructureUpgrade getStructure(Faction aFaction, boolean aUpgraded) {
        if (Faction.CLAN == aFaction) {
            return aUpgraded ? CLAN_ES_STRUCTURE : CLAN_STD_STRUCTURE;
        }
        return aUpgraded ? IS_ES_STRUCTURE : IS_STD_STRUCTURE;
    }

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
}
