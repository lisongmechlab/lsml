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

import static org.junit.Assert.*;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyZeroInteractions;
import static org.mockito.Mockito.when;

import org.junit.Test;
import org.lisoft.lsml.messages.DropShipMessage;
import org.lisoft.lsml.messages.MessageDelivery;
import org.lisoft.lsml.model.garage.DropShip;
import org.lisoft.lsml.model.garage.GarageException;
import org.lisoft.lsml.model.loadout.LoadoutBase;
import org.lisoft.lsml.model.loadout.component.ConfiguredComponentBase;
import org.mockito.InOrder;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;

public class CmdDropShipSetLoadoutTest {
    private final DropShip        ds          = mock(DropShip.class);
    private final MessageDelivery msgDelivery = mock(MessageDelivery.class);
    private final LoadoutBase<?>  loadout     = mock(LoadoutBase.class);

    @Test
    public void testDescribe() {
        int index = 2;
        CmdDropShipSetLoadout cut = new CmdDropShipSetLoadout(msgDelivery, ds, index, loadout);

        String ans = cut.describe();
        assertTrue(ans.contains("drop ship"));
        assertTrue(ans.contains("remove"));
        assertTrue(ans.contains("mech"));
    }

    @Test
    public void testSetLoadout() throws GarageException {
        int index = 2;
        CmdDropShipSetLoadout cut = new CmdDropShipSetLoadout(msgDelivery, ds, index, loadout);
        verifyZeroInteractions(msgDelivery);

        cut.apply();

        InOrder sequence = inOrder(ds, msgDelivery);
        sequence.verify(ds).setMech(index, loadout);
        sequence.verify(msgDelivery).post(new DropShipMessage());
    }

    @Test
    public void testSetLoadout_NoMessage() throws GarageException {
        int index = 2;
        CmdDropShipSetLoadout cut = new CmdDropShipSetLoadout(null, ds, index, loadout);

        cut.apply();

        verify(ds).setMech(index, loadout);
    }

    @Test
    public void testUndoSetLoadout() throws GarageException {
        int index = 2;
        final LoadoutBase<ConfiguredComponentBase> loadout1 = mock(LoadoutBase.class);
        CmdDropShipSetLoadout cut = new CmdDropShipSetLoadout(msgDelivery, ds, index, loadout);

        // when(x).thenReturn() has some issues with type erasure in generics, use answer
        // API instead to get around this.
        when(ds.getMech(index)).thenAnswer(new Answer<LoadoutBase<?>>() {
            @Override
            public LoadoutBase<?> answer(InvocationOnMock aInvocation) throws Throwable {
                return loadout1;
            }
        });

        verifyZeroInteractions(msgDelivery);

        cut.apply();
        cut.undo();

        InOrder sequence = inOrder(ds, msgDelivery);
        sequence.verify(ds).setMech(index, loadout);
        sequence.verify(msgDelivery).post(new DropShipMessage());
        sequence.verify(ds).setMech(index, loadout1);
        sequence.verify(msgDelivery).post(new DropShipMessage());
    }

    @Test
    public void testUndoSetLoadout_NoMesage() throws GarageException {
        int index = 2;
        final LoadoutBase<ConfiguredComponentBase> loadout1 = mock(LoadoutBase.class);
        CmdDropShipSetLoadout cut = new CmdDropShipSetLoadout(null, ds, index, loadout);

        // when(x).thenReturn() has some issues with type erasure in generics, use answer
        // API instead to get around this.
        when(ds.getMech(index)).thenAnswer(new Answer<LoadoutBase<?>>() {
            @Override
            public LoadoutBase<?> answer(InvocationOnMock aInvocation) throws Throwable {
                return loadout1;
            }
        });

        cut.apply();
        cut.undo();

        InOrder sequence = inOrder(ds, msgDelivery);
        sequence.verify(ds).setMech(index, loadout);
        sequence.verify(ds).setMech(index, loadout1);
    }

    @Test(expected = RuntimeException.class)
    public void testUndoSetLoadout_Fails() {
        CmdDropShipSetLoadout cut = null;
        try {
            int index = 2;
            cut = new CmdDropShipSetLoadout(null, ds, index, loadout);

            when(ds.getMech(index)).thenReturn(null);
            doThrow(new GarageException("")).when(ds).setMech(index, null);

            cut.apply();
        }
        catch (Throwable t) {
            fail("Setup threw");
            return;
        }
        cut.undo();
    }
}
