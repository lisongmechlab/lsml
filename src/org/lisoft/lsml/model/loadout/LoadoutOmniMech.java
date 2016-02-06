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
package org.lisoft.lsml.model.loadout;

import java.util.Collection;

import org.lisoft.lsml.model.chassi.ChassisOmniMech;
import org.lisoft.lsml.model.chassi.Location;
import org.lisoft.lsml.model.chassi.OmniPod;
import org.lisoft.lsml.model.item.Engine;
import org.lisoft.lsml.model.item.ModuleSlot;
import org.lisoft.lsml.model.loadout.component.ConfiguredComponentOmniMech;
import org.lisoft.lsml.model.modifiers.Modifier;
import org.lisoft.lsml.model.upgrades.Upgrades;
import org.lisoft.lsml.util.CommandStack.Command;

import com.thoughtworks.xstream.annotations.XStreamAlias;

/**
 * This class represents a configured loadout for an omnimech.
 * 
 * @author Li Song
 */
@XStreamAlias("loadout")
public class LoadoutOmniMech extends Loadout {
    transient private final Upgrades upgrades;

    /**
     * Creates a new, empty loadout.
     * 
     * @param aComponents
     *            The components of this loadout.
     * @param aChassis
     *            The chassis to base this loadout on.
     * @param aUpgrades
     *            The upgrades to use.
     * @param aWeaponGroups
     *            The weapon groups object for this loadout.
     */
    LoadoutOmniMech(ConfiguredComponentOmniMech[] aComponents, ChassisOmniMech aChassis, Upgrades aUpgrades,
            WeaponGroups aWeaponGroups) {
        super(aComponents, aChassis, aWeaponGroups);
        upgrades = aUpgrades;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = super.hashCode();
        result = prime * result + ((upgrades == null) ? 0 : upgrades.hashCode());
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (!super.equals(obj))
            return false;
        LoadoutOmniMech other = (LoadoutOmniMech) obj;
        if (!upgrades.equals(other.upgrades))
            return false;
        return true;
    }

    /**
     * This setter method is only intended to be used from package local {@link Command}s. It's a raw, unchecked
     * accessor.
     * 
     * @param aOmniPod
     *            The omnipod to set, it's put in it's dedicated slot.
     */
    public void setOmniPod(OmniPod aOmniPod) {
        ConfiguredComponentOmniMech component = getComponent(aOmniPod.getLocation());
        component.setOmniPod(aOmniPod);
    }

    @Override
    public int getJumpJetsMax() {
        int ans = getChassis().getFixedJumpJets();
        for (Location location : Location.values()) {
            ans += getComponent(location).getOmniPod().getJumpJetsMax();
        }
        return ans;
    }

    @Override
    public ChassisOmniMech getChassis() {
        return (ChassisOmniMech) super.getChassis();
    }

    @Override
    public Engine getEngine() {
        return getChassis().getFixedEngine();
    }

    /**
     * @return The number of globally used critical slots.
     */
    @Override
    public int getNumCriticalSlotsUsed() {
        int ans = 0;
        for (Location location : Location.values()) {
            ans += getComponent(location).getSlotsUsed();
        }
        return ans;
    }

    @Override
    public Upgrades getUpgrades() {
        return upgrades;
    }

    @Override
    public ConfiguredComponentOmniMech getComponent(Location aLocation) {
        return (ConfiguredComponentOmniMech) super.getComponent(aLocation);
    }

    @Override
    public int getModulesMax(ModuleSlot aModuleSlot) {
        if (aModuleSlot == ModuleSlot.MECH) {
            int ans = getChassis().getMechModulesMax();
            for (Location location : Location.values()) {
                ans += getComponent(location).getOmniPod().getPilotModulesMax();
            }
            return ans;
        }
        else if (aModuleSlot == ModuleSlot.CONSUMABLE) {
            return getChassis().getConsumableModulesMax();
        }
        else if (aModuleSlot == ModuleSlot.WEAPON) {
            return getChassis().getWeaponModulesMax();
        }
        else if (aModuleSlot == ModuleSlot.HYBRID) {
            return 1; // +1 for mastery
        }
        else {
            throw new IllegalArgumentException("Unknown module slot type!");
        }
    }

    @Override
    public Collection<Modifier> getModifiers() {
        Collection<Modifier> ans = super.getModifiers();
        for (Location location : Location.values()) {
            ans.addAll(getComponent(location).getOmniPod().getQuirks());
        }
        return ans;
    }
}
