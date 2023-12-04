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
package org.lisoft.mwo_data.equipment;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.lisoft.lsml.model.ItemDB;
import org.lisoft.mwo_data.mechs.HardPointType;
import org.mockito.junit.MockitoJUnitRunner;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import static org.junit.Assert.*;

@SuppressWarnings("javadoc")
@RunWith(MockitoJUnitRunner.class)
public class ItemTests {

  /** */
  @Test
  public void testAMS() throws Exception {
    final AmmoWeapon ams = (AmmoWeapon) ItemDB.lookup("AMS");
    assertSame(ams, ItemDB.AMS);
    assertEquals(1, ams.getSlots());
    assertEquals(0.5, ams.getMass(), 0.0);
    assertEquals(HardPointType.AMS, ams.getHardpointType());
  }

  @Test
  public void testEngines() throws Exception {
    final Engine std175 = (Engine) ItemDB.lookup("STD ENGINE 175");
    final Engine std180 = (Engine) ItemDB.lookup("STD ENGINE 180");
    final Engine xl330 = (Engine) ItemDB.lookup("XL ENGINE 330");
    final Engine xl335 = (Engine) ItemDB.lookup("XL ENGINE 335");

    assertEquals(6, std175.getSlots());
    assertEquals(6, std180.getSlots());
    assertEquals(6, xl330.getSlots());
    assertEquals(6, xl335.getSlots());

    assertEquals(9.0, std175.getMass(), 0.0);
    assertEquals(9.0, std180.getMass(), 0.0);
    assertEquals(19.5, xl330.getMass(), 0.0);
    assertEquals(20.0, xl335.getMass(), 0.0);

    // Engines have a base heat of the dissipation equal to 2 standard heat sinks when using 100%
    // throttle.
    assertEquals(0.2, std175.getHeat(null), 0.0);
    assertEquals(0.2, std180.getHeat(null), 0.0);
    assertEquals(0.2, xl330.getHeat(null), 0.0);
    assertEquals(0.2, xl335.getHeat(null), 0.0);

    assertEquals(Engine.EngineType.STD, std175.getType());
    assertEquals(Engine.EngineType.STD, std180.getType());
    assertEquals(Engine.EngineType.XL, xl330.getType());
    assertEquals(Engine.EngineType.XL, xl335.getType());
  }

  /** There must be heat sinks in the item database */
  @Test
  public void testHeatsinks() {
    final Collection<HeatSink> heatsinks = ItemDB.lookup(HeatSink.class);

    // Should contain at least double and standard (+ typically clan versions)
    assertTrue(heatsinks.size() >= 2);

    // All parameters should be positive
    final HeatSink shs = ItemDB.SHS;
    final HeatSink dhs = ItemDB.DHS;
    for (final HeatSink heatSink : heatsinks) {
      assertTrue(heatSink.getDissipation() > 0);
      assertTrue(heatSink.getCapacity() > 0);
    }

    assertNotNull(dhs);
    assertNotNull(shs);

    // Double should have higher dissipation
    assertTrue(dhs.getDissipation() > shs.getDissipation());

    assertEquals(3, dhs.getSlots());
    assertEquals(1, shs.getSlots());
  }

  /**
   * According to: <a href=
   * "http://mwomercs.com/forums/topic/147990-paging-karl-bergkarl-berg-please-pick-up-the-white-courtesy-phone/page__view__findpost__p__3484591"
   * >here</a> C.A.S.E. can not be critically hit and should not be a part of the calculations.
   */
  @Test
  public void testIsCrittable_Case() {
    assertFalse(ItemDB.CASE.canBeCriticallyHit());
  }

  /** XL engine sides do affect the critical hit rolls. */
  @Test
  public void testIsCrittable_EngineSides() {
    for (final Engine e : ItemDB.lookup(Engine.class)) {
      e.getSide()
          .ifPresent(
              side -> {
                assertTrue(side.canBeCriticallyHit());
                assertTrue(side.canBeCriticallyHit());
              });
    }
  }

  @Test
  public void testJumpJets() throws Exception {
    final JumpJet jj = (JumpJet) ItemDB.lookup(1503); // Class IV JJ

    assertTrue(jj.getDuration() > 1);
    assertTrue(jj.getJumpHeat() != 0.0);
    assertTrue(jj.getForce() > 1);

    assertTrue(jj.getMinTons() > 0);
    assertTrue(jj.getMaxTons() > 0);
    assertTrue(jj.getMaxTons() > jj.getMinTons());
  }

  /**
   * ECM/BAP/CC/CASE etc should exist and only be equippable on the correct mechs
   *
   * @throws Exception
   */
  @Test
  public void testModules() throws Exception {

    final Item ECM = ItemDB.lookup("GUARDIAN ECM");
    final Item BAP = ItemDB.lookup("BEAGLE ACTIVE PROBE");
    final Item Case = ItemDB.lookup("C.A.S.E.");

    final Item JJC3 = ItemDB.lookup("Jump Jets - Class III");
    final Item JJC4 = ItemDB.lookup("Jump Jets - Class IV");
    final Item JJC5 = ItemDB.lookup("Jump Jets - Class V");

    assertEquals(2, ECM.getSlots());
    assertEquals(1, BAP.getSlots());
    assertEquals(1, Case.getSlots());
    assertEquals(1, JJC3.getSlots());
    assertEquals(1, JJC4.getSlots());
    assertEquals(1, JJC5.getSlots());

    assertEquals(1.5, ECM.getMass(), 0.0);
    assertEquals(1, BAP.getMass(), 0.0);
    assertEquals(0.0, Case.getMass(), 0.0);
    assertEquals(1, JJC3.getMass(), 0.0);
    assertEquals(0.5, JJC4.getMass(), 0.0);
    assertEquals(0.5, JJC5.getMass(), 0.0);

    assertEquals(HardPointType.ECM, ECM.getHardpointType());
  }

  /** No item must exist twice in the database! */
  @Test
  public void testNoDoubles() {
    final Collection<Item> items = ItemDB.lookup(Item.class);

    final List<Item> found = new ArrayList<>();
    for (final Item item : items) {
      if (!found.contains(item)) {
        found.add(item);
      } else {
        fail();
      }
    }
  }
}
