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

import java.io.File;

import org.lisoft.lsml.model.datacache.gamedata.GameVFS;
import org.lisoft.lsml.view_fx.util.FxControlUtils;

import javafx.beans.property.Property;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.control.TextFormatter;
import javafx.scene.control.ToggleButton;
import javafx.scene.layout.BorderPane;
import javafx.util.converter.IntegerStringConverter;

/**
 * This page will show all the available settings for LSML.
 *
 * @author Li Song
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
    private ToggleButton defaultMaxArmor;
    @FXML
    private TextField defaultArmorRatio;

    public SettingsPage() {
        FxControlUtils.loadFxmlControl(this);

        bindToggle(updatesCheckAutomatically, Settings.CORE_CHECK_FOR_UPDATES);
        bindToggle(updatesAcceptBeta, Settings.CORE_ACCEPT_BETA_UPDATES);

        bindToggle(defaultUpgradeDHS, Settings.UPGRADES_DHS);
        bindToggle(defaultUpgradeES, Settings.UPGRADES_ES);
        bindToggle(defaultUpgradeFF, Settings.UPGRADES_FF);
        bindToggle(defaultUpgradeArtemis, Settings.UPGRADES_ARTEMIS);

        bindToggle(defaultEffsAll, Settings.EFFICIENCIES_ALL);

        bindToggle(defaultMaxArmor, Settings.MAX_ARMOR);

        bindToggle(coreForceBundled, Settings.CORE_FORCE_BUNDLED_DATA);

        bindToggle(uiShowQuirkedToolTips, Settings.UI_SHOW_TOOL_TIP_QUIRKED);
        bindToggle(uiSmartPlace, Settings.UI_SMART_PLACE);
        bindToggle(uiMechVariants, Settings.UI_MECH_VARIANTS);
        bindToggle(uiCompactLayout, Settings.UI_COMPACT_LAYOUT);

        final TextFormatter<Integer> formatter = new TextFormatter<>(new IntegerStringConverter());
        defaultArmorRatio.setTextFormatter(formatter);
        formatter.valueProperty().bindBidirectional(settings.getProperty(Settings.ARMOR_RATIO, Integer.class));
        FxControlUtils.fixTextField(defaultArmorRatio);

        final Property<String> gameDir = settings.getProperty(Settings.CORE_GAME_DIRECTORY, String.class);
        gameDataFolder.textProperty().bindBidirectional(gameDir);
        gameDataFolder.textProperty().addListener((aObservable, aOld, aNew) -> {
            invalidPathError.setVisible(!GameVFS.isValidGameDirectory(new File(aNew)));
        });
        invalidPathError.setVisible(!GameVFS.isValidGameDirectory(new File(gameDir.getValue())));
    }

    private void bindToggle(ToggleButton aButton, String aProperty) {
        aButton.selectedProperty().bindBidirectional(settings.getProperty(aProperty, Boolean.class));
        FxControlUtils.setupToggleText(aButton, "Yes", "No");
    }

}
