/*
 * Li Song Mechlab - A 'mech building tool for PGI's MechWarrior: Online.
 * Copyright (C) 2013-2023  Li Song
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
package org.lisoft.lsml.model.metrics;

import org.lisoft.mwo_data.Faction;
import org.lisoft.mwo_data.ItemDB;
import org.lisoft.mwo_data.equipment.ArmourUpgrade;
import org.lisoft.mwo_data.equipment.Engine;
import org.lisoft.mwo_data.equipment.NoSuchItemException;
import org.lisoft.mwo_data.equipment.StructureUpgrade;
import org.lisoft.mwo_data.equipment.UpgradeDB;
import org.lisoft.mwo_data.mechs.Chassis;
import org.lisoft.mwo_data.mechs.ChassisOmniMech;
import org.lisoft.mwo_data.mechs.ChassisStandard;

/**
 * This class calculates how many tons of payload a mech chassis can carry given a specific engine
 * rating/type, upgrades, and with max/no armour.
 *
 * @author Li Song
 */
public class PayloadStatistics {
  private boolean endoSteel;
  private boolean ferroFibrous;
  private boolean maxArmour;
  private boolean xlEngine;

  public PayloadStatistics(
      boolean aXlEngine, boolean aMaxArmour, boolean aEndoSteel, boolean aFerroFibrous) {
    xlEngine = aXlEngine;
    maxArmour = aMaxArmour;
    endoSteel = aEndoSteel;
    ferroFibrous = aFerroFibrous;
  }

  /**
   * Calculates how much user payload the given omnimech can carry. Will consider the status of the
   * {@link #setMaxArmour(boolean)} the status of XL engine or other upgrades are ignored as they
   * are fixed on omnimechs.
   *
   * @param aChassis The {@link ChassisOmniMech} to calculate the payload tonnage for.
   * @return The payload tonnage.
   */
  public double calculate(ChassisOmniMech aChassis) {
    return calculate(
        aChassis,
        aChassis.getFixedEngine(),
        aChassis.getFixedStructureType(),
        aChassis.getFixedArmourType());
  }

  /**
   * Calculates the payload tonnage for the given {@link ChassisStandard}, with the given engine
   * rating under the upgrades that have been given to the constructor or changed through the
   * various setters.
   *
   * <p>If the engine is smaller than 250, then additional heat sinks required to operate the mech
   * will be subtracted from the payload.
   *
   * @param aChassis The {@link ChassisStandard} to calculate for.
   * @param aEngineRating The engine rating to use.
   * @return The payload tonnage.
   * @throws NoSuchItemException if no matching engine was found.
   */
  public double calculate(ChassisStandard aChassis, int aEngineRating) throws NoSuchItemException {
    final Engine engine =
        ItemDB.getEngine(
            aEngineRating,
            xlEngine ? Engine.EngineType.XL : Engine.EngineType.STD,
            aChassis.getFaction());

    final ArmourUpgrade armour;
    final StructureUpgrade structure;
    if (aChassis.getFaction() == Faction.CLAN) {
      armour = ferroFibrous ? UpgradeDB.CLAN_FF_ARMOUR : UpgradeDB.CLAN_STD_ARMOUR;
      structure = endoSteel ? UpgradeDB.CLAN_ES_STRUCTURE : UpgradeDB.CLAN_STD_STRUCTURE;
    } else {
      armour = ferroFibrous ? UpgradeDB.IS_FF_ARMOUR : UpgradeDB.IS_STD_ARMOUR;
      structure = endoSteel ? UpgradeDB.IS_ES_STRUCTURE : UpgradeDB.IS_STD_STRUCTURE;
    }
    return calculate(aChassis, engine, structure, armour);
  }

  /**
   * @return <code>true</code> if ferro fibrous is assumed.
   */
  public boolean isFerroFibrous() {
    return ferroFibrous;
  }

  /**
   * Changes whether or not endo steel should be assumed when calculating the payload.
   *
   * @param aEndoSteel <code>true</code> if endo steel should be assumed.
   */
  public void setEndoSteel(boolean aEndoSteel) {
    endoSteel = aEndoSteel;
  }

  /**
   * Changes whether or not ferro fibrous should be assumed when calculating the payload.
   *
   * @param aFerroFibrous <code>true</code> if ferro fibrous should be assumed.
   */
  public void setFerroFibrous(boolean aFerroFibrous) {
    ferroFibrous = aFerroFibrous;
  }

  /**
   * Changes whether or not max armour should be assumed when calculating the payload.
   *
   * @param aMaxArmour <code>true</code> if max armour should be assumed.
   */
  public void setMaxArmour(boolean aMaxArmour) {
    maxArmour = aMaxArmour;
  }

  /**
   * Changes whether or not XL engine should be assumed when calculating the payload.
   *
   * @param aXlEngine <code>true</code> if XL engine should be assumed.
   */
  public void setXLEngine(boolean aXlEngine) {
    xlEngine = aXlEngine;
  }

  private double calculate(
      Chassis aChassis,
      Engine aEngine,
      StructureUpgrade aStructureUpgrade,
      ArmourUpgrade aArmourUpgrade) {
    final double internalMass = aStructureUpgrade.getStructureMass(aChassis);
    double maxPayload = aChassis.getMassMax() - internalMass;

    maxPayload -= aEngine.getMass();
    maxPayload -= 10 - aEngine.getNumInternalHeatsinks();

    if (maxArmour) {
      maxPayload -= aArmourUpgrade.getArmourMass(aChassis.getArmourMax());
    }

    return maxPayload;
  }
}
