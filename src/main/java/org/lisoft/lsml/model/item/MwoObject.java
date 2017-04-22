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
package org.lisoft.lsml.model.item;

import com.thoughtworks.xstream.annotations.XStreamAsAttribute;

/**
 * A common base class for just about all things read from the MWO game files.
 *
 * @author Emily Björk
 */
public class MwoObject {
    @XStreamAsAttribute
    private final String locName;
    @XStreamAsAttribute
    private final String locDesc;
    @XStreamAsAttribute
    private final String mwoName;
    @XStreamAsAttribute
    private final int mwoIdx;
    @XStreamAsAttribute
    private final Faction faction;

    public MwoObject(String aUiName, String aUiDesc, String aMwoName, int aMwoId, Faction aFaction) {
        locName = aUiName;
        locDesc = aUiDesc;
        mwoName = aMwoName;
        mwoIdx = aMwoId;
        faction = aFaction;
    }

    /*
    @Override
    public boolean equals(Object aObj) {
        if (aObj instanceof MwoObject) {
            final MwoObject new_name = (MwoObject) aObj;
            return new_name.getMwoId() == getMwoId();
        }
        return false;
    }*/

    /**
     * @return The description as found in the data files. May be empty string.
     */
    public String getDescription() {
        return locDesc;
    }

    /**
     * @return The faction requirement of this {@link Item}.
     */
    public Faction getFaction() {
        return faction;
    }

    /**
     * @return The MWO key as found in the data files. May be empty if it was not read in.
     */
    public String getKey() {
        return mwoName;
    }

    public int getMwoId() {
        return mwoIdx;
    }

    public String getName() {
        return locName;
    }

    public String getShortName() {
        return getName().replaceAll("[cC][Ll][Aa][Nn] ", "C-");
    }

    /*
    @Override
    public int hashCode() {
        return getMwoId();
    }
*/

    @Override
    public String toString() {
        return getName();
    }
}
