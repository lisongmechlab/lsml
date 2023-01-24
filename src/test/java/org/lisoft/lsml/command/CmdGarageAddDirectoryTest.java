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
import org.lisoft.lsml.model.garage.GarageDirectory;
import org.lisoft.lsml.model.garage.GarageException;
import org.lisoft.lsml.model.garage.GaragePath;

import java.io.IOException;

import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;
import static org.lisoft.lsml.model.garage.GaragePath.fromPath;
import static org.mockito.Mockito.*;

public class CmdGarageAddDirectoryTest {
    private final MessageDelivery delivery = mock(MessageDelivery.class);
    private final TestGarageTree tgt = new TestGarageTree();

    @Test
    public void testApplyDestinationIsLeaf() throws Exception {
        try {
            final NamedObject newObject = new NamedObject("foo");
            final GarageDirectory<NamedObject> newDir = new GarageDirectory<>("Hai aim new!");
            new CmdGarageAddDirectory<>(delivery, new GaragePath<>(fromPath("/2/d/", tgt.root), newObject),
                                        newDir).apply();
            fail("Expected exception!");
        } catch (final GarageException e) {
            assertTrue(e.getMessage().toLowerCase().contains("not a directory"));
        }

        tgt.assertUnmodified();
        verifyNoInteractions(delivery);
    }

    @Test
    public void testApplyExists() throws Exception {
        try {
            new CmdGarageAddDirectory<>(delivery, fromPath("/", tgt.root), tgt.dir1).apply();
            fail("Expected exception!");
        } catch (final GarageException e) {
            assertTrue(e.getMessage().toLowerCase().contains("exists"));
        }

        tgt.assertUnmodified();
        verifyNoInteractions(delivery);
    }

    @Test
    public void testApplyExistsWithValue() throws Exception {
        try {
            new CmdGarageAddDirectory<>(delivery, fromPath("/", tgt.root), new GarageDirectory<>("x")).apply();
            fail("Expected exception!");
        } catch (final GarageException e) {
            assertTrue(e.getMessage().toLowerCase().contains("exists"));
        }

        tgt.assertUnmodified();
        verifyNoInteractions(delivery);
    }

    @Test
    public void testApplyNameExists() throws Exception {
        try {
            final GarageDirectory<NamedObject> new1 = new GarageDirectory<>(tgt.dir1.getName());
            new CmdGarageAddDirectory<>(delivery, fromPath("/", tgt.root), new1).apply();
            fail("Expected exception!");
        } catch (final GarageException e) {
            assertTrue(e.getMessage().toLowerCase().contains("exists"));
        }

        tgt.assertUnmodified();
        verifyNoInteractions(delivery);
    }

    @Test
    public void testApplyUndo() throws Exception {
        final GarageDirectory<NamedObject> newDir = new GarageDirectory<>("Hai aim new!");
        final GaragePath<NamedObject> dstDirPath = fromPath("/2/c/", tgt.root);
        final GaragePath<NamedObject> expectedPath = new GaragePath<>(dstDirPath, newDir);
        final CmdGarageAddDirectory<NamedObject> cut = new CmdGarageAddDirectory<>(delivery, dstDirPath, newDir);

        cut.apply();
        assertTrue(tgt.dirc.getDirectories().contains(newDir));
        verify(delivery).post(new GarageMessage<>(GarageMessageType.ADDED, expectedPath));

        reset(delivery);
        cut.undo();
        tgt.assertUnmodified();
        verify(delivery).post(new GarageMessage<>(GarageMessageType.REMOVED, expectedPath));
    }

    @Test
    public void testDescribe() throws IOException {
        final GarageDirectory<NamedObject> newDir = new GarageDirectory<>("Hai aim new!");
        final GaragePath<NamedObject> dstDirPath = fromPath("/2/c/", tgt.root);
        final CmdGarageAddDirectory<NamedObject> cut = new CmdGarageAddDirectory<>(delivery, dstDirPath, newDir);

        final String description = cut.describe();
        assertTrue(description.contains("make "));
        assertTrue(description.contains(newDir.toString()));
    }
}
