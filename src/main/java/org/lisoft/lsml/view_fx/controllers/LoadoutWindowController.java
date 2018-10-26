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
package org.lisoft.lsml.view_fx.controllers;

import static javafx.beans.binding.Bindings.format;
import static javafx.beans.binding.Bindings.isNull;
import static org.lisoft.lsml.view_fx.LiSongMechLab.safeCommand;

import java.awt.Desktop;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import javax.inject.Inject;
import javax.inject.Named;

import org.lisoft.lsml.command.CmdDistributeArmour;
import org.lisoft.lsml.command.CmdGarageAdd;
import org.lisoft.lsml.command.CmdLoadStock;
import org.lisoft.lsml.command.CmdSetArmour;
import org.lisoft.lsml.command.CmdSetArmourType;
import org.lisoft.lsml.command.CmdSetGuidanceType;
import org.lisoft.lsml.command.CmdSetHeatSinkType;
import org.lisoft.lsml.command.CmdSetMaxArmour;
import org.lisoft.lsml.command.CmdSetStructureType;
import org.lisoft.lsml.command.CmdStripArmour;
import org.lisoft.lsml.command.CmdStripEquipment;
import org.lisoft.lsml.command.CmdStripLoadout;
import org.lisoft.lsml.messages.ApplicationMessage;
import org.lisoft.lsml.messages.ArmourMessage;
import org.lisoft.lsml.messages.ArmourMessage.Type;
import org.lisoft.lsml.messages.GarageMessage;
import org.lisoft.lsml.messages.GarageMessageType;
import org.lisoft.lsml.messages.ItemMessage;
import org.lisoft.lsml.messages.LoadoutMessage;
import org.lisoft.lsml.messages.Message;
import org.lisoft.lsml.messages.MessageXBar;
import org.lisoft.lsml.messages.NotificationMessage;
import org.lisoft.lsml.messages.OmniPodMessage;
import org.lisoft.lsml.messages.UpgradesMessage;
import org.lisoft.lsml.model.chassi.ArmourSide;
import org.lisoft.lsml.model.chassi.Chassis;
import org.lisoft.lsml.model.chassi.Location;
import org.lisoft.lsml.model.database.ChassisDB;
import org.lisoft.lsml.model.database.ConsumableDB;
import org.lisoft.lsml.model.database.ItemDB;
import org.lisoft.lsml.model.database.UpgradeDB;
import org.lisoft.lsml.model.item.ConsumableType;
import org.lisoft.lsml.model.item.Item;
import org.lisoft.lsml.model.item.ItemComparator;
import org.lisoft.lsml.model.loadout.ConfiguredComponent;
import org.lisoft.lsml.model.loadout.Loadout;
import org.lisoft.lsml.model.loadout.LoadoutFactory;
import org.lisoft.lsml.model.loadout.LoadoutStandard;
import org.lisoft.lsml.model.upgrades.ArmourUpgrade;
import org.lisoft.lsml.model.upgrades.GuidanceUpgrade;
import org.lisoft.lsml.model.upgrades.HeatSinkUpgrade;
import org.lisoft.lsml.model.upgrades.StructureUpgrade;
import org.lisoft.lsml.model.upgrades.Upgrades;
import org.lisoft.lsml.util.CommandStack;
import org.lisoft.lsml.util.CommandStack.Command;
import org.lisoft.lsml.util.CommandStack.CompositeCommand;
import org.lisoft.lsml.view_fx.GlobalGarage;
import org.lisoft.lsml.view_fx.SensibleTreeColumnResizePolicy;
import org.lisoft.lsml.view_fx.Settings;
import org.lisoft.lsml.view_fx.controllers.loadoutwindow.LoadoutInfoPaneController;
import org.lisoft.lsml.view_fx.controllers.loadoutwindow.LoadoutPaneFactory;
import org.lisoft.lsml.view_fx.controllers.loadoutwindow.WeaponLabPaneController;
import org.lisoft.lsml.view_fx.controls.EquipmentTableCell;
import org.lisoft.lsml.view_fx.controls.EquipmentTableRow;
import org.lisoft.lsml.view_fx.controls.FilterTreeItem;
import org.lisoft.lsml.view_fx.controls.ItemValueFactory;
import org.lisoft.lsml.view_fx.controls.LsmlAlert;
import org.lisoft.lsml.view_fx.controls.NameField;
import org.lisoft.lsml.view_fx.controls.UpgradeCell;
import org.lisoft.lsml.view_fx.properties.LoadoutModelAdaptor;
import org.lisoft.lsml.view_fx.style.ItemToolTipFormatter;
import org.lisoft.lsml.view_fx.style.StyleManager;
import org.lisoft.lsml.view_fx.util.EquipmentCategory;
import org.lisoft.lsml.view_fx.util.EquippablePredicate;

import javafx.animation.FadeTransition;
import javafx.animation.KeyFrame;
import javafx.animation.PauseTransition;
import javafx.animation.SequentialTransition;
import javafx.animation.Timeline;
import javafx.application.Platform;
import javafx.collections.ObservableList;
import javafx.collections.ObservableMap;
import javafx.fxml.FXML;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonBar.ButtonData;
import javafx.scene.control.ButtonType;
import javafx.scene.control.ChoiceDialog;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.MenuItem;
import javafx.scene.control.ProgressBar;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.Slider;
import javafx.scene.control.TextInputDialog;
import javafx.scene.control.TreeItem;
import javafx.scene.control.TreeTableColumn;
import javafx.scene.control.TreeTableView;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyCodeCombination;
import javafx.scene.input.KeyCombination;
import javafx.scene.layout.Pane;
import javafx.scene.layout.Region;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.util.Duration;

/**
 * Controller for the loadout window.
 *
 * @author Li Song
 */
public class LoadoutWindowController extends AbstractFXStageController {
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
        public boolean canCoalesce(Command aOperation) {
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
        public boolean canCoalesce(Command aOperation) {
            return aOperation != this && aOperation instanceof CmdResetManualArmour;
        }

        @Override
        public void undo() {
            super.undo();
            updateArmourWizard();
        }
    }

    private static final KeyCombination CLOSE_WINDOW_KEYCOMBINATION = new KeyCodeCombination(KeyCode.W,
            KeyCombination.SHORTCUT_DOWN);
    private static final String EQ_COL_MASS = "Mass";

    private static final String EQ_COL_NAME = "Name";

    private static final String EQ_COL_SLOTS = "Slots";
    private final Timeline armourUpdateTimeout;

    @FXML
    private Slider armourWizardAmount;
    @FXML
    private Slider armourWizardRatio;
    @FXML
    private Label chassisLabel;
    private final CommandStack cmdStack;
    @FXML
    private Label dhsLabelSlots;
    private boolean disableSliderAction = false;
    @FXML
    private Button editNameButton;
    @FXML
    private TreeTableView<Object> equipmentList;
    @FXML
    private ProgressBar generalArmourBar;
    @FXML
    private Label generalArmourLabel;
    @FXML
    private Label generalArmourOverlay;
    @FXML
    private ProgressBar generalMassBar;
    @FXML
    private Label generalMassLabel;
    @FXML
    private Label generalMassOverlay;
    @FXML
    private ProgressBar generalSlotsBar;
    @FXML
    private Label generalSlotsLabel;
    @FXML
    private Label generalSlotsOverlay;
    private final GlobalGarage globalGarage;
    @FXML
    private ScrollPane infoScrollPane;
    @FXML
    private VBox layoutColumnCenter;
    @FXML
    private VBox layoutColumnLeftArm;
    @FXML
    private VBox layoutColumnLeftTorso;
    @FXML
    private VBox layoutColumnRightArm;
    @FXML
    private VBox layoutColumnRightTorso;
    private final LoadoutFactory loadoutFactory;
    @FXML
    private MenuItem menuAddToGarage;
    @FXML
    private MenuItem menuLoadStock;
    @FXML
    private MenuItem menuRedo;
    @FXML
    private MenuItem menuUndo;
    private final LoadoutModelAdaptor model;
    private final NameField<Loadout> nameField;
    private final LoadoutPaneFactory paneFactory;
    private final CommandStack sideStack = new CommandStack(0);
    private final ItemToolTipFormatter toolTipFormatter;

    @FXML
    private ComboBox<ArmourUpgrade> upgradeArmour;
    @FXML
    private ComboBox<GuidanceUpgrade> upgradeGuidance;
    @FXML
    private ComboBox<HeatSinkUpgrade> upgradeHeatSinks;
    @FXML
    private ComboBox<StructureUpgrade> upgradeStructure;
    @FXML
    private Label warningText;
    private final WeaponLabPaneController weaponLabPaneController;
    private final MessageXBar xBar;

    @Inject
    public LoadoutWindowController(Settings aSettings, @Named("global") MessageXBar aGlobalXBar,
            @Named("local") MessageXBar aLocalXBar, @Named("local") CommandStack aCommandStack,
            GlobalGarage aGlobalGarage, ItemToolTipFormatter aToolTipFormatter, Loadout aLoadout,
            LoadoutFactory aLoadoutFactory, WeaponLabPaneController aWeaponLabPaneController,
            LoadoutInfoPaneController aLoadoutInfoPaneController, LoadoutModelAdaptor aModel,
            LoadoutPaneFactory aPaneFactory) {
        super(aSettings, aGlobalXBar);
        globalGarage = aGlobalGarage;
        cmdStack = aCommandStack;
        loadoutFactory = aLoadoutFactory;
        xBar = aLocalXBar;
        xBar.attach(this);
        model = aModel;
        toolTipFormatter = aToolTipFormatter;
        weaponLabPaneController = aWeaponLabPaneController;
        StyleManager.makeOverlay(weaponLabPaneController.getView());
        paneFactory = aPaneFactory;

        nameField = new NameField<>(cmdStack, aGlobalXBar);
        nameField.changeObject(aLoadout, globalGarage.getGarage().getLoadoutRoot().find(aLoadout).orElse(null));
        nameField.getStyleClass().add(StyleManager.CLASS_H1);

        chassisLabel.setText(model.loadout.getChassis().getName());

        warningText.setVisible(false);

        final Pane containerForNameField = (Pane) editNameButton.getParent();
        final int insertAt = containerForNameField.getChildren().indexOf(editNameButton);
        containerForNameField.getChildren().add(insertAt, nameField);
        nameField.applyCss();

        closeWeaponLab();

        setupLayoutView();
        setupEquipmentList();
        setupMenuBar();
        setupUpgradesPane();
        setupGeneralStatsPane();
        setupArmourWizard();

        armourUpdateTimeout = new Timeline(new KeyFrame(Duration.millis(50), e -> {
            final FilterTreeItem<Object> equipmentRoot = (FilterTreeItem<Object>) equipmentList.getRoot();
            equipmentRoot.updatePredicate();
        }));

        infoScrollPane.setContent(aLoadoutInfoPaneController.getView());
        infoScrollPane.setFitToHeight(true);
        // infoScrollPane.setFitToWidth(true);
    }

    @FXML
    public void addToGarage() {
        if (safeCommand(getRoot(), cmdStack,
                new CmdGarageAdd<>(globalXBar, globalGarage.getDefaultSaveTo(), model.loadout), xBar)) {
            menuAddToGarage.setDisable(true);
        }
    }

    @FXML
    public void armourWizardResetAll() throws Exception {
        cmdStack.pushAndApply(new CmdResetManualArmour());
        updateArmourWizard();
    }

    private void changeUpgradeCmd(Command cmd) {
        if (!safeCommand(getRoot(), cmdStack, cmd, xBar)) {
            // Needed to prevent an index out of bounds exception as this might be called from the
            // click handler setting the upgrade box causing concurrent modifications messing up the
            // internal state of the upgrade box.
            Platform.runLater(() -> updateUpgrades());
        }
    }

    @FXML
    public void changeArmourType() {
        final LoadoutStandard loadout = (LoadoutStandard) model.loadout;
        changeUpgradeCmd(new CmdSetArmourType(xBar, loadout, upgradeArmour.getValue()));
    }

    @FXML
    public void changeGuidanceType() {
        changeUpgradeCmd(new CmdSetGuidanceType(xBar, model.loadout, upgradeGuidance.getValue()));
    }

    @FXML
    public void changeHeatSinkType() {
        final LoadoutStandard loadout = (LoadoutStandard) model.loadout;
        changeUpgradeCmd(new CmdSetHeatSinkType(xBar, loadout, upgradeHeatSinks.getValue()));
    }

    @FXML
    public void changeStructureType() {
        final LoadoutStandard loadout = (LoadoutStandard) model.loadout;
        changeUpgradeCmd(new CmdSetStructureType(xBar, loadout, upgradeStructure.getValue()));

    }

    @FXML
    public void cloneLoadout() {
        final Loadout clone = loadoutFactory.produceClone(model.loadout);
        clone.setName(clone.getName() + " (Clone)");
        globalXBar.post(new ApplicationMessage(clone, ApplicationMessage.Type.OPEN_LOADOUT, root));
    }

    @FXML
    public void closeWeaponLab() {
        weaponLabPaneController.closeWeaponLab();
    }

    @FXML
    public void editName() {
        nameField.startEdit();
    }

    @FXML
    public void loadStock() throws Exception {
        final Chassis chassis = model.loadout.getChassis();
        final Collection<Chassis> variations = ChassisDB.lookupVariations(chassis);

        if (variations.size() == 1) {
            safeCommand(getRoot(), cmdStack, new CmdLoadStock(chassis, model.loadout, xBar), xBar);
        }
        else {
            final ChoiceDialog<Chassis> dialog = new ChoiceDialog<>(chassis, variations);

            // FIXME: Style this
            dialog.setTitle("Select stock loadout");
            dialog.setHeaderText("This chassis has several different stock loadout variants.");
            dialog.setContentText("Select a variant:");

            final Optional<Chassis> result = dialog.showAndWait();
            if (result.isPresent()) {
                safeCommand(getRoot(), cmdStack, new CmdLoadStock(result.get(), model.loadout, xBar), xBar);
            }
        }
    }

    @FXML
    public void maxArmour10to1() throws Exception {
        maxArmour(10);
    }

    @FXML
    public void maxArmour3to1() throws Exception {
        maxArmour(3);
    }

    @FXML
    public void maxArmour5to1() throws Exception {
        maxArmour(5);
    }

    @FXML
    public void maxArmourCustom() throws Exception {
        final TextInputDialog dialog = new TextInputDialog();
        dialog.setTitle("Max armour");
        dialog.setHeaderText("Setting max armour with custom ratio");
        dialog.setContentText("Front to back ratio:");
        // FIXME: Style this

        final Optional<String> result = dialog.showAndWait();
        if (result.isPresent()) {
            final String textRatio = result.get().replace(',', '.');
            final double ratio;
            try {
                ratio = Double.parseDouble(textRatio);
            }
            catch (final NumberFormatException e) {
                final LsmlAlert alert = new LsmlAlert(root, AlertType.ERROR);
                alert.setHeaderText("Invalid ratio");
                alert.setHeaderText("Unable to set the max armour");
                alert.setContentText("You must ender a decimal number!");
                alert.show();
                return;
            }
            maxArmour(ratio);
        }
    }

    @FXML
    public void openManual() throws IOException, URISyntaxException {
        Desktop.getDesktop().browse(new URI("https://github.com/lisongmechlab/lsml/wiki"));
    }

    @Override
    public void receive(Message aMsg) {
        super.receive(aMsg);

        final boolean items = aMsg instanceof ItemMessage;
        final boolean upgrades = aMsg instanceof UpgradesMessage;
        final boolean omniPods = aMsg instanceof OmniPodMessage;
        final boolean modules = aMsg instanceof LoadoutMessage;
        final boolean armour = aMsg instanceof ArmourMessage;
        final boolean autoArmourUpdate = aMsg instanceof ArmourMessage
                && ((ArmourMessage) aMsg).type == Type.ARMOUR_DISTRIBUTION_UPDATE_REQUEST;

        if (armour) {
            // Cancel previous update, and start a new one.
            armourUpdateTimeout.stop();
            armourUpdateTimeout.play();
        }

        if (upgrades) {
            updateUpgrades();
        }

        if (items || upgrades || omniPods || modules) {
            final FilterTreeItem<Object> equipmentRoot = (FilterTreeItem<Object>) equipmentList.getRoot();
            equipmentRoot.updatePredicate();
        }

        if (aMsg instanceof GarageMessage && aMsg.isForMe(model.loadout)) {
            final GarageMessage<?> garageMessage = (GarageMessage<?>) aMsg;
            if (garageMessage.type == GarageMessageType.RENAMED) {
                nameField.setText(model.loadout.getName());
            }
        }

        if (aMsg instanceof NotificationMessage) {
            final NotificationMessage msg = (NotificationMessage) aMsg;
            warningText.setText(msg.severity + ": " + msg.message);
            warningText.setVisible(true);

            final String colour;
            switch (msg.severity) {
                case ERROR:
                    colour = StyleManager.COLOUR_TEXT_ERROR;
                    break;
                case WARNING:
                    colour = StyleManager.COLOUR_TEXT_WARNING;
                    break;
                case NOTICE:
                    colour = StyleManager.COLOUR_TEXT_NOTICE;
                    break;
                default:
                    throw new IllegalArgumentException("Unknown enum value: " + msg.severity);
            }

            warningText.setStyle("-fx-text-fill: " + colour + ";-fx-color: " + colour);

            final FadeTransition blinkIn = new FadeTransition(Duration.millis(400), warningText);
            blinkIn.setFromValue(0.0);
            blinkIn.setToValue(1.0);
            blinkIn.setCycleCount(5);
            blinkIn.setAutoReverse(true);

            final PauseTransition pause = new PauseTransition(Duration.seconds(5));

            final FadeTransition fadeOut = new FadeTransition(Duration.seconds(3), warningText);
            fadeOut.setFromValue(1.0);
            fadeOut.setToValue(0.0);

            final SequentialTransition st = new SequentialTransition(blinkIn, pause, fadeOut);

            st.play();
        }

        if (upgrades || items || autoArmourUpdate) {
            Platform.runLater(() -> updateArmourWizard());
        }
    }

    @FXML
    public void redo() throws Exception {
        cmdStack.redo();
    }

    @FXML
    public void reportBug() throws IOException, URISyntaxException {
        Desktop.getDesktop().browse(new URI("https://github.com/lisongmechlab/lsml/wiki/Reporting-Issues"));
    }

    @FXML
    public void shareMWOLink() {
        globalXBar.post(new ApplicationMessage(model.loadout, ApplicationMessage.Type.SHARE_MWO, root));
    }

    @FXML
    public void shareLsmlLink() {
        globalXBar.post(new ApplicationMessage(model.loadout, ApplicationMessage.Type.SHARE_LSML, root));
    }

    @FXML
    public void shareSmurfy() {
        globalXBar.post(new ApplicationMessage(model.loadout, ApplicationMessage.Type.SHARE_SMURFY, root));
    }

    @FXML
    public void showWeaponLab() {
        Platform.runLater(() -> {
            weaponLabPaneController.open();
            openOverlay(weaponLabPaneController, false);
            getRoot().getChildren().get(0).setDisable(true);
        });
    }

    @FXML
    public void stripArmour() throws Exception {
        cmdStack.pushAndApply(new CmdStripArmour(model.loadout, xBar));
    }

    @FXML
    public void stripEquipment() throws Exception {
        cmdStack.pushAndApply(new CmdStripEquipment(model.loadout, xBar));
    }

    @FXML
    public void stripEverything() throws Exception {
        cmdStack.pushAndApply(new CmdStripLoadout(xBar, model.loadout));
    }

    @FXML
    public void undo() {
        cmdStack.undo();
    }

    @Override
    protected void onShow(LSMLStage aStage) {

        aStage.titleProperty().bind(nameField.textProperty());
        final ObservableMap<KeyCombination, Runnable> accelerators = aStage.getScene().getAccelerators();
        accelerators.put(CLOSE_WINDOW_KEYCOMBINATION, () -> {
            if (isOverlayOpen(weaponLabPaneController)) {
                weaponLabPaneController.closeWeaponLab();
            }
            else {
                windowClose();
            }
        });

        aStage.setOnCloseRequest((aWindowEvent) -> {
            if (!closeConfirm()) {
                aWindowEvent.consume();
            }
        });

        nameField.textProperty().addListener((aObs, aOld, aNew) -> {
            final Loadout loadout = model.loadout;
            String title = aNew;
            if (!title.contains(loadout.getChassis().getShortName())) {
                title += " (" + loadout.getChassis().getShortName() + ")";
            }
        });
    }

    private boolean closeConfirm() {
        if (!globalGarage.getGarage().getLoadoutRoot().find(model.loadout).isPresent()) {
            final LsmlAlert alert = new LsmlAlert(root, AlertType.CONFIRMATION);
            alert.setTitle("Add to Garage?");
            alert.setContentText("The loadout is not saved in your garage.");
            final ButtonType add = new ButtonType("Save to garage");
            final ButtonType discard = new ButtonType("Discard");
            final ButtonType cancel = new ButtonType("Cancel", ButtonData.CANCEL_CLOSE);
            alert.getButtonTypes().setAll(add, discard, cancel);
            final Optional<ButtonType> result = alert.showAndWait();
            if (result.isPresent()) {
                if (add == result.get()) {
                    addToGarage();
                    return true;
                }
                else if (discard == result.get()) {
                    return true;
                }
                else {
                    return false;
                }
            }
        }
        return true;
    }

    private StackPane getRoot() {
        return (StackPane) root;
    }

    private void maxArmour(double aRatio) throws Exception {
        safeCommand(getRoot(), cmdStack, new CmdSetMaxArmour(model.loadout, xBar, aRatio, true), xBar);
    }

    private void setupArmourWizard() {
        armourWizardAmount.setMax(model.loadout.getChassis().getArmourMax());
        armourWizardAmount.setValue(model.loadout.getArmour());
        armourWizardAmount.valueProperty().addListener((aObservable, aOld, aNew) -> {
            if (disableSliderAction) {
                return;
            }
            safeCommand(root, cmdStack, new CmdArmourSlider(armourWizardAmount, aOld.doubleValue()), xBar);
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
            safeCommand(root, cmdStack, new CmdArmourSlider(armourWizardRatio, aOld.doubleValue()), xBar);
        });
    }

    private void setupEquipmentList() {
        final boolean pgiMode = settings.getBoolean(Settings.UI_PGI_COMPATIBILITY).getValue();
        final Chassis chassis = model.loadout.getChassis();

        final FilterTreeItem<Object> equipmentRoot = new FilterTreeItem<>();
        equipmentRoot.setExpanded(true);

        // Prepare all category roots
        final Map<EquipmentCategory, FilterTreeItem<Object>> categoryRoots = new HashMap<>();
        for (final EquipmentCategory category : pgiMode ? EquipmentCategory.ORDER_PGI : EquipmentCategory.ORDER_LSML) {
            final FilterTreeItem<Object> categoryRoot = new FilterTreeItem<>(category);
            if (category == EquipmentCategory.LE_ENGINE || category == EquipmentCategory.STD_ENGINE
                    || category == EquipmentCategory.XL_ENGINE) {
                categoryRoot.setExpanded(false);
            }
            else {
                categoryRoot.setExpanded(true);
            }
            equipmentRoot.add(categoryRoot);
            categoryRoots.put(category, categoryRoot);
        }
        // Add all items (after filtering for impossible items) to their respective categories
        ItemDB.lookup(Item.class).stream().sorted(new ItemComparator(pgiMode))
        .filter(aItem -> aItem.getFaction().isCompatible(chassis.getFaction()) && chassis.isAllowed(aItem))
        .forEachOrdered(
                aItem -> categoryRoots.get(EquipmentCategory.classify(aItem)).add(new TreeItem<>(aItem)));

        // Add all modules
        for (final ConsumableType type : ConsumableType.values()) {
            final FilterTreeItem<Object> categoryRoot = categoryRoots.get(EquipmentCategory.classify(type));
            ConsumableDB.lookup(type).stream().sorted((aLeft, aRight) -> aLeft.getName().compareTo(aRight.getName()))
            .forEachOrdered(aModule -> categoryRoot.add(new TreeItem<>(aModule)));

        }

        equipmentList.setRowFactory(
                aParam -> new EquipmentTableRow(model.loadout, cmdStack, xBar, loadoutFactory, settings));
        equipmentList.setRoot(equipmentRoot);
        equipmentList.setColumnResizePolicy(new SensibleTreeColumnResizePolicy());
        equipmentRoot.setPredicateRecursively(new EquippablePredicate(model.loadout));

        final TreeTableColumn<Object, String> nameColumn = new TreeTableColumn<>(EQ_COL_NAME);
        nameColumn.setCellValueFactory(new ItemValueFactory(item -> item.getShortName(), true));
        nameColumn.setCellFactory(aColumn -> new EquipmentTableCell(settings, model.loadout, true, toolTipFormatter));
        nameColumn.prefWidthProperty().bind(equipmentList.widthProperty().multiply(0.6));

        final TreeTableColumn<Object, String> slotsColumn = new TreeTableColumn<>(EQ_COL_SLOTS);
        slotsColumn.setCellValueFactory(new ItemValueFactory(item -> Integer.toString(item.getSlots()), false));
        slotsColumn.setCellFactory(aColumn -> new EquipmentTableCell(settings, model.loadout, false, toolTipFormatter));
        slotsColumn.prefWidthProperty().bind(equipmentList.widthProperty().multiply(0.15));

        final TreeTableColumn<Object, String> massColumn = new TreeTableColumn<>(EQ_COL_MASS);
        massColumn.setCellValueFactory(new ItemValueFactory(item -> Double.toString(item.getMass()), false));
        massColumn.setCellFactory(aColumn -> new EquipmentTableCell(settings, model.loadout, false, toolTipFormatter));
        massColumn.prefWidthProperty().bind(equipmentList.widthProperty().multiply(0.15));

        final ObservableList<TreeTableColumn<Object, ?>> columns = equipmentList.getColumns();
        columns.clear();
        columns.add(nameColumn);
        columns.add(slotsColumn);
        columns.add(massColumn);
    }

    private void setupGeneralStatsPane() {
        final Chassis chassis = model.loadout.getChassis();
        final int massMax = chassis.getMassMax();

        final Pane parent = (Pane) generalMassBar.getParent();
        generalMassBar.progressProperty().bind(model.statsMass.divide(massMax));
        generalMassBar.prefWidthProperty().bind(parent.widthProperty());
        generalMassLabel.textProperty().bind(format("%.2f free", model.statsFreeMass));
        generalMassOverlay.textProperty().bind(format("%.2f / %d", model.statsMass, massMax));

        final int armourMax = chassis.getArmourMax();
        generalArmourBar.progressProperty().bind(model.statsArmour.divide((double) armourMax));
        generalArmourBar.prefWidthProperty().bind(parent.widthProperty());
        generalArmourLabel.textProperty().bind(format("%d free", model.statsArmourFree));
        generalArmourOverlay.textProperty().bind(format("%d / %d", model.statsArmour, armourMax));

        final int criticalSlotsTotal = chassis.getSlotsTotal();
        generalSlotsBar.progressProperty().bind(model.statsSlots.divide((double) criticalSlotsTotal));
        generalSlotsBar.prefWidthProperty().bind(parent.widthProperty());
        generalSlotsLabel.textProperty().bind(format("%d free", model.statsSlots.negate().add(criticalSlotsTotal)));
        generalSlotsOverlay.textProperty().bind(format("%d / %d", model.statsSlots, criticalSlotsTotal));
    }

    private void setupLayoutView() {
        final Region rightArmStrut = new Region();
        rightArmStrut.getStyleClass().add(StyleManager.CLASS_ARM_STRUT);

        final Region leftArmStrut = new Region();
        leftArmStrut.getStyleClass().add(StyleManager.CLASS_ARM_STRUT);

        layoutColumnRightArm.getChildren().setAll(rightArmStrut, paneFactory.component(Location.RightArm));

        layoutColumnRightTorso.getChildren().setAll(paneFactory.component(Location.RightTorso),
                paneFactory.component(Location.RightLeg));

        layoutColumnCenter.getChildren().setAll(paneFactory.component(Location.Head),
                paneFactory.component(Location.CenterTorso));

        layoutColumnLeftTorso.getChildren().setAll(paneFactory.component(Location.LeftTorso),
                paneFactory.component(Location.LeftLeg));

        layoutColumnLeftArm.getChildren().setAll(leftArmStrut, paneFactory.component(Location.LeftArm),
                paneFactory.modulePane());
    }

    /**
     *
     */
    private void setupMenuBar() {
        menuRedo.setAccelerator(MainWindowController.REDO_KEYCOMBINATION);
        menuRedo.disableProperty().bind(isNull(cmdStack.nextRedoProperty()));
        cmdStack.nextRedoProperty().addListener((aObs, aOld, aNew) -> {
            if (aNew == null) {
                menuRedo.setText("Redo");
            }
            else {
                menuRedo.setText("Redo (" + aNew.describe() + ")");
            }
        });

        menuUndo.setAccelerator(MainWindowController.UNDO_KEYCOMBINATION);
        menuUndo.disableProperty().bind(isNull(cmdStack.nextUndoProperty()));
        cmdStack.nextUndoProperty().addListener((aObs, aOld, aNew) -> {
            if (aNew == null) {
                menuUndo.setText("Undo");
            }
            else {
                menuUndo.setText("Undo (" + aNew.describe() + ")");
            }
        });

        // FIXME: This has problems if this loadout is removed from the garage
        // after
        // the menu bar is setup, then one cannot save the loadout any more.
        // menuAddToGarage.setDisable(garage.getMechs().contains(model.loadout));

        if (ChassisDB.lookupVariations(model.loadout.getChassis()).size() > 1) {
            menuLoadStock.setText(menuLoadStock.getText() + "...");
        }
    }

    private void setupUpgradesPane() {
        final Chassis chassis = model.loadout.getChassis();
        UpgradeDB.streamCompatible(chassis, ArmourUpgrade.class)
        .collect(Collectors.toCollection(() -> upgradeArmour.getItems()));
        UpgradeDB.streamCompatible(chassis, StructureUpgrade.class)
        .collect(Collectors.toCollection(() -> upgradeStructure.getItems()));
        UpgradeDB.streamCompatible(chassis, HeatSinkUpgrade.class)
        .collect(Collectors.toCollection(() -> upgradeHeatSinks.getItems()));
        UpgradeDB.streamCompatible(chassis, GuidanceUpgrade.class)
        .collect(Collectors.toCollection(() -> upgradeGuidance.getItems()));

        updateUpgrades();

        if (upgradeArmour.getItems().size() == 1) {
            upgradeArmour.setDisable(true);
        }

        if (upgradeStructure.getItems().size() == 1) {
            upgradeStructure.setDisable(true);
        }

        if (upgradeHeatSinks.getItems().size() == 1) {
            upgradeHeatSinks.setDisable(true);
        }

        if (upgradeGuidance.getItems().size() == 1) {
            upgradeGuidance.setDisable(true);
        }

        upgradeArmour.setCellFactory(aListView -> new UpgradeCell<>(xBar, model.loadout));
        upgradeStructure.setCellFactory(aListView -> new UpgradeCell<>(xBar, model.loadout));
        upgradeHeatSinks.setCellFactory(aListView -> new UpgradeCell<>(xBar, model.loadout));
        upgradeGuidance.setCellFactory(aListView -> new UpgradeCell<>(xBar, model.loadout));
    }

    private void updateUpgrades() {
        final Upgrades upgrades = model.loadout.getUpgrades();
        upgradeArmour.getSelectionModel().select(upgrades.getArmour());
        upgradeStructure.getSelectionModel().select(upgrades.getStructure());
        upgradeHeatSinks.getSelectionModel().select(upgrades.getHeatSink());
        upgradeGuidance.getSelectionModel().select(upgrades.getGuidance());
    }

    private void updateArmourWizard() {
        safeCommand(root, sideStack, new CmdDistributeArmour(model.loadout, (int) armourWizardAmount.getValue(),
                armourWizardRatio.getValue(), xBar), xBar);
    }
}
