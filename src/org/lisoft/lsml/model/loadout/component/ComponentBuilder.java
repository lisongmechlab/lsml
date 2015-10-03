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
package org.lisoft.lsml.model.loadout.component;

import org.lisoft.lsml.model.chassi.ChassisBase;
import org.lisoft.lsml.model.chassi.ChassisOmniMech;
import org.lisoft.lsml.model.chassi.ChassisStandard;
import org.lisoft.lsml.model.chassi.ComponentStandard;
import org.lisoft.lsml.model.chassi.Location;
import org.lisoft.lsml.model.chassi.OmniPodDB;
import org.lisoft.lsml.model.loadout.LoadoutBase;

/**
 * This factory object can construct configured components.
 * 
 * TODO: This class is a bit of a mess, tidy it up.
 * 
 * @author Emily Björk
 */
public class ComponentBuilder {
    public interface Factory<T extends ConfiguredComponentBase> {
        T[] cloneComponents(LoadoutBase<T> aLoadout);

        T[] defaultComponents(ChassisBase aChassis);
    }

    private static class StandardFactory implements Factory<ConfiguredComponentStandard> {
        @Override
        public ConfiguredComponentStandard[] cloneComponents(LoadoutBase<ConfiguredComponentStandard> aLoadout) {
            ConfiguredComponentStandard[] ans = new ConfiguredComponentStandard[Location.values().length];
            for (ConfiguredComponentStandard component : aLoadout.getComponents()) {
                ans[component.getInternalComponent().getLocation().ordinal()] = new ConfiguredComponentStandard(
                        component);
            }
            return ans;
        }

        @Override
        public ConfiguredComponentStandard[] defaultComponents(ChassisBase aChassis) {
            ChassisStandard chassis = (ChassisStandard) aChassis;
            ConfiguredComponentStandard[] ans = new ConfiguredComponentStandard[Location.values().length];
            for (ComponentStandard component : chassis.getComponents()) {
                ans[component.getLocation().ordinal()] = new ConfiguredComponentStandard(component, false);
            }
            return ans;
        }
    }

    private static class OmniMechFactory implements Factory<ConfiguredComponentOmniMech> {
        @Override
        public ConfiguredComponentOmniMech[] cloneComponents(LoadoutBase<ConfiguredComponentOmniMech> aLoadout) {
            ConfiguredComponentOmniMech[] ans = new ConfiguredComponentOmniMech[Location.values().length];
            for (Location location : Location.values()) {
                ans[location.ordinal()] = new ConfiguredComponentOmniMech(aLoadout.getComponent(location));
            }
            return ans;
        }

        @Override
        public ConfiguredComponentOmniMech[] defaultComponents(ChassisBase aChassis) {
            ChassisOmniMech omniMech = (ChassisOmniMech) aChassis;
            ConfiguredComponentOmniMech[] ans = new ConfiguredComponentOmniMech[Location.values().length];
            for (Location location : Location.values()) {
                ans[location.ordinal()] = new ConfiguredComponentOmniMech(omniMech.getComponent(location), false,
                        OmniPodDB.lookupOriginal(omniMech, location));
            }
            return ans;
        }
    }

    private static Factory<ConfiguredComponentStandard> is   = new StandardFactory();
    private static Factory<ConfiguredComponentOmniMech> omni = new OmniMechFactory();

    static public Factory<ConfiguredComponentStandard> getStandardComponentFactory() {
        return is;
    }

    static public Factory<ConfiguredComponentOmniMech> getOmniComponentFactory() {
        return omni;
    }
}
