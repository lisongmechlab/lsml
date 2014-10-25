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
package lisong_mechlab.model.chassi;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.util.ArrayList;
import java.util.List;

import lisong_mechlab.model.item.Engine;
import lisong_mechlab.model.item.HeatSink;
import lisong_mechlab.model.item.Item;
import lisong_mechlab.model.item.ItemDB;
import lisong_mechlab.model.item.JumpJet;
import lisong_mechlab.model.item.MissileWeapon;

import org.junit.Test;
import org.mockito.Mockito;

/**
 * Test suite for {@link ComponentStandard}
 * 
 * @author Li Song
 */
public class ComponentStandardTest extends ComponentBaseTest {

    private List<HardPoint> hardPoints = new ArrayList<>();

    @Override
    protected ComponentStandard makeDefaultCUT() {
        return new ComponentStandard(location, criticalSlots, hp, fixedItems, hardPoints);
    }

    /**
     * A component has missile bay doors if any one of its {@link HardPoint}s has missile bay doors.
     */
    @Test
    public void testHasMissileBayDoors_NoDoors() {
        hardPoints.add(new HardPoint(HardPointType.MISSILE, 5, false));
        ComponentStandard cut = makeDefaultCUT();
        assertFalse(cut.hasMissileBayDoors());
    }

    /**
     * A component has missile bay doors if any one of its {@link HardPoint}s has missile bay doors.
     */
    @Test
    public void testHasMissileBayDoors_HasDoors() {
        hardPoints.add(new HardPoint(HardPointType.MISSILE, 5, true));
        ComponentStandard cut = makeDefaultCUT();
        assertTrue(cut.hasMissileBayDoors());
    }

    /**
     * The list of hard points returned shall be immutable.
     */
    @Test(expected = UnsupportedOperationException.class)
    public void testGetHardpoints_Immutable() {
        makeDefaultCUT().getHardPoints().add(new HardPoint(HardPointType.ENERGY));
    }

    /**
     * Engine is only allowed in CT
     */
    @Test
    public void testIsAllowed_Engine() {
        Engine engine = Mockito.mock(Engine.class);
        Mockito.when(engine.getHardpointType()).thenReturn(HardPointType.NONE);

        for (Location loc : Location.values()) {
            location = loc;
            if (loc == Location.CenterTorso) {
                assertTrue(makeDefaultCUT().isAllowed(engine));
            }
            else {
                assertFalse(makeDefaultCUT().isAllowed(engine));
            }
        }
    }
    
    /**
     * Double HS allowed in CT
     */
    @Test
    public void testIsAllowed_DhsInCt() {
        HeatSink heatsink = Mockito.mock(HeatSink.class);
        Mockito.when(heatsink.getHardpointType()).thenReturn(HardPointType.NONE);
        Mockito.when(heatsink.getNumCriticalSlots()).thenReturn(3);

        location = Location.CenterTorso;
        assertTrue(makeDefaultCUT().isAllowed(heatsink));
    }

    /**
     * Jump jets are only allowed in legs and torsii.
     */
    @Test
    public void testIsAllowed_Jumpjets() {
        JumpJet jj = Mockito.mock(JumpJet.class);
        Mockito.when(jj.getHardpointType()).thenReturn(HardPointType.NONE);
        criticalSlots = 12;

        List<Location> allowedLocations = new ArrayList<>();
        allowedLocations.add(Location.CenterTorso);
        allowedLocations.add(Location.RightTorso);
        allowedLocations.add(Location.LeftTorso);
        allowedLocations.add(Location.LeftLeg);
        allowedLocations.add(Location.RightLeg);

        for (Location loc : Location.values()) {
            location = loc;
            if (allowedLocations.contains(loc)) {
                assertTrue(makeDefaultCUT().isAllowed(jj));
            }
            else {
                assertFalse(makeDefaultCUT().isAllowed(jj));
            }
        }
    }

    /**
     * C.A.S.E. is only allowed on side torsii, (doesn't make sense in CT).
     */
    @Test
    public void testIsAllowed_CASE() {
        List<Location> allowedLocations = new ArrayList<>();
        allowedLocations.add(Location.RightTorso);
        allowedLocations.add(Location.LeftTorso);

        for (Location loc : Location.values()) {
            location = loc;
            if (allowedLocations.contains(loc)) {
                assertTrue(makeDefaultCUT().isAllowed(ItemDB.CASE));
            }
            else {
                assertFalse(makeDefaultCUT().isAllowed(ItemDB.CASE));
            }
        }
    }

    /**
     * Items that do not have a matching hard point are not allowed.
     */
    @Test
    public void testIsAllowed_NoHardpoints() {
        hardPoints.add(new HardPoint(HardPointType.BALLISTIC));
        hardPoints.add(new HardPoint(HardPointType.ENERGY));

        MissileWeapon missile = Mockito.mock(MissileWeapon.class);
        Mockito.when(missile.getHardpointType()).thenReturn(HardPointType.MISSILE);

        assertFalse(makeDefaultCUT().isAllowed(missile));

        hardPoints.add(new HardPoint(HardPointType.MISSILE, 6, false));
        assertTrue(makeDefaultCUT().isAllowed(missile));
    }

    /**
     * The presence of the correct hard point type shall not short circuit check for item size.
     */
    @Test
    public void testIsAllowed_HasHardpointsButTooBig() {
        hardPoints.add(new HardPoint(HardPointType.MISSILE, 6, false));

        MissileWeapon missile = Mockito.mock(MissileWeapon.class);
        Mockito.when(missile.getHardpointType()).thenReturn(HardPointType.MISSILE);

        assertTrue(makeDefaultCUT().isAllowed(missile));

        Mockito.when(missile.getNumCriticalSlots()).thenReturn(criticalSlots + 1);

        assertFalse(makeDefaultCUT().isAllowed(missile));
    }

    /**
     * Items that are too big to fit together with fixed items are not allowed.
     */
    @Test
    public void testIsAllowed_TooBig() {
        Item fixedItem = Mockito.mock(Item.class);
        fixedItems.add(fixedItem);
        int fixedSize = criticalSlots / 2;
        Mockito.when(fixedItem.getNumCriticalSlots()).thenReturn(fixedSize);

        Item item = Mockito.mock(Item.class);
        Mockito.when(item.getHardpointType()).thenReturn(HardPointType.NONE);
        Mockito.when(item.getNumCriticalSlots()).thenReturn(criticalSlots - fixedSize);

        assertTrue(makeDefaultCUT().isAllowed(item));

        Mockito.when(item.getNumCriticalSlots()).thenReturn(criticalSlots - fixedSize + 1);

        assertFalse(makeDefaultCUT().isAllowed(item));
    }

    /**
     * Item's with no particular hard point requirements and that are small enough should be allowed.
     */
    @Test
    public void testIsAllowed_Basic() {
        Item item = Mockito.mock(Item.class);
        Mockito.when(item.getHardpointType()).thenReturn(HardPointType.NONE);
        Mockito.when(item.getNumCriticalSlots()).thenReturn(criticalSlots / 2);

        assertTrue(makeDefaultCUT().isAllowed(item));
    }
}
