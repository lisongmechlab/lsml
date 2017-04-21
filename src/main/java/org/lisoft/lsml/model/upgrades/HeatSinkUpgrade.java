/*
 * @formatter:off
 * Li Song Mechlab - A 'mech building tool for PGI's MechWarrior: Online.
 * Copyright (C) 2013  Emily Björk
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

import org.lisoft.lsml.model.chassi.Location;
import org.lisoft.lsml.model.database.UpgradeDB;
import org.lisoft.lsml.model.item.Engine;
import org.lisoft.lsml.model.item.Faction;
import org.lisoft.lsml.model.item.HeatSink;
import org.lisoft.lsml.model.loadout.Loadout;

import com.thoughtworks.xstream.annotations.XStreamAsAttribute;

public class HeatSinkUpgrade extends Upgrade {
    @XStreamAsAttribute
    private final HeatSink heatSinkType;

    public HeatSinkUpgrade(String aUiName, String aUiDesc, String aMwoName, int aMwoId, Faction aFaction,
            HeatSink aHeatSink) {
        super(aUiName, aUiDesc, aMwoName, aMwoId, aFaction);
        heatSinkType = aHeatSink;
    }

    /**
     * Calculates how many extra slots this heat sink type would require on the given loadout as compared to the
     * standard heat sink type.
     *
     * @param aLoadout
     *            The loadout to calculate for.
     * @return A number of slots needed.
     */
    public int getExtraSlots(Loadout aLoadout) {
        final Faction faction = aLoadout.getChassis().getFaction();
        final Engine engine = aLoadout.getEngine();
        final int externalEngineHS = aLoadout.getComponent(Location.CenterTorso).getEngineHeatSinks();
        final int internalEngineHS = engine != null ? engine.getNumInternalHeatsinks() : 0;
        final int hs = aLoadout.getHeatsinksCount() - externalEngineHS - internalEngineHS;
        final int stdHSSlots = UpgradeDB.getHeatSinks(faction, false).getHeatSinkType().getSlots();
        final int thisHSSlots = getHeatSinkType().getSlots();
        return (thisHSSlots - stdHSSlots) * hs;
    }

    /**
     * @return The type of {@link HeatSink}s associated with this upgrade.
     */
    public HeatSink getHeatSinkType() {
        return heatSinkType;
    }

    @Override
    public UpgradeType getType() {
        return UpgradeType.HEATSINK;
    }

    /**
     * @return <code>true</code> if this heat sink is a double type.
     */
    public boolean isDouble() {
        return getHeatSinkType().getSlots() > 1;
    }
}
