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
package lisong_mechlab.model.modifiers;

import java.io.IOException;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import lisong_mechlab.model.DataCache;
import lisong_mechlab.model.modifiers.ModifierDescription.Operation;
import lisong_mechlab.model.modifiers.ModifierDescription.ValueType;

/**
 * A database of all the quirks in the game.
 * 
 * @author Li Song
 *
 */
public class ModifiersDB {
    public final static String                            SEL_MOVEMENT_MAX_SPEED    = "speed";
    public final static String                            SEL_MOVEMENT_REVERSE_MUL  = "reversespeed";
    public final static String                            SEL_MOVEMENT_TORSO_SPEED  = "torsospeed";
    public final static String                            SEL_MOVEMENT_ARM_SPEED    = "armspeed";
    public final static String                            SEL_MOVEMENT_TORSO_ANGLE  = "torsoangle";
    public final static String                            SEL_MOVEMENT_ARM_ANGLE    = "armrotate";
    public final static String                            SEL_MOVEMENT_TURN_SPEED   = "turnlerp_speed";
    public final static String                            SEL_MOVEMENT_TURN_RATE    = "turnlerp";

    public final static String                            SEL_HEAT_DISSIPATION      = "heatloss";
    public final static String                            SEL_HEAT_LIMIT            = "heatlimit";
    public final static String                            SEL_HEAT_EXTERNALTRANSFER = "externalheat";

    public final static String                            SEL_WEAPON_RANGE          = "range";
    public final static String                            SEL_WEAPON_COOLDOWN       = "cooldown";
    public final static String                            SEL_WEAPON_HEAT           = "heat";
    public final static String                            SEL_WEAPON_LARGE_BORE     = "largeweapon";

    private final static List<String>                     ALL_WEAPONS;
    public final static ModifierDescription               HEAT_CONTAINMENT_DESC;
    public final static ModifierDescription               COOL_RUN_DESC;
    public final static ModifierDescription               FAST_FIRE_DESC;
    public final static ModifierDescription               SPEED_TWEAK_DESC;
    public final static ModifierDescription               ANCHOR_TURN_LOW_DESC;
    public final static ModifierDescription               ANCHOR_TURN_MID_DESC;
    public final static ModifierDescription               ANCHOR_TURN_HIGH_DESC;

    private final static Map<String, ModifierDescription> mwoname2modifier;

    /**
     * Looks up a {@link ModifierDescription} by a MWO key.
     * 
     * @param aKey
     *            The lookup key.
     * @return A {@link ModifierDescription}.
     */
    public static ModifierDescription lookup(String aKey) {
        ModifierDescription description = mwoname2modifier.get(aKey);
        if (description == null) {
            throw new IllegalArgumentException("Unknown key!");
        }
        return description;
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

        mwoname2modifier = new HashMap<>();
        Collection<ModifierDescription> modifiers = dataCache.getModifierDescriptions();
        for (ModifierDescription description : modifiers) {
            mwoname2modifier.put(description.getKey(), description);
        }

        // FIXME: Remove constants
        ALL_WEAPONS = Collections
                .unmodifiableList(Arrays.asList("energy", "ballistic", "missile", "antimissilesystem"));
        HEAT_CONTAINMENT_DESC = new ModifierDescription("HEAT CONTAINMENT", null, Operation.ADDITIVE, SEL_HEAT_LIMIT,
                null, ValueType.POSITIVE_GOOD);
        COOL_RUN_DESC = new ModifierDescription("COOL RUN", null, Operation.MULTIPLICATIVE, "heatloss", null,
                ValueType.POSITIVE_GOOD);
        FAST_FIRE_DESC = new ModifierDescription("FAST FIRE", null, Operation.MULTIPLICATIVE, ALL_WEAPONS,
                SEL_WEAPON_COOLDOWN, ValueType.NEGATIVE_GOOD);
        SPEED_TWEAK_DESC = new ModifierDescription("SPEED TWEAK", null, Operation.MULTIPLICATIVE, Arrays.asList(
                "speed", "reversespeed"), null, ValueType.POSITIVE_GOOD);
        ANCHOR_TURN_LOW_DESC = new ModifierDescription("ANCHOR TURN (LOW SPEED)", null, Operation.MULTIPLICATIVE,
                "accellerp", "lowrate", ValueType.POSITIVE_GOOD);
        ANCHOR_TURN_MID_DESC = new ModifierDescription("ANCHOR TURN (MID SPEED)", null, Operation.MULTIPLICATIVE,
                "accellerp", "midrate", ValueType.POSITIVE_GOOD);
        ANCHOR_TURN_HIGH_DESC = new ModifierDescription("ANCHOR TURN (HIGH SPEED)", null, Operation.MULTIPLICATIVE,
                "accellerp", "highrate", ValueType.POSITIVE_GOOD);

    }
}
