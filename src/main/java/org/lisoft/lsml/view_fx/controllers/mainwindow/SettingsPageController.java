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
package org.lisoft.lsml.view_fx.controllers.mainwindow;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Arrays;
import java.util.Collection;

import javax.inject.Inject;

import org.lisoft.lsml.model.NoSuchItemException;
import org.lisoft.lsml.model.database.UpgradeDB;
import org.lisoft.lsml.model.database.gamedata.GameVFS;
import org.lisoft.lsml.model.upgrades.ArmourUpgrade;
import org.lisoft.lsml.model.upgrades.HeatSinkUpgrade;
import org.lisoft.lsml.model.upgrades.StructureUpgrade;
import org.lisoft.lsml.model.upgrades.Upgrade;
import org.lisoft.lsml.view_fx.GlobalGarage;
import org.lisoft.lsml.view_fx.Settings;
import org.lisoft.lsml.view_fx.controllers.AbstractFXController;
import org.lisoft.lsml.view_fx.controls.LsmlAlert;
import org.lisoft.lsml.view_fx.util.FxControlUtils;
import org.lisoft.lsml.view_fx.util.IntegerFilter;

import javafx.beans.property.Property;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.SingleSelectionModel;
import javafx.scene.control.TextField;
import javafx.scene.control.TextFormatter;
import javafx.util.converter.IntegerStringConverter;

/**
 * This page will show all the available settings for LSML.
 *
 * @author Li Song
 */
public class SettingsPageController extends AbstractFXController {
    private final Settings settings;
    @FXML
    private CheckBox updatesCheckAutomatically;
    @FXML
    private CheckBox updatesAcceptBeta;
    @FXML
    private CheckBox defaultUpgradeArtemis;
    @FXML
    private CheckBox coreForceBundled;
    @FXML
    private TextField gameDataFolder;
    @FXML
    private CheckBox uiSmartPlace;
    @FXML
    private CheckBox uiMechVariants;
    @FXML
    private CheckBox uiCompactLayout;
    @FXML
    private CheckBox uiShowQuirkedToolTips;
    @FXML
    private Label invalidPathError;
    @FXML
    private CheckBox defaultMaxArmour;
    @FXML
    private TextField defaultArmourRatio;
    @FXML
    private TextField garageFile;
    @FXML
    private CheckBox uiShowFilteredQuirks;

    @FXML
    private CheckBox uiMwoCompat;
    private final GlobalGarage globalGarage;
    @FXML
    private ComboBox<ArmourUpgrade> isArmour;
    @FXML
    private ComboBox<StructureUpgrade> isStructure;
    @FXML
    private ComboBox<HeatSinkUpgrade> isHeatSinks;
    @FXML
    private ComboBox<ArmourUpgrade> clanArmour;
    @FXML
    private ComboBox<StructureUpgrade> clanStructure;
    @FXML
    private ComboBox<HeatSinkUpgrade> clanHeatSinks;

    @SuppressWarnings("unchecked")
    private <T extends Upgrade> void bindItemComboBox(String aSettingsKey, ComboBox<T> aComboBox,
            Collection<T> aItems) throws NoSuchItemException {
        final Property<Integer> integer = settings.getInteger(aSettingsKey);
        aComboBox.setItems(FXCollections.observableArrayList(aItems));
        final SingleSelectionModel<T> selection = aComboBox.getSelectionModel();
        selection.select((T) UpgradeDB.lookup(integer.getValue()));
        selection.selectedItemProperty().addListener((aObs, aOld, aNew) -> {
            integer.setValue(aNew.getId());
        });
        integer.addListener((aObs, aOld, aNew) -> {
            try {
                selection.select((T) UpgradeDB.lookup(aNew));
            }
            catch (final NoSuchItemException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        });
    }

    @Inject
    public SettingsPageController(Settings aSettings, GlobalGarage aGlobalGarage) {
        settings = aSettings;
        globalGarage = aGlobalGarage;
        FxControlUtils.fixTextField(defaultArmourRatio);
        FxControlUtils.fixTextField(gameDataFolder);
        FxControlUtils.fixTextField(garageFile);

        bindCheckBoxProperty(updatesCheckAutomatically, Settings.CORE_CHECK_FOR_UPDATES);
        bindCheckBoxProperty(updatesAcceptBeta, Settings.CORE_ACCEPT_BETA_UPDATES);

        try {
            bindItemComboBox(Settings.UPGRADES_DEFAULT_CLAN_ARMOUR, clanArmour,
                    Arrays.asList(UpgradeDB.CLAN_STD_ARMOUR, UpgradeDB.CLAN_FF_ARMOUR));
            bindItemComboBox(Settings.UPGRADES_DEFAULT_CLAN_STRUCTURE, clanStructure,
                    Arrays.asList(UpgradeDB.CLAN_STD_STRUCTURE, UpgradeDB.CLAN_ES_STRUCTURE));
            bindItemComboBox(Settings.UPGRADES_DEFAULT_CLAN_HEAT_SINKS, clanHeatSinks,
                    Arrays.asList(UpgradeDB.CLAN_SHS, UpgradeDB.CLAN_DHS));

            bindItemComboBox(Settings.UPGRADES_DEFAULT_IS_ARMOUR, isArmour,
                    Arrays.asList(UpgradeDB.IS_STD_ARMOUR, UpgradeDB.IS_FF_ARMOUR, UpgradeDB.IS_LIGHT_FF_ARMOUR));
            bindItemComboBox(Settings.UPGRADES_DEFAULT_IS_STRUCTURE, isStructure,
                    Arrays.asList(UpgradeDB.IS_STD_STRUCTURE, UpgradeDB.IS_ES_STRUCTURE));
            bindItemComboBox(Settings.UPGRADES_DEFAULT_IS_HEAT_SINKS, isHeatSinks,
                    Arrays.asList(UpgradeDB.IS_SHS, UpgradeDB.IS_DHS));
        }
        catch (final NoSuchItemException e) {
            throw new RuntimeException(e);
        }

        bindCheckBoxProperty(defaultUpgradeArtemis, Settings.UPGRADES_DEFAULT_ARTEMIS);

        bindCheckBoxProperty(defaultMaxArmour, Settings.MAX_ARMOUR);

        bindCheckBoxProperty(coreForceBundled, Settings.CORE_FORCE_BUNDLED_DATA);

        bindCheckBoxProperty(uiShowQuirkedToolTips, Settings.UI_SHOW_TOOL_TIP_QUIRKED);
        bindCheckBoxProperty(uiSmartPlace, Settings.UI_SMART_PLACE);
        bindCheckBoxProperty(uiMechVariants, Settings.UI_MECH_VARIANTS);
        bindCheckBoxProperty(uiCompactLayout, Settings.UI_COMPACT_LAYOUT);
        bindCheckBoxProperty(uiShowFilteredQuirks, Settings.UI_SHOW_STRUCTURE_ARMOR_QUIRKS);
        bindCheckBoxProperty(uiMwoCompat, Settings.UI_PGI_COMPATIBILITY);

        final TextFormatter<Integer> formatter = new TextFormatter<>(new IntegerStringConverter(), 0,
                new IntegerFilter());
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

        settings.getBoolean(Settings.UI_COMPACT_LAYOUT).addListener((aObs, aOld, aNew) -> {
            if (aNew) {
                final LsmlAlert alert = new LsmlAlert(root, AlertType.INFORMATION);
                alert.setTitle("Enabling compact mode...");
                alert.setContentText(
                        "Compact mode sacrifices some readability and looks to make the software function on "
                                + "screens with smaller resolution. Some things will look different and ugly.");
                alert.showAndWait();
            }
        });
    }

    @FXML
    public void browseGarage() {
        globalGarage.openGarage(root.getScene().getWindow());
    }

    @FXML
    public void newGarage() throws FileNotFoundException, IOException {
        globalGarage.newGarage(root.getScene().getWindow());
    }

    @FXML
    public void saveGarage() throws FileNotFoundException, IOException {
        globalGarage.saveGarage();
    }

    private void bindCheckBoxProperty(CheckBox aButton, String aProperty) {
        aButton.selectedProperty().bindBidirectional(settings.getBoolean(aProperty));
    }

}
