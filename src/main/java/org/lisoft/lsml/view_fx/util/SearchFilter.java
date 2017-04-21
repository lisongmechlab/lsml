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
package org.lisoft.lsml.view_fx.util;

import java.util.List;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import org.lisoft.lsml.model.loadout.Loadout;

/**
 * This class implements a predicate that filters {@link Loadout}s based on text key words.
 *
 * @author Li Song
 */
public class SearchFilter implements Predicate<Loadout> {
    private final String searchString;

    public SearchFilter(String aFilterString) {
        searchString = aFilterString.toLowerCase().trim();
    }

    @Override
    public boolean test(Loadout aT) {
        final List<String> modifiers = aT.getModifiers().stream()
                .map(m -> m.getDescription().getUiName().toLowerCase().replaceAll("\\(.*?\\)", "").trim())
                .collect(Collectors.toList());

        final String massString = Integer.toString(aT.getChassis().getMassMax());
        final String factionShortString = aT.getChassis().getFaction().getUiShortName().toLowerCase();
        final String factionLongString = aT.getChassis().getFaction().getUiName().toLowerCase();
        final String chassisString = aT.getChassis().getName().toLowerCase();
        final String loadoutName = aT.getName().toLowerCase();

        for (int i = 0; i < searchString.length();) {
            int nextSpace = searchString.indexOf(' ', i);
            if (nextSpace < 0) {
                nextSpace = searchString.length();
            }
            final CharSequence term = searchString.subSequence(i, nextSpace);

            if (loadoutName.contains(term) || //
                    chassisString.contains(term) || //
                    factionLongString.contentEquals(term) || //
                    factionShortString.contentEquals(term) || //
                    massString.contentEquals(term)) {
                i = nextSpace + 1;
                continue;
            }

            boolean failure = true;
            for (final String modifier : modifiers) {
                if (i + modifier.length() <= searchString.length() && //
                        searchString.substring(i, i + modifier.length()).contentEquals(modifier)) {
                    failure = false;
                    i += modifier.length();
                    if (i < searchString.length() && searchString.charAt(i) == ' ') {
                        i++;
                    }
                    break;
                }
            }
            if (failure) {
                return false;
            }
        }
        return true;
    }
}
