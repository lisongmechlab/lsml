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

import org.junit.Test;
import org.lisoft.lsml.TestGarageTree;
import org.lisoft.lsml.messages.GarageMessage;
import org.lisoft.lsml.messages.GarageMessageType;
import org.lisoft.lsml.messages.MessageDelivery;
import org.lisoft.lsml.model.NamedObject;
import org.lisoft.lsml.model.garage.GaragePath;

import java.io.IOException;
import java.util.Locale;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.lisoft.lsml.model.garage.GaragePath.fromPath;
import static org.mockito.Mockito.*;

public class CmdGarageMoveTest {
    private final MessageDelivery md = mock(MessageDelivery.class);
    private final TestGarageTree tgt = new TestGarageTree();

    @Test
    public void testDescribe() throws IOException {
        final CmdGarageMove<NamedObject> cut = new CmdGarageMove<>(md, fromPath("/2", tgt.root),
                                                                   fromPath("/x", tgt.root));
        final String desc = cut.describe().toLowerCase(Locale.ENGLISH);
        assertTrue(desc.contains("move"));
    }

    @SuppressWarnings("unlikely-arg-type")
    @Test
    public void testMoveDirectory() throws Exception {
        final GaragePath<NamedObject> dstPath = fromPath("/1", tgt.root);
        final GaragePath<NamedObject> srcPath = fromPath("/2/d", tgt.root);
        final CmdGarageMove<NamedObject> cut = new CmdGarageMove<>(md, dstPath, srcPath);

        cut.apply();
        verify(md).post(new GarageMessage<>(GarageMessageType.REMOVED, srcPath));
        verify(md).post(new GarageMessage<>(GarageMessageType.ADDED, new GaragePath<>(dstPath, tgt.dird)));
        assertTrue(tgt.dir1.getDirectories().contains(tgt.dird));
        assertFalse(tgt.dir2.getValues().contains(tgt.dird));

        reset(md);
        cut.undo();
        verify(md).post(new GarageMessage<>(GarageMessageType.REMOVED, new GaragePath<>(dstPath, tgt.dird)));
        verify(md).post(new GarageMessage<>(GarageMessageType.ADDED, srcPath));
        tgt.assertUnmodified();
    }

    @Test
    public void testMoveObject() throws Exception {
        final GaragePath<NamedObject> dstPath = fromPath("/1/a", tgt.root);
        final GaragePath<NamedObject> srcPath = fromPath("/2/d/z", tgt.root);
        final CmdGarageMove<NamedObject> cut = new CmdGarageMove<>(md, dstPath, srcPath);

        cut.apply();
        verify(md).post(new GarageMessage<>(GarageMessageType.REMOVED, srcPath));
        verify(md).post(new GarageMessage<>(GarageMessageType.ADDED, new GaragePath<>(dstPath, tgt.z)));
        assertTrue(tgt.dira.getValues().contains(tgt.z));
        assertFalse(tgt.dird.getValues().contains(tgt.z));

        reset(md);
        cut.undo();
        verify(md).post(new GarageMessage<>(GarageMessageType.REMOVED, new GaragePath<>(dstPath, tgt.z)));
        verify(md).post(new GarageMessage<>(GarageMessageType.ADDED, srcPath));
        tgt.assertUnmodified();
    }

    @Test
    public void testSameDirectoryMoveNoOp() throws Exception {
        final GaragePath<NamedObject> dstPath = fromPath("/", tgt.root);
        final GaragePath<NamedObject> srcPath = fromPath("/x", tgt.root);
        final CmdGarageMove<NamedObject> cut = new CmdGarageMove<>(md, dstPath, srcPath);

        cut.apply();
        tgt.assertUnmodified();

        cut.undo();
        tgt.assertUnmodified();
        verifyZeroInteractions(md);
    }

    @Test
    public void testSelfMoveDirectoryNoOp() throws Exception {
        final GaragePath<NamedObject> dstPath = fromPath("/2/d", tgt.root);
        final GaragePath<NamedObject> srcPath = fromPath("/2/d", tgt.root);
        final CmdGarageMove<NamedObject> cut = new CmdGarageMove<>(md, dstPath, srcPath);

        cut.apply();
        tgt.assertUnmodified();

        cut.undo();
        tgt.assertUnmodified();
        verifyZeroInteractions(md);
    }

}
