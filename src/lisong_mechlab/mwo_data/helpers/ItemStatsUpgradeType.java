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
package lisong_mechlab.mwo_data.helpers;

import com.thoughtworks.xstream.annotations.XStreamAsAttribute;

public class ItemStatsUpgradeType extends ItemStatsModule {
	public static class ArmorTypeStatsType {
		@XStreamAsAttribute
		public double armorPerTon;
		@XStreamAsAttribute
		public int containerId;
	}

	public static class SlotUsageType {
		@XStreamAsAttribute
		public int slots;
		@XStreamAsAttribute
		public int fixedSlotItem;
	}

	public static class StructureTypeStatsType {
		@XStreamAsAttribute
		public double weightPerTon;
	}

	public static class HeatSinkTypeStatsType {
		@XStreamAsAttribute
		public int compatibleHeatSink;
	}

	public static class ArtemisTypeStatsType {
		@XStreamAsAttribute
		public int extraSlots;
		@XStreamAsAttribute
		public double extraTons;
	}

	public ArmorTypeStatsType ArmorTypeStats;
	public StructureTypeStatsType StructureTypeStats;
	public HeatSinkTypeStatsType HeatSinkTypeStats;
	public ArtemisTypeStatsType ArtemisTypeStats;
	public SlotUsageType SlotUsage;

}
