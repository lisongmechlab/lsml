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
package org.lisoft.lsml.view.mechlab.garagetree;

import javax.swing.JTextField;
import javax.swing.SwingUtilities;
import javax.swing.event.TreeModelEvent;

import org.lisoft.lsml.messages.DropShipMessage;
import org.lisoft.lsml.messages.GarageMessage;
import org.lisoft.lsml.messages.GarageMessage.Type;
import org.lisoft.lsml.messages.Message;
import org.lisoft.lsml.messages.MessageXBar;
import org.lisoft.lsml.model.garage.DropShip;
import org.lisoft.lsml.model.garage.MechGarage;
import org.lisoft.lsml.model.item.Faction;

public class DropShipContainerNode extends FilterTreeNode<DropShip> {
    private MechGarage    garage = null;
    private final Faction faction;

    public DropShipContainerNode(String aName, TreeNode aParent, GarageTreeModel aModel, MessageXBar xbar,
            JTextField aFilterBar, GarageTree aGarageTree, Faction aFaction) {
        super(xbar, aName, aParent, aModel, aFilterBar, aGarageTree);
        faction = aFaction;
    }

    /**
     * @return the faction
     */
    public Faction getFaction() {
        return faction;
    }

    @Override
    public void receive(Message aMsg) {
        assert (SwingUtilities.isEventDispatchThread());
        if (aMsg instanceof GarageMessage) {
            GarageMessage msg = (GarageMessage) aMsg;
            if (msg.type == Type.NewGarage) {
                garage = msg.garage;
            }
            garageChanged();
        }
        else if (aMsg instanceof DropShipMessage) {
            garageChanged();
        }
        super.receive(aMsg);
    }

    @Override
    protected boolean filter(DropShip aDropShip) {
        return aDropShip.getName().toLowerCase().contains(getFilterString());
    }

    private void garageChanged() {
        children.clear();
        if (garage != null) {
            for (DropShip dropShip : garage.getDropShips()) {
                if (dropShip.getFaction().isCompatible(faction))
                    children.add(dropShip);
            }
        }
        setDirtyBit();
        getModel().notifyTreeChange(new TreeModelEvent(this, getPath()));
        garageTree.expandPath(getPath());
    }
}
