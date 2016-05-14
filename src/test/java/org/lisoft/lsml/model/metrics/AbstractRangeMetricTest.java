package org.lisoft.lsml.model.metrics;

import static org.junit.Assert.assertEquals;

import java.util.ArrayList;
import java.util.List;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.lisoft.lsml.model.datacache.ItemDB;
import org.lisoft.lsml.model.item.Weapon;
import org.lisoft.lsml.model.loadout.Loadout;
import org.lisoft.lsml.model.loadout.LoadoutStandard;
import org.lisoft.lsml.util.WeaponRanges;
import org.mockito.Matchers;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.runners.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class AbstractRangeMetricTest {
    class ConcreteAbstractCut extends AbstractRangeMetric {
        public ConcreteAbstractCut(LoadoutStandard aLoadout) {
            super(aLoadout);
        }

        @Override
        public double calculate(double aRange) {
            return 0;
        }
    }

    @Mock
    private LoadoutStandard     loadout;
    private ConcreteAbstractCut cut;
    private List<Weapon>        items = new ArrayList<>();

    @Before
    public void startup() {
        Mockito.when(loadout.items(Weapon.class)).thenReturn(items);
        cut = Mockito.spy(new ConcreteAbstractCut(loadout));
    }

    /**
     * After a call to {@link AbstractRangeMetric#calculate()}, {@link AbstractRangeMetric#getRange()} should return the range for which
     * {@link AbstractRangeMetric#calculate(double)} returned the highest value of the ranges determined by the weapons on the
     * loadout.
     */
    @Test
    public final void testGetRange() {
        // Should give ranges: 0, 270, 450, 540, 900
        items.add((Weapon) ItemDB.lookup("MEDIUM LASER"));
        items.add((Weapon) ItemDB.lookup("LARGE LASER"));

        Mockito.when(cut.calculate(Matchers.anyDouble())).thenReturn(0.0);
        Mockito.when(cut.calculate(270.0)).thenReturn(1.0);
        Mockito.when(cut.calculate(450.0)).thenReturn(3.0);
        Mockito.when(cut.calculate(540.0)).thenReturn(2.0);
        Mockito.when(cut.calculate(900.0)).thenReturn(1.0);

        cut.calculate();

        assertEquals(450.0, cut.getRange(), 0.0);
    }

    /**
     * If {@link AbstractRangeMetric#setRange(double)} has not been called; a call to {@link AbstractRangeMetric#calculate()} should
     * return the maximum value of {@link AbstractRangeMetric#calculate(double)} for all the ranges returned by
     * {@link WeaponRanges#getRanges(Loadout)}.
     */
    @Test
    public final void testCalculate_noChangeRange() {
        // Should give ranges: 0, 270, 450, 540, 900
        items.add((Weapon) ItemDB.lookup("MEDIUM LASER"));
        items.add((Weapon) ItemDB.lookup("LARGE LASER"));

        Mockito.when(cut.calculate(Matchers.anyDouble())).thenReturn(0.0);
        Mockito.when(cut.calculate(270.0)).thenReturn(1.0);
        Mockito.when(cut.calculate(450.0)).thenReturn(3.0);
        Mockito.when(cut.calculate(540.0)).thenReturn(2.0);
        Mockito.when(cut.calculate(900.0)).thenReturn(1.0);

        assertEquals(3.0, cut.calculate(), 0.0);
    }

    /**
     * If {@link AbstractRangeMetric#setRange(double)} was last called with a negative or zero argument; a call to
     * {@link AbstractRangeMetric#calculate()} should return the maximum value of {@link AbstractRangeMetric#calculate(double)} for all
     * the ranges returned by {@link WeaponRanges#getRanges(Loadout)}.
     */
    @Test
    public final void testCalculate_negativeChangeRange() {
        cut.setRange(10.0);
        cut.setRange(-1.0);

        testCalculate_noChangeRange();
    }

    /**
     * A call to {@link AbstractRangeMetric#calculate()} after {@link AbstractRangeMetric#setRange(double)} has been called with a
     * positive, non-negative argument should return the value of {@link AbstractRangeMetric#calculate(double)} called with the
     * same argument as {@link AbstractRangeMetric#setRange(double)} was called.
     */
    @Test
    public final void testCalculate_changeRange() {
        double range = 20.0;

        Mockito.when(cut.calculate(Matchers.anyDouble())).thenReturn(0.0);
        Mockito.when(cut.calculate(range)).thenReturn(1.0);

        cut.setRange(range);
        assertEquals(1.0, cut.calculate(), 0.0);
    }

}
