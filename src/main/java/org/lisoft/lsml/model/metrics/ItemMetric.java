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
package org.lisoft.lsml.model.metrics;

import org.lisoft.lsml.model.item.Item;

/**
 * This is an other type of metric that performs calculations for a specific item on a loadout or part.
 * 
 * @author Emily Björk
 */
public interface ItemMetric {
    /**
     * Calculates the value of the metric. May employ caching but the caching must be transparent.
     * 
     * @param aItem
     *            The {@link Item} to calculate the metric for.
     * @return The value of the metric.
     */
    public double calculate(Item aItem);
}
