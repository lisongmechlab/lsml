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

import org.lisoft.lsml.messages.GarageMessage;
import org.lisoft.lsml.messages.GarageMessageType;
import org.lisoft.lsml.messages.LoadoutMessage;
import org.lisoft.lsml.messages.Message;
import org.lisoft.lsml.messages.MessageXBar;
import org.lisoft.lsml.model.chassi.Chassis;
import org.lisoft.lsml.model.chassi.ChassisClass;
import org.lisoft.lsml.model.garage.Garage;
import org.lisoft.lsml.model.item.Faction;
import org.lisoft.lsml.model.loadout.Loadout;

class LoadoutContainerNode extends FilterTreeNode<Loadout<?>> {
    private Garage             garage = null;
    private final ChassisClass chassiClass;
    private final Faction      faction;

    public LoadoutContainerNode(String aName, TreeNode aParent, GarageTreeModel aModel, MessageXBar xbar,
            ChassisClass aChassiClass, JTextField aFilterBar, GarageTree aGarageTree, Faction aFaction) {
        super(xbar, aName, aParent, aModel, aFilterBar, aGarageTree);
        chassiClass = aChassiClass;
        faction = aFaction;
    }

    @Override
    public void receive(Message aMsg) {
        assert (SwingUtilities.isEventDispatchThread());
        if (aMsg instanceof GarageMessage) {
            GarageMessage msg = (GarageMessage) aMsg;
            if (msg.type == GarageMessageType.GARAGE_LOADED) {
                garage = msg.garageDir;
            }
            garageChanged();
        }
        else if (aMsg instanceof LoadoutMessage) {
            LoadoutMessage message = (LoadoutMessage) aMsg;
            if (message.type == LoadoutMessage.Type.CREATE || message.type == LoadoutMessage.Type.RENAME) {
                garageChanged();
            }
        }
        super.receive(aMsg);
    }

    @Override
    protected boolean filter(Loadout<?> aLoadout) {
        Chassis chassi = aLoadout.getChassis();
        return aLoadout.getName().toLowerCase().contains(getFilterString())
                || chassi.getName().toLowerCase().contains(getFilterString());
    }

    private void garageChanged() {
        children.clear();
        if (garage != null) {
            for (Loadout<?> loadout : garage.getMechs()) {
                if (loadout.getChassis().getChassiClass() == chassiClass
                        && loadout.getChassis().getFaction().isCompatible(faction))
                    children.add(loadout);
            }
        }
        setDirtyBit();
        getModel().notifyTreeChange(new TreeModelEvent(this, getPath()));
        garageTree.expandPath(getPath());
    }
}
