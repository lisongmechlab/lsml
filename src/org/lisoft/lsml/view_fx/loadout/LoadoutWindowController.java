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

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.lisoft.lsml.messages.ItemMessage;
import org.lisoft.lsml.messages.Message;
import org.lisoft.lsml.messages.MessageReceiver;
import org.lisoft.lsml.messages.MessageXBar;
import org.lisoft.lsml.messages.UpgradesMessage;
import org.lisoft.lsml.model.DynamicSlotDistributor;
import org.lisoft.lsml.model.chassi.ChassisBase;
import org.lisoft.lsml.model.chassi.Location;
import org.lisoft.lsml.model.datacache.ItemDB;
import org.lisoft.lsml.model.item.Item;
import org.lisoft.lsml.model.loadout.LoadoutBase;
import org.lisoft.lsml.model.loadout.LoadoutStandard;
import org.lisoft.lsml.model.modifiers.MechEfficiencyType;
import org.lisoft.lsml.model.upgrades.Upgrades;
import org.lisoft.lsml.util.CommandStack;
import org.lisoft.lsml.view_fx.LiSongMechLab;
import org.lisoft.lsml.view_fx.controls.FilterTreeItem;
import org.lisoft.lsml.view_fx.loadout.component.ComponentPane;
import org.lisoft.lsml.view_fx.loadout.component.ComponentPaneController;
import org.lisoft.lsml.view_fx.loadout.equipment.EquipmentCategory;
import org.lisoft.lsml.view_fx.loadout.equipment.EquipmentTableCell;
import org.lisoft.lsml.view_fx.loadout.equipment.EquipmentTableRow;
import org.lisoft.lsml.view_fx.loadout.equipment.EquippablePredicate;
import org.lisoft.lsml.view_fx.properties.LoadoutModelAdaptor;
import org.lisoft.lsml.view_fx.style.StyleManager;

import javafx.beans.binding.Bindings;
import javafx.beans.property.BooleanProperty;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.control.CheckBox;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressBar;
import javafx.scene.control.TreeItem;
import javafx.scene.control.TreeTableColumn;
import javafx.scene.control.TreeTableView;
import javafx.scene.input.ClipboardContent;
import javafx.scene.input.Dragboard;
import javafx.scene.input.MouseEvent;
import javafx.scene.input.TransferMode;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;

/**
 * Controller for the loadout window.
 * 
 * @author Emily Björk
 */
public class LoadoutWindowController implements MessageReceiver {

    // Constants

    private static final String    COLUMN_MASS  = "Mass";
    private static final String    COLUMN_SLOTS = "Slots";
    private static final String    COLUMN_NAME  = "Name";
    private static final int       UNDO_DEPTH   = 128;

    // FXML elements

    @FXML
    private ProgressBar            generalMassBar;
    @FXML
    private ProgressBar            generalArmorBar;
    @FXML
    private ProgressBar            generalSlotsBar;
    @FXML
    private Label                  generalMassLabel;
    @FXML
    private Label                  generalArmorLabel;
    @FXML
    private Label                  generalSlotsLabel;
    @FXML
    private CheckBox               upgradeEndoSteel;
    @FXML
    private CheckBox               upgradeFerroFibrous;
    @FXML
    private CheckBox               upgradeDoubleHeatSinks;
    @FXML
    private CheckBox               upgradeArtemis;

    @FXML
    private CheckBox               effCoolRun;
    @FXML
    private CheckBox               effHeatContainment;
    @FXML
    private CheckBox               effTwistX;
    @FXML
    private CheckBox               effTwistSpeed;
    @FXML
    private CheckBox               effAnchorTurn;
    @FXML
    private CheckBox               effArmReflex;
    @FXML
    private CheckBox               effFastFire;
    @FXML
    private CheckBox               effSpeedTweak;
    @FXML
    private CheckBox               effDoubleBasics;

    @FXML
    private HBox                   layoutContainer;
    @FXML
    private TreeTableView<Object>  equipmentList;

    // Local state
    private LoadoutModelAdaptor    model;
    private MessageXBar            xBar         = new MessageXBar();
    private CommandStack           cmdStack     = new CommandStack(UNDO_DEPTH);
    private DynamicSlotDistributor distributor;

    // Methods

    public void setLoadout(LoadoutBase<?> aLoadout) {
        xBar.attach(this);
        model = new LoadoutModelAdaptor(aLoadout, xBar, cmdStack);
        distributor = new DynamicSlotDistributor(aLoadout);

        setupLayoutView();
        setupEquipmentList();
        setupGeneralPanel();
        setupUpgradesPanel();
        setupEfficienciesPanel();
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

    private void setupGeneralPanel() {
        ChassisBase chassis = model.loadout.getChassis();
        int massMax = chassis.getMassMax();
        generalMassBar.progressProperty().bind(model.statsMass.divide(massMax));
        generalMassLabel.textProperty().bind(Bindings.format("%.2f t free", model.statsFreeMass));

        int armorMax = chassis.getArmorMax();
        generalArmorBar.progressProperty().bind(model.statsArmor.divide((double) armorMax));
        generalArmorLabel.textProperty().bind(Bindings.format("%d free", model.statsArmorFree));

        int criticalSlotsTotal = chassis.getCriticalSlotsTotal();
        generalSlotsBar.progressProperty().bind(model.statsSlots.divide((double) criticalSlotsTotal));
        generalSlotsLabel.textProperty()
                .bind(Bindings.format("%d free", model.statsSlots.negate().add(criticalSlotsTotal)));
    }

    private void setupLayoutView() {
        Region rightArmStrut = new Region();
        rightArmStrut.getStyleClass().add(StyleManager.CSS_CLASS_ARM_STRUT);

        Region leftArmStrut = new Region();
        leftArmStrut.getStyleClass().add(StyleManager.CSS_CLASS_ARM_STRUT);

        Region rightTorsoStrut = new Region();
        rightTorsoStrut.getStyleClass().add(StyleManager.CSS_CLASS_TORSO_STRUT);
        Region leftTorsoStrut = new Region();
        leftTorsoStrut.getStyleClass().add(StyleManager.CSS_CLASS_TORSO_STRUT);

        ObservableList<Node> children = layoutContainer.getChildren();
        children.add(new VBox(leftArmStrut, new ComponentPane(xBar, cmdStack, model, Location.RightArm, distributor)));
        children.add(
                new VBox(rightTorsoStrut, new ComponentPane(xBar, cmdStack, model, Location.RightTorso, distributor),
                        new ComponentPane(xBar, cmdStack, model, Location.RightLeg, distributor)));
        children.add(new VBox(new ComponentPane(xBar, cmdStack, model, Location.Head, distributor),
                new ComponentPane(xBar, cmdStack, model, Location.CenterTorso, distributor)));
        children.add(new VBox(leftTorsoStrut, new ComponentPane(xBar, cmdStack, model, Location.LeftTorso, distributor),
                new ComponentPane(xBar, cmdStack, model, Location.LeftLeg, distributor)));
        children.add(new VBox(rightArmStrut, new ComponentPane(xBar, cmdStack, model, Location.LeftArm, distributor)));
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
        TreeTableColumn<Object, String> nameColumn = new TreeTableColumn<>(COLUMN_NAME);
        nameColumn.setCellValueFactory(new ItemValueFactory(item -> item.getShortName(), true));
        nameColumn.setCellFactory(aColumn -> new EquipmentTableCell(model.loadout, true));
        nameColumn.setPrefWidth(ComponentPaneController.ITEM_WIDTH * 1.2);

        TreeTableColumn<Object, String> slotsColumn = new TreeTableColumn<>(COLUMN_SLOTS);
        slotsColumn
                .setCellValueFactory(new ItemValueFactory(item -> Integer.toString(item.getNumCriticalSlots()), false));
        slotsColumn.setCellFactory(aColumn -> new EquipmentTableCell(model.loadout, false));

        TreeTableColumn<Object, String> massColumn = new TreeTableColumn<>(COLUMN_MASS);
        massColumn.setCellValueFactory(new ItemValueFactory(item -> Double.toString(item.getMass()), false));
        massColumn.setCellFactory(aColumn -> new EquipmentTableCell(model.loadout, false));

        ObservableList<TreeTableColumn<Object, ?>> columns = equipmentList.getColumns();
        columns.add(nameColumn);
        columns.add(slotsColumn);
        columns.add(massColumn);
    }

    @FXML
    void equipmentDragDetected(MouseEvent aEvent) {
        TreeItem<Object> treeItem = equipmentList.getSelectionModel().getSelectedItem();
        if (null == treeItem || !(treeItem.getValue() instanceof Item))
            return;

        Item item = (Item) treeItem.getValue();
        Dragboard db = equipmentList.startDragAndDrop(TransferMode.COPY);

        ClipboardContent cc = new ClipboardContent();
        cc.put(LiSongMechLab.ITEM_DATA_FORMAT, item);
        db.setContent(cc);

        aEvent.consume();
    }

    @Override
    public void receive(Message aMsg) {
        if (aMsg instanceof ItemMessage || aMsg instanceof UpgradesMessage) {
            updateEquipmentPredicates();
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
}
