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

import java.text.DecimalFormat;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.control.ContentDisplay;
import javafx.scene.control.Label;
import javafx.scene.control.ListCell;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import org.lisoft.lsml.messages.*;
import org.lisoft.lsml.model.loadout.Loadout;
import org.lisoft.lsml.view_fx.style.StyleManager;
import org.lisoft.mwo_data.equipment.Upgrade;
import org.lisoft.mwo_data.mechs.Upgrades;

/**
 * A cell factory for combo boxes that display upgrades
 *
 * @param <T> The type of the {@link Upgrade} that is shown in the cell. Must extend {@link
 *     Upgrade}.
 * @author Li Song
 */
public class UpgradeCell<T extends Upgrade> extends ListCell<T> implements MessageReceiver {
  private static final DecimalFormat FMT_SLOTS = new DecimalFormat("+#.# s;-#.# s");
  private static final DecimalFormat FMT_TONS = new DecimalFormat("+#.# t;-#.# t");
  private final Loadout loadout;
  private final Parent root;
  private final Label slots = new Label();
  private final Label title = new Label();
  private final Label tons = new Label();
  private boolean changed = false;

  public UpgradeCell(MessageXBar aXBar, Loadout aLoadout) {
    aXBar.attach(this);
    loadout = aLoadout;
    final HBox infoBox = new HBox(new Label("("), slots, new Label(", "), tons, new Label(")"));
    infoBox.getStyleClass().add("h4");
    root = new VBox(title, infoBox);
    setContentDisplay(ContentDisplay.GRAPHIC_ONLY);
  }

  @Override
  public void receive(Message aMsg) {
    final boolean items = aMsg instanceof ItemMessage;
    final boolean upgrades = aMsg instanceof UpgradesMessage;
    final boolean omniPods = aMsg instanceof OmniPodMessage;
    final boolean armour = aMsg instanceof ArmourMessage;

    if (items || upgrades || omniPods || armour) {
      changed = true;
    }
  }

  @Override
  protected boolean isItemChanged(T aOldUpgrade, T aNewUpgrade) {
    return changed || super.isItemChanged(aOldUpgrade, aNewUpgrade);
  }

  @Override
  protected void updateItem(T aUpgrade, boolean aEmpty) {
    super.updateItem(aUpgrade, aEmpty);

    if (aUpgrade == null || aEmpty) {
      setGraphic(null);
    } else {
      title.setText(aUpgrade.getShortName());

      final Upgrades upgrades = loadout.getUpgrades();
      final Upgrade currentUpgrade = upgrades.getUpgradeOfType(aUpgrade.getClass());
      final int deltaSlots =
          loadout.getUpgradeSlotsCost(aUpgrade) - loadout.getUpgradeSlotsCost(currentUpgrade);
      final double deltaTons =
          loadout.getUpgradeMassCost(aUpgrade) - loadout.getUpgradeMassCost(currentUpgrade);

      slots.setText(FMT_SLOTS.format(deltaSlots));
      tons.setText(FMT_TONS.format(deltaTons));
      changeUpgradeLabelStyle(tons, deltaTons);
      changeUpgradeLabelStyle(slots, deltaSlots);

      setGraphic(root);
    }
  }

  private void changeUpgradeLabelStyle(Node aNode, double aValue) {
    final String color;
    if (aValue < 0.0) {
      color = StyleManager.COLOUR_QUIRK_GOOD;
    } else if (aValue > 0.0) {
      color = StyleManager.COLOUR_QUIRK_BAD;
    } else {
      color = StyleManager.COLOUR_QUIRK_NEUTRAL;
    }

    aNode.setStyle("-fx-text-fill:" + color);
  }
}
