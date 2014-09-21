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

import lisong_mechlab.model.chassi.HardPointType;
import lisong_mechlab.mwo_data.helpers.ItemStatsModule;

/**
 * A generic ammunition item.
 * 
 * @author Li Song
 */
public class Ammunition extends Item {
	protected final int				rounds;
	protected final double			internalDamage;
	protected final HardPointType	type;
	protected final String			ammoType;

	public Ammunition(ItemStatsModule aStatsModule) {
		super(aStatsModule, HardPointType.NONE, aStatsModule.ModuleStats.slots, aStatsModule.ModuleStats.tons,
				aStatsModule.ModuleStats.health);
		internalDamage = aStatsModule.AmmoTypeStats.internalDamage;
		rounds = aStatsModule.AmmoTypeStats.numShots;
		ammoType = aStatsModule.AmmoTypeStats.type;

		if (getName().contains("LRM") || getName().contains("SRM") || getName().contains("NARC")) {
			type = HardPointType.MISSILE;
		} else if (getName().contains("AMS")) {
			type = HardPointType.AMS;
		} else {
			type = HardPointType.BALLISTIC;
		}
	}

	public int getNumShots() {
		return rounds;
	}

	/**
	 * @return The {@link HardPointType} that the weapon that uses this ammo is using. Useful for color coding and
	 *         searching.
	 */
	public HardPointType getWeaponHardpointType() {
		return type;
	}

	@Override
	public String getShortName() {
		String name = getName();
		name = name.replace("ULTRA ", "U");
		name = name.replace("MACHINE GUN", "MG");
		return name;
	}

	/**
	 * @return The type name of this {@link Ammunition}. Used to match with {@link Weapon} ammo type.
	 */
	public String getAmmoType() {
		return ammoType;
	}
}
