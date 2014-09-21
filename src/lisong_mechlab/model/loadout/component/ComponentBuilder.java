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
package lisong_mechlab.model.loadout.component;

import lisong_mechlab.model.chassi.ChassisBase;
import lisong_mechlab.model.chassi.ChassisOmniMech;
import lisong_mechlab.model.chassi.ChassisStandard;
import lisong_mechlab.model.chassi.ComponentStandard;
import lisong_mechlab.model.chassi.Location;
import lisong_mechlab.model.chassi.OmniPodDB;
import lisong_mechlab.model.item.EngineType;
import lisong_mechlab.model.loadout.LoadoutBase;

/**
 * This factory object can construct configured components.
 * 
 * @author Li Song
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
				ans[component.getLocation().ordinal()] = new ConfiguredComponentStandard(component, true);
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
				ans[location.ordinal()] = new ConfiguredComponentOmniMech(omniMech.getComponent(location), true,
						OmniPodDB.lookupOriginal(omniMech, location));
				if ((location == Location.LeftTorso || location == Location.RightTorso)
						&& omniMech.getFixedEngine().getType() == EngineType.XL) {
					ans[location.ordinal()].addItem(ConfiguredComponentBase.ENGINE_INTERNAL_CLAN);
				}
			}
			return ans;
		}
	}

	private static Factory<ConfiguredComponentStandard>	is		= new StandardFactory();
	private static Factory<ConfiguredComponentOmniMech>	omni	= new OmniMechFactory();

	static public Factory<ConfiguredComponentStandard> getISComponentFactory() {
		return is;
	}

	static public Factory<ConfiguredComponentOmniMech> getOmniPodFactory() {
		return omni;
	}
}
