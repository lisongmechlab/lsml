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
package lisong_mechlab.mwo_data.helpers;

import java.util.List;

import lisong_mechlab.model.item.Ammunition;
import lisong_mechlab.model.item.ECM;
import lisong_mechlab.model.item.HeatSink;
import lisong_mechlab.model.item.Internal;
import lisong_mechlab.model.item.Item;
import lisong_mechlab.model.item.JumpJet;
import lisong_mechlab.model.item.Module;
import lisong_mechlab.model.item.TargetingComputer;

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
                return new Ammunition(this);
            case "CEngineStats":
                return EngineStats.asEngine(this);
            case "CHeatSinkStats":
                return new HeatSink(this);
            case "CJumpJetStats":
                return new JumpJet(this);
            case "CGECMStats":
                return new ECM(this);
            case "CBAPStats":
            case "CClanBAPStats":
            case "CCASEStats":
                return new Module(this);
            case "CLowerArmActuatorStats":
            case "CInternalStats":
                return new Internal(this);
            case "CTargetingComputerStats":
                return new TargetingComputer(this);
            default:
                return null;
        }
    }
}
