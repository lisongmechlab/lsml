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
package lisong_mechlab.model.item;

import lisong_mechlab.model.chassi.HardPointType;
import lisong_mechlab.model.upgrades.Upgrades;

public class HeatSink extends Module {
    private final double dissapation;
    private final double capacity;

    public HeatSink(String aName, String aDesc, String aMwoName, int aMwoId, int aSlots, double aTons,
            HardPointType aHardpointType, int aHP, Faction aFaction, double aDissipation, double aCapacity) {
        super(aName, aDesc, aMwoName, aMwoId, aSlots, aTons, aHardpointType, aHP, aFaction);
        dissapation = aDissipation;
        capacity = aCapacity;
    }

    public double getDissipation() {
        return dissapation;
    }

    public double getCapacity() {
        return capacity;
    }

    public boolean isDouble() {
        return capacity > 1.00001; // Account for double precision
    }

    @Override
    public boolean isCompatible(Upgrades aUpgrades) {
        return aUpgrades.getHeatSink().getHeatSinkType() == this;
    }
}
