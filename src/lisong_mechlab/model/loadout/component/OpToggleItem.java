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
package lisong_mechlab.model.loadout.component;

import lisong_mechlab.model.item.Item;
import lisong_mechlab.model.item.ItemDB;
import lisong_mechlab.model.loadout.LoadoutBase;
import lisong_mechlab.model.loadout.component.ConfiguredComponentBase.ComponentMessage.Type;
import lisong_mechlab.util.OperationStack.Operation;
import lisong_mechlab.util.message.MessageXBar;

/**
 * This operation toggles the state of toggleable items, for now HA/LAA.
 * 
 * @author Li Song
 */
public class OpToggleItem extends Operation {
    private final Item                        item;
    private final MessageXBar                 xBar;
    private final LoadoutBase<?>              loadout;
    private final ConfiguredComponentOmniMech component;
    private final boolean                     newState;
    private boolean                           oldState;
    private boolean                           oldHAState;

    public OpToggleItem(MessageXBar aXBar, LoadoutBase<?> aLoadout, ConfiguredComponentOmniMech aComponent, Item aItem,
            boolean aNewState) {
        if (aItem != ItemDB.HA && aItem != ItemDB.LAA) {
            throw new IllegalArgumentException("Can't toggle anything but HA/LAA");
        }
        xBar = aXBar;
        item = aItem;
        loadout = aLoadout;
        component = aComponent;
        newState = aNewState;
    }

    @Override
    public String describe() {
        return "toggle " + item;
    }

    @Override
    protected void apply() {
        oldState = component.getToggleState(item);
        oldHAState = component.getToggleState(ItemDB.HA);

        if (newState == oldState)
            return;

        if (newState == true) {
            if (loadout.getNumCriticalSlotsFree() < 1) {
                throw new IllegalArgumentException("Not enough globally free slots to toggle " + item);
            }
            if (!component.canToggleOn(item)) {
                throw new IllegalArgumentException("Not allowed to toggle " + item);
            }
        }

        component.setToggleState(item, newState);

        if (item == ItemDB.LAA && newState == false && component.getToggleState(ItemDB.HA)) {
            component.setToggleState(ItemDB.HA, false);
        }

        if (xBar != null) {
            xBar.post(new ConfiguredComponentBase.ComponentMessage(component, Type.ItemsChanged));
        }
    }

    @Override
    protected void undo() {
        if (newState == oldState)
            return;
        component.setToggleState(item, oldState);
        if (item == ItemDB.LAA && oldHAState == true && component.canToggleOn(ItemDB.HA)) {
            component.setToggleState(ItemDB.HA, true);
        }
        if (xBar != null) {
            xBar.post(new ConfiguredComponentBase.ComponentMessage(component, Type.ItemsChanged));
        }
    }
}
