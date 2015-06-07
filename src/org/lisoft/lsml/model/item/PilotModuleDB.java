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
package org.lisoft.lsml.model.item;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.lisoft.lsml.model.DataCache;

/**
 * This class acts as a database of all the pilot modules that are parsed.
 * 
 * @author Li Song
 */
public class PilotModuleDB {
    private final static Map<Integer, PilotModule> mwoidx2module;
    private final static Map<String, PilotModule>  name2module;

    public static PilotModule lookup(int aId) {
        return mwoidx2module.get(aId);
    }

    public static List<PilotModule> lookup(ModuleCathegory aCathegory) {
        List<PilotModule> ans = new ArrayList<>();
        for (PilotModule module : mwoidx2module.values()) {
            if (module.getCathegory() == aCathegory) {
                ans.add(module);
            }
        }
        return ans;
    }

    public static List<PilotModule> lookup(ModuleSlot aSlotType) {
        List<PilotModule> ans = new ArrayList<>();
        for (PilotModule module : mwoidx2module.values()) {
            if (module.getSlot() == aSlotType) {
                ans.add(module);
            }
        }
        return ans;
    }

    public static List<PilotModule> lookup(Class<? extends PilotModule> aClass) {
        List<PilotModule> ans = new ArrayList<>();
        for (PilotModule module : mwoidx2module.values()) {
            if (aClass.isAssignableFrom(module.getClass())) {
                ans.add(module);
            }
        }
        return ans;
    }

    /**
     * A decision has been made to rely on static initializers for *DB classes. The motivation is that all items are
     * immutable, and this is the only way that allows providing global item constants such as ItemDB.AMS.
     */
    static {
        DataCache dataCache;
        try {
            dataCache = DataCache.getInstance();
        }
        catch (IOException e) {
            throw new RuntimeException(e); // Promote to unchecked. This is a critical failure.
        }

        mwoidx2module = new HashMap<>();
        name2module = new HashMap<>();

        for (PilotModule module : dataCache.getPilotModules()) {
            mwoidx2module.put(module.getMwoId(), module);
            name2module.put(module.getName(), module);
        }
    }

    /**
     * Looks up a pilot module by string name.
     * 
     * @param aName
     *            The name of the module to lookup.
     * @return A {@link PilotModule} by the given name.
     */
    public static PilotModule lookup(String aName) {
        PilotModule module = name2module.get(aName);
        if (module == null) {
            throw new IllegalArgumentException("No module by name: " + aName);
        }
        return module;
    }
}
