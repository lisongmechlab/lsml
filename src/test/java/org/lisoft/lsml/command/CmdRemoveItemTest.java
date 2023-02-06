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

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

import java.util.ArrayList;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.lisoft.lsml.messages.ItemMessage;
import org.lisoft.lsml.messages.ItemMessage.Type;
import org.lisoft.lsml.messages.MessageXBar;
import org.lisoft.lsml.model.loadout.ConfiguredComponent;
import org.lisoft.lsml.model.loadout.EquipException;
import org.lisoft.lsml.model.loadout.EquipResult;
import org.lisoft.lsml.model.loadout.Loadout;
import org.lisoft.lsml.mwo_data.ItemDB;
import org.lisoft.lsml.mwo_data.equipment.Engine;
import org.lisoft.lsml.mwo_data.equipment.HeatSinkUpgrade;
import org.lisoft.lsml.mwo_data.equipment.Internal;
import org.lisoft.lsml.mwo_data.equipment.Item;
import org.lisoft.lsml.mwo_data.equipment.UpgradeDB;
import org.lisoft.lsml.mwo_data.mechs.Component;
import org.lisoft.lsml.mwo_data.mechs.Location;
import org.lisoft.lsml.mwo_data.mechs.Upgrades;
import org.mockito.InOrder;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.MockitoJUnitRunner;

/**
 * Test suite for {@link CmdRemoveItem}.
 *
 * @author Li Song
 */
@SuppressWarnings("javadoc")
@RunWith(MockitoJUnitRunner.Silent.class)
public class CmdRemoveItemTest {
  @Mock private ConfiguredComponent component;
  @Mock private Component internalPart;
  @Mock private Loadout loadout;
  @Mock private Upgrades upgrades;
  @Mock private MessageXBar xBar;

  @Before
  public void setup() {
    Mockito.when(loadout.getUpgrades()).thenReturn(upgrades);
    Mockito.when(component.getInternalComponent()).thenReturn(internalPart);
    Mockito.when(internalPart.getLocation()).thenReturn(Location.CenterTorso);
  }

  /**
   * Internal items can't be removed. Shall throw directly on creation.
   *
   * <p>It is a programmer error to attempt to remove an internal.
   */
  @SuppressWarnings("unused")
  @Test(expected = IllegalArgumentException.class)
  public void testCantRemoveInternal() {
    final Internal item = Mockito.mock(Internal.class);
    new CmdRemoveItem(xBar, loadout, component, item);
  }

  /**
   * If an item can't be removed, an exception shall be thrown when the operation is applied.
   *
   * @throws EquipException
   */
  @Test(expected = IllegalArgumentException.class)
  public void testCantRemoveItem() throws EquipException {
    CmdRemoveItem cut = null;
    try {
      final Item item = ItemDB.lookup("LRM 20");
      Mockito.when(component.getItemsEquipped()).thenReturn(new ArrayList<>());
      cut = new CmdRemoveItem(xBar, loadout, component, item);
    } catch (final Throwable t) {
      fail("Setup failed");
      return;
    }

    cut.apply();
  }

  @Test
  public void testDescription() {
    final Item item = ItemDB.ECM;

    final CmdRemoveItem cut = new CmdRemoveItem(xBar, loadout, component, item);

    assertTrue(cut.describe().contains("remove"));
    assertTrue(cut.describe().contains("from"));
    assertTrue(cut.describe().contains(component.getInternalComponent().getLocation().toString()));
    assertTrue(cut.describe().contains(item.getName()));
  }

  @Test
  public void testDescription_artemis() throws Exception {
    final Item item = ItemDB.lookup("LRM 20");
    Mockito.when(upgrades.getGuidance()).thenReturn(UpgradeDB.ARTEMIS_IV);

    final CmdRemoveItem cut = new CmdRemoveItem(xBar, loadout, component, item);

    assertTrue(cut.describe().contains("remove"));
    assertTrue(cut.describe().contains("from"));
    assertTrue(cut.describe().contains(component.getInternalComponent().getLocation().toString()));
    assertTrue(cut.describe().contains(item.getName()));
  }

  @Test
  public final void testRemoveECMWithStealth() {
    // Setup
    when(upgrades.getArmour()).thenReturn(UpgradeDB.IS_STEALTH_ARMOUR);
    final Item item = ItemDB.ECM;
    when(component.canRemoveItem(item)).thenReturn(true);
    final CmdRemoveItem cut = new CmdRemoveItem(xBar, loadout, component, item);

    // Execute (do)
    try {
      cut.apply();
      fail("Expected EquipException!");
    } catch (final EquipException e) {
      assertSame(EquipResult.EquipResultType.CannotRemoveECM, e.getResult().getType());
    }
    verify(component, never()).removeItem(any());
    verify(xBar, never()).post(any());
  }

  @Test
  public final void testRemoveItem() throws EquipException {
    // Setup
    final Item item = ItemDB.ECM;
    final int index = 4;
    when(component.canRemoveItem(item)).thenReturn(true);
    when(component.removeItem(item)).thenReturn(index);
    when(component.addItem(item)).thenReturn(index);
    final CmdRemoveItem cut = new CmdRemoveItem(xBar, loadout, component, item);

    // Execute (do)
    cut.apply();

    // Verify (do)
    final InOrder io = inOrder(component, xBar);
    io.verify(component).removeItem(item);
    io.verify(xBar).post(new ItemMessage(component, Type.Removed, item, index));

    // Execute (undo)
    cut.undo();

    // Verify (undo)
    io.verify(component).addItem(item);
    io.verify(xBar).post(new ItemMessage(component, Type.Added, item, index));
  }

  @Test
  public final void testRemoveItem_NoMessages() throws EquipException {
    // Setup
    final Item item = ItemDB.ECM;
    final int index = 4;
    when(component.canRemoveItem(item)).thenReturn(true);
    when(component.removeItem(item)).thenReturn(index);
    when(component.addItem(item)).thenReturn(index);
    final CmdRemoveItem cut = new CmdRemoveItem(null, loadout, component, item);

    // Execute (do)
    cut.apply();

    // Verify (do)
    final InOrder io = inOrder(component, xBar);
    io.verify(component).removeItem(item);

    // Execute (undo)
    cut.undo();

    // Verify (undo)
    io.verify(component).addItem(item);
  }

  /** Removing a standard engine shall also remove engine heat sinks (SHS). */
  @Test
  public final void testRemoveItem_StdEngine_DHS() throws Exception {
    final Engine engine = (Engine) ItemDB.lookup("STD ENGINE 300");
    testRemoveEngine(engine, UpgradeDB.IS_DHS, 2);
  }

  /** Removing a standard engine shall also remove engine heat sinks (DHS). */
  @Test
  public final void testRemoveItem_StdEngine_SHS() throws Exception {
    final Engine engine = (Engine) ItemDB.lookup("STD ENGINE 300");
    testRemoveEngine(engine, UpgradeDB.IS_SHS, 2);
  }

  /** Removing an XL engine shall also remove ENGINE_INTERNAL from side torsii */
  @Test
  public final void testRemoveItem_XLEngine() throws Exception {
    final Engine engine = (Engine) ItemDB.lookup("XL ENGINE 300");
    testRemoveEngine(engine, UpgradeDB.IS_DHS, 0);
  }

  private void testRemoveEngine(Engine aEngine, HeatSinkUpgrade aSinkUpgrade, int aEngineHS)
      throws EquipException {
    // Setup
    final Item hsType = aSinkUpgrade.getHeatSinkType();
    final int index = 0;
    final int indexLt = 3;
    final int indexRt = 5;

    final ConfiguredComponent lt = mock(ConfiguredComponent.class);
    final ConfiguredComponent rt = mock(ConfiguredComponent.class);
    final Internal side = aEngine.getSide().orElse(null);
    when(lt.removeItem(side)).thenReturn(indexLt);
    when(rt.removeItem(side)).thenReturn(indexRt);
    when(lt.addItem(side)).thenReturn(indexLt);
    when(rt.addItem(side)).thenReturn(indexRt);

    when(upgrades.getHeatSink()).thenReturn(aSinkUpgrade);
    when(loadout.getComponent(Location.LeftTorso)).thenReturn(lt);
    when(loadout.getComponent(Location.RightTorso)).thenReturn(rt);
    when(loadout.getUpgrades()).thenReturn(upgrades);
    when(component.getEngineHeatSinks()).thenReturn(aEngineHS);
    when(component.canRemoveItem(aEngine)).thenReturn(true);
    when(component.removeItem(aEngine)).thenReturn(index);
    when(component.addItem(aEngine)).thenReturn(index);
    when(component.removeItem(hsType)).thenReturn(-1);
    when(component.addItem(hsType)).thenReturn(-1);
    final CmdRemoveItem cut = new CmdRemoveItem(xBar, loadout, component, aEngine);

    // Execute (do)
    cut.apply();

    // Verify (do)
    if (null != side) {
      final InOrder ioLeft = inOrder(lt, xBar);
      ioLeft.verify(lt).removeItem(side);
      ioLeft.verify(xBar).post(new ItemMessage(lt, Type.Removed, side, indexLt));

      final InOrder ioRight = inOrder(rt, xBar);
      ioRight.verify(rt).removeItem(side);
      ioRight.verify(xBar).post(new ItemMessage(rt, Type.Removed, side, indexRt));
    }

    final InOrder io = inOrder(component, xBar);
    for (int i = 0; i < aEngineHS; ++i) {
      io.verify(component, calls(1)).removeItem(hsType); // HS first
      io.verify(xBar, calls(1)).post(new ItemMessage(component, Type.Removed, hsType, -1));
    }
    io.verify(component).removeItem(aEngine);
    io.verify(xBar).post(new ItemMessage(component, Type.Removed, aEngine, index));

    // Execute (undo)
    cut.undo();

    // Verify (undo)
    if (null != side) {
      final InOrder ioLeft = inOrder(lt, xBar);
      ioLeft.verify(lt).addItem(side);
      ioLeft.verify(xBar).post(new ItemMessage(lt, Type.Added, side, indexLt));

      final InOrder ioRight = inOrder(rt, xBar);
      ioRight.verify(rt).addItem(side);
      ioRight.verify(xBar).post(new ItemMessage(rt, Type.Added, side, indexRt));
    }
    io.verify(component).addItem(aEngine);
    io.verify(xBar).post(new ItemMessage(component, Type.Added, aEngine, index));
    for (int i = 0; i < aEngineHS; ++i) {
      io.verify(component, calls(1)).addItem(hsType); // HS first
      io.verify(xBar, calls(1)).post(new ItemMessage(component, Type.Added, hsType, -1));
    }
  }
}
