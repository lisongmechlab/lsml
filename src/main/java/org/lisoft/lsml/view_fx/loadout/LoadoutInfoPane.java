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

import static javafx.beans.binding.Bindings.when;
import static org.lisoft.lsml.view_fx.util.FxBindingUtils.format;

import java.text.DecimalFormat;
import java.util.function.Predicate;
import java.util.regex.Pattern;

import org.lisoft.lsml.command.CmdDistributeArmour;
import org.lisoft.lsml.command.CmdSetArmour;
import org.lisoft.lsml.messages.ArmourMessage;
import org.lisoft.lsml.messages.ArmourMessage.Type;
import org.lisoft.lsml.messages.EfficienciesMessage;
import org.lisoft.lsml.messages.ItemMessage;
import org.lisoft.lsml.messages.LoadoutMessage;
import org.lisoft.lsml.messages.Message;
import org.lisoft.lsml.messages.MessageReceiver;
import org.lisoft.lsml.messages.MessageXBar;
import org.lisoft.lsml.messages.OmniPodMessage;
import org.lisoft.lsml.messages.UpgradesMessage;
import org.lisoft.lsml.model.chassi.ArmourSide;
import org.lisoft.lsml.model.chassi.Location;
import org.lisoft.lsml.model.datacache.EnvironmentDB;
import org.lisoft.lsml.model.environment.Environment;
import org.lisoft.lsml.model.loadout.Loadout;
import org.lisoft.lsml.model.loadout.component.ConfiguredComponent;
import org.lisoft.lsml.model.modifiers.MechEfficiencyType;
import org.lisoft.lsml.model.modifiers.Modifier;
import org.lisoft.lsml.model.modifiers.ModifierDescription;
import org.lisoft.lsml.util.CommandStack;
import org.lisoft.lsml.util.CommandStack.Command;
import org.lisoft.lsml.util.CommandStack.CompositeCommand;
import org.lisoft.lsml.view_fx.LiSongMechLab;
import org.lisoft.lsml.view_fx.Settings;
import org.lisoft.lsml.view_fx.controls.BetterTextFormatter;
import org.lisoft.lsml.view_fx.controls.FixedRowsTableView;
import org.lisoft.lsml.view_fx.controls.RegexStringConverter;
import org.lisoft.lsml.view_fx.properties.LoadoutMetrics;
import org.lisoft.lsml.view_fx.properties.LoadoutModelAdaptor;
import org.lisoft.lsml.view_fx.style.PredicatedModifierFormatter;
import org.lisoft.lsml.view_fx.util.FxControlUtils;

import javafx.application.Platform;
import javafx.beans.binding.ObjectBinding;
import javafx.beans.binding.StringBinding;
import javafx.beans.property.BooleanProperty;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.CheckBox;
import javafx.scene.control.CheckMenuItem;
import javafx.scene.control.ComboBox;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.Label;
import javafx.scene.control.Slider;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TextFormatter;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.VBox;
import javafx.scene.shape.Arc;

/**
 * This control shows all the stats for a loadout in one convenient place.
 *
 * @author Emily Björk
 */
public class LoadoutInfoPane extends VBox implements MessageReceiver {

    private class CmdArmourSlider extends CompositeCommand {
        private final double newValue;
        private double oldValue;
        private final Slider slider;

        public CmdArmourSlider(Slider aSlider, double aOldValue) {
            super("armour adjustment", xBar);
            slider = aSlider;
            oldValue = aOldValue;
            newValue = slider.getValue();
        }

        @Override
        public void apply() throws Exception {
            disableSliderAction = true;
            slider.setValue(newValue);
            super.apply();
            disableSliderAction = false;
        }

        @Override
        public void buildCommand() {
            addOp(new CmdDistributeArmour(model.loadout, (int) armourWizardAmount.getValue(),
                    armourWizardRatio.getValue(), messageBuffer));
        }

        @Override
        public boolean canCoalescele(Command aOperation) {
            if (aOperation != this && aOperation != null && aOperation instanceof CmdArmourSlider) {
                final CmdArmourSlider op = (CmdArmourSlider) aOperation;
                final boolean ans = slider == op.slider;
                if (ans) {
                    op.oldValue = oldValue;
                }
                return ans;
            }
            return false;
        }

        @Override
        public void undo() {
            disableSliderAction = true;
            slider.setValue(oldValue);
            super.undo();
            disableSliderAction = false;
        }
    }

    private class CmdResetManualArmour extends CompositeCommand {
        public CmdResetManualArmour() {
            super("reset manual armour", xBar);
        }

        @Override
        public void apply() throws Exception {
            super.apply();
            updateArmourWizard();
        }

        @Override
        public void buildCommand() {
            final Loadout loadout = model.loadout;
            for (final ConfiguredComponent component : loadout.getComponents()) {
                for (final ArmourSide side : ArmourSide.allSides(component.getInternalComponent())) {
                    addOp(new CmdSetArmour(messageBuffer, loadout, component, side, component.getArmour(side), false));
                }
            }
        }

        @Override
        public boolean canCoalescele(Command aOperation) {
            return aOperation != this && aOperation instanceof CmdResetManualArmour;
        }

        @Override
        public void undo() {
            super.undo();
            updateArmourWizard();
        }
    }

    private static final String WSTAT_COL_AMMO = "Rnds";
    private static final String WSTAT_COL_DAMAGE = "Dmg";
    private static final String WSTAT_COL_EAPON = "Weapon";
    private static final String WSTAT_COL_SECONDS = "Time";
    private static final String WSTAT_COL_VOLLEYS = "Vlys";
    @FXML
    private Slider armourWizardAmount;
    @FXML
    private Slider armourWizardRatio;
    private final CommandStack cmdStack;
    private boolean disableSliderAction = false;
    @FXML
    private CheckBox effAnchorTurn;
    @FXML
    private CheckBox effArmReflex;
    @FXML
    private CheckBox effCoolRun;
    @FXML
    private CheckBox effDoubleBasics;
    @FXML
    private CheckBox effFastFire;
    @FXML
    private CheckBox effHeatContainment;
    @FXML
    private CheckBox effSpeedTweak;
    @FXML
    private CheckBox effTwistSpeed;
    @FXML
    private CheckBox effTwistX;
    @FXML
    private Label heatCapacity;
    @FXML
    private Label heatCoolingRatio;
    @FXML
    private ComboBox<Environment> heatEnvironment;
    @FXML
    private Label heatSinkCount;
    @FXML
    private Label heatTimeToCool;
    private final LoadoutMetrics metrics;
    @FXML
    private Arc mobilityArcPitchInner;
    @FXML
    private Arc mobilityArcPitchOuter;
    @FXML
    private Arc mobilityArcYawInner;
    @FXML
    private Arc mobilityArcYawOuter;
    @FXML
    private Label mobilityArmPitchSpeed;
    @FXML
    private Label mobilityArmYawSpeed;
    @FXML
    private Label mobilityJumpJets;
    @FXML
    private Label mobilityTopSpeed;
    @FXML
    private Label mobilityMascSpeed;
    @FXML
    private Label mobilityTorsoPitchSpeed;
    @FXML
    private Label mobilityTorsoYawSpeed;
    @FXML
    private Label mobilityTurnSpeed;
    private final LoadoutModelAdaptor model;
    private final PredicatedModifierFormatter modifierFormatter = new PredicatedModifierFormatter(x -> true);
    @FXML
    private VBox modifiersBox;
    @FXML
    private ComboBox<String> offensiveRange;
    @FXML
    private ComboBox<String> offensiveTime;
    @FXML
    private FixedRowsTableView<WeaponSummary> offensiveWeaponTable;
    private final CommandStack sideStack = new CommandStack(0);
    private final MessageXBar xBar;
    @FXML
    private VBox offensivePane;

    private final Settings settings = Settings.getSettings();
    private final BooleanProperty compactUI = BooleanProperty
            .booleanProperty(settings.getBoolean(Settings.UI_COMPACT_LAYOUT));

    public LoadoutInfoPane(MessageXBar aXBar, CommandStack aStack, LoadoutModelAdaptor aModel,
            LoadoutMetrics aMetrics) {
        FxControlUtils.loadFxmlControl(this);

        aXBar.attach(this);
        xBar = aXBar;
        cmdStack = aStack;
        model = aModel;
        metrics = aMetrics;

        final BooleanProperty showArmorStructureQuirks = BooleanProperty
                .booleanProperty(settings.getBoolean(Settings.UI_SHOW_STRUCTURE_ARMOR_QUIRKS));
        showArmorStructureQuirks.addListener((aObs, aOld, aNew) -> updateModifiers());

        final Predicate<Modifier> truePredicate = aModifier -> {
            return true;
        };
        final Predicate<Modifier> filterPredicate = aModifier -> {
            final boolean isArmor = aModifier.getDescription().getSelectors()
                    .containsAll(ModifierDescription.SEL_ARMOUR);
            final boolean isStructure = aModifier.getDescription().getSelectors()
                    .containsAll(ModifierDescription.SEL_STRUCTURE);
            return !isArmor && !isStructure;
        };

        final ObjectBinding<Predicate<Modifier>> predicateBinding = when(showArmorStructureQuirks).then(truePredicate)
                .otherwise(filterPredicate);

        final CheckMenuItem mi = new CheckMenuItem("Show structure & armor quirks");
        mi.selectedProperty().bindBidirectional(showArmorStructureQuirks);
        final ContextMenu cm = new ContextMenu(mi);
        modifierFormatter.predicateProperty().bind(predicateBinding);
        modifiersBox.setOnMousePressed(aEvent -> {
            if (aEvent.isSecondaryButtonDown()) {
                cm.show(modifiersBox, aEvent.getScreenX(), aEvent.getScreenY());
            }
            else if (aEvent.isPrimaryButtonDown()) {
                cm.hide();
            }
            aEvent.consume();
        });

        setupEfficienciesPanel();
        setupArmourWizard();
        updateModifiers();
        setupMobilityPanel();
        setupHeatPanel();
        setupOffensivePanel();
    }

    @FXML
    public void armourWizardResetAll() throws Exception {
        cmdStack.pushAndApply(new CmdResetManualArmour());
        updateArmourWizard();
    }

    @Override
    public void receive(Message aMsg) {
        final boolean efficiencies = aMsg instanceof EfficienciesMessage;
        final boolean items = aMsg instanceof ItemMessage;
        final boolean modules = aMsg instanceof LoadoutMessage
                && ((LoadoutMessage) aMsg).type == LoadoutMessage.Type.MODULES_CHANGED;
        final boolean upgrades = aMsg instanceof UpgradesMessage;
        final boolean omniPods = aMsg instanceof OmniPodMessage;
        final boolean autoArmourUpdate = aMsg instanceof ArmourMessage
                && ((ArmourMessage) aMsg).type == Type.ARMOUR_DISTRIBUTION_UPDATE_REQUEST;

        if (efficiencies || items || omniPods || modules) {
            updateModifiers();
        }

        if (upgrades || items || autoArmourUpdate) {
            Platform.runLater(() -> updateArmourWizard());
        }
    }

    private void setupArmourWizard() {
        armourWizardAmount.setMax(model.loadout.getChassis().getArmourMax());
        armourWizardAmount.setValue(model.loadout.getArmour());
        armourWizardAmount.valueProperty().addListener((aObservable, aOld, aNew) -> {
            if (disableSliderAction) {
                return;
            }
            LiSongMechLab.safeCommand(this, cmdStack, new CmdArmourSlider(armourWizardAmount, aOld.doubleValue()),
                    xBar);
        });

        final double max_ratio = 24;
        final ConfiguredComponent ct = model.loadout.getComponent(Location.CenterTorso);
        double currentRatio = (double) ct.getArmour(ArmourSide.FRONT) / Math.max(ct.getArmour(ArmourSide.BACK), 1);
        currentRatio = Math.min(max_ratio, currentRatio);

        armourWizardRatio.setMax(max_ratio);
        armourWizardRatio.setValue(currentRatio);
        armourWizardRatio.valueProperty().addListener((aObservable, aOld, aNew) -> {
            if (disableSliderAction) {
                return;
            }
            LiSongMechLab.safeCommand(this, cmdStack, new CmdArmourSlider(armourWizardRatio, aOld.doubleValue()), xBar);
        });
    }

    private void setupEffCheckbox(CheckBox aCheckBox, MechEfficiencyType aEfficiencyType) {
        FxControlUtils.bindTogglable(aCheckBox, model.hasEfficiency.get(aEfficiencyType), aValue -> {
            model.loadout.getEfficiencies().setEfficiency(aEfficiencyType, aValue, xBar);
            return true;
        });
    }

    private void setupEfficienciesPanel() {
        setupEffCheckbox(effCoolRun, MechEfficiencyType.COOL_RUN);
        setupEffCheckbox(effHeatContainment, MechEfficiencyType.HEAT_CONTAINMENT);
        setupEffCheckbox(effTwistX, MechEfficiencyType.TWIST_X);
        setupEffCheckbox(effTwistSpeed, MechEfficiencyType.TWIST_SPEED);
        setupEffCheckbox(effAnchorTurn, MechEfficiencyType.ANCHORTURN);
        setupEffCheckbox(effArmReflex, MechEfficiencyType.ARM_REFLEX);
        setupEffCheckbox(effFastFire, MechEfficiencyType.FAST_FIRE);
        setupEffCheckbox(effSpeedTweak, MechEfficiencyType.SPEED_TWEAK);

        effDoubleBasics.selectedProperty().bindBidirectional(model.hasDoubleBasics);
    }

    private void setupHeatPanel() {
        heatEnvironment.getItems();
        heatEnvironment.getItems().add(new Environment("Neutral", 0.0));
        for (final Environment e : EnvironmentDB.lookupAll()) {
            heatEnvironment.getItems().add(e);
        }
        heatEnvironment.valueProperty().bindBidirectional(metrics.environmentProperty);
        heatEnvironment.getSelectionModel().select(0);

        if (compactUI.get()) {
            heatSinkCount.textProperty().bind(format("Sinks: %", metrics.heatSinkCount));
            heatCapacity.textProperty().bind(format("Capacity: %.1h", metrics.heatCapacity));
            heatCoolingRatio.textProperty().bind(format("Ratio: %.1ph", metrics.alphaGroup.coolingRatio));
            heatTimeToCool.textProperty().bind(format("TtC: %.1h s", metrics.timeToCool));
        }
        else {
            heatSinkCount.textProperty().bind(format("Heat Sinks: %", metrics.heatSinkCount));
            heatCapacity.textProperty().bind(format("Heat Capacity: %.1h", metrics.heatCapacity));
            heatCoolingRatio.textProperty().bind(format("Cooling Ratio: %.1ph", metrics.alphaGroup.coolingRatio));
            heatTimeToCool.textProperty().bind(format("Time to Cool: %.1h s", metrics.timeToCool));
        }

        heatSinkCount.styleProperty()
                .bind(when(metrics.heatSinkCount.lessThan(10)).then("-fx-text-fill: quirk-bad;").otherwise(""));
    }

    private void setupMobilityPanel() {
        mobilityTopSpeed.textProperty().bind(format("Speed: %.1h km/h", metrics.topSpeed));
        mobilityMascSpeed.textProperty().bind(format("MASC: %.1h km/h", metrics.mascSpeed));
        if (!compactUI.get()) {
            mobilityTurnSpeed.textProperty().bind(format("Turn Speed: %.1h °/s", metrics.turnSpeed));
            mobilityTorsoPitchSpeed.textProperty().bind(format("Torso (pitch): %.1h °/s", metrics.torsoPitchSpeed));
            mobilityTorsoYawSpeed.textProperty().bind(format("Torso (yaw): %.1h °/s", metrics.torsoYawSpeed));
            mobilityArmPitchSpeed.textProperty().bind(format("Arm (pitch): %.1h °/s", metrics.armPitchSpeed));
            mobilityArmYawSpeed.textProperty().bind(format("Arm (yaw): %.1h °/s", metrics.armYawSpeed));
            mobilityJumpJets.textProperty().bind(format("Jump Jets: %/%", metrics.jumpJetCount, metrics.jumpJetMax));
        }
        else {
            mobilityTurnSpeed.textProperty().bind(format("Turning: %.1h °/s", metrics.turnSpeed));
            mobilityTorsoPitchSpeed.textProperty().bind(format("Torso (p): %.1h °/s", metrics.torsoPitchSpeed));
            mobilityTorsoYawSpeed.textProperty().bind(format("Torso (y): %.1h °/s", metrics.torsoYawSpeed));
            mobilityArmPitchSpeed.textProperty().bind(format("Arm (p): %.1h °/s", metrics.armPitchSpeed));
            mobilityArmYawSpeed.textProperty().bind(format("Arm (y): %.1h °/s", metrics.armYawSpeed));
            mobilityJumpJets.textProperty().bind(format("Jump Jets: %/%", metrics.jumpJetCount, metrics.jumpJetMax));
        }

        mobilityArcPitchOuter.lengthProperty().bind(metrics.torsoPitch.add(metrics.armPitch).multiply(2.0));
        mobilityArcPitchInner.lengthProperty().bind(metrics.torsoPitch.multiply(2.0));
        mobilityArcYawOuter.lengthProperty().bind(metrics.torsoYaw.add(metrics.armYaw).multiply(2.0));
        mobilityArcYawInner.lengthProperty().bind(metrics.torsoYaw.multiply(2.0));

        mobilityArcPitchOuter.startAngleProperty().bind(mobilityArcPitchOuter.lengthProperty().negate().divide(2));
        mobilityArcPitchInner.startAngleProperty().bind(mobilityArcPitchInner.lengthProperty().negate().divide(2));
        mobilityArcYawOuter.startAngleProperty().bind(mobilityArcYawOuter.lengthProperty().divide(-2).add(90));
        mobilityArcYawInner.startAngleProperty().bind(mobilityArcYawInner.lengthProperty().divide(-2).add(90));
    }

    private void setupOffensivePanel() {

        final RegexStringConverter rangeConverter = new RegexStringConverter(
                Pattern.compile("\\s*(-?\\d*(?:\\.\\d*)?)\\s*m?"), new DecimalFormat("#")) {

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
        metrics.range.bind(rangeFormatter.valueProperty());

        offensiveRange.getItems().add("Optimal");
        offensiveRange.getItems().add("90m");
        offensiveRange.getItems().add("180m");
        offensiveRange.getItems().add("270m");
        offensiveRange.getItems().add("450m");
        offensiveRange.getItems().add("720m");
        offensiveRange.getEditor().setTextFormatter(rangeFormatter);
        offensiveRange.getSelectionModel().select(0);

        final TextFormatter<Double> timeFormatter = new BetterTextFormatter<>(
                new RegexStringConverter(Pattern.compile("\\s*(-?\\d*)\\s*s?"), new DecimalFormat("# s")), 5.0);
        metrics.burstTime.bind(timeFormatter.valueProperty());

        offensiveTime.getItems().add("5 s");
        offensiveTime.getItems().add("10 s");
        offensiveTime.getItems().add("20 s");
        offensiveTime.getItems().add("50 s");
        offensiveTime.getEditor().setTextFormatter(timeFormatter);
        offensiveTime.getSelectionModel().select(0);

        offensivePane.getChildren().add(1, new WeaponGroupStats(metrics.alphaGroup, metrics));

        setupWeaponsTable();
    }

    private void setupWeaponsTable() {
        offensiveWeaponTable.setItems(new WeaponSummaryList(xBar, model.loadout));
        offensiveWeaponTable.setVisibleRows(5);

        final DecimalFormat df = new DecimalFormat("#");
        final double nameSize = 0.35;
        final double margin = 0.02;

        final TableColumn<WeaponSummary, String> nameColumn = new TableColumn<>(WSTAT_COL_EAPON);
        nameColumn.setCellValueFactory(new PropertyValueFactory<>("name"));
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
                    return df.format(value.roundsProperty().get() / value.volleySizeProperty().get());
                }
            };
        });
        volleysColumn.prefWidthProperty().bind(offensiveWeaponTable.widthProperty().multiply((1 - nameSize) / 4));

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

        final ObservableList<TableColumn<WeaponSummary, ?>> cols = offensiveWeaponTable.getColumns();
        cols.clear();
        cols.add(nameColumn);
        cols.add(ammoColumn);
        cols.add(volleysColumn);
        cols.add(secondsColumn);
        cols.add(damageColumn);
    }

    private void updateArmourWizard() {
        LiSongMechLab.safeCommand(this, sideStack, new CmdDistributeArmour(model.loadout,
                (int) armourWizardAmount.getValue(), armourWizardRatio.getValue(), xBar), xBar);
    }

    private void updateModifiers() {
        modifiersBox.getChildren().clear();
        modifierFormatter.format(model.loadout.getModifiers(), modifiersBox.getChildren());
    }

}
