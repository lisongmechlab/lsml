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
package org.lisoft.lsml.model.chassi;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;

import java.util.ArrayList;
import java.util.List;

import org.junit.Test;
import org.lisoft.lsml.model.item.Item;
import org.lisoft.lsml.model.modifiers.Modifier;
import org.mockito.Mockito;

/**
 * Test suite for {@link OmniPod} class.
 * 
 * @author Li Song
 */
public class OmniPodTest {
    private String chassisName = "tbr-prime";
    private List<Item> fixedItems = new ArrayList<>();
    private List<HardPoint> hardPoints = new ArrayList<>();
    private Location location = Location.CenterTorso;
    private int maxJumpJets = 2;
    private int maxPilotModules = 1;
    private int mwoID = 30012;
    private List<Modifier> quirks = new ArrayList<>();
    private String series = "timber wolf";
    private List<Item> toggleableItems = new ArrayList<>();

    /**
     * Omnipods have unique MWO IDs so they are equal if the id is equal.
     */
    @Test
    public void testEquals() {
        OmniPod A = makeCUT();
        mwoID *= 2;
        OmniPod B = makeCUT();
        mwoID /= 2;
        series = "foobara";
        OmniPod C = makeCUT();

        assertTrue(A.equals(A));
        assertFalse(A.equals(B));
        assertTrue(A.equals(C));
        assertFalse(C.equals(chassisName.toUpperCase()));
    }

    @Test
    public void testGetChassisName() {
        assertEquals(chassisName.toUpperCase(), makeCUT().getChassisName());
    }

    @Test
    public void testGetChassisSeries() {
        assertEquals(series.toUpperCase(), makeCUT().getChassisSeries());
    }

    @Test
    public void testGetFixedItem() {
        Item i0 = Mockito.mock(Item.class);
        Item i1 = Mockito.mock(Item.class);
        fixedItems.add(i0);
        fixedItems.add(i1);

        List<Item> ans = new ArrayList<>(makeCUT().getFixedItems());

        assertEquals(2, ans.size());
        assertTrue(ans.remove(i0));
        assertTrue(ans.remove(i1));
    }

    @Test(expected = UnsupportedOperationException.class)
    public void testGetFixedItems_NoMod() {
        makeCUT().getFixedItems().add(Mockito.mock(Item.class));
    }

    @Test
    public void testGetHardPointCount() {
        HardPoint hp1 = Mockito.mock(HardPoint.class);
        HardPoint hp2 = Mockito.mock(HardPoint.class);
        HardPoint hp3 = Mockito.mock(HardPoint.class);
        HardPoint hp4 = Mockito.mock(HardPoint.class);

        Mockito.when(hp1.getType()).thenReturn(HardPointType.MISSILE);
        Mockito.when(hp2.getType()).thenReturn(HardPointType.MISSILE);
        Mockito.when(hp3.getType()).thenReturn(HardPointType.ECM);
        Mockito.when(hp4.getType()).thenReturn(HardPointType.ENERGY);

        hardPoints.add(hp1);
        hardPoints.add(hp2);
        hardPoints.add(hp3);
        hardPoints.add(hp4);

        assertEquals(2, makeCUT().getHardPointCount(HardPointType.MISSILE));
        assertEquals(1, makeCUT().getHardPointCount(HardPointType.ECM));
        assertEquals(1, makeCUT().getHardPointCount(HardPointType.ENERGY));
        assertEquals(0, makeCUT().getHardPointCount(HardPointType.BALLISTIC));
    }

    @Test
    public void testGetHardPoints() {
        HardPoint hp1 = Mockito.mock(HardPoint.class);
        HardPoint hp2 = Mockito.mock(HardPoint.class);
        hardPoints.add(hp1);
        hardPoints.add(hp2);

        List<HardPoint> ans = new ArrayList<>(makeCUT().getHardPoints());

        assertEquals(2, ans.size());
        assertTrue(ans.remove(hp1));
        assertTrue(ans.remove(hp2));
    }

    @Test(expected = UnsupportedOperationException.class)
    public void testGetHardPoints_NoMod() {
        makeCUT().getHardPoints().add(Mockito.mock(HardPoint.class));
    }

    @Test
    public void testGetJumpJetsMax() {
        assertEquals(maxJumpJets, makeCUT().getJumpJetsMax());
    }

    @Test
    public void testGetLocation() {
        assertEquals(location, makeCUT().getLocation());
    }

    @Test
    public void testGetMwoId() {
        assertEquals(mwoID, makeCUT().getMwoId());
    }

    @Test
    public void testGetPilotModulesMax() {
        assertEquals(maxPilotModules, makeCUT().getPilotModulesMax());
    }

    @Test
    public void testGetQuirks() {
        assertSame(quirks, makeCUT().getQuirks());
    }

    @Test
    public void testGetToggleableItems() {
        Item i0 = Mockito.mock(Item.class);
        Item i1 = Mockito.mock(Item.class);
        toggleableItems.add(i0);
        toggleableItems.add(i1);

        List<Item> ans = new ArrayList<>(makeCUT().getToggleableItems());

        assertEquals(2, ans.size());
        assertTrue(ans.remove(i0));
        assertTrue(ans.remove(i1));
    }

    @Test(expected = UnsupportedOperationException.class)
    public void testGetToggleableItems_NoMod() {
        makeCUT().getToggleableItems().add(Mockito.mock(Item.class));
    }

    @Test
    public void testHashCode() {
        assertEquals(mwoID, makeCUT().hashCode());
    }

    @Test
    public void testHasMissileBayDoors_No() {
        HardPoint hp1 = Mockito.mock(HardPoint.class);
        HardPoint hp2 = Mockito.mock(HardPoint.class);
        HardPoint hp3 = Mockito.mock(HardPoint.class);
        HardPoint hp4 = Mockito.mock(HardPoint.class);

        Mockito.when(hp1.getType()).thenReturn(HardPointType.MISSILE);
        Mockito.when(hp2.getType()).thenReturn(HardPointType.MISSILE);
        Mockito.when(hp3.getType()).thenReturn(HardPointType.ECM);
        Mockito.when(hp4.getType()).thenReturn(HardPointType.ENERGY);

        hardPoints.add(hp1);
        hardPoints.add(hp2);
        hardPoints.add(hp3);
        hardPoints.add(hp4);

        assertFalse(makeCUT().hasMissileBayDoors());
    }

    @Test
    public void testHasMissileBayDoors_Yes() {
        HardPoint hp1 = Mockito.mock(HardPoint.class);
        HardPoint hp2 = Mockito.mock(HardPoint.class);
        HardPoint hp3 = Mockito.mock(HardPoint.class);
        HardPoint hp4 = Mockito.mock(HardPoint.class);

        Mockito.when(hp1.getType()).thenReturn(HardPointType.MISSILE);
        Mockito.when(hp2.getType()).thenReturn(HardPointType.MISSILE);
        Mockito.when(hp2.hasMissileBayDoor()).thenReturn(true);
        Mockito.when(hp3.getType()).thenReturn(HardPointType.ECM);
        Mockito.when(hp4.getType()).thenReturn(HardPointType.ENERGY);

        hardPoints.add(hp1);
        hardPoints.add(hp2);
        hardPoints.add(hp3);
        hardPoints.add(hp4);

        assertTrue(makeCUT().hasMissileBayDoors());
    }

    @Test
    public void testIsCompatible() {
        series = "TIMBER WOLF";
        chassisName = "TBR-PRIME";

        ChassisOmniMech chassisP = Mockito.mock(ChassisOmniMech.class);
        Mockito.when(chassisP.getSeriesName()).thenReturn(series.toLowerCase());
        Mockito.when(chassisP.getName()).thenReturn(series.toLowerCase() + " tBR-PRIME");
        Mockito.when(chassisP.getNameShort()).thenReturn("TBR-PRImE");

        ChassisOmniMech chassisPI = Mockito.mock(ChassisOmniMech.class);
        Mockito.when(chassisPI.getSeriesName()).thenReturn(series.toLowerCase());
        Mockito.when(chassisPI.getName()).thenReturn(series.toLowerCase() + " TBR-PRIME(I)");
        Mockito.when(chassisPI.getNameShort()).thenReturn("TBR-PRiME");

        ChassisOmniMech chassisPG = Mockito.mock(ChassisOmniMech.class);
        Mockito.when(chassisPG.getSeriesName()).thenReturn(series.toLowerCase());
        Mockito.when(chassisPG.getName()).thenReturn(series.toLowerCase() + " TBR-PRIME(G)");
        Mockito.when(chassisPG.getNameShort()).thenReturn("TBr-PRIME(G)");

        ChassisOmniMech chassisC = Mockito.mock(ChassisOmniMech.class);
        Mockito.when(chassisC.getSeriesName()).thenReturn(series.toLowerCase());
        Mockito.when(chassisC.getName()).thenReturn(series.toLowerCase() + " TBR-C");
        Mockito.when(chassisC.getNameShort()).thenReturn("TBr-c");

        ChassisOmniMech scr = Mockito.mock(ChassisOmniMech.class);
        Mockito.when(scr.getSeriesName()).thenReturn("stormcrow");
        Mockito.when(scr.getName()).thenReturn("stormcrow scr-C");
        Mockito.when(scr.getNameShort()).thenReturn("scrr-c");

        assertTrue(makeCUT().isCompatible(chassisP));
        assertTrue(makeCUT().isCompatible(chassisPI));
        assertTrue(makeCUT().isCompatible(chassisPG));
        assertTrue(makeCUT().isCompatible(chassisC));

        assertFalse(makeCUT().isCompatible(scr));
    }

    @Test
    public void testIsOriginalForChassis() {
        series = "TIMBER WOLF";
        chassisName = "TBR-PRIME";

        ChassisOmniMech chassisP = Mockito.mock(ChassisOmniMech.class);
        Mockito.when(chassisP.getSeriesName()).thenReturn(series.toLowerCase());
        Mockito.when(chassisP.getName()).thenReturn(series.toLowerCase() + " tBR-PRIME");
        Mockito.when(chassisP.getNameShort()).thenReturn("TBR-PRImE");

        ChassisOmniMech chassisPI = Mockito.mock(ChassisOmniMech.class);
        Mockito.when(chassisPI.getSeriesName()).thenReturn(series.toLowerCase());
        Mockito.when(chassisPI.getName()).thenReturn(series.toLowerCase() + " TBR-PRIME(I)");
        Mockito.when(chassisPI.getNameShort()).thenReturn("TBR-PRiME");

        ChassisOmniMech chassisPG = Mockito.mock(ChassisOmniMech.class);
        Mockito.when(chassisPG.getSeriesName()).thenReturn(series.toLowerCase());
        Mockito.when(chassisPG.getName()).thenReturn(series.toLowerCase() + " TBR-PRIME(G)");
        Mockito.when(chassisPG.getNameShort()).thenReturn("TBr-PRIME(G)");

        ChassisOmniMech chassisC = Mockito.mock(ChassisOmniMech.class);
        Mockito.when(chassisC.getSeriesName()).thenReturn(series.toLowerCase());
        Mockito.when(chassisC.getName()).thenReturn(series.toLowerCase() + " TBR-C");
        Mockito.when(chassisC.getNameShort()).thenReturn("TBr-c");

        assertTrue(makeCUT().isOriginalForChassis(chassisP));
        assertTrue(makeCUT().isOriginalForChassis(chassisPI));
        assertTrue(makeCUT().isOriginalForChassis(chassisPG));
        assertFalse(makeCUT().isOriginalForChassis(chassisC));
    }

    @Test
    public void testToString() {
        assertEquals(chassisName.toUpperCase(), makeCUT().toString());
    }

    protected OmniPod makeCUT() {
        return new OmniPod(mwoID, location, series, chassisName, quirks, hardPoints, fixedItems, toggleableItems,
                maxJumpJets, maxPilotModules);
    }
}
