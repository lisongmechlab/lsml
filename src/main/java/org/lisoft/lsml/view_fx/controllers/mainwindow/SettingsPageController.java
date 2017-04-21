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
package org.lisoft.lsml.view_fx.controllers.mainwindow;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;

import javax.inject.Inject;

import org.lisoft.lsml.model.datacache.gamedata.GameVFS;
import org.lisoft.lsml.view_fx.GlobalGarage;
import org.lisoft.lsml.view_fx.Settings;
import org.lisoft.lsml.view_fx.controllers.AbstractFXController;
import org.lisoft.lsml.view_fx.util.FxControlUtils;
import org.lisoft.lsml.view_fx.util.IntegerFilter;

import javafx.beans.property.Property;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.CheckBox;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.control.TextFormatter;
import javafx.util.converter.IntegerStringConverter;

/**
 * This page will show all the available settings for LSML.
 *
 * @author Emily Björk
 */
public class SettingsPageController extends AbstractFXController {
	private final Settings settings;
	@FXML
	private CheckBox updatesCheckAutomatically;
	@FXML
	private CheckBox updatesAcceptBeta;
	@FXML
	private CheckBox defaultUpgradeDHS;
	@FXML
	private CheckBox defaultUpgradeES;
	@FXML
	private CheckBox defaultUpgradeFF;
	@FXML
	private CheckBox defaultUpgradeArtemis;
	@FXML
	private CheckBox defaultEffsAll;
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

	@Inject
	public SettingsPageController(Settings aSettings, GlobalGarage aGlobalGarage) {
		settings = aSettings;
		globalGarage = aGlobalGarage;
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

	@Override
	protected void onLoad() {
		FxControlUtils.fixTextField(defaultArmourRatio);
		FxControlUtils.fixTextField(gameDataFolder);
		FxControlUtils.fixTextField(garageFile);

		bindCheckBoxProperty(updatesCheckAutomatically, Settings.CORE_CHECK_FOR_UPDATES);
		bindCheckBoxProperty(updatesAcceptBeta, Settings.CORE_ACCEPT_BETA_UPDATES);

		bindCheckBoxProperty(defaultUpgradeDHS, Settings.UPGRADES_DHS);
		bindCheckBoxProperty(defaultUpgradeES, Settings.UPGRADES_ES);
		bindCheckBoxProperty(defaultUpgradeFF, Settings.UPGRADES_FF);
		bindCheckBoxProperty(defaultUpgradeArtemis, Settings.UPGRADES_ARTEMIS);

		bindCheckBoxProperty(defaultEffsAll, Settings.EFFICIENCIES_ALL);

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
				final Alert alert = new Alert(AlertType.INFORMATION);
				alert.setTitle("Enabling compact mode...");
				alert.setContentText(
						"Compact mode sacrifices some readability and looks to make the software function on "
								+ "screens with smaller resolution. Some things will look different and ugly.");
				alert.showAndWait();
			}
		});
	}

	private void bindCheckBoxProperty(CheckBox aButton, String aProperty) {
		aButton.selectedProperty().bindBidirectional(settings.getBoolean(aProperty));
	}

}
