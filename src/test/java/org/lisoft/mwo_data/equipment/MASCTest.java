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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import org.junit.Test;
import org.lisoft.mwo_data.Faction;

/**
 * Test suite for MASC.
 *
 * @author Li Song
 */
public class MASCTest {

  @Test
  public void testConstruction() {
    final String aUiName = "name";
    final String aUiDesc = "desc";
    final String aMwoName = "mwo";
    final int aMwoId = 10;
    final int aSlots = 11;
    final double aTons = 12;
    final double aHP = 13.3;
    final Faction aFaction = Faction.CLAN;
    final int aMinTons = 14;
    final int aMaxTons = 15;
    final double aBoostTurn = 16;
    final double aBoostDecel = 17;
    final double aBoostAccel = 18;
    final double aBoostSpeed = 19;
    final Integer aAllowedAmount = 1;
    final MASC cut =
        new MASC(
            aUiName,
            aUiDesc,
            aMwoName,
            aMwoId,
            aSlots,
            aTons,
            aHP,
            aFaction,
            aAllowedAmount,
            aMinTons,
            aMaxTons,
            aBoostSpeed,
            aBoostAccel,
            aBoostDecel,
            aBoostTurn);

    assertEquals(aUiName, cut.getName());
    assertEquals(aUiDesc, cut.getDescription());
    assertEquals(aMwoName, cut.getKey());
    assertEquals(aMwoId, cut.getId());
    assertEquals(aSlots, cut.getSlots());
    assertEquals(aTons, cut.getMass(), 0.0);
    assertEquals(aHP, cut.getHealth(), 0.0);
    assertEquals(aFaction, cut.getFaction());
    assertEquals(aMinTons, cut.getMinTons());
    assertEquals(aMaxTons, cut.getMaxTons());
    assertEquals(aBoostSpeed, cut.getSpeedBoost(), 0.0);
    assertTrue(cut.getAllowedAmountOfType().isPresent());
    assertEquals(aAllowedAmount, cut.getAllowedAmountOfType().get());
  }
}
