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
import static org.lisoft.lsml.model.garage.GaragePath.fromPath;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.reset;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyZeroInteractions;

import java.io.IOException;
import java.util.Locale;

import org.junit.Test;
import org.lisoft.lsml.TestGarageTree;
import org.lisoft.lsml.messages.GarageMessage;
import org.lisoft.lsml.messages.GarageMessageType;
import org.lisoft.lsml.messages.MessageDelivery;
import org.lisoft.lsml.model.NamedObject;
import org.lisoft.lsml.model.garage.GarageException;
import org.lisoft.lsml.model.garage.GaragePath;

/**
 * Test suite for {@link CmdGarageRename}.
 *
 * @author Emily Björk
 */
public class CmdGarageRenameTest {
    private static final String ANY_NAME = "foobar";
    private final TestGarageTree tgt = new TestGarageTree();
    private final MessageDelivery md = mock(MessageDelivery.class);

    /**
     * We can't rename to a name that already exists
     */
    @Test
    public void testApply_DirectoryWithThatNameAlreadyExists() throws IOException {
        final GaragePath<NamedObject> oldPath = fromPath("/2/c", tgt.root);
        final CmdGarageRename<NamedObject> cut = new CmdGarageRename<>(md, oldPath, tgt.dird.getName());

        try {
            cut.apply();
            fail("Expected exception!");
        }
        catch (final GarageException e) {
            final String msg = e.getMessage().toLowerCase(Locale.ENGLISH);
            assertTrue(msg.contains("already exists"));
            assertTrue(msg.contains(tgt.dird.getName()));
        }
        verifyZeroInteractions(md);
        tgt.assertUnmodified();
    }

    /**
     * We can't rename to a name that already exists
     */
    @Test
    public void testApply_DirectoryWithThatNameAlreadyExistsForValue() throws IOException {
        final GaragePath<NamedObject> oldPath = fromPath("/1", tgt.root);
        final CmdGarageRename<NamedObject> cut = new CmdGarageRename<>(md, oldPath, tgt.x.getName());

        try {
            cut.apply();
            fail("Expected exception!");
        }
        catch (final GarageException e) {
            final String msg = e.getMessage().toLowerCase(Locale.ENGLISH);
            assertTrue(msg.contains("already exists"));
            assertTrue(msg.contains(tgt.x.getName()));
        }
        verifyZeroInteractions(md);
        tgt.assertUnmodified();
    }

    /**
     * We can't rename to a name that already exists
     */
    @Test
    public void testApply_ValueWithThatNameAlreadyExists() throws IOException {
        final GaragePath<NamedObject> oldPath = fromPath("/x", tgt.root);
        final CmdGarageRename<NamedObject> cut = new CmdGarageRename<>(md, oldPath, tgt.w.getName());

        try {
            cut.apply();
            fail("Expected exception!");
        }
        catch (final GarageException e) {
            final String msg = e.getMessage().toLowerCase(Locale.ENGLISH);
            assertTrue(msg.contains("already exists"));
            assertTrue(msg.contains(tgt.w.getName()));
        }
        verifyZeroInteractions(md);
        tgt.assertUnmodified();
    }

    /**
     * We can't rename to a name that already exists
     */
    @Test
    public void testApply_ValueWithThatNameAlreadyExistsForDirectory() throws IOException {
        final GaragePath<NamedObject> oldPath = fromPath("/x", tgt.root);
        final CmdGarageRename<NamedObject> cut = new CmdGarageRename<>(md, oldPath, tgt.dir1.getName());

        try {
            cut.apply();
            fail("Expected exception!");
        }
        catch (final GarageException e) {
            final String msg = e.getMessage().toLowerCase(Locale.ENGLISH);
            assertTrue(msg.contains("already exists"));
            assertTrue(msg.contains(tgt.dir1.getName()));
        }
        verifyZeroInteractions(md);
        tgt.assertUnmodified();
    }

    /**
     * We can rename directories
     */
    @Test
    public void testApplyUndo_Directory() throws GarageException, IOException {
        final GaragePath<NamedObject> oldPath = fromPath("/2/d/", tgt.root);
        final CmdGarageRename<NamedObject> cut = new CmdGarageRename<>(md, oldPath, ANY_NAME);

        cut.apply();
        verify(md).post(new GarageMessage<>(GarageMessageType.RENAMED, oldPath));
        assertEquals(ANY_NAME, tgt.dird.getName());
        assertTrue(oldPath.toPath().endsWith(ANY_NAME));

        reset(md);
        cut.undo();
        verify(md).post(new GarageMessage<>(GarageMessageType.RENAMED, oldPath));
        tgt.assertUnmodified();
    }

    /**
     * We can rename values
     */
    @Test
    public void testApplyUndo_Object() throws GarageException, IOException {
        final GaragePath<NamedObject> oldPath = fromPath("/2/d/z", tgt.root);
        final CmdGarageRename<NamedObject> cut = new CmdGarageRename<>(md, oldPath, ANY_NAME);

        cut.apply();
        verify(md).post(new GarageMessage<>(GarageMessageType.RENAMED, oldPath));
        assertEquals(ANY_NAME, tgt.z.getName());
        assertTrue(oldPath.toPath().endsWith(ANY_NAME));

        reset(md);
        cut.undo();
        verify(md).post(new GarageMessage<>(GarageMessageType.RENAMED, oldPath));
        tgt.assertUnmodified();
    }

    /**
     * We can rename root
     */
    @Test
    public void testApplyUndo_Root() throws GarageException, IOException {
        final GaragePath<NamedObject> oldPath = fromPath("/", tgt.root);
        final CmdGarageRename<NamedObject> cut = new CmdGarageRename<>(md, oldPath, ANY_NAME);

        cut.apply();
        verify(md).post(new GarageMessage<>(GarageMessageType.RENAMED, oldPath));
        assertEquals(ANY_NAME, tgt.root.getName());

        reset(md);
        cut.undo();
        verify(md).post(new GarageMessage<>(GarageMessageType.RENAMED, oldPath));
        tgt.assertUnmodified();
    }

    /**
     * Description is useful
     */
    @Test
    public void testDescribe() throws IOException {
        final GaragePath<NamedObject> oldPath = fromPath("/", tgt.root);
        final CmdGarageRename<NamedObject> cut = new CmdGarageRename<>(md, oldPath, ANY_NAME);
        final String desc = cut.describe().toLowerCase(Locale.ENGLISH);
        assertTrue(desc.contains("rename"));
    }
}
