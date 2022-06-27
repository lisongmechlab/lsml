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
package org.lisoft.lsml.view_fx.controls;

import javafx.collections.ObservableListBase;
import org.lisoft.lsml.messages.LoadoutMessage;
import org.lisoft.lsml.messages.LoadoutMessage.Type;
import org.lisoft.lsml.messages.Message;
import org.lisoft.lsml.messages.MessageReceiver;
import org.lisoft.lsml.messages.MessageReception;
import org.lisoft.lsml.model.item.Consumable;
import org.lisoft.lsml.model.loadout.Loadout;

/**
 * This is an observable, read-only list of the equipment on a component of a loadout.
 *
 * @author Li Song
 */
public class EquippedConsumablesList extends ObservableListBase<Consumable> implements MessageReceiver {
    private final Loadout loadout;

    public EquippedConsumablesList(MessageReception aMessageReception, Loadout aLoadout) {
        aMessageReception.attach(this);
        loadout = aLoadout;
    }

    @Override
    public Consumable get(int aIndex) {
        return aIndex < loadout.getConsumables().size() ? loadout.getConsumables().get(aIndex) : null;
    }

    @Override
    public void receive(Message aMsg) {
        if (aMsg instanceof LoadoutMessage) {
            final LoadoutMessage loadoutMessage = (LoadoutMessage) aMsg;
            if (loadoutMessage.type == Type.MODULES_CHANGED) {
                beginChange();
                for (int i = 0; i < size() + 1; ++i) {
                    // This is really cheating but it's fast enough for a
                    // not so frequent event.
                    nextUpdate(i);
                }
                endChange();
            }
        }
    }

    @Override
    public int size() {
        return loadout.getConsumablesMax();
    }
}
