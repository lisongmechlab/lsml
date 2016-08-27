/*
 * @formatter:off
 * Li Song Mechlab - A 'mech building tool for PGI's MechWarrior: Online.
 * Copyright (C) 2013  Emily Björk
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
 * @author Emily Björk
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

        addAttributeColumn(weapons, "Mass", "mass");
        addAttributeColumn(weapons, "Slots", "slots");
        addAttributeColumn(weapons, "HP", "health");
        addStatColumn(weapons, "Dmg", "d");
        addStatColumn(weapons, "RoF", "s");
        addStatColumn(weapons, "Heat", "h");
        addAttributeColumn(weapons, "Impulse", "impulse");
        addAttributeColumn(weapons, "Speed", "projectileSpeed");

        final TableColumn<Weapon, String> range = new TableColumn<>("Range");
        range.getColumns().clear();
        range.getColumns().add(makeAttributeColumn("Min", "rangeMin"));
        range.getColumns().add(makeAttributeColumn("Long", "rangeLong"));
        range.getColumns().add(makeAttributeColumn("Max", "rangeMax"));
        weapons.getColumns().add(range);

        addStatColumn(weapons, "DPS", "d/s");
        addStatColumn(weapons, "DPH", "d/h");
        addStatColumn(weapons, "DPT", "d/t");
        addStatColumn(weapons, "DPST", "d/st");
        addStatColumn(weapons, "HPS", "h/s");

        weapons.getSortOrder().add(weapons.getColumns().get(0));
    }

    private void refresh() {
        filtered.setPredicate(null);
        filtered.setPredicate(predicate);
    }
}
