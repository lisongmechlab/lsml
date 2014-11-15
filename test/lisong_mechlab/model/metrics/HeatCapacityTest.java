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
package lisong_mechlab.model.metrics;

import static org.junit.Assert.assertEquals;

import java.util.ArrayList;
import java.util.List;

import lisong_mechlab.model.chassi.ChassisBase;
import lisong_mechlab.model.item.Engine;
import lisong_mechlab.model.item.HeatSink;
import lisong_mechlab.model.loadout.LoadoutBase;
import lisong_mechlab.model.quirks.Attribute;
import lisong_mechlab.model.quirks.Modifier;
import lisong_mechlab.model.quirks.ModifierDescription;
import lisong_mechlab.model.quirks.ModifierDescription.Operation;
import lisong_mechlab.model.quirks.ModifiersDB;
import lisong_mechlab.model.upgrades.HeatSinkUpgrade;
import lisong_mechlab.model.upgrades.Upgrades;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Matchers;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.runners.MockitoJUnitRunner;
import org.mockito.stubbing.Answer;

/**
 * Test suite for {@link HeatCapacity}
 * 
 * @author Emily Björk
 */
@RunWith(MockitoJUnitRunner.class)
public class HeatCapacityTest {
    @Mock
    Engine          engine;
    @Mock
    LoadoutBase<?>  loadout;
    List<Modifier>  modifiers          = new ArrayList<>();
    @Mock
    ChassisBase     chassis;
    @Mock
    HeatSink        heatSinkType;
    @Mock
    HeatSinkUpgrade heatSinkUpgrade;
    @Mock
    Upgrades        upgrades;

    final double    capacityFactor     = 1.3;
    final int       numExternalHs      = 5;
    final int       numInternalHs      = 9;
    final double    basecapacity       = 30;
    final double    internalHsCapacity = 2.0;
    final double    externalHsCapacity = 1.4;

    @Before
    public void setup() {
        Mockito.when(loadout.getHeatsinksCount()).thenReturn(numInternalHs + numExternalHs);
        Mockito.when(loadout.getModifiers()).thenReturn(modifiers);
        Mockito.when(loadout.getChassis()).thenReturn(chassis);
        Mockito.when(loadout.getEngine()).thenReturn(engine);
        Mockito.when(loadout.getUpgrades()).thenReturn(upgrades);
        Mockito.when(upgrades.getHeatSink()).thenReturn(heatSinkUpgrade);
        Mockito.when(heatSinkUpgrade.getHeatSinkType()).thenReturn(heatSinkType);
        Mockito.when(heatSinkType.getCapacity()).thenReturn(externalHsCapacity);
        Mockito.when(engine.getNumInternalHeatsinks()).thenReturn(numInternalHs);
    }

    /**
     * Each 'mech has a base heat capacity of 30 heat. Each single heat sink adds 1 capacity. Each double heat sink adds
     * 1.4 capacity. Except for the ones counted as engine internal heat sinks which count as 2.0 (those in engine slots
     * count as 1.4 still). These values are also affected by the efficiency modifier.
     */
    @Test
    public void testCalculate() {
        double expectedCapacity = (basecapacity + numInternalHs * internalHsCapacity + numExternalHs
                * externalHsCapacity)
                * capacityFactor;

        ModifierDescription description = Mockito.mock(ModifierDescription.class);
        Mockito.when(description.getOperation()).thenReturn(Operation.MULTIPLICATIVE);
        Mockito.when(description.affects(Matchers.any(Attribute.class))).then(new Answer<Boolean>() {
            @Override
            public Boolean answer(InvocationOnMock aInvocation) throws Throwable {
                Attribute a = (Attribute) aInvocation.getArguments()[0];
                return a.getSelectors().contains(ModifiersDB.SEL_HEAT_LIMIT);
            }
        });

        Modifier heatlimit = Mockito.mock(Modifier.class);
        Mockito.when(heatlimit.getValue()).thenReturn(capacityFactor - 1.0);
        Mockito.when(heatlimit.getDescription()).thenReturn(description);
        modifiers.add(heatlimit);
        Mockito.when(heatSinkUpgrade.isDouble()).thenReturn(true);
        Mockito.when(heatSinkType.isDouble()).thenReturn(true);

        HeatCapacity cut = new HeatCapacity(loadout);

        assertEquals(expectedCapacity, cut.calculate(), 0.0);
    }

}
