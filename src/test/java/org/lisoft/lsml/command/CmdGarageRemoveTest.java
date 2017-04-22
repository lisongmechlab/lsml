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
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.reset;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.verifyZeroInteractions;
import static org.mockito.Mockito.when;

import java.io.IOException;
import java.util.List;
import java.util.Locale;
import java.util.Optional;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.lisoft.lsml.TestGarageTree;
import org.lisoft.lsml.messages.GarageMessage;
import org.lisoft.lsml.messages.GarageMessageType;
import org.lisoft.lsml.messages.MessageDelivery;
import org.lisoft.lsml.model.NamedObject;
import org.lisoft.lsml.model.garage.GarageDirectory;
import org.lisoft.lsml.model.garage.GarageException;
import org.lisoft.lsml.model.garage.GaragePath;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

/**
 * Test suite for {@link CmdGarageRemove}.
 *
 * @author Emily Björk
 */
@SuppressWarnings("unchecked")
@RunWith(MockitoJUnitRunner.Silent.class)
public class CmdGarageRemoveTest {
    TestGarageTree tgt;
    @Mock
    private MessageDelivery delivery;

    @Before
    public void setup() {
        tgt = new TestGarageTree();
    }

    @Test
    public void testApply_DirectoryDoesntExists() {
        final GaragePath<NamedObject> parentPath = mock(GaragePath.class);
        final GarageDirectory<NamedObject> parentDir = mock(GarageDirectory.class);
        final List<GarageDirectory<NamedObject>> parentsChildren = mock(List.class);
        final GarageDirectory<NamedObject> topDir = mock(GarageDirectory.class);
        final GaragePath<NamedObject> path = mock(GaragePath.class);

        when(path.isLeaf()).thenReturn(false);
        when(path.getTopDirectory()).thenReturn(topDir);
        when(path.getParent()).thenReturn(parentPath);

        when(parentPath.getTopDirectory()).thenReturn(parentDir);
        when(parentDir.getDirectories()).thenReturn(parentsChildren);
        when(parentsChildren.remove(topDir)).thenReturn(false);

        try {
            new CmdGarageRemove<>(delivery, path).apply();
            fail("Expected exception!");
        }
        catch (final GarageException e) {
            assertTrue(e.getMessage().toLowerCase().contains("doesn't exist"));
        }

        verify(parentsChildren).remove(topDir);
        verifyNoMoreInteractions(parentsChildren);
        verifyZeroInteractions(delivery);
        tgt.assertUnmodified();
    }

    @Test
    public void testApply_Root() throws IOException {
        try {
            new CmdGarageRemove<>(delivery, GaragePath.fromPath("/", tgt.root)).apply();
            fail("Expectedd GarageException");
        }
        catch (final GarageException e) {
            final String lowerCase = e.getMessage().toLowerCase(Locale.ENGLISH);
            assertTrue(lowerCase.contains("remove"));
            assertTrue(lowerCase.contains("root"));
        }
        tgt.assertUnmodified();
    }

    @Test
    public void testApply_ValueDoesntExists() {
        final GarageDirectory<NamedObject> topDir = mock(GarageDirectory.class);
        final GaragePath<NamedObject> path = mock(GaragePath.class);
        final NamedObject value = mock(NamedObject.class);
        final List<NamedObject> values = mock(List.class);
        when(path.isLeaf()).thenReturn(true);
        when(path.getValue()).thenReturn(Optional.of(value));
        when(path.getTopDirectory()).thenReturn(topDir);
        when(topDir.getValues()).thenReturn(values);
        when(values.remove(value)).thenReturn(false);

        try {
            new CmdGarageRemove<>(delivery, path).apply();
            fail("Expected exception!");
        }
        catch (final GarageException e) {
            assertTrue(e.getMessage().toLowerCase().contains("doesn't exist"));
        }

        verify(values).remove(value);
        verifyNoMoreInteractions(values);
        verifyZeroInteractions(delivery);
        tgt.assertUnmodified();
    }

    @Test
    public void testApplyUndo_DirectoryInRoot() throws GarageException, IOException {
        final GaragePath<NamedObject> path = GaragePath.fromPath("/1", tgt.root);
        final CmdGarageRemove<NamedObject> cut = new CmdGarageRemove<>(delivery, path);

        cut.apply();
        assertEquals(2, tgt.root.getValues().size());
        assertEquals(1, tgt.root.getDirectories().size());
        assertTrue(tgt.root.getDirectories().get(0).getName().equals("2"));
        verify(delivery).post(new GarageMessage<>(GarageMessageType.REMOVED, path));

        reset(delivery);
        cut.undo();
        verify(delivery).post(new GarageMessage<>(GarageMessageType.ADDED, path));
        tgt.assertUnmodified();
    }

    @Test
    public void testApplyUndo_ValueInRoot() throws GarageException, IOException {
        final GaragePath<NamedObject> path = GaragePath.fromPath("/x", tgt.root);
        final CmdGarageRemove<NamedObject> cut = new CmdGarageRemove<>(delivery, path);

        cut.apply();
        assertEquals(1, tgt.root.getValues().size());
        assertEquals(2, tgt.root.getDirectories().size());
        assertTrue(tgt.root.getValues().get(0).getName().equals("w"));
        verify(delivery).post(new GarageMessage<>(GarageMessageType.REMOVED, path));

        reset(delivery);
        cut.undo();
        verify(delivery).post(new GarageMessage<>(GarageMessageType.ADDED, path));
        tgt.assertUnmodified();
    }

    @Test
    public void testDescribe() throws IOException {
        final GaragePath<NamedObject> path = GaragePath.fromPath("/2/d/z", tgt.root);
        final String ans = new CmdGarageRemove<>(delivery, path).describe();
        assertTrue(ans.contains("remove"));
        assertTrue(ans.contains(path.toPath()));
    }

}