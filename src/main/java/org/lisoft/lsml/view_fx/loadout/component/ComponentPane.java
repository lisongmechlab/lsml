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
package org.lisoft.lsml.view_fx.loadout.component;

import java.util.Arrays;
import java.util.Collection;
import java.util.Optional;

import org.lisoft.lsml.command.CmdAddItem;
import org.lisoft.lsml.command.CmdRemoveItem;
import org.lisoft.lsml.command.CmdSetArmor;
import org.lisoft.lsml.command.CmdSetOmniPod;
import org.lisoft.lsml.command.CmdToggleItem;
import org.lisoft.lsml.messages.ArmorMessage;
import org.lisoft.lsml.messages.ArmorMessage.Type;
import org.lisoft.lsml.messages.Message;
import org.lisoft.lsml.messages.MessageReceiver;
import org.lisoft.lsml.messages.MessageXBar;
import org.lisoft.lsml.messages.OmniPodMessage;
import org.lisoft.lsml.model.DynamicSlotDistributor;
import org.lisoft.lsml.model.chassi.ArmorSide;
import org.lisoft.lsml.model.chassi.ChassisOmniMech;
import org.lisoft.lsml.model.chassi.Location;
import org.lisoft.lsml.model.chassi.OmniPod;
import org.lisoft.lsml.model.datacache.ItemDB;
import org.lisoft.lsml.model.datacache.OmniPodDB;
import org.lisoft.lsml.model.item.Item;
import org.lisoft.lsml.model.loadout.EquipResult;
import org.lisoft.lsml.model.loadout.LoadoutOmniMech;
import org.lisoft.lsml.model.loadout.component.ConfiguredComponent;
import org.lisoft.lsml.model.loadout.component.ConfiguredComponentOmniMech;
import org.lisoft.lsml.util.CommandStack;
import org.lisoft.lsml.view_fx.LiSongMechLab;
import org.lisoft.lsml.view_fx.controls.FixedRowsListView;
import org.lisoft.lsml.view_fx.drawers.EquippedItemCell;
import org.lisoft.lsml.view_fx.drawers.OmniPodListCell;
import org.lisoft.lsml.view_fx.properties.ArmorFactory;
import org.lisoft.lsml.view_fx.properties.LoadoutModelAdaptor;
import org.lisoft.lsml.view_fx.properties.LoadoutModelAdaptor.ComponentModel;
import org.lisoft.lsml.view_fx.style.ItemToolTipFormatter;
import org.lisoft.lsml.view_fx.style.StyleManager;
import org.lisoft.lsml.view_fx.util.EquipmentDragHelper;
import org.lisoft.lsml.view_fx.util.FxmlHelpers;

import javafx.beans.binding.Bindings;
import javafx.beans.binding.BooleanExpression;
import javafx.beans.binding.DoubleBinding;
import javafx.beans.binding.NumberBinding;
import javafx.beans.binding.StringBinding;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ComboBox;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.Label;
import javafx.scene.control.Labeled;
import javafx.scene.control.Spinner;
import javafx.scene.control.TitledPane;
import javafx.scene.input.DragEvent;
import javafx.scene.input.Dragboard;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;
import javafx.scene.input.TransferMode;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;

/**
 * A controller for the LoadoutComponent.fxml view.
 * 
 * @author Li Song
 */
public class ComponentPane extends TitledPane implements MessageReceiver {
    public static final int         ITEM_WIDTH = 150;

    @FXML
    private ContextMenu             armorContextMenu;
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
    private ConfiguredComponent     component;
    @FXML
    private GridPane                container;
    @FXML
    private HBox                    hardPointContainer;

    private HardPointPane           hardPointPane;
    @FXML
    private FixedRowsListView<Item> itemView;

    private Location                location;
    private LoadoutModelAdaptor     model;
    @FXML
    private ComboBox<OmniPod>       omniPodSelection;
    @FXML
    private TitledPane              rootPane;
    private CommandStack            stack;

    @FXML
    private CheckBox                toggleHA;

    @FXML
    private CheckBox                toggleLAA;

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
     * @param aToolTipFormatter
     */
    public ComponentPane(MessageXBar aMessageXBar, CommandStack aStack, LoadoutModelAdaptor aModel, Location aLocation,
            DynamicSlotDistributor aDistributor, ItemToolTipFormatter aToolTipFormatter) {
        FxmlHelpers.loadFxmlControl(this);
        aMessageXBar.attach(this);
        stack = aStack;
        model = aModel;
        location = aLocation;
        xBar = aMessageXBar;
        component = model.loadout.getComponent(location);
        rootPane.setContextMenu(null);
        hardPointPane = new HardPointPane(component);
        hardPointContainer.getChildren().setAll(hardPointPane);

        setupToggles();
        setupItemView(aDistributor, aToolTipFormatter);
        updateTitle();
        setupArmors();
        setupOmniPods();
    }

    @Override
    public void receive(Message aMsg) {
        if (aMsg instanceof OmniPodMessage) {
            OmniPodMessage omniPodMessage = (OmniPodMessage) aMsg;
            if (omniPodMessage.component == component) {
                hardPointPane.updateHardPoints();
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
    void onDragDropped(final DragEvent aDragEvent) {
        final Dragboard db = aDragEvent.getDragboard();
        final Optional<Item> data = EquipmentDragHelper.unpackDrag(db, Item.class);
        boolean success = false;

        if (data.isPresent()) {
            success = LiSongMechLab.safeCommand(this, stack,
                    new CmdAddItem(xBar, model.loadout, component, data.get()));
        }
        aDragEvent.setDropCompleted(success);
        aDragEvent.consume();
    }

    @FXML
    void onDragOver(DragEvent aDragEvent) {
        Dragboard db = aDragEvent.getDragboard();

        EquipmentDragHelper.unpackDrag(db, Item.class).ifPresent(aItem -> {
            if (EquipResult.SUCCESS == model.loadout.canEquipDirectly(aItem)
                    && EquipResult.SUCCESS == component.canEquip(aItem)) {
                aDragEvent.acceptTransferModes(TransferMode.COPY_OR_MOVE);
            }
        });
        aDragEvent.consume();
    }

    @FXML
    void onDragStart(MouseEvent aMouseEvent) throws Exception {
        Item item = itemView.getSelectionModel().getSelectedItem();
        if (component.canRemoveItem(item)) {
            Dragboard db = itemView.startDragAndDrop(TransferMode.MOVE);
            EquipmentDragHelper.doDrag(db, item);
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
            container.getChildren().remove(armorLabelBack);
            container.getChildren().remove(armorSpinnerBack);
            container.getChildren().remove(armorMaxBack);
        }
    }

    private void setupArmorSpinner(ArmorSide aSide, Spinner<Integer> aSpinner, Labeled aLabel, Labeled aMaxLabel) {
        ComponentModel componentModel = model.components.get(location);
        ArmorFactory af = new ArmorFactory(xBar, model.loadout, component, aSide, stack, aSpinner);
        af.manualSetProperty().addListener((aObservable, aOld, aNew) -> {
            aSpinner.pseudoClassStateChanged(StyleManager.PC_AUTOARMOR, !aNew.booleanValue());
            aMaxLabel.pseudoClassStateChanged(StyleManager.PC_AUTOARMOR, !aNew.booleanValue());
        });
        aSpinner.pseudoClassStateChanged(StyleManager.PC_AUTOARMOR, !af.getManualSet());
        aSpinner.setValueFactory(af);
        aSpinner.setContextMenu(armorContextMenu);
        aLabel.setContextMenu(armorContextMenu);

        aMaxLabel.pseudoClassStateChanged(StyleManager.PC_AUTOARMOR, !af.getManualSet());

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

    private void setupItemView(DynamicSlotDistributor aDistributor, ItemToolTipFormatter aTooltipFormatter) {
        itemView.setVisibleRows(component.getInternalComponent().getSlots());
        itemView.setItems(new EquippedItemsList(xBar, component, aDistributor));
        itemView.setCellFactory((aList) -> {
            return new EquippedItemCell((FixedRowsListView<Item>) aList, component, model.loadout, stack, xBar,
                    aTooltipFormatter);
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
                LiSongMechLab.safeCommand(this, stack,
                        new CmdSetOmniPod(xBar, (LoadoutOmniMech) model.loadout, componentOmniMech, aNew));
            });
        }
        else {
            container.getChildren().remove(omniPodSelection);
            omniPodSelection = null;
        }
    }

    private void setupTogglable(CheckBox aButton, BooleanExpression aToggleProperty, Item aItem) {
        if (aToggleProperty == null) {
            container.getChildren().remove(aButton);
            return;
        }
        LoadoutOmniMech loadoutOmni = (LoadoutOmniMech) model.loadout;
        ConfiguredComponentOmniMech componentOmniMech = (ConfiguredComponentOmniMech) component;
        FxmlHelpers.bindTogglable(aButton, aToggleProperty, aValue -> LiSongMechLab.safeCommand(aButton, stack,
                new CmdToggleItem(xBar, loadoutOmni, componentOmniMech, aItem, aValue)));
    }

    private void setupToggles() {
        if (Location.LeftArm == location) {
            setupTogglable(toggleLAA, model.hasLeftLAA, ItemDB.LAA);
            setupTogglable(toggleHA, model.hasLeftHA, ItemDB.HA);
        }
        else if (Location.RightArm == location) {
            setupTogglable(toggleLAA, model.hasRightLAA, ItemDB.LAA);
            setupTogglable(toggleHA, model.hasRightHA, ItemDB.HA);
        }
        else {
            container.getChildren().remove(toggleLAA);
            container.getChildren().remove(toggleHA);
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
