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
package org.lisoft.lsml.view_fx.controls;

import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import org.lisoft.lsml.model.loadout.ConfiguredComponent;
import org.lisoft.lsml.view_fx.style.HardPointFormatter;
import org.lisoft.lsml.view_fx.style.StyleManager;
import org.lisoft.mwo_data.mechs.HardPointType;
import org.lisoft.mwo_data.mechs.Location;

/**
 * This control displays the hard points for a given component.
 *
 * @author Li Song
 */
public class HardPointPane extends HBox {
  private final HardPointFormatter hardPointFormatter;

  /**
   * Creates a new {@link HardPointFormatter} that is not initialised. You need to call {@link
   * #updateHardPoints(ConfiguredComponent)} to show the hard points.
   *
   * @param aHardPointFormatter The {@link HardPointFormatter} to use for showing the hard points.
   */
  public HardPointPane(HardPointFormatter aHardPointFormatter) {
    hardPointFormatter = aHardPointFormatter;
    getStyleClass().add(StyleManager.CLASS_SMALL_SPACING);
  }

  /**
   * Creates a new {@link HardPointPane} control that shows the hard points for the given component.
   *
   * @param aHardPointFormatter A {@link HardPointFormatter} object to use for printing the text
   *     representation of hard points.
   * @param aComponent The component to create the pane for.
   */
  public HardPointPane(HardPointFormatter aHardPointFormatter, ConfiguredComponent aComponent) {
    this(aHardPointFormatter);
    updateHardPoints(aComponent);
  }

  /**
   * Updates the displayed hard points to reflect changes in the component (OmniPod swap).
   *
   * @param aComponent The component to show.
   */
  public void updateHardPoints(ConfiguredComponent aComponent) {
    getChildren().clear();
    final Location location = aComponent.getInternalComponent().getLocation();
    for (final HardPointType hardPointType : HardPointType.values()) {
      final int num = aComponent.getHardPointCount(hardPointType);
      if (num > 0) {
        getChildren().add(hardPointFormatter.format(num, hardPointType));
      }
    }

    if (getChildren().isEmpty()
        && location != Location.LeftLeg
        && location != Location.RightLeg
        && location != Location.Head
        && location != Location.CenterTorso) {
      // This spaces out components that don't have any hard points to be as tall
      // as their opposite component that may or may not have a hard point.
      final Label noHardPoint = new Label();
      noHardPoint.getStyleClass().add(StyleManager.CLASS_HARDPOINT);
      noHardPoint.setVisible(false);
      getChildren().add(noHardPoint);
    }
  }
}
