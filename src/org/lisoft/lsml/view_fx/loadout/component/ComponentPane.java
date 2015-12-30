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
package org.lisoft.lsml.view_fx.loadout.component;

import java.io.IOException;
import java.util.Arrays;
import java.util.Collection;

import org.lisoft.lsml.command.CmdAddItem;
import org.lisoft.lsml.command.CmdRemoveItem;
import org.lisoft.lsml.command.CmdSetArmor;
import org.lisoft.lsml.command.CmdSetOmniPod;
import org.lisoft.lsml.messages.ArmorMessage;
import org.lisoft.lsml.messages.ArmorMessage.Type;
import org.lisoft.lsml.messages.Message;
import org.lisoft.lsml.messages.MessageReceiver;
import org.lisoft.lsml.messages.MessageXBar;
import org.lisoft.lsml.messages.OmniPodMessage;
import org.lisoft.lsml.model.DynamicSlotDistributor;
import org.lisoft.lsml.model.chassi.ArmorSide;
import org.lisoft.lsml.model.chassi.ChassisOmniMech;
import org.lisoft.lsml.model.chassi.HardPointType;
import org.lisoft.lsml.model.chassi.Location;
import org.lisoft.lsml.model.chassi.OmniPod;
import org.lisoft.lsml.model.datacache.ItemDB;
import org.lisoft.lsml.model.datacache.OmniPodDB;
import org.lisoft.lsml.model.item.Item;
import org.lisoft.lsml.model.loadout.EquipResult;
import org.lisoft.lsml.model.loadout.LoadoutOmniMech;
import org.lisoft.lsml.model.loadout.component.ConfiguredComponentBase;
import org.lisoft.lsml.model.loadout.component.ConfiguredComponentOmniMech;
import org.lisoft.lsml.util.CommandStack;
import org.lisoft.lsml.view_fx.LiSongMechLab;
import org.lisoft.lsml.view_fx.controls.FixedRowsListView;
import org.lisoft.lsml.view_fx.drawers.EquippedItemCell;
import org.lisoft.lsml.view_fx.drawers.OmniPodListCell;
import org.lisoft.lsml.view_fx.properties.ArmorFactory;
import org.lisoft.lsml.view_fx.properties.LoadoutModelAdaptor;
import org.lisoft.lsml.view_fx.properties.LoadoutModelAdaptor.ComponentModel;
import org.lisoft.lsml.view_fx.style.HardPointFormatter;
import org.lisoft.lsml.view_fx.style.StyleManager;
import org.lisoft.lsml.view_fx.util.FxmlHelpers;

import javafx.beans.binding.Bindings;
import javafx.beans.binding.DoubleBinding;
import javafx.beans.binding.NumberBinding;
import javafx.beans.binding.StringBinding;
import javafx.beans.property.BooleanProperty;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.ComboBox;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.Label;
import javafx.scene.control.Labeled;
import javafx.scene.control.Spinner;
import javafx.scene.control.TitledPane;
import javafx.scene.control.ToggleButton;
import javafx.scene.input.DragEvent;
import javafx.scene.input.Dragboard;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;
import javafx.scene.input.TransferMode;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;

/**
 * A controller for the LoadoutComponent.fxml view.
 * 
 * @author Emily Björk
 */
public class ComponentPane extends TitledPane implements MessageReceiver {
    public static final int         ITEM_WIDTH = 150;

    @FXML
    ContextMenu                     armorContextMenu;
    @FXML
    private HBox                    armorBox;
    @FXML
    private HBox                    armorBoxBack;
    @FXML
    private Label                   armorLabel;
    @FXML
    private Label                   armorLabelBack;
    @FXML
    private Label                   armorMax;
    @FXML
    private Label                   armorMaxBack;
    @FXML
    private Spinner<Integer>        armorSpinner;
    @FXML
    private Spinner<Integer>        armorSpinnerBack;
    private ConfiguredComponentBase component;
    @FXML
    private VBox                    container;
    @FXML
    private HBox                    hardPointContainer;
    @FXML
    private FixedRowsListView<Item>          itemView;

    private Location                location;
    private LoadoutModelAdaptor     model;
    @FXML
    private ComboBox<OmniPod>       omniPodSelection;
    @FXML
    private TitledPane              rootPane;
    private CommandStack            stack;

    @FXML
    private ToggleButton            toggleHA;

    @FXML
    private ToggleButton            toggleLAA;

    private MessageXBar             xBar;

    /**
     * Creates a new {@link ComponentPane}.
     * 
     * @param aMessageXBar
     *            A {@link MessageXBar} to send and receive messages on.
     * @param aStack
     *            The {@link CommandStack} to use for doing commands.
     * @param aModel
     *            The loadout to get the component from.
     * @param aLocation
     *            The location of the loadout to get component for.
     * @param aDistributor
     *            A {@link DynamicSlotDistributor} to use for determining how many armor/structure slots to show.
     * @throws IOException
     */
    public ComponentPane(MessageXBar aMessageXBar, CommandStack aStack, LoadoutModelAdaptor aModel, Location aLocation,
            DynamicSlotDistributor aDistributor) throws IOException {
        FxmlHelpers.loadFxmlControl(this);
        aMessageXBar.attach(this);
        stack = aStack;
        model = aModel;
        location = aLocation;
        xBar = aMessageXBar;
        component = model.loadout.getComponent(location);
        rootPane.setContextMenu(null);

        setupToggles();
        setupItemView(aDistributor);
        updateTitle();
        setupArmors();
        updateHardPoints();
        setupOmniPods();
    }

    @Override
    public void receive(Message aMsg) {
        if (aMsg instanceof OmniPodMessage) {
            OmniPodMessage omniPodMessage = (OmniPodMessage) aMsg;
            if (omniPodMessage.component == component) {
                updateHardPoints();
            }
        }

    }

    @FXML
    public void resetManualArmor(@SuppressWarnings("unused") ActionEvent event) throws Exception {
        for (ArmorSide side : ArmorSide.allSides(component.getInternalComponent())) {
            stack.pushAndApply(new CmdSetArmor(xBar, model.loadout, component, side, component.getArmor(side), false));
        }
        xBar.post(new ArmorMessage(component, Type.ARMOR_DISTRIBUTION_UPDATE_REQUEST));
    }

    @FXML
    void onDragDropped(DragEvent aDragEvent) {
        Dragboard db = aDragEvent.getDragboard();
        boolean success = false;
        if (db.hasString()) {
            try {
                Item item = ItemDB.lookup(Integer.parseInt(db.getString()));
                stack.pushAndApply(new CmdAddItem(xBar, model.loadout, component, item));
                success = true;
            }
            catch (Exception e) {
                LiSongMechLab.showError(e);
            }
        }
        aDragEvent.setDropCompleted(success);
        aDragEvent.consume();
    }

    @FXML
    void onDragOver(DragEvent aDragEvent) {
        Dragboard db = aDragEvent.getDragboard();
        if (db.hasString()) {
            try {
                Item item = ItemDB.lookup(Integer.parseInt(db.getString()));
                if (EquipResult.SUCCESS == model.loadout.canEquipDirectly(item)
                        && EquipResult.SUCCESS == component.canEquip(item)) {
                    aDragEvent.acceptTransferModes(TransferMode.COPY_OR_MOVE);
                }
            }
            catch (Throwable t) {
                // User dragging junk, ignore it.
                // Sue: Why you always bring me junk?!
            }
        }
        aDragEvent.consume();
    }

    @FXML
    void onDragStart(MouseEvent aMouseEvent) throws Exception {
        Item item = itemView.getSelectionModel().getSelectedItem();
        if (component.canRemoveItem(item)) {
            Dragboard db = itemView.startDragAndDrop(TransferMode.MOVE);
            LiSongMechLab.addEquipmentDrag(db, item);
            stack.pushAndApply(new CmdRemoveItem(xBar, model.loadout, component, item));
        }
        aMouseEvent.consume();
    }

    @FXML
    void onEquipmentClicked(MouseEvent aEvent) throws Exception {
        if (aEvent.getButton() == MouseButton.PRIMARY && aEvent.getClickCount() == 2) {
            if (aEvent.getSource() == itemView) {
                Item item = itemView.getSelectionModel().getSelectedItem();
                if (item != null && component.canRemoveItem(item)) {
                    stack.pushAndApply(new CmdRemoveItem(xBar, model.loadout, component, item));
                }
            }
        }
    }

    private void setupArmors() {
        if (location.isTwoSided()) {
            setupArmorSpinner(ArmorSide.FRONT, armorSpinner, armorLabel, armorMax);
            setupArmorSpinner(ArmorSide.BACK, armorSpinnerBack, armorLabelBack, armorMaxBack);
            armorLabel.setText("Front:");
            armorLabelBack.setText("Back:");
        }
        else {
            setupArmorSpinner(ArmorSide.ONLY, armorSpinner, armorLabel, armorMax);
            armorLabel.setText("Armor:");
            container.getChildren().remove(armorBoxBack);
        }
    }

    private void setupArmorSpinner(ArmorSide aSide, Spinner<Integer> aSpinner, Labeled aLabel, Labeled aMaxLabel) {
        ComponentModel componentModel = model.components.get(location);
        ArmorFactory af = new ArmorFactory(xBar, model.loadout, component, aSide, stack, aSpinner);
        af.manualSetProperty().addListener((aObservable, aOld, aNew) -> {
            aSpinner.pseudoClassStateChanged(StyleManager.CSS_PC_AUTOARMOR, !aNew.booleanValue());
            aMaxLabel.pseudoClassStateChanged(StyleManager.CSS_PC_AUTOARMOR, !aNew.booleanValue());
        });
        aSpinner.pseudoClassStateChanged(StyleManager.CSS_PC_AUTOARMOR, !af.getManualSet());
        aSpinner.setValueFactory(af);
        aSpinner.setContextMenu(armorContextMenu);
        aLabel.setContextMenu(armorContextMenu);

        aMaxLabel.pseudoClassStateChanged(StyleManager.CSS_PC_AUTOARMOR, !af.getManualSet());

        NumberBinding armorMaxBinding = aSide == ArmorSide.BACK ? componentModel.armorMaxBack : componentModel.armorMax;
        NumberBinding armorEffBinding = aSide == ArmorSide.BACK ? componentModel.armorEffBack : componentModel.armorEff;
        NumberBinding armorBinding = aSide == ArmorSide.BACK ? componentModel.armorBack : componentModel.armor;
        NumberBinding armorBonus = armorEffBinding.subtract(armorBinding);
        StringBinding formatBinding = Bindings.when(armorBonus.isEqualTo(0))
                .then(Bindings.format(" /%.0f", armorMaxBinding))
                .otherwise(Bindings.format(" /%.0f %+d", armorMaxBinding, armorBonus));
        aMaxLabel.textProperty().bind(formatBinding);
        aMaxLabel.setContextMenu(armorContextMenu);
    }

    private void setupItemView(DynamicSlotDistributor aDistributor) {
        itemView.setVisibleRows(component.getInternalComponent().getSlots());
        itemView.setItems(new EquippedItemsList(xBar, component, aDistributor));
        itemView.setCellFactory((aList) -> {
            return new EquippedItemCell((FixedRowsListView<Item>) aList, component, model.loadout, stack, xBar);
        });

        itemView.setPrefWidth(ITEM_WIDTH);
    }

    private void setupOmniPods() {
        if (component instanceof ConfiguredComponentOmniMech) {
            ConfiguredComponentOmniMech componentOmniMech = (ConfiguredComponentOmniMech) component;

            final Collection<OmniPod> allPods;
            if (location == Location.CenterTorso) {
                allPods = Arrays.asList(componentOmniMech.getOmniPod());
            }
            else {
                allPods = OmniPodDB.lookup((ChassisOmniMech) model.loadout.getChassis(), location);
            }

            omniPodSelection.getItems().addAll(allPods);
            omniPodSelection.getSelectionModel().select(componentOmniMech.getOmniPod());
            omniPodSelection.setCellFactory(aListView -> new OmniPodListCell());

            DoubleBinding padding = Bindings.selectDouble(container.paddingProperty(), "left")
                    .add(Bindings.selectDouble(container.paddingProperty(), "right"));

            omniPodSelection.maxWidthProperty().bind(container.widthProperty().subtract(padding));
            omniPodSelection.getSelectionModel().selectedItemProperty().addListener((aObservable, aOld, aNew) -> {
                try {
                    stack.pushAndApply(
                            new CmdSetOmniPod(xBar, (LoadoutOmniMech) model.loadout, componentOmniMech, aNew));
                }
                catch (Exception e) {
                    // Should never fail.
                    LiSongMechLab.showError(e);
                }
            });
        }
        else {
            container.getChildren().remove(omniPodSelection);
            omniPodSelection = null;
        }
    }

    private void setupTogglable(ToggleButton aButton, BooleanProperty aToggleProperty) {
        if (aToggleProperty == null) {
            container.getChildren().remove(aButton);
            return;
        }
        aButton.selectedProperty().bindBidirectional(aToggleProperty);
    }

    private void setupToggles() {
        if (Location.LeftArm == location) {
            setupTogglable(toggleLAA, model.hasLeftLAA);
            setupTogglable(toggleHA, model.hasLeftHA);
        }
        else if (Location.RightArm == location) {
            setupTogglable(toggleLAA, model.hasRightLAA);
            setupTogglable(toggleHA, model.hasRightHA);
        }
        else {
            container.getChildren().remove(toggleLAA);
            container.getChildren().remove(toggleHA);
        }
    }

    private void updateHardPoints() {
        hardPointContainer.getChildren().clear();
        if (location != Location.LeftLeg && location != Location.RightLeg && location != Location.Head
                && location != Location.CenterTorso) {
            // This spaces out components that don't have any hard points to be as tall
            // as their opposite component that may or may not have a hard point.
            Label noHardPoint = new Label();
            noHardPoint.getStyleClass().add(StyleManager.CSS_CLASS_HARDPOINT);
            noHardPoint.setVisible(false);
            hardPointContainer.getChildren().add(noHardPoint);
        }

        HardPointFormatter hardPointFormatter = new HardPointFormatter();
        for (HardPointType hardPointType : HardPointType.values()) {
            int num = component.getHardPointCount(hardPointType);
            if (num > 0) {
                hardPointContainer.getChildren().add(hardPointFormatter.format(num, hardPointType));
            }
        }
    }

    private void updateTitle() {
        ComponentModel componentModel = model.components.get(location);
        DoubleBinding diff = componentModel.healthEff.subtract(componentModel.health);

        StringBinding formatBinding = Bindings.when(diff.isEqualTo(0))
                .then(Bindings.format("%s (%.0f hp)", location.longName(), componentModel.health))
                .otherwise(Bindings.format("%s (%.0f %+.0f hp)", location.longName(), componentModel.health, diff));
        rootPane.textProperty().bind(formatBinding);
    }
}
