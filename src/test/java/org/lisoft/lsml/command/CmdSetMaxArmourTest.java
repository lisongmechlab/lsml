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
package org.lisoft.lsml.command;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.atLeast;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.lisoft.lsml.messages.ArmourMessage;
import org.lisoft.lsml.messages.MessageDelivery;
import org.lisoft.lsml.model.chassi.ArmourSide;
import org.lisoft.lsml.model.chassi.Location;
import org.lisoft.lsml.model.helpers.MockLoadoutContainer;
import org.lisoft.lsml.model.loadout.ConfiguredComponent;
import org.lisoft.lsml.util.CommandStack;
import org.mockito.ArgumentCaptor;
import org.mockito.InOrder;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.MockitoJUnitRunner;

/**
 * Test suite for {@link CmdSetMaxArmour}.
 *
 * @author Emily Björk
 */
@RunWith(MockitoJUnitRunner.Silent.class)
public class CmdSetMaxArmourTest {
    private final MockLoadoutContainer mlc = new MockLoadoutContainer();

    @Mock
    private MessageDelivery xBar;

    private final CommandStack stack = new CommandStack(0);
    private List<ConfiguredComponent> components;
    private Map<Location, Integer> maxArmour;

    public CmdSetMaxArmour makeCut(double aRatio, boolean aManual) {
        Mockito.when(mlc.chassis.getMassMax()).thenReturn(100);
        Mockito.when(mlc.loadout.getFreeMass()).thenReturn(80.0);
        Mockito.when(mlc.loadout.getMassStructItems()).thenReturn(0.0);

        components = Arrays.asList(mlc.ra, mlc.rt, mlc.rl, mlc.hd, mlc.ct, mlc.lt, mlc.ll, mlc.la);
        maxArmour = new HashMap<>();
        int armourMax = 10;
        for (final Location loc : Location.values()) {
            armourMax += 20;
            maxArmour.put(loc, armourMax);
            Mockito.when(mlc.loadout.getComponent(loc).getArmourMax(any(ArmourSide.class))).thenReturn(armourMax);
            Mockito.when(mlc.loadout.getComponent(loc).getInternalComponent().getArmourMax()).thenReturn(armourMax);
        }

        return new CmdSetMaxArmour(mlc.loadout, xBar, aRatio, aManual);
    }

    @Test
    public void testApply() throws Exception {
        final int tolerance = 1;
        final double frontBackRatio = 3.0 / 2.0;
        final boolean manual = true;
        final CmdSetMaxArmour cut = makeCut(frontBackRatio, manual);

        stack.pushAndApply(cut);

        for (final ConfiguredComponent component : components) {
            final Location loc = component.getInternalComponent().getLocation();

            final InOrder inOrder = inOrder(component);
            if (loc.isTwoSided()) {
                final ArgumentCaptor<Integer> frontCaptor = ArgumentCaptor.forClass(Integer.class);
                final ArgumentCaptor<Integer> backCaptor = ArgumentCaptor.forClass(Integer.class);
                inOrder.verify(component).setArmour(eq(ArmourSide.BACK), eq(0), eq(manual));
                inOrder.verify(component).setArmour(eq(ArmourSide.FRONT), frontCaptor.capture(), eq(manual));
                inOrder.verify(component).setArmour(eq(ArmourSide.BACK), backCaptor.capture(), eq(manual));
                final int front = frontCaptor.getValue();
                final int back = backCaptor.getValue();

                final double lb = (double) (front - tolerance) / (back + tolerance);
                final double ub = (double) (front + tolerance) / (back - tolerance);

                assertTrue(lb < frontBackRatio);
                assertTrue(ub > frontBackRatio);
                assertEquals(maxArmour.get(loc).intValue(), front + back);

                verify(xBar, atLeast(2)).post(new ArmourMessage(component, ArmourMessage.Type.ARMOUR_CHANGED, manual));

            }
            else {
                final int expected = maxArmour.get(loc).intValue();
                verify(component).setArmour(ArmourSide.ONLY, expected, manual);
                verify(xBar, times(1)).post(new ArmourMessage(component, ArmourMessage.Type.ARMOUR_CHANGED, manual));
            }
        }
    }
}
