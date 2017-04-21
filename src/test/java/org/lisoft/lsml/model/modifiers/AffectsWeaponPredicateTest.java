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
package org.lisoft.lsml.model.modifiers;

import static org.junit.Assert.assertEquals;

import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

import org.junit.Test;
import org.lisoft.lsml.model.chassi.Chassis;
import org.lisoft.lsml.model.database.ChassisDB;
import org.lisoft.lsml.model.database.ModifiersDB;
import org.lisoft.lsml.model.loadout.DefaultLoadoutFactory;
import org.lisoft.lsml.model.loadout.Loadout;
import org.lisoft.lsml.model.loadout.LoadoutFactory;

/**
 * Test suite for {@link AffectsWeaponPredicate}.
 *
 * @author Emily Björk
 */
public class AffectsWeaponPredicateTest {
    private final LoadoutFactory loadoutFactory = new DefaultLoadoutFactory();

    @Test
    public void testTest() { // Durrr...
        final Collection<String> selectors = ModifiersDB.getAllWeaponSelectors();

        final AffectsWeaponPredicate cut = new AffectsWeaponPredicate();

        for (final Chassis chassis : ChassisDB.lookupAll()) {
            final Loadout loadout = loadoutFactory.produceEmpty(chassis);
            final Collection<Modifier> modifiers = loadout.getModifiers();

            final List<Modifier> expectedModifiers = modifiers.stream().filter(aModifier -> {
                for (final String selector : selectors) {
                    if (aModifier.getDescription().getSelectors().contains(selector)) {
                        return true;
                    }
                }
                return false;
            }).collect(Collectors.toList());

            final List<Modifier> actualModifiers = modifiers.stream().filter(cut).collect(Collectors.toList());

            assertEquals(expectedModifiers, actualModifiers);
        }

    }

}
