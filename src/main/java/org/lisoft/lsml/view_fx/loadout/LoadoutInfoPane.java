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

import static org.lisoft.lsml.view_fx.util.FxBindingUtils.format;

import java.text.DecimalFormat;
import java.util.regex.Pattern;

import org.lisoft.lsml.command.CmdDistributeArmor;
import org.lisoft.lsml.command.CmdSetArmor;
import org.lisoft.lsml.messages.ArmorMessage;
import org.lisoft.lsml.messages.ArmorMessage.Type;
import org.lisoft.lsml.messages.EfficienciesMessage;
import org.lisoft.lsml.messages.ItemMessage;
import org.lisoft.lsml.messages.LoadoutMessage;
import org.lisoft.lsml.messages.Message;
import org.lisoft.lsml.messages.MessageReceiver;
import org.lisoft.lsml.messages.MessageXBar;
import org.lisoft.lsml.messages.OmniPodMessage;
import org.lisoft.lsml.messages.UpgradesMessage;
import org.lisoft.lsml.model.chassi.ArmorSide;
import org.lisoft.lsml.model.chassi.Location;
import org.lisoft.lsml.model.datacache.EnvironmentDB;
import org.lisoft.lsml.model.environment.Environment;
import org.lisoft.lsml.model.loadout.Loadout;
import org.lisoft.lsml.model.loadout.component.ConfiguredComponent;
import org.lisoft.lsml.model.modifiers.MechEfficiencyType;
import org.lisoft.lsml.util.CommandStack;
import org.lisoft.lsml.util.CommandStack.Command;
import org.lisoft.lsml.util.CommandStack.CompositeCommand;
import org.lisoft.lsml.view_fx.LiSongMechLab;
import org.lisoft.lsml.view_fx.controls.BetterTextFormatter;
import org.lisoft.lsml.view_fx.controls.FixedRowsTableView;
import org.lisoft.lsml.view_fx.controls.RegexStringConverter;
import org.lisoft.lsml.view_fx.properties.LoadoutMetricsModelAdaptor;
import org.lisoft.lsml.view_fx.properties.LoadoutModelAdaptor;
import org.lisoft.lsml.view_fx.style.ModifierFormatter;
import org.lisoft.lsml.view_fx.util.FxControlUtils;

import javafx.application.Platform;
import javafx.beans.binding.StringBinding;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ComboBox;
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

    private class CmdArmorSlider extends CompositeCommand {
        private final double newValue;
        private double oldValue;
        private final Slider slider;

        public CmdArmorSlider(Slider aSlider, double aOldValue) {
            super("armor adjustment", xBar);
            slider = aSlider;
            oldValue = aOldValue;
            newValue = slider.getValue();
        }

        @Override
        public void buildCommand() {
            addOp(new CmdDistributeArmor(model.loadout, (int) armorWizardAmount.getValue(), armorWizardRatio.getValue(),
                    messageBuffer));
        }

        @Override
        public boolean canCoalescele(Command aOperation) {
            if (aOperation != this && aOperation != null && aOperation instanceof CmdArmorSlider) {
                CmdArmorSlider op = (CmdArmorSlider) aOperation;
                boolean ans = slider == op.slider;
                if (ans) {
                    op.oldValue = oldValue;
                }
                return ans;
            }
            return false;
        }

        @Override
        protected void apply() throws Exception {
            disableSliderAction = true;
            slider.setValue(newValue);
            super.apply();
            disableSliderAction = false;
        }

        @Override
        protected void undo() {
            disableSliderAction = true;
            slider.setValue(oldValue);
            super.undo();
            disableSliderAction = false;
        }
    }

    private class CmdResetManualArmor extends CompositeCommand {
        public CmdResetManualArmor() {
            super("reset manual armor", xBar);
        }

        @Override
        public void buildCommand() {
            Loadout loadout = model.loadout;
            for (ConfiguredComponent component : loadout.getComponents()) {
                for (ArmorSide side : ArmorSide.allSides(component.getInternalComponent())) {
                    addOp(new CmdSetArmor(messageBuffer, loadout, component, side, component.getArmor(side), false));
                }
            }
        }

        @Override
        public boolean canCoalescele(Command aOperation) {
            return aOperation != this && aOperation instanceof CmdResetManualArmor;
        }

        @Override
        protected void apply() throws Exception {
            super.apply();
            updateArmorWizard();
        }

        @Override
        protected void undo() {
            super.undo();
            updateArmorWizard();
        }
    }

    private static final String WSTAT_COL_AMMO = "Rnds";
    private static final String WSTAT_COL_DAMAGE = "Dmg";
    private static final String WSTAT_COL_EAPON = "Weapon";
    private static final String WSTAT_COL_SECONDS = "Time";
    private static final String WSTAT_COL_VOLLEYS = "Vlys";
    @FXML
    private Slider armorWizardAmount;
    @FXML
    private Slider armorWizardRatio;
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
    private final LoadoutMetricsModelAdaptor metrics;
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
    private Label mobilityTorsoPitchSpeed;
    @FXML
    private Label mobilityTorsoYawSpeed;
    @FXML
    private Label mobilityTurnSpeed;
    private final LoadoutModelAdaptor model;
    private final ModifierFormatter modifierFormatter = new ModifierFormatter();
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

    public LoadoutInfoPane(MessageXBar aXBar, CommandStack aStack, LoadoutModelAdaptor aModel,
            LoadoutMetricsModelAdaptor aMetrics) {
        FxControlUtils.loadFxmlControl(this);

        aXBar.attach(this);
        xBar = aXBar;
        cmdStack = aStack;
        model = aModel;
        metrics = aMetrics;

        setupEfficienciesPanel();
        setupArmorWizard();
        updateModifiers();
        setupMobilityPanel();
        setupHeatPanel();
        setupOffensivePanel();
    }

    @FXML
    public void armorWizardResetAll() throws Exception {
        cmdStack.pushAndApply(new CmdResetManualArmor());
        updateArmorWizard();
    }

    @Override
    public void receive(Message aMsg) {
        boolean efficiencies = aMsg instanceof EfficienciesMessage;
        boolean items = aMsg instanceof ItemMessage;
        boolean modules = aMsg instanceof LoadoutMessage
                && (((LoadoutMessage) aMsg).type == LoadoutMessage.Type.MODULES_CHANGED);
        boolean upgrades = aMsg instanceof UpgradesMessage;
        boolean omniPods = aMsg instanceof OmniPodMessage;
        boolean autoArmorUpdate = aMsg instanceof ArmorMessage
                && ((ArmorMessage) aMsg).type == Type.ARMOR_DISTRIBUTION_UPDATE_REQUEST;

        if (efficiencies || items || omniPods || modules) {
            updateModifiers();
        }

        if (upgrades || items || autoArmorUpdate) {
            Platform.runLater(() -> updateArmorWizard());
        }
    }

    private void setupArmorWizard() {
        armorWizardAmount.setMax(model.loadout.getChassis().getArmorMax());
        armorWizardAmount.setValue(model.loadout.getArmor());
        armorWizardAmount.valueProperty().addListener((aObservable, aOld, aNew) -> {
            if (disableSliderAction)
                return;
            LiSongMechLab.safeCommand(this, cmdStack, new CmdArmorSlider(armorWizardAmount, aOld.doubleValue()));
        });

        final double max_ratio = 24;
        ConfiguredComponent ct = model.loadout.getComponent(Location.CenterTorso);
        double currentRatio = ((double) ct.getArmor(ArmorSide.FRONT)) / Math.max(ct.getArmor(ArmorSide.BACK), 1);
        currentRatio = Math.min(max_ratio, currentRatio);

        armorWizardRatio.setMax(max_ratio);
        armorWizardRatio.setValue(currentRatio);
        armorWizardRatio.valueProperty().addListener((aObservable, aOld, aNew) -> {
            if (disableSliderAction)
                return;
            LiSongMechLab.safeCommand(this, cmdStack, new CmdArmorSlider(armorWizardRatio, aOld.doubleValue()));
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
        for (Environment e : EnvironmentDB.lookupAll()) {
            heatEnvironment.getItems().add(e);
        }
        heatEnvironment.valueProperty().bindBidirectional(metrics.environment);
        heatEnvironment.getSelectionModel().select(0);

        heatSinkCount.textProperty().bind(format("Heat Sinks: %", metrics.heatSinkCount));
        heatCapacity.textProperty().bind(format("Heat Capacity: %.1h", metrics.heatCapacity));
        heatCoolingRatio.textProperty().bind(format("Cooling Ratio: %.1ph", metrics.coolingRatio));
        heatTimeToCool.textProperty().bind(format("Time to Cool: %.1h s", metrics.timeToCool));
    }

    private void setupMobilityPanel() {
        mobilityTopSpeed.textProperty().bind(format("Top Speed: %.1h km/h", metrics.topSpeed));
        mobilityTurnSpeed.textProperty().bind(format("Turn Speed: %.1h °/s", metrics.turnSpeed));

        mobilityTorsoPitchSpeed.textProperty().bind(format("Torso (pitch): %.1h °/s", metrics.torsoPitchSpeed));
        mobilityTorsoYawSpeed.textProperty().bind(format("Torso (yaw): %.1h °/s", metrics.torsoYawSpeed));
        mobilityArmPitchSpeed.textProperty().bind(format("Arm (pitch): %.1h °/s", metrics.armPitchSpeed));
        mobilityArmYawSpeed.textProperty().bind(format("Arm (yaw): %.1h °/s", metrics.armYawSpeed));
        mobilityJumpJets.textProperty().bind(format("JumpJets: %/%", metrics.jumpJetCount, metrics.jumpJetMax));

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

        RegexStringConverter rangeConverter = new RegexStringConverter(
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

        TextFormatter<Double> rangeFormatter = new BetterTextFormatter<Double>(rangeConverter, -1.0);
        metrics.range.bind(rangeFormatter.valueProperty());

        offensiveRange.getItems().add("Optimal");
        offensiveRange.getItems().add("90m");
        offensiveRange.getItems().add("180m");
        offensiveRange.getItems().add("270m");
        offensiveRange.getItems().add("450m");
        offensiveRange.getItems().add("720m");
        offensiveRange.getEditor().setTextFormatter(rangeFormatter);
        offensiveTime.getSelectionModel().select(0);

        TextFormatter<Double> timeFormatter = new BetterTextFormatter<Double>(
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

        DecimalFormat df = new DecimalFormat("#");
        final double nameSize = 0.35;
        final double margin = 0.02;

        TableColumn<WeaponSummary, String> nameColumn = new TableColumn<>(WSTAT_COL_EAPON);
        nameColumn.setCellValueFactory(new PropertyValueFactory<>("name"));
        nameColumn.prefWidthProperty().bind(offensiveWeaponTable.widthProperty().multiply(nameSize - margin));

        TableColumn<WeaponSummary, String> ammoColumn = new TableColumn<>(WSTAT_COL_AMMO);
        ammoColumn.setCellValueFactory((aFeatures) -> {
            WeaponSummary value = aFeatures.getValue();
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

        TableColumn<WeaponSummary, String> volleysColumn = new TableColumn<>(WSTAT_COL_VOLLEYS);
        volleysColumn.setCellValueFactory((aFeatures) -> {
            WeaponSummary value = aFeatures.getValue();
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

        TableColumn<WeaponSummary, String> secondsColumn = new TableColumn<>(WSTAT_COL_SECONDS);
        secondsColumn.setCellValueFactory((aFeatures) -> {
            WeaponSummary value = aFeatures.getValue();
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

        TableColumn<WeaponSummary, String> damageColumn = new TableColumn<>(WSTAT_COL_DAMAGE);
        damageColumn.setCellValueFactory((aFeatures) -> {
            WeaponSummary value = aFeatures.getValue();
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

        ObservableList<TableColumn<WeaponSummary, ?>> cols = offensiveWeaponTable.getColumns();
        cols.clear();
        cols.add(nameColumn);
        cols.add(ammoColumn);
        cols.add(volleysColumn);
        cols.add(secondsColumn);
        cols.add(damageColumn);
    }

    private void updateArmorWizard() {
        LiSongMechLab.safeCommand(this, sideStack, new CmdDistributeArmor(model.loadout,
                (int) armorWizardAmount.getValue(), armorWizardRatio.getValue(), xBar));
    }

    private void updateModifiers() {
        modifiersBox.getChildren().clear();
        modifierFormatter.format(model.loadout.getModifiers(), modifiersBox.getChildren());
    }

}
