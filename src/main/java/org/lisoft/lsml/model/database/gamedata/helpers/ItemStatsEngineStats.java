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

import org.lisoft.lsml.model.item.Engine;
import org.lisoft.lsml.model.item.EngineType;
import org.lisoft.lsml.model.item.Faction;
import org.lisoft.lsml.model.modifiers.Attribute;
import org.lisoft.lsml.model.modifiers.ModifierDescription;

import com.thoughtworks.xstream.annotations.XStreamAsAttribute;

public class ItemStatsEngineStats extends ItemStatsModuleStats {
    static Attribute ENGINE_HEAT = new Attribute(Engine.ENGINE_HEAT_FULL_THROTTLE,
            ModifierDescription.SEL_HEAT_MOVEMENT, null);
    @XStreamAsAttribute
    public int rating;
    @XStreamAsAttribute
    public int type;
    @XStreamAsAttribute
    public int heatsinks;
    @XStreamAsAttribute
    public int sideSlots;

    @XStreamAsAttribute
    public double movementHeatMultiplier;

    public Engine asEngine(ItemStats aStats) {
        final String uiName = aStats.getUiName();
        final String uiDesc = aStats.getUiDescription();
        final String mwoName = aStats.getMwoKey();
        final int mwoId = aStats.getMwoId();
        final Faction itemFaction = aStats.getFaction();

        final int hs = heatsinks;
        final int internalHs = Math.min(10, hs);
        final int heatSinkSlots = hs - internalHs;

        String lcName = aStats.name.toLowerCase();
        final EngineType engineType;
        if (lcName.contains("xl")) {
            engineType = EngineType.XL;
        }
        else if (lcName.contains("light")) {
            engineType = EngineType.LE;
        }
        else if (lcName.contains("std")) {
            engineType = EngineType.STD;
        }
        else {
            throw new IllegalArgumentException("Unknown engine type: " + uiName);
        }

        return new Engine(uiName, uiDesc, mwoName, mwoId, slots, tons, health, itemFaction, ENGINE_HEAT, rating,
                engineType, internalHs, heatSinkSlots, sideSlots, movementHeatMultiplier);
    }
}
