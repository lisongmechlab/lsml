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

import java.text.DecimalFormat;

import org.lisoft.lsml.model.modifiers.ModifierDescription.Operation;

import com.thoughtworks.xstream.annotations.XStreamAsAttribute;

/**
 * This class is a concrete instance of a {@link ModifierDescription} with a value.
 * 
 * @author Emily Björk
 *
 */
public class Modifier {
    protected final static DecimalFormat FORMAT = new DecimalFormat("###.#");
    private final ModifierDescription    description;
    @XStreamAsAttribute
    private final double                 value;

    /**
     * Creates a new modifier instance.
     * 
     * @param aDescription
     *            The description of the {@link Modifier}.
     * @param aValue
     *            The actual modification value.
     */
    public Modifier(ModifierDescription aDescription, double aValue) {
        description = aDescription;
        value = aValue;
    }

    /**
     * @return The {@link ModifierDescription} for this {@link Modifier}.
     */
    public ModifierDescription getDescription() {
        return description;
    }

    /**
     * @return The value of this {@link Modifier}.
     */
    public double getValue() {
        return value;
    }

    @Override
    public String toString() {
        StringBuilder aSB = new StringBuilder();
        aSB.append(description.getUiName()).append(": ");
        if (value > 0) {
            aSB.append("+");
        }
        if (description.getOperation() == Operation.MUL) {
            aSB.append(FORMAT.format(value * 100)).append("%");
        }
        else {
            aSB.append(FORMAT.format(value));
        }
        return aSB.toString();
    }
}
