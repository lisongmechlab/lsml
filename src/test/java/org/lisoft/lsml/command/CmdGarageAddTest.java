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
import static org.lisoft.lsml.model.garage.GaragePath.fromPath;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.reset;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyZeroInteractions;

import java.io.IOException;

import org.junit.Test;
import org.lisoft.lsml.TestGarageTree;
import org.lisoft.lsml.messages.GarageMessage;
import org.lisoft.lsml.messages.GarageMessageType;
import org.lisoft.lsml.messages.MessageDelivery;
import org.lisoft.lsml.model.NamedObject;
import org.lisoft.lsml.model.garage.GarageException;
import org.lisoft.lsml.model.garage.GaragePath;

/**
 * Test suite for {@link CmdGarageAdd}.
 *
 * @author Emily Björk
 */
public class CmdGarageAddTest {
    private final TestGarageTree tgt = new TestGarageTree();
    private final MessageDelivery delivery = mock(MessageDelivery.class);

    @Test
    public void testApplyDestinationIsLeaf() throws IOException {
        try {
            final NamedObject newObject = new NamedObject("foo");
            new CmdGarageAdd<>(delivery, new GaragePath<>(fromPath("/2/d/", tgt.root), newObject), newObject).apply();
            fail("Expected exception!");
        }
        catch (final GarageException e) {
            assertTrue(e.getMessage().toLowerCase().contains("not a directory"));
        }

        tgt.assertUnmodified();
        verifyZeroInteractions(delivery);
    }

    @Test
    public void testApplyExists() throws IOException {
        try {
            new CmdGarageAdd<>(delivery, fromPath("/", tgt.root), tgt.x).apply();
            fail("Expected exception!");
        }
        catch (final GarageException e) {
            assertTrue(e.getMessage().toLowerCase().contains("exists"));
        }

        tgt.assertUnmodified();
        verifyZeroInteractions(delivery);
    }

    @Test
    public void testApplyNameExists() throws IOException {
        try {
            final NamedObject newZ = new NamedObject(tgt.z.getName());
            new CmdGarageAdd<>(delivery, fromPath("/2/d", tgt.root), newZ).apply();
            fail("Expected exception!");
        }
        catch (final GarageException e) {
            assertTrue(e.getMessage().toLowerCase().contains("exists"));
        }

        tgt.assertUnmodified();
        verifyZeroInteractions(delivery);
    }

    @Test
    public void testApplyNameExistsDir() throws IOException {
        try {
            final NamedObject newC = new NamedObject(tgt.dirc.getName());
            new CmdGarageAdd<>(delivery, fromPath("/2/", tgt.root), newC).apply();
            fail("Expected exception!");
        }
        catch (final GarageException e) {
            assertTrue(e.getMessage().toLowerCase().contains("exists"));
        }

        tgt.assertUnmodified();
        verifyZeroInteractions(delivery);
    }

    @Test
    public void testApplyUndo() throws GarageException, IOException {
        final NamedObject newObject = new NamedObject("Hai aim new!");
        final GaragePath<NamedObject> dstDirPath = fromPath("/2/c/", tgt.root);
        final GaragePath<NamedObject> expectedPath = new GaragePath<>(dstDirPath, newObject);
        final CmdGarageAdd<NamedObject> cut = new CmdGarageAdd<>(delivery, dstDirPath, newObject);

        cut.apply();
        assertTrue(tgt.dirc.getValues().contains(newObject));
        verify(delivery).post(new GarageMessage<>(GarageMessageType.ADDED, expectedPath));

        reset(delivery);
        cut.undo();
        tgt.assertUnmodified();
        verify(delivery).post(new GarageMessage<>(GarageMessageType.REMOVED, expectedPath));
    }

    @Test
    public void testDescribe() throws IOException {
        final NamedObject newObject = new NamedObject("Hai aim new!");
        final GaragePath<NamedObject> dstDirPath = fromPath("/2/c/", tgt.root);
        final CmdGarageAdd<NamedObject> cut = new CmdGarageAdd<>(delivery, dstDirPath, newObject);

        final String description = cut.describe();
        assertTrue(description.contains("add "));
        assertTrue(description.contains(newObject.toString()));
    }
}
