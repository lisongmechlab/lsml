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
package org.lisoft.lsml.view_fx.loadout;

import static org.lisoft.lsml.view_fx.util.FxControlUtils.loadFxmlControl;

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
import org.lisoft.lsml.model.loadout.WeaponGroups;
import org.lisoft.lsml.util.Pair;
import org.lisoft.lsml.view_fx.controls.FixedRowsTableView;
import org.lisoft.lsml.view_fx.properties.LoadoutMetrics;
import org.lisoft.lsml.view_fx.style.StyleManager;
import org.lisoft.lsml.view_fx.util.FxControlUtils;
import org.lisoft.lsml.view_fx.util.FxGraphUtils;
import org.lisoft.lsml.view_fx.util.FxTableUtils;

import javafx.application.Platform;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.geometry.Side;
import javafx.scene.chart.StackedAreaChart;
import javafx.scene.chart.XYChart;
import javafx.scene.chart.XYChart.Data;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TitledPane;
import javafx.scene.control.cell.CheckBoxTableCell;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.VBox;

/**
 * A control that displays stats for a weapon group.
 *
 * @author Emily Björk
 */
public class WeaponLabPane extends BorderPane implements MessageReceiver {

    static public class WeaponState {
        private final BooleanProperty[] groupState;
        private final Weapon weapon;

        public WeaponState(Weapon aWeapon, int aWeaponIndex, Loadout aLoadout, MessageXBar aXBar) {
            final WeaponGroups weaponGroups = aLoadout.getWeaponGroups();
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
    private FixedRowsTableView<WeaponState> weaponGroupTable;
    @FXML
    private VBox leftColumn;
    private final MessageXBar xBar;
    private final Loadout loadout;
    private final List<TitledPane> wpnGroupPanes = new ArrayList<>();
    @FXML
    private StackedAreaChart<Double, Double> graphAlphaStrike;
    @FXML
    private StackedAreaChart<Double, Double> graphSustainedDPS;
    @FXML
    private StackedAreaChart<Double, Double> graphMaxDPS;
    private final AlphaStrikeGraphModel graphModelAlpha;
    private final SustainedDpsGraphModel graphModelSustained;
    private final MaxDpsGraphModel graphModelMaxDPS;
    private final Runnable closeCallback;

    public WeaponLabPane(MessageXBar aXBar, Loadout aLoadout, LoadoutMetrics aMetrics, Runnable aCloseCallback) {
        loadFxmlControl(this);
        aXBar.attach(this);

        closeCallback = aCloseCallback;
        loadout = aLoadout;
        xBar = aXBar;
        graphModelAlpha = new AlphaStrikeGraphModel(aMetrics.alphaGroup.alphaDamage.getMetric(), loadout);
        graphModelSustained = new SustainedDpsGraphModel(aMetrics.alphaGroup.sustainedDPS.getMetric(), loadout);
        graphModelMaxDPS = new MaxDpsGraphModel(loadout);

        for (int i = 0; i < WeaponGroups.MAX_GROUPS; ++i) {
            final WeaponGroupStats weaponGroupStats = new WeaponGroupStats(aMetrics.weaponGroups[i], aMetrics);
            StyleManager.addClass(weaponGroupStats, StyleManager.CLASS_DEFAULT_PADDING);
            final TitledPane titledPane = new TitledPane("Group " + (i + 1), weaponGroupStats);
            leftColumn.getChildren().add(titledPane);
            wpnGroupPanes.add(titledPane);
        }

        weaponGroupTable.setColumnResizePolicy((param) -> true);
        weaponGroupTable.setVisibleRows(5);
        weaponGroupTable.getColumns().clear();
        FxTableUtils.addPropertyColumn(weaponGroupTable, "Weapon", "weapon", "The name of the weapon system.");

        for (int i = 0; i < WeaponGroups.MAX_GROUPS; ++i) {
            final int group = i;
            final TableColumn<WeaponState, Boolean> col = new TableColumn<>(Integer.toString(group + 1));
            col.setCellValueFactory(aFeature -> aFeature.getValue().groupState[group]);
            col.setCellFactory(CheckBoxTableCell.forTableColumn(col));
            col.setEditable(true);
            weaponGroupTable.getColumns().add(col);
        }
        weaponGroupTable.setEditable(true);
        Platform.runLater(() -> FxTableUtils.resizeColumnsToFit(weaponGroupTable));

        graphAlphaStrike.setLegendSide(Side.TOP);
        graphSustainedDPS.setLegendVisible(false);
        graphMaxDPS.setLegendVisible(false);
        update();
        updateGraphs();
    }

    @FXML
    public void closeWeaponLab() {
        closeCallback.run();
    }

    @FXML
    public void keyRelease(KeyEvent aEvent) {
        FxControlUtils.escapeWindow(aEvent, this, () -> closeWeaponLab());
    }

    @Override
    public void receive(Message aMsg) {
        if (aMsg.affectsHeatOrDamage()) {
            updateGraphs();
        }

        if (aMsg instanceof ItemMessage) {
            final ItemMessage itemMessage = (ItemMessage) aMsg;
            if (itemMessage.item instanceof Weapon) {
                update();
            }
        }
        else if (aMsg instanceof LoadoutMessage) {
            final LoadoutMessage loadoutMessage = (LoadoutMessage) aMsg;
            if (loadoutMessage.type == Type.WEAPON_GROUPS_CHANGED) {
                updateGroups();
            }
        }
    }

    private void update() {
        final ObservableList<WeaponState> states = FXCollections.observableArrayList();
        final List<Weapon> weapons = loadout.getWeaponGroups().getWeaponOrder(loadout);
        for (int weapon = 0; weapon < weapons.size(); ++weapon) {
            states.add(new WeaponState(weapons.get(weapon), weapon, loadout, xBar));
        }
        weaponGroupTable.setItems(states);
        weaponGroupTable.setVisibleRows(weapons.size() + 1);
        updateGroups();
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
        final SortedMap<Weapon, List<Pair<Double, Double>>> data = aModel.getData();
        for (final Entry<Weapon, List<Pair<Double, Double>>> entry : data.entrySet()) {
            final XYChart.Series<Double, Double> series = new XYChart.Series<>();
            series.setName(entry.getKey().getName());
            final ObservableList<Data<Double, Double>> seriesData = series.getData();
            double maxYLocal = 0;
            for (final Pair<Double, Double> point : entry.getValue()) {
                seriesData.add(new XYChart.Data<>(point.first, point.second));
                maxX = Math.max(maxX, point.first);
                maxYLocal = Math.max(maxYLocal, point.second);
            }
            maxY += maxYLocal;
            aChart.getData().add(series);
        }

        final double xStep = 200;
        final double yStep = 2.5;
        FxGraphUtils.setAxisBound(aChart.getXAxis(), 0, maxX, xStep);
        FxGraphUtils.setAxisBound(aChart.getYAxis(), 0, maxY, yStep);
    }

    private void updateGraphs() {
        updateGraph(graphAlphaStrike, graphModelAlpha);
        updateGraph(graphSustainedDPS, graphModelSustained);
        updateGraph(graphMaxDPS, graphModelMaxDPS);
    }

    private void updateGroups() {
        for (int group = 0; group < WeaponGroups.MAX_GROUPS; ++group) {
            final boolean empty = loadout.getWeaponGroups().getWeapons(group, loadout).isEmpty();
            wpnGroupPanes.get(group).setDisable(empty);
            wpnGroupPanes.get(group).setExpanded(!empty);
        }
    }
}
