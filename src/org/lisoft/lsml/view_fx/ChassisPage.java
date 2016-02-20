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

import static org.lisoft.lsml.view_fx.util.FxmlHelpers.addAttributeColumn;
import static org.lisoft.lsml.view_fx.util.FxmlHelpers.addPropertyColumn;
import static org.lisoft.lsml.view_fx.util.FxmlHelpers.loadFxmlControl;
import static org.lisoft.lsml.view_fx.util.FxmlHelpers.makeAttributeColumn;

import java.util.Collection;
import java.util.function.Predicate;

import org.lisoft.lsml.model.chassi.Chassis;
import org.lisoft.lsml.model.chassi.ChassisClass;
import org.lisoft.lsml.model.chassi.Location;
import org.lisoft.lsml.model.datacache.ChassisDB;
import org.lisoft.lsml.model.datacache.ModifiersDB;
import org.lisoft.lsml.model.item.Faction;
import org.lisoft.lsml.model.loadout.DefaultLoadoutFactory;
import org.lisoft.lsml.model.loadout.Loadout;
import org.lisoft.lsml.model.loadout.component.ConfiguredComponent;
import org.lisoft.lsml.model.modifiers.Modifier;
import org.lisoft.lsml.view_fx.loadout.component.HardPointPane;
import org.lisoft.lsml.view_fx.style.FilteredModifierFormatter;

import javafx.beans.property.ObjectProperty;
import javafx.beans.property.Property;
import javafx.beans.property.ReadOnlyObjectWrapper;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.collections.transformation.SortedList;
import javafx.fxml.FXML;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.VBox;

/**
 * This is a controller class for the chassis page.
 * 
 * @author Emily Björk
 */
public class ChassisPage extends BorderPane {
    private Settings                               settings           = Settings.getSettings();

    @FXML
    private TableView<Loadout>                     tableLights;
    @FXML
    private TableView<Loadout>                     tableMediums;
    @FXML
    private TableView<Loadout>                     tableHeavies;
    @FXML
    private TableView<Loadout>                     tableAssaults;

    @Deprecated // Inject with DI
    private static final FilteredModifierFormatter MODIFIER_FORMATTER = new FilteredModifierFormatter(
            ModifiersDB.getAllWeaponSelectors());

    private static class ChassisFilter implements Predicate<Loadout> {
        private Faction faction;
        private boolean showVariants;

        public ChassisFilter(Faction aFaction, boolean aShowVariants) {
            faction = aFaction;
            showVariants = aShowVariants;
        }

        @Override
        public boolean test(Loadout aLoadout) {
            Chassis chassis = aLoadout.getChassis();
            if (!showVariants && chassis.getVariantType().isVariation())
                return false;
            return chassis.getFaction().isCompatible(faction);
        }
    }

    public ChassisPage(ObjectProperty<Faction> aFactionFilter) {
        loadFxmlControl(this);

        setupChassisTable(tableLights, ChassisClass.LIGHT, aFactionFilter);
        setupChassisTable(tableMediums, ChassisClass.MEDIUM, aFactionFilter);
        setupChassisTable(tableHeavies, ChassisClass.HEAVY, aFactionFilter);
        setupChassisTable(tableAssaults, ChassisClass.ASSAULT, aFactionFilter);
    }

    private void setupChassisTable(TableView<Loadout> aTable, ChassisClass aChassisClass,
            ObjectProperty<Faction> aFactionFilter) {
        setupTableData(aTable, aChassisClass, aFactionFilter);

        aTable.getColumns().clear();
        addAttributeColumn(aTable, "Name", "chassis.name");
        addAttributeColumn(aTable, "Mass", "chassis.massMax");
        addAttributeColumn(aTable, "Speed", "chassis.massMax"); // XXX
        addAttributeColumn(aTable, "Faction", "chassis.faction");

        TableColumn<Loadout, String> range = new TableColumn<>("Modules");
        range.getColumns().clear();
        range.getColumns().add(makeAttributeColumn("M", "chassis.mechModulesMax"));
        range.getColumns().add(makeAttributeColumn("C", "chassis.consumableModulesMax"));
        range.getColumns().add(makeAttributeColumn("W", "chassis.weaponModulesMax"));
        aTable.getColumns().add(range);

        addHardpointsColumn(aTable, Location.RightArm);
        addHardpointsColumn(aTable, Location.RightTorso);
        addHardpointsColumn(aTable, Location.Head);
        addHardpointsColumn(aTable, Location.CenterTorso);
        addHardpointsColumn(aTable, Location.LeftTorso);
        addHardpointsColumn(aTable, Location.LeftArm);
        addPropertyColumn(aTable, "JJ", "jumpJetsMax");

        TableColumn<Loadout, Collection<Modifier>> col = new TableColumn<>("Weapon Quirks");
        col.setCellValueFactory(aFeatures -> new ReadOnlyObjectWrapper<>(aFeatures.getValue().getModifiers()));
        col.setCellFactory(aView -> new TableCell<Loadout, Collection<Modifier>>() {
            private final VBox box = new VBox();

            @Override
            protected void updateItem(Collection<Modifier> aObject, boolean aEmpty) {
                if (null != aObject && !aEmpty) {
                    box.getChildren().clear();
                    MODIFIER_FORMATTER.format(aObject, box.getChildren());
                    setGraphic(box);
                }
                else {
                    setGraphic(null);
                }
            }
        });
        col.setSortable(false);

        aTable.getColumns().add(col);
    }

    private void addHardpointsColumn(TableView<Loadout> aTable, Location aLocation) {
        TableColumn<Loadout, ConfiguredComponent> col = new TableColumn<>(aLocation.shortName());

        col.setCellValueFactory(aFeatures -> new ReadOnlyObjectWrapper<>(aFeatures.getValue().getComponent(aLocation)));

        col.setCellFactory(aView -> new TableCell<Loadout, ConfiguredComponent>() {
            @Override
            protected void updateItem(ConfiguredComponent aObject, boolean aEmpty) {
                if (null != aObject && !aEmpty) {
                    setGraphic(new HardPointPane(aObject));
                }
                else {
                    setGraphic(null);
                }
            }
        });
        col.setSortable(false);

        aTable.getColumns().add(col);
    }

    private void setupTableData(TableView<Loadout> aTable, ChassisClass aChassisClass,
            ObjectProperty<Faction> aFactionFilter) {
        Property<Boolean> showMechVariants = settings.getProperty(Settings.UI_MECH_VARIANTS, Boolean.class);

        ObservableList<Loadout> loadouts = FXCollections.observableArrayList();
        for (Chassis chassis : ChassisDB.lookup(aChassisClass)) {
            try {
                loadouts.add(DefaultLoadoutFactory.instance.produceStock(chassis));
            }
            catch (Exception e) {
                LiSongMechLab.showError(this, e);
            }
        }

        FilteredList<Loadout> filtered = new FilteredList<>(loadouts,
                new ChassisFilter(aFactionFilter.get(), showMechVariants.getValue()));
        SortedList<Loadout> sorted = new SortedList<>(filtered);
        sorted.comparatorProperty().bind(aTable.comparatorProperty());
        aTable.setItems(sorted);

        showMechVariants.addListener((aObs, aOld, aNew) -> {
            filtered.setPredicate(new ChassisFilter(aFactionFilter.get(), aNew));
            // Don't consume event, others may listen for it too.
        });

        aFactionFilter.addListener((aObs, aOld, aNew) -> {
            filtered.setPredicate(new ChassisFilter(aNew, showMechVariants.getValue()));
            // Don't consume event, others may listen for it too.
        });
    }
}
