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
package lisong_mechlab.model.loadout.component;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.util.ArrayList;
import java.util.List;

import lisong_mechlab.model.chassi.ComponentStandard;
import lisong_mechlab.model.chassi.HardPoint;
import lisong_mechlab.model.chassi.HardPointType;
import lisong_mechlab.model.item.Engine;
import lisong_mechlab.model.item.HeatSink;
import lisong_mechlab.model.item.Item;
import lisong_mechlab.model.item.ItemDB;
import lisong_mechlab.util.ListArrayUtils;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Matchers;
import org.mockito.Mockito;

/**
 * Test suite for {@link ConfiguredComponentBase}.
 * 
 * @author Li Song
 */
public class ConfiguredComponentStandardTest extends ConfiguredComponentBaseTest {

	protected boolean			baydoors	= false;
	protected ComponentStandard	stdInternal;
	protected List<HardPoint>	hardPoints	= new ArrayList<>();

	@Before
	public void setup() {
		stdInternal = Mockito.mock(ComponentStandard.class);
		internal = stdInternal;
		Mockito.when(internal.isAllowed(Matchers.any(Item.class))).thenReturn(true);
	}

	@Override
	protected ConfiguredComponentStandard makeDefaultCUT() {
		Mockito.when(internal.getLocation()).thenReturn(location);
		Mockito.when(internal.getSlots()).thenReturn(slots);
		Mockito.when(internal.getFixedItemSlots()).thenReturn(internalFixedSlots);
		Mockito.when(internal.getFixedItems()).thenReturn(internalFixedItems);
		Mockito.when(internal.getArmorMax()).thenReturn(maxArmor);
		Mockito.when(stdInternal.getHardPoints()).thenReturn(hardPoints);
		Mockito.when(stdInternal.hasMissileBayDoors()).thenReturn(baydoors);
		return new ConfiguredComponentStandard(stdInternal, autoArmor);
	}

	/**
	 * We do not allow two C.A.S.E. in the same component as that is just bonkers.
	 */
	@Test
	public final void testCanAddItem_TwoCASE() {
		ConfiguredComponentBase cut = makeDefaultCUT();
		cut.addItem(ItemDB.CASE);
		assertFalse(cut.canAddItem(ItemDB.CASE));
	}

	/**
	 * Having C.A.S.E. does not prohibit other items.
	 */
	@Test
	public final void testCanAddItem_OneCASE() {
		ConfiguredComponentBase cut = makeDefaultCUT();
		cut.addItem(ItemDB.CASE);

		Item item = Mockito.mock(Item.class);
		Mockito.when(item.getHardpointType()).thenReturn(HardPointType.NONE);
		Mockito.when(item.getNumCriticalSlots()).thenReturn(1);

		assertTrue(cut.canAddItem(item));
	}

	/**
	 * C.A.S.E. is allowed (provided internal component allows it).
	 */
	@Test
	public final void testCanAddItem_CASEAllowed() {
		assertTrue(makeDefaultCUT().canAddItem(ItemDB.CASE));
	}

	@Test
	public void testCopyCtorEquals() {
		ConfiguredComponentStandard cut = makeDefaultCUT();
		assertEquals(cut, new ConfiguredComponentStandard(cut));
	}

	@Test
	public void testGetHardPointCount() {
		Mockito.when(stdInternal.getHardPointCount(HardPointType.ENERGY)).thenReturn(7);
		assertEquals(7, makeDefaultCUT().getHardPointCount(HardPointType.ENERGY));
	}

	@Test
	public void testGetHardPoints() {
		hardPoints.add(new HardPoint(HardPointType.BALLISTIC));
		hardPoints.add(new HardPoint(HardPointType.ECM));
		hardPoints.add(new HardPoint(HardPointType.ENERGY));
		hardPoints.add(new HardPoint(HardPointType.ENERGY));

		assertTrue(ListArrayUtils.equalsUnordered(hardPoints, new ArrayList<>(makeDefaultCUT().getHardPoints())));
	}

	@Test
	public void testHasMissileBayDoors() {
		assertEquals(baydoors, makeDefaultCUT().hasMissileBayDoors());
		baydoors = !baydoors;
		assertEquals(baydoors, makeDefaultCUT().hasMissileBayDoors());
	}

	@Test
	public void testIsAllowed_EngineHS() {
		Engine engine = Mockito.mock(Engine.class);
		Mockito.when(engine.getNumCriticalSlots()).thenReturn(slots);
		Mockito.when(engine.getNumHeatsinkSlots()).thenReturn(2);

		HeatSink heatSink = Mockito.mock(HeatSink.class);
		Mockito.when(heatSink.getNumCriticalSlots()).thenReturn(3);

		ConfiguredComponentStandard cut = makeDefaultCUT();
		cut.addItem(engine);

		assertTrue(cut.canAddItem(heatSink));
		cut.addItem(heatSink);
		assertTrue(cut.canAddItem(heatSink));
		cut.addItem(heatSink);
		assertFalse(cut.canAddItem(heatSink));
	}

	@Test
	public void testIsAllowed_NoHardpoint() {
		Item item = Mockito.mock(Item.class);
		Mockito.when(item.getNumCriticalSlots()).thenReturn(1);
		Mockito.when(item.getHardpointType()).thenReturn(HardPointType.ENERGY);

		assertFalse(makeDefaultCUT().canAddItem(item));
	}

	@Test
	public void testIsAllowed_HasHardpoint() {
		Item item = Mockito.mock(Item.class);
		Mockito.when(item.getNumCriticalSlots()).thenReturn(1);
		Mockito.when(item.getHardpointType()).thenReturn(HardPointType.ENERGY);

		Mockito.when(stdInternal.getHardPointCount(HardPointType.ENERGY)).thenReturn(1);
		hardPoints.add(new HardPoint(HardPointType.ENERGY));

		assertTrue(makeDefaultCUT().canAddItem(item));
	}

	@Test
	public void testIsAllowed_AllHardpointsTaken() {
		Item item = Mockito.mock(Item.class);
		Mockito.when(item.getNumCriticalSlots()).thenReturn(1);
		Mockito.when(item.getHardpointType()).thenReturn(HardPointType.ENERGY);

		Mockito.when(stdInternal.getHardPointCount(HardPointType.ENERGY)).thenReturn(1);
		hardPoints.add(new HardPoint(HardPointType.ENERGY));
		ConfiguredComponentStandard cut = makeDefaultCUT();
		cut.addItem(item);

		assertFalse(cut.canAddItem(item));
	}

}
