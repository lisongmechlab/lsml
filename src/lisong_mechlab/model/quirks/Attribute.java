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
package lisong_mechlab.model.quirks;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import lisong_mechlab.model.quirks.ModifierDescription.Operation;

/**
 * A generic attribute of "some thing" that can be affected by {@link Modifier}s.
 * 
 * @author Emily Björk
 */
public class Attribute {
    private final String             name;
    private final double             baseValue;
    private final Collection<String> selectors;

    /**
     * Creates a new attribute.
     * 
     * @param aBaseValue
     *            The base value of the attribute.
     * @param aSelectors
     *            The list of selectors that can be matched to modifiers.
     * @param aAttributeName
     *            The name of the attribute, or <code>null</code> if the attribute is implicitly understood from the
     *            selector(s). Must not be non-null and empty.
     */
    public Attribute(double aBaseValue, Collection<String> aSelectors, String aAttributeName) {
        name = ModifierDescription.canonizeName(aAttributeName);
        baseValue = aBaseValue;
        List<String> tmpSelectors = new ArrayList<>();
        for (String selector : aSelectors) {
            tmpSelectors.add(ModifierDescription.canonizeName(selector));
        }
        selectors = Collections.unmodifiableList(tmpSelectors);
    }

    /**
     * Creates a new attribute.
     * 
     * @param aBaseValue
     *            The base value of the attribute.
     * @param aSelector
     *            The selector that can be matched to modifiers.
     * @param aAttributeName
     *            The name of the attribute, or <code>null</code> if the attribute is implicitly understood from the
     *            selector(s). Must not be non-null and empty.
     */
    public Attribute(double aBaseValue, String aSelector, String aAttributeName) {
        this(aBaseValue, Arrays.asList(aSelector), aAttributeName);
    }

    /**
     * Creates a new attribute with a null name.
     * 
     * @param aBaseValue
     *            The base value of the attribute.
     * @param aSelector
     *            The selector that can be matched to modifiers.
     */
    public Attribute(double aBaseValue, String aSelector) {
        this(aBaseValue, aSelector, null);
    }

    /**
     * @return The {@link List} of selectors for this attribute.
     */
    public Collection<String> getSelectors() {
        return selectors;
    }

    /**
     * @return The name of this attribute. May be <code>null</code> if the name is implicitly understood from the
     *         selector(s). If it is non-<code>null</code> it is not empty.
     */
    public String getName() {
        return name;
    }

    /**
     * @param aModifiers
     *            A {@link Collection} of {@link Modifier} that should be applied (if applicable) to this attribute.
     * @return The value of this {@link Attribute} after applying the the {@link Modifier}s that affect this attribute
     *         from the given list.
     */
    public double value(Collection<Modifier> aModifiers) {
        double additive = 0.0;
        double multiplicative = 1.0;
        if (aModifiers != null) {
            for (Modifier modifier : aModifiers) {
                if (modifier.getDescription().affects(this)) {
                    Operation op = modifier.getDescription().getOperation();
                    switch (op) {
                        case ADDITIVE:
                            additive += modifier.getValue();
                            break;
                        case MULTIPLICATIVE:
                            multiplicative += modifier.getValue();
                            break;
                        default:
                            throw new IllegalArgumentException("Unhandled operation: " + op);
                    }
                }
            }
        }
        return (baseValue + additive) * multiplicative;
    }
}
