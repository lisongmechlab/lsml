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

import java.util.ArrayList;
import java.util.List;

import org.lisoft.lsml.model.chassi.HardPointType;
import org.lisoft.lsml.model.database.gamedata.QuirkModifiers;
import org.lisoft.lsml.model.item.TargetingComputer;
import org.lisoft.lsml.model.modifiers.Modifier;

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
            public double speed;
            @XStreamAsAttribute
            public String critChanceIncrease;
        }

        @XStreamImplicit
        public List<XMLWeaponStats> WeaponStats;

        @XStreamAlias("Range")
        public ItemStatsWeapon.Range range;

        @XStreamAsAttribute
        public String compatibleWeapons;
    }

    @XStreamImplicit
    public List<XMLWeaponStatsFilter> WeaponStatsFilter;

    public TargetingComputer asTargetingComputer(ItemStatsModule aStats) {
        final String name = aStats.getUiName();

        final List<Modifier> modifiers = new ArrayList<>();
        if (null != WeaponStatsFilter) {
            for (final XMLTargetingComputerStats.XMLWeaponStatsFilter filter : WeaponStatsFilter) {
                for (final XMLTargetingComputerStats.XMLWeaponStatsFilter.XMLWeaponStats stats : filter.WeaponStats) {
                    final double range = filter.range != null ? filter.range.multiplier : 0.0;
                    modifiers.addAll(QuirkModifiers.createModifiers(name, stats.operation, filter.compatibleWeapons, 0,
                            range, stats.speed, 0, 0));
                }
            }
        }

        return new TargetingComputer(name, aStats.getUiDescription(), aStats.getMwoKey(), aStats.getMwoId(),
                aStats.ModuleStats.slots, aStats.ModuleStats.tons, HardPointType.NONE, aStats.ModuleStats.health,
                aStats.getFaction(), aStats.ModuleStats.getLocations(), aStats.ModuleStats.getMechClasses(),
                aStats.ModuleStats.amountAllowed, modifiers);
    }
}
