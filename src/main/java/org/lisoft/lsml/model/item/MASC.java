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

import org.lisoft.lsml.model.chassi.HardPointType;

import com.thoughtworks.xstream.annotations.XStreamAsAttribute;

/**
 * This class models the MASC item from the data files.
 *
 * @author Emily Björk
 *
 */
public class MASC extends Item {
    @XStreamAsAttribute
    private final int minTons;
    @XStreamAsAttribute
    private final int maxTons;
    @XStreamAsAttribute
    private final double boostSpeed;
    @XStreamAsAttribute
    private final double boostAccel;
    @XStreamAsAttribute
    private final double boostDecel;
    @XStreamAsAttribute
    private final double boostTurn;

    public MASC(String aUiName, String aUiDesc, String aMwoName, int aMwoId, int aSlots, double aTons, int aHP,
            Faction aFaction, int aMinTons, int aMaxTons, double aBoostSpeed, double aBoostAccel, double aBoostDecel,
            double aBoostTurn) {
        super(aUiName, aUiDesc, aMwoName, aMwoId, aSlots, aTons, HardPointType.NONE, aHP, aFaction, null, null);
        minTons = aMinTons;
        maxTons = aMaxTons;
        boostSpeed = aBoostSpeed;
        boostAccel = aBoostAccel;
        boostDecel = aBoostDecel;
        boostTurn = aBoostTurn;
    }

    /**
     * @return The maximum tons (inclusive) of the chassis that this MASC can be equipped on.
     */
    public int getMaxTons() {
        return maxTons;
    }

    /**
     * @return The minimum tons (inclusive) of the chassis that this MASC can be equipped on.
     */
    public int getMinTons() {
        return minTons;
    }

    /**
     * @return The speed boost of this MASC.
     */
    public double getSpeedBoost() {
        return boostSpeed;
    }
}
