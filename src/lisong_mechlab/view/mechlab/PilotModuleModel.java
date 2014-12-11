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
package lisong_mechlab.view.mechlab;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.swing.ListModel;
import javax.swing.event.ListDataEvent;
import javax.swing.event.ListDataListener;

import lisong_mechlab.model.item.ModuleSlot;
import lisong_mechlab.model.item.PilotModule;
import lisong_mechlab.model.loadout.LoadoutBase;
import lisong_mechlab.model.loadout.LoadoutMessage;
import lisong_mechlab.model.loadout.LoadoutMessage.Type;
import lisong_mechlab.util.message.Message;
import lisong_mechlab.util.message.MessageXBar;

/**
 * This class implements a {@link ListModel} for selecting {@link PilotModule}s for a {@link LoadoutBase}.
 * 
 * @author Li Song
 */
public class PilotModuleModel implements ListModel<String>, Message.Recipient {
    private final LoadoutBase<?>        loadout;
    private final Set<ListDataListener> listeners = new HashSet<>();
    private final List<PilotModule>     modules   = new ArrayList<>();
    private final ModuleSlot            moduleSlot;

    public final static String          EMPTY     = "EMPTY";

    public PilotModuleModel(LoadoutBase<?> aLoadout, MessageXBar aXBar, ModuleSlot aModuleSlot) {
        loadout = aLoadout;
        moduleSlot = aModuleSlot;
        aXBar.attach(this);
        updateModules();
    }

    private void fireListeners() {
        ListDataEvent e = new ListDataEvent(this, ListDataEvent.CONTENTS_CHANGED, 0, modules.size());
        for (ListDataListener listener : listeners) {

            listener.contentsChanged(e);
        }
    }

    private void updateModules() {
        modules.clear();

        if (moduleSlot == ModuleSlot.HYBRID) {
            int weaponMax = loadout.getModulesMax(ModuleSlot.WEAPON);
            int mechMax = loadout.getModulesMax(ModuleSlot.MECH);
            for (PilotModule module : loadout.getModules()) {
                if (module.getSlot() == ModuleSlot.WEAPON) {
                    weaponMax--;
                    if (weaponMax < 0) {
                        modules.add(module); // Overflows
                    }
                }
                if (module.getSlot() == ModuleSlot.MECH) {
                    mechMax--;
                    if (mechMax < 0) {
                        modules.add(module); // Overflows
                    }
                }
            }
        }
        else {
            final int max = loadout.getModulesMax(moduleSlot);
            for (PilotModule module : loadout.getModules()) {
                if (module.getSlot() == moduleSlot) {
                    modules.add(module);
                }
                if (modules.size() == max) {
                    break;
                }
            }
        }
        fireListeners();
    }

    @Override
    public int getSize() {
        return loadout.getModulesMax(moduleSlot);
    }

    @Override
    public String getElementAt(int aIndex) {
        if (aIndex >= modules.size())
            return EMPTY;
        return modules.get(aIndex).getName();
    }

    @Override
    public void addListDataListener(ListDataListener aL) {
        listeners.add(aL);
    }

    @Override
    public void removeListDataListener(ListDataListener aL) {
        listeners.remove(aL);
    }

    @Override
    public void receive(Message aMsg) {
        if (aMsg.isForMe(loadout) && aMsg instanceof LoadoutMessage) {
            LoadoutMessage msg = (LoadoutMessage) aMsg;
            if (msg.type == Type.MODULES_CHANGED) {
                updateModules();
            }
        }
    }
}
