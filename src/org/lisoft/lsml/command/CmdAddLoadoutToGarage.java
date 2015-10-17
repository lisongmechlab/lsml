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

import org.lisoft.lsml.model.garage.GarageException;
import org.lisoft.lsml.model.garage.MechGarage;
import org.lisoft.lsml.model.loadout.LoadoutBase;
import org.lisoft.lsml.model.loadout.LoadoutStandard;
import org.lisoft.lsml.util.CommandStack.Command;

/**
 * This operation adds a new {@link LoadoutStandard} to a {@link MechGarage}.
 * 
 * @author Li Song
 */
public class CmdAddLoadoutToGarage extends Command {
    private final MechGarage     garage;
    private final LoadoutBase<?> loadout;

    public CmdAddLoadoutToGarage(MechGarage aGarage, LoadoutBase<?> aLoadout) {
        garage = aGarage;
        loadout = aLoadout;
    }

    @Override
    public String describe() {
        return "add " + loadout.getName() + " to garage";
    }

    @Override
    protected void apply() throws GarageException {
        if (garage.getMechs().contains(loadout)) {
            throw new GarageException("The loadout \"" + loadout.getName() + "\" is already saved to the garage!");
        }
        garage.add(loadout);
    }

    @Override
    protected void undo() {
        if (garage.getMechs().contains(loadout)) {
            garage.remove(loadout); // Undo must not throw.
        }
    }
}
