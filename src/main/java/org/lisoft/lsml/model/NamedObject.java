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
package org.lisoft.lsml.model;

import org.lisoft.lsml.model.garage.DropShip;
import org.lisoft.lsml.model.garage.GarageDirectory;
import org.lisoft.lsml.model.loadout.Loadout;

/**
 * This class represents an object with a name. This is used as a common base class for {@link DropShip} and
 * {@link Loadout} so that {@link GarageDirectory} and some commands can be made generic.
 *
 * @author Li Song
 */
public class NamedObject {
    protected String name = "Unnamed Drop Ship";

    public NamedObject(String aName) {
        name = aName;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (!(obj instanceof NamedObject)) {
            return false;
        }
        final NamedObject other = (NamedObject) obj;
        if (name == null) {
            if (other.name != null) {
                return false;
            }
        }
        else if (!name.equals(other.name)) {
            return false;
        }
        return true;
    }

    /**
     * @return The name of this object.
     */
    public String getName() {
        return name;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + (name == null ? 0 : name.hashCode());
        return result;
    }

    /**
     * Changes the name of this object.
     *
     * @param aString
     *            The new name.
     */
    public void setName(String aString) {
        name = aString;
    }

    @Override
    public String toString() {
        return name;
    }
}
