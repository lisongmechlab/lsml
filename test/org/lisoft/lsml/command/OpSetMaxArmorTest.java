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
package org.lisoft.lsml.command;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.eq;
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
import org.lisoft.lsml.model.chassi.ArmorSide;
import org.lisoft.lsml.model.chassi.Location;
import org.lisoft.lsml.model.helpers.MockLoadoutContainer;
import org.lisoft.lsml.model.loadout.component.ComponentMessage;
import org.lisoft.lsml.model.loadout.component.ConfiguredComponentBase;
import org.lisoft.lsml.util.OperationStack;
import org.lisoft.lsml.util.message.MessageDelivery;
import org.mockito.ArgumentCaptor;
import org.mockito.InOrder;
import org.mockito.Matchers;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.runners.MockitoJUnitRunner;

/**
 * Test suite for {@link OpSetMaxArmor}.
 * 
 * @author Li Song
 */
@RunWith(MockitoJUnitRunner.class)
public class OpSetMaxArmorTest {
    private MockLoadoutContainer mlc = new MockLoadoutContainer();

    @Mock
    private MessageDelivery xBar;

    private final OperationStack          stack = new OperationStack(0);
    private List<ConfiguredComponentBase> components;
    private Map<Location, Integer>        maxArmor;

    public OpSetMaxArmor makeCut(double aRatio, boolean aManual) {
        Mockito.when(mlc.chassis.getMassMax()).thenReturn(100);
        Mockito.when(mlc.loadout.getFreeMass()).thenReturn(80.0);
        Mockito.when(mlc.loadout.getMassStructItems()).thenReturn(0.0);

        components = Arrays.asList(mlc.ra, mlc.rt, mlc.rl, mlc.hd, mlc.ct, mlc.lt, mlc.ll, mlc.la);
        maxArmor = new HashMap<>();
        int armorMax = 10;
        for (Location loc : Location.values()) {
            armorMax += 20;
            maxArmor.put(loc, armorMax);
            Mockito.when(mlc.loadout.getComponent(loc).getArmorMax(Matchers.any(ArmorSide.class))).thenReturn(armorMax);
            Mockito.when(mlc.loadout.getComponent(loc).getInternalComponent().getArmorMax()).thenReturn(armorMax);
        }

        return new OpSetMaxArmor(mlc.loadout, xBar, aRatio, aManual);
    }

    @Test
    public void testApply() {
        final int tolerance = 1;
        final double frontBackRatio = 3.0 / 2.0;
        final boolean manual = true;
        OpSetMaxArmor cut = makeCut(frontBackRatio, manual);

        stack.pushAndApply(cut);

        for (ConfiguredComponentBase component : components) {
            Location loc = component.getInternalComponent().getLocation();
            
            InOrder inOrder = inOrder(component);
            if (loc.isTwoSided()) {
                ArgumentCaptor<Integer> frontCaptor = ArgumentCaptor.forClass(Integer.class);
                ArgumentCaptor<Integer> backCaptor = ArgumentCaptor.forClass(Integer.class);
                inOrder.verify(component).setArmor(eq(ArmorSide.BACK), eq(0), eq(manual));
                inOrder.verify(component).setArmor(eq(ArmorSide.FRONT), frontCaptor.capture(), eq(manual));
                inOrder.verify(component).setArmor(eq(ArmorSide.BACK), backCaptor.capture(), eq(manual));
                int front = frontCaptor.getValue();
                int back = backCaptor.getValue();

                double lb = (double) (front - tolerance) / (back + tolerance);
                double ub = (double) (front + tolerance) / (back - tolerance);

                assertTrue(lb < frontBackRatio);
                assertTrue(ub > frontBackRatio);
                assertEquals(maxArmor.get(loc).intValue(), front + back);

                verify(xBar, atLeast(2))
                        .post(new ComponentMessage(component, ComponentMessage.Type.ArmorChanged, manual));

            }
            else {
                int expected = maxArmor.get(loc).intValue();
                verify(component).setArmor(ArmorSide.ONLY, expected, manual);
                verify(xBar, times(1))
                        .post(new ComponentMessage(component, ComponentMessage.Type.ArmorChanged, manual));
            }
        }
    }
}
