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
package org.lisoft.mwo_data.mwo_parser;

import java.util.*;
import org.lisoft.mwo_data.modifiers.Modifier;
import org.lisoft.mwo_data.modifiers.ModifierDescription;
import org.lisoft.mwo_data.modifiers.ModifierType;
import org.lisoft.mwo_data.modifiers.Operation;

/**
 * This class consolidates all logic that deals with quirks in an effort to unify the modifiers and
 * quirks that are slightly different in the game data files.
 *
 * @author Li Song
 */
@SuppressWarnings("SpellCheckingInspection") // MWO identifiers are spelled the way they are.
class QuirkModifiers {
  static final String SPECIFIC_ITEM_PREFIX = "key#";
  private static final String SUFFIX_COOL_DOWN = " (COOLDOWN)";
  private static final String SUFFIX_DAMAGE = " (DAMAGE)";
  private static final String SUFFIX_PROJ_SPEED = " (SPEED)";
  private static final String SUFFIX_RANGE = " (RANGE)";
  private static final String SUFFIX_TAG_DURATION = " (DURATION)";

  /**
   * Given an {@link XMLQuirk} (typically from chassis or omnipod) generates a matching collection
   * of {@link Modifier} s.
   *
   * @param aQuirk The quirk to generate modifiers from.
   * @param aPartialDatabase A {@link PartialDatabase} with anything that has been parsed so far.
   *     Used to construct cross-references of items instead of raw item IDs.
   * @return A {@link Collection} of {@link Modifier}.
   */
  public static Modifier createModifier(XMLQuirk aQuirk, PartialDatabase aPartialDatabase) {
    final String key = ModifierDescription.canonizeIdentifier(aQuirk.name);
    final ModifierDescription desc =
        aPartialDatabase.getOrCreateModifierDescription(
            key, k -> createModifierDescription(k, aPartialDatabase));
    return canoniseModifier(new Modifier(desc, aQuirk.value));
  }

  /**
   * Creates a {@link Collection} of {@link Modifier} for the given parameters.
   *
   * @param aName The UI name string of the modifier
   * @param aOperation The operation to be performed as a text string.
   * @param aCompatibleWeapons A comma separated list of weapon KEY values that the modifiers should
   *     apply to.
   * @param aCoolDown A cool down modifier value 0 if no modifier is present. A value of 1.0
   *     indicates no bonus.
   * @param aRange A range modifier value 0 if no modifier is present. A value of 1.0 indicates no
   *     bonus.
   * @param aTAGDuration A tag duration modifier
   * @param aSpeed A projectile speed modifier
   * @param aDamage A damage modifier
   * @return A {@link Collection} of {@link Modifier}.
   */
  public static Collection<Modifier> createModifiers(
      String aName,
      String aOperation,
      String aCompatibleWeapons,
      double aCoolDown,
      double aRange,
      double aSpeed,
      double aTAGDuration,
      double aDamage) {
    final Operation op = Operation.fromString(aOperation);
    final List<String> selectors = Arrays.asList(aCompatibleWeapons.split("\\s*,\\s*"));
    selectors.replaceAll(s -> SPECIFIC_ITEM_PREFIX + s);

    String name = aName.replace("TARGETING", "T.");
    name = name.replace("COMP.", "C.");

    final List<Modifier> modifiers = new ArrayList<>();
    if (aCoolDown != 0) {
      final ModifierDescription desc =
          new ModifierDescription(
              name + SUFFIX_COOL_DOWN,
              makeKey(name, ModifierDescription.SPEC_WEAPON_COOL_DOWN, op),
              op,
              selectors,
              ModifierDescription.SPEC_WEAPON_COOL_DOWN,
              ModifierType.NEGATIVE_GOOD);
      modifiers.add(canoniseModifier(new Modifier(desc, 1.0 - aCoolDown)));
    }
    if (aRange != 0) {
      final ModifierDescription desc =
          new ModifierDescription(
              name + SUFFIX_RANGE,
              makeKey(name, ModifierDescription.SPEC_WEAPON_RANGE, Operation.MUL),
              Operation.MUL,
              selectors,
              ModifierDescription.SPEC_WEAPON_RANGE,
              ModifierType.POSITIVE_GOOD);
      modifiers.add(canoniseModifier(new Modifier(desc, aRange - 1.0)));
    }
    if (aSpeed != 0) {
      final ModifierDescription desc =
          new ModifierDescription(
              name + SUFFIX_PROJ_SPEED,
              makeKey(name, ModifierDescription.SPEC_WEAPON_PROJECTILE_SPEED, op),
              op,
              selectors,
              ModifierDescription.SPEC_WEAPON_PROJECTILE_SPEED,
              ModifierType.POSITIVE_GOOD);
      modifiers.add(canoniseModifier(new Modifier(desc, aSpeed - 1.0)));
    }
    if (aTAGDuration != 0) {
      final ModifierDescription desc =
          new ModifierDescription(
              name + SUFFIX_TAG_DURATION,
              makeKey(name, ModifierDescription.SPEC_WEAPON_TAG_DURATION, op),
              op,
              selectors,
              ModifierDescription.SPEC_WEAPON_TAG_DURATION,
              ModifierType.POSITIVE_GOOD);
      modifiers.add(canoniseModifier(new Modifier(desc, aTAGDuration - 1.0)));
    }
    if (aDamage != 0) {
      final ModifierDescription desc =
          new ModifierDescription(
              name + SUFFIX_DAMAGE,
              makeKey(name, ModifierDescription.SPEC_WEAPON_DAMAGE, op),
              op,
              selectors,
              ModifierDescription.SPEC_WEAPON_DAMAGE,
              ModifierType.POSITIVE_GOOD);
      modifiers.add(canoniseModifier(new Modifier(desc, aDamage - 1.0)));
    }
    return modifiers;
  }

  /**
   * Transforms a {@link Modifier} as to align with the LSML way of doing things. For example
   * joining ROF and cool down attributes and having cool down be negative good.
   *
   * @param aModifier A {@link Modifier} to transform.
   * @return A {@link Collection} of {@link Modifier}s that are LSML compatible.
   */
  private static Modifier canoniseModifier(Modifier aModifier) {
    final ModifierDescription description = aModifier.getDescription();
    final String key = description.getKey();
    if (null != key && key.contains("_" + ModifierDescription.SPEC_WEAPON_ROF + "_")) {
      if (description.getOperation() == Operation.ADD) {
        throw new RuntimeException("Cannot handle additive ROF quirks yet!");
      }
      final double oldValue = aModifier.getValue();
      final double newValue = -oldValue / (1 + oldValue); // Transform ROF to CD
      return new Modifier(description, newValue);
    }
    return aModifier;
  }

  private static ModifierDescription createModifierDescription(
      String aKey, PartialDatabase aPartialDatabase) {
    // Example quirk tags
    // <Quirk name="internalresist_rt_additive" value="7"/>
    // <Quirk name="arm_pitchspeed_multiplier" value="0.05"/>
    // <Quirk name="arm_yawspeed_multiplier" value="0.05"/>
    // <Quirk name="ammocapacity_cstreak_srm_additive" value="70"/>
    final String[] quirkParts = aKey.split("_");

    final String selector;
    String specifier;
    final Operation op;
    if (quirkParts.length == 2) {
      selector = quirkParts[0];
      specifier = null;
      op = Operation.fromString(quirkParts[1]);
    } else if (quirkParts.length == 3) {
      selector = quirkParts[0];
      specifier = quirkParts[1];
      op = Operation.fromString(quirkParts[2]);
    } else if (quirkParts.length == 4) {
      selector = quirkParts[0];
      specifier = quirkParts[1] + quirkParts[2];
      op = Operation.fromString(quirkParts[3]);
    } else {
      throw new IllegalArgumentException("Didn't understand quirk: " + aKey);
    }

    if (!ModifierDescription.isKnownSelector(selector, aPartialDatabase.allItems())) {
      System.err.println("Unknown selector: " + selector + " in quirk: " + aKey);
    }

    if (specifier != null
        && !ModifierDescription.isKnownSpecifier(specifier, aPartialDatabase.allItems())) {
      System.err.println("Unknown spec: " + specifier + " in quirk: " + aKey);
    }

    String localization = null;
    try {
      localization = aPartialDatabase.localise("qrk_" + aKey).toUpperCase();
    } catch (IllegalArgumentException e) {
      System.err.println(e.getMessage());
    }
    final String uiName;
    if (localization != null) {
      uiName = shortenName(localization);
    } else {
      uiName = aKey;
    }
    ModifierType type = heuristicType(aKey);

    if (ModifierDescription.SPEC_WEAPON_ROF.equals(specifier)) {
      // ROF quirk should affect cool down as we normalize ROF to cool down in weapon parsing.
      // See matching code in fromQuirk().
      specifier = ModifierDescription.SPEC_WEAPON_COOL_DOWN;
      // Technically ROF is positive good, but we will convert the value to a CD value so the
      // converted value will be negative good. The UI name will still say ROF and we will
      // have a special case in rendering of ROF quirks based on the key value that will convert
      // the CD value back to ROF for display. Whatever we do here must match the visualization
      // of the modifier.
      type = ModifierType.NEGATIVE_GOOD;
    }
    return new ModifierDescription(
        uiName, aKey, op, Collections.singletonList(selector), specifier, type);
  }

  /**
   * Attempts to heuristically determine the {@link ModifierType} for a quirk key.
   *
   * @param aKey They key to attempt to determine the {@link ModifierType} for. Must have been
   *     canonized.
   * @return A {@link ModifierType}.
   */
  private static ModifierType heuristicType(String aKey) {
    // Most quirk types are Positive Good. Check for known neutral and
    // negative good and assume the rest are positive good.
    if (aKey.startsWith("externalheat")) {
      return ModifierType.INDETERMINATE;
    } else if (aKey.contains("receiving")
        || aKey.contains("falldamage_")
        || aKey.contains("_jamduration_")
        || aKey.contains("overheatdamage")
        || aKey.contains("_heat_")
        || aKey.contains("_spread_")
        || aKey.contains("_jamchance_")
        || aKey.contains("_jamtime_")
        || aKey.contains("_duration_")
        || aKey.contains("_cooldown_")
        || aKey.contains("_jamrampdownduration_")) {
      return ModifierType.NEGATIVE_GOOD;
    }
    return ModifierType.POSITIVE_GOOD;
  }

  private static String makeKey(String aSelector, String aSpecifier, Operation aOp) {
    return aSelector + "_" + (aSpecifier == null ? "" : aSpecifier + "_") + aOp.toString();
  }

  private static String shortenName(String aName) {
    String name = aName.replace("TARGETING", "T.");
    name = name.replace("LARGE ", "L");
    name = name.replace("MEDIUM ", "M");
    name = name.replace("SMALL ", "S");
    name = name.replace("PULSE ", "P");
    name = name.replace("CLAN ", "C-");
    name = name.replace("AMMO CAPACITY", "AMMO/TON");
    name = name.replace("GENERATION", "GEN.");
    if (!name.startsWith("LASER")) {
      name = name.replace("LASER", "LAS");
    }
    return name;
  }
}
