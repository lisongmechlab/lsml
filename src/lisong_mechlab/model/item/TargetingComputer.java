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
package lisong_mechlab.model.item;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

import lisong_mechlab.model.quirks.Modifier;
import lisong_mechlab.model.quirks.ModifierDescription;
import lisong_mechlab.model.quirks.ModifiersDB;
import lisong_mechlab.model.quirks.ModifierDescription.Operation;
import lisong_mechlab.model.quirks.ModifierDescription.ValueType;
import lisong_mechlab.mwo_data.helpers.ItemStatsModule;
import lisong_mechlab.mwo_data.helpers.XMLTargetingComputerStats;

/**
 * Models a targeting computer or command console.
 * <p>
 * XXX: Only takes range modifiers into account as of yet. We don't display crit and modified projectile speeds yet.
 * 
 * @author Emily Björk
 */
public class TargetingComputer extends Module implements ModifierEquipment {
    private List<Modifier> modifiers = new ArrayList<>();

    public TargetingComputer(ItemStatsModule aModule) {
        super(aModule);

        if (null != aModule.TargetingComputerStats.WeaponStatsFilter) {
            for (XMLTargetingComputerStats.XMLWeaponStatsFilter filter : aModule.TargetingComputerStats.WeaponStatsFilter) {
                List<String> selectors = Arrays.asList(filter.compatibleWeapons.split("\\s*,\\s*"));

                for (XMLTargetingComputerStats.XMLWeaponStatsFilter.XMLWeaponStats stats : filter.WeaponStats) {
                    if (stats.longRange != 0.0 || stats.maxRange != 0.0) {

                        // FIXME add the selectors to the modifier description somehow.
                        Operation op = Operation.fromString(stats.operation);
                        ModifierDescription longRangeDesc = new ModifierDescription(getName() + " (LONG RANGE)", null,
                                op, selectors, ModifiersDB.SEL_WEAPON_LONGRANGE, ValueType.POSITIVE_GOOD);
                        ModifierDescription maxRangeDesc = new ModifierDescription(getName() + " (MAX RANGE)", null, op,
                                selectors, ModifiersDB.SEL_WEAPON_MAXRANGE, ValueType.POSITIVE_GOOD);

                        Modifier longRange = new Modifier(longRangeDesc, stats.longRange - 1);
                        Modifier maxRange = new Modifier(maxRangeDesc, stats.maxRange - 1);

                        modifiers.add(longRange);
                        modifiers.add(maxRange);
                    }
                }
            }
        }
    }

    @Override
    public Collection<Modifier> getModifiers() {
        return modifiers;
    }
}
