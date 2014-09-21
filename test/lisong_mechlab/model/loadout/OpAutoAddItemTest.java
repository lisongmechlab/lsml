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
package lisong_mechlab.model.loadout;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import java.util.ArrayList;
import java.util.List;

import lisong_mechlab.model.chassi.ChassisDB;
import lisong_mechlab.model.chassi.ChassisStandard;
import lisong_mechlab.model.chassi.Location;
import lisong_mechlab.model.item.Internal;
import lisong_mechlab.model.item.Item;
import lisong_mechlab.model.item.ItemDB;
import lisong_mechlab.model.loadout.component.ConfiguredComponentBase;
import lisong_mechlab.model.loadout.component.ConfiguredComponentBase.ComponentMessage.Type;
import lisong_mechlab.model.loadout.component.OpAddItem;
import lisong_mechlab.model.loadout.export.Base64LoadoutCoder;
import lisong_mechlab.model.upgrades.OpSetHeatSinkType;
import lisong_mechlab.model.upgrades.UpgradeDB;
import lisong_mechlab.util.DecodingException;
import lisong_mechlab.util.OperationStack;
import lisong_mechlab.util.message.MessageXBar;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.runners.MockitoJUnitRunner;

/**
 * Test suite for {@link OpAutoAddItem}.
 * 
 * @author Li Song
 */
@RunWith(MockitoJUnitRunner.class)
public class OpAutoAddItemTest {
	@Mock
	private MessageXBar		xBar;

	private OperationStack	stack	= new OperationStack(0);

	@Test(timeout = 5000)
	public void testApply_XLEnginePerformance() throws DecodingException {
		// Setup
		Base64LoadoutCoder coder = new Base64LoadoutCoder();
		LoadoutBase<?> loadout = coder.parse("lsml://rQAAKCwqCDISSg4qCDEDvqmbFj6wWK9evXsLLAEYCg==");
		// There is one free hard point in CT but no free slots, LRM10 must be swapped with LRM 5

		// Execute
		stack.pushAndApply(new OpAutoAddItem(loadout, xBar, ItemDB.lookup("XL ENGINE 200")));
	}

	@Test
	public void testMoveItem_Bug2() throws DecodingException {
		// Setup
		Base64LoadoutCoder coder = new Base64LoadoutCoder();
		LoadoutBase<?> loadout = coder.parse("lsml://rRsAkEBHCFASSAhHCFBAuihsWsWrVrYLS3G21q0UFBQUFrWg2tWi");
		// There is one free hard point in CT but no free slots, LRM10 must be swapped with LRM 5

		// Execute
		stack.pushAndApply(new OpAutoAddItem(loadout, xBar, ItemDB.AMS));

		// Verify
		List<Item> allItems = new ArrayList<>();
		for(Item item : loadout.items()){
			if(item instanceof Internal)
				continue;
			allItems.add(item);
		}
		assertTrue(allItems.remove(ItemDB.AMS));
	}

	// Bug #345
	@Test
	public void testMoveItem_Bug_345() throws DecodingException {
		// Setup
		Base64LoadoutCoder coder = new Base64LoadoutCoder();
		LoadoutBase<?> loadout = coder.parse("lsml://rgCkLzsFLw9VBzsFLy4A6zGmSpSSkyq1vElShF9atWn3ErScSVKlVqtahiYkqVKs");
		Item item = ItemDB.lookup("CLAN DOUBLE HEAT SINK");

		// Execute
		stack.pushAndApply(new OpAutoAddItem(loadout, xBar, item));

		// Verify
		assertEquals(27, loadout.getHeatsinksCount()); // Heat sink is added
		assertEquals(72.4, loadout.getMass(), 0.1); // Mass is as is expected
	}

	// Bug #349
	@Test(expected = IllegalArgumentException.class, timeout = 5000)
	public void testMoveItem_Bug_349() throws DecodingException {
		// Setup
		Base64LoadoutCoder coder = new Base64LoadoutCoder();
		LoadoutBase<?> loadout = coder
				.parse("lsml://rgCzAAAAAAAAAAAAAAAA6zHWZdZdZdZdZdZdSpVd3KlSq66untdjKlSq62uoy6y6y6y6y6y6lSr+2f6M");
		Item item = ItemDB.lookup("CLAN DOUBLE HEAT SINK");

		// Execute
		stack.pushAndApply(new OpAutoAddItem(loadout, xBar, item));
	}

	/**
	 * This test is a regression test for a bug where auto-add an ER PPC would fail on
	 * lsml://rRoAkQAAAAAAAAAAAAAAuihsbMzMbDCRE22zG2DF where a trivial solution is available.
	 */
	@Test
	public void testMoveItem_Bug1() {
		// Setup
		LoadoutStandard loadout = new LoadoutStandard((ChassisStandard) ChassisDB.lookup("BNC-3M"));
		stack.pushAndApply(new OpSetHeatSinkType(xBar, loadout, UpgradeDB.DOUBLE_HEATSINKS));
		stack.pushAndApply(new OpAddItem(xBar, loadout, loadout.getComponent(Location.RightArm), ItemDB.DHS));
		stack.pushAndApply(new OpAddItem(xBar, loadout, loadout.getComponent(Location.RightArm), ItemDB.DHS));
		stack.pushAndApply(new OpAddItem(xBar, loadout, loadout.getComponent(Location.RightTorso), ItemDB.DHS));
		stack.pushAndApply(new OpAddItem(xBar, loadout, loadout.getComponent(Location.RightTorso), ItemDB.DHS));
		stack.pushAndApply(new OpAddItem(xBar, loadout, loadout.getComponent(Location.RightTorso), ItemDB
				.lookup("MEDIUM LASER")));
		stack.pushAndApply(new OpAddItem(xBar, loadout, loadout.getComponent(Location.RightTorso), ItemDB
				.lookup("MEDIUM LASER")));
		stack.pushAndApply(new OpAddItem(xBar, loadout, loadout.getComponent(Location.RightTorso), ItemDB
				.lookup("MEDIUM LASER")));
		stack.pushAndApply(new OpAddItem(xBar, loadout, loadout.getComponent(Location.RightTorso), ItemDB
				.lookup("MEDIUM LASER")));
		stack.pushAndApply(new OpAddItem(xBar, loadout, loadout.getComponent(Location.Head), ItemDB
				.lookup("MEDIUM LASER")));
		stack.pushAndApply(new OpAddItem(xBar, loadout, loadout.getComponent(Location.CenterTorso), ItemDB
				.lookup("STD ENGINE 200")));
		stack.pushAndApply(new OpAddItem(xBar, loadout, loadout.getComponent(Location.LeftTorso), ItemDB
				.lookup("MEDIUM LASER")));
		stack.pushAndApply(new OpAddItem(xBar, loadout, loadout.getComponent(Location.LeftTorso), ItemDB.DHS));
		stack.pushAndApply(new OpAddItem(xBar, loadout, loadout.getComponent(Location.LeftTorso), ItemDB.DHS));
		stack.pushAndApply(new OpAddItem(xBar, loadout, loadout.getComponent(Location.LeftTorso), ItemDB.DHS));
		stack.pushAndApply(new OpAddItem(xBar, loadout, loadout.getComponent(Location.LeftArm), ItemDB.DHS));
		// There is one free hard point in CT but no free slots, LRM10 must be swapped with LRM 5

		// Execute
		stack.pushAndApply(new OpAutoAddItem(loadout, xBar, ItemDB.lookup("ER PPC")));

		// Verify
		List<Item> allItems = new ArrayList<>();
		for(Item item : loadout.items()){
			if(item instanceof Internal)
				continue;
			allItems.add(item);
		}
		assertEquals(16, allItems.size());
		assertTrue(allItems.remove(ItemDB.lookup("ER PPC")));
	}

	/**
	 * {@link OpAutoAddItem} shall be able to swap items in addition to just moving one at a time. Otherwise there we
	 * miss some solutions.
	 */
	@Test
	public void testMoveItem_SwapItems() {
		// Setup
		LoadoutStandard loadout = new LoadoutStandard((ChassisStandard) ChassisDB.lookup("JR7-O"));
		stack.pushAndApply(new OpAddItem(xBar, loadout, loadout.getComponent(Location.CenterTorso), ItemDB
				.lookup("XL ENGINE 200")));
		stack.pushAndApply(new OpAddItem(xBar, loadout, loadout.getComponent(Location.CenterTorso), ItemDB
				.lookup("LRM 10")));
		stack.pushAndApply(new OpAddItem(xBar, loadout, loadout.getComponent(Location.RightArm), ItemDB
				.lookup("LRM 10")));
		stack.pushAndApply(new OpAddItem(xBar, loadout, loadout.getComponent(Location.LeftArm), ItemDB.lookup("LRM 5")));
		Mockito.reset(xBar);
		// There is one free hard point in CT but no free slots, LRM10 must be swapped with LRM 5

		// Execute
		stack.pushAndApply(new OpAutoAddItem(loadout, xBar, ItemDB.lookup("LRM 5")));

		// Verify
		List<Item> allItems = new ArrayList<>();
		for(Item item : loadout.items()){
			if(item instanceof Internal)
				continue;
			allItems.add(item);
		}
		assertEquals(5, allItems.size());
		assertTrue(allItems.remove(ItemDB.lookup("LRM 10")));
		assertTrue(allItems.remove(ItemDB.lookup("LRM 10")));
		assertTrue(allItems.remove(ItemDB.lookup("LRM 5")));
		assertTrue(allItems.remove(ItemDB.lookup("LRM 5")));
		assertTrue(allItems.remove(ItemDB.lookup("XL ENGINE 200")));

		// 1 + 1, move one lrm 5 here and add the wanted lrm 5
		verify(xBar, times(2)).post(
				new ConfiguredComponentBase.ComponentMessage(loadout.getComponent(Location.CenterTorso), Type.ItemAdded));
		verify(xBar, times(1)).post(
				new ConfiguredComponentBase.ComponentMessage(loadout.getComponent(Location.CenterTorso), Type.ItemRemoved));
		verify(xBar, times(1)).post(
				new ConfiguredComponentBase.ComponentMessage(loadout.getComponent(Location.LeftArm), Type.ItemAdded));
		verify(xBar, times(1)).post(
				new ConfiguredComponentBase.ComponentMessage(loadout.getComponent(Location.LeftArm), Type.ItemRemoved));
	}

	/**
	 * {@link OpAutoAddItem} shall throw an {@link IllegalArgumentException} if the item cannot be auto added on any
	 * permutation of the loadout.
	 */
	@Test(expected = IllegalArgumentException.class)
	public void testMoveItem_NotPossible() {
		LoadoutStandard loadout = null;
		Item gaussRifle = null;
		try {
			// Setup
			loadout = new LoadoutStandard((ChassisStandard) ChassisDB.lookup("AS7-D-DC"));
			stack.pushAndApply(new OpSetHeatSinkType(xBar, loadout, UpgradeDB.DOUBLE_HEATSINKS));

			// 2 slots in either leg
			// 2 slots left in CT
			stack.pushAndApply(new OpAddItem(xBar, loadout, loadout.getComponent(Location.CenterTorso), ItemDB
					.lookup("XL ENGINE 200")));

			// 2 slots left on right arm, cannot contain DHS
			stack.pushAndApply(new OpAddItem(xBar, loadout, loadout.getComponent(Location.RightArm), ItemDB.DHS));
			stack.pushAndApply(new OpAddItem(xBar, loadout, loadout.getComponent(Location.RightArm), ItemDB.DHS));

			// 2 slots left on left arm, cannot contain DHS
			stack.pushAndApply(new OpAddItem(xBar, loadout, loadout.getComponent(Location.LeftArm), ItemDB.DHS));
			stack.pushAndApply(new OpAddItem(xBar, loadout, loadout.getComponent(Location.LeftArm), ItemDB.DHS));

			// 6 slots left in right torso (3 taken by engine)
			stack.pushAndApply(new OpAddItem(xBar, loadout, loadout.getComponent(Location.RightTorso), ItemDB.DHS));

			// 0 slots left in left torso (3 taken by engine)
			stack.pushAndApply(new OpAddItem(xBar, loadout, loadout.getComponent(Location.LeftTorso), ItemDB.DHS));
			stack.pushAndApply(new OpAddItem(xBar, loadout, loadout.getComponent(Location.LeftTorso), ItemDB.DHS));
			stack.pushAndApply(new OpAddItem(xBar, loadout, loadout.getComponent(Location.LeftTorso), ItemDB.DHS));

			gaussRifle = ItemDB.lookup("GAUSS RIFLE");
		} catch (Throwable e) {
			fail("Setup threw");
			return;
		}
		// Execute
		stack.pushAndApply(new OpAutoAddItem(loadout, xBar, gaussRifle));
	}

	/**
	 * {@link OpAutoAddItem} shall try to move items in order to make room for the added item if there is no room in any
	 * component with a hard point but there are items that could be moved to make room.
	 */
	@Test
	public void testMoveItem() {
		// Setup
		LoadoutStandard loadout = new LoadoutStandard((ChassisStandard) ChassisDB.lookup("AS7-D-DC"));
		stack.pushAndApply(new OpSetHeatSinkType(xBar, loadout, UpgradeDB.DOUBLE_HEATSINKS));
		stack.pushAndApply(new OpAddItem(xBar, loadout, loadout.getComponent(Location.RightTorso), ItemDB.DHS));
		stack.pushAndApply(new OpAddItem(xBar, loadout, loadout.getComponent(Location.RightTorso), ItemDB.DHS));
		Item gaussRifle = ItemDB.lookup("GAUSS RIFLE");

		// Execute
		stack.pushAndApply(new OpAutoAddItem(loadout, xBar, gaussRifle));

		// Verify
		List<Item> allItems = new ArrayList<>();
		for(Item item : loadout.items()){
			if(item instanceof Internal)
				continue;
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
	 * {@link OpAutoAddItem} shall try to move items in order to make room for the added item if there is no room in any
	 * component with a hard point but there are items that could be moved to make room.
	 */
	@Test
	public void testMoveItem_() {
		// Setup
		Item ac20 = ItemDB.lookup("AC/20");
		Item ac10 = ItemDB.lookup("AC/10");
		LoadoutStandard loadout = new LoadoutStandard((ChassisStandard) ChassisDB.lookup("CTF-IM"));
		stack.pushAndApply(new OpAddItem(xBar, loadout, loadout.getComponent(Location.RightTorso), ac10));

		// Execute
		stack.pushAndApply(new OpAutoAddItem(loadout, xBar, ac20));

		// Verify
		List<Item> allItems = new ArrayList<>();
		for(Item item : loadout.items()){
			if(item instanceof Internal)
				continue;
			allItems.add(item);
		}
		assertEquals(2, allItems.size());

		assertTrue(loadout.getComponent(Location.RightTorso).getItemsEquipped().contains(ac20));
		assertTrue(loadout.getComponent(Location.RightArm).getItemsEquipped().contains(ac10)
				|| loadout.getComponent(Location.LeftArm).getItemsEquipped().contains(ac10));
	}

	/**
	 * {@link OpAutoAddItem} shall add an item to the first applicable slot in this loadout. Order the items are added
	 * is: RA, RT, RL, HD, CT, LT, LL, LA
	 */
	@Test
	public void testAddItem() {
		LoadoutStandard loadout = new LoadoutStandard((ChassisStandard) ChassisDB.lookup("AS7-D-DC"));
		stack.pushAndApply(new OpSetHeatSinkType(xBar, loadout, UpgradeDB.DOUBLE_HEATSINKS));

		Item mlas = ItemDB.lookup("MEDIUM LASER");
		Item ac20 = ItemDB.lookup("AC/20");
		Item lrm5 = ItemDB.lookup("LRM 5");
		Item lrm15 = ItemDB.lookup("LRM 15");
		Item std250 = ItemDB.lookup("STD ENGINE 250");

		stack.pushAndApply(new OpAutoAddItem(loadout, xBar, mlas));
		assertTrue(loadout.getComponent(Location.RightArm).getItemsEquipped().contains(mlas));

		stack.pushAndApply(new OpAutoAddItem(loadout, xBar, mlas));
		assertTrue(loadout.getComponent(Location.LeftArm).getItemsEquipped().contains(mlas));

		stack.pushAndApply(new OpAutoAddItem(loadout, xBar, ac20));
		assertTrue(loadout.getComponent(Location.RightTorso).getItemsEquipped().contains(ac20));

		stack.pushAndApply(new OpAutoAddItem(loadout, xBar, lrm5));
		assertTrue(loadout.getComponent(Location.LeftTorso).getItemsEquipped().contains(lrm5));

		stack.pushAndApply(new OpAutoAddItem(loadout, xBar, lrm15));
		assertTrue(loadout.getComponent(Location.LeftTorso).getItemsEquipped().contains(lrm15));

		stack.pushAndApply(new OpAutoAddItem(loadout, xBar, std250));
		assertTrue(loadout.getComponent(Location.CenterTorso).getItemsEquipped().contains(std250));

		// Fill right arm
		stack.pushAndApply(new OpAutoAddItem(loadout, xBar, ItemDB.DHS));
		stack.pushAndApply(new OpAutoAddItem(loadout, xBar, ItemDB.DHS));
		assertTrue(loadout.getComponent(Location.RightArm).getItemsEquipped().contains(ItemDB.DHS));
		verify(xBar, times(1 + 2)).post(
				new ConfiguredComponentBase.ComponentMessage(loadout.getComponent(Location.RightArm), Type.ItemAdded));

		// Skips RA, RT, RL, HD, CT (too few slots) and places the item in LT
		stack.pushAndApply(new OpAutoAddItem(loadout, xBar, ItemDB.DHS));
		assertTrue(loadout.getComponent(Location.LeftTorso).getItemsEquipped().contains(ItemDB.DHS));

		// Skips RA (too few slots) and places the item in RT
		stack.pushAndApply(new OpAutoAddItem(loadout, xBar, ItemDB.BAP));
		assertTrue(loadout.getComponent(Location.RightTorso).getItemsEquipped().contains(ItemDB.BAP));
	}

	/**
	 * {@link OpAutoAddItem}shall prioritize engine slots for heat sinks
	 */
	@Test
	public void testAddItem_engineHS() {
		LoadoutStandard loadout = new LoadoutStandard((ChassisStandard) ChassisDB.lookup("AS7-D-DC"));

		Item std300 = ItemDB.lookup("STD ENGINE 300");
		stack.pushAndApply(new OpAutoAddItem(loadout, xBar, std300));
		assertTrue(loadout.getComponent(Location.CenterTorso).getItemsEquipped()
				.contains(ItemDB.lookup("STD ENGINE 300")));

		stack.pushAndApply(new OpAutoAddItem(loadout, xBar, ItemDB.SHS)); // Engine HS slot 1
		stack.pushAndApply(new OpAutoAddItem(loadout, xBar, ItemDB.SHS)); // Engine HS slot 2
		stack.pushAndApply(new OpAutoAddItem(loadout, xBar, ItemDB.SHS)); // Right arm

		verify(xBar, times(1 + 2)).post(
				new ConfiguredComponentBase.ComponentMessage(loadout.getComponent(Location.CenterTorso), Type.ItemAdded));
		assertTrue(loadout.getComponent(Location.CenterTorso).getItemsEquipped().contains(ItemDB.SHS)); // 1 remaining
		assertTrue(loadout.getComponent(Location.RightArm).getItemsEquipped().contains(ItemDB.SHS));
	}
}
