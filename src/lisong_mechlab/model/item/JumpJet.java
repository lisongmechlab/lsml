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
package lisong_mechlab.model.item;

import java.util.List;

import lisong_mechlab.model.chassi.ChassisClass;
import lisong_mechlab.model.chassi.HardPointType;
import lisong_mechlab.model.chassi.Location;

public class JumpJet extends Module {
    private final double minTons;
    private final double maxTons;
    private final double boost_z;
    private final double duration;
    private final double heat;

    public JumpJet(String aName, String aDesc, String aMwoName, int aMwoId, int aSlots, double aTons,
            HardPointType aHardpointType, int aHP, Faction aFaction, List<Location> aAllowedLocations,
            List<ChassisClass> aAllowedChassisClasses, double aMinTons, double aMaxTons, double aBoost,
            double aDuration, double aHeat) {
        super(aName, aDesc, aMwoName, aMwoId, aSlots, aTons, aHardpointType, aHP, aFaction, aAllowedLocations,
                aAllowedChassisClasses);

        minTons = aMinTons;
        maxTons = aMaxTons;
        boost_z = aBoost;
        duration = aDuration;
        heat = aHeat;
        // TODO: Parse extra heat and make use of it somethow.
    }

    public double getForce() {
        return boost_z;
    }

    public double getDuration() {
        return duration;
    }

    public double getJumpHeat() {
        return heat;
    }

    @Override
    public String getShortName() {
        String name = getName();
        name = name.replace("JUMP JETS", "JJ");
        name = name.replace("CLASS ", "");
        return name;
    }

    public double getMaxTons() {
        return maxTons;
    }

    public double getMinTons() {
        return minTons;
    }
}
