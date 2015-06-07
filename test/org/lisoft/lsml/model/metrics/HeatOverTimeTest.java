package org.lisoft.lsml.model.metrics;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.junit.Before;
import org.junit.Test;
import org.lisoft.lsml.model.helpers.MockLoadoutContainer;
import org.lisoft.lsml.model.item.EnergyWeapon;
import org.lisoft.lsml.model.item.Engine;
import org.lisoft.lsml.model.item.HeatSource;
import org.lisoft.lsml.model.item.ItemDB;
import org.lisoft.lsml.model.item.Weapon;
import org.lisoft.lsml.model.loadout.component.ConfiguredComponentBase;
import org.lisoft.lsml.util.message.MessageXBar;
import org.mockito.Mockito;

public class HeatOverTimeTest {
    private MessageXBar            xBar;
    private MockLoadoutContainer   mlc   = new MockLoadoutContainer();

    private final List<HeatSource> items = new ArrayList<>();

    @Before
    public void setup() {
        xBar = Mockito.mock(MessageXBar.class);
        Mockito.when(mlc.loadout.items(HeatSource.class)).thenReturn(items);
    }

    /**
     * Tag shall not contribute heat
     */
    @Test
    public void testCalculate_TAG() {
        EnergyWeapon erllas = (EnergyWeapon) ItemDB.lookup("TAG");
        items.add(erllas);

        HeatOverTime cut = new HeatOverTime(mlc.loadout, xBar);

        assertEquals(0, cut.calculate(0), 0.0);
        assertEquals(0, cut.calculate(100), 0.0);
    }

    @Test
    public void testCalculate_ERLLAS() {
        EnergyWeapon erllas = (EnergyWeapon) ItemDB.lookup("ER LARGE LASER");
        items.add(erllas);

        HeatOverTime cut = new HeatOverTime(mlc.loadout, xBar);

        assertEquals(0, cut.calculate(0), 0.0);
        assertEquals(erllas.getHeat(null) / 2, cut.calculate(erllas.getDuration(null) / 2), 0.0);
        assertEquals(erllas.getHeat(null) * 10.5,
                cut.calculate(erllas.getSecondsPerShot(null) * 10 + erllas.getDuration(null) / 2), 0.0);
    }

    @Test
    public void testCalculate_ERPPC() {
        EnergyWeapon erppc = (EnergyWeapon) ItemDB.lookup("ER PPC");
        items.add(erppc);

        HeatOverTime cut = new HeatOverTime(mlc.loadout, xBar);

        assertEquals(erppc.getHeat(null), cut.calculate(0), 0.0);
        assertEquals(erppc.getHeat(null), cut.calculate(0 + Math.ulp(1)), 0.0);
        assertEquals(erppc.getHeat(null) * 5, cut.calculate(erppc.getSecondsPerShot(null) * 5 - Math.ulp(1)), 0.0);
    }

    @Test
    public void testCalculate_AC20() {
        Weapon ac20 = (Weapon) ItemDB.lookup("AC/20");
        items.add(ac20);

        HeatOverTime cut = new HeatOverTime(mlc.loadout, xBar);

        assertEquals(ac20.getHeat(null), cut.calculate(0), 0.0);
        assertEquals(ac20.getHeat(null), cut.calculate(0 + Math.ulp(1)), 0.0);
        assertEquals(ac20.getHeat(null) * 5, cut.calculate(ac20.getSecondsPerShot(null) * 5 - Math.ulp(1)), 0.0);
    }

    @Test
    public void testCalculate_Engine() {
        Engine engine = (Engine) ItemDB.lookup("STD ENGINE 200");
        items.add(engine);

        HeatOverTime cut = new HeatOverTime(mlc.loadout, xBar);

        assertEquals(0, cut.calculate(0), 0.0);
        assertEquals(2, cut.calculate(10), 0.0);
        assertEquals(2.02, cut.calculate(10.1), 0.0);
    }

    @Test
    public void testCalculate_MultiItem() {
        Engine engine = (Engine) ItemDB.lookup("STD ENGINE 200");
        EnergyWeapon erllas = (EnergyWeapon) ItemDB.lookup("ER LARGE LASER");
        Weapon ac20 = (Weapon) ItemDB.lookup("AC/20");
        items.add(engine);
        items.add(erllas);
        items.add(ac20);

        HeatOverTime cut = new HeatOverTime(mlc.loadout, xBar);

        assertEquals(0.2 * 20 + ac20.getHeat(null) * 5 + erllas.getHeat(null) * 5, cut.calculate(20 - Math.ulp(20)),
                Math.ulp(80));
    }

    @Test
    public void testCalculate_WeaponGroup() {
        Engine engine = (Engine) ItemDB.lookup("STD ENGINE 200");
        EnergyWeapon erllas = (EnergyWeapon) ItemDB.lookup("ER LARGE LASER");
        Weapon ac20 = (Weapon) ItemDB.lookup("AC/20");
        items.add(engine);
        items.add(erllas);
        items.add(erllas);
        items.add(erllas);
        items.add(ac20);

        int group = 3;
        Collection<Weapon> weaponsGroup = new ArrayList<>();
        weaponsGroup.add(erllas);
        Mockito.when(mlc.weaponGroups.getWeapons(group)).thenReturn(weaponsGroup);

        HeatOverTime cut = new HeatOverTime(mlc.loadout, xBar, group);

        assertEquals(0.2 * 20 + erllas.getHeat(null) * 5, cut.calculate(20 - Math.ulp(20)), Math.ulp(80));
    }

    @Test
    public void testUpdate() {
        Engine engine = (Engine) ItemDB.lookup("STD ENGINE 200");
        EnergyWeapon erllas = (EnergyWeapon) ItemDB.lookup("ER LARGE LASER");
        Weapon ac20 = (Weapon) ItemDB.lookup("AC/20");
        items.add(engine);
        items.add(erllas);
        items.add(ac20);

        HeatOverTime cut = new HeatOverTime(mlc.loadout, xBar);
        Mockito.verify(xBar).attach(cut);

        double old = cut.calculate(20);
        items.remove(ac20);
        Collection<ConfiguredComponentBase> partLoadouts = Mockito.mock(Collection.class);
        Mockito.when(partLoadouts.contains(null)).thenReturn(true);
        Mockito.when(mlc.loadout.getComponents()).thenReturn(partLoadouts);
        cut.receive(new ConfiguredComponentBase.ComponentMessage(null,
                ConfiguredComponentBase.ComponentMessage.Type.ItemAdded));
        assertTrue(old != cut.calculate(20));
    }
}
