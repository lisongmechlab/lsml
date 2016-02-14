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
import java.io.UnsupportedEncodingException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.lisoft.lsml.command.CmdAddToGarage;
import org.lisoft.lsml.command.CmdLoadStock;
import org.lisoft.lsml.command.CmdRename;
import org.lisoft.lsml.command.CmdSetMaxArmor;
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
import org.lisoft.lsml.model.chassi.Chassis;
import org.lisoft.lsml.model.chassi.Location;
import org.lisoft.lsml.model.datacache.ChassisDB;
import org.lisoft.lsml.model.datacache.ItemDB;
import org.lisoft.lsml.model.datacache.PilotModuleDB;
import org.lisoft.lsml.model.export.Base64LoadoutCoder;
import org.lisoft.lsml.model.export.SmurfyImportExport;
import org.lisoft.lsml.model.garage.Garage;
import org.lisoft.lsml.model.item.Item;
import org.lisoft.lsml.model.item.ModuleSlot;
import org.lisoft.lsml.model.item.PilotModule;
import org.lisoft.lsml.model.item.Weapon;
import org.lisoft.lsml.model.item.WeaponModule;
import org.lisoft.lsml.model.loadout.Loadout;
import org.lisoft.lsml.model.loadout.LoadoutMetrics;
import org.lisoft.lsml.util.CommandStack;
import org.lisoft.lsml.util.EncodingException;
import org.lisoft.lsml.view_fx.LiSongMechLab;
import org.lisoft.lsml.view_fx.controls.FilterTreeItem;
import org.lisoft.lsml.view_fx.loadout.component.ComponentPane;
import org.lisoft.lsml.view_fx.loadout.component.ModulePane;
import org.lisoft.lsml.view_fx.loadout.equipment.EquipmentCategory;
import org.lisoft.lsml.view_fx.loadout.equipment.EquipmentTableCell;
import org.lisoft.lsml.view_fx.loadout.equipment.EquipmentTableRow;
import org.lisoft.lsml.view_fx.loadout.equipment.EquippablePredicate;
import org.lisoft.lsml.view_fx.loadout.equipment.ModuleTableRow;
import org.lisoft.lsml.view_fx.properties.LoadoutMetricsModelAdaptor;
import org.lisoft.lsml.view_fx.properties.LoadoutModelAdaptor;
import org.lisoft.lsml.view_fx.style.ItemToolTipFormatter;
import org.lisoft.lsml.view_fx.style.StyleManager;
import org.lisoft.lsml.view_fx.util.FxmlHelpers;

import javafx.beans.property.ReadOnlyStringWrapper;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.ButtonBar.ButtonData;
import javafx.scene.control.ButtonType;
import javafx.scene.control.ChoiceDialog;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.Hyperlink;
import javafx.scene.control.Label;
import javafx.scene.control.MenuItem;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.Tab;
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
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

/**
 * Controller for the loadout window.
 * 
 * @author Li Song
 */
public class LoadoutWindow extends BorderPane implements MessageReceiver {
    private static final String              EQ_COL_MASS   = "Mass";
    private static final String              EQ_COL_NAME   = "Name";
    private static final String              EQ_COL_SLOTS  = "Slots";
    private static final Base64LoadoutCoder  LOADOUT_CODER = new Base64LoadoutCoder();
    private static final int                 UNDO_DEPTH    = 128;
    private final CommandStack               cmdStack      = new CommandStack(UNDO_DEPTH);
    private final LoadoutMetricsModelAdaptor metrics;
    private final LoadoutModelAdaptor        model;
    private final Stage                      stage;
    private final MessageXBar                xBar          = new MessageXBar();
    private final ItemToolTipFormatter       toolTipFormatter;
    private final Garage                     garage;
    private final MessageXBar                globalXBar;

    @FXML
    private TreeTableView<Object>            equipmentList;
    @FXML
    private HBox                             layoutContainer;
    @FXML
    private MenuItem                         menuAddToGarage;
    @FXML
    private MenuItem                         menuLoadStock;
    @FXML
    private MenuItem                         menuRedo;
    @FXML
    private MenuItem                         menuUndo;
    @FXML
    private TreeTableView<Object>            moduleList;
    @FXML
    private Tab                              weaponLabTab;
    @FXML
    private ScrollPane                       infoScrollPane;

    public LoadoutWindow(MessageXBar aGlobalXBar, Loadout aLoadout, Garage aGarage, Stage aStage) {
        FxmlHelpers.loadFxmlControl(this);
        globalXBar = aGlobalXBar;
        globalXBar.attach(this);
        xBar.attach(this);
        model = new LoadoutModelAdaptor(aLoadout, xBar);
        metrics = new LoadoutMetricsModelAdaptor(new LoadoutMetrics(aLoadout, null, xBar), aLoadout, xBar);
        garage = aGarage;
        stage = aStage;
        toolTipFormatter = new ItemToolTipFormatter();

        stage.setOnCloseRequest((aWindowEvent) -> {
            if (!garage.getLoadoutRoot().contains(model.loadout)) {
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

        updateTitle();
        setupLayoutView();
        setupEquipmentList();
        setupModulesList();
        setupMenuBar();
        setupWeaponLabPane();

        infoScrollPane.setContent(new LoadoutInfoPane(xBar, cmdStack, model, metrics));
    }

    private void setupWeaponLabPane() {
        WeaponLabPane weaponLabPane = new WeaponLabPane(xBar, model.loadout, metrics);
        weaponLabTab.setContent(weaponLabPane);
        weaponLabPane.maxWidthProperty().bind(layoutContainer.widthProperty());
    }

    @FXML
    public void addToGarage() {
        LiSongMechLab.safeCommand(this, cmdStack,
                new CmdAddToGarage<>(globalXBar, garage.getLoadoutRoot(), model.loadout));
        menuAddToGarage.setDisable(true);
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

        if (items || upgrades || omniPods) {
            updateEquipmentPredicates();
        }

        if (modules) {
            updateModulePredicates();
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
            if (LiSongMechLab.safeCommand(this, cmdStack, new CmdRename(model.loadout, xBar, aName))) {
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
    public void shareLsmlLink() throws EncodingException, UnsupportedEncodingException {
        String trampolineLink = LOADOUT_CODER.encodeHttpTrampoline(model.loadout);

        showLink("LSML Export Complete", "The loadout " + model.loadout.getName() + " has been encoded to a LSML link.",
                trampolineLink);
    }

    @FXML
    public void shareSmurfy() {
        SmurfyImportExport export = new SmurfyImportExport(null, LOADOUT_CODER);
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
            Chassis chassis = model.loadout.getChassis();
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
        nameColumn.setCellFactory(aColumn -> new EquipmentTableCell(model.loadout, true, toolTipFormatter));
        nameColumn.prefWidthProperty().bind(equipmentList.widthProperty().multiply(0.6));

        TreeTableColumn<Object, String> slotsColumn = new TreeTableColumn<>(EQ_COL_SLOTS);
        slotsColumn
                .setCellValueFactory(new ItemValueFactory(item -> Integer.toString(item.getNumCriticalSlots()), false));
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
                new ComponentPane(xBar, cmdStack, model, Location.RightArm, distributor, toolTipFormatter));
        VBox rightTorsoBox = new VBox(rightTorsoStrut,
                new ComponentPane(xBar, cmdStack, model, Location.RightTorso, distributor, toolTipFormatter),
                new ComponentPane(xBar, cmdStack, model, Location.RightLeg, distributor, toolTipFormatter));
        VBox centralBox = new VBox(
                new ComponentPane(xBar, cmdStack, model, Location.Head, distributor, toolTipFormatter),
                new ComponentPane(xBar, cmdStack, model, Location.CenterTorso, distributor, toolTipFormatter));
        VBox leftTorsoBox = new VBox(leftTorsoStrut,
                new ComponentPane(xBar, cmdStack, model, Location.LeftTorso, distributor, toolTipFormatter),
                new ComponentPane(xBar, cmdStack, model, Location.LeftLeg, distributor, toolTipFormatter));
        VBox leftArmBox = new VBox(leftArmStrut,
                new ComponentPane(xBar, cmdStack, model, Location.LeftArm, distributor, toolTipFormatter));

        ModulePane modulePane = new ModulePane(xBar, cmdStack, model);
        rightArmBox.getChildren().add(modulePane);

        Region rightShim = new Region();
        rightShim.setPrefHeight(0);
        VBox.setVgrow(rightShim, Priority.ALWAYS);
        HBox rightComponents = new HBox(rightArmBox, rightTorsoBox);
        VBox rightSide = new VBox(
                new StackPane(leftTorsoStrut, new VBox(new UpgradesPane(xBar, cmdStack, model), rightShim)),
                rightComponents);

        Region leftShim = new Region();
        leftShim.setPrefHeight(0);
        VBox.setVgrow(leftShim, Priority.ALWAYS);
        HBox leftComponents = new HBox(leftTorsoBox, leftArmBox);
        VBox leftSide = new VBox(new StackPane(rightTorsoStrut, new VBox(new GeneralStatsPane(model), leftShim)),
                leftComponents);

        children.add(rightSide);
        children.add(centralBox);
        children.add(leftSide);

        leftComponents.getStyleClass().add(StyleManager.CSS_CLASS_LAYOUT_CONTAINER);
        rightComponents.getStyleClass().add(StyleManager.CSS_CLASS_LAYOUT_CONTAINER);
        rightArmBox.getStyleClass().add(StyleManager.CSS_CLASS_LAYOUT_CONTAINER);
        rightTorsoBox.getStyleClass().add(StyleManager.CSS_CLASS_LAYOUT_CONTAINER);
        centralBox.getStyleClass().add(StyleManager.CSS_CLASS_LAYOUT_CONTAINER);
        leftTorsoBox.getStyleClass().add(StyleManager.CSS_CLASS_LAYOUT_CONTAINER);
        leftArmBox.getStyleClass().add(StyleManager.CSS_CLASS_LAYOUT_CONTAINER);
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

    private void updateTitle() {
        Loadout loadout = model.loadout;
        stage.setTitle("Li Song Mechlab - " + loadout.getName() + " (" + loadout.getChassis().getNameShort() + ")");
    }

}
