/*
 * Li Song Mechlab - A 'mech building tool for PGI's MechWarrior: Online.
 * Copyright (C) 2013-2022  Li Song
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
package org.lisoft.lsml.view_fx.controllers.mainwindow;

import java.io.IOException;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.Alert.AlertType;
import javax.inject.Inject;
import javax.inject.Named;
import org.lisoft.lsml.command.CmdGarageMergeDirectories;
import org.lisoft.lsml.messages.MessageXBar;
import org.lisoft.lsml.model.export.BatchImportExporter;
import org.lisoft.lsml.model.export.LsmlLinkProtocol;
import org.lisoft.lsml.model.garage.GarageDirectory;
import org.lisoft.lsml.model.garage.GaragePath;
import org.lisoft.lsml.model.loadout.Loadout;
import org.lisoft.lsml.util.CommandStack;
import org.lisoft.lsml.util.EncodingException;
import org.lisoft.lsml.view_fx.GlobalGarage;
import org.lisoft.lsml.view_fx.LiSongMechLab;
import org.lisoft.lsml.view_fx.controllers.AbstractFXController;
import org.lisoft.lsml.view_fx.controls.LsmlAlert;
import org.lisoft.lsml.view_fx.util.FxControlUtils;

/**
 * This page allows the user to mass export/import loadouts.
 *
 * @author Li Song
 */
public class ImportExportPageController extends AbstractFXController {
  private final BatchImportExporter batchImporterExporter;
  private final ObjectProperty<LsmlLinkProtocol> protocolProperty = new SimpleObjectProperty<>();
  private final CommandStack stack;
  private final MessageXBar xBar;
  @FXML private TreeView<GaragePath<Loadout>> garageViewLSML;
  @FXML private TextArea linkInputOutput;
  @FXML private ToggleGroup protocol;
  @FXML private RadioButton protocolHttp;
  @FXML private RadioButton protocolLsml;

  @Inject
  public ImportExportPageController(
      @Named("global") MessageXBar aXBar,
      CommandStack aStack,
      GlobalGarage aGlobalGarage,
      BatchImportExporter aBatchImporterExporter) {
    batchImporterExporter = aBatchImporterExporter;
    stack = aStack;
    xBar = aXBar;

    protocol
        .selectedToggleProperty()
        .addListener(
            (aObservable, aOld, aNew) -> {
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

    FxControlUtils.setupGarageTree(
        garageViewLSML,
        aGlobalGarage.getGarage().getLoadoutRoot(),
        xBar,
        stack,
        true,
        Loadout.class);
  }

  @FXML
  public void exportSelectedLSML() throws IOException {
    final GarageDirectory<Loadout> garageRoot = new GarageDirectory<>("");

    for (final TreeItem<GaragePath<Loadout>> selected :
        garageViewLSML.getSelectionModel().getSelectedItems()) {
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
  public void importSelectedLSML() {
    final GarageDirectory<Loadout> importedRoot =
        batchImporterExporter.parse(linkInputOutput.getText());

    final GaragePath<Loadout> targetPath;
    final TreeItem<GaragePath<Loadout>> selectedTreeItem =
        garageViewLSML.getSelectionModel().getSelectedItem();
    if (null == selectedTreeItem) {
      targetPath = garageViewLSML.getRoot().getValue();
    } else {
      targetPath = selectedTreeItem.getValue();
    }

    if (!targetPath.isLeaf()) {
      LiSongMechLab.safeCommand(
          root,
          stack,
          new CmdGarageMergeDirectories<>(
              "import LSML batch", xBar, targetPath, new GaragePath<>(importedRoot)),
          xBar);
    } else {
      showLsmlImportInstructions();
    }
  }

  private void addAllChildrenRecursive(
      GarageDirectory<Loadout> aTarget, GarageDirectory<Loadout> aSource) throws IOException {
    for (final GarageDirectory<Loadout> sourceChild : aSource.getDirectories()) {
      final GarageDirectory<Loadout> targetChild = aTarget.makeDirsRecursive(sourceChild.getName());
      targetChild.getValues().addAll(sourceChild.getValues());
      addAllChildrenRecursive(targetChild, sourceChild);
    }
  }

  private GarageDirectory<Loadout> makeRecursiveDirs(
      GarageDirectory<Loadout> implicitRoot, GaragePath<Loadout> value) throws IOException {
    final StringBuilder sb = new StringBuilder();
    assert !value.isLeaf();
    value.toPath(sb);
    // FIXME: This should be a command so that it can be undone.
    return implicitRoot.makeDirsRecursive(sb.toString());
  }

  private void showLsmlImportInstructions() {
    final LsmlAlert alert = new LsmlAlert(root, AlertType.INFORMATION);
    alert.setTitle("No destination folder selected!");
    alert.setContentText(
        "Please select the destination folder to transfer the loadouts under from the left.");
    alert.show();
  }
}
