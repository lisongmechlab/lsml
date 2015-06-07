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
package org.lisoft.lsml.model.chassi;

import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.lisoft.lsml.model.modifiers.Modifier;

/**
 * This movement profile gives the minimum possible value of a combination of different movement profiles.
 * <p>
 * More specifically, a base profile is defined (typically chassis standard values) and a number of alternatives are
 * given in groups. For each group the lowest value among the alternatives is taken and added to the base profile.
 * <p>
 * Movement archetype is always that of the base profile.
 * 
 * @author Emily Björk
 */
public class MinMovementProfile extends ModifiedProfileBase {

    private MovementProfile                  base;
    private List<List<Collection<Modifier>>> groups;

    public MinMovementProfile(MovementProfile aBase, List<List<Collection<Modifier>>> aGroups) {
        base = aBase;
        groups = aGroups;
    }

    @Override
    protected double calc(String aMethodName, Collection<Modifier> aExtraModifiers) {
        try {
            double baseValue = (double) base.getClass().getMethod(aMethodName, Collection.class)
                    .invoke(base, aExtraModifiers);
            double ans = baseValue;
            for (List<Collection<Modifier>> group : groups) {
                double min = Double.POSITIVE_INFINITY;
                for (Collection<Modifier> quirks : group) {
                    List<Modifier> fullQuirks = new ArrayList<>(quirks);
                    if (aExtraModifiers != null) {
                        fullQuirks.addAll(aExtraModifiers);
                    }
                    double value = (double) base.getClass().getMethod(aMethodName, Collection.class)
                            .invoke(base, fullQuirks);
                    min = Math.min(value - baseValue, min);
                }
                if (min != Double.POSITIVE_INFINITY)
                    ans += min;
            }
            return ans;
        }
        catch (IllegalAccessException | IllegalArgumentException | InvocationTargetException | NoSuchMethodException
                | SecurityException e) {
            throw new IllegalArgumentException();
        }
    }

    @Override
    public MovementArchetype getMovementArchetype() {
        return base.getMovementArchetype();
    }
}
