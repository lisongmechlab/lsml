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
package org.lisoft.lsml.model.item;

import com.thoughtworks.xstream.annotations.XStreamAsAttribute;

/**
 * A common base class for all things that can be put on a loadout.
 * 
 * @author Li Song
 */
public class Equipment {
    @XStreamAsAttribute
    private final String  locName;
    @XStreamAsAttribute
    private final String  locDesc;
    @XStreamAsAttribute
    private final String  mwoName;
    @XStreamAsAttribute
    private final int     mwoIdx;
    @XStreamAsAttribute
    private final Faction faction;

    public Equipment(String aUiName, String aUiDesc, String aMwoName, int aMwoId, Faction aFaction) {
        locName = aUiName;
        locDesc = aUiDesc;
        mwoName = aMwoName;
        mwoIdx = aMwoId;
        faction = aFaction;
    }

    public String getKey() {
        return mwoName;
    }

    @Override
    public String toString() {
        return getName();
    }

    public String getName() {
        return locName;
    }

    public int getMwoId() {
        return mwoIdx;
    }

    public String getShortName() {
        return getName().replaceAll("[cC][Ll][Aa][Nn] ", "C-");
    }

    public String getDescription() {
        return locDesc;
    }

    /**
     * @return The faction requirement of this {@link Item}.
     */
    public Faction getFaction() {
        return faction;
    }
}
