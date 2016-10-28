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

import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyZeroInteractions;
import static org.mockito.Mockito.when;

import java.util.List;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.lisoft.lsml.messages.GarageMessage;
import org.lisoft.lsml.messages.GarageMessageType;
import org.lisoft.lsml.messages.MessageDelivery;
import org.lisoft.lsml.model.garage.GarageDirectory;
import org.lisoft.lsml.model.garage.GarageException;
import org.lisoft.lsml.model.loadout.Loadout;
import org.mockito.InOrder;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

/**
 * Test suite for {@link CmdRemoveFromGarage}.
 *
 * @author Emily Björk
 */
@RunWith(MockitoJUnitRunner.Silent.class)
public class CmdRemoveFromGarageTest {
    @Mock
    private GarageDirectory<Loadout> dir;
    @Mock
    private List<Loadout> dirLoadouts;
    @Mock
    private Loadout loadout;
    @Mock
    private MessageDelivery delivery;
    private CmdRemoveFromGarage<Loadout> cut;

    @Before
    public void setup() {
        when(loadout.getName()).thenReturn("XYZ");
        when(dir.getValues()).thenReturn(dirLoadouts);
        cut = new CmdRemoveFromGarage<>(delivery, dir, loadout);
    }

    @Test
    public void testApplyExists() {
        when(dirLoadouts.contains(loadout)).thenReturn(false);

        try {
            cut.apply();
            fail("Expected exception!");
        }
        catch (final GarageException e) {
            assertTrue(e.getMessage().toLowerCase().contains("doesn't exist"));
        }

        verify(dirLoadouts, never()).add(loadout);
        verifyZeroInteractions(delivery);
    }

    @Test
    public void testApplyUndo() throws GarageException {
        when(dirLoadouts.contains(loadout)).thenReturn(true);

        cut.apply();

        when(dirLoadouts.contains(loadout)).thenReturn(false);
        cut.undo();

        final InOrder inOrder = inOrder(delivery, dirLoadouts);
        inOrder.verify(dirLoadouts).remove(loadout);
        inOrder.verify(delivery).post(new GarageMessage<>(GarageMessageType.REMOVED, dir, loadout));
        inOrder.verify(dirLoadouts).add(loadout);
        inOrder.verify(delivery).post(new GarageMessage<>(GarageMessageType.ADDED, dir, loadout));
    }

    @Test
    public void testDescribe() {
        final String description = cut.describe();
        assertTrue(description.contains("remove "));
        assertTrue(description.contains(loadout.toString()));
    }
}