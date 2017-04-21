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
package org.lisoft.lsml.view_fx.controls;

import org.lisoft.lsml.messages.EfficienciesMessage;
import org.lisoft.lsml.messages.ItemMessage;
import org.lisoft.lsml.messages.ItemMessage.Type;
import org.lisoft.lsml.messages.LoadoutMessage;
import org.lisoft.lsml.messages.Message;
import org.lisoft.lsml.messages.MessageReceiver;
import org.lisoft.lsml.messages.MessageReception;
import org.lisoft.lsml.messages.OmniPodMessage;
import org.lisoft.lsml.model.item.Ammunition;
import org.lisoft.lsml.model.item.Item;
import org.lisoft.lsml.model.item.Weapon;
import org.lisoft.lsml.model.loadout.Loadout;
import org.lisoft.lsml.view_fx.util.WeaponSummary;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.ObservableListBase;

/**
 * This provides an observable view over the weapon summary state of a {@link Loadout}.
 * 
 * @author Emily Björk
 */
public class WeaponSummaryList extends ObservableListBase<WeaponSummary> implements MessageReceiver {

    private final ObservableList<WeaponSummary> entries = FXCollections.observableArrayList();
    private Loadout loadout;

    private void add(Item aItem) {
        for (WeaponSummary summary : entries) {
            if (summary.consume(aItem)) {
                beginChange();
                nextUpdate(entries.indexOf(summary));
                endChange();
                return;
            }
        }

        beginChange();
        int idx = entries.size();
        entries.add(new WeaponSummary(loadout, aItem));
        nextAdd(idx, idx + 1);
        endChange();
    }

    public WeaponSummaryList(MessageReception aReception, Loadout aLoadoutBase) {
        aReception.attach(this);
        loadout = aLoadoutBase;
        for (Ammunition ammunition : aLoadoutBase.items(Ammunition.class)) {
            add(ammunition);
        }
        for (Weapon weapon : aLoadoutBase.items(Weapon.class)) {
            add(weapon);
        }
    }

    @Override
    public WeaponSummary get(int aArg0) {
        return entries.get(aArg0);
    }

    @Override
    public int size() {
        return entries.size();
    }

    @Override
    public void receive(Message aMsg) {
        if (aMsg instanceof ItemMessage) {
            ItemMessage itemMessage = (ItemMessage) aMsg;
            if (!(itemMessage.item instanceof Ammunition || itemMessage.item instanceof Weapon)) {
                return;
            }

            if (itemMessage.type == Type.Added) {
                boolean consumed = false;
                for (WeaponSummary summary : entries) {
                    if (summary.consume(itemMessage.item)) {
                        beginChange();
                        nextUpdate(entries.indexOf(summary));
                        endChange();
                        consumed = true;
                        break;
                    }
                }
                if (!consumed) {
                    beginChange();
                    int idx = entries.size();
                    entries.add(new WeaponSummary(loadout, itemMessage.item));
                    nextAdd(idx, idx + 1);
                    endChange();
                }
            }
            else if (itemMessage.type == Type.Removed) {
                for (WeaponSummary summary : entries) {
                    if (summary.remove(itemMessage.item)) {
                        int idx = entries.indexOf(summary);
                        if (summary.empty()) {
                            beginChange();
                            nextRemove(idx, entries.remove(idx));
                            endChange();
                        }
                        else {
                            beginChange();
                            nextUpdate(idx);
                            endChange();
                        }
                        break;
                    }
                }
            }
        }
        else if (aMsg instanceof LoadoutMessage || aMsg instanceof OmniPodMessage
                || aMsg instanceof EfficienciesMessage) {
            // Efficiencies or quirks changed, update values.
            beginChange();
            int sz = size();
            for (int i = 0; i < sz; ++i) {
                get(i).battleTimeProperty().invalidate();
                nextUpdate(i);
            }
            endChange();
        }
    }

}
