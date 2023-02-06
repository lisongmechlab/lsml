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
import org.lisoft.lsml.model.loadout.EquipException;
import org.lisoft.lsml.model.loadout.Loadout;
import org.lisoft.lsml.model.loadout.LoadoutStandard;
import org.lisoft.lsml.mwo_data.Faction;
import org.lisoft.lsml.mwo_data.equipment.UpgradeDB;
import org.lisoft.lsml.util.CommandStack.CompositeCommand;

/**
 * This operation removes everything from the loadout and puts it to a "blank" state.
 *
 * @author Li Song
 */
public class CmdStripLoadout extends CompositeCommand {

  private final Loadout loadout;

  public CmdStripLoadout(MessageDelivery aMessageTarget, Loadout aLoadout) {
    super("strip everything", aMessageTarget);
    loadout = aLoadout;
  }

  @Override
  protected void buildCommand() throws EquipException {
    addOp(new CmdStripArmour(loadout, messageBuffer));
    addOp(new CmdStripEquipment(loadout, messageBuffer));

    addOp(new CmdSetGuidanceType(messageBuffer, loadout, UpgradeDB.STD_GUIDANCE));
    if (loadout instanceof LoadoutStandard) {
      final LoadoutStandard loadoutStandard = (LoadoutStandard) loadout;
      final Faction faction = loadoutStandard.getChassis().getFaction();
      addOp(
          new CmdSetStructureType(
              messageBuffer, loadoutStandard, UpgradeDB.getDefaultStructure(faction)));
      addOp(
          new CmdSetArmourType(
              messageBuffer, loadoutStandard, UpgradeDB.getDefaultArmour(faction)));
      addOp(
          new CmdSetHeatSinkType(
              messageBuffer, loadoutStandard, UpgradeDB.getDefaultHeatSinks(faction)));
    }
  }
}
