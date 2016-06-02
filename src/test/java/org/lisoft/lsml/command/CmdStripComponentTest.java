/*
 * @formatter:off
 * Li Song Mechlab - A 'mech building tool for PGI's MechWarrior: Online.
 * Copyright (C) 2013  Emily Björk
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
//@formatter:on
package org.lisoft.lsml.command;

import static org.mockito.Matchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.reset;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.List;

import org.junit.Before;
import org.junit.Test;
import org.lisoft.lsml.messages.ArmourMessage;
import org.lisoft.lsml.messages.ArmourMessage.Type;
import org.lisoft.lsml.messages.ItemMessage;
import org.lisoft.lsml.messages.MessageDelivery;
import org.lisoft.lsml.model.chassi.ArmourSide;
import org.lisoft.lsml.model.datacache.ItemDB;
import org.lisoft.lsml.model.helpers.MockLoadoutContainer;
import org.lisoft.lsml.model.item.HeatSink;
import org.lisoft.lsml.model.item.Item;
import org.lisoft.lsml.util.CommandStack;
import org.mockito.Matchers;

public class CmdStripComponentTest {
    private final MockLoadoutContainer mlc = new MockLoadoutContainer();
    private final List<Item> items = new ArrayList<>();
    private final MessageDelivery messages = mock(MessageDelivery.class);
    private final int frontArmour = 20;
    private final int backArmour = 10;
    private final int onlyArmour = 10;

    @Before
    public void setup() {
        when(mlc.loadout.getFreeMass()).thenReturn(100.0);
        when(mlc.armourUpgrade.getArmourMass(Matchers.anyInt())).thenReturn(0.0);

        when(mlc.la.getArmour(ArmourSide.ONLY)).thenReturn(onlyArmour);
        when(mlc.rt.getArmour(ArmourSide.FRONT)).thenReturn(frontArmour);
        when(mlc.rt.getArmour(ArmourSide.BACK)).thenReturn(backArmour);
        when(mlc.rt.getItemsEquipped()).thenReturn(items);
        when(mlc.rt.canRemoveItem(ItemDB.ECM)).thenReturn(true);
    }

    @Test
    public void testStripComponent_ArmourResetBothSides() throws Exception {
        final CmdStripComponent cut = new CmdStripComponent(messages, mlc.loadout, mlc.rt);
        final CommandStack os = new CommandStack(2);
        os.pushAndApply(cut);
        final boolean manualSet = false;
        when(mlc.rt.hasManualArmour()).thenReturn(manualSet);

        // Test apply
        verify(mlc.rt).setArmour(ArmourSide.FRONT, 0, false);
        verify(mlc.rt).setArmour(ArmourSide.BACK, 0, false);
        verify(messages, times(2)).post(new ArmourMessage(mlc.rt, Type.ARMOUR_CHANGED, false));

        // Test undo
        reset(mlc.rt);
        reset(messages);
        os.undo();
        verify(mlc.rt).setArmour(ArmourSide.BACK, backArmour, manualSet);
        verify(mlc.rt).setArmour(ArmourSide.FRONT, frontArmour, manualSet);
        verify(messages, times(2)).post(new ArmourMessage(mlc.rt, Type.ARMOUR_CHANGED, manualSet));
    }

    @Test
    public void testStripComponent_ArmourResetOnlySide() throws Exception {
        final CmdStripComponent cut = new CmdStripComponent(messages, mlc.loadout, mlc.la);
        final CommandStack os = new CommandStack(2);
        os.pushAndApply(cut);
        final boolean manualArmour = false;
        when(mlc.rt.hasManualArmour()).thenReturn(manualArmour);

        // Test apply
        verify(mlc.la).setArmour(ArmourSide.ONLY, 0, false);
        verify(messages, times(1)).post(new ArmourMessage(mlc.la, Type.ARMOUR_CHANGED, false));

        // Test undo
        reset(mlc.rt);
        reset(messages);
        os.undo();
        verify(mlc.la).setArmour(ArmourSide.ONLY, onlyArmour, manualArmour);
        verify(messages, times(1)).post(new ArmourMessage(mlc.la, Type.ARMOUR_CHANGED, manualArmour));
    }

    @Test
    public void testStripComponent_EngineHs() throws Exception {
        final Item engine = ItemDB.lookup("STD ENGINE 325");
        final HeatSink hs = ItemDB.SHS;
        when(mlc.heatSinkUpgrade.getHeatSinkType()).thenReturn(hs);
        when(mlc.ct.getEngineHeatSinks()).thenReturn(3);
        when(mlc.ct.getItemsEquipped()).thenReturn(items);
        when(mlc.ct.canRemoveItem(hs)).thenReturn(true);
        when(mlc.ct.canRemoveItem(engine)).thenReturn(true);
        items.add(hs);
        items.add(hs);
        items.add(hs);
        items.add(hs);
        items.add(engine);

        final CmdStripComponent cut = new CmdStripComponent(messages, mlc.loadout, mlc.ct);
        final CommandStack os = new CommandStack(2);

        // Test apply
        os.pushAndApply(cut);
        verify(mlc.ct, times(1)).removeItem(engine);
        verify(mlc.ct, times(4)).removeItem(hs);
        verify(messages, times(5)).post(any(ItemMessage.class));

        // Test undo
        reset(mlc.rt);
        reset(messages);
        os.undo();
        verify(mlc.ct, times(1)).addItem(engine);
        verify(mlc.ct, times(4)).addItem(hs);
        verify(messages, times(5)).post(any(ItemMessage.class));
    }

    @Test
    public void testStripComponent_ItemsRemoved() throws Exception {
        items.add(ItemDB.ECM);

        final CmdStripComponent cut = new CmdStripComponent(messages, mlc.loadout, mlc.rt);
        final CommandStack os = new CommandStack(2);
        os.pushAndApply(cut);

        verify(mlc.rt).removeItem(ItemDB.ECM);
        verify(mlc.rt).setArmour(ArmourSide.FRONT, 0, false);
        verify(mlc.rt).setArmour(ArmourSide.BACK, 0, false);
        verify(messages, times(1)).post(new ItemMessage(mlc.rt, ItemMessage.Type.Removed, ItemDB.ECM, 0));
        verify(messages, times(2)).post(new ArmourMessage(mlc.rt, Type.ARMOUR_CHANGED, false));
    }

    @Test
    public void testStripComponent_LeaveArmour() throws Exception {
        items.add(ItemDB.ECM);

        final CmdStripComponent cut = new CmdStripComponent(messages, mlc.loadout, mlc.rt, false);
        final CommandStack os = new CommandStack(2);
        os.pushAndApply(cut);

        verify(mlc.rt).removeItem(ItemDB.ECM);
        verify(messages, times(1)).post(new ItemMessage(mlc.rt, ItemMessage.Type.Removed, ItemDB.ECM, 0));
        verify(messages, never()).post(new ArmourMessage(mlc.rt, Type.ARMOUR_CHANGED, false));
    }

    @Test
    public void testStripComponent_NoInternals() throws Exception {
        final Item ha = ItemDB.HA;
        when(mlc.ct.getItemsEquipped()).thenReturn(items);
        items.add(ha);

        final CmdStripComponent cut = new CmdStripComponent(messages, mlc.loadout, mlc.ct);
        final CommandStack os = new CommandStack(2);

        // Test apply
        os.pushAndApply(cut);
        verify(mlc.ct, never()).removeItem(any(Item.class));
        verify(messages, never()).post(any(ItemMessage.class));

        // Test undo
        reset(mlc.rt);
        reset(messages);
        os.undo();
        verify(mlc.ct, never()).addItem(any(Item.class));
        verify(messages, never()).post(any(ItemMessage.class));
    }

    @Test
    public void testStripComponent_NoMessages() throws Exception {
        items.add(ItemDB.ECM);

        final CmdStripComponent cut = new CmdStripComponent(null, mlc.loadout, mlc.rt);
        final CommandStack os = new CommandStack(2);
        os.pushAndApply(cut);

        verify(mlc.rt).removeItem(ItemDB.ECM);
        verify(mlc.rt).setArmour(ArmourSide.FRONT, 0, false);
        verify(mlc.rt).setArmour(ArmourSide.BACK, 0, false);
    }
}
