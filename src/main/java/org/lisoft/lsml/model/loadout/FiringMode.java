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
package org.lisoft.lsml.model.loadout;

import com.thoughtworks.xstream.annotations.XStreamAlias;

/**
 * This enum represents different firing patterns for a weapon group. The firing pattern will affect sustained DPS and
 * heat values. For maximal DPS, alpha strike is assumed.
 * 
 * @author Li Song
 *
 */
@XStreamAlias("firingmode")
public enum FiringMode {
    /**
     * Assumes that all the weapons in the group are fired in an optimal pattern. Useful for calculating total sustained
     * DPS for example.
     */
    Optimal,
    /**
     * Assumes that all weapons are fired as often as possible.
     */
    AlphaStrike,
    /**
     * Assumes that all weapons are fired 0.5s after each other. Weapons on cool-down when their turn arrives are
     * skipped past and the next available weapon fires.
     */
    ChainFire
}