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

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.lisoft.lsml.messages.ItemMessage;
import org.lisoft.lsml.messages.LoadoutMessage;
import org.lisoft.lsml.messages.LoadoutMessage.Type;
import org.lisoft.lsml.messages.Message;
import org.lisoft.lsml.messages.MessageReceiver;
import org.lisoft.lsml.messages.MessageXBar;
import org.lisoft.lsml.model.item.Weapon;
import org.lisoft.lsml.model.loadout.LoadoutBase;
import org.lisoft.lsml.model.loadout.WeaponGroups;
import org.lisoft.lsml.view_fx.controls.FixedRowsTableView;
import org.lisoft.lsml.view_fx.properties.LoadoutMetricsModelAdaptor;
import org.lisoft.lsml.view_fx.util.FxmlHelpers;

import javafx.beans.binding.DoubleBinding;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.ReadOnlyStringWrapper;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.TableColumn;
import javafx.scene.control.cell.CheckBoxTableCell;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;

/**
 * A control that displays stats for a weapon group.
 * 
 * @author Emily Björk
 */
public class WeaponLabPane extends HBox implements MessageReceiver {

    class WeaponState {
        private final BooleanProperty[] groupState;
        private final Weapon            weapon;

        public WeaponState(Weapon aWeapon, int aWeaponIndex) {
            weapon = aWeapon;
            groupState = new SimpleBooleanProperty[WeaponGroups.MAX_GROUPS];
            for (int i = 0; i < WeaponGroups.MAX_GROUPS; ++i) {
                final int group = i;
                groupState[i] = new SimpleBooleanProperty(weaponGroups.isInGroup(group, aWeaponIndex));
                groupState[i].addListener((aObservable, aOld, aNew) -> {
                    weaponGroups.setGroup(group, aWeaponIndex, aNew.booleanValue());
                    xBar.post(new LoadoutMessage(loadout, LoadoutMessage.Type.WEAPON_GROUPS_CHANGED));
                });
            }
        }
    }

    @FXML
    private FixedRowsTableView<WeaponState> weaponGroupTable;
    @FXML
    private VBox                            leftColumn;
    private final WeaponGroups              weaponGroups;
    private final MessageXBar               xBar;
    private final LoadoutBase<?>            loadout;
    private final List<WeaponGroupPane>     wpnGroupPanes = new ArrayList<>();

    public WeaponLabPane(MessageXBar aXBar, LoadoutBase<?> aLoadout, LoadoutMetricsModelAdaptor aMetrics)
            throws IOException {
        FxmlHelpers.loadFxmlControl(this);
        aXBar.attach(this);

        for (int i = 0; i < WeaponGroups.MAX_GROUPS; ++i) {
            WeaponGroupPane weaponGroupPane = new WeaponGroupPane(aMetrics, i);
            leftColumn.getChildren().add(weaponGroupPane);
            wpnGroupPanes.add(weaponGroupPane);
        }
        loadout = aLoadout;
        xBar = aXBar;
        weaponGroups = aLoadout.getWeaponGroups();

        weaponGroupTable.setVisibleRows(5);
        weaponGroupTable.getColumns().clear();
        TableColumn<WeaponState, String> nameCol = new TableColumn<>("Weapon");
        nameCol.setCellValueFactory(aFeature -> new ReadOnlyStringWrapper(aFeature.getValue().weapon.getName()));
        double padding = 2; // FIXME: Figure out how to do this correctly without this ugly magic.
        DoubleBinding colWidthSum = nameCol.widthProperty().add(padding);
        weaponGroupTable.getColumns().add(nameCol);

        for (int i = 0; i < WeaponGroups.MAX_GROUPS; ++i) {
            final int group = i;
            TableColumn<WeaponState, Boolean> col = new TableColumn<>(Integer.toString(group + 1));
            col.setCellValueFactory(aFeature -> aFeature.getValue().groupState[group]);
            col.setCellFactory(CheckBoxTableCell.forTableColumn(col));
            col.setEditable(true);
            colWidthSum = colWidthSum.add(col.widthProperty().add(padding));
            weaponGroupTable.getColumns().add(col);
        }
        weaponGroupTable.setEditable(true);
        leftColumn.maxWidthProperty().bind(colWidthSum);
        update();
    }

    @Override
    public void receive(Message aMsg) {
        if (aMsg instanceof ItemMessage) {
            ItemMessage itemMessage = (ItemMessage) aMsg;
            if (itemMessage.item instanceof Weapon) {
                update();
            }
        }
        else if (aMsg instanceof LoadoutMessage) {
            LoadoutMessage loadoutMessage = (LoadoutMessage) aMsg;
            if (loadoutMessage.type == Type.WEAPON_GROUPS_CHANGED) {
                updateGroups();
            }
        }
    }

    private void update() {
        ObservableList<WeaponState> states = FXCollections.observableArrayList();
        List<Weapon> weapons = weaponGroups.getWeaponOrder(loadout);
        for (int weapon = 0; weapon < weapons.size(); ++weapon) {
            states.add(new WeaponState(weapons.get(weapon), weapon));
        }
        weaponGroupTable.setItems(states);
        weaponGroupTable.setVisibleRows(states.size());
        updateGroups();
    }

    private void updateGroups() {
        for (int group = 0; group < WeaponGroups.MAX_GROUPS; ++group) {
            wpnGroupPanes.get(group).setDisable(weaponGroups.getWeapons(group, loadout).isEmpty());
        }
    }
}
