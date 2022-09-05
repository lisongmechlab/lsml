/*
 * Li Song Mechlab - A 'mech building tool for PGI's MechWarrior: Online.
 * Copyright (C) 2013-2022  Li Song
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
package org.lisoft.lsml.view_fx.controllers.mainwindow;

import static org.lisoft.lsml.view_fx.util.FxTableUtils.*;

import java.util.function.Predicate;
import javafx.beans.binding.ObjectExpression;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.collections.transformation.SortedList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javax.inject.Inject;
import org.lisoft.lsml.model.chassi.HardPointType;
import org.lisoft.lsml.model.database.ItemDB;
import org.lisoft.lsml.model.item.Faction;
import org.lisoft.lsml.model.item.ItemComparator;
import org.lisoft.lsml.model.item.Weapon;
import org.lisoft.lsml.util.Pair;
import org.lisoft.lsml.view_fx.controllers.AbstractFXController;
import org.lisoft.lsml.view_fx.util.FxBindingUtils;
import org.lisoft.lsml.view_fx.util.FxTableUtils;

/**
 * This class is a controller for the weapons statistics page.
 *
 * @author Li Song
 */
public class WeaponsPageController extends AbstractFXController {
  private final ObjectExpression<Faction> faction;
  private final FilteredList<Weapon> filtered;
  private final Predicate<Weapon> predicate;
  @FXML private RadioButton factionFilterClan;
  @FXML private ToggleGroup factionFilterGroup;
  @FXML private RadioButton factionFilterIS;
  @FXML private CheckBox showBallistic;
  @FXML private CheckBox showEnergy;
  @FXML private CheckBox showMisc;
  @FXML private CheckBox showMissile;
  @FXML private TableView<Weapon> weapons;

  @Inject
  public WeaponsPageController() {
    faction =
        FxBindingUtils.createFactionBinding(
            factionFilterGroup.selectedToggleProperty(), factionFilterClan, factionFilterIS);
    predicate = this::shouldShowWeapon;

    ObservableList<Weapon> sourceList = FXCollections.observableArrayList();
    sourceList.addAll(ItemDB.lookup(Weapon.class));
    filtered = new FilteredList<>(sourceList, predicate);

    faction.addListener((aObs, aOld, aNew) -> refresh());
    showEnergy.selectedProperty().addListener((aObs, aOld, aNew) -> refresh());
    showBallistic.selectedProperty().addListener((aObs, aOld, aNew) -> refresh());
    showMissile.selectedProperty().addListener((aObs, aOld, aNew) -> refresh());
    showMisc.selectedProperty().addListener((aObs, aOld, aNew) -> refresh());

    final SortedList<Weapon> sorted = new SortedList<>(filtered);
    sorted.comparatorProperty().bind(weapons.comparatorProperty());
    weapons.setItems(sorted);
    weapons.getColumns().clear();

    final TableColumn<Weapon, String> nameCol =
        makePropertyColumn("Name", "shortName", "The name of the weapon system.");
    nameCol.setComparator(new ItemComparator.ByString(false));
    weapons.getColumns().add(nameCol);

    addAttributeColumn(weapons, "Mass", "mass", "The weight of the weapon.");
    addAttributeColumn(
        weapons, "Slots", "slots", "The number of critical slots occupied by the weapon.");
    addAttributeColumn(weapons, "HP", "health", "The amount of hit points the weapon has.");
    addStatColumn(
        weapons, "Dmg", "d", "The volley damage of the weapon, for RAC this is per projectile.");
    addStatColumn(
        weapons,
        "rFP",
        "r",
        "The Raw Firing Period (rFP) of the weapon, ignoring spin-up time of RAC.\n"
            + "This is cooldown plus burn time of lasers, charge time of gauss etc.\n"
            + "For MG,RAC and flamers this is the time between projectiles.");
    addStatColumn(
        weapons,
        "eFP",
        "s",
        "Same as rFP but gives you the statistically Expected Firing Period (eFP)\n"
            + "if the trigger is permanently held down and double fire is used on UACs.\n"
            + "It includes jam probabilities, jam clear times, weapon spin up after jam etc.");
    addStatColumn(weapons, "Ht", "h", "The heat generated every firing period.");
    addAttributeColumn(
        weapons, "Imp", "impulse", "The impulse (cockpit shake) imparted on the target when hit.");
    addAttributeColumn(weapons, "Spd", "projectileSpeed", "The travel speed of the projectile.");

    final TableColumn<Weapon, String> range = new TableColumn<>("Range");
    range.getColumns().clear();

    final TableColumn<Weapon, String> range90LB = new TableColumn<>("<90%");
    range90LB.setCellValueFactory(
        aFeatures -> {
          final Pair<Double, Double> pctRange =
              aFeatures.getValue().getRangeProfile().getPercentileRange(0.9, null);
          return FxBindingUtils.formatValue(FxTableUtils.STAT_FMT, false, pctRange.first);
        });
    range90LB.setComparator(FxTableUtils.NUMERICAL_ORDERING);
    addColumnToolTip(
        range90LB, "The shortest range where the weapon will do at least 90% of maximum damage.");

    final TableColumn<Weapon, String> range90UB = new TableColumn<>("max (90%)");
    range90UB.setCellValueFactory(
        aFeatures -> {
          final Pair<Double, Double> pctRange =
              aFeatures.getValue().getRangeProfile().getPercentileRange(0.9, null);
          return FxBindingUtils.formatValue(FxTableUtils.STAT_FMT, true, pctRange.second);
        });
    range90UB.setComparator(FxTableUtils.NUMERICAL_ORDERING);
    addColumnToolTip(
        range90UB, "The longest range at which the weapon will do at least 90% of maximum damage.");

    range.getColumns().add(range90LB);
    range.getColumns().add(range90UB);
    range
        .getColumns()
        .add(
            makeAttributeColumn(
                "Max",
                "rangeMax",
                "The range at which the weapon does no damage, the fall-off from Long to Max is linear."));
    weapons.getColumns().add(range);
    addColumnToolTip(
        range, "The range properties of the weapon. A hyphen (-) indicates not applicable.");

    addStatColumn(
        weapons,
        "rDPS",
        "d/r",
        "Raw Damage Per Second (rDPS): Damage divided by raw firing period");
    addStatColumn(
        weapons,
        "eDPS",
        "d/s",
        "Expected Damage Per Second (eDPS): Damage divided by expected firing period");
    addStatColumn(weapons, "DPH", "d/h", "Damage per Heat");
    addStatColumn(weapons, "DPT", "d/t", "Damage per Ton");
    addStatColumn(weapons, "eDPST", "d/st", "Expected Damage per Second per Ton");
    addStatColumn(weapons, "eHPS", "h/s", "Expected Heat per Second");
    weapons.setColumnResizePolicy((x) -> true);

    weapons.getSortOrder().add(weapons.getColumns().get(0));

    // TODO: Sizing TableViews is hard:
    // https://stackoverflow.com/questions/23284437/javafx-tablecolumn-resize-to-fit-cell-content
    // https://stackoverflow.com/questions/38090353/javafx-how-automatically-width-of-tableview-column-depending-on-the-content
    // https://stackoverflow.com/questions/56508235/where-javafx-tableview-width-comes-from
    // plus many more.
    // TL;DR: There's a default preferred column width that's 80 px (96DPI) and this then determines
    // the width of the table during initial layout. The workarounds access a private API
    // that's hidden in Java9+ to resize the columns, apart from being undesirable, I couldn't get
    // this to work with the time I have right now. It seems that we need some fancy way to
    // calculate and set the prefWidth of each column reliably some examples render text with some
    // random font off-screen to determine how wide the column needs to be but this doesn't take
    // styles from style sheets into account and seem brittle. Then it'd need to compute the actual
    // prefWidth of the table based on that and set it before we return from this function.
    weapons.setPrefWidth(1100);
  }

  private void refresh() {
    filtered.setPredicate(null);
    filtered.setPredicate(predicate);
  }

  private boolean shouldShowWeapon(Weapon aWeapon) {
    if (aWeapon.getHardpointType() == HardPointType.ENERGY && !showEnergy.isSelected()
        || aWeapon.getHardpointType() == HardPointType.MISSILE && !showMissile.isSelected()
        || aWeapon.getHardpointType() == HardPointType.BALLISTIC && !showBallistic.isSelected()
        || !aWeapon.isOffensive() && !showMisc.isSelected()) {
      return false;
    }
    return aWeapon.getFaction().isCompatible(faction.getValue());
  }
}
