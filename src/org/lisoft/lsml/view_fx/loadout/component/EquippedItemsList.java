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

import java.util.Arrays;
import java.util.List;

import org.lisoft.lsml.messages.ItemMessage;
import org.lisoft.lsml.messages.Message;
import org.lisoft.lsml.messages.MessageReceiver;
import org.lisoft.lsml.messages.MessageReception;
import org.lisoft.lsml.messages.UpgradesMessage;
import org.lisoft.lsml.messages.UpgradesMessage.ChangeMsg;
import org.lisoft.lsml.model.DynamicSlotDistributor;
import org.lisoft.lsml.model.datacache.ItemDB;
import org.lisoft.lsml.model.item.Engine;
import org.lisoft.lsml.model.item.HeatSink;
import org.lisoft.lsml.model.item.Internal;
import org.lisoft.lsml.model.item.Item;
import org.lisoft.lsml.model.loadout.component.ConfiguredComponentBase;
import org.lisoft.lsml.model.loadout.component.ConfiguredComponentOmniMech;

import javafx.collections.ObservableListBase;

/**
 * This is an observable, read-only list of the equipment on a component of a loadout.
 * 
 * @author Li Song
 */
public class EquippedItemsList extends ObservableListBase<Item> implements MessageReceiver {
    private final DynamicSlotDistributor  distributor;
    private final ConfiguredComponentBase component;

    enum EquippedType {
        FIXED, EQUIPPED, DYN_ARMOR, DYN_STRUCTURE, EMPTY
    }

    private static class Classification {
        public final Item         item;
        public final EquippedType type;

        public Classification(Item aItem, EquippedType aType) {
            item = aItem;
            type = aType;
        }
    }

    public EquippedItemsList(MessageReception aMessageReception, ConfiguredComponentBase aComponent,
            DynamicSlotDistributor aDistributor) {
        aMessageReception.attach(this);
        distributor = aDistributor;
        component = aComponent;
    }

    private Classification classify(int aIndex) {
        int visibleLeft = aIndex;
        int engineHeatSinks = component.getEngineHeatSinks();

        EquippedType type = EquippedType.FIXED;
        for (List<Item> items : Arrays.asList(component.getItemsFixed(), component.getItemsEquipped())) {
            for (Item item : items) {
                if (engineHeatSinks > 0 && item instanceof HeatSink) {
                    engineHeatSinks--; // Consumed by engine
                }
                else {
                    if (visibleLeft == 0) {
                        if (item instanceof Internal)
                            type = EquippedType.FIXED;
                        return new Classification(item, type);
                    }
                    visibleLeft--;
                }
            }
            type = EquippedType.EQUIPPED;
        }

        final boolean omni = component instanceof ConfiguredComponentOmniMech;
        final int armorSlots = distributor.getDynamicArmorSlots(component);
        if (visibleLeft < armorSlots) {
            return new Classification(omni ? ItemDB.FIX_ARMOR : ItemDB.DYN_ARMOR, EquippedType.DYN_ARMOR);
        }

        visibleLeft -= armorSlots;
        if (visibleLeft < distributor.getDynamicStructureSlots(component)) {
            return new Classification(omni ? ItemDB.FIX_STRUCT : ItemDB.DYN_STRUCT, EquippedType.DYN_STRUCTURE);
        }
        return new Classification(null, EquippedType.EMPTY);
    }

    public boolean isFixed(int aIndex) {
        return classify(aIndex).type != EquippedType.EQUIPPED;
    }

    @Override
    public Item get(int aIndex) {
        return classify(aIndex).item;
    }

    private int sizeOfItems() {
        List<Item> fixed = component.getItemsFixed();
        List<Item> equipped = component.getItemsEquipped();

        int engineHS = component.getEngineHeatSinks();
        return fixed.size() + equipped.size() - engineHS;
    }

    private int sizeOfDynamics() {
        int armor = distributor.getDynamicArmorSlots(component);
        int structure = distributor.getDynamicStructureSlots(component);
        return armor + structure;
    }

    @Override
    public int size() {
        int actualSize = sizeOfItems() + sizeOfDynamics();
        // Size must always at least one to prevent "empty list" display from happening
        // when the contents are empty. This means that a "null" item will appear
        // in the rendering but this is OK as they do that already anyway.
        return Math.max(actualSize, 1);
    }

    private void nextEngineUpdate() {
        int enginePos = 0;
        while (enginePos < size() && !(get(enginePos) instanceof Engine)) {
            enginePos++;
        }
        nextUpdate(enginePos);
    }

    private void nextUpdateDynamic() {
        int start = sizeOfItems();
        // We have to go to the end here as we don't know how many slots of
        // dynamic armor were here originally.
        int end = component.getInternalComponent().getSlots();
        while (start < end) {
            nextUpdate(start);
            start++;
        }
    }

    private void changeDynamics() {
        beginChange();
        nextUpdateDynamic();
        endChange();
    }

    @Override
    public void receive(Message aMsg) {
        if (!(aMsg instanceof ItemMessage)) {
            if (aMsg instanceof UpgradesMessage) {
                UpgradesMessage msg = (UpgradesMessage) aMsg;
                if (msg.msg == ChangeMsg.ARMOR || msg.msg == ChangeMsg.STRUCTURE) {
                    changeDynamics();
                }
            }
            return;
        }

        ItemMessage msg = (ItemMessage) aMsg;
        if (!(msg.component == component)) {
            changeDynamics();
            return;
        }

        beginChange();
        switch (msg.type) {
            case Added: {
                if (msg.relativeIndex < 0) {
                    if (msg.item instanceof HeatSink) {
                        nextEngineUpdate();
                    }
                    else {
                        int fixedIdx = component.getItemsFixed().size() - 1;
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
                        nextEngineUpdate();
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
