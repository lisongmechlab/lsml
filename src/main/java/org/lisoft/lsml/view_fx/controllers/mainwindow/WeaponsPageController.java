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
package org.lisoft.lsml.view_fx.controllers.mainwindow;

import javafx.beans.binding.ObjectExpression;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.collections.transformation.SortedList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import org.lisoft.lsml.model.chassi.HardPointType;
import org.lisoft.lsml.model.database.ItemDB;
import org.lisoft.lsml.model.item.Faction;
import org.lisoft.lsml.model.item.ItemComparator;
import org.lisoft.lsml.model.item.Weapon;
import org.lisoft.lsml.util.Pair;
import org.lisoft.lsml.view_fx.controllers.AbstractFXController;
import org.lisoft.lsml.view_fx.util.FxBindingUtils;
import org.lisoft.lsml.view_fx.util.FxTableUtils;

import javax.inject.Inject;
import java.util.function.Predicate;

import static org.lisoft.lsml.view_fx.util.FxTableUtils.*;

/**
 * This class is a controller for the weapons statistics page.
 *
 * @author Li Song
 */
public class WeaponsPageController extends AbstractFXController {
    final FilteredList<Weapon> filtered;
    private final Predicate<Weapon> predicate;
    private final ObservableList<Weapon> sourceList = FXCollections.observableArrayList();
    private ObjectExpression<Faction> faction;
    @FXML
    private RadioButton factionFilterAll;
    @FXML
    private RadioButton factionFilterClan;
    @FXML
    private ToggleGroup factionFilterGroup;
    @FXML
    private RadioButton factionFilterIS;
    @FXML
    private CheckBox showBallistic;
    @FXML
    private CheckBox showEnergy;
    @FXML
    private CheckBox showMisc;
    @FXML
    private CheckBox showMissile;
    @FXML
    private TableView<Weapon> weapons;

    @Inject
    public WeaponsPageController() {
        predicate = aWeapon -> {
            if (aWeapon.getHardpointType() == HardPointType.ENERGY && !showEnergy.isSelected() || //
                aWeapon.getHardpointType() == HardPointType.MISSILE && !showMissile.isSelected() || //
                aWeapon.getHardpointType() == HardPointType.BALLISTIC && !showBallistic.isSelected() || //
                !aWeapon.isOffensive() && !showMisc.isSelected()) {
                return false;
            }
            return aWeapon.getFaction().isCompatible(faction.getValue());
        };

        filtered = new FilteredList<>(sourceList, predicate);
        faction = FxBindingUtils.createFactionBinding(factionFilterGroup.selectedToggleProperty(), factionFilterClan,
                                                      factionFilterIS);
        faction.addListener((aObs, aOld, aNew) -> {
            refresh();
        });
        showEnergy.selectedProperty().addListener((aObs, aOld, aNew) -> {
            refresh();
        });
        showBallistic.selectedProperty().addListener((aObs, aOld, aNew) -> {
            refresh();
        });
        showMissile.selectedProperty().addListener((aObs, aOld, aNew) -> {
            refresh();
        });
        showMisc.selectedProperty().addListener((aObs, aOld, aNew) -> {
            refresh();
        });

        sourceList.addAll(ItemDB.lookup(Weapon.class));
        // filtered.getSource().(ItemDB.lookup(Weapon.class));

        final SortedList<Weapon> sorted = new SortedList<>(filtered);
        sorted.comparatorProperty().bind(weapons.comparatorProperty());
        weapons.setItems(sorted);
        weapons.getColumns().clear();

        final TableColumn<Weapon, String> nameCol = makePropertyColumn("Name", "shortName",
                                                                       "The name of the weapon system.");
        nameCol.setComparator(new ItemComparator.ByString(false));
        weapons.getColumns().add(nameCol);

        addAttributeColumn(weapons, "Mass", "mass", "The weight of the weapon.");
        addAttributeColumn(weapons, "Slots", "slots", "The number of critical slots occupied by the weapon.");
        addAttributeColumn(weapons, "HP", "health", "The amount of hitpoints the weapon has.");
        addStatColumn(weapons, "Dmg", "d", "The volley damage of the weapon, for RAC this is per projectile.");
        addStatColumn(weapons, "rFP", "r",
                      "The Raw Firing Period (rFP) of the weapon, ignoring spin-up time of RAC.\n" +
                      "This is cooldown plus burn time of lasers, charge time of gauss etc.\n" +
                      "For MG,RAC and flamers this is the time between projectiles.");
        addStatColumn(weapons, "eFP", "s",
                      "Same as rFP but gives you the statistically Expected Firing Period (eFP)\n" +
                      "if the trigger is permanently held down and double fire is used on UACs.\n" +
                      "It includes jam probabilities, jam clear times, weapon spin up after jam etc.");
        addStatColumn(weapons, "Ht", "h", "The heat generated every firing period.");
        addAttributeColumn(weapons, "Imp", "impulse", "The impulse (cockpit shake) imparted on the target when hit.");
        addAttributeColumn(weapons, "Spd", "projectileSpeed", "The travel speed of the projectile.");

        final TableColumn<Weapon, String> range = new TableColumn<>("Range");
        range.getColumns().clear();

        final TableColumn<Weapon, String> range90LB = new TableColumn<>("<90%");
        range90LB.setCellValueFactory(aFeatures -> {
            final Pair<Double, Double> pctRange = aFeatures.getValue().getRangeProfile().getPercentileRange(0.9, null);
            return FxBindingUtils.formatValue(FxTableUtils.STAT_FMT, false, pctRange.first.doubleValue());
        });
        range90LB.setComparator(FxTableUtils.NUMERICAL_ORDERING);
        addColumnToolTip(range90LB, "The shortest range where the weapon will do at least 90% of maximum damage.");

        final TableColumn<Weapon, String> range90UB = new TableColumn<>("max (90%)");
        range90UB.setCellValueFactory(aFeatures -> {
            final Pair<Double, Double> pctRange = aFeatures.getValue().getRangeProfile().getPercentileRange(0.9, null);
            return FxBindingUtils.formatValue(FxTableUtils.STAT_FMT, true, pctRange.second.doubleValue());
        });
        range90UB.setComparator(FxTableUtils.NUMERICAL_ORDERING);
        addColumnToolTip(range90UB, "The longest range at which the weapon will do at least 90% of maximum damage.");

        range.getColumns().add(range90LB);
        range.getColumns().add(range90UB);
        range.getColumns().add(makeAttributeColumn("Max", "rangeMax",
                                                   "The range at which the weapon does no damage, the fall-off from Long to Max is linear."));
        weapons.getColumns().add(range);
        addColumnToolTip(range, "The range properties of the weapon. A hyphen (-) indicates not applicable.");

        addStatColumn(weapons, "rDPS", "d/r", "Raw Damage Per Second (rDPS): Damage divided by raw firing period");
        addStatColumn(weapons, "eDPS", "d/s",
                      "Expected Damage Per Second (eDPS): Damage divided by expected firing period");
        addStatColumn(weapons, "DPH", "d/h", "Damage per Heat");
        addStatColumn(weapons, "DPT", "d/t", "Damage per Ton");
        addStatColumn(weapons, "eDPST", "d/st", "Expected Damage per Second per Ton");
        addStatColumn(weapons, "eHPS", "h/s", "Expected Heat per Second");

        weapons.getSortOrder().add(weapons.getColumns().get(0));
    }

    private void refresh() {
        filtered.setPredicate(null);
        filtered.setPredicate(predicate);
    }
}
