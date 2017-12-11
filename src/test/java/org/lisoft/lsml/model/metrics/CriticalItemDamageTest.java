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

import static org.junit.Assert.assertEquals;

import java.util.ArrayList;
import java.util.List;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.lisoft.lsml.model.database.ItemDB;
import org.lisoft.lsml.model.item.Internal;
import org.lisoft.lsml.model.item.Item;
import org.lisoft.lsml.model.loadout.ConfiguredComponent;
import org.lisoft.lsml.model.loadout.Loadout;
import org.lisoft.lsml.model.upgrades.Upgrades;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.MockitoJUnitRunner;

/**
 * Test suite for {@link CriticalItemDamage}.
 *
 * @author Emily Björk
 */
@RunWith(MockitoJUnitRunner.Silent.class)
public class CriticalItemDamageTest {
	List<Item> items = new ArrayList<>();
	@Mock
	ConfiguredComponent loadoutPart;
	@Mock
	Loadout loadout;
	@Mock
	Upgrades upgrades;
	@InjectMocks
	CriticalItemDamage cut;

	@Before
	public void setup() {
		Mockito.when(loadoutPart.getItemsEquipped()).thenReturn(items);
		Mockito.when(loadout.getUpgrades()).thenReturn(upgrades);
	}

	/**
	 * XL engine sides do affect the critical hit rolls.
	 */
	@Test
	public void testEngineInternals() {
		final Item i = ItemDB.ENGINE_INTERNAL;
		final Item internal = Mockito.mock(Internal.class);
		Mockito.when(internal.getSlots()).thenReturn(5);
		items.add(i);
		items.add(internal);

		assertEquals(0.25 * 1 + 0.14 * 2 + 0.03 * 3, cut.calculate(i), 0.0);
	}

	/**
	 * Internal items do not affect the critical hit rolls.
	 */
	@Test
	public void testNotCrittable() {
		final Item i = Mockito.mock(Item.class);
		final Item nocrit = Mockito.mock(Internal.class);
		Mockito.when(i.getSlots()).thenReturn(5);
		Mockito.when(i.isCrittable()).thenReturn(true);
		Mockito.when(nocrit.getSlots()).thenReturn(5);
		Mockito.when(nocrit.isCrittable()).thenReturn(false);
		items.add(i);
		items.add(nocrit);

		assertEquals(0.25 * 1 + 0.14 * 2 + 0.03 * 3, cut.calculate(i), 0.0);
	}

	/**
	 * Easy case. When there is only one item, the chance to critical hit is the sum
	 * of the critical hit probabilities for 1, 2 and 3 hits with weights 1,2 and 3.
	 */
	@Test
	public void testOneItem() {
		final Item i = Mockito.mock(Item.class);
		Mockito.when(i.getSlots()).thenReturn(5);
		Mockito.when(i.isCrittable()).thenReturn(true);
		items.add(i);

		assertEquals(0.25 * 1 + 0.14 * 2 + 0.03 * 3, cut.calculate(i), 0.0);
	}

	/**
	 * When two or more items are involved the calculations are a bit more complex.
	 * The damage dealt by 1 damage has to take into account the number of hits from
	 * a critical hit. A 3-roll critical strike has 3 chances to hit this item. The
	 * probability that the item will be hit is X~bin(n,p) where n is the number of
	 * rolls and p is the chance that the item will be hit in the roll. The binomial
	 * probability needs to be weighted with the number of hits. For example P(X=3)
	 * shall be weighted by 3 to produce the correct damage amount.
	 */
	@Test
	public void testTwoItems() {
		final Item i0 = Mockito.mock(Item.class);
		final Item i1 = Mockito.mock(Item.class);
		Mockito.when(i0.getSlots()).thenReturn(5);
		Mockito.when(i0.isCrittable()).thenReturn(true);
		Mockito.when(i1.getSlots()).thenReturn(15);
		Mockito.when(i1.isCrittable()).thenReturn(true);
		items.add(i0);
		items.add(i1);

		final double p_hit0 = 5.0 / 20;
		final double p_hit1 = 15.0 / 20;
		double ans0 = 0, ans1 = 0;
		// 1 crit hit: 25%
		ans0 = 1 * 0.25 * p_hit0;
		ans1 = 1 * 0.25 * p_hit1;

		// 2 crit hits: 14%
		ans0 += 0.14 * (2 * p_hit0 * (1 - p_hit0) * 1 + p_hit0 * p_hit0 * 2);
		ans1 += 0.14 * (2 * p_hit1 * (1 - p_hit1) * 1 + p_hit1 * p_hit1 * 2);

		// 3 crit hits: 3%
		ans0 += 0.03 * (3 * p_hit0 * (1 - p_hit0) * (1 - p_hit0) * 1 + 3 * p_hit0 * p_hit0 * (1 - p_hit0) * 2
				+ p_hit0 * p_hit0 * p_hit0 * 3);
		ans1 += 0.03 * (3 * p_hit1 * (1 - p_hit1) * (1 - p_hit1) * 1 + 3 * p_hit1 * p_hit1 * (1 - p_hit1) * 2
				+ p_hit1 * p_hit1 * p_hit1 * 3);

		assertEquals(ans0, cut.calculate(i0), 0.000001);
		assertEquals(ans1, cut.calculate(i1), 0.000001);
	}
}
