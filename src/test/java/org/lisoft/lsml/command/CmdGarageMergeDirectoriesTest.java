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
import static org.mockito.Mockito.mock;

import java.util.Arrays;

import org.junit.Test;
import org.lisoft.lsml.messages.MessageDelivery;
import org.lisoft.lsml.model.NamedObject;
import org.lisoft.lsml.model.garage.GarageDirectory;
import org.lisoft.lsml.model.garage.GaragePath;

public class CmdGarageMergeDirectoriesTest {
    private final MessageDelivery messageDelivery = mock(MessageDelivery.class);

    @Test
    public void testMerge() throws Exception {
        // Destination:
        // root (1, 2)
        // +-sub1 (3, 4)
        // | +-sub1sub1 (7, 8)
        // | +-sub1sub2 (9 10)
        // +-sub2 (5, 6)
        final GarageDirectory<NamedObject> dstRoot = new GarageDirectory<>("root");
        final GarageDirectory<NamedObject> dstSub1 = new GarageDirectory<>("sub1");
        final GarageDirectory<NamedObject> dstSub2 = new GarageDirectory<>("sub2");
        final GarageDirectory<NamedObject> dstSub1Sub1 = new GarageDirectory<>("sub1sub1");
        final GarageDirectory<NamedObject> dstSub1Sub2 = new GarageDirectory<>("sub1sub2");
        dstRoot.getDirectories().addAll(Arrays.asList(dstSub1, dstSub2));
        dstSub1.getDirectories().addAll(Arrays.asList(dstSub1Sub1, dstSub1Sub2));
        dstRoot.getValues().addAll(Arrays.asList(new NamedObject("1"), new NamedObject("2")));
        dstSub1.getValues().addAll(Arrays.asList(new NamedObject("3"), new NamedObject("4")));
        dstSub2.getValues().addAll(Arrays.asList(new NamedObject("5"), new NamedObject("6")));
        dstSub1Sub1.getValues().addAll(Arrays.asList(new NamedObject("7"), new NamedObject("8")));
        dstSub1Sub2.getValues().addAll(Arrays.asList(new NamedObject("9"), new NamedObject("10")));
        final GaragePath<NamedObject> dstPath = new GaragePath<>(dstRoot);

        // Source:
        // root (2, 3)
        // +-sub1 (4, 5)
        // | +-sub1sub1 (8, 9)
        // | +-sub1sub3 (19, 20)
        // +-sub3 (6, 7)
        // | +-sub3sub1 (11, 12)
        final GarageDirectory<NamedObject> srcRoot = new GarageDirectory<>("root");
        final GarageDirectory<NamedObject> srcSub1 = new GarageDirectory<>("sub1");
        final GarageDirectory<NamedObject> srcSub3 = new GarageDirectory<>("sub3");
        final GarageDirectory<NamedObject> srcSub1Sub1 = new GarageDirectory<>("sub1sub1");
        final GarageDirectory<NamedObject> srcSub1Sub3 = new GarageDirectory<>("sub1sub3");
        final GarageDirectory<NamedObject> srcSub3Sub1 = new GarageDirectory<>("sub3sub1");
        srcRoot.getDirectories().addAll(Arrays.asList(srcSub1, srcSub3));
        srcSub1.getDirectories().addAll(Arrays.asList(srcSub1Sub1, srcSub1Sub3));
        srcSub3.getDirectories().addAll(Arrays.asList(srcSub3Sub1));
        srcRoot.getValues().addAll(Arrays.asList(new NamedObject("2"), new NamedObject("3")));
        srcSub1.getValues().addAll(Arrays.asList(new NamedObject("4"), new NamedObject("5")));
        srcSub3.getValues().addAll(Arrays.asList(new NamedObject("6"), new NamedObject("7")));
        srcSub1Sub1.getValues().addAll(Arrays.asList(new NamedObject("8"), new NamedObject("9")));
        srcSub1Sub3.getValues().addAll(Arrays.asList(new NamedObject("19"), new NamedObject("20")));
        srcSub3Sub1.getValues().addAll(Arrays.asList(new NamedObject("11"), new NamedObject("12")));
        final GaragePath<NamedObject> srcPath = new GaragePath<>(srcRoot);

        new CmdGarageMergeDirectories<>("test", messageDelivery, dstPath, srcPath).apply();

        // Source:
        // root (1, 2, 3)
        // +-sub1 (3, 4, 5)
        // | +-sub1sub1 (7, 8, 9)
        // | +-sub1sub2 (9, 10)
        // | +-sub1sub3 (19, 20)
        // +-sub2 (5, 6)
        // +-sub3 (6, 7)
        // | +-sub3sub1 (11, 12)
        final GarageDirectory<NamedObject> expRoot = new GarageDirectory<>("root");
        final GarageDirectory<NamedObject> expSub1 = new GarageDirectory<>("sub1");
        final GarageDirectory<NamedObject> expSub2 = new GarageDirectory<>("sub2");
        final GarageDirectory<NamedObject> expSub3 = new GarageDirectory<>("sub3");
        final GarageDirectory<NamedObject> expSub1Sub1 = new GarageDirectory<>("sub1sub1");
        final GarageDirectory<NamedObject> expSub1Sub2 = new GarageDirectory<>("sub1sub2");
        final GarageDirectory<NamedObject> expSub1Sub3 = new GarageDirectory<>("sub1sub3");
        final GarageDirectory<NamedObject> expSub3Sub1 = new GarageDirectory<>("sub3sub1");
        expRoot.getDirectories().addAll(Arrays.asList(expSub1, expSub2, expSub3));
        expSub1.getDirectories().addAll(Arrays.asList(expSub1Sub1, expSub1Sub2, expSub1Sub3));
        expSub3.getDirectories().addAll(Arrays.asList(expSub3Sub1));

        expRoot.getValues().addAll(Arrays.asList(new NamedObject("1"), new NamedObject("2"), new NamedObject("3")));
        expSub1.getValues().addAll(Arrays.asList(new NamedObject("3"), new NamedObject("4"), new NamedObject("5")));
        expSub1Sub1.getValues().addAll(Arrays.asList(new NamedObject("7"), new NamedObject("8"), new NamedObject("9")));
        expSub1Sub2.getValues().addAll(Arrays.asList(new NamedObject("9"), new NamedObject("10")));
        expSub1Sub3.getValues().addAll(Arrays.asList(new NamedObject("19"), new NamedObject("20")));
        expSub2.getValues().addAll(Arrays.asList(new NamedObject("5"), new NamedObject("6")));
        expSub3.getValues().addAll(Arrays.asList(new NamedObject("6"), new NamedObject("7")));
        expSub3Sub1.getValues().addAll(Arrays.asList(new NamedObject("11"), new NamedObject("12")));

        assertEquals(expRoot, dstRoot);
    }
}
