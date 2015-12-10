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
package org.lisoft.lsml.view_fx;

import java.util.ArrayList;
import java.util.List;

import org.lisoft.lsml.messages.GarageMessage;
import org.lisoft.lsml.messages.LoadoutMessage;
import org.lisoft.lsml.messages.LoadoutMessage.Type;
import org.lisoft.lsml.messages.Message;
import org.lisoft.lsml.messages.MessageReceiver;
import org.lisoft.lsml.messages.MessageXBar;
import org.lisoft.lsml.model.chassi.ChassisClass;
import org.lisoft.lsml.model.loadout.LoadoutBase;

import javafx.collections.ObservableListBase;

/**
 * This is a observable list that will observe a class of 'Mechs in a garage.
 * 
 * @author Li Song
 */
public class ObservableGarageList extends ObservableListBase<LoadoutBase<?>> implements MessageReceiver {

    private final List<LoadoutBase<?>> loadouts = new ArrayList<>();
    private final ChassisClass         chassisClass;

    public ObservableGarageList(ChassisClass aChassisClass, MessageXBar aXBar) {
        chassisClass = aChassisClass;
        aXBar.attach(this);
    }

    @Override
    public LoadoutBase<?> get(int aIndex) {
        return loadouts.get(aIndex);
    }

    @Override
    public int size() {
        return loadouts.size();
    }

    private final void softAddLoadout(LoadoutBase<?> aLoadoutBase) {
        if (chassisClass == null || aLoadoutBase.getChassis().getChassiClass() == chassisClass) {
            loadouts.add(aLoadoutBase);
            nextAdd(loadouts.size() - 1, loadouts.size());
        }
    }

    @Override
    public void receive(Message aMsg) {
        if (aMsg instanceof GarageMessage) {
            GarageMessage garageMessage = (GarageMessage) aMsg;
            switch (garageMessage.type) {
                case LoadoutAdded:
                    beginChange();
                    softAddLoadout(garageMessage.loadout);
                    endChange();
                    break;
                case LoadoutRemoved: {
                    beginChange();

                    int index = loadouts.indexOf(garageMessage.loadout);
                    if (index >= 0) {
                        nextRemove(index, loadouts.remove(index));
                    }
                    endChange();
                    break;
                }
                case NewGarage:
                    beginChange();
                    try {
                        loadouts.clear();
                        for (LoadoutBase<?> loadout : garageMessage.garage.getMechs()) {
                            softAddLoadout(loadout);
                        }
                    }
                    finally {
                        endChange();
                    }
                    break;
                default:
                    break; // no-op
            }
        }
        else if (aMsg instanceof LoadoutMessage) {
            LoadoutMessage loadoutMessage = (LoadoutMessage) aMsg;
            if (loadoutMessage.type == Type.RENAME) {
                int index = loadouts.indexOf(loadoutMessage.loadout);
                if (index >= 0) {
                    beginChange();
                    nextUpdate(index);
                    endChange();
                }
            }
        }
    }

}
