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
package org.lisoft.lsml.model.modifiers;

import java.io.IOException;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.lisoft.lsml.model.DataCache;
import org.lisoft.lsml.model.item.ItemDB;
import org.lisoft.lsml.model.item.Weapon;
import org.lisoft.lsml.model.modifiers.ModifierDescription.ModifierType;
import org.lisoft.lsml.model.modifiers.ModifierDescription.Operation;

/**
 * A database of all the quirks in the game.
 * 
 * @author Li Song
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
    public final static String                            SEL_WEAPON_JAMMING_CHANCE = "jamchance";
    public final static String                            SEL_WEAPON_JAMMED_TIME    = "jamtime";

    public final static List<String>                      ALL_WEAPONS;
    public final static ModifierDescription               HEAT_CONTAINMENT_DESC;
    public final static ModifierDescription               COOL_RUN_DESC;
    public final static ModifierDescription               FAST_FIRE_DESC;
    public final static ModifierDescription               SPEED_TWEAK_DESC;
    public final static ModifierDescription               ANCHOR_TURN_LOW_DESC;
    public final static ModifierDescription               ANCHOR_TURN_MID_DESC;
    public final static ModifierDescription               ANCHOR_TURN_HIGH_DESC;
    public final static ModifierDescription               TWIST_X_PITCH_DESC;
    public final static ModifierDescription               TWIST_X_YAW_DESC;
    public final static ModifierDescription               ARM_REFLEX_PITCH_DESC;
    public final static ModifierDescription               ARM_REFLEX_YAW_DESC;
    public final static ModifierDescription               TWIST_SPEED_PITCH_DESC;
    public final static ModifierDescription               TWIST_SPEED_YAW_DESC;

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

    public static Collection<String> getAllWeaponSelectors() {
        Set<String> ans = new HashSet<>();
        for (Weapon w : ItemDB.lookup(Weapon.class)) {
            ans.addAll(w.getAliases());
        }
        return ans;
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

        ALL_WEAPONS = Collections
                .unmodifiableList(Arrays.asList("energy", "ballistic", "missile", "antimissilesystem"));

        // Descriptions for Basic Pilot Efficiencies
        HEAT_CONTAINMENT_DESC = new ModifierDescription("HEAT CONTAINMENT", null, Operation.MUL, SEL_HEAT_LIMIT, null,
                ModifierType.POSITIVE_GOOD);
        COOL_RUN_DESC = new ModifierDescription("COOL RUN", null, Operation.MUL, "heatloss", null,
                ModifierType.POSITIVE_GOOD);
        ANCHOR_TURN_LOW_DESC = new ModifierDescription("ANCHOR TURN (LOW SPEED)", null, Operation.MUL,
                SEL_MOVEMENT_TURN_RATE, "lowrate", ModifierType.POSITIVE_GOOD);
        ANCHOR_TURN_MID_DESC = new ModifierDescription("ANCHOR TURN (MID SPEED)", null, Operation.MUL,
                SEL_MOVEMENT_TURN_RATE, "midrate", ModifierType.POSITIVE_GOOD);
        ANCHOR_TURN_HIGH_DESC = new ModifierDescription("ANCHOR TURN (HIGH SPEED)", null, Operation.MUL,
                SEL_MOVEMENT_TURN_RATE, "highrate", ModifierType.POSITIVE_GOOD);

        TWIST_X_PITCH_DESC = new ModifierDescription("TORSO TURN ANGLE", null, Operation.MUL, SEL_MOVEMENT_TORSO_ANGLE,
                "pitch", ModifierType.POSITIVE_GOOD);
        TWIST_X_YAW_DESC = new ModifierDescription("TORSO TURN ANGLE", null, Operation.MUL, SEL_MOVEMENT_TORSO_ANGLE,
                "yaw", ModifierType.POSITIVE_GOOD);

        ARM_REFLEX_PITCH_DESC = new ModifierDescription("ARM MOVEMENT RATE", null, Operation.MUL,
                SEL_MOVEMENT_ARM_SPEED, "pitch", ModifierType.POSITIVE_GOOD);
        ARM_REFLEX_YAW_DESC = new ModifierDescription("ARM MOVEMENT RATE", null, Operation.MUL, SEL_MOVEMENT_ARM_SPEED,
                "yaw", ModifierType.POSITIVE_GOOD);

        TWIST_SPEED_PITCH_DESC = new ModifierDescription("TORSO TURN RATE", null, Operation.MUL,
                SEL_MOVEMENT_TORSO_SPEED, "pitch", ModifierType.POSITIVE_GOOD);
        TWIST_SPEED_YAW_DESC = new ModifierDescription("TORSO TURN RATE", null, Operation.MUL, SEL_MOVEMENT_TORSO_SPEED,
                "yaw", ModifierType.POSITIVE_GOOD);

        // Descriptions for Elite Pilot Efficiencies
        FAST_FIRE_DESC = new ModifierDescription("FAST FIRE", null, Operation.MUL, ALL_WEAPONS, SEL_WEAPON_COOLDOWN,
                ModifierType.NEGATIVE_GOOD);
        SPEED_TWEAK_DESC = new ModifierDescription("SPEED TWEAK", null, Operation.MUL,
                Arrays.asList(SEL_MOVEMENT_MAX_SPEED, SEL_MOVEMENT_REVERSE_MUL), null, ModifierType.POSITIVE_GOOD);

    }
}
