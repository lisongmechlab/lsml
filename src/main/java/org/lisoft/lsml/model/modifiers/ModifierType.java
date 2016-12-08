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

/**
 * Values can be categorised based on how the affect the subjective performance of a mech.
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
public enum ModifierType {
    INDETERMINATE, NEGATIVE_GOOD, POSITIVE_GOOD;

    /**
     * @param aContext
     *            The string to convert.
     * @return A {@link ModifierType}.
     */
    public static ModifierType fromMwo(String aContext) {
        final String canon = aContext.toLowerCase();
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