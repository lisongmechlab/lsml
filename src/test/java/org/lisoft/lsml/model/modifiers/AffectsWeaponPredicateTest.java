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
package org.lisoft.lsml.model.modifiers;

import static org.junit.Assert.assertEquals;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.lisoft.lsml.model.chassi.Chassis;
import org.lisoft.lsml.model.chassi.ChassisClass;
import org.lisoft.lsml.model.database.ChassisDB;
import org.lisoft.lsml.model.database.ModifiersDB;
import org.lisoft.lsml.model.loadout.DefaultLoadoutFactory;
import org.lisoft.lsml.model.loadout.Loadout;
import org.lisoft.lsml.model.loadout.LoadoutFactory;

import junitparams.JUnitParamsRunner;
import junitparams.Parameters;

/**
 * Test suite for {@link AffectsWeaponPredicate}.
 *
 * @author Li Song
 */
@RunWith(JUnitParamsRunner.class)
public class AffectsWeaponPredicateTest {
    private final LoadoutFactory loadoutFactory = new DefaultLoadoutFactory();
    private final Set<String> universal_weapon_specifiers = new HashSet<>();

    public AffectsWeaponPredicateTest() {
        universal_weapon_specifiers.add(ModifierDescription.SPEC_WEAPON_COOL_DOWN);
        universal_weapon_specifiers.add(ModifierDescription.SPEC_WEAPON_RANGE);
        universal_weapon_specifiers.add(ModifierDescription.SPEC_WEAPON_PROJECTILE_SPEED);
        universal_weapon_specifiers.add(ModifierDescription.SPEC_WEAPON_HEAT);
    }

    public Object[] allChassis() {
        final List<Chassis> chassii = new ArrayList<>();
        chassii.addAll(ChassisDB.lookup(ChassisClass.LIGHT));
        chassii.addAll(ChassisDB.lookup(ChassisClass.MEDIUM));
        chassii.addAll(ChassisDB.lookup(ChassisClass.HEAVY));
        chassii.addAll(ChassisDB.lookup(ChassisClass.ASSAULT));
        return chassii.toArray();
    }

    @Test
    @Parameters(method = "allChassis")
    public void testAllModifiersAffectWeapons(Chassis aChassis) {
        final Collection<String> allWeaponSelectors = ModifiersDB.getAllWeaponSelectors();
        final Loadout loadout = loadoutFactory.produceEmpty(aChassis);
        final Collection<Modifier> modifiers = loadout.getAllModifiers();
        final List<Modifier> expectedModifiers = modifiers.stream()
                .filter(aModifier -> shouldAffectAWeapon(allWeaponSelectors, aModifier)).collect(Collectors.toList());

        final AffectsWeaponPredicate cut = new AffectsWeaponPredicate();
        final List<Modifier> actualModifiers = modifiers.stream().filter(cut).collect(Collectors.toList());

        assertEquals(expectedModifiers.toString(), actualModifiers.toString());
    }

    private boolean shouldAffectAWeapon(final Collection<String> allWeaponSelectors, Modifier aModifier) {
        for (final String weaponSelector : allWeaponSelectors) {
            final ModifierDescription description = aModifier.getDescription();
            if (description.getSelectors().contains(weaponSelector)) {
                // Selects any weapon specifically
                return true;
            }
            else if (description.getSelectors().containsAll(ModifierDescription.SEL_ALL) &&
                    universal_weapon_specifiers.contains(description.getSpecifier())) {
                // Selects everything but specifies an attribute affecting weapons
                return true;
            }
        }
        return false;
    }

}
