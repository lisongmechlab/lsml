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
package org.lisoft.lsml.view_fx;

import java.io.IOException;
import java.util.Optional;

import org.lisoft.lsml.command.CmdAddLoadoutToGarage;
import org.lisoft.lsml.command.CmdRemoveLoadoutFromGarage;
import org.lisoft.lsml.command.CmdSetName;
import org.lisoft.lsml.messages.GarageMessage;
import org.lisoft.lsml.messages.GarageMessage.Type;
import org.lisoft.lsml.messages.Message;
import org.lisoft.lsml.messages.MessageReceiver;
import org.lisoft.lsml.messages.MessageXBar;
import org.lisoft.lsml.model.chassi.ChassisClass;
import org.lisoft.lsml.model.export.Base64LoadoutCoder;
import org.lisoft.lsml.model.garage.MechGarage;
import org.lisoft.lsml.model.item.Faction;
import org.lisoft.lsml.model.loadout.DefaultLoadoutFactory;
import org.lisoft.lsml.model.loadout.LoadoutBase;
import org.lisoft.lsml.util.CommandStack;

import javafx.beans.property.SimpleObjectProperty;
import javafx.collections.transformation.FilteredList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.ButtonType;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.scene.control.TextInputDialog;
import javafx.scene.control.TitledPane;
import javafx.scene.input.Clipboard;
import javafx.scene.input.ClipboardContent;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;

/**
 * This is a controller class for the list view that shows a class of mech loadouts (i.e. light, medium, etc) in the
 * garage window.
 * 
 * @author Li Song
 */
public class GarageListViewController implements MessageReceiver {
    @FXML
    private TitledPane               root;

    @FXML
    private ListView<LoadoutBase<?>> contents;

    private CommandStack             cmdStack;
    private MessageXBar              xBar;
    private MechGarage               garage;

    public void initialize(ChassisClass aChassisClass, MessageXBar aXBar, CommandStack aCmdStack,
            SimpleObjectProperty<Faction> aFilteredFaction) {
        xBar = aXBar;
        xBar.attach(this);
        cmdStack = aCmdStack;

        FilteredList<LoadoutBase<?>> filteredLoadouts = new FilteredList<>(
                new ObservableGarageList(aChassisClass, aXBar));

        aFilteredFaction.addListener((aObserved, aOldValue, aNewValue) -> {
            filteredLoadouts.setPredicate(loadout -> {
                return loadout.getChassis().getFaction().isCompatible(aNewValue);
            });
        });

        contents.setItems(filteredLoadouts);
        root.setText(aChassisClass.toString());

        fixContextMenu();
    }

    private void fixContextMenu() {
        // "Steal" the context menu from the ListView and set it on the children.
        final ContextMenu menu = contents.getContextMenu();
        contents.setContextMenu(null);
        contents.setCellFactory(lv -> {
            ListCell<LoadoutBase<?>> cell = new ListCell<LoadoutBase<?>>() {
                @Override
                public void updateItem(LoadoutBase<?> item, boolean empty) {
                    super.updateItem(item, empty);
                    setText(empty ? null : item.toString());
                }
            };
            cell.emptyProperty().addListener((obs, wasEmpty, isNowEmpty) -> {
                cell.setContextMenu(isNowEmpty ? null : menu);
            });
            return cell;
        });
    }

    private LoadoutBase<?> getSelected() {
        return contents.getSelectionModel().getSelectedItem();
    }

    @FXML
    public void contentsClick(MouseEvent aEvent) throws IOException {
        if (aEvent.getButton() == MouseButton.PRIMARY && aEvent.getClickCount() >= 2) {
            LiSongMechLab.openLoadout(getSelected(), garage);
        }
    }

    @FXML
    public void contextMenuOpen(@SuppressWarnings("unused") ActionEvent aEvent) throws IOException {
        LiSongMechLab.openLoadout(getSelected(), garage);
    }

    @FXML
    public void contextMenuRename(@SuppressWarnings("unused") ActionEvent aEvent) throws Exception {
        LoadoutBase<?> selected = getSelected();

        TextInputDialog dialog = new TextInputDialog(selected.getName());
        Optional<String> result = dialog.showAndWait();
        if (result.isPresent() && !result.get().isEmpty()) {
            cmdStack.pushAndApply(new CmdSetName(selected, xBar, result.get()));
        }
    }

    @FXML
    public void contextMenuClone(@SuppressWarnings("unused") ActionEvent aEvent) throws Exception {
        LoadoutBase<?> selected = getSelected();
        LoadoutBase<?> copy = DefaultLoadoutFactory.instance.produceClone(selected);
        copy.rename(copy.getName() + " (copy)");

        cmdStack.pushAndApply(new CmdAddLoadoutToGarage(garage, copy));
    }

    @FXML
    public void contextMenuDelete(@SuppressWarnings("unused") ActionEvent aEvent) throws Exception {
        LoadoutBase<?> selected = getSelected();

        Alert alert = new Alert(AlertType.CONFIRMATION, "Are you sure you want to delete: " + selected);
        Optional<ButtonType> result = alert.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.OK) {
            cmdStack.pushAndApply(new CmdRemoveLoadoutFromGarage(garage, selected));
        }
    }

    @FXML
    public void contextMenuExport(@SuppressWarnings("unused") ActionEvent aEvent) throws Exception {
        LoadoutBase<?> selected = getSelected();
        Base64LoadoutCoder coder = new Base64LoadoutCoder();
        String trampoline = coder.encodeHttpTrampoline(selected);
        ClipboardContent content = new ClipboardContent();
        content.putString(trampoline);
        Clipboard.getSystemClipboard().setContent(content);
        Alert alert = new Alert(AlertType.INFORMATION, "LSML Link copied to clipboard (Ctrl+V to paste)");
        alert.show();
    }

    @Override
    public void receive(Message aMsg) {
        if (aMsg instanceof GarageMessage) {
            GarageMessage msg = (GarageMessage) aMsg;
            if (msg.type == Type.NewGarage) {
                garage = msg.garage;
            }
        }
    }
}
