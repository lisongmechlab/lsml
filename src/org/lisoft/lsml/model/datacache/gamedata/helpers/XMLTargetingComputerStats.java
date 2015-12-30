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
package org.lisoft.lsml.model.datacache.gamedata.helpers;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.lisoft.lsml.model.chassi.HardPointType;
import org.lisoft.lsml.model.item.TargetingComputer;
import org.lisoft.lsml.model.modifiers.Modifier;
import org.lisoft.lsml.model.modifiers.ModifierDescription;
import org.lisoft.lsml.model.modifiers.ModifierDescription.ModifierType;
import org.lisoft.lsml.model.modifiers.ModifierDescription.Operation;

import com.thoughtworks.xstream.annotations.XStreamAlias;
import com.thoughtworks.xstream.annotations.XStreamAsAttribute;
import com.thoughtworks.xstream.annotations.XStreamImplicit;

/**
 * Helper class for parsing targeting computer information from XML files.
 * 
 * @author Li Song
 */
public class XMLTargetingComputerStats {
    @XStreamAlias("WeaponStatsFilter")
    public static class XMLWeaponStatsFilter {
        @XStreamAlias("WeaponStats")
        public static class XMLWeaponStats {
            @XStreamAsAttribute
            public String operation;
            @XStreamAsAttribute
            public double longRange;
            @XStreamAsAttribute
            public double maxRange;
            @XStreamAsAttribute
            public double speed;
            @XStreamAsAttribute
            public String critChanceIncrease;
        }

        @XStreamImplicit
        public List<XMLWeaponStats> WeaponStats;
        @XStreamAsAttribute
        public String               compatibleWeapons;
    }

    @XStreamImplicit
    public List<XMLWeaponStatsFilter> WeaponStatsFilter;

    public TargetingComputer asTargetingComputer(ItemStatsModule aStats) {
        final String name = aStats.getUiName();

        List<Modifier> modifiers = new ArrayList<>();
        if (null != WeaponStatsFilter) {
            for (XMLTargetingComputerStats.XMLWeaponStatsFilter filter : WeaponStatsFilter) {
                List<String> selectors = Arrays.asList(filter.compatibleWeapons.split("\\s*,\\s*"));

                for (XMLTargetingComputerStats.XMLWeaponStatsFilter.XMLWeaponStats stats : filter.WeaponStats) {
                    if (stats.longRange != 0.0 || stats.maxRange != 0.0) {

                        // FIXME add the selectors to the modifier description somehow.
                        Operation op = Operation.fromString(stats.operation);
                        ModifierDescription longRangeDesc = new ModifierDescription(name + " (LONG RANGE)", null, op,
                                selectors, ModifierDescription.SPEC_WEAPON_RANGE, ModifierType.POSITIVE_GOOD);
                        ModifierDescription maxRangeDesc = new ModifierDescription(name + " (MAX RANGE)", null, op,
                                selectors, ModifierDescription.SPEC_WEAPON_RANGE, ModifierType.POSITIVE_GOOD);

                        Modifier longRange = new Modifier(longRangeDesc, stats.longRange - 1);
                        Modifier maxRange = new Modifier(maxRangeDesc, stats.maxRange - 1);

                        modifiers.add(longRange);
                        modifiers.add(maxRange);
                    }
                }
            }
        }

        return new TargetingComputer(name, aStats.getUiDesc(), aStats.getMwoKey(), aStats.getMwoId(),
                aStats.ModuleStats.slots, aStats.ModuleStats.tons, HardPointType.NONE, aStats.ModuleStats.health,
                aStats.getFaction(), aStats.ModuleStats.getLocations(), aStats.ModuleStats.getMechClasses(), modifiers);
    }
}
