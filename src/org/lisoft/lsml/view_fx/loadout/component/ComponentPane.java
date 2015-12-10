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

import java.io.IOException;

import org.lisoft.lsml.messages.MessageXBar;
import org.lisoft.lsml.model.chassi.Location;
import org.lisoft.lsml.model.loadout.LoadoutBase;
import org.lisoft.lsml.util.CommandStack;
import org.lisoft.lsml.view_fx.LiSongMechLab;

import javafx.fxml.FXMLLoader;
import javafx.scene.control.TitledPane;

/**
 * A controller for the LoadoutComponent.fxml view.
 * 
 * @author Li Song
 */
public class ComponentPane extends TitledPane {

    public ComponentPane(MessageXBar aMessageDelivery, CommandStack aStack, LoadoutBase<?> aLoadout,
            Location aLocation) {
        FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("ComponentPane.fxml"));
        try {
            fxmlLoader.setRoot(this);
            fxmlLoader.load();
            ComponentPaneController controller = fxmlLoader.getController();
            controller.setComponent(aMessageDelivery, aStack, aLoadout, aLocation);
        }
        catch (IOException exception) {
            LiSongMechLab.showError(exception);
        }
    }
}
