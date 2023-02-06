/*
 * Li Song Mechlab - A 'mech building tool for PGI's MechWarrior: Online.
 * Copyright (C) 2013-2023  Li Song
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
package org.lisoft.lsml.view_fx.controllers.mainwindow;

import java.text.DecimalFormat;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import org.lisoft.lsml.messages.MessageXBar;
import org.lisoft.lsml.model.garage.GaragePath;
import org.lisoft.lsml.model.loadout.Loadout;
import org.lisoft.lsml.model.loadout.LoadoutFactory;
import org.lisoft.lsml.model.metrics.TopSpeed;
import org.lisoft.lsml.mwo_data.equipment.*;
import org.lisoft.lsml.mwo_data.mechs.Chassis;
import org.lisoft.lsml.util.CommandStack;
import org.lisoft.lsml.view_fx.style.StyleManager;

/**
 * This class shows a summary of a loadout inside of a "pill".
 *
 * @author Li Song
 */
public class LoadoutPillController extends LoadoutPillSmallController {
  private static final DecimalFormat df = new DecimalFormat("Speed: #.# kph");
  @FXML private Label armourLabel;
  @FXML private Label engineLabel;
  @FXML private HBox equipment;
  @FXML private Label speedLabel;

  public LoadoutPillController(
      CommandStack aCommandStack, MessageXBar aXBar, LoadoutFactory aLoadoutFactory) {
    super(aCommandStack, aXBar, aLoadoutFactory);
    nameField.getStyleClass().add(StyleManager.CLASS_H2);
  }

  @Override
  public void setLoadout(Loadout aLoadout, GaragePath<Loadout> aGaragePath) {
    super.setLoadout(aLoadout, aGaragePath);
    final Chassis chassisBase = aLoadout.getChassis();
    final int massMax = chassisBase.getMassMax();

    final Engine engine = aLoadout.getEngine();
    if (engine != null) {
      final double topSpeed =
          TopSpeed.calculate(
              engine.getRating(),
              aLoadout.getMovementProfile(),
              massMax,
              aLoadout.getAllModifiers());

      speedLabel.setText(df.format(topSpeed));
      engineLabel.setText(engine.getShortName());
    } else {
      speedLabel.setText("Speed: -");
      engineLabel.setText("No Engine");
    }

    armourLabel.setText("Armour: " + aLoadout.getArmour() + "/" + chassisBase.getArmourMax());

    final Map<Weapon, Integer> multiplicity = new HashMap<>();
    equipment.getChildren().clear();
    for (final Weapon weapon : aLoadout.items(Weapon.class)) {
      Integer i = multiplicity.get(weapon);
      if (i == null) {
        i = 0;
      }
      i = i + 1;
      multiplicity.put(weapon, i);
    }
    for (final Entry<Weapon, Integer> entry : multiplicity.entrySet()) {
      addEquipment(entry.getKey(), entry.getValue());
    }

    for (final ECM ecm : aLoadout.items(ECM.class)) {
      addEquipment(ecm, 1);
      break;
    }

    for (final JumpJet jj : aLoadout.items(JumpJet.class)) {
      addEquipment(jj, aLoadout.getJumpJetCount());
      break;
    }

    if (aLoadout.getTotalHeatSinksCount() > 0) {
      addEquipment(
          aLoadout.getUpgrades().getHeatSink().getHeatSinkType(),
          aLoadout.getTotalHeatSinksCount());
    }
  }

  private void addEquipment(Item aItem, int aMultiplier) {
    Label label;
    if (aMultiplier > 1) {
      label = new Label(aMultiplier + "x" + aItem.getShortName());
    } else {
      label = new Label(aItem.getShortName());
    }
    StyleManager.changeStyle(label, aItem);
    label.getStyleClass().add(StyleManager.CLASS_HARDPOINT);
    equipment.getChildren().add(label);
  }
}
