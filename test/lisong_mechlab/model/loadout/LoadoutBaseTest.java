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
package lisong_mechlab.model.loadout;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotSame;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import lisong_mechlab.model.chassi.ChassisBase;
import lisong_mechlab.model.chassi.HardPointType;
import lisong_mechlab.model.chassi.Location;
import lisong_mechlab.model.item.Engine;
import lisong_mechlab.model.item.Item;
import lisong_mechlab.model.item.ItemDB;
import lisong_mechlab.model.item.JumpJet;
import lisong_mechlab.model.item.Weapon;
import lisong_mechlab.model.loadout.component.ConfiguredComponentBase;
import lisong_mechlab.model.upgrades.ArmorUpgrade;
import lisong_mechlab.model.upgrades.HeatSinkUpgrade;
import lisong_mechlab.model.upgrades.StructureUpgrade;
import lisong_mechlab.util.ListArrayUtils;
import lisong_mechlab.util.MessageXBar;

import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.mockito.Mockito;

/**
 * Test suite for {@link LoadoutBase}
 * 
 * @author Emily Björk
 */
public abstract class LoadoutBaseTest {
	protected int						mass				= 75;
	protected String					chassisName			= "chassis";
	protected String					chassisShortName	= "short chassis";
	protected MessageXBar				xBar;
	protected ChassisBase				chassis;
	protected ConfiguredComponentBase[]	components;

	protected int						slots				= 10;
	protected HeatSinkUpgrade			heatSinks;
	protected StructureUpgrade			structure;
	protected ArmorUpgrade				armor;

	@Before
	public void setup() {
		xBar = Mockito.mock(MessageXBar.class);
		structure = Mockito.mock(StructureUpgrade.class);
		armor = Mockito.mock(ArmorUpgrade.class);
		heatSinks = Mockito.mock(HeatSinkUpgrade.class);
	}

	protected abstract LoadoutBase<?> makeDefaultCUT();

	@Test
	public final void testToString() throws Exception {
		LoadoutBase<?> cut = makeDefaultCUT();
		String name = "mamboyeeya";
		cut.rename(name);

		assertEquals(name + " (" + chassis.getNameShort() + ")", cut.toString());
	}

	/**
	 * items() shall return an {@link Iterable} that will include all {@link Item}s on the loadout.
	 */
	@Test
	public final void testItems_AllItemsAccounted() {
		List<Item> fixed0 = new ArrayList<>();
		List<Item> fixed1 = new ArrayList<>();
		List<Item> fixed2 = new ArrayList<>();
		List<Item> fixed3 = new ArrayList<>();
		List<Item> fixed4 = new ArrayList<>();
		List<Item> fixed5 = new ArrayList<>();
		List<Item> fixed6 = new ArrayList<>();
		List<Item> fixed7 = new ArrayList<>();
		List<Item> equipped0 = new ArrayList<>();
		List<Item> equipped1 = new ArrayList<>();
		List<Item> equipped2 = new ArrayList<>();
		List<Item> equipped3 = new ArrayList<>();
		List<Item> equipped4 = new ArrayList<>();
		List<Item> equipped5 = new ArrayList<>();
		List<Item> equipped6 = new ArrayList<>();
		List<Item> equipped7 = new ArrayList<>();

		// Non-sense, unique items
		fixed0.add(ItemDB.BAP);
		fixed1.add(ItemDB.CASE);
		fixed2.add(ItemDB.ECM);
		fixed3.add(ItemDB.AMS);
		fixed4.add(ItemDB.C_AMS);
		fixed5.add(ItemDB.DHS);
		fixed6.add(ItemDB.HA);
		fixed7.add(ItemDB.LAA);

		equipped0.add(ItemDB.SHS);
		equipped1.add(ItemDB.UAA);
		equipped2.add(ItemDB.lookup("AC/2"));
		equipped3.add(ItemDB.lookup("AC/5"));
		equipped4.add(ItemDB.lookup("AC/10"));
		equipped5.add(ItemDB.lookup("AC/20"));
		equipped6.add(ItemDB.lookup("SMALL LASER"));
		equipped7.add(ItemDB.lookup("MEDIUM LASER"));
		equipped7.add(ItemDB.lookup("LARGE LASER"));

		List<Item> expected = new ArrayList<>();
		expected.addAll(fixed0);
		expected.addAll(fixed1);
		expected.addAll(fixed2);
		expected.addAll(fixed3);
		expected.addAll(fixed4);
		expected.addAll(fixed5);
		expected.addAll(fixed6);
		expected.addAll(fixed7);
		expected.addAll(equipped0);
		expected.addAll(equipped1);
		expected.addAll(equipped2);
		expected.addAll(equipped3);
		expected.addAll(equipped4);
		expected.addAll(equipped5);
		expected.addAll(equipped6);
		expected.addAll(equipped7);

		Mockito.when(components[0].getItemsFixed()).thenReturn(fixed0);
		Mockito.when(components[0].getItemsEquipped()).thenReturn(equipped0);
		Mockito.when(components[1].getItemsFixed()).thenReturn(fixed1);
		Mockito.when(components[1].getItemsEquipped()).thenReturn(equipped1);
		Mockito.when(components[2].getItemsFixed()).thenReturn(fixed2);
		Mockito.when(components[2].getItemsEquipped()).thenReturn(equipped2);
		Mockito.when(components[3].getItemsFixed()).thenReturn(fixed3);
		Mockito.when(components[3].getItemsEquipped()).thenReturn(equipped3);
		Mockito.when(components[4].getItemsFixed()).thenReturn(fixed4);
		Mockito.when(components[4].getItemsEquipped()).thenReturn(equipped4);
		Mockito.when(components[5].getItemsFixed()).thenReturn(fixed5);
		Mockito.when(components[5].getItemsEquipped()).thenReturn(equipped5);
		Mockito.when(components[6].getItemsFixed()).thenReturn(fixed6);
		Mockito.when(components[6].getItemsEquipped()).thenReturn(equipped6);
		Mockito.when(components[7].getItemsFixed()).thenReturn(fixed7);
		Mockito.when(components[7].getItemsEquipped()).thenReturn(equipped7);

		List<Item> ans = new ArrayList<>();
		for (Item item : makeDefaultCUT().items()) {
			ans.add(item);
		}

		assertTrue(ListArrayUtils.equalsUnordered(expected, ans));
	}

	/**
	 * items() shall function correctly even if there are no items on the loadout.
	 */
	@Test
	public final void testItems_Empty() {
		List<Item> empty = new ArrayList<>();
		List<Item> expected = new ArrayList<>();

		for (int i = 0; i < 8; ++i) {
			Mockito.when(components[i].getItemsFixed()).thenReturn(empty);
			Mockito.when(components[i].getItemsEquipped()).thenReturn(empty);
		}

		List<Item> ans = new ArrayList<>();
		for (Item item : makeDefaultCUT().items()) {
			ans.add(item);
		}

		assertTrue(ListArrayUtils.equalsUnordered(expected, ans));
	}

	/**
	 * items() shall function correctly even if no item is included in filter.
	 */
	@Test
	public final void testItems_FilterEmpty() {
		List<Item> empty = new ArrayList<>();
		empty.add(ItemDB.SHS);
		List<Item> expected = new ArrayList<>();

		for (int i = 0; i < 8; ++i) {
			Mockito.when(components[i].getItemsFixed()).thenReturn(empty);
			Mockito.when(components[i].getItemsEquipped()).thenReturn(empty);
		}

		List<Item> ans = new ArrayList<>();
		for (Item item : makeDefaultCUT().items(Weapon.class)) {
			ans.add(item);
		}

		assertTrue(ListArrayUtils.equalsUnordered(expected, ans));
	}

	/**
	 * items() shall return an {@link Iterable} that will include all {@link Item}s on the loadout.
	 */
	@Test
	public final void testItems_Filter() {
		List<Item> fixed0 = new ArrayList<>();
		List<Item> fixed1 = new ArrayList<>();
		List<Item> fixed2 = new ArrayList<>();
		List<Item> fixed3 = new ArrayList<>();
		List<Item> fixed4 = new ArrayList<>();
		List<Item> fixed5 = new ArrayList<>();
		List<Item> fixed6 = new ArrayList<>();
		List<Item> fixed7 = new ArrayList<>();
		List<Item> equipped0 = new ArrayList<>();
		List<Item> equipped1 = new ArrayList<>();
		List<Item> equipped2 = new ArrayList<>();
		List<Item> equipped3 = new ArrayList<>();
		List<Item> equipped4 = new ArrayList<>();
		List<Item> equipped5 = new ArrayList<>();
		List<Item> equipped6 = new ArrayList<>();
		List<Item> equipped7 = new ArrayList<>();

		// Non-sense, unique items
		fixed0.add(ItemDB.BAP);
		fixed1.add(ItemDB.lookup("AC/2"));
		fixed2.add(ItemDB.ECM);
		fixed3.add(ItemDB.AMS);
		fixed4.add(ItemDB.lookup("SMALL LASER"));
		fixed5.add(ItemDB.DHS);
		fixed6.add(ItemDB.HA);
		fixed7.add(ItemDB.LAA);

		equipped0.add(ItemDB.SHS);
		equipped1.add(ItemDB.UAA);
		equipped2.add(ItemDB.CASE);
		equipped3.add(ItemDB.lookup("AC/5"));
		equipped4.add(ItemDB.lookup("AC/10"));
		equipped5.add(ItemDB.lookup("AC/20"));
		equipped6.add(ItemDB.C_AMS);
		equipped7.add(ItemDB.lookup("MEDIUM LASER"));
		equipped7.add(ItemDB.lookup("LARGE LASER"));

		List<Item> expected = new ArrayList<>();
		expected.add(ItemDB.lookup("AC/2"));
		expected.add(ItemDB.AMS);
		expected.add(ItemDB.lookup("SMALL LASER"));
		expected.add(ItemDB.lookup("AC/5"));
		expected.add(ItemDB.lookup("AC/10"));
		expected.add(ItemDB.lookup("AC/20"));
		expected.add(ItemDB.C_AMS);
		expected.add(ItemDB.lookup("MEDIUM LASER"));
		expected.add(ItemDB.lookup("LARGE LASER"));

		Mockito.when(components[0].getItemsFixed()).thenReturn(fixed0);
		Mockito.when(components[0].getItemsEquipped()).thenReturn(equipped0);
		Mockito.when(components[1].getItemsFixed()).thenReturn(fixed1);
		Mockito.when(components[1].getItemsEquipped()).thenReturn(equipped1);
		Mockito.when(components[2].getItemsFixed()).thenReturn(fixed2);
		Mockito.when(components[2].getItemsEquipped()).thenReturn(equipped2);
		Mockito.when(components[3].getItemsFixed()).thenReturn(fixed3);
		Mockito.when(components[3].getItemsEquipped()).thenReturn(equipped3);
		Mockito.when(components[4].getItemsFixed()).thenReturn(fixed4);
		Mockito.when(components[4].getItemsEquipped()).thenReturn(equipped4);
		Mockito.when(components[5].getItemsFixed()).thenReturn(fixed5);
		Mockito.when(components[5].getItemsEquipped()).thenReturn(equipped5);
		Mockito.when(components[6].getItemsFixed()).thenReturn(fixed6);
		Mockito.when(components[6].getItemsEquipped()).thenReturn(equipped6);
		Mockito.when(components[7].getItemsFixed()).thenReturn(fixed7);
		Mockito.when(components[7].getItemsEquipped()).thenReturn(equipped7);

		List<Item> ans = new ArrayList<>();
		for (Weapon item : makeDefaultCUT().items(Weapon.class)) {
			ans.add(item);
		}

		assertTrue(ListArrayUtils.equalsUnordered(expected, ans));
	}

	@Test
	public final void testGetArmor() throws Exception {
		Mockito.when(components[0].getArmorTotal()).thenReturn(2);
		Mockito.when(components[3].getArmorTotal()).thenReturn(3);
		Mockito.when(components[5].getArmorTotal()).thenReturn(7);

		assertEquals(12, makeDefaultCUT().getArmor());
	}

	@Test
	public final void testGetEfficiencies() throws Exception {
		assertNotSame(makeDefaultCUT(), makeDefaultCUT()); // Unique

		LoadoutBase<?> cut = makeDefaultCUT();
		assertSame(cut.getEfficiencies(), cut.getEfficiencies()); // Stable
	}

	@Test
	public final void testGetMassFreeMass() throws Exception {
		Mockito.when(components[0].getItemMass()).thenReturn(2.0);
		Mockito.when(components[3].getItemMass()).thenReturn(3.0);
		Mockito.when(components[5].getItemMass()).thenReturn(7.0);

		Mockito.when(components[0].getArmorTotal()).thenReturn(10);
		Mockito.when(components[3].getArmorTotal()).thenReturn(13);
		Mockito.when(components[5].getArmorTotal()).thenReturn(19);

		Mockito.when(structure.getStructureMass(chassis)).thenReturn(7.3);
		Mockito.when(armor.getArmorMass(42)).thenReturn(4.6);

		assertEquals(23.9, makeDefaultCUT().getMass(), 1E-9);

		Mockito.verify(armor).getArmorMass(42);
		Mockito.verify(structure).getStructureMass(chassis);

		assertEquals(mass - 23.9, makeDefaultCUT().getFreeMass(), 1E-9);
	}

	@Test
	public final void testGetChassis() throws Exception {
		assertSame(chassis, makeDefaultCUT().getChassis());
	}

	@Test
	public final void testGetName() throws Exception {
		assertEquals(chassisShortName, makeDefaultCUT().getName());
	}

	@Test
	public final void testGetComponent() throws Exception {
		for (Location loc : Location.values()) {
			assertSame(components[loc.ordinal()], makeDefaultCUT().getComponent(loc));
		}
	}

	@Test(expected = UnsupportedOperationException.class)
	public final void testGetComponents_Immutable() throws Exception {
		makeDefaultCUT().getComponents().add(null);
	}

	@Test
	public final void testGetComponents() throws Exception {
		Collection<?> ans = makeDefaultCUT().getComponents();
		assertEquals(components.length, ans.size());

		for (ConfiguredComponentBase component : components) {
			assertTrue(ans.contains(component));
		}
	}

	@Test
	public final void testGetHardpointsCount() throws Exception {
		Mockito.when(components[0].getHardPointCount(HardPointType.ENERGY)).thenReturn(2);
		Mockito.when(components[1].getHardPointCount(HardPointType.ENERGY)).thenReturn(3);
		Mockito.when(components[1].getHardPointCount(HardPointType.BALLISTIC)).thenReturn(5);
		Mockito.when(components[2].getHardPointCount(HardPointType.MISSILE)).thenReturn(7);

		assertEquals(5, makeDefaultCUT().getHardpointsCount(HardPointType.ENERGY));
		assertEquals(5, makeDefaultCUT().getHardpointsCount(HardPointType.BALLISTIC));
		assertEquals(7, makeDefaultCUT().getHardpointsCount(HardPointType.MISSILE));
	}

	@Ignore
	// Needs to be tested in subclasses due to handling of engines.
	@Test
	public final void testGetHeatsinksCount() throws Exception {
		List<Item> empty = new ArrayList<>();
		List<Item> fixed1 = new ArrayList<>();
		List<Item> fixed2 = new ArrayList<>();
		List<Item> equipped1 = new ArrayList<>();
		List<Item> equipped2 = new ArrayList<>();

		Engine engine = Mockito.mock(Engine.class);
		Mockito.when(engine.getNumInternalHeatsinks()).thenReturn(3);

		fixed1.add(ItemDB.BAP);
		fixed1.add(ItemDB.CASE);

		fixed2.add(ItemDB.SHS);

		equipped1.add(ItemDB.AMS);
		equipped1.add(ItemDB.DHS);
		equipped1.add(engine);

		equipped2.add(ItemDB.DHS);
		equipped2.add(ItemDB.DHS);
		equipped2.add(ItemDB.DHS);

		Mockito.when(components[0].getItemsFixed()).thenReturn(fixed1);
		Mockito.when(components[0].getItemsEquipped()).thenReturn(equipped1);
		Mockito.when(components[1].getItemsFixed()).thenReturn(empty);
		Mockito.when(components[1].getItemsEquipped()).thenReturn(empty);
		Mockito.when(components[2].getItemsFixed()).thenReturn(empty);
		Mockito.when(components[2].getItemsEquipped()).thenReturn(equipped2);
		Mockito.when(components[3].getItemsFixed()).thenReturn(fixed2);
		Mockito.when(components[3].getItemsEquipped()).thenReturn(empty);

		for (int i = 4; i < Location.values().length; ++i) {
			Mockito.when(components[i].getItemsFixed()).thenReturn(empty);
			Mockito.when(components[i].getItemsEquipped()).thenReturn(empty);
		}

		assertEquals(8, makeDefaultCUT().getHeatsinksCount());
	}

	@Test
	public final void testGetJumpJetCount() throws Exception {
		List<Item> empty = new ArrayList<>();
		List<Item> fixed1 = new ArrayList<>();
		List<Item> fixed2 = new ArrayList<>();
		List<Item> equipped1 = new ArrayList<>();
		List<Item> equipped2 = new ArrayList<>();

		JumpJet jj = Mockito.mock(JumpJet.class);

		fixed1.add(ItemDB.BAP);
		fixed1.add(jj);

		fixed2.add(ItemDB.SHS);

		equipped1.add(ItemDB.AMS);
		equipped1.add(jj);
		equipped1.add(jj);

		equipped2.add(ItemDB.DHS);
		equipped2.add(ItemDB.DHS);
		equipped2.add(ItemDB.DHS);

		Mockito.when(components[0].getItemsFixed()).thenReturn(fixed1);
		Mockito.when(components[0].getItemsEquipped()).thenReturn(equipped1);
		Mockito.when(components[1].getItemsFixed()).thenReturn(empty);
		Mockito.when(components[1].getItemsEquipped()).thenReturn(empty);
		Mockito.when(components[2].getItemsFixed()).thenReturn(empty);
		Mockito.when(components[2].getItemsEquipped()).thenReturn(equipped2);
		Mockito.when(components[3].getItemsFixed()).thenReturn(fixed2);
		Mockito.when(components[3].getItemsEquipped()).thenReturn(empty);

		for (int i = 4; i < Location.values().length; ++i) {
			Mockito.when(components[i].getItemsFixed()).thenReturn(empty);
			Mockito.when(components[i].getItemsEquipped()).thenReturn(empty);
		}

		assertEquals(3, makeDefaultCUT().getJumpJetCount());
	}
}
