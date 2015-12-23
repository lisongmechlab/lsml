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
package org.lisoft.lsml.view_fx.loadout.component;

import java.util.List;

import org.lisoft.lsml.messages.ItemMessage;
import org.lisoft.lsml.messages.Message;
import org.lisoft.lsml.messages.MessageReceiver;
import org.lisoft.lsml.messages.MessageReception;
import org.lisoft.lsml.model.DynamicSlotDistributor;
import org.lisoft.lsml.model.chassi.Location;
import org.lisoft.lsml.model.datacache.ItemDB;
import org.lisoft.lsml.model.item.HeatSink;
import org.lisoft.lsml.model.item.Item;
import org.lisoft.lsml.model.loadout.LoadoutBase;
import org.lisoft.lsml.model.loadout.component.ConfiguredComponentBase;

import javafx.collections.ObservableListBase;

/**
 * This is an observable, read-only list of the equipment on a component of a loadout.
 * 
 * @author Li Song
 */
public class ComponentItemsList extends ObservableListBase<Item> implements MessageReceiver {
    private final LoadoutBase<?>         loadout;
    private final Location               location;
    private final DynamicSlotDistributor distributor;

    public ComponentItemsList(MessageReception aMessageReception, LoadoutBase<?> aLoadout, Location aLocation,
            DynamicSlotDistributor aDistributor) {
        aMessageReception.attach(this);
        loadout = aLoadout;
        location = aLocation;
        distributor = aDistributor;
    }

    public boolean isFixed(int aIndex) {
        ConfiguredComponentBase component = loadout.getComponent(location);

        List<Item> fixed = component.getItemsFixed();
        if (aIndex < fixed.size()) {
            return true;
        }

        int equipIndex = aIndex - fixed.size();
        List<Item> equipped = component.getItemsEquipped();
        if (equipIndex < equipped.size()) {
            return false;
        }
        equipIndex -= equipped.size();

        int armor = distributor.getDynamicArmorSlots(component);
        if (equipIndex < armor) {
            return true;
        }
        equipIndex -= armor;
        int structure = distributor.getDynamicStructureSlots(component);
        if (equipIndex < structure) {
            return true;
        }
        return false;
    }

    @Override
    public Item get(int aIndex) {
        ConfiguredComponentBase component = loadout.getComponent(location);

        List<Item> fixed = component.getItemsFixed();
        if (aIndex < fixed.size()) {
            return fixed.get(aIndex);
        }

        int equipIndex = aIndex - fixed.size();
        List<Item> equipped = component.getItemsEquipped();
        if (equipIndex < equipped.size()) {
            return equipped.get(equipIndex);
        }
        equipIndex -= equipped.size();

        int armor = distributor.getDynamicArmorSlots(component);
        if (equipIndex < armor) {
            return ItemDB.DYN_ARMOR;
        }
        equipIndex -= armor;
        int structure = distributor.getDynamicStructureSlots(component);
        if (equipIndex < structure) {
            return ItemDB.DYN_STRUCT;
        }
        return null;
    }

    @Override
    public int size() {
        ConfiguredComponentBase component = loadout.getComponent(location);
        List<Item> fixed = component.getItemsFixed();
        List<Item> equipped = component.getItemsEquipped();

        int armor = distributor.getDynamicArmorSlots(component);
        int structure = distributor.getDynamicStructureSlots(component);
        return fixed.size() + equipped.size() + armor + structure;
    }

    @Override
    public void receive(Message aMsg) {
        if (!(aMsg instanceof ItemMessage)) {
            return;
        }

        ItemMessage msg = (ItemMessage) aMsg;
        if (!(msg.component == loadout.getComponent(location))) {
            return;
        }

        beginChange();
        switch (msg.type) {
            case Added: {
                if (msg.relativeIndex < 0) {
                    if (msg.item instanceof HeatSink) {
                        // Heat sink was consumed into engine... no-op
                        // FIXME: Do I need to redraw?
                    }
                    else {
                        int fixedIdx = msg.component.getItemsFixed().size() - 1;
                        nextAdd(fixedIdx, fixedIdx + 1);
                    }
                }
                else {
                    nextAdd(msg.relativeIndex, msg.relativeIndex + 1);
                }
                break;
            }
            case Removed: {
                if (msg.relativeIndex < 0) {
                    if (msg.item instanceof HeatSink) {
                        // Heat sink removed from inside of engine... no-op
                        // FIXME: Do I need to redraw?
                    }
                    else {
                        int fixedIdx = msg.component.getItemsFixed().size() - 1;
                        nextRemove(fixedIdx, msg.item);
                    }
                }
                else {
                    nextRemove(msg.relativeIndex, msg.item);
                }
                break;
            }
            default:
                throw new RuntimeException("Unknown message type!");
        }

        endChange();
    }
}
