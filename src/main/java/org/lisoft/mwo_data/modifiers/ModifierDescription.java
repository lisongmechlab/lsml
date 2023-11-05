/*
 * Li Song Mechlab - A 'mech building tool for PGI's MechWarrior: Online.
 * Copyright (C) 2013-2023  Li Song
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
package org.lisoft.mwo_data.modifiers;

import com.thoughtworks.xstream.annotations.XStreamAsAttribute;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import org.lisoft.mwo_data.equipment.Ammunition;
import org.lisoft.mwo_data.equipment.Item;
import org.lisoft.mwo_data.equipment.Weapon;
import org.lisoft.mwo_data.mechs.ArmourSide;
import org.lisoft.mwo_data.mechs.Location;

/**
 * This class models a description of how a generic modifier can be applied to an {@link Attribute}.
 * A {@link Modifier} is a {@link ModifierDescription} together with actual modifier value that
 * modifiers an {@link Attribute}.
 *
 * <p>One {@link ModifierDescription}s can affect many attributes. To facilitate this, {@link
 * ModifierDescription}s and {@link Attribute}s have a set of "selector tags". Selector tags can be
 * things such as "Top Speed", "Laser weapons", "IS Large Laser", "Clan ACs". In addition to
 * selector tags, each {@link Attribute} and {@link ModifierDescription} can have a specific named
 * specifier within the selector tag that is affected. For example the selector tag may be "IS Laser
 * Weapons" and the specifier can be "BurnTime". However, when the selector uniquely identifies
 * exactly one attribute, like in the case of "Top Speed" then the specifier is <code>null</code>.
 *
 * <p>All of this conspires to create a powerful system where just about any value can be affected
 * by modifiers coming from different sources, such as pilot efficiencies, Mech quirks, equipped
 * items and modules etc.
 *
 * <p>Example: A quirk in a chassis could be called "energy_range_multiplier" and this would have
 * selector: "energy" and specifier: "range", and an operation as "multiplier". It would select the
 * "range" attribute of for example a MEDIUM LASER that has the following selectors from the data
 * file: `HardpointAliases="Energy,Laser,StdLaser,MediumLaser,
 * ISLaser,ISStdLaser,ISMediumLaserFamily,ISMediumLaser,NonPulseLaser,ISNonPulseLaser"` because of
 * the energy alias.
 *
 * @author Li Song
 */
@SuppressWarnings("SpellCheckingInspection")
public class ModifierDescription {
  /**
   * Special selector used to select everything with a matching specifier. For example the quirk:
   * "all_heat_multiplier".
   */
  public static final Collection<String> SEL_ALL = uc("all");

  public static final Collection<String> SEL_AMMO_CAPACITY = uc("ammocapacity");
  public static final Collection<String> SEL_ARMOUR_RESIST = uc("armorresist");
  public static final Collection<String> SEL_ARMOUR_STEALTH_COOL_DOWN = uc("stealtharmorcooldown");
  public static final Collection<String> SEL_CAP_ACCELERATOR = uc("captureaccelerator");
  public static final Collection<String> SEL_CONSUMABLE_SLOTS = uc("extraconsumableslot");
  public static final Collection<String> SEL_COOLSHOT_CAPACITY = uc("coolshotcapacity");
  public static final Collection<String> SEL_CRITICAL_CHANCE = uc("critchance");
  public static final Collection<String> SEL_HEAT_DAMAGE = uc("overheatdamage");
  public static final Collection<String> SEL_HEAT_DISSIPATION = uc("heatloss", "heatdissipation");
  public static final Collection<String> SEL_HEAT_EXTERNAL_TRANSFER = uc("externalheat");
  public static final Collection<String> SEL_HEAT_LIMIT = uc("maxheat");
  public static final Collection<String> SEL_HEAT_MOVEMENT = uc("movementheat");
  public static final Collection<String> SEL_MOVEMENT_ACCEL = uc("mechacceleration");
  public static final Collection<String> SEL_MOVEMENT_ACCEL_LERP = uc("accellerp");
  public static final Collection<String> SEL_MOVEMENT_ARM = uc("arm");
  public static final Collection<String> SEL_MOVEMENT_DECEL = uc("mechdeceleration");
  public static final Collection<String> SEL_MOVEMENT_DECEL_LERP = uc("decellerp");
  public static final Collection<String> SEL_MOVEMENT_FALL_DAMAGE = uc("falldamage");
  public static final Collection<String> SEL_MOVEMENT_HILL_CLIMB = uc("hillclimb");
  public static final Collection<String> SEL_MOVEMENT_JUMP_JETS = uc("jumpjets");
  public static final Collection<String> SEL_MOVEMENT_MAX_FWD_SPEED = uc("speed", "mechtopspeed");
  public static final Collection<String> SEL_MOVEMENT_MAX_REV_SPEED = uc("reversespeed");
  public static final Collection<String> SEL_MOVEMENT_MAX_SPEED =
      uc("speed", "reversespeed", "mechtopspeed");
  public static final Collection<String> SEL_MOVEMENT_TORSO = uc("torso");
  public static final Collection<String> SEL_MOVEMENT_TURN_RATE = uc("turnlerp", "turnrate");
  public static final Collection<String> SEL_MOVEMENT_TURN_SPEED = uc("turnlerp_speed");
  public static final Collection<String> SEL_SENSOR_ECM_TARGET_RANGE_REDUCTION =
      uc("ecmtargetrangereduction");
  public static final Collection<String> SEL_SENSOR_RADAR_DEPRIVATION = uc("radardeprivation");
  public static final Collection<String> SEL_SENSOR_RANGE = uc("sensorrange");
  public static final Collection<String> SEL_SENSOR_SEISMIC_RANGE = uc("seismicsensorrange");
  public static final Collection<String> SEL_SENSOR_TARGET_DECAY_DURATION =
      uc("targetdecayduration");
  public static final Collection<String> SEL_SENSOR_TARGET_INFO_GATHERING =
      uc("targetinfogathering");
  public static final Collection<String> SEL_SENSOR_TARGET_RETENTION =
      uc("backfacetargetretentionrange");
  public static final Collection<String> SEL_STRATEGIC_STRIKE_CAPACITY =
      uc("strategicstrikecapacity");
  public static final Collection<String> SEL_STRUCTURE = uc("internalresist");
  public static final Collection<String> SEL_UAV_CAPACITY = uc("uavcapacity");
  public static final Collection<String> SEL_UAV_DURATION = uc("uavduration");
  public static final Collection<String> SEL_UAV_RANGE = uc("uavrange");
  public static final Collection<String> SEL_XP_BONUS = uc("xpbonus");

  /** Special specifier used to match all specifiers of a selector (e.g. all LERP parameters) */
  public static final String SPEC_ALL = "all";

  public static final String SPEC_CRITICAL_RECEIVING = "receiving";
  public static final String SPEC_JUMP_JETS_BURN_TIME = "burntime";
  public static final String SPEC_JUMP_JETS_INITIAL_THRUST = "initialthrust";
  public static final String SPEC_MOVEMENT_PITCH_ANGLE = "pitchangle";
  public static final String SPEC_MOVEMENT_PITCH_SPEED = "pitchspeed";
  public static final String SPEC_MOVEMENT_YAW_ANGLE = "yawangle";
  public static final String SPEC_MOVEMENT_YAW_SPEED = "yawspeed";
  public static final String SPEC_WEAPON_COOL_DOWN = "cooldown";
  public static final String SPEC_WEAPON_DAMAGE = "damage";
  public static final String SPEC_WEAPON_DURATION = "duration";
  public static final String SPEC_WEAPON_HEAT = "heat";
  public static final String SPEC_WEAPON_JAM_DURATION = "jamduration";
  public static final String SPEC_WEAPON_JAM_PROBABILITY = "jamchance";
  public static final String SPEC_WEAPON_JAM_RAMP_DOWN_TIME = "jamrampdownduration";
  public static final String SPEC_WEAPON_LARGE_BORE = "largeweapon";
  public static final String SPEC_WEAPON_MAX_FREE_ALPHA = "minheatpenaltylevel";
  public static final String SPEC_WEAPON_NARC_DURATION = "narcduration";
  public static final String SPEC_WEAPON_PROJECTILE_SPEED = "velocity";
  public static final String SPEC_WEAPON_RANGE = "range";
  public static final String SPEC_WEAPON_ROF = "rof";
  public static final String SPEC_WEAPON_SPREAD = "spread";
  public static final String SPEC_WEAPON_TAG_DURATION = "tagduration";
  public static final String SPEC_LOW_RATE = "lowrate";
  public static final String SPEC_MID_RATE = "midrate";
  public static final String SPEC_HIGH_RATE = "highrate";

  private static final Set<String> ALL_SELECTORS;
  private static final Set<String> ALL_SPECIFIERS;

  static {
    ALL_SELECTORS = new HashSet<>();
    ALL_SELECTORS.addAll(SEL_ALL);
    ALL_SELECTORS.addAll(SEL_AMMO_CAPACITY);
    ALL_SELECTORS.addAll(SEL_ARMOUR_RESIST);
    ALL_SELECTORS.addAll(SEL_ARMOUR_STEALTH_COOL_DOWN);
    ALL_SELECTORS.addAll(SEL_CAP_ACCELERATOR);
    ALL_SELECTORS.addAll(SEL_CONSUMABLE_SLOTS);
    ALL_SELECTORS.addAll(SEL_COOLSHOT_CAPACITY);
    ALL_SELECTORS.addAll(SEL_CRITICAL_CHANCE);
    ALL_SELECTORS.addAll(SEL_HEAT_DAMAGE);
    ALL_SELECTORS.addAll(SEL_HEAT_DISSIPATION);
    ALL_SELECTORS.addAll(SEL_HEAT_EXTERNAL_TRANSFER);
    ALL_SELECTORS.addAll(SEL_HEAT_LIMIT);
    ALL_SELECTORS.addAll(SEL_HEAT_MOVEMENT);
    ALL_SELECTORS.addAll(SEL_MOVEMENT_ACCEL);
    ALL_SELECTORS.addAll(SEL_MOVEMENT_ACCEL_LERP);
    ALL_SELECTORS.addAll(SEL_MOVEMENT_ARM);
    ALL_SELECTORS.addAll(SEL_MOVEMENT_DECEL);
    ALL_SELECTORS.addAll(SEL_MOVEMENT_DECEL_LERP);
    ALL_SELECTORS.addAll(SEL_MOVEMENT_FALL_DAMAGE);
    ALL_SELECTORS.addAll(SEL_MOVEMENT_HILL_CLIMB);
    ALL_SELECTORS.addAll(SEL_MOVEMENT_JUMP_JETS);
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
    ALL_SELECTORS.addAll(SEL_STRATEGIC_STRIKE_CAPACITY);
    ALL_SELECTORS.addAll(SEL_STRUCTURE);
    ALL_SELECTORS.addAll(SEL_UAV_CAPACITY);
    ALL_SELECTORS.addAll(SEL_UAV_DURATION);
    ALL_SELECTORS.addAll(SEL_UAV_RANGE);
    ALL_SELECTORS.addAll(SEL_XP_BONUS);

    ALL_SPECIFIERS = new HashSet<>();
    ALL_SPECIFIERS.add(SPEC_ALL);
    ALL_SPECIFIERS.add(SPEC_CRITICAL_RECEIVING);
    ALL_SPECIFIERS.add(SPEC_JUMP_JETS_BURN_TIME);
    ALL_SPECIFIERS.add(SPEC_JUMP_JETS_INITIAL_THRUST);
    ALL_SPECIFIERS.add(SPEC_MOVEMENT_PITCH_ANGLE);
    ALL_SPECIFIERS.add(SPEC_MOVEMENT_PITCH_SPEED);
    ALL_SPECIFIERS.add(SPEC_MOVEMENT_YAW_ANGLE);
    ALL_SPECIFIERS.add(SPEC_MOVEMENT_YAW_SPEED);
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

  @XStreamAsAttribute private final String mwoKey;
  @XStreamAsAttribute private final Operation operation;
  private final Collection<String> selectors;
  @XStreamAsAttribute private final String specifier; // Can be null
  @XStreamAsAttribute private final ModifierType type;
  @XStreamAsAttribute private final String uiName;

  /**
   * Creates a new modifier.
   *
   * @param aUiName The human-readable name of the modifier.
   * @param aKeyName The MWO enumeration name of this modifier.
   * @param aOperation The {@link Operation} to perform.
   * @param aSelectors A {@link List} of selectors, used to see if this modifier is applied to a
   *     given {@link Attribute}.
   * @param aSpecifier The attribute of the selected datum to modify, may be <code>null</code> if
   *     the attribute is implicitly understood from the context.
   * @param aValueType The type of value (positive good, negative good, indeterminate) that this
   *     {@link ModifierDescription} represents.
   */
  public ModifierDescription(
      String aUiName,
      String aKeyName,
      Operation aOperation,
      Collection<String> aSelectors,
      String aSpecifier,
      ModifierType aValueType) {
    uiName = aUiName;
    mwoKey = canonizeIdentifier(Objects.requireNonNull(aKeyName));
    operation = aOperation;
    selectors =
        aSelectors.stream()
            .map(ModifierDescription::canonizeIdentifier)
            .collect(Collectors.toSet());
    specifier = canonizeIdentifier(aSpecifier);
    type = aValueType;
  }

  public static String canonizeIdentifier(String aString) {
    if (aString != null && !aString.isEmpty()) {
      return aString.toLowerCase().trim();
    }
    return null;
  }

  /**
   * This method can be used before the ItemDB class is available (i.e. while building the database)
   * to determine whether a proposed selector would select any known quantity.
   *
   * @param aSelector The selector to test.
   * @param aItems A stream of items to check for selectors in.
   * @return true if the selector is known to affect something.
   */
  public static boolean isKnownSelector(String aSelector, Stream<Item> aItems) {
    if (ALL_SELECTORS.contains(aSelector)) {
      return true;
    }

    return aItems
        .filter(o -> o instanceof Weapon)
        .map(o -> (Weapon) o)
        .anyMatch(w -> w.getAliases().contains(aSelector));
  }

  /**
   * This method checks if a specifier affects an attribute that LSML knows about. This is mostly
   * useful for verification while parsing the game data files.
   *
   * @param aSpecifier the specifier to test.
   * @param aItems A stream of items to check for specifiers in.
   * @return true if the specifier is known to affect something.
   */
  public static boolean isKnownSpecifier(String aSpecifier, Stream<Item> aItems) {
    if (ALL_SPECIFIERS.contains(aSpecifier)) {
      return true;
    }
    return aItems
        .filter(o -> o instanceof Ammunition)
        .map(o -> (Ammunition) o)
        .anyMatch(a -> a.getQuirkSpecifier().equalsIgnoreCase(aSpecifier));
  }

  /**
   * Create a specifier for as specific component location and side.
   *
   * @param aLocation The location
   * @param aArmourSide The side
   * @return a specifier
   */
  public static String specifierFor(Location aLocation, ArmourSide aArmourSide) {
    return aLocation.shortName().toLowerCase() + (aArmourSide == ArmourSide.BACK ? "R" : "");
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
    if (obj instanceof final ModifierDescription other) {
      if (specifier == null) {
        if (other.specifier != null) {
          return false;
        }
      } else if (!specifier.equals(other.specifier)) {
        return false;
      }

      return other.uiName.equals(uiName)
          && other.mwoKey.equals(mwoKey)
          && other.operation == operation
          && other.type == type
          && selectors.containsAll(other.selectors)
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
   * @return The human-readable name of this {@link ModifierDescription}.
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

  private static List<String> uc(String... aStrings) {
    return Collections.unmodifiableList(Arrays.asList(aStrings));
  }
}
