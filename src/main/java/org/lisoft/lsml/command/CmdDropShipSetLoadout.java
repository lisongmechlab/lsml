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

import org.lisoft.lsml.messages.DropShipMessage;
import org.lisoft.lsml.messages.MessageDelivery;
import org.lisoft.lsml.model.garage.DropShip;
import org.lisoft.lsml.model.garage.GarageException;
import org.lisoft.lsml.model.loadout.Loadout;
import org.lisoft.lsml.util.CommandStack.Command;

/**
 * Changes the loadout on a {@link DropShip}s bay.
 * 
 * @author Li Song
 */
public class CmdDropShipSetLoadout extends Command {
    private final DropShip        dropShip;
    private final Loadout         loadout;
    private final MessageDelivery delivery;
    private final int             bayIndex;
    private Loadout               previousloadout;

    public CmdDropShipSetLoadout(MessageDelivery aMsgDelivery, DropShip aDropShip, int aBayIndex, Loadout aLoadout) {
        dropShip = aDropShip;
        loadout = aLoadout;
        bayIndex = aBayIndex;
        delivery = aMsgDelivery;
    }

    @Override
    public String describe() {
        return "remove mech from drop ship";
    }

    @Override
    protected void apply() throws GarageException {
        previousloadout = dropShip.getMech(bayIndex);

        dropShip.setMech(bayIndex, loadout);
        if (delivery != null) {
            delivery.post(new DropShipMessage());
        }
    }

    @Override
    protected void undo() {
        try {
            dropShip.setMech(bayIndex, previousloadout);
            if (delivery != null) {
                delivery.post(new DropShipMessage());
            }
        }
        catch (GarageException e) {
            // This should never happen as the mech was previously able to be put in the drop ship.
            // So this must be a programmer error and we'll promote the exception to unchecked.
            throw new RuntimeException(e);
        }
    }
}
