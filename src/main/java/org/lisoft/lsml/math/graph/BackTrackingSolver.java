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
package org.lisoft.lsml.math.graph;

import java.util.Optional;

/**
 * This class implements a general back-tracking algorithm for Constraint Satisfaction Problems (CSP).
 * 
 * @author Li Song
 * @param <T>
 *            The problem type to solve.
 */
public class BackTrackingSolver<T extends PartialCandidate<T>> {

    /**
     * With the {@link PartialCandidate} <code>aRoot</code> as root of the potential search tree, will search for the
     * first {@link PartialCandidate} that returns <code>true</code> from {@link PartialCandidate#accept()}.
     * 
     * @param aRoot
     *            The root of the search tree.
     * @return An optional {@link PartialCandidate} if a solution exists, {@link Optional#empty()} otherwise.
     */
    public Optional<T> solveOne(T aRoot) {
        if (aRoot.reject()) {
            return Optional.empty();
        }

        if (aRoot.accept()) {
            return Optional.of(aRoot);
        }

        Optional<T> child = aRoot.first();
        while (child.isPresent()) {
            Optional<T> solution = solveOne(child.get());
            if (solution.isPresent()) {
                return solution;
            }

            child = child.get().next();
        }
        return Optional.empty();
    }
}
