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
package org.lisoft.lsml.model.database.gamedata.helpers;

import com.thoughtworks.xstream.annotations.XStreamAsAttribute;
import org.lisoft.lsml.model.chassi.Location;
import org.lisoft.lsml.model.item.Internal;

import java.util.Map;
import java.util.Optional;

public class ItemStatsUpgradeType extends ItemStatsModule {
    public static class ArmorTypeStatsType {
        @XStreamAsAttribute
        public double armorPerTon;
        @XStreamAsAttribute
        public int containerId;
    }

    public static class ArtemisTypeStatsType {
        @XStreamAsAttribute
        public int extraSlots;
        @XStreamAsAttribute
        public double extraTons;
        @XStreamAsAttribute
        public double missileSpread;
    }

    public static class HeatSinkTypeStatsType {
        @XStreamAsAttribute
        public int compatibleHeatSink;
    }

    public static class SlotUsageType {
        @XStreamAsAttribute
        public int componentsWithFixedSlots;
        @XStreamAsAttribute
        public int fixedSlotItem;
        @XStreamAsAttribute
        public int fixedSlotsPerComponent;
        @XStreamAsAttribute
        public int slots;
    }

    public static class StructureTypeStatsType {
        @XStreamAsAttribute
        public double weightPerTon;
    }
    public ArmorTypeStatsType ArmorTypeStats;
    public ArtemisTypeStatsType ArtemisTypeStats;
    public HeatSinkTypeStatsType HeatSinkTypeStats;
    public SlotUsageType SlotUsage;
    public StructureTypeStatsType StructureTypeStats;

    public Optional<Internal> getFixedSlotItem(Map<Integer, Object> aItems) {
        if (SlotUsage != null && SlotUsage.fixedSlotItem > 0) {
            return Optional.of((Internal) aItems.get(SlotUsage.fixedSlotItem));
        }
        return Optional.empty();
    }

    public Optional<int[]> getFixedSlotsPerComponent() {
        if (SlotUsage != null && SlotUsage.fixedSlotsPerComponent > 0) {
            // SlotsUsage.componentsWithFixedSlots is a 8 bit bitmap where a set bit indicates
            // that a component should have SlotUsage.fixedSlotsPerComponent fixed armour slots.
            // The exact mapping from bits to components is unknown at this stage but we do know
            // that bits 7 and 0 map to either of Head and CT and thus 1-6 map to the remaining
            // components.

            // We assume a order to get somewhere
            final Location[] order = new Location[]{Location.Head, // LSB
                    Location.LeftArm, Location.LeftTorso, Location.LeftLeg, Location.RightLeg, Location.RightTorso,
                    Location.RightArm, Location.CenterTorso // MSB
            };
            final int[] slotsPerComponent = new int[8];
            int slotBitMap = SlotUsage.componentsWithFixedSlots;
            for (int i = 0; i < 8; ++i) {
                if ((slotBitMap & 0x1) != 0) {
                    slotsPerComponent[order[i].ordinal()] = SlotUsage.fixedSlotsPerComponent;
                }
                slotBitMap >>= 1;
            }
            return Optional.of(slotsPerComponent);
        }
        return Optional.empty();
    }

}
