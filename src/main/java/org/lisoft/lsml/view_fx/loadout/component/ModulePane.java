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
import java.util.Optional;

import org.lisoft.lsml.command.CmdAddModule;
import org.lisoft.lsml.messages.MessageXBar;
import org.lisoft.lsml.model.item.ModuleSlot;
import org.lisoft.lsml.model.item.PilotModule;
import org.lisoft.lsml.model.loadout.EquipResult;
import org.lisoft.lsml.model.loadout.Loadout;
import org.lisoft.lsml.util.CommandStack;
import org.lisoft.lsml.view_fx.LiSongMechLab;
import org.lisoft.lsml.view_fx.controls.FixedRowsListView;
import org.lisoft.lsml.view_fx.drawers.EquippedModuleCell;
import org.lisoft.lsml.view_fx.properties.LoadoutModelAdaptor;
import org.lisoft.lsml.view_fx.util.EquipmentDragUtils;
import org.lisoft.lsml.view_fx.util.FxControlUtils;

import javafx.fxml.FXML;
import javafx.scene.control.TitledPane;
import javafx.scene.input.DragEvent;
import javafx.scene.input.Dragboard;
import javafx.scene.input.TransferMode;
import javafx.scene.layout.VBox;

/**
 * A controller for the LoadoutComponent.fxml view.
 *
 * @author Li Song
 */
public class ModulePane extends TitledPane {

    @FXML
    private FixedRowsListView<PilotModule> consumablesView;
    @FXML
    private FixedRowsListView<PilotModule> mechModulesView;
    @FXML
    private FixedRowsListView<PilotModule> weaponModulesView;
    @FXML
    private FixedRowsListView<PilotModule> masterSlotView;

    private final Map<ModuleSlot, FixedRowsListView<PilotModule>> moduleViews = new HashMap<>();
    private final MessageXBar messageDelivery;
    private final Loadout loadout;
    private final CommandStack stack;
    @FXML
    private VBox weaponCategory;
    @FXML
    private VBox hybridCategory;
    @FXML
    private VBox mechCategory;
    @FXML
    private VBox consumableCategory;
    @FXML
    private VBox content;

    /**
     * Updates this module pane controller to show the matching contents.
     *
     * @param aMessageDelivery
     *            A message delivery to use for catching updates.
     * @param aStack
     *            A {@link CommandStack} to use for effecting changes.
     * @param aModel
     *            A {@link LoadoutModelAdaptor} to display data for.
     * @param aPgiMode
     *            <code>true</code> if PGI mode is enabled.
     */
    public ModulePane(MessageXBar aMessageDelivery, CommandStack aStack, LoadoutModelAdaptor aModel, boolean aPgiMode) {
        FxControlUtils.loadFxmlControl(this);
        messageDelivery = aMessageDelivery;
        loadout = aModel.loadout;
        stack = aStack;

        moduleViews.put(ModuleSlot.MECH, mechModulesView);
        moduleViews.put(ModuleSlot.WEAPON, weaponModulesView);
        moduleViews.put(ModuleSlot.HYBRID, masterSlotView);
        moduleViews.put(ModuleSlot.CONSUMABLE, consumablesView);

        if (aPgiMode) {
            content.getChildren().setAll(mechCategory, weaponCategory, hybridCategory, consumableCategory);
        }

        for (final ModuleSlot slot : ModuleSlot.values()) {
            final FixedRowsListView<PilotModule> view = moduleViews.get(slot);
            view.setVisibleRows(loadout.getModulesMax(slot));
            view.setPrefWidth(ComponentPane.ITEM_WIDTH);
            view.setItems(new EquippedModulesList(aMessageDelivery, loadout, slot));
            view.setCellFactory(listView -> new EquippedModuleCell(view, aStack, aMessageDelivery, loadout));
        }
    }

    @FXML
    public void onDragDropped(DragEvent aDragEvent) {
        final Dragboard db = aDragEvent.getDragboard();
        final Optional<PilotModule> data = EquipmentDragUtils.unpackDrag(db, PilotModule.class);
        boolean success = false;
        if (data.isPresent()) {
            success = LiSongMechLab.safeCommand(this, stack, new CmdAddModule(messageDelivery, loadout, data.get()),
                    messageDelivery);
        }
        aDragEvent.setDropCompleted(success);
        aDragEvent.consume();
    }

    @FXML
    public void onDragOver(DragEvent aDragEvent) {
        final Dragboard db = aDragEvent.getDragboard();
        EquipmentDragUtils.unpackDrag(db, PilotModule.class).ifPresent(aModule -> {
            if (EquipResult.SUCCESS == loadout.canAddModule(aModule)) {
                aDragEvent.acceptTransferModes(TransferMode.COPY_OR_MOVE);
            }
        });
        aDragEvent.consume();
    }

}
