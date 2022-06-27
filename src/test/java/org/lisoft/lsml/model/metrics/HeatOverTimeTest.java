package org.lisoft.lsml.model.metrics;

import org.junit.Before;
import org.junit.Test;
import org.lisoft.lsml.messages.Message;
import org.lisoft.lsml.messages.MessageXBar;
import org.lisoft.lsml.model.helpers.MockLoadoutContainer;
import org.lisoft.lsml.model.item.*;
import org.lisoft.lsml.model.loadout.ConfiguredComponent;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * Test suite for {@link HeatOverTime}.
 *
 * @author Li Song
 */
@SuppressWarnings({"unchecked"})
public class HeatOverTimeTest {
    private final List<HeatSource> items = new ArrayList<>();
    private final MockLoadoutContainer mlc = new MockLoadoutContainer();
    private BallisticWeapon ac20;
    private Engine engine;
    private EnergyWeapon erLargeLaser;
    private EnergyWeapon erPPC;
    private EnergyWeapon tag;
    private MessageXBar xBar;

    @Before
    public void setup() {
        xBar = mock(MessageXBar.class);

        engine = mock(Engine.class);
        when(engine.getHeat(any())).thenReturn(0.2);

        erLargeLaser = mock(EnergyWeapon.class);
        when(erLargeLaser.getDuration(any())).thenReturn(1.1);
        when(erLargeLaser.getExpectedFiringPeriod(any())).thenReturn(4.5);
        when(erLargeLaser.getHeat(any())).thenReturn(8.75);

        erPPC = mock(EnergyWeapon.class);
        when(erPPC.getExpectedFiringPeriod(any())).thenReturn(4.0);
        when(erPPC.getHeat(any())).thenReturn(13.5);

        tag = mock(EnergyWeapon.class);
        when(tag.getExpectedFiringPeriod(any())).thenReturn(1.0);
        when(tag.getHeat(any())).thenReturn(0.0);

        ac20 = mock(BallisticWeapon.class);
        when(ac20.getExpectedFiringPeriod(any())).thenReturn(4.0);
        when(ac20.getHeat(any())).thenReturn(6.0);

        when(mlc.loadout.items(HeatSource.class)).thenReturn(items);
    }

    @Test
    public void testCalculate_AC20() {
        items.add(ac20);

        final HeatOverTime cut = new HeatOverTime(mlc.loadout, xBar);

        assertEquals(ac20.getHeat(null), cut.calculate(0), 0.0);
        assertEquals(ac20.getHeat(null), cut.calculate(0 + Math.ulp(1)), 0.0);
        assertEquals(ac20.getHeat(null) * 5, cut.calculate(ac20.getExpectedFiringPeriod(null) * 5 - Math.ulp(1)), 0.0);
    }

    @Test
    public void testCalculate_ERLLAS() {
        items.add(erLargeLaser);

        final HeatOverTime cut = new HeatOverTime(mlc.loadout, xBar);

        assertEquals(0, cut.calculate(0), 1E-6);
        assertEquals(erLargeLaser.getHeat(null) / 2, cut.calculate(erLargeLaser.getDuration(null) / 2), 1E-6);
        assertEquals(erLargeLaser.getHeat(null) * 10.5, cut.calculate(
                erLargeLaser.getExpectedFiringPeriod(null) * 10 + erLargeLaser.getDuration(null) / 2), 1E-6);
    }

    @Test
    public void testCalculate_ERPPC() {
        items.add(erPPC);

        final HeatOverTime cut = new HeatOverTime(mlc.loadout, xBar);

        assertEquals(erPPC.getHeat(null), cut.calculate(0), 0.0);
        assertEquals(erPPC.getHeat(null), cut.calculate(0 + Math.ulp(1)), 0.0);
        assertEquals(erPPC.getHeat(null) * 5, cut.calculate(erPPC.getExpectedFiringPeriod(null) * 5 - Math.ulp(1)),
                     0.0);
    }

    @Test
    public void testCalculate_Engine() {
        items.add(engine);

        final HeatOverTime cut = new HeatOverTime(mlc.loadout, xBar);

        assertEquals(0, cut.calculate(0), 0.0);
        assertEquals(2, cut.calculate(10), 0.0);
        assertEquals(2.02, cut.calculate(10.1), 0.0);
    }

    @Test
    public void testCalculate_MultiItem() {
        items.add(engine);
        items.add(erLargeLaser);
        items.add(ac20);

        final HeatOverTime cut = new HeatOverTime(mlc.loadout, xBar);

        assertEquals(0.2 * 20 + ac20.getHeat(null) * 5 + erLargeLaser.getHeat(null) * 5,
                     cut.calculate(20 - Math.ulp(20)), Math.ulp(80));
    }

    /**
     * Tag shall not contribute heat
     */
    @Test
    public void testCalculate_TAG() {
        items.add(tag);

        final HeatOverTime cut = new HeatOverTime(mlc.loadout, xBar);

        assertEquals(0, cut.calculate(0), 0.0);
        assertEquals(0, cut.calculate(100), 0.0);
    }

    @Test
    public void testCalculate_WeaponGroup() {
        items.add(engine);
        items.add(erLargeLaser);
        items.add(erLargeLaser);
        items.add(erLargeLaser);
        items.add(ac20);

        final int group = 3;
        final Collection<Weapon> weaponsGroup = new ArrayList<>();
        weaponsGroup.add(erLargeLaser);
        when(mlc.weaponGroups.getWeapons(group, mlc.loadout)).thenReturn(weaponsGroup);

        final HeatOverTime cut = new HeatOverTime(mlc.loadout, xBar, group);

        assertEquals(0.2 * 20 + erLargeLaser.getHeat(null) * 5, cut.calculate(20 - Math.ulp(20)), Math.ulp(80));
    }

    @Test
    public void testUpdate() {
        items.add(engine);
        items.add(erLargeLaser);
        items.add(ac20);

        final HeatOverTime cut = new HeatOverTime(mlc.loadout, xBar);
        verify(xBar).attach(cut);

        final double old = cut.calculate(20);

        items.remove(ac20);
        final Collection<ConfiguredComponent> partLoadouts = mock(Collection.class);
        when(partLoadouts.contains(null)).thenReturn(true);
        when(mlc.loadout.getComponents()).thenReturn(partLoadouts);

        final Message msg = mock(Message.class);
        when(msg.isForMe(mlc.loadout)).thenReturn(true);
        when(msg.affectsHeatOrDamage()).thenReturn(true);
        cut.receive(msg);
        assertTrue(old != cut.calculate(20));
    }
}
