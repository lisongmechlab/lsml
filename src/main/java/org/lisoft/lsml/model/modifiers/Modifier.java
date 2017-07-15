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

import java.text.DecimalFormat;

import com.thoughtworks.xstream.annotations.XStreamAsAttribute;

/**
 * This class is a concrete instance of a {@link ModifierDescription} with a value.
 *
 * @author Li Song
 *
 */
public class Modifier {
    protected final static DecimalFormat FORMAT = new DecimalFormat("###.#");
    private final ModifierDescription description;
    @XStreamAsAttribute
    private final double value;

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

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (!(obj instanceof Modifier)) {
            return false;
        }
        final Modifier other = (Modifier) obj;
        if (description == null) {
            if (other.description != null) {
                return false;
            }
        }
        else if (!description.equals(other.description)) {
            return false;
        }
        if (Double.doubleToLongBits(value) != Double.doubleToLongBits(other.value)) {
            return false;
        }
        return true;
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
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + (description == null ? 0 : description.hashCode());
        long temp;
        temp = Double.doubleToLongBits(value);
        result = prime * result + (int) (temp ^ temp >>> 32);
        return result;
    }

    @Override
    public String toString() {
        final StringBuilder aSB = new StringBuilder();
        aSB.append(description.getUiName()).append(": ");
        if (value > 0) {
            aSB.append("+");
        }
        if (description.getOperation() == Operation.MUL) {
            aSB.append(FORMAT.format(getTransformedValue() * 100)).append("%");
        }
        else {
            aSB.append(FORMAT.format(getTransformedValue()));
        }
        return aSB.toString();
    }

    private double getTransformedValue() {
        final String key = getDescription().getKey();
        if (key != null && key.contains("_" + ModifierDescription.SPEC_WEAPON_ROF + "_")) {
            // The ROF quirks are "special". The quirk values are converted to cool down so that they can apply
            // to all calculations without modification. But the UI string is left as "RATE OF FIRE" so before
            // display we need to convert back to ROF values to have the display be consistent with MWO.
            //
            // CD_q = CD * (1+x)
            // ROF_q = ROF * (1+y)
            // CD_q = 1 / ROF_q
            // (1+y) = 1 / (1+x) <=> y = -x/(1+x)
            return -value / (1 + value);
        }
        return value;
    }
}
