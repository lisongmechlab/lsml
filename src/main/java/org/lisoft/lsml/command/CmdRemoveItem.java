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
package org.lisoft.lsml.command;

import org.lisoft.lsml.messages.MessageDelivery;
import org.lisoft.lsml.model.loadout.ConfiguredComponent;
import org.lisoft.lsml.model.loadout.EquipException;
import org.lisoft.lsml.model.loadout.EquipResult;
import org.lisoft.lsml.model.loadout.EquipResult.EquipResultType;
import org.lisoft.lsml.model.loadout.Loadout;
import org.lisoft.lsml.mwo_data.equipment.*;
import org.lisoft.lsml.util.CommandStack.Command;

/**
 * This {@link Command} removes an {@link Item} from a {@link ConfiguredComponent}.
 *
 * @author Li Song
 */
public class CmdRemoveItem extends CmdItemBase {
  private int numEngineHS = 0;

  /**
   * Creates a new operation.
   *
   * @param aMessageDelivery The {@link MessageDelivery} to send messages on when items are removed.
   * @param aLoadout The {@link Loadout} to remove the item from.
   * @param aComponent The {@link ConfiguredComponent} to remove from.
   * @param aItem The {@link Item} to remove.
   */
  public CmdRemoveItem(
      MessageDelivery aMessageDelivery,
      Loadout aLoadout,
      ConfiguredComponent aComponent,
      Item aItem) {
    super(aMessageDelivery, aLoadout, aComponent, aItem);
    if (aItem instanceof Internal) {
      throw new IllegalArgumentException("Internals cannot be removed!");
    }
  }

  @Override
  public void apply() throws EquipException {
    if (item instanceof ECM && loadout.getUpgrades().getArmour() == UpgradeDB.IS_STEALTH_ARMOUR) {
      EquipException.checkAndThrow(EquipResult.make(EquipResultType.CannotRemoveECM));
    }

    if (!component.canRemoveItem(item)) {
      throw new IllegalArgumentException("Can not remove item: " + item + " from " + component);
    }

    if (item instanceof final Engine engine) {
      removeXLSides(engine);

      int engineHsLeft = component.getEngineHeatSinks();
      final HeatSink heatSinkType = loadout.getUpgrades().getHeatSink().getHeatSinkType();
      while (engineHsLeft > 0) {
        engineHsLeft--;
        numEngineHS++;
        remove(component, heatSinkType);
      }
    }
    remove(component, item);
  }

  @Override
  public String describe() {
    return "remove " + item.getName() + " from " + component.getInternalComponent().getLocation();
  }

  /*
   * (non-Javadoc)
   *
   * @see java.lang.Object#equals(java.lang.Object)
   */
  @Override
  public boolean equals(Object obj) {
    if (this == obj) {
      return true;
    }
    if (!super.equals(obj)) {
      return false;
    }
    if (!(obj instanceof final CmdRemoveItem other)) {
      return false;
    }
    return numEngineHS == other.numEngineHS;
  }

  /*
   * (non-Javadoc)
   *
   * @see java.lang.Object#hashCode()
   */
  @Override
  public int hashCode() {
    final int prime = 31;
    int result = super.hashCode();
    result = prime * result + numEngineHS;
    return result;
  }

  @Override
  public void undo() {
    add(component, item);

    if (item instanceof final Engine engine) {
      addXLSides(engine);

      final HeatSink heatSinkType = loadout.getUpgrades().getHeatSink().getHeatSinkType();
      while (numEngineHS > 0) {
        numEngineHS--;
        add(component, heatSinkType);
      }
    }
  }
}
