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

import java.util.Collection;
import java.util.List;

import org.lisoft.lsml.command.CmdAddToGarage;
import org.lisoft.lsml.command.CmdMergeGarageDirectories;
import org.lisoft.lsml.messages.MessageXBar;
import org.lisoft.lsml.model.export.BatchImportExporter;
import org.lisoft.lsml.model.export.LsmlLinkProtocol;
import org.lisoft.lsml.model.export.SmurfyImportExport;
import org.lisoft.lsml.model.garage.GarageDirectory;
import org.lisoft.lsml.model.garage.GarageException;
import org.lisoft.lsml.model.garage.GaragePath;
import org.lisoft.lsml.model.loadout.EquipException;
import org.lisoft.lsml.model.loadout.Loadout;
import org.lisoft.lsml.util.CommandStack;
import org.lisoft.lsml.util.CommandStack.CompositeCommand;
import org.lisoft.lsml.util.EncodingException;
import org.lisoft.lsml.view_fx.util.FxControlUtils;

import javafx.beans.property.ObjectProperty;
import javafx.beans.property.Property;
import javafx.beans.property.SimpleObjectProperty;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.CheckBox;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.control.SelectionMode;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.control.ToggleButton;
import javafx.scene.control.ToggleGroup;
import javafx.scene.control.TreeItem;
import javafx.scene.control.TreeView;
import javafx.scene.layout.BorderPane;

/**
 * This page allows the user to mass export/import loadouts.
 *
 * @author Li Song
 *
 */
public class ImportExportPage extends BorderPane {
    private final BatchImportExporter batchImporterExporter;
    @FXML
    private TreeView<GaragePath<Loadout>> garageViewLSML;
    @FXML
    private TreeView<GaragePath<Loadout>> garageViewSmurfy;
    @FXML
    private TextArea linkInputOutput;
    @FXML
    private ToggleGroup protocol;
    @FXML
    private ToggleButton protocolHttp;
    @FXML
    private ToggleButton protocolLsml;
    private final ObjectProperty<LsmlLinkProtocol> protocolProperty;
    // FIXME: Replace by DI
    private final Settings settings = Settings.getSettings();
    private final SmurfyImportExport smurfyImportExport;
    @FXML
    private TextField smurfyKey;
    @FXML
    private CheckBox smurfyKeyRemember;
    @FXML
    private Label smurfyKeyValid;
    @FXML
    private ListView<Loadout> smurfyList;
    private final CommandStack stack;
    private final MessageXBar xBar;

    private final GlobalGarage globalGarage = GlobalGarage.instance;

    // FIXME Make clan/IS filter apply

    public ImportExportPage(MessageXBar aXBar, BatchImportExporter aBatchImporterExporter,
            SmurfyImportExport aSmurfyImportExport, CommandStack aStack) {
        FxControlUtils.loadFxmlControl(this);
        batchImporterExporter = aBatchImporterExporter;
        smurfyImportExport = aSmurfyImportExport;
        stack = aStack;
        xBar = aXBar;

        protocolProperty = new SimpleObjectProperty<LsmlLinkProtocol>();
        protocol.selectedToggleProperty().addListener((aObservable, aOld, aNew) -> {
            if (aNew == protocolLsml) {
                protocolProperty.set(LsmlLinkProtocol.LSML);
            }
            else if (aNew == protocolHttp) {
                protocolProperty.set(LsmlLinkProtocol.HTTP);
            }
            else if (aNew == null) {
                aOld.setSelected(true);
            }
            else {
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
            }
            else {
                apiKeyProperty.setValue("");
            }
        });
        smurfyKeyRemember.selectedProperty().bindBidirectional(rememberKeyProperty);
        smurfyKey.textProperty().addListener((aObs, aOld, aNew) -> {
            if (SmurfyImportExport.isValidApiKey(aNew.trim())) {
                smurfyKeyValid.setVisible(false);
                try {
                    final List<Loadout> loadouts = smurfyImportExport.listMechBay(aNew);
                    smurfyList.getItems().setAll(loadouts);
                    if (rememberKeyProperty.getValue() == Boolean.TRUE) {
                        apiKeyProperty.setValue(aNew);
                    }
                }
                catch (final Exception e) {
                    LiSongMechLab.showError(this, e);
                }
            }
            else {
                smurfyKeyValid.setVisible(!aNew.trim().isEmpty());
            }
        });

        if (rememberKeyProperty.getValue() == Boolean.TRUE) {
            smurfyKey.setText(apiKeyProperty.getValue());
        }

        FxControlUtils.setupGarageTree(garageViewSmurfy, globalGarage.getGarage().getLoadoutRoot(), xBar, stack, false);
        FxControlUtils.setupGarageTree(garageViewLSML, globalGarage.getGarage().getLoadoutRoot(), xBar, stack, true);
    }

    @FXML
    public void exportSelectedLSML() {
        final GarageDirectory<Loadout> root = new GarageDirectory<>("");

        for (final TreeItem<GaragePath<Loadout>> selected : garageViewLSML.getSelectionModel().getSelectedItems()) {
            final GaragePath<Loadout> value = selected.getValue();
            if (value.isLeaf()) {
                final Loadout loadout = value.getValue().get();
                final GarageDirectory<Loadout> targetDir = makeRecursiveDirs(root, value.getParent());
                targetDir.getValues().add(loadout);
            }
            else {
                final GarageDirectory<Loadout> sourceDir = value.getTopDirectory();
                final GarageDirectory<Loadout> targetDir = makeRecursiveDirs(root, value);
                targetDir.getValues().addAll(sourceDir.getValues());
                addAllChildrenRecursive(targetDir, sourceDir);
            }
        }

        try {
            batchImporterExporter.setProtocol(protocolProperty.get());
            final String exported = batchImporterExporter.export(root);
            linkInputOutput.setText(exported);
        }
        catch (final EncodingException e) {
            LiSongMechLab.showError(this, e);
        }
    }

    @FXML
    public void exportSelectedSmurfy() {
        // TODO: When smurfy supports import into mechlab.
    }

    @FXML
    public void importSelectedLSML() throws Exception {
        final GarageDirectory<Loadout> importedRoot = batchImporterExporter.parse(linkInputOutput.getText());

        final GaragePath<Loadout> targetPath;
        final TreeItem<GaragePath<Loadout>> selectedTreeItem = garageViewLSML.getSelectionModel().getSelectedItem();
        if (null == selectedTreeItem) {
            targetPath = garageViewLSML.getRoot().getValue();
        }
        else {
            targetPath = selectedTreeItem.getValue();
        }

        if (!targetPath.isLeaf()) {
            final GarageDirectory<Loadout> selectedDst = targetPath.getTopDirectory();
            try {
                stack.pushAndApply(
                        new CmdMergeGarageDirectories<>("import LSML batch", xBar, selectedDst, importedRoot));
            }
            catch (final GarageException exception) {
                LiSongMechLab.showError(this, exception);
            }
        }
        else {
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
                importMechs(directory.getTopDirectory(), selected);
                showInstructions = false;
            }
        }

        if (showInstructions) {
            final Alert alert = new Alert(AlertType.INFORMATION);
            alert.setTitle("No mechs selected!");
            alert.setContentText(
                    "Please select what mechs you want on the right and then select the folder you want to import them into on the left.");
            alert.show();
        }
    }

    private void addAllChildrenRecursive(GarageDirectory<Loadout> aTarget, GarageDirectory<Loadout> aSource) {
        for (final GarageDirectory<Loadout> sourceChild : aSource.getDirectories()) {
            final GarageDirectory<Loadout> targetChild = aTarget.makeDirsRecursive(sourceChild.getName());
            targetChild.getValues().addAll(sourceChild.getValues());
            addAllChildrenRecursive(targetChild, sourceChild);
        }
    }

    private void importMechs(GarageDirectory<Loadout> directory, Collection<Loadout> selected) throws Exception {
        final CompositeCommand importCmd = new CompositeCommand("import mechs", xBar) {
            @Override
            protected void buildCommand() throws EquipException {
                for (final Loadout l : selected) {
                    addOp(new CmdAddToGarage<Loadout>(xBar, directory, l));
                }
            }
        };
        stack.pushAndApply(importCmd);
    }

    private GarageDirectory<Loadout> makeRecursiveDirs(GarageDirectory<Loadout> implicitRoot,
            GaragePath<Loadout> value) {
        final StringBuilder sb = new StringBuilder();
        assert (!value.isLeaf());
        value.toPath(sb);
        // FIXME: This should be a command so that it can be undone.
        final GarageDirectory<Loadout> targetDir = implicitRoot.makeDirsRecursive(sb.toString());
        return targetDir;
    }

    private void showLsmlImportInstructions() {
        final Alert alert = new Alert(AlertType.INFORMATION);
        alert.setTitle("No destination folder selected!");
        alert.setContentText("Please select the destination folder to transfer the loadouts under from the left.");
        alert.show();
    }
}
