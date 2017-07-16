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
package org.lisoft.lsml.model.loadout;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.lisoft.lsml.command.CmdAddModule;
import org.lisoft.lsml.command.CmdSetGuidanceType;
import org.lisoft.lsml.command.CmdSetOmniPod;
import org.lisoft.lsml.model.chassi.ChassisOmniMech;
import org.lisoft.lsml.model.chassi.ComponentOmniMech;
import org.lisoft.lsml.model.chassi.HardPointType;
import org.lisoft.lsml.model.chassi.Location;
import org.lisoft.lsml.model.chassi.MovementProfile;
import org.lisoft.lsml.model.chassi.OmniPod;
import org.lisoft.lsml.model.chassi.OmniPodSet;
import org.lisoft.lsml.model.database.ChassisDB;
import org.lisoft.lsml.model.database.ConsumableDB;
import org.lisoft.lsml.model.database.ItemDB;
import org.lisoft.lsml.model.database.OmniPodDB;
import org.lisoft.lsml.model.database.UpgradeDB;
import org.lisoft.lsml.model.item.Engine;
import org.lisoft.lsml.model.item.Item;
import org.lisoft.lsml.model.loadout.EquipResult.EquipResultType;
import org.lisoft.lsml.model.modifiers.Modifier;
import org.lisoft.lsml.model.upgrades.Upgrades;
import org.lisoft.lsml.util.CommandStack;

/**
 * Test suite for {@link LoadoutOmniMech}.
 *
 * @author Emily Björk
 */
@SuppressWarnings("javadoc")
public class LoadoutOmniMechTest extends LoadoutTest {
    protected Map<Location, Collection<Modifier>> podQuirks = new HashMap<>();
    protected OmniPod[] pods = new OmniPod[Location.values().length];

    protected Engine engine;
    private ChassisOmniMech chassisOmni;
    private MovementProfile movementProfile;
    private final LoadoutFactory loadoutFactory = new DefaultLoadoutFactory();

    @Override
    @Before
    public void setup() {
        super.setup();
        chassisOmni = mock(ChassisOmniMech.class);
        chassis = chassisOmni;
        engine = mock(Engine.class);
        movementProfile = mock(MovementProfile.class);

        internals = new ComponentOmniMech[Location.values().length];
        components = new ConfiguredComponentOmniMech[Location.values().length];
        for (final Location location : Location.values()) {
            final int loc = location.ordinal();
            podQuirks.put(location, new ArrayList<Modifier>());
            pods[loc] = mock(OmniPod.class);
            internals[loc] = mock(ComponentOmniMech.class);
            components[loc] = mock(ConfiguredComponentOmniMech.class);

            when(pods[loc].getQuirks()).thenReturn(podQuirks.get(location));
            when(components[loc].getInternalComponent()).thenReturn(internals[loc]);
            when(getComponent(location).getOmniPod()).thenReturn(pods[loc]);
        }
    }

    @Test
    public void testCanEquip_NoEngine() throws Exception {
        final Engine item = makeTestItem(0.0, 0, HardPointType.NONE, true, true, true, Engine.class);
        assertEquals(EquipResult.make(EquipResultType.EngineAlreadyEquipped), makeDefaultCUT().canEquipDirectly(item));
    }

    /**
     * {@link #equals(Object)} shall return <code>false</code> if the chassis differ.
     *
     * @throws Exception
     */
    @Test
    public final void testEquals_Chassis() throws Exception {
        final ChassisOmniMech dwfa = (ChassisOmniMech) ChassisDB.lookup("DWF-A");
        final ChassisOmniMech dwfb = (ChassisOmniMech) ChassisDB.lookup("DWF-B");
        final Loadout cut = loadoutFactory.produceEmpty(dwfa);
        final Loadout cut1 = loadoutFactory.produceEmpty(dwfb);

        cut.setName("fooba");
        cut1.setName("fooba");

        assertNotEquals(cut, cut1);
    }

    /**
     * {@link #equals(Object)} shall return <code>false</code> if the objects have differing components.
     *
     * @throws Exception
     */
    @Test
    public final void testEquals_Components() throws Exception {
        final LoadoutOmniMech cut = (LoadoutOmniMech) loadoutFactory.produceEmpty(ChassisDB.lookup("DWF-A"));
        final LoadoutOmniMech cut1 = (LoadoutOmniMech) loadoutFactory.produceEmpty(ChassisDB.lookup("DWF-A"));

        final CommandStack stack = new CommandStack(0);
        stack.pushAndApply(new CmdSetOmniPod(null, cut, cut.getComponent(Location.LeftArm),
                OmniPodDB.lookupStock((ChassisOmniMech) ChassisDB.lookup("DWF-B"), Location.LeftArm).get()));

        assertNotEquals(cut, cut1);
    }

    /**
     * {@link #equals(Object)} shall return <code>true</code> if the objects have different efficiencies. (Efficiens are
     * not part of the loadout per say)
     */
    @Ignore
    @Test
    public final void testEquals_Efficiencies() {
        final LoadoutOmniMech cut = (LoadoutOmniMech) loadoutFactory.produceEmpty(ChassisDB.lookup("DWF-A"));
        final LoadoutOmniMech cut1 = (LoadoutOmniMech) loadoutFactory.produceEmpty(ChassisDB.lookup("DWF-A"));

        // cut.getEfficiencies().setEfficiency(MechEfficiencyType.ANCHORTURN, true, null);
        // cut1.getEfficiencies().setEfficiency(MechEfficiencyType.ANCHORTURN, false, null);

        assertEquals(cut, cut1);
    }

    /**
     * {@link #equals(Object)} shall return <code>true</code> if the objects are equal.
     *
     * @throws Exception
     */
    @Test
    public final void testEquals_Equal() throws Exception {
        final ChassisOmniMech dwfa = (ChassisOmniMech) ChassisDB.lookup("DWF-A");
        final Loadout cut = loadoutFactory.produceStock(dwfa);
        final Loadout cut1 = loadoutFactory.produceStock(dwfa);
        assertEquals(cut, cut1);
    }

    /**
     * {@link #equals(Object)} shall return <code>false</code> if the objects have differing modules.
     *
     * @throws Exception
     */
    @Test
    public final void testEquals_Modules() throws Exception {
        final LoadoutOmniMech cut = (LoadoutOmniMech) loadoutFactory.produceEmpty(ChassisDB.lookup("DWF-A"));
        final LoadoutOmniMech cut1 = (LoadoutOmniMech) loadoutFactory.produceEmpty(ChassisDB.lookup("DWF-A"));

        final CommandStack stack = new CommandStack(0);
        stack.pushAndApply(new CmdAddModule(null, cut, ConsumableDB.lookup("ADVANCED UAV")));

        assertNotEquals(cut, cut1);
    }

    /**
     * {@link #equals(Object)} shall return <code>false</code> if the objects have different names.
     *
     * @throws Exception
     */
    @Test
    public final void testEquals_Name() throws Exception {
        final LoadoutOmniMech cut = (LoadoutOmniMech) loadoutFactory.produceEmpty(ChassisDB.lookup("DWF-A"));
        final LoadoutOmniMech cut1 = (LoadoutOmniMech) loadoutFactory.produceEmpty(ChassisDB.lookup("DWF-A"));

        cut.setName("fooba");

        assertNotEquals(cut, cut1);
    }

    /**
     * {@link #equals(Object)} shall always return <code>true</code> for the same object.
     */
    @Test
    public final void testEquals_Self() {
        final LoadoutOmniMech cut = new LoadoutOmniMech((ConfiguredComponentOmniMech[]) components,
                (ChassisOmniMech) ChassisDB.lookup("DWF-A"), upgrades, weaponGroups);

        assertEquals(cut, cut);
    }

    /**
     * {@link #equals(Object)} shall return <code>false</code> if the objects have different upgrades.
     *
     * @throws Exception
     */
    @Test
    public final void testEquals_Upgrades() throws Exception {
        final LoadoutOmniMech cut = (LoadoutOmniMech) loadoutFactory.produceEmpty(ChassisDB.lookup("DWF-A"));
        final LoadoutOmniMech cut1 = (LoadoutOmniMech) loadoutFactory.produceEmpty(ChassisDB.lookup("DWF-A"));

        final CommandStack stack = new CommandStack(0);
        stack.pushAndApply(new CmdSetGuidanceType(null, cut1, UpgradeDB.ARTEMIS_IV));
        assertNotEquals(cut, cut1);
    }

    /**
     * {@link #equals(Object)} shall return <code>false</code> if the objects are not of the same loadout type.
     */
    @Test
    public final void testEquals_WrongType() {
        final Loadout cut = loadoutFactory.produceEmpty(ChassisDB.lookup("DWF-A"));
        final Loadout cut1 = loadoutFactory.produceEmpty(ChassisDB.lookup("JR7-F"));

        assertNotEquals(cut, cut1);
    }

    @Test
    public final void testGetConsumablesMax() throws Exception {
        when(chassisOmni.getConsumablesMax()).thenReturn(1);
        assertEquals(1, makeDefaultCUT().getConsumablesMax());
    }

    @Test
    public final void testGetEngine() throws Exception {
        assertSame(engine, makeDefaultCUT().getEngine());
    }

    @Test
    public final void testGetHeatSinksCount() throws Exception {
        final List<Item> empty = new ArrayList<>();
        final List<Item> fixed1 = new ArrayList<>();
        final List<Item> fixed2 = new ArrayList<>();
        final List<Item> equipped1 = new ArrayList<>();
        final List<Item> equipped2 = new ArrayList<>();

        when(engine.getNumInternalHeatsinks()).thenReturn(3);

        fixed1.add(ItemDB.BAP);
        fixed1.add(ItemDB.CASE);

        fixed2.add(ItemDB.SHS);

        equipped1.add(ItemDB.AMS);
        equipped1.add(ItemDB.DHS);

        equipped2.add(ItemDB.DHS);
        equipped2.add(ItemDB.DHS);
        equipped2.add(ItemDB.DHS);

        when(components[0].getItemsFixed()).thenReturn(fixed1);
        when(components[0].getItemsEquipped()).thenReturn(equipped1); // 1 DHS
        when(components[1].getItemsFixed()).thenReturn(empty);
        when(components[1].getItemsEquipped()).thenReturn(empty);
        when(components[2].getItemsFixed()).thenReturn(empty);
        when(components[2].getItemsEquipped()).thenReturn(equipped2); // 3 DHS
        when(components[3].getItemsFixed()).thenReturn(fixed2); // 1 SHS
        when(components[3].getItemsEquipped()).thenReturn(empty);

        for (int i = 4; i < Location.values().length; ++i) {
            when(components[i].getItemsFixed()).thenReturn(empty);
            when(components[i].getItemsEquipped()).thenReturn(empty);
        }

        assertEquals(8, makeDefaultCUT().getHeatsinksCount());
    }

    @Test
    public final void testGetJumpJetsMax() throws Exception {
        when(chassisOmni.getFixedJumpJets()).thenReturn(7);

        when(pods[3].getJumpJetsMax()).thenReturn(2);
        when(pods[6].getJumpJetsMax()).thenReturn(3);
        when(pods[7].getJumpJetsMax()).thenReturn(5);

        assertEquals(17, makeDefaultCUT().getJumpJetsMax());
    }

    @Test
    public final void testGetModifiersOmniPodQuirks() throws Exception {
        final OmniPodSet setA = mock(OmniPodSet.class);
        final OmniPodSet setB = mock(OmniPodSet.class);

        when(pods[Location.LeftArm.ordinal()].getOmniPodSet()).thenReturn(setA);
        when(pods[Location.LeftTorso.ordinal()].getOmniPodSet()).thenReturn(setA);
        when(pods[Location.LeftLeg.ordinal()].getOmniPodSet()).thenReturn(setA);
        when(pods[Location.Head.ordinal()].getOmniPodSet()).thenReturn(setA);
        when(pods[Location.CenterTorso.ordinal()].getOmniPodSet()).thenReturn(setA);
        when(pods[Location.RightTorso.ordinal()].getOmniPodSet()).thenReturn(setA);
        when(pods[Location.RightLeg.ordinal()].getOmniPodSet()).thenReturn(setB);
        when(pods[Location.RightArm.ordinal()].getOmniPodSet()).thenReturn(setB);

        final Modifier modifier1 = mock(Modifier.class);
        final Modifier modifier2 = mock(Modifier.class);
        final Modifier modifier3 = mock(Modifier.class);
        final Modifier modifier4 = mock(Modifier.class);
        final Modifier modifier5 = mock(Modifier.class);
        final Modifier modifier6 = mock(Modifier.class);
        final Modifier modifier7 = mock(Modifier.class);
        final Modifier modifier8 = mock(Modifier.class);
        final Modifier modifier9 = mock(Modifier.class);

        podQuirks.get(Location.LeftArm).add(modifier1);
        podQuirks.get(Location.LeftTorso).add(modifier2);
        podQuirks.get(Location.LeftLeg).add(modifier3);
        podQuirks.get(Location.Head).add(modifier4);
        podQuirks.get(Location.CenterTorso).add(modifier5);
        podQuirks.get(Location.RightTorso).add(modifier6);
        podQuirks.get(Location.RightLeg).add(modifier7);
        podQuirks.get(Location.RightArm).add(modifier8);
        podQuirks.get(Location.RightArm).add(modifier9);

        final Collection<Modifier> modifiers = makeDefaultCUT().getModifiers();
        assertEquals(9, modifiers.size());
        assertTrue(modifiers.contains(modifier1));
        assertTrue(modifiers.contains(modifier2));
        assertTrue(modifiers.contains(modifier3));
        assertTrue(modifiers.contains(modifier4));
        assertTrue(modifiers.contains(modifier5));
        assertTrue(modifiers.contains(modifier6));
        assertTrue(modifiers.contains(modifier7));
        assertTrue(modifiers.contains(modifier8));
        assertTrue(modifiers.contains(modifier9));
    }

    @Test
    public final void testGetModifiersOmniPodSetBonus() throws Exception {
        final OmniPodSet setA = mock(OmniPodSet.class);
        final OmniPodSet setB = mock(OmniPodSet.class);

        final Collection<Modifier> setAModifiers = new ArrayList<>();
        final Modifier modifier = mock(Modifier.class);
        setAModifiers.add(modifier);
        when(setA.getModifiers()).thenReturn(setAModifiers);

        when(pods[Location.LeftArm.ordinal()].getOmniPodSet()).thenReturn(setA);
        when(pods[Location.LeftTorso.ordinal()].getOmniPodSet()).thenReturn(setA);
        when(pods[Location.LeftLeg.ordinal()].getOmniPodSet()).thenReturn(setA);
        when(pods[Location.Head.ordinal()].getOmniPodSet()).thenReturn(setA);
        when(pods[Location.CenterTorso.ordinal()].getOmniPodSet()).thenReturn(setA);
        when(pods[Location.RightTorso.ordinal()].getOmniPodSet()).thenReturn(setA);
        when(pods[Location.RightLeg.ordinal()].getOmniPodSet()).thenReturn(setA);
        when(pods[Location.RightArm.ordinal()].getOmniPodSet()).thenReturn(setB);

        assertFalse(makeDefaultCUT().getModifiers().contains(modifier));

        when(pods[Location.RightArm.ordinal()].getOmniPodSet()).thenReturn(setA);
        assertTrue(makeDefaultCUT().getModifiers().contains(modifier));
    }

    @Test
    public final void testGetMovementProfile_() throws Exception {
        assertSame(movementProfile, makeDefaultCUT().getMovementProfile());
    }

    @Test
    public final void testGetSlotsUsedFree() throws Exception {
        when(structure.getExtraSlots()).thenReturn(7);
        when(armour.getExtraSlots()).thenReturn(7);

        when(getComponent(Location.LeftArm).getSlotsUsed()).thenReturn(5);
        when(getComponent(Location.RightLeg).getSlotsUsed()).thenReturn(3);

        assertEquals(8, makeDefaultCUT().getSlotsUsed());
        assertEquals(chassisSlots - 8, makeDefaultCUT().getFreeSlots());
    }

    @Test
    public final void testGetUpgrades() throws Exception {
        final Upgrades cut = makeDefaultCUT().getUpgrades();
        assertSame(armour, cut.getArmour());
        assertSame(structure, cut.getStructure());
        assertSame(heatSinks, cut.getHeatSink());
        assertSame(guidance, cut.getGuidance());
    }

    @Override
    protected Loadout makeDefaultCUT() {
        when(chassis.getName()).thenReturn(chassisName);
        when(chassis.getShortName()).thenReturn(chassisShortName);
        when(chassis.getMassMax()).thenReturn(mass);
        when(chassis.getSlotsTotal()).thenReturn(chassisSlots);
        when(chassisOmni.getFixedArmourType()).thenReturn(armour);
        when(chassisOmni.getFixedStructureType()).thenReturn(structure);
        when(chassisOmni.getFixedHeatSinkType()).thenReturn(heatSinks);
        when(chassisOmni.getFixedEngine()).thenReturn(engine);
        when(chassisOmni.getMovementProfileBase()).thenReturn(movementProfile);
        return new LoadoutOmniMech((ConfiguredComponentOmniMech[]) components, (ChassisOmniMech) chassis, upgrades,
                weaponGroups);
    }

    private ConfiguredComponentOmniMech getComponent(Location aLocation) {
        return (ConfiguredComponentOmniMech) components[aLocation.ordinal()];
    }
}
