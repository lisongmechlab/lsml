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
package org.lisoft.lsml.model.garage;

import org.lisoft.lsml.model.loadout.LoadoutBase;

import com.thoughtworks.xstream.annotations.XStreamAlias;

/**
 * A garage is a ordered hierarchical structure of loadouts and drop-ships.
 * 
 * @author Li Song
 */
@XStreamAlias(value = "garage")
public class GarageTwo {
    private final GarageDirectory<LoadoutBase<?>> loadouts  = new GarageDirectory<>("root");
    private final GarageDirectory<DropShip>       dropships = new GarageDirectory<>("root");

    /**
     * @return The root directory for all loadouts.
     */
    public GarageDirectory<LoadoutBase<?>> getLoadoutRoot() {
        return loadouts;
    }

    /**
     * @return The root directory for all drop ships.
     */
    public GarageDirectory<DropShip> getDropShipRoot() {
        return dropships;
    }

    @Override
    public boolean equals(Object aObj) {
        if (aObj instanceof GarageTwo) {
            GarageTwo that = (GarageTwo) aObj;
            return loadouts.equals(that.loadouts) && dropships.equals(that.dropships);
        }
        return false;
    }

}
