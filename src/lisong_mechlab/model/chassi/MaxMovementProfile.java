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
package lisong_mechlab.model.chassi;

import java.lang.reflect.InvocationTargetException;
import java.util.List;

/**
 * This movement profile gives the maximum possible value of a combination of different movement profiles.
 * <p>
 * More specifically, a base profile is defined (typically chassis standard values) and a number of alternatives are
 * given in groups. For each group the highest value among the alternatives is taken and added to the base profile.
 * <p>
 * Movement archetype is always that of the base profile.
 * 
 * @author Emily Björk
 */
public class MaxMovementProfile extends ModifiedProfileBase {

    private MovementProfile    base;
    private List<List<Quirks>> groups;

    public MaxMovementProfile(MovementProfile aBase, List<List<Quirks>> aGroups) {
        base = aBase;
        groups = aGroups;
    }

    @Override
    protected double calc(String aMethodName) {
        try {
            double baseValue = (double) base.getClass().getMethod(aMethodName).invoke(base);
            double ans = baseValue;
            for (List<Quirks> group : groups) {
                double max = Double.NEGATIVE_INFINITY;
                for (MovementModifier profile : group) {
                    max = Math.max(max,
                            (double) profile.getClass().getMethod(aMethodName.replace("get", "extra"), double.class)
                                    .invoke(profile, baseValue));
                }
                if (max != Double.NEGATIVE_INFINITY)
                    ans += max;
            }
            return ans;
        }
        catch (IllegalAccessException | IllegalArgumentException | InvocationTargetException | NoSuchMethodException
                | SecurityException e) {
            throw new IllegalArgumentException();
        }
    }

    @Override
    public double getMaxMovementSpeed() {
        return base.getMaxMovementSpeed();
    }

    @Override
    public double getReverseSpeedMultiplier() {
        return base.getReverseSpeedMultiplier();
    }

    @Override
    public MovementArchetype getMovementArchetype() {
        return base.getMovementArchetype();
    }
}
