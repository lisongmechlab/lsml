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

import org.lisoft.lsml.model.datacache.ItemDB;
import org.lisoft.lsml.model.item.BallisticWeapon;
import org.lisoft.lsml.model.item.EnergyWeapon;
import org.lisoft.lsml.model.item.Faction;
import org.lisoft.lsml.model.item.MissileWeapon;
import org.lisoft.lsml.model.item.Weapon;

import javafx.beans.binding.ObjectExpression;
import javafx.collections.FXCollections;
import javafx.collections.transformation.FilteredList;
import javafx.collections.transformation.SortedList;
import javafx.fxml.FXML;
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
    private TableView<Weapon> missileWeapons;
    @FXML
    private TableView<Weapon> ballisticWeapons;
    @FXML
    private TableView<Weapon> energyWeapons;

    /**
     * @param aFactionFilter
     *            A observable faction to filter the results on.
     */
    public WeaponsPage(ObjectExpression<Faction> aFactionFilter) {
        loadFxmlControl(this);

        Faction faction = aFactionFilter.get();
        FilteredList<Weapon> filteredMissiles = setupTable(missileWeapons, MissileWeapon.class, faction);
        FilteredList<Weapon> filteredBallistics = setupTable(ballisticWeapons, BallisticWeapon.class, faction);
        FilteredList<Weapon> filteredEnergy = setupTable(energyWeapons, EnergyWeapon.class, faction);

        aFactionFilter.addListener((aObs, aOld, aNew) -> {
            Predicate<? super Weapon> p = aWeapon -> {
                return aWeapon.getFaction().isCompatible(aNew);
            };

            filteredMissiles.setPredicate(p);
            filteredBallistics.setPredicate(p);
            filteredEnergy.setPredicate(p);
        });
    }

    private FilteredList<Weapon> setupTable(TableView<Weapon> aTable, Class<? extends Weapon> aClass,
            Faction aFaction) {
        FilteredList<Weapon> filtered = new FilteredList<>(FXCollections.observableArrayList(ItemDB.lookup(aClass)),
                aWeapon -> aWeapon.getFaction().isCompatible(aFaction));
        SortedList<Weapon> sorted = new SortedList<>(filtered);
        sorted.comparatorProperty().bind(aTable.comparatorProperty());
        aTable.setItems(sorted);
        aTable.getColumns().clear();

        TableColumn<Weapon, String> nameCol = makePropertyColumn("Name", "name");
        nameCol.setComparator(Weapon.DEFAULT_WEAPON_ORDERING_STR);
        aTable.getColumns().add(nameCol);

        addAttributeColumn(aTable, "Mass", "mass");
        addAttributeColumn(aTable, "Slots", "slots");
        addAttributeColumn(aTable, "HP", "health");
        addStatColumn(aTable, "Dmg", "d");
        addStatColumn(aTable, "RoF", "s");
        addStatColumn(aTable, "Heat", "h");
        addAttributeColumn(aTable, "Impulse", "impulse");
        addAttributeColumn(aTable, "Speed", "projectileSpeed");

        TableColumn<Weapon, String> range = new TableColumn<>("Range");
        range.getColumns().clear();
        range.getColumns().add(makeAttributeColumn("Min", "rangeMin"));
        range.getColumns().add(makeAttributeColumn("Long", "rangeLong"));
        range.getColumns().add(makeAttributeColumn("Max", "rangeMax"));
        aTable.getColumns().add(range);

        addStatColumn(aTable, "DPS", "d/s");
        addStatColumn(aTable, "DPH", "d/h");
        addStatColumn(aTable, "DPT", "d/t");
        addStatColumn(aTable, "DPST", "d/st");
        addStatColumn(aTable, "HPS", "h/s");

        aTable.getSortOrder().add(aTable.getColumns().get(0));
        return filtered;
    }

}
