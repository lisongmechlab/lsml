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

import static javafx.beans.binding.Bindings.format;
import static javafx.beans.binding.Bindings.isNull;
import static javafx.beans.binding.Bindings.when;

import java.text.DecimalFormat;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
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
import org.lisoft.lsml.model.DynamicSlotDistributor;
import org.lisoft.lsml.model.chassi.ArmorSide;
import org.lisoft.lsml.model.chassi.ChassisBase;
import org.lisoft.lsml.model.chassi.Location;
import org.lisoft.lsml.model.datacache.EnvironmentDB;
import org.lisoft.lsml.model.datacache.ItemDB;
import org.lisoft.lsml.model.datacache.PilotModuleDB;
import org.lisoft.lsml.model.environment.Environment;
import org.lisoft.lsml.model.item.Item;
import org.lisoft.lsml.model.item.ModuleSlot;
import org.lisoft.lsml.model.item.PilotModule;
import org.lisoft.lsml.model.item.Weapon;
import org.lisoft.lsml.model.item.WeaponModule;
import org.lisoft.lsml.model.loadout.LoadoutBase;
import org.lisoft.lsml.model.loadout.LoadoutMetrics;
import org.lisoft.lsml.model.loadout.LoadoutStandard;
import org.lisoft.lsml.model.loadout.component.ConfiguredComponentBase;
import org.lisoft.lsml.model.modifiers.MechEfficiencyType;
import org.lisoft.lsml.model.upgrades.Upgrades;
import org.lisoft.lsml.util.CommandStack;
import org.lisoft.lsml.util.CommandStack.Command;
import org.lisoft.lsml.util.CommandStack.CompositeCommand;
import org.lisoft.lsml.view_fx.LiSongMechLab;
import org.lisoft.lsml.view_fx.controls.BetterTextFormatter;
import org.lisoft.lsml.view_fx.controls.FilterTreeItem;
import org.lisoft.lsml.view_fx.controls.RegexStringConverter;
import org.lisoft.lsml.view_fx.loadout.component.ComponentPane;
import org.lisoft.lsml.view_fx.loadout.component.ModulePane;
import org.lisoft.lsml.view_fx.loadout.equipment.EquipmentCategory;
import org.lisoft.lsml.view_fx.loadout.equipment.EquipmentTableCell;
import org.lisoft.lsml.view_fx.loadout.equipment.EquipmentTableRow;
import org.lisoft.lsml.view_fx.loadout.equipment.EquippablePredicate;
import org.lisoft.lsml.view_fx.loadout.equipment.ModuleTableRow;
import org.lisoft.lsml.view_fx.properties.LoadoutMetricsModelAdaptor;
import org.lisoft.lsml.view_fx.properties.LoadoutModelAdaptor;
import org.lisoft.lsml.view_fx.style.ModifierFormatter;
import org.lisoft.lsml.view_fx.style.StyleManager;

import javafx.application.Platform;
import javafx.beans.binding.StringBinding;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.ReadOnlyStringWrapper;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.MenuItem;
import javafx.scene.control.ProgressBar;
import javafx.scene.control.Slider;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextFormatter;
import javafx.scene.control.TreeItem;
import javafx.scene.control.TreeTableColumn;
import javafx.scene.control.TreeTableView;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyCodeCombination;
import javafx.scene.input.KeyCombination;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import javafx.scene.shape.Arc;

/**
 * Controller for the loadout window.
 * 
 * @author Emily Björk
 */
public class LoadoutWindowController implements MessageReceiver {
    private class CmdArmorSlider extends CompositeCommand {
        private final double newValue;
        private double       oldValue;
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
            LoadoutBase<?> loadout = model.loadout;
            for (ConfiguredComponentBase component : loadout.getComponents()) {
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

    private static final String        EQ_COL_MASS         = "Mass";
    private static final String        EQ_COL_NAME         = "Name";
    private static final String        EQ_COL_SLOTS        = "Slots";
    private static final int           UNDO_DEPTH          = 128;
    private static final String        WSTAT_COL_AMMO      = "Rnds";
    private static final String        WSTAT_COL_DAMAGE    = "Dmg";
    private static final String        WSTAT_COL_EAPON     = "Weapon";

    private static final String        WSTAT_COL_SECONDS   = "Time";
    private static final String        WSTAT_COL_VOLLEYS   = "Vlys";
    @FXML
    MenuItem                           menuRedo;
    @FXML
    MenuItem                           menuUndo;
    @FXML
    private Slider                     armorWizardAmount;
    @FXML
    private Slider                     armorWizardRatio;
    private final CommandStack         cmdStack            = new CommandStack(UNDO_DEPTH);
    private boolean                    disableSliderAction = false;
    @FXML
    private CheckBox                   effAnchorTurn;
    @FXML
    private CheckBox                   effArmReflex;
    @FXML
    private CheckBox                   effCoolRun;
    @FXML
    private CheckBox                   effDoubleBasics;
    @FXML
    private CheckBox                   effFastFire;
    @FXML
    private CheckBox                   effHeatContainment;
    @FXML
    private CheckBox                   effSpeedTweak;
    @FXML
    private CheckBox                   effTwistSpeed;
    @FXML
    private CheckBox                   effTwistX;
    @FXML
    private TreeTableView<Object>      equipmentList;
    @FXML
    private ProgressBar                generalArmorBar;
    @FXML
    private Label                      generalArmorLabel;
    @FXML
    private ProgressBar                generalMassBar;
    @FXML
    private Label                      generalMassLabel;
    @FXML
    private ProgressBar                generalSlotsBar;
    @FXML
    private Label                      generalSlotsLabel;
    @FXML
    private Label                      heatCapacity;
    @FXML
    private Label                      heatCoolingRatio;
    @FXML
    private ComboBox<Environment>      heatEnvironment;
    @FXML
    private Label                      heatSinkCount;
    @FXML
    private Label                      heatTimeToCool;
    @FXML
    private HBox                       layoutContainer;
    private LoadoutMetricsModelAdaptor metrics;
    @FXML
    private Arc                        mobilityArcPitchInner;
    @FXML
    private Arc                        mobilityArcPitchOuter;
    @FXML
    private Arc                        mobilityArcYawInner;
    @FXML
    private Arc                        mobilityArcYawOuter;
    @FXML
    private Label                      mobilityArmPitchSpeed;
    @FXML
    private Label                      mobilityArmYawSpeed;
    @FXML
    private Label                      mobilityJumpJets;
    @FXML
    private Label                      mobilityTopSpeed;
    @FXML
    private Label                      mobilityTorsoPitchSpeed;
    @FXML
    private Label                      mobilityTorsoYawSpeed;
    @FXML
    private Label                      mobilityTurnSpeed;
    private LoadoutModelAdaptor        model;
    private final ModifierFormatter    modifierFormatter   = new ModifierFormatter();
    @FXML
    private VBox                       modifiersBox;
    @FXML
    private TreeTableView<Object>      moduleList;
    @FXML
    private Label                      offensiveAlphaDamage;
    @FXML
    private Label                      offensiveAlphaGhostHeat;
    @FXML
    private Label                      offensiveAlphaHeat;
    @FXML
    private Label                      offensiveAlphaTimeToCool;
    @FXML
    private Label                      offensiveBurstDamage;
    @FXML
    private Label                      offensiveMaxDPS;

    @FXML
    private ComboBox<String>           offensiveRange;
    @FXML
    private Label                      offensiveSustainedDPS;
    @FXML
    private ComboBox<String>           offensiveTime;
    @FXML
    private Label                      offensiveTimeToOverheat;
    @FXML
    private TableView<WeaponSummary>   offensiveWeaponTable;
    private final CommandStack         sideStack           = new CommandStack(0);
    @FXML
    private CheckBox                   upgradeArtemis;
    @FXML
    private CheckBox                   upgradeDoubleHeatSinks;
    @FXML
    private CheckBox                   upgradeEndoSteel;
    @FXML
    private CheckBox                   upgradeFerroFibrous;
    private final MessageXBar          xBar                = new MessageXBar();

    @FXML
    public void armorWizardResetAll(@SuppressWarnings("unused") ActionEvent event) throws Exception {
        cmdStack.pushAndApply(new CmdResetManualArmor());
        updateArmorWizard();
    }

    @Override
    public void receive(Message aMsg) {
        boolean efficiencies = aMsg instanceof EfficienciesMessage;
        boolean items = aMsg instanceof ItemMessage;
        boolean upgrades = aMsg instanceof UpgradesMessage;
        boolean omniPods = aMsg instanceof OmniPodMessage;
        boolean modules = aMsg instanceof LoadoutMessage;

        boolean autoArmorUpdate = aMsg instanceof ArmorMessage
                && ((ArmorMessage) aMsg).type == Type.ARMOR_DISTRIBUTION_UPDATE_REQUEST;

        if (efficiencies || items || omniPods) {
            updateModifiers();
        }

        if (items || upgrades || omniPods) {
            updateEquipmentPredicates();
        }

        if (modules) {
            updateModulePredicates();
        }

        if (upgrades || items || autoArmorUpdate) {
            Platform.runLater(() -> updateArmorWizard());
        }
    }

    @FXML
    public void redo(@SuppressWarnings("unused") ActionEvent event) throws Exception {
        cmdStack.redo();
    }

    public void setLoadout(LoadoutBase<?> aLoadout) {
        xBar.attach(this);
        model = new LoadoutModelAdaptor(aLoadout, xBar, cmdStack);
        metrics = new LoadoutMetricsModelAdaptor(new LoadoutMetrics(aLoadout, null, xBar), aLoadout, xBar);

        setupLayoutView();
        setupEquipmentList();
        setupGeneralPanel();
        setupUpgradesPanel();
        setupEfficienciesPanel();
        setupModulesList();
        setupArmorWizard();
        updateModifiers();
        setupMobilityPanel();
        setupHeatPanel();
        setupOffensivePanel();

        setupMenuBar();
    }

    @FXML
    public void undo(@SuppressWarnings("unused") ActionEvent event) {
        cmdStack.undo();
    }

    private void setupArmorWizard() {
        armorWizardAmount.setMax(model.loadout.getChassis().getArmorMax());
        armorWizardAmount.setValue(model.loadout.getArmor());
        armorWizardAmount.valueProperty().addListener((aObservable, aOld, aNew) -> {
            if (disableSliderAction)
                return;
            try {
                cmdStack.pushAndApply(new CmdArmorSlider(armorWizardAmount, aOld.doubleValue()));
            }
            catch (Exception e) {
                LiSongMechLab.showError(e);
            }
        });

        final double max_ratio = 24;
        ConfiguredComponentBase ct = model.loadout.getComponent(Location.CenterTorso);
        double currentRatio = ((double) ct.getArmor(ArmorSide.FRONT)) / Math.max(ct.getArmor(ArmorSide.BACK), 1);
        currentRatio = Math.min(max_ratio, currentRatio);

        armorWizardRatio.setMax(max_ratio);
        armorWizardRatio.setValue(currentRatio);
        armorWizardRatio.valueProperty().addListener((aObservable, aOld, aNew) -> {
            if (disableSliderAction)
                return;
            try {
                cmdStack.pushAndApply(new CmdArmorSlider(armorWizardRatio, aOld.doubleValue()));
            }
            catch (Exception e) {
                LiSongMechLab.showError(e);
            }
        });
    }

    private void setupEffCheckbox(CheckBox aCheckBox, MechEfficiencyType aEfficiencyType) {
        BooleanProperty property = model.hasEfficiency.get(aEfficiencyType);
        aCheckBox.selectedProperty().bindBidirectional(property);
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

    private void setupEquipmentList() {
        FilterTreeItem<Object> root = new FilterTreeItem<>();
        root.setExpanded(true);

        List<Item> allItems = ItemDB.lookup(Item.class);
        allItems.sort(null);

        Map<EquipmentCategory, FilterTreeItem<Object>> categories = new HashMap<>();
        for (EquipmentCategory category : EquipmentCategory.values()) {
            FilterTreeItem<Object> treeItem = new FilterTreeItem<>(category);
            treeItem.setExpanded(true);
            root.add(treeItem);
            categories.put(category, treeItem);
        }

        allItems.stream().filter(aItem -> {
            ChassisBase chassis = model.loadout.getChassis();
            return aItem.getFaction().isCompatible(chassis.getFaction()) && chassis.isAllowed(aItem);
        }).forEach(aItem -> {
            final EquipmentCategory category = EquipmentCategory.classify(aItem);
            categories.get(category).add(new TreeItem<>(aItem));
        });

        setupEquipmentListColumns();

        equipmentList.setRowFactory(aParam -> new EquipmentTableRow(model.loadout, cmdStack, xBar));
        equipmentList.setRoot(root);
        updateEquipmentPredicates();
    }

    private void setupEquipmentListColumns() {

        TreeTableColumn<Object, String> nameColumn = new TreeTableColumn<>(EQ_COL_NAME);
        nameColumn.setCellValueFactory(new ItemValueFactory(item -> item.getShortName(), true));
        nameColumn.setCellFactory(aColumn -> new EquipmentTableCell(model.loadout, true));
        nameColumn.prefWidthProperty().bind(equipmentList.widthProperty().multiply(0.6));

        TreeTableColumn<Object, String> slotsColumn = new TreeTableColumn<>(EQ_COL_SLOTS);
        slotsColumn
                .setCellValueFactory(new ItemValueFactory(item -> Integer.toString(item.getNumCriticalSlots()), false));
        slotsColumn.setCellFactory(aColumn -> new EquipmentTableCell(model.loadout, false));
        slotsColumn.prefWidthProperty().bind(equipmentList.widthProperty().multiply(0.15));

        TreeTableColumn<Object, String> massColumn = new TreeTableColumn<>(EQ_COL_MASS);
        massColumn.setCellValueFactory(new ItemValueFactory(item -> Double.toString(item.getMass()), false));
        massColumn.setCellFactory(aColumn -> new EquipmentTableCell(model.loadout, false));
        massColumn.prefWidthProperty().bind(equipmentList.widthProperty().multiply(0.15));

        ObservableList<TreeTableColumn<Object, ?>> columns = equipmentList.getColumns();
        columns.clear();
        columns.add(nameColumn);
        columns.add(slotsColumn);
        columns.add(massColumn);
    }

    private void setupGeneralPanel() {
        ChassisBase chassis = model.loadout.getChassis();
        int massMax = chassis.getMassMax();

        Pane parent = (Pane) generalMassBar.getParent();
        generalMassBar.progressProperty().bind(model.statsMass.divide(massMax));
        generalMassBar.prefWidthProperty().bind(parent.widthProperty());
        generalMassLabel.textProperty().bind(format("%.2f t free", model.statsFreeMass));

        int armorMax = chassis.getArmorMax();
        generalArmorBar.progressProperty().bind(model.statsArmor.divide((double) armorMax));
        generalArmorBar.prefWidthProperty().bind(parent.widthProperty());
        generalArmorLabel.textProperty().bind(format("%d free", model.statsArmorFree));

        int criticalSlotsTotal = chassis.getCriticalSlotsTotal();
        generalSlotsBar.progressProperty().bind(model.statsSlots.divide((double) criticalSlotsTotal));
        generalSlotsBar.prefWidthProperty().bind(parent.widthProperty());
        generalSlotsLabel.textProperty().bind(format("%d free", model.statsSlots.negate().add(criticalSlotsTotal)));
    }

    private void setupHeatPanel() {
        heatEnvironment.getItems();
        heatEnvironment.getItems().add(new Environment("Neutral", 0.0));
        for (Environment e : EnvironmentDB.lookupAll()) {
            heatEnvironment.getItems().add(e);
        }
        heatEnvironment.valueProperty().bindBidirectional(metrics.environment);
        heatEnvironment.getSelectionModel().select(0);

        heatSinkCount.textProperty().bind(format("Heat Sinks: %d", metrics.heatSinkCount));
        heatCapacity.textProperty().bind(format("Heat Capacity: %.1f", metrics.heatCapacity));
        heatCoolingRatio.textProperty().bind(format("Cooling Ratio: %.1f%%", metrics.coolingRatio.multiply(100)));
        heatTimeToCool.textProperty().bind(format("Time to Cool: %.1fs", metrics.timeToCool));
    }

    private void setupLayoutView() {
        DynamicSlotDistributor distributor = new DynamicSlotDistributor(model.loadout);

        Region rightArmStrut = new Region();
        rightArmStrut.getStyleClass().add(StyleManager.CSS_CLASS_ARM_STRUT);

        Region leftArmStrut = new Region();
        leftArmStrut.getStyleClass().add(StyleManager.CSS_CLASS_ARM_STRUT);

        Region rightTorsoStrut = new Region();
        rightTorsoStrut.getStyleClass().add(StyleManager.CSS_CLASS_TORSO_STRUT);
        Region leftTorsoStrut = new Region();
        leftTorsoStrut.getStyleClass().add(StyleManager.CSS_CLASS_TORSO_STRUT);

        ObservableList<Node> children = layoutContainer.getChildren();
        VBox rightArmBox = new VBox(rightArmStrut,
                new ComponentPane(xBar, cmdStack, model, Location.RightArm, distributor));
        VBox rightTorsoBox = new VBox(rightTorsoStrut,
                new ComponentPane(xBar, cmdStack, model, Location.RightTorso, distributor),
                new ComponentPane(xBar, cmdStack, model, Location.RightLeg, distributor));
        VBox centralBox = new VBox(new ComponentPane(xBar, cmdStack, model, Location.Head, distributor),
                new ComponentPane(xBar, cmdStack, model, Location.CenterTorso, distributor));
        VBox leftTorsoBox = new VBox(leftTorsoStrut,
                new ComponentPane(xBar, cmdStack, model, Location.LeftTorso, distributor),
                new ComponentPane(xBar, cmdStack, model, Location.LeftLeg, distributor));
        VBox leftArmBox = new VBox(leftArmStrut,
                new ComponentPane(xBar, cmdStack, model, Location.LeftArm, distributor));

        ModulePane modulePane = new ModulePane(xBar, cmdStack, model);
        rightArmBox.getChildren().add(modulePane);

        rightArmBox.getStyleClass().add(StyleManager.CSS_CLASS_LAYOUT_CONTAINER);
        rightTorsoBox.getStyleClass().add(StyleManager.CSS_CLASS_LAYOUT_CONTAINER);
        centralBox.getStyleClass().add(StyleManager.CSS_CLASS_LAYOUT_CONTAINER);
        leftTorsoBox.getStyleClass().add(StyleManager.CSS_CLASS_LAYOUT_CONTAINER);
        leftArmBox.getStyleClass().add(StyleManager.CSS_CLASS_LAYOUT_CONTAINER);

        children.add(rightArmBox);
        children.add(rightTorsoBox);
        children.add(centralBox);
        children.add(leftTorsoBox);
        children.add(leftArmBox);
    }

    /**
     * 
     */
    private void setupMenuBar() {
        menuRedo.setAccelerator(new KeyCodeCombination(KeyCode.Y, KeyCombination.CONTROL_DOWN));
        menuRedo.disableProperty().bind(isNull(cmdStack.nextRedoProperty()));
        menuRedo.textProperty().bind(when(isNull(cmdStack.nextRedoProperty())).then("Redo")
                .otherwise(format("Redo (%s)", cmdStack.nextRedoProperty().asString())));

        menuUndo.setAccelerator(new KeyCodeCombination(KeyCode.Z, KeyCombination.CONTROL_DOWN));
        menuUndo.disableProperty().bind(isNull(cmdStack.nextUndoProperty()));
        menuUndo.textProperty().bind(when(isNull(cmdStack.nextUndoProperty())).then("Undo")
                .otherwise(format("Undo (%s)", cmdStack.nextUndoProperty().asString())));
    }

    private void setupMobilityPanel() {
        mobilityTopSpeed.textProperty().bind(format("Top Speed: %.1f km/h", metrics.topSpeed));
        mobilityTurnSpeed.textProperty().bind(format("Turn Speed: %.1f °/s", metrics.turnSpeed));

        mobilityTorsoPitchSpeed.textProperty().bind(format("Torso (pitch): %.1f °/s", metrics.torsoPitchSpeed));
        mobilityTorsoYawSpeed.textProperty().bind(format("Torso (yaw): %.1f °/s", metrics.torsoYawSpeed));
        mobilityArmPitchSpeed.textProperty().bind(format("Arm (pitch): %.1f °/s", metrics.armPitchSpeed));
        mobilityArmYawSpeed.textProperty().bind(format("Arm (yaw): %.1f °/s", metrics.armYawSpeed));
        mobilityJumpJets.textProperty().bind(format("JumpJets: %d/%d", metrics.jumpJetCount, metrics.jumpJetMax));

        mobilityArcPitchOuter.lengthProperty().bind(metrics.torsoPitch.add(metrics.armPitch).multiply(2.0));
        mobilityArcPitchInner.lengthProperty().bind(metrics.torsoPitch.multiply(2.0));
        mobilityArcYawOuter.lengthProperty().bind(metrics.torsoYaw.add(metrics.armYaw).multiply(2.0));
        mobilityArcYawInner.lengthProperty().bind(metrics.torsoYaw.multiply(2.0));

        mobilityArcPitchOuter.startAngleProperty().bind(mobilityArcPitchOuter.lengthProperty().negate().divide(2));
        mobilityArcPitchInner.startAngleProperty().bind(mobilityArcPitchInner.lengthProperty().negate().divide(2));
        mobilityArcYawOuter.startAngleProperty().bind(mobilityArcYawOuter.lengthProperty().divide(-2).add(90));
        mobilityArcYawInner.startAngleProperty().bind(mobilityArcYawInner.lengthProperty().divide(-2).add(90));
    }

    private void setupModulesList() {
        TreeItem<Object> root = new TreeItem<>();
        root.setExpanded(true);

        for (ModuleSlot slot : ModuleSlot.values()) {
            if (slot == ModuleSlot.HYBRID)
                continue;

            FilterTreeItem<Object> item = new FilterTreeItem<Object>(EquipmentCategory.classify(slot));
            item.setExpanded(true);

            List<PilotModule> modules = PilotModuleDB.lookup(slot);
            modules.sort((aLeft, aRight) -> {
                return aLeft.getName().compareTo(aRight.getName());
            });

            for (PilotModule module : modules) {
                TreeItem<Object> moduleTreeItem = new TreeItem<>(module);
                item.add(moduleTreeItem);
            }
            root.getChildren().add(item);
        }

        TreeTableColumn<Object, String> nameColumn = new TreeTableColumn<>(EQ_COL_NAME);
        nameColumn.setCellValueFactory((aFeatures) -> {
            TreeItem<Object> treeItem = aFeatures.getValue();
            if (null != treeItem && null != treeItem.getValue()) {
                Object objectValue = treeItem.getValue();
                if (objectValue instanceof PilotModule) {
                    return new ReadOnlyStringWrapper(((PilotModule) objectValue).getName());
                }
                return new ReadOnlyStringWrapper(objectValue.toString());
            }
            return new ReadOnlyStringWrapper("");
        });

        moduleList.setRoot(root);
        moduleList.getColumns().clear();
        moduleList.getColumns().add(nameColumn);
        moduleList.setRowFactory(aTree -> new ModuleTableRow(model.loadout, cmdStack, xBar));
        updateModulePredicates();
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

        offensiveAlphaDamage.textProperty().bind(format("A. Dmg: %.1f@%.0fm", metrics.alphaDamage, metrics.alphaRange));
        offensiveAlphaHeat.textProperty().bind(format("A. Heat: %.0f%%",
                metrics.alphaHeat.add(metrics.alphaGhostHeat).divide(metrics.heatCapacity).multiply(100)));
        offensiveAlphaTimeToCool.textProperty()
                .bind(format("A. Cool: %.1fs", metrics.alphaHeat.divide(metrics.heatDissipation)));
        offensiveAlphaGhostHeat.textProperty().bind(format("A. Ghost Heat: %.1f", metrics.alphaGhostHeat));

        offensiveMaxDPS.textProperty().bind(format("Max DPS: %.1f@%.0fm", metrics.maxDPS, metrics.maxDPSRange));
        offensiveSustainedDPS.textProperty()
                .bind(format("Sust. DPS: %.1f@%.0fm", metrics.sustainedDPS, metrics.sustainedDPSRange));
        offensiveBurstDamage.textProperty()
                .bind(format("Burst %.0fs: %.1f@%.0fm", metrics.burstTime, metrics.burstDamage, metrics.burstRange));
        offensiveTimeToOverheat.textProperty().bind(format("A. Overheat: %.1fs", metrics.alphaTimeToOverheat));

        setupWeaponsTable();
    }

    private void setupUpgradesPanel() {
        upgradeArtemis.selectedProperty().bindBidirectional(model.hasArtemis);

        if (!(model.loadout instanceof LoadoutStandard)) {
            Upgrades upgrades = model.loadout.getUpgrades();
            upgradeDoubleHeatSinks.setSelected(upgrades.getHeatSink().isDouble());
            upgradeEndoSteel.setSelected(upgrades.getStructure().getExtraSlots() != 0);
            upgradeFerroFibrous.setSelected(upgrades.getArmor().getExtraSlots() != 0);
            upgradeDoubleHeatSinks.setDisable(true);
            upgradeEndoSteel.setDisable(true);
            upgradeFerroFibrous.setDisable(true);
        }
        else {
            upgradeDoubleHeatSinks.selectedProperty().bindBidirectional(model.hasDoubleHeatSinks);
            upgradeEndoSteel.selectedProperty().bindBidirectional(model.hasEndoSteel);
            upgradeFerroFibrous.selectedProperty().bindBidirectional(model.hasFerroFibrous);

        }
    }

    private void setupWeaponsTable() {
        offensiveWeaponTable.setItems(new WeaponSummaryList(xBar, model.loadout));

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
        try {

            sideStack.pushAndApply(new CmdDistributeArmor(model.loadout, (int) armorWizardAmount.getValue(),
                    armorWizardRatio.getValue(), xBar));
        }
        catch (Exception e) {
            LiSongMechLab.showError(e);
        }
    }

    private void updateEquipmentPredicates() {
        FilterTreeItem<Object> root = (FilterTreeItem<Object>) equipmentList.getRoot();
        for (TreeItem<Object> category : root.getChildren()) {
            FilterTreeItem<Object> filterTreeItem = (FilterTreeItem<Object>) category;
            filterTreeItem.setPredicate(new EquippablePredicate(model.loadout));
        }
        root.setPredicate(aCategory -> {
            return !aCategory.getChildren().isEmpty();
        });
        // Force full refresh of tree, because apparently the observed changes on the children aren't enough.
        equipmentList.setRoot(null);
        equipmentList.setRoot(root);
    }

    private void updateModifiers() {
        modifiersBox.getChildren().clear();
        modifierFormatter.format(model.loadout.getModifiers(), modifiersBox.getChildren());
    }

    private void updateModulePredicates() {
        TreeItem<Object> root = moduleList.getRoot();
        for (TreeItem<Object> category : root.getChildren()) {
            FilterTreeItem<Object> filterTreeItem = (FilterTreeItem<Object>) category;
            filterTreeItem.setPredicate(aObject -> {
                if (aObject.getValue() instanceof WeaponModule) {
                    WeaponModule weaponModule = (WeaponModule) aObject.getValue();
                    boolean affectsAtLeastOne = false;
                    for (Weapon weapon : model.loadout.items(Weapon.class)) {
                        if (weaponModule.affectsWeapon(weapon)) {
                            affectsAtLeastOne = true;
                            break;
                        }
                    }
                    return affectsAtLeastOne;
                }
                return true;
            });
        }
        // Force full refresh of tree, because apparently the observed changes on the children aren't enough.
        moduleList.setRoot(null);
        moduleList.setRoot(root);
    }

}
