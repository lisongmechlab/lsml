/*
 * Li Song Mechlab - A 'mech building tool for PGI's MechWarrior: Online.
 * Copyright (C) 2013-2023  Li Song
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
package org.lisoft.lsml.view_fx.controls;

import java.util.Collection;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Optional;
import java.util.function.Predicate;
import javafx.beans.property.*;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javax.inject.Inject;
import org.lisoft.lsml.model.OmniPodSelector;
import org.lisoft.lsml.model.loadout.Loadout;
import org.lisoft.lsml.model.loadout.LoadoutFactory;
import org.lisoft.lsml.model.loadout.LoadoutOmniMech;
import org.lisoft.lsml.model.loadout.LoadoutStandard;
import org.lisoft.lsml.model.metrics.TopSpeed;
import org.lisoft.lsml.view_fx.Settings;
import org.lisoft.mwo_data.Faction;
import org.lisoft.mwo_data.mechs.*;
import org.lisoft.mwo_data.modifiers.Modifier;

/**
 * This class maintains an observable list of chassis based on certain filters.
 *
 * @author Li Song
 */
public class ChassisFilter {
  private class Filter implements Predicate<Loadout> {
    private boolean ecm = ecmFilter.get();
    private Faction faction = factionFilter.get();
    private boolean hero = heroFilter.get();
    private boolean masc = mascFilter.get();
    private int maxMass = maxMassFilter.get();
    private int minBallistic = minBallisticFilter.get();
    private int minEnergy = minEnergyFilter.get();
    private int minJumpJet = minJumpJetFilter.get();
    private int minMass = minMassFilter.get();
    private int minMissile = minMissileFilter.get();
    private int minSpeed = minSpeedFilter.get();

    @Override
    public boolean test(Loadout aLoadout) {
      final Chassis chassis = aLoadout.getChassis();
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
      return canMatchHardpoints(aLoadout);
    }

    /**
     * @param aLoadout
     * @return
     */
    private boolean canMatchHardpoints(Loadout aLoadout) {

      if (masc && !aLoadout.getChassis().isMascCapable()) {
        // XXX: Are there Omnimechs that don't have MASC capable set but have fixed MASC in omnipod?
        return false;
      }

      if (aLoadout instanceof final LoadoutStandard loadoutStandard) {
        final ChassisStandard chassis = loadoutStandard.getChassis();

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
        return minMissile <= chassis.getHardPointsCount(HardPointType.MISSILE);
      } else if (aLoadout instanceof final LoadoutOmniMech loadoutOmniMech) {
        final ChassisOmniMech chassis = loadoutOmniMech.getChassis();

        final Optional<Map<Location, OmniPod>> pods =
            omniPodSelector.selectPods(
                chassis, minEnergy, minMissile, minBallistic, minJumpJet, ecm);

        if (!pods.isPresent()) {
          return false;
        }

        for (final Entry<Location, OmniPod> entry : pods.get().entrySet()) {
          loadoutOmniMech.getComponent(entry.getKey()).changeOmniPod(entry.getValue());
        }

        return true;
      }
      return false;
    }

    /**
     * Checks if the loadout can have an engine that will match the given speed.
     *
     * @param aLoadout The loadout to check.
     * @return <code>true</code> if the loadout can sustain the min-speed, false otherwise.
     */
    private boolean checkEngine(Loadout aLoadout) {
      final Collection<Modifier> modifiers = aLoadout.getAllModifiers();
      final MovementProfile mp = aLoadout.getChassis().getMovementProfileBase();
      final int rating;
      if (aLoadout instanceof LoadoutOmniMech) {
        final ChassisOmniMech chassis = ((LoadoutOmniMech) aLoadout).getChassis();
        rating = chassis.getFixedEngine().getRating();
      } else if (aLoadout instanceof LoadoutStandard) {
        final ChassisStandard chassis = ((LoadoutStandard) aLoadout).getChassis();
        rating = chassis.getEngineMax();
      } else {
        throw new RuntimeException("Unknown loadout type!");
      }
      final double speed =
          TopSpeed.calculate(rating, mp, aLoadout.getChassis().getMassMax(), modifiers);
      return speed >= minSpeed;
    }
  }

  private final BooleanProperty ecmFilter = new SimpleBooleanProperty(false);
  private final ObjectProperty<Faction> factionFilter = new SimpleObjectProperty<>(Faction.ANY);
  private final Filter filter;
  private final BooleanProperty heroFilter = new SimpleBooleanProperty(true);
  private final LoadoutFactory loadoutFactory;
  private final ObservableList<Loadout> loadouts = FXCollections.observableArrayList();
  private final FilteredList<Loadout> filtered = new FilteredList<>(loadouts);
  private final BooleanProperty mascFilter = new SimpleBooleanProperty(false);
  private final IntegerProperty maxMassFilter = new SimpleIntegerProperty(100);
  private final IntegerProperty minBallisticFilter = new SimpleIntegerProperty(0);
  private final IntegerProperty minEnergyFilter = new SimpleIntegerProperty(0);
  private final IntegerProperty minJumpJetFilter = new SimpleIntegerProperty(0);
  private final IntegerProperty minMassFilter = new SimpleIntegerProperty(0);
  private final IntegerProperty minMissileFilter = new SimpleIntegerProperty(0);
  private final IntegerProperty minSpeedFilter = new SimpleIntegerProperty(0);
  private final OmniPodSelector omniPodSelector;
  private final Settings settings;

  /**
   * Creates a new {@link ChassisFilter}.
   *
   * @param aLoadoutFactory A factory for constructing loadouts.
   * @param aOmniPodSelector A {@link OmniPodSelector} to use for satisfying hard points on omni
   *     mechs.
   * @param aSettings A {@link Settings} object to use for reading the settings to use for building
   *     the empty loadouts.
   */
  @Inject
  public ChassisFilter(
      LoadoutFactory aLoadoutFactory, OmniPodSelector aOmniPodSelector, Settings aSettings) {
    settings = aSettings;
    loadoutFactory = aLoadoutFactory;
    omniPodSelector = aOmniPodSelector;
    filter = new Filter();

    factionFilter.addListener(
        (aObs, aOld, aNew) -> {
          filter.faction = aNew;
          updateFilter();
        });

    heroFilter.addListener(
        (aObs, aOld, aNew) -> {
          filter.hero = aNew;
          updateFilter();
        });

    minMassFilter.addListener(
        (aObs, aOld, aNew) -> {
          filter.minMass = aNew.intValue();
          updateFilter();
        });

    maxMassFilter.addListener(
        (aObs, aOld, aNew) -> {
          filter.maxMass = aNew.intValue();
          updateFilter();
        });

    minSpeedFilter.addListener(
        (aObs, aOld, aNew) -> {
          filter.minSpeed = aNew.intValue();
          updateFilter();
        });

    ecmFilter.addListener(
        (aObs, aOld, aNew) -> {
          filter.ecm = aNew;
          updateFilter();
        });

    mascFilter.addListener(
        (aObs, aOld, aNew) -> {
          filter.masc = aNew;
          updateFilter();
        });

    minBallisticFilter.addListener(
        (aObs, aOld, aNew) -> {
          filter.minBallistic = aNew.intValue();
          updateFilter();
        });

    minMissileFilter.addListener(
        (aObs, aOld, aNew) -> {
          filter.minMissile = aNew.intValue();
          updateFilter();
        });

    minEnergyFilter.addListener(
        (aObs, aOld, aNew) -> {
          filter.minEnergy = aNew.intValue();
          updateFilter();
        });

    minJumpJetFilter.addListener(
        (aObs, aOld, aNew) -> {
          filter.minJumpJet = aNew.intValue();
          updateFilter();
        });
  }

  /**
   * @return A {@link BooleanProperty} to filter mechs by ECM capability.
   */
  public BooleanProperty ecmFilterProperty() {
    return ecmFilter;
  }

  /**
   * @return An {@link ObjectProperty} of {@link Faction} to filter on.
   */
  public ObjectProperty<Faction> factionFilterProperty() {
    return factionFilter;
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
   * @return A {@link BooleanProperty} to filter hero mechs on.
   */
  public BooleanProperty heroFilterProperty() {
    return heroFilter;
  }

  /**
   * @return A {@link BooleanProperty} to filter chassis by being MASC capable.
   */
  public BooleanProperty mascFilterProperty() {
    return mascFilter;
  }

  /**
   * @return A {@link IntegerProperty} to filter chassis by maximum mass.
   */
  public IntegerProperty maxMassFilterProperty() {
    return maxMassFilter;
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
   * @return A {@link IntegerProperty} to filter chassis by minimum jump jets.
   */
  public IntegerProperty minJumpJetFilterProperty() {
    return minJumpJetFilter;
  }

  /**
   * @return A {@link IntegerProperty} to filter chassis by minimum mass.
   */
  public IntegerProperty minMassFilterProperty() {
    return minMassFilter;
  }

  /**
   * @return A {@link IntegerProperty} to filter chassis by minimum missile hard points.
   */
  public IntegerProperty minMissileFilterProperty() {
    return minMissileFilter;
  }

  /**
   * @return A {@link IntegerProperty} to filter chassis by minimum speed.
   */
  public IntegerProperty minSpeedFilterProperty() {
    return minSpeedFilter;
  }

  public void setAll(Collection<Chassis> aValues) {
    for (final Chassis chassis : aValues) {
      loadouts.add(loadoutFactory.produceDefault(chassis, settings));
    }
    updateFilter();
  }

  private void updateFilter() {
    filtered.setPredicate(null);
    filtered.setPredicate(filter);
  }
}
