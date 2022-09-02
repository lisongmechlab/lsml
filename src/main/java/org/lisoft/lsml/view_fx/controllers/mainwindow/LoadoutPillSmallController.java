/*
 * Li Song Mechlab - A 'mech building tool for PGI's MechWarrior: Online.
 * Copyright (C) 2013-2022  Li Song
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

import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.layout.GridPane;
import org.lisoft.lsml.command.CmdGarageAdd;
import org.lisoft.lsml.messages.ApplicationMessage;
import org.lisoft.lsml.messages.MessageXBar;
import org.lisoft.lsml.model.chassi.Chassis;
import org.lisoft.lsml.model.garage.GaragePath;
import org.lisoft.lsml.model.loadout.Loadout;
import org.lisoft.lsml.model.loadout.LoadoutFactory;
import org.lisoft.lsml.util.CommandStack;
import org.lisoft.lsml.view_fx.GlobalGarage;
import org.lisoft.lsml.view_fx.LiSongMechLab;
import org.lisoft.lsml.view_fx.controllers.AbstractFXController;
import org.lisoft.lsml.view_fx.controls.NameField;

/**
 * This class shows a summary of a loadout inside a "pill".
 *
 * @author Li Song
 */
public class LoadoutPillSmallController extends AbstractFXController {
  protected final LoadoutFactory loadoutFactory;
  protected final NameField<Loadout> nameField;
  protected final CommandStack stack;
  protected final MessageXBar xBar;
  @FXML protected Label chassisLabel;
  protected Loadout loadout;
  protected GaragePath<Loadout> loadoutPath;

  public LoadoutPillSmallController(
      CommandStack aCommandStack, MessageXBar aXBar, LoadoutFactory aLoadoutFactory) {
    stack = aCommandStack;
    xBar = aXBar;
    loadoutFactory = aLoadoutFactory;
    nameField = new NameField<>(stack, xBar);
    final GridPane grid = (GridPane) root;
    GridPane.setConstraints(nameField, 1, 0);
    grid.getChildren().add(nameField);
  }

  @FXML
  public void cloneLoadout() {
    final Loadout clone = loadoutFactory.produceClone(loadout);
    clone.setName(clone.getName() + " (Clone)");
    LiSongMechLab.safeCommand(
        root, stack, new CmdGarageAdd<>(xBar, loadoutPath.getParent(), clone), xBar);
  }

  @FXML
  public void remove() {
    GlobalGarage.remove(loadoutPath, root, stack, xBar);
  }

  @FXML
  public void rename() {
    nameField.startEdit();
  }

  public void setLoadout(Loadout aLoadout, GaragePath<Loadout> aLoadoutPath) {
    nameField.changeObject(aLoadout, aLoadoutPath);
    loadoutPath = aLoadoutPath;
    loadout = aLoadout;
    final Chassis chassisBase = aLoadout.getChassis();
    final int massMax = chassisBase.getMassMax();
    chassisLabel.setText(aLoadout.getChassis().getShortName() + " (" + massMax + "t)");
  }

  @FXML
  public void shareLsmlLink() {
    xBar.post(new ApplicationMessage(loadout, ApplicationMessage.Type.SHARE_LSML, root));
  }

  @FXML
  public void shareMWOLink() {
    xBar.post(new ApplicationMessage(loadout, ApplicationMessage.Type.SHARE_MWO, root));
  }
}
