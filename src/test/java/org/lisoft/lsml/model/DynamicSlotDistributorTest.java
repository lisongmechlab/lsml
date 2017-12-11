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
package org.lisoft.lsml.model;

import static org.junit.Assert.assertEquals;
import static org.mockito.ArgumentMatchers.isA;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.HashMap;
import java.util.Map;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.lisoft.lsml.model.chassi.ComponentOmniMech;
import org.lisoft.lsml.model.chassi.Location;
import org.lisoft.lsml.model.loadout.ConfiguredComponentOmniMech;
import org.lisoft.lsml.model.loadout.ConfiguredComponentStandard;
import org.lisoft.lsml.model.loadout.LoadoutOmniMech;
import org.lisoft.lsml.model.loadout.LoadoutStandard;
import org.lisoft.lsml.model.upgrades.ArmourUpgrade;
import org.lisoft.lsml.model.upgrades.StructureUpgrade;
import org.lisoft.lsml.model.upgrades.UpgradesMutable;
import org.mockito.junit.MockitoJUnitRunner;

/**
 * Test suite for {@link DynamicSlotDistributor}.
 *
 * @author Li Song
 */
@RunWith(MockitoJUnitRunner.class)
public class DynamicSlotDistributorTest {
	/*
	 * MockLoadoutContainer mlc = new MockLoadoutContainer();
	 * List<ConfiguredComponent> priorityOrder; DynamicSlotDistributor cut;
	 */
	@Before
	public void setup() {
		// cut = new DynamicSlotDistributor(mlc.loadout);

		// Priority order: RA, RT, RL, HD, CT, LT, LL, LA
		// priorityOrder = Arrays.asList(mlc.ra, mlc.rt, mlc.rl, mlc.hd, mlc.ct, mlc.lt,
		// mlc.ll, mlc.la);
	}

	@Test
	public void testGetDynamicArmourSlots() {
		final LoadoutStandard loadout = mock(LoadoutStandard.class);
		final UpgradesMutable upgrades = mock(UpgradesMutable.class);
		final ArmourUpgrade armourType = mock(ArmourUpgrade.class);

		final int armourSlots = 13;

		final Map<Location, ConfiguredComponentStandard> components = new HashMap<>();
		when(loadout.getComponent(isA(Location.class))).then(args -> components.get(args.getArgument(0)));
		when(loadout.getUpgrades()).thenReturn(upgrades);
		when(upgrades.getArmour()).thenReturn(armourType);
		when(armourType.getDynamicSlots()).thenReturn(armourSlots);

		for (final Location location : Location.values()) {
			final ConfiguredComponentStandard component = mock(ConfiguredComponentStandard.class);
			components.put(location, component);
		}

		when(components.get(Location.RightArm).getSlotsFree()).thenReturn(1);
		when(components.get(Location.RightTorso).getSlotsFree()).thenReturn(1);
		when(components.get(Location.RightLeg).getSlotsFree()).thenReturn(3);
		when(components.get(Location.Head).getSlotsFree()).thenReturn(0); // 5 this far
		when(components.get(Location.CenterTorso).getSlotsFree()).thenReturn(5); // 10
		when(components.get(Location.LeftTorso).getSlotsFree()).thenReturn(6); // 16
		when(components.get(Location.LeftLeg).getSlotsFree()).thenReturn(2); // 18
		// when(components.get(Location.LeftArm).getSlotsFree()).thenReturn(1); // 19

		final DynamicSlotDistributor cut = new DynamicSlotDistributor(loadout);

		assertEquals(1, cut.getDynamicArmourSlots(Location.RightArm));
		assertEquals(1, cut.getDynamicArmourSlots(Location.RightTorso));
		assertEquals(3, cut.getDynamicArmourSlots(Location.RightLeg));
		assertEquals(0, cut.getDynamicArmourSlots(Location.Head));
		assertEquals(5, cut.getDynamicArmourSlots(Location.CenterTorso));
		assertEquals(3, cut.getDynamicArmourSlots(Location.LeftTorso));
		assertEquals(0, cut.getDynamicArmourSlots(Location.LeftLeg));
		assertEquals(0, cut.getDynamicArmourSlots(Location.LeftArm));
	}

	@Test
	public void testGetDynamicArmourSlots_Full() {
		final LoadoutStandard loadout = mock(LoadoutStandard.class);
		final UpgradesMutable upgrades = mock(UpgradesMutable.class);
		final ArmourUpgrade armourType = mock(ArmourUpgrade.class);

		final int slotsFreePerComponent = 2;
		final int armourSlots = slotsFreePerComponent * Location.values().length;

		final Map<Location, ConfiguredComponentStandard> components = new HashMap<>();
		when(loadout.getComponent(isA(Location.class))).then(args -> components.get(args.getArgument(0)));
		when(loadout.getUpgrades()).thenReturn(upgrades);
		when(upgrades.getArmour()).thenReturn(armourType);
		when(armourType.getDynamicSlots()).thenReturn(armourSlots);

		for (final Location location : Location.values()) {
			final ConfiguredComponentStandard component = mock(ConfiguredComponentStandard.class);
			when(component.getSlotsFree()).thenReturn(slotsFreePerComponent);
			components.put(location, component);
		}

		final DynamicSlotDistributor cut = new DynamicSlotDistributor(loadout);

		for (final Location location : Location.values()) {
			assertEquals(slotsFreePerComponent, cut.getDynamicArmourSlots(location));
		}
	}

	@Test
	public void testGetDynamicArmourSlots_OmniMech() {
		final LoadoutOmniMech loadout = mock(LoadoutOmniMech.class);
		final ConfiguredComponentOmniMech component = mock(ConfiguredComponentOmniMech.class);
		final ComponentOmniMech internal = mock(ComponentOmniMech.class);

		when(loadout.getComponent(Location.LeftArm)).thenReturn(component);
		when(component.getInternalComponent()).thenReturn(internal);
		when(internal.getDynamicArmourSlots()).thenReturn(12);

		// Execute + Verify
		final DynamicSlotDistributor cut = new DynamicSlotDistributor(loadout);

		assertEquals(12, cut.getDynamicArmourSlots(Location.LeftArm));
	}

	@Test
	public void testGetDynamicArmourSlots_StdArmour() {
		final LoadoutStandard loadout = mock(LoadoutStandard.class);
		final ConfiguredComponentStandard component = mock(ConfiguredComponentStandard.class);
		final UpgradesMutable upgrades = mock(UpgradesMutable.class);
		final ArmourUpgrade armourType = mock(ArmourUpgrade.class);

		when(loadout.getComponent(Location.LeftArm)).thenReturn(component);
		when(loadout.getUpgrades()).thenReturn(upgrades);
		when(upgrades.getArmour()).thenReturn(armourType);
		when(armourType.getDynamicSlots()).thenReturn(0);

		// Execute + Verify
		final DynamicSlotDistributor cut = new DynamicSlotDistributor(loadout);

		assertEquals(0, cut.getDynamicArmourSlots(Location.LeftArm));
	}

	@Test
	public void testGetDynamicStructureSlots() {
		final LoadoutStandard loadout = mock(LoadoutStandard.class);
		final UpgradesMutable upgrades = mock(UpgradesMutable.class);
		final StructureUpgrade structureType = mock(StructureUpgrade.class);
		final ArmourUpgrade armourType = mock(ArmourUpgrade.class);

		final int armourSlots = 3;
		final int structureSlots = 10;

		final Map<Location, ConfiguredComponentStandard> components = new HashMap<>();
		when(loadout.getComponent(isA(Location.class))).then(args -> components.get(args.getArgument(0)));
		when(loadout.getUpgrades()).thenReturn(upgrades);
		when(upgrades.getStructure()).thenReturn(structureType);
		when(upgrades.getArmour()).thenReturn(armourType);
		when(structureType.getExtraSlots()).thenReturn(structureSlots);
		when(armourType.getDynamicSlots()).thenReturn(armourSlots);

		for (final Location location : Location.values()) {
			final ConfiguredComponentStandard component = mock(ConfiguredComponentStandard.class);
			components.put(location, component);
		}

		when(components.get(Location.RightArm).getSlotsFree()).thenReturn(1);
		when(components.get(Location.RightTorso).getSlotsFree()).thenReturn(1);
		when(components.get(Location.RightLeg).getSlotsFree()).thenReturn(3);
		when(components.get(Location.Head).getSlotsFree()).thenReturn(0); // 5 this far
		when(components.get(Location.CenterTorso).getSlotsFree()).thenReturn(5); // 10
		when(components.get(Location.LeftTorso).getSlotsFree()).thenReturn(6); // 16
		when(components.get(Location.LeftLeg).getSlotsFree()).thenReturn(2); // 18
		// when(components.get(Location.LeftArm).getSlotsFree()).thenReturn(1); // 19

		final DynamicSlotDistributor cut = new DynamicSlotDistributor(loadout);

		assertEquals(0, cut.getDynamicStructureSlots(Location.RightArm));
		assertEquals(0, cut.getDynamicStructureSlots(Location.RightTorso));
		assertEquals(2, cut.getDynamicStructureSlots(Location.RightLeg));
		assertEquals(0, cut.getDynamicStructureSlots(Location.Head));
		assertEquals(5, cut.getDynamicStructureSlots(Location.CenterTorso));
		assertEquals(3, cut.getDynamicStructureSlots(Location.LeftTorso));
		assertEquals(0, cut.getDynamicStructureSlots(Location.LeftLeg));
		assertEquals(0, cut.getDynamicStructureSlots(Location.LeftArm));
	}

	@Test
	public void testGetDynamicStructureSlots_AllStructuresAndSomeArmorInOneComponent() {
		final LoadoutStandard loadout = mock(LoadoutStandard.class);
		final UpgradesMutable upgrades = mock(UpgradesMutable.class);
		final StructureUpgrade structureType = mock(StructureUpgrade.class);
		final ArmourUpgrade armourType = mock(ArmourUpgrade.class);

		final int armourSlots = 3;
		final int structureSlots = 3;

		final Map<Location, ConfiguredComponentStandard> components = new HashMap<>();
		when(loadout.getComponent(isA(Location.class))).then(args -> components.get(args.getArgument(0)));
		when(loadout.getUpgrades()).thenReturn(upgrades);
		when(upgrades.getStructure()).thenReturn(structureType);
		when(upgrades.getArmour()).thenReturn(armourType);
		when(structureType.getExtraSlots()).thenReturn(structureSlots);
		when(armourType.getDynamicSlots()).thenReturn(armourSlots);

		for (final Location location : Location.values()) {
			final ConfiguredComponentStandard component = mock(ConfiguredComponentStandard.class);
			components.put(location, component);
		}

		when(components.get(Location.RightArm).getSlotsFree()).thenReturn(10);

		final DynamicSlotDistributor cut = new DynamicSlotDistributor(loadout);

		assertEquals(3, cut.getDynamicStructureSlots(Location.RightArm));
	}

	@Test
	public void testGetDynamicStructureSlots_Full() {
		final LoadoutStandard loadout = mock(LoadoutStandard.class);
		final UpgradesMutable upgrades = mock(UpgradesMutable.class);
		final StructureUpgrade structureType = mock(StructureUpgrade.class);
		final ArmourUpgrade armourType = mock(ArmourUpgrade.class);

		final int slotsFreePerComponent = 2;
		final int structureSlots = slotsFreePerComponent * Location.values().length;

		final Map<Location, ConfiguredComponentStandard> components = new HashMap<>();
		when(loadout.getComponent(isA(Location.class))).then(args -> components.get(args.getArgument(0)));
		when(loadout.getUpgrades()).thenReturn(upgrades);
		when(upgrades.getStructure()).thenReturn(structureType);
		when(upgrades.getArmour()).thenReturn(armourType);
		when(structureType.getExtraSlots()).thenReturn(structureSlots);

		for (final Location location : Location.values()) {
			final ConfiguredComponentStandard component = mock(ConfiguredComponentStandard.class);
			when(component.getSlotsFree()).thenReturn(slotsFreePerComponent);
			components.put(location, component);
		}

		final DynamicSlotDistributor cut = new DynamicSlotDistributor(loadout);

		for (final Location location : Location.values()) {
			assertEquals(slotsFreePerComponent, cut.getDynamicStructureSlots(location));
		}
	}

	@Test
	public void testGetDynamicStructureSlots_OmniMech() {
		final LoadoutOmniMech loadout = mock(LoadoutOmniMech.class);
		final ConfiguredComponentOmniMech component = mock(ConfiguredComponentOmniMech.class);
		final ComponentOmniMech internal = mock(ComponentOmniMech.class);

		when(loadout.getComponent(Location.RightTorso)).thenReturn(component);
		when(component.getInternalComponent()).thenReturn(internal);
		when(internal.getDynamicStructureSlots()).thenReturn(12);

		// Execute + Verify
		final DynamicSlotDistributor cut = new DynamicSlotDistributor(loadout);

		assertEquals(12, cut.getDynamicStructureSlots(Location.RightTorso));
	}

	@Test
	public void testGetDynamicStructureSlots_StdStructure() {
		final LoadoutStandard loadout = mock(LoadoutStandard.class);
		final ConfiguredComponentStandard component = mock(ConfiguredComponentStandard.class);
		final UpgradesMutable upgrades = mock(UpgradesMutable.class);
		final StructureUpgrade structureType = mock(StructureUpgrade.class);
		when(loadout.getComponent(Location.LeftArm)).thenReturn(component);
		when(loadout.getUpgrades()).thenReturn(upgrades);
		when(upgrades.getStructure()).thenReturn(structureType);
		when(structureType.getExtraSlots()).thenReturn(0);

		// Execute + Verify
		final DynamicSlotDistributor cut = new DynamicSlotDistributor(loadout);

		assertEquals(0, cut.getDynamicStructureSlots(Location.LeftArm));
	}

	//
	// @Ignore
	// @Test
	// public void testGetDynamicArmourSlotsForComponent_Priority() {
	// when(mlc.upgrades.getStructure()).thenReturn(UpgradeDB.IS_STD_STRUCTURE);
	// when(mlc.upgrades.getArmour()).thenReturn(UpgradeDB.IS_FF_ARMOUR);
	//
	// when(mlc.ra.getSlotsFree()).thenReturn(12);
	// when(mlc.rt.getSlotsFree()).thenReturn(12);
	// when(mlc.rl.getSlotsFree()).thenReturn(12);
	// when(mlc.hd.getSlotsFree()).thenReturn(12);
	// when(mlc.ct.getSlotsFree()).thenReturn(12);
	// when(mlc.ll.getSlotsFree()).thenReturn(12);
	// when(mlc.lt.getSlotsFree()).thenReturn(12);
	// when(mlc.la.getSlotsFree()).thenReturn(12);
	//
	// for (final ConfiguredComponent part : priorityOrder) {
	// assertEquals(expectedStructure(14), slotsOccupied(part, 14),
	// cut.getDynamicArmourSlots(part.getInternalComponent().getLocation()));
	// }
	//
	// when(mlc.ra.getSlotsFree()).thenReturn(1);
	// for (final ConfiguredComponent part : priorityOrder) {
	// assertEquals(expectedStructure(14), slotsOccupied(part, 14),
	// cut.getDynamicArmourSlots(part.getInternalComponent().getLocation()));
	// }
	//
	// when(mlc.rt.getSlotsFree()).thenReturn(1);
	// for (final ConfiguredComponent part : priorityOrder) {
	// assertEquals(expectedStructure(14), slotsOccupied(part, 14),
	// cut.getDynamicArmourSlots(part.getInternalComponent().getLocation()));
	// }
	//
	// when(mlc.rl.getSlotsFree()).thenReturn(2);
	// for (final ConfiguredComponent part : priorityOrder) {
	// assertEquals(expectedStructure(14), slotsOccupied(part, 14),
	// cut.getDynamicArmourSlots(part.getInternalComponent().getLocation()));
	// }
	//
	// when(mlc.hd.getSlotsFree()).thenReturn(1);
	// for (final ConfiguredComponent part : priorityOrder) {
	// assertEquals(expectedStructure(14), slotsOccupied(part, 14),
	// cut.getDynamicArmourSlots(part.getInternalComponent().getLocation()));
	// }
	//
	// when(mlc.ct.getSlotsFree()).thenReturn(3);
	// for (final ConfiguredComponent part : priorityOrder) {
	// assertEquals(expectedStructure(14), slotsOccupied(part, 14),
	// cut.getDynamicArmourSlots(part.getInternalComponent().getLocation()));
	// }
	//
	// when(mlc.lt.getSlotsFree()).thenReturn(1);
	// for (final ConfiguredComponent part : priorityOrder) {
	// assertEquals(expectedStructure(14), slotsOccupied(part, 14),
	// cut.getDynamicArmourSlots(part.getInternalComponent().getLocation()));
	// }
	//
	// when(mlc.ll.getSlotsFree()).thenReturn(1);
	// for (final ConfiguredComponent part : priorityOrder) {
	// assertEquals(expectedStructure(14), slotsOccupied(part, 14),
	// cut.getDynamicArmourSlots(part.getInternalComponent().getLocation()));
	// }
	//
	// when(mlc.la.getSlotsFree()).thenReturn(1);
	// for (final ConfiguredComponent part : priorityOrder) {
	// assertEquals(expectedStructure(14), slotsOccupied(part, 14),
	// cut.getDynamicArmourSlots(part.getInternalComponent().getLocation()));
	// }
	// // Slot overflow, fail graciously, no exceptions thrown
	// }
	//
	// @Ignore
	// @Test
	// public void testGetDynamicStructureSlotsForComponent_NoUpgrades() {
	// when(mlc.upgrades.getStructure()).thenReturn(UpgradeDB.IS_STD_STRUCTURE);
	// when(mlc.upgrades.getArmour()).thenReturn(UpgradeDB.IS_STD_ARMOUR);
	//
	// when(mlc.ra.getSlotsFree()).thenReturn(12);
	// when(mlc.rt.getSlotsFree()).thenReturn(12);
	// when(mlc.rl.getSlotsFree()).thenReturn(12);
	// when(mlc.hd.getSlotsFree()).thenReturn(12);
	// when(mlc.ct.getSlotsFree()).thenReturn(12);
	// when(mlc.ll.getSlotsFree()).thenReturn(12);
	// when(mlc.lt.getSlotsFree()).thenReturn(12);
	// when(mlc.la.getSlotsFree()).thenReturn(12);
	//
	// assertEquals(0, cut.getDynamicStructureSlots(Location.RightArm));
	// assertEquals(0, cut.getDynamicStructureSlots(Location.RightTorso));
	// assertEquals(0, cut.getDynamicStructureSlots(Location.RightLeg));
	// assertEquals(0, cut.getDynamicStructureSlots(Location.Head));
	// assertEquals(0, cut.getDynamicStructureSlots(Location.CenterTorso));
	// assertEquals(0, cut.getDynamicStructureSlots(Location.LeftTorso));
	// assertEquals(0, cut.getDynamicStructureSlots(Location.LeftLeg));
	// assertEquals(0, cut.getDynamicStructureSlots(Location.LeftArm));
	//
	// assertEquals(0, cut.getDynamicArmourSlots(Location.RightArm));
	// assertEquals(0, cut.getDynamicArmourSlots(Location.RightTorso));
	// assertEquals(0, cut.getDynamicArmourSlots(Location.RightLeg));
	// assertEquals(0, cut.getDynamicArmourSlots(Location.Head));
	// assertEquals(0, cut.getDynamicArmourSlots(Location.CenterTorso));
	// assertEquals(0, cut.getDynamicArmourSlots(Location.LeftTorso));
	// assertEquals(0, cut.getDynamicArmourSlots(Location.LeftLeg));
	// assertEquals(0, cut.getDynamicArmourSlots(Location.LeftArm));
	// }
	//
	// @Ignore
	// @Test
	// public void testGetDynamicStructureSlotsForComponent_Priority() {
	// when(mlc.upgrades.getStructure()).thenReturn(UpgradeDB.IS_ES_STRUCTURE);
	// when(mlc.upgrades.getArmour()).thenReturn(UpgradeDB.IS_STD_ARMOUR);
	//
	// when(mlc.ra.getSlotsFree()).thenReturn(12);
	// when(mlc.rt.getSlotsFree()).thenReturn(12);
	// when(mlc.rl.getSlotsFree()).thenReturn(12);
	// when(mlc.hd.getSlotsFree()).thenReturn(12);
	// when(mlc.ct.getSlotsFree()).thenReturn(12);
	// when(mlc.ll.getSlotsFree()).thenReturn(12);
	// when(mlc.lt.getSlotsFree()).thenReturn(12);
	// when(mlc.la.getSlotsFree()).thenReturn(12);
	//
	// for (final ConfiguredComponent part : priorityOrder) {
	// assertEquals(expectedStructure(14), slotsOccupied(part, 14),
	// cut.getDynamicStructureSlots(part.getInternalComponent().getLocation()));
	// }
	//
	// when(mlc.ra.getSlotsFree()).thenReturn(1);
	// for (final ConfiguredComponent part : priorityOrder) {
	// assertEquals(expectedStructure(14), slotsOccupied(part, 14),
	// cut.getDynamicStructureSlots(part.getInternalComponent().getLocation()));
	// }
	//
	// when(mlc.rt.getSlotsFree()).thenReturn(1);
	// for (final ConfiguredComponent part : priorityOrder) {
	// assertEquals(expectedStructure(14), slotsOccupied(part, 14),
	// cut.getDynamicStructureSlots(part.getInternalComponent().getLocation()));
	// }
	//
	// when(mlc.rl.getSlotsFree()).thenReturn(2);
	// for (final ConfiguredComponent part : priorityOrder) {
	// assertEquals(expectedStructure(14), slotsOccupied(part, 14),
	// cut.getDynamicStructureSlots(part.getInternalComponent().getLocation()));
	// }
	//
	// when(mlc.hd.getSlotsFree()).thenReturn(1);
	// for (final ConfiguredComponent part : priorityOrder) {
	// assertEquals(expectedStructure(14), slotsOccupied(part, 14),
	// cut.getDynamicStructureSlots(part.getInternalComponent().getLocation()));
	// }
	//
	// when(mlc.ct.getSlotsFree()).thenReturn(3);
	// for (final ConfiguredComponent part : priorityOrder) {
	// assertEquals(expectedStructure(14), slotsOccupied(part, 14),
	// cut.getDynamicStructureSlots(part.getInternalComponent().getLocation()));
	// }
	//
	// when(mlc.lt.getSlotsFree()).thenReturn(1);
	// for (final ConfiguredComponent part : priorityOrder) {
	// assertEquals(expectedStructure(14), slotsOccupied(part, 14),
	// cut.getDynamicStructureSlots(part.getInternalComponent().getLocation()));
	// }
	//
	// when(mlc.ll.getSlotsFree()).thenReturn(1);
	// for (final ConfiguredComponent part : priorityOrder) {
	// assertEquals(expectedStructure(14), slotsOccupied(part, 14),
	// cut.getDynamicStructureSlots(part.getInternalComponent().getLocation()));
	// }
	//
	// when(mlc.la.getSlotsFree()).thenReturn(1);
	// for (final ConfiguredComponent part : priorityOrder) {
	// assertEquals(expectedStructure(14), slotsOccupied(part, 14),
	// cut.getDynamicStructureSlots(part.getInternalComponent().getLocation()));
	// }
	// // Slot overflow, fail graciously, no exceptions thrown
	// }
	//
	// /**
	// * Dynamic armour slots are distributed before dynamic structure (arbitrary
	// design decision).
	// */
	// @Ignore
	// @Test
	// public void testMixedArmourStructurePriority() {
	// when(mlc.upgrades.getStructure()).thenReturn(UpgradeDB.IS_ES_STRUCTURE);
	// when(mlc.upgrades.getArmour()).thenReturn(UpgradeDB.IS_FF_ARMOUR);
	//
	// when(mlc.ra.getSlotsFree()).thenReturn(4); // 4 armour
	// when(mlc.rt.getSlotsFree()).thenReturn(4);
	// when(mlc.rl.getSlotsFree()).thenReturn(4);
	// when(mlc.hd.getSlotsFree()).thenReturn(4); // 2 armour 2 structure
	// when(mlc.ct.getSlotsFree()).thenReturn(2);
	// when(mlc.lt.getSlotsFree()).thenReturn(4);
	// when(mlc.ll.getSlotsFree()).thenReturn(7); // 6 structure
	// when(mlc.la.getSlotsFree()).thenReturn(1); // 0 structure
	//
	// assertEquals(0, cut.getDynamicStructureSlots(Location.RightArm));
	// assertEquals(4, cut.getDynamicArmourSlots(Location.RightArm));
	//
	// assertEquals(0, cut.getDynamicStructureSlots(Location.RightTorso));
	// assertEquals(4, cut.getDynamicArmourSlots(Location.RightTorso));
	//
	// assertEquals(0, cut.getDynamicStructureSlots(Location.RightLeg));
	// assertEquals(4, cut.getDynamicArmourSlots(Location.RightLeg));
	//
	// assertEquals(2, cut.getDynamicStructureSlots(Location.Head));
	// assertEquals(2, cut.getDynamicArmourSlots(Location.Head));
	//
	// assertEquals(2, cut.getDynamicStructureSlots(Location.CenterTorso));
	// assertEquals(0, cut.getDynamicArmourSlots(Location.CenterTorso));
	//
	// assertEquals(4, cut.getDynamicStructureSlots(Location.LeftTorso));
	// assertEquals(0, cut.getDynamicArmourSlots(Location.LeftTorso));
	//
	// assertEquals(6, cut.getDynamicStructureSlots(Location.LeftLeg));
	// assertEquals(0, cut.getDynamicArmourSlots(Location.LeftLeg));
	//
	// assertEquals(0, cut.getDynamicStructureSlots(Location.LeftArm));
	// assertEquals(0, cut.getDynamicArmourSlots(Location.LeftArm));
	// }
	//
	// /**
	// * Calculates the cumulative number of slots that are free up until the
	// argument according to the priority order
	// of
	// * components.
	// *
	// * @param aPart
	// * @return
	// */
	// private int cumSlotsFree(ConfiguredComponent aPart) {
	// int i = priorityOrder.indexOf(aPart);
	// int sum = 0;
	// while (i > 0) {
	// i--;
	// sum += priorityOrder.get(i).getSlotsFree();
	// }
	// return sum;
	// }
	//
	// private String expectedStructure(int aSlotsTotal) {
	// final StringBuilder sb = new StringBuilder();
	// sb.append("{");
	// for (final ConfiguredComponent part : priorityOrder) {
	// sb.append(part).append(" = ");
	// sb.append(slotsOccupied(part, aSlotsTotal));
	// sb.append(", ");
	// }
	// sb.append("}");
	// return sb.toString();
	// }
	//
	// /**
	// * Calculates the number of cumulative slots that are occupied by dynamic
	// slots given the maximum number of
	// dynamic
	// * slots that can be distributed.
	// *
	// * @param aComponent
	// * @param aSlotsTotal
	// * @return
	// */
	// private int slotsOccupied(ConfiguredComponent aComponent, int aSlotsTotal) {
	// return Math.min(aComponent.getSlotsFree(), Math.max(0, aSlotsTotal -
	// cumSlotsFree(aComponent)));
	// }
}
