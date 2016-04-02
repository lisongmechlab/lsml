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
package org.lisoft.lsml.view_fx.loadout;

import static org.lisoft.lsml.view_fx.util.FxmlHelpers.addPropertyColumn;
import static org.lisoft.lsml.view_fx.util.FxmlHelpers.loadFxmlControl;
import static org.lisoft.lsml.view_fx.util.FxmlHelpers.resizeTableToFit;

import java.util.ArrayList;
import java.util.List;
import java.util.Map.Entry;
import java.util.SortedMap;

import org.lisoft.lsml.messages.ItemMessage;
import org.lisoft.lsml.messages.LoadoutMessage;
import org.lisoft.lsml.messages.LoadoutMessage.Type;
import org.lisoft.lsml.messages.Message;
import org.lisoft.lsml.messages.MessageReceiver;
import org.lisoft.lsml.messages.MessageXBar;
import org.lisoft.lsml.model.graphs.AlphaStrikeGraphModel;
import org.lisoft.lsml.model.graphs.DamageGraphModel;
import org.lisoft.lsml.model.graphs.MaxDpsGraphModel;
import org.lisoft.lsml.model.graphs.SustainedDpsGraphModel;
import org.lisoft.lsml.model.item.Weapon;
import org.lisoft.lsml.model.loadout.Loadout;
import org.lisoft.lsml.model.loadout.LoadoutMetrics;
import org.lisoft.lsml.model.loadout.WeaponGroups;
import org.lisoft.lsml.util.Pair;
import org.lisoft.lsml.view_fx.controls.FixedRowsTableView;
import org.lisoft.lsml.view_fx.properties.LoadoutMetricsModelAdaptor;

import javafx.application.Platform;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.geometry.Side;
import javafx.scene.chart.Axis;
import javafx.scene.chart.NumberAxis;
import javafx.scene.chart.StackedAreaChart;
import javafx.scene.chart.XYChart;
import javafx.scene.chart.XYChart.Data;
import javafx.scene.control.TableColumn;
import javafx.scene.control.cell.CheckBoxTableCell;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.VBox;

/**
 * A control that displays stats for a weapon group.
 * 
 * @author Li Song
 */
public class WeaponLabPane extends BorderPane implements MessageReceiver {

    static public class WeaponState {
        private final BooleanProperty[] groupState;
        private final Weapon            weapon;

        public WeaponState(Weapon aWeapon, int aWeaponIndex, Loadout aLoadout, MessageXBar aXBar) {
            WeaponGroups weaponGroups = aLoadout.getWeaponGroups();
            weapon = aWeapon;
            groupState = new SimpleBooleanProperty[WeaponGroups.MAX_GROUPS];
            for (int i = 0; i < WeaponGroups.MAX_GROUPS; ++i) {
                final int group = i;
                groupState[i] = new SimpleBooleanProperty(weaponGroups.isInGroup(group, aWeaponIndex));
                groupState[i].addListener((aObservable, aOld, aNew) -> {
                    weaponGroups.setGroup(group, aWeaponIndex, aNew.booleanValue());
                    aXBar.post(new LoadoutMessage(aLoadout, LoadoutMessage.Type.WEAPON_GROUPS_CHANGED));
                });
            }
        }

        /**
         * @return the weapon
         */
        public Weapon getWeapon() {
            return weapon;
        }
    }

    @FXML
    private FixedRowsTableView<WeaponState>  weaponGroupTable;
    @FXML
    private VBox                             leftColumn;
    private final MessageXBar                xBar;
    private final Loadout                    loadout;
    private final List<WeaponGroupPane>      wpnGroupPanes = new ArrayList<>();
    @FXML
    private StackedAreaChart<Double, Double> graphAlphaStrike;
    @FXML
    private StackedAreaChart<Double, Double> graphSustainedDPS;
    @FXML
    private StackedAreaChart<Double, Double> graphMaxDPS;
    private LoadoutMetrics                   metrics;
    private AlphaStrikeGraphModel            graphModelAlpha;
    private SustainedDpsGraphModel           graphModelSustained;
    private MaxDpsGraphModel                 graphModelMaxDPS;
    private Runnable                         closeCallback;

    @FXML
    public void closeWeaponLab() {
        closeCallback.run();
    }

    public WeaponLabPane(MessageXBar aXBar, Loadout aLoadout, LoadoutMetricsModelAdaptor aMetrics,
            Runnable aCloseCallback) {
        loadFxmlControl(this);
        aXBar.attach(this);

        metrics = aMetrics.metrics;
        closeCallback = aCloseCallback;
        loadout = aLoadout;
        xBar = aXBar;
        graphModelAlpha = new AlphaStrikeGraphModel(metrics, loadout);
        graphModelSustained = new SustainedDpsGraphModel(metrics, loadout);
        graphModelMaxDPS = new MaxDpsGraphModel(loadout);

        for (int i = 0; i < WeaponGroups.MAX_GROUPS; ++i) {
            WeaponGroupPane weaponGroupPane = new WeaponGroupPane(aMetrics, i);
            leftColumn.getChildren().add(weaponGroupPane);
            wpnGroupPanes.add(weaponGroupPane);
        }

        weaponGroupTable.setColumnResizePolicy((param) -> true);
        weaponGroupTable.setVisibleRows(5);
        weaponGroupTable.getColumns().clear();
        addPropertyColumn(weaponGroupTable, "Weapon", "weapon");

        for (int i = 0; i < WeaponGroups.MAX_GROUPS; ++i) {
            final int group = i;
            TableColumn<WeaponState, Boolean> col = new TableColumn<>(Integer.toString(group + 1));
            col.setCellValueFactory(aFeature -> aFeature.getValue().groupState[group]);
            col.setCellFactory(CheckBoxTableCell.forTableColumn(col));
            col.setEditable(true);
            weaponGroupTable.getColumns().add(col);
        }
        weaponGroupTable.setEditable(true);
        Platform.runLater(() -> resizeTableToFit(weaponGroupTable));

        graphAlphaStrike.setLegendSide(Side.TOP);
        graphSustainedDPS.setLegendVisible(false);
        graphMaxDPS.setLegendVisible(false);
        update();
        updateGraphs();
    }

    private void updateGraphs() {
        updateGraph(graphAlphaStrike, graphModelAlpha);
        updateGraph(graphSustainedDPS, graphModelSustained);
        updateGraph(graphMaxDPS, graphModelMaxDPS);
    }

    @Override
    public void receive(Message aMsg) {
        if (aMsg.affectsHeatOrDamage()) {
            updateGraphs();
        }

        if (aMsg instanceof ItemMessage) {
            ItemMessage itemMessage = (ItemMessage) aMsg;
            if (itemMessage.item instanceof Weapon) {
                update();
            }
        }
        else if (aMsg instanceof LoadoutMessage) {
            LoadoutMessage loadoutMessage = (LoadoutMessage) aMsg;
            if (loadoutMessage.type == Type.WEAPON_GROUPS_CHANGED) {
                updateGroups();
            }
        }
    }

    private void update() {
        ObservableList<WeaponState> states = FXCollections.observableArrayList();
        List<Weapon> weapons = loadout.getWeaponGroups().getWeaponOrder(loadout);
        for (int weapon = 0; weapon < weapons.size(); ++weapon) {
            states.add(new WeaponState(weapons.get(weapon), weapon, loadout, xBar));
        }
        weaponGroupTable.setItems(states);
        weaponGroupTable.setVisibleRows(weapons.size() + 1);
        updateGroups();
    }

    private void updateGroups() {
        for (int group = 0; group < WeaponGroups.MAX_GROUPS; ++group) {
            boolean empty = loadout.getWeaponGroups().getWeapons(group, loadout).isEmpty();
            wpnGroupPanes.get(group).setDisable(empty);
            wpnGroupPanes.get(group).setExpanded(!empty);
        }
    }

    private void updateGraph(StackedAreaChart<Double, Double> aChart, DamageGraphModel aModel) {
        aChart.setTitle(aModel.getTitle());
        double maxX = 0;
        double maxY = 0;
        aChart.getXAxis().setLabel(aModel.getXAxisLabel());
        aChart.getYAxis().setLabel(aModel.getYAxisLabel());
        aChart.getXAxis().setAutoRanging(false);
        aChart.getYAxis().setAutoRanging(false);
        aChart.getData().clear();
        aChart.setCreateSymbols(false);
        SortedMap<Weapon, List<Pair<Double, Double>>> data = aModel.getData();
        for (Entry<Weapon, List<Pair<Double, Double>>> entry : data.entrySet()) {
            XYChart.Series<Double, Double> series = new XYChart.Series<>();
            series.setName(entry.getKey().getName());
            ObservableList<Data<Double, Double>> seriesData = series.getData();
            double maxYLocal = 0;
            for (Pair<Double, Double> point : entry.getValue()) {
                seriesData.add(new XYChart.Data<>(point.first, point.second));
                maxX = Math.max(maxX, point.first);
                maxYLocal = Math.max(maxYLocal, point.second);
            }
            maxY += maxYLocal;
            aChart.getData().add(series);
        }

        double xStep = 200;
        double yStep = 2.5;
        setBounds(aChart.getXAxis(), (int) ((maxX + xStep) / xStep) * xStep, xStep);
        setBounds(aChart.getYAxis(), (int) ((maxY + yStep) / yStep) * yStep, yStep);
    }

    private void setBounds(@SuppressWarnings("rawtypes") Axis aAxis, double aBound, double aTick) {
        NumberAxis numberAxis = (NumberAxis) aAxis;
        numberAxis.setLowerBound(0.0);
        numberAxis.setUpperBound(aBound);
        numberAxis.setTickUnit(aTick);
    }
}
