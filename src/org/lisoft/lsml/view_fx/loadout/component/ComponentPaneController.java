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

import org.lisoft.lsml.command.CmdAddItem;
import org.lisoft.lsml.command.CmdRemoveItem;
import org.lisoft.lsml.command.CmdToggleItem;
import org.lisoft.lsml.messages.MessageXBar;
import org.lisoft.lsml.model.chassi.Location;
import org.lisoft.lsml.model.datacache.ItemDB;
import org.lisoft.lsml.model.item.Item;
import org.lisoft.lsml.model.loadout.EquipResult;
import org.lisoft.lsml.model.loadout.LoadoutBase;
import org.lisoft.lsml.model.loadout.component.ConfiguredComponentBase;
import org.lisoft.lsml.model.loadout.component.ConfiguredComponentOmniMech;
import org.lisoft.lsml.util.CommandStack;
import org.lisoft.lsml.view_fx.LiSongMechLab;

import javafx.beans.property.BooleanPropertyBase;
import javafx.fxml.FXML;
import javafx.scene.control.TitledPane;
import javafx.scene.control.ToggleButton;
import javafx.scene.input.DragEvent;
import javafx.scene.input.Dragboard;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;
import javafx.scene.input.TransferMode;

/**
 * @author Emily Björk
 *
 */
public class ComponentPaneController {

    public static final int         ITEM_WIDTH = 150;
    @FXML
    private TitledPane              rootPane;
    @FXML
    private ItemView<Item>          itemView;
    @FXML
    private ToggleButton            toggleLAA;
    @FXML
    private ToggleButton            toggleHA;

    private CommandStack            stack;
    private LoadoutBase<?>          loadout;
    private MessageXBar             xBar;
    private Location                location;
    private ConfiguredComponentBase component;

    public void setComponent(MessageXBar aMessageDelivery, CommandStack aStack, LoadoutBase<?> aLoadout,
            Location aLocation) {
        stack = aStack;
        loadout = aLoadout;
        location = aLocation;
        xBar = aMessageDelivery;
        component = loadout.getComponent(location);

        setupTogglable(toggleLAA, ItemDB.LAA);
        setupTogglable(toggleHA, ItemDB.HA);
        setupItemView();
        setupTitle();
    }

    private void setupTitle() {
        rootPane.setText(location.longName() + " (" + (int) component.getInternalComponent().getHitPoints() + " hp)");
    }

    private void setupItemView() {
        itemView.setVisibleRows(component.getInternalComponent().getSlots());
        itemView.setItems(new ComponentItemsList(xBar, loadout, location));
        itemView.setCellFactory((aList) -> {
            return new ComponentItemsCell((ItemView<Item>) aList);
        });

        itemView.setPrefWidth(ITEM_WIDTH);
    }

    private void setupTogglable(ToggleButton aButton, Item aTogglee) {
        if (component instanceof ConfiguredComponentOmniMech) {
            ConfiguredComponentOmniMech componentOmniMech = (ConfiguredComponentOmniMech) component;

            if (componentOmniMech.getOmniPod().getToggleableItems().contains(aTogglee)) {
                aButton.setVisible(true);
                aButton.selectedProperty().bindBidirectional(new BooleanPropertyBase() {

                    @Override
                    public String getName() {
                        return "Toggle " + aTogglee.getName();
                    }

                    @Override
                    public Object getBean() {
                        return null;
                    }

                    @Override
                    public boolean get() {
                        return componentOmniMech.getToggleState(aTogglee);
                    }

                    @Override
                    public void set(boolean aValue) {
                        try {
                            stack.pushAndApply(new CmdToggleItem(xBar, loadout, componentOmniMech, aTogglee, aValue));
                        }
                        catch (Exception e) {
                            LiSongMechLab.showError(e);
                        }
                    }
                });

                aButton.disableProperty().bind(new BooleanPropertyBase() {
                    @Override
                    public String getName() {
                        return "Disable toggle for " + aTogglee.getName();
                    }

                    @Override
                    public Object getBean() {
                        return null;
                    }

                    @Override
                    public boolean get() {
                        return EquipResult.SUCCESS != componentOmniMech.canToggleOn(aTogglee);
                    }
                });
                return;
            }
        }
        aButton.setVisible(false);
    }

    @FXML
    void onEquipmentClicked(MouseEvent aEvent) throws EquipResult, Exception {
        if (aEvent.getButton() == MouseButton.PRIMARY && aEvent.getClickCount() == 2) {
            if (aEvent.getSource() == itemView) {
                Item item = itemView.getSelectionModel().getSelectedItem();
                if (item != null && component.canRemoveItem(item)) {
                    stack.pushAndApply(new CmdRemoveItem(xBar, loadout, component, item));
                }
            }
        }
    }

    @FXML
    void onDragStart(MouseEvent aMouseEvent) throws EquipResult, Exception {
        Item item = itemView.getSelectionModel().getSelectedItem();
        if (component.canRemoveItem(item)) {
            Dragboard db = itemView.startDragAndDrop(TransferMode.MOVE);
            LiSongMechLab.addItemDrag(db, item);
            stack.pushAndApply(new CmdRemoveItem(xBar, loadout, component, item));
        }
        aMouseEvent.consume();
    }

    @FXML
    void onDragOver(DragEvent aDragEvent) {
        Dragboard db = aDragEvent.getDragboard();
        if (db.hasString()) {
            try {
                Item item = ItemDB.lookup(Integer.parseInt(db.getString()));
                if (EquipResult.SUCCESS == loadout.canEquipDirectly(item) && EquipResult.SUCCESS == component.canEquip(item)) {
                    aDragEvent.acceptTransferModes(TransferMode.COPY_OR_MOVE);
                }
            }
            catch (Throwable t) {
                // User dragging junk, ignore it.
            }
        }
        aDragEvent.consume();
    }

    @FXML
    void onDragDropped(DragEvent aDragEvent) {
        Dragboard db = aDragEvent.getDragboard();
        boolean success = false;
        if (db.hasString()) {
            try {
                Item item = ItemDB.lookup(Integer.parseInt(db.getString()));
                stack.pushAndApply(new CmdAddItem(xBar, loadout, component, item));
                success = true;
            }
            catch (Exception e) {
                LiSongMechLab.showError(e);
            }
        }
        aDragEvent.setDropCompleted(success);
        aDragEvent.consume();
    }
}
