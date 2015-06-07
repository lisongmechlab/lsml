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

import com.thoughtworks.xstream.annotations.XStreamAsAttribute;

/**
 * This class describes a template for a generic modifier that can be applied to an {@link Attribute}.
 * 
 * @author Emily Björk
 */
public class ModifierDescription {
    public static enum Operation {
        ADDITIVE, MULTIPLICATIVE;

        public static Operation fromString(String aString) {
            String canon = aString.toLowerCase();
            if (canon.contains("mult") || aString.contains("*")) {
                return MULTIPLICATIVE;
            }
            else if (canon.contains("add") || aString.contains("+")) {
                return ADDITIVE;
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
                case ADDITIVE:
                    return "add";
                case MULTIPLICATIVE:
                    return "mult";
                default:
                    throw new RuntimeException("Unknown modifier!");
            }
        }
    }

    public static enum ValueType {
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
         * @return A {@link ValueType}.
         */
        public static ValueType fromMwo(String aContext) {
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
    private final Operation          op;
    private final Collection<String> selectors;
    @XStreamAsAttribute
    private final String             attribute; // Can be null
    @XStreamAsAttribute
    private final ValueType          valueType;
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
            String aAttribute, ValueType aValueType) {
        uiName = aUiName;
        mwoKey = canonizeName(aKeyName);
        op = aOperation;
        selectors = new ArrayList<>();
        for (String selector : aSelectors) {
            selectors.add(canonizeName(selector));
        }
        attribute = canonizeName(aAttribute);

        if (attribute != null && attribute.equals(ModifiersDB.SEL_WEAPON_COOLDOWN)) {
            // Ugh... PGI, PGI... why did you have to make cooldown a positive good?
            valueType = ValueType.NEGATIVE_GOOD;
        }
        else {
            valueType = aValueType;
        }
    }

    public ModifierDescription(String aUiName, String aKeyName, Operation aOperation, String aSelector,
            String aAttribute, ValueType aValueType) {
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
        return op;
    }

    /**
     * Checks if this {@link ModifierDescription} affects the given {@link Attribute}.
     * 
     * @param aAttribute
     *            The {@link Attribute} to test.
     * @return <code>true</code> if the attribute is affected, false otherwise.
     */
    public boolean affects(Attribute aAttribute) {
        if (attribute == null) {
            if (aAttribute.getName() != null)
                return false;
        }
        else {
            if (aAttribute.getName() == null || !aAttribute.getName().equals(attribute))
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
     * @return The {@link ValueType} of this {@link ModifierDescription}.
     */
    public ValueType getValueType() {
        return valueType;
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
        return attribute;
    }
}
