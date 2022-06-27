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
package org.lisoft.lsml.view_fx.controllers.loadoutwindow;

import javafx.application.Platform;
import javafx.beans.binding.DoubleBinding;
import javafx.beans.binding.ListBinding;
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
import javafx.scene.chart.XYChart.Series;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TitledPane;
import javafx.scene.control.cell.CheckBoxTableCell;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import org.lisoft.lsml.messages.*;
import org.lisoft.lsml.messages.LoadoutMessage.Type;
import org.lisoft.lsml.model.graphs.AlphaStrikeGraphModel;
import org.lisoft.lsml.model.graphs.DamageGraphModel;
import org.lisoft.lsml.model.graphs.MaxDpsGraphModel;
import org.lisoft.lsml.model.graphs.SustainedDpsGraphModel;
import org.lisoft.lsml.model.item.Weapon;
import org.lisoft.lsml.model.loadout.Loadout;
import org.lisoft.lsml.model.loadout.WeaponGroups;
import org.lisoft.lsml.util.Pair;
import org.lisoft.lsml.view_fx.controllers.AbstractFXController;
import org.lisoft.lsml.view_fx.controls.FixedRowsTableView;
import org.lisoft.lsml.view_fx.properties.LoadoutMetrics;
import org.lisoft.lsml.view_fx.style.StyleManager;
import org.lisoft.lsml.view_fx.util.FxControlUtils;
import org.lisoft.lsml.view_fx.util.FxTableUtils;

import javax.inject.Inject;
import javax.inject.Named;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map.Entry;
import java.util.SortedMap;

/**
 * A control that displays stats for a weapon group.
 *
 * @author Li Song
 */
public class WeaponLabPaneController extends AbstractFXController implements MessageReceiver {
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
    private static final double MINIMUM_Y_AXIS_UPPER_BOUND = 1.0;
    private final ListBinding<Series<Double, Double>> alphaStrikeData;
    private final AlphaStrikeGraphModel graphModelAlpha;
    private final MaxDpsGraphModel graphModelMaxDPS;
    private final SustainedDpsGraphModel graphModelSustained;
    private final Loadout loadout;
    private final ListBinding<Series<Double, Double>> maxDpsData;
    private final ListBinding<Series<Double, Double>> sustainedDpsData;
    private final List<TitledPane> wpnGroupPanes = new ArrayList<>();
    private final MessageXBar xBar;
    @FXML
    private StackedAreaChart<Double, Double> graphAlphaStrike;
    @FXML
    private StackedAreaChart<Double, Double> graphMaxDPS;
    @FXML
    private StackedAreaChart<Double, Double> graphSustainedDPS;
    @FXML
    private VBox leftColumn;
    @FXML
    private FixedRowsTableView<WeaponState> weaponGroupTable;

    @Inject
    public WeaponLabPaneController(@Named("local") MessageXBar aXBar, Loadout aLoadout, LoadoutMetrics aMetrics) {
        loadout = aLoadout;
        xBar = aXBar;
        xBar.attach(this);
        graphModelAlpha = new AlphaStrikeGraphModel(aMetrics.alphaGroup.alphaDamage.getMetric(), loadout);
        graphModelSustained = new SustainedDpsGraphModel(aMetrics.alphaGroup.sustainedDPS.getMetric(), loadout);
        graphModelMaxDPS = new MaxDpsGraphModel(loadout);

        for (int i = 0; i < WeaponGroups.MAX_GROUPS; ++i) {
            // FIXME: Factory or injection
            final Region weaponGroupStats = new WeaponGroupStatsController(aMetrics.weaponGroups[i],
                                                                           aMetrics).getView();
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

        alphaStrikeData = setupGraph(graphAlphaStrike, graphModelAlpha);
        sustainedDpsData = setupGraph(graphSustainedDPS, graphModelSustained);
        maxDpsData = setupGraph(graphMaxDPS, graphModelMaxDPS);

        graphAlphaStrike.setLegendSide(Side.TOP);
        graphSustainedDPS.setLegendVisible(false);
        graphMaxDPS.setLegendVisible(false);
        update();
    }

    @FXML
    public void closeWeaponLab() {
        graphAlphaStrike.dataProperty().unbind();
        graphSustainedDPS.dataProperty().unbind();
        graphMaxDPS.dataProperty().unbind();
        xBar.post(new ApplicationMessage(ApplicationMessage.Type.CLOSE_OVERLAY, root));
    }

    @FXML
    public void keyRelease(KeyEvent aEvent) {
        FxControlUtils.escapeWindow(aEvent, root, () -> closeWeaponLab());
    }

    public void open() {
        // We keep the data series unbound while the overlay is closed, this avoids
        // unnecessary re-computation of the graphs for every added item even though
        // the graphs are not shown.
        graphAlphaStrike.dataProperty().bind(alphaStrikeData);
        graphSustainedDPS.dataProperty().bind(sustainedDpsData);
        graphMaxDPS.dataProperty().bind(maxDpsData);
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
        } else if (aMsg instanceof LoadoutMessage) {
            final LoadoutMessage loadoutMessage = (LoadoutMessage) aMsg;
            if (loadoutMessage.type == Type.WEAPON_GROUPS_CHANGED) {
                updateGroups();
            }
        }
    }

    private ListBinding<Series<Double, Double>> setupGraph(StackedAreaChart<Double, Double> aChart,
                                                           DamageGraphModel aModel) {
        aChart.setTitle(aModel.getTitle());
        aChart.getXAxis().setLabel(aModel.getXAxisLabel());
        aChart.getYAxis().setLabel(aModel.getYAxisLabel());
        aChart.getXAxis().setAutoRanging(false);
        aChart.getYAxis().setAutoRanging(false);
        aChart.setCreateSymbols(false);

        final ListBinding<Series<Double, Double>> dataBinding = new ListBinding<Series<Double, Double>>() {
            @Override
            protected ObservableList<Series<Double, Double>> computeValue() {
                final ObservableList<Series<Double, Double>> ans = FXCollections.observableArrayList();
                final SortedMap<Weapon, List<Pair<Double, Double>>> data = aModel.getData();
                for (final Entry<Weapon, List<Pair<Double, Double>>> entry : data.entrySet()) {
                    final XYChart.Series<Double, Double> series = new XYChart.Series<>();
                    series.setName(entry.getKey().getName());
                    final ObservableList<Data<Double, Double>> seriesData = series.getData();
                    for (final Pair<Double, Double> point : entry.getValue()) {
                        seriesData.add(new XYChart.Data<>(point.first, point.second));
                    }
                    ans.add(series);
                }
                return ans;
            }
        };

        final Axis<? extends Number> xAxisRaw = aChart.getXAxis();
        final Axis<? extends Number> yAxisRaw = aChart.getYAxis();
        final NumberAxis xAxis = (NumberAxis) xAxisRaw;
        final NumberAxis yAxis = (NumberAxis) yAxisRaw;

        xAxis.setLowerBound(1.0);
        yAxis.setLowerBound(0.0);

        final double xStep = 200;
        final double yStep = 2.5;
        xAxis.setTickUnit(xStep);
        yAxis.setTickUnit(yStep);

        xAxis.upperBoundProperty().bind(new DoubleBinding() {
            {
                bind(dataBinding);
            }

            @Override
            protected double computeValue() {
                double maxX = 0.0;
                for (final Series<Double, Double> series : dataBinding.get()) {
                    for (final Data<Double, Double> point : series.getData()) {
                        maxX = Math.max(maxX, point.getXValue());
                    }
                }
                return Math.ceil(maxX / xStep) * xStep;
            }
        });

        yAxis.upperBoundProperty().bind(new DoubleBinding() {
            {
                bind(dataBinding);
            }

            @Override
            protected double computeValue() {
                double maxY = 0.0;
                for (final Series<Double, Double> series : dataBinding.get()) {
                    maxY += series.getData().stream().map(x -> x.getYValue()).max(Comparator.naturalOrder())
                                  .orElse(0.0);
                }
                return Math.max(MINIMUM_Y_AXIS_UPPER_BOUND, Math.ceil(maxY / yStep) * yStep);
            }
        });
        return dataBinding;
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

    private void updateGraphs() {
        sustainedDpsData.invalidate();
        maxDpsData.invalidate();
        alphaStrikeData.invalidate();
    }

    private void updateGroups() {
        for (int group = 0; group < WeaponGroups.MAX_GROUPS; ++group) {
            final boolean empty = loadout.getWeaponGroups().getWeapons(group, loadout).isEmpty();
            wpnGroupPanes.get(group).setDisable(empty);
            wpnGroupPanes.get(group).setExpanded(!empty);
        }
    }
}
