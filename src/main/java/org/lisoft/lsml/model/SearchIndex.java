/*
 * Li Song Mechlab - A 'mech building tool for PGI's MechWarrior: Online.
 * Copyright (C) 2013-2023  Li Song
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
package org.lisoft.lsml.model;

import java.util.*;
import org.lisoft.lsml.model.loadout.Loadout;
import org.lisoft.mwo_data.Faction;
import org.lisoft.mwo_data.mechs.Chassis;
import org.lisoft.mwo_data.modifiers.Modifier;

/**
 * A search index that can be used for finding loadouts based on keywords.
 *
 * @author Li Song
 */
public class SearchIndex {
  private static final String ALL_DOCUMENTS = "";
  private final Map<String, Set<Loadout>> invertedIndex = new HashMap<>();
  private boolean dirty = false;

  /**
   * Merges the given loadout into the search index.
   *
   * @param aLoadout A loadout to merge
   */
  public void merge(Loadout aLoadout) {
    documentsByKey(ALL_DOCUMENTS).add(aLoadout);

    addPrefixes(aLoadout, aLoadout.getName());

    final Chassis chassis = aLoadout.getChassis();
    addPrefixes(aLoadout, chassis.getSeriesName());
    addPrefixes(aLoadout, chassis.getShortName());
    addPrefixes(aLoadout, chassis.getName());
    addPrefixes(aLoadout, chassis.getMassMax() + "ton");
    addPrefixes(aLoadout, chassis.getMassMax() + " ton");

    final Faction faction = chassis.getFaction();
    addPrefixes(aLoadout, faction.getUiName());
    addPrefixes(aLoadout, faction.getUiShortName());

    for (final Modifier modifier : aLoadout.getAllModifiers()) {
      addPrefixes(aLoadout, modifier.getDescription().getUiName());
    }
  }

  /**
   * Queries the index for a search string. It will match substrings of the indexed documents, it is
   * case-insensitive.
   *
   * @param aSearchString A query string with search terms separated by white space
   * @return A {@link Collection} of {@link Loadout}s.
   */
  public Collection<Loadout> query(String aSearchString) {
    if (dirty) {
      rebuild();
    }
    aSearchString = aSearchString.trim();

    if (aSearchString.isEmpty()) {
      return Collections.emptyList();
    }

    final List<Set<Loadout>> hits = new ArrayList<>();
    for (final String part : aSearchString.toLowerCase().split(" ")) {
      hits.add(invertedIndex.getOrDefault(part, Collections.emptySet()));
    }
    hits.sort(Comparator.comparingInt(Set::size));

    final Iterator<Set<Loadout>> it = hits.iterator();
    final Set<Loadout> ans = new HashSet<>(it.next());
    while (it.hasNext()) {
      ans.retainAll(it.next());
    }

    return ans;
  }

  /** Rebuilds the search index to take updated documents changes into the index. */
  public void rebuild() {
    final Set<Loadout> documents = documentsByKey(ALL_DOCUMENTS);
    invertedIndex.clear();
    for (final Loadout document : documents) {
      merge(document);
    }
    dirty = false;
  }

  /**
   * Removes the given loadout from the search index.
   *
   * <p>An index rebuild is automatically performed on the next query if it has not been forced
   * before the query.
   *
   * @param aLoadout The {@link Loadout} to remove from the index.
   */
  public void unmerge(Loadout aLoadout) {
    documentsByKey(ALL_DOCUMENTS).remove(aLoadout);
    dirty = true;
  }

  /**
   * Call when a document has been changed. Will cause a reindexing of all documents on the next
   * query.
   */
  public void update() {
    dirty = true;
  }

  private void addPrefixes(Loadout aLoadout, String aKeyword) {
    if (null == aKeyword) {
      // These keywords will never be null in production but makes
      // setting up tests much easier.
      return;
    }
    if (aKeyword.contains(" ")) {
      for (final String part : aKeyword.split(" ")) {
        addPrefixes(aLoadout, part);
      }
    }
    String prefix = aKeyword.toLowerCase();
    while (!prefix.isEmpty()) {
      final Set<Loadout> documents = documentsByKey(prefix);
      documents.add(aLoadout);
      prefix = prefix.substring(0, prefix.length() - 1);
    }
  }

  private Set<Loadout> documentsByKey(String aKeyword) {
    return invertedIndex.computeIfAbsent(aKeyword, k -> new HashSet<>());
  }
}
