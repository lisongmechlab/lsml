/*
 * Li Song Mechlab - A 'mech building tool for PGI's MechWarrior: Online.
 * Copyright (C) 2013-2023  Li Song
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

import java.util.Set;
import java.util.Stack;
import java.util.stream.Collectors;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.input.KeyEvent;
import javax.inject.Inject;
import javax.inject.Named;
import org.lisoft.lsml.command.CmdGarageRename;
import org.lisoft.lsml.messages.*;
import org.lisoft.lsml.model.SearchIndex;
import org.lisoft.lsml.model.garage.GarageDirectory;
import org.lisoft.lsml.model.loadout.Loadout;
import org.lisoft.lsml.model.loadout.LoadoutFactory;
import org.lisoft.lsml.mwo_data.ChassisDB;
import org.lisoft.lsml.util.CommandStack;
import org.lisoft.lsml.view_fx.GlobalGarage;
import org.lisoft.lsml.view_fx.LiSongMechLab;
import org.lisoft.lsml.view_fx.Settings;
import org.lisoft.lsml.view_fx.controllers.AbstractFXController;
import org.lisoft.lsml.view_fx.util.FxControlUtils;
import org.lisoft.lsml.view_fx.util.FxTableUtils;

/**
 * This pane shows the search results based on a filter text.
 *
 * @author Li Song
 */
public class SearchResultsPaneController extends AbstractFXController implements MessageReceiver {
  private final Set<Loadout> allEmptyLoadouts;
  private final SimpleStringProperty filterString = new SimpleStringProperty();
  private final GlobalGarage globalGarage;
  private final CommandStack globalStack;
  private final LoadoutFactory loadoutFactory;
  private final ObservableList<Loadout> resultList;
  private final SearchIndex searchIndex = new SearchIndex();
  private final MessageXBar xBar;
  @FXML private TableView<Loadout> results;

  @Inject
  public SearchResultsPaneController(
      @Named("global") MessageXBar aXBar,
      CommandStack aStack,
      GlobalGarage aGlobalGarage,
      LoadoutFactory aLoadoutFactory,
      Settings aSettings) {
    xBar = aXBar;
    xBar.attach(this);
    globalStack = aStack;
    loadoutFactory = aLoadoutFactory;
    globalGarage = aGlobalGarage;

    allEmptyLoadouts =
        ChassisDB.lookupAll().stream()
            .map(
                c -> {
                  final Loadout l = loadoutFactory.produceDefault(c, aSettings);
                  l.setName("[New] " + l.getName());
                  return l;
                })
            .collect(Collectors.toUnmodifiableSet());

    buildIndex();
    FxTableUtils.setupChassisTable(results);

    resultList = FXCollections.observableArrayList();
    results.setItems(resultList);
    results.setRowFactory(tv -> makeSearchResultRow());
    filterString.addListener((aObs, aOld, aNew) -> refreshQuery(aNew));
  }

  @FXML
  public void closeWindow() {
    xBar.post(new ApplicationMessage(ApplicationMessage.Type.CLOSE_OVERLAY, root));
  }

  /**
   * This is necessary to allow ESC to close the overlay if one of the search results has focus.
   *
   * @param aEvent The event that triggered this call.
   */
  @FXML
  public void keyRelease(KeyEvent aEvent) {
    FxControlUtils.escapeWindow(aEvent, root, this::closeWindow);
  }

  @Override
  public void receive(Message aMsg) {
    if (aMsg instanceof final GarageMessage<?> garageMessage) {
      if (garageMessage.type == GarageMessageType.ADDED) {
        garageMessage
            .path
            .getValue()
            .ifPresent(
                document -> {
                  if (document instanceof Loadout) {
                    searchIndex.merge((Loadout) document);
                  }
                });
      } else if (garageMessage.type == GarageMessageType.REMOVED) {
        garageMessage
            .path
            .getValue()
            .ifPresent(
                document -> {
                  if (document instanceof Loadout) {
                    searchIndex.unmerge((Loadout) document);
                  }
                });
      } else {
        garageMessage
            .path
            .getValue()
            .ifPresent(
                document -> {
                  if (document instanceof Loadout) {
                    searchIndex.update();
                  }
                });
      }
      if (!filterString.isEmpty().get()) {
        refreshQuery(filterString.get());
      }
    }
  }

  /**
   * @return A {@link StringProperty} that holds the current search string.
   */
  public StringProperty searchStringProperty() {
    return filterString;
  }

  private void buildIndex() {
    allEmptyLoadouts.forEach(searchIndex::merge);

    final Stack<GarageDirectory<Loadout>> fringe = new Stack<>();
    fringe.push(globalGarage.getGarage().getLoadoutRoot());
    while (!fringe.empty()) {
      final GarageDirectory<Loadout> current = fringe.pop();
      for (final GarageDirectory<Loadout> child : current.getDirectories()) {
        fringe.push(child);
      }
      current.getValues().forEach(searchIndex::merge);
    }
  }

  private TableRow<Loadout> makeSearchResultRow() {
    final TableRow<Loadout> row = new TableRow<>();
    row.setOnMouseClicked(
        event -> {
          event.consume();
          if (row.isEmpty() && null != row.getItem()) {
            return;
          }
          Loadout loadout = row.getItem();

          if (FxControlUtils.isDoubleClick(event)) {
            if (allEmptyLoadouts.contains(loadout)) {
              // Don't modify proto layouts
              loadout = loadoutFactory.produceClone(loadout);
            }
            xBar.post(new ApplicationMessage(loadout, ApplicationMessage.Type.OPEN_LOADOUT, root));
          }
        });
    row.setContextMenu(showSearchResultContextMenu(row));
    return row;
  }

  private void refreshQuery(String aNew) {
    resultList.setAll(searchIndex.query(aNew));
  }

  private ContextMenu showSearchResultContextMenu(TableRow<Loadout> aRow) {
    final MenuItem rename = new MenuItem("Rename...");
    final MenuItem delete = new MenuItem("Delete...");
    final ContextMenu cm = new ContextMenu(rename, delete);
    cm.setOnShowing(
        e -> {
          if (aRow.isEmpty() && null != aRow.getItem()) {
            return;
          }
          final Loadout loadout = aRow.getItem();
          final boolean isProtoLayout = allEmptyLoadouts.contains(loadout);
          rename.setDisable(isProtoLayout);
          delete.setDisable(isProtoLayout);
          delete.setOnAction(
              event ->
                  globalGarage
                      .getGarage()
                      .getLoadoutRoot()
                      .find(loadout)
                      .ifPresent(p -> GlobalGarage.remove(p, root, globalStack, xBar)));
          rename.setOnAction(
              event -> {
                final TextInputDialog dialog = new TextInputDialog(loadout.getName());
                dialog.setTitle("Rename Loadout...");
                dialog.setHeaderText("Renaming loadout: " + loadout.getName());

                dialog
                    .showAndWait()
                    .ifPresent(
                        result ->
                            globalGarage
                                .getGarage()
                                .getLoadoutRoot()
                                .find(loadout)
                                .ifPresent(
                                    p ->
                                        LiSongMechLab.safeCommand(
                                            root,
                                            globalStack,
                                            new CmdGarageRename<>(xBar, p, result),
                                            xBar)));
              });
        });
    return cm;
  }
}
