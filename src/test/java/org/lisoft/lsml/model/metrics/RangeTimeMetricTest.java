/*
 * @formatter:off
 * Li Song Mechlab - A 'mech building tool for PGI's MechWarrior: Online.
 * Copyright (C) 2014  Emily Björk
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
import static org.mockito.ArgumentMatchers.anyDouble;

import java.util.ArrayList;
import java.util.List;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.lisoft.lsml.model.database.ItemDB;
import org.lisoft.lsml.model.item.Weapon;
import org.lisoft.lsml.model.loadout.Loadout;
import org.lisoft.lsml.model.loadout.LoadoutStandard;
import org.lisoft.lsml.util.WeaponRanges;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.MockitoJUnitRunner;

/**
 * A test suite for {@link RangeTimeMetric} class.
 *
 * @author Emily Björk
 */
@SuppressWarnings("javadoc")
@RunWith(MockitoJUnitRunner.class)
public class RangeTimeMetricTest {
    static class ConcreteAbstractCut extends RangeTimeMetric {
        public ConcreteAbstractCut(LoadoutStandard aLoadout) {
            super(aLoadout);
        }

        @Override
        public double calculate(double aRange) {
            // TODO Auto-generated method stub
            return 0;
        }

        @Override
        public double calculate(double aRange, double aTime) {
            return 0;
        }
    }

    @Mock
    private LoadoutStandard loadout;
    private ConcreteAbstractCut cut;
    private final List<Weapon> items = new ArrayList<>();

    @Before
    public void startup() {
        Mockito.when(loadout.items(Weapon.class)).thenReturn(items);
        cut = Mockito.spy(new ConcreteAbstractCut(loadout));
    }

    /**
     * A call to {@link RangeTimeMetric#calculate()} after {@link RangeTimeMetric#setUserRange(double)} has been called
     * with a positive, non-negative argument should return the value of
     * {@link RangeTimeMetric#calculate(double, double)} called with the same argument as
     * {@link RangeTimeMetric#setUserRange(double)} was called.
     */
    @Test
    public final void testCalculate_changeRange() {
        final double range = 20.0;

        Mockito.when(cut.calculate(anyDouble(), anyDouble())).thenReturn(0.0);
        Mockito.when(cut.calculate(range, 10.0)).thenReturn(1.0);
        cut.changeTime(10.0); // Perform test for time 10.0
        cut.setUserRange(range);

        assertEquals(1.0, cut.calculate(), 0.0);
    }

    /**
     * If {@link RangeTimeMetric#setUserRange(double)} was last called with a negative or zero argument; a call to
     * {@link RangeTimeMetric#calculate()} should return the maximum value of
     * {@link RangeTimeMetric#calculate(double, double)} for all the ranges returned by
     * {@link WeaponRanges#getRanges(Loadout)}.
     */
    @Test
    public final void testCalculate_negativeChangeRange() throws Exception {
        cut.setUserRange(10.0);
        cut.setUserRange(-1.0);

        testCalculate_noChangeRange();
    }

    /**
     * If {@link RangeTimeMetric#setUserRange(double)} has not been called; a call to
     * {@link RangeTimeMetric#calculate()} should return the maximum value of
     * {@link RangeTimeMetric#calculate(double, double)} for all the ranges returned by
     * {@link WeaponRanges#getRanges(Loadout)}.
     */
    @Test
    public final void testCalculate_noChangeRange() throws Exception {
        // Should give ranges: 0, 270, 450, 540, 900
        items.add((Weapon) ItemDB.lookup("MEDIUM LASER"));
        items.add((Weapon) ItemDB.lookup("LARGE LASER"));

        cut.changeTime(10.0); // Perform test for time 10.0

        Mockito.when(cut.calculate(anyDouble(), anyDouble())).thenReturn(0.0);
        Mockito.when(cut.calculate(270.0, 10.0)).thenReturn(1.0);
        Mockito.when(cut.calculate(450.0, 10.0)).thenReturn(3.0);
        Mockito.when(cut.calculate(540.0, 10.0)).thenReturn(2.0);
        Mockito.when(cut.calculate(900.0, 10.0)).thenReturn(1.0);

        assertEquals(3.0, cut.calculate(), 0.0);
    }

    /**
     * After a call to {@link RangeTimeMetric#calculate()}, {@link RangeTimeMetric#getUserRange()} should return the
     * range for which {@link RangeTimeMetric#calculate(double, double)} returned the highest value of the ranges
     * determined by the weapons on the loadout.
     */
    @Test
    public final void testGetDisplayRange() throws Exception {
        // Should give ranges: 0, 270, 450, 540, 900
        items.add((Weapon) ItemDB.lookup("MEDIUM LASER"));
        items.add((Weapon) ItemDB.lookup("LARGE LASER"));

        cut.changeTime(10.0); // Perform test for time 10.0

        Mockito.when(cut.calculate(anyDouble(), anyDouble())).thenReturn(0.0);
        Mockito.when(cut.calculate(270.0, 10.0)).thenReturn(1.0);
        Mockito.when(cut.calculate(450.0, 10.0)).thenReturn(3.0);
        Mockito.when(cut.calculate(540.0, 10.0)).thenReturn(2.0);
        Mockito.when(cut.calculate(900.0, 10.0)).thenReturn(1.0);

        cut.calculate();

        assertEquals(450.0, cut.getDisplayRange(), 0.0);
    }

    /**
     * {@link RangeTimeMetric#getTime()} shall return the value of the last call to
     * {@link RangeTimeMetric#changeTime(double)}.
     */
    @Test
    public final void testGetTime() {
        cut.changeTime(13.13);
        assertEquals(13.13, cut.getTime(), 0.0);

        cut.changeTime(-13.13);
        assertEquals(-13.13, cut.getTime(), 0.0);
    }
}
