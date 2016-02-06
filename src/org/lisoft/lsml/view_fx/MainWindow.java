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

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Optional;

import org.lisoft.lsml.messages.MessageXBar;
import org.lisoft.lsml.model.garage.Garage;
import org.lisoft.lsml.model.garage.GarageDirectory;
import org.lisoft.lsml.model.garage.GarageSerialiser;
import org.lisoft.lsml.model.loadout.Loadout;
import org.lisoft.lsml.util.CommandStack;
import org.lisoft.lsml.view.preferences.PreferenceStore;
import org.lisoft.lsml.view_fx.util.FxmlHelpers;

import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.ButtonType;
import javafx.scene.control.ListView;
import javafx.scene.control.SelectionMode;
import javafx.scene.control.Toggle;
import javafx.scene.control.ToggleGroup;
import javafx.scene.control.TreeView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.layout.StackPane;
import javafx.stage.FileChooser;

/**
 * This class is the controller for the main window.
 * 
 * @author Emily Björk
 */
public class MainWindow extends HBox {
    @FXML
    private StackPane                          block_content;
    @FXML
    private Toggle                             nav_loadouts;
    @FXML
    private Toggle                             nav_dropships;
    @FXML
    private Toggle                             nav_chassis;
    @FXML
    private Toggle                             nav_weapons;
    @FXML
    private ToggleGroup                        nav_group;
    @FXML
    private HBox                               page_loadouts;
    @FXML
    private Pane                               page_dropships;
    @FXML
    private Pane                               page_chassis;
    @FXML
    private Pane                               page_weapons;
    @FXML
    private TreeView<GarageDirectory<Loadout>> loadout_tree;
    @FXML
    private ListView<Loadout>                  loadout_pills;

    private Garage                             garage;
    private GarageSerialiser                   garageSerialiser = new GarageSerialiser();
    private File                               garageFile;
    private CommandStack                       cmdStack         = new CommandStack(100);
    private MessageXBar                        xBar             = new MessageXBar();

    public MainWindow() throws IOException {
        FxmlHelpers.loadFxmlControl(this);

        loadLastGarage();

        nav_group.selectedToggleProperty().addListener((aObservable, aOld, aNew) -> {
            if (aNew == nav_loadouts) {
                block_content.getChildren().setAll(page_loadouts);
            }
            else if (aNew == nav_dropships) {
                block_content.getChildren().setAll(page_dropships);
            }
            else if (aNew == nav_chassis) {
                block_content.getChildren().setAll(page_chassis);
            }
            else if (aNew == nav_weapons) {
                block_content.getChildren().setAll(page_weapons);
            }
            else {
                throw new IllegalArgumentException("Unknown toggle value! " + aNew);
            }
        });
        nav_group.selectToggle(nav_loadouts);

        // loadout_tree.setShowRoot(false);
        loadout_tree.setRoot(new GarageTreeItem<>(xBar, garage.getLoadoutRoot()));
        loadout_tree.getRoot().setExpanded(true);
        loadout_tree.getSelectionModel().selectedItemProperty().addListener((aObservable, aOld, aNew) -> {
            if (null == aNew)
                loadout_pills.getItems().clear();
            else
                loadout_pills.setItems(FXCollections.observableArrayList(aNew.getValue().getValues()));
        });
        loadout_tree.setCellFactory(aView -> new GarageTreeCell<Loadout>(xBar, cmdStack, loadout_tree, Loadout.class));
        loadout_tree.setEditable(true);

        loadout_pills.setCellFactory(aView -> new LoadoutPillCell(garage, xBar, loadout_tree, aView));
        loadout_pills.getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE);

    }

    private void loadLastGarage() throws IOException {
        String garageFileName = PreferenceStore.getString(PreferenceStore.GARAGEFILE_KEY,
                PreferenceStore.GARAGEFILE_DEFAULT);
        garageFile = new File(garageFileName);
        if (garageFile.exists()) {
            try (FileInputStream fis = new FileInputStream(garageFile);
                    BufferedInputStream bis = new BufferedInputStream(fis);) {
                garage = garageSerialiser.load(bis);
            }
        }
        else {
            garageFile = null;
        }
    }

    @FXML
    public boolean saveGarageAs() throws IOException {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Save Garage as");
        fileChooser.getExtensionFilters().addAll(new FileChooser.ExtensionFilter("LSML Garage 2.0", "*.lsxml"));

        if (null != garageFile) {
            fileChooser.setInitialDirectory(garageFile);
        }
        else {
            fileChooser.setInitialDirectory(new File(System.getProperty("user.home")));
        }

        File file = fileChooser.showSaveDialog(getScene().getWindow());
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

            try (FileOutputStream fos = new FileOutputStream(file);
                    BufferedOutputStream bos = new BufferedOutputStream(fos);) {
                garageSerialiser.save(bos, garage);
                garageFile = file;
            }
            return true;
        }
        return false;
    }

    @FXML
    public void saveGarage() throws IOException {
        if (null != garageFile) {
            try (FileOutputStream fos = new FileOutputStream(garageFile);
                    BufferedOutputStream bos = new BufferedOutputStream(fos);) {
                garageSerialiser.save(bos, garage);
            }
        }
    }

    @FXML
    public void openGarage() throws IOException {
        if (null != garage) {
            boolean saved = false;
            boolean cancel = false;
            while (!saved && !cancel) {
                Alert saveConfirm = new Alert(AlertType.CONFIRMATION, "Save current garage?");
                Optional<ButtonType> result = saveConfirm.showAndWait();
                if (result.isPresent()) {
                    if (ButtonType.OK == result.get()) {
                        if (null != garageFile) {
                            saved = saveGarageAs();
                        }
                        else {
                            saveGarage();
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

        if (null != garageFile) {
            fileChooser.setInitialDirectory(garageFile);
        }
        else {
            fileChooser.setInitialDirectory(new File(System.getProperty("user.home")));
        }

        File file = fileChooser.showOpenDialog(getScene().getWindow());

        if (null != file) {
            try (FileInputStream fis = new FileInputStream(file);
                    BufferedInputStream bis = new BufferedInputStream(fis);) {
                garage = garageSerialiser.load(bis);
                garageFile = file;
            }
        }
    }
}
