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

import org.lisoft.lsml.model.datacache.ModifiersDB;
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
    @XStreamAsAttribute
    private final double                 value;
    private final ModifierDescription    description;

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

        if (aDescription.getSpecifier() != null && aDescription.getSpecifier().equals(ModifiersDB.SEL_WEAPON_COOLDOWN)) {
            // Ugh... PGI, PGI... why did you have to make cooldown a positive good?
            value = -aValue;
        }
        else {
            value = aValue;
        }
    }

    @Override
    public String toString() {
        return description.toString() + " " + value;
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

    /**
     * Outputs HTML to describe this quirk to an existing document. I.e. body and header tags are not emitted.
     * 
     * @param aSB
     *            The {@link StringBuilder} to send the output to.
     */
    public void describeToHtml(StringBuilder aSB) {
        aSB.append("<div>");
        aSB.append("<span style=\"color:").append(description.getModifierType().getColor(value)).append(";\">");

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

        aSB.append("</span>");
        aSB.append("</div>");
    }
}
