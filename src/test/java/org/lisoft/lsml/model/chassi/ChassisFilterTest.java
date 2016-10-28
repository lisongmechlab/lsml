package org.lisoft.lsml.model.chassi;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertSame;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
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
import org.lisoft.lsml.model.loadout.LoadoutFactory;
import org.lisoft.lsml.model.metrics.TopSpeed;
import org.lisoft.lsml.view_fx.Settings;

import javafx.beans.property.SimpleBooleanProperty;
import javafx.collections.ObservableList;

public class ChassisFilterTest {

    private List<Chassis> chassis = new ArrayList<>();
    private final OmniPodSelector omniPodSelector = mock(OmniPodSelector.class);
    private final Settings settings = mock(Settings.class);
    private final LoadoutFactory factory = mock(LoadoutFactory.class);

    @Before
    public void setup() {
        chassis.addAll(ChassisDB.lookup(ChassisClass.LIGHT));
        chassis.addAll(ChassisDB.lookup(ChassisClass.MEDIUM));
        chassis.addAll(ChassisDB.lookup(ChassisClass.HEAVY));
        chassis.addAll(ChassisDB.lookup(ChassisClass.ASSAULT));

        when(settings.getProperty(anyString(), eq(Boolean.class))).thenReturn(new SimpleBooleanProperty(false));
        when(settings.getBoolean(anyString())).thenReturn(new SimpleBooleanProperty(false));

        when(factory.produceDefault(any(Chassis.class), eq(settings))).then(aInvocation -> {
            return DefaultLoadoutFactory.instance.produceDefault(aInvocation.getArgument(0), settings);
        });
    }

    @Test
    public void testChassisFilter() {
        acceptAllOmniMechHardpoints();
        final ChassisFilter cut = new ChassisFilter(chassis, factory, omniPodSelector, settings);
        final ObservableList<Loadout> loadouts = cut.getChildren();
        assertEquals(chassis.size(), loadouts.size());
    }

    @Test
    public void testEcmFilter_Standard() {
        final Chassis cda_3m = ChassisDB.lookup("CDA-3M");

        chassis.clear();
        chassis.add(cda_3m);
        chassis.add(ChassisDB.lookup("CDA-2B")); // ECM capable

        final ChassisFilter cut = new ChassisFilter(chassis, factory, omniPodSelector, settings);
        cut.ecmFilterProperty().set(true);

        final List<Loadout> loadouts = new ArrayList<>(cut.getChildren());
        assertEquals(1, loadouts.size());
        assertSame(cda_3m, loadouts.get(0).getChassis());
    }

    @Test
    public void testFactionFilter() {
        chassis = chassis.stream().filter(aChassis -> aChassis.getFaction() == Faction.CLAN)
                .collect(Collectors.toCollection(ArrayList::new));

        acceptAllOmniMechHardpoints();

        final ChassisFilter cut = new ChassisFilter(chassis, factory, omniPodSelector, settings);
        cut.factionFilterProperty().set(Faction.INNERSPHERE);

        final ObservableList<Loadout> loadouts = cut.getChildren();
        assertEquals(0, loadouts.size());
    }

    @Test
    public void testHeroFilter() {
        chassis = chassis.stream().filter(aChassis -> aChassis.getVariantType() != ChassisVariant.NORMAL)
                .collect(Collectors.toCollection(ArrayList::new));

        acceptAllOmniMechHardpoints();

        final ChassisFilter cut = new ChassisFilter(chassis, factory, omniPodSelector, settings);
        cut.heroFilterProperty().set(false);

        final ObservableList<Loadout> loadouts = cut.getChildren();
        assertEquals(0, loadouts.size());
    }

    @Test
    public void testMaxMassFilter() {
        final int maxMass = 60;
        chassis = chassis.stream().filter(aChassis -> aChassis.getMassMax() > maxMass)
                .collect(Collectors.toCollection(ArrayList::new));

        acceptAllOmniMechHardpoints();

        final ChassisFilter cut = new ChassisFilter(chassis, factory, omniPodSelector, settings);
        cut.maxMassFilterProperty().set(maxMass);

        final ObservableList<Loadout> loadouts = cut.getChildren();
        assertEquals(0, loadouts.size());
    }

    @Test
    public void testMinBallisticFilter_Standard() {
        final int minBallistic = 4;

        chassis = chassis.stream().filter(aChassis -> {
            if (aChassis instanceof ChassisStandard) {
                final ChassisStandard chassisStandard = (ChassisStandard) aChassis;
                return chassisStandard.getHardPointsCount(HardPointType.BALLISTIC) < minBallistic;
            }
            return false;
        }).collect(Collectors.toCollection(ArrayList::new));

        final ChassisFilter cut = new ChassisFilter(chassis, factory, omniPodSelector, settings);
        cut.minBallisticFilterProperty().set(minBallistic);

        final ObservableList<Loadout> loadouts = cut.getChildren();
        assertEquals(0, loadouts.size());
    }

    @Test
    public void testMinEnergyFilter_Standard() {
        final int minEnergy = 4;

        chassis = chassis.stream().filter(aChassis -> {
            if (aChassis instanceof ChassisStandard) {
                final ChassisStandard chassisStandard = (ChassisStandard) aChassis;
                return chassisStandard.getHardPointsCount(HardPointType.ENERGY) < minEnergy;
            }
            return false;
        }).collect(Collectors.toCollection(ArrayList::new));

        final ChassisFilter cut = new ChassisFilter(chassis, factory, omniPodSelector, settings);
        cut.minEnergyFilterProperty().set(minEnergy);

        final ObservableList<Loadout> loadouts = cut.getChildren();
        assertEquals(0, loadouts.size());
    }

    @Test
    public void testMinJumpJetsFilter_Standard() {
        final int minJJ = 4;

        chassis = chassis.stream().filter(aChassis -> {
            if (aChassis instanceof ChassisStandard) {
                final ChassisStandard chassisStandard = (ChassisStandard) aChassis;
                return chassisStandard.getJumpJetsMax() < minJJ;
            }
            return false;
        }).collect(Collectors.toCollection(ArrayList::new));

        final ChassisFilter cut = new ChassisFilter(chassis, factory, omniPodSelector, settings);
        cut.minJumpJetFilterProperty().set(minJJ);

        final ObservableList<Loadout> loadouts = cut.getChildren();
        assertEquals(0, loadouts.size());
    }

    @Test
    public void testMinMassFilter() {
        final int minMass = 75;
        chassis = chassis.stream().filter(aChassis -> aChassis.getMassMax() < minMass)
                .collect(Collectors.toCollection(ArrayList::new));

        acceptAllOmniMechHardpoints();

        final ChassisFilter cut = new ChassisFilter(chassis, factory, omniPodSelector, settings);
        cut.minMassFilterProperty().set(75);

        final ObservableList<Loadout> loadouts = cut.getChildren();
        assertEquals(0, loadouts.size());
    }

    @Test
    public void testMinMissileFilter_Standard() {
        final int minMissile = 4;

        chassis = chassis.stream().filter(aChassis -> {
            if (aChassis instanceof ChassisStandard) {
                final ChassisStandard chassisStandard = (ChassisStandard) aChassis;
                return chassisStandard.getHardPointsCount(HardPointType.MISSILE) < minMissile;
            }
            return false;
        }).collect(Collectors.toCollection(ArrayList::new));

        final ChassisFilter cut = new ChassisFilter(chassis, factory, omniPodSelector, settings);
        cut.minMissileFilterProperty().set(minMissile);

        final ObservableList<Loadout> loadouts = cut.getChildren();
        assertEquals(0, loadouts.size());
    }

    @Test
    public void testMinSpeedFilter() {
        final int minSpeed = 100;

        chassis = chassis.stream().filter(aChassis -> {
            final int rating;
            if (aChassis instanceof ChassisStandard) {
                final ChassisStandard chassisStandard = (ChassisStandard) aChassis;
                rating = chassisStandard.getEngineMax();
            }
            else {
                final ChassisOmniMech chassisOmniMech = (ChassisOmniMech) aChassis;
                rating = chassisOmniMech.getFixedEngine().getRating();
            }

            final double speed = TopSpeed.calculate(rating, aChassis.getMovementProfileBase(), aChassis.getMassMax(),
                    null);

            return speed < minSpeed;

        }).collect(Collectors.toCollection(ArrayList::new));

        acceptAllOmniMechHardpoints();

        final ChassisFilter cut = new ChassisFilter(chassis, factory, omniPodSelector, settings);
        cut.minSpeedFilterProperty().set(minSpeed);

        final ObservableList<Loadout> loadouts = cut.getChildren();
        assertEquals(0, loadouts.size());
    }

    private void acceptAllOmniMechHardpoints() {
        final Optional<Map<Location, OmniPod>> pods = Optional.of(new HashMap<>());
        when(omniPodSelector.selectPods(any(ChassisOmniMech.class), anyInt(), anyInt(), anyInt(), anyInt(),
                anyBoolean())).thenReturn(pods);
    }
}
