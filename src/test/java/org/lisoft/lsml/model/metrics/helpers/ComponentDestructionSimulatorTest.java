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
package org.lisoft.lsml.model.metrics.helpers;

import org.junit.Test;
import org.lisoft.lsml.model.chassi.ComponentStandard;
import org.lisoft.lsml.model.chassi.HardPointType;
import org.lisoft.lsml.model.item.Faction;
import org.lisoft.lsml.model.item.Item;
import org.lisoft.lsml.model.loadout.ConfiguredComponent;
import org.lisoft.lsml.model.metrics.CriticalStrikeProbability;
import org.mockito.Mockito;

import java.util.Arrays;
import java.util.List;

import static org.junit.Assert.assertEquals;

/**
 * Test suite for {@link ComponentDestructionSimulator}.
 *
 * @author Li Song
 */
public class ComponentDestructionSimulatorTest {

    /**
     * The test item has 10 slots, 18 health and needs two 10-point alphas to be destroyed
     */
    @Test
    public void testDoubleCritToDestroy() {
        final Item item = makeTestItem(10, 18.0);
        // Low HP to only allow one alpha before component is destroyed
        final ConfiguredComponent component = makeTestComponent(2.0, Arrays.asList(item));
        final ComponentDestructionSimulator cut = new ComponentDestructionSimulator(component);

        cut.simulate(null);

        // The AC/20 will only explode if there is a double or triple critical hit (14+3%)
        final double P_hit = CriticalStrikeProbability.CRIT_CHANCE.get(1) +
                             CriticalStrikeProbability.CRIT_CHANCE.get(2);

        assertEquals(P_hit, cut.getProbabilityOfDestruction(item), 0.0001);
    }

    /**
     * In this test there is a single item with low HP that will be destroyed by a single crit.
     * <p>
     * We take 5 shots and compute the chance that it is destroyed.
     */
    @Test
    public void testSingleCritToDestroy() {
        final Item item = makeTestItem(2, 7.5);
        final ConfiguredComponent component = makeTestComponent(42.0, Arrays.asList(item));
        final ComponentDestructionSimulator cut = new ComponentDestructionSimulator(component);

        cut.simulate(null);

        // There are 5 shots before the item explodes, thus the probability of all of them missing the item is:
        // 0.58^5, i.e. the probability of the item exploding is: 1 - 0.58^5
        final double P_hit = 1 - Math.pow(CriticalStrikeProbability.MISS_CHANCE, 5);

        assertEquals(P_hit, cut.getProbabilityOfDestruction(item), 0.0001);
    }

    /**
     * The test item has 10 slots and 18 health and needs two 10-point alphas to be destroyed.
     * <p>
     * Test with two shots.
     */
    @Test
    public void testTwoHitsToTwoCrits() {
        final Item item = makeTestItem(10, 18.0);
        // Give the component 12.0 HP to survive one shot and explode on the second.
        final ConfiguredComponent component = makeTestComponent(12.0, Arrays.asList(item));
        final ComponentDestructionSimulator cut = new ComponentDestructionSimulator(component);

        cut.simulate(null);

        // The AC/20 will only explode if it is hit twice or more
        // First shot: 58% chance to miss, 25% chance of one hit and 17% of 2 or more hits.
        // Second shot, first one missed: Need 2 hits, ie 0.58*0.17 chance
        // Second shit, first one was a single hit: Need 1 hits, ie 0.25*0.42
        // Total probability: 0.17*1.0 + 0.25*0.42 + 0.58*0.17
        final double P_hit = 0.17 * 1.0 + 0.25 * 0.42 + 0.58 * 0.17;
        assertEquals(P_hit, cut.getProbabilityOfDestruction(item), 0.0001);
    }

    // TODO: Add test to test with modifiers

    private ConfiguredComponent makeTestComponent(double aHP, List<Item> aItems) {
        final ComponentStandard componentStructure = Mockito.mock(ComponentStandard.class);
        Mockito.when(componentStructure.getHitPoints(null)).thenReturn(aHP);

        final ConfiguredComponent component = Mockito.mock(ConfiguredComponent.class);
        Mockito.when(component.getItemsEquipped()).thenReturn(aItems);
        Mockito.when(component.getInternalComponent()).thenReturn(componentStructure);
        return component;
    }

    private Item makeTestItem(int aSlots, double aHP) {
        return new Item(null, null, null, 0, aSlots, 0, HardPointType.NONE, aHP, Faction.INNERSPHERE, null, null);
    }
}
