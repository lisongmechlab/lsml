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
package org.lisoft.lsml.model.metrics;

import org.lisoft.lsml.model.item.Weapon;
import org.lisoft.lsml.model.loadout.Loadout;
import org.lisoft.lsml.model.loadout.LoadoutStandard;
import org.lisoft.lsml.model.modifiers.Modifier;

import java.util.Collection;

/**
 * This metric calculates the alpha strike heat for a given {@link LoadoutStandard}.
 * <p>
 * Does not include ghost heat.
 *
 * @author Li Song
 */
public class AlphaHeat implements Metric {
    private final Loadout loadout;
    private final int weaponGroup;

    /**
     * Creates a new {@link AlphaHeat} that calculates the alpha strike damage for a given loadout using all weapons.
     *
     * @param aLoadout The loadout to calculate for.
     */
    public AlphaHeat(final Loadout aLoadout) {
        this(aLoadout, -1);
    }

    /**
     * Creates a new {@link AlphaHeat} metric that calculates the alpha strike for the given weapon group.
     *
     * @param aLoadout The loadout to calculate for.
     * @param aGroup   The weapon group to calculate for.
     */
    public AlphaHeat(Loadout aLoadout, int aGroup) {
        loadout = aLoadout;
        weaponGroup = aGroup;
    }

    @Override
    public double calculate() {
        double ans = 0;
        final Collection<Modifier> modifiers = loadout.getAllModifiers();

        final Iterable<Weapon> weapons;
        if (weaponGroup < 0) {
            weapons = loadout.items(Weapon.class);
        } else {
            weapons = loadout.getWeaponGroups().getWeapons(weaponGroup, loadout);
        }

        for (final Weapon weapon : weapons) {
            if (weapon.isOffensive()) {
                ans += weapon.getHeat(modifiers);
            }
        }
        return ans;
    }
}
