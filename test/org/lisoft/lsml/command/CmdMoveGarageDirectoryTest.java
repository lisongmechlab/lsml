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

import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.List;

import org.junit.Test;
import org.lisoft.lsml.messages.GarageMessage;
import org.lisoft.lsml.messages.GarageMessageType;
import org.lisoft.lsml.messages.MessageDelivery;
import org.lisoft.lsml.model.garage.GarageDirectory;
import org.mockito.InOrder;

public class CmdMoveGarageDirectoryTest {
    private MessageDelivery delivery = mock(MessageDelivery.class);

    @Test
    public void testSimple() throws Exception {
        GarageDirectory<Integer> root = mock(GarageDirectory.class);
        GarageDirectory<Integer> src = mock(GarageDirectory.class);
        GarageDirectory<Integer> dst = mock(GarageDirectory.class);
        List<GarageDirectory<Integer>> rootsChildren = mock(List.class);
        List<GarageDirectory<Integer>> dstsChildren = mock(List.class);

        when(dst.getDirectories()).thenReturn(dstsChildren);
        when(root.getDirectories()).thenReturn(rootsChildren);
        when(rootsChildren.contains(src)).thenReturn(true);

        CmdMoveGarageDirectory<Integer> cut = new CmdMoveGarageDirectory<>(delivery, dst, src, root);
        cut.apply();

        InOrder io = inOrder(rootsChildren, dstsChildren, delivery);
        io.verify(rootsChildren).remove(src);
        io.verify(dstsChildren).add(src);
        io.verify(delivery).post(new GarageMessage(GarageMessageType.MOVED));

        cut.undo();
        io.verify(dstsChildren).remove(src);
        io.verify(rootsChildren).add(src);
        io.verify(delivery).post(new GarageMessage(GarageMessageType.MOVED));
    }

}
