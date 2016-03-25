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

import org.lisoft.lsml.model.loadout.Loadout;
import org.lisoft.lsml.view_fx.util.FxmlHelpers;

import javafx.fxml.FXML;
import javafx.scene.control.CheckBox;
import javafx.scene.control.Spinner;
import javafx.scene.control.TableView;
import javafx.scene.layout.BorderPane;

/**
 * This pane will show a dialog where the user can use filters to find a mech that matches certain criteria.
 * 
 * @author Li Song
 */
public class NewMechPane extends BorderPane {
    @FXML
    private Spinner<Integer>   filterMaxMass;
    @FXML
    private Spinner<Integer>   filterMinMass;
    @FXML
    private Spinner<Integer>   filterMinSpeed;
    @FXML
    private Spinner<Integer>   filterMinBallistic;
    @FXML
    private Spinner<Integer>   filterMinEnergy;
    @FXML
    private Spinner<Integer>   filterMinMissile;
    @FXML
    private CheckBox           filterAllowHero;
    @FXML
    private CheckBox           filterClan;
    @FXML
    private CheckBox           filterInnerSphere;
    @FXML
    private CheckBox           filterJumpJets;
    @FXML
    private TableView<Loadout> resultsTable;
    @FXML
    private CheckBox           filterECM;

    private final Runnable     onClose;

    /**
     * @param aOnClose
     *            A callback to call when the close button is pressed.
     */
    public NewMechPane(Runnable aOnClose) {
        FxmlHelpers.loadFxmlControl(this);
        onClose = aOnClose;
    }

    @FXML
    public void closeNewMech() {
        onClose.run();
    }
}
