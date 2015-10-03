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

import org.lisoft.lsml.command.OpLoadStock;
import org.lisoft.lsml.model.chassi.ChassisBase;
import org.lisoft.lsml.model.chassi.ChassisOmniMech;
import org.lisoft.lsml.model.chassi.ChassisStandard;
import org.lisoft.lsml.model.loadout.component.ComponentBuilder;
import org.lisoft.lsml.model.loadout.component.ComponentBuilder.Factory;
import org.lisoft.lsml.model.loadout.component.ConfiguredComponentOmniMech;
import org.lisoft.lsml.model.loadout.component.ConfiguredComponentStandard;
import org.lisoft.lsml.model.upgrades.UpgradeDB;
import org.lisoft.lsml.model.upgrades.Upgrades;
import org.lisoft.lsml.model.upgrades.UpgradesMutable;
import org.lisoft.lsml.util.OperationStack;

/**
 * This class produces loadouts as they are typically used by the application.
 * 
 * @author Emily Björk
 *
 */
public class DefaultLoadoutFactory implements LoadoutFactory {
    private final Factory<ConfiguredComponentStandard> stdComponentFactory;
    private final Factory<ConfiguredComponentOmniMech> omniComponentFactory;

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
    public LoadoutBase<?> produceStock(ChassisBase aChassis) {
        LoadoutBase<?> ans = produceEmpty(aChassis);
        OperationStack operationStack = new OperationStack(0);
        operationStack.pushAndApply(new OpLoadStock(aChassis, ans, null));
        return ans;
    }

    // TODO: Use factory to produce copies
}
