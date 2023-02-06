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
import static org.mockito.Mockito.*;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import org.junit.Before;
import org.junit.Test;
import org.lisoft.lsml.messages.ItemMessage;
import org.lisoft.lsml.messages.MessageDelivery;
import org.lisoft.lsml.model.helpers.MockLoadoutContainer;
import org.lisoft.lsml.util.CommandStack.Command;
import org.lisoft.mwo_data.equipment.AmmoWeapon;
import org.lisoft.mwo_data.equipment.Ammunition;
import org.lisoft.mwo_data.equipment.Item;
import org.lisoft.mwo_data.equipment.Weapon;

/**
 * Test suite for {@link CmdRemoveMatching}.
 *
 * @author Li Song
 */
public class CmdRemoveMatchingTest {
  private static final String DESCRIPTION = "desc";
  final List<Item> ctItems = new ArrayList<>();
  final List<Item> hdItems = new ArrayList<>();
  final List<Item> laItems = new ArrayList<>();
  final List<Item> llItems = new ArrayList<>();
  final List<Item> ltItems = new ArrayList<>();
  final List<Item> raItems = new ArrayList<>();
  final List<Item> rlItems = new ArrayList<>();
  final List<Item> rtItems = new ArrayList<>();
  private final MessageDelivery mdt = mock(MessageDelivery.class);
  private final MockLoadoutContainer mlc = new MockLoadoutContainer();

  @Before
  public void setup() {
    when(mlc.la.getItemsEquipped()).thenReturn(laItems);
    when(mlc.lt.getItemsEquipped()).thenReturn(ltItems);
    when(mlc.ll.getItemsEquipped()).thenReturn(llItems);
    when(mlc.hd.getItemsEquipped()).thenReturn(hdItems);
    when(mlc.ct.getItemsEquipped()).thenReturn(ctItems);
    when(mlc.rt.getItemsEquipped()).thenReturn(rtItems);
    when(mlc.rl.getItemsEquipped()).thenReturn(rlItems);
    when(mlc.ra.getItemsEquipped()).thenReturn(raItems);
  }

  @Test
  public void testPredicate() throws Exception {
    final Item item1 = mock(Item.class);
    final Item item2 = mock(Item.class);
    final Item item3 = mock(Item.class);
    final Item item4 = mock(Item.class);

    laItems.addAll(Arrays.asList(item1, item2, item3, item4));
    ctItems.addAll(Arrays.asList(item1, item1, item1, item2));
    when(mlc.la.canRemoveItem(item1)).thenReturn(true);
    when(mlc.la.canRemoveItem(item2)).thenReturn(true);
    when(mlc.ct.canRemoveItem(item1)).thenReturn(true);
    when(mlc.ct.canRemoveItem(item2)).thenReturn(true);

    final CmdRemoveMatching cut =
        new CmdRemoveMatching(DESCRIPTION, mdt, mlc.loadout, i -> i == item1 || i == item2);

    cut.apply();

    verify(mlc.la).removeItem(item1);
    verify(mlc.la).removeItem(item2);
    verify(mlc.ct, times(3)).removeItem(item1);
    verify(mlc.ct).removeItem(item2);

    verify(mlc.la, never()).removeItem(item3);
    verify(mlc.la, never()).removeItem(item4);
    verify(mlc.ct, never()).removeItem(item3);
    verify(mlc.ct, never()).removeItem(item4);

    verify(mdt, times(6)).post(any(ItemMessage.class));
  }

  @Test
  public void testRemoveWeaponSystem() throws Exception {
    final Weapon item1 = mock(Weapon.class);
    final Weapon item2 = mock(Weapon.class);
    when(item1.getName()).thenReturn("item1");
    when(item2.getName()).thenReturn("item2");

    laItems.addAll(Arrays.asList(item1, item2));
    ctItems.addAll(Arrays.asList(item1, item1, item1, item2));
    when(mlc.la.canRemoveItem(item1)).thenReturn(true);
    when(mlc.ct.canRemoveItem(item1)).thenReturn(true);

    final Command cut = CmdRemoveMatching.removeWeaponSystem(mdt, mlc.loadout, item1);
    cut.apply();
    assertEquals("remove all item1", cut.describe());

    verify(mlc.la).removeItem(item1);
    verify(mlc.ct, times(3)).removeItem(item1);

    verify(mlc.la, never()).removeItem(item2);
    verify(mlc.ct, never()).removeItem(item2);

    verify(mdt, times(4)).post(any(ItemMessage.class));
  }

  @Test
  public void testRemoveWeaponSystemAmmoWeapon() throws Exception {
    final Ammunition ammo = mock(Ammunition.class);
    final Ammunition ammoHalf = mock(Ammunition.class);
    final AmmoWeapon item1 = mock(AmmoWeapon.class);
    final Weapon item2 = mock(Weapon.class);
    when(item1.getName()).thenReturn("item1");
    when(item1.getAmmoType()).thenReturn(ammo);
    when(item1.getAmmoHalfType()).thenReturn(ammoHalf);
    when(item2.getName()).thenReturn("item2");

    laItems.addAll(Arrays.asList(item1, item2, ammo, ammo));
    ctItems.addAll(Arrays.asList(item1, item1, item1, item2, ammo, ammoHalf));
    when(mlc.la.canRemoveItem(item1)).thenReturn(true);
    when(mlc.la.canRemoveItem(ammo)).thenReturn(true);
    when(mlc.ct.canRemoveItem(item1)).thenReturn(true);
    when(mlc.ct.canRemoveItem(ammo)).thenReturn(true);
    when(mlc.ct.canRemoveItem(ammoHalf)).thenReturn(true);

    final Command cut = CmdRemoveMatching.removeWeaponSystem(mdt, mlc.loadout, item1);
    cut.apply();
    assertEquals("remove all item1 and ammo", cut.describe());

    verify(mlc.la).removeItem(item1);
    verify(mlc.la, times(2)).removeItem(ammo);
    verify(mlc.ct, times(3)).removeItem(item1);
    verify(mlc.ct).removeItem(ammo);
    verify(mlc.ct).removeItem(ammoHalf);

    verify(mlc.la, never()).removeItem(item2);
    verify(mlc.ct, never()).removeItem(item2);

    verify(mdt, times(8)).post(any(ItemMessage.class));
  }

  @Test
  public void testRemoveWeaponSystemBuiltInAmmo() throws Exception {
    final AmmoWeapon item1 = mock(AmmoWeapon.class);
    final Weapon item2 = mock(Weapon.class);
    when(item1.getName()).thenReturn("item1");
    when(item1.hasBuiltInAmmo()).thenReturn(true);
    when(item2.getName()).thenReturn("item2");

    laItems.addAll(Arrays.asList(item1, item2));
    ctItems.addAll(Arrays.asList(item1, item1, item1, item2));
    when(mlc.la.canRemoveItem(item1)).thenReturn(true);
    when(mlc.ct.canRemoveItem(item1)).thenReturn(true);

    final Command cut = CmdRemoveMatching.removeWeaponSystem(mdt, mlc.loadout, item1);
    cut.apply();
    assertEquals("remove all item1", cut.describe());

    verify(mlc.la).removeItem(item1);
    verify(mlc.ct, times(3)).removeItem(item1);

    verify(mlc.la, never()).removeItem(item2);
    verify(mlc.ct, never()).removeItem(item2);

    verify(mdt, times(4)).post(any(ItemMessage.class));
  }
}
