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

import org.lisoft.lsml.view_fx.style.StyleManager;
import org.lisoft.lsml.view_fx.style.WindowState;
import org.lisoft.lsml.view_fx.util.FxControlUtils;

import javafx.fxml.FXML;
import javafx.scene.Parent;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.TextField;
import javafx.scene.control.Toggle;
import javafx.scene.control.ToggleGroup;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.StackPane;
import javafx.stage.Stage;

/**
 * This class is the controller for the main window.
 *
 * @author Li Song
 */
public class MainWindow extends StackPane {
    @FXML
    private BorderPane content;
    @FXML
    private Toggle nav_chassis;
    @FXML
    private Toggle nav_dropships;
    @FXML
    private ToggleGroup nav_group;
    @FXML
    private Toggle nav_imexport;
    @FXML
    private Toggle nav_loadouts;
    @FXML
    private Toggle nav_settings;
    @FXML
    private Toggle nav_weapons;
    private final Parent page_chassis;
    private final Parent page_dropships;
    private final Parent page_imexport;
    private final Parent page_loadouts;
    private final Parent page_settings;
    private final Parent page_weapons;
    @FXML
    private TextField searchField;
    private final WindowState windowState;

    private final ApplicationModel model = ApplicationModel.model;

    public MainWindow(Stage aStage) {
        FxControlUtils.loadFxmlControl(this);
        FxControlUtils.fixTextField(searchField);
        windowState = new WindowState(aStage, this);

        page_chassis = new ChassisPage(model.xBar);
        page_dropships = new ViewDropShipsPane();
        page_imexport = new ImportExportPage(model.xBar, model.importer, model.smurfyImportExport, model.cmdStack);
        page_loadouts = new ViewLoadoutsPane(model);
        page_settings = new SettingsPage();
        page_weapons = new WeaponsPage();

        searchField.textProperty().addListener((aObs, aOld, aNew) -> {
            if (aNew != null && !aNew.isEmpty()) {
                if (aOld.isEmpty()) {
                    openSearchOverlay();
                }
            }
            else {
                closeSearchOverlay();
            }
        });

        nav_group.selectToggle(nav_loadouts);
        content.setCenter(page_loadouts);
        nav_group.selectedToggleProperty().addListener((aObservable, aOld, aNew) -> {
            if (aNew == nav_loadouts) {
                content.setCenter(page_loadouts);
            }
            else if (aNew == nav_dropships) {
                content.setCenter(page_dropships);
            }
            else if (aNew == nav_chassis) {
                content.setCenter(page_chassis);
            }
            else if (aNew == nav_weapons) {
                content.setCenter(page_weapons);
            }
            else if (aNew == nav_imexport) {
                content.setCenter(page_imexport);
            }
            else if (aNew == nav_settings) {
                content.setCenter(page_settings);
            }
            else if (aNew == null) {
                aOld.setSelected(true);
            }
            else {
                throw new IllegalArgumentException("Unknown toggle value! " + aNew);
            }
        });

        aStage.addEventHandler(KeyEvent.KEY_RELEASED, event -> {
            if (model.redoKeyCombination.match(event)) {
                model.globalRedo();
            }
            else if (model.undoKeyCombination.match(event)) {
                model.globalUndo();
            }
        });
    }

    public WindowState getWindowState() {
        return windowState;
    }

    @FXML
    public void openNewDropshipOverlay() {
        final Alert alert = new Alert(AlertType.INFORMATION);
        alert.setTitle("Coming soon!™");
        alert.setHeaderText("Drop ship mode is not yet available.");
        alert.setContentText("Drop ship mode is planned for release in 2.1");
        alert.showAndWait();
    }

    @FXML
    public void openNewMechOverlay() {
        final NewMechPane newMechPane = new NewMechPane(() -> {
            getChildren().removeIf(aNode -> aNode instanceof NewMechPane);
            content.setDisable(false);
        }, model.xBar, model.settings);
        StyleManager.makeOverlay(newMechPane);
        getChildren().add(newMechPane);
        content.setDisable(true);
    }

    @FXML
    public void windowClose() {
        windowState.windowClose();
    }

    @FXML
    public void windowIconify() {
        windowState.windowIconify();
    }

    @FXML
    public void windowMaximize() {
        windowState.windowMaximize();
    }

    private void closeSearchOverlay() {
        if (getChildren().size() > 1) {
            getChildren().removeIf(aNode -> aNode instanceof SearchResultsPane);
        }
        searchField.textProperty().setValue("");
    }

    private void openSearchOverlay() {
        final SearchResultsPane searchResultsPane = new SearchResultsPane(searchField.textProperty(),
                GlobalGarage.instance.getGarage(), this::closeSearchOverlay);
        StyleManager.makeOverlay(searchResultsPane);
        getChildren().add(searchResultsPane);
    }
}
