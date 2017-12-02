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
package org.lisoft.lsml.view_fx.controllers.loadoutwindow;

import static javafx.beans.binding.Bindings.when;
import static org.lisoft.lsml.view_fx.util.FxBindingUtils.format;

import javax.inject.Inject;

import org.lisoft.lsml.model.loadout.Loadout;
import org.lisoft.lsml.view_fx.controllers.AbstractFXController;
import org.lisoft.lsml.view_fx.properties.LoadoutMetrics;
import org.lisoft.lsml.view_fx.properties.LoadoutMetrics.GroupMetrics;

import javafx.beans.binding.DoubleBinding;
import javafx.beans.binding.ObjectBinding;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.paint.Color;
import javafx.scene.paint.Paint;

/**
 * A control that displays stats for a weapon group.
 *
 * @author Emily Björk
 */
public class WeaponGroupStatsController extends AbstractFXController {
    @FXML
    private Label alphaDamage;
    @FXML
    private Label alphaGhostHeat;
    @FXML
    private Label alphaHeat;
    @FXML
    private Label alphaTimeToCool;
    @FXML
    private Label alphaTimeToOverheat;
    @FXML
    private Label burstDamage;
    @FXML
    private Label maxDPS;
    @FXML
    private Label sustainedDPS;

    /**
     * Sets up the data to show in this {@link WeaponGroupStatsController}.
     *
     * @param aGroupMetrics
     *            The group metrics to get data for the current weapon group from.
     * @param aGlobalMetrics
     *            The global metrics to get data for the {@link Loadout} from.
     */
    @Inject
    public WeaponGroupStatsController(GroupMetrics aGroupMetrics, LoadoutMetrics aGlobalMetrics) {
        final DoubleBinding alphaWithGhost = aGroupMetrics.alphaHeat.add(aGroupMetrics.alphaGhostHeat);
        alphaDamage.textProperty().bind(
                format("Alpha: %.1h @ %.0h m", aGroupMetrics.alphaDamage, aGroupMetrics.alphaDamage.displayRange()));
        alphaHeat.textProperty().bind(format("Alpha Heat: %.0ph", aGroupMetrics.alphaHeatPct));
        alphaTimeToCool.textProperty()
                .bind(format("TtC Alpha: %.1h s", alphaWithGhost.divide(aGlobalMetrics.heatDissipation)));
        alphaGhostHeat.textProperty().bind(format("GH Alpha: %.1h", aGroupMetrics.alphaGhostHeat));
        final Paint defaultFill = alphaGhostHeat.getTextFill();

        final ObjectBinding<Paint> colorBinding = when(aGroupMetrics.alphaGhostHeat.lessThanOrEqualTo(0.0))
                .then(defaultFill).otherwise(Color.RED);
        alphaGhostHeat.textFillProperty().bind(colorBinding);

        maxDPS.textProperty()
                .bind(format("Max. DPS: %.1h @ %.0h m", aGroupMetrics.maxDPS, aGroupMetrics.maxDPS.displayRange()));
        sustainedDPS.textProperty().bind(format("Sust. DPS: %.1h @ %.0h m", aGroupMetrics.sustainedDPS,
                aGroupMetrics.sustainedDPS.displayRange()));
        burstDamage.textProperty().bind(format("Burst %.1h s: %.1h @ %.0h m", aGlobalMetrics.burstTime,
                aGroupMetrics.burstDamage, aGroupMetrics.burstDamage.displayRange()));
        alphaTimeToOverheat.textProperty().bind(format("TtO Alpha: %.1h s", aGroupMetrics.maxDPSTtO));
    }

}
