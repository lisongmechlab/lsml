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
import java.io.FileNotFoundException;
import java.io.IOException;

import org.lisoft.lsml.model.datacache.gamedata.GameVFS;
import org.lisoft.lsml.view_fx.util.FxControlUtils;

import javafx.beans.property.Property;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.control.TextFormatter;
import javafx.scene.control.ToggleButton;
import javafx.scene.layout.BorderPane;
import javafx.util.converter.IntegerStringConverter;

/**
 * This page will show all the available settings for LSML.
 *
 * @author Emily Björk
 */
public class SettingsPage extends BorderPane {
    private final Settings settings = Settings.getSettings();

    @FXML
    private ToggleButton updatesCheckAutomatically;
    @FXML
    private ToggleButton updatesAcceptBeta;
    @FXML
    private ToggleButton defaultUpgradeDHS;
    @FXML
    private ToggleButton defaultUpgradeES;
    @FXML
    private ToggleButton defaultUpgradeFF;
    @FXML
    private ToggleButton defaultUpgradeArtemis;
    @FXML
    private ToggleButton defaultEffsAll;
    @FXML
    private ToggleButton coreForceBundled;
    @FXML
    private TextField gameDataFolder;
    @FXML
    private ToggleButton uiSmartPlace;
    @FXML
    private ToggleButton uiMechVariants;
    @FXML
    private ToggleButton uiCompactLayout;
    @FXML
    private ToggleButton uiShowQuirkedToolTips;
    @FXML
    private Label invalidPathError;
    @FXML
    private ToggleButton defaultMaxArmour;
    @FXML
    private TextField defaultArmourRatio;
    @FXML
    private TextField garageFile;
    @FXML
    private ToggleButton uiShowFilteredQuirks;

    public SettingsPage() {
        FxControlUtils.loadFxmlControl(this);

        bindToggle(updatesCheckAutomatically, Settings.CORE_CHECK_FOR_UPDATES);
        bindToggle(updatesAcceptBeta, Settings.CORE_ACCEPT_BETA_UPDATES);

        bindToggle(defaultUpgradeDHS, Settings.UPGRADES_DHS);
        bindToggle(defaultUpgradeES, Settings.UPGRADES_ES);
        bindToggle(defaultUpgradeFF, Settings.UPGRADES_FF);
        bindToggle(defaultUpgradeArtemis, Settings.UPGRADES_ARTEMIS);

        bindToggle(defaultEffsAll, Settings.EFFICIENCIES_ALL);

        bindToggle(defaultMaxArmour, Settings.MAX_ARMOUR);

        bindToggle(coreForceBundled, Settings.CORE_FORCE_BUNDLED_DATA);

        bindToggle(uiShowQuirkedToolTips, Settings.UI_SHOW_TOOL_TIP_QUIRKED);
        bindToggle(uiSmartPlace, Settings.UI_SMART_PLACE);
        bindToggle(uiMechVariants, Settings.UI_MECH_VARIANTS);
        bindToggle(uiCompactLayout, Settings.UI_COMPACT_LAYOUT);
        bindToggle(uiShowFilteredQuirks, Settings.UI_SHOW_STRUCTURE_ARMOR_QUIRKS);

        final TextFormatter<Integer> formatter = new TextFormatter<>(new IntegerStringConverter());
        defaultArmourRatio.setTextFormatter(formatter);
        formatter.valueProperty().bindBidirectional(settings.getInteger(Settings.ARMOUR_RATIO));

        garageFile.textProperty().bind(settings.getString(Settings.CORE_GARAGE_FILE));
        garageFile.setDisable(true);

        final Property<String> gameDir = settings.getString(Settings.CORE_GAME_DIRECTORY);
        gameDataFolder.textProperty().bindBidirectional(gameDir);
        gameDataFolder.textProperty().addListener((aObservable, aOld, aNew) -> {
            invalidPathError.setVisible(!GameVFS.isValidGameDirectory(new File(aNew)));
        });
        invalidPathError.setVisible(!GameVFS.isValidGameDirectory(new File(gameDir.getValue())));

        FxControlUtils.fixTextField(defaultArmourRatio);
        FxControlUtils.fixTextField(gameDataFolder);
        FxControlUtils.fixTextField(garageFile);

        settings.getBoolean(Settings.UI_COMPACT_LAYOUT).addListener((aObs, aOld, aNew) -> {
            if (aNew) {
                final Alert alert = new Alert(AlertType.INFORMATION);
                alert.setTitle("Enabling compact mode...");
                alert.setContentText(
                        "Compact mode sacrifices some readability and looks to make the software function on "
                                + "screens with smaller resolution. Some things will look different and ugly.");
                alert.showAndWait();
            }
        });
    }

    @FXML
    public void browseGarage() throws IOException {
        GlobalGarage.instance.openGarage(this.getScene().getWindow());
    }

    @FXML
    public void newGarage() throws FileNotFoundException, IOException {
        GlobalGarage.instance.newGarage(this.getScene().getWindow());
    }

    private void bindToggle(ToggleButton aButton, String aProperty) {
        aButton.selectedProperty().bindBidirectional(settings.getBoolean(aProperty));
        FxControlUtils.setupToggleText(aButton, "Yes", "No");
    }

}
