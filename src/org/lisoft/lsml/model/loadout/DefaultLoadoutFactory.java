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
package org.lisoft.lsml.model.loadout;

import org.lisoft.lsml.command.CmdLoadStock;
import org.lisoft.lsml.model.chassi.ArmorSide;
import org.lisoft.lsml.model.chassi.ChassisBase;
import org.lisoft.lsml.model.chassi.ChassisOmniMech;
import org.lisoft.lsml.model.chassi.ChassisStandard;
import org.lisoft.lsml.model.chassi.Location;
import org.lisoft.lsml.model.item.Item;
import org.lisoft.lsml.model.item.ItemDB;
import org.lisoft.lsml.model.item.PilotModule;
import org.lisoft.lsml.model.loadout.component.ComponentBuilder;
import org.lisoft.lsml.model.loadout.component.ComponentBuilder.Factory;
import org.lisoft.lsml.model.loadout.component.ConfiguredComponentBase;
import org.lisoft.lsml.model.loadout.component.ConfiguredComponentOmniMech;
import org.lisoft.lsml.model.loadout.component.ConfiguredComponentStandard;
import org.lisoft.lsml.model.upgrades.UpgradeDB;
import org.lisoft.lsml.model.upgrades.Upgrades;
import org.lisoft.lsml.model.upgrades.UpgradesMutable;
import org.lisoft.lsml.util.CommandStack;

/**
 * This class produces loadouts as they are typically used by the application.
 * 
 * @author Li Song
 *
 */
public class DefaultLoadoutFactory implements LoadoutFactory {
    private final Factory<ConfiguredComponentStandard> stdComponentFactory;
    private final Factory<ConfiguredComponentOmniMech> omniComponentFactory;
    private final CommandStack                         stack = new CommandStack(0);

    public final static DefaultLoadoutFactory instance = new DefaultLoadoutFactory();

    public DefaultLoadoutFactory() {
        this(ComponentBuilder.getStandardComponentFactory(), ComponentBuilder.getOmniComponentFactory());
    }

    public DefaultLoadoutFactory(Factory<ConfiguredComponentStandard> aStdFactory,
            Factory<ConfiguredComponentOmniMech> aOmniFactory) {
        stdComponentFactory = aStdFactory;
        omniComponentFactory = aOmniFactory;
    }

    @Override
    public LoadoutBase<?> produceEmpty(ChassisBase aChassis) {
        if (aChassis instanceof ChassisStandard) {
            ChassisStandard chassis = (ChassisStandard) aChassis;
            return new LoadoutStandard(stdComponentFactory, chassis, UpgradesMutable.standardUpgrades(),
                    new WeaponGroups());
        }
        else if (aChassis instanceof ChassisOmniMech) {
            ChassisOmniMech chassis = (ChassisOmniMech) aChassis;
            Upgrades upgrades = new Upgrades(chassis.getFixedArmorType(), chassis.getFixedStructureType(),
                    UpgradeDB.STANDARD_GUIDANCE, chassis.getFixedHeatSinkType());
            return new LoadoutOmniMech(omniComponentFactory, chassis, upgrades, new WeaponGroups());
        }
        throw new IllegalArgumentException("Unknown chassis type!");
    }

    @Override
    public LoadoutBase<?> produceStock(ChassisBase aChassis) throws Exception {
        LoadoutBase<?> ans = produceEmpty(aChassis);
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
    public LoadoutBase<?> produceClone(LoadoutBase<?> aSource) {
        LoadoutBase<?> target = produceEmpty(aSource.getChassis());

        // Base attributes
        target.getWeaponGroups().assign(aSource.getWeaponGroups());
        target.getEfficiencies().assign(aSource.getEfficiencies());
        target.getUpgrades().assign(aSource.getUpgrades());
        target.rename(aSource.getName());

        // Modules
        for (PilotModule module : aSource.getModules()) {
            target.addModule(module);
        }

        for (ConfiguredComponentBase srcCmpnt : aSource.getComponents()) {
            Location loc = srcCmpnt.getInternalComponent().getLocation();
            ConfiguredComponentBase tgtCmpnt = target.getComponent(loc);

            // Omnipod + Actuator
            if (srcCmpnt instanceof ConfiguredComponentOmniMech) {
                ConfiguredComponentOmniMech omniSourceComponent = (ConfiguredComponentOmniMech) srcCmpnt;
                ConfiguredComponentOmniMech omniTargetComponent = (ConfiguredComponentOmniMech) tgtCmpnt;
                omniTargetComponent.setOmniPod(omniSourceComponent.getOmniPod());

                matchToggleState(omniTargetComponent, omniSourceComponent, ItemDB.HA);
                matchToggleState(omniTargetComponent, omniSourceComponent, ItemDB.LAA);
            }

            // Armor
            if (srcCmpnt.getInternalComponent().getLocation().isTwoSided()) {
                tgtCmpnt.setArmor(ArmorSide.FRONT, srcCmpnt.getArmor(ArmorSide.FRONT), srcCmpnt.hasManualArmor());
                tgtCmpnt.setArmor(ArmorSide.BACK, srcCmpnt.getArmor(ArmorSide.BACK), srcCmpnt.hasManualArmor());
            }
            else {
                tgtCmpnt.setArmor(ArmorSide.ONLY, srcCmpnt.getArmor(ArmorSide.ONLY), srcCmpnt.hasManualArmor());
            }

            // Equipment
            for (Item item : srcCmpnt.getItemsEquipped()) {
                tgtCmpnt.addItem(item);
            }
        }
        return target;
    }
}
