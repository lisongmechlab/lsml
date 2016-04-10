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
import org.lisoft.lsml.model.datacache.UpgradeDB;
import org.lisoft.lsml.model.item.Engine;
import org.lisoft.lsml.model.item.EngineType;
import org.lisoft.lsml.model.upgrades.ArmorUpgrade;
import org.lisoft.lsml.model.upgrades.StructureUpgrade;

/**
 * This class calculates how many tons of payload a mech chassis can carry given a specific engine rating/type,
 * upgrades, and with max/no armour.
 * 
 * @author Emily Björk
 */
public class PayloadStatistics {
    private boolean xlEngine;
    private boolean maxArmour;
    private boolean endoSteel;
    private boolean ferroFibrous;

    public PayloadStatistics(boolean aXlEngine, boolean aMaxArmour, boolean aEndoSteel, boolean aFerroFibrous) {
        xlEngine = aXlEngine;
        maxArmour = aMaxArmour;
        endoSteel = aEndoSteel;
        ferroFibrous = aFerroFibrous;
    }

    /**
     * Changes whether or not XL engine should be assumed when calculating the payload.
     * 
     * @param aXlEngine
     *            <code>true</code> if XL engine should be assumed.
     */
    public void setXLEngine(boolean aXlEngine) {
        xlEngine = aXlEngine;
    }

    /**
     * Changes whether or not max armour should be assumed when calculating the payload.
     * 
     * @param aMaxArmour
     *            <code>true</code> if max armour should be assumed.
     */
    public void setMaxArmor(boolean aMaxArmour) {
        maxArmour = aMaxArmour;
    }

    /**
     * Changes whether or not endo steel should be assumed when calculating the payload.
     * 
     * @param aEndoSteel
     *            <code>true</code> if endo steel should be assumed.
     */
    public void setEndoSteel(boolean aEndoSteel) {
        endoSteel = aEndoSteel;
    }

    /**
     * Changes whether or not ferro fibrous should be assumed when calculating the payload.
     * 
     * @param aFerroFibrous
     *            <code>true</code> if ferro fibrous should be assumed.
     */
    public void setFerroFibrous(boolean aFerroFibrous) {
        ferroFibrous = aFerroFibrous;
    }

    /**
     * @return <code>true</code> if ferro fibrous is assumed.
     */
    public boolean isFerroFibrous() {
        return ferroFibrous;
    }

    /**
     * Calculates the payload tonnage for the given {@link ChassisStandard}, with the given engine rating under the
     * upgrades that have been given to the constructor or changed through the various setters.
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
        Engine engine = ItemDB.getEngine(aEngineRating, xlEngine ? EngineType.XL : EngineType.STD,
                aChassis.getFaction());

        ArmorUpgrade armor = UpgradeDB.getArmor(aChassis.getFaction(), ferroFibrous);
        StructureUpgrade structure = UpgradeDB.getStructure(aChassis.getFaction(), endoSteel);
        return calculate(aChassis, engine, structure, armor);
    }

    /**
     * Calculates how much user payload the given omnimech can carry. Will consider the status of the
     * {@link #setMaxArmor(boolean)} the status of XL engine or other upgrades are ignored as they are fixed on
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

        if (maxArmour) {
            maxPayload -= aArmorUpgrade.getArmorMass(aChassis.getArmorMax());
        }

        return maxPayload;
    }
}
