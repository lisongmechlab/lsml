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
package org.lisoft.lsml.parsing.mwo_gamedata.helpers;

import java.util.Arrays;

import org.lisoft.lsml.model.item.Engine;
import org.lisoft.lsml.model.item.EngineType;
import org.lisoft.lsml.model.item.Faction;
import org.lisoft.lsml.model.modifiers.Attribute;

import com.thoughtworks.xstream.annotations.XStreamAsAttribute;

public class ItemStatsEngineStats extends ItemStatsModuleStats {
    @XStreamAsAttribute
    public int       rating;
    @XStreamAsAttribute
    public int       type;
    @XStreamAsAttribute
    public int       heatsinks;

    static Attribute ENGINE_HEAT = new Attribute(Engine.ENGINE_HEAT_FULL_THROTTLE, Arrays.asList("engineheat"), null);

    public Engine asEngine(ItemStats aStats) {
        String uiName = aStats.getUiName();
        String uiDesc = aStats.getUiDesc();
        String mwoName = aStats.getMwoKey();
        int mwoId = aStats.getMwoId();
        Faction itemFaction = aStats.getFaction();

        int hs = heatsinks;
        int internalHs = Math.min(10, hs);
        int heatSinkSlots = hs - internalHs;
        EngineType engineType = (uiName.toLowerCase().contains("xl")) ? (EngineType.XL) : (EngineType.STD);
        return new Engine(uiName, uiDesc, mwoName, mwoId, slots, tons, health, itemFaction, ENGINE_HEAT, rating,
                engineType, internalHs, heatSinkSlots);
    }
}
