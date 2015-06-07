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
package org.lisoft.lsml.model.modifiers;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;
import static org.mockito.Mockito.reset;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyZeroInteractions;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.lisoft.lsml.model.modifiers.Efficiencies.EfficienciesMessage.Type;
import org.lisoft.lsml.util.message.MessageXBar;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class EfficienciesTest {
    @Mock
    private MessageXBar  xBar;
    private Efficiencies cut;

    @Before
    public void setup() {
        cut = new Efficiencies();
    }

    @Test
    public void testEquals() {
        Efficiencies cut1 = new Efficiencies();

        assertEquals(cut, cut);
        assertEquals(cut, cut1);

        cut1.setAnchorTurn(true, null);
        assertNotEquals(cut, cut1);
        cut1.setAnchorTurn(false, null);

        cut1.setCoolRun(true, null);
        assertNotEquals(cut, cut1);
        cut1.setCoolRun(false, null);

        cut1.setDoubleBasics(true, null);
        assertNotEquals(cut, cut1);
        cut1.setDoubleBasics(false, null);

        cut1.setFastFire(true, null);
        assertNotEquals(cut, cut1);
        cut1.setFastFire(false, null);

        cut1.setHeatContainment(true, null);
        assertNotEquals(cut, cut1);
        cut1.setHeatContainment(false, null);

        cut1.setSpeedTweak(true, null);
        assertNotEquals(cut, cut1);
        cut1.setSpeedTweak(false, null);
    }

    @Test
    public void testSetHasSpeedTweak() throws Exception {
        // Default false
        assertEquals(false, cut.hasSpeedTweak());
        verifyZeroInteractions(xBar);

        // We want messages too!
        for (boolean b : new boolean[] { true, false }) {
            cut.setSpeedTweak(b, xBar);
            assertEquals(b, cut.hasSpeedTweak());
            verify(xBar).post(new Efficiencies.EfficienciesMessage(cut, Type.Changed));
            reset(xBar);
        }

        // No messages if there was no change.
        for (boolean b : new boolean[] { true, false }) {
            cut.setSpeedTweak(b, xBar);
            reset(xBar);
            cut.setSpeedTweak(b, xBar);
            verifyZeroInteractions(xBar);
        }
    }

    @Test
    public void testSpeedModifier() throws Exception {
        Attribute attribute = new Attribute(1.0, ModifiersDB.SEL_MOVEMENT_MAX_SPEED);
        cut.setSpeedTweak(true, null);
                
        assertEquals(1.1, attribute.value(cut.getModifiers()), 0.0);
    }

    @Test
    public void testSetHasCoolRun() throws Exception {
        // Default false
        assertEquals(false, cut.hasCoolRun());
        verifyZeroInteractions(xBar);

        // We want messages too!
        for (boolean b : new boolean[] { true, false }) {
            cut.setCoolRun(b, xBar);
            assertEquals(b, cut.hasCoolRun());
            verify(xBar).post(new Efficiencies.EfficienciesMessage(cut, Type.Changed));
            reset(xBar);
        }

        // No messages if there was no change.
        for (boolean b : new boolean[] { true, false }) {
            cut.setCoolRun(b, xBar);
            reset(xBar);
            cut.setCoolRun(b, xBar);
            verifyZeroInteractions(xBar);
        }
    }

    @Test
    public void testGetHeatDissipationModifier() throws Exception {
        Attribute attribute = new Attribute(1.0, ModifiersDB.SEL_HEAT_DISSIPATION);
        cut.setCoolRun(true, null);
        
        assertEquals(1.075, attribute.value(cut.getModifiers()), 0.0);
        
        cut.setDoubleBasics(true, null);
        assertEquals(1.15, attribute.value(cut.getModifiers()), 0.0);
    }

    @Test
    public void testSetHasHeatContainment() throws Exception {
        // Default false
        assertEquals(false, cut.hasHeatContainment());
        verifyZeroInteractions(xBar);

        // We want messages too!
        for (boolean b : new boolean[] { true, false }) {
            cut.setHeatContainment(b, xBar);
            assertEquals(b, cut.hasHeatContainment());
            verify(xBar).post(new Efficiencies.EfficienciesMessage(cut, Type.Changed));
            reset(xBar);
        }

        // No messages if there was no change.
        for (boolean b : new boolean[] { true, false }) {
            cut.setHeatContainment(b, xBar);
            reset(xBar);
            cut.setHeatContainment(b, xBar);
            verifyZeroInteractions(xBar);
        }
    }

    @Test
    public void testGetHeatCapacityModifier() throws Exception {
        Attribute attribute = new Attribute(1.0, ModifiersDB.SEL_HEAT_LIMIT);
        cut.setHeatContainment(true, null);
        
        assertEquals(1.1, attribute.value(cut.getModifiers()), 0.0);
        
        cut.setDoubleBasics(true, null);
        assertEquals(1.2, attribute.value(cut.getModifiers()), 0.0);
    }

    @Test
    public void testSetHasDoubleBasics() throws Exception {
        // Default false
        assertEquals(false, cut.hasDoubleBasics());
        verifyZeroInteractions(xBar);

        // We want messages too!
        for (boolean b : new boolean[] { true, false }) {
            cut.setDoubleBasics(b, xBar);
            assertEquals(b, cut.hasDoubleBasics());
            verify(xBar).post(new Efficiencies.EfficienciesMessage(cut, Type.Changed));
            reset(xBar);
        }

        // No messages if there was no change.
        for (boolean b : new boolean[] { true, false }) {
            cut.setDoubleBasics(b, xBar);
            reset(xBar);
            cut.setDoubleBasics(b, xBar);
            verifyZeroInteractions(xBar);
        }
    }

    @Test
    public void testSetHasFastFire() throws Exception {
        // Default false
        assertEquals(false, cut.hasFastFire());
        verifyZeroInteractions(xBar);

        // We want messages too!
        for (boolean b : new boolean[] { true, false }) {
            cut.setFastFire(b, xBar);
            assertEquals(b, cut.hasFastFire());
            verify(xBar).post(new Efficiencies.EfficienciesMessage(cut, Type.Changed));
            reset(xBar);
        }

        // No messages if there was no change.
        for (boolean b : new boolean[] { true, false }) {
            cut.setFastFire(b, xBar);
            reset(xBar);
            cut.setFastFire(b, xBar);
            verifyZeroInteractions(xBar);
        }
    }

    @Test
    public void testGetWeaponCycletimeModifier() throws Exception {
        Attribute attribute = new Attribute(1.0, ModifiersDB.ALL_WEAPONS , ModifiersDB.SEL_WEAPON_COOLDOWN);
        cut.setFastFire(true, null);
                
        assertEquals(0.95, attribute.value(cut.getModifiers()), 0.0);
    }

}
