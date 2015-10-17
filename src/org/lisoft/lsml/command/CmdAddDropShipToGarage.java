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

import org.lisoft.lsml.model.garage.DropShip;
import org.lisoft.lsml.model.garage.GarageException;
import org.lisoft.lsml.model.garage.MechGarage;
import org.lisoft.lsml.model.loadout.LoadoutStandard;
import org.lisoft.lsml.util.CommandStack.Command;

/**
 * This operation adds a new {@link LoadoutStandard} to a {@link MechGarage}.
 * 
 * @author Li Song
 */
public class CmdAddDropShipToGarage extends Command {
    private final MechGarage garage;
    private final DropShip   dropShip;

    public CmdAddDropShipToGarage(MechGarage aGarage, DropShip aDropShip) {
        garage = aGarage;
        dropShip = aDropShip;
    }

    @Override
    public String describe() {
        return "add " + dropShip.getName() + " to drop ship";
    }

    @Override
    protected void apply() throws GarageException {
        if (garage.getDropShips().contains(dropShip)) {
            throw new GarageException("The dropship \"" + dropShip.getName() + "\" is already saved to the garage!");
        }
        garage.add(dropShip);
    }

    @Override
    protected void undo() {
        if (garage.getMechs().contains(dropShip)) {
            garage.remove(dropShip);
        }
    }
}
