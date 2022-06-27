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
import org.lisoft.lsml.model.NamedObject;
import org.lisoft.lsml.model.garage.GaragePath;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Locale;

import static org.junit.Assert.assertTrue;
import static org.lisoft.lsml.model.garage.GaragePath.fromPath;

/**
 * Test suite for {@link CmdGarageMultiMoveOperation}.
 *
 * @author Li Song
 */
public class CmdGarageMultiMoveOperationTest {
    private final TestGarageTree tgt = new TestGarageTree();

    @Test
    public void testApplyUndo() throws Exception {
        final Collection<GaragePath<NamedObject>> paths = new ArrayList<>();
        paths.add(fromPath("/x", tgt.root));
        paths.add(fromPath("/1/b", tgt.root));
        paths.add(fromPath("/2/d/z", tgt.root));

        final GaragePath<NamedObject> dst = fromPath("/1/a/", tgt.root);
        final CmdGarageMultiMoveOperation<NamedObject> cut = new CmdGarageMultiMoveOperation<>(null, dst, paths);

        cut.apply();
        assertTrue(tgt.dira.getDirectories().contains(tgt.dirb));
        assertTrue(tgt.dira.getValues().contains(tgt.x));
        assertTrue(tgt.dira.getValues().contains(tgt.z));

        cut.undo();
        tgt.assertUnmodified();
    }

    @Test
    public void testDescribe() throws IOException {
        final Collection<GaragePath<NamedObject>> paths = new ArrayList<>();
        final GaragePath<NamedObject> dst = fromPath("/1/a/", tgt.root);
        final CmdGarageMultiMoveOperation<NamedObject> cut = new CmdGarageMultiMoveOperation<>(null, dst, paths);

        final String desc = cut.describe().toLowerCase(Locale.ENGLISH);
        assertTrue(desc.contains("multi"));
        assertTrue(desc.contains("move"));
    }
}
