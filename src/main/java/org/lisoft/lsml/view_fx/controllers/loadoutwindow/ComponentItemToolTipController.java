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
package org.lisoft.lsml.view_fx.controllers.loadoutwindow;

import javafx.fxml.FXML;
import javafx.scene.control.Label;
import org.lisoft.lsml.model.item.Item;
import org.lisoft.lsml.model.loadout.ConfiguredComponent;
import org.lisoft.lsml.model.metrics.CriticalStrikeProbability;
import org.lisoft.lsml.model.metrics.ItemEffectiveHP;
import org.lisoft.lsml.model.metrics.helpers.ComponentDestructionSimulator;
import org.lisoft.lsml.model.modifiers.Modifier;
import org.lisoft.lsml.view_fx.controllers.AbstractFXController;

import javax.inject.Inject;
import java.text.DecimalFormat;
import java.util.Collection;

/**
 * A pane that displays information about an item equipped on a component.
 *
 * @author Li Song
 */
public class ComponentItemToolTipController extends AbstractFXController {

    private final static DecimalFormat DF_HP = new DecimalFormat("#.# hp");
    private final static DecimalFormat DF_PCT = new DecimalFormat("#.## %");
    @FXML
    private Label buffer;
    @FXML
    private Label criticalHit;
    @FXML
    private Label description;
    @FXML
    private Label hp;
    @FXML
    private Label survival;

    @Inject
    public ComponentItemToolTipController() {
    }

    public void update(ConfiguredComponent aComponent, Item aItem, Collection<Modifier> aModifiers) {

        description.setText(aItem.getName());
        final ItemEffectiveHP ehp = new ItemEffectiveHP(aComponent);
        final CriticalStrikeProbability csp = new CriticalStrikeProbability(aComponent);
        final ComponentDestructionSimulator cds = new ComponentDestructionSimulator(aComponent);
        hp.setText(DF_HP.format(aItem.getHealth()));

        cds.simulate(aModifiers);

        criticalHit.setText(DF_PCT.format(csp.calculate(aItem)));
        final double sieHP = ehp.calculate(aItem);
        buffer.setText(DF_HP.format(sieHP));
        final double P_destroyed = cds.getProbabilityOfDestruction(aItem);
        survival.setText(DF_PCT.format(1.0 - P_destroyed));
    }
}
