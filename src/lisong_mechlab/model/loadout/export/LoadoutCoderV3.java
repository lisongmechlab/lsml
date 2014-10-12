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
package lisong_mechlab.model.loadout.export;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Scanner;
import java.util.TreeMap;

import javax.swing.JOptionPane;

import lisong_mechlab.model.chassi.ArmorSide;
import lisong_mechlab.model.chassi.ChassisBase;
import lisong_mechlab.model.chassi.ChassisClass;
import lisong_mechlab.model.chassi.ChassisDB;
import lisong_mechlab.model.chassi.ChassisOmniMech;
import lisong_mechlab.model.chassi.ChassisStandard;
import lisong_mechlab.model.chassi.Location;
import lisong_mechlab.model.chassi.OmniPod;
import lisong_mechlab.model.chassi.OmniPodDB;
import lisong_mechlab.model.item.Internal;
import lisong_mechlab.model.item.Item;
import lisong_mechlab.model.item.ItemDB;
import lisong_mechlab.model.item.PilotModule;
import lisong_mechlab.model.item.PilotModuleDB;
import lisong_mechlab.model.loadout.LoadoutBase;
import lisong_mechlab.model.loadout.LoadoutBuilder;
import lisong_mechlab.model.loadout.LoadoutOmniMech;
import lisong_mechlab.model.loadout.LoadoutStandard;
import lisong_mechlab.model.loadout.OpAddModule;
import lisong_mechlab.model.loadout.OpLoadStock;
import lisong_mechlab.model.loadout.component.ComponentBuilder;
import lisong_mechlab.model.loadout.component.ConfiguredComponentBase;
import lisong_mechlab.model.loadout.component.ConfiguredComponentOmniMech;
import lisong_mechlab.model.loadout.component.OpAddItem;
import lisong_mechlab.model.loadout.component.OpChangeOmniPod;
import lisong_mechlab.model.loadout.component.OpSetArmor;
import lisong_mechlab.model.loadout.component.OpToggleItem;
import lisong_mechlab.model.upgrades.ArmorUpgrade;
import lisong_mechlab.model.upgrades.GuidanceUpgrade;
import lisong_mechlab.model.upgrades.HeatSinkUpgrade;
import lisong_mechlab.model.upgrades.OpSetArmorType;
import lisong_mechlab.model.upgrades.OpSetGuidanceType;
import lisong_mechlab.model.upgrades.OpSetHeatSinkType;
import lisong_mechlab.model.upgrades.OpSetStructureType;
import lisong_mechlab.model.upgrades.StructureUpgrade;
import lisong_mechlab.model.upgrades.UpgradeDB;
import lisong_mechlab.util.DecodingException;
import lisong_mechlab.util.EncodingException;
import lisong_mechlab.util.Huffman1;
import lisong_mechlab.util.Huffman2;
import lisong_mechlab.util.OperationStack;
import lisong_mechlab.view.ProgramInit;

/**
 * The Second version of {@link LoadoutCoder} for LSML.
 * 
 * @author Li Song
 */
public class LoadoutCoderV3 implements LoadoutCoder {
    private static final int        HEADER_MAGIC = 0xAC + 2;
    private final Huffman2<Integer> huff;

    public LoadoutCoderV3() {
        ObjectInputStream in = null;
        try {
            InputStream is = LoadoutCoderV3.class.getResourceAsStream("/resources/coderstats_v3.bin");
            in = new ObjectInputStream(is);
            @SuppressWarnings("unchecked")
            Map<Integer, Integer> freqs = (Map<Integer, Integer>) in.readObject();
            huff = new Huffman2<Integer>(freqs, null);
            /*
             * for(Map.Entry<Integer, Integer> e : freqs.entrySet()) System.out.println("[" + e.getKey() + "] = " +
             * e.getValue()); List<Integer> tmp = new ArrayList<>(); tmp.add(3051); tmp.add(30077); tmp.add(1233);
             * tmp.add(1201); tmp.add(2218); tmp.add(2202); tmp.add(-1); tmp.add(30076); tmp.add(-1); tmp.add(30079);
             * tmp.add(-1); tmp.add(30072); tmp.add(-1); tmp.add(-1); tmp.add(30074); tmp.add(-1); tmp.add(30078);
             * tmp.add(-1); tmp.add(30073); tmp.add(1213); tmp.add(1214); tmp.add(-1); byte[] out = huff.encode(tmp);
             * for(byte b : out){ int i; if( b < 0 ){ i = 256 + b; } else i = b; System.out.println(i); }
             */
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
    public boolean canDecode(byte[] aBitStream) {
        final ByteArrayInputStream buffer = new ByteArrayInputStream(aBitStream);
        return buffer.read() == HEADER_MAGIC;
    }

    @Override
    public LoadoutBase<?> decode(final byte[] aBitStream) throws DecodingException {
        final ByteArrayInputStream buffer = new ByteArrayInputStream(aBitStream);

        // Read header
        if (buffer.read() != HEADER_MAGIC) {
            throw new DecodingException("Wrong format!"); // Wrong format
        }

        final LoadoutBuilder builder = new LoadoutBuilder();
        final LoadoutBase<?> loadout = readChassisLoadout(buffer);
        final boolean isOmniMech = loadout instanceof LoadoutOmniMech;

        readArmorValues(buffer, loadout, builder);
        if (isOmniMech) {
            readActuatorState(buffer.read(), loadout, builder);
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
            if (!isOmniMech) {
                LoadoutStandard loadoutStandard = (LoadoutStandard) loadout;
                builder.push(new OpSetArmorType(null, loadoutStandard, (ArmorUpgrade) UpgradeDB.lookup(ids.remove(0))));
                builder.push(new OpSetStructureType(null, loadoutStandard, (StructureUpgrade) UpgradeDB.lookup(ids
                        .remove(0))));
                builder.push(new OpSetHeatSinkType(null, loadoutStandard, (HeatSinkUpgrade) UpgradeDB.lookup(ids
                        .remove(0))));
            }
            builder.push(new OpSetGuidanceType(null, loadout, (GuidanceUpgrade) UpgradeDB.lookup(ids.remove(0))));

            for (Location location : Location.right2Left()) {
                if (isOmniMech && location != Location.CenterTorso) {
                    LoadoutOmniMech omniMech = (LoadoutOmniMech) loadout;
                    OmniPod omniPod = OmniPodDB.lookup(ids.remove(0));
                    builder.push(new OpChangeOmniPod(null, omniMech, omniMech.getComponent(location), omniPod));
                }

                Integer v;
                while (!ids.isEmpty() && -1 != (v = ids.remove(0))) {
                    builder.push(new OpAddItem(null, loadout, loadout.getComponent(location), ItemDB.lookup(v)));
                }
            }

            while (!ids.isEmpty()) {
                builder.push(new OpAddModule(null, loadout, PilotModuleDB.lookup(ids.remove(0).intValue())));
            }
        }

        builder.apply();
        String errors = builder.getErrors("<nameless LSML import>");
        if (null != errors) {
            JOptionPane.showMessageDialog(ProgramInit.lsml(), errors, "Error parsing loadout: <nameless LSML import>",
                    JOptionPane.WARNING_MESSAGE);
        }
        return loadout;
    }

    /**
     * Encodes the given {@link LoadoutBase} as a bit stream.
     * <p>
     * Bit stream format v3:
     * 
     * <pre>
     * <h1>Header:</h1>
     * Stream Offset(bytes)                Comment
     *    0  +---------------------------+
     *       | MAGIC_NUMBER (8bits)      | Must be equal to HEADER_MAGIC.
     *    1  +---------------------------+
     *       | MWO Chassis ID (16bits)   | The MWO ID of the chassis in Big-Endian (Motorola) order (NOTE: x86 is Little-Endian).
     *    3  +---------------------------+
     *       | RA Armor (8bits)          |
     *    4  +---------------------------+
     *       | RT Front Armor (8bits)    |
     *    5  +---------------------------+
     *       | RT Back Armor (8bits)     |
     *    6  +---------------------------+
     *       | RL Armor (8bits)          |
     *    7  +---------------------------+
     *       | HD Armor (8bits)          |
     *    8  +---------------------------+
     *       | CT Front Armor (8bits)    |
     *    9  +---------------------------+
     *       | CT Back Armor (8bits)     |
     *    10 +---------------------------+
     *       | LT Front Armor (8bits)    |
     *    11 +---------------------------+
     *       | LT Back Armor (8bits)     |
     *    12 +---------------------------+
     *       | LL Armor (8bits)          |
     *    13 +---------------------------+
     *       | LA Armor (8bits)          |
     *       +---------------------------+
     * 
     * If chassis is an OmniMech:
     *    14 +---------------------------+
     *       | Actuator State (8bits)    | An 8bit map of actuator states. The upper 4 bits are reserved and must be 0. 
     *       | Format: 0000XYZW          | Where X and Y is Right LAA and HA respectively, and Z and W is Left LAA and HA respectively.
     *       |                           | The bits are set if the actuators are present and false if the are removed or not available.
     *    15 +---------------------------+
     *       | Huffman coded data        | See further down.
     *   EOS +---------------------------+
     *    
     * Else if chassis is a Standard BattleMech:   
     *    14 +---------------------------+
     *       | Huffman coded data        | See further down.
     *   EOS +---------------------------+
     * 
     * <h1>Huffman coded data</h1>
     * The Huffman coded data is a list (or array if you want) of MWO item IDs and OmniPod IDs that have been encoded to a bit stream.
     * The list contents differs between OmniMechs and standard BattleMechs. The exact Huffman algorithm is defined by
     * {@link Huffman1} and the probability table is given in https://gist.github.com/LiSong-Mechlab/d1af79527270e862cf83c032e64f8083.
     * 
     * <h2>OmniMechs Format</h2>
     *  List:
     *    {
     *       (Guidance upgrade ID), 
     *       (RA OmniPod ID), {RA equipment IDs}, -1, 
     *       (RT OmniPod ID), {RT equipment IDs}, -1,
     *       (RL OmniPod ID), {TL equipment IDs}, -1,
     *       (HD OmniPod ID), {HD equipment IDs}, -1,
     *       {CT equipment IDs}, -1,
     *       (LT OmniPod ID), {LT equipment IDs}, -1,
     *       (LL OmniPod ID), {LL equipment IDs}, -1,
     *       (LT OmniPod ID), {LT equipment IDs}, -1,
     *       {Pilot Modules}
     *    }
     *    
     * <h2>Standard BattleMech Format</h2>
     *  List:
     *    {
     *       (Armor upgrade ID),
     *       (Structure upgrade ID),
     *       (Heat sink upgrade ID), 
     *       (Guidance upgrade ID), 
     *       {RA equipment IDs}, -1, 
     *       {RT equipment IDs}, -1,
     *       {TL equipment IDs}, -1,
     *       {HD equipment IDs}, -1,
     *       {CT equipment IDs}, -1,
     *       {LT equipment IDs}, -1,
     *       {LL equipment IDs}, -1,
     *       {LT equipment IDs}, -1,
     *       {Pilot Modules}
     *    }
     *    
     * The complete bit stream will be encoded in Base64 and used as link.
     * </pre>
     */
    @Override
    public byte[] encode(final LoadoutBase<?> aLoadout) throws EncodingException {
        boolean isOmniMech = aLoadout instanceof LoadoutOmniMech;

        final ByteArrayOutputStream buffer = new ByteArrayOutputStream(100);
        buffer.write(HEADER_MAGIC); // 8 bits for version number

        writeChassis(buffer, aLoadout);
        writeArmorValues(buffer, aLoadout);

        if (isOmniMech)
            writeActuatorState(buffer, aLoadout);

        List<Integer> ids = new ArrayList<>();

        if (!isOmniMech) {
            ids.add(aLoadout.getUpgrades().getArmor().getMwoId());
            ids.add(aLoadout.getUpgrades().getStructure().getMwoId());
            ids.add(aLoadout.getUpgrades().getHeatSink().getMwoId());
        }
        ids.add(aLoadout.getUpgrades().getGuidance().getMwoId());

        for (Location location : Location.right2Left()) {
            ConfiguredComponentBase component = aLoadout.getComponent(location);
            if (isOmniMech && location != Location.CenterTorso) {
                ids.add(((ConfiguredComponentOmniMech) component).getOmniPod().getMwoId());
            }

            for (Item item : component.getItemsEquipped()) {
                if (!(item instanceof Internal)) {
                    ids.add(item.getMwoId());
                }
            }
            ids.add(-1);
        }

        Collection<PilotModule> modules = aLoadout.getModules();
        for (PilotModule module : modules) {
            ids.add(module.getMwoId());
        }

        // Encode the list with huffman
        try {
            buffer.write(huff.encode(ids));
            return buffer.toByteArray();
        }
        catch (IOException e) {
            throw new EncodingException(e);
        }
    }

    private void readActuatorState(int aActuatorState, LoadoutBase<?> aLoadout, LoadoutBuilder aBuilder) {
        boolean RLAA = (aActuatorState & (1 << 3)) != 0;
        boolean RHA = (aActuatorState & (1 << 2)) != 0;
        boolean LLAA = (aActuatorState & (1 << 1)) != 0;
        boolean LHA = (aActuatorState & (1 << 0)) != 0;

        LoadoutOmniMech omniMech = (LoadoutOmniMech) aLoadout;
        aBuilder.push(new OpToggleItem(null, omniMech, omniMech.getComponent(Location.LeftArm), ItemDB.LAA, LLAA));
        aBuilder.push(new OpToggleItem(null, omniMech, omniMech.getComponent(Location.LeftArm), ItemDB.HA, LHA));
        aBuilder.push(new OpToggleItem(null, omniMech, omniMech.getComponent(Location.RightArm), ItemDB.LAA, RLAA));
        aBuilder.push(new OpToggleItem(null, omniMech, omniMech.getComponent(Location.RightArm), ItemDB.HA, RHA));
    }

    private void writeActuatorState(ByteArrayOutputStream aBuffer, LoadoutBase<?> aLoadout) {
        LoadoutOmniMech omniMech = (LoadoutOmniMech) aLoadout;
        int actuatorState = 0; // 8 bits for actuator toggle states.
        // All actuator states are encoded even if they don't exist on the equipped omnipod. Actuators that don't exist
        // are
        // encoded as false/0.
        actuatorState = (actuatorState << 1)
                | (omniMech.getComponent(Location.RightArm).getToggleState(ItemDB.LAA) ? 1 : 0);
        actuatorState = (actuatorState << 1)
                | (omniMech.getComponent(Location.RightArm).getToggleState(ItemDB.HA) ? 1 : 0);
        actuatorState = (actuatorState << 1)
                | (omniMech.getComponent(Location.LeftArm).getToggleState(ItemDB.LAA) ? 1 : 0);
        actuatorState = (actuatorState << 1)
                | (omniMech.getComponent(Location.LeftArm).getToggleState(ItemDB.HA) ? 1 : 0);
        aBuffer.write((byte) actuatorState);
    }

    private void readArmorValues(ByteArrayInputStream aBuffer, LoadoutBase<?> aLoadout, LoadoutBuilder aBuilder) {

        // Armor values next, RA, RT, RL, HD, CT, LT, LL, LA
        // 1 byte per armor value (2 for RT,CT,LT front first)
        for (Location part : Location.right2Left()) {
            if (part.isTwoSided()) {
                aBuilder.push(new OpSetArmor(null, aLoadout, aLoadout.getComponent(part), ArmorSide.FRONT, aBuffer
                        .read(), true));
                aBuilder.push(new OpSetArmor(null, aLoadout, aLoadout.getComponent(part), ArmorSide.BACK, aBuffer
                        .read(), true));
            }
            else {
                aBuilder.push(new OpSetArmor(null, aLoadout, aLoadout.getComponent(part), ArmorSide.ONLY, aBuffer
                        .read(), true));
            }
        }
    }

    private void writeArmorValues(ByteArrayOutputStream aBuffer, LoadoutBase<?> aLoadout) {
        for (Location part : Location.right2Left()) {
            if (part.isTwoSided()) {
                aBuffer.write((byte) aLoadout.getComponent(part).getArmor(ArmorSide.FRONT));
                aBuffer.write((byte) aLoadout.getComponent(part).getArmor(ArmorSide.BACK));
            }
            else {
                aBuffer.write((byte) aLoadout.getComponent(part).getArmor(ArmorSide.ONLY));
            }
        }
    }

    private LoadoutBase<?> readChassisLoadout(ByteArrayInputStream aBuffer) {

        // 16 bits contain chassis ID (Big endian, respecting RFC 1700)
        short chassisId = (short) (((aBuffer.read() & 0xFF) << 8) | (aBuffer.read() & 0xFF));
        ChassisBase chassis = ChassisDB.lookup(chassisId);

        if (chassis instanceof ChassisOmniMech) {
            return new LoadoutOmniMech(ComponentBuilder.getOmniPodFactory(), (ChassisOmniMech) chassis);
        }
        return new LoadoutStandard((ChassisStandard) chassis);
    }

    private void writeChassis(ByteArrayOutputStream aBuffer, LoadoutBase<?> aLoadout) {
        // 16 bits (BigEndian, respecting RFC 1700) contains chassis ID.
        short chassiId = (short) aLoadout.getChassis().getMwoId();
        if (chassiId != aLoadout.getChassis().getMwoId())
            throw new RuntimeException("Chassi ID was larger than 16 bits!");
        aBuffer.write((chassiId & 0xFF00) >> 8);
        aBuffer.write((chassiId & 0xFF));
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
        // generateStatsFromStock();
    }

    @SuppressWarnings("unused")
    private static void generateAllLoadouts() throws Exception {
        List<ChassisBase> chassii = new ArrayList<>(ChassisDB.lookup(ChassisClass.LIGHT));
        chassii.addAll(ChassisDB.lookup(ChassisClass.MEDIUM));
        chassii.addAll(ChassisDB.lookup(ChassisClass.HEAVY));
        chassii.addAll(ChassisDB.lookup(ChassisClass.ASSAULT));
        Base64LoadoutCoder coder = new Base64LoadoutCoder();
        OperationStack stack = new OperationStack(0);
        for (ChassisBase chassis : chassii) {
            LoadoutBase<?> loadout;
            if (chassis instanceof ChassisStandard)
                loadout = new LoadoutStandard((ChassisStandard) chassis);
            else
                loadout = new LoadoutOmniMech(ComponentBuilder.getOmniPodFactory(), (ChassisOmniMech) chassis);

            stack.pushAndApply(new OpLoadStock(chassis, loadout, null));
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

    @SuppressWarnings("unused")
    private static void generateStatsFromStock() throws Exception {
        Map<Integer, Integer> freqs = new HashMap<>();

        List<ChassisBase> chassii = new ArrayList<>(ChassisDB.lookup(ChassisClass.LIGHT));
        chassii.addAll(ChassisDB.lookup(ChassisClass.MEDIUM));
        chassii.addAll(ChassisDB.lookup(ChassisClass.HEAVY));
        chassii.addAll(ChassisDB.lookup(ChassisClass.ASSAULT));
        OperationStack stack = new OperationStack(0);

        List<Integer> idStats = new ArrayList<>();

        // Process items from all stock loadouts
        for (ChassisBase chassis : chassii) {
            final LoadoutBase<?> loadout;
            if (chassis instanceof ChassisStandard) {
                loadout = new LoadoutStandard((ChassisStandard) chassis);
            }
            else if (chassis instanceof ChassisOmniMech) {
                loadout = new LoadoutOmniMech(ComponentBuilder.getOmniPodFactory(), (ChassisOmniMech) chassis);
            }
            else {
                throw new RuntimeException("Unknown chassis type!");
            }
            stack.pushAndApply(new OpLoadStock(chassis, loadout, null));

            for (ConfiguredComponentBase component : loadout.getComponents()) {
                for (Item item : component.getItemsEquipped()) {
                    Integer f = freqs.get(item.getMwoId());
                    f = (f == null) ? 1 : f + 1;
                    freqs.put(item.getMwoId(), f);
                }
            }
        }

        // Add all item ids to the stats list
        for (Item item : ItemDB.lookup(Item.class)) {
            idStats.add(item.getMwoId());
        }

        // Process omnipods with equal probability
        for (OmniPod omniPod : OmniPodDB.all()) {
            // Constant frequency of 5, every omnipod appears at most once in the stocks.
            // But this is not representative.
            freqs.put(omniPod.getMwoId(), 5);
            idStats.add(omniPod.getMwoId());
        }

        // Process Pilot modules with equal probability
        for (PilotModule module : PilotModuleDB.lookup(PilotModule.class)) {
            // Constant frequency of 5, every omnipod appears at most once in the stocks.
            // But this is not representative.
            freqs.put(module.getMwoId(), 3);
            idStats.add(module.getMwoId());
        }

        // Add all unused IDs in the used ranges to the frequency map with frequency 1.
        Collections.sort(idStats);
        int start = -1;
        int last = 0;
        for (int i : idStats) {
            if (start == -1) {
                start = 1000 * (i / 1000);
            }
            else if (last + 1000 < i) {
                final int end = 1000 * (last / 1000) + 1000;
                for (int id = start; id < end; ++id) {
                    Integer f = freqs.get(id);
                    if (f == null)
                        freqs.put(id, 1);
                }
                System.out.println("Added range: [" + start + ", " + end + "],");
                start = 1000 * (i / 1000);
            }
            last = i;
        }

        // Some manual tweaks

        // 1) Swap DHS and SHS probability for IS mechs.
        {
            Integer shs = freqs.get(ItemDB.SHS.getMwoId());
            Integer dhs = freqs.get(ItemDB.DHS.getMwoId());
            freqs.put(ItemDB.SHS.getMwoId(), dhs);
            freqs.put(ItemDB.DHS.getMwoId(), shs);
        }

        // 2) The separators need to be accounted for.
        freqs.put(-1, chassii.size() * 8);

        for (Entry<Integer, Integer> entry : freqs.entrySet()) {
            String name;
            try {
                Item item = ItemDB.lookup(entry.getKey());
                name = item.getName();
            }
            catch (Throwable t) {
                try {
                    OmniPod omniPod = OmniPodDB.lookup(entry.getKey());
                    name = "omnipod for " + omniPod.getChassisSeries();
                }
                catch (Throwable t1) {
                    try {
                        PilotModule module = PilotModuleDB.lookup(entry.getKey());
                        name = module.getName();
                    }
                    catch (Throwable t2) {
                        name = "reserved id";
                    }
                }
            }

            System.out.println(entry.getKey() + " : " + entry.getValue() + " // " + name);
        }

        ObjectOutputStream out = new ObjectOutputStream(new FileOutputStream("resources/resources/coderstats_v3.bin"));
        out.writeObject(freqs);
        out.close();
    }
}
