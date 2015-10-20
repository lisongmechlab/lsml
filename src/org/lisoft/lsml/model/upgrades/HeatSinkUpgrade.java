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

import org.lisoft.lsml.model.DataCache;
import org.lisoft.lsml.model.datacache.gamedata.helpers.ItemStatsUpgradeType;
import org.lisoft.lsml.model.item.Faction;
import org.lisoft.lsml.model.item.HeatSink;

import com.thoughtworks.xstream.annotations.XStreamAsAttribute;

public class HeatSinkUpgrade extends Upgrade {
    @XStreamAsAttribute
    private final HeatSink heatSinkType;

    public HeatSinkUpgrade(String aName, String aDescription, int aMwoId, Faction aFaction, HeatSink aHeatSink) {
        super(aName, aDescription, aMwoId, aFaction);
        heatSinkType = aHeatSink;
    }

    public HeatSinkUpgrade(ItemStatsUpgradeType aUpgradeType, DataCache aDataCache) {
        super(aUpgradeType);
        heatSinkType = (HeatSink) DataCache.findItem(aUpgradeType.HeatSinkTypeStats.compatibleHeatSink,
                aDataCache.getItems());
    }

    /**
     * @return The type of {@link HeatSink}s associated with this upgrade.
     */
    public HeatSink getHeatSinkType() {
        return heatSinkType;
    }

    /**
     * @return <code>true</code> if this heat sink is a double type.
     */
    public boolean isDouble() {
        return getHeatSinkType().getNumCriticalSlots() > 1;
    }

    @Override
    public UpgradeType getType() {
        return UpgradeType.HEATSINK;
    }
}
