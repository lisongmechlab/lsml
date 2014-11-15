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
package lisong_mechlab.model.item;

import lisong_mechlab.model.chassi.HardPointType;
import lisong_mechlab.model.loadout.component.ConfiguredComponentBase;
import lisong_mechlab.model.modifiers.Attribute;

import com.thoughtworks.xstream.annotations.XStreamAsAttribute;

/**
 * This immutable class represents an engine for a battle mech.
 * 
 * @author Li Song
 *
 */
public class Engine extends HeatSource {
    public final static double ENGINE_HEAT_FULL_THROTTLE = 0.2;
    public final static double ENGINE_HEAT_66_THROTTLE   = 0.1;

    @XStreamAsAttribute
    protected final int        rating;
    @XStreamAsAttribute
    protected final EngineType type;
    @XStreamAsAttribute
    final private int          internalHs;
    @XStreamAsAttribute
    final private int          heatSinkSlots;

    public Engine(String aName, String aDesc, String aMwoName, int aMwoId, int aSlots, double aTons, int aHP,
            Faction aFaction, Attribute aHeat, int aRating, EngineType aType, int aInternalHS, int aHSSlots) {
        super(aName, aDesc, aMwoName, aMwoId, aSlots, aTons, HardPointType.NONE, aHP, aFaction, aHeat);
        rating = aRating;
        type = aType;
        internalHs = aInternalHS;
        heatSinkSlots = aHSSlots;
    }
    
    public EngineType getType() {
        return type;
    }

    public int getRating() {
        return rating;
    }

    public int getNumInternalHeatsinks() {
        return internalHs;
    }

    public int getNumHeatsinkSlots() {
        return heatSinkSlots;
    }

    @Override
    public String getShortName() {
        String name = getName();
        name = name.replace("ENGINE ", "");
        return name;
    }

    /**
     * @return The side part of this engine if it is an XL engine, <code>null</code> otherwise.
     */
    public Internal getSide() {
        if (getType() == EngineType.XL)
            return getFaction() == Faction.Clan ? ConfiguredComponentBase.ENGINE_INTERNAL_CLAN
                    : ConfiguredComponentBase.ENGINE_INTERNAL;
        return null;
    }
}
