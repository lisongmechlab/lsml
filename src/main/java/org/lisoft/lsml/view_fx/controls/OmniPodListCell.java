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

import java.util.ArrayList;
import java.util.List;
import javafx.scene.control.Label;
import javafx.scene.control.ListCell;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import org.lisoft.lsml.model.chassi.HardPointType;
import org.lisoft.lsml.model.chassi.OmniPod;
import org.lisoft.lsml.model.item.Faction;
import org.lisoft.lsml.model.item.JumpJet;
import org.lisoft.lsml.model.modifiers.Modifier;
import org.lisoft.lsml.view_fx.style.HardPointFormatter;
import org.lisoft.lsml.view_fx.style.ModifierFormatter;
import org.lisoft.lsml.view_fx.style.StyleManager;

/**
 * This class implements {@link ListCell} for {@link OmniPod}s to show hard points and quirks for
 * the pod.
 *
 * @author Li Song
 */
public class OmniPodListCell extends ListCell<OmniPod> {
  private static final JumpJet PROTO_JUMPJET =
      new JumpJet(
          "", "", "", 0, 0, 0, HardPointType.NONE, 0, Faction.ANY, null, null, 0, 0, 0, 0, 0);
  private final HardPointFormatter hardPointFormatter = new HardPointFormatter();
  private final ModifierFormatter modifierFormatter = new ModifierFormatter();

  @Override
  protected void updateItem(OmniPod aItem, boolean aEmpty) {
    super.updateItem(aItem, aEmpty);
    if (null == aItem) {
      setGraphic(null);
      return;
    }

    final VBox root = new VBox();
    final HBox hardPointsList = new HBox();

    for (final HardPointType hardPointType : HardPointType.values()) {
      final int num = aItem.getHardPointCount(hardPointType);
      if (num > 0) {
        hardPointsList.getChildren().add(hardPointFormatter.format(num, hardPointType));
      }
    }

    if (aItem.getJumpJetsMax() > 0) {
      hardPointsList
          .getChildren()
          .add(hardPointFormatter.format(aItem.getJumpJetsMax(), PROTO_JUMPJET));
    }

    root.getChildren().add(new Label(aItem.getChassisName()));
    root.getChildren().add(hardPointsList);
    StyleManager.addClass(root, StyleManager.CLASS_DEFAULT_PADDING);
    StyleManager.addClass(root, StyleManager.CLASS_DEFAULT_SPACING);
    StyleManager.addClass(hardPointsList, StyleManager.CLASS_DEFAULT_SPACING);

    if (aItem.getQuirks().size() > 6) {
      final HBox quirksColumns = new HBox();
      final VBox quirksLeftColumn = new VBox();
      final VBox quirksRightColumn = new VBox();
      final List<Modifier> all = new ArrayList<>(aItem.getQuirks());
      final int splitPoint = (aItem.getQuirks().size() + 1) / 2;
      root.getChildren().add(quirksColumns);
      quirksColumns.getChildren().addAll(quirksLeftColumn, quirksRightColumn);

      List<Modifier> leftModifiers = all.subList(0, splitPoint);
      List<Modifier> rightModifiers = all.subList(splitPoint, all.size());
      modifierFormatter.format(leftModifiers, quirksLeftColumn.getChildren());
      modifierFormatter.format(rightModifiers, quirksRightColumn.getChildren());

      StyleManager.addClass(quirksColumns, StyleManager.CLASS_DEFAULT_SPACING);
      StyleManager.addClass(quirksLeftColumn, StyleManager.CLASS_DEFAULT_SPACING);
      StyleManager.addClass(quirksRightColumn, StyleManager.CLASS_DEFAULT_SPACING);
    } else {
      modifierFormatter.format(aItem.getQuirks(), root.getChildren());
    }
    setGraphic(root);
  }
}
