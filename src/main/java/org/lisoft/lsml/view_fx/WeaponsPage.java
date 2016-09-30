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
package org.lisoft.lsml.view_fx;

import static org.lisoft.lsml.view_fx.util.FxControlUtils.loadFxmlControl;
import static org.lisoft.lsml.view_fx.util.FxTableUtils.addAttributeColumn;
import static org.lisoft.lsml.view_fx.util.FxTableUtils.addStatColumn;
import static org.lisoft.lsml.view_fx.util.FxTableUtils.makeAttributeColumn;
import static org.lisoft.lsml.view_fx.util.FxTableUtils.makePropertyColumn;

import java.util.function.Predicate;

import org.lisoft.lsml.model.chassi.HardPointType;
import org.lisoft.lsml.model.datacache.ItemDB;
import org.lisoft.lsml.model.item.Faction;
import org.lisoft.lsml.model.item.ItemComparator;
import org.lisoft.lsml.model.item.Weapon;

import javafx.beans.binding.ObjectExpression;
import javafx.collections.FXCollections;
import javafx.collections.transformation.FilteredList;
import javafx.collections.transformation.SortedList;
import javafx.fxml.FXML;
import javafx.scene.control.CheckBox;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.layout.BorderPane;

/**
 * This class is a controller for the weapons statistics page.
 *
 * @author Li Song
 */
public class WeaponsPage extends BorderPane {
    @FXML
    private TableView<Weapon> weapons;
    @FXML
    private CheckBox showEnergy;
    @FXML
    private CheckBox showBallistic;
    @FXML
    private CheckBox showMissile;
    private final ObjectExpression<Faction> faction;

    private final Predicate<Weapon> predicate;
    final FilteredList<Weapon> filtered;
    @FXML
    private CheckBox showMisc;

    /**
     * @param aFactionFilter
     *            A observable faction to filter the results on.
     */
    public WeaponsPage(ObjectExpression<Faction> aFactionFilter) {
        loadFxmlControl(this);
        faction = aFactionFilter;
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

        predicate = aWeapon -> {
            if (aWeapon.getHardpointType() == HardPointType.ENERGY && !showEnergy.isSelected() || //
            aWeapon.getHardpointType() == HardPointType.MISSILE && !showMissile.isSelected() || //
            aWeapon.getHardpointType() == HardPointType.BALLISTIC && !showBallistic.isSelected() || //
            !aWeapon.isOffensive() && !showMisc.isSelected()) {
                return false;
            }
            return aWeapon.getFaction().isCompatible(faction.getValue());
        };

        filtered = new FilteredList<>(FXCollections.observableArrayList(ItemDB.lookup(Weapon.class)), predicate);
        final SortedList<Weapon> sorted = new SortedList<>(filtered);
        sorted.comparatorProperty().bind(weapons.comparatorProperty());
        weapons.setItems(sorted);
        weapons.getColumns().clear();

        final TableColumn<Weapon, String> nameCol = makePropertyColumn("Name", "name");
        nameCol.setComparator(ItemComparator.WEAPONS_NATURAL_STRING);
        weapons.getColumns().add(nameCol);

        addAttributeColumn(weapons, "Mass", "mass", "The weight of the weapon.");
        addAttributeColumn(weapons, "Slots", "slots", "The number of critical slots occupied by the weapon.");
        addAttributeColumn(weapons, "HP", "health", "The amount of hit points the weapon has.");
        addStatColumn(weapons, "Dmg", "d", "The alpha strike damage of the weapon.");
        addStatColumn(weapons, "CD", "s", "The average cool down time of the weapon. Assumes double tap for Ultra AC type weapons.");
        addStatColumn(weapons, "Heat", "h", "The heat generate by one shot.");
        addAttributeColumn(weapons, "Impulse", "impulse", "The impulse (cockpit shake) imparted on the target when hit.");
        addAttributeColumn(weapons, "Speed", "projectileSpeed", "The speed of the projectile.");

        final TableColumn<Weapon, String> range = new TableColumn<>("Range");
        range.getColumns().clear();
        range.getColumns().add(makeAttributeColumn("Min", "rangeMin", "The minimum range of the weapon, closer than this the weapon does no or less damage."));
        range.getColumns().add(makeAttributeColumn("Long", "rangeLong", "The longest range at which the weapon will do full damage."));
        range.getColumns().add(makeAttributeColumn("Max", "rangeMax", "The range at which the weapon does no damage, the fall-off from Long to Max is linear."));
        weapons.getColumns().add(range);

        addStatColumn(weapons, "DPS", "d/s", "Damage per Second");
        addStatColumn(weapons, "DPH", "d/h", "Damage per Heat");
        addStatColumn(weapons, "DPT", "d/t", "Damage per Ton");
        addStatColumn(weapons, "DPST", "d/st", "Damage per Second per Ton");
        addStatColumn(weapons, "HPS", "h/s", "Heat per Second");

        weapons.getSortOrder().add(weapons.getColumns().get(0));
    }

    private void refresh() {
        filtered.setPredicate(null);
        filtered.setPredicate(predicate);
    }
}
