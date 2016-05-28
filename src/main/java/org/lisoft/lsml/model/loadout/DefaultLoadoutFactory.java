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

import org.lisoft.lsml.command.CmdDistributeArmor;
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
    public final static DefaultLoadoutFactory instance = new DefaultLoadoutFactory();
    private final CommandStack stack = new CommandStack(0);

    @Override
    public Loadout produceClone(Loadout aSource) {
        final Loadout target = produceEmpty(aSource.getChassis());
        target.setName(aSource.getName());

        // Base attributes
        target.getWeaponGroups().assign(aSource.getWeaponGroups());
        target.getEfficiencies().assign(aSource.getEfficiencies());
        target.getUpgrades().assign(aSource.getUpgrades());

        // Modules
        for (final PilotModule module : aSource.getModules()) {
            target.addModule(module);
        }

        for (final ConfiguredComponent srcCmpnt : aSource.getComponents()) {
            final Location loc = srcCmpnt.getInternalComponent().getLocation();
            final ConfiguredComponent tgtCmpnt = target.getComponent(loc);

            // Omnipod + Actuator
            if (srcCmpnt instanceof ConfiguredComponentOmniMech) {
                final ConfiguredComponentOmniMech omniSourceComponent = (ConfiguredComponentOmniMech) srcCmpnt;
                final ConfiguredComponentOmniMech omniTargetComponent = (ConfiguredComponentOmniMech) tgtCmpnt;
                omniTargetComponent.setOmniPod(omniSourceComponent.getOmniPod());

                matchToggleState(omniTargetComponent, omniSourceComponent, ItemDB.HA);
                matchToggleState(omniTargetComponent, omniSourceComponent, ItemDB.LAA);
            }

            // Armor
            for (final ArmorSide side : ArmorSide.allSides(srcCmpnt.getInternalComponent())) {
                tgtCmpnt.setArmor(side, srcCmpnt.getArmor(side), srcCmpnt.hasManualArmor());
            }

            // Equipment
            for (final Item item : srcCmpnt.getItemsEquipped()) {
                tgtCmpnt.addItem(item);
            }
        }
        return target;
    }

    @Override
    public Loadout produceDefault(Chassis aChassis, Settings aSettings) {
        final Loadout ans = produceEmpty(aChassis);
        final Faction faction = ans.getChassis().getFaction();

        if (aSettings.getBoolean(Settings.UPGRADES_ARTEMIS).getValue()) {
            ans.getUpgrades().setGuidance(UpgradeDB.getGuidance(faction, true));
        }

        if (ans instanceof LoadoutStandard) {
            final LoadoutStandard loadoutStandard = (LoadoutStandard) ans;
            final UpgradesMutable upgrades = loadoutStandard.getUpgrades();
            if (aSettings.getBoolean(Settings.UPGRADES_ES).getValue()) {
                upgrades.setStructure(UpgradeDB.getStructure(faction, true));
            }
            if (aSettings.getBoolean(Settings.UPGRADES_FF).getValue()) {
                upgrades.setArmor(UpgradeDB.getArmor(faction, true));
            }
            if (aSettings.getBoolean(Settings.UPGRADES_DHS).getValue()) {
                upgrades.setHeatSink(UpgradeDB.getHeatSinks(faction, true));
            }
        }

        final Efficiencies effs = ans.getEfficiencies();

        if (aSettings.getBoolean(Settings.EFFICIENCIES_ALL).getValue()) {
            for (final MechEfficiencyType type : MechEfficiencyType.values()) {
                effs.setEfficiency(type, true, null);
            }
            effs.setDoubleBasics(true, null);
        }

        if (aSettings.getBoolean(Settings.MAX_ARMOR).getValue()) {
            final int ratio = aSettings.getInteger(Settings.ARMOR_RATIO).getValue();
            final CmdDistributeArmor cmd = new CmdDistributeArmor(ans, ans.getChassis().getArmorMax(), ratio, null);
            try {
                stack.pushAndApply(cmd);
            }
            catch (final Exception e) {
                throw new AssertionError("Armor distribution failed when it shouldn't be possible", e);
            }
        }

        return ans;
    }

    @Override
    public Loadout produceEmpty(Chassis aChassis) {
        if (aChassis instanceof ChassisStandard) {
            final ChassisStandard chassis = (ChassisStandard) aChassis;
            final Faction faction = aChassis.getFaction();
            final UpgradesMutable upgrades = new UpgradesMutable(UpgradeDB.getArmor(faction, false),
                    UpgradeDB.getStructure(faction, false), UpgradeDB.STD_GUIDANCE,
                    UpgradeDB.getHeatSinks(faction, false));

            final ConfiguredComponentStandard[] components = new ConfiguredComponentStandard[Location.values().length];
            for (final ComponentStandard component : chassis.getComponents()) {
                components[component.getLocation().ordinal()] = new ConfiguredComponentStandard(component, false);
            }

            return new LoadoutStandard(components, chassis, upgrades, new WeaponGroups());
        }
        else if (aChassis instanceof ChassisOmniMech) {
            final ChassisOmniMech chassis = (ChassisOmniMech) aChassis;
            final Upgrades upgrades = new Upgrades(chassis.getFixedArmorType(), chassis.getFixedStructureType(),
                    UpgradeDB.STD_GUIDANCE, chassis.getFixedHeatSinkType());

            final ConfiguredComponentOmniMech[] components = new ConfiguredComponentOmniMech[Location.values().length];
            for (final Location location : Location.values()) {
                components[location.ordinal()] = new ConfiguredComponentOmniMech(chassis.getComponent(location), false,
                        OmniPodDB.lookupOriginal(chassis, location));
            }
            return new LoadoutOmniMech(components, chassis, upgrades, new WeaponGroups());
        }
        throw new IllegalArgumentException("Unknown chassis type!");
    }

    @Override
    public Loadout produceStock(Chassis aChassis) throws Exception {
        final Loadout ans = produceEmpty(aChassis);
        stack.pushAndApply(new CmdLoadStock(aChassis, ans, null));
        return ans;
    }

    private void matchToggleState(ConfiguredComponentOmniMech aTarget, ConfiguredComponentOmniMech aSource,
            Item aItem) {
        if (EquipResult.SUCCESS == aTarget.canToggleOn(aItem)) {
            aTarget.setToggleState(aItem, aSource.getToggleState(aItem));
        }
    }
}
