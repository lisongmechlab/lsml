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

import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.mock;

import org.junit.Test;
import org.lisoft.lsml.messages.GarageMessage;
import org.lisoft.lsml.messages.GarageMessageType;
import org.lisoft.lsml.messages.MessageDelivery;
import org.lisoft.lsml.model.NamedObject;
import org.lisoft.lsml.model.garage.GarageDirectory;
import org.lisoft.lsml.model.garage.GarageException;
import org.mockito.InOrder;

public class CmdAddGarageDirectoryTest {
    private final MessageDelivery md = mock(MessageDelivery.class);

    @Test(expected = GarageException.class)
    public void testApplyUndo_AlreadyExists() throws Exception {
        final GarageDirectory<NamedObject> dir = new GarageDirectory<>("dir");
        final GarageDirectory<NamedObject> dirOld = new GarageDirectory<>("dir");
        final GarageDirectory<NamedObject> parent = new GarageDirectory<>("parent");
        parent.getDirectories().add(dirOld);
        dirOld.getValues().add(new NamedObject("Foo"));

        final CmdAddGarageDirectory<NamedObject> cut = new CmdAddGarageDirectory<>(md, dir, parent);
        cut.apply();
    }

    @Test
    public void testApplyUndo_Basic() throws Exception {
        final GarageDirectory<NamedObject> dir = new GarageDirectory<>("dir");
        final GarageDirectory<NamedObject> parent = new GarageDirectory<>("parent");

        final CmdAddGarageDirectory<NamedObject> cut = new CmdAddGarageDirectory<>(md, dir, parent);
        cut.apply();
        assertTrue(parent.getDirectories().contains(dir));

        final InOrder io = inOrder(md);
        io.verify(md).post(new GarageMessage<>(GarageMessageType.ADDED, parent, dir));

        cut.undo();
        assertTrue(parent.getDirectories().isEmpty());
        io.verify(md).post(new GarageMessage<>(GarageMessageType.REMOVED, parent, dir));
    }
}
