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

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import org.junit.Test;
import org.lisoft.lsml.model.ItemDB;
import org.lisoft.mwo_data.Faction;
import org.lisoft.mwo_data.equipment.Item;
import org.lisoft.mwo_data.modifiers.Modifier;
import org.mockito.Mockito;

/**
 * Test suite for {@link OmniPod} class.
 *
 * @author Li Song
 */
public class OmniPodTest {
  private final Faction faction = Faction.CLAN;
  private final List<Item> fixedItems = new ArrayList<>();
  private final List<HardPoint> hardPoints = new ArrayList<>();
  private final Location location = Location.CenterTorso;
  private final int maxJumpJets = 2;
  private final int mwoID = 30012;
  private final List<OmniPodSetBonus> omniPodSetBonuses = new ArrayList<>();
  private final List<Modifier> quirks = new ArrayList<>();
  private final List<Item> toggleableItems = new ArrayList<>();
  private String setName = "tbr-prime";
  private String series = "timber wolf";

  @Test
  public void testGetSetName() {
    assertEquals(setName.toUpperCase(), makeCUT().getSetName());
  }

  @Test
  public void testGetChassisSeries() {
    assertEquals(series.toUpperCase(), makeCUT().getChassisSeries());
  }

  @Test
  public void testGetFixedItem() {
    final Item i0 = Mockito.mock(Item.class);
    final Item i1 = Mockito.mock(Item.class);
    fixedItems.add(i0);
    fixedItems.add(i1);

    final List<Item> ans = new ArrayList<>(makeCUT().getFixedItems());

    assertEquals(2, ans.size());
    assertTrue(ans.remove(i0));
    assertTrue(ans.remove(i1));
  }

  @Test(expected = UnsupportedOperationException.class)
  public void testGetFixedItems_NoMod() {
    makeCUT().getFixedItems().add(Mockito.mock(Item.class));
  }

  @Test
  public void testGetHardPointCount() {
    final HardPoint hp1 = Mockito.mock(HardPoint.class);
    final HardPoint hp2 = Mockito.mock(HardPoint.class);
    final HardPoint hp3 = Mockito.mock(HardPoint.class);
    final HardPoint hp4 = Mockito.mock(HardPoint.class);

    Mockito.when(hp1.getType()).thenReturn(HardPointType.MISSILE);
    Mockito.when(hp2.getType()).thenReturn(HardPointType.MISSILE);
    Mockito.when(hp3.getType()).thenReturn(HardPointType.ECM);
    Mockito.when(hp4.getType()).thenReturn(HardPointType.ENERGY);

    hardPoints.add(hp1);
    hardPoints.add(hp2);
    hardPoints.add(hp3);
    hardPoints.add(hp4);

    assertEquals(2, makeCUT().getHardPointCount(HardPointType.MISSILE));
    assertEquals(1, makeCUT().getHardPointCount(HardPointType.ECM));
    assertEquals(1, makeCUT().getHardPointCount(HardPointType.ENERGY));
    assertEquals(0, makeCUT().getHardPointCount(HardPointType.BALLISTIC));
  }

  @Test
  public void testGetHardPoints() {
    final HardPoint hp1 = Mockito.mock(HardPoint.class);
    final HardPoint hp2 = Mockito.mock(HardPoint.class);
    hardPoints.add(hp1);
    hardPoints.add(hp2);

    final List<HardPoint> ans = new ArrayList<>(makeCUT().getHardPoints());

    assertEquals(2, ans.size());
    assertTrue(ans.remove(hp1));
    assertTrue(ans.remove(hp2));
  }

  @Test(expected = UnsupportedOperationException.class)
  public void testGetHardPoints_NoMod() {
    makeCUT().getHardPoints().add(Mockito.mock(HardPoint.class));
  }

  @Test
  public void testGetJumpJetsMax() {
    assertEquals(maxJumpJets, makeCUT().getJumpJetsMax());
  }

  @Test
  public void testGetLocation() {
    assertEquals(location, makeCUT().getLocation());
  }

  @Test
  public void testGetMwoId() {
    assertEquals(mwoID, makeCUT().getId());
  }

  @Test
  public void testGetOmniPodSet() {
    Modifier modifier3 = Mockito.mock(Modifier.class);
    Modifier modifier4 = Mockito.mock(Modifier.class);
    Modifier modifier5 = Mockito.mock(Modifier.class);
    Modifier modifier8 = Mockito.mock(Modifier.class);

    OmniPodSetBonus bonus3 = new OmniPodSetBonus(3, List.of(modifier3));
    OmniPodSetBonus bonus4 = new OmniPodSetBonus(4, List.of(modifier4));
    OmniPodSetBonus bonus5 = new OmniPodSetBonus(5, List.of(modifier5));
    OmniPodSetBonus bonus8 = new OmniPodSetBonus(8, List.of(modifier8));
    omniPodSetBonuses.addAll(List.of(bonus3, bonus4, bonus5, bonus8));

    OmniPod cut = makeCUT();

    assertEquals(Collections.emptyList(), cut.getOmniPodSetBonuses(0));
    assertEquals(Collections.emptyList(), cut.getOmniPodSetBonuses(1));
    assertEquals(Collections.emptyList(), cut.getOmniPodSetBonuses(2));
    assertEquals(List.of(3), cut.getOmniPodSetBonuses(3));
    assertEquals(List.of(4), cut.getOmniPodSetBonuses(4));
    assertEquals(List.of(5), cut.getOmniPodSetBonuses(5));
    assertEquals(List.of(5), cut.getOmniPodSetBonuses(6));
    assertEquals(List.of(5), cut.getOmniPodSetBonuses(7));
    assertEquals(List.of(8), cut.getOmniPodSetBonuses(8));
  }

  @Test
  public void testGetQuirks() {
    assertSame(quirks, makeCUT().getQuirks());
  }

  @Test
  public void testGetToggleableItems() {
    final Item i0 = ItemDB.HA;
    final Item i1 = ItemDB.LAA;
    toggleableItems.add(i0);
    toggleableItems.add(i1);

    final List<Item> ans = new ArrayList<>(makeCUT().getToggleableItems());

    assertEquals(2, ans.size());
    assertTrue(ans.remove(i0));
    assertTrue(ans.remove(i1));
  }

  @Test(expected = UnsupportedOperationException.class)
  public void testGetToggleableItems_NoMod() {
    makeCUT().getToggleableItems().add(Mockito.mock(Item.class));
  }

  @Test
  public void testHasMissileBayDoors_No() {
    final HardPoint hp1 = Mockito.mock(HardPoint.class);
    final HardPoint hp2 = Mockito.mock(HardPoint.class);
    final HardPoint hp3 = Mockito.mock(HardPoint.class);
    final HardPoint hp4 = Mockito.mock(HardPoint.class);

    Mockito.when(hp1.getType()).thenReturn(HardPointType.MISSILE);
    Mockito.when(hp2.getType()).thenReturn(HardPointType.MISSILE);
    Mockito.when(hp3.getType()).thenReturn(HardPointType.ECM);
    Mockito.when(hp4.getType()).thenReturn(HardPointType.ENERGY);

    hardPoints.add(hp1);
    hardPoints.add(hp2);
    hardPoints.add(hp3);
    hardPoints.add(hp4);

    assertFalse(makeCUT().hasMissileBayDoors());
  }

  @Test
  public void testHasMissileBayDoors_Yes() {
    final HardPoint hp1 = Mockito.mock(HardPoint.class);
    final HardPoint hp2 = Mockito.mock(HardPoint.class);
    final HardPoint hp3 = Mockito.mock(HardPoint.class);
    final HardPoint hp4 = Mockito.mock(HardPoint.class);

    Mockito.when(hp1.getType()).thenReturn(HardPointType.MISSILE);
    Mockito.when(hp2.getType()).thenReturn(HardPointType.MISSILE);
    Mockito.when(hp2.hasMissileBayDoor()).thenReturn(true);
    Mockito.when(hp3.getType()).thenReturn(HardPointType.ECM);
    Mockito.when(hp4.getType()).thenReturn(HardPointType.ENERGY);

    hardPoints.add(hp1);
    hardPoints.add(hp2);
    hardPoints.add(hp3);
    hardPoints.add(hp4);

    assertTrue(makeCUT().hasMissileBayDoors());
  }

  @Test
  public void testIsCompatible() {
    series = "TIMBER WOLF";
    setName = "TBR-PRIME";

    final ChassisOmniMech chassisP = Mockito.mock(ChassisOmniMech.class);
    Mockito.when(chassisP.getSeriesName()).thenReturn(series.toLowerCase());
    Mockito.when(chassisP.getName()).thenReturn(series.toLowerCase() + " tBR-PRIME");
    Mockito.when(chassisP.getShortName()).thenReturn("TBR-PRImE");

    final ChassisOmniMech chassisPI = Mockito.mock(ChassisOmniMech.class);
    Mockito.when(chassisPI.getSeriesName()).thenReturn(series.toLowerCase());
    Mockito.when(chassisPI.getName()).thenReturn(series.toLowerCase() + " TBR-PRIME(I)");
    Mockito.when(chassisPI.getShortName()).thenReturn("TBR-PRiME");

    final ChassisOmniMech chassisPG = Mockito.mock(ChassisOmniMech.class);
    Mockito.when(chassisPG.getSeriesName()).thenReturn(series.toLowerCase());
    Mockito.when(chassisPG.getName()).thenReturn(series.toLowerCase() + " TBR-PRIME(G)");
    Mockito.when(chassisPG.getShortName()).thenReturn("TBr-PRIME(G)");

    final ChassisOmniMech chassisC = Mockito.mock(ChassisOmniMech.class);
    Mockito.when(chassisC.getSeriesName()).thenReturn(series.toLowerCase());
    Mockito.when(chassisC.getName()).thenReturn(series.toLowerCase() + " TBR-C");
    Mockito.when(chassisC.getShortName()).thenReturn("TBr-c");

    final ChassisOmniMech scr = Mockito.mock(ChassisOmniMech.class);
    Mockito.when(scr.getSeriesName()).thenReturn("stormcrow");
    Mockito.when(scr.getName()).thenReturn("stormcrow scr-C");
    Mockito.when(scr.getShortName()).thenReturn("scrr-c");

    assertTrue(makeCUT().isCompatible(chassisP));
    assertTrue(makeCUT().isCompatible(chassisPI));
    assertTrue(makeCUT().isCompatible(chassisPG));
    assertTrue(makeCUT().isCompatible(chassisC));

    assertFalse(makeCUT().isCompatible(scr));
  }

  @Test
  public void testToString() {
    assertEquals(setName.toUpperCase(), makeCUT().toString());
  }

  protected OmniPod makeCUT() {
    return new OmniPod(
        mwoID,
        location,
        series,
            setName,
        omniPodSetBonuses,
        quirks,
        hardPoints,
        fixedItems,
        toggleableItems,
        maxJumpJets,
        faction);
  }
}
