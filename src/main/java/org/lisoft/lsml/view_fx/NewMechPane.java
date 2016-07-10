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

import static org.lisoft.lsml.view_fx.util.FxControlUtils.fixSpinner;

import java.util.ArrayList;
import java.util.List;

import org.lisoft.lsml.messages.MessageXBar;
import org.lisoft.lsml.model.chassi.Chassis;
import org.lisoft.lsml.model.chassi.ChassisFilter;
import org.lisoft.lsml.model.chassi.OmniPodSelector;
import org.lisoft.lsml.model.datacache.ChassisDB;
import org.lisoft.lsml.model.item.Faction;
import org.lisoft.lsml.model.loadout.DefaultLoadoutFactory;
import org.lisoft.lsml.model.loadout.Loadout;
import org.lisoft.lsml.view_fx.util.FxBindingUtils;
import org.lisoft.lsml.view_fx.util.FxControlUtils;
import org.lisoft.lsml.view_fx.util.FxTableUtils;

import javafx.beans.binding.ObjectBinding;
import javafx.fxml.FXML;
import javafx.scene.control.CheckBox;
import javafx.scene.control.Spinner;
import javafx.scene.control.SpinnerValueFactory.IntegerSpinnerValueFactory;
import javafx.scene.control.TableView;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.BorderPane;

/**
 * This pane will show a dialog where the user can use filters to find a mech that matches certain criteria.
 *
 * @author Li Song
 */
public class NewMechPane extends BorderPane {
    @FXML
    private Spinner<Integer> filterMaxMass;
    @FXML
    private Spinner<Integer> filterMinMass;
    @FXML
    private Spinner<Integer> filterMinSpeed;
    @FXML
    private Spinner<Integer> filterMinBallistic;
    @FXML
    private Spinner<Integer> filterMinEnergy;
    @FXML
    private Spinner<Integer> filterMinMissile;
    @FXML
    private CheckBox filterAllowHero;
    @FXML
    private CheckBox filterClan;
    @FXML
    private CheckBox filterInnerSphere;
    @FXML
    private Spinner<Integer> filterMinJumpJets;
    @FXML
    private TableView<Loadout> resultsTable;

    @FXML
    private CheckBox filterECM;
    private final Runnable onClose;

    // FIXME: Inject through DI
    private final ChassisFilter chassisFilter;

    private final MessageXBar xBar;

    /**
     * @param aOnClose
     *            A callback to call when the close button is pressed.
     * @param aXBar
     *            The message xBar to use for global messages from a new loadout.
     * @param aSettings
     *            The settings to use in this pane.
     */
    public NewMechPane(Runnable aOnClose, MessageXBar aXBar, Settings aSettings) {
        FxControlUtils.loadFxmlControl(this);
        onClose = aOnClose;
        xBar = aXBar;

        final ObjectBinding<Faction> factionFilter = FxBindingUtils.createFactionBinding(filterClan.selectedProperty(),
                filterInnerSphere.selectedProperty());

        final List<Chassis> aChassis = new ArrayList<>(ChassisDB.lookupAll());
        chassisFilter = new ChassisFilter(aChassis, DefaultLoadoutFactory.instance, new OmniPodSelector(), aSettings);

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

        chassisFilter.factionFilterProperty().bind(factionFilter);
        chassisFilter.ecmFilterProperty().bind(filterECM.selectedProperty());
        chassisFilter.minBallisticFilterProperty().bind(filterMinBallistic.valueProperty());
        chassisFilter.minEnergyFilterProperty().bind(filterMinEnergy.valueProperty());
        chassisFilter.minMissileFilterProperty().bind(filterMinMissile.valueProperty());
        chassisFilter.maxMassFilterProperty().bind(filterMaxMass.valueProperty());
        chassisFilter.minMassFilterProperty().bind(filterMinMass.valueProperty());
        chassisFilter.minSpeedFilterProperty().bind(filterMinSpeed.valueProperty());
        chassisFilter.minJumpJetFilterProperty().bind(filterMinJumpJets.valueProperty());
        chassisFilter.heroFilterProperty().bind(filterAllowHero.selectedProperty());

        resultsTable.setItems(chassisFilter.getChildren());
        FxTableUtils.setupChassisTable(resultsTable);
    }

    @FXML
    public void closeNewMech() {
        onClose.run();
    }

    @FXML
    public void createFromSelected() {
        final Loadout loadout = resultsTable.getSelectionModel().getSelectedItem();
        if (null != loadout) {
            LiSongMechLab.openLoadout(xBar, loadout);
        }
    }

    @FXML
    public void keyRelease(KeyEvent aEvent) {
        FxControlUtils.escapeWindow(aEvent, this, () -> closeNewMech());
    }
}
