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
package lisong_mechlab.model.item;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import lisong_mechlab.model.pilot.PilotSkillTree;
import lisong_mechlab.mwo_data.helpers.ItemStatsModule;
import lisong_mechlab.mwo_data.helpers.XMLTargetingComputerStats;

import com.thoughtworks.xstream.annotations.XStreamAsAttribute;

/**
 * Models a targeting computer or command console.
 * <p>
 * XXX: Only takes range modifiers into account as of yet. We don't display crit and modified projectile speeds yet.
 * 
 * @author Li Song
 */
public class TargetingComputer extends Module implements WeaponModifier {
	private static class Filter implements WeaponModifier {
		private final List<String>	compatibleWeapons;

		@XStreamAsAttribute
		public double				longRange;
		@XStreamAsAttribute
		public double				maxRange;

		Filter(XMLTargetingComputerStats.XMLWeaponStatsFilter aFilter) {
			compatibleWeapons = Arrays.asList(aFilter.compatibleWeapons.split("\\s*,\\s*"));

			boolean ok = false;
			for (XMLTargetingComputerStats.XMLWeaponStatsFilter.XMLWeaponStats stats : aFilter.WeaponStats) {
				if (stats.longRange != 0.0 || stats.maxRange != 0.0) {
					longRange = stats.longRange - 1;
					maxRange = stats.maxRange - 1;
					ok = true;
					break;
				}
			}

			if (!ok)
				throw new UnsupportedOperationException();
		}

		@Override
		public boolean affectsWeapon(Weapon aWeapon) {
			for (String name : compatibleWeapons) {
				if (name.equals(aWeapon.getKey()))
					return true;
			}
			return false;
		}

		@Override
		public double extraMaxRange(Weapon aWeapon, double aRange, PilotSkillTree aPilotSkillTree) {
			if (affectsWeapon(aWeapon))
				return maxRange * aRange;
			return 0;
		}

		@Override
		public double extraLongRange(Weapon aWeapon, double aRange, PilotSkillTree aPilotSkillTree) {
			if (affectsWeapon(aWeapon))
				return longRange * aRange;
			return 0;
		}

		@Override
		public double extraWeaponHeat(Weapon aWeapon, double aHeat, PilotSkillTree aPilotSkillTree) {
			return 0;
		}

		@Override
		public double extraCooldown(Weapon aWeapon, double aCooldown, PilotSkillTree aPilotSkillTree) {
			return 0;
		}
	}

	private final List<Filter>	filters	= new ArrayList<>();

	public TargetingComputer(ItemStatsModule aModule) {
		super(aModule);

		if (null != aModule.TargetingComputerStats.WeaponStatsFilter) {
			for (XMLTargetingComputerStats.XMLWeaponStatsFilter filter : aModule.TargetingComputerStats.WeaponStatsFilter) {
				try {
					filters.add(new Filter(filter));
				} catch (UnsupportedOperationException e) {
					// Keep calm and carry on.
				}
			}
		}
	}

	@Override
	public boolean affectsWeapon(Weapon aWeapon) {
		for (Filter filter : filters) {
			if (filter.affectsWeapon(aWeapon))
				return true;
		}
		return false;
	}

	@Override
	public double extraMaxRange(Weapon aWeapon, double aRange, PilotSkillTree aPilotSkillTree) {
		for (Filter filter : filters) {
			if (filter.affectsWeapon(aWeapon)) {
				return filter.extraMaxRange(aWeapon, aRange, aPilotSkillTree);
			}
		}
		return 0;
	}

	@Override
	public double extraLongRange(Weapon aWeapon, double aRange, PilotSkillTree aPilotSkillTree) {
		for (Filter filter : filters) {
			if (filter.affectsWeapon(aWeapon)) {
				return filter.extraLongRange(aWeapon, aRange, aPilotSkillTree);
			}
		}
		return 0;
	}

	@Override
	public double extraWeaponHeat(Weapon aWeapon, double aHeat, PilotSkillTree aPilotSkillTree) {
		for (Filter filter : filters) {
			if (filter.affectsWeapon(aWeapon)) {
				return filter.extraWeaponHeat(aWeapon, aHeat, aPilotSkillTree);
			}
		}
		return 0;
	}

	@Override
	public double extraCooldown(Weapon aWeapon, double aCooldown, PilotSkillTree aPilotSkillTree) {
		for (Filter filter : filters) {
			if (filter.affectsWeapon(aWeapon)) {
				return filter.extraCooldown(aWeapon, aCooldown, aPilotSkillTree);
			}
		}
		return 0;
	}

}
