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
package org.lisoft.lsml.model.search;

import java.util.*;

import org.lisoft.lsml.model.chassi.Chassis;
import org.lisoft.lsml.model.item.Faction;
import org.lisoft.lsml.model.loadout.Loadout;
import org.lisoft.lsml.model.modifiers.Modifier;

/**
 * A search index that can be used for finding loadouts based on keywords
 *
 * @author Li Song
 */
public class SearchIndex {
    private final Map<String, Set<Loadout>> inverseIndex = new HashMap<>();
    private boolean dirty = false;

    /**
     * Removes the given loadout from the search index.
     *
     * An index rebuild is automatically performed on the next query if it has not been forced before the query.
     *
     * @param aLoadout
     *            The {@link Loadout} to remove from the index.
     */
    public void unmerge(Loadout aLoadout) {
        inverseIndex.computeIfAbsent("", k -> new HashSet<>()).remove(aLoadout);
        dirty = true;
    }

    /**
     * Rebuilds the search index to take updated documents changes into the index.
     */
    public void rebuild() {
        final Set<Loadout> documents = inverseIndex.computeIfAbsent("", k -> new HashSet<>());
        inverseIndex.clear();
        for (final Loadout document : documents) {
            merge(document);
        }
        dirty = false;
    }

    /**
     * Merges the given loadout into the search index.
     *
     * @param aLoadout
     *            A loadout to merge
     */
    public void merge(Loadout aLoadout) {
        inverseIndex.computeIfAbsent("", k -> new HashSet<>()).add(aLoadout);

        addPrefixes(aLoadout, aLoadout.getName());

        final Chassis chassis = aLoadout.getChassis();
        addPrefixes(aLoadout, chassis.getSeriesName());
        addPrefixes(aLoadout, chassis.getShortName());
        addPrefixes(aLoadout, chassis.getName());
        addPrefixes(aLoadout, Integer.toString(chassis.getMassMax()) + "ton");
        addPrefixes(aLoadout, Integer.toString(chassis.getMassMax()) + " ton");

        final Faction faction = chassis.getFaction();
        if (null != faction) {
            // Faction will never be null in actual production code but this helps
            // make setup of test cases easier.
            addPrefixes(aLoadout, faction.getUiName());
            addPrefixes(aLoadout, faction.getUiShortName());
        }

        for (final Modifier modifier : aLoadout.getAllModifiers()) {
            addPrefixes(aLoadout, modifier.getDescription().getUiName());
        }
    }

    private void addPrefixes(Loadout aLoadout, String aKeyword) {
        if (null == aKeyword) {
            return;
        }
        if (aKeyword.contains(" ")) {
            for (final String part : aKeyword.split(" ")) {
                addPrefixes(aLoadout, part);
            }
        }
        String prefix = aKeyword.toLowerCase();
        while (!prefix.isEmpty()) {
            final Set<Loadout> documents = inverseIndex.computeIfAbsent(prefix, k -> new HashSet<>());
            documents.add(aLoadout);
            prefix = prefix.substring(0, prefix.length() - 1);
        }
    }

    /**
     * Queries the index for a search string. It will match substrings of the indexed document and it will be case
     * insensitive.
     *
     * @param aSearchString
     * @return A {@link Collection} of {@link Loadout}s.
     */
    public Collection<Loadout> query(String aSearchString) {
        if (dirty) {
            rebuild();
        }

        final List<Set<Loadout>> hits = new ArrayList<>();
        for (final String part : aSearchString.toLowerCase().split(" ")) {
            hits.add(inverseIndex.getOrDefault(part, Collections.EMPTY_SET));
        }
        hits.sort((l, r) -> l.size() - r.size());

        final Iterator<Set<Loadout>> it = hits.iterator();
        final Set<Loadout> ans = new HashSet<>(it.next());
        while (it.hasNext()) {
            ans.retainAll(it.next());
        }

        return ans;
    }

    /**
     * Call when a document has been changed. Will cause a reindexing of all documents on the next query.
     */
    public void update() {
        dirty = true;
    }
}
