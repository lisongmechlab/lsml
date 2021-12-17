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

import com.thoughtworks.xstream.annotations.XStreamAsAttribute;
import org.lisoft.lsml.model.chassi.ArmourSide;
import org.lisoft.lsml.model.chassi.Location;
import org.lisoft.lsml.model.item.Ammunition;
import org.lisoft.lsml.model.item.Weapon;

import java.util.*;
import java.util.stream.Collectors;

/**
 * This class models a description of how a generic modifier can be applied to an {@link Attribute}. A {@link Modifier}
 * is a {@link ModifierDescription} together with actual modifier value that modifiers an {@link Attribute}.
 * <p>
 * One {@link ModifierDescription}s can affect many attributes. To facilitate this,
 * {@link ModifierDescription}s and {@link Attribute}s have a set of "selector tags". Selector tags can be things such
 * as "Top Speed", "Laser weapons", "IS Large Laser", "Clan ACs". In addition to selector tags, each {@link Attribute}
 * and {@link ModifierDescription} can have a specific named specifier within the selector tag that is affected. For
 * example the selector tag may be "IS Laser Weapons" and the specifier can be "BurnTime". However when the selector
 * uniquely identifies exactly one attribute, like in the case of "Top Speed" then the specifier is <code>null</code>.
 * <p>
 * All of this conspires to create a powerful system where just about any value can be affected by modifiers coming from
 * different sources, such as pilot efficiencies, 'Mech quirks, equipped items and modules etc.
 *
 * Example: A quirk in a chassis could be called "energy_range_multiplier" and this would have selector: "energy" and
 * specifier: "range", and an operation as "multiplier". It would select the "range" attribute of for example a MEDIUM
 * LASER that has the following selectors from the data file: `HardpointAliases="Energy,Laser,StdLaser,MediumLaser,
 * ISLaser,ISStdLaser,ISMediumLaserFamily,ISMediumLaser,NonPulseLaser,ISNonPulseLaser"` because of the energy alias.
 *
 * @author Li Song
 */
public class ModifierDescription {
    /**
     * Special selector used to select everything with a matching specifier.
     * For example the quirk: "all_heat_multiplier".
     */
    public final static Collection<String> SEL_ALL = uc("all");

    public final static Collection<String> SEL_AMMOCAPACITY = uc("ammocapacity");
    public final static Collection<String> SEL_ARMOUR_RESIST = uc("armorresist");
    public final static Collection<String> SEL_ARMOUR_STEALTH_COOLDOWN = uc("stealtharmorcooldown");
    public final static Collection<String> SEL_CAP_ACCELERATOR = uc("captureaccelerator");
    public final static Collection<String> SEL_CONSUMABLE_SLOTS = uc("extraconsumableslot");
    public final static Collection<String> SEL_CONSUMABLE_UAVS = uc("uavcapacity");
    public final static Collection<String> SEL_CONSUMABLE_UAV_RANGE = uc("uavrange");
    public final static Collection<String> SEL_CRIT_CHANCE = uc("critchance");
    public final static Collection<String> SEL_HEAT_DAMAGE = uc("overheatdamage");
    public final static Collection<String> SEL_HEAT_DISSIPATION = uc("heatloss", "heatdissipation");
    public final static Collection<String> SEL_HEAT_EXTERNALTRANSFER = uc("externalheat");
    public final static Collection<String> SEL_HEAT_LIMIT = uc("heatlimit");
    public final static Collection<String> SEL_HEAT_MOVEMENT = uc("movementheat");
    public final static Collection<String> SEL_MOVEMENT_ACCEL = uc("mechacceleration");
    public final static Collection<String> SEL_MOVEMENT_ACCELLERP = uc("accellerp");
    public final static Collection<String> SEL_MOVEMENT_ARM = uc("arm");
    public final static Collection<String> SEL_MOVEMENT_DECEL = uc("mechdeceleration");
    public final static Collection<String> SEL_MOVEMENT_DECELLERP = uc("decellerp");
    public final static Collection<String> SEL_MOVEMENT_FALL_DAMAGE = uc("falldamage");
    public final static Collection<String> SEL_MOVEMENT_HILL_CLIMB = uc("hillclimb");
    public final static Collection<String> SEL_MOVEMENT_JUMPJETS = uc("jumpjets");
    public final static Collection<String> SEL_MOVEMENT_MAX_FWD_SPEED = uc("speed", "mechtopspeed");
    public final static Collection<String> SEL_MOVEMENT_MAX_REV_SPEED = uc("reversespeed");
    public final static Collection<String> SEL_MOVEMENT_MAX_SPEED = uc("speed", "reversespeed", "mechtopspeed");
    public final static Collection<String> SEL_MOVEMENT_TORSO = uc("torso");
    public final static Collection<String> SEL_MOVEMENT_TURN_RATE = uc("turnlerp", "turnrate");
    public final static Collection<String> SEL_MOVEMENT_TURN_SPEED = uc("turnlerp_speed");
    public final static Collection<String> SEL_SENSOR_ECM_TARGET_RANGE_REDUCTION = uc("ecmtargetrangereduction");
    public final static Collection<String> SEL_SENSOR_RADAR_DEPRIVATION = uc("radardeprivation");
    public final static Collection<String> SEL_SENSOR_RANGE = uc("sensorrange");
    public final static Collection<String> SEL_SENSOR_SEISMIC_RANGE = uc("seismicsensorrange");
    public final static Collection<String> SEL_SENSOR_TARGET_DECAY_DURATION = uc("targetdecayduration");
    public final static Collection<String> SEL_SENSOR_TARGET_INFO_GATHERING = uc("targetinfogathering");
    public final static Collection<String> SEL_SENSOR_TARGET_RETENTION = uc("backfacetargetretentionrange");
    public final static Collection<String> SEL_STRUCTURE = uc("internalresist");
    public final static Collection<String> SEL_XP_BONUS = uc("xpbonus");


    /**
     * Special specifier used to match all specifiers of a selector (e.g. all LERP parameters)
     */
    public final static String SPEC_ALL = "all";

    public final static String SPEC_CRIT_RECEIVING = "receiving";
    public final static String SPEC_JUMPJETS_BURN_TIME = "burntime";
    public final static String SPEC_JUMPJETS_INITIAL_THRUST = "initialthrust";
    public final static String SPEC_MOVEMENT_PITCHANGLE = "pitchangle";
    public final static String SPEC_MOVEMENT_PITCHSPEED = "pitchspeed";
    public final static String SPEC_MOVEMENT_YAWANGLE = "yawangle";
    public final static String SPEC_MOVEMENT_YAWSPEED = "yawspeed";
    public final static String SPEC_WEAPON_COOL_DOWN = "cooldown";
    public final static String SPEC_WEAPON_DAMAGE = "damage";
    public final static String SPEC_WEAPON_DURATION = "duration";
    public final static String SPEC_WEAPON_HEAT = "heat";
    public final static String SPEC_WEAPON_JAM_DURATION = "jamduration";
    public final static String SPEC_WEAPON_JAM_PROBABILITY = "jamchance";
    public final static String SPEC_WEAPON_JAM_RAMP_DOWN_TIME = "jamrampdownduration";
    public final static String SPEC_WEAPON_LARGE_BORE = "largeweapon";
    public final static String SPEC_WEAPON_MAX_FREE_ALPHA = "minheatpenaltylevel";
    public final static String SPEC_WEAPON_NARC_DURATION = "narcduration";
    public final static String SPEC_WEAPON_PROJECTILE_SPEED = "velocity";
    public final static String SPEC_WEAPON_RANGE = "range";
    public final static String SPEC_WEAPON_ROF = "rof";
    public final static String SPEC_WEAPON_SPREAD = "spread";
    public final static String SPEC_WEAPON_TAG_DURATION = "tagduration";

    private final static Set<String> ALL_SELECTORS;
    private final static Set<String> ALL_SPECIFIERS;

    static {
        ALL_SELECTORS = new HashSet<>();
        ALL_SELECTORS.addAll(SEL_ALL);
        ALL_SELECTORS.addAll(SEL_AMMOCAPACITY);
        ALL_SELECTORS.addAll(SEL_ARMOUR_RESIST);
        ALL_SELECTORS.addAll(SEL_ARMOUR_STEALTH_COOLDOWN);
        ALL_SELECTORS.addAll(SEL_CAP_ACCELERATOR);
        ALL_SELECTORS.addAll(SEL_CONSUMABLE_SLOTS);
        ALL_SELECTORS.addAll(SEL_CONSUMABLE_UAVS);
        ALL_SELECTORS.addAll(SEL_CONSUMABLE_UAV_RANGE);
        ALL_SELECTORS.addAll(SEL_CRIT_CHANCE);
        ALL_SELECTORS.addAll(SEL_HEAT_DAMAGE);
        ALL_SELECTORS.addAll(SEL_HEAT_DISSIPATION);
        ALL_SELECTORS.addAll(SEL_HEAT_EXTERNALTRANSFER);
        ALL_SELECTORS.addAll(SEL_HEAT_LIMIT);
        ALL_SELECTORS.addAll(SEL_HEAT_MOVEMENT);
        ALL_SELECTORS.addAll(SEL_MOVEMENT_ACCEL);
        ALL_SELECTORS.addAll(SEL_MOVEMENT_ACCELLERP);
        ALL_SELECTORS.addAll(SEL_MOVEMENT_ARM);
        ALL_SELECTORS.addAll(SEL_MOVEMENT_DECEL);
        ALL_SELECTORS.addAll(SEL_MOVEMENT_DECELLERP);
        ALL_SELECTORS.addAll(SEL_MOVEMENT_FALL_DAMAGE);
        ALL_SELECTORS.addAll(SEL_MOVEMENT_HILL_CLIMB);
        ALL_SELECTORS.addAll(SEL_MOVEMENT_JUMPJETS);
        ALL_SELECTORS.addAll(SEL_MOVEMENT_MAX_FWD_SPEED);
        ALL_SELECTORS.addAll(SEL_MOVEMENT_MAX_REV_SPEED);
        ALL_SELECTORS.addAll(SEL_MOVEMENT_MAX_SPEED);
        ALL_SELECTORS.addAll(SEL_MOVEMENT_TORSO);
        ALL_SELECTORS.addAll(SEL_MOVEMENT_TURN_RATE);
        ALL_SELECTORS.addAll(SEL_MOVEMENT_TURN_SPEED);
        ALL_SELECTORS.addAll(SEL_SENSOR_ECM_TARGET_RANGE_REDUCTION);
        ALL_SELECTORS.addAll(SEL_SENSOR_RADAR_DEPRIVATION);
        ALL_SELECTORS.addAll(SEL_SENSOR_RANGE);
        ALL_SELECTORS.addAll(SEL_SENSOR_SEISMIC_RANGE);
        ALL_SELECTORS.addAll(SEL_SENSOR_TARGET_DECAY_DURATION);
        ALL_SELECTORS.addAll(SEL_SENSOR_TARGET_INFO_GATHERING);
        ALL_SELECTORS.addAll(SEL_SENSOR_TARGET_RETENTION);
        ALL_SELECTORS.addAll(SEL_STRUCTURE);
        ALL_SELECTORS.addAll(SEL_XP_BONUS);

        ALL_SPECIFIERS = new HashSet<>();
        ALL_SPECIFIERS.add(SPEC_ALL);
        ALL_SPECIFIERS.add(SPEC_CRIT_RECEIVING);
        ALL_SPECIFIERS.add(SPEC_JUMPJETS_BURN_TIME);
        ALL_SPECIFIERS.add(SPEC_JUMPJETS_INITIAL_THRUST);
        ALL_SPECIFIERS.add(SPEC_MOVEMENT_PITCHANGLE);
        ALL_SPECIFIERS.add(SPEC_MOVEMENT_PITCHSPEED);
        ALL_SPECIFIERS.add(SPEC_MOVEMENT_YAWANGLE);
        ALL_SPECIFIERS.add(SPEC_MOVEMENT_YAWSPEED);
        ALL_SPECIFIERS.add(SPEC_WEAPON_COOL_DOWN);
        ALL_SPECIFIERS.add(SPEC_WEAPON_DAMAGE);
        ALL_SPECIFIERS.add(SPEC_WEAPON_DURATION);
        ALL_SPECIFIERS.add(SPEC_WEAPON_HEAT);
        ALL_SPECIFIERS.add(SPEC_WEAPON_JAM_DURATION);
        ALL_SPECIFIERS.add(SPEC_WEAPON_JAM_PROBABILITY);
        ALL_SPECIFIERS.add(SPEC_WEAPON_JAM_RAMP_DOWN_TIME);
        ALL_SPECIFIERS.add(SPEC_WEAPON_LARGE_BORE);
        ALL_SPECIFIERS.add(SPEC_WEAPON_MAX_FREE_ALPHA);
        ALL_SPECIFIERS.add(SPEC_WEAPON_NARC_DURATION);
        ALL_SPECIFIERS.add(SPEC_WEAPON_PROJECTILE_SPEED);
        ALL_SPECIFIERS.add(SPEC_WEAPON_RANGE);
        ALL_SPECIFIERS.add(SPEC_WEAPON_ROF);
        ALL_SPECIFIERS.add(SPEC_WEAPON_SPREAD);
        ALL_SPECIFIERS.add(SPEC_WEAPON_TAG_DURATION);

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

    /**
     * This method can be used before the ItemDB class is available (i.e. while building the database) to determine
     * whether a proposed selector would select any known quantity.
     *
     * @param aSelector The selector to test.
     * @param aItems    A collection of items to check for selectors in.
     * @return true if the selector is known to affect something.
     */
    public static boolean isKnownSelector(String aSelector, Collection<Object> aItems) {
        if (ALL_SELECTORS.contains(aSelector)) {
            return true;
        }

        return aItems.stream().filter(o -> o instanceof Weapon).map(o -> (Weapon) o)
                .anyMatch(w -> w.getAliases().contains(aSelector));
    }

    /**
     * This method checks if a specifier affects an attribute that LSML knowns about. This is mostly useful for
     * verification while parsing the game data files.
     *
     * @param aSpecifier the specifier to test.
     * @param aItems    A collection of items to check for specifiers in.
     * @return true if the specifier is known to affect something.
     */
    public static boolean isKnownSpecifier(String aSpecifier, Collection<Object> aItems) {
        if(ALL_SPECIFIERS.contains(aSpecifier)){
            return true;
        }
        return aItems.stream().filter(o -> o instanceof Ammunition).map(o -> (Ammunition) o)
                .anyMatch(w -> w.getQuirkSpecifier().equalsIgnoreCase(aSpecifier));
    }

    /**
     * Create a specifier for as specific component location and side.
     *
     * @param aLocation   The location
     * @param aArmourSide The side
     * @return a specifier
     */
    public static String specifierFor(Location aLocation, ArmourSide aArmourSide) {
        return aLocation.shortName().toLowerCase() + (aArmourSide == ArmourSide.BACK ? "R" : "");
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
     * @param aUiName    The human readable name of the modifier.
     * @param aKeyName   The MWO enumeration name of this modifier.
     * @param aOperation The {@link Operation} to perform.
     * @param aSelectors A {@link List} of selectors, used to see if this modifier is applied to a given {@link Attribute}.
     * @param aSpecifier The attribute of the selected datum to modify, may be <code>null</code> if the attribute is implicitly
     *                   understood from the context.
     * @param aValueType The type of value (positive good, negative good, indeterminate) that this {@link ModifierDescription}
     *                   represents.
     */
    public ModifierDescription(String aUiName, String aKeyName, Operation aOperation, Collection<String> aSelectors,
                               String aSpecifier, ModifierType aValueType) {
        uiName = aUiName;
        mwoKey = canonizeIdentifier(Objects.requireNonNull(aKeyName));
        operation = aOperation;
        selectors = aSelectors.stream().map(ModifierDescription::canonizeIdentifier).collect(Collectors.toSet());
        specifier = canonizeIdentifier(aSpecifier);
        type = aValueType;
    }

    /**
     * Checks if this {@link ModifierDescription} affects the given {@link Attribute}.
     *
     * @param aAttribute The {@link Attribute} to test.
     * @return <code>true</code> if the attribute is affected, false otherwise.
     */
    public boolean affects(Attribute aAttribute) {
        if (specifier == null) {
            if (aAttribute.getSpecifier() != null) {
                return false;
            }
        } else {
            if (!specifier.equals(SPEC_ALL)
                    && (aAttribute.getSpecifier() == null || !aAttribute.getSpecifier().equals(specifier))) {
                return false;
            }
        }

        // Is this a special attribute that has the "all" selector that affects everything?
        if (!Collections.disjoint(SEL_ALL, selectors)) {
            return true;
        }

        // Does at least one of the selectors match?
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
            } else if (!specifier.equals(other.specifier)) {
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
