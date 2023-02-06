/*
 * Li Song Mechlab - A 'mech building tool for PGI's MechWarrior: Online.
 * Copyright (C) 2013-2023  Li Song
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
package org.lisoft.mwo_data.mechs;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.junit.Before;
import org.junit.Test;
import org.lisoft.mwo_data.ChassisDB;
import org.lisoft.mwo_data.Faction;
import org.lisoft.mwo_data.ItemDB;
import org.lisoft.mwo_data.equipment.*;
import org.mockito.Mockito;

/**
 * A test suite for {@link ChassisOmniMech}.
 *
 * @author Li Song
 */
public class ChassisOmniMechTest extends ChassisTest {
  private ArmourUpgrade armourType;
  private ComponentOmniMech[] components;
  private Engine engine;
  private HeatSinkUpgrade heatSinkType;
  private Map<Location, List<Item>> items;
  private StructureUpgrade structureType;

  @Override
  @Before
  public void setup() {
    super.setup();

    engine = Mockito.mock(Engine.class);
    when(engine.getFaction()).thenReturn(faction);
    when(engine.getHardpointType()).thenReturn(HardPointType.NONE);
    when(engine.getRating()).thenReturn(250);
    when(engine.getType()).thenReturn(Engine.EngineType.XL);
    when(engine.isCompatible(any(Upgrades.class))).thenReturn(true);

    structureType = Mockito.mock(StructureUpgrade.class);
    armourType = Mockito.mock(ArmourUpgrade.class);
    heatSinkType = Mockito.mock(HeatSinkUpgrade.class);

    items = new HashMap<>();
    components = new ComponentOmniMech[Location.values().length];
    for (final Location location : Location.values()) {
      items.put(location, new ArrayList<>());
      components[location.ordinal()] = Mockito.mock(ComponentOmniMech.class);
      when(components[location.ordinal()].isAllowed(isA(Item.class), any())).thenReturn(true);
      when(components[location.ordinal()].getFixedItems()).thenReturn(items.get(location));
    }
    componentBases = components;

    items.get(Location.CenterTorso).add(engine);

    when(components[Location.Head.ordinal()].getDynamicArmourSlots()).thenReturn(1);
    when(components[Location.CenterTorso.ordinal()].getDynamicArmourSlots()).thenReturn(1);
    when(components[Location.LeftTorso.ordinal()].getDynamicArmourSlots()).thenReturn(3);
    when(components[Location.RightArm.ordinal()].getDynamicArmourSlots()).thenReturn(2);

    when(components[Location.LeftLeg.ordinal()].getDynamicStructureSlots()).thenReturn(1);
    when(components[Location.RightTorso.ordinal()].getDynamicStructureSlots()).thenReturn(1);
    when(components[Location.LeftArm.ordinal()].getDynamicStructureSlots()).thenReturn(3);
    when(components[Location.RightLeg.ordinal()].getDynamicStructureSlots()).thenReturn(2);

    when(structureType.getDynamicSlots()).thenReturn(7);
    when(armourType.getTotalSlots()).thenReturn(7);
  }

  @Test
  public final void testCanUseUpgrade_Armour() {
    final ArmourUpgrade armour = mock(ArmourUpgrade.class);
    when(armour.getFaction()).thenReturn(Faction.ANY);
    assertFalse(makeDefaultCUT().canUseUpgrade(armour));

    // Can always use the type of the fixed armour
    assertTrue(makeDefaultCUT().canUseUpgrade(armourType));
  }

  @Test
  public final void testCanUseUpgrade_HeatSinks() {
    final HeatSinkUpgrade heatSink = mock(HeatSinkUpgrade.class);
    when(heatSink.getFaction()).thenReturn(Faction.ANY);
    assertFalse(makeDefaultCUT().canUseUpgrade(heatSink));

    // Can always use the type of the fixed heat sinks
    assertTrue(makeDefaultCUT().canUseUpgrade(heatSinkType));
  }

  @Test
  public final void testCanUseUpgrade_Structure() {
    final StructureUpgrade structure = mock(StructureUpgrade.class);
    when(structure.getFaction()).thenReturn(Faction.ANY);
    assertFalse(makeDefaultCUT().canUseUpgrade(structure));

    // Can always use the type of the fixed structure
    assertTrue(makeDefaultCUT().canUseUpgrade(structureType));
  }

  @SuppressWarnings("unused")
  // Expecting exception
  @Test(expected = IllegalArgumentException.class)
  public final void testCtor_BadDynArmour() {
    when(components[Location.Head.ordinal()].getDynamicArmourSlots()).thenReturn(13);
    new ChassisOmniMech(
        mwoID,
        mwoName,
        series,
        name,
        shortName,
        maxTons,
        variant,
        baseVariant,
        movementProfile,
        faction,
        components,
        structureType,
        armourType,
        heatSinkType,
        mascCapable);
  }

  @SuppressWarnings("unused")
  // Expecting exception
  @Test(expected = IllegalArgumentException.class)
  public final void testCtor_BadDynStructure() {
    when(components[Location.Head.ordinal()].getDynamicStructureSlots()).thenReturn(13);
    new ChassisOmniMech(
        mwoID,
        mwoName,
        series,
        name,
        shortName,
        maxTons,
        variant,
        baseVariant,
        movementProfile,
        faction,
        components,
        structureType,
        armourType,
        heatSinkType,
        mascCapable);
  }

  @Test
  public final void testGetFixedArmourType() {
    assertSame(armourType, makeDefaultCUT().getFixedArmourType());
  }

  @Test
  public final void testGetFixedEngine() {
    assertSame(engine, makeDefaultCUT().getFixedEngine());
  }

  @Test(expected = IllegalStateException.class)
  public final void testGetFixedEngine_noEngine() {
    final Item item1 = Mockito.mock(Item.class);
    final HeatSink hs1 = Mockito.mock(HeatSink.class);

    items.get(Location.LeftArm).add(item1);
    items.get(Location.RightTorso).add(hs1);
    items.get(Location.RightTorso).add(item1);
    items.get(Location.CenterTorso).clear(); // Remove engine.
    items.get(Location.CenterTorso).add(hs1);
    items.get(Location.CenterTorso).add(item1);

    makeDefaultCUT().getFixedEngine();
  }

  @Test
  public final void testGetFixedHeatSinkType() {
    assertSame(heatSinkType, makeDefaultCUT().getFixedHeatSinkType());
  }

  /**
   * {@link ChassisOmniMech#getFixedMass()} shall return the mass of the chassis with all non fixed
   * items and armour removed.
   */
  @Test
  public final void testGetFixedHeatSinks() {
    final ChassisOmniMech cut = makeDefaultCUT();

    final List<Item> fixed1 = new ArrayList<>();
    final List<Item> fixed2 = new ArrayList<>();
    final List<Item> fixed3 = new ArrayList<>();

    final Item item1 = Mockito.mock(Item.class);
    final HeatSink hs1 = Mockito.mock(HeatSink.class);
    final HeatSink hs2 = Mockito.mock(HeatSink.class);

    when(item1.getMass()).thenReturn(1.0);
    when(hs1.getMass()).thenReturn(2.0);
    when(hs2.getMass()).thenReturn(3.0);

    fixed1.add(item1);
    fixed1.add(hs1);
    fixed2.add(hs1);
    fixed2.add(hs1);
    fixed3.add(hs2);

    when(components[2].getFixedItems()).thenReturn(fixed1);
    when(components[3].getFixedItems()).thenReturn(fixed2);
    when(components[5].getFixedItems()).thenReturn(fixed3);

    assertEquals(4, cut.getFixedHeatSinks());
  }

  @Test
  public final void testGetFixedJumpJets() {
    final List<Item> fixed1 = new ArrayList<>();
    final List<Item> fixed2 = new ArrayList<>();
    final List<Item> fixed3 = new ArrayList<>();

    final Item item1 = Mockito.mock(Item.class);
    final JumpJet jj1 = Mockito.mock(JumpJet.class);
    final JumpJet jj2 = Mockito.mock(JumpJet.class);

    fixed1.add(item1);
    fixed1.add(jj1);
    fixed2.add(jj1);
    fixed2.add(jj1);
    fixed3.add(jj2);

    when(components[2].getFixedItems()).thenReturn(fixed1);
    when(components[3].getFixedItems()).thenReturn(fixed2);
    when(components[5].getFixedItems()).thenReturn(fixed3);

    assertSame(4, makeDefaultCUT().getFixedJumpJets());
  }

  /**
   * {@link ChassisOmniMech#getFixedMass()} shall return the mass of the chassis with all non fixed
   * items and armour removed.
   */
  @Test
  public final void testGetFixedMass() {
    final ChassisOmniMech cut = makeDefaultCUT();

    final List<Item> fixed1 = new ArrayList<>();
    final List<Item> fixed2 = new ArrayList<>();
    final List<Item> fixed3 = new ArrayList<>();

    final Item item1 = Mockito.mock(Item.class);
    final Item item2 = Mockito.mock(Item.class);
    final Item item3 = Mockito.mock(Item.class);

    when(item1.getMass()).thenReturn(1.0);
    when(item2.getMass()).thenReturn(2.0);
    when(item3.getMass()).thenReturn(3.0);

    fixed1.add(item1);
    fixed1.add(item2);
    fixed2.add(item2);
    fixed2.add(item2);
    fixed3.add(item3);

    when(components[2].getFixedItems()).thenReturn(fixed1);
    when(components[3].getFixedItems()).thenReturn(fixed2);
    when(components[5].getFixedItems()).thenReturn(fixed3);

    when(structureType.getStructureMass(cut)).thenReturn(3.0);

    final double expected = 1 + 3 * 2 + 3 + 3;

    assertEquals(expected, cut.getFixedMass(), 0.0);

    verify(armourType, Mockito.never()).getArmourMass(anyInt());
  }

  @Test
  public final void testGetFixedStructureType() {
    assertSame(structureType, makeDefaultCUT().getFixedStructureType());
  }

  /** Check that all movement profiles have been parsed to non-zero values. */
  @Test
  public final void testGetMovementProfiles_AllParsed() {
    ChassisDB.lookupAll().stream()
        .filter(c -> c instanceof ChassisOmniMech)
        .map(c -> (ChassisOmniMech) c)
        .forEach(
            c -> {
              final MovementProfile baseProfile = c.getMovementProfileBase();
              assertNotEquals(0, baseProfile.getTorsoYawMax(null));
              assertNotEquals(0, baseProfile.getTorsoYawSpeed(null));
              assertNotEquals(0, baseProfile.getTorsoPitchMax(null));
              assertNotEquals(0, baseProfile.getTorsoPitchSpeed(null));

              assertNotEquals(0, baseProfile.getArmYawMax(null));
              assertNotEquals(0, baseProfile.getArmYawSpeed(null));
              assertNotEquals(0, baseProfile.getArmPitchMax(null));
              assertNotEquals(0, baseProfile.getArmPitchSpeed(null));

              assertNotEquals(0, baseProfile.getReverseSpeedMultiplier(null));
              assertNotEquals(0, baseProfile.getSpeedFactor(null));

              assertNotEquals(0, baseProfile.getTurnLerpHighRate(null));
              assertNotEquals(0, baseProfile.getTurnLerpHighSpeed(null));
              assertNotEquals(0, baseProfile.getTurnLerpMidRate(null));
              assertNotEquals(0, baseProfile.getTurnLerpMidSpeed(null));
              assertNotEquals(0, baseProfile.getTurnLerpLowRate(null));
              assertNotEquals(0, baseProfile.getTurnLerpLowSpeed(null));
            });
  }

  /** Check that all min profile <= max profile holds for all movement profiles */
  @Test
  public final void testGetMovementProfiles_MaxGEMin() {
    ChassisDB.lookupAll().stream()
        .filter(c -> c instanceof ChassisOmniMech)
        .map(c -> (ChassisOmniMech) c)
        .forEach(
            c -> {
              final MovementProfile max = c.getMovementProfileMax();
              final MovementProfile min = c.getMovementProfileMin();

              assertTrue(min.getTorsoYawMax(null) <= max.getTorsoYawMax(null));
              assertTrue(min.getTorsoYawSpeed(null) <= max.getTorsoYawSpeed(null));
              assertTrue(min.getTorsoPitchMax(null) <= max.getTorsoPitchMax(null));
              assertTrue(min.getTorsoPitchSpeed(null) <= max.getTorsoPitchSpeed(null));

              assertTrue(min.getArmYawMax(null) <= max.getArmYawMax(null));
              assertTrue(min.getArmYawSpeed(null) <= max.getArmYawSpeed(null));
              assertTrue(min.getArmPitchMax(null) <= max.getArmPitchMax(null));
              assertTrue(min.getArmPitchSpeed(null) <= max.getArmPitchSpeed(null));

              assertTrue(
                  min.getReverseSpeedMultiplier(null) <= max.getReverseSpeedMultiplier(null));
              assertTrue(min.getSpeedFactor(null) <= max.getSpeedFactor(null));

              assertTrue(min.getTurnLerpHighRate(null) <= max.getTurnLerpHighRate(null));
              assertTrue(min.getTurnLerpHighSpeed(null) <= max.getTurnLerpHighSpeed(null));
              assertTrue(min.getTurnLerpMidRate(null) <= max.getTurnLerpMidRate(null));
              assertTrue(min.getTurnLerpMidSpeed(null) <= max.getTurnLerpMidSpeed(null));
              assertTrue(min.getTurnLerpLowRate(null) <= max.getTurnLerpLowRate(null));
              assertTrue(min.getTurnLerpLowSpeed(null) <= max.getTurnLerpLowSpeed(null));
            });
  }

  @Test
  public final void testIsAllowed_CASE() {
    final Item item = ItemDB.CASE;

    final ChassisOmniMech cut = makeDefaultCUT();
    assertFalse(cut.isAllowed(item));
  }

  @Test
  public final void testIsAllowed_Engine() {
    assertFalse(makeDefaultCUT().isAllowed(engine));
  }

  @Test
  public final void testIsAllowed_NoComponentSupport() {
    final Item item = mock(Item.class);
    when(item.getHardpointType()).thenReturn(HardPointType.NONE);
    when(item.getFaction()).thenReturn(Faction.CLAN);
    when(item.isCompatible(any(Upgrades.class))).thenReturn(true);

    final ChassisOmniMech cut = makeDefaultCUT();
    assertTrue(cut.isAllowed(item)); // Item in it self is allowed

    // But no component supports it.
    for (final Location location : Location.values()) {
      Mockito.when(components[location.ordinal()].isAllowed(item, null)).thenReturn(false);
    }
    assertFalse(cut.isAllowed(item));
  }

  @Override
  protected ChassisOmniMech makeDefaultCUT() {
    return new ChassisOmniMech(
        mwoID,
        mwoName,
        series,
        name,
        shortName,
        maxTons,
        variant,
        baseVariant,
        movementProfile,
        faction,
        components,
        structureType,
        armourType,
        heatSinkType,
        mascCapable);
  }
}
