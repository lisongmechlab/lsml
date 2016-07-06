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
package org.lisoft.lsml.model.export;

import java.io.FileOutputStream;
import java.io.ObjectOutputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Scanner;
import java.util.TreeMap;

import org.lisoft.lsml.model.chassi.Chassis;
import org.lisoft.lsml.model.chassi.ChassisClass;
import org.lisoft.lsml.model.chassi.OmniPod;
import org.lisoft.lsml.model.datacache.ChassisDB;
import org.lisoft.lsml.model.datacache.ItemDB;
import org.lisoft.lsml.model.datacache.OmniPodDB;
import org.lisoft.lsml.model.datacache.PilotModuleDB;
import org.lisoft.lsml.model.datacache.UpgradeDB;
import org.lisoft.lsml.model.item.Item;
import org.lisoft.lsml.model.item.PilotModule;
import org.lisoft.lsml.model.loadout.DefaultLoadoutFactory;
import org.lisoft.lsml.model.loadout.Loadout;
import org.lisoft.lsml.model.loadout.component.ConfiguredComponent;

/**
 * This class is used for generating the frequency tables that are used for the Huffman coding in the loadout coders.
 *
 * @author Li Song
 *
 */
public class LoadoutCoderStatsGenerator {

    /**
     * Will process the stock builds and generate statistics and dump it to a file.
     *
     * @param arg
     *            Not used
     * @throws Exception
     *             if something went awry.
     */
    public static void main(String[] arg) throws Exception {
        // generateAllLoadouts();
        // generateStatsFromStdin();
        // generateStatsFromStock();
    }

    @SuppressWarnings("unused")
    private static void generateAllLoadouts() throws Exception {
        final List<Chassis> chassii = new ArrayList<>(ChassisDB.lookup(ChassisClass.LIGHT));
        chassii.addAll(ChassisDB.lookup(ChassisClass.MEDIUM));
        chassii.addAll(ChassisDB.lookup(ChassisClass.HEAVY));
        chassii.addAll(ChassisDB.lookup(ChassisClass.ASSAULT));
        final Base64LoadoutCoder coder = new Base64LoadoutCoder(null);
        for (final Chassis chassis : chassii) {
            final Loadout loadout = DefaultLoadoutFactory.instance.produceStock(chassis);
            System.out.println("[" + chassis.getName() + "]=" + coder.encodeLSML(loadout));
        }
    }

    @SuppressWarnings("unused")
    private static void generateStatsFromStdin() throws Exception {
        try (final Scanner sc = new Scanner(System.in, "ASCII");) {

            final int numLoadouts = Integer.parseInt(sc.nextLine());

            final Map<Integer, Integer> freqs = new TreeMap<>();
            String line = sc.nextLine();
            do {
                final String[] s = line.split(" ");
                final int id = Integer.parseInt(s[0]);
                final int freq = Integer.parseInt(s[1]);
                freqs.put(id, freq);
                line = sc.nextLine();
            } while (!line.contains("q"));

            // Make sure all items are in the statistics even if they have a very low probability
            for (final Item item : ItemDB.lookup(Item.class)) {
                final int id = item.getMwoId();
                if (!freqs.containsKey(id)) {
                    freqs.put(id, 1);
                }
            }

            freqs.put(-1, numLoadouts * 9); // 9 separators per loadout
            freqs.put(UpgradeDB.IS_STD_ARMOUR.getMwoId(), numLoadouts * 7 / 10); // Standard armour
            freqs.put(UpgradeDB.IS_FF_ARMOUR.getMwoId(), numLoadouts * 3 / 10); // Ferro Fibrous Armour
            freqs.put(UpgradeDB.IS_STD_STRUCTURE.getMwoId(), numLoadouts * 3 / 10); // Standard structure
            freqs.put(UpgradeDB.IS_ES_STRUCTURE.getMwoId(), numLoadouts * 7 / 10); // Endo-Steel
            freqs.put(UpgradeDB.IS_SHS.getMwoId(), numLoadouts * 1 / 20); // SHS
            freqs.put(UpgradeDB.IS_DHS.getMwoId(), numLoadouts * 19 / 20); // DHS
            freqs.put(UpgradeDB.STD_GUIDANCE.getMwoId(), numLoadouts * 7 / 10); // No Artemis
            freqs.put(UpgradeDB.ARTEMIS_IV.getMwoId(), numLoadouts * 3 / 10); // Artemis IV

            try (final FileOutputStream fos = new FileOutputStream("resources/resources/coderstats_v2.bin");
                    final ObjectOutputStream out = new ObjectOutputStream(fos);) {
                out.writeObject(freqs);
            }
        }
    }

    @SuppressWarnings("unused")
    private static void generateStatsFromStock() throws Exception {
        final Map<Integer, Integer> freqs = new HashMap<>();

        final List<Chassis> chassii = new ArrayList<>(ChassisDB.lookup(ChassisClass.LIGHT));
        chassii.addAll(ChassisDB.lookup(ChassisClass.MEDIUM));
        chassii.addAll(ChassisDB.lookup(ChassisClass.HEAVY));
        chassii.addAll(ChassisDB.lookup(ChassisClass.ASSAULT));
        final List<Integer> idStats = new ArrayList<>();

        // Process items from all stock loadouts
        for (final Chassis chassis : chassii) {
            final Loadout loadout = DefaultLoadoutFactory.instance.produceStock(chassis);

            for (final ConfiguredComponent component : loadout.getComponents()) {
                for (final Item item : component.getItemsEquipped()) {
                    Integer f = freqs.get(item.getMwoId());
                    f = f == null ? 1 : f + 1;
                    freqs.put(item.getMwoId(), f);
                }
            }
        }

        // Add all item ids to the stats list
        for (final Item item : ItemDB.lookup(Item.class)) {
            idStats.add(item.getMwoId());
        }

        // Process omnipods with equal probability
        for (final OmniPod omniPod : OmniPodDB.all()) {
            // Constant frequency of 5, every omnipod appears at most once in the stocks.
            // But this is not representative.
            freqs.put(omniPod.getMwoId(), 5);
            idStats.add(omniPod.getMwoId());
        }

        // Process Pilot modules with equal probability
        for (final PilotModule module : PilotModuleDB.lookup(PilotModule.class)) {
            // Constant frequency of 5, every omnipod appears at most once in the stocks.
            // But this is not representative.
            freqs.put(module.getMwoId(), 3);
            idStats.add(module.getMwoId());
        }

        // Add all unused IDs in the used ranges to the frequency map with frequency 1.
        Collections.sort(idStats);
        int start = -1;
        int last = 0;
        for (final int i : idStats) {
            if (start == -1) {
                start = 1000 * (i / 1000);
            }
            else if (last + 1000 < i) {
                final int end = 1000 * (last / 1000) + 1000;
                for (int id = start; id < end; ++id) {
                    final Integer f = freqs.get(id);
                    if (f == null) {
                        freqs.put(id, 1);
                    }
                }
                System.out.println("Added range: [" + start + ", " + end + "],");
                start = 1000 * (i / 1000);
            }
            last = i;
        }

        // Some manual tweaks

        // 1) Swap DHS and SHS probability for IS mechs.
        {
            final Integer shs = freqs.get(ItemDB.SHS.getMwoId());
            final Integer dhs = freqs.get(ItemDB.DHS.getMwoId());
            freqs.put(ItemDB.SHS.getMwoId(), dhs);
            freqs.put(ItemDB.DHS.getMwoId(), shs);
        }

        // 2) The separators need to be accounted for.
        freqs.put(-1, chassii.size() * 8);

        for (final Entry<Integer, Integer> entry : freqs.entrySet()) {
            String name;
            try {
                final Item item = ItemDB.lookup(entry.getKey());
                name = item.getName();
            }
            catch (final Throwable t) {
                try {
                    final OmniPod omniPod = OmniPodDB.lookup(entry.getKey());
                    name = "omnipod for " + omniPod.getChassisSeries();
                }
                catch (final Throwable t1) {
                    try {
                        final PilotModule module = PilotModuleDB.lookup(entry.getKey());
                        name = module.getName();
                    }
                    catch (final Throwable t2) {
                        name = "reserved id";
                    }
                }
            }

            System.out.println(entry.getKey() + " : " + entry.getValue() + " // " + name);
        }

        try (final FileOutputStream fos = new FileOutputStream("resources/resources/coderstats_v3.bin");
                final ObjectOutputStream out = new ObjectOutputStream(fos);) {
            out.writeObject(freqs);
        }
    }

}
