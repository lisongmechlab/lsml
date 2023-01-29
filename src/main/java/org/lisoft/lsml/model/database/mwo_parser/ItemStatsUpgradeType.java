/*
 * Li Song Mechlab - A 'mech building tool for PGI's MechWarrior: Online.
 * Copyright (C) 2013-2023  Li Song
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
package org.lisoft.lsml.model.database.mwo_parser;

import com.thoughtworks.xstream.annotations.XStreamAsAttribute;
import java.util.Map;
import java.util.Optional;
import org.lisoft.lsml.model.chassi.Location;
import org.lisoft.lsml.model.item.Faction;
import org.lisoft.lsml.model.item.HeatSink;
import org.lisoft.lsml.model.item.Internal;
import org.lisoft.lsml.model.upgrades.*;

class ItemStatsUpgradeType extends ItemStatsModule {
  private static class ArmorTypeStatsType {
    @XStreamAsAttribute public double armorPerTon;
  }

  private static class ArtemisTypeStatsType {
    @XStreamAsAttribute public int extraSlots;
    @XStreamAsAttribute public double extraTons;
    @XStreamAsAttribute public double missileSpread;
  }

  private static class HeatSinkTypeStatsType {
    @XStreamAsAttribute public int compatibleHeatSink;
  }

  private static class SlotUsageType {
    @XStreamAsAttribute public int componentsWithFixedSlots;
    @XStreamAsAttribute public int fixedSlotItem;
    @XStreamAsAttribute public int fixedSlotsPerComponent;
    @XStreamAsAttribute public int slots;
  }

  private static class StructureTypeStatsType {
    @XStreamAsAttribute public double weightPerTon;
  }

  @XmlElement private ArmorTypeStatsType ArmorTypeStats;
  @XmlElement private ArtemisTypeStatsType ArtemisTypeStats;
  @XmlElement private HeatSinkTypeStatsType HeatSinkTypeStats;
  @XmlElement private SlotUsageType SlotUsage;
  @XmlElement private StructureTypeStatsType StructureTypeStats;

  Upgrade asUpgrade(Map<Integer, Object> aId2ObjMap) {
    final UpgradeType type = UpgradeType.fromMwo(CType);
    final String mwoName = getMwoKey();
    final String name = getUiName();
    final String desc = getUiDescription();
    final Faction faction = getFaction();
    final int mwoId = getMwoId();

    switch (type) {
      case ARMOUR -> {
        final int slots = SlotUsage == null ? 0 : SlotUsage.slots;
        final double armourPerTon = ArmorTypeStats.armorPerTon;

        return new ArmourUpgrade(
            name.replace("ARMOR", "ARMOUR"),
            desc,
            mwoName,
            mwoId,
            faction,
            slots,
            armourPerTon,
            getFixedSlotsPerComponent().orElse(null),
            getFixedSlotItem(aId2ObjMap).orElse(null));
      }
      case ARTEMIS -> {
        final int slots = ArtemisTypeStats.extraSlots;
        final double tons = ArtemisTypeStats.extraTons;
        final double spread = ArtemisTypeStats.missileSpread;
        return new GuidanceUpgrade(name, desc, mwoName, mwoId, faction, slots, tons, spread);
      }
      case HEATSINK -> {
        final HeatSink heatSink = (HeatSink) aId2ObjMap.get(HeatSinkTypeStats.compatibleHeatSink);
        return new HeatSinkUpgrade(name, desc, mwoName, mwoId, faction, heatSink);
      }
      case STRUCTURE -> {
        final int slots = SlotUsage == null ? 0 : SlotUsage.slots;
        final double structurePct = StructureTypeStats.weightPerTon;
        return new StructureUpgrade(name, desc, mwoName, mwoId, faction, slots, structurePct);
      }
      default -> throw new IllegalArgumentException("Unknown upgrade type: " + type);
    }
  }

  private Optional<Internal> getFixedSlotItem(Map<Integer, Object> aItems) {
    if (SlotUsage != null && SlotUsage.fixedSlotItem > 0) {
      return Optional.of((Internal) aItems.get(SlotUsage.fixedSlotItem));
    }
    return Optional.empty();
  }

  private Optional<int[]> getFixedSlotsPerComponent() {
    if (SlotUsage != null && SlotUsage.fixedSlotsPerComponent > 0) {
      // SlotsUsage.componentsWithFixedSlots is an 8 bit bitmap where a set bit indicates
      // that a component should have SlotUsage.fixedSlotsPerComponent fixed armour slots.
      // The exact mapping from bits to components is unknown at this stage, but we do know
      // that bits 7 and 0 map to either of Head and CT and thus 1-6 map to the remaining
      // components.

      // We assume an order to get somewhere
      final Location[] order =
          new Location[] {
            Location.Head, // LSB
            Location.LeftArm,
            Location.LeftTorso,
            Location.LeftLeg,
            Location.RightLeg,
            Location.RightTorso,
            Location.RightArm,
            Location.CenterTorso // MSB
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
