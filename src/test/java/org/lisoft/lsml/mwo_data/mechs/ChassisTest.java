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
package org.lisoft.lsml.mwo_data.mechs;

import static org.junit.Assert.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.Collection;
import junitparams.JUnitParamsRunner;
import junitparams.Parameters;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.lisoft.lsml.mwo_data.*;
import org.lisoft.lsml.mwo_data.ChassisDB;
import org.lisoft.lsml.mwo_data.equipment.*;
import org.lisoft.lsml.mwo_data.equipment.UpgradeDB;

/**
 * An abstract base class for testing {@link Chassis} derived objects.
 *
 * @author Li Song
 */
@RunWith(JUnitParamsRunner.class)
public abstract class ChassisTest {
  protected int baseVariant = 12;
  protected Component[] componentBases;
  protected Faction faction = Faction.CLAN;
  protected boolean mascCapable = false;
  protected int maxTons = 75;
  protected MovementProfile movementProfile;
  protected int mwoID = 300;
  protected String mwoName = "tbw-p";
  protected String name = "Timber Wolf Primal";
  protected String series = "Timber Wolf";
  protected String shortName = "tbw primal";
  protected ChassisVariant variant = ChassisVariant.HERO;

  @Before
  public void setup() {
    movementProfile = mock(MovementProfile.class);
  }

  @Test
  public final void testCanUseUpgrade() {
    assertTrue(makeDefaultCUT().canUseUpgrade(UpgradeDB.ARTEMIS_IV));
    assertTrue(makeDefaultCUT().canUseUpgrade(UpgradeDB.STD_GUIDANCE));
  }

  @Test
  public final void testGetArmourMax() {
    final int armour = 12;
    for (final Location location : Location.values()) {
      when(componentBases[location.ordinal()].getArmourMax()).thenReturn(armour);
    }

    assertEquals(armour * Location.values().length, makeDefaultCUT().getArmourMax());
  }

  @Test
  public final void testGetBaseVariantId() throws Exception {
    assertEquals(baseVariant, makeDefaultCUT().getBaseVariantId());
  }

  @Test
  public final void testGetChassiClass() throws Exception {
    assertEquals(ChassisClass.fromMaxTons(maxTons), makeDefaultCUT().getChassisClass());
  }

  /** {@link Chassis#getComponents()} shall return an immutable {@link Collection}. */
  @Test
  public final void testGetComponents_AllThere() {
    final Chassis base = makeDefaultCUT();
    assertEquals(Location.values().length, base.getComponents().size());
  }

  /** {@link Chassis#getComponents()} shall return an immutable {@link Collection}. */
  @Test(expected = UnsupportedOperationException.class)
  public final void testGetComponents_Ammutable() {
    final Chassis base = makeDefaultCUT();
    base.getComponents().remove(base.getComponent(Location.Head));
  }

  @Test
  public final void testGetConsumablesMax() throws Exception {
    assertEquals(1, makeDefaultCUT().getConsumablesMax());
  }

  @Test
  public final void testGetMassMax() throws Exception {
    assertEquals(maxTons, makeDefaultCUT().getMassMax());
  }

  @Test
  public final void testGetMovementProfileBase() throws Exception {
    assertSame(movementProfile, makeDefaultCUT().getMovementProfileBase());
  }

  @Test
  public final void testGetMwoId() throws Exception {
    assertEquals(mwoID, makeDefaultCUT().getId());
  }

  @Test
  public final void testGetMwoName() throws Exception {
    assertEquals(mwoName, makeDefaultCUT().getKey());
  }

  @Test
  public final void testGetName() throws Exception {
    assertEquals(name, makeDefaultCUT().getName());
  }

  @Test
  public final void testGetSeriesName() throws Exception {
    assertEquals(series, makeDefaultCUT().getSeriesName());
  }

  @Test
  public final void testGetShortName() throws Exception {
    assertEquals(shortName, makeDefaultCUT().getShortName());
  }

  @Test
  public final void testGetSlotsTotal() throws Exception {
    assertEquals(78, makeDefaultCUT().getSlotsTotal());
  }

  @Test
  public final void testGetVariantType() throws Exception {
    assertEquals(variant, makeDefaultCUT().getVariantType());
  }

  @Parameters({"SDR-5K", "JR7-D", "CDA-2A"})
  @Test
  public void testGetVariantType_Negative(String aChassis) {
    assertFalse(ChassisDB.lookup(aChassis).getVariantType().isVariation());
  }

  @Parameters({
    "SDR-5K(C)",
    "JR7-D(S)",
    "CDA-2A(C)",
    "PNT-10K(R)",
    "UM-R63(S)",
    "TBR-PRIME(G)",
    "MLX-PRIME(I)",
    "MDD-PRIME(I)"
  })
  @Test
  public void testGetVariantType_Positive(String aChassis) {
    assertTrue(ChassisDB.lookup(aChassis).getVariantType().isVariation());
  }

  @Test
  public final void testIsAllowed() throws Exception {
    final Chassis cut0 = makeDefaultCUT();
    final Item clanItem = mock(Item.class);
    when(clanItem.getFaction()).thenReturn(Faction.CLAN);
    final Item isItem = mock(Item.class);
    when(isItem.getFaction()).thenReturn(Faction.INNERSPHERE);

    if (cut0.getFaction() == Faction.CLAN) {
      assertTrue(cut0.isAllowed(clanItem));
      assertFalse(cut0.isAllowed(isItem));
    } else {
      assertFalse(cut0.isAllowed(clanItem));
      assertTrue(cut0.isAllowed(isItem));
    }
  }

  @Test
  public void testIsAllowed_Internal() {
    assertFalse(
        makeDefaultCUT()
            .isAllowed(new Internal("", "", "", 0, 1, 0, HardPointType.NONE, 0, faction)));
  }

  @Test
  public final void testIsAllowed_JJPerfectFit() {
    assertTrue(makeDefaultCUT().isAllowed(makeJumpJet(maxTons, maxTons + 1)));
  }

  @Test
  public final void testIsAllowed_JJTooBig() {
    assertFalse(makeDefaultCUT().isAllowed(makeJumpJet(maxTons + 1, maxTons * 2)));
  }

  @Test
  public final void testIsAllowed_JJTooSmall() {
    assertFalse(makeDefaultCUT().isAllowed(makeJumpJet(0, maxTons)));
  }

  @Test
  public final void testIsAllowed_Masc() {
    final MASC masc =
        new MASC("", "", "", 0, 1, 1.0, 0, faction, null, maxTons - 5, maxTons + 5, 0, 0, 0, 0);

    mascCapable = false;
    assertFalse(makeDefaultCUT().isAllowed(masc));
    mascCapable = true;
    assertTrue(makeDefaultCUT().isAllowed(masc));
  }

  @Test
  public final void testIsAllowed_MascTooHeavy() {
    final MASC masc =
        new MASC("", "", "", 0, 1, 1.0, 0, faction, null, maxTons - 25, maxTons - 5, 0, 0, 0, 0);
    mascCapable = true;
    assertFalse(makeDefaultCUT().isAllowed(masc));
  }

  @Test
  public final void testIsAllowed_MascTooLight() {
    final MASC masc =
        new MASC("", "", "", 0, 1, 1.0, 0, faction, null, maxTons + 25, maxTons + 35, 0, 0, 0, 0);
    mascCapable = true;
    assertFalse(makeDefaultCUT().isAllowed(masc));
  }

  @Test
  public final void testIsClan() throws Exception {
    assertEquals(faction, makeDefaultCUT().getFaction());
  }

  @Test
  public void testIsHero() {
    final Chassis ilya = ChassisDB.lookup("Ilya Muromets");
    assertEquals(ChassisVariant.HERO, ilya.getVariantType());

    final Chassis ctf3d = ChassisDB.lookup("CTF-3D");
    assertEquals(ChassisVariant.NORMAL, ctf3d.getVariantType());
  }

  @Test
  public void testIsMascCapable() {
    assertTrue(ChassisDB.lookup("KDK-SB").isMascCapable());
    assertFalse(ChassisDB.lookup("KDK-4").isMascCapable());
  }

  @Test
  public final void testIsSameSeries() throws Exception {
    final Chassis cut0 = makeDefaultCUT();
    final Chassis cut1 = makeDefaultCUT();

    series = "Other Series";
    final Chassis cut2 = makeDefaultCUT();

    assertTrue(cut0.isSameSeries(cut0));
    assertTrue(cut0.isSameSeries(cut1));
    assertFalse(cut0.isSameSeries(cut2));
  }

  @Parameters({"HBK-4J, CTF-3D", "EMBER, Ilya Muromets"})
  @Test
  public void testIsSameSeries_Negative(String aChassiA, String aChassiB) {
    assertFalse(ChassisDB.lookup(aChassiA).isSameSeries(ChassisDB.lookup(aChassiB)));
  }

  @Parameters({"HBK-4J, HBK-4P", "CTF-3D, Ilya Muromets"})
  @Test
  public void testIsSameSeries_Positive(String aChassisA, String aChassisB) {
    assertTrue(ChassisDB.lookup(aChassisA).isSameSeries(ChassisDB.lookup(aChassisB)));
  }

  @Test
  public final void testToString() throws Exception {
    assertEquals(shortName, makeDefaultCUT().toString());
  }

  protected abstract Chassis makeDefaultCUT();

  protected Engine makeEngine(int rating) {
    final Engine engine = mock(Engine.class);
    when(engine.getFaction()).thenReturn(faction);
    when(engine.getHardpointType()).thenReturn(HardPointType.NONE);
    when(engine.getRating()).thenReturn(rating);
    when(engine.getType()).thenReturn(Engine.EngineType.XL);
    when(engine.isCompatible(any(Upgrades.class))).thenReturn(true);
    return engine;
  }

  protected JumpJet makeJumpJet(int aMinTons, int aMaxTons) {
    final JumpJet jj = mock(JumpJet.class);
    when(jj.getHardpointType()).thenReturn(HardPointType.NONE);
    when(jj.getFaction()).thenReturn(faction);
    when(jj.isCompatible(any(Upgrades.class))).thenReturn(true);

    when(jj.getMinTons()).thenReturn((double) aMinTons);
    when(jj.getMaxTons()).thenReturn((double) aMaxTons);
    return jj;
  }
}
