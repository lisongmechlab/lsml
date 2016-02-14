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
import org.lisoft.lsml.view_fx.util.FxmlHelpers;

import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.ButtonType;
import javafx.scene.control.ListView;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.SelectionMode;
import javafx.scene.control.Toggle;
import javafx.scene.control.ToggleButton;
import javafx.scene.control.ToggleGroup;
import javafx.scene.control.TreeView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.layout.StackPane;
import javafx.stage.FileChooser;

/**
 * This class is the controller for the main window.
 * 
 * @author Li Song
 */
public class MainWindow extends HBox {
    private final Settings                     settings         = Settings.getSettings();
    private final GarageSerialiser             garageSerialiser = new GarageSerialiser();
    private final CommandStack                 cmdStack         = new CommandStack(100);
    private final MessageXBar                  xBar             = new MessageXBar();
    private Garage                             garage;
    private File                               garageFile;

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
    @FXML
    private ToggleButton                       nav_imexport;
    @FXML
    private ToggleButton                       nav_settings;
    @FXML
    private Pane                               page_imexport;
    @FXML
    private ScrollPane                         page_settings;

    public MainWindow() {
        FxmlHelpers.loadFxmlControl(this);
    }

    private void setupLoadoutPage() {
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

    private void setupNavigationBar() {
        page_settings.setContent(new SettingsPage());

        nav_group.selectedToggleProperty().addListener((aObservable, aOld, aNew) -> {
            if (aNew == nav_loadouts) {
                block_content.getChildren().setAll(page_loadouts);
                page_loadouts.setVisible(true);
            }
            else if (aNew == nav_dropships) {
                block_content.getChildren().setAll(page_dropships);
                page_dropships.setVisible(true);
            }
            else if (aNew == nav_chassis) {
                block_content.getChildren().setAll(page_chassis);
                page_chassis.setVisible(true);
            }
            else if (aNew == nav_weapons) {
                block_content.getChildren().setAll(page_weapons);
                page_weapons.setVisible(true);
            }
            else if (aNew == nav_imexport) {
                block_content.getChildren().setAll(page_imexport);
                page_imexport.setVisible(true);
            }
            else if (aNew == nav_settings) {
                block_content.getChildren().setAll(page_settings);
                page_settings.setVisible(true);
            }
            else {
                throw new IllegalArgumentException("Unknown toggle value! " + aNew);
            }
        });
        nav_group.selectToggle(nav_loadouts);
    }

    private void loadLastGarage() throws IOException {
        String garageFileName = settings.getProperty(Settings.CORE_GARAGE_FILE, String.class).getValue();
        garageFile = new File(garageFileName);
        if (garageFile.exists()) {
            try (FileInputStream fis = new FileInputStream(garageFile);
                    BufferedInputStream bis = new BufferedInputStream(fis);) {
                garage = garageSerialiser.load(bis);
            }
        }
        else {
            // FIXME Show dialog to the user
            openGarage();
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

        if (null != garageFile && garageFile.exists()) {
            fileChooser.setInitialDirectory(garageFile.getParentFile());
        }
        else {
            fileChooser.setInitialDirectory(new File(System.getProperty("user.home")));
        }

        Scene scene = getScene();
        File file = fileChooser.showOpenDialog(scene == null ? null : scene.getWindow());

        if (null != file) {
            try (FileInputStream fis = new FileInputStream(file);
                    BufferedInputStream bis = new BufferedInputStream(fis);) {
                garage = garageSerialiser.load(bis);
                garageFile = file;
            }
        }
    }

    /**
     * @throws IOException
     * 
     */
    public void prepareShow() throws IOException {
        loadLastGarage();
        setupNavigationBar();
        setupLoadoutPage();
    }
}