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

import java.util.List;

import org.lisoft.lsml.messages.GarageMessage;
import org.lisoft.lsml.messages.Message;
import org.lisoft.lsml.messages.MessageReceiver;
import org.lisoft.lsml.messages.MessageReception;
import org.lisoft.lsml.model.export.BatchImportExporter;
import org.lisoft.lsml.model.export.LsmlLinkProtocol;
import org.lisoft.lsml.model.export.SmurfyImportExport;
import org.lisoft.lsml.model.garage.Garage;
import org.lisoft.lsml.model.garage.GarageDirectory;
import org.lisoft.lsml.model.loadout.Loadout;
import org.lisoft.lsml.util.EncodingException;
import org.lisoft.lsml.view_fx.style.StyleManager;
import org.lisoft.lsml.view_fx.util.FxmlHelpers;

import javafx.beans.property.ObjectProperty;
import javafx.beans.property.Property;
import javafx.beans.property.SimpleObjectProperty;
import javafx.fxml.FXML;
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
public class ImportExportPage extends BorderPane implements MessageReceiver {
    private final BatchImportExporter              batchImporterExporter;
    @FXML
    private ToggleGroup                            protocol;
    @FXML
    private ToggleButton                           protocolLsml;
    @FXML
    private ToggleButton                           protocolHttp;
    private final ObjectProperty<LsmlLinkProtocol> protocolProperty;
    @FXML
    private TreeView<Object>                       garageViewLSML;
    private final Garage                           garage;
    @FXML
    private TextArea                               linkInputOutput;
    @FXML
    private TextField                              smurfyKey;
    @FXML
    private CheckBox                               smurfyKeyRemember;
    @FXML
    private TreeView<Object>                       garageViewSmurfy;
    @FXML
    private ListView<Loadout>                      smurfyList;
    private final SmurfyImportExport               smurfyImportExport;
    @FXML
    private Label                                  smurfyKeyValid;
    // FIXME: Replace by DI
    private final Settings                         settings = Settings.getSettings();

    // FIXME Make clan/IS filter apply

    public ImportExportPage(MessageReception aReception, Garage aGarage, BatchImportExporter aBatchImporterExporter,
            SmurfyImportExport aSmurfyImportExport) {
        FxmlHelpers.loadFxmlControl(this);
        aReception.attach(this);
        batchImporterExporter = aBatchImporterExporter;
        smurfyImportExport = aSmurfyImportExport;
        garage = aGarage;

        protocolProperty = new SimpleObjectProperty<LsmlLinkProtocol>();
        protocol.selectedToggleProperty().addListener((aObservable, aOld, aNew) -> {
            if (aNew == protocolLsml) {
                protocolProperty.set(LsmlLinkProtocol.LSML);
            }
            else if (aNew == protocolHttp) {
                protocolProperty.set(LsmlLinkProtocol.HTTP);
            }
            else {
                throw new RuntimeException("Unknown toggle for group!");
            }
        });
        protocolLsml.setSelected(true);

        garageViewLSML.getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE);
        updateGarageView();

        Property<Boolean> rememberKeyProperty = settings.getProperty(Settings.SMURFY_REMEMBER, Boolean.class);
        Property<String> apiKeyProperty = settings.getProperty(Settings.SMURFY_APIKEY, String.class);
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
                    List<Loadout> loadouts = smurfyImportExport.listMechBay(aNew);
                    smurfyList.getItems().setAll(loadouts);
                    if (rememberKeyProperty.getValue() == Boolean.TRUE) {
                        apiKeyProperty.setValue(aNew);
                    }
                }
                catch (Exception e) {
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

    }

    private void updateGarageView() {
        GarageDirectory<Loadout> root = garage.getLoadoutRoot();
        TreeItem<Object> treeRoot = new TreeItem<Object>(root, StyleManager.makeDirectoryIcon());
        garageViewLSML.setRoot(treeRoot);
        addChildrenToView(root, treeRoot);
    }

    private void addChildrenToView(GarageDirectory<Loadout> aGarageParent, TreeItem<Object> aTreeParent) {
        for (GarageDirectory<Loadout> childGarageDir : aGarageParent.getDirectories()) {
            TreeItem<Object> childTreeItem = new TreeItem<Object>(childGarageDir, StyleManager.makeDirectoryIcon());
            aTreeParent.getChildren().add(childTreeItem);
            addChildrenToView(childGarageDir, childTreeItem);
        }

        for (Loadout loadout : aGarageParent.getValues()) {
            TreeItem<Object> childTreeItem = new TreeItem<Object>(loadout);
            aTreeParent.getChildren().add(childTreeItem);
        }
    }

    private void addAllChildrenRecursive(GarageDirectory<Loadout> aTarget, GarageDirectory<Loadout> aSource) {
        for (GarageDirectory<Loadout> sourceChild : aSource.getDirectories()) {
            GarageDirectory<Loadout> targetChild = aTarget.makeDirsRecursive(sourceChild.getName());
            targetChild.getValues().addAll(sourceChild.getValues());
            addAllChildrenRecursive(targetChild, sourceChild);
        }
    }

    @FXML
    public void exportSelectedSmurfy() {
    }

    @FXML
    public void importSelectedSmurfy() {

    }

    @FXML
    public void exportSelectedLSML() {
        GarageDirectory<Loadout> implicitRoot = new GarageDirectory<>("");

        for (TreeItem<Object> selected : garageViewLSML.getSelectionModel().getSelectedItems()) {
            Object value = selected.getValue();
            if (value instanceof GarageDirectory) {
                // GarageDirectory<Loadout> is the only type of garage directory in the tree.
                @SuppressWarnings("unchecked")
                GarageDirectory<Loadout> sourceDir = (GarageDirectory<Loadout>) value;
                List<String> path = FxmlHelpers.getTreePath(selected);
                GarageDirectory<Loadout> targetDir = implicitRoot.makeDirsRecursive(path);
                targetDir.getValues().addAll(sourceDir.getValues());
                addAllChildrenRecursive(targetDir, sourceDir);
            }
            else if (value instanceof Loadout) {
                Loadout loadout = (Loadout) value;
                List<String> path = FxmlHelpers.getTreePath(selected.getParent());
                GarageDirectory<Loadout> targetDir = implicitRoot.makeDirsRecursive(path);
                targetDir.getValues().add(loadout);
            }
            else {
                throw new RuntimeException(
                        "Unknown value selected! : " + value.toString() + " type: " + value.getClass().getSimpleName());
            }
        }

        try {
            batchImporterExporter.setProtocol(protocolProperty.get());
            String exported = batchImporterExporter.export(implicitRoot);
            linkInputOutput.setText(exported);
        }
        catch (EncodingException e) {
            LiSongMechLab.showError(this, e);
        }
    }

    @FXML
    public void importSelectedLSML() {
        GarageDirectory<Loadout> importedRoot = batchImporterExporter.parse(linkInputOutput.getText());
    }

    @Override
    public void receive(Message aMsg) {
        if (aMsg instanceof GarageMessage) {
            updateGarageView();
        }
    }

}
