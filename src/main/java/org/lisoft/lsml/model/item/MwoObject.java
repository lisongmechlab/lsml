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
 * A common base class for just about all things read from the MWO game files.
 *
 * @author Li Song
 */
public class MwoObject {
    @XStreamAsAttribute
    private final String description;
    @XStreamAsAttribute
    private final Faction faction;
    @XStreamAsAttribute
    private final int mwoId;
    @XStreamAsAttribute
    private final String mwoKey;
    @XStreamAsAttribute
    private final String name;
    @XStreamAsAttribute
    private final String shortName;

    public MwoObject(String aUiName, String aUiDesc, String aMwoName, int aMwoId, Faction aFaction) {
        this(aUiName, heuristicShorten(aUiName), aUiDesc, aMwoName, aMwoId, aFaction);
    }

    public MwoObject(String aUiName, String aUiShortName, String aUiDesc, String aMwoName, int aMwoId,
                     Faction aFaction) {
        name = aUiName;
        description = aUiDesc;
        shortName = aUiShortName;
        mwoKey = aMwoName;
        mwoId = aMwoId;
        faction = aFaction;
    }

    /**
     * @return The description as found in the data files. May be empty string.
     */
    public String getDescription() {
        return description;
    }

    /**
     * @return The faction requirement of this {@link Item}.
     */
    public Faction getFaction() {
        return faction;
    }

    public int getId() {
        return mwoId;
    }

    /**
     * @return The MWO key as found in the data files. May be empty if it was not read in.
     */
    public String getKey() {
        return mwoKey;
    }

    public String getName() {
        return name;
    }

    public String getShortName() {
        if (null == shortName) {
            return getName();
        }
        return shortName;
    }

    @Override
    public String toString() {
        return getName();
    }

    private static String heuristicShorten(String aName) {
        if (aName == null) {
            return null;
        }

        String name = aName.replaceAll("[cC][Ll][Aa][Nn] ", "C-");
        name = name.replace("ANTI-MISSILE SYSTEM", "AMS");
        name = name.replace("ULTRA ", "U");
        name = name.replace("MACHINE GUN", "MG");
        name = name.replace("LASER", "LAS");
        name = name.replace("LARGE ", "L");
        name = name.replace("LRG ", "L");
        name = name.replace("SML ", "S");
        name = name.replace("SMALL ", "S");
        name = name.replace("MED ", "M");
        name = name.replace("MEDIUM ", "M");
        name = name.replace("MICRO ", "U");
        name = name.replace("PULSE ", "P");
        name = name.replace("ENGINE ", "");
        name = name.replace("DOUBLE ", "D");
        name = name.replace("HEAT SINK", "HS");
        name = name.replace("UPPER ", "U-");
        name = name.replace("LOWER ", "L-");
        name = name.replace("ACTUATOR", "");
        name = name.replace("JUMP JETS", "JJ");
        name = name.replace("CLASS ", "");
        name = name.replace("ARTEMIS", "A.");
        name = name.replace("STREAK ", "S-");
        name = name.replace("TARGETING COMP.", "T.C.");
        name = name.replace("T.COMP.", "T.C.");
        return name;
    }
}
