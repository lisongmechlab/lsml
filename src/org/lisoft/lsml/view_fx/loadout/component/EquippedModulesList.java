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

import java.util.Optional;

import org.lisoft.lsml.messages.LoadoutMessage;
import org.lisoft.lsml.messages.LoadoutMessage.Type;
import org.lisoft.lsml.messages.Message;
import org.lisoft.lsml.messages.MessageReceiver;
import org.lisoft.lsml.messages.MessageReception;
import org.lisoft.lsml.model.item.ModuleSlot;
import org.lisoft.lsml.model.item.PilotModule;
import org.lisoft.lsml.model.loadout.LoadoutBase;

import javafx.collections.ObservableListBase;

/**
 * This is an observable, read-only list of the equipment on a component of a loadout.
 * 
 * @author Li Song
 */
public class EquippedModulesList extends ObservableListBase<PilotModule> implements MessageReceiver {
    private final LoadoutBase<?> loadout;
    private final ModuleSlot     moduleType;

    public EquippedModulesList(MessageReception aMessageReception, LoadoutBase<?> aLoadout, ModuleSlot aModuleType) {
        aMessageReception.attach(this);
        loadout = aLoadout;
        moduleType = aModuleType;
    }

    @Override
    public PilotModule get(int aIndex) {
        int typeIndex = aIndex;

        if (moduleType == ModuleSlot.HYBRID) {
            int maxWeapon = loadout.getModulesMax(ModuleSlot.WEAPON);
            int maxMech = loadout.getModulesMax(ModuleSlot.MECH);
            for (PilotModule module : loadout.getModules()) {
                if (module.getSlot() == ModuleSlot.MECH) {
                    if (maxMech == 0)
                        return module;
                    maxMech--;
                }
                else if (module.getSlot() == ModuleSlot.WEAPON) {
                    if (maxWeapon == 0)
                        return module;
                    maxWeapon--;
                }
            }
            return null;
        }
        Optional<PilotModule> result = loadout.getModules().stream().filter((aModule) -> {
            return aModule.getSlot() == moduleType;
        }).skip(typeIndex).findFirst();
        return result.orElse(null);
    }

    @Override
    public int size() {
        return loadout.getModulesMax(moduleType);
    }

    @Override
    public void receive(Message aMsg) {
        if (aMsg instanceof LoadoutMessage) {
            LoadoutMessage loadoutMessage = (LoadoutMessage) aMsg;
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
}
