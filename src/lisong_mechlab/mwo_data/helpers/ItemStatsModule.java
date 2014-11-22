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
package lisong_mechlab.mwo_data.helpers;

import java.util.List;

import lisong_mechlab.model.chassi.HardPointType;
import lisong_mechlab.model.item.ECM;
import lisong_mechlab.model.item.HeatSink;
import lisong_mechlab.model.item.Internal;
import lisong_mechlab.model.item.Item;
import lisong_mechlab.model.item.JumpJet;
import lisong_mechlab.model.item.Module;

import com.thoughtworks.xstream.annotations.XStreamAsAttribute;
import com.thoughtworks.xstream.annotations.XStreamImplicit;

public class ItemStatsModule extends ItemStats {
    @XStreamAsAttribute
    public String                    CType;

    public ItemStatsModuleStats      ModuleStats;
    public ItemStatsJumpJetStats     JumpJetStats;
    public ItemStatsHeatSinkStats    HeatSinkStats;
    public ItemStatsEngineStats      EngineStats;
    public AmmoTypeStats             AmmoTypeStats;
    public XMLPilotModuleStats       PilotModuleStats;
    public XMLTargetingComputerStats TargetingComputerStats;

    public XMLPilotModuleWeaponStats PilotModuleWeaponStats;

    @XStreamImplicit
    public List<XMLWeaponStats>      WeaponStats;

    public Item asItem() {
        switch (CType) {
            case "CAmmoTypeStats":
                return AmmoTypeStats.asAmmunition(this);
            case "CEngineStats":
                return EngineStats.asEngine(this);
            case "CHeatSinkStats":
                return new HeatSink(getUiName(), getUiDesc(), getMwoKey(), getMwoId(), ModuleStats.slots,
                        ModuleStats.tons, HardPointType.NONE, ModuleStats.health, getFaction(), HeatSinkStats.cooling,
                        HeatSinkStats.heatbase);
            case "CJumpJetStats":
                // Two values, first is heat for one JJ
                double heat = Double.parseDouble(JumpJetStats.heat.split(",")[0]);
                return new JumpJet(getUiName(), getUiDesc(), getMwoKey(), getMwoId(), ModuleStats.slots,
                        ModuleStats.tons, HardPointType.NONE, ModuleStats.health, getFaction(), JumpJetStats.minTons,
                        JumpJetStats.maxTons, JumpJetStats.boost, JumpJetStats.duration, heat);
            case "CGECMStats":
                return new ECM(getUiName(), getUiDesc(), getMwoKey(), getMwoId(), ModuleStats.slots, ModuleStats.tons,
                        ModuleStats.health, getFaction());
            case "CBAPStats":
            case "CClanBAPStats":
            case "CCASEStats":
                return new Module(getUiName(), getUiDesc(), getMwoKey(), getMwoId(), ModuleStats.slots,
                        ModuleStats.tons, HardPointType.NONE, ModuleStats.health, getFaction());
            case "CLowerArmActuatorStats":
            case "CInternalStats":
                return new Internal(getUiName(), getUiDesc(), getMwoKey(), getMwoId(), ModuleStats.slots,
                        ModuleStats.tons, HardPointType.NONE, ModuleStats.health, getFaction());
            case "CTargetingComputerStats":
                return TargetingComputerStats.asTargetingComputer(this);
            default:
                return null;
        }
    }
}
