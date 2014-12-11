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

import static org.junit.Assert.assertEquals;

import java.util.ArrayList;
import java.util.List;

import lisong_mechlab.model.item.Internal;
import lisong_mechlab.model.item.Item;
import lisong_mechlab.model.loadout.LoadoutBase;
import lisong_mechlab.model.loadout.component.ConfiguredComponentBase;
import lisong_mechlab.model.upgrades.Upgrades;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.runners.MockitoJUnitRunner;

/**
 * Test suite for {@link ItemEffectiveHP}.
 * 
 * @author Li Song
 */
@RunWith(MockitoJUnitRunner.class)
public class ItemEffectiveHPTest {
    private List<Item>              items = new ArrayList<>();
    @Mock
    private ConfiguredComponentBase loadoutPart;
    @Mock
    private LoadoutBase<?>          loadout;
    @Mock
    private Upgrades                upgrades;
    @InjectMocks
    private ItemEffectiveHP         cut;

    @Before
    public void setup() {
        Mockito.when(loadoutPart.getItemsEquipped()).thenReturn(items);
        Mockito.when(loadout.getUpgrades()).thenReturn(upgrades);
    }

    /**
     * The effective HP of an {@link Item} is the amount of damage that has to be dealt to the component the
     * {@link Item} is housed in before the {@link Item} breaks. The actual effective HP is very dependent on the
     * projectile damage and very stochastic in nature. However under the assumption that the damage is applied in
     * infinitesimal chunks we can calculate the mean effective HP. This is an approximation of the effective HP for
     * lasers and other weapons with a low projectile damage with high projectile count.
     * <p>
     * Easy case. When there is only one item, the effective HP is the item HP divided by the weighted (by multiplicity)
     * chance to critically hit. See {@link CriticalItemDamage} for how much damage one damage to the component
     * internals does to the item.
     * <p>
     * HP = EffectiveHP * CriticalItemDamage(item)
     */
    @Test
    public void testOneItem() {
        Item i = Mockito.mock(Item.class);
        Mockito.when(i.getNumCriticalSlots()).thenReturn(5);
        Mockito.when(i.getHealth()).thenReturn(15);
        Mockito.when(i.isCrittable()).thenReturn(true);
        items.add(i);

        assertEquals(15 / (0.25 * 1 + 0.14 * 2 + 0.03 * 3), cut.calculate(i), 0.0);
    }

    /**
     * Internal items do not affect the critical hit rolls.
     */
    @Test
    public void testNoInternals() {
        Item i = Mockito.mock(Item.class);
        Item nocrit = Mockito.mock(Internal.class);
        Mockito.when(i.getNumCriticalSlots()).thenReturn(5);
        Mockito.when(i.getHealth()).thenReturn(15);
        Mockito.when(i.isCrittable()).thenReturn(true);
        Mockito.when(nocrit.getNumCriticalSlots()).thenReturn(5);
        Mockito.when(nocrit.getHealth()).thenReturn(0);
        Mockito.when(nocrit.isCrittable()).thenReturn(false);
        items.add(i);
        items.add(nocrit);

        assertEquals(15 / (0.25 * 1 + 0.14 * 2 + 0.03 * 3), cut.calculate(i), 0.0);
    }

    /**
     * When two or more items are involved the calculations are a bit more complex. After one of the components has
     * reached it's maximum effective HP, it is destroyed and removed from the crit rolls so this affects other
     * components effective HP.
     */
    @Test
    public void testTwoItems() {
        Item i0 = Mockito.mock(Item.class);
        Item i1 = Mockito.mock(Item.class);
        Mockito.when(i0.getNumCriticalSlots()).thenReturn(5);
        Mockito.when(i0.getHealth()).thenReturn(15);
        Mockito.when(i0.isCrittable()).thenReturn(true);
        Mockito.when(i1.getNumCriticalSlots()).thenReturn(15);
        Mockito.when(i1.getHealth()).thenReturn(15);
        Mockito.when(i1.isCrittable()).thenReturn(true);
        items.add(i0);
        items.add(i1);

        double i0_hpLeft = 15;
        double i1_hpLeft = 15;
        double i0_ehp = 15 / CriticalItemDamage.calculate(5, 20);
        double i1_ehp = 15 / CriticalItemDamage.calculate(15, 20);

        double ehpDealt = Math.min(i0_ehp, i1_ehp); // Deal enough effective damage to break the weakest component.
        i0_hpLeft -= ehpDealt * CriticalItemDamage.calculate(5, 20); // Figure out new actual HP
        i1_hpLeft -= ehpDealt * CriticalItemDamage.calculate(15, 20);

        assert (i1_hpLeft == 0.0); // Weakest component destroyed

        i0_ehp = (15 - i0_hpLeft) / CriticalItemDamage.calculate(5, 20); // The effective HP accumulated from first
                                                                         // round
        i0_ehp += i0_hpLeft / CriticalItemDamage.calculate(5, 5); // Critical slot count adjusted as i1 was destroyed

        assertEquals(i0_ehp, cut.calculate(i0), 0.00001);
        assertEquals(i1_ehp, cut.calculate(i1), 0.00001);
    }
}
