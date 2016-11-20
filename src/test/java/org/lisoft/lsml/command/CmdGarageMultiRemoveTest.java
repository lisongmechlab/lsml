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

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.lisoft.lsml.model.garage.GaragePath.fromPath;
import static org.mockito.Mockito.mock;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Locale;

import org.junit.Test;
import org.lisoft.lsml.TestGarageTree;
import org.lisoft.lsml.messages.MessageDelivery;
import org.lisoft.lsml.model.NamedObject;
import org.lisoft.lsml.model.garage.GaragePath;

/**
 * Test suite for {@link CmdGarageMultiRemove}.
 *
 * @author Emily Björk
 */
public class CmdGarageMultiRemoveTest {

    private final TestGarageTree tgt = new TestGarageTree();
    private final MessageDelivery md = mock(MessageDelivery.class);

    @Test
    public void testApplyUndo() throws Exception {
        final Collection<GaragePath<NamedObject>> paths = new ArrayList<>();
        paths.add(fromPath("/x", tgt.root));
        paths.add(fromPath("/1/a", tgt.root));
        paths.add(fromPath("/2/d/z", tgt.root));
        final CmdGarageMultiRemove<?> cut = new CmdGarageMultiRemove<>(md, paths);

        cut.apply();
        assertFalse(tgt.root.getValues().contains(tgt.x));
        assertFalse(tgt.dir1.getDirectories().contains(tgt.dira));
        assertFalse(tgt.dird.getValues().contains(tgt.z));

        cut.undo();
        tgt.assertUnmodified();
    }

    @Test
    public void testDescribe() {
        final Collection<GaragePath<NamedObject>> paths = new ArrayList<>();
        final CmdGarageMultiRemove<?> cut = new CmdGarageMultiRemove<>(null, paths);

        final String desc = cut.describe().toLowerCase(Locale.ENGLISH);
        assertTrue(desc.contains("multi"));
        assertTrue(desc.contains("remove"));
    }
}
