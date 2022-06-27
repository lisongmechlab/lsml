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

import com.thoughtworks.xstream.annotations.XStreamAlias;
import org.lisoft.lsml.model.chassi.ChassisStandard;
import org.lisoft.lsml.model.chassi.Location;
import org.lisoft.lsml.model.item.Engine;
import org.lisoft.lsml.model.item.Item;
import org.lisoft.lsml.model.modifiers.Modifier;
import org.lisoft.lsml.model.upgrades.UpgradesMutable;

import java.util.Collection;

/**
 * This class represents the complete state of a 'mechs configuration.
 *
 * @author Li Song
 */
@XStreamAlias("loadout")
public class LoadoutStandard extends Loadout {
    private final UpgradesMutable upgrades;

    /**
     * Will create a new, empty load out based on the given chassis.
     *
     * @param aComponents      The components of this loadout.
     * @param aChassi          The chassis to base the load out on.
     * @param aUpgradesMutable The {@link UpgradesMutable} that will be used for this chassis.
     * @param aWeaponGroups
     */
    LoadoutStandard(ConfiguredComponentStandard[] aComponents, ChassisStandard aChassi,
                    UpgradesMutable aUpgradesMutable, WeaponGroups aWeaponGroups) {
        super(aComponents, aChassi, aWeaponGroups);

        upgrades = aUpgradesMutable;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (!(obj instanceof LoadoutStandard)) {
            return false;
        }
        if (!super.equals(obj)) {
            return false;
        }
        final LoadoutStandard other = (LoadoutStandard) obj;
        return upgrades.equals(other.upgrades);
    }

    @Override
    public ChassisStandard getChassis() {
        return (ChassisStandard) super.getChassis();
    }

    @Override
    public ConfiguredComponentStandard getComponent(Location aLocation) {
        return (ConfiguredComponentStandard) super.getComponent(aLocation);
    }

    /**
     * @return The {@link Engine} equipped on this loadout, or <code>null</code> if no engine is equipped.
     */
    @Override
    public Engine getEngine() {
        // The engine is not among the fixed items for a standard loadout.
        for (final Item item : getComponent(Location.CenterTorso).getItemsEquipped()) {
            if (item instanceof Engine) {
                return (Engine) item;
            }
        }
        return null;
    }

    @Override
    public int getJumpJetsMax() {
        return getChassis().getJumpJetsMax();
    }

    @Override
    public Collection<Modifier> getQuirks() {
        return getChassis().getQuirks();
    }

    @Override
    public int getSlotsUsed() {
        int ans = getUpgrades().getStructure().getExtraSlots() + getUpgrades().getArmour().getDynamicSlots();
        for (final ConfiguredComponent component : getComponents()) {
            ans += component.getSlotsUsed();
        }
        return ans;
    }

    @Override
    public UpgradesMutable getUpgrades() {
        return upgrades;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = super.hashCode();
        result = prime * result + upgrades.hashCode();
        return result;
    }
}
