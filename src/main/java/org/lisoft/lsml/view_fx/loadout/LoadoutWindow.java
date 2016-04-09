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
import org.lisoft.lsml.messages.ItemMessage;
import org.lisoft.lsml.messages.LoadoutMessage;
import org.lisoft.lsml.messages.Message;
import org.lisoft.lsml.messages.MessageReceiver;
import org.lisoft.lsml.messages.MessageXBar;
import org.lisoft.lsml.messages.OmniPodMessage;
import org.lisoft.lsml.messages.UpgradesMessage;
import org.lisoft.lsml.model.DynamicSlotDistributor;
import org.lisoft.lsml.model.NamedObject;
import org.lisoft.lsml.model.chassi.Chassis;
import org.lisoft.lsml.model.chassi.Location;
import org.lisoft.lsml.model.datacache.ChassisDB;
import org.lisoft.lsml.model.datacache.ItemDB;
import org.lisoft.lsml.model.datacache.PilotModuleDB;
import org.lisoft.lsml.model.datacache.UpgradeDB;
import org.lisoft.lsml.model.export.Base64LoadoutCoder;
import org.lisoft.lsml.model.export.SmurfyImportExport;
import org.lisoft.lsml.model.garage.GarageDirectory;
import org.lisoft.lsml.model.item.Faction;
import org.lisoft.lsml.model.item.Item;
import org.lisoft.lsml.model.item.ModuleSlot;
import org.lisoft.lsml.model.loadout.Loadout;
import org.lisoft.lsml.model.loadout.LoadoutMetrics;
import org.lisoft.lsml.model.loadout.LoadoutStandard;
import org.lisoft.lsml.model.upgrades.Upgrades;
import org.lisoft.lsml.util.CommandStack;
import org.lisoft.lsml.util.EncodingException;
import org.lisoft.lsml.view_fx.DefaultLoadoutErrorReporter;
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
import org.lisoft.lsml.view_fx.style.WindowDecoration;
import org.lisoft.lsml.view_fx.util.FxmlHelpers;

import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.ButtonBar.ButtonData;
import javafx.scene.control.ButtonType;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ChoiceDialog;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.Hyperlink;
import javafx.scene.control.Label;
import javafx.scene.control.MenuItem;
import javafx.scene.control.ProgressBar;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TextInputDialog;
import javafx.scene.control.TreeItem;
import javafx.scene.control.TreeTableColumn;
import javafx.scene.control.TreeTableView;
import javafx.scene.input.Clipboard;
import javafx.scene.input.ClipboardContent;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyCodeCombination;
import javafx.scene.input.KeyCombination;
import javafx.scene.layout.BorderPane;
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
    private final WindowDecoration windowDecoration;
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
    private final Base64LoadoutCoder loadoutCoder;
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
    @FXML
    private BorderPane overlayPane;
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

    public LoadoutWindow(MessageXBar aGlobalXBar, Loadout aLoadout, Stage aStage, Base64LoadoutCoder aLoadoutCoder) {
        Objects.requireNonNull(aLoadout);

        FxmlHelpers.loadFxmlControl(this);
        loadoutCoder = aLoadoutCoder;
        globalXBar = aGlobalXBar;
        globalXBar.attach(this);
        xBar.attach(this);
        model = new LoadoutModelAdaptor(aLoadout, xBar);
        metrics = new LoadoutMetricsModelAdaptor(new LoadoutMetrics(aLoadout, null, xBar), aLoadout, xBar);
        stage = aStage;
        toolTipFormatter = new ItemToolTipFormatter();

        stage.setOnCloseRequest((aWindowEvent) -> {
            if (!globalGarage.getGarage().getLoadoutRoot().recursiveFind(model.loadout).isPresent()) {
                Alert alert = new Alert(AlertType.CONFIRMATION);
                alert.setTitle("Add to Garage?");
                alert.setContentText("The loadout is not saved in your garage.");
                ButtonType add = new ButtonType("Save to garage");
                ButtonType discard = new ButtonType("Discard");
                ButtonType cancel = new ButtonType("Cancel", ButtonData.CANCEL_CLOSE);
                alert.getButtonTypes().setAll(add, discard, cancel);
                Optional<ButtonType> result = alert.showAndWait();
                if (result.isPresent()) {
                    if (add == result.get()) {
                        addToGarage();
                        aWindowEvent.consume();
                    }
                    else if (discard == result.get()) {
                        // no-op
                    }
                    else {
                        aWindowEvent.consume();
                    }
                }
            }
        });

        closeWeaponLab();

        updateTitle();
        setupLayoutView();
        setupEquipmentList();
        setupMenuBar();
        setupUpgradesPane();
        setupGeneralStatsPane();

        infoScrollPane.setContent(new LoadoutInfoPane(xBar, cmdStack, model, metrics));

        windowDecoration = new WindowDecoration(stage, this);
    }

    @FXML
    public void windowClose() {
        windowDecoration.windowClose();
    }

    @FXML
    public void windowIconify() {
        windowDecoration.windowIconify();
    }

    @FXML
    public void windowMaximize() {
        windowDecoration.windowMaximize();
    }

    @FXML
    public void addToGarage() {
        LiSongMechLab.safeCommand(this, cmdStack,
                new CmdAddToGarage<>(globalXBar, globalGarage.getGarage().getLoadoutRoot(), model.loadout));
        menuAddToGarage.setDisable(true);
    }

    @FXML
    public void closeWeaponLab() {
        if (getChildren().size() > 1) {
            getChildren().remove(overlayPane);
            getChildren().get(0).setDisable(false);
        }
    }

    @FXML
    public void closeWindow() {
        stage.close();
    }

    @FXML
    public void loadStock() throws Exception {
        Chassis chassis = model.loadout.getChassis();
        Collection<Chassis> variations = ChassisDB.lookupVariations(chassis);

        if (variations.size() == 1) {
            cmdStack.pushAndApply(new CmdLoadStock(chassis, model.loadout, xBar));
        }
        else {
            ChoiceDialog<Chassis> dialog = new ChoiceDialog<Chassis>(chassis, variations);

            dialog.setTitle("Select stock loadout");
            dialog.setHeaderText("This chassis has several different stock loadout variants.");
            dialog.setContentText("Select a variant:");

            Optional<Chassis> result = dialog.showAndWait();
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
        TextInputDialog dialog = new TextInputDialog();
        dialog.setTitle("Max armor");
        dialog.setHeaderText("Setting max armor with custom ratio");
        dialog.setContentText("Front to back ratio:");

        Optional<String> result = dialog.showAndWait();
        if (result.isPresent()) {
            String textRatio = result.get().replace(',', '.');
            final double ratio;
            try {
                ratio = Double.parseDouble(textRatio);
            }
            catch (NumberFormatException e) {
                Alert alert = new Alert(AlertType.ERROR);
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
        boolean items = aMsg instanceof ItemMessage;
        boolean upgrades = aMsg instanceof UpgradesMessage;
        boolean omniPods = aMsg instanceof OmniPodMessage;
        boolean modules = aMsg instanceof LoadoutMessage;

        if (items || upgrades || omniPods || modules) {
            updateEquipmentPredicates();
        }
    }

    @FXML
    public void redo(@SuppressWarnings("unused") ActionEvent event) throws Exception {
        cmdStack.redo();
    }

    @FXML
    public void renameLoadout() {
        TextInputDialog dialog = new TextInputDialog(model.loadout.getName());
        dialog.setTitle("Renaming Loadout");
        dialog.setHeaderText("Renaming Loadout");
        dialog.setContentText("Please enter the new name:");

        dialog.showAndWait().ifPresent((aName) -> {

            Optional<GarageDirectory<Loadout>> foundDir = globalGarage.getGarage().getLoadoutRoot()
                    .recursiveFind(model.loadout);
            Optional<GarageDirectory<? extends NamedObject>> dir = Optional.empty();
            if (foundDir.isPresent()) {
                GarageDirectory<? extends NamedObject> nakedDir = foundDir.get();
                dir = Optional.of(nakedDir);
            }

            if (LiSongMechLab.safeCommand(this, cmdStack, new CmdRename<>(model.loadout, xBar, aName, dir))) {
                // TODO: The message needs to be passed to the garage window too so that it updates.
                updateTitle();
            }
        });
    }

    @FXML
    public void reportBug() throws IOException, URISyntaxException {
        Desktop.getDesktop().browse(new URI("https://github.com/lisongmechlab/lsml/wiki/Reporting-Issues"));
    }

    @FXML
    public void shareLsmlLink() throws EncodingException {
        String trampolineLink = loadoutCoder.encodeHttpTrampoline(model.loadout);

        showLink("LSML Export Complete", "The loadout " + model.loadout.getName() + " has been encoded to a LSML link.",
                trampolineLink);
    }

    @FXML
    public void shareSmurfy() {
        // FIXME: Use DI to inject this.
        SmurfyImportExport export = new SmurfyImportExport(loadoutCoder, DefaultLoadoutErrorReporter.instance);
        try {
            String url = export.sendLoadout(model.loadout);
            showLink("Smurfy Export Complete",
                    "The loadout " + model.loadout.getName() + " has been uploaded to smurfy.", url);
        }
        catch (IOException e) {
            LiSongMechLab.showError(this, e);
        }
    }

    @FXML
    public void showWeaponLab() {
        if (getChildren().size() < 2) {
            WeaponLabPane weaponLabPane = new WeaponLabPane(xBar, model.loadout, metrics, () -> {
                closeWeaponLab();
            });
            overlayPane.setCenter(weaponLabPane);
            getChildren().add(overlayPane);
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

    private void maxArmor(double aRatio) throws Exception {
        LiSongMechLab.safeCommand(this, cmdStack, new CmdSetMaxArmor(model.loadout, xBar, aRatio, true));
    }

    private void setupEquipmentList() {
        final Chassis chassis = model.loadout.getChassis();

        FilterTreeItem<Object> root = new FilterTreeItem<>();
        root.setExpanded(true);

        // Prepare all category roots
        Map<EquipmentCategory, FilterTreeItem<Object>> categoryRoots = new HashMap<>();
        for (EquipmentCategory category : EquipmentCategory.values()) {
            FilterTreeItem<Object> categoryRoot = new FilterTreeItem<>(category);
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
        for (ModuleSlot slot : ModuleSlot.values()) {
            if (slot == ModuleSlot.HYBRID)
                continue;
            FilterTreeItem<Object> categoryRoot = categoryRoots.get(EquipmentCategory.classify(slot));
            PilotModuleDB.lookup(slot).stream().sorted((aLeft, aRight) -> aLeft.getName().compareTo(aRight.getName()))
                    .forEachOrdered(aModule -> categoryRoot.add(new TreeItem<>(aModule)));

        }

        equipmentList.setRowFactory(aParam -> new EquipmentTableRow(model.loadout, cmdStack, xBar));
        equipmentList.setRoot(root);
        updateEquipmentPredicates();

        TreeTableColumn<Object, String> nameColumn = new TreeTableColumn<>(EQ_COL_NAME);
        nameColumn.setCellValueFactory(new ItemValueFactory(item -> item.getShortName(), true));
        nameColumn.setCellFactory(aColumn -> new EquipmentTableCell(model.loadout, true, toolTipFormatter));
        nameColumn.prefWidthProperty().bind(equipmentList.widthProperty().multiply(0.6));

        TreeTableColumn<Object, String> slotsColumn = new TreeTableColumn<>(EQ_COL_SLOTS);
        slotsColumn.setCellValueFactory(new ItemValueFactory(item -> Integer.toString(item.getSlots()), false));
        slotsColumn.setCellFactory(aColumn -> new EquipmentTableCell(model.loadout, false, toolTipFormatter));
        slotsColumn.prefWidthProperty().bind(equipmentList.widthProperty().multiply(0.15));

        TreeTableColumn<Object, String> massColumn = new TreeTableColumn<>(EQ_COL_MASS);
        massColumn.setCellValueFactory(new ItemValueFactory(item -> Double.toString(item.getMass()), false));
        massColumn.setCellFactory(aColumn -> new EquipmentTableCell(model.loadout, false, toolTipFormatter));
        massColumn.prefWidthProperty().bind(equipmentList.widthProperty().multiply(0.15));

        ObservableList<TreeTableColumn<Object, ?>> columns = equipmentList.getColumns();
        columns.clear();
        columns.add(nameColumn);
        columns.add(slotsColumn);
        columns.add(massColumn);
    }

    private void setupGeneralStatsPane() {
        Chassis chassis = model.loadout.getChassis();
        int massMax = chassis.getMassMax();

        Pane parent = (Pane) generalMassBar.getParent();
        generalMassBar.progressProperty().bind(model.statsMass.divide(massMax));
        generalMassBar.prefWidthProperty().bind(parent.widthProperty());
        generalMassLabel.textProperty().bind(format("%.2f free", model.statsFreeMass));
        generalMassOverlay.textProperty().bind(format("%.2f / %d", model.statsMass, massMax));

        int armorMax = chassis.getArmorMax();
        generalArmorBar.progressProperty().bind(model.statsArmor.divide((double) armorMax));
        generalArmorBar.prefWidthProperty().bind(parent.widthProperty());
        generalArmorLabel.textProperty().bind(format("%d free", model.statsArmorFree));
        generalArmorOverlay.textProperty().bind(format("%d / %d", model.statsArmor, armorMax));

        int criticalSlotsTotal = chassis.getCriticalSlotsTotal();
        generalSlotsBar.progressProperty().bind(model.statsSlots.divide((double) criticalSlotsTotal));
        generalSlotsBar.prefWidthProperty().bind(parent.widthProperty());
        generalSlotsLabel.textProperty().bind(format("%d free", model.statsSlots.negate().add(criticalSlotsTotal)));
        generalSlotsOverlay.textProperty().bind(format("%d / %d", model.statsSlots, criticalSlotsTotal));
    }

    private void setupLayoutView() {
        DynamicSlotDistributor distributor = new DynamicSlotDistributor(model.loadout);

        Region rightArmStrut = new Region();
        rightArmStrut.getStyleClass().add(StyleManager.CLASS_ARM_STRUT);

        Region leftArmStrut = new Region();
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
        Faction faction = model.loadout.getChassis().getFaction();

        FxmlHelpers.bindTogglable(upgradeArtemis, model.hasArtemis, aNewValue -> LiSongMechLab.safeCommand(this,
                cmdStack, new CmdSetGuidanceType(xBar, model.loadout, UpgradeDB.getGuidance(faction, aNewValue))));

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
            LoadoutStandard lstd = (LoadoutStandard) model.loadout;

            FxmlHelpers.bindTogglable(upgradeDoubleHeatSinks, model.hasDoubleHeatSinks,
                    aNewValue -> LiSongMechLab.safeCommand(this, cmdStack,
                            new CmdSetHeatSinkType(xBar, lstd, UpgradeDB.getHeatSinks(faction, aNewValue))));

            FxmlHelpers.bindTogglable(upgradeEndoSteel, model.hasEndoSteel, aNewValue -> LiSongMechLab.safeCommand(this,
                    cmdStack, new CmdSetStructureType(xBar, lstd, UpgradeDB.getStructure(faction, aNewValue))));

            FxmlHelpers.bindTogglable(upgradeFerroFibrous, model.hasFerroFibrous,
                    aNewValue -> LiSongMechLab.safeCommand(this, cmdStack,
                            new CmdSetArmorType(xBar, lstd, UpgradeDB.getArmor(faction, aNewValue))));
        }
    }

    private void showLink(String aTitle, String aContent, String aLink) {
        Hyperlink hyperlink = new Hyperlink(aLink);
        hyperlink.setOnAction((aEvent) -> {
            try {
                Desktop.getDesktop().browse(new URI(aLink));
            }
            catch (Exception e) {
                LiSongMechLab.showError(this, e);
            }
        });

        MenuItem mi = new MenuItem("Copy link");
        mi.setOnAction((aEvent) -> {
            ClipboardContent content = new ClipboardContent();
            content.putString(aLink);
            Clipboard.getSystemClipboard().setContent(content);
        });
        ContextMenu cm = new ContextMenu(mi);
        hyperlink.setContextMenu(cm);

        VBox content = new VBox();
        content.getChildren().add(new Label("Right click to copy:"));
        content.getChildren().add(hyperlink);

        Alert alert = new Alert(AlertType.INFORMATION, aLink, ButtonType.OK);
        alert.setTitle(aTitle);
        alert.setHeaderText(aContent);
        alert.show();
        alert.getDialogPane().setContent(content);
    }

    private void updateEquipmentPredicates() {
        FilterTreeItem<Object> root = (FilterTreeItem<Object>) equipmentList.getRoot();
        for (TreeItem<Object> category : root.getChildrenRaw()) {
            FilterTreeItem<Object> filterTreeItem = (FilterTreeItem<Object>) category;
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

    private void updateTitle() {
        Loadout loadout = model.loadout;
        stage.setTitle("Li Song Mechlab - " + loadout.getName() + " (" + loadout.getChassis().getNameShort() + ")");
    }

}
