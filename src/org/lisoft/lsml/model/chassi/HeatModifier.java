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
 * This interface allows for querying of heat effects on mech or environment.
 * 
 * @author Li Song
 */
public interface HeatModifier {
    /**
     * Calculates how much additional heat (or cooling) should be added to the environmental factor of a map.
     * 
     * @param aEnvironmentHeat
     *            The current heat from the environment.
     * @return An additional heat that should be added to the environmental effect (may be negative or positive).
     */
    public double extraEnvironmentHeat(double aEnvironmentHeat);

    /**
     * Calculates how much extra heat dissipation should be given to the mech.
     * 
     * @param aHeat
     *            The base (unmodified) heat dissipation.
     * @return An additional heat dissipation that should be added to the current heat dissipation (may be negative or
     *         positive).
     */
    public double extraHeatDissipation(double aHeat);

    /**
     * Calculates how much extra heat generation should be given to the mech.
     * 
     * @param aHeat
     *            The base (unmodified) heat generation.
     * @return An additional heat generation that should be added to the current heat generation (may be negative or
     *         positive).
     */
    public double extraHeatGeneration(double aHeat);

    /**
     * Calculates how much extra heat capacity should be given to the mech.
     * 
     * @param aHeat
     *            The base (unmodified) heat capacity.
     * @return An additional heat capacity that should be added to the current heat capacity (may be negative or
     *         positive).
     */
    public double extraHeatCapacity(double aHeat);
}
