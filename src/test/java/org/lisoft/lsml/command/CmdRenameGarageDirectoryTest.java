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
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.List;

import org.junit.Test;
import org.lisoft.lsml.messages.GarageMessage;
import org.lisoft.lsml.messages.GarageMessageType;
import org.lisoft.lsml.messages.MessageDelivery;
import org.lisoft.lsml.model.NamedObject;
import org.lisoft.lsml.model.garage.GarageDirectory;
import org.lisoft.lsml.model.garage.GarageException;
import org.mockito.InOrder;

public class CmdRenameGarageDirectoryTest {

    private final MessageDelivery md = mock(MessageDelivery.class);

    @Test
    public void testApply_NameExists() throws Exception {
        final GarageDirectory<NamedObject> subdir1 = mock(GarageDirectory.class);
        final GarageDirectory<NamedObject> subdir2 = mock(GarageDirectory.class);
        when(subdir1.getName()).thenReturn("Name");
        when(subdir2.getName()).thenReturn("Other");

        final List<GarageDirectory<NamedObject>> dirs = new ArrayList<>();
        dirs.add(subdir1);
        dirs.add(subdir2);

        final GarageDirectory<NamedObject> dir = mock(GarageDirectory.class);
        when(dir.getDirectories()).thenReturn(dirs);

        try {
            final CmdRenameGarageDirectory<NamedObject> cut = new CmdRenameGarageDirectory<>(md, subdir1, "name", dir);
            cut.apply();
            fail("Expected exception");
        }
        catch (final GarageException e) {
            assertTrue(e.getMessage().toLowerCase().contains("already"));
        }
    }

    @Test
    public void testApplyUndo() throws Exception {
        final GarageDirectory<NamedObject> dir = mock(GarageDirectory.class);
        final String oldName = "foo";
        final String newName = "bar";
        when(dir.getName()).thenReturn(oldName);

        final CmdRenameGarageDirectory<NamedObject> cut = new CmdRenameGarageDirectory<>(md, dir, newName, null);
        cut.apply();

        final InOrder io = inOrder(md, dir);
        io.verify(dir).setName(newName);
        io.verify(md).post(new GarageMessage<>(GarageMessageType.RENAMED, null, dir));

        cut.undo();
        io.verify(dir).setName(oldName);
        io.verify(md).post(new GarageMessage<>(GarageMessageType.RENAMED, null, dir));
    }
}
