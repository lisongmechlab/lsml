package org.lisoft.lsml.model.chassi;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertSame;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import org.junit.Before;
import org.junit.Test;
import org.lisoft.lsml.model.datacache.ChassisDB;
import org.lisoft.lsml.model.datacache.OmniPodDB;
import org.lisoft.lsml.model.item.Faction;
import org.lisoft.lsml.model.loadout.DefaultLoadoutFactory;
import org.lisoft.lsml.model.loadout.Loadout;
import org.lisoft.lsml.model.loadout.LoadoutOmniMech;
import org.lisoft.lsml.model.metrics.TopSpeed;

import javafx.collections.ObservableList;

public class ChassisFilterTest {

    private List<Chassis> chassis = new ArrayList<>();

    @Before
    public void setup() {
        chassis.addAll(ChassisDB.lookup(ChassisClass.LIGHT));
        chassis.addAll(ChassisDB.lookup(ChassisClass.MEDIUM));
        chassis.addAll(ChassisDB.lookup(ChassisClass.HEAVY));
        chassis.addAll(ChassisDB.lookup(ChassisClass.ASSAULT));
    }

    @Test
    public void testChassisFilter() {
        ChassisFilter cut = new ChassisFilter(chassis, DefaultLoadoutFactory.instance);

        ObservableList<Loadout> loadouts = cut.getChildren();
        assertEquals(chassis.size(), loadouts.size());
    }

    @Test
    public void testFactionFilter() {
        chassis = chassis.stream().filter(aChassis -> aChassis.getFaction() == Faction.CLAN)
                .collect(Collectors.toCollection(ArrayList::new));

        ChassisFilter cut = new ChassisFilter(chassis, DefaultLoadoutFactory.instance);
        cut.factionFilterProperty().set(Faction.INNERSPHERE);

        ObservableList<Loadout> loadouts = cut.getChildren();
        assertEquals(0, loadouts.size());
    }

    @Test
    public void testHeroFilter() {
        chassis = chassis.stream().filter(aChassis -> aChassis.getVariantType() != ChassisVariant.NORMAL)
                .collect(Collectors.toCollection(ArrayList::new));

        ChassisFilter cut = new ChassisFilter(chassis, DefaultLoadoutFactory.instance);
        cut.heroFilterProperty().set(false);

        ObservableList<Loadout> loadouts = cut.getChildren();
        assertEquals(0, loadouts.size());
    }

    @Test
    public void testMinMassFilter() {
        final int minMass = 75;
        chassis = chassis.stream().filter(aChassis -> aChassis.getMassMax() < minMass)
                .collect(Collectors.toCollection(ArrayList::new));

        ChassisFilter cut = new ChassisFilter(chassis, DefaultLoadoutFactory.instance);
        cut.minMassFilterProperty().set(75);

        ObservableList<Loadout> loadouts = cut.getChildren();
        assertEquals(0, loadouts.size());
    }

    @Test
    public void testMaxMassFilter() {
        final int maxMass = 60;
        chassis = chassis.stream().filter(aChassis -> aChassis.getMassMax() > maxMass)
                .collect(Collectors.toCollection(ArrayList::new));

        ChassisFilter cut = new ChassisFilter(chassis, DefaultLoadoutFactory.instance);
        cut.maxMassFilterProperty().set(maxMass);

        ObservableList<Loadout> loadouts = cut.getChildren();
        assertEquals(0, loadouts.size());
    }

    @Test
    public void testMinSpeedFilter() {
        final int minSpeed = 100;

        chassis = chassis.stream().filter(aChassis -> {
            final int rating;
            if (aChassis instanceof ChassisStandard) {
                ChassisStandard chassisStandard = (ChassisStandard) aChassis;
                rating = chassisStandard.getEngineMax();
            }
            else {
                ChassisOmniMech chassisOmniMech = (ChassisOmniMech) aChassis;
                rating = chassisOmniMech.getFixedEngine().getRating();
            }

            double speed = TopSpeed.calculate(rating, aChassis.getMovementProfileBase(), aChassis.getMassMax(), null);

            return speed < minSpeed;

        }).collect(Collectors.toCollection(ArrayList::new));

        ChassisFilter cut = new ChassisFilter(chassis, DefaultLoadoutFactory.instance);
        cut.minSpeedFilterProperty().set(minSpeed);

        ObservableList<Loadout> loadouts = cut.getChildren();
        assertEquals(0, loadouts.size());
    }

    @Test
    public void testEcmFilter() {
        ChassisOmniMech hbr_prime = (ChassisOmniMech) ChassisDB.lookup("HBR-PRIME");
        ChassisOmniMech hbr_a = (ChassisOmniMech) ChassisDB.lookup("HBR-A");
        Chassis cda_3m = ChassisDB.lookup("CDA-3M");

        chassis.clear();
        chassis.add(ChassisDB.lookup("TBR-PRIME"));
        chassis.add(hbr_prime); // ECM capable
        chassis.add(hbr_a); // ECM capable (with prime LT)
        chassis.add(cda_3m);
        chassis.add(ChassisDB.lookup("CDA-2B")); // ECM capable

        ChassisFilter cut = new ChassisFilter(chassis, DefaultLoadoutFactory.instance);
        cut.ecmFilterProperty().set(true);

        List<Loadout> loadouts = new ArrayList<>(cut.getChildren());
        assertEquals(3, loadouts.size());
        assertSame(hbr_prime, loadouts.get(0).getChassis());
        assertSame(hbr_a, loadouts.get(1).getChassis());
        assertSame(cda_3m, loadouts.get(2).getChassis());

        assertSame(OmniPodDB.lookupOriginal(hbr_prime, Location.LeftTorso),
                ((LoadoutOmniMech) loadouts.get(1)).getComponent(Location.LeftTorso).getOmniPod());
    }
}
