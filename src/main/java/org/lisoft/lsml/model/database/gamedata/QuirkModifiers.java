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
package org.lisoft.lsml.model.database.gamedata;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.lisoft.lsml.model.modifiers.Modifier;
import org.lisoft.lsml.model.modifiers.ModifierDescription;
import org.lisoft.lsml.model.modifiers.ModifierType;
import org.lisoft.lsml.model.modifiers.Operation;

/**
 * This class consolidates all logic that deals with quirks in an effort to unify the modifiers and quirks that are
 * slightly different in the game data files.
 *
 * @author Emily Björk
 */
public class QuirkModifiers {
    public static final String SPECIFIC_ITEM_PREFIX = "key#";
    public static final String SPEC_ROF = "rof";
    private static final String SPEC_RANGE = "range";
    private static final String SUFFIC_TAG_DURATION = " (DURATION)";
    private static final String SUFFIX_DAMAGE = " (DAMAGE)";
    private static final String SUFFIX_LONG = " (LONG)";
    private static final String SUFFIX_MAX = " (MAX)";
    private static final String SUFFIX_PROJ_SPEED = " (SPEED)";

    /**
     * Performs necessary transformations on {@link ModifierDescription}s as to align the various systems in MWO with
     * the unified modifier system in LSML.
     *
     * @param aDescriptions
     *            A {@link Collection} of {@link ModifierDescription}s to transform.
     * @return A {@link Collection} of canonised {@link ModifierDescription}s.
     */
    static public Collection<ModifierDescription> canoniseModifierDescriptions(
            Collection<ModifierDescription> aDescriptions) {
        return aDescriptions.stream().map(aDescription -> {
            // Convert name to something usable
            String name = aDescription.getUiName().replace("TARGETING", "T.");
            name = name.replace("LARGE ", "L");
            name = name.replace("MEDIUM ", "M");
            name = name.replace("SMALL ", "S");
            name = name.replace("PULSE ", "P");
            if (!name.startsWith("LASER")) {
                name = name.replace("LASER", "LAS");
            }

            final Collection<String> selectors = aDescription.getSelectors();
            final String key = aDescription.getKey();
            final Operation op = aDescription.getOperation();
            String spec = aDescription.getSpecifier();
            ModifierType type = aDescription.getModifierType();
            if ("velocity".equals(spec)) {
                // Dadgum pgi!
                spec = ModifierDescription.SPEC_WEAPON_PROJECTILE_SPEED;
            }

            if (SPEC_ROF.equals(spec)) {
                // ROF quirk should affect cool down as we normalise ROF to cool down in weapon parsing.
                // See matching code in fromQuirk().
                spec = ModifierDescription.SPEC_WEAPON_COOL_DOWN;
                // Technically ROF is positive good, but we will convert the value to a CD value so the converted
                // value will be negative good. The UI name will still say ROF and we will have a special case in
                // rendering of ROF quirks based on the key value that will convert the CD value back to ROF for
                // display. Whatever we do here must match the visualisation of the modifier.
                type = ModifierType.NEGATIVE_GOOD;
            }
            else if (ModifierDescription.SPEC_WEAPON_COOL_DOWN.equals(spec)) {
                // PGI specifies a reduction in cool down as a positive value, we want a negative value for reduction.
                type = ModifierType.NEGATIVE_GOOD;
            }
            else if (SPEC_RANGE.equals(spec)) {
                // We need to split the "range" specifier into "long" and "max" range.
                final String specLong = ModifierDescription.SPEC_WEAPON_RANGE_LONG;
                final String specMax = ModifierDescription.SPEC_WEAPON_RANGE_MAX;
                final String keyLong = key.replace(SPEC_RANGE, specLong);
                final String keyMax = key.replace(SPEC_RANGE, specMax);

                return Arrays.asList(
                        new ModifierDescription(name + SUFFIX_LONG, keyLong, op, selectors, specLong, type),
                        new ModifierDescription(name + SUFFIX_MAX, keyMax, op, selectors, specMax, type));
            }
            return Arrays.asList(new ModifierDescription(name, key, op, selectors, spec, type));
        }).flatMap(List::stream).collect(Collectors.toList());
    }

    /**
     * Transforms a list of {@link Modifier}s as to align with the LSML way of doing things. For example joining ROF and
     * cool down attributes and having cool down be negative good.
     *
     * @param aModifiers
     *            A {@link Collection} of {@link Modifier}s to transform.
     * @return A {@link Collection} of {@link Modifier}s that are LSML compatible.
     */
    static public Collection<Modifier> canoniseModifiers(Collection<Modifier> aModifiers) {
        return aModifiers.stream().map(aModifier -> {
            final ModifierDescription description = aModifier.getDescription();
            final String key = description.getKey();
            final String specifier = description.getSpecifier();
            if (null != key && key.contains("_" + SPEC_ROF + "_")) {
                if (description.getOperation() == Operation.ADD) {
                    throw new RuntimeException("Cannot handle additive ROF quirks yet!");
                }
                final double oldValue = aModifier.getValue();
                final double newValue = -oldValue / (1 + oldValue); // Transform ROF to CD
                return new Modifier(description, newValue);
            }
            else if (null != specifier && specifier.equals(ModifierDescription.SPEC_WEAPON_COOL_DOWN)) {
                // For some obscure reason, PGI decided to represent a reduction in cooldown as a positive value.
                return new Modifier(description, -aModifier.getValue());
            }
            return aModifier;
        }).collect(Collectors.toList());
    }

    /**
     * Reads a quirk definition as parsed from <code>"Quirks.def.xml"</code> and returns a {@link Collection} of usable
     * {@link Modifier}s.
     *
     * In the Mech Definition Files (MDF) quirks are addressed by a unique "KeyName", which typically looks something
     * like this: <code>laser_duration_multiplier</code>. The exact format is
     * <code>quirk.name_[modify.specifier_]modify.operation</code> where <code>modify.specifier</code> is optional.
     *
     * In the quirks definition file the quirks are defined in an XML structure, grouped by category and then by
     * specifier. For example:
     *
     * <pre>
     * &lt;Quirk name="TorsoAngle" loc="TORSO TURN ANGLE"&gt;
     *     &lt;Modify specifier="Pitch" operation="Additive" context="PositiveGood" loc="(PITCH)" /&gt;
     *     &lt;Modify specifier="Yaw" operation="Additive" context="PositiveGood" loc="(YAW)" /&gt;
     * &lt;/Quirk&gt;
     * </pre>
     *
     * for which <code>torsoangle_pitch_additive</code> would select the first quirk. Although identifiers for the
     * quirks exist in "TheRealLoc.xml" it is easier to use the localisation strings encoded in the quirks definition
     * file because the location keys are again slightly different from the quirk lookup keys.
     *
     *
     * @param aQuirk
     *            The quirk to parse.
     * @return A {@link Collection} of {@link Modifier}s.
     */
    static public Collection<ModifierDescription> createModifierDescription(XMLQuirkDef.Category.Quirk aQuirk) {
        return canoniseModifierDescriptions(aQuirk.modifiers.stream().map(aModifier -> {
            final String key = getKey(aQuirk.name, aModifier.specifier, aModifier.operation);
            final String uiName = getUIString(aQuirk.name, aQuirk.loc, aModifier.loc, aModifier.specifier);
            final ModifierType type = ModifierType.fromMwo(aModifier.context);
            final Operation op = Operation.fromString(aModifier.operation);
            final Collection<String> selectors = Arrays.asList(aQuirk.name);
            final String specifier = ModifierDescription.canonizeIdentifier(aModifier.specifier);
            return new ModifierDescription(uiName, key, op, selectors, specifier, type);
        }).collect(Collectors.toList()));
    }

    /**
     * Creates a {@link Collection} of {@link Modifier} for the given parameters.
     *
     * @param aName
     *            The UI name string of the modifier
     * @param aOperation
     *            The operation to be performed as a text string.
     * @param aCompatibleWeapons
     *            A comma separated list of weapon KEY values that the modifiers should apply to.
     * @param aCooldown
     *            A cool down modifier value 0 if no modifier is present. A value of 1.0 indicates no bonus.
     * @param aLongRange
     *            A long range modifier value 0 if no modifier is present. A value of 1.0 indicates no bonus.
     * @param aMaxRange
     *            A max range modifier value 0 if no modifier is present. A value of 1.0 indicates no bonus.
     * @param aTAGDuration
     *            A tag duration modifier
     * @param aSpeed
     *            A projectile speed modifier
     * @param aDamage
     *            A damage modifier
     *
     * @return A {@link Collection} of {@link Modifier}.
     */
    static public Collection<Modifier> createModifiers(String aName, String aOperation, String aCompatibleWeapons,
            double aCooldown, double aLongRange, double aMaxRange, double aSpeed, double aTAGDuration, double aDamage) {
        final Operation op = Operation.fromString(aOperation);
        final List<String> selectors = Arrays.asList(aCompatibleWeapons.split("\\s*,\\s*"));
        for (int i = 0; i < selectors.size(); i++) {
            selectors.set(i, SPECIFIC_ITEM_PREFIX + selectors.get(i));
        }

        final String name = aName.replace("TARGETING", "T.");

        final List<Modifier> modifiers = new ArrayList<>();
        if (aCooldown != 0) {
            final ModifierDescription desc = new ModifierDescription(name, null, op, selectors,
                    ModifierDescription.SPEC_WEAPON_COOL_DOWN, ModifierType.NEGATIVE_GOOD);
            modifiers.add(new Modifier(desc, 1.0 - aCooldown));
        }
        if (aLongRange != 0) {
            final ModifierDescription desc = new ModifierDescription(name + SUFFIX_LONG, null, op, selectors,
                    ModifierDescription.SPEC_WEAPON_RANGE_LONG, ModifierType.POSITIVE_GOOD);
            modifiers.add(new Modifier(desc, aLongRange - 1.0));
        }
        if (aMaxRange != 0) {
            final ModifierDescription desc = new ModifierDescription(name + SUFFIX_MAX, null, op, selectors,
                    ModifierDescription.SPEC_WEAPON_RANGE_MAX, ModifierType.POSITIVE_GOOD);
            modifiers.add(new Modifier(desc, aMaxRange - 1.0));
        }
        if (aSpeed != 0) {
            final ModifierDescription desc = new ModifierDescription(name + SUFFIX_PROJ_SPEED, null, op, selectors,
                    ModifierDescription.SPEC_WEAPON_PROJECTILE_SPEED, ModifierType.POSITIVE_GOOD);
            modifiers.add(new Modifier(desc, aSpeed - 1.0));
        }
        if (aTAGDuration != 0) {
            final ModifierDescription desc = new ModifierDescription(name + SUFFIC_TAG_DURATION, null, op, selectors,
                    ModifierDescription.SPEC_WEAPON_TAG_DURATION, ModifierType.POSITIVE_GOOD);
            modifiers.add(new Modifier(desc, aTAGDuration - 1.0));
        }
        if (aDamage != 0) {
            final ModifierDescription desc = new ModifierDescription(name + SUFFIX_DAMAGE, null, op, selectors,
                    ModifierDescription.SPEC_WEAPON_DAMAGE, ModifierType.POSITIVE_GOOD);
            modifiers.add(new Modifier(desc, aDamage - 1.0));
        }
        return canoniseModifiers(modifiers);
    }

    /**
     * Given an {@link XMLQuirk} (typically from chassis or omnipod) generates a matching collection of {@link Modifier}
     * s.
     *
     * @param aQuirk
     *            The quirk to generate modifiers from.
     * @param aModifierDescriptors
     *            A {@link Map} to get {@link ModifierDescription}s from by key.
     * @return A {@link Collection} of {@link Modifier}.
     */
    static public Collection<Modifier> createModifiers(XMLQuirk aQuirk,
            Map<String, ModifierDescription> aModifierDescriptors) {
        final String key = ModifierDescription.canonizeIdentifier(aQuirk.name);
        final String rangeQuirkKeyPart = "_" + SPEC_RANGE + "_";
        final List<Modifier> ans = new ArrayList<>();

        if (key.contains(rangeQuirkKeyPart)) {
            final String specLong = ModifierDescription.SPEC_WEAPON_RANGE_LONG;
            final String specMax = ModifierDescription.SPEC_WEAPON_RANGE_MAX;
            final XMLQuirk longQuirk = new XMLQuirk();
            final XMLQuirk maxQuirk = new XMLQuirk();
            longQuirk.name = key.replace(rangeQuirkKeyPart, "_" + specLong + "_");
            longQuirk.value = aQuirk.value;
            maxQuirk.name = key.replace(rangeQuirkKeyPart, "_" + specMax + "_");
            maxQuirk.value = aQuirk.value;
            ans.addAll(createModifiers(longQuirk, aModifierDescriptors));
            ans.addAll(createModifiers(maxQuirk, aModifierDescriptors));
        }
        else {
            final ModifierDescription description = aModifierDescriptors.get(key);
            if (null == description) {
                throw new IllegalArgumentException("Unknown qurk: " + aQuirk.name);
            }
            ans.add(new Modifier(description, aQuirk.value));
        }
        return canoniseModifiers(ans);
    }

    /**
     * Given the quirk name, specifier and operation produces a canonised quirk identifier which can be matched against
     * identifiers specified in MDF and omnipod definition files.
     *
     * See quirks.def.xmll for example data.
     *
     * @param aName
     *            The name of the quirk.
     * @param aSpecifier
     *            The specifier value of the quirk.
     * @param aOperation
     *            The operation of the quirk.
     * @return A canonised quirk identifier string.
     */
    static public String getKey(String aName, String aSpecifier, String aOperation) {
        // quirk.name_[modify.specifier]_modify.operation
        String keyName = aName;
        if (aSpecifier != null && !aSpecifier.isEmpty()) {
            keyName += "_" + aSpecifier;
        }
        keyName += "_" + aOperation;
        keyName = keyName.toLowerCase();
        return keyName;
    }

    /**
     * Computes the UI display string for the given quirk parts.
     *
     * For example <code>getQuirkUIString(null, "ARMOR STRENGTH", "(CT)", null)</code> would produce "ARMOR STRENGTH
     * (CT)".
     *
     * @param aName
     *            The raw name of the quirk (used if <code>aQuirkLoc</code> is <code>null</code>).
     * @param aQuirkLoc
     *            The localised quirk name, may be <code>null</code> in which case <code>aName</code> is used.
     * @param aModifyLoc
     *            The localised modifier string, this is preferred over <code>aModifySpecifier</code> if both are
     *            available and non-empty. May be <code>null</code> or empty string.
     * @param aModifySpecifier
     *            The raw modifier string, used if <code>aModifyLoc</code> is <code>null</code> or empty string.
     * @return A human readable display name of the quirk.
     */
    static public String getUIString(String aName, String aQuirkLoc, String aModifyLoc, String aModifySpecifier) {
        // qrk_{quirk.loctag||quirk.name}_[modify.specifier]_modify.operation
        // String uiKey = "qrk_";
        // if (quirk.loc != null && !quirk.loc.isEmpty())
        // uiKey += quirk.loc;
        // else
        // uiKey = quirk.name;
        // if (modify.specifier != null && !modify.specifier.isEmpty())
        // uiKey += "_" + modify.specifier;
        // uiKey += "_" + Operation.fromString(modify.operation).uiAbbrev();
        // uiKey = uiKey.toLowerCase();
        String uiName;
        if (aQuirkLoc != null && !aQuirkLoc.isEmpty()) {
            uiName = aQuirkLoc;
        }
        else {
            uiName = aName.toUpperCase();
        }

        if (aModifyLoc != null && !aModifyLoc.isEmpty()) {
            uiName += " " + aModifyLoc;
        }
        else if (aModifySpecifier != null && !aModifySpecifier.isEmpty()) {
            uiName += " " + aModifySpecifier.toUpperCase();
        }
        return uiName;
    }
}
