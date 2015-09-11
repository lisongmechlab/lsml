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

import lisong_mechlab.model.item.Item;
import lisong_mechlab.model.item.MASC;

/**
 * A helper class for parsing MASC data from the MWO data files.
 * 
 * @author Li Song
 *
 */
public class ItemStatsMascStats {

    @XStreamAsAttribute
    public double BoostSpeed;

    @XStreamAsAttribute
    public double BoostAccel;

    @XStreamAsAttribute
    public double BoostDecel;
    @XStreamAsAttribute
    public double BoostTurn;
    @XStreamAsAttribute
    public double GaugeFill;
    @XStreamAsAttribute
    public double GaugeDrain;
    @XStreamAsAttribute
    public double GaugeDamagePoint;

    /**
     * Creates a new MASC item using this object.
     * 
     * @param aStats
     *            The stats object to generate the item from.
     * @return a {@link MASC}.
     */
    public Item asMasc(ItemStatsModule aStats) {
        return new MASC(aStats.getUiName(), aStats.getUiDesc(), aStats.getMwoKey(), aStats.getMwoId(),
                aStats.ModuleStats.slots, aStats.ModuleStats.tons, aStats.ModuleStats.health, aStats.getFaction(),
                aStats.ModuleStats.TonsMin, aStats.ModuleStats.TonsMax, BoostSpeed, BoostAccel, BoostDecel, BoostTurn);
    }

}
