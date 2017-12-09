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
package org.lisoft.lsml.model.chassi;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.isA;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.List;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.lisoft.lsml.model.database.UpgradeDB;
import org.lisoft.lsml.model.item.Engine;
import org.lisoft.lsml.model.item.Faction;
import org.lisoft.lsml.model.item.Item;
import org.lisoft.lsml.model.modifiers.Modifier;
import org.lisoft.lsml.model.upgrades.ArmourUpgrade;
import org.lisoft.lsml.model.upgrades.HeatSinkUpgrade;
import org.lisoft.lsml.model.upgrades.StructureUpgrade;
import org.lisoft.lsml.model.upgrades.Upgrades;

import junitparams.JUnitParamsRunner;

/**
 * Test suite for {@link ChassisStandard}.
 *
 * @author Emily Björk
 */
@RunWith(JUnitParamsRunner.class)
public class ChassisStandardTest extends ChassisTest {

    private int engineMin;
    private int engineMax;
    private int maxJumpJets;
    private ComponentStandard[] components;
    private final List<Modifier> quirks = new ArrayList<>();

    @Override
    @Before
    public void setup() {
        super.setup();
        engineMin = 100;
        engineMax = 325;
        maxJumpJets = 2;
        components = new ComponentStandard[Location.values().length];
        for (final Location location : Location.values()) {
            components[location.ordinal()] = mock(ComponentStandard.class);
            when(components[location.ordinal()].isAllowed(isA(Item.class), any())).thenReturn(true);
        }
        componentBases = components;
    }

    @Test
    public final void testCanUseUpgrade_Armour() {
        faction = Faction.CLAN;
        final ArmourUpgrade armourWrongFaction = mock(ArmourUpgrade.class);
        when(armourWrongFaction.getFaction()).thenReturn(Faction.INNERSPHERE);

        final ArmourUpgrade armourRightFaction = mock(ArmourUpgrade.class);
        when(armourRightFaction.getFaction()).thenReturn(faction);

        assertFalse(makeDefaultCUT().canUseUpgrade(armourWrongFaction));
        assertTrue(makeDefaultCUT().canUseUpgrade(armourRightFaction));
    }

    @Test
    public final void testCanUseUpgrade_HeatSinks() {
        faction = Faction.CLAN;
        final HeatSinkUpgrade heatSinksWrongFaction = mock(HeatSinkUpgrade.class);
        when(heatSinksWrongFaction.getFaction()).thenReturn(Faction.INNERSPHERE);

        final HeatSinkUpgrade heatSinksRightFaction = mock(HeatSinkUpgrade.class);
        when(heatSinksRightFaction.getFaction()).thenReturn(faction);

        assertFalse(makeDefaultCUT().canUseUpgrade(heatSinksWrongFaction));
        assertTrue(makeDefaultCUT().canUseUpgrade(heatSinksRightFaction));
    }

    @Test
    public final void testCanUseUpgrade_StealthArmour() {
        // Stealth armour has ID: 2814, it has no other reliably discernible attribute from
        // other armour types right now other than name and ID. So we choose to use the ID
        // which is immune to changes in the name due to translations. IDs have also proven
        // to be static with upstream so I deem this to be the safest choice, even though it
        // is ugly AF.
        final int stealthArmourID = UpgradeDB.STEALTH_ARMOUR_ID;

        faction = Faction.INNERSPHERE;
        final ArmourUpgrade armourWrongFaction = mock(ArmourUpgrade.class);
        when(armourWrongFaction.getFaction()).thenReturn(Faction.CLAN);
        when(armourWrongFaction.getId()).thenReturn(stealthArmourID);

        final ArmourUpgrade armourRightFaction = mock(ArmourUpgrade.class);
        when(armourRightFaction.getFaction()).thenReturn(faction);
        when(armourRightFaction.getId()).thenReturn(stealthArmourID);

        // No ECM hard point: No stealth armour for you!
        assertFalse(makeDefaultCUT().canUseUpgrade(armourRightFaction));
        assertFalse(makeDefaultCUT().canUseUpgrade(armourWrongFaction));

        // Has ECM? You get a stealth armour!
        when(components[Location.CenterTorso.ordinal()].getHardPointCount(HardPointType.ECM)).thenReturn(1);
        assertTrue(makeDefaultCUT().canUseUpgrade(armourRightFaction));
        assertFalse(makeDefaultCUT().canUseUpgrade(armourWrongFaction));
    }

    @Test
    public final void testCanUseUpgrade_Structure() {
        faction = Faction.CLAN;
        final StructureUpgrade structureWrongFaction = mock(StructureUpgrade.class);
        when(structureWrongFaction.getFaction()).thenReturn(Faction.INNERSPHERE);

        final StructureUpgrade structureRightFaction = mock(StructureUpgrade.class);
        when(structureRightFaction.getFaction()).thenReturn(faction);

        assertFalse(makeDefaultCUT().canUseUpgrade(structureWrongFaction));
        assertTrue(makeDefaultCUT().canUseUpgrade(structureRightFaction));
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
    public void testGetHardPointsCount() {
        final HardPointType hp = HardPointType.BALLISTIC;
        when(components[Location.LeftArm.ordinal()].getHardPointCount(hp)).thenReturn(2);
        when(components[Location.RightTorso.ordinal()].getHardPointCount(hp)).thenReturn(1);

        assertEquals(3, makeDefaultCUT().getHardPointsCount(hp));
    }

    @Test
    public void testGetJumpJetsMax() {
        assertEquals(maxJumpJets, makeDefaultCUT().getJumpJetsMax());
    }

    @Test
    public void testIsAllowed_ClanEngineIsChassis() {
        faction = Faction.CLAN;
        final Engine engine = makeEngine(engineMin);

        faction = Faction.INNERSPHERE;
        assertFalse(makeDefaultCUT().isAllowed(engine));
    }

    @Test
    public void testIsAllowed_EngineBigEnough() {
        assertTrue(makeDefaultCUT().isAllowed(makeEngine(engineMin)));
    }

    @Test
    public void testIsAllowed_EngineSmalllEnough() {
        assertTrue(makeDefaultCUT().isAllowed(makeEngine(engineMax)));
    }

    @Test
    public void testIsAllowed_EngineTooBig() {
        assertFalse(makeDefaultCUT().isAllowed(makeEngine(engineMax + 1)));
    }

    @Test
    public void testIsAllowed_EngineTooSmall() {
        assertFalse(makeDefaultCUT().isAllowed(makeEngine(engineMin - 1)));
    }

    @Test
    public void testIsAllowed_IsEngineClanChassis() {
        faction = Faction.INNERSPHERE;
        final Engine engine = makeEngine(engineMin);

        faction = Faction.CLAN;
        assertFalse(makeDefaultCUT().isAllowed(engine));
    }

    @Test
    public final void testIsAllowed_NoComponentSupport() {
        final Item item = mock(Item.class);
        when(item.getHardpointType()).thenReturn(HardPointType.NONE);
        when(item.getFaction()).thenReturn(Faction.CLAN);
        when(item.isCompatible(isA(Upgrades.class))).thenReturn(true);

        final ChassisStandard cut = makeDefaultCUT();
        assertTrue(cut.isAllowed(item)); // Item in it self is allowed

        // But no component supports it.
        for (final Location location : Location.values()) {
            when(components[location.ordinal()].isAllowed(item, null)).thenReturn(false);
        }
        assertFalse(cut.isAllowed(item));
    }

    @Test
    public void testIsAllowed_NoJJSupport() {
        maxJumpJets = 0;
        assertFalse(makeDefaultCUT().isAllowed(makeJumpJet(maxTons, maxTons + 1)));
    }

    @Override
    protected ChassisStandard makeDefaultCUT() {
        return new ChassisStandard(mwoID, mwoName, series, name, shortName, maxTons, variant, baseVariant,
                movementProfile, faction, engineMin, engineMax, maxJumpJets, components, quirks, mascCapable);
    }
}
