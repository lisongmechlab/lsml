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
package org.lisoft.lsml.model.item;

/**
 * Enumerate the different engine types available.
 *
 * @author Li Song
 */
public enum EngineType {
    XL, LE, STD;

    public int minRating() {
        if (this == XL) {
            return 100;
        } else if (this == LE) {
            return 100;
        } else if (this == STD) {
            return 60;
        }

        throw new IllegalArgumentException("Missing branch for engine min rating");
    }

    /**
     * Clamp an engine rating into an EngineType's available range.
     *
     * @param aRating
     *            The rating value to clamp.
     * @return The closest valid rating for the engine type.
     */
    public int clampRating(int aRating) {
        return aRating < minRating() ? minRating() : aRating;
    }
}
