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
package org.lisoft.lsml.view_fx.style;

import org.lisoft.lsml.model.chassi.HardPoint;
import org.lisoft.lsml.model.chassi.HardPointType;
import org.lisoft.lsml.model.item.JumpJet;
import org.lisoft.lsml.view_fx.loadout.equipment.EquipmentCategory;

import javafx.scene.Node;
import javafx.scene.control.Label;

/**
 * This class will format a {@link HardPoint} to a {@link Label}.
 * 
 * @author Emily Björk
 *
 */
public class HardPointFormatter {

    public Node format(int aNumHardPoints, HardPointType aHardPointType) {
        Label label = new Label();
        if (aNumHardPoints == 1)
            label.setText(aHardPointType.shortName());
        else
            label.setText(aNumHardPoints + aHardPointType.shortName());

        label.getStyleClass().add(StyleManager.CSS_CLASS_HARDPOINT);
        StyleManager.changeStyle(label, EquipmentCategory.classify(aHardPointType));

        return label;
    }

    public Node format(int aNumHardPoints, JumpJet aJumpJet) {
        Label label = new Label();
        if (aNumHardPoints == 1)
            label.setText("JJ");
        else
            label.setText(aNumHardPoints + "JJ");

        label.getStyleClass().add(StyleManager.CSS_CLASS_HARDPOINT);
        StyleManager.changeStyle(label, EquipmentCategory.classify(aJumpJet));

        return label;
    }
}
