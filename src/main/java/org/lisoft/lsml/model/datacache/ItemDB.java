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

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.lisoft.lsml.model.chassi.HardPointType;
import org.lisoft.lsml.model.item.AmmoWeapon;
import org.lisoft.lsml.model.item.Engine;
import org.lisoft.lsml.model.item.EngineType;
import org.lisoft.lsml.model.item.Faction;
import org.lisoft.lsml.model.item.HeatSink;
import org.lisoft.lsml.model.item.Internal;
import org.lisoft.lsml.model.item.Item;

/**
 * This class is a database of all {@link Item}s. One can lookup by MWO id, textual name and MWO string name of the
 * item.
 * 
 * @author Li Song
 */
public class ItemDB {
    // Special global "item constants" useful when checking special cases.
    // Feel free to populate if you find yourself consistently using
    // ItemDB.lookup() with constant strings.

    static public final int                 ENGINE_INTERNAL_ID      = 60000;
    static public final int                 ENGINE_INTERNAL_CLAN_ID = 60001;

    static public final AmmoWeapon          AMS;
    static public final AmmoWeapon          C_AMS;
    static public final HeatSink            SHS;
    static public final HeatSink            DHS;
    static public final Item                ECM;
    static public final Item                BAP;
    static public final Item                CASE;

    static public final Internal            UAA;
    static public final Internal            LAA;
    static public final Internal            HA;

    static public final Internal            DYN_ARMOR;
    static public final Internal            DYN_STRUCT;
    static public final Internal            FIX_ARMOR;
    static public final Internal            FIX_STRUCT;

    static private final Map<String, Item>  locname2item;
    static private final Map<String, Item>  mwoname2item;
    static private final Map<Integer, Item> mwoidx2item;

    public static Item lookup(final String aItemName) {
        String key = canonize(aItemName);
        if (!locname2item.containsKey(key)) {
            if (!mwoname2item.containsKey(key)) {
                throw new IllegalArgumentException("There exists no item by name:" + aItemName);
            }
            return mwoname2item.get(key);
        }
        return locname2item.get(key);
    }

    @SuppressWarnings("unchecked")
    // It is checked...
    public static <T extends Item> List<T> lookup(Class<T> aClass) {
        List<T> ans = new ArrayList<T>();
        for (Item it : locname2item.values()) {
            if (aClass.isInstance(it)) {
                ans.add((T) it);
            }
        }
        return ans;
    }

    public static Item lookup(int aMwoIndex) {
        if (!mwoidx2item.containsKey(aMwoIndex)) {
            throw new IllegalArgumentException("No item with that index: " + aMwoIndex);
        }
        return mwoidx2item.get(aMwoIndex);
    }

    public static Engine getEngine(int aRating, EngineType aType, Faction aFaction) {
        StringBuilder sb = new StringBuilder();
        if (aType == EngineType.XL && aFaction == Faction.CLAN) {
            sb.append("CLAN ");
        }
        sb.append(aType.name()).append(" ENGINE ").append(aRating);
        return (Engine) lookup(sb.toString());
    }

    private static void put(Item aItem) {
        assert aItem != null;
        assert (!locname2item.containsKey(aItem));

        mwoname2item.put(canonize(aItem.getKey()), aItem);
        locname2item.put(canonize(aItem.getName()), aItem);
        if (aItem.getMwoId() >= 0)
            mwoidx2item.put(aItem.getMwoId(), aItem);
    }

    private static String canonize(String aString) {
        String key = aString.toLowerCase();
        return key;
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

        mwoname2item = new HashMap<String, Item>();
        locname2item = new HashMap<String, Item>();
        mwoidx2item = new HashMap<Integer, Item>();

        for (Item item : dataCache.getItems()) {
            put(item);
        }

        // Initialize special items
        C_AMS = (AmmoWeapon) lookup("C-AMS");
        AMS = (AmmoWeapon) lookup("AMS");
        SHS = (HeatSink) lookup("STD HEAT SINK");
        DHS = (HeatSink) lookup("DOUBLE HEAT SINK");
        ECM = lookup("GUARDIAN ECM");
        BAP = lookup("BEAGLE ACTIVE PROBE");
        CASE = lookup("C.A.S.E.");

        UAA = (Internal) lookup("UpperArmActuator");
        LAA = (Internal) lookup("LowerArmActuator");
        HA = (Internal) lookup("HandActuator");

        DYN_ARMOR = new Internal("DYNAMIC ARMOR", null, null, 0, 1, 0, HardPointType.NONE, 0, Faction.ANY);
        DYN_STRUCT = new Internal("DYNAMIC STRUCTURE", null, null, 0, 1, 0, HardPointType.NONE, 0, Faction.ANY);
        FIX_ARMOR = new Internal("FIXED ARMOR", null, null, 0, 1, 0, HardPointType.NONE, 0, Faction.ANY);
        FIX_STRUCT = new Internal("FIXED STRUCTURE", null, null, 0, 1, 0, HardPointType.NONE, 0, Faction.ANY);
    }
}
