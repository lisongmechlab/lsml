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

import org.lisoft.lsml.messages.MessageXBar;
import org.lisoft.lsml.model.garage.GarageTwo;
import org.lisoft.lsml.model.loadout.LoadoutBase;

import javafx.scene.control.ListCell;
import javafx.scene.input.MouseButton;

/**
 * @author Li Song
 */
public class LoadoutPillCell extends ListCell<LoadoutBase<?>> {

    private final LoadoutPill pill;

    public LoadoutPillCell(GarageTwo aGarage, MessageXBar aXBar) {
        pill = new LoadoutPill();

        setOnMouseClicked(aEvent -> {
            if (aEvent.getButton() == MouseButton.PRIMARY && aEvent.getClickCount() >= 2) {
                LiSongMechLab.openLoadout(aXBar, getItem(), aGarage);
            }
        });
    }

    @Override
    protected void updateItem(LoadoutBase<?> aItem, boolean aEmpty) {
        super.updateItem(aItem, aEmpty);
        if (aItem != null && !aEmpty) {
            setText(null);
            pill.setLoadout(aItem);
            setGraphic(pill);
        }
        else {
            setText(null);
            setGraphic(null);
        }
    }
}
