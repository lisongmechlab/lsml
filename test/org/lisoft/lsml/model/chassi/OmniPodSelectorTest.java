package org.lisoft.lsml.model.chassi;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;

import java.util.Map;
import java.util.Optional;

import org.junit.Test;
import org.lisoft.lsml.model.datacache.ChassisDB;
import org.lisoft.lsml.model.datacache.OmniPodDB;

/**
 * Test suite for the {@link OmniPodSelector} class.
 * 
 * @author Li Song
 *
 */
/**
 * @author Li Song
 *
 */
public class OmniPodSelectorTest {

    @Test
    public void testSelectPods() {
        ChassisOmniMech hbr_a = (ChassisOmniMech) ChassisDB.lookup("HBR-A");
        ChassisOmniMech hbr_b = (ChassisOmniMech) ChassisDB.lookup("HBR-B");
        ChassisOmniMech hbr_prime = (ChassisOmniMech) ChassisDB.lookup("HBR-PRIME");

        OmniPodSelector cut = new OmniPodSelector();
        Optional<Map<Location, OmniPod>> ans = cut.selectPods(hbr_a, 7, 2, 0, 0, true);

        // Should only exist one solution:
        // RA-Prime, RT-B, HD-A, LT-Prime, LA-A
        assertTrue(ans.isPresent());
        Map<Location, OmniPod> pods = ans.get();
        assertSame(OmniPodDB.lookupOriginal(hbr_prime, Location.RightArm), pods.get(Location.RightArm));
        assertSame(OmniPodDB.lookupOriginal(hbr_b, Location.RightTorso), pods.get(Location.RightTorso));
        assertSame(OmniPodDB.lookupOriginal(hbr_a, Location.Head), pods.get(Location.Head));
        assertSame(OmniPodDB.lookupOriginal(hbr_prime, Location.LeftTorso), pods.get(Location.LeftTorso));
        assertSame(OmniPodDB.lookupOriginal(hbr_a, Location.LeftArm), pods.get(Location.LeftArm));
    }

    @Test
    public void testSelectPods_OmniJJ() {
        ChassisOmniMech tbr_s = (ChassisOmniMech) ChassisDB.lookup("TBR-S");
        ChassisOmniMech tbr_prime = (ChassisOmniMech) ChassisDB.lookup("TBR-PRIME");

        OmniPodSelector cut = new OmniPodSelector();
        Optional<Map<Location, OmniPod>> ans = cut.selectPods(tbr_prime, 0, 0, 0, 4, false);

        // Expected solution:
        // RT/LT-S
        assertTrue(ans.isPresent());
        Map<Location, OmniPod> pods = ans.get();
        assertSame(OmniPodDB.lookupOriginal(tbr_s, Location.RightTorso), pods.get(Location.RightTorso));
        assertSame(OmniPodDB.lookupOriginal(tbr_s, Location.LeftTorso), pods.get(Location.LeftTorso));
    }

    @Test
    public void testSelectPods_NoSolution() {
        ChassisOmniMech adr_prime = (ChassisOmniMech) ChassisDB.lookup("ADR-PRIME");

        OmniPodSelector cut = new OmniPodSelector();
        Optional<Map<Location, OmniPod>> ans = cut.selectPods(adr_prime, 5, 0, 2, 0, false);

        assertFalse(ans.isPresent());
    }

    /**
     * Test that jump jets that are satisfied through the chassis (as opposed to the omni mech) are accounted properly.
     */
    @Test
    public void testSelectPods_NonOmniJJ() {
        ChassisOmniMech shc_a = (ChassisOmniMech) ChassisDB.lookup("SHC-A");
        ChassisOmniMech shc_b = (ChassisOmniMech) ChassisDB.lookup("SHC-B");
        ChassisOmniMech shc_p = (ChassisOmniMech) ChassisDB.lookup("SHC-P");
        ChassisOmniMech shc_prime = (ChassisOmniMech) ChassisDB.lookup("SHC-PRIME");

        OmniPodSelector cut = new OmniPodSelector();
        Optional<Map<Location, OmniPod>> ans = cut.selectPods(shc_prime, 0, 2, 3, 6, true);

        // Expected solution:
        // RA-B, RT-A, LT-B, LA-P
        assertTrue(ans.isPresent());
        Map<Location, OmniPod> pods = ans.get();
        assertSame(OmniPodDB.lookupOriginal(shc_b, Location.RightArm), pods.get(Location.RightArm));
        assertSame(OmniPodDB.lookupOriginal(shc_a, Location.RightTorso), pods.get(Location.RightTorso));
        assertSame(OmniPodDB.lookupOriginal(shc_b, Location.LeftTorso), pods.get(Location.LeftTorso));
        assertSame(OmniPodDB.lookupOriginal(shc_p, Location.LeftArm), pods.get(Location.LeftArm));
    }

    /**
     * Test that hard points in the CT are counted towards the required values.
     */
    @Test
    public void testSelectPods_CTHardPointCounted() {
        ChassisOmniMech ifr_a = (ChassisOmniMech) ChassisDB.lookup("IFR-A");
        ChassisOmniMech ifr_d = (ChassisOmniMech) ChassisDB.lookup("IFR-D");
        ChassisOmniMech ifr_prime = (ChassisOmniMech) ChassisDB.lookup("IFR-PRIME");

        OmniPodSelector cut = new OmniPodSelector();
        Optional<Map<Location, OmniPod>> ans = cut.selectPods(ifr_a, 5, 0, 0, 0, false);

        // Expected solution:
        // RA-D, LA-Prime
        assertTrue(ans.isPresent());
        Map<Location, OmniPod> pods = ans.get();
        assertSame(OmniPodDB.lookupOriginal(ifr_d, Location.RightArm), pods.get(Location.RightArm));
        assertSame(OmniPodDB.lookupOriginal(ifr_prime, Location.LeftArm), pods.get(Location.LeftArm));
    }
}
