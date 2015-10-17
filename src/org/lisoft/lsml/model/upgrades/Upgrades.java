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

import org.lisoft.lsml.messages.Message;
import org.lisoft.lsml.model.loadout.LoadoutBase;

/**
 * This class is a simple container that manages upgrades for an loadout.
 * 
 * @author Li Song
 */
public class Upgrades {
    protected ArmorUpgrade     armorType     = UpgradeDB.STANDARD_ARMOR;
    protected StructureUpgrade structureType = UpgradeDB.STANDARD_STRUCTURE;
    protected GuidanceUpgrade  guidanceType  = UpgradeDB.STANDARD_GUIDANCE;
    protected HeatSinkUpgrade  heatSinkType  = UpgradeDB.STANDARD_HEATSINKS;

    public static class UpgradesMessage implements Message {
        public final ChangeMsg msg;
        private final Upgrades source;

        public enum ChangeMsg {
            GUIDANCE, STRUCTURE, ARMOR, HEATSINKS
        }

        @Override
        public boolean equals(Object obj) {
            if (obj instanceof UpgradesMessage) {
                UpgradesMessage other = (UpgradesMessage) obj;
                return msg == other.msg && source == other.source;
            }
            return false;
        }

        public UpgradesMessage(ChangeMsg aChangeMsg, Upgrades anUpgrades) {
            msg = aChangeMsg;
            source = anUpgrades;
        }

        @Override
        public boolean isForMe(LoadoutBase<?> aLoadout) {
            return aLoadout.getUpgrades() == source;
        }

        @Override
        public boolean affectsHeatOrDamage() {
            if (msg == ChangeMsg.HEATSINKS)
                return true;
            return false; // Changes to the items that are a side effect of change to upgrades can affect but the item
                          // messages will trigger that already.
        }
    }

    /**
     * @param aArmor
     * @param aStructure
     * @param aGuidance
     * @param aHeatSinks
     */
    public Upgrades(ArmorUpgrade aArmor, StructureUpgrade aStructure, GuidanceUpgrade aGuidance,
            HeatSinkUpgrade aHeatSinks) {
        armorType = aArmor;
        structureType = aStructure;
        guidanceType = aGuidance;
        heatSinkType = aHeatSinks;
    }

    public Upgrades(Upgrades aUpgrades) {
        this(aUpgrades.armorType, aUpgrades.structureType, aUpgrades.guidanceType, aUpgrades.heatSinkType);
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (!(obj instanceof Upgrades))
            return false;
        Upgrades that = (Upgrades) obj;
        if (this.guidanceType != that.guidanceType)
            return false;
        if (this.heatSinkType != that.heatSinkType)
            return false;
        if (this.structureType != that.structureType)
            return false;
        if (this.armorType != that.armorType)
            return false;
        return true;
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

    public ArmorUpgrade getArmor() {
        return armorType;
    }

    /**
     * Changes the guidance type.
     * <p>
     * This is package visibility as it is only intended to be modified by the Op* classes.
     * 
     * @param aGuidanceUpgrade
     *            The new {@link GuidanceUpgrade}.
     */
    public void setGuidance(GuidanceUpgrade aGuidanceUpgrade) {
        guidanceType = aGuidanceUpgrade;
    }

    /**
     * Assigns the upgrades of that to this.
     * 
     * @param aUpgrades
     *            The upgrades to copy.
     */
    public void assign(Upgrades aUpgrades) {
        armorType = aUpgrades.armorType;
        structureType = aUpgrades.structureType;
        guidanceType = aUpgrades.guidanceType;
        heatSinkType = aUpgrades.heatSinkType;
    }

}
