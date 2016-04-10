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
package org.lisoft.lsml.model.loadout;

import org.lisoft.lsml.command.CmdLoadStock;
import org.lisoft.lsml.model.chassi.ArmorSide;
import org.lisoft.lsml.model.chassi.Chassis;
import org.lisoft.lsml.model.chassi.ChassisOmniMech;
import org.lisoft.lsml.model.chassi.ChassisStandard;
import org.lisoft.lsml.model.chassi.ComponentStandard;
import org.lisoft.lsml.model.chassi.Location;
import org.lisoft.lsml.model.datacache.ItemDB;
import org.lisoft.lsml.model.datacache.OmniPodDB;
import org.lisoft.lsml.model.datacache.UpgradeDB;
import org.lisoft.lsml.model.item.Faction;
import org.lisoft.lsml.model.item.Item;
import org.lisoft.lsml.model.item.PilotModule;
import org.lisoft.lsml.model.loadout.component.ConfiguredComponent;
import org.lisoft.lsml.model.loadout.component.ConfiguredComponentOmniMech;
import org.lisoft.lsml.model.loadout.component.ConfiguredComponentStandard;
import org.lisoft.lsml.model.modifiers.Efficiencies;
import org.lisoft.lsml.model.modifiers.MechEfficiencyType;
import org.lisoft.lsml.model.upgrades.Upgrades;
import org.lisoft.lsml.model.upgrades.UpgradesMutable;
import org.lisoft.lsml.util.CommandStack;
import org.lisoft.lsml.view_fx.Settings;

/**
 * This class produces loadouts as they are typically used by the application.
 * 
 * @author Emily Björk
 *
 */
public class DefaultLoadoutFactory implements LoadoutFactory {
    private final CommandStack stack = new CommandStack(0);
    public final static DefaultLoadoutFactory instance = new DefaultLoadoutFactory();

    @Override
    public Loadout produceEmpty(Chassis aChassis) {
        if (aChassis instanceof ChassisStandard) {
            ChassisStandard chassis = (ChassisStandard) aChassis;
            Faction faction = aChassis.getFaction();
            UpgradesMutable upgrades = new UpgradesMutable(UpgradeDB.getArmor(faction, false),
                    UpgradeDB.getStructure(faction, false), UpgradeDB.STD_GUIDANCE,
                    UpgradeDB.getHeatSinks(faction, false));

            ConfiguredComponentStandard[] components = new ConfiguredComponentStandard[Location.values().length];
            for (ComponentStandard component : chassis.getComponents()) {
                components[component.getLocation().ordinal()] = new ConfiguredComponentStandard(component, false);
            }

            return new LoadoutStandard(components, chassis, upgrades, new WeaponGroups());
        }
        else if (aChassis instanceof ChassisOmniMech) {
            ChassisOmniMech chassis = (ChassisOmniMech) aChassis;
            Upgrades upgrades = new Upgrades(chassis.getFixedArmorType(), chassis.getFixedStructureType(),
                    UpgradeDB.STD_GUIDANCE, chassis.getFixedHeatSinkType());

            ConfiguredComponentOmniMech[] components = new ConfiguredComponentOmniMech[Location.values().length];
            for (Location location : Location.values()) {
                components[location.ordinal()] = new ConfiguredComponentOmniMech(chassis.getComponent(location), false,
                        OmniPodDB.lookupOriginal(chassis, location));
            }
            return new LoadoutOmniMech(components, chassis, upgrades, new WeaponGroups());
        }
        throw new IllegalArgumentException("Unknown chassis type!");
    }

    @Override
    public Loadout produceStock(Chassis aChassis) throws Exception {
        Loadout ans = produceEmpty(aChassis);
        stack.pushAndApply(new CmdLoadStock(aChassis, ans, null));
        return ans;
    }

    private void matchToggleState(ConfiguredComponentOmniMech aTarget, ConfiguredComponentOmniMech aSource,
            Item aItem) {
        if (EquipResult.SUCCESS == aTarget.canToggleOn(aItem)) {
            aTarget.setToggleState(aItem, aSource.getToggleState(aItem));
        }
    }

    @Override
    public Loadout produceClone(Loadout aSource) {
        Loadout target = produceEmpty(aSource.getChassis());

        // Base attributes
        target.getWeaponGroups().assign(aSource.getWeaponGroups());
        target.getEfficiencies().assign(aSource.getEfficiencies());
        target.getUpgrades().assign(aSource.getUpgrades());
        target.setName(aSource.getName());

        // Modules
        for (PilotModule module : aSource.getModules()) {
            target.addModule(module);
        }

        for (ConfiguredComponent srcCmpnt : aSource.getComponents()) {
            Location loc = srcCmpnt.getInternalComponent().getLocation();
            ConfiguredComponent tgtCmpnt = target.getComponent(loc);

            // Omnipod + Actuator
            if (srcCmpnt instanceof ConfiguredComponentOmniMech) {
                ConfiguredComponentOmniMech omniSourceComponent = (ConfiguredComponentOmniMech) srcCmpnt;
                ConfiguredComponentOmniMech omniTargetComponent = (ConfiguredComponentOmniMech) tgtCmpnt;
                omniTargetComponent.setOmniPod(omniSourceComponent.getOmniPod());

                matchToggleState(omniTargetComponent, omniSourceComponent, ItemDB.HA);
                matchToggleState(omniTargetComponent, omniSourceComponent, ItemDB.LAA);
            }

            // Armor
            for (ArmorSide side : ArmorSide.allSides(srcCmpnt.getInternalComponent())) {
                tgtCmpnt.setArmor(side, srcCmpnt.getArmor(side), srcCmpnt.hasManualArmor());
            }

            // Equipment
            for (Item item : srcCmpnt.getItemsEquipped()) {
                tgtCmpnt.addItem(item);
            }
        }
        return target;
    }

    @Override
    public Loadout produceDefault(Chassis aChassis, Settings aSettings) {
        Loadout ans = produceEmpty(aChassis);
        Faction faction = ans.getChassis().getFaction();

        if (aSettings.getProperty(Settings.UPGRADES_ARTEMIS, Boolean.class).getValue()) {
            ans.getUpgrades().setGuidance(UpgradeDB.getGuidance(faction, true));
        }

        if (ans instanceof LoadoutStandard) {
            LoadoutStandard loadoutStandard = (LoadoutStandard) ans;
            UpgradesMutable upgrades = loadoutStandard.getUpgrades();
            if (aSettings.getProperty(Settings.UPGRADES_ES, Boolean.class).getValue()) {
                upgrades.setStructure(UpgradeDB.getStructure(faction, true));
            }
            if (aSettings.getProperty(Settings.UPGRADES_FF, Boolean.class).getValue()) {
                upgrades.setArmor(UpgradeDB.getArmor(faction, true));
            }
            if (aSettings.getProperty(Settings.UPGRADES_DHS, Boolean.class).getValue()) {
                upgrades.setHeatSink(UpgradeDB.getHeatSinks(faction, true));
            }
        }

        Efficiencies effs = ans.getEfficiencies();

        if (aSettings.getProperty(Settings.EFFICIENCIES_ALL, Boolean.class).getValue()) {
            for (MechEfficiencyType type : MechEfficiencyType.values()) {
                effs.setEfficiency(type, true, null);
            }
            effs.setDoubleBasics(true, null);
        }

        return ans;
    }
}
