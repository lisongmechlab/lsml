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
package org.lisoft.lsml.model.export;

import java.io.FileOutputStream;
import java.io.ObjectOutputStream;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.Map.Entry;
import java.util.stream.Collectors;
import org.lisoft.lsml.model.ConsumableDB;
import org.lisoft.lsml.model.loadout.ConfiguredComponent;
import org.lisoft.lsml.model.loadout.DefaultLoadoutFactory;
import org.lisoft.lsml.model.loadout.Loadout;
import org.lisoft.lsml.model.loadout.LoadoutFactory;
import org.lisoft.lsml.util.TestHelpers;
import org.lisoft.mwo_data.ChassisDB;
import org.lisoft.mwo_data.ItemDB;
import org.lisoft.mwo_data.OmniPodDB;
import org.lisoft.mwo_data.equipment.Consumable;
import org.lisoft.mwo_data.equipment.Item;
import org.lisoft.mwo_data.equipment.MwoObject;
import org.lisoft.mwo_data.equipment.UpgradeDB;
import org.lisoft.mwo_data.mechs.Chassis;
import org.lisoft.mwo_data.mechs.OmniPod;

/**
 * This class is used for generating the frequency tables that are used for the Huffman coding in
 * the loadout coders.
 *
 * @author Li Song
 */
public class LoadoutCoderStatsGenerator {
  private static final LoadoutFactory loadoutFactory = new DefaultLoadoutFactory();

  /**
   * Will process the stock builds and generate statistics and dump it to a file.
   *
   * @param arg Not used
   */
  public static void main(String[] arg) {
    generateAllLoadouts();
    // generateStatsFromStdIn();
    // generateStatsFromStock();
  }

  @SuppressWarnings("unused")
  private static void generateAllLoadouts() {
    for (final Chassis chassis : ChassisDB.lookupAll()) {
      try {
        final Loadout loadout = loadoutFactory.produceStock(chassis);
        System.out.println("[" + chassis.getName() + "]=" + TestHelpers.encodeLSML(loadout));
      } catch (final Throwable e) {
        // Silently ignore errors when we can't load stock due to data errors from PGI.
        e.fillInStackTrace(); // Make spotbugs shut up about ignoring the exception.
      }
    }
  }

  @SuppressWarnings("unused")
  private static void generateStatsFromStdIn() throws Exception {
    try (final Scanner sc = new Scanner(System.in, StandardCharsets.US_ASCII)) {

      final int numLoadouts = Integer.parseInt(sc.nextLine());

      final Map<Integer, Integer> frequencies = new TreeMap<>();
      String line = sc.nextLine();
      do {
        final String[] s = line.split(" ");
        final int id = Integer.parseInt(s[0]);
        final int freq = Integer.parseInt(s[1]);
        frequencies.put(id, freq);
        line = sc.nextLine();
      } while (!line.contains("q"));

      // Make sure all items are in the statistics even if they have a very low
      // probability
      for (final Item item : ItemDB.lookup(Item.class)) {
        final int id = item.getId();
        if (!frequencies.containsKey(id)) {
          frequencies.put(id, 1);
        }
      }

      frequencies.put(-1, numLoadouts * 9); // 9 separators per loadout
      frequencies.put(UpgradeDB.IS_STD_ARMOUR.getId(), numLoadouts * 7 / 10); // Standard armour
      frequencies.put(UpgradeDB.IS_FF_ARMOUR.getId(), numLoadouts * 3 / 10); // Ferro-Fibrous Armour
      frequencies.put(
          UpgradeDB.IS_STD_STRUCTURE.getId(), numLoadouts * 3 / 10); // Standard structure
      frequencies.put(UpgradeDB.IS_ES_STRUCTURE.getId(), numLoadouts * 7 / 10); // Endo-Steel
      frequencies.put(UpgradeDB.IS_SHS.getId(), numLoadouts * 1 / 20); // SHS
      frequencies.put(UpgradeDB.IS_DHS.getId(), numLoadouts * 19 / 20); // DHS
      frequencies.put(UpgradeDB.STD_GUIDANCE.getId(), numLoadouts * 7 / 10); // No Artemis
      frequencies.put(UpgradeDB.ARTEMIS_IV.getId(), numLoadouts * 3 / 10); // Artemis IV

      try (final FileOutputStream fos =
              new FileOutputStream("resources/resources/coderstats_v2.bin");
          final ObjectOutputStream out = new ObjectOutputStream(fos)) {
        out.writeObject(frequencies);
      }
    }
  }

  @SuppressWarnings("unused")
  private static void generateStatsFromStock() throws Exception {
    final Map<Integer, Integer> frequencies = new HashMap<>();

    // Process items from all stock loadouts
    final Collection<Chassis> allChassis = ChassisDB.lookupAll();
    for (final Chassis chassis : allChassis) {
      try {
        final Loadout loadout = loadoutFactory.produceStock(chassis);
        for (final ConfiguredComponent component : loadout.getComponents()) {
          for (final Item item : component.getItemsEquipped()) {
            Integer f = frequencies.get(item.getId());
            f = f == null ? 1 : f + 1;
            frequencies.put(item.getId(), f);
          }
        }
      } catch (final Exception e) {
        System.out.println("Skipping: " + chassis.getName() + ", couldn't load stock.");
      }
    }

    // Add all item ids to the stats list
    final List<Integer> idStats =
        ItemDB.lookup(Item.class).stream().map(MwoObject::getId).collect(Collectors.toList());

    // Process omni pods with equal probability
    for (final OmniPod omniPod : OmniPodDB.all()) {
      // Constant frequency of 5, every omni pod appears at most once in the stocks.
      // But this is not representative.
      frequencies.put(omniPod.getId(), 5);
      idStats.add(omniPod.getId());
    }

    // Process Pilot modules with equal probability
    for (final Consumable module : ConsumableDB.lookup(Consumable.class)) {
      // Constant frequency of 5, every omni pod appears at most once in the stocks.
      // But this is not representative.
      frequencies.put(module.getId(), 3);
      idStats.add(module.getId());
    }

    // Add all unused IDs in the used ranges to the frequency map with frequency 1.
    Collections.sort(idStats);
    final int rangeSize = 2000;
    int start = rangeSize * (idStats.get(0) / rangeSize);
    int last = 0;
    for (final int i : idStats) {
      if (i > last + rangeSize) {
        final int end = rangeSize * (last / rangeSize) + rangeSize;
        for (int id = start; id < end; ++id) {
          frequencies.putIfAbsent(id, 1);
        }
        System.out.println("Added range: [" + start + ", " + end + "],");
        start = rangeSize * (i / rangeSize);
      }
      last = i;
    }

    // Some manual tweaks

    // 1) Swap DHS and SHS probability for IS mechs.
    {
      final Integer shs = frequencies.get(ItemDB.SHS.getId());
      final Integer dhs = frequencies.get(ItemDB.DHS.getId());
      frequencies.put(ItemDB.SHS.getId(), dhs);
      frequencies.put(ItemDB.DHS.getId(), shs);
    }

    // 2) The separators need to be accounted for.
    frequencies.put(-1, allChassis.size() * 8);

    for (final Entry<Integer, Integer> entry : frequencies.entrySet()) {
      String name;
      try {
        final Item item = ItemDB.lookup(entry.getKey());
        name = item.getName();
      } catch (final Throwable t) {
        try {
          final OmniPod omniPod = OmniPodDB.lookup(entry.getKey());
          name = "omnipod for " + omniPod.getChassisSeries();
        } catch (final Throwable t1) {
          try {
            final Consumable module = ConsumableDB.lookup(entry.getKey());
            name = module.getName();
          } catch (final Throwable t2) {
            name = "reserved id";
          }
        }
      }

      System.out.println(entry.getKey() + " : " + entry.getValue() + " // " + name);
    }

    try (final FileOutputStream fos = new FileOutputStream("src/main/resources/coderstats_v4.bin");
        final ObjectOutputStream out = new ObjectOutputStream(fos)) {
      out.writeObject(frequencies);
    }
  }
}
