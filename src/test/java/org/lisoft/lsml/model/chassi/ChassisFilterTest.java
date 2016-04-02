package org.lisoft.lsml.model.chassi;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertSame;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyBoolean;
import static org.mockito.Matchers.anyInt;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import org.junit.Before;
import org.junit.Test;
import org.lisoft.lsml.model.datacache.ChassisDB;
import org.lisoft.lsml.model.item.Faction;
import org.lisoft.lsml.model.loadout.DefaultLoadoutFactory;
import org.lisoft.lsml.model.loadout.Loadout;
import org.lisoft.lsml.model.metrics.TopSpeed;

import javafx.collections.ObservableList;

public class ChassisFilterTest {

    private List<Chassis>   chassis         = new ArrayList<>();
    private OmniPodSelector omniPodSelector = mock(OmniPodSelector.class);

    @Before
    public void setup() {
        chassis.addAll(ChassisDB.lookup(ChassisClass.LIGHT));
        chassis.addAll(ChassisDB.lookup(ChassisClass.MEDIUM));
        chassis.addAll(ChassisDB.lookup(ChassisClass.HEAVY));
        chassis.addAll(ChassisDB.lookup(ChassisClass.ASSAULT));
    }

    @Test
    public void testChassisFilter() {
        acceptAllOmniMechHardpoints();
        ChassisFilter cut = new ChassisFilter(chassis, DefaultLoadoutFactory.instance, omniPodSelector);
        ObservableList<Loadout> loadouts = cut.getChildren();
        assertEquals(chassis.size(), loadouts.size());
    }

    private void acceptAllOmniMechHardpoints() {
        Optional<Map<Location, OmniPod>> pods = Optional.of(new HashMap<>());
        when(omniPodSelector.selectPods(any(ChassisOmniMech.class), anyInt(), anyInt(), anyInt(), anyInt(),
                anyBoolean())).thenReturn(pods);
    }

    @Test
    public void testFactionFilter() {
        chassis = chassis.stream().filter(aChassis -> aChassis.getFaction() == Faction.CLAN)
                .collect(Collectors.toCollection(ArrayList::new));

        acceptAllOmniMechHardpoints();

        ChassisFilter cut = new ChassisFilter(chassis, DefaultLoadoutFactory.instance, omniPodSelector);
        cut.factionFilterProperty().set(Faction.INNERSPHERE);

        ObservableList<Loadout> loadouts = cut.getChildren();
        assertEquals(0, loadouts.size());
    }

    @Test
    public void testHeroFilter() {
        chassis = chassis.stream().filter(aChassis -> aChassis.getVariantType() != ChassisVariant.NORMAL)
                .collect(Collectors.toCollection(ArrayList::new));

        acceptAllOmniMechHardpoints();

        ChassisFilter cut = new ChassisFilter(chassis, DefaultLoadoutFactory.instance, omniPodSelector);
        cut.heroFilterProperty().set(false);

        ObservableList<Loadout> loadouts = cut.getChildren();
        assertEquals(0, loadouts.size());
    }

    @Test
    public void testMinMassFilter() {
        final int minMass = 75;
        chassis = chassis.stream().filter(aChassis -> aChassis.getMassMax() < minMass)
                .collect(Collectors.toCollection(ArrayList::new));

        acceptAllOmniMechHardpoints();

        ChassisFilter cut = new ChassisFilter(chassis, DefaultLoadoutFactory.instance, omniPodSelector);
        cut.minMassFilterProperty().set(75);

        ObservableList<Loadout> loadouts = cut.getChildren();
        assertEquals(0, loadouts.size());
    }

    @Test
    public void testMaxMassFilter() {
        final int maxMass = 60;
        chassis = chassis.stream().filter(aChassis -> aChassis.getMassMax() > maxMass)
                .collect(Collectors.toCollection(ArrayList::new));

        acceptAllOmniMechHardpoints();

        ChassisFilter cut = new ChassisFilter(chassis, DefaultLoadoutFactory.instance, omniPodSelector);
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

        acceptAllOmniMechHardpoints();

        ChassisFilter cut = new ChassisFilter(chassis, DefaultLoadoutFactory.instance, omniPodSelector);
        cut.minSpeedFilterProperty().set(minSpeed);

        ObservableList<Loadout> loadouts = cut.getChildren();
        assertEquals(0, loadouts.size());
    }

    @Test
    public void testEcmFilter_Standard() {
        Chassis cda_3m = ChassisDB.lookup("CDA-3M");

        chassis.clear();
        chassis.add(cda_3m);
        chassis.add(ChassisDB.lookup("CDA-2B")); // ECM capable

        ChassisFilter cut = new ChassisFilter(chassis, DefaultLoadoutFactory.instance, omniPodSelector);
        cut.ecmFilterProperty().set(true);

        List<Loadout> loadouts = new ArrayList<>(cut.getChildren());
        assertEquals(1, loadouts.size());
        assertSame(cda_3m, loadouts.get(0).getChassis());
    }

    @Test
    public void testMinJumpJetsFilter_Standard() {
        final int minJJ = 4;

        chassis = chassis.stream().filter(aChassis -> {
            if (aChassis instanceof ChassisStandard) {
                ChassisStandard chassisStandard = (ChassisStandard) aChassis;
                return chassisStandard.getJumpJetsMax() < minJJ;
            }
            return false;
        }).collect(Collectors.toCollection(ArrayList::new));

        ChassisFilter cut = new ChassisFilter(chassis, DefaultLoadoutFactory.instance, omniPodSelector);
        cut.minJumpJetFilterProperty().set(minJJ);

        ObservableList<Loadout> loadouts = cut.getChildren();
        assertEquals(0, loadouts.size());
    }

    @Test
    public void testMinEnergyFilter_Standard() {
        final int minEnergy = 4;

        chassis = chassis.stream().filter(aChassis -> {
            if (aChassis instanceof ChassisStandard) {
                ChassisStandard chassisStandard = (ChassisStandard) aChassis;
                return chassisStandard.getHardPointsCount(HardPointType.ENERGY) < minEnergy;
            }
            return false;
        }).collect(Collectors.toCollection(ArrayList::new));

        ChassisFilter cut = new ChassisFilter(chassis, DefaultLoadoutFactory.instance, omniPodSelector);
        cut.minEnergyFilterProperty().set(minEnergy);

        ObservableList<Loadout> loadouts = cut.getChildren();
        assertEquals(0, loadouts.size());
    }

    @Test
    public void testMinBallisticFilter_Standard() {
        final int minBallistic = 4;

        chassis = chassis.stream().filter(aChassis -> {
            if (aChassis instanceof ChassisStandard) {
                ChassisStandard chassisStandard = (ChassisStandard) aChassis;
                return chassisStandard.getHardPointsCount(HardPointType.BALLISTIC) < minBallistic;
            }
            return false;
        }).collect(Collectors.toCollection(ArrayList::new));

        ChassisFilter cut = new ChassisFilter(chassis, DefaultLoadoutFactory.instance, omniPodSelector);
        cut.minBallisticFilterProperty().set(minBallistic);

        ObservableList<Loadout> loadouts = cut.getChildren();
        assertEquals(0, loadouts.size());
    }

    @Test
    public void testMinMissileFilter_Standard() {
        final int minMissile = 4;

        chassis = chassis.stream().filter(aChassis -> {
            if (aChassis instanceof ChassisStandard) {
                ChassisStandard chassisStandard = (ChassisStandard) aChassis;
                return chassisStandard.getHardPointsCount(HardPointType.MISSILE) < minMissile;
            }
            return false;
        }).collect(Collectors.toCollection(ArrayList::new));

        ChassisFilter cut = new ChassisFilter(chassis, DefaultLoadoutFactory.instance, omniPodSelector);
        cut.minMissileFilterProperty().set(minMissile);

        ObservableList<Loadout> loadouts = cut.getChildren();
        assertEquals(0, loadouts.size());
    }
}
