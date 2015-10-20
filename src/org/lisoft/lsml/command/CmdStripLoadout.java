/*
 * @formatter:off
 * Li Song Mechlab - A 'mech building tool for PGI's MechWarrior: Online.
 * Copyright (C) 2013  Li Song
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

import org.lisoft.lsml.messages.MessageDelivery;
import org.lisoft.lsml.model.datacache.UpgradeDB;
import org.lisoft.lsml.model.loadout.LoadoutBase;
import org.lisoft.lsml.model.loadout.LoadoutStandard;
import org.lisoft.lsml.model.loadout.component.ConfiguredComponentBase;
import org.lisoft.lsml.util.CommandStack.CompositeCommand;

/**
 * This operation removes all armor, upgrades and items from a {@link LoadoutStandard}.
 * 
 * @author Li Song
 */
public class CmdStripLoadout extends CompositeCommand {
    private final LoadoutBase<?> loadout;
    private final boolean        removeArmor;

    /**
     * Creates a new strip operation that removes armor, equipment and modules.
     * 
     * @param aLoadout
     *            The loadout to strip.
     * @param aMessageDelivery
     *            Where to deliver message changes.
     */
    public CmdStripLoadout(LoadoutBase<?> aLoadout, MessageDelivery aMessageDelivery) {
        this(aLoadout, aMessageDelivery, true);
    }

    /**
     * Creates a new strip operation that optionally removes armor, and always removes equipment and modules.
     * 
     * @param aLoadout
     *            The loadout to strip.
     * @param aMessageDelivery
     *            Where to deliver message changes.
     * @param aRemoveArmor
     *            If <code>true</code> armor will be removed too.
     */
    public CmdStripLoadout(LoadoutBase<?> aLoadout, MessageDelivery aMessageDelivery, boolean aRemoveArmor) {
        super("strip mech", aMessageDelivery);
        loadout = aLoadout;
        removeArmor = aRemoveArmor;
    }

    @Override
    public void buildCommand() {
        for (ConfiguredComponentBase component : loadout.getComponents()) {
            addOp(new CmdStripComponent(messageBuffer, loadout, component, removeArmor));
        }

        addOp(new CmdSetGuidanceType(messageBuffer, loadout, UpgradeDB.STANDARD_GUIDANCE));
        
        if (loadout instanceof LoadoutStandard) {
            LoadoutStandard loadoutStandard = (LoadoutStandard) loadout;
            addOp(new CmdSetStructureType(messageBuffer, loadoutStandard, UpgradeDB.STANDARD_STRUCTURE));
            addOp(new CmdSetArmorType(messageBuffer, loadoutStandard, UpgradeDB.STANDARD_ARMOR));
            addOp(new CmdSetHeatSinkType(messageBuffer, loadoutStandard, UpgradeDB.STANDARD_HEATSINKS));
        }
    }
}
