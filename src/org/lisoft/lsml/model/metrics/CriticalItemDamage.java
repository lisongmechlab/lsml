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

import org.lisoft.lsml.model.item.Item;
import org.lisoft.lsml.model.loadout.component.ConfiguredComponentBase;
import org.lisoft.lsml.util.BinomialDistribution;

/**
 * This {@link ItemMetric} calculates statistically how much damage the given item takes per 1 damage to the component
 * applied in infinitesimal chunks.
 * <p>
 * This applies mostly to for lasers. MG and LB 10-X AC have higher critical hit probabilities and different
 * multipliers.
 * 
 * @author Li Song
 */
public class CriticalItemDamage implements ItemMetric {
    private final ConfiguredComponentBase loadoutPart;

    public CriticalItemDamage(ConfiguredComponentBase aLoadoutPart) {
        loadoutPart = aLoadoutPart;
    }

    @Override
    public double calculate(Item aItem) {
        return calculate(aItem, loadoutPart);
    }

    public static double calculate(Item anItem, ConfiguredComponentBase aLoadoutPart) {
        int slots = 0;
        for (Item it : aLoadoutPart.getItemsEquipped()) {
            if (it.isCrittable())
                slots += it.getNumCriticalSlots();
        }
        for (Item it : aLoadoutPart.getItemsFixed()) {
            if (it.isCrittable())
                slots += it.getNumCriticalSlots();
        }
        return calculate(anItem.getNumCriticalSlots(), slots);
    }

    public static double calculate(int aItemCrits, int aTotalCrits) {
        double p_hit = (double) aItemCrits / aTotalCrits;

        double ans = 0;
        for (int i = 0; i < CriticalStrikeProbability.CRIT_CHANCE.length; ++i) {
            final int numCritRolls = i + 1;
            // The event of 'k' hits out of numCritRolls tries, with p_hit probability is
            // binomially distributed.
            BinomialDistribution bin = new BinomialDistribution(p_hit, numCritRolls);

            for (int numHits = 1; numHits <= numCritRolls; ++numHits) {
                ans += bin.pdf(numHits) * numHits * CriticalStrikeProbability.CRIT_CHANCE[i];
            }
        }
        return ans;
    }

}
