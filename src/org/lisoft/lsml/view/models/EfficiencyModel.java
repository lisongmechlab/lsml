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
package org.lisoft.lsml.view.models;

import javax.swing.JToggleButton;

import org.lisoft.lsml.messages.MessageXBar;
import org.lisoft.lsml.model.modifiers.Efficiencies;
import org.lisoft.lsml.model.modifiers.MechEfficiencyType;

/**
 * This model models a efficiency for a mech.
 * 
 * @author Emily Björk
 */
public class EfficiencyModel extends JToggleButton.ToggleButtonModel {
    private final MechEfficiencyType type;
    private final Efficiencies       efficiencies;
    private final MessageXBar        xBar;

    /**
     * Creates a new {@link EfficiencyModel}.
     * 
     * @param aXBar
     *            The {@link MessageXBar} to listen for changes to the backing model on and to send messages to when the
     *            model changes.
     * @param aType
     *            The type of efficiency this model is for.
     * @param aEfficiencies
     */
    public EfficiencyModel(MessageXBar aXBar, MechEfficiencyType aType, Efficiencies aEfficiencies) {
        type = aType;
        efficiencies = aEfficiencies;
        xBar = aXBar;
    }

    @Override
    public boolean isSelected() {
        return efficiencies.hasEfficiency(type);
    }

    @Override
    public void setSelected(boolean aEnabled) {
        efficiencies.setEfficiency(type, aEnabled, xBar);
    }
}
