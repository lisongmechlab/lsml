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
package org.lisoft.lsml.view_fx.loadout.component;

import org.lisoft.lsml.model.chassi.HardPointType;
import org.lisoft.lsml.model.chassi.Location;
import org.lisoft.lsml.model.loadout.component.ConfiguredComponent;
import org.lisoft.lsml.view_fx.style.HardPointFormatter;
import org.lisoft.lsml.view_fx.style.StyleManager;

import javafx.scene.control.Label;
import javafx.scene.layout.HBox;

/**
 * This control displays the hard points for a given component.
 *
 * @author Emily Björk
 */
public class HardPointPane extends HBox {

    @Deprecated // Should be injected
    private final static HardPointFormatter HARD_POINT_FORMATTER = new HardPointFormatter();
    private final Location location;
    private final ConfiguredComponent component;

    /**
     * Creates a new {@link HardPointPane} control that shows the hard points for the given component.
     *
     * @param aComponent
     *            The component to create the pane for.
     */
    public HardPointPane(ConfiguredComponent aComponent) {
        location = aComponent.getInternalComponent().getLocation();
        component = aComponent;
        updateHardPoints();
        getStyleClass().add(StyleManager.CLASS_DEFAULT_SPACING);
    }

    /**
     * Updates the displayed hard points to reflect changes in the component (omnipod swap).
     */
    public void updateHardPoints() {
        getChildren().clear();
        for (final HardPointType hardPointType : HardPointType.values()) {
            final int num = component.getHardPointCount(hardPointType);
            if (num > 0) {
                getChildren().add(HARD_POINT_FORMATTER.format(num, hardPointType));
            }
        }

        if (getChildren().isEmpty() && location != Location.LeftLeg && location != Location.RightLeg
                && location != Location.Head && location != Location.CenterTorso) {
            // This spaces out components that don't have any hard points to be as tall
            // as their opposite component that may or may not have a hard point.
            final Label noHardPoint = new Label();
            noHardPoint.getStyleClass().add(StyleManager.CLASS_HARDPOINT);
            noHardPoint.setVisible(false);
            getChildren().add(noHardPoint);
        }

    }
}
