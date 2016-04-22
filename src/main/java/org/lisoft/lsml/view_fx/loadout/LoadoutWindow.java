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

import static javafx.beans.binding.Bindings.format;
import static javafx.beans.binding.Bindings.isNull;
import static javafx.beans.binding.Bindings.when;
import static org.lisoft.lsml.view_fx.util.FxControlUtils.bindTogglable;
import static org.lisoft.lsml.view_fx.util.FxControlUtils.loadFxmlControl;

import java.awt.Desktop;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

import org.lisoft.lsml.command.CmdAddToGarage;
import org.lisoft.lsml.command.CmdLoadStock;
import org.lisoft.lsml.command.CmdRename;
import org.lisoft.lsml.command.CmdSetArmorType;
import org.lisoft.lsml.command.CmdSetGuidanceType;
import org.lisoft.lsml.command.CmdSetHeatSinkType;
import org.lisoft.lsml.command.CmdSetMaxArmor;
import org.lisoft.lsml.command.CmdSetStructureType;
import org.lisoft.lsml.command.CmdStripArmor;
import org.lisoft.lsml.command.CmdStripEquipment;
import org.lisoft.lsml.command.CmdStripLoadout;
import org.lisoft.lsml.messages.GarageMessage;
import org.lisoft.lsml.messages.GarageMessageType;
import org.lisoft.lsml.messages.ItemMessage;
import org.lisoft.lsml.messages.LoadoutMessage;
import org.lisoft.lsml.messages.Message;
import org.lisoft.lsml.messages.MessageReceiver;
import org.lisoft.lsml.messages.MessageXBar;
import org.lisoft.lsml.messages.OmniPodMessage;
import org.lisoft.lsml.messages.UpgradesMessage;
import org.lisoft.lsml.model.DynamicSlotDistributor;
import org.lisoft.lsml.model.chassi.Chassis;
import org.lisoft.lsml.model.chassi.Location;
import org.lisoft.lsml.model.datacache.ChassisDB;
import org.lisoft.lsml.model.datacache.ItemDB;
import org.lisoft.lsml.model.datacache.PilotModuleDB;
import org.lisoft.lsml.model.datacache.UpgradeDB;
import org.lisoft.lsml.model.garage.GarageDirectory;
import org.lisoft.lsml.model.item.Faction;
import org.lisoft.lsml.model.item.Item;
import org.lisoft.lsml.model.item.ModuleSlot;
import org.lisoft.lsml.model.loadout.DefaultLoadoutFactory;
import org.lisoft.lsml.model.loadout.Loadout;
import org.lisoft.lsml.model.loadout.LoadoutMetrics;
import org.lisoft.lsml.model.loadout.LoadoutStandard;
import org.lisoft.lsml.model.upgrades.Upgrades;
import org.lisoft.lsml.util.CommandStack;
import org.lisoft.lsml.util.EncodingException;
import org.lisoft.lsml.view_fx.GlobalGarage;
import org.lisoft.lsml.view_fx.LiSongMechLab;
import org.lisoft.lsml.view_fx.controls.FilterTreeItem;
import org.lisoft.lsml.view_fx.loadout.component.ComponentPane;
import org.lisoft.lsml.view_fx.loadout.component.ModulePane;
import org.lisoft.lsml.view_fx.loadout.equipment.EquipmentCategory;
import org.lisoft.lsml.view_fx.loadout.equipment.EquipmentTableCell;
import org.lisoft.lsml.view_fx.loadout.equipment.EquipmentTableRow;
import org.lisoft.lsml.view_fx.loadout.equipment.EquippablePredicate;
import org.lisoft.lsml.view_fx.properties.LoadoutMetricsModelAdaptor;
import org.lisoft.lsml.view_fx.properties.LoadoutModelAdaptor;
import org.lisoft.lsml.view_fx.style.ItemToolTipFormatter;
import org.lisoft.lsml.view_fx.style.StyleManager;
import org.lisoft.lsml.view_fx.style.WindowState;
import org.lisoft.lsml.view_fx.util.FxControlUtils;

import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.ButtonBar.ButtonData;
import javafx.scene.control.ButtonType;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ChoiceDialog;
import javafx.scene.control.Label;
import javafx.scene.control.MenuItem;
import javafx.scene.control.ProgressBar;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TextField;
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
import javafx.stage.Stage;

/**
 * Controller for the loadout window.
 *
 * @author Li Song
 */
public class LoadoutWindow extends StackPane implements MessageReceiver {
    private static final String EQ_COL_MASS = "Mass";
    private static final String EQ_COL_NAME = "Name";
    private static final String EQ_COL_SLOTS = "Slots";
    private static final int UNDO_DEPTH = 128;
    private final WindowState windowState;
    private final CommandStack cmdStack = new CommandStack(UNDO_DEPTH);
    @FXML
    private TreeTableView<Object> equipmentList;
    @FXML
    private ProgressBar generalArmorBar;
    @FXML
    private Label generalArmorLabel;
    @FXML
    private Label generalArmorOverlay;
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
    private TextField titleLabel;
    @FXML
    private Label generalSlotsOverlay;
    private final MessageXBar globalXBar;
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
    @FXML
    private MenuItem menuAddToGarage;
    @FXML
    private MenuItem menuLoadStock;
    @FXML
    private MenuItem menuRedo;
    @FXML
    private MenuItem menuUndo;
    private final LoadoutMetricsModelAdaptor metrics;
    private final LoadoutModelAdaptor model;
    private final Stage stage;
    private final ItemToolTipFormatter toolTipFormatter;
    @FXML
    private CheckBox upgradeArtemis;
    @FXML
    private CheckBox upgradeDoubleHeatSinks;
    @FXML
    private CheckBox upgradeEndoSteel;
    @FXML
    private CheckBox upgradeFerroFibrous;
    private final MessageXBar xBar = new MessageXBar();

    private final GlobalGarage globalGarage = GlobalGarage.instance;

    public LoadoutWindow(MessageXBar aGlobalXBar, Loadout aLoadout, Stage aStage) {
        Objects.requireNonNull(aLoadout);

        loadFxmlControl(this);
        globalXBar = aGlobalXBar;
        globalXBar.attach(this);
        xBar.attach(this);
        model = new LoadoutModelAdaptor(aLoadout, xBar);
        metrics = new LoadoutMetricsModelAdaptor(new LoadoutMetrics(aLoadout, null, xBar), aLoadout, xBar);
        stage = aStage;
        toolTipFormatter = new ItemToolTipFormatter();

        stage.setOnCloseRequest((aWindowEvent) -> {
            if (!closeConfirm()) {
                aWindowEvent.consume();
            }
        });

        titleLabel.setText(aLoadout.getName());
        titleLabel.setOnAction(aEvent -> {
            if (titleLabel.getText().equals(model.loadout.getName())) {
                return;
            }

            final Optional<GarageDirectory<Loadout>> foundDir = globalGarage.getGarage().getLoadoutRoot()
                    .recursiveFind(model.loadout);
            GarageDirectory<Loadout> dir = null;
            if (foundDir.isPresent()) {
                dir = foundDir.get();
            }

            if (LiSongMechLab.safeCommand(this, cmdStack,
                    new CmdRename<>(model.loadout, globalXBar, titleLabel.getText(), dir))) {
                updateStageTitle();
            }
            else {
                titleLabel.setText(model.loadout.getName());
            }
        });
        FxControlUtils.fixTextField(titleLabel);

        closeWeaponLab();

        updateStageTitle();
        setupLayoutView();
        setupEquipmentList();
        setupMenuBar();
        setupUpgradesPane();
        setupGeneralStatsPane();

        infoScrollPane.setContent(new LoadoutInfoPane(xBar, cmdStack, model, metrics));

        windowState = new WindowState(stage, this);
    }

    @FXML
    public void addToGarage() {
        LiSongMechLab.safeCommand(this, cmdStack,
                new CmdAddToGarage<>(globalXBar, globalGarage.getGarage().getLoadoutRoot(), model.loadout));
        menuAddToGarage.setDisable(true);
    }

    @FXML
    public void cloneLoadout() {
        final Loadout clone = DefaultLoadoutFactory.instance.produceClone(model.loadout);
        clone.setName(clone.getName() + " (Clone)");
        LiSongMechLab.openLoadout(globalXBar, clone);
    }

    @FXML
    public void closeWeaponLab() {
        if (getChildren().size() > 1) {
            getChildren().remove(1);
            getChildren().get(0).setDisable(false);
        }
    }

    @FXML
    public void closeWindow() {
        stage.close();
    }

    @FXML
    public void editName() {
        titleLabel.requestFocus();
        titleLabel.selectAll();
    }

    public WindowState getWindowState() {
        return windowState;
    }

    @FXML
    public void loadStock() throws Exception {
        final Chassis chassis = model.loadout.getChassis();
        final Collection<Chassis> variations = ChassisDB.lookupVariations(chassis);

        if (variations.size() == 1) {
            cmdStack.pushAndApply(new CmdLoadStock(chassis, model.loadout, xBar));
        }
        else {
            final ChoiceDialog<Chassis> dialog = new ChoiceDialog<Chassis>(chassis, variations);

            dialog.setTitle("Select stock loadout");
            dialog.setHeaderText("This chassis has several different stock loadout variants.");
            dialog.setContentText("Select a variant:");

            final Optional<Chassis> result = dialog.showAndWait();
            if (result.isPresent()) {
                cmdStack.pushAndApply(new CmdLoadStock(result.get(), model.loadout, xBar));
            }
        }
    }

    @FXML
    public void maxArmor10to1() throws Exception {
        maxArmor(10);
    }

    @FXML
    public void maxArmor3to1() throws Exception {
        maxArmor(3);
    }

    @FXML
    public void maxArmor5to1() throws Exception {
        maxArmor(5);
    }

    @FXML
    public void maxArmorCustom() throws Exception {
        final TextInputDialog dialog = new TextInputDialog();
        dialog.setTitle("Max armor");
        dialog.setHeaderText("Setting max armor with custom ratio");
        dialog.setContentText("Front to back ratio:");

        final Optional<String> result = dialog.showAndWait();
        if (result.isPresent()) {
            final String textRatio = result.get().replace(',', '.');
            final double ratio;
            try {
                ratio = Double.parseDouble(textRatio);
            }
            catch (final NumberFormatException e) {
                final Alert alert = new Alert(AlertType.ERROR);
                alert.setHeaderText("Invalid ratio");
                alert.setHeaderText("Unable to set the max armor");
                alert.setContentText("You must ender a decimal number!");
                alert.show();
                return;
            }
            maxArmor(ratio);
        }
    }

    @FXML
    public void openManual() throws IOException, URISyntaxException {
        Desktop.getDesktop().browse(new URI("https://github.com/lisongmechlab/lsml/wiki"));
    }

    @Override
    public void receive(Message aMsg) {
        final boolean items = aMsg instanceof ItemMessage;
        final boolean upgrades = aMsg instanceof UpgradesMessage;
        final boolean omniPods = aMsg instanceof OmniPodMessage;
        final boolean modules = aMsg instanceof LoadoutMessage;

        if (items || upgrades || omniPods || modules) {
            updateEquipmentPredicates();
        }

        if (aMsg instanceof GarageMessage && aMsg.isForMe(model.loadout)) {
            final GarageMessage garageMessage = (GarageMessage) aMsg;
            if (garageMessage.type == GarageMessageType.RENAMED) {
                titleLabel.setText(model.loadout.getName());
                updateStageTitle();
            }
        }
    }

    @FXML
    public void redo(@SuppressWarnings("unused") ActionEvent event) throws Exception {
        cmdStack.redo();
    }

    @FXML
    public void reportBug() throws IOException, URISyntaxException {
        Desktop.getDesktop().browse(new URI("https://github.com/lisongmechlab/lsml/wiki/Reporting-Issues"));
    }

    @FXML
    public void shareLsmlLink() throws EncodingException {
        LiSongMechLab.shareLsmlLink(model.loadout, this);
    }

    @FXML
    public void shareSmurfy() {
        LiSongMechLab.shareSmurfy(model.loadout, this);
    }

    @FXML
    public void showWeaponLab() {
        if (getChildren().size() < 2) {
            final WeaponLabPane weaponLabPane = new WeaponLabPane(xBar, model.loadout, metrics, () -> {
                closeWeaponLab();
            });
            StyleManager.makeOverlay(weaponLabPane);
            getChildren().add(weaponLabPane);
            getChildren().get(0).setDisable(true);
        }
    }

    @FXML
    public void stripArmor() throws Exception {
        cmdStack.pushAndApply(new CmdStripArmor(model.loadout, xBar));
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
    public void undo(@SuppressWarnings("unused") ActionEvent event) {
        cmdStack.undo();
    }

    @FXML
    public void windowClose() {
        if (closeConfirm()) {
            windowState.windowClose();
        }
    }

    @FXML
    public void windowIconify() {
        windowState.windowIconify();
    }

    @FXML
    public void windowMaximize() {
        windowState.windowMaximize();
    }

    private boolean closeConfirm() {
        if (!globalGarage.getGarage().getLoadoutRoot().recursiveFind(model.loadout).isPresent()) {
            final Alert alert = new Alert(AlertType.CONFIRMATION);
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

    private void maxArmor(double aRatio) throws Exception {
        LiSongMechLab.safeCommand(this, cmdStack, new CmdSetMaxArmor(model.loadout, xBar, aRatio, true));
    }

    private void setupEquipmentList() {
        final Chassis chassis = model.loadout.getChassis();

        final FilterTreeItem<Object> root = new FilterTreeItem<>();
        root.setExpanded(true);

        // Prepare all category roots
        final Map<EquipmentCategory, FilterTreeItem<Object>> categoryRoots = new HashMap<>();
        for (final EquipmentCategory category : EquipmentCategory.values()) {
            final FilterTreeItem<Object> categoryRoot = new FilterTreeItem<>(category);
            categoryRoot.setExpanded(true);
            root.add(categoryRoot);
            categoryRoots.put(category, categoryRoot);
        }

        // Add all items (after filtering for impossible items) to their respective categories
        ItemDB.lookup(Item.class).stream().sorted()
                .filter(aItem -> aItem.getFaction().isCompatible(chassis.getFaction()) && chassis.isAllowed(aItem))
                .forEachOrdered(
                        aItem -> categoryRoots.get(EquipmentCategory.classify(aItem)).add(new TreeItem<>(aItem)));

        // Add all modules
        for (final ModuleSlot slot : ModuleSlot.values()) {
            if (slot == ModuleSlot.HYBRID) {
                continue;
            }
            final FilterTreeItem<Object> categoryRoot = categoryRoots.get(EquipmentCategory.classify(slot));
            PilotModuleDB.lookup(slot).stream().sorted((aLeft, aRight) -> aLeft.getName().compareTo(aRight.getName()))
                    .forEachOrdered(aModule -> categoryRoot.add(new TreeItem<>(aModule)));

        }

        equipmentList.setRowFactory(aParam -> new EquipmentTableRow(model.loadout, cmdStack, xBar));
        equipmentList.setRoot(root);
        updateEquipmentPredicates();

        final TreeTableColumn<Object, String> nameColumn = new TreeTableColumn<>(EQ_COL_NAME);
        nameColumn.setCellValueFactory(new ItemValueFactory(item -> item.getShortName(), true));
        nameColumn.setCellFactory(aColumn -> new EquipmentTableCell(model.loadout, true, toolTipFormatter));
        nameColumn.prefWidthProperty().bind(equipmentList.widthProperty().multiply(0.6));

        final TreeTableColumn<Object, String> slotsColumn = new TreeTableColumn<>(EQ_COL_SLOTS);
        slotsColumn.setCellValueFactory(new ItemValueFactory(item -> Integer.toString(item.getSlots()), false));
        slotsColumn.setCellFactory(aColumn -> new EquipmentTableCell(model.loadout, false, toolTipFormatter));
        slotsColumn.prefWidthProperty().bind(equipmentList.widthProperty().multiply(0.15));

        final TreeTableColumn<Object, String> massColumn = new TreeTableColumn<>(EQ_COL_MASS);
        massColumn.setCellValueFactory(new ItemValueFactory(item -> Double.toString(item.getMass()), false));
        massColumn.setCellFactory(aColumn -> new EquipmentTableCell(model.loadout, false, toolTipFormatter));
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

        final int armorMax = chassis.getArmorMax();
        generalArmorBar.progressProperty().bind(model.statsArmor.divide((double) armorMax));
        generalArmorBar.prefWidthProperty().bind(parent.widthProperty());
        generalArmorLabel.textProperty().bind(format("%d free", model.statsArmorFree));
        generalArmorOverlay.textProperty().bind(format("%d / %d", model.statsArmor, armorMax));

        final int criticalSlotsTotal = chassis.getCriticalSlotsTotal();
        generalSlotsBar.progressProperty().bind(model.statsSlots.divide((double) criticalSlotsTotal));
        generalSlotsBar.prefWidthProperty().bind(parent.widthProperty());
        generalSlotsLabel.textProperty().bind(format("%d free", model.statsSlots.negate().add(criticalSlotsTotal)));
        generalSlotsOverlay.textProperty().bind(format("%d / %d", model.statsSlots, criticalSlotsTotal));
    }

    private void setupLayoutView() {
        final DynamicSlotDistributor distributor = new DynamicSlotDistributor(model.loadout);

        final Region rightArmStrut = new Region();
        rightArmStrut.getStyleClass().add(StyleManager.CLASS_ARM_STRUT);

        final Region leftArmStrut = new Region();
        leftArmStrut.getStyleClass().add(StyleManager.CLASS_ARM_STRUT);

        layoutColumnRightArm.getChildren().setAll(rightArmStrut,
                new ComponentPane(xBar, cmdStack, model, Location.RightArm, distributor, toolTipFormatter),
                new ModulePane(xBar, cmdStack, model));

        layoutColumnRightTorso.getChildren().setAll(
                new ComponentPane(xBar, cmdStack, model, Location.RightTorso, distributor, toolTipFormatter),
                new ComponentPane(xBar, cmdStack, model, Location.RightLeg, distributor, toolTipFormatter));

        layoutColumnCenter.getChildren().setAll(
                new ComponentPane(xBar, cmdStack, model, Location.Head, distributor, toolTipFormatter),
                new ComponentPane(xBar, cmdStack, model, Location.CenterTorso, distributor, toolTipFormatter));

        layoutColumnLeftTorso.getChildren().setAll(
                new ComponentPane(xBar, cmdStack, model, Location.LeftTorso, distributor, toolTipFormatter),
                new ComponentPane(xBar, cmdStack, model, Location.LeftLeg, distributor, toolTipFormatter));

        layoutColumnLeftArm.getChildren().setAll(leftArmStrut,
                new ComponentPane(xBar, cmdStack, model, Location.LeftArm, distributor, toolTipFormatter));
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

        // FIXME: This has problems if this loadout is removed from the garage after
        // the menu bar is setup, then one cannot save the loadout any more.
        // menuAddToGarage.setDisable(garage.getMechs().contains(model.loadout));

        if (ChassisDB.lookupVariations(model.loadout.getChassis()).size() > 1) {
            menuLoadStock.setText(menuLoadStock.getText() + "...");
        }
    }

    private void setupUpgradesPane() {
        final Faction faction = model.loadout.getChassis().getFaction();

        bindTogglable(upgradeArtemis, model.hasArtemis, aNewValue -> LiSongMechLab.safeCommand(this, cmdStack,
                new CmdSetGuidanceType(xBar, model.loadout, UpgradeDB.getGuidance(faction, aNewValue))));

        if (!(model.loadout instanceof LoadoutStandard)) {
            final Upgrades upgrades = model.loadout.getUpgrades();
            upgradeDoubleHeatSinks.setSelected(upgrades.getHeatSink().isDouble());
            upgradeEndoSteel.setSelected(upgrades.getStructure().getExtraSlots() != 0);
            upgradeFerroFibrous.setSelected(upgrades.getArmor().getExtraSlots() != 0);
            upgradeDoubleHeatSinks.setDisable(true);
            upgradeEndoSteel.setDisable(true);
            upgradeFerroFibrous.setDisable(true);
        }
        else {
            final LoadoutStandard lstd = (LoadoutStandard) model.loadout;

            bindTogglable(upgradeDoubleHeatSinks, model.hasDoubleHeatSinks, aNewValue -> LiSongMechLab.safeCommand(this,
                    cmdStack, new CmdSetHeatSinkType(xBar, lstd, UpgradeDB.getHeatSinks(faction, aNewValue))));

            bindTogglable(upgradeEndoSteel, model.hasEndoSteel, aNewValue -> LiSongMechLab.safeCommand(this, cmdStack,
                    new CmdSetStructureType(xBar, lstd, UpgradeDB.getStructure(faction, aNewValue))));

            bindTogglable(upgradeFerroFibrous, model.hasFerroFibrous, aNewValue -> LiSongMechLab.safeCommand(this,
                    cmdStack, new CmdSetArmorType(xBar, lstd, UpgradeDB.getArmor(faction, aNewValue))));
        }
    }

    private void updateEquipmentPredicates() {
        final FilterTreeItem<Object> root = (FilterTreeItem<Object>) equipmentList.getRoot();
        for (final TreeItem<Object> category : root.getChildrenRaw()) {
            final FilterTreeItem<Object> filterTreeItem = (FilterTreeItem<Object>) category;
            filterTreeItem.setPredicate(new EquippablePredicate(model.loadout));
        }

        root.setPredicate(null); // Must set to null first to make it re-filter...
        root.setPredicate(aCategory -> {
            return !aCategory.getChildren().isEmpty();
        });

        // Force full refresh of tree, because apparently the observed changes on the children aren't enough.
        equipmentList.setRoot(null);
        equipmentList.setRoot(root);
    }

    private void updateStageTitle() {
        final Loadout loadout = model.loadout;
        String title = loadout.getName();
        if (!title.contains(loadout.getChassis().getNameShort())) {
            title += " (" + loadout.getChassis().getNameShort() + ")";
        }
        stage.setTitle(title);
    }

}
