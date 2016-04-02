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
package org.lisoft.lsml.model.metrics;

import org.lisoft.lsml.model.chassi.Chassis;
import org.lisoft.lsml.model.chassi.ChassisOmniMech;
import org.lisoft.lsml.model.chassi.ChassisStandard;
import org.lisoft.lsml.model.datacache.ItemDB;
import org.lisoft.lsml.model.item.Engine;
import org.lisoft.lsml.model.upgrades.ArmorUpgrade;
import org.lisoft.lsml.model.upgrades.StructureUpgrade;
import org.lisoft.lsml.model.upgrades.Upgrades;

/**
 * This class calculates how many tons of payload a mech chassis can carry given a specific engine rating/type,
 * upgrades, and with max/no armor.
 * 
 * @author Emily Björk
 */
public class PayloadStatistics {

    private boolean  xlEngine;
    private boolean  maxArmor;
    private Upgrades upgrades;

    public PayloadStatistics(boolean aUseXlEngine, boolean aUseMaxArmor, Upgrades anUpgrades) {
        xlEngine = aUseXlEngine;
        maxArmor = aUseMaxArmor;
        upgrades = anUpgrades;
    }

    public void changeUseXLEngine(boolean aUseXlEngine) {
        xlEngine = aUseXlEngine;
    }

    public void changeUseMaxArmor(boolean aUseMaxArmor) {
        maxArmor = aUseMaxArmor;
    }

    public void changeUpgrades(Upgrades anUpgrades) {
        upgrades = anUpgrades;
    }

    /**
     * Calculates the payload tonnage for the given {@link ChassisStandard}, with the given engine rating under the
     * upgrades that have been given to the constructor. Will consider the status of the
     * {@link #changeUpgrades(Upgrades)}, {@link #changeUseMaxArmor(boolean)} and {@link #changeUseXLEngine(boolean)}.
     * <p>
     * If the engine is smaller than 250, then additional heat sinks required to operate the mech will be subtracted
     * from the payload.
     * 
     * @param aChassis
     *            The {@link ChassisStandard} to calculate for.
     * @param aEngineRating
     *            The engine rating to use.
     * @return The payload tonnage.
     */
    public double calculate(ChassisStandard aChassis, int aEngineRating) {
        Engine engine = (Engine) ItemDB.lookup((xlEngine ? "XL" : "STD") + " ENGINE " + aEngineRating);
        return calculate(aChassis, engine, upgrades.getStructure(), upgrades.getArmor());
    }

    /**
     * Calculates how much user payload the given omnimech can carry. Will consider the status of the
     * {@link #changeUseMaxArmor(boolean)} the status of XL engine or other upgrades are ignored as they are fixed on
     * omnimechs.
     * 
     * @param aChassis
     *            The {@link ChassisOmniMech} to calculate the payload tonnage for.
     * @return The payload tonnage.
     */
    public double calculate(ChassisOmniMech aChassis) {
        return calculate(aChassis, aChassis.getFixedEngine(), aChassis.getFixedStructureType(),
                aChassis.getFixedArmorType());

    }

    private double calculate(Chassis aChassis, Engine aEngine, StructureUpgrade aStructureUpgrade,
            ArmorUpgrade aArmorUpgrade) {
        double internalMass = aStructureUpgrade.getStructureMass(aChassis);
        double maxPayload = aChassis.getMassMax() - internalMass;

        maxPayload -= aEngine.getMass();
        maxPayload -= 10 - aEngine.getNumInternalHeatsinks();

        if (maxArmor) {
            maxPayload -= aArmorUpgrade.getArmorMass(aChassis.getArmorMax());
        }

        return maxPayload;
    }
}
