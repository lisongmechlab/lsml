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
 * Users of the back tracking solver must implement this interface to describe the problem.
 * <p>
 * The functions {@link #first()} and {@link #next()} together define to search space of the problem. You can think of
 * the search space as a tree structure where {@link #first()} returns the first child of the current node and
 * {@link #next()} returns the right (or left) sibling of the current node. They must be chosen so that every solution
 * of the problem exists in the tree that they describe and that no partial candidate occurrs more than once.
 *
 * @param <U> CRTP Parameter
 * @author Li Song
 */
public interface PartialCandidate<U extends PartialCandidate<U>> {
    /**
     * Checks whether or not this partial candidate solves the CSP.
     *
     * @return <code>true</code> if this {@link PartialCandidate} is a solution to the problem.
     */
    boolean accept();

    /**
     * Generates the first extension of this partial candidate. For example by choosing a value for the next unknown.
     * <p>
     * Together with {@link #next()} defines the search space of the problem.
     *
     * @return An {@link Optional} new {@link PartialCandidate}.
     */
    Optional<U> first();

    /**
     * Generates the next alternative extension of this partial candidate. For example by changing the value of the last
     * chosen unknown.
     * <p>
     * Together with {@link #first()} defines the search space of the problem.
     *
     * @return An {@link Optional} new {@link PartialCandidate}.
     */
    Optional<U> next();

    /**
     * Checks whether or not this partial candidate can ever become a solution to the CSP being solved.
     * <p>
     * Note: This must not return true if as long as there is any chance that this {@link PartialCandidate} can in some
     * extension satisfy the problem, or the solution may be missed by the solver.
     *
     * @return <code>true</code> if and only if this {@link PartialCandidate} can never result in an acceptable
     * solution.
     */
    boolean reject();
}