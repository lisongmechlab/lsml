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
package lisong_mechlab.model.chassi;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import junitparams.JUnitParamsRunner;
import lisong_mechlab.model.item.Engine;
import lisong_mechlab.model.item.Faction;
import lisong_mechlab.model.item.Item;
import lisong_mechlab.model.quirks.Quirks;
import lisong_mechlab.model.upgrades.Upgrades;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Matchers;
import org.mockito.Mockito;

/**
 * Test suite for {@link ChassisStandard}.
 * 
 * @author Li Song
 */
@RunWith(JUnitParamsRunner.class)
public class ChassisStandardTest extends ChassisBaseTest {

    private int                 engineMin;
    private int                 engineMax;
    private int                 maxJumpJets;
    private ComponentStandard[] components;
    private Quirks              quirks;

    @Override
    @Before
    public void setup() {
        super.setup();
        engineMin = 100;
        engineMax = 325;
        maxJumpJets = 2;
        components = new ComponentStandard[Location.values().length];
        for (Location location : Location.values()) {
            components[location.ordinal()] = Mockito.mock(ComponentStandard.class);
            Mockito.when(components[location.ordinal()].isAllowed(Matchers.any(Item.class))).thenReturn(true);
            Mockito.when(components[location.ordinal()].isAllowed(Matchers.any(Item.class), Matchers.any(Engine.class)))
                    .thenReturn(true);
        }
        componentBases = components;
        quirks = Mockito.mock(Quirks.class);
    }

    @Override
    protected ChassisStandard makeDefaultCUT() {
        return new ChassisStandard(mwoID, mwoName, series, name, shortName, maxTons, variant, baseVariant,
                movementProfile, faction, engineMin, engineMax, maxJumpJets, components, maxPilotModules,
                maxConsumableModules, maxWeaponModules, quirks);
    }

    /**
     * Internal parts list can not be modified.
     */
    @Test(expected = UnsupportedOperationException.class)
    public void testGetComponents_Immutable() {
        makeDefaultCUT().getComponents().add(null);
    }

    @Test
    public void testGetEngineMax() {
        assertEquals(engineMax, makeDefaultCUT().getEngineMax());
    }

    @Test
    public void testGetEngineMin() {
        assertEquals(engineMin, makeDefaultCUT().getEngineMin());
    }

    @Test
    public void testGetJumpJetsMax() {
        assertEquals(maxJumpJets, makeDefaultCUT().getJumpJetsMax());
    }

    @Test
    public void testGetHardPointsCount() {
        HardPointType hp = HardPointType.BALLISTIC;
        Mockito.when(components[Location.LeftArm.ordinal()].getHardPointCount(hp)).thenReturn(2);
        Mockito.when(components[Location.RightTorso.ordinal()].getHardPointCount(hp)).thenReturn(1);

        assertEquals(3, makeDefaultCUT().getHardPointsCount(hp));
    }

    @Test
    public void testIsAllowed_NoJJSupport() {
        maxJumpJets = 0;
        assertFalse(makeDefaultCUT().isAllowed(makeJumpJet(maxTons, maxTons + 1)));
    }

    @Test
    public void testIsAllowed_EngineTooSmall() {
        assertFalse(makeDefaultCUT().isAllowed(makeEngine(engineMin - 1)));
    }

    @Test
    public void testIsAllowed_EngineTooBig() {
        assertFalse(makeDefaultCUT().isAllowed(makeEngine(engineMax + 1)));
    }

    @Test
    public void testIsAllowed_EngineBigEnough() {
        assertTrue(makeDefaultCUT().isAllowed(makeEngine(engineMin)));
    }

    @Test
    public void testIsAllowed_ClanEngineIsChassis() {
        faction = Faction.Clan;
        Engine engine = makeEngine(engineMin);

        faction = Faction.InnerSphere;
        assertFalse(makeDefaultCUT().isAllowed(engine));
    }

    @Test
    public void testIsAllowed_IsEngineClanChassis() {
        faction = Faction.InnerSphere;
        Engine engine = makeEngine(engineMin);

        faction = Faction.Clan;
        assertFalse(makeDefaultCUT().isAllowed(engine));
    }

    @Test
    public void testIsAllowed_EngineSmalllEnough() {
        assertTrue(makeDefaultCUT().isAllowed(makeEngine(engineMax)));
    }

    @Test
    public final void testIsAllowed_NoComponentSupport() {
        Item item = Mockito.mock(Item.class);
        Mockito.when(item.getHardpointType()).thenReturn(HardPointType.NONE);
        Mockito.when(item.getFaction()).thenReturn(Faction.Clan);
        Mockito.when(item.isCompatible(Matchers.any(Upgrades.class))).thenReturn(true);

        ChassisStandard cut = makeDefaultCUT();
        assertTrue(cut.isAllowed(item)); // Item in it self is allowed

        // But no component supports it.
        for (Location location : Location.values()) {
            Mockito.when(components[location.ordinal()].isAllowed(item, null)).thenReturn(false);
        }
        assertFalse(cut.isAllowed(item));
    }
}
