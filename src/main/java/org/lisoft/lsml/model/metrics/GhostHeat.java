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
import org.lisoft.lsml.model.modifiers.Modifier;

import java.util.*;
import java.util.Map.Entry;

import static java.util.Comparator.naturalOrder;

/**
 * This {@link Metric} calculates the total ghost heat penalty for an alpha strike from a loadout.
 *
 * @author Li Song
 */
public class GhostHeat implements Metric {
    private static final double[] HEAT_SCALE = {0, 0, 0.08, 0.18, 0.30, 0.45, 0.60, 0.80, 1.10, 1.50, 2.00, 3.00, 5.00};
    private final Loadout loadout;
    private final int weaponGroup;

    /**
     * Constructs a new {@link GhostHeat} metric that will calculate the ghost heat for the entire loadout.
     *
     * @param aLoadout The loadout to calculate for.
     */
    public GhostHeat(Loadout aLoadout) {
        this(aLoadout, -1);
    }

    /**
     * Constructs a new {@link GhostHeat} metric that calculates the ghost heat for a given weapon group.
     *
     * @param aLoadout The loadout to calculate for.
     * @param aGroup   The weapon group to calculate for.
     */
    public GhostHeat(Loadout aLoadout, int aGroup) {
        loadout = aLoadout;
        weaponGroup = aGroup;
    }

    @Override
    public double calculate() {
        final Collection<Modifier> modifiers = loadout.getAllModifiers();
        final Map<Weapon, Integer> ungroupedWeapons = new HashMap<>();
        final Map<Integer, List<Weapon>> groups = new HashMap<>();

        final Iterable<Weapon> weapons;
        if (weaponGroup < 0) {
            weapons = loadout.items(Weapon.class);
        } else {
            weapons = loadout.getWeaponGroups().getWeapons(weaponGroup, loadout);
        }

        for (final Weapon weapon : weapons) {
            final int group = weapon.getGhostHeatGroup();
            if (group == 0) {
                ungroupedWeapons.put(weapon, ungroupedWeapons.getOrDefault(weapon, 0) + 1);
            } else if (group > 0) {
                groups.computeIfAbsent(group, ArrayList::new).add(weapon);
            }
        }

        double penalty = 0;
        for (final Entry<Weapon, Integer> entry : ungroupedWeapons.entrySet()) {
            Weapon weapon = entry.getKey();
            int count = entry.getValue();
            penalty += calculatePenalty(weapon.getHeat(modifiers), weapon.getGhostHeatMultiplier(),
                                        weapon.getGhostHeatMaxFreeAlpha(modifiers), count);
        }

        // XXX: http://mwomercs.com/forums/topic/127904-heat-scale-the-maths/ is not completely
        // clear on this. We interpret the post to mean that for the purpose of ghost heat, every weapon
        // in the linked group is equal to the weapon with the highest base heat.
        // Update(2018-10-28): It appears the post was amended after the fact which confirms the above
        // assumption
        for (final List<Weapon> group : groups.values()) {
            @SuppressWarnings("OptionalGetWithoutIsPresent") // Always present
            double maxHeat = group.stream().map(w -> w.getHeat(modifiers)).max(naturalOrder()).get();
            @SuppressWarnings("OptionalGetWithoutIsPresent") // Always present
            double maxMultiplier = group.stream().map(Weapon::getGhostHeatMultiplier).max(naturalOrder()).get();
            @SuppressWarnings("OptionalGetWithoutIsPresent") // Always present
            int maxFreeAlpha = group.stream().map(w -> w.getGhostHeatMaxFreeAlpha(modifiers)).min(naturalOrder()).get();
            penalty += calculatePenalty(maxHeat, maxMultiplier, maxFreeAlpha, group.size());
        }
        return penalty;
    }

    private double calculatePenalty(double aHeat, double aMultiplier, int aMaxFreeAlpha, int aCount) {
        double penalty = 0;
        int count = aCount;
        while (count > aMaxFreeAlpha) {
            penalty += HEAT_SCALE[Math.min(count, HEAT_SCALE.length - 1)] * aMultiplier * aHeat;
            count--;
        }
        return penalty;
    }

}
