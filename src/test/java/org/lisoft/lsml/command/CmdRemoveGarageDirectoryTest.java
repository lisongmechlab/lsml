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

import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.List;

import org.junit.Test;
import org.lisoft.lsml.messages.GarageMessage;
import org.lisoft.lsml.messages.GarageMessageType;
import org.lisoft.lsml.messages.MessageDelivery;
import org.lisoft.lsml.model.NamedObject;
import org.lisoft.lsml.model.garage.GarageDirectory;
import org.lisoft.lsml.model.garage.GarageException;
import org.mockito.InOrder;

public class CmdRemoveGarageDirectoryTest {
    private final MessageDelivery md = mock(MessageDelivery.class);

    @Test
    public void testApplyUndo_Basic() throws Exception {
        final GarageDirectory<NamedObject> dir = mock(GarageDirectory.class);
        final GarageDirectory<NamedObject> parent = mock(GarageDirectory.class);
        final List<GarageDirectory<NamedObject>> parentsChildren = mock(List.class);

        when(parent.getDirectories()).thenReturn(parentsChildren);
        when(parentsChildren.contains(dir)).thenReturn(true);

        final CmdRemoveGarageDirectory<NamedObject> cut = new CmdRemoveGarageDirectory<>(md, dir, parent);
        cut.apply();

        final InOrder io = inOrder(parentsChildren, md);
        io.verify(parentsChildren).remove(dir);
        io.verify(md).post(new GarageMessage<>(GarageMessageType.REMOVED, parent, dir));

        cut.undo();
        io.verify(parentsChildren).add(dir);
        io.verify(md).post(new GarageMessage<>(GarageMessageType.ADDED, parent, dir));
    }

    @Test(expected = GarageException.class)
    public void testApplyUndo_NotAChild() throws Exception {
        final GarageDirectory<NamedObject> dir = mock(GarageDirectory.class);
        final GarageDirectory<NamedObject> parent = mock(GarageDirectory.class);
        final List<GarageDirectory<NamedObject>> parentsChildren = mock(List.class);

        when(parent.getDirectories()).thenReturn(parentsChildren);
        when(parentsChildren.contains(dir)).thenReturn(false);

        final CmdRemoveGarageDirectory<NamedObject> cut = new CmdRemoveGarageDirectory<>(md, dir, parent);
        cut.apply();
    }
}