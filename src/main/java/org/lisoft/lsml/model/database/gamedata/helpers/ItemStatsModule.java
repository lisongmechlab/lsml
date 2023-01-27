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
package org.lisoft.lsml.model.database.gamedata.helpers;

import com.thoughtworks.xstream.annotations.XStreamAsAttribute;
import com.thoughtworks.xstream.annotations.XStreamImplicit;
import java.util.List;
import org.lisoft.lsml.model.chassi.HardPointType;
import org.lisoft.lsml.model.item.*;
import org.lisoft.lsml.model.item.Module;

public class ItemStatsModule extends ItemStats {
  public AmmoTypeStats AmmoTypeStats;
  @XStreamAsAttribute public String CType;
  public XMLConsumableStats ConsumableStats;
  public ItemStatsEngineStats EngineStats;
  public ItemStatsHeatSinkStats HeatSinkStats;
  public ItemStatsJumpJetStats JumpJetStats;
  public ItemStatsMascStats MASCStats;
  public ItemStatsModuleStats ModuleStats;
  public XMLTargetingComputerStats TargetingComputerStats;
  @XStreamImplicit public List<XMLWeaponStats> WeaponStats;

  public Item asItem() {
    switch (CType) {
      case "CAmmoTypeStats":
        return AmmoTypeStats.asAmmunition(this);
      case "CEngineStats":
        return EngineStats.asEngine(this);
      case "CHeatSinkStats":
        return new HeatSink(
            getUiName(),
            getUiDescription(),
            getMwoKey(),
            getMwoId(),
            ModuleStats.slots,
            ModuleStats.tons,
            HardPointType.NONE,
            ModuleStats.health,
            getFaction(),
            HeatSinkStats.cooling,
            HeatSinkStats.engineCooling,
            -HeatSinkStats.heatbase,
            -HeatSinkStats.engineHeatbase);
      case "CJumpJetStats":
        // Two values, first is heat for one JJ
        final double heat = Double.parseDouble(JumpJetStats.heat.split(",")[0]);
        return new JumpJet(
            getUiName(),
            getUiDescription(),
            getMwoKey(),
            getMwoId(),
            ModuleStats.slots,
            ModuleStats.tons,
            HardPointType.NONE,
            ModuleStats.health,
            getFaction(),
            ModuleStats.getLocations(),
            ModuleStats.getMechClasses(),
            JumpJetStats.minTons,
            JumpJetStats.maxTons,
            JumpJetStats.boost,
            JumpJetStats.duration,
            heat);
      case "CGECMStats":
        return new ECM(
            getUiName(),
            getUiDescription(),
            getMwoKey(),
            getMwoId(),
            ModuleStats.slots,
            ModuleStats.tons,
            ModuleStats.health,
            getFaction(),
            ModuleStats.amountAllowed);
      case "CTargetingComputerStats":
        return TargetingComputerStats.asTargetingComputer(this);
      case "CMASCStats":
        return MASCStats.asMasc(this);

      case "CBAPStats": // FALLTHROUGH
      case "CClanBAPStats": // FALLTHROUGH
      case "CClanLightBAPStats":
        return new ActiveProbe(
            getUiName(),
            getUiDescription(),
            getMwoKey(),
            getMwoId(),
            ModuleStats.slots,
            ModuleStats.tons,
            HardPointType.NONE,
            ModuleStats.health,
            getFaction(),
            ModuleStats.getLocations(),
            ModuleStats.getMechClasses(),
            ModuleStats.amountAllowed);
      case "CCASEStats":
        return new Module(
            getUiName(),
            getUiDescription(),
            getMwoKey(),
            getMwoId(),
            ModuleStats.slots,
            ModuleStats.tons,
            HardPointType.NONE,
            ModuleStats.health,
            getFaction(),
            ModuleStats.getLocations(),
            ModuleStats.getMechClasses(),
            null);

        // Miscellaneous Internals:
      case "CAdvancedSensorsStats":
      case "CLowerArmActuatorStats":
      case "CInternalStats":
        return new Internal(
            getUiName(),
            getUiDescription(),
            getMwoKey(),
            getMwoId(),
            ModuleStats.slots,
            ModuleStats.tons,
            HardPointType.NONE,
            ModuleStats.health,
            getFaction());
      default:
        return null;
    }
  }
}
