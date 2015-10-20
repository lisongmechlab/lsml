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
package org.lisoft.lsml.model.modifiers;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import org.lisoft.lsml.model.datacache.ModifiersDB;

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
 * @author Emily Björk
 */
public class ModifierDescription {
    /**
     * This attribute defines how a modifier is applied.
     * 
     * The formula to use is: modifiedValue = (baseValue + sum(additive)) * (1.0 + sum(multiplicative)).
     * 
     * Source: Email conversation with Brian Buckton @ PGI.
     * 
     * @author Emily Björk
     */
    public static enum Operation {
        ADD, MUL;

        public static Operation fromString(String aString) {
            String canon = aString.toLowerCase();
            if (canon.contains("mult") || aString.contains("*")) {
                return MUL;
            }
            else if (canon.contains("add") || aString.contains("+")) {
                return ADD;
            }
            else {
                throw new IllegalArgumentException("Unknown operation: " + aString);
            }
        }

        /**
         * @return The name of the operation as used when looking up the modifier in the UI translation table.
         */
        public String uiAbbrev() {
            switch (this) {
                case ADD:
                    return "add";
                case MUL:
                    return "mult";
                default:
                    throw new RuntimeException("Unknown modifier!");
            }
        }
    }

    /**
     * Values can be categorized based on how the affect the subjective performance of a mech.
     * 
     * There are three classes:
     * <ul>
     * <li>Positive Good: A positive value on the quirk is desirable for the pilot.</li>
     * <li>Negative Good: A negative value on the quirk is desirable for the pilot.</li>
     * <li>Indeterminate: Value isn't unanimously desirable. For example heat transfer quirk is good for cold maps but
     * bad on hot maps, so it's indeterminate.</li>
     * </ul>
     * 
     * @author Emily Björk
     *
     */
    public static enum ModifierType {
        POSITIVE_GOOD, NEGATIVE_GOOD, INDETERMINATE;

        public String getColor(double aValue) {
            switch (this) {
                case INDETERMINATE:
                    return "black";
                case NEGATIVE_GOOD:
                    return (aValue < 0) ? "green" : "red";
                case POSITIVE_GOOD:
                    return (aValue > 0) ? "green" : "red";
                default:
                    throw new IllegalArgumentException("Unknown quirkmode!");
            }
        }

        /**
         * @param aContext
         *            The string to convert.
         * @return A {@link ModifierType}.
         */
        public static ModifierType fromMwo(String aContext) {
            String canon = aContext.toLowerCase();
            if (canon.contains("positive")) {
                return POSITIVE_GOOD;
            }
            else if (canon.contains("negat")) {
                return NEGATIVE_GOOD;
            }
            else if (canon.contains("neut")) {
                return INDETERMINATE;
            }
            else {
                throw new IllegalArgumentException("Unknown context: " + aContext);
            }
        }
    }

    @XStreamAsAttribute
    private final Operation          operation;
    private final Collection<String> selectors;
    @XStreamAsAttribute
    private final String             specifier; // Can be null
    @XStreamAsAttribute
    private final ModifierType       type;
    @XStreamAsAttribute
    private final String             uiName;
    @XStreamAsAttribute
    private final String             mwoKey;

    /**
     * Creates a new modifier.
     * 
     * @param aUiName
     *            The human readable name of the modifier.
     * @param aKeyName
     *            The MWO enum name of this modifier.
     * @param aOperation
     *            The {@link Operation} to perform.
     * @param aSelectors
     *            A {@link List} of selectors, used to see if this modifier is applied to a given {@link Attribute}.
     * @param aAttribute
     *            The attribute of the selected datum to modify, may be <code>null</code> if the attribute is implicitly
     *            understood from the context.
     * @param aValueType
     *            The type of value (positive good, negative good, indeterminate) that this {@link ModifierDescription}
     *            represents.
     */
    public ModifierDescription(String aUiName, String aKeyName, Operation aOperation, Collection<String> aSelectors,
            String aAttribute, ModifierType aValueType) {
        uiName = aUiName;
        mwoKey = canonizeName(aKeyName);
        operation = aOperation;
        selectors = new ArrayList<>();
        for (String selector : aSelectors) {
            selectors.add(canonizeName(selector));
        }
        specifier = canonizeName(aAttribute);

        if (specifier != null && specifier.equals(ModifiersDB.SEL_WEAPON_COOLDOWN)) {
            // Ugh... PGI, PGI... why did you have to make cooldown a positive good?
            type = ModifierType.NEGATIVE_GOOD;
        }
        else {
            type = aValueType;
        }
    }

    public ModifierDescription(String aUiName, String aKeyName, Operation aOperation, String aSelector,
            String aAttribute, ModifierType aValueType) {
        this(aUiName, aKeyName, aOperation, Arrays.asList(aSelector), aAttribute, aValueType);
    }

    @Override
    public String toString() {
        return uiName;
    }

    public static String canonizeName(String aString) {
        if (aString != null && !aString.isEmpty()) {
            return aString.toLowerCase();
        }
        return null;
    }

    /**
     * @return The {@link Operation} that this {@link ModifierDescription} performs.
     */
    public Operation getOperation() {
        return operation;
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
            if (aAttribute.getSpecifier() != null)
                return false;
        }
        else {
            if (aAttribute.getSpecifier() == null || !aAttribute.getSpecifier().equals(specifier))
                return false;
        }

        for (String selector : selectors) {
            for (String attributeSelector : aAttribute.getSelectors()) {
                if (selector.equals(attributeSelector)) {
                    return true;
                }
            }
        }
        return false;
    }

    /**
     * @return The {@link ModifierType} of this {@link ModifierDescription}.
     */
    public ModifierType getModifierType() {
        return type;
    }

    /**
     * @return The human readable name of this {@link ModifierDescription}.
     */
    public String getUiName() {
        return uiName;
    }

    /**
     * @return A {@link Collection} if {@link String}s with all the selectors of this modifier.
     */
    public Collection<String> getSelectors() {
        return Collections.unmodifiableCollection(selectors);
    }

    /**
     * @return The MWO key for referring to this description.
     */
    public String getKey() {
        return mwoKey;
    }

    /**
     * @return The specifier for the modifier.
     */
    public String getSpecifier() {
        return specifier;
    }
}
