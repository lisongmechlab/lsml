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
package org.lisoft.lsml.command;

import org.lisoft.lsml.model.loadout.LoadoutBase;
import org.lisoft.lsml.model.loadout.LoadoutStandard;
import org.lisoft.lsml.model.loadout.component.ConfiguredComponentBase;
import org.lisoft.lsml.model.upgrades.UpgradeDB;
import org.lisoft.lsml.util.CommandStack.CompositeCommand;
import org.lisoft.lsml.util.message.MessageDelivery;

/**
 * This operation removes all armor, upgrades and items from a {@link LoadoutStandard}.
 * 
 * @author Emily Björk
 */
public class CmdStripLoadout extends CompositeCommand {
    protected final LoadoutBase<?> loadout;

    public CmdStripLoadout(LoadoutBase<?> aLoadout, MessageDelivery aMessageDelivery) {
        super("strip mech", aMessageDelivery);
        loadout = aLoadout;
    }

    @Override
    public void buildCommand() {
        for (ConfiguredComponentBase component : loadout.getComponents()) {
            addOp(new CmdStripComponent(messageBuffer, loadout, component));
        }

        if (loadout instanceof LoadoutStandard) {
            LoadoutStandard loadoutStandard = (LoadoutStandard) loadout;
            addOp(new CmdSetStructureType(messageBuffer, loadoutStandard, UpgradeDB.STANDARD_STRUCTURE));
            addOp(new CmdSetGuidanceType(messageBuffer, loadoutStandard, UpgradeDB.STANDARD_GUIDANCE));
            addOp(new CmdSetArmorType(messageBuffer, loadoutStandard, UpgradeDB.STANDARD_ARMOR));
            addOp(new CmdSetHeatSinkType(messageBuffer, loadoutStandard, UpgradeDB.STANDARD_HEATSINKS));
        }
    }
}
