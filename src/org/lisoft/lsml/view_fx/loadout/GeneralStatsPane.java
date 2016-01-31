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
package org.lisoft.lsml.view_fx.loadout;

import static javafx.beans.binding.Bindings.format;

import org.lisoft.lsml.model.chassi.ChassisBase;
import org.lisoft.lsml.view_fx.properties.LoadoutModelAdaptor;
import org.lisoft.lsml.view_fx.util.FxmlHelpers;

import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressBar;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Pane;

/**
 * This control shows all the stats for a loadout in one convenient place.
 * 
 * @author Li Song
 */
public class GeneralStatsPane extends GridPane {
    @FXML
    private ProgressBar generalArmorBar;
    @FXML
    private Label       generalArmorLabel;
    @FXML
    private ProgressBar generalMassBar;
    @FXML
    private Label       generalMassLabel;
    @FXML
    private ProgressBar generalSlotsBar;
    @FXML
    private Label       generalSlotsLabel;
    @FXML
    private Label       generalMassOverlay;
    @FXML
    private Label       generalSlotsOverlay;
    @FXML
    private Label       generalArmorOverlay;

    public GeneralStatsPane(LoadoutModelAdaptor aModel) {
        FxmlHelpers.loadFxmlControl(this);
        ChassisBase chassis = aModel.loadout.getChassis();
        int massMax = chassis.getMassMax();

        Pane parent = (Pane) generalMassBar.getParent();
        generalMassBar.progressProperty().bind(aModel.statsMass.divide(massMax));
        generalMassBar.prefWidthProperty().bind(parent.widthProperty());
        generalMassLabel.textProperty().bind(format("%.2f free", aModel.statsFreeMass));
        generalMassOverlay.textProperty().bind(format("%.2f / %d", aModel.statsMass, massMax));

        int armorMax = chassis.getArmorMax();
        generalArmorBar.progressProperty().bind(aModel.statsArmor.divide((double) armorMax));
        generalArmorBar.prefWidthProperty().bind(parent.widthProperty());
        generalArmorLabel.textProperty().bind(format("%d free", aModel.statsArmorFree));
        generalArmorOverlay.textProperty().bind(format("%d / %d", aModel.statsArmor, armorMax));

        int criticalSlotsTotal = chassis.getCriticalSlotsTotal();
        generalSlotsBar.progressProperty().bind(aModel.statsSlots.divide((double) criticalSlotsTotal));
        generalSlotsBar.prefWidthProperty().bind(parent.widthProperty());
        generalSlotsLabel.textProperty().bind(format("%d free", aModel.statsSlots.negate().add(criticalSlotsTotal)));
        generalSlotsOverlay.textProperty().bind(format("%d / %d", aModel.statsSlots, criticalSlotsTotal));
    }
}
