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
package org.lisoft.lsml.view_fx;

import java.text.DecimalFormat;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import org.lisoft.lsml.model.chassi.Chassis;
import org.lisoft.lsml.model.item.ECM;
import org.lisoft.lsml.model.item.Engine;
import org.lisoft.lsml.model.item.Item;
import org.lisoft.lsml.model.item.JumpJet;
import org.lisoft.lsml.model.item.Weapon;
import org.lisoft.lsml.model.loadout.Loadout;
import org.lisoft.lsml.model.metrics.TopSpeed;
import org.lisoft.lsml.view_fx.style.StyleManager;
import org.lisoft.lsml.view_fx.util.FxmlHelpers;

import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Region;

/**
 * This class shows a summary of a loadout inside of a "pill".
 * 
 * @author Emily Björk
 */
public class LoadoutPill extends GridPane {
    @FXML
    private Label name;
    @FXML
    private Label chassis;
    @FXML
    private Label speed;
    @FXML
    private Label armor;
    @FXML
    private HBox equipment;
    @FXML
    private Region icon;
    private final static DecimalFormat df = new DecimalFormat("Speed: #.# kph");

    public LoadoutPill() {
        FxmlHelpers.loadFxmlControl(this);
        // icon.prefWidthProperty().bind(prefHeightProperty());
        // icon.prefHeightProperty().bind(prefHeightProperty());
    }

    public void setLoadout(Loadout aLoadout) {
        name.setText(aLoadout.getName());
        Chassis chassisBase = aLoadout.getChassis();
        int massMax = chassisBase.getMassMax();
        chassis.setText(aLoadout.getChassis().getNameShort() + " (" + massMax + "t)");

        Engine engine = aLoadout.getEngine();
        if (engine != null) {
            double topSpeed = TopSpeed.calculate(engine.getRating(), aLoadout.getMovementProfile(), massMax,
                    aLoadout.getModifiers());

            speed.setText(df.format(topSpeed));
        }
        else {
            speed.setText("Speed: No Engine");
        }

        armor.setText("Armor: " + aLoadout.getArmor() + "/" + chassisBase.getArmorMax());

        Map<Weapon, Integer> multiplicity = new HashMap<>();
        equipment.getChildren().clear();
        for (Weapon weapon : aLoadout.items(Weapon.class)) {
            Integer i = multiplicity.get(weapon);
            if (i == null)
                i = 0;
            i = i + 1;
            multiplicity.put(weapon, i);
        }
        for (Entry<Weapon, Integer> entry : multiplicity.entrySet()) {
            addEquipment(entry.getKey(), entry.getValue().intValue());
        }

        for (ECM ecm : aLoadout.items(ECM.class)) {
            addEquipment(ecm, 1);
            break;
        }

        for (JumpJet jj : aLoadout.items(JumpJet.class)) {
            addEquipment(jj, aLoadout.getJumpJetCount());
            break;
        }

        addEquipment(aLoadout.getUpgrades().getHeatSink().getHeatSinkType(), aLoadout.getHeatsinksCount());

        if (null != engine) {
            addEquipment(engine, 1);
        }
    }

    void addEquipment(Item aItem, int aMultiplier) {
        Label label;
        if (aMultiplier > 1) {
            label = new Label(aMultiplier + "x" + aItem.getShortName());
        }
        else {
            label = new Label(aItem.getShortName());
        }
        StyleManager.changeStyle(label, aItem);
        label.getStyleClass().add(StyleManager.CLASS_HARDPOINT);
        equipment.getChildren().add(label);

    }
}