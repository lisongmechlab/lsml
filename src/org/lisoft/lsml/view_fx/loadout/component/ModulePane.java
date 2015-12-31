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
import java.util.HashMap;
import java.util.Map;

import org.lisoft.lsml.command.CmdAddModule;
import org.lisoft.lsml.command.CmdRemoveModule;
import org.lisoft.lsml.messages.MessageXBar;
import org.lisoft.lsml.model.datacache.PilotModuleDB;
import org.lisoft.lsml.model.item.ModuleSlot;
import org.lisoft.lsml.model.item.PilotModule;
import org.lisoft.lsml.model.loadout.EquipResult;
import org.lisoft.lsml.model.loadout.LoadoutBase;
import org.lisoft.lsml.util.CommandStack;
import org.lisoft.lsml.view_fx.LiSongMechLab;
import org.lisoft.lsml.view_fx.controls.FixedRowsListView;
import org.lisoft.lsml.view_fx.drawers.EquippedModuleCell;
import org.lisoft.lsml.view_fx.properties.LoadoutModelAdaptor;
import org.lisoft.lsml.view_fx.util.FxmlHelpers;

import javafx.fxml.FXML;
import javafx.scene.control.TitledPane;
import javafx.scene.input.DragEvent;
import javafx.scene.input.Dragboard;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;
import javafx.scene.input.TransferMode;

/**
 * A controller for the LoadoutComponent.fxml view.
 * 
 * @author Emily Björk
 */
public class ModulePane extends TitledPane {

    @FXML
    private FixedRowsListView<PilotModule>                        consumablesView;
    @FXML
    private FixedRowsListView<PilotModule>                        mechModulesView;
    @FXML
    private FixedRowsListView<PilotModule>                        weaponModulesView;
    @FXML
    private FixedRowsListView<PilotModule>                        masterSlotView;

    private final Map<ModuleSlot, FixedRowsListView<PilotModule>> moduleViews = new HashMap<>();
    private final MessageXBar                                     messageDelivery;
    private final LoadoutBase<?>                                  loadout;
    private final CommandStack                                    stack;

    /**
     * Updates this module pane controller to show the matching contents.
     * 
     * @param aMessageDelivery
     *            A message delivery to use for catching updates.
     * @param aStack
     *            A {@link CommandStack} to use for effecting changes.
     * @param aModel
     *            A {@link LoadoutModelAdaptor} to display data for.
     * @throws IOException
     *             If loading the control failed.
     */
    public ModulePane(MessageXBar aMessageDelivery, CommandStack aStack, LoadoutModelAdaptor aModel)
            throws IOException {
        FxmlHelpers.loadFxmlControl(this);
        messageDelivery = aMessageDelivery;
        loadout = aModel.loadout;
        stack = aStack;
        moduleViews.put(ModuleSlot.CONSUMABLE, consumablesView);
        moduleViews.put(ModuleSlot.MECH, mechModulesView);
        moduleViews.put(ModuleSlot.WEAPON, weaponModulesView);
        moduleViews.put(ModuleSlot.HYBRID, masterSlotView);

        for (ModuleSlot slot : ModuleSlot.values()) {
            FixedRowsListView<PilotModule> view = moduleViews.get(slot);
            view.setVisibleRows(loadout.getModulesMax(slot));
            view.setPrefWidth(ComponentPane.ITEM_WIDTH);
            view.setItems(new EquippedModulesList(aMessageDelivery, loadout, slot));
            view.setCellFactory(listView -> new EquippedModuleCell(view));
        }
    }

    private FixedRowsListView<PilotModule> selectView(Object aRaw) {
        for (FixedRowsListView<PilotModule> view : moduleViews.values()) {
            if (view == aRaw)
                return view;
        }
        return null;
    }

    @FXML
    public void onDragStart(MouseEvent aMouseEvent) throws Exception {
        FixedRowsListView<PilotModule> view = selectView(aMouseEvent.getSource());
        if (view != null) {
            PilotModule item = view.getSelectionModel().getSelectedItem();
            Dragboard db = view.startDragAndDrop(TransferMode.MOVE);
            LiSongMechLab.addEquipmentDrag(db, item);
            stack.pushAndApply(new CmdRemoveModule(messageDelivery, loadout, item));
        }

        aMouseEvent.consume();
    }

    @FXML
    public void onDragDropped(DragEvent aDragEvent) {
        Dragboard db = aDragEvent.getDragboard();
        boolean success = false;
        if (db.hasString()) {
            try {
                PilotModule item = PilotModuleDB.lookup(Integer.parseInt(db.getString()));
                success = LiSongMechLab.safeCommand(stack, new CmdAddModule(messageDelivery, loadout, item));
            }
            catch (Throwable e) {
                // Swallow it, junk was dragged over us.
            }
        }
        aDragEvent.setDropCompleted(success);
        aDragEvent.consume();
    }

    @FXML
    public void onDragOver(DragEvent aDragEvent) {
        Dragboard db = aDragEvent.getDragboard();
        if (db.hasString()) {
            try {
                PilotModule item = PilotModuleDB.lookup(Integer.parseInt(db.getString()));
                if (EquipResult.SUCCESS == loadout.canAddModule(item)) {
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
    public void onEquipmentClicked(MouseEvent aEvent) throws Exception {
        if (aEvent.getButton() == MouseButton.PRIMARY && aEvent.getClickCount() == 2) {
            FixedRowsListView<PilotModule> view = selectView(aEvent.getSource());
            if (view != null) {
                PilotModule item = view.getSelectionModel().getSelectedItem();
                if (item != null) {
                    stack.pushAndApply(new CmdRemoveModule(messageDelivery, loadout, item));
                }
            }
        }
    }
}
