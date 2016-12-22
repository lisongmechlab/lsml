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
package org.lisoft.lsml.model.item;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

import org.lisoft.lsml.model.chassi.HardPointType;
import org.lisoft.lsml.model.chassi.Location;
import org.lisoft.lsml.model.datacache.ModifiersDB;
import org.lisoft.lsml.model.loadout.component.ConfiguredComponent;
import org.lisoft.lsml.model.modifiers.Attribute;
import org.lisoft.lsml.model.modifiers.Modifier;

import com.thoughtworks.xstream.annotations.XStreamAsAttribute;

/**
 * This immutable class represents an engine for a battle mech.
 *
 * @author Li Song
 */
public class Engine extends HeatSource implements ModifierEquipment {
    // Values from: http://mwomercs.com/forums/topic/100089-breakdown/
    public final static double ENGINE_HEAT_FULL_THROTTLE = 0.2;
    public final static double ENGINE_HEAT_66_THROTTLE = 0.1;

    @XStreamAsAttribute
    final private EngineType type;
    @XStreamAsAttribute
    final private int rating;
    @XStreamAsAttribute
    final private int internalHs;
    @XStreamAsAttribute
    final private int heatSinkSlots;
    @XStreamAsAttribute
    final private double movementHeatMultiplier;

    transient private List<Modifier> modifiers = null;

    public Engine(String aName, String aDesc, String aMwoName, int aMwoId, int aSlots, double aTons, double aHP,
            Faction aFaction, Attribute aHeat, int aRating, EngineType aType, int aInternalHS, int aHSSlots,
            double aMovementHeatMultiplier) {
        super(aName, aDesc, aMwoName, aMwoId, aSlots, aTons, HardPointType.NONE, aHP, aFaction,
                Arrays.asList(Location.CenterTorso), null, aHeat);
        rating = aRating;
        type = aType;
        internalHs = aInternalHS;
        heatSinkSlots = aHSSlots;
        movementHeatMultiplier = aMovementHeatMultiplier;
    }

    @Override
    public Collection<Modifier> getModifiers() {
        if (null == modifiers) {
            modifiers = new ArrayList<>();
            if (movementHeatMultiplier != 0.0) {
                modifiers.add(new Modifier(ModifiersDB.HEAT_MOVEMENT_DESC, movementHeatMultiplier));
            }
        }
        return modifiers;
    }

    /**
     * @return The number of slots for external heat sinks that this {@link Engine} has.
     */
    public int getNumHeatsinkSlots() {
        return heatSinkSlots;
    }

    /**
     * @return The number of fixed internal heat sinks that this {@link Engine} has.
     */
    public int getNumInternalHeatsinks() {
        return internalHs;
    }

    /**
     * @return The speed rating of this {@link Engine}.
     */
    public int getRating() {
        return rating;
    }

    @Override
    public String getShortName() {
        String name = super.getShortName();
        name = name.replace("ENGINE ", "");
        return name;
    }

    /**
     * @return The side part of this engine if it is an XL engine, <code>null</code> otherwise.
     */
    public Internal getSide() {
        if (getType() == EngineType.XL) {
            return getFaction() == Faction.CLAN ? ConfiguredComponent.ENGINE_INTERNAL_CLAN
                    : ConfiguredComponent.ENGINE_INTERNAL;
        }
        return null;
    }

    /**
     * @return The type of the engine (XL/STD).
     */
    public EngineType getType() {
        return type;
    }
}
