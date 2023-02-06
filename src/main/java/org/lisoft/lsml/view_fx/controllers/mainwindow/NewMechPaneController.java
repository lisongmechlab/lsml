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

import static org.lisoft.lsml.view_fx.util.FxControlUtils.fixSpinner;

import javafx.beans.binding.ObjectBinding;
import javafx.fxml.FXML;
import javafx.scene.control.CheckBox;
import javafx.scene.control.Spinner;
import javafx.scene.control.SpinnerValueFactory.IntegerSpinnerValueFactory;
import javafx.scene.control.TableRow;
import javafx.scene.control.TableView;
import javafx.scene.input.KeyEvent;
import javax.inject.Inject;
import javax.inject.Named;
import org.lisoft.lsml.messages.ApplicationMessage;
import org.lisoft.lsml.messages.MessageXBar;
import org.lisoft.lsml.model.loadout.Loadout;
import org.lisoft.lsml.model.loadout.LoadoutFactory;
import org.lisoft.lsml.mwo_data.ChassisDB;
import org.lisoft.lsml.mwo_data.Faction;
import org.lisoft.lsml.view_fx.controllers.AbstractFXController;
import org.lisoft.lsml.view_fx.controls.ChassisFilter;
import org.lisoft.lsml.view_fx.util.FxBindingUtils;
import org.lisoft.lsml.view_fx.util.FxControlUtils;
import org.lisoft.lsml.view_fx.util.FxTableUtils;

/**
 * This pane will show a dialog where the user can use filters to find a mech that matches certain
 * criteria.
 *
 * @author Li Song
 */
public class NewMechPaneController extends AbstractFXController {
  private final MessageXBar xBar;
  @FXML private CheckBox filterAllowHero;
  @FXML private CheckBox filterClan;
  @FXML private CheckBox filterECM;
  @FXML private CheckBox filterInnerSphere;
  @FXML private CheckBox filterMASC;
  @FXML private Spinner<Integer> filterMaxMass;
  @FXML private Spinner<Integer> filterMinBallistic;
  @FXML private Spinner<Integer> filterMinEnergy;
  @FXML private Spinner<Integer> filterMinJumpJets;
  @FXML private Spinner<Integer> filterMinMass;
  @FXML private Spinner<Integer> filterMinMissile;
  @FXML private Spinner<Integer> filterMinSpeed;
  @FXML private TableView<Loadout> resultsTable;

  @Inject
  public NewMechPaneController(
      @Named("global") MessageXBar aXBar,
      LoadoutFactory aLoadoutFactory,
      ChassisFilter aChassisFilter) {
    xBar = aXBar;
    aChassisFilter.setAll(ChassisDB.lookupAll());
    filterMinMass.setValueFactory(new IntegerSpinnerValueFactory(20, 100, 20, 5));
    filterMaxMass.setValueFactory(new IntegerSpinnerValueFactory(20, 100, 100, 5));
    filterMinSpeed.setValueFactory(new IntegerSpinnerValueFactory(0, 200, 0, 5));
    fixSpinner(filterMinMass);
    fixSpinner(filterMaxMass);
    fixSpinner(filterMinSpeed);

    filterMinBallistic.setValueFactory(new IntegerSpinnerValueFactory(0, 16, 0, 1));
    filterMinEnergy.setValueFactory(new IntegerSpinnerValueFactory(0, 16, 0, 1));
    filterMinMissile.setValueFactory(new IntegerSpinnerValueFactory(0, 16, 0, 1));
    filterMinJumpJets.setValueFactory(new IntegerSpinnerValueFactory(0, 16, 0, 1));
    fixSpinner(filterMinBallistic);
    fixSpinner(filterMinEnergy);
    fixSpinner(filterMinMissile);
    fixSpinner(filterMinJumpJets);

    final ObjectBinding<Faction> factionFilter =
        FxBindingUtils.createFactionBinding(
            filterClan.selectedProperty(), filterInnerSphere.selectedProperty());
    aChassisFilter.factionFilterProperty().bind(factionFilter);
    aChassisFilter.ecmFilterProperty().bind(filterECM.selectedProperty());
    aChassisFilter.mascFilterProperty().bind(filterMASC.selectedProperty());
    aChassisFilter.minBallisticFilterProperty().bind(filterMinBallistic.valueProperty());
    aChassisFilter.minEnergyFilterProperty().bind(filterMinEnergy.valueProperty());
    aChassisFilter.minMissileFilterProperty().bind(filterMinMissile.valueProperty());
    aChassisFilter.maxMassFilterProperty().bind(filterMaxMass.valueProperty());
    aChassisFilter.minMassFilterProperty().bind(filterMinMass.valueProperty());
    aChassisFilter.minSpeedFilterProperty().bind(filterMinSpeed.valueProperty());
    aChassisFilter.minJumpJetFilterProperty().bind(filterMinJumpJets.valueProperty());
    aChassisFilter.heroFilterProperty().bind(filterAllowHero.selectedProperty());

    resultsTable.setItems(aChassisFilter.getChildren());
    resultsTable.setRowFactory(
        tv -> {
          final TableRow<Loadout> row = new TableRow<>();
          row.setOnMouseClicked(
              event -> {
                if (FxControlUtils.isDoubleClick(event) && !row.isEmpty()) {
                  final Loadout loadout = row.getItem();
                  if (null != loadout) {
                    final Loadout clone = aLoadoutFactory.produceClone(loadout);
                    xBar.post(
                        new ApplicationMessage(clone, ApplicationMessage.Type.OPEN_LOADOUT, root));
                  }
                }
              });
          return row;
        });
    FxTableUtils.setupChassisTable(resultsTable);
  }

  @FXML
  public void closeNewMech() {
    xBar.post(new ApplicationMessage(ApplicationMessage.Type.CLOSE_OVERLAY, root));
  }

  /**
   * This is necessary to allow ESC to close the overlay if one of the search results has focus.
   *
   * @param aEvent The event that triggered this call.
   */
  @FXML
  public void keyRelease(KeyEvent aEvent) {
    FxControlUtils.escapeWindow(aEvent, root, this::closeNewMech);
  }
}
