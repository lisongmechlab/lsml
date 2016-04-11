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

import static org.lisoft.lsml.view_fx.util.FxTableUtils.addAttributeColumn;
import static org.lisoft.lsml.view_fx.util.FxTableUtils.addTotalHardpointsColumn;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.lisoft.lsml.messages.MessageXBar;
import org.lisoft.lsml.model.chassi.Chassis;
import org.lisoft.lsml.model.chassi.ChassisClass;
import org.lisoft.lsml.model.chassi.ChassisFilter;
import org.lisoft.lsml.model.chassi.HardPointType;
import org.lisoft.lsml.model.chassi.OmniPodSelector;
import org.lisoft.lsml.model.datacache.ChassisDB;
import org.lisoft.lsml.model.datacache.ModifiersDB;
import org.lisoft.lsml.model.item.BallisticWeapon;
import org.lisoft.lsml.model.item.EnergyWeapon;
import org.lisoft.lsml.model.item.Faction;
import org.lisoft.lsml.model.item.MissileWeapon;
import org.lisoft.lsml.model.item.Weapon;
import org.lisoft.lsml.model.loadout.DefaultLoadoutFactory;
import org.lisoft.lsml.model.loadout.Loadout;
import org.lisoft.lsml.model.modifiers.MechEfficiencyType;
import org.lisoft.lsml.model.modifiers.Modifier;
import org.lisoft.lsml.view_fx.style.FilteredModifierFormatter;
import org.lisoft.lsml.view_fx.util.FxBindingUtils;
import org.lisoft.lsml.view_fx.util.FxControlUtils;

import javafx.beans.binding.ObjectBinding;
import javafx.beans.property.ReadOnlyObjectWrapper;
import javafx.beans.property.ReadOnlyStringWrapper;
import javafx.fxml.FXML;
import javafx.scene.control.CheckBox;
import javafx.scene.control.Spinner;
import javafx.scene.control.SpinnerValueFactory;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableRow;
import javafx.scene.control.TableView;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.VBox;

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

        ObjectBinding<Faction> factionFilter = FxBindingUtils.createFactionBinding(filterClan.selectedProperty(),
                filterInnerSphere.selectedProperty());

        List<Chassis> aChassis = new ArrayList<>();
        aChassis.addAll(ChassisDB.lookup(ChassisClass.LIGHT));
        aChassis.addAll(ChassisDB.lookup(ChassisClass.MEDIUM));
        aChassis.addAll(ChassisDB.lookup(ChassisClass.HEAVY));
        aChassis.addAll(ChassisDB.lookup(ChassisClass.ASSAULT));
        chassisFilter = new ChassisFilter(aChassis, DefaultLoadoutFactory.instance, new OmniPodSelector(), aSettings);

        filterMinMass.setValueFactory(new SpinnerValueFactory.IntegerSpinnerValueFactory(20, 100, 20, 5));
        filterMaxMass.setValueFactory(new SpinnerValueFactory.IntegerSpinnerValueFactory(20, 100, 100, 5));
        filterMinSpeed.setValueFactory(new SpinnerValueFactory.IntegerSpinnerValueFactory(0, 200, 0, 5));

        filterMinBallistic.setValueFactory(new SpinnerValueFactory.IntegerSpinnerValueFactory(0, 16, 0, 1));
        filterMinEnergy.setValueFactory(new SpinnerValueFactory.IntegerSpinnerValueFactory(0, 16, 0, 1));
        filterMinMissile.setValueFactory(new SpinnerValueFactory.IntegerSpinnerValueFactory(0, 16, 0, 1));
        filterMinJumpJets.setValueFactory(new SpinnerValueFactory.IntegerSpinnerValueFactory(0, 16, 0, 1));

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
        resultsTable.setRowFactory(tv -> {
            TableRow<Loadout> row = new TableRow<>();
            row.setOnMouseClicked(event -> {
                if (event.getClickCount() == 2 && (!row.isEmpty())) {
                    createFromSelected();
                }
            });
            return row;
        });

        resultsTable.getColumns().clear();
        addAttributeColumn(resultsTable, "Name", "chassis.name");
        addAttributeColumn(resultsTable, "Mass", "chassis.massMax");
        addAttributeColumn(resultsTable, "Faction", "chassis.faction");

        addAttributeColumn(resultsTable, "JJ", "jumpJetsMax");

        TableColumn<Loadout, String> col = new TableColumn<>(HardPointType.ECM.shortName());
        col.setCellValueFactory(aFeatures -> new ReadOnlyStringWrapper(
                aFeatures.getValue().getHardpointsCount(HardPointType.ECM) > 0 ? "Yes" : "No"));
        resultsTable.getColumns().add(col);

        TableColumn<Loadout, String> hardpointsCol = new TableColumn<>("Hard Points");
        addTotalHardpointsColumn(hardpointsCol.getColumns(), HardPointType.ENERGY);
        addTotalHardpointsColumn(hardpointsCol.getColumns(), HardPointType.BALLISTIC);
        addTotalHardpointsColumn(hardpointsCol.getColumns(), HardPointType.MISSILE);
        resultsTable.getColumns().add(hardpointsCol);

        TableColumn<Loadout, String> quirksCol = new TableColumn<>("Quirks");
        quirksCol.getColumns().add(makeQuirkColumn(EnergyWeapon.class, HardPointType.ENERGY));
        quirksCol.getColumns().add(makeQuirkColumn(BallisticWeapon.class, HardPointType.BALLISTIC));
        quirksCol.getColumns().add(makeQuirkColumn(MissileWeapon.class, HardPointType.MISSILE));
        resultsTable.getColumns().add(quirksCol);
    }

    @FXML
    public void createFromSelected() {
        Loadout loadout = resultsTable.getSelectionModel().getSelectedItem();
        if (null != loadout) {
            LiSongMechLab.openLoadout(xBar, loadout);
        }
    }

    private TableColumn<Loadout, Collection<Modifier>> makeQuirkColumn(Class<? extends Weapon> aClass,
            HardPointType aHardPointType) {

        TableColumn<Loadout, Collection<Modifier>> col = new TableColumn<>(aHardPointType.shortName());
        col.setCellValueFactory(aFeatures -> new ReadOnlyObjectWrapper<>(aFeatures.getValue().getModifiers()));
        col.setCellFactory(aView -> new TableCell<Loadout, Collection<Modifier>>() {
            Collection<String> selectors = ModifiersDB.getAllSelectors(aClass);
            FilteredModifierFormatter formatter = new FilteredModifierFormatter(selectors);

            @Override
            protected void updateItem(Collection<Modifier> aModifiers, boolean aEmpty) {
                if (null != aModifiers && !aEmpty) {
                    VBox g = new VBox();
                    aModifiers.removeAll(ModifiersDB.lookupEfficiencyModifiers(MechEfficiencyType.FAST_FIRE, false));
                    formatter.format(aModifiers, g.getChildren());
                    setGraphic(g);
                }
                else {
                    setGraphic(null);
                }
            }
        });
        return col;
    }

    @FXML
    public void closeNewMech() {
        onClose.run();
    }
}
