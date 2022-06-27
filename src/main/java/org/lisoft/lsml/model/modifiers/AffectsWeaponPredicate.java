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

import org.lisoft.lsml.model.database.ItemDB;
import org.lisoft.lsml.model.item.Weapon;

import javax.inject.Inject;
import java.util.*;
import java.util.function.Predicate;
import java.util.stream.Collectors;

/**
 * This {@link Predicate} will return <code>true</code> if the tested {@link Modifier} will affect the performance of
 * any weapon.
 *
 * @author Li Song
 */
public class AffectsWeaponPredicate implements Predicate<Modifier> {
    private final static List<String> HEAT_SELECTORS;
    private final static Set<String> WEAPON_SELECTORS;
    private final static Set<String> WEAPON_SPECIFIERS;

    static {
        WEAPON_SPECIFIERS = new HashSet<>();
        WEAPON_SPECIFIERS.add(ModifierDescription.SPEC_WEAPON_COOL_DOWN);
        WEAPON_SPECIFIERS.add(ModifierDescription.SPEC_WEAPON_ROF);
        WEAPON_SPECIFIERS.add(ModifierDescription.SPEC_WEAPON_HEAT);
        WEAPON_SPECIFIERS.add(ModifierDescription.SPEC_WEAPON_PROJECTILE_SPEED);
        WEAPON_SPECIFIERS.add(ModifierDescription.SPEC_WEAPON_JAM_DURATION);
        WEAPON_SPECIFIERS.add(ModifierDescription.SPEC_WEAPON_JAM_PROBABILITY);
        WEAPON_SPECIFIERS.add(ModifierDescription.SPEC_WEAPON_JAM_RAMP_DOWN_TIME);
        WEAPON_SPECIFIERS.add(ModifierDescription.SPEC_WEAPON_LARGE_BORE);
        WEAPON_SPECIFIERS.add(ModifierDescription.SPEC_WEAPON_RANGE);
        WEAPON_SPECIFIERS.add(ModifierDescription.SPEC_WEAPON_SPREAD);
        WEAPON_SPECIFIERS.add(ModifierDescription.SPEC_WEAPON_TAG_DURATION);
        WEAPON_SPECIFIERS.add(ModifierDescription.SPEC_WEAPON_DAMAGE);
        WEAPON_SPECIFIERS.add(ModifierDescription.SPEC_WEAPON_DURATION);
        WEAPON_SPECIFIERS.add(ModifierDescription.SPEC_WEAPON_NARC_DURATION);
        WEAPON_SPECIFIERS.add(ModifierDescription.SPEC_WEAPON_MAX_FREE_ALPHA);

        HEAT_SELECTORS = new ArrayList<>();
        HEAT_SELECTORS.addAll(ModifierDescription.SEL_HEAT_DISSIPATION);
        HEAT_SELECTORS.addAll(ModifierDescription.SEL_HEAT_LIMIT);
        HEAT_SELECTORS.addAll(ModifierDescription.SEL_HEAT_EXTERNALTRANSFER);

        WEAPON_SELECTORS = ItemDB.lookup(Weapon.class).stream().flatMap(weapon -> weapon.getAliases().stream())
                                 .collect(Collectors.toSet());
    }

    @Inject
    public AffectsWeaponPredicate() {
        // NOP
    }

    @Override
    public boolean test(Modifier aModifier) {
        final ModifierDescription description = aModifier.getDescription();
        final String specifier = description.getSpecifier();
        final Collection<String> selectors = description.getSelectors();

        if (!Collections.disjoint(selectors, ModifierDescription.SEL_ALL) &&
            (specifier != null && WEAPON_SPECIFIERS.contains(specifier))) {
            return true; // Selects everything and affects a specifier of weapons
        } else if (!Collections.disjoint(WEAPON_SELECTORS, selectors)) {
            return true; // Selects a weapon
        } else {
            return !Collections.disjoint(HEAT_SELECTORS, selectors); // Selects heat dissipation of entire mech
        }
    }
}
