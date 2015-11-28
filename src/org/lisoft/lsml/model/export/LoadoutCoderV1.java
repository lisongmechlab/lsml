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
package org.lisoft.lsml.model.export;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.lisoft.lsml.command.CmdAddItem;
import org.lisoft.lsml.command.CmdSetArmor;
import org.lisoft.lsml.command.CmdSetArmorType;
import org.lisoft.lsml.command.CmdSetGuidanceType;
import org.lisoft.lsml.command.CmdSetHeatSinkType;
import org.lisoft.lsml.command.CmdSetStructureType;
import org.lisoft.lsml.model.chassi.ArmorSide;
import org.lisoft.lsml.model.chassi.ChassisStandard;
import org.lisoft.lsml.model.chassi.Location;
import org.lisoft.lsml.model.datacache.ChassisDB;
import org.lisoft.lsml.model.datacache.ItemDB;
import org.lisoft.lsml.model.datacache.UpgradeDB;
import org.lisoft.lsml.model.export.garage.CompatibilityHelper;
import org.lisoft.lsml.model.item.HeatSink;
import org.lisoft.lsml.model.item.Item;
import org.lisoft.lsml.model.loadout.DefaultLoadoutFactory;
import org.lisoft.lsml.model.loadout.LoadoutBase;
import org.lisoft.lsml.model.loadout.LoadoutStandard;
import org.lisoft.lsml.model.modifiers.MechEfficiencyType;
import org.lisoft.lsml.model.upgrades.ArmorUpgrade;
import org.lisoft.lsml.model.upgrades.GuidanceUpgrade;
import org.lisoft.lsml.model.upgrades.HeatSinkUpgrade;
import org.lisoft.lsml.model.upgrades.StructureUpgrade;
import org.lisoft.lsml.util.CommandStack;
import org.lisoft.lsml.util.DecodingException;
import org.lisoft.lsml.util.EncodingException;
import org.lisoft.lsml.util.Huffman1;

/**
 * The first version of {@link LoadoutCoder} for LSML.
 * 
 * @author Emily Björk
 */
public class LoadoutCoderV1 implements LoadoutCoder {
    private static final int        HEADER_MAGIC = 0xAC;
    private final Huffman1<Integer> huff;

    public LoadoutCoderV1() {
        try (InputStream is = LoadoutCoderV1.class.getResourceAsStream("/resources/coderstats.bin");
                ObjectInputStream in = new ObjectInputStream(is);) {
            @SuppressWarnings("unchecked")
            Map<Integer, Integer> freqs = (Map<Integer, Integer>) in.readObject();
            huff = new Huffman1<Integer>(freqs, null);
        }
        catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public byte[] encode(final LoadoutBase<?> aLoadout) throws EncodingException {
        throw new EncodingException("Protocol version 1 encoding is no longer allowed.");
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
            // 16 bits contain chassi ID.
            short chassiId = (short) (((buffer.read() & 0xFF) << 8) | (buffer.read() & 0xFF)); // Big endian, respecting
                                                                                               // RFC
                                                                                               // 1700

            ChassisStandard chassis = (ChassisStandard) ChassisDB.lookup(chassiId);
            loadout = (LoadoutStandard) DefaultLoadoutFactory.instance.produceEmpty(chassis);

            boolean artemisIv = (upeff & (1 << 7)) != 0;
            boolean endoSteel = (upeff & (1 << 4)) != 0;
            boolean ferroFib = (upeff & (1 << 5)) != 0;
            boolean dhs = (upeff & (1 << 6)) != 0;
            GuidanceUpgrade guidance = artemisIv ? UpgradeDB.ARTEMIS_IV : UpgradeDB.STANDARD_GUIDANCE;
            StructureUpgrade structure = endoSteel ? UpgradeDB.ENDO_STEEL_STRUCTURE : UpgradeDB.STANDARD_STRUCTURE;
            ArmorUpgrade armor = ferroFib ? UpgradeDB.FERRO_FIBROUS_ARMOR : UpgradeDB.STANDARD_ARMOR;
            HeatSinkUpgrade heatSinks = dhs ? UpgradeDB.DOUBLE_HEATSINKS : UpgradeDB.STANDARD_HEATSINKS;

            stack.pushAndApply(new CmdSetGuidanceType(null, loadout, guidance));
            stack.pushAndApply(new CmdSetHeatSinkType(null, loadout, heatSinks));
            stack.pushAndApply(new CmdSetStructureType(null, loadout, structure));
            stack.pushAndApply(new CmdSetArmorType(null, loadout, armor));
            loadout.getEfficiencies().setEfficiency(MechEfficiencyType.COOL_RUN, (upeff & (1 << 3)) != 0, null);
            loadout.getEfficiencies().setEfficiency(MechEfficiencyType.HEAT_CONTAINMENT, (upeff & (1 << 2)) != 0, null);
            loadout.getEfficiencies().setEfficiency(MechEfficiencyType.SPEED_TWEAK, (upeff & (1 << 1)) != 0, null);
            loadout.getEfficiencies().setDoubleBasics((upeff & (1 << 0)) != 0, null);
        }

        // Armor values next, RA, RT, RL, HD, CT, LT, LL, LA
        // 1 byte per armor value (2 for RT,CT,LT front first)
        for (Location location : Location.right2Left()) {
            if (location.isTwoSided()) {
                stack.pushAndApply(new CmdSetArmor(null, loadout, loadout.getComponent(location), ArmorSide.FRONT,
                        buffer.read(), true));
                stack.pushAndApply(new CmdSetArmor(null, loadout, loadout.getComponent(location), ArmorSide.BACK,
                        buffer.read(), true));
            }
            else {
                stack.pushAndApply(new CmdSetArmor(null, loadout, loadout.getComponent(location), ArmorSide.ONLY,
                        buffer.read(), true));
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
            for (Location location : Location.right2Left()) {
                Integer v;
                List<Item> later = new ArrayList<>();
                while (!ids.isEmpty() && -1 != (v = ids.remove(0))) {
                    Item pItem = ItemDB.lookup(v);
                    Item item = CompatibilityHelper.fixArtemis(pItem, loadout.getUpgrades().getGuidance());
                    if (item instanceof HeatSink) {
                        later.add(item); // Add heat sinks last after engine has been added
                        continue;
                    }
                    stack.pushAndApply(new CmdAddItem(null, loadout, loadout.getComponent(location), item));
                }
                for (Item i : later) {
                    stack.pushAndApply(new CmdAddItem(null, loadout, loadout.getComponent(location), i));
                }
            }
        }
        return loadout;
    }

    @Override
    public boolean canDecode(byte[] aBitStream) {
        final ByteArrayInputStream buffer = new ByteArrayInputStream(aBitStream);
        return buffer.read() == HEADER_MAGIC;
    }
}
