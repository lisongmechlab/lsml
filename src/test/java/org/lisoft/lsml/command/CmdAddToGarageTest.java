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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.verifyZeroInteractions;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

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
import org.mockito.Mockito;
import org.mockito.runners.MockitoJUnitRunner;

/**
 * Test suite for {@link CmdAddToGarage}.
 * 
 * @author Emily Björk
 */
@RunWith(MockitoJUnitRunner.class)
public class CmdAddToGarageTest {
    @Mock
    private GarageDirectory<Loadout> dir;
    private List<Loadout>            dirLoadouts = new ArrayList<>();
    @Mock
    private Loadout                  loadout;
    @Mock
    private Loadout                  loadout2;
    @Mock
    private MessageDelivery          delivery;
    private CmdAddToGarage<Loadout>  cut;

    @Before
    public void setup() {
        when(loadout.getName()).thenReturn("XYZ");
        when(dir.getValues()).thenReturn(dirLoadouts);
        cut = new CmdAddToGarage<>(delivery, dir, loadout);
    }

    @Test
    public void testDescribe() {

        String description = cut.describe();
        assertTrue(description.contains("add "));
        assertTrue(description.contains(loadout.toString()));
    }

    @Test
    public void testApplyUndo() throws GarageException {
        cut.apply();
        assertTrue(dirLoadouts.contains(loadout));

        cut.undo();
        assertTrue(dirLoadouts.isEmpty());

        InOrder inOrder = inOrder(delivery);
        inOrder.verify(delivery)
                .post(new GarageMessage(GarageMessageType.ADDED, Optional.of(dir), Optional.of(loadout)));
        inOrder.verify(delivery)
                .post(new GarageMessage(GarageMessageType.REMOVED, Optional.of(dir), Optional.of(loadout)));
    }

    @Test
    public void testApplyExists() {
        setup();
        dirLoadouts.add(loadout);

        try {
            cut.apply();
            fail("Expected exception!");
        }
        catch (GarageException e) {
            e.getMessage().toLowerCase().contains("exists");
        }

        assertEquals(1, dirLoadouts.size());
        verifyZeroInteractions(delivery);
    }

    @Test
    public void testApplyNameExists() {
        when(loadout.toString()).thenReturn("name");
        when(loadout2.toString()).thenReturn("name");

        List<Loadout> loadouts = new ArrayList<>();
        loadouts.add(loadout2);
        dir = Mockito.mock(GarageDirectory.class);
        when(dir.getValues()).thenReturn(loadouts);
        cut = new CmdAddToGarage<>(delivery, dir, loadout);

        try {
            cut.apply();
            fail("Expected exception!");
        }
        catch (GarageException e) {
            e.getMessage().toLowerCase().contains("exists");
        }
        verifyZeroInteractions(delivery);
    }
}
