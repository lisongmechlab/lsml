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

import org.lisoft.lsml.model.loadout.Loadout;

import com.thoughtworks.xstream.annotations.XStreamAlias;

/**
 * A garage is a ordered hierarchical structure of loadouts and drop-ships.
 *
 * @author Li Song
 */
@XStreamAlias(value = "garage")
public class Garage {
    private final GarageDirectory<Loadout> loadouts = new GarageDirectory<>("Garage");
    private final GarageDirectory<DropShip> dropships = new GarageDirectory<>("Garage");

    @Override
    public boolean equals(Object aObj) {
        if (aObj instanceof Garage) {
            final Garage that = (Garage) aObj;
            return loadouts.equals(that.loadouts) && dropships.equals(that.dropships);
        }
        return false;
    }

    /**
     * @return The root directory for all drop ships.
     */
    public GarageDirectory<DropShip> getDropShipRoot() {
        return dropships;
    }

    /**
     * @return The root directory for all loadouts.
     */
    public GarageDirectory<Loadout> getLoadoutRoot() {
        return loadouts;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + (dropships == null ? 0 : dropships.hashCode());
        result = prime * result + (loadouts == null ? 0 : loadouts.hashCode());
        return result;
    }
}
