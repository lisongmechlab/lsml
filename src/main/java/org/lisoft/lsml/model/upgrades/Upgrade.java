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
package org.lisoft.lsml.model.upgrades;

import org.lisoft.lsml.model.item.Faction;
import org.lisoft.lsml.model.item.MwoObject;
import org.lisoft.lsml.model.loadout.Loadout;

/**
 * Base class for all upgrades for 'mechs.
 *
 * @author Emily Björk
 */
public abstract class Upgrade extends MwoObject {
    private static String shorten(String aUiName) {
        String shrt = aUiName.replace("CLAN ", "C-");
        shrt = aUiName.replace("ENDO-STEEL ", "ES-");
        shrt = aUiName.replace("FERRO-FIBROUS ", "FF-");
        return shrt;
    }

    protected Upgrade(String aUiName, String aUiDesc, String aMwoName, int aMwoId, Faction aFaction) {
        super(aUiName, shorten(aUiName), aUiDesc, aMwoName, aMwoId, aFaction);
    }

    /**
     * Computes the number of extra slots required over having the default upgrade on the current loadout.
     *
     * @param aLoadout
     *            The loadout to compute for.
     * @return A number of extra slots required by this upgrade.
     */
    public abstract int getTotalSlots(Loadout aLoadout);

    /**
     * Computes the amount of extra mass required compared having the default upgrade on the current loadout, may be
     * negative!
     *
     * @param aLoadout
     *            The loadout to compute for.
     * @return A amount of extra mass required by this upgrade.
     */
    public abstract double getTotalTons(Loadout aLoadout);

    /**
     * @return The {@link UpgradeType} of this upgrade.
     */
    public abstract UpgradeType getType();
}
