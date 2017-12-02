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

import static javafx.beans.binding.Bindings.max;
import static javafx.beans.binding.Bindings.size;
import static javafx.beans.binding.Bindings.when;
import static org.lisoft.lsml.view_fx.util.FxBindingUtils.format;

import java.text.DecimalFormat;
import java.util.Collection;
import java.util.function.Predicate;
import java.util.regex.Pattern;

import javax.inject.Inject;
import javax.inject.Named;

import org.lisoft.lsml.messages.ItemMessage;
import org.lisoft.lsml.messages.LoadoutMessage;
import org.lisoft.lsml.messages.Message;
import org.lisoft.lsml.messages.MessageReceiver;
import org.lisoft.lsml.messages.MessageReception;
import org.lisoft.lsml.messages.MessageXBar;
import org.lisoft.lsml.messages.OmniPodMessage;
import org.lisoft.lsml.messages.PilotSkillMessage;
import org.lisoft.lsml.model.database.EnvironmentDB;
import org.lisoft.lsml.model.environment.Environment;
import org.lisoft.lsml.model.metrics.RangeMetric;
import org.lisoft.lsml.model.metrics.RangeTimeMetric;
import org.lisoft.lsml.model.modifiers.Modifier;
import org.lisoft.lsml.model.modifiers.ModifierDescription;
import org.lisoft.lsml.view_fx.SensibleTableColumnResizePolicy;
import org.lisoft.lsml.view_fx.Settings;
import org.lisoft.lsml.view_fx.controllers.AbstractFXController;
import org.lisoft.lsml.view_fx.controls.FixedRowsTableView;
import org.lisoft.lsml.view_fx.controls.WeaponSummaryList;
import org.lisoft.lsml.view_fx.properties.LoadoutMetrics;
import org.lisoft.lsml.view_fx.properties.LoadoutMetrics.GroupMetrics;
import org.lisoft.lsml.view_fx.properties.LoadoutModelAdaptor;
import org.lisoft.lsml.view_fx.properties.RangeMetricBinding;
import org.lisoft.lsml.view_fx.properties.RangeTimeMetricBinding;
import org.lisoft.lsml.view_fx.style.PredicatedModifierFormatter;
import org.lisoft.lsml.view_fx.util.BetterTextFormatter;
import org.lisoft.lsml.view_fx.util.FxControlUtils;
import org.lisoft.lsml.view_fx.util.FxTableUtils;
import org.lisoft.lsml.view_fx.util.RegexStringConverter;
import org.lisoft.lsml.view_fx.util.WeaponSummary;

import javafx.beans.binding.BooleanBinding;
import javafx.beans.binding.DoubleBinding;
import javafx.beans.binding.NumberBinding;
import javafx.beans.binding.ObjectBinding;
import javafx.beans.binding.StringBinding;
import javafx.beans.property.BooleanProperty;
import javafx.beans.value.ObservableNumberValue;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.control.CheckMenuItem;
import javafx.scene.control.ComboBox;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TextFormatter;
import javafx.scene.control.Toggle;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import javafx.scene.shape.Arc;
import javafx.scene.shape.Line;

/**
 * This control shows all the stats for a loadout in one convenient place.
 *
 * @author Li Song
 */
public class LoadoutInfoPaneController extends AbstractFXController implements MessageReceiver {
    /**
     *
     */
    private static final Pattern RANGE_PATTERN = Pattern.compile("\\s*(-?\\d*(?:\\.\\d*)?)\\s*m?");

    private static final String TEXT_FILL_BAD = "-fx-text-fill: quirk-bad;";

    private static final String WSTAT_COL_AMMO = "Rnds";
    private static final String WSTAT_COL_DAMAGE = "Dmg";
    private static final String WSTAT_COL_WEAPON = "Wpn";
    private static final String WSTAT_COL_SECONDS = "Time";
    private static final String WSTAT_COL_VOLLEYS = "Vlys";
    @FXML
    private VBox alphaContentData;
    @FXML
    private Label alphaDamage;
    @FXML
    private Label alphaHeat;
    @FXML
    private ComboBox<String> alphaRange;
    @FXML
    private Label alphaSummary;
    @FXML
    private VBox burstContentData;
    @FXML
    private Label burstDamage;
    @FXML
    private Label burstHeat;
    @FXML
    private ComboBox<String> burstRange;
    @FXML
    private Label burstSummary;
    @FXML
    private ComboBox<String> burstTime;
    @FXML
    private VBox dpsContentData;
    @FXML
    private Label dpsMax;
    @FXML
    private ComboBox<String> dpsRange;
    @FXML
    private Label dpsSummary;
    @FXML
    private Label dpsSustained;
    @FXML
    private Label heatCapacity;
    @FXML
    private VBox heatContentData;
    @FXML
    private ComboBox<Environment> heatEnvironment;
    @FXML
    private Label heatRatio;
    @FXML
    private Label heatSinks;
    @FXML
    private Label heatSummary;
    private final LoadoutMetrics metrics;
    @FXML
    private Line mobilityArcPitchArrow;
    @FXML
    private Arc mobilityArcPitchInner;
    @FXML
    private Arc mobilityArcPitchOuter;
    @FXML
    private Line mobilityArcYawArrow;
    @FXML
    private Arc mobilityArcYawInner;
    @FXML
    private Arc mobilityArcYawOuter;
    @FXML
    private Label mobilityArmPitchSpeed;
    @FXML
    private Label mobilityArmYawSpeed;
    @FXML
    private VBox mobilityContentData;
    @FXML
    private Label mobilityJumpJets;
    @FXML
    private Label mobilitySpeed;
    @FXML
    private Label mobilitySummary;

    @FXML
    private Label mobilityTorsoPitchSpeed;

    @FXML
    private Label mobilityTorsoYawSpeed;

    @FXML
    private Label mobilityTurnSpeed;

    @FXML
    private Label offenseSummary;

    @FXML
    private FixedRowsTableView<WeaponSummary> offensiveWeaponTable;

    @FXML
    private VBox quirksBox;

    @FXML
    private VBox modifiersBox;

    private final PredicatedModifierFormatter modifierFormatter = new PredicatedModifierFormatter(x -> true);

    private final LoadoutModelAdaptor model;

    @FXML
    private VBox heatContentLabel;

    @FXML
    private VBox alphaContentLabel;

    @FXML
    private VBox dpsContentLabel;

    @FXML
    private VBox burstContentLabel;

    @FXML
    private VBox mobilityContentLabel;

    @Inject
    public LoadoutInfoPaneController(Settings aSettings, @Named("local") MessageXBar aXBar, LoadoutModelAdaptor aModel,
            LoadoutMetrics aMetrics) {
        aXBar.attach(this);
        metrics = aMetrics;
        model = aModel;
        BooleanProperty.booleanProperty(aSettings.getBoolean(Settings.UI_COMPACT_LAYOUT));

        setupMobilityPanel();
        setupHeatPanel();
        setupOffensivePanels();
        setupArmamentsPanel(aXBar);
        setupQuirkModifierBoxes(aSettings);

        final Region view = getView();
        view.parentProperty().addListener((aObs, aOld, aNew) -> {
            if (aNew != null) {
                final Region parent = (Region) aNew;
                offensiveWeaponTable.maxWidthProperty().bind(parent.widthProperty());
            }
        });

        // offensiveWeaponTable.setMaxWidth(100);
    }

    @Override
    public void receive(Message aMsg) {
        final boolean efficiencies = aMsg instanceof PilotSkillMessage;
        final boolean items = aMsg instanceof ItemMessage;
        final boolean modules = aMsg instanceof LoadoutMessage
                && ((LoadoutMessage) aMsg).type == LoadoutMessage.Type.MODULES_CHANGED;
        final boolean omniPods = aMsg instanceof OmniPodMessage;
        if (efficiencies || items || omniPods || modules) {
            updateModifiers();
        }

    }

    @FXML
    public void toggleAlphaDisplay(ActionEvent aEvent) {
        togglePane(aEvent, alphaSummary, alphaContentLabel, alphaContentData);
    }

    @FXML
    public void toggleBurstDisplay(ActionEvent aEvent) {
        togglePane(aEvent, burstSummary, burstContentLabel, burstContentData);
    }

    @FXML
    public void toggleDpsDisplay(ActionEvent aEvent) {
        togglePane(aEvent, dpsSummary, dpsContentLabel, dpsContentData);
    }

    @FXML
    public void toggleHeatDisplay(ActionEvent aEvent) {
        togglePane(aEvent, heatSummary, heatContentLabel, heatContentData);
    }

    @FXML
    public void toggleMobilityDisplay(ActionEvent aEvent) {
        togglePane(aEvent, mobilitySummary, mobilityContentLabel, mobilityContentData);
    }

    @FXML
    public void toggleOffenseDisplay(ActionEvent aEvent) {
        togglePane(aEvent, offenseSummary, offensiveWeaponTable, null);
    }

    private void formatComboBox(ComboBox<?> aComboBox) {
        FxControlUtils.resizeComboBoxToContent(aComboBox);
        FxControlUtils.fixComboBox(aComboBox);
    }

    private final void formatLabel(Label aLabel, String aFormat, ObservableNumberValue... aNumbers) {
        aLabel.textProperty().bind(format(aFormat, aNumbers));
    }

    private void setupArmamentsPanel(MessageReception aXBar) {
        offenseSummary.setText(""); // TODO: Generate a useful text here

        offensiveWeaponTable.setItems(new WeaponSummaryList(aXBar, model.loadout));
        offensiveWeaponTable.setVisibleRows(4);
        final NumberBinding numItems = max(size(offensiveWeaponTable.getItems()), 1);
        offensiveWeaponTable.visibleRowsProperty().bind(numItems);

        offensiveWeaponTable.setColumnResizePolicy(new SensibleTableColumnResizePolicy());

        final DecimalFormat df = new DecimalFormat("#");
        final double nameSize = 0.35;
        final double margin = 0.02;

        final TableColumn<WeaponSummary, String> nameColumn = FxTableUtils.makeAttributeColumn(WSTAT_COL_WEAPON, "name",
                "The name of the weapon system. Missile launchers that share ammo type are grouped together.");
        nameColumn.prefWidthProperty().bind(offensiveWeaponTable.widthProperty().multiply(nameSize - margin));

        final TableColumn<WeaponSummary, String> ammoColumn = new TableColumn<>(WSTAT_COL_AMMO);
        ammoColumn.setCellValueFactory((aFeatures) -> {
            final WeaponSummary value = aFeatures.getValue();
            return new StringBinding() {

                {
                    bind(value.roundsProperty());
                }

                @Override
                protected String computeValue() {
                    return df.format(value.roundsProperty().get());
                }
            };
        });
        ammoColumn.prefWidthProperty().bind(offensiveWeaponTable.widthProperty().multiply((1 - nameSize) / 4));
        FxTableUtils.addColumnToolTip(ammoColumn, "The total amount of ammo for this weapon type.");

        final TableColumn<WeaponSummary, String> volleysColumn = new TableColumn<>(WSTAT_COL_VOLLEYS);
        volleysColumn.setCellValueFactory((aFeatures) -> {
            final WeaponSummary value = aFeatures.getValue();
            return new StringBinding() {
                {
                    bind(value.roundsProperty());
                    bind(value.volleySizeProperty());
                }

                @Override
                protected String computeValue() {
                    final int volleySize = value.volleySizeProperty().get();
                    if (volleySize == 0) {
                        return "0";
                    }
                    return df.format(value.roundsProperty().get() / volleySize);
                }
            };
        });
        volleysColumn.prefWidthProperty().bind(offensiveWeaponTable.widthProperty().multiply((1 - nameSize) / 4));
        FxTableUtils.addColumnToolTip(volleysColumn,
                "The number of full volleys/alpha strikes you can do with this weapon type before you run out of ammo.");

        final TableColumn<WeaponSummary, String> secondsColumn = new TableColumn<>(WSTAT_COL_SECONDS);
        secondsColumn.setCellValueFactory((aFeatures) -> {
            final WeaponSummary value = aFeatures.getValue();
            return new StringBinding() {
                {
                    bind(value.battleTimeProperty());
                }

                @Override
                protected String computeValue() {
                    return df.format(value.battleTimeProperty().get());
                }
            };
        });
        secondsColumn.prefWidthProperty().bind(offensiveWeaponTable.widthProperty().multiply((1 - nameSize) / 4));
        FxTableUtils.addColumnToolTip(secondsColumn,
                "The amount of time that you can continuously alpha with this weapon type before you run out of ammo.");

        final TableColumn<WeaponSummary, String> damageColumn = new TableColumn<>(WSTAT_COL_DAMAGE);
        damageColumn.setCellValueFactory((aFeatures) -> {
            final WeaponSummary value = aFeatures.getValue();
            return new StringBinding() {
                {
                    bind(value.totalDamageProperty());
                }

                @Override
                protected String computeValue() {
                    return df.format(value.totalDamageProperty().get());
                }
            };
        });
        damageColumn.prefWidthProperty().bind(offensiveWeaponTable.widthProperty().multiply((1 - nameSize) / 4));
        FxTableUtils.addColumnToolTip(damageColumn,
                "The maximal damage potential with this weapon type and the amount of ammo carried.");

        final ObservableList<TableColumn<WeaponSummary, ?>> cols = offensiveWeaponTable.getColumns();
        cols.clear();
        cols.add(nameColumn);
        cols.add(ammoColumn);
        cols.add(volleysColumn);
        cols.add(secondsColumn);
        cols.add(damageColumn);
    }

    private void setupHeatPanel() {
        heatEnvironment.getItems();
        for (final Environment e : EnvironmentDB.lookupAll()) {
            heatEnvironment.getItems().add(e);
        }
        heatEnvironment.valueProperty().bindBidirectional(metrics.environmentProperty);
        heatEnvironment.getSelectionModel().select(Environment.NEUTRAL);
        formatComboBox(heatEnvironment);

        final BooleanBinding sinksBelow10 = metrics.heatSinkCount.lessThan(10);
        heatSinks.styleProperty().bind(when(sinksBelow10).then(TEXT_FILL_BAD).otherwise(""));
        formatLabel(heatSinks, "% (% s)", metrics.heatSinkCount, metrics.timeToCool);
        formatLabel(heatCapacity, "%.1h", metrics.heatCapacity);
        final GroupMetrics g = metrics.alphaGroup;
        formatLabel(heatRatio, "%.1ph", g.maxDPSCoolingRatio);
        formatLabel(heatSummary, "%.1ph %.1h s", g.maxDPSCoolingRatio, metrics.timeToCool);
    }

    private void setupMobilityPanel() {
        formatLabel(mobilitySummary, "%.1h km/h", metrics.topSpeed);
        formatLabel(mobilitySpeed, "%.1h (%.1h) km/h", metrics.topSpeed, metrics.mascSpeed);
        formatLabel(mobilityTurnSpeed, "%.1h °/s", metrics.turnSpeed);
        formatLabel(mobilityTorsoPitchSpeed, "%.1h °/s", metrics.torsoPitchSpeed);
        formatLabel(mobilityTorsoYawSpeed, "%.1h °/s", metrics.torsoYawSpeed);
        formatLabel(mobilityArmPitchSpeed, "%.1h °/s", metrics.armPitchSpeed);
        formatLabel(mobilityArmYawSpeed, "%.1h °/s", metrics.armYawSpeed);
        formatLabel(mobilityJumpJets, "%/%", metrics.jumpJetCount, metrics.jumpJetMax);

        mobilityArcPitchOuter.lengthProperty().bind(metrics.torsoPitch.add(metrics.armPitch).multiply(2.0));
        mobilityArcPitchInner.lengthProperty().bind(metrics.torsoPitch.multiply(2.0));
        mobilityArcYawOuter.lengthProperty().bind(metrics.torsoYaw.add(metrics.armYaw).multiply(2.0));
        mobilityArcYawInner.lengthProperty().bind(metrics.torsoYaw.multiply(2.0));

        final DoubleBinding offset = mobilityArcPitchOuter.radiusXProperty().multiply(0.8);
        mobilityArcPitchArrow.startXProperty().bind(mobilityArcPitchOuter.centerXProperty().add(offset));
        mobilityArcPitchArrow.startYProperty().bind(mobilityArcPitchOuter.centerYProperty());
        mobilityArcPitchArrow.endXProperty()
                .bind(mobilityArcPitchOuter.centerXProperty().add(mobilityArcPitchOuter.radiusXProperty()));
        mobilityArcPitchArrow.endYProperty().bind(mobilityArcPitchOuter.centerYProperty());
        mobilityArcYawArrow.startXProperty().bind(mobilityArcYawOuter.centerXProperty());
        mobilityArcYawArrow.startYProperty().bind(mobilityArcYawOuter.centerYProperty().subtract(offset));
        mobilityArcYawArrow.endXProperty().bind(mobilityArcYawOuter.centerXProperty());
        mobilityArcYawArrow.endYProperty()
                .bind(mobilityArcYawOuter.centerYProperty().subtract(mobilityArcYawOuter.radiusYProperty()));

        mobilityArcPitchOuter.startAngleProperty().bind(mobilityArcPitchOuter.lengthProperty().negate().divide(2));
        mobilityArcPitchInner.startAngleProperty().bind(mobilityArcPitchInner.lengthProperty().negate().divide(2));
        mobilityArcYawOuter.startAngleProperty().bind(mobilityArcYawOuter.lengthProperty().divide(-2).add(90));
        mobilityArcYawInner.startAngleProperty().bind(mobilityArcYawInner.lengthProperty().divide(-2).add(90));
    }

    private void setupOffensivePanels() {
        final GroupMetrics g = metrics.alphaGroup;
        // Using alphaHeatPct here includes ghost heat where alphaHeat doesn't
        final DoubleBinding alphaTtC = g.alphaHeatPct.multiply(metrics.timeToCool);

        formatLabel(alphaSummary, "%.0h @ %.0h m (%.0hp)", g.alphaDamage, g.alphaDamage.displayRange(), g.alphaHeatPct);
        setupRangeCombobox(alphaRange, g.alphaDamage);
        formatLabel(alphaDamage, "%.1 (%.1 s)", g.alphaDamage, alphaTtC);
        formatLabel(alphaHeat, "%.0p (%.0 +%.0)", g.alphaHeatPct, g.alphaHeat, g.alphaGhostHeat);

        formatLabel(dpsSummary, "%.0 @ %.0 m (%.0 s)", g.maxDPS, g.maxDPS.displayRange(), g.maxDPSTtO);
        setupRangeCombobox(dpsRange, g.maxDPS, g.sustainedDPS);
        formatLabel(dpsMax, "%.1 @ %.0 m (%.1 s)", g.maxDPS, g.maxDPS.displayRange(), g.maxDPSTtO);
        formatLabel(dpsSustained, "%.1 @ %.0 m", g.sustainedDPS, g.sustainedDPS.displayRange());

        // This will give the heat at the end of the burst.
        final DoubleBinding heatAtEndOfBurst = g.burstHeat
                .subtract(g.burstDamage.timeProperty().multiply(metrics.heatDissipation));

        final DoubleBinding burstCoolDown = heatAtEndOfBurst.divide(metrics.heatDissipation);
        final DoubleBinding burstPct = heatAtEndOfBurst.divide(metrics.heatCapacity);

        setupRangeCombobox(burstRange, g.burstDamage);
        setupTimeCombobox(burstTime, g.burstDamage);
        formatLabel(burstDamage, "%.0", g.burstDamage);
        formatLabel(burstHeat, "%.1p (%.1 s)", burstPct, burstCoolDown);
    }

    private void setupQuirkModifierBoxes(Settings aSettings) {
        final BooleanProperty showArmorStructureQuirks = BooleanProperty
                .booleanProperty(aSettings.getBoolean(Settings.UI_SHOW_STRUCTURE_ARMOR_QUIRKS));
        showArmorStructureQuirks.addListener((aObs, aOld, aNew) -> updateModifiers());

        final Predicate<Modifier> truePredicate = aModifier -> true;
        final Predicate<Modifier> filterPredicate = aModifier -> {
            final Collection<String> selectors = aModifier.getDescription().getSelectors();
            final boolean isArmor = selectors.containsAll(ModifierDescription.SEL_ARMOUR);
            final boolean isStructure = selectors.containsAll(ModifierDescription.SEL_STRUCTURE);
            return !isArmor && !isStructure;
        };

        final ObjectBinding<Predicate<Modifier>> predicateBinding = when(showArmorStructureQuirks).then(truePredicate)
                .otherwise(filterPredicate);

        final CheckMenuItem mi = new CheckMenuItem("Show structure & armor quirks");
        mi.selectedProperty().bindBidirectional(showArmorStructureQuirks);
        final ContextMenu cm = new ContextMenu(mi);
        modifierFormatter.predicateProperty().bind(predicateBinding);
        quirksBox.setOnMousePressed(aEvent -> {
            if (aEvent.isSecondaryButtonDown()) {
                cm.show(modifiersBox, aEvent.getScreenX(), aEvent.getScreenY());
            }
            else if (aEvent.isPrimaryButtonDown()) {
                cm.hide();
            }
            aEvent.consume();
        });
        updateModifiers();
    }

    @SafeVarargs
    final private void setupRangeCombobox(ComboBox<String> aComboBox,
            RangeMetricBinding<? extends RangeMetric>... aMetrics) {
        final RegexStringConverter rangeConverter = new RegexStringConverter(RANGE_PATTERN, new DecimalFormat("# m")) {

            @Override
            public Double fromString(String aString) {
                if (aString.trim().regionMatches(true, 0, "optimal", 0, Math.min(aString.length(), 7))) {
                    return -1.0;
                }
                return super.fromString(aString);
            }

            @Override
            public String toString(Double aObject) {
                if (aObject <= 0.0) {
                    return "Optimal";
                }
                return super.toString(aObject);
            }
        };

        final TextFormatter<Double> rangeFormatter = new BetterTextFormatter<>(rangeConverter, -1.0);
        for (final RangeMetricBinding<? extends RangeMetric> metric : aMetrics) {
            metric.userRangeProperty().bind(rangeFormatter.valueProperty());
        }

        aComboBox.getItems().add("Optimal");
        aComboBox.getItems().add("90 m");
        aComboBox.getItems().add("180 m");
        aComboBox.getItems().add("270 m");
        aComboBox.getItems().add("450 m");
        aComboBox.getItems().add("720 m");
        aComboBox.getItems().add("1000 m");
        aComboBox.getEditor().setTextFormatter(rangeFormatter);
        aComboBox.getSelectionModel().select(0);

        aComboBox.getEditor().prefColumnCountProperty()
                .bind(aComboBox.getSelectionModel().selectedItemProperty().asString().length());

        formatComboBox(aComboBox);
    }

    private void setupTimeCombobox(ComboBox<String> aComboBox,
            RangeTimeMetricBinding<? extends RangeTimeMetric> aMetric) {

        final TextFormatter<Double> timeFormatter = new BetterTextFormatter<>(
                new RegexStringConverter(Pattern.compile("\\s*(-?\\d*[,.]?\\d*)\\s*s?"), new DecimalFormat("#.# s")),
                5.0);
        aMetric.timeProperty().bind(timeFormatter.valueProperty());

        aComboBox.getItems().add("5 s");
        aComboBox.getItems().add("10 s");
        aComboBox.getItems().add("20 s");
        aComboBox.getItems().add("50 s");
        aComboBox.getEditor().setTextFormatter(timeFormatter);
        aComboBox.getSelectionModel().select(0);

        formatComboBox(aComboBox);
    }

    private void togglePane(ActionEvent aEvent, Node aSummary, Node aContentLabel, Node aContentData) {
        final Toggle t = (Toggle) aEvent.getSource();
        aSummary.setVisible(!t.isSelected());
        aSummary.setManaged(!t.isSelected());
        aContentLabel.setVisible(t.isSelected());
        aContentLabel.setManaged(t.isSelected());
        if (aContentData != null) {
            aContentData.setVisible(t.isSelected());
            aContentData.setManaged(t.isSelected());
        }
        aEvent.consume();

        getView().requestLayout();
    }

    private void updateModifiers() {
        quirksBox.getChildren().clear();
        modifierFormatter.format(model.loadout.getQuirks(), quirksBox.getChildren());
        if (quirksBox.getChildren().isEmpty()) {
            quirksBox.getChildren().add(new Label("N/A"));
        }

        modifiersBox.getChildren().clear();
        modifierFormatter.format(model.loadout.getModifiers(), modifiersBox.getChildren());
        if (modifiersBox.getChildren().isEmpty()) {
            modifiersBox.getChildren().add(new Label("N/A"));
        }
    }

}
