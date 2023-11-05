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
package org.lisoft.lsml.command;

import static org.junit.Assert.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

import java.util.Arrays;
import java.util.List;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.lisoft.lsml.messages.MessageXBar;
import org.lisoft.lsml.model.UpgradeDB;
import org.lisoft.lsml.model.helpers.MockLoadoutContainer;
import org.lisoft.lsml.model.loadout.EquipResult;
import org.lisoft.lsml.model.loadout.Loadout;
import org.lisoft.lsml.model.loadout.LoadoutStandard;
import org.lisoft.lsml.util.CommandStack;
import org.lisoft.lsml.util.TestHelpers;
import org.lisoft.mwo_data.equipment.Ammunition;
import org.lisoft.mwo_data.equipment.GuidanceUpgrade;
import org.lisoft.mwo_data.equipment.Item;
import org.lisoft.mwo_data.equipment.MissileWeapon;
import org.lisoft.mwo_data.mechs.Upgrades;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

/**
 * Test suite for {@link CmdSetGuidanceType}.
 *
 * @author Li Song
 */
@RunWith(MockitoJUnitRunner.Silent.class)
public class CmdSetGuidanceTypeTest {
  final MockLoadoutContainer mlc = new MockLoadoutContainer();
  @Mock GuidanceUpgrade newGuidance;
  @Mock GuidanceUpgrade oldGuidance;
  @Mock MessageXBar xBar;

  /**
   * Apply shall change the {@link GuidanceUpgrade} of the {@link Upgrades}s object of the {@link
   * LoadoutStandard} given as argument.
   */
  @Test
  public void testApply() throws Exception {
    when(mlc.upgrades.getGuidance()).thenReturn(oldGuidance);
    final CommandStack stack = new CommandStack(0);
    when(mlc.loadout.getFreeMass()).thenReturn(100.0);
    when(mlc.loadout.getFreeSlots()).thenReturn(100);

    stack.pushAndApply(new CmdSetGuidanceType(xBar, mlc.loadout, newGuidance));

    verify(mlc.upgrades).setGuidance(newGuidance);
  }

  /** If apply fails, the changes shall have been rolled back completely. */
  @Test
  public void testApply_FailRollback() {
    when(mlc.loadout.getFreeMass()).thenReturn(0.0);
    when(mlc.loadout.getUpgradeMassCost(newGuidance)).thenReturn(1.0);
    when(mlc.upgrades.getGuidance()).thenReturn(oldGuidance);

    try {
      new CommandStack(0).pushAndApply(new CmdSetGuidanceType(xBar, mlc.loadout, newGuidance));
    } catch (final Throwable t) {
      /* No-Op */
    }

    verify(mlc.upgrades, never()).setGuidance(any(GuidanceUpgrade.class));
  }

  /**
   * Apply shall delegate to the upgrades object to change all Missile Weapons and Ammunition types.
   */
  @Test
  public void testApply_changeMissileLaunchersAndAmmo() throws Exception {
    when(mlc.upgrades.getGuidance()).thenReturn(oldGuidance);
    final CommandStack stack = new CommandStack(0);
    when(mlc.loadout.getFreeMass()).thenReturn(100.0);
    when(mlc.loadout.getFreeSlots()).thenReturn(100);
    when(mlc.loadout.canEquipDirectly(any(Item.class))).thenReturn(EquipResult.SUCCESS);

    final MissileWeapon lrm5 = mock(MissileWeapon.class);
    final MissileWeapon lrm5Artemis = mock(MissileWeapon.class);
    final MissileWeapon narc = mock(MissileWeapon.class);
    final Ammunition lrmAmmo = mock(Ammunition.class);
    final Ammunition lrmAmmoArtemis = mock(Ammunition.class);
    final Ammunition narcAmmo = mock(Ammunition.class);

    final List<Item> rlItems = Arrays.asList(lrm5, lrmAmmo);
    final List<Item> ltItems = Arrays.asList(lrm5, narcAmmo, narc, lrmAmmo);

    when(newGuidance.upgrade(lrm5)).thenReturn(lrm5Artemis);
    when(newGuidance.upgrade(narc)).thenReturn(narc);
    when(newGuidance.upgrade(lrmAmmo)).thenReturn(lrmAmmoArtemis);
    when(newGuidance.upgrade(narcAmmo)).thenReturn(narcAmmo);
    when(mlc.rl.canEquip(any(Item.class))).thenReturn(EquipResult.SUCCESS);
    when(mlc.lt.canEquip(any(Item.class))).thenReturn(EquipResult.SUCCESS);
    when(mlc.rl.getItemsEquipped()).thenReturn(rlItems);
    when(mlc.lt.getItemsEquipped()).thenReturn(ltItems);
    when(mlc.rl.canRemoveItem(any(Item.class))).thenReturn(true);
    when(mlc.lt.canRemoveItem(any(Item.class))).thenReturn(true);

    stack.pushAndApply(new CmdSetGuidanceType(xBar, mlc.loadout, newGuidance));

    // FIXME: Verify... I can't gain access to verify this in any way...
  }

  @Test
  public void testUndo() throws Exception {
    final Loadout loadout =
        TestHelpers.parse("AA0830a1|jb|i^|i^p=1|k?|k?|FO|FO|FO|[<2q=1|@@|@@|@@|^<2r41|`?|f?|AO|_Os41|`?|FOtD1|_OuD1vB0w;07070");
    final Loadout loadoutOriginal =
        TestHelpers.parse("AA0830a1|jb|i^|i^p=1|k?|k?|FO|FO|FO|[<2q=1|@@|@@|@@|^<2r41|`?|f?|AO|_Os41|`?|FOtD1|_OuD1vB0w;07070");
    final CommandStack stack = new CommandStack(1);

    stack.pushAndApply(new CmdSetGuidanceType(xBar, loadout, UpgradeDB.STD_GUIDANCE));
    stack.undo();

    assertEquals(loadoutOriginal, loadout);
  }
}
