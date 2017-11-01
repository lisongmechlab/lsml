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
import org.lisoft.lsml.model.upgrades.Upgrades;

public class HeatSink extends Module {
    private final double dissipation;
    private final double capacity;
    private final double engineDissipation;

    public HeatSink(String aName, String aDesc, String aMwoName, int aMwoId, int aSlots, double aTons,
            HardPointType aHardpointType, double aHP, Faction aFaction, double aDissipation, double aEngineDissipation,
            double aCapacity) {
        super(aName, aDesc, aMwoName, aMwoId, aSlots, aTons, aHardpointType, aHP, aFaction, null, null, null);
        dissipation = aDissipation;
        engineDissipation = aEngineDissipation;
        capacity = aCapacity;
    }

    public double getCapacity() {
        return capacity;
    }

    public double getDissipation() {
        return dissipation;
    }

    /**
     * @return The heat dissipation of one heat sink of this type when internal to the engine.
     */
    public double getEngineDissipation() {
        return engineDissipation;
    }

    @Override
    public boolean isCompatible(Upgrades aUpgrades) {
        return aUpgrades.getHeatSink().getHeatSinkType() == this;
    }

    public boolean isDouble() {
        return capacity > 1.00001; // Account for double precision
    }
}
