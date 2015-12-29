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

import java.io.File;
import java.io.IOException;
import java.util.Optional;

import org.lisoft.lsml.messages.MessageXBar;
import org.lisoft.lsml.model.chassi.ChassisClass;
import org.lisoft.lsml.model.garage.MechGarage;
import org.lisoft.lsml.model.item.Faction;
import org.lisoft.lsml.model.loadout.LoadoutBase;
import org.lisoft.lsml.util.CommandStack;
import org.lisoft.lsml.view.preferences.PreferenceStore;

import javafx.beans.property.BooleanProperty;
import javafx.beans.property.ReadOnlyObjectWrapper;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.collections.transformation.FilteredList;
import javafx.collections.transformation.SortedList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.Accordion;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.ButtonType;
import javafx.scene.control.MenuItem;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.control.TitledPane;
import javafx.scene.layout.BorderPane;
import javafx.stage.FileChooser;

/**
 * This is the controller for the garage window.
 * 
 * @author Emily Björk
 */
public class GarageController {
    @FXML
    private BorderPane                    root;

    @FXML
    private MenuItem                      menuItemSave;

    @FXML
    private Accordion                     garageAccordion;

    @FXML
    private TableView<LoadoutBase<?>>     searchTable;

    @FXML
    private TextField                     searchText;

    private ObservableGarageList          allMechs;
    private FilteredList<LoadoutBase<?>>  filteredMechs;
    private MechGarage                    garage;
    private CommandStack                  cmdStack        = new CommandStack(100);
    private MessageXBar                   xBar            = new MessageXBar();
    private BooleanProperty               hasGarageFile   = new SimpleBooleanProperty();

    private SimpleObjectProperty<Faction> filteredFaction = new SimpleObjectProperty<Faction>(Faction.ANY);

    private boolean isGarageSaved() {
        return null != garage && null != garage.getFile();
    }

    private void updateFilter(Faction aFaction, String aFilter) {
        filteredMechs.setPredicate(loadout -> {
            String filter = aFilter.toLowerCase();
            return aFaction.isCompatible(loadout.getChassis().getFaction())
                    && (loadout.getName().toLowerCase().contains(filter)
                            || loadout.getChassis().getName().toLowerCase().contains(filter));
        });
    }

    @FXML
    private void initialize() throws IOException {
        hasGarageFile.set(isGarageSaved());
        menuItemSave.disableProperty().bind(hasGarageFile.not());

        initializeGarageCategories();
        initializeSearchTable();

        loadLastGarage();
    }

    private void initializeSearchTable() {
        allMechs = new ObservableGarageList(null, xBar);
        filteredMechs = new FilteredList<>(allMechs);

        SortedList<LoadoutBase<?>> sortedMechs = new SortedList<>(filteredMechs);
        sortedMechs.comparatorProperty().bind(searchTable.comparatorProperty());
        searchTable.setItems(sortedMechs);
        searchText.textProperty().addListener((observable, oldValue, newValue) -> {
            updateFilter(filteredFaction.get(), newValue);
        });
        filteredFaction.addListener((observable, oldValue, newValue) -> {
            updateFilter(newValue, searchText.textProperty().get());
        });

        TableColumn<LoadoutBase<?>, String> nameCol = new TableColumn<>("Name");
        nameCol.setCellValueFactory(data -> {
            return new ReadOnlyObjectWrapper<String>(data.getValue().getName());
        });

        TableColumn<LoadoutBase<?>, String> chassisCol = new TableColumn<>("Chassis");
        chassisCol.setCellValueFactory(data -> {
            return new ReadOnlyObjectWrapper<String>(data.getValue().getChassis().getNameShort());
        });

        TableColumn<LoadoutBase<?>, Integer> massCol = new TableColumn<>("Mass");
        massCol.setCellValueFactory(data -> {
            return new ReadOnlyObjectWrapper<Integer>(data.getValue().getChassis().getMassMax());
        });

        TableColumn<LoadoutBase<?>, String> factionCol = new TableColumn<>("Faction");
        factionCol.setCellValueFactory(data -> {
            return new ReadOnlyObjectWrapper<String>(data.getValue().getChassis().getFaction().toString());
        });

        searchTable.getColumns().clear();
        searchTable.getColumns().add(nameCol);
        searchTable.getColumns().add(chassisCol);
        searchTable.getColumns().add(massCol);
        searchTable.getColumns().add(factionCol);
    }

    private void initializeGarageCategories() throws IOException {
        for (ChassisClass chassisClass : ChassisClass.values()) {
            if (chassisClass == ChassisClass.COLOSSAL)
                continue;
            FXMLLoader loader = new FXMLLoader(LSMLFXML.GARAGE_MECH_LIST_VIEW);
            TitledPane mechListView = loader.load();
            GarageListViewController controller = loader.getController();
            controller.initialize(chassisClass, xBar, cmdStack, filteredFaction);
            garageAccordion.getPanes().add(mechListView);
        }
    }

    private void loadLastGarage() {
        String garageFileName = PreferenceStore.getString(PreferenceStore.GARAGEFILE_KEY,
                PreferenceStore.GARAGEFILE_DEFAULT);
        File garageFile = new File(garageFileName);
        if (garageFile.exists()) {
            try {
                garage = MechGarage.open(garageFile, xBar);
                hasGarageFile.set(true);
            }
            catch (Exception e) {

            }
        }
    }

    @FXML
    public boolean saveGarageAs(@SuppressWarnings("unused") ActionEvent aEvent) throws IOException {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Save Garage as");
        fileChooser.getExtensionFilters().addAll(new FileChooser.ExtensionFilter("LSML Garage 2.0", "*.lsxml"));

        if (isGarageSaved() && garage.getFile().exists()) {
            fileChooser.setInitialDirectory(garage.getFile());
        }
        else {
            fileChooser.setInitialDirectory(new File(System.getProperty("user.home")));
        }

        File file = fileChooser.showSaveDialog(root.getScene().getWindow());
        if (null != file) {
            if (file.exists()) {
                Alert confirmOverwrite = new Alert(AlertType.CONFIRMATION, "Overwrite selected garage?");
                Optional<ButtonType> result = confirmOverwrite.showAndWait();
                if (result.isPresent()) {
                    if (ButtonType.OK != result.get()) {
                        return false;
                    }
                }
            }

            garage.saveas(file, true);
            hasGarageFile.set(true);
            return true;
        }
        return false;
    }

    @FXML
    public void filterIS(@SuppressWarnings("unused") ActionEvent aEvent) {
        filteredFaction.set(Faction.INNERSPHERE);
    }

    @FXML
    public void filterClan(@SuppressWarnings("unused") ActionEvent aEvent) {
        filteredFaction.set(Faction.CLAN);
    }

    @FXML
    public void filterBoth(@SuppressWarnings("unused") ActionEvent aEvent) {
        filteredFaction.set(Faction.ANY);
    }

    @FXML
    public void saveGarage(@SuppressWarnings("unused") ActionEvent aEvent) throws IOException {
        if (isGarageSaved()) {
            garage.save();
        }
    }

    @FXML
    public void openGarage(ActionEvent aEvent) throws IOException {
        if (null != garage) {
            boolean saved = false;
            boolean cancel = false;
            while (!saved && !cancel) {
                Alert saveConfirm = new Alert(AlertType.CONFIRMATION, "Save current garage?");
                Optional<ButtonType> result = saveConfirm.showAndWait();
                if (result.isPresent()) {
                    if (ButtonType.OK == result.get()) {
                        if (null != garage.getFile()) {
                            saved = saveGarageAs(aEvent);
                        }
                        else {
                            saveGarage(aEvent);
                            saved = true;
                        }
                    }
                    else {
                        cancel = true;
                        saved = false;
                    }
                }
            }
        }

        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Open Garage");
        fileChooser.getExtensionFilters().addAll(new FileChooser.ExtensionFilter("LSML Garage 2.0", "*.lsxml"),
                new FileChooser.ExtensionFilter("LSML Garage 1.0", "*.xml"));

        if (isGarageSaved() && garage.getFile().exists()) {
            fileChooser.setInitialDirectory(garage.getFile());
        }
        else {
            fileChooser.setInitialDirectory(new File(System.getProperty("user.home")));
        }

        File file = fileChooser.showOpenDialog(root.getScene().getWindow());

        if (null != file) {
            garage = MechGarage.open(file, xBar);
            hasGarageFile.set(true);
        }
    }
}
