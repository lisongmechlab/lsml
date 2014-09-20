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
package lisong_mechlab.util;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.SortedSet;
import java.util.TreeSet;

import lisong_mechlab.model.item.Item;
import lisong_mechlab.model.item.Weapon;
import lisong_mechlab.model.item.WeaponModifier;
import lisong_mechlab.model.loadout.LoadoutBase;

/**
 * This class will calculate the set of ranges at which weapons change damage. In essence, it calculates the ordered
 * union of the zero, min, long and max ranges for all given weapons.
 * 
 * @author Li Song
 */
public class WeaponRanges {

	static private void addRange(SortedSet<Double> result, double aStart, double aEnd) {
		double start = aStart;
		final double step = 10;
		while (start + step < aEnd) {
			start += step;
			result.add(start);
		}
	}

	static public Double[] getRanges(Collection<Weapon> aWeaponCollection, Collection<WeaponModifier> aModifiers) {
		SortedSet<Double> ans = new TreeSet<>();

		ans.add(Double.valueOf(0.0));
		for (Weapon weapon : aWeaponCollection) {
			if (!weapon.isOffensive())
				continue;

			ans.add(weapon.getRangeZero());
			if (weapon.hasNonLinearFalloff()) {
				addRange(ans, weapon.getRangeZero(), weapon.getRangeMin());
			}
			ans.add(weapon.getRangeMin());

			if (weapon.hasSpread()) {
				addRange(ans, weapon.getRangeMin(), weapon.getRangeMax(aModifiers));
				ans.add(weapon.getRangeMax(aModifiers));
			} else {
				ans.add(weapon.getRangeLong(aModifiers));
				if (weapon.hasNonLinearFalloff()) {
					addRange(ans, weapon.getRangeZero(), weapon.getRangeMin());
				}
				ans.add(weapon.getRangeMax(aModifiers));
			}
		}
		return ans.toArray(new Double[ans.size()]);
	}

	static public Double[] getRanges(LoadoutBase<?> aLoadout) {
		List<Weapon> weapons = new ArrayList<>();
		for (Item item : aLoadout.getAllItems()) {
			if (item instanceof Weapon) {
				Weapon weapon = (Weapon) item;
				weapons.add(weapon);
			}
		}
		return getRanges(weapons, aLoadout.getWeaponModifiers());
	}
}
