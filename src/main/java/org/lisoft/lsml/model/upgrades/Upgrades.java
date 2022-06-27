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
package org.lisoft.lsml.model.upgrades;

/**
 * This class is a simple container that manages upgrades for an loadout.
 *
 * @author Li Song
 */
public class Upgrades {
    protected ArmourUpgrade armourType;
    protected GuidanceUpgrade guidanceType;
    protected HeatSinkUpgrade heatSinkType;
    protected StructureUpgrade structureType;

    public Upgrades(ArmourUpgrade aArmour, StructureUpgrade aStructure, GuidanceUpgrade aGuidance,
                    HeatSinkUpgrade aHeatSinks) {
        armourType = aArmour;
        structureType = aStructure;
        guidanceType = aGuidance;
        heatSinkType = aHeatSinks;
    }

    public Upgrades(Upgrades aUpgrades) {
        this(aUpgrades.armourType, aUpgrades.structureType, aUpgrades.guidanceType, aUpgrades.heatSinkType);
    }

    /**
     * Assigns the upgrades of that to this.
     *
     * @param aUpgrades The upgrades to copy.
     */
    public void assign(Upgrades aUpgrades) {
        armourType = aUpgrades.armourType;
        structureType = aUpgrades.structureType;
        guidanceType = aUpgrades.guidanceType;
        heatSinkType = aUpgrades.heatSinkType;
    }

    @Override
    public boolean equals(Object obj) {
        if (!(obj instanceof Upgrades)) {
            return false;
        }
        final Upgrades other = (Upgrades) obj;
        return armourType == other.armourType && guidanceType == other.guidanceType &&
               heatSinkType == other.heatSinkType && structureType == other.structureType;
    }

    public ArmourUpgrade getArmour() {
        return armourType;
    }

    public GuidanceUpgrade getGuidance() {
        return guidanceType;
    }

    public HeatSinkUpgrade getHeatSink() {
        return heatSinkType;
    }

    public StructureUpgrade getStructure() {
        return structureType;
    }

    public <T extends Upgrade> T getUpgradeOfType(Class<T> aClass) {
        if (aClass.isAssignableFrom(ArmourUpgrade.class)) {
            return aClass.cast(getArmour());
        }
        if (aClass.isAssignableFrom(StructureUpgrade.class)) {
            return aClass.cast(getStructure());
        }
        if (aClass.isAssignableFrom(GuidanceUpgrade.class)) {
            return aClass.cast(getGuidance());
        }
        if (aClass.isAssignableFrom(HeatSinkUpgrade.class)) {
            return aClass.cast(getHeatSink());
        }
        throw new IllegalArgumentException("getUpgradeOfType must be called with an upgrade type class!");
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + armourType.hashCode();
        result = prime * result + guidanceType.hashCode();
        result = prime * result + heatSinkType.hashCode();
        result = prime * result + structureType.hashCode();
        return result;
    }

    /**
     * Changes the guidance type.
     * <p>
     * This is package visibility as it is only intended to be modified by the Op* classes.
     *
     * @param aGuidanceUpgrade The new {@link GuidanceUpgrade}.
     */
    public void setGuidance(GuidanceUpgrade aGuidanceUpgrade) {
        guidanceType = aGuidanceUpgrade;
    }

}
