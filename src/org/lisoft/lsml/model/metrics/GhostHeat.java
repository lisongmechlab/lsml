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

import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.lisoft.lsml.model.item.Weapon;
import org.lisoft.lsml.model.loadout.LoadoutBase;
import org.lisoft.lsml.model.modifiers.Modifier;

/**
 * This {@link Metric} calculates the total ghost heat penalty for an alpha strike from a loadout.
 * 
 * @author Li Song
 */
public class GhostHeat implements Metric {
    private static final double  HEAT_SCALE[] = { 0, 0, 0.08, 0.18, 0.30, 0.45, 0.60, 0.80, 1.10, 1.50, 2.00, 3.00,
            5.00                             };
    private final LoadoutBase<?> loadout;
    private final int            weaponGroup;

    /**
     * Constructs a new {@link GhostHeat} metric that will calculate the ghost heat for the entire loadout.
     * 
     * @param aLoadout
     *            The loadout to calculate for.
     */
    public GhostHeat(LoadoutBase<?> aLoadout) {
        this(aLoadout, -1);
    }

    /**
     * Constructs a new {@link GhostHeat} metric that calculates the ghost heat for a given weapon group.
     * 
     * @param aLoadout
     *            The loadout to calculate for.
     * @param aGroup
     *            The weapon group to calculate for.
     */
    public GhostHeat(LoadoutBase<?> aLoadout, int aGroup) {
        loadout = aLoadout;
        weaponGroup = aGroup;
    }

    @Override
    public double calculate() {
        List<Weapon> ungroupedWeapons = new LinkedList<>();
        Map<Integer, List<Weapon>> groups = new HashMap<Integer, List<Weapon>>();

        final Iterable<Weapon> weapons;
        if (weaponGroup < 0) {
            weapons = loadout.items(Weapon.class);
        }
        else {
            weapons = loadout.getWeaponGroups().getWeapons(weaponGroup, loadout);
        }

        for (Weapon weapon : weapons) {
            int group = weapon.getGhostHeatGroup();
            if (group == 0) {
                ungroupedWeapons.add(weapon);
            }
            else if (group > 0) {
                if (!groups.containsKey(group)) {
                    groups.put(group, new LinkedList<Weapon>());
                }
                groups.get(group).add(weapon);
            }
        }

        double penalty = 0;
        while (!ungroupedWeapons.isEmpty()) {
            Weapon weapon = ungroupedWeapons.remove(0);
            int count = 1;
            Iterator<Weapon> it = ungroupedWeapons.iterator();
            while (it.hasNext()) {
                Weapon w = it.next();
                if (w == weapon) {
                    count++;
                    it.remove();
                }
            }
            penalty += calculatePenalty(weapon, count);
        }

        Collection<Modifier> modifiers = loadout.getModifiers();
        // XXX: http://mwomercs.com/forums/topic/127904-heat-scale-the-maths/ is not completely
        // clear on this. We interpret the post to mean that for the purpose of ghost heat, every weapon
        // in the linked group is equal to the weapon with highest base heat.
        for (List<Weapon> group : groups.values()) {
            double maxbaseheat = Double.NEGATIVE_INFINITY;
            Weapon maxweapon = null;
            for (Weapon w : group) {
                // XXX: It's not certain that heat applied from modules will affect the base heat value
                if (w.getHeat(modifiers) > maxbaseheat) {
                    maxbaseheat = w.getHeat(modifiers);
                    maxweapon = w;
                }
            }
            penalty += calculatePenalty(maxweapon, group.size());
        }
        return penalty;
    }

    private double calculatePenalty(Weapon aWeapon, int aCount) {
        double penalty = 0;
        int count = aCount;
        Collection<Modifier> modifiers = loadout.getModifiers();
        while (count > aWeapon.getGhostHeatMaxFreeAlpha()) {
            penalty += HEAT_SCALE[Math.min(count, HEAT_SCALE.length - 1)] * aWeapon.getGhostHeatMultiplier()
                    * aWeapon.getHeat(modifiers);
            count--;
        }
        return penalty;
    }

}
