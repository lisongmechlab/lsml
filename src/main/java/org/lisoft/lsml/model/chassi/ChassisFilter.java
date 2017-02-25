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

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Optional;
import java.util.function.Predicate;

import org.lisoft.lsml.model.item.Faction;
import org.lisoft.lsml.model.loadout.Loadout;
import org.lisoft.lsml.model.loadout.LoadoutFactory;
import org.lisoft.lsml.model.loadout.LoadoutOmniMech;
import org.lisoft.lsml.model.loadout.LoadoutStandard;
import org.lisoft.lsml.model.metrics.TopSpeed;
import org.lisoft.lsml.model.modifiers.Modifier;
import org.lisoft.lsml.view_fx.Settings;

import javafx.beans.property.BooleanProperty;
import javafx.beans.property.IntegerProperty;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;

/**
 * This class maintains an observable list of chassis based on certain filters.
 * 
 * @author Li Song
 */
public class ChassisFilter {
    private final BooleanProperty heroFilter = new SimpleBooleanProperty(true);
    private final BooleanProperty ecmFilter = new SimpleBooleanProperty(false);
    private final IntegerProperty minMassFilter = new SimpleIntegerProperty(0);
    private final IntegerProperty maxMassFilter = new SimpleIntegerProperty(100);
    private final IntegerProperty minSpeedFilter = new SimpleIntegerProperty(0);
    private final IntegerProperty minBallisticFilter = new SimpleIntegerProperty(0);
    private final IntegerProperty minMissileFilter = new SimpleIntegerProperty(0);
    private final IntegerProperty minEnergyFilter = new SimpleIntegerProperty(0);
    private final IntegerProperty minJumpJetFilter = new SimpleIntegerProperty(0);
    private final ObjectProperty<Faction> factionFilter = new SimpleObjectProperty<>(Faction.ANY);

    private final ObservableList<Loadout> loadouts = FXCollections.observableArrayList();
    private final FilteredList<Loadout> filtered = new FilteredList<>(loadouts);
    private final Filter filter = new Filter();
    private final OmniPodSelector omniPodSelector;

    private class Filter implements Predicate<Loadout> {
        private Faction faction = factionFilter.get();
        private boolean hero = heroFilter.get();
        private boolean ecm = ecmFilter.get();
        private int minMass = minMassFilter.get();
        private int maxMass = maxMassFilter.get();
        private int minSpeed = minSpeedFilter.get();
        private int minBallistic = minBallisticFilter.get();
        private int minMissile = minMissileFilter.get();
        private int minEnergy = minEnergyFilter.get();
        private int minJumpJet = minJumpJetFilter.get();

        @Override
        public boolean test(Loadout aLoadout) {
            Chassis chassis = aLoadout.getChassis();
            if (!chassis.getFaction().isCompatible(faction)) {
                return false;
            }
            if (!hero && chassis.getVariantType() != ChassisVariant.NORMAL) {
                return false;
            }
            if (chassis.getMassMax() < minMass) {
                return false;
            }
            if (chassis.getMassMax() > maxMass) {
                return false;
            }
            if (!checkEngine(aLoadout)) {
                return false;
            }
            if (!canMatchHardpoints(aLoadout)) {
                return false;
            }
            return true;
        }

        /**
         * @param aLoadout
         * @return
         */
        private boolean canMatchHardpoints(Loadout aLoadout) {
            if (aLoadout instanceof LoadoutStandard) {
                LoadoutStandard loadoutStandard = (LoadoutStandard) aLoadout;
                ChassisStandard chassis = loadoutStandard.getChassis();

                if (ecm && chassis.getHardPointsCount(HardPointType.ECM) < 1) {
                    return false;
                }
                if (minJumpJet > chassis.getJumpJetsMax()) {
                    return false;
                }
                if (minEnergy > chassis.getHardPointsCount(HardPointType.ENERGY)) {
                    return false;
                }
                if (minBallistic > chassis.getHardPointsCount(HardPointType.BALLISTIC)) {
                    return false;
                }
                if (minMissile > chassis.getHardPointsCount(HardPointType.MISSILE)) {
                    return false;
                }
                return true;
            }
            else if (aLoadout instanceof LoadoutOmniMech) {
                LoadoutOmniMech loadoutOmniMech = (LoadoutOmniMech) aLoadout;
                ChassisOmniMech chassis = loadoutOmniMech.getChassis();

                Optional<Map<Location, OmniPod>> pods = omniPodSelector.selectPods(chassis, minEnergy, minMissile,
                        minBallistic, minJumpJet, ecm);

                if (!pods.isPresent())
                    return false;

                for (Entry<Location, OmniPod> entry : pods.get().entrySet()) {
                    loadoutOmniMech.getComponent(entry.getKey()).changeOmniPod(entry.getValue());
                }

                return true;
            }
            return false;
        }

        /**
         * Checks if the loadout can have an engine that will match the given speed.
         * 
         * @param aLoadout
         *            The loadout to check.
         * @return <code>true</code> if the loadout can sustain the min-speed, false otherwise.
         */
        private boolean checkEngine(Loadout aLoadout) {
            final Collection<Modifier> modifiers = aLoadout.getModifiers();
            final MovementProfile mp = aLoadout.getChassis().getMovementProfileBase();
            final int rating;
            if (aLoadout instanceof LoadoutOmniMech) {
                final ChassisOmniMech chassis = ((LoadoutOmniMech) aLoadout).getChassis();
                rating = chassis.getFixedEngine().getRating();
            }
            else if (aLoadout instanceof LoadoutStandard) {
                final ChassisStandard chassis = ((LoadoutStandard) aLoadout).getChassis();
                rating = chassis.getEngineMax();
            }
            else {
                throw new RuntimeException("Unknown loadout type!");
            }
            final double speed = TopSpeed.calculate(rating, mp, aLoadout.getChassis().getMassMax(), modifiers);
            return speed >= minSpeed;

            // if (aLoadout instanceof LoadoutOmniMech) {
            // final LoadoutOmniMech loadoutOmniMech = (LoadoutOmniMech) aLoadout;
            // final ChassisOmniMech chassis = loadoutOmniMech.getChassis();
            // final int rating = chassis.getFixedEngine().getRating();
            // final double speed = TopSpeed.calculate(rating, movementProfileBase, chassis.getMassMax(), modifiers);
            // return speed >= minSpeed;
            // }
            // else if (aLoadout instanceof LoadoutStandard) {
            // LoadoutStandard loadoutStandard = (LoadoutStandard) aLoadout;
            // ChassisStandard chassis = loadoutStandard.getChassis();
            //
            // // Binary search for the smallest engine that reaches the min-speed
            // int min = chassis.getEngineMin() / 5;
            // int max = chassis.getEngineMax() / 5 + 1;
            // while (max - min > 1) {
            // int pivot = min + (max - min) / 2;
            //
            // double speed = TopSpeed.calculate(pivot * 5, movementProfileBase, chassis.getMassMax(), modifiers);
            // if (speed < minSpeed) {
            // min = pivot;
            // }
            // else {
            // max = pivot;
            // }
            // }
            //
            // final int rating = max * 5;
            // return rating <= chassis.getEngineMax();
            // }
            // else {
            // throw new RuntimeException("Unknown loadout type!");
            // }
        }
    }

    private void updateFilter() {
        filtered.setPredicate(null);
        filtered.setPredicate(filter);
    }

    /**
     * Creates a new {@link ChassisFilter}.
     * 
     * @param aChassis
     *            The list of chassis to consider.
     * @param aLoadoutFactory
     *            A factory for constructing loadouts.
     * @param aOmniPodSelector
     *            A {@link OmniPodSelector} to use for satisfying hard points on omni mechs.
     * @param aSettings
     *            A {@link Settings} object to use for reading the settings to use for building the empty loadouts.
     */
    public ChassisFilter(List<Chassis> aChassis, LoadoutFactory aLoadoutFactory, OmniPodSelector aOmniPodSelector,
            Settings aSettings) {
        omniPodSelector = aOmniPodSelector;
        for (Chassis chassis : aChassis) {
            loadouts.add(aLoadoutFactory.produceDefault(chassis, aSettings));
        }
        updateFilter();

        factionFilter.addListener((aObs, aOld, aNew) -> {
            filter.faction = aNew;
            updateFilter();
        });

        heroFilter.addListener((aObs, aOld, aNew) -> {
            filter.hero = aNew;
            updateFilter();
        });

        minMassFilter.addListener((aObs, aOld, aNew) -> {
            filter.minMass = aNew.intValue();
            updateFilter();
        });

        maxMassFilter.addListener((aObs, aOld, aNew) -> {
            filter.maxMass = aNew.intValue();
            updateFilter();
        });

        minSpeedFilter.addListener((aObs, aOld, aNew) -> {
            filter.minSpeed = aNew.intValue();
            updateFilter();
        });

        ecmFilter.addListener((aObs, aOld, aNew) -> {
            filter.ecm = aNew;
            updateFilter();
        });

        minBallisticFilter.addListener((aObs, aOld, aNew) -> {
            filter.minBallistic = aNew.intValue();
            updateFilter();
        });

        minMissileFilter.addListener((aObs, aOld, aNew) -> {
            filter.minMissile = aNew.intValue();
            updateFilter();
        });

        minEnergyFilter.addListener((aObs, aOld, aNew) -> {
            filter.minEnergy = aNew.intValue();
            updateFilter();
        });

        minJumpJetFilter.addListener((aObs, aOld, aNew) -> {
            filter.minJumpJet = aNew.intValue();
            updateFilter();
        });
    }

    /**
     * Gets a list of loadouts configured so that they will match the filter criteria.
     * 
     * @return An {@link ObservableList} of {@link Loadout}s.
     */
    public ObservableList<Loadout> getChildren() {
        return FXCollections.unmodifiableObservableList(filtered);
    }

    /**
     * @return An {@link ObjectProperty} of {@link Faction} to filter on.
     */
    public ObjectProperty<Faction> factionFilterProperty() {
        return factionFilter;
    }

    /**
     * @return A {@link BooleanProperty} to filter hero mechs on.
     */
    public BooleanProperty heroFilterProperty() {
        return heroFilter;
    }

    /**
     * @return A {@link IntegerProperty} to filter chassis by minimum mass.
     */
    public IntegerProperty minMassFilterProperty() {
        return minMassFilter;
    }

    /**
     * @return A {@link IntegerProperty} to filter chassis by maximum mass.
     */
    public IntegerProperty maxMassFilterProperty() {
        return maxMassFilter;
    }

    /**
     * @return A {@link IntegerProperty} to filter chassis by minimum speed.
     */
    public IntegerProperty minSpeedFilterProperty() {
        return minSpeedFilter;
    }

    /**
     * @return A {@link BooleanProperty} to filter mechs by ECM capability.
     */
    public BooleanProperty ecmFilterProperty() {
        return ecmFilter;
    }

    /**
     * @return A {@link IntegerProperty} to filter chassis by minimum ballistic hard points.
     */
    public IntegerProperty minBallisticFilterProperty() {
        return minBallisticFilter;
    }

    /**
     * @return A {@link IntegerProperty} to filter chassis by minimum missile hard points.
     */
    public IntegerProperty minEnergyFilterProperty() {
        return minEnergyFilter;
    }

    /**
     * @return A {@link IntegerProperty} to filter chassis by minimum missile hard points.
     */
    public IntegerProperty minMissileFilterProperty() {
        return minMissileFilter;
    }

    /**
     * @return A {@link IntegerProperty} to filter chassis by minimum jump jets.
     */
    public IntegerProperty minJumpJetFilterProperty() {
        return minJumpJetFilter;
    }
}
