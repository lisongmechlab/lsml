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

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

import org.lisoft.lsml.model.chassi.ArmourSide;
import org.lisoft.lsml.model.chassi.Location;
import org.lisoft.lsml.model.item.Weapon;

import com.thoughtworks.xstream.annotations.XStreamAsAttribute;

/**
 * This class models a description of how a generic modifier can be applied to an {@link Attribute}. A {@link Modifier}
 * is a {@link ModifierDescription} together with actual modifier value that modifiers an {@link Attribute}.
 *
 * One {@link ModifierDescription}s can affect many different attributes. To facilitate this,
 * {@link ModifierDescription}s and {@link Attribute}s have a set of "selector tags". Selector tags can be things such
 * as "Top Speed", "Laser weapons", "IS Large Laser", "Clan ACs". In addition to selector tags, each {@link Attribute}
 * and {@link ModifierDescription} can have a specific named specifier within the selector tag that is affected. For
 * example the selector tag may be "IS Laser Weapons" and the specifier can be "BurnTime". However when the selector
 * uniquely identifies exactly one attribute, like in the case of "Top Speed" then the specifier is <code>null</code>.
 *
 * All of this conspires to create a powerful system where just about any value can be affected by modifiers coming from
 * different sources, such as pilot efficiencies, 'Mech quirks, equipped items and modules etc.
 *
 * @author Li Song
 */
public class ModifierDescription {
    public final static List<String> SEL_ALL = uc("all");
    public final static List<String> SEL_ALL_WEAPONS = uc("energy", "ballistic", "missile", "antimissilesystem");
    public final static List<String> SEL_ARMOUR = uc("armorresist");
    public final static List<String> SEL_HEAT_DISSIPATION = uc("heatloss", "heatdissipation");
    public final static List<String> SEL_HEAT_EXTERNALTRANSFER = uc("externalheat");
    public final static List<String> SEL_HEAT_LIMIT = uc("heatlimit");
    public final static List<String> SEL_HEAT_MOVEMENT = uc("movementheat");
    public final static List<String> SEL_HEAT_DAMAGE = uc("overheatdamage");
    public final static List<String> SEL_MOVEMENT_ARM = uc("arm");
    public final static List<String> SEL_MOVEMENT_MAX_SPEED = uc("speed", "reversespeed");
    public final static List<String> SEL_MOVEMENT_MAX_FWD_SPEED = uc("speed");
    public final static List<String> SEL_MOVEMENT_MAX_REV_SPEED = uc("reversespeed");
    public final static List<String> SEL_MOVEMENT_TORSO = uc("torso");
    public final static List<String> SEL_MOVEMENT_TURN_RATE = uc("turnlerp", "turnrate");
    public final static List<String> SEL_MOVEMENT_TURN_SPEED = uc("turnlerp_speed");
    public final static List<String> SEL_MOVEMENT_ACCELLERP = uc("accellerp");
    public final static List<String> SEL_MOVEMENT_DECELLERP = uc("decellerp");
    public final static List<String> SEL_STRUCTURE = uc("internalresist");
    public final static List<String> SEL_XP_BONUS = uc("xpbonus");
    public final static List<String> SEL_CRIT_CHANCE = uc("critchance");
    public final static List<String> SEL_SENSOR_RANGE = uc("sensorrange");
    public final static List<String> SEL_SENSOR_TARGET_DECAY_DURATION = uc("targetdecayduration");

    public final static String SPEC_ALL = "all";
    public final static String SPEC_WEAPON_COOL_DOWN = "cooldown";
    public final static String SPEC_WEAPON_ROF = "rof";
    public final static String SPEC_WEAPON_HEAT = "heat";
    public final static String SPEC_WEAPON_PROJECTILE_SPEED = "velocity";
    public final static String SPEC_WEAPON_JAMMED_TIME = "jamtime";
    public final static String SPEC_WEAPON_JAMMING_CHANCE = "jamchance";
    public final static String SPEC_WEAPON_LARGE_BORE = "largeweapon";
    public final static String SPEC_WEAPON_RANGE = "range";
    public final static String SPEC_WEAPON_SPREAD = "spread";
    public final static String SPEC_WEAPON_TAG_DURATION = "tagduration";
    public final static String SPEC_WEAPON_DAMAGE = "damage";
    public final static String SPEC_WEAPON_DURATION = "duration";
    public final static String SPEC_WEAPON_NARC_DURATION = "narcduration";
    public static final String SPEC_WEAPON_MAX_FREE_ALPAHA = "minheatpenaltylevel";

    public final static String SPEC_MOVEMENT_PITCHSPEED = "pitchspeed";
    public final static String SPEC_MOVEMENT_YAWSPEED = "yawspeed";
    public final static String SPEC_MOVEMENT_PITCHANGLE = "pitchangle";
    public final static String SPEC_MOVEMENT_YAWANGLE = "yawangle";

    public final static String SPEC_CRIT_RECEIVING = "receiving";

    private final static Set<String> ALL_SELECTORS;

    private final static Set<String> ALL_SPECIFIERS;
    static {
        ALL_SELECTORS = new HashSet<>();
        ALL_SELECTORS.addAll(SEL_ALL);
        ALL_SELECTORS.addAll(SEL_ALL_WEAPONS);
        ALL_SELECTORS.addAll(SEL_ARMOUR);
        ALL_SELECTORS.addAll(SEL_HEAT_DISSIPATION);
        ALL_SELECTORS.addAll(SEL_HEAT_EXTERNALTRANSFER);
        ALL_SELECTORS.addAll(SEL_HEAT_LIMIT);
        ALL_SELECTORS.addAll(SEL_HEAT_MOVEMENT);
        ALL_SELECTORS.addAll(SEL_MOVEMENT_ARM);
        ALL_SELECTORS.addAll(SEL_MOVEMENT_MAX_SPEED);
        ALL_SELECTORS.addAll(SEL_MOVEMENT_MAX_FWD_SPEED);
        ALL_SELECTORS.addAll(SEL_MOVEMENT_MAX_REV_SPEED);
        ALL_SELECTORS.addAll(SEL_MOVEMENT_TORSO);
        ALL_SELECTORS.addAll(SEL_MOVEMENT_TURN_RATE);
        ALL_SELECTORS.addAll(SEL_MOVEMENT_TURN_SPEED);
        ALL_SELECTORS.addAll(SEL_STRUCTURE);
        ALL_SELECTORS.addAll(SEL_XP_BONUS);
        ALL_SELECTORS.addAll(SEL_MOVEMENT_ACCELLERP);
        ALL_SELECTORS.addAll(SEL_MOVEMENT_DECELLERP);
        ALL_SELECTORS.addAll(SEL_HEAT_DAMAGE);
        ALL_SELECTORS.addAll(SEL_CRIT_CHANCE);
        ALL_SELECTORS.addAll(SEL_SENSOR_RANGE);
        ALL_SELECTORS.addAll(SEL_SENSOR_TARGET_DECAY_DURATION);

        ALL_SPECIFIERS = new HashSet<>();
        ALL_SPECIFIERS.add(SPEC_ALL);
        ALL_SPECIFIERS.add(SPEC_WEAPON_COOL_DOWN);
        ALL_SPECIFIERS.add(SPEC_WEAPON_ROF);
        ALL_SPECIFIERS.add(SPEC_WEAPON_HEAT);
        ALL_SPECIFIERS.add(SPEC_WEAPON_PROJECTILE_SPEED);
        ALL_SPECIFIERS.add(SPEC_WEAPON_JAMMED_TIME);
        ALL_SPECIFIERS.add(SPEC_WEAPON_JAMMING_CHANCE);
        ALL_SPECIFIERS.add(SPEC_WEAPON_LARGE_BORE);
        ALL_SPECIFIERS.add(SPEC_WEAPON_RANGE);
        ALL_SPECIFIERS.add(SPEC_WEAPON_SPREAD);
        ALL_SPECIFIERS.add(SPEC_WEAPON_TAG_DURATION);
        ALL_SPECIFIERS.add(SPEC_WEAPON_DAMAGE);
        ALL_SPECIFIERS.add(SPEC_WEAPON_DURATION);
        ALL_SPECIFIERS.add(SPEC_WEAPON_NARC_DURATION);
        ALL_SPECIFIERS.add(SPEC_WEAPON_MAX_FREE_ALPAHA);

        ALL_SPECIFIERS.add(SPEC_MOVEMENT_PITCHSPEED);
        ALL_SPECIFIERS.add(SPEC_MOVEMENT_YAWSPEED);
        ALL_SPECIFIERS.add(SPEC_MOVEMENT_PITCHANGLE);
        ALL_SPECIFIERS.add(SPEC_MOVEMENT_YAWANGLE);

        ALL_SPECIFIERS.add(SPEC_CRIT_RECEIVING);

        for (final Location location : Location.values()) {
            for (final ArmourSide side : ArmourSide.values()) {
                ALL_SPECIFIERS.add(specifierFor(location, side));
            }
        }
    }

    public static String canonizeIdentifier(String aString) {
        if (aString != null && !aString.isEmpty()) {
            return aString.toLowerCase().trim();
        }
        return null;
    }

    public static boolean isKnownSelector(String aSelector, Map<Integer, Object> aItems) {
        if (ALL_SELECTORS.contains(aSelector)) {
            return true;
        }

        return aItems.values().stream().filter(o -> o instanceof Weapon).map(o -> (Weapon) o)
                .filter(w -> w.getAliases().contains(aSelector)).findAny().isPresent();
    }

    public static boolean isKnownSpecifier(String aSpecifier) {
        return ALL_SPECIFIERS.contains(aSpecifier);
    }

    public static String specifierFor(Location aLocation, ArmourSide aArmourSide) {
        if (aArmourSide == ArmourSide.BACK) {
            return aLocation.shortName().toLowerCase() + "R";
        }
        return aLocation.shortName().toLowerCase();
    }

    private static List<String> uc(String... aStrings) {
        return Collections.unmodifiableList(Arrays.asList(aStrings));
    }

    @XStreamAsAttribute
    private final String mwoKey;
    @XStreamAsAttribute
    private final Operation operation;
    private final Collection<String> selectors;
    @XStreamAsAttribute
    private final String specifier; // Can be null
    @XStreamAsAttribute
    private final ModifierType type;
    @XStreamAsAttribute
    private final String uiName;

    /**
     * Creates a new modifier.
     *
     * @param aUiName
     *            The human readable name of the modifier.
     * @param aKeyName
     *            The MWO enumeration name of this modifier.
     * @param aOperation
     *            The {@link Operation} to perform.
     * @param aSelectors
     *            A {@link List} of selectors, used to see if this modifier is applied to a given {@link Attribute}.
     * @param aSpecifier
     *            The attribute of the selected datum to modify, may be <code>null</code> if the attribute is implicitly
     *            understood from the context.
     * @param aValueType
     *            The type of value (positive good, negative good, indeterminate) that this {@link ModifierDescription}
     *            represents.
     */
    public ModifierDescription(String aUiName, String aKeyName, Operation aOperation, Collection<String> aSelectors,
            String aSpecifier, ModifierType aValueType) {
        uiName = aUiName;
        mwoKey = canonizeIdentifier(Objects.requireNonNull(aKeyName));
        operation = aOperation;
        selectors = aSelectors.stream().map(s -> canonizeIdentifier(s)).collect(Collectors.toSet());
        specifier = canonizeIdentifier(aSpecifier);
        type = aValueType;
    }

    /**
     * Checks if this {@link ModifierDescription} affects the given {@link Attribute}.
     *
     * @param aAttribute
     *            The {@link Attribute} to test.
     * @return <code>true</code> if the attribute is affected, false otherwise.
     */
    public boolean affects(Attribute aAttribute) {
        if (specifier == null) {
            if (aAttribute.getSpecifier() != null) {
                return false;
            }
        }
        else {
            if (!specifier.equals(SPEC_ALL)
                    && (aAttribute.getSpecifier() == null || !aAttribute.getSpecifier().equals(specifier))) {
                return false;
            }
        }

        if (!Collections.disjoint(SEL_ALL, selectors)) {
            return true;
        }

        return !Collections.disjoint(selectors, aAttribute.getSelectors());
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof ModifierDescription) {
            final ModifierDescription other = (ModifierDescription) obj;

            if (specifier == null) {
                if (other.specifier != null) {
                    return false;
                }
            }
            else if (!specifier.equals(other.specifier)) {
                return false;
            }

            return other.uiName.equals(uiName) && other.mwoKey.equals(mwoKey) && other.operation == operation
                    && other.type == type && selectors.containsAll(other.selectors)
                    && selectors.size() == other.selectors.size();
        }
        return false;
    }

    /**
     * @return The MWO key for referring to this description.
     */
    public String getKey() {
        return mwoKey;
    }

    /**
     * @return The {@link ModifierType} of this {@link ModifierDescription}.
     */
    public ModifierType getModifierType() {
        return type;
    }

    /**
     * @return The {@link Operation} that this {@link ModifierDescription} performs.
     */
    public Operation getOperation() {
        return operation;
    }

    /**
     * @return A {@link Collection} if {@link String}s with all the selectors of this modifier.
     */
    public Collection<String> getSelectors() {
        return Collections.unmodifiableCollection(selectors);
    }

    /**
     * @return The specifier for the modifier.
     */
    public String getSpecifier() {
        return specifier;
    }

    /**
     * @return The human readable name of this {@link ModifierDescription}.
     */
    public String getUiName() {
        return uiName;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = (prime + operation.hashCode()) * prime + type.hashCode();
        result = prime * result + selectors.hashCode();
        result = prime * result + (specifier == null ? 0 : specifier.hashCode());
        return result;
    }

    @Override
    public String toString() {
        return uiName;
    }
}
