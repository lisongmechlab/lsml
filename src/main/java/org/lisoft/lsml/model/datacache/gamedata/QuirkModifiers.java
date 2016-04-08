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
package org.lisoft.lsml.model.datacache.gamedata;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

import org.lisoft.lsml.model.datacache.DataCache;
import org.lisoft.lsml.model.datacache.gamedata.XMLQuirkDef.Category;
import org.lisoft.lsml.model.modifiers.Modifier;
import org.lisoft.lsml.model.modifiers.ModifierDescription;
import org.lisoft.lsml.model.modifiers.ModifierDescription.ModifierType;
import org.lisoft.lsml.model.modifiers.ModifierDescription.Operation;

/**
 * This class consolidates all logic that deals with quirks in an effort to unify the modifiers and quirks that are
 * slightly different in the game data files.
 * 
 * @author Li Song
 */
public class QuirkModifiers {
    private static final String MAX_SUFFIX = " (MAX)";
    private static final String LONG_SUFFIX = " (LONG)";
    public static final String SPECIFIC_ITEM_PREFIX = "key#";
    private static final String RANGE_QUIRK = "range";

    static public String getQuirkKey(String aName, String aSpecifier, String aOperation) {
        // quirk.name_[modify.specifier]_modify.operation
        String keyName = aName;
        if (aSpecifier != null && !aSpecifier.isEmpty())
            keyName += "_" + aSpecifier;
        keyName += "_" + aOperation;
        keyName = keyName.toLowerCase();
        return keyName;
    }

    static public String getQuirkUIString(String aName, String aQuirkLoc, String aModifyLoc, String aModifySpecifier) {
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
    static public Collection<ModifierDescription> fromQuirksDef(XMLQuirkDef.Category.Quirk aQuirk) {
        List<ModifierDescription> ans = new ArrayList<>();

        for (Category.Quirk.Modify modify : aQuirk.modifiers) {
            final String key = getQuirkKey(aQuirk.name, modify.specifier, modify.operation);
            final String uiName = getQuirkUIString(aQuirk.name, aQuirk.loc, modify.loc, modify.specifier);
            final ModifierType type = getModifierType(modify.context, modify.specifier);
            final Operation op = Operation.fromString(modify.operation);
            final Collection<String> selectors = Arrays.asList(aQuirk.name);

            modify.specifier = ModifierDescription.canonizeName(modify.specifier);

            if (RANGE_QUIRK.equals(modify.specifier)) {
                // We need to split the "range" specifier into "long" and "max" range.
                final String specLong = ModifierDescription.SPEC_WEAPON_RANGE_LONG;
                final String specMax = ModifierDescription.SPEC_WEAPON_RANGE_MAX;
                final String keyLong = key.replace(RANGE_QUIRK, specLong);
                final String keyMax = key.replace(RANGE_QUIRK, specMax);

                ans.add(new ModifierDescription(uiName + LONG_SUFFIX, keyLong, op, selectors, specLong, type));
                ans.add(new ModifierDescription(uiName + MAX_SUFFIX, keyMax, op, selectors, specMax, type));
            }
            else {
                ans.add(new ModifierDescription(uiName, key, op, selectors, modify.specifier, type));
            }
        }
        return ans;
    }

    static public Collection<Modifier> fromQuirk(XMLQuirk aQuirk, DataCache aDataCache) {
        String nameLC = aQuirk.name.toLowerCase();
        String rangeQuirkKeyPart = "_" + RANGE_QUIRK + "_";
        if (nameLC.contains(rangeQuirkKeyPart)) {
            final String specLong = ModifierDescription.SPEC_WEAPON_RANGE_LONG;
            final String specMax = ModifierDescription.SPEC_WEAPON_RANGE_MAX;
            XMLQuirk longQuirk = new XMLQuirk();
            XMLQuirk maxQuirk = new XMLQuirk();
            longQuirk.name = nameLC.replace(rangeQuirkKeyPart, "_" + specLong + "_");
            longQuirk.value = aQuirk.value;
            maxQuirk.name = nameLC.replace(rangeQuirkKeyPart, "_" + specMax + "_");
            maxQuirk.value = aQuirk.value;
            List<Modifier> ans = new ArrayList<>();
            ans.addAll(fromQuirk(longQuirk, aDataCache));
            ans.addAll(fromQuirk(maxQuirk, aDataCache));
            return ans;
        }

        for (ModifierDescription description : aDataCache.getModifierDescriptions()) {
            if (description.getKey().equals(nameLC)) {
                if (ModifierDescription.SPEC_WEAPON_COOLDOWN.equals(description.getSpecifier())) {
                    aQuirk.value = -aQuirk.value; // Because PGI...
                }
                return Arrays.asList(new Modifier(description, aQuirk.value));
            }
        }
        throw new IllegalArgumentException("Unknown qurk: " + aQuirk.name);
    }

    static public Collection<Modifier> fromPilotModule(String aName, String aOperation, String aCompatibleWeapons,
            double aCooldown, double aLongRange, double aMaxRange) {
        final Operation op = Operation.fromString(aOperation);
        final List<String> selectors = Arrays.asList(aCompatibleWeapons.split(","));
        for (int i = 0; i < selectors.size(); i++) {
            selectors.set(i, SPECIFIC_ITEM_PREFIX + selectors.get(i));

        }

        List<Modifier> modifiers = new ArrayList<>();
        if (aCooldown != 0) {

            ModifierDescription cooldownDesc = new ModifierDescription(aName, null, op, selectors,
                    ModifierDescription.SPEC_WEAPON_COOLDOWN, ModifierType.NEGATIVE_GOOD);

            modifiers.add(new Modifier(cooldownDesc, -(1.0 - aCooldown)));// Negation because PGI...
        }
        if (aLongRange != 0) {
            ModifierDescription rangeLongDesc = new ModifierDescription(aName + LONG_SUFFIX, null, op, selectors,
                    ModifierDescription.SPEC_WEAPON_RANGE_LONG, ModifierType.POSITIVE_GOOD);
            ModifierDescription rangeMaxDesc = new ModifierDescription(aName + MAX_SUFFIX, null, op, selectors,
                    ModifierDescription.SPEC_WEAPON_RANGE_MAX, ModifierType.POSITIVE_GOOD);
            modifiers.add(new Modifier(rangeLongDesc, aLongRange - 1.0));
            modifiers.add(new Modifier(rangeMaxDesc, aMaxRange - 1.0));
        }
        return modifiers;

    }

    static private ModifierType getModifierType(String aContext, String aSpecifier) {
        ModifierType modifierType = ModifierType.fromMwo(aContext);
        if (ModifierDescription.SPEC_WEAPON_COOLDOWN.equalsIgnoreCase(aSpecifier)) {
            modifierType = ModifierType.NEGATIVE_GOOD; // Because PGI
        }
        return modifierType;
    }
}
