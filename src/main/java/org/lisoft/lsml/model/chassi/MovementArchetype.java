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
package org.lisoft.lsml.model.chassi;

/**
 * The different movement archetypes.
 * <p>
 * <a href="http://mwomercs.com/forums/topic/124437-new-battlemech-movement-behavior/">Reference</a>
 * </p>
 * 
 * @author Li Song
 */
public enum MovementArchetype {
    Tiny(40.0), Small(35.0), Medium(30.0), Large(25.0), Huge(20.0);

    MovementArchetype(double aSlowDownAngle) {
        slowDownDeg = aSlowDownAngle;
    }

    private final double slowDownDeg;

    /**
     * @return The maximal slope angle (in degrees) this archetype can climb.
     */
    public static double getMaxSlope() {
        return 45.0;
    }

    /**
     * @return The maximal slope angle (in degrees) after which the mech starts to slow down.
     */
    public double getSlowDownSlope() {
        return slowDownDeg;
    }

    /**
     * Calculates the slow down factor at any angle for this movement archetype.
     * 
     * @param aAngle
     *            The angle (in degrees) at which to get the slow down.
     * @return A factor where 1.0 means full speed and 0.0 means standstill.
     */
    public double getSlowDownFactor(double aAngle) {
        if (aAngle < slowDownDeg) {
            return 1.0;
        }
        else if (aAngle > getMaxSlope()) {
            return 0.0;
        }
        else {
            return (getMaxSlope() - aAngle) / (getMaxSlope() - getSlowDownSlope());
        }
    }

}
