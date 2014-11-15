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
package lisong_mechlab.model.metrics;

import java.util.Collection;

import lisong_mechlab.model.item.Weapon;
import lisong_mechlab.model.loadout.LoadoutBase;
import lisong_mechlab.model.loadout.LoadoutStandard;
import lisong_mechlab.model.quirks.Modifier;

/**
 * This {@link Metric} calculates the maximal DPS a {@link LoadoutStandard} can output.
 * 
 * @author Li Song
 */
public class MaxDPS extends RangeMetric {
    public MaxDPS(LoadoutBase<?> aLoadout) {
        super(aLoadout);
    }

    @Override
    public double calculate(double aRange) {
        double ans = 0;
        Collection<Modifier> modifiers = loadout.getModifiers();
        for (Weapon weapon : loadout.items(Weapon.class)) {
            if (weapon.isOffensive())
                ans += weapon.getRangeEffectivity(aRange, modifiers) * weapon.getStat("d/s", modifiers);

        }
        return ans;
    }
}
