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

import java.util.Arrays;

import org.lisoft.lsml.model.NamedObject;
import org.lisoft.lsml.model.item.Faction;
import org.lisoft.lsml.model.loadout.Loadout;

/**
 * This class models a drop ship in CW games. A drop ship consists of 4 'Mechs of the same faction and a tonnage limit.
 * 
 * @author Li Song
 */
public class DropShip extends NamedObject {
    public final static int MIN_CLAN_TONNAGE  = 160;
    public final static int MAX_CLAN_TONNAGE  = 240;
    public final static int MIN_IS_TONNAGE    = 160;
    public final static int MAX_IS_TONNAGE    = 250;
    public final static int MECHS_IN_DROPSHIP = 4;

    private final Faction   faction;
    private final Loadout   loadouts[]        = new Loadout[4];

    /**
     * Creates a new drop ship for the given faction.
     * 
     * @param aFaction
     *            The faction of the new drop ship.
     */
    public DropShip(Faction aFaction) {
        super("Unnamed Drop Ship");
        faction = aFaction;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((faction == null) ? 0 : faction.hashCode());
        result = prime * result + ((loadouts == null) ? 0 : loadouts.hashCode());
        return result;
    }

    @Override
    public String toString() {
        return getName();
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        DropShip other = (DropShip) obj;
        if (faction != other.faction)
            return false;
        if (loadouts == null) {
            if (other.loadouts != null)
                return false;
        }
        else if (!Arrays.deepEquals(loadouts, other.loadouts))
            return false;
        return true;
    }

    /**
     * @return The minimum tonnage allowed for this drop ship.
     */
    public int getMinTonnage() {
        return faction == Faction.INNERSPHERE ? MIN_IS_TONNAGE : MIN_CLAN_TONNAGE;
    }

    /**
     * @return The maximum tonnage allowed for this drop ship.
     */
    public int getMaxTonnage() {
        return faction == Faction.INNERSPHERE ? MAX_IS_TONNAGE : MAX_CLAN_TONNAGE;
    }

    /**
     * Gets the 'Mech with the given bay index in the drop ship.
     * 
     * @param aBayIndex
     *            The index of the mech to get, must be less than {@link DropShip#MECHS_IN_DROPSHIP} but larger than or
     *            equal to zero.
     * @return The loadout, or <code>null</code> if there is no mech for that index.
     */
    public Loadout getMech(int aBayIndex) {
        return loadouts[aBayIndex];
    }

    /**
     * Stores a 'Mech in one of the bays in the drop ship.
     * 
     * @param aBayIndex
     *            The index of the bay to store the 'Mech in. Must be less than {@link DropShip#MECHS_IN_DROPSHIP} but
     *            larger than or equal to zero.
     * 
     * @param aLoadout
     *            The loadout to add.
     * @throws GarageException
     *             If the loadout has the wrong faction for this drop ship or if the drop ship is full already.
     */
    public void setMech(int aBayIndex, Loadout aLoadout) throws GarageException {
        if (aLoadout != null && aLoadout.getChassis().getFaction() != faction) {
            throw new GarageException("Wrong faction for drop ship!");
        }
        loadouts[aBayIndex] = aLoadout;
    }

    /**
     * @return The total tonnage of the drop ship's 'Mechs.
     */
    public int getTonnage() {
        int ans = 0;
        for (Loadout loadout : loadouts) {
            ans += loadout == null ? 0 : loadout.getChassis().getMassMax();
        }
        return ans;
    }

    /**
     * Checks if the loadout is compatible to be put in this drop ship.
     * 
     * @param aLoadout
     *            The loadout to check.
     * @return <code>true</code> if it is, <code>false</code> otherwise.
     */
    public boolean isCompatible(Loadout aLoadout) {
        return faction.isCompatible(aLoadout.getChassis().getFaction());
    }

    /**
     * @return The faction of this drop ship.
     */
    public Faction getFaction() {
        return faction;
    }
}
