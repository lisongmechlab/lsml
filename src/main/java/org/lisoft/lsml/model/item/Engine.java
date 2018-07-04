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
package org.lisoft.lsml.model.item;

import java.util.*;

import org.lisoft.lsml.model.chassi.*;
import org.lisoft.lsml.model.database.ModifiersDB;
import org.lisoft.lsml.model.modifiers.*;

import com.thoughtworks.xstream.annotations.XStreamAsAttribute;

/**
 * This immutable class represents an engine for a battle mech.
 *
 * @author Emily Björk
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
    @XStreamAsAttribute
    final private int sideSlots;

    transient private List<Modifier> modifiers = null;
    transient private Internal side = null;

    public Engine(String aName, String aDesc, String aMwoName, int aMwoId, int aSlots, double aTons, double aHP,
            Faction aFaction, Attribute aHeat, int aRating, EngineType aType, int aInternalHS, int aHSSlots,
            int aSideSlots, double aMovementHeatMultiplier) {
        super(aName, aDesc, aMwoName, aMwoId, aSlots, aTons, HardPointType.NONE, aHP, aFaction,
                Arrays.asList(Location.CenterTorso), null, aHeat);
        rating = aRating;
        type = aType;
        internalHs = aInternalHS;
        sideSlots = aSideSlots;
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

    /**
     * @return The {@link Optional} side part of this engine.
     */
    public Optional<Internal> getSide() {
        if (sideSlots > 0) {
            if (side == null) {
                final int id = (getFaction() == Faction.CLAN ? 60000 : 60010) + sideSlots;

                final String name = getFaction() == Faction.CLAN ? "C-ENGINE" : "ENGINE";
                final String key = getFaction() == Faction.CLAN ? "mdf_CEngine" : "mdf_Engine";

                side = new Internal(name, "", key, id, sideSlots, 0, HardPointType.NONE, 15, getFaction());
            }
            return Optional.of(side);
        }
        return Optional.empty();
    }

    /**
     * @return The number of intact side torsos that are required to stay alive.
     */
    public int getSidesToLive() {
        switch (type) {
            case LE:
                return 1;
            case XL:
                return getFaction() == Faction.CLAN ? 1 : 2;
            case STD:
                return 0;
            default:
                throw new IllegalStateException("Unknown engine type");
        }
    }

    /**
     * Before using this function, see if you can solve your problem better with {@link #getSidesToLive()} or
     * {@link #getSide()} instead.
     *
     * @return The type of the engine (XL/LE/STD).
     */
    public EngineType getType() {
        return type;
    }
}
