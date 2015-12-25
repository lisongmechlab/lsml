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
import org.lisoft.lsml.view_fx.controls.ItemView;
import org.lisoft.lsml.view_fx.drawers.EquippedModuleCell;

import javafx.fxml.FXML;
import javafx.scene.input.DragEvent;
import javafx.scene.input.Dragboard;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;
import javafx.scene.input.TransferMode;

/**
 * Controller for the modules pane that shows which modules are equipped on the loadout.
 * 
 * @author Li Song
 */
public class ModulePaneController {
    @FXML
    private ItemView<PilotModule>                  consumablesView;
    @FXML
    private ItemView<PilotModule>                  mechModulesView;
    @FXML
    private ItemView<PilotModule>                  weaponModulesView;
    @FXML
    private ItemView<PilotModule>                  masterSlotView;

    private Map<ModuleSlot, ItemView<PilotModule>> moduleViews;
    private MessageXBar                            messageDelivery;
    private LoadoutBase<?>                         loadout;
    private CommandStack                           stack;

    /**
     * Updates this module pane controller to show the matching contents.
     * 
     * @param aMessageDelivery
     *            A message delivery to use for catching updates.
     * @param aStack
     *            A {@link CommandStack} to use for effecting changes.
     * @param aLoadout
     *            A {@link LoadoutBase} to display data for.
     */
    public void setLoadout(MessageXBar aMessageDelivery, CommandStack aStack, LoadoutBase<?> aLoadout) {
        messageDelivery = aMessageDelivery;
        loadout = aLoadout;
        stack = aStack;

        moduleViews = new HashMap<>();
        moduleViews.put(ModuleSlot.CONSUMABLE, consumablesView);
        moduleViews.put(ModuleSlot.MECH, mechModulesView);
        moduleViews.put(ModuleSlot.WEAPON, weaponModulesView);
        moduleViews.put(ModuleSlot.HYBRID, masterSlotView);

        for (ModuleSlot slot : ModuleSlot.values()) {
            ItemView<PilotModule> view = moduleViews.get(slot);
            view.setVisibleRows(loadout.getModulesMax(slot));
            view.setPrefWidth(ComponentPaneController.ITEM_WIDTH);
            view.setItems(new EquippedModulesList(aMessageDelivery, loadout, slot));
            view.setCellFactory(listView -> new EquippedModuleCell(view));
        }
    }

    private ItemView<PilotModule> selectView(Object aRaw) {
        for (ItemView<PilotModule> view : moduleViews.values()) {
            if (view == aRaw)
                return view;
        }
        return null;
    }

    @FXML
    public void onDragStart(MouseEvent aMouseEvent) throws Exception {
        ItemView<PilotModule> view = selectView(aMouseEvent.getSource());
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
                stack.pushAndApply(new CmdAddModule(messageDelivery, loadout, item));
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
            ItemView<PilotModule> view = selectView(aEvent.getSource());
            if (view != null) {
                PilotModule item = view.getSelectionModel().getSelectedItem();
                if (item != null) {
                    stack.pushAndApply(new CmdRemoveModule(messageDelivery, loadout, item));
                }
            }
        }
    }

}
