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
import static org.lisoft.lsml.view_fx.util.FxmlHelpers.addHardpointsColumn;
import static org.lisoft.lsml.view_fx.util.FxmlHelpers.addPropertyColumn;
import static org.lisoft.lsml.view_fx.util.FxmlHelpers.addTopSpeedColumn;
import static org.lisoft.lsml.view_fx.util.FxmlHelpers.loadFxmlControl;
import static org.lisoft.lsml.view_fx.util.FxmlHelpers.makeAttributeColumn;
import static org.lisoft.lsml.view_fx.util.FxmlHelpers.setToggleText;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.function.Predicate;

import org.lisoft.lsml.messages.MessageXBar;
import org.lisoft.lsml.model.chassi.Chassis;
import org.lisoft.lsml.model.chassi.ChassisClass;
import org.lisoft.lsml.model.chassi.Location;
import org.lisoft.lsml.model.datacache.ChassisDB;
import org.lisoft.lsml.model.datacache.ModifiersDB;
import org.lisoft.lsml.model.item.Faction;
import org.lisoft.lsml.model.loadout.DefaultLoadoutFactory;
import org.lisoft.lsml.model.loadout.Loadout;
import org.lisoft.lsml.model.metrics.PayloadStatistics;
import org.lisoft.lsml.model.modifiers.Efficiencies;
import org.lisoft.lsml.model.modifiers.MechEfficiencyType;
import org.lisoft.lsml.model.modifiers.Modifier;
import org.lisoft.lsml.view_fx.style.FilteredModifierFormatter;
import org.lisoft.lsml.view_fx.util.FxmlHelpers;

import javafx.application.Platform;
import javafx.beans.InvalidationListener;
import javafx.beans.binding.ObjectExpression;
import javafx.beans.property.Property;
import javafx.beans.property.ReadOnlyObjectWrapper;
import javafx.beans.value.ObservableObjectValue;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.collections.transformation.SortedList;
import javafx.fxml.FXML;
import javafx.scene.chart.LineChart;
import javafx.scene.control.ListView;
import javafx.scene.control.SelectionMode;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableRow;
import javafx.scene.control.TableView;
import javafx.scene.control.ToggleButton;
import javafx.scene.input.MouseButton;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.VBox;

/**
 * This is a controller class for the chassis page.
 * 
 * @author Emily Björk
 */
public class ChassisPage extends BorderPane {
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

    @Deprecated // Inject with DI
    private static final FilteredModifierFormatter MODIFIER_FORMATTER = new FilteredModifierFormatter(
            ModifiersDB.getAllWeaponSelectors());
    private Settings settings = Settings.getSettings();

    @FXML
    private TableView<Loadout> tableLights;
    @FXML
    private TableView<Loadout> tableMediums;
    @FXML
    private TableView<Loadout> tableHeavies;
    @FXML
    private TableView<Loadout> tableAssaults;
    @FXML
    private LineChart<Double, Double> payloadGraph;
    @FXML
    private ToggleButton payloadXLEngine;
    @FXML
    private ToggleButton payloadEndoSteel;
    @FXML
    private ToggleButton payloadFerroFibrous;
    @FXML
    private ToggleButton payloadMaxArmor;
    @FXML
    private ToggleButton payloadSpeedTweak;
    @FXML
    private ListView<ChassisGroup> payloadChassis;
    private final MessageXBar globalXBar;
    private final ObservableObjectValue<Faction> factionFilter;

    public ChassisPage(ObjectExpression<Faction> aFactionFilter, MessageXBar aGlobalXBar) {
        loadFxmlControl(this);

        globalXBar = aGlobalXBar;
        factionFilter = aFactionFilter;

        setupChassisTable(tableLights, ChassisClass.LIGHT, aFactionFilter);
        setupChassisTable(tableMediums, ChassisClass.MEDIUM, aFactionFilter);
        setupChassisTable(tableHeavies, ChassisClass.HEAVY, aFactionFilter);
        setupChassisTable(tableAssaults, ChassisClass.ASSAULT, aFactionFilter);

        setupPayloadGraph();
        updateGraph();
    }

    /**
     * 
     */
    private void setupPayloadGraph() {
        // Group all chassis by mass
        Map<Integer, ChassisGroup> groups = new TreeMap<>();
        for (ChassisClass aChassiClass : ChassisClass.values()) {
            for (Chassis chassis : ChassisDB.lookup(aChassiClass)) {
                if (chassis.getVariantType().isVariation()) {
                    continue;
                }
                int mass = chassis.getMassMax();
                ChassisGroup group = groups.get(mass);
                if (null == group) {
                    group = new ChassisGroup(mass + " tons");
                    groups.put(mass, group);
                }
                group.add(chassis);
            }
        }
        payloadChassis.getItems().setAll(groups.values());
        payloadChassis.getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE);

        // Setup axis
        payloadGraph.getXAxis().setLabel("Speed");
        payloadGraph.getYAxis().setLabel("Payload mass");
        payloadGraph.getData().clear();

        // Setup settings
        setToggleText(payloadXLEngine, "XL", "Standard");
        setToggleText(payloadEndoSteel, "Endo-Steel", "Standard");
        setToggleText(payloadFerroFibrous, "Ferro-Fibrous", "Standard");
        setToggleText(payloadMaxArmor, "Max Armor", "No Armor");
        setToggleText(payloadSpeedTweak, "Speed Tweak", "None");

        // Setup hooks to update the graphs when settings change
        InvalidationListener il = aObservable -> {
            Platform.runLater(() -> {
                updateGraph();
            });
        };
        payloadXLEngine.selectedProperty().addListener(il);
        payloadEndoSteel.selectedProperty().addListener(il);
        payloadFerroFibrous.selectedProperty().addListener(il);
        payloadMaxArmor.selectedProperty().addListener(il);
        payloadSpeedTweak.selectedProperty().addListener(il);
        payloadChassis.getSelectionModel().getSelectedItems().addListener(il);
        factionFilter.addListener(il);
    }

    /**
     * 
     */
    private void updateGraph() {
        List<PayloadGrouping> dataGroups = new ArrayList<>();

        for (ChassisGroup selectionGroup : payloadChassis.getSelectionModel().getSelectedItems()) {
            if (selectionGroup == null)
                continue;
            for (Chassis chassis : selectionGroup) {
                if (!chassis.getFaction().isCompatible(factionFilter.get())) {
                    continue;
                }

                boolean consumed = false;
                for (PayloadGrouping dataGroup : dataGroups) {
                    if (dataGroup.offer(chassis)) {
                        consumed = true;
                        break;
                    }
                }
                if (!consumed) {
                    PayloadStatistics statistics = new PayloadStatistics(payloadXLEngine.isSelected(),
                            payloadMaxArmor.isSelected(), payloadEndoSteel.isSelected(),
                            payloadFerroFibrous.isSelected());
                    dataGroups.add(new PayloadGrouping(chassis, statistics));
                }
            }
        }
        payloadGraph.getData().clear();
        Efficiencies efficiencies = new Efficiencies();
        efficiencies.setEfficiency(MechEfficiencyType.SPEED_TWEAK, payloadSpeedTweak.isSelected(), null);
        for (PayloadGrouping dataGroup : dataGroups) {
            dataGroup.addToGraph(efficiencies, payloadGraph);
        }

        FxmlHelpers.setGraphTightBounds(payloadGraph.getXAxis(), payloadGraph.getYAxis(), 10.0, 5.0,
                payloadGraph.getData());
    }

    private void openChassis(Chassis aChassis) {
        try {
            Loadout loadout = DefaultLoadoutFactory.instance.produceDefault(aChassis, Settings.getSettings());
            LiSongMechLab.openLoadout(globalXBar, loadout);
        }
        catch (Exception e) {
            LiSongMechLab.showError(ChassisPage.this, e);
        }
    }

    private void setupChassisTable(TableView<Loadout> aTable, ChassisClass aChassisClass,
            ObjectExpression<Faction> aFactionFilter) {

        setupTableData(aTable, aChassisClass, aFactionFilter);
        aTable.setRowFactory(aView -> {
            TableRow<Loadout> tr = new TableRow<>();
            tr.setOnMouseClicked(aEvent -> {
                if (aEvent.getClickCount() >= 2 && aEvent.getButton() == MouseButton.PRIMARY) {
                    Loadout item = tr.getItem();
                    if (item != null) {
                        openChassis(item.getChassis());
                    }
                }
            });
            return tr;
        });

        aTable.getColumns().clear();
        addAttributeColumn(aTable, "Name", "chassis.nameShort");
        addAttributeColumn(aTable, "Mass", "chassis.massMax");
        addTopSpeedColumn(aTable);
        addAttributeColumn(aTable, "Faction", "chassis.faction.uiShortName");
        addHardpointsColumn(aTable, Location.RightArm);
        addHardpointsColumn(aTable, Location.RightTorso);
        addHardpointsColumn(aTable, Location.Head);
        addHardpointsColumn(aTable, Location.CenterTorso);
        addHardpointsColumn(aTable, Location.LeftTorso);
        addHardpointsColumn(aTable, Location.LeftArm);
        addPropertyColumn(aTable, "JJ", "jumpJetsMax");

        TableColumn<Loadout, Collection<Modifier>> quirksCol = new TableColumn<>("Weapon Quirks");
        quirksCol.setCellValueFactory(aFeatures -> new ReadOnlyObjectWrapper<>(aFeatures.getValue().getModifiers()));
        quirksCol.setCellFactory(aView -> new TableCell<Loadout, Collection<Modifier>>() {
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
        quirksCol.setSortable(false);
        aTable.getColumns().add(quirksCol);

        TableColumn<Loadout, String> modules = new TableColumn<>("Modules");
        modules.getColumns().clear();
        modules.getColumns().add(makeAttributeColumn("M", "chassis.mechModulesMax"));
        modules.getColumns().add(makeAttributeColumn("C", "chassis.consumableModulesMax"));
        modules.getColumns().add(makeAttributeColumn("W", "chassis.weaponModulesMax"));
        aTable.getColumns().add(modules);

    }

    private void setupTableData(TableView<Loadout> aTable, ChassisClass aChassisClass,
            ObjectExpression<Faction> aFactionFilter) {
        Property<Boolean> showMechVariants = settings.getProperty(Settings.UI_MECH_VARIANTS, Boolean.class);

        ObservableList<Loadout> loadouts = FXCollections.observableArrayList();
        for (Chassis chassis : ChassisDB.lookup(aChassisClass)) {
            try {
                loadouts.add(DefaultLoadoutFactory.instance.produceEmpty(chassis));
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
