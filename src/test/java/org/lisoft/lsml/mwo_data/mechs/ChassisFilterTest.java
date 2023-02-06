/*
 * Li Song Mechlab - A 'mech building tool for PGI's MechWarrior: Online.
 * Copyright (C) 2023  Li Song
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
package org.lisoft.lsml.mwo_data.mechs;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertSame;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.*;
import java.util.stream.Collectors;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.collections.ObservableList;
import org.junit.Before;
import org.junit.Test;
import org.lisoft.lsml.model.OmniPodSelector;
import org.lisoft.lsml.model.loadout.DefaultLoadoutFactory;
import org.lisoft.lsml.model.loadout.Loadout;
import org.lisoft.lsml.model.loadout.LoadoutFactory;
import org.lisoft.lsml.model.metrics.TopSpeed;
import org.lisoft.lsml.mwo_data.*;
import org.lisoft.lsml.mwo_data.equipment.UpgradeDB;
import org.lisoft.lsml.view_fx.Settings;
import org.lisoft.lsml.view_fx.controls.ChassisFilter;

public class ChassisFilterTest {

  private final LoadoutFactory factory = mock(LoadoutFactory.class);
  private final OmniPodSelector omniPodSelector = mock(OmniPodSelector.class);
  private final Settings settings = mock(Settings.class);
  private List<Chassis> chassis = new ArrayList<>();
  private ChassisFilter cut;

  @Before
  public void setup() {
    chassis.addAll(ChassisDB.lookup(ChassisClass.LIGHT));
    chassis.addAll(ChassisDB.lookup(ChassisClass.MEDIUM));
    chassis.addAll(ChassisDB.lookup(ChassisClass.HEAVY));
    chassis.addAll(ChassisDB.lookup(ChassisClass.ASSAULT));

    when(settings.getProperty(anyString(), eq(Boolean.class)))
        .thenReturn(new SimpleBooleanProperty(false));
    when(settings.getBoolean(anyString())).thenReturn(new SimpleBooleanProperty(false));

    setupDefaultUpgrade(Settings.UPGRADES_DEFAULT_CLAN_ARMOUR, UpgradeDB.CLAN_STD_ARMOUR_ID);
    setupDefaultUpgrade(Settings.UPGRADES_DEFAULT_IS_ARMOUR, UpgradeDB.IS_STD_ARMOUR_ID);
    setupDefaultUpgrade(Settings.UPGRADES_DEFAULT_CLAN_STRUCTURE, UpgradeDB.CLAN_STD_STRUCTURE_ID);
    setupDefaultUpgrade(Settings.UPGRADES_DEFAULT_IS_STRUCTURE, UpgradeDB.IS_STD_STRUCTURE_ID);
    setupDefaultUpgrade(Settings.UPGRADES_DEFAULT_CLAN_HEAT_SINKS, UpgradeDB.CLAN_SHS_ID);
    setupDefaultUpgrade(Settings.UPGRADES_DEFAULT_IS_HEAT_SINKS, UpgradeDB.IS_SHS_ID);

    final LoadoutFactory loadoutFactory = new DefaultLoadoutFactory();
    when(factory.produceDefault(any(Chassis.class), eq(settings)))
        .then(aInvocation -> loadoutFactory.produceDefault(aInvocation.getArgument(0), settings));

    cut = new ChassisFilter(factory, omniPodSelector, settings);
  }

  @Test
  public void testChassisFilter() {
    acceptAllOmniMechHardpoints();
    cut.setAll(chassis);
    final ObservableList<Loadout> loadouts = cut.getChildren();
    assertEquals(chassis.size(), loadouts.size());
  }

  @Test
  public void testEcmFilter_Standard() {
    final Chassis cda_3m = ChassisDB.lookup("CDA-3M");

    chassis.clear();
    chassis.add(cda_3m);
    chassis.add(ChassisDB.lookup("CDA-2B")); // ECM capable

    cut.setAll(chassis);
    cut.ecmFilterProperty().set(true);

    final List<Loadout> loadouts = new ArrayList<>(cut.getChildren());
    assertEquals(1, loadouts.size());
    assertSame(cda_3m, loadouts.get(0).getChassis());
  }

  @Test
  public void testFactionFilter() {
    chassis =
        chassis.stream()
            .filter(aChassis -> aChassis.getFaction() == Faction.CLAN)
            .collect(Collectors.toList());

    acceptAllOmniMechHardpoints();

    cut.setAll(chassis);
    cut.factionFilterProperty().set(Faction.INNERSPHERE);

    final ObservableList<Loadout> loadouts = cut.getChildren();
    assertEquals(0, loadouts.size());
  }

  @Test
  public void testHeroFilter() {
    chassis =
        chassis.stream()
            .filter(aChassis -> aChassis.getVariantType() != ChassisVariant.NORMAL)
            .collect(Collectors.toList());

    acceptAllOmniMechHardpoints();

    cut.setAll(chassis);
    cut.heroFilterProperty().set(false);

    final ObservableList<Loadout> loadouts = cut.getChildren();
    assertEquals(0, loadouts.size());
  }

  @Test
  public void testMascFilter_OmniChassis() {
    final Chassis hasMasc = ChassisDB.lookup("EXE-PRIME");
    final Chassis nonMasc = ChassisDB.lookup("MLX-A");

    acceptAllOmniMechHardpoints();

    chassis.clear();
    chassis.add(hasMasc);
    chassis.add(nonMasc);

    cut.setAll(chassis);
    cut.mascFilterProperty().set(true);

    final List<Loadout> loadouts = new ArrayList<>(cut.getChildren());
    assertEquals(1, loadouts.size());
    assertSame(hasMasc, loadouts.get(0).getChassis());
  }

  @Test
  public void testMascFilter_StdChassis() {
    final Chassis hasMasc = ChassisDB.lookup("KDK-SB");
    final Chassis nonMasc = ChassisDB.lookup("KDK-4");

    chassis.clear();
    chassis.add(hasMasc);
    chassis.add(nonMasc);

    cut.setAll(chassis);
    cut.mascFilterProperty().set(true);

    final List<Loadout> loadouts = new ArrayList<>(cut.getChildren());
    assertEquals(1, loadouts.size());
    assertSame(hasMasc, loadouts.get(0).getChassis());
  }

  @Test
  public void testMaxMassFilter() {
    final int maxMass = 60;
    chassis =
        chassis.stream()
            .filter(aChassis -> aChassis.getMassMax() > maxMass)
            .collect(Collectors.toList());

    acceptAllOmniMechHardpoints();

    cut.setAll(chassis);
    cut.maxMassFilterProperty().set(maxMass);

    final ObservableList<Loadout> loadouts = cut.getChildren();
    assertEquals(0, loadouts.size());
  }

  @Test
  public void testMinBallisticFilter_Standard() {
    final int minBallistic = 4;

    chassis =
        chassis.stream()
            .filter(
                aChassis -> {
                  if (aChassis instanceof final ChassisStandard chassisStandard) {
                    return chassisStandard.getHardPointsCount(HardPointType.BALLISTIC)
                        < minBallistic;
                  }
                  return false;
                })
            .collect(Collectors.toList());

    cut.setAll(chassis);
    cut.minBallisticFilterProperty().set(minBallistic);

    final ObservableList<Loadout> loadouts = cut.getChildren();
    assertEquals(0, loadouts.size());
  }

  @Test
  public void testMinEnergyFilter_Standard() {
    final int minEnergy = 4;

    chassis =
        chassis.stream()
            .filter(
                aChassis -> {
                  if (aChassis instanceof final ChassisStandard chassisStandard) {
                    return chassisStandard.getHardPointsCount(HardPointType.ENERGY) < minEnergy;
                  }
                  return false;
                })
            .collect(Collectors.toList());

    cut.setAll(chassis);
    cut.minEnergyFilterProperty().set(minEnergy);

    final ObservableList<Loadout> loadouts = cut.getChildren();
    assertEquals(0, loadouts.size());
  }

  @Test
  public void testMinJumpJetsFilter_Standard() {
    final int minJJ = 4;

    chassis =
        chassis.stream()
            .filter(
                aChassis -> {
                  if (aChassis instanceof final ChassisStandard chassisStandard) {
                    return chassisStandard.getJumpJetsMax() < minJJ;
                  }
                  return false;
                })
            .collect(Collectors.toList());

    cut.setAll(chassis);
    cut.minJumpJetFilterProperty().set(minJJ);

    final ObservableList<Loadout> loadouts = cut.getChildren();
    assertEquals(0, loadouts.size());
  }

  @Test
  public void testMinMassFilter() {
    final int minMass = 75;
    chassis =
        chassis.stream()
            .filter(aChassis -> aChassis.getMassMax() < minMass)
            .collect(Collectors.toList());

    acceptAllOmniMechHardpoints();

    cut.setAll(chassis);
    cut.minMassFilterProperty().set(75);

    final ObservableList<Loadout> loadouts = cut.getChildren();
    assertEquals(0, loadouts.size());
  }

  @Test
  public void testMinMissileFilter_Standard() {
    final int minMissile = 4;

    chassis =
        chassis.stream()
            .filter(
                aChassis -> {
                  if (aChassis instanceof final ChassisStandard chassisStandard) {
                    return chassisStandard.getHardPointsCount(HardPointType.MISSILE) < minMissile;
                  }
                  return false;
                })
            .collect(Collectors.toList());

    cut.setAll(chassis);
    cut.minMissileFilterProperty().set(minMissile);

    final ObservableList<Loadout> loadouts = cut.getChildren();
    assertEquals(0, loadouts.size());
  }

  @Test
  public void testMinSpeedFilter() {
    final int minSpeed = 100;

    chassis =
        chassis.stream()
            .filter(
                aChassis -> {
                  LoadoutFactory factory = new DefaultLoadoutFactory();
                  Loadout loadout = factory.produceEmpty(aChassis);
                  final int rating;
                  if (aChassis instanceof final ChassisStandard chassisStandard) {
                    rating = chassisStandard.getEngineMax();
                  } else {
                    final ChassisOmniMech chassisOmniMech = (ChassisOmniMech) aChassis;
                    rating = chassisOmniMech.getFixedEngine().getRating();
                  }

                  final double speed =
                      TopSpeed.calculate(
                          rating,
                          aChassis.getMovementProfileBase(),
                          aChassis.getMassMax(),
                          loadout.getAllModifiers());

                  return speed < minSpeed;
                })
            .collect(Collectors.toList());

    acceptAllOmniMechHardpoints();

    cut.setAll(chassis);
    cut.minSpeedFilterProperty().set(minSpeed);

    final ObservableList<Loadout> loadouts = cut.getChildren();
    assertEquals(0, loadouts.size());
  }

  private void acceptAllOmniMechHardpoints() {
    final Optional<Map<Location, OmniPod>> pods = Optional.of(new HashMap<>());
    when(omniPodSelector.selectPods(
            any(ChassisOmniMech.class), anyInt(), anyInt(), anyInt(), anyInt(), anyBoolean()))
        .thenReturn(pods);
  }

  void setupDefaultUpgrade(String aKey, int aValue) {
    when(settings.getInteger(aKey)).thenReturn(new SimpleIntegerProperty(aValue).asObject());
  }
}
