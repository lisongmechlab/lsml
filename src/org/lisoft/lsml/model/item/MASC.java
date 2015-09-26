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

import org.lisoft.lsml.model.chassi.HardPointType;


/**
 * This class models the MASC item from the data files.
 * 
 * @author Li Song
 *
 */
public class MASC extends Item {

    private final int    minTons;
    private final int    maxTons;
    private final double boostSpeed;
    private final double boostAccel;
    private final double boostDecel;
    private final double boostTurn;

    /**
     * @param aUiName
     * @param aUiDesc
     * @param aMwoName
     * @param aMwoId
     * @param aSlots
     * @param aTons
     * @param aHP
     * @param aFaction
     * @param aMinTons
     * @param aMaxTons
     * @param aBoostSpeed
     * @param aBoostAccel
     * @param aBoostDecel
     * @param aBoostTurn
     */
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
     * @return The minimum tons (inclusive) of the chassis that this MASC can be equipped on.
     */
    public int getMinTons() {
        return minTons;
    }

    /**
     * @return The maximum tons (inclusive) of the chassis that this MASC can be equipped on.
     */
    public int getMaxTons() {
        return maxTons;
    }

}
