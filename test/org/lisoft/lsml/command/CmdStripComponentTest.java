/*
 * @formatter:off
 * Li Song Mechlab - A 'mech building tool for PGI's MechWarrior: Online.
 * Copyright (C) 2013  Li Song
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

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.reset;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.List;

import org.junit.Before;
import org.junit.Test;
import org.lisoft.lsml.model.chassi.ArmorSide;
import org.lisoft.lsml.model.helpers.MockLoadoutContainer;
import org.lisoft.lsml.model.item.Item;
import org.lisoft.lsml.model.item.ItemDB;
import org.lisoft.lsml.model.loadout.component.ComponentMessage;
import org.lisoft.lsml.model.loadout.component.ComponentMessage.Type;
import org.lisoft.lsml.util.CommandStack;
import org.lisoft.lsml.util.message.MessageDelivery;
import org.mockito.Matchers;

public class CmdStripComponentTest {
    private MockLoadoutContainer mlc        = new MockLoadoutContainer();
    private List<Item>           items      = new ArrayList<>();
    private MessageDelivery      messages   = mock(MessageDelivery.class);
    private final int            frontArmor = 20;
    private final int            backArmor  = 10;
    private final int            onlyArmor  = 10;

    @Before
    public void setup() {
        when(mlc.loadout.getFreeMass()).thenReturn(100.0);
        when(mlc.armorUpgrade.getArmorMass(Matchers.anyInt())).thenReturn(0.0);

        when(mlc.la.getArmor(ArmorSide.ONLY)).thenReturn(onlyArmor);
        when(mlc.rt.getArmor(ArmorSide.FRONT)).thenReturn(frontArmor);
        when(mlc.rt.getArmor(ArmorSide.BACK)).thenReturn(backArmor);
        when(mlc.rt.getItemsEquipped()).thenReturn(items);
        when(mlc.rt.canRemoveItem(ItemDB.ECM)).thenReturn(true);
    }

    @Test
    public void testStripComponent_ArmorResetBothSides() {
        CmdStripComponent cut = new CmdStripComponent(messages, mlc.loadout, mlc.rt);
        CommandStack os = new CommandStack(2);
        os.pushAndApply(cut);
        boolean manualSet = false;
        when(mlc.rt.hasManualArmor()).thenReturn(manualSet);

        // Test apply
        verify(mlc.rt).setArmor(ArmorSide.FRONT, 0, false);
        verify(mlc.rt).setArmor(ArmorSide.BACK, 0, false);
        verify(messages, times(2)).post(new ComponentMessage(mlc.rt, Type.ArmorChanged, false));

        // Test undo
        reset(mlc.rt);
        reset(messages);
        os.undo();
        verify(mlc.rt).setArmor(ArmorSide.BACK, backArmor, manualSet);
        verify(mlc.rt).setArmor(ArmorSide.FRONT, frontArmor, manualSet);
        verify(messages, times(2)).post(new ComponentMessage(mlc.rt, Type.ArmorChanged, manualSet));
    }

    @Test
    public void testStripComponent_ArmorResetOnlySide() {
        CmdStripComponent cut = new CmdStripComponent(messages, mlc.loadout, mlc.la);
        CommandStack os = new CommandStack(2);
        os.pushAndApply(cut);
        boolean manualArmor = false;
        when(mlc.rt.hasManualArmor()).thenReturn(manualArmor);

        // Test apply
        verify(mlc.la).setArmor(ArmorSide.ONLY, 0, false);
        verify(messages, times(1)).post(new ComponentMessage(mlc.la, Type.ArmorChanged, false));

        // Test undo
        reset(mlc.rt);
        reset(messages);
        os.undo();
        verify(mlc.la).setArmor(ArmorSide.ONLY, onlyArmor, manualArmor);
        verify(messages, times(1)).post(new ComponentMessage(mlc.la, Type.ArmorChanged, manualArmor));
    }

    @Test
    public void testStripComponent_ItemsRemoved() {
        items.add(ItemDB.ECM);

        CmdStripComponent cut = new CmdStripComponent(messages, mlc.loadout, mlc.rt);
        CommandStack os = new CommandStack(2);
        os.pushAndApply(cut);

        verify(mlc.rt).removeItem(ItemDB.ECM);
        verify(mlc.rt).setArmor(ArmorSide.FRONT, 0, false);
        verify(mlc.rt).setArmor(ArmorSide.BACK, 0, false);
        verify(messages, times(1)).post(new ComponentMessage(mlc.rt, Type.ItemRemoved));
        verify(messages, times(2)).post(new ComponentMessage(mlc.rt, Type.ArmorChanged, false));
    }
}
