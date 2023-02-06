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

import org.lisoft.lsml.messages.DropShipMessage;
import org.lisoft.lsml.messages.MessageDelivery;
import org.lisoft.lsml.model.garage.DropShip;
import org.lisoft.lsml.util.CommandStack.Command;

/**
 * This command will change the name of a drop ship.
 *
 * @author Li Song
 */
public class CmdSetDropShipName implements Command {
  private final DropShip dropShip;
  private final MessageDelivery messageDelivery;
  private final String newName;
  private String oldName;

  /**
   * Creates a new command.
   *
   * @param aDropShip The drop ship to rename.
   * @param aMessageDelivery The {@link MessageDelivery} to post to changes on.
   * @param aName The new name.
   */
  public CmdSetDropShipName(DropShip aDropShip, MessageDelivery aMessageDelivery, String aName) {
    dropShip = aDropShip;
    newName = aName;
    messageDelivery = aMessageDelivery;
  }

  @Override
  public void apply() {
    oldName = dropShip.getName();
    execute(newName);
  }

  @Override
  public String describe() {
    return "rename drop ship";
  }

  @Override
  public void undo() {
    execute(oldName);
  }

  private void execute(String aName) {
    dropShip.setName(aName);
    if (null != messageDelivery) {
      messageDelivery.post(new DropShipMessage());
    }
  }
}
