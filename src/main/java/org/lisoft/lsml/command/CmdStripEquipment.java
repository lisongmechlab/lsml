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
import org.lisoft.lsml.model.loadout.Loadout;
import org.lisoft.lsml.model.loadout.LoadoutStandard;
import org.lisoft.lsml.util.CommandStack.CompositeCommand;
import org.lisoft.mwo_data.equipment.HeatSink;
import org.lisoft.mwo_data.equipment.Internal;
import org.lisoft.mwo_data.equipment.Item;

/**
 * This operation removes all {@link Item}s from a {@link LoadoutStandard}.
 *
 * @author Li Song
 */
public class CmdStripEquipment extends CompositeCommand {
  private final Loadout loadout;

  /**
   * Creates a new strip operation that removes equipment and modules.
   *
   * @param aLoadout The loadout to strip.
   * @param aMessageDelivery Where to deliver message changes.
   */
  public CmdStripEquipment(Loadout aLoadout, MessageDelivery aMessageDelivery) {
    super("strip mech", aMessageDelivery);
    loadout = aLoadout;
  }

  @Override
  public void buildCommand() {
    for (final ConfiguredComponent component : loadout.getComponents()) {
      int hsSkipp = component.getEngineHeatSinks();
      for (final Item item : component.getItemsEquipped()) {
        if (!(item instanceof Internal)) {
          if (item instanceof HeatSink) {
            if (hsSkipp > 0) {
              hsSkipp--;
              continue;
            }
          }
          addOp(new CmdRemoveItem(messageBuffer, loadout, component, item));
        }
      }
    }
  }
}
