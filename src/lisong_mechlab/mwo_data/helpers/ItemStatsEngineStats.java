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

import java.util.Arrays;

import lisong_mechlab.model.item.Engine;
import lisong_mechlab.model.item.EngineType;
import lisong_mechlab.model.item.Faction;
import lisong_mechlab.model.modifiers.Attribute;

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
