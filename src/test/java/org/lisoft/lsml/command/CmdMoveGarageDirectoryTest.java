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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.junit.Test;
import org.lisoft.lsml.messages.GarageMessage;
import org.lisoft.lsml.messages.GarageMessageType;
import org.lisoft.lsml.messages.MessageDelivery;
import org.lisoft.lsml.model.NamedObject;
import org.lisoft.lsml.model.garage.GarageDirectory;
import org.lisoft.lsml.util.CommandStack;
import org.mockito.InOrder;
import org.mockito.Mockito;

public class CmdMoveGarageDirectoryTest {
    private final MessageDelivery delivery = mock(MessageDelivery.class);

    @Test
    public void testSimple() throws Exception {
        final GarageDirectory<NamedObject> srcParent = mock(GarageDirectory.class);
        final GarageDirectory<NamedObject> dir = mock(GarageDirectory.class);
        final GarageDirectory<NamedObject> dstParent = mock(GarageDirectory.class);
        final List<GarageDirectory<NamedObject>> srcsChildren = Mockito.spy(new ArrayList<>(Arrays.asList(dir)));
        final List<GarageDirectory<NamedObject>> dstsChildren = Mockito.spy(new ArrayList<>());

        when(dstParent.getDirectories()).thenReturn(dstsChildren);
        when(srcParent.getDirectories()).thenReturn(srcsChildren);

        final CommandStack stack = new CommandStack(1);
        final CmdMoveGarageDirectory<NamedObject> cut = new CmdMoveGarageDirectory<>(delivery, dstParent, dir,
                srcParent);
        stack.pushAndApply(cut);

        final InOrder io = inOrder(srcsChildren, dstsChildren, delivery);
        io.verify(srcsChildren).remove(dir);
        io.verify(dstsChildren).add(dir);
        io.verify(delivery).post(new GarageMessage<>(GarageMessageType.REMOVED, srcParent, dir));
        io.verify(delivery).post(new GarageMessage<>(GarageMessageType.ADDED, dstParent, dir));

        stack.undo();
        io.verify(dstsChildren).remove(dir);
        io.verify(srcsChildren).add(dir);
        io.verify(delivery).post(new GarageMessage<>(GarageMessageType.REMOVED, dstParent, dir));
        io.verify(delivery).post(new GarageMessage<>(GarageMessageType.ADDED, srcParent, dir));
    }

}
