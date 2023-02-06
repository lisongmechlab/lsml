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

import org.lisoft.lsml.messages.LoadoutMessage;
import org.lisoft.lsml.messages.MessageDelivery;
import org.lisoft.lsml.messages.MessageXBar;
import org.lisoft.lsml.model.loadout.EquipException;
import org.lisoft.lsml.model.loadout.EquipResult;
import org.lisoft.lsml.model.loadout.Loadout;
import org.lisoft.lsml.mwo_data.equipment.Consumable;
import org.lisoft.lsml.util.CommandStack.Command;

/**
 * This {@link Command} adds a module to a loadout.
 *
 * @author Li Song
 */
public class CmdAddModule extends MessageCommand {
  private final Consumable consumable;
  private final Loadout loadout;

  /**
   * Creates a new {@link CmdAddModule}.
   *
   * @param aMessageDelivery The {@link MessageXBar} to signal changes to the loadout on.
   * @param aLoadout The {@link Loadout} to add the module to.
   * @param aConsumable The {@link Consumable} to add.
   */
  public CmdAddModule(MessageDelivery aMessageDelivery, Loadout aLoadout, Consumable aConsumable) {
    super(aMessageDelivery);
    consumable = aConsumable;
    loadout = aLoadout;
  }

  @Override
  public void apply() throws EquipException {
    final EquipResult result = loadout.canAddModule(consumable);
    EquipException.checkAndThrow(result);
    loadout.addModule(consumable);

    post();
  }

  @Override
  public String describe() {
    return "add " + consumable + " to " + loadout;
  }

  @Override
  public void undo() {
    loadout.removeModule(consumable);
    post();
  }

  void post() {
    post(new LoadoutMessage(loadout, LoadoutMessage.Type.MODULES_CHANGED));
  }
}
