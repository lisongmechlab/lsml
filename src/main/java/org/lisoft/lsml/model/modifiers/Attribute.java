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

import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import com.thoughtworks.xstream.annotations.XStreamAsAttribute;

/**
 * A generic attribute of "some thing" that can be affected by {@link Modifier}s.
 *
 * @author Li Song
 */
public class Attribute {
    @XStreamAsAttribute
    private final String specifier;
    @XStreamAsAttribute
    private double baseValue;
    private final Collection<String> selectors;

    /**
     * Creates a new attribute with a <code>null</code> specifier.
     *
     * @param aBaseValue
     *            The base value of the attribute.
     * @param aSelectors
     *            The list of selectors that can be matched to modifiers.
     */
    public Attribute(double aBaseValue, Collection<String> aSelectors) {
        this(aBaseValue, aSelectors, null);
    }

    /**
     * Creates a new attribute.
     *
     * @param aBaseValue
     *            The base value of the attribute.
     * @param aSelectors
     *            The list of selectors that can be matched to modifiers.
     * @param aSpecifier
     *            The name of the attribute, or <code>null</code> if the attribute is implicitly understood from the
     *            selector(s). Must not be non-null and empty.
     */
    public Attribute(double aBaseValue, Collection<String> aSelectors, String aSpecifier) {
        specifier = ModifierDescription.canonizeIdentifier(aSpecifier);
        baseValue = aBaseValue;
        selectors = aSelectors.stream().map(s -> ModifierDescription.canonizeIdentifier(s)).collect(Collectors.toSet());
    }

    @Override
    public boolean equals(Object aObj) {
        if (!(aObj instanceof Attribute)) {
            return false;
        }
        final Attribute that = (Attribute) aObj;
        return this.baseValue == that.baseValue && this.specifier.equals(that.specifier)
                && this.selectors.equals(that.selectors);
    }

    /**
     * @return The base value of this {@link Attribute}.
     */
    public double getBaseValue() {
        return baseValue;
    }

    /**
     * @return The {@link List} of selectors for this attribute.
     */
    public Collection<String> getSelectors() {
        return Collections.unmodifiableCollection(selectors);
    }

    /**
     * @return The name of this attribute. May be <code>null</code> if the name is implicitly understood from the
     *         selector(s). If it is non-<code>null</code> it is not empty.
     */
    public String getSpecifier() {
        return specifier;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + Double.hashCode(baseValue);
        result = prime * result + (selectors == null ? 0 : selectors.hashCode());
        result = prime * result + (specifier == null ? 0 : specifier.hashCode());
        return result;
    }

    /**
     * Changes the base value of this modifier.
     *
     * @param aAmount
     *            The new base value.
     */
    public void setBaseValue(double aAmount) {
        baseValue = aAmount;
    }

    @Override
    public String toString() {
        return Double.toString(baseValue);
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
            for (final Modifier modifier : aModifiers) {
                if (modifier.getDescription().affects(this)) {
                    final Operation op = modifier.getDescription().getOperation();
                    switch (op) {
                        case ADD:
                            additive += modifier.getValue();
                            break;
                        case MUL:
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
