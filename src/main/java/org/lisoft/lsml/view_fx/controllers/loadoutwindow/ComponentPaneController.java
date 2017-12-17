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
package org.lisoft.lsml.view_fx.controllers.loadoutwindow;

import static javafx.beans.binding.Bindings.format;
import static javafx.beans.binding.Bindings.selectDouble;
import static javafx.beans.binding.Bindings.when;

import java.util.Arrays;
import java.util.Collection;
import java.util.Optional;

import org.lisoft.lsml.command.CmdAddItem;
import org.lisoft.lsml.command.CmdRemoveItem;
import org.lisoft.lsml.command.CmdSetArmour;
import org.lisoft.lsml.command.CmdSetOmniPod;
import org.lisoft.lsml.command.CmdToggleItem;
import org.lisoft.lsml.messages.ArmourMessage;
import org.lisoft.lsml.messages.ArmourMessage.Type;
import org.lisoft.lsml.messages.Message;
import org.lisoft.lsml.messages.MessageReceiver;
import org.lisoft.lsml.messages.MessageXBar;
import org.lisoft.lsml.messages.OmniPodMessage;
import org.lisoft.lsml.model.DynamicSlotDistributor;
import org.lisoft.lsml.model.chassi.ArmourSide;
import org.lisoft.lsml.model.chassi.ChassisOmniMech;
import org.lisoft.lsml.model.chassi.Location;
import org.lisoft.lsml.model.chassi.OmniPod;
import org.lisoft.lsml.model.database.ItemDB;
import org.lisoft.lsml.model.database.OmniPodDB;
import org.lisoft.lsml.model.item.Item;
import org.lisoft.lsml.model.loadout.ConfiguredComponent;
import org.lisoft.lsml.model.loadout.ConfiguredComponentOmniMech;
import org.lisoft.lsml.model.loadout.EquipResult;
import org.lisoft.lsml.model.loadout.LoadoutFactory;
import org.lisoft.lsml.model.loadout.LoadoutOmniMech;
import org.lisoft.lsml.util.CommandStack;
import org.lisoft.lsml.view_fx.LiSongMechLab;
import org.lisoft.lsml.view_fx.Settings;
import org.lisoft.lsml.view_fx.controllers.AbstractFXController;
import org.lisoft.lsml.view_fx.controls.EquippedItemCell;
import org.lisoft.lsml.view_fx.controls.EquippedItemsList;
import org.lisoft.lsml.view_fx.controls.FixedRowsListView;
import org.lisoft.lsml.view_fx.controls.HardPointPane;
import org.lisoft.lsml.view_fx.controls.OmniPodListCell;
import org.lisoft.lsml.view_fx.properties.ArmourFactory;
import org.lisoft.lsml.view_fx.properties.LoadoutModelAdaptor;
import org.lisoft.lsml.view_fx.properties.LoadoutModelAdaptor.ComponentModel;
import org.lisoft.lsml.view_fx.style.HardPointFormatter;
import org.lisoft.lsml.view_fx.style.ItemToolTipFormatter;
import org.lisoft.lsml.view_fx.style.StyleManager;
import org.lisoft.lsml.view_fx.util.EquipmentDragUtils;
import org.lisoft.lsml.view_fx.util.FxControlUtils;

import javafx.beans.binding.BooleanExpression;
import javafx.beans.binding.DoubleBinding;
import javafx.beans.binding.NumberExpression;
import javafx.beans.binding.StringBinding;
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
import javafx.scene.input.MouseEvent;
import javafx.scene.input.TransferMode;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;

/**
 * A controller for the LoadoutComponent.fxml view.
 *
 * @author Emily Björk
 */
public class ComponentPaneController extends AbstractFXController implements MessageReceiver {
    public static final int ITEM_WIDTH = 150;

    @FXML
    private ContextMenu armourContextMenu;
    @FXML
    private Region armourIcon;
    @FXML
    private Region armourBackIcon;
    @FXML
    private Label armourMax;
    @FXML
    private Label armourMaxBack;
    @FXML
    private Spinner<Integer> armourSpinner;
    @FXML
    private Spinner<Integer> armourSpinnerBack;
    private final ConfiguredComponent component;
    @FXML
    private VBox container;
    @FXML
    private HBox hardPointContainer;
    private final HardPointPane hardPointPane;
    @FXML
    private FixedRowsListView<Item> itemView;
    private final Location location;
    private final LoadoutModelAdaptor model;
    @FXML
    private ComboBox<OmniPod> omniPodSelection;
    @FXML
    private TitledPane rootPane;
    private final CommandStack stack;
    @FXML
    private CheckBox toggleHA;
    @FXML
    private CheckBox toggleLAA;

    private final MessageXBar xBar;
    private final Settings settings;

    /**
     * Creates a new {@link ComponentPaneController}.
     * 
     * @param aSettings
     *            A {@link Settings} object which is used for affecting certain properties of this controller.
     * @param aMessageXBar
     *            A {@link MessageXBar} to send and receive messages on.
     * @param aStack
     *            The {@link CommandStack} to use for doing commands.
     * @param aModel
     *            The loadout to get the component from.
     * @param aLocation
     *            The location of the loadout to get component for.
     * @param aDistributor
     *            A {@link DynamicSlotDistributor} to use for determining how many armour/structure slots to show.
     * @param aToolTipFormatter
     *            A {@link ItemToolTipFormatter} to use for formatting tool tips.
     * @param aLoadoutFactory
     *            Is used by one of the commands in it's search strategy.
     */
    public ComponentPaneController(Settings aSettings, MessageXBar aMessageXBar, CommandStack aStack,
            LoadoutModelAdaptor aModel, Location aLocation, DynamicSlotDistributor aDistributor,
            ItemToolTipFormatter aToolTipFormatter, LoadoutFactory aLoadoutFactory) {
        aMessageXBar.attach(this);
        settings = aSettings;
        stack = aStack;
        model = aModel;
        location = aLocation;
        xBar = aMessageXBar;
        component = model.loadout.getComponent(location);

        rootPane.setContextMenu(null);
        hardPointPane = new HardPointPane(new HardPointFormatter(), component);
        hardPointContainer.getChildren().setAll(hardPointPane);

        final ComponentModel componentModel = model.components.get(location);
        final DoubleBinding healthBonus = componentModel.healthEff.subtract(componentModel.health);
        final StringBinding titleText = when(healthBonus.isEqualTo(0))
                .then(format("%s (%.0f hp)", location.shortName(), componentModel.health))
                .otherwise(format("%s (%.0f %+.0f hp)", location.shortName(), componentModel.health, healthBonus));

        rootPane.textProperty().bind(titleText);

        setupToggles();
        setupItemView(aDistributor, aToolTipFormatter, aLoadoutFactory);
        setupArmours();
        setupOmniPods();

    }

    @Override
    public void receive(Message aMsg) {
        if (aMsg instanceof OmniPodMessage) {
            final OmniPodMessage omniPodMessage = (OmniPodMessage) aMsg;
            if (omniPodMessage.component == component) {
                hardPointPane.updateHardPoints(component);
            }
        }
    }

    @FXML
    public void resetManualArmour() throws Exception {
        for (final ArmourSide side : ArmourSide.allSides(component.getInternalComponent())) {
            stack.pushAndApply(
                    new CmdSetArmour(xBar, model.loadout, component, side, component.getArmour(side), false));
        }
        xBar.post(new ArmourMessage(component, Type.ARMOUR_DISTRIBUTION_UPDATE_REQUEST));
    }

    @FXML
    void onDragDropped(final DragEvent aDragEvent) {
        final Dragboard db = aDragEvent.getDragboard();
        final Optional<Item> data = EquipmentDragUtils.unpackDrag(db, Item.class);
        boolean success = false;

        if (data.isPresent()) {
            success = LiSongMechLab.safeCommand(root, stack, new CmdAddItem(xBar, model.loadout, component, data.get()),
                    xBar);
        }
        aDragEvent.setDropCompleted(success);
        aDragEvent.consume();
    }

    @FXML
    void onDragOver(DragEvent aDragEvent) {
        final Dragboard db = aDragEvent.getDragboard();

        EquipmentDragUtils.unpackDrag(db, Item.class).ifPresent(aItem -> {
            if (EquipResult.SUCCESS == model.loadout.canEquipDirectly(aItem)
                    && EquipResult.SUCCESS == component.canEquip(aItem)) {
                aDragEvent.acceptTransferModes(TransferMode.COPY_OR_MOVE);
            }
        });
        aDragEvent.consume();
    }

    @FXML
    void onDragStart(MouseEvent aMouseEvent) throws Exception {
        final Item item = itemView.getSelectionModel().getSelectedItem();
        if (component.canRemoveItem(item)) {
            final Dragboard db = itemView.startDragAndDrop(TransferMode.MOVE);
            EquipmentDragUtils.doDrag(db, item);
            stack.pushAndApply(new CmdRemoveItem(xBar, model.loadout, component, item));
        }
        aMouseEvent.consume();
    }

    @FXML
    void onEquipmentClicked(MouseEvent aEvent) throws Exception {
        if (FxControlUtils.isDoubleClick(aEvent)) {
            if (aEvent.getSource() == itemView) {
                final Item item = itemView.getSelectionModel().getSelectedItem();
                if (item != null && component.canRemoveItem(item)) {
                    stack.pushAndApply(new CmdRemoveItem(xBar, model.loadout, component, item));
                }
            }
        }
    }

    private void setupArmours() {
        if (location.isTwoSided()) {
            setupArmourSpinner(ArmourSide.FRONT, armourSpinner, armourMax);
            setupArmourSpinner(ArmourSide.BACK, armourSpinnerBack, armourMaxBack);
            armourIcon.getStyleClass().setAll(StyleManager.CLASS_ARMOR_FRONT, StyleManager.CLASS_ICON_MEDIUM);
            armourBackIcon.getStyleClass().setAll(StyleManager.CLASS_ARMOR_BACK, StyleManager.CLASS_ICON_MEDIUM);
        }
        else {
            setupArmourSpinner(ArmourSide.ONLY, armourSpinner, armourMax);
            armourIcon.getStyleClass().setAll(StyleManager.CLASS_ARMOR, StyleManager.CLASS_ICON_MEDIUM);
            container.getChildren().remove(armourBackIcon.getParent());
            container.getChildren().remove(armourSpinnerBack);
            container.getChildren().remove(armourMaxBack);
        }
    }

    private void setupArmourSpinner(ArmourSide aSide, Spinner<Integer> aSpinner, Labeled aMaxLabel) {
        final ComponentModel componentModel = model.components.get(location);
        final ArmourFactory af = new ArmourFactory(xBar, model.loadout, component, aSide, stack, aSpinner);
        af.manualSetProperty().addListener((aObservable, aOld, aNew) -> {
            aSpinner.pseudoClassStateChanged(StyleManager.PC_AUTOARMOUR, !aNew.booleanValue());
            aMaxLabel.pseudoClassStateChanged(StyleManager.PC_AUTOARMOUR, !aNew.booleanValue());
        });
        aSpinner.pseudoClassStateChanged(StyleManager.PC_AUTOARMOUR, !af.getManualSet());
        aSpinner.setValueFactory(af);
        aSpinner.setContextMenu(armourContextMenu);

        aMaxLabel.pseudoClassStateChanged(StyleManager.PC_AUTOARMOUR, !af.getManualSet());

        final NumberExpression armourMaxBinding = aSide == ArmourSide.BACK ? componentModel.armourMaxBack
                : componentModel.armourMax;
        final NumberExpression armourEffBinding = aSide == ArmourSide.BACK ? componentModel.armourEffBack
                : componentModel.armourEff;
        final NumberExpression armourBinding = aSide == ArmourSide.BACK ? componentModel.armourBack
                : componentModel.armour;
        final NumberExpression armourBonus = armourEffBinding.subtract(armourBinding);
        final StringBinding formatBinding = when(armourBonus.isEqualTo(0)).then(format(" /%d", armourMaxBinding))
                .otherwise(format(" /%d %+d", armourMaxBinding, armourBonus));
        aMaxLabel.textProperty().bind(formatBinding);
        aMaxLabel.setContextMenu(armourContextMenu);
    }

    private void setupItemView(DynamicSlotDistributor aDistributor, ItemToolTipFormatter aTooltipFormatter,
            LoadoutFactory aLoadoutFactory) {
        itemView.setVisibleRows(component.getInternalComponent().getSlots());
        itemView.setItems(new EquippedItemsList(xBar, component, aDistributor));
        itemView.setCellFactory((aList) -> {
            return new EquippedItemCell((FixedRowsListView<Item>) aList, component, model.loadout, stack, xBar,
                    aTooltipFormatter, settings.getBoolean(Settings.UI_PGI_COMPATIBILITY).getValue(), aLoadoutFactory,
                    settings);
        });
        itemView.setPrefWidth(ITEM_WIDTH);
    }

    private void setupOmniPods() {
        if (component instanceof ConfiguredComponentOmniMech) {
            final ConfiguredComponentOmniMech componentOmniMech = (ConfiguredComponentOmniMech) component;

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

            final DoubleBinding padding = selectDouble(container.paddingProperty(), "left")
                    .add(selectDouble(container.paddingProperty(), "right"));

            omniPodSelection.maxWidthProperty().bind(container.widthProperty().subtract(padding));
            omniPodSelection.getSelectionModel().selectedItemProperty().addListener((aObservable, aOld, aNew) -> {
                LiSongMechLab.safeCommand(root, stack,
                        new CmdSetOmniPod(xBar, (LoadoutOmniMech) model.loadout, componentOmniMech, aNew), xBar);
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
        final LoadoutOmniMech loadoutOmni = (LoadoutOmniMech) model.loadout;
        final ConfiguredComponentOmniMech componentOmniMech = (ConfiguredComponentOmniMech) component;
        FxControlUtils.bindTogglable(aButton, aToggleProperty, aValue -> LiSongMechLab.safeCommand(aButton, stack,
                new CmdToggleItem(xBar, loadoutOmni, componentOmniMech, aItem, aValue), xBar));
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
}
