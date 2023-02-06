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
package org.lisoft.lsml.model.garage;

import static org.junit.Assert.*;

import org.junit.Test;
import org.lisoft.lsml.model.helpers.MockLoadoutContainer;
import org.lisoft.mwo_data.Faction;
import org.mockito.Mockito;

public class DropShipTest {

  final MockLoadoutContainer mlc = new MockLoadoutContainer();

  @Test
  public void testGetFaction_Clan() {
    DropShip cut = new DropShip(Faction.CLAN);
    assertEquals(Faction.CLAN, cut.getFaction());
  }

  @Test
  public void testGetFaction_IS() {
    DropShip cut = new DropShip(Faction.INNERSPHERE);
    assertEquals(Faction.INNERSPHERE, cut.getFaction());
  }

  @Test
  public void testGetMaxTonnage_Clan() {
    DropShip cut = new DropShip(Faction.CLAN);
    assertEquals(240, cut.getMaxTonnage());
  }

  @Test
  public void testGetMaxTonnage_IS() {
    DropShip cut = new DropShip(Faction.INNERSPHERE);
    assertEquals(250, cut.getMaxTonnage());
  }

  @Test
  public void testGetMech_Empty() {
    DropShip cut = new DropShip(Faction.CLAN);
    for (int i = 0; i < DropShip.MECHS_IN_DROPSHIP; ++i) {
      assertNull(cut.getMech(i));
    }
  }

  @Test
  public void testGetMinTonnage_Clan() {
    DropShip cut = new DropShip(Faction.CLAN);
    assertEquals(160, cut.getMinTonnage());
  }

  @Test
  public void testGetMinTonnage_IS() {
    DropShip cut = new DropShip(Faction.INNERSPHERE);
    assertEquals(160, cut.getMinTonnage());
  }

  @Test
  public void testGetName_Default() {
    DropShip cut = new DropShip(Faction.INNERSPHERE);
    assertEquals("Unnamed Drop Ship", cut.getName());
  }

  @Test
  public void testGetTonnage() throws GarageException {
    DropShip cut = new DropShip(Faction.CLAN);
    MockLoadoutContainer mlc1 = new MockLoadoutContainer();
    MockLoadoutContainer mlc2 = new MockLoadoutContainer();
    MockLoadoutContainer mlc3 = new MockLoadoutContainer();
    MockLoadoutContainer mlc4 = new MockLoadoutContainer();

    Mockito.when(mlc1.chassis.getFaction()).thenReturn(Faction.CLAN);
    Mockito.when(mlc2.chassis.getFaction()).thenReturn(Faction.CLAN);
    Mockito.when(mlc3.chassis.getFaction()).thenReturn(Faction.CLAN);
    Mockito.when(mlc4.chassis.getFaction()).thenReturn(Faction.CLAN);

    Mockito.when(mlc1.chassis.getMassMax()).thenReturn(80);
    Mockito.when(mlc2.chassis.getMassMax()).thenReturn(70);
    Mockito.when(mlc3.chassis.getMassMax()).thenReturn(30);
    Mockito.when(mlc4.chassis.getMassMax()).thenReturn(35);

    cut.setMech(0, mlc1.loadout);
    cut.setMech(1, mlc2.loadout);
    cut.setMech(2, mlc3.loadout);
    cut.setMech(3, mlc4.loadout);

    int tonnage = cut.getTonnage();

    assertEquals(215, tonnage);
  }

  @Test
  public void testGetTonnage_empty() {
    DropShip cut = new DropShip(Faction.CLAN);
    assertEquals(0, cut.getTonnage());
  }

  @Test
  public void testIsCompatible_No() {
    DropShip cut = new DropShip(Faction.CLAN);
    Mockito.when(mlc.chassis.getFaction()).thenReturn(Faction.INNERSPHERE);

    assertFalse(cut.isCompatible(mlc.loadout));
  }

  @Test
  public void testIsCompatible_Yes() {
    DropShip cut = new DropShip(Faction.CLAN);
    Mockito.when(mlc.chassis.getFaction()).thenReturn(Faction.CLAN);

    assertTrue(cut.isCompatible(mlc.loadout));
  }

  @Test
  public void testRemoveMech() throws GarageException {
    DropShip cut = new DropShip(Faction.CLAN);
    Mockito.when(mlc.chassis.getFaction()).thenReturn(Faction.CLAN);

    cut.setMech(1, mlc.loadout);
    cut.setMech(1, null);

    assertNull(cut.getMech(1));
  }

  @Test
  public void testSetMech() throws GarageException {
    DropShip cut = new DropShip(Faction.CLAN);
    Mockito.when(mlc.chassis.getFaction()).thenReturn(Faction.CLAN);

    cut.setMech(2, mlc.loadout);
    assertNull(cut.getMech(0));
    assertNull(cut.getMech(1));
    assertSame(mlc.loadout, cut.getMech(2));
    assertNull(cut.getMech(3));
  }

  @Test
  public void testSetMechMech_WrongFaction() {
    DropShip cut = new DropShip(Faction.CLAN);
    Mockito.when(mlc.chassis.getFaction()).thenReturn(Faction.INNERSPHERE);

    try {
      cut.setMech(0, mlc.loadout);
      fail("Didn't throw!");
    } catch (GarageException e) {
      String msg = e.getMessage().toLowerCase();
      assertTrue(msg.contains("wrong"));
      assertTrue(msg.contains("faction"));
      assertNull(cut.getMech(0));
    }
  }

  @Test
  public void testSetName() {
    DropShip cut = new DropShip(Faction.INNERSPHERE);
    cut.setName("My Drop Ship");
    assertEquals("My Drop Ship", cut.getName());
  }

  @Test
  public void testToString() {
    DropShip cut = new DropShip(Faction.INNERSPHERE);
    assertEquals(cut.getName(), cut.toString());
  }
}
