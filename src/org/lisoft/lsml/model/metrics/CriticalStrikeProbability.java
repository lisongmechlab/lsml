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
package org.lisoft.lsml.model.metrics;

import org.lisoft.lsml.model.item.Item;
import org.lisoft.lsml.model.loadout.component.ConfiguredComponent;

/**
 * This {@link ItemMetric} calculates the probability that the given item will be critically hit at least once by a
 * shot.
 * <p>
 * If hit by an high alpha weapons such as PPC, Gauss Rifle, AC/20,10 the item will likely be destroyed if it's hp is 10
 * or less.
 * 
 * @author Emily Björk
 */
public class CriticalStrikeProbability implements ItemMetric {
    public final static double        CRIT_CHANCE[] = { 0.25, 0.14, 0.03 }; // 25% risk of 1 hit, 15% risk of 2
                                                                            // hits,
                                                                            // 3%
                                                                            // risk of 3
                                                                            // hits
    private final ConfiguredComponent loadoutPart;

    public CriticalStrikeProbability(ConfiguredComponent aLoadoutPart) {
        loadoutPart = aLoadoutPart;
    }

    @Override
    public double calculate(Item aItem) {
        int slots = 0;
        for (Item it : loadoutPart.getItemsEquipped()) {
            if (it.isCrittable())
                slots += it.getNumCriticalSlots();
        }
        for (Item it : loadoutPart.getItemsFixed()) {
            if (it.isCrittable())
                slots += it.getNumCriticalSlots();
        }

        // The probability that this item will be hit at any one event
        double p_hit = (double) aItem.getNumCriticalSlots() / slots;
        return calculate(p_hit);
    }

    public static double calculate(double aP_hit) {
        double ans = 0;
        for (int i = 0; i < CriticalStrikeProbability.CRIT_CHANCE.length; ++i) {
            ans += (1 - Math.pow(1 - aP_hit, i + 1)) * CriticalStrikeProbability.CRIT_CHANCE[i];
        }
        return ans;
    }
}
