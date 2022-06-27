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
package org.lisoft.lsml.view_fx.controllers.loadoutwindow;

import javafx.fxml.FXML;
import javafx.scene.input.DragEvent;
import javafx.scene.input.Dragboard;
import javafx.scene.input.TransferMode;
import org.lisoft.lsml.command.CmdAddModule;
import org.lisoft.lsml.messages.MessageXBar;
import org.lisoft.lsml.model.item.Consumable;
import org.lisoft.lsml.model.loadout.EquipResult;
import org.lisoft.lsml.model.loadout.Loadout;
import org.lisoft.lsml.util.CommandStack;
import org.lisoft.lsml.view_fx.LiSongMechLab;
import org.lisoft.lsml.view_fx.controllers.AbstractFXController;
import org.lisoft.lsml.view_fx.controls.EquippedConsumablesList;
import org.lisoft.lsml.view_fx.controls.EquippedModuleCell;
import org.lisoft.lsml.view_fx.controls.FixedRowsListView;
import org.lisoft.lsml.view_fx.properties.LoadoutModelAdaptor;
import org.lisoft.lsml.view_fx.util.EquipmentDragUtils;

import java.util.Optional;

/**
 * A controller for the LoadoutComponent.fxml view.
 *
 * @author Li Song
 */
public class ModulePaneController extends AbstractFXController {

    private final Loadout loadout;
    private final MessageXBar messageDelivery;
    private final CommandStack stack;
    @FXML
    private FixedRowsListView<Consumable> consumablesView;

    /**
     * Updates this module pane controller to show the matching contents.
     *
     * @param aMessageDelivery A message delivery to use for catching updates.
     * @param aStack           A {@link CommandStack} to use for effecting changes.
     * @param aModel           A {@link LoadoutModelAdaptor} to display data for.
     * @param aPgiMode         <code>true</code> if PGI mode is enabled.
     */
    public ModulePaneController(MessageXBar aMessageDelivery, CommandStack aStack, LoadoutModelAdaptor aModel,
                                boolean aPgiMode) {
        messageDelivery = aMessageDelivery;
        loadout = aModel.loadout;
        stack = aStack;

        consumablesView.setVisibleRows(loadout.getConsumablesMax());
        consumablesView.setPrefWidth(ComponentPaneController.ITEM_WIDTH);
        consumablesView.setItems(new EquippedConsumablesList(messageDelivery, loadout));
        consumablesView.setCellFactory(
                listView -> new EquippedModuleCell(consumablesView, stack, messageDelivery, loadout));

    }

    @FXML
    public void onDragDropped(DragEvent aDragEvent) {
        final Dragboard db = aDragEvent.getDragboard();
        final Optional<Consumable> data = EquipmentDragUtils.unpackDrag(db, Consumable.class);
        boolean success = false;
        if (data.isPresent()) {
            success = LiSongMechLab.safeCommand(root, stack, new CmdAddModule(messageDelivery, loadout, data.get()),
                                                messageDelivery);
        }
        aDragEvent.setDropCompleted(success);
        aDragEvent.consume();
    }

    @FXML
    public void onDragOver(DragEvent aDragEvent) {
        final Dragboard db = aDragEvent.getDragboard();
        EquipmentDragUtils.unpackDrag(db, Consumable.class).ifPresent(aModule -> {
            if (EquipResult.SUCCESS == loadout.canAddModule(aModule)) {
                aDragEvent.acceptTransferModes(TransferMode.COPY_OR_MOVE);
            }
        });
        aDragEvent.consume();
    }
}
