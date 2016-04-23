package org.lisoft.lsml.model.metrics;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.junit.Before;
import org.junit.Test;
import org.lisoft.lsml.model.datacache.ItemDB;
import org.lisoft.lsml.model.helpers.MockLoadoutContainer;
import org.lisoft.lsml.model.item.Weapon;
import org.mockito.Mockito;

public class GhostHeatTest {
    final static private Weapon ppc = (Weapon) ItemDB.lookup("PPC");
    MockLoadoutContainer mlc = new MockLoadoutContainer();

    private GhostHeat cut;
    private final List<Weapon> weapons = new ArrayList<>();

    @Before
    public void setup() {
        when(mlc.loadout.items(Weapon.class)).thenReturn(weapons);
        cut = new GhostHeat(mlc.loadout);
    }

    @Test
    public void testCalculate_13FrickenLasers() throws Exception {
        // Acording to personal conversation with Karl Berg:
        // "Hi Li Song! I'll go bug Paul again right now. I didn't get a response to my earlier email.
        // Ok, the last number simply caps and repeats for any weapons past 13."
        final Weapon slas = (Weapon) ItemDB.lookup("SMALL LASER");
        final int lasers = 12;
        for (int i = 0; i < lasers; ++i) {
            weapons.add(slas);
        }
        final double result12 = cut.calculate();
        weapons.add(slas);
        final double maxHeatScale = 5.0;

        // Execute
        final double result13 = cut.calculate();

        // Verify
        final double expected = result12 + slas.getHeat(null) * maxHeatScale * slas.getGhostHeatMultiplier();
        assertEquals(expected, result13, 0.0);
    }

    @Test
    public void testCalculate_2ppc() throws Exception {
        // Example from: http://mwomercs.com/forums/topic/127904-heat-scale-the-maths/
        weapons.add(ppc);
        weapons.add(ppc);

        final double result = cut.calculate();
        assertEquals(0.0, result, 0.0);
    }

    @Test
    public void testCalculate_3ppc() throws Exception {
        // Example from: http://mwomercs.com/forums/topic/127904-heat-scale-the-maths/
        weapons.add(ppc);
        weapons.add(ppc);
        weapons.add(ppc);

        final double result = cut.calculate();
        assertEquals(11.97, result, 0.01); // Adjusting for new base head (9.5)
    }

    @Test
    public void testCalculate_4ppc() throws Exception {
        // Example from: http://mwomercs.com/forums/topic/127904-heat-scale-the-maths/
        weapons.add(ppc);
        weapons.add(ppc);
        weapons.add(ppc);
        weapons.add(ppc);

        final double result = cut.calculate();
        assertEquals(31.92, result, 0.01);// Adjusting for new base head (9.5)
    }

    @Test
    public void testCalculate_linkedMixedGroup() throws Exception {
        // Example from: http://mwomercs.com/forums/topic/127904-heat-scale-the-maths/

        final Weapon lplas = (Weapon) ItemDB.lookup("LRG PULSE LASER");
        final Weapon erllas = (Weapon) ItemDB.lookup("ER LARGE LASER");
        final Weapon llas = (Weapon) ItemDB.lookup("LARGE LASER");

        weapons.add(erllas);
        weapons.add(llas);
        weapons.add(lplas);
        weapons.add(llas);
        weapons.add(llas);

        Weapon max = lplas.getHeat(null) > erllas.getHeat(null) ? lplas : erllas;
        max = max.getHeat(null) > llas.getHeat(null) ? max : llas;

        final double result = cut.calculate();
        assertEquals(max.getGhostHeatMultiplier() * max.getHeat(null) * (0.30 + 0.45), result, 0.0001);
    }

    @Test
    public void testCalculate_unlinkedMixedGroup() throws Exception {
        // Example from: http://mwomercs.com/forums/topic/127904-heat-scale-the-maths/

        final Weapon ac20 = (Weapon) ItemDB.lookup("AC/20");
        final Weapon mlas = (Weapon) ItemDB.lookup("MEDIUM LASER");
        weapons.add(ac20);
        weapons.add(mlas);
        weapons.add(ac20);
        weapons.add(mlas);
        weapons.add(mlas);
        weapons.add(mlas);
        weapons.add(mlas);
        weapons.add(mlas);
        weapons.add(mlas);
        weapons.add(mlas);
        weapons.add(mlas);

        final double result = cut.calculate();
        final double ac20penalty = ac20.getGhostHeatMultiplier() * ac20.getHeat(null) * 0.08;
        final double mlaspenalty = mlas.getGhostHeatMultiplier() * mlas.getHeat(null) * (0.80 + 1.10 + 1.50);
        assertEquals(ac20penalty + mlaspenalty, result, 0.0001);
    }

    @Test
    public void testCalculate_unpenalizedWeapons() throws Exception {
        // Example from: http://mwomercs.com/forums/topic/127904-heat-scale-the-maths/
        weapons.add((Weapon) ItemDB.lookup("SMALL LASER"));
        weapons.add((Weapon) ItemDB.lookup("SML PULSE LASER"));
        weapons.add((Weapon) ItemDB.lookup("AC/5"));
        weapons.add((Weapon) ItemDB.lookup("LRM5"));
        weapons.add((Weapon) ItemDB.lookup("FLAMER"));

        final double result = cut.calculate();
        assertEquals(0.0, result, 0.0);
    }

    @Test
    public void testCalculate_WeaponGroups() throws Exception {
        // Example from: http://mwomercs.com/forums/topic/127904-heat-scale-the-maths/

        final int aGroup = 3;
        final Collection<Weapon> groupWeapons = new ArrayList<>();
        groupWeapons.add(ppc);
        groupWeapons.add(ppc);
        groupWeapons.add(ppc);
        groupWeapons.add(ppc);
        Mockito.when(mlc.weaponGroups.getWeapons(aGroup, mlc.loadout)).thenReturn(groupWeapons);

        weapons.add(ppc);
        weapons.add(ppc);
        weapons.add(ppc);
        weapons.add(ppc);
        weapons.add(ppc);
        weapons.add(ppc);
        weapons.add(ppc);
        weapons.add(ppc);

        cut = new GhostHeat(mlc.loadout, aGroup);
        final double result = cut.calculate();
        assertEquals(31.92, result, 0.01); // Adjusting for new base head (9.5)
    }
}
