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
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.List;

import lisong_mechlab.model.chassi.ChassisBase;
import lisong_mechlab.model.environment.Environment;
import lisong_mechlab.model.item.Engine;
import lisong_mechlab.model.item.HeatSink;
import lisong_mechlab.model.loadout.LoadoutBase;
import lisong_mechlab.model.modifiers.Attribute;
import lisong_mechlab.model.modifiers.Modifier;
import lisong_mechlab.model.modifiers.ModifierDescription;
import lisong_mechlab.model.modifiers.ModifierDescription.Operation;
import lisong_mechlab.model.modifiers.ModifiersDB;
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
 * Test suite for {@link HeatDissipation}.
 * 
 * @author Li Song
 */
@RunWith(MockitoJUnitRunner.class)
public class HeatDissipationTest {
    @Mock
    Engine          engine;
    @Mock
    LoadoutBase<?>  loadout;
    List<Modifier>  modifiers             = new ArrayList<>();
    @Mock
    ChassisBase     chassis;
    @Mock
    HeatSink        heatSinkType;
    @Mock
    HeatSinkUpgrade heatSinkUpgrade;
    @Mock
    Upgrades        upgrades;

    final double    dissipationFactor     = 1.3;
    final int       numExternalHs         = 5;
    final int       numInternalHs         = 9;
    final double    internalHsDissipation = 0.2;
    final double    externalHsDissipation = 0.14;

    @Before
    public void setup() {
        Mockito.when(loadout.getHeatsinksCount()).thenReturn(numInternalHs + numExternalHs);
        Mockito.when(loadout.getModifiers()).thenReturn(modifiers);
        Mockito.when(loadout.getChassis()).thenReturn(chassis);
        Mockito.when(loadout.getEngine()).thenReturn(engine);
        Mockito.when(loadout.getUpgrades()).thenReturn(upgrades);
        Mockito.when(upgrades.getHeatSink()).thenReturn(heatSinkUpgrade);
        Mockito.when(heatSinkUpgrade.getHeatSinkType()).thenReturn(heatSinkType);
        Mockito.when(heatSinkType.getDissipation()).thenReturn(externalHsDissipation);
        Mockito.when(engine.getNumInternalHeatsinks()).thenReturn(numInternalHs);
    }

    /**
     * The heat dissipation of a 'mech is dependent on the heat sink types. > For single heat sinks it is simply the
     * number of heat sinks multiplied by any modifier from the efficiencies. > For double heat sinks it each engine
     * internal heat sink counts as 0.2 and any other heat sinks count as 0.14.
     */
    @Test
    public void testCalculate() {
        double expectedDissipation = (numInternalHs * internalHsDissipation + numExternalHs * externalHsDissipation)
                * dissipationFactor;
        
        ModifierDescription description = Mockito.mock(ModifierDescription.class);
        Mockito.when(description.getOperation()).thenReturn(Operation.MULTIPLICATIVE);
        Mockito.when(description.affects(Matchers.any(Attribute.class))).then(new Answer<Boolean>() {
            @Override
            public Boolean answer(InvocationOnMock aInvocation) throws Throwable {
                Attribute a = (Attribute) aInvocation.getArguments()[0];
                return a.getSelectors().contains(ModifiersDB.SEL_HEAT_DISSIPATION);
            }
        });

        Modifier heatdissipation = Mockito.mock(Modifier.class);
        Mockito.when(heatdissipation.getValue()).thenReturn(dissipationFactor - 1.0);
        Mockito.when(heatdissipation.getDescription()).thenReturn(description);
        modifiers.add(heatdissipation);
        Mockito.when(heatSinkUpgrade.isDouble()).thenReturn(true);
        Mockito.when(heatSinkType.isDouble()).thenReturn(true);
        

        Environment environment = mock(Environment.class);
        final double environmentHeat = 0.3;
        when(environment.getHeat(modifiers)).thenReturn(environmentHeat);
        
        HeatDissipation cut = new HeatDissipation(loadout, environment);
        expectedDissipation -= environmentHeat;
        
        assertEquals(expectedDissipation, cut.calculate(), Math.ulp(expectedDissipation) * 4);
    }
}
