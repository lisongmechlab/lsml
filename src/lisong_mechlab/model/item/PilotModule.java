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
package lisong_mechlab.model.item;

import com.thoughtworks.xstream.annotations.XStreamAsAttribute;

/**
 * Base class that models modules that can be equipped in module slots on mechs.
 * 
 * @author Li Song
 */
public class PilotModule {

    private final String          locName;
    private final String          locDesc;
    @XStreamAsAttribute
    private final String          mwoName;
    @XStreamAsAttribute
    private final int             mwoIdx;
    @XStreamAsAttribute
    private final Faction         faction;
    @XStreamAsAttribute
    private final ModuleCathegory cathegory;
    @XStreamAsAttribute
    private final ModuleSlot      slotType;

    /**
     * Creates a new {@link PilotModule}.
     * 
     * @param aMwoName
     *            The name of the module in the MWO data files.
     * @param aMwoIdx
     *            The ID of the module in the MWO data files.
     * @param aName
     *            The human readable name of the module.
     * @param aDescription
     *            The human readable description of the module.
     * @param aFaction
     *            The required faction for this module.
     * @param aCathegory
     *            The {@link ModuleCathegory} for this {@link Module}.
     * @param aSlotType
     *            The {@link ModuleSlot} of the module.
     */
    public PilotModule(String aMwoName, int aMwoIdx, String aName, String aDescription, Faction aFaction,
            ModuleCathegory aCathegory, ModuleSlot aSlotType) {
        mwoName = aMwoName;
        mwoIdx = aMwoIdx;
        locName = aName;
        locDesc = aDescription;
        faction = aFaction;
        cathegory = aCathegory;
        slotType = aSlotType;
    }

    public String getKey() {
        return mwoName;
    }

    @Override
    public String toString() {
        return getName();
    }

    public String getName() {
        return locName;
    }

    public int getMwoId() {
        return mwoIdx;
    }

    public String getDescription() {
        return locDesc;
    }

    public Faction getFaction() {
        return faction;
    }

    public ModuleCathegory getCathegory() {
        return cathegory;
    }

    public ModuleSlot getSlot() {
        return slotType;
    }
}
