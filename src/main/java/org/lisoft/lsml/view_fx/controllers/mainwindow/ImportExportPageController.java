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

import javafx.beans.binding.BooleanBinding;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.Property;
import javafx.beans.property.SimpleObjectProperty;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.*;
import org.lisoft.lsml.command.CmdGarageAdd;
import org.lisoft.lsml.command.CmdGarageMergeDirectories;
import org.lisoft.lsml.messages.MessageXBar;
import org.lisoft.lsml.model.export.BatchImportExporter;
import org.lisoft.lsml.model.export.LsmlLinkProtocol;
import org.lisoft.lsml.model.export.SmurfyImportExport;
import org.lisoft.lsml.model.garage.GarageDirectory;
import org.lisoft.lsml.model.garage.GaragePath;
import org.lisoft.lsml.model.loadout.EquipException;
import org.lisoft.lsml.model.loadout.Loadout;
import org.lisoft.lsml.util.CommandStack;
import org.lisoft.lsml.util.CommandStack.CompositeCommand;
import org.lisoft.lsml.util.EncodingException;
import org.lisoft.lsml.view_fx.GlobalGarage;
import org.lisoft.lsml.view_fx.LiSongMechLab;
import org.lisoft.lsml.view_fx.Settings;
import org.lisoft.lsml.view_fx.controllers.AbstractFXController;
import org.lisoft.lsml.view_fx.controls.LsmlAlert;
import org.lisoft.lsml.view_fx.util.FxControlUtils;

import javax.inject.Inject;
import javax.inject.Named;
import java.io.IOException;
import java.nio.file.AccessDeniedException;
import java.util.Collection;
import java.util.List;

/**
 * This page allows the user to mass export/import loadouts.
 *
 * @author Li Song
 */
public class ImportExportPageController extends AbstractFXController {
    private final BatchImportExporter batchImporterExporter;
    private final GlobalGarage globalGarage;
    private final ObjectProperty<LsmlLinkProtocol> protocolProperty = new SimpleObjectProperty<>();
    private final Settings settings;
    private final SmurfyImportExport smurfyImportExport;
    private final CommandStack stack;
    private final MessageXBar xBar;
    @FXML
    private TreeView<GaragePath<Loadout>> garageViewLSML;
    @FXML
    private TreeView<GaragePath<Loadout>> garageViewSmurfy;
    @FXML
    private TextArea linkInputOutput;
    @FXML
    private ToggleGroup protocol;
    @FXML
    private RadioButton protocolHttp;
    @FXML
    private RadioButton protocolLsml;
    @FXML
    private Button smurfyConnect;
    @FXML
    private TextField smurfyKey;
    @FXML
    private CheckBox smurfyKeyRemember;
    @FXML
    private Label smurfyKeyValid;
    @FXML
    private ListView<Loadout> smurfyList;

    @Inject
    public ImportExportPageController(Settings aSettings, @Named("global") MessageXBar aXBar, CommandStack aStack,
                                      GlobalGarage aGlobalGarage, BatchImportExporter aBatchImporterExporter,
                                      SmurfyImportExport aSmurfyImportExport) {
        settings = aSettings;
        batchImporterExporter = aBatchImporterExporter;
        smurfyImportExport = aSmurfyImportExport;
        stack = aStack;
        globalGarage = aGlobalGarage;
        xBar = aXBar;

        protocol.selectedToggleProperty().addListener((aObservable, aOld, aNew) -> {
            if (aNew == protocolLsml) {
                protocolProperty.set(LsmlLinkProtocol.LSML);
            } else if (aNew == protocolHttp) {
                protocolProperty.set(LsmlLinkProtocol.HTTP);
            } else if (aNew == null) {
                aOld.setSelected(true);
            } else {
                throw new RuntimeException("Unknown toggle for group!");
            }
        });
        protocolLsml.setSelected(true);

        garageViewLSML.getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE);

        final Property<Boolean> rememberKeyProperty = settings.getBoolean(Settings.SMURFY_REMEMBER);
        final Property<String> apiKeyProperty = settings.getString(Settings.SMURFY_APIKEY);
        rememberKeyProperty.addListener((aObs, aOld, aNew) -> {
            if (aNew == Boolean.TRUE) {
                apiKeyProperty.setValue(smurfyKey.getText());
            } else {
                apiKeyProperty.setValue("");
            }
        });
        smurfyKeyRemember.selectedProperty().bindBidirectional(rememberKeyProperty);

        if (rememberKeyProperty.getValue() == Boolean.TRUE) {
            smurfyKey.setText(apiKeyProperty.getValue());
        }

        final BooleanBinding invalidApiKey = new BooleanBinding() {
            {
                bind(smurfyKey.textProperty());
            }

            @Override
            protected boolean computeValue() {
                return !SmurfyImportExport.isValidApiKey(smurfyKey.textProperty().get());
            }
        };
        smurfyKeyValid.visibleProperty().bind(invalidApiKey);
        smurfyConnect.disableProperty().bind(invalidApiKey);

        FxControlUtils.setupGarageTree(garageViewSmurfy, globalGarage.getGarage().getLoadoutRoot(), xBar, stack, false,
                                       Loadout.class);
        FxControlUtils.setupGarageTree(garageViewLSML, globalGarage.getGarage().getLoadoutRoot(), xBar, stack, true,
                                       Loadout.class);
    }

    @FXML
    public void exportSelectedLSML() throws IOException {
        final GarageDirectory<Loadout> garageRoot = new GarageDirectory<>("");

        for (final TreeItem<GaragePath<Loadout>> selected : garageViewLSML.getSelectionModel().getSelectedItems()) {
            final GaragePath<Loadout> value = selected.getValue();
            if (value.isLeaf()) {
                final Loadout loadout = value.getValue().get();
                final GarageDirectory<Loadout> targetDir = makeRecursiveDirs(garageRoot, value.getParent());
                targetDir.getValues().add(loadout);
            } else {
                final GarageDirectory<Loadout> sourceDir = value.getTopDirectory();
                final GarageDirectory<Loadout> targetDir = makeRecursiveDirs(garageRoot, value);
                targetDir.getValues().addAll(sourceDir.getValues());
                addAllChildrenRecursive(targetDir, sourceDir);
            }
        }

        try {
            batchImporterExporter.setProtocol(protocolProperty.get());
            final String exported = batchImporterExporter.export(garageRoot);
            linkInputOutput.setText(exported);
        } catch (final EncodingException e) {
            LiSongMechLab.showError(root, e);
        }
    }

    @FXML
    public void exportSelectedSmurfy() {
        final LsmlAlert alert = new LsmlAlert(root, AlertType.INFORMATION,
                                              "Export to Smurfy Mechbay is not yet supported.", ButtonType.OK);
        alert.showAndWait();
    }

    @FXML
    public void importSelectedLSML() throws Exception {
        final GarageDirectory<Loadout> importedRoot = batchImporterExporter.parse(linkInputOutput.getText());

        final GaragePath<Loadout> targetPath;
        final TreeItem<GaragePath<Loadout>> selectedTreeItem = garageViewLSML.getSelectionModel().getSelectedItem();
        if (null == selectedTreeItem) {
            targetPath = garageViewLSML.getRoot().getValue();
        } else {
            targetPath = selectedTreeItem.getValue();
        }

        if (!targetPath.isLeaf()) {
            LiSongMechLab.safeCommand(root, stack,
                                      new CmdGarageMergeDirectories<>("import LSML batch", xBar, targetPath,
                                                                      new GaragePath<>(importedRoot)), xBar);
        } else {
            showLsmlImportInstructions();
            return;
        }
    }

    @FXML
    public void importSelectedSmurfy() throws Exception {
        final TreeItem<GaragePath<Loadout>> item = garageViewSmurfy.getSelectionModel().getSelectedItem();
        boolean showInstructions = true;
        if (null != item) {
            final GaragePath<Loadout> directory = item.getValue();
            final ObservableList<Loadout> selected = smurfyList.getSelectionModel().getSelectedItems();
            if (!selected.isEmpty() && !directory.isLeaf()) {
                importMechs(directory, selected);
                showInstructions = false;
            }
        }

        if (showInstructions) {
            final LsmlAlert alert = new LsmlAlert(root, AlertType.INFORMATION);
            alert.setTitle("No mechs selected!");
            alert.setContentText(
                "Please select what mechs you want on the right and then select the folder you want to import them into on the left.");
            alert.show();
        }
    }

    @FXML
    public void refreshSmurfyGarage() {
        final String key = smurfyKey.getText();
        final Property<Boolean> rememberKeyProperty = settings.getBoolean(Settings.SMURFY_REMEMBER);
        final Property<String> apiKeyProperty = settings.getString(Settings.SMURFY_APIKEY);
        if (SmurfyImportExport.isValidApiKey(key.trim())) {
            try {
                final List<Loadout> loadouts = smurfyImportExport.listMechBay(key);
                smurfyList.getItems().setAll(loadouts);
                if (rememberKeyProperty.getValue() == Boolean.TRUE) {
                    apiKeyProperty.setValue(key);
                }
            } catch (final AccessDeniedException e) {
                final LsmlAlert alert = new LsmlAlert(root.getScene().getWindow(), AlertType.ERROR,
                                                      "Please check that you are using the correct API key as provided by smurfy.");
                alert.setTitle("Invalid API key!");
                alert.setHeaderText("No 'Mechbay exists for that API key.");
                alert.showAndWait();
            } catch (final Exception e) {
                LiSongMechLab.showError(root, e);
            }
        }
    }

    private void addAllChildrenRecursive(GarageDirectory<Loadout> aTarget, GarageDirectory<Loadout> aSource)
        throws IOException {
        for (final GarageDirectory<Loadout> sourceChild : aSource.getDirectories()) {
            final GarageDirectory<Loadout> targetChild = aTarget.makeDirsRecursive(sourceChild.getName());
            targetChild.getValues().addAll(sourceChild.getValues());
            addAllChildrenRecursive(targetChild, sourceChild);
        }
    }

    private void importMechs(GaragePath<Loadout> aDestinationDir, Collection<Loadout> selected) throws Exception {
        final CompositeCommand importCmd = new CompositeCommand("import mechs", xBar) {
            @Override
            protected void buildCommand() throws EquipException {
                for (final Loadout l : selected) {
                    addOp(new CmdGarageAdd<>(xBar, aDestinationDir, l));
                }
            }
        };
        stack.pushAndApply(importCmd);
    }

    private GarageDirectory<Loadout> makeRecursiveDirs(GarageDirectory<Loadout> implicitRoot, GaragePath<Loadout> value)
        throws IOException {
        final StringBuilder sb = new StringBuilder();
        assert !value.isLeaf();
        value.toPath(sb);
        // FIXME: This should be a command so that it can be undone.
        final GarageDirectory<Loadout> targetDir = implicitRoot.makeDirsRecursive(sb.toString());
        return targetDir;
    }

    private void showLsmlImportInstructions() {
        final LsmlAlert alert = new LsmlAlert(root, AlertType.INFORMATION);
        alert.setTitle("No destination folder selected!");
        alert.setContentText("Please select the destination folder to transfer the loadouts under from the left.");
        alert.show();
    }
}
