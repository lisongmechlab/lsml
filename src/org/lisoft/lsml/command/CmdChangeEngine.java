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
import org.lisoft.lsml.model.chassi.Location;
import org.lisoft.lsml.model.item.Engine;
import org.lisoft.lsml.model.item.HeatSink;
import org.lisoft.lsml.model.loadout.EquipResult;
import org.lisoft.lsml.model.loadout.LoadoutStandard;
import org.lisoft.lsml.model.loadout.component.ConfiguredComponentStandard;
import org.lisoft.lsml.util.CommandStack.CompositeCommand;

/**
 * This command will change the engine to another one if possible.
 * 
 * @author Li Song
 */
public class CmdChangeEngine extends CompositeCommand {

    private final LoadoutStandard loadout;
    private final Engine          newEngine;

    /**
     * @param aMessageTarget
     *            Where to send messages due to the change.
     * @param aLoadoutStandard
     *            The {@link LoadoutStandard} to modify (Omni 'Mechs have fixed engines so this command doesn't apply).
     * @param aEngine
     *            The new engine to change to, must not be null.
     */
    public CmdChangeEngine(MessageDelivery aMessageTarget, LoadoutStandard aLoadoutStandard, Engine aEngine) {
        super("change engine", aMessageTarget);
        if (null == aEngine) {
            throw new NullPointerException();
        }

        loadout = aLoadoutStandard;
        newEngine = aEngine;
    }

    @Override
    protected void buildCommand() throws EquipResult {
        Engine oldEngine = loadout.getEngine();
        HeatSink hs = loadout.getUpgrades().getHeatSink().getHeatSinkType();
        ConfiguredComponentStandard ct = loadout.getComponent(Location.CenterTorso);
        
        double freeMass = loadout.getFreeMass() - (newEngine.getMass() - oldEngine.getMass());

        int hsToAdd = Math.min(ct.getEngineHeatsinks(), newEngine.getNumHeatsinkSlots());
        hsToAdd = Math.min(hsToAdd, (int)freeMass);
        
        addOp(new CmdRemoveItem(messageBuffer, loadout, ct, oldEngine));
        addOp(new CmdAddItem(messageBuffer, loadout, ct, newEngine));
        while (hsToAdd > 0) {
            addOp(new CmdAddItem(messageBuffer, loadout, ct, hs));
            hsToAdd--;
        }
    }

}
