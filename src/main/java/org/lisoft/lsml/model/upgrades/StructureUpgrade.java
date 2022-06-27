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
package org.lisoft.lsml.model.upgrades;

import com.thoughtworks.xstream.annotations.XStreamAsAttribute;
import org.lisoft.lsml.model.chassi.Chassis;
import org.lisoft.lsml.model.database.UpgradeDB;
import org.lisoft.lsml.model.item.Faction;
import org.lisoft.lsml.model.loadout.Loadout;

/**
 * Represents an upgrade to a 'Mechs internal structure.
 *
 * @author Li Song
 */
public class StructureUpgrade extends Upgrade {
    @XStreamAsAttribute
    private final int extraSlots;
    @XStreamAsAttribute
    private final double internalStructurePct;

    public StructureUpgrade(String aUiName, String aUiDesc, String aMwoName, int aMwoId, Faction aFaction,
                            int aExtraSlots, double aStructurePct) {
        super(aUiName, aUiDesc, aMwoName, aMwoId, aFaction);
        extraSlots = aExtraSlots;
        internalStructurePct = aStructurePct;
    }

    /**
     * @return The number of extra slots that this upgrade requires to be applied.
     */
    public int getExtraSlots() {
        return getTotalSlots(null);
    }

    /**
     * Calculates the mass of the internal structure of a mech of the given chassis.
     *
     * @param aChassis The chassis to calculate the internal structure mass for.
     * @return The mass of the internal structure.
     */
    public double getStructureMass(Chassis aChassis) {
        final double ans = aChassis.getMassMax() * internalStructurePct;
        return Math.round(10 * ans / 5) * 0.5;
    }

    @Override
    public int getTotalSlots(Loadout aLoadout) {
        return extraSlots;
    }

    @Override
    public double getTotalTons(Loadout aLoadout) {
        final Chassis c = aLoadout.getChassis();
        return getStructureMass(c) - UpgradeDB.getDefaultStructure(c.getFaction()).getStructureMass(c);
    }

    @Override
    public UpgradeType getType() {
        return UpgradeType.STRUCTURE;
    }
}
