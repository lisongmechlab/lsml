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
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.lisoft.lsml.messages.ItemMessage;
import org.lisoft.lsml.messages.ItemMessage.Type;
import org.lisoft.lsml.messages.MessageXBar;
import org.lisoft.lsml.model.chassi.Chassis;
import org.lisoft.lsml.model.chassi.Location;
import org.lisoft.lsml.model.database.ChassisDB;
import org.lisoft.lsml.model.database.ItemDB;
import org.lisoft.lsml.model.database.UpgradeDB;
import org.lisoft.lsml.model.item.HeatSink;
import org.lisoft.lsml.model.item.Internal;
import org.lisoft.lsml.model.item.Item;
import org.lisoft.lsml.model.loadout.ConfiguredComponentStandard;
import org.lisoft.lsml.model.loadout.DefaultLoadoutFactory;
import org.lisoft.lsml.model.loadout.EquipException;
import org.lisoft.lsml.model.loadout.EquipResult;
import org.lisoft.lsml.model.loadout.EquipResult.EquipResultType;
import org.lisoft.lsml.model.loadout.Loadout;
import org.lisoft.lsml.model.loadout.LoadoutFactory;
import org.lisoft.lsml.model.loadout.LoadoutStandard;
import org.lisoft.lsml.util.CommandStack;
import org.lisoft.lsml.util.ListArrayUtils;
import org.lisoft.lsml.util.TestHelpers;
import org.mockito.InOrder;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.MockitoJUnitRunner;

/**
 * Test suite for {@link CmdAutoAddItem}.
 *
 * @author Emily Björk
 */
@SuppressWarnings("javadoc")
@RunWith(MockitoJUnitRunner.class)
public class CmdAutoAddItemTest {
    @Mock
    private MessageXBar xBar;

    private final CommandStack stack = new CommandStack(0);
    private final LoadoutFactory loadoutFactory = new DefaultLoadoutFactory();

    /**
     * {@link CmdAutoAddItem} shall add an item to the first applicable slot in this loadout. Order the items are added
     * is: RA, RT, RL, HD, CT, LT, LL, LA
     */
    @Test
    public void testAddItem() throws Exception {
        final Chassis chassis = ChassisDB.lookup("AS7-D-DC");
        final Item mlas = ItemDB.lookup("MEDIUM LASER");
        final Item ac20 = ItemDB.lookup("AC/20");
        final Item lrm5 = ItemDB.lookup("LRM 5");
        final Item lrm15 = ItemDB.lookup("LRM 15");
        final Item std250 = ItemDB.lookup("STD ENGINE 250");
        final HeatSink dhs = ItemDB.DHS;

        final LoadoutStandard loadout = (LoadoutStandard) loadoutFactory.produceEmpty(chassis);
        final ConfiguredComponentStandard ct = loadout.getComponent(Location.CenterTorso);
        final ConfiguredComponentStandard lt = loadout.getComponent(Location.LeftTorso);
        final ConfiguredComponentStandard rt = loadout.getComponent(Location.RightTorso);
        final ConfiguredComponentStandard la = loadout.getComponent(Location.LeftArm);
        final ConfiguredComponentStandard ra = loadout.getComponent(Location.RightArm);
        stack.pushAndApply(new CmdSetHeatSinkType(xBar, loadout, UpgradeDB.IS_DHS));

        final InOrder io = Mockito.inOrder(xBar);

        stack.pushAndApply(new CmdAutoAddItem(loadout, xBar, mlas, loadoutFactory));
        assertTrue(ra.getItemsEquipped().contains(mlas));
        io.verify(xBar).post(new ItemMessage(ra, Type.Added, mlas, 0));

        stack.pushAndApply(new CmdAutoAddItem(loadout, xBar, mlas, loadoutFactory));
        assertTrue(la.getItemsEquipped().contains(mlas));
        io.verify(xBar).post(new ItemMessage(la, Type.Added, mlas, 0));

        stack.pushAndApply(new CmdAutoAddItem(loadout, xBar, ac20, loadoutFactory));
        assertTrue(rt.getItemsEquipped().contains(ac20));
        io.verify(xBar).post(new ItemMessage(rt, Type.Added, ac20, 0));

        stack.pushAndApply(new CmdAutoAddItem(loadout, xBar, lrm5, loadoutFactory));
        assertTrue(lt.getItemsEquipped().contains(lrm5));
        io.verify(xBar).post(new ItemMessage(lt, Type.Added, lrm5, 0));

        stack.pushAndApply(new CmdAutoAddItem(loadout, xBar, lrm15, loadoutFactory));
        assertTrue(lt.getItemsEquipped().contains(lrm15));
        io.verify(xBar).post(new ItemMessage(lt, Type.Added, lrm15, 1));

        stack.pushAndApply(new CmdAutoAddItem(loadout, xBar, std250, loadoutFactory));
        assertTrue(ct.getItemsEquipped().contains(std250));
        io.verify(xBar).post(new ItemMessage(ct, Type.Added, std250, 0));

        // Fill right arm
        stack.pushAndApply(new CmdAutoAddItem(loadout, xBar, dhs, loadoutFactory));
        assertEquals(1, ListArrayUtils.countByType(ra.getItemsEquipped(), HeatSink.class));
        io.verify(xBar).post(new ItemMessage(ra, Type.Added, dhs, 1));

        stack.pushAndApply(new CmdAutoAddItem(loadout, xBar, dhs, loadoutFactory));
        assertEquals(2, ListArrayUtils.countByType(ra.getItemsEquipped(), HeatSink.class));
        io.verify(xBar).post(new ItemMessage(ra, Type.Added, dhs, 2));

        // Skips RA, RT, RL, HD, CT (too few slots) and places the item in LT
        stack.pushAndApply(new CmdAutoAddItem(loadout, xBar, dhs, loadoutFactory));
        assertTrue(lt.getItemsEquipped().contains(dhs));
        io.verify(xBar).post(new ItemMessage(lt, Type.Added, dhs, 2));

        // Skips RA (too few slots) and places the item in RT
        final Item bap = ItemDB.BAP;
        stack.pushAndApply(new CmdAutoAddItem(loadout, xBar, bap, loadoutFactory));
        assertTrue(rt.getItemsEquipped().contains(bap));
        io.verify(xBar).post(new ItemMessage(rt, Type.Added, bap, 1));
    }

    /**
     * {@link CmdAutoAddItem} shall prioritise engine slots for heat sinks.
     */
    @Test
    public void testAddItem_engineHS() throws Exception {
        final LoadoutStandard loadout = (LoadoutStandard) loadoutFactory.produceEmpty(ChassisDB.lookup("AS7-D-DC"));

        final Item std300 = ItemDB.lookup("STD ENGINE 300");
        stack.pushAndApply(new CmdAutoAddItem(loadout, xBar, std300, loadoutFactory));
        assertTrue(loadout.getComponent(Location.CenterTorso).getItemsEquipped()
                .contains(ItemDB.lookup("STD ENGINE 300")));

        final HeatSink shs = ItemDB.SHS;
        stack.pushAndApply(new CmdAutoAddItem(loadout, xBar, shs, loadoutFactory)); // Engine HS slot 1
        stack.pushAndApply(new CmdAutoAddItem(loadout, xBar, shs, loadoutFactory)); // Engine HS slot 2
        stack.pushAndApply(new CmdAutoAddItem(loadout, xBar, shs, loadoutFactory)); // Right arm

        verify(xBar, times(2)).post(new ItemMessage(loadout.getComponent(Location.CenterTorso), Type.Added, shs, -1));
        verify(xBar).post(new ItemMessage(loadout.getComponent(Location.RightArm), Type.Added, shs, 0));
        assertTrue(loadout.getComponent(Location.CenterTorso).getItemsEquipped().contains(shs)); // 1 remaining
        assertTrue(loadout.getComponent(Location.RightArm).getItemsEquipped().contains(shs));
    }

    /**
     * {@link CmdAutoAddItem} shall throw the correct error if the item is not feasible globally.
     */
    @Test
    public void testAddItem_NotGloballyFeasible() throws Exception {
        final LoadoutStandard loadout = (LoadoutStandard) loadoutFactory.produceEmpty(ChassisDB.lookup("JR7-D"));

        try {
            stack.pushAndApply(new CmdAutoAddItem(loadout, xBar, ItemDB.ECM, loadoutFactory));
            fail("Expected exception");
        }
        catch (final EquipException equipException) {
            assertSame(EquipResult.make(EquipResultType.NotSupported), equipException.getResult());
        }
        catch (final Throwable t) {
            fail("Wrong exeption type!");
        }
    }

    @Test(timeout = 5000)
    public void testApply_XLEnginePerformance() throws Exception {
        // Setup
        final Loadout loadout = TestHelpers.parse("lsml://rQAAKCwqCDISSg4qCDEDvqmbFj6wWK9evXsLLAEYCg==");
        // There is one free hard point in CT but no free slots, LRM10 must be swapped
        // with LRM 5

        // Execute
        stack.pushAndApply(new CmdAutoAddItem(loadout, xBar, ItemDB.lookup("XL ENGINE 200"), loadoutFactory));
    }

    /**
     * {@link CmdAutoAddItem} shall try to move items in order to make room for the added item if there is no room in
     * any component with a hard point but there are items that could be moved to make room.
     */
    @Test
    public void testMoveItem() throws Exception {
        // Setup
        final LoadoutStandard loadout = (LoadoutStandard) loadoutFactory.produceEmpty(ChassisDB.lookup("AS7-D-DC"));
        stack.pushAndApply(new CmdSetHeatSinkType(xBar, loadout, UpgradeDB.IS_DHS));
        stack.pushAndApply(new CmdAddItem(xBar, loadout, loadout.getComponent(Location.RightTorso), ItemDB.DHS));
        stack.pushAndApply(new CmdAddItem(xBar, loadout, loadout.getComponent(Location.RightTorso), ItemDB.DHS));
        final Item gaussRifle = ItemDB.lookup("GAUSS RIFLE");

        // Execute
        stack.pushAndApply(new CmdAutoAddItem(loadout, xBar, gaussRifle, loadoutFactory));

        // Verify
        final List<Item> allItems = new ArrayList<>();
        for (final Item item : loadout.items()) {
            if (item instanceof Internal) {
                continue;
            }
            allItems.add(item);
        }
        assertTrue(allItems.remove(ItemDB.DHS));
        assertTrue(allItems.remove(ItemDB.DHS));
        assertTrue(allItems.remove(gaussRifle));

        // Must be minimal change to allow the item in.
        assertTrue(loadout.getComponent(Location.RightTorso).getItemsEquipped().contains(ItemDB.DHS));
        assertTrue(loadout.getComponent(Location.RightTorso).getItemsEquipped().contains(gaussRifle));
    }

    /**
     * {@link CmdAutoAddItem} shall try to move items in order to make room for the added item if there is no room in
     * any component with a hard point but there are items that could be moved to make room.
     */
    @Test
    public void testMoveItem_() throws Exception {
        // Setup
        final Item ac20 = ItemDB.lookup("AC/20");
        final Item ac10 = ItemDB.lookup("AC/10");
        final LoadoutStandard loadout = (LoadoutStandard) loadoutFactory.produceEmpty(ChassisDB.lookup("CTF-IM"));
        stack.pushAndApply(new CmdAddItem(xBar, loadout, loadout.getComponent(Location.RightTorso), ac10));

        // Execute
        stack.pushAndApply(new CmdAutoAddItem(loadout, xBar, ac20, loadoutFactory));

        // Verify
        final List<Item> allItems = new ArrayList<>();
        for (final Item item : loadout.items()) {
            if (item instanceof Internal) {
                continue;
            }
            allItems.add(item);
        }
        assertEquals(2, allItems.size());

        assertTrue(loadout.getComponent(Location.RightTorso).getItemsEquipped().contains(ac20));
        assertTrue(loadout.getComponent(Location.RightArm).getItemsEquipped().contains(ac10)
                || loadout.getComponent(Location.LeftArm).getItemsEquipped().contains(ac10));
    }

    // Bug #345
    @Test
    public void testMoveItem_Bug_345() throws Exception {
        // Setup
        final Loadout loadout = TestHelpers
                .parse("lsml://rgCkLzsFLw9VBzsFLy4A6zGmJKTKlSq1vEEXyq1atPuJWk4kqVKrVa1DExJUqVY=");
        final Item item = ItemDB.lookup("CLAN DOUBLE HEAT SINK");

        // Execute
        stack.pushAndApply(new CmdAutoAddItem(loadout, xBar, item, loadoutFactory));

        // Verify
        assertEquals(26, loadout.getHeatsinksCount()); // Heat sink is added
        assertEquals(73.4, loadout.getMass(), 0.1); // Mass is as is expected
    }

    // Bug #349
    @Test(expected = EquipException.class/* , timeout = 5000 */)
    public void testMoveItem_Bug_349() throws Exception {
        // Setup
        Loadout loadout;
        Item item;
        try {
            loadout = TestHelpers
                    .parse("lsml://rgCzAAAAAAAAAAAAAAAA6zHWZdZdZdZdZdZdSpVd3KlSq66untdjKlSq62uoy6y6y6y6y6y6lSr+2f6M");
            item = ItemDB.lookup("CLAN DOUBLE HEAT SINK");
        }
        catch (final Throwable e) {
            fail("Unexpected exception: " + e.toString());
            return; // Tell compiler that loadout and item are always initialised
        }

        // Execute
        stack.pushAndApply(new CmdAutoAddItem(loadout, xBar, item, loadoutFactory));
    }

    /**
     * This test is a regression test for a bug where auto-add an ER PPC would fail on
     * lsml://rRoAkQAAAAAAAAAAAAAAuihsbMzMbDCRE22zG2DF where a trivial solution is available.
     */
    @Test
    public void testMoveItem_Bug1() throws Exception {
        // Setup
        final LoadoutStandard loadout = (LoadoutStandard) loadoutFactory.produceEmpty(ChassisDB.lookup("BNC-3M"));
        stack.pushAndApply(new CmdSetHeatSinkType(xBar, loadout, UpgradeDB.IS_DHS));
        stack.pushAndApply(new CmdAddItem(xBar, loadout, loadout.getComponent(Location.RightArm), ItemDB.DHS));
        stack.pushAndApply(new CmdAddItem(xBar, loadout, loadout.getComponent(Location.RightArm), ItemDB.DHS));
        stack.pushAndApply(new CmdAddItem(xBar, loadout, loadout.getComponent(Location.RightTorso), ItemDB.DHS));
        stack.pushAndApply(new CmdAddItem(xBar, loadout, loadout.getComponent(Location.RightTorso), ItemDB.DHS));
        stack.pushAndApply(new CmdAddItem(xBar, loadout, loadout.getComponent(Location.RightTorso),
                ItemDB.lookup("MEDIUM LASER")));
        stack.pushAndApply(new CmdAddItem(xBar, loadout, loadout.getComponent(Location.RightTorso),
                ItemDB.lookup("MEDIUM LASER")));
        stack.pushAndApply(new CmdAddItem(xBar, loadout, loadout.getComponent(Location.RightTorso),
                ItemDB.lookup("MEDIUM LASER")));
        stack.pushAndApply(new CmdAddItem(xBar, loadout, loadout.getComponent(Location.RightTorso),
                ItemDB.lookup("MEDIUM LASER")));
        stack.pushAndApply(
                new CmdAddItem(xBar, loadout, loadout.getComponent(Location.Head), ItemDB.lookup("MEDIUM LASER")));
        stack.pushAndApply(new CmdAddItem(xBar, loadout, loadout.getComponent(Location.CenterTorso),
                ItemDB.lookup("STD ENGINE 200")));
        stack.pushAndApply(
                new CmdAddItem(xBar, loadout, loadout.getComponent(Location.LeftTorso), ItemDB.lookup("MEDIUM LASER")));
        stack.pushAndApply(new CmdAddItem(xBar, loadout, loadout.getComponent(Location.LeftTorso), ItemDB.DHS));
        stack.pushAndApply(new CmdAddItem(xBar, loadout, loadout.getComponent(Location.LeftTorso), ItemDB.DHS));
        stack.pushAndApply(new CmdAddItem(xBar, loadout, loadout.getComponent(Location.LeftTorso), ItemDB.DHS));
        stack.pushAndApply(new CmdAddItem(xBar, loadout, loadout.getComponent(Location.LeftArm), ItemDB.DHS));
        // There is one free hard point in CT but no free slots, LRM10 must be swapped
        // with LRM 5

        // Execute
        stack.pushAndApply(new CmdAutoAddItem(loadout, xBar, ItemDB.lookup("ER PPC"), loadoutFactory));

        // Verify
        final List<Item> allItems = new ArrayList<>();
        for (final Item item : loadout.items()) {
            if (item instanceof Internal) {
                continue;
            }
            allItems.add(item);
        }
        assertEquals(16, allItems.size());
        assertTrue(allItems.remove(ItemDB.lookup("ER PPC")));
    }

    @Test
    public void testMoveItem_Bug2() throws Exception {
        // Setup
        final Loadout loadout = TestHelpers.parse("lsml://rRsAkEBHCFASSAhHCFBAuihsWsWrVrYLS3G21q0UFBQUFrWg2tWi");
        // There is one free hard point in CT but no free slots, LRM10 must be swapped
        // with LRM 5

        // Execute
        stack.pushAndApply(new CmdAutoAddItem(loadout, xBar, ItemDB.AMS, loadoutFactory));

        // Verify
        final List<Item> allItems = new ArrayList<>();
        for (final Item item : loadout.items()) {
            if (item instanceof Internal) {
                continue;
            }
            allItems.add(item);
        }
        assertTrue(allItems.remove(ItemDB.AMS));
    }

    /**
     * {@link CmdAutoAddItem} shall throw an {@link EquipResult} if the item cannot be auto added on any permutation of
     * the loadout.
     */
    @Test(expected = EquipException.class)
    public void testMoveItem_NotPossible() throws Exception {
        LoadoutStandard loadout = null;
        Item gaussRifle = null;
        try {
            // Setup
            loadout = (LoadoutStandard) loadoutFactory.produceEmpty(ChassisDB.lookup("AS7-D-DC"));
            stack.pushAndApply(new CmdSetHeatSinkType(xBar, loadout, UpgradeDB.IS_DHS));

            // 2 slots in either leg
            // 2 slots left in CT
            stack.pushAndApply(new CmdAddItem(xBar, loadout, loadout.getComponent(Location.CenterTorso),
                    ItemDB.lookup("XL ENGINE 200")));

            // 2 slots left on right arm, cannot contain DHS
            stack.pushAndApply(new CmdAddItem(xBar, loadout, loadout.getComponent(Location.RightArm), ItemDB.DHS));
            stack.pushAndApply(new CmdAddItem(xBar, loadout, loadout.getComponent(Location.RightArm), ItemDB.DHS));

            // 2 slots left on left arm, cannot contain DHS
            stack.pushAndApply(new CmdAddItem(xBar, loadout, loadout.getComponent(Location.LeftArm), ItemDB.DHS));
            stack.pushAndApply(new CmdAddItem(xBar, loadout, loadout.getComponent(Location.LeftArm), ItemDB.DHS));

            // 6 slots left in right torso (3 taken by engine)
            stack.pushAndApply(new CmdAddItem(xBar, loadout, loadout.getComponent(Location.RightTorso), ItemDB.DHS));

            // 0 slots left in left torso (3 taken by engine)
            stack.pushAndApply(new CmdAddItem(xBar, loadout, loadout.getComponent(Location.LeftTorso), ItemDB.DHS));
            stack.pushAndApply(new CmdAddItem(xBar, loadout, loadout.getComponent(Location.LeftTorso), ItemDB.DHS));
            stack.pushAndApply(new CmdAddItem(xBar, loadout, loadout.getComponent(Location.LeftTorso), ItemDB.DHS));

            gaussRifle = ItemDB.lookup("GAUSS RIFLE");
        }
        catch (final Throwable e) {
            fail("Setup threw");
            return;
        }
        // Execute
        stack.pushAndApply(new CmdAutoAddItem(loadout, xBar, gaussRifle, loadoutFactory));
    }

    /**
     * {@link CmdAutoAddItem} shall be able to swap items in addition to just moving one at a time. Otherwise there we
     * miss some solutions.
     */
    @Test
    public void testMoveItem_SwapItems() throws Exception {
        // Setup
        final Chassis chassis = ChassisDB.lookup("JR7-O");
        final LoadoutStandard loadout = (LoadoutStandard) loadoutFactory.produceEmpty(chassis);
        final ConfiguredComponentStandard la = loadout.getComponent(Location.LeftArm);
        final ConfiguredComponentStandard ra = loadout.getComponent(Location.RightArm);
        final ConfiguredComponentStandard ct = loadout.getComponent(Location.CenterTorso);
        final Item lrm5 = ItemDB.lookup("LRM 5");
        final Item lrm10 = ItemDB.lookup("LRM 10");
        final Item engine = ItemDB.lookup("XL ENGINE 200");

        ct.addItem(engine);
        ct.addItem(lrm10);
        ra.addItem(lrm10);
        la.addItem(lrm5);
        // There is one free hard point in CT but no free slots, LRM10 must be swapped
        // with LRM 5

        // Execute
        stack.pushAndApply(new CmdAutoAddItem(loadout, xBar, lrm5, loadoutFactory));

        // Verify (this is the only valid solution)
        assertTrue(ListArrayUtils.equalsUnordered(ct.getItemsEquipped(), Arrays.asList(engine, lrm5, lrm5)));
        assertTrue(ListArrayUtils.equalsUnordered(la.getItemsEquipped(), Arrays.asList(lrm10)));
        assertTrue(ListArrayUtils.equalsUnordered(ra.getItemsEquipped(), Arrays.asList(lrm10)));

        verify(xBar).post(new ItemMessage(ct, Type.Removed, lrm10, 1));
        verify(xBar).post(new ItemMessage(ct, Type.Added, lrm5, 1));
        verify(xBar).post(new ItemMessage(ct, Type.Added, lrm5, 2));
        verify(xBar).post(new ItemMessage(la, Type.Removed, lrm5, 0));
        verify(xBar).post(new ItemMessage(la, Type.Added, lrm10, 0));
    }
}
