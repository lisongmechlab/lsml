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

import java.io.IOException;

import org.lisoft.lsml.view_fx.properties.LoadoutMetricsModelAdaptor;
import org.lisoft.lsml.view_fx.util.FxmlHelpers;

import javafx.beans.binding.DoubleBinding;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.TitledPane;

/**
 * A control that displays stats for a weapon group.
 * 
 * @author Li Song
 */
public class WeaponGroupPane extends TitledPane {
    @FXML
    private Label      alphaDamage;
    @FXML
    private Label      alphaGhostHeat;
    @FXML
    private Label      alphaHeat;
    @FXML
    private Label      alphaTimeToCool;
    @FXML
    private Label      alphaTimeToOverheat;
    @FXML
    private Label      burstDamage;
    @FXML
    private Label      maxDPS;
    @FXML
    private TitledPane root;
    @FXML
    private Label      sustainedDPS;

    /**
     * Sets up the data to show in this {@link WeaponGroupPane}.
     * 
     * @param aMetrics
     *            The data to show.
     * @param aGroupIdx
     *            A group index to show data for.
     * @throws IOException
     *             Thrown if the control couldn't be loaded.
     */
    public WeaponGroupPane(LoadoutMetricsModelAdaptor aMetrics, int aGroupIdx) throws IOException {
        FxmlHelpers.loadFxmlControl(this);
        root.setText("Group " + (aGroupIdx + 1));

        DoubleBinding alphaWithGhost = aMetrics.groupAlphaHeat[aGroupIdx].add(aMetrics.groupAlphaGhostHeat[aGroupIdx]);

        alphaDamage.textProperty()
                .bind(format("A. Dmg: %.1f@%.0fm", aMetrics.groupAlphaDamage[aGroupIdx], aMetrics.alphaRange));
        alphaHeat.textProperty()
                .bind(format("A. Heat: %.0f%%", alphaWithGhost.divide(aMetrics.heatCapacity).multiply(100)));
        alphaTimeToCool.textProperty().bind(format("A. Cool: %.1fs", alphaWithGhost.divide(aMetrics.heatDissipation)));
        alphaGhostHeat.textProperty().bind(format("A. Ghost Heat: %.1f", aMetrics.groupAlphaGhostHeat[aGroupIdx]));

        maxDPS.textProperty()
                .bind(format("Max DPS: %.1f@%.0fm", aMetrics.groupMaxDPS[aGroupIdx], aMetrics.maxDPSRange));
        sustainedDPS.textProperty().bind(
                format("Sust. DPS: %.1f@%.0fm", aMetrics.groupSustainedDPS[aGroupIdx], aMetrics.sustainedDPSRange));
        burstDamage.textProperty().bind(format("Burst %.0fs: %.1f@%.0fm", aMetrics.burstTime,
                aMetrics.groupBurstDamage[aGroupIdx], aMetrics.burstRange));
        alphaTimeToOverheat.textProperty()
                .bind(format("A. Overheat: %.1fs", aMetrics.groupAlphaTimeToOverHeat[aGroupIdx]));
    }

}
