/*
 * Li Song Mechlab - A 'mech building tool for PGI's MechWarrior: Online.
 * Copyright (C) 2023  Li Song
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
package org.lisoft.lsml.model.metrics;

import static org.junit.Assert.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import org.junit.Before;
import org.junit.Test;
import org.lisoft.lsml.model.helpers.MockLoadoutContainer;
import org.lisoft.mwo_data.ItemDB;
import org.lisoft.mwo_data.equipment.BallisticWeapon;
import org.lisoft.mwo_data.equipment.EnergyWeapon;
import org.lisoft.mwo_data.equipment.Weapon;
import org.lisoft.mwo_data.modifiers.Modifier;
import org.lisoft.mwo_data.modifiers.ModifierDescription;
import org.lisoft.mwo_data.modifiers.ModifierType;
import org.lisoft.mwo_data.modifiers.Operation;
import org.mockito.Mockito;

public class GhostHeatTest {
  private final MockLoadoutContainer mlc = new MockLoadoutContainer();
  private final EnergyWeapon ppc;
  private final List<Weapon> weapons = new ArrayList<>();
  private GhostHeat cut;

  public GhostHeatTest() {
    ppc = mock(EnergyWeapon.class);
    when(ppc.getHeat(any())).thenReturn(9.5);
    when(ppc.getGhostHeatMaxFreeAlpha(any())).thenReturn(2);
    when(ppc.getGhostHeatMultiplier()).thenReturn(7.0);
    when(ppc.getGhostHeatGroup()).thenReturn(4);
  }

  @Before
  public void setup() {
    when(mlc.loadout.items(Weapon.class)).thenReturn(weapons);
    cut = new GhostHeat(mlc.loadout);
  }

  @Test
  public void testCalculate_13FrickingLasers() throws Exception {
    // According to personal conversation with Karl Berg:
    // "Hi Li Song! I'll go bug Paul again right now. I didn't get a response to my earlier email.
    // OK, the last number simply caps and repeats for any weapons past 13."
    final Weapon slas = (Weapon) ItemDB.lookup("SMALL LASER");
    final int lasers = 12;
    for (int i = 0; i < lasers; ++i) {
      weapons.add(slas);
    }
    final double result12 = cut.calculate();
    weapons.add(slas);
    final double maxHeatScale = 5.0;

    // Execute
    final double result13 = cut.calculate();

    // Verify
    final double expected =
        result12 + slas.getHeat(null) * maxHeatScale * slas.getGhostHeatMultiplier();
    assertEquals(expected, result13, 0.0);
  }

  @Test
  public void testCalculate_2ppc() {
    // Example from: http://mwomercs.com/forums/topic/127904-heat-scale-the-maths/
    weapons.add(ppc);
    weapons.add(ppc);

    final double result = cut.calculate();
    assertEquals(0.0, result, 0.0);
  }

  @Test
  public void testCalculate_3ppc() {
    // Example from: http://mwomercs.com/forums/topic/127904-heat-scale-the-maths/
    weapons.add(ppc);
    weapons.add(ppc);
    weapons.add(ppc);

    final double result = cut.calculate();
    assertEquals(11.97, result, 0.01); // Adjusting for new base head (9.5)
  }

  @Test
  public void testCalculate_4ppc() {
    // Example from: http://mwomercs.com/forums/topic/127904-heat-scale-the-maths/
    weapons.add(ppc);
    weapons.add(ppc);
    weapons.add(ppc);
    weapons.add(ppc);

    final double result = cut.calculate();
    assertEquals(31.92, result, 0.01); // Adjusting for new base head (9.5)
  }

  @Test
  public void testCalculate_HSL_Quirk() throws Exception {
    EnergyWeapon realPPC = (EnergyWeapon) ItemDB.lookup("PPC");
    for (int i = 0; i < realPPC.getGhostHeatMaxFreeAlpha(null); ++i) {
      weapons.add(realPPC);
    }
    weapons.add(realPPC);

    final ModifierDescription hslDescription =
        new ModifierDescription(
            "",
            "",
            Operation.ADD,
            ModifierDescription.SEL_ALL,
            ModifierDescription.SPEC_WEAPON_MAX_FREE_ALPHA,
            ModifierType.POSITIVE_GOOD);
    final Modifier hslQuirk = new Modifier(hslDescription, 1);
    final List<Modifier> modifiers = Collections.singletonList(hslQuirk);
    when(mlc.loadout.getAllModifiers()).thenReturn(modifiers);

    final double result = cut.calculate();
    assertEquals(0.0, result, 0.01);
  }

  @Test
  public void testCalculate_NoGhostHeatWeapons() throws Exception {
    // Example from: http://mwomercs.com/forums/topic/127904-heat-scale-the-maths/
    weapons.add((Weapon) ItemDB.lookup("SMALL LASER"));
    weapons.add((Weapon) ItemDB.lookup("SML PULSE LASER"));
    weapons.add((Weapon) ItemDB.lookup("AC/5"));
    weapons.add((Weapon) ItemDB.lookup("LRM5"));
    weapons.add((Weapon) ItemDB.lookup("FLAMER"));

    final double result = cut.calculate();
    assertEquals(0.0, result, 0.0);
  }

  @Test
  public void testCalculate_WeaponGroups() {
    // Example from: http://mwomercs.com/forums/topic/127904-heat-scale-the-maths/

    final int aGroup = 3;
    final Collection<Weapon> groupWeapons = new ArrayList<>();
    groupWeapons.add(ppc);
    groupWeapons.add(ppc);
    groupWeapons.add(ppc);
    groupWeapons.add(ppc);
    Mockito.when(mlc.weaponGroups.getWeapons(aGroup, mlc.loadout)).thenReturn(groupWeapons);

    weapons.add(ppc);
    weapons.add(ppc);
    weapons.add(ppc);
    weapons.add(ppc);
    weapons.add(ppc);
    weapons.add(ppc);
    weapons.add(ppc);
    weapons.add(ppc);

    cut = new GhostHeat(mlc.loadout, aGroup);
    final double result = cut.calculate();
    assertEquals(31.92, result, 0.01); // Adjusting for new base head (9.5)
  }

  @Test
  public void testCalculate_linkedMixedGroup() throws Exception {
    // Example from: http://mwomercs.com/forums/topic/127904-heat-scale-the-maths/

    final Weapon lplas = (Weapon) ItemDB.lookup("LRG PULSE LASER");
    final Weapon erllas = (Weapon) ItemDB.lookup("ER LARGE LASER");
    final Weapon llas = (Weapon) ItemDB.lookup("LARGE LASER");

    weapons.add(erllas);
    weapons.add(llas);
    weapons.add(lplas);
    weapons.add(llas);
    weapons.add(llas);

    Weapon max = lplas.getHeat(null) > erllas.getHeat(null) ? lplas : erllas;
    max = max.getHeat(null) > llas.getHeat(null) ? max : llas;

    final double result = cut.calculate();
    assertEquals(max.getGhostHeatMultiplier() * max.getHeat(null) * (0.30 + 0.45), result, 0.0001);
  }

  @Test
  public void testCalculate_linkedMixedMaxFreeAlpha() {
    // Based on sciencing in the in-game mechlab, the lowest maxFreeAlpha of any weapon applies to
    // the whole group.
    EnergyWeapon lightPPC = mock(EnergyWeapon.class);
    when(lightPPC.getHeat(any())).thenReturn(4.5);
    when(lightPPC.getGhostHeatMaxFreeAlpha(any())).thenReturn(3);
    when(lightPPC.getGhostHeatMultiplier()).thenReturn(7.0);
    when(lightPPC.getGhostHeatGroup()).thenReturn(4);

    EnergyWeapon erPPC = mock(EnergyWeapon.class);
    when(erPPC.getHeat(any())).thenReturn(12.0);
    when(erPPC.getGhostHeatMaxFreeAlpha(any())).thenReturn(2);
    when(erPPC.getGhostHeatMultiplier()).thenReturn(4.0);
    when(erPPC.getGhostHeatGroup()).thenReturn(4);

    weapons.add(lightPPC);
    weapons.add(lightPPC);
    weapons.add(lightPPC);

    // Three light PPCs cause no ghost heat
    assertEquals(0, cut.calculate(), 0.0001);

    weapons.remove(lightPPC);
    weapons.add(erPPC);

    // Two Light PPCs and one ER PPC causes ghost heat because the ER PPC brings down the max free
    // alpha of the
    // group to 2.
    final double expected = 0.18 * 12 * 7;
    assertEquals(expected, cut.calculate(), 0.0001);
  }

  @Test
  public void testCalculate_linkedMixedMultiplier() {
    EnergyWeapon erPPC = mock(EnergyWeapon.class);
    when(erPPC.getHeat(any())).thenReturn(14.5);
    when(erPPC.getGhostHeatMaxFreeAlpha(any())).thenReturn(2);
    when(erPPC.getGhostHeatMultiplier()).thenReturn(7.0);
    when(erPPC.getGhostHeatGroup()).thenReturn(4);

    BallisticWeapon gauss = mock(BallisticWeapon.class);
    when(gauss.getHeat(any())).thenReturn(1.0);
    when(gauss.getGhostHeatMaxFreeAlpha(any())).thenReturn(2);
    when(gauss.getGhostHeatMultiplier()).thenReturn(8.5);
    when(gauss.getGhostHeatGroup()).thenReturn(4);

    weapons.add(erPPC);
    weapons.add(erPPC);
    weapons.add(gauss);

    final double result = cut.calculate();
    final double expected = 0.18 * 14.5 * 8.5;
    assertEquals(expected, result, 0.0001);
  }

  @Test
  public void testCalculate_unGrouped() {
    // Weapons that do not have a group specified are treated as their own group

    BallisticWeapon ac20 = mock(BallisticWeapon.class);
    when(ac20.getHeat(any())).thenReturn(5.0);
    when(ac20.getGhostHeatMaxFreeAlpha(any())).thenReturn(1);
    when(ac20.getGhostHeatMultiplier()).thenReturn(24.0);
    when(ac20.getGhostHeatGroup()).thenReturn(0);

    EnergyWeapon mlas = mock(EnergyWeapon.class);
    when(mlas.getHeat(any())).thenReturn(3.25);
    when(mlas.getGhostHeatMaxFreeAlpha(any())).thenReturn(6);
    when(mlas.getGhostHeatMultiplier()).thenReturn(1.0);
    when(mlas.getGhostHeatGroup()).thenReturn(0);

    weapons.add(ac20);
    weapons.add(mlas);
    weapons.add(ac20);
    weapons.add(mlas);
    weapons.add(mlas);
    weapons.add(mlas);
    weapons.add(mlas);
    weapons.add(mlas);
    weapons.add(mlas);
    weapons.add(mlas);
    weapons.add(mlas);

    final double result = cut.calculate();
    final double ac20Penalty = 24.0 * 5 * 0.08;
    final double mlasPenalty = 1 * 3.25 * (0.80 + 1.10 + 1.50);
    assertEquals(ac20Penalty + mlasPenalty, result, 0.0001);
  }

  @Test
  public void testCalculate_unlinkedMixedGroup() {
    // Example from: http://mwomercs.com/forums/topic/127904-heat-scale-the-maths/

    BallisticWeapon ac20 = mock(BallisticWeapon.class);
    when(ac20.getHeat(any())).thenReturn(5.0);
    when(ac20.getGhostHeatMaxFreeAlpha(any())).thenReturn(1);
    when(ac20.getGhostHeatMultiplier()).thenReturn(24.0);
    when(ac20.getGhostHeatGroup()).thenReturn(9);

    EnergyWeapon mlas = mock(EnergyWeapon.class);
    when(mlas.getHeat(any())).thenReturn(3.25);
    when(mlas.getGhostHeatMaxFreeAlpha(any())).thenReturn(6);
    when(mlas.getGhostHeatMultiplier()).thenReturn(1.0);
    when(mlas.getGhostHeatGroup()).thenReturn(12);

    weapons.add(ac20);
    weapons.add(mlas);
    weapons.add(ac20);
    weapons.add(mlas);
    weapons.add(mlas);
    weapons.add(mlas);
    weapons.add(mlas);
    weapons.add(mlas);
    weapons.add(mlas);
    weapons.add(mlas);
    weapons.add(mlas);

    final double result = cut.calculate();
    final double ac20Penalty = 24.0 * 5 * 0.08;
    final double mlasPenalty = 1 * 3.25 * (0.80 + 1.10 + 1.50);
    assertEquals(ac20Penalty + mlasPenalty, result, 0.0001);
  }
}
