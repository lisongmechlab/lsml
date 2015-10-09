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
package org.lisoft.lsml.parsing.export;

import java.io.ByteArrayInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Scanner;
import java.util.TreeMap;

import org.lisoft.lsml.command.CmdAddItem;
import org.lisoft.lsml.command.CmdAddModule;
import org.lisoft.lsml.command.CmdLoadStock;
import org.lisoft.lsml.command.CmdSetArmor;
import org.lisoft.lsml.command.CmdSetArmorType;
import org.lisoft.lsml.command.CmdSetGuidanceType;
import org.lisoft.lsml.command.CmdSetHeatSinkType;
import org.lisoft.lsml.command.CmdSetStructureType;
import org.lisoft.lsml.model.chassi.ArmorSide;
import org.lisoft.lsml.model.chassi.ChassisBase;
import org.lisoft.lsml.model.chassi.ChassisClass;
import org.lisoft.lsml.model.chassi.ChassisDB;
import org.lisoft.lsml.model.chassi.ChassisStandard;
import org.lisoft.lsml.model.chassi.Location;
import org.lisoft.lsml.model.item.HeatSink;
import org.lisoft.lsml.model.item.Item;
import org.lisoft.lsml.model.item.ItemDB;
import org.lisoft.lsml.model.item.PilotModuleDB;
import org.lisoft.lsml.model.loadout.DefaultLoadoutFactory;
import org.lisoft.lsml.model.loadout.LoadoutBase;
import org.lisoft.lsml.model.loadout.LoadoutStandard;
import org.lisoft.lsml.model.upgrades.ArmorUpgrade;
import org.lisoft.lsml.model.upgrades.GuidanceUpgrade;
import org.lisoft.lsml.model.upgrades.HeatSinkUpgrade;
import org.lisoft.lsml.model.upgrades.StructureUpgrade;
import org.lisoft.lsml.model.upgrades.UpgradeDB;
import org.lisoft.lsml.util.CommandStack;
import org.lisoft.lsml.util.DecodingException;
import org.lisoft.lsml.util.EncodingException;
import org.lisoft.lsml.util.Huffman1;

/**
 * The Second version of {@link LoadoutCoder} for LSML.
 * 
 * @author Emily Björk
 */
public class LoadoutCoderV2 implements LoadoutCoder {
    private static final int        HEADER_MAGIC = 0xAC + 1;
    private final Huffman1<Integer> huff;

    public LoadoutCoderV2() {
        ObjectInputStream in = null;
        try {
            InputStream is = LoadoutCoderV2.class.getResourceAsStream("/resources/coderstats_v2.bin");
            in = new ObjectInputStream(is);
            @SuppressWarnings("unchecked")
            Map<Integer, Integer> freqs = (Map<Integer, Integer>) in.readObject();
            huff = new Huffman1<Integer>(freqs, null);

            // for(Map.Entry<Integer, Integer> e : freqs.entrySet())
            // System.out.println("["+e.getKey() + "] = " + e.getValue());
        }
        catch (Exception e) {
            throw new RuntimeException(e);
        }
        finally {
            if (in != null) {
                try {
                    in.close();
                }
                catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    @Override
    public byte[] encode(final LoadoutBase<?> aLoadout) throws EncodingException {
        throw new EncodingException("Protocol version 2 encoding is no longer allowed.");
    }

    @Override
    public LoadoutStandard decode(final byte[] aBitStream) throws DecodingException, Exception {
        final ByteArrayInputStream buffer = new ByteArrayInputStream(aBitStream);
        final LoadoutStandard loadout;
        final CommandStack stack = new CommandStack(0);

        // Read header
        {
            if (buffer.read() != HEADER_MAGIC) {
                throw new DecodingException("Wrong format!"); // Wrong format
            }

            int upeff = buffer.read() & 0xFF; // 8 bits for efficiencies and
            // 16 bits contain chassis ID (Big endian, respecting RFC 1700)
            short chassiId = (short) (((buffer.read() & 0xFF) << 8) | (buffer.read() & 0xFF));

            ChassisBase chassis = ChassisDB.lookup(chassiId);
            if (!(chassis instanceof ChassisStandard)) {
                throw new DecodingException("LSML link format v2 does not support omni mechs.");
            }
            loadout = (LoadoutStandard) DefaultLoadoutFactory.instance.produceEmpty(chassis);
            loadout.getEfficiencies().setCoolRun((upeff & (1 << 4)) != 0, null);
            loadout.getEfficiencies().setHeatContainment((upeff & (1 << 3)) != 0, null);
            loadout.getEfficiencies().setSpeedTweak((upeff & (1 << 2)) != 0, null);
            loadout.getEfficiencies().setDoubleBasics((upeff & (1 << 1)) != 0, null);
            loadout.getEfficiencies().setFastFire((upeff & (1 << 0)) != 0, null);
        }

        // Armor values next, RA, RT, RL, HD, CT, LT, LL, LA
        // 1 byte per armor value (2 for RT,CT,LT front first)
        for (Location location : Location.right2Left()) {
            if (location.isTwoSided()) {
                stack.pushAndApply(new CmdSetArmor(null, loadout, loadout.getComponent(location), ArmorSide.FRONT,
                        buffer.read(), true));
                stack.pushAndApply(new CmdSetArmor(null, loadout, loadout.getComponent(location), ArmorSide.BACK, buffer
                        .read(), true));
            }
            else {
                stack.pushAndApply(new CmdSetArmor(null, loadout, loadout.getComponent(location), ArmorSide.ONLY, buffer
                        .read(), true));
            }
        }

        // Items are encoded as a list of integers which record the item ID. Components are separated by -1.
        // The order is the same as for armor: RA, RT, RL, HD, CT, LT, LL, LA
        {
            byte[] rest = new byte[buffer.available()];
            try {
                buffer.read(rest);
            }
            catch (IOException e) {
                throw new DecodingException(e);
            }
            List<Integer> ids = huff.decode(rest);
            stack.pushAndApply(new CmdSetArmorType(null, loadout, (ArmorUpgrade) UpgradeDB.lookup(ids.get(0))));
            stack.pushAndApply(new CmdSetStructureType(null, loadout, (StructureUpgrade) UpgradeDB.lookup(ids.get(1))));
            stack.pushAndApply(new CmdSetHeatSinkType(null, loadout, (HeatSinkUpgrade) UpgradeDB.lookup(ids.get(2))));
            stack.pushAndApply(new CmdSetGuidanceType(null, loadout, (GuidanceUpgrade) UpgradeDB.lookup(ids.get(3))));

            if (-1 != ids.get(4)) {
                throw new DecodingException("Broken LSML link, expected separator got: " + ids.get(4));
            }
            ids.remove(4);
            ids.remove(3);
            ids.remove(2);
            ids.remove(1);
            ids.remove(0);

            for (Location location : Location.right2Left()) {
                Integer v;
                List<Item> later = new ArrayList<>();
                while (!ids.isEmpty() && -1 != (v = ids.remove(0))) {
                    Item item = ItemDB.lookup(v);
                    if (item instanceof HeatSink) {
                        later.add(item); // Add heat sinks last after engine has been added
                        continue;
                    }
                    stack.pushAndApply(new CmdAddItem(null, loadout, loadout.getComponent(location), ItemDB.lookup(v)));
                }
                for (Item i : later) {
                    stack.pushAndApply(new CmdAddItem(null, loadout, loadout.getComponent(location), i));
                }
            }

            Integer v;
            while (!ids.isEmpty() && -1 != (v = ids.remove(0))) {
                stack.pushAndApply(new CmdAddModule(null, loadout, PilotModuleDB.lookup(v.intValue())));
            }
        }
        return loadout;
    }

    @Override
    public boolean canDecode(byte[] aBitStream) {
        final ByteArrayInputStream buffer = new ByteArrayInputStream(aBitStream);
        return buffer.read() == HEADER_MAGIC;
    }

    /**
     * Will process the stock builds and generate statistics and dump it to a file.
     * 
     * @param arg
     * @throws Exception
     */
    public static void main(String[] arg) throws Exception {
        // generateAllLoadouts();
        // generateStatsFromStdin();
    }

    @SuppressWarnings("unused")
    private static void generateAllLoadouts() throws Exception {
        List<ChassisBase> chassii = new ArrayList<>(ChassisDB.lookup(ChassisClass.LIGHT));
        chassii.addAll(ChassisDB.lookup(ChassisClass.MEDIUM));
        chassii.addAll(ChassisDB.lookup(ChassisClass.HEAVY));
        chassii.addAll(ChassisDB.lookup(ChassisClass.ASSAULT));
        Base64LoadoutCoder coder = new Base64LoadoutCoder();
        CommandStack stack = new CommandStack(0);
        for (ChassisBase chassis : chassii) {
            if (!(chassis instanceof ChassisStandard))
                continue;
            LoadoutStandard loadout = (LoadoutStandard) DefaultLoadoutFactory.instance.produceEmpty(chassis);
            stack.pushAndApply(new CmdLoadStock(chassis, loadout, null));
            System.out.println("[" + chassis.getName() + "]=" + coder.encodeLSML(loadout));
        }
    }

    @SuppressWarnings("unused")
    private static void generateStatsFromStdin() throws Exception {
        Scanner sc = new Scanner(System.in);

        int numLoadouts = Integer.parseInt(sc.nextLine());

        Map<Integer, Integer> freqs = new TreeMap<>();
        String line = sc.nextLine();
        do {
            String[] s = line.split(" ");
            int id = Integer.parseInt(s[0]);
            int freq = Integer.parseInt(s[1]);
            freqs.put(id, freq);
            line = sc.nextLine();
        } while (!line.contains("q"));

        // Make sure all items are in the statistics even if they have a very low probability
        for (Item item : ItemDB.lookup(Item.class)) {
            int id = item.getMwoId();
            if (!freqs.containsKey(id))
                freqs.put(id, 1);
        }

        freqs.put(-1, numLoadouts * 9); // 9 separators per loadout
        freqs.put(UpgradeDB.STANDARD_ARMOR.getMwoId(), numLoadouts * 7 / 10); // Standard armor
        freqs.put(UpgradeDB.FERRO_FIBROUS_ARMOR.getMwoId(), numLoadouts * 3 / 10); // Ferro Fibrous Armor
        freqs.put(UpgradeDB.STANDARD_STRUCTURE.getMwoId(), numLoadouts * 3 / 10); // Standard structure
        freqs.put(UpgradeDB.ENDO_STEEL_STRUCTURE.getMwoId(), numLoadouts * 7 / 10); // Endo-Steel
        freqs.put(UpgradeDB.STANDARD_HEATSINKS.getMwoId(), numLoadouts * 1 / 20); // SHS
        freqs.put(UpgradeDB.DOUBLE_HEATSINKS.getMwoId(), numLoadouts * 19 / 20); // DHS
        freqs.put(UpgradeDB.STANDARD_GUIDANCE.getMwoId(), numLoadouts * 7 / 10); // No Artemis
        freqs.put(UpgradeDB.ARTEMIS_IV.getMwoId(), numLoadouts * 3 / 10); // Artemis IV

        ObjectOutputStream out = new ObjectOutputStream(new FileOutputStream("resources/resources/coderstats_v2.bin"));
        out.writeObject(freqs);
        out.close();
        sc.close();
    }
}
